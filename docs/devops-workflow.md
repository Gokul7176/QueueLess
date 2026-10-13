# QueueLess End-to-End DevOps Workflow

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  

---

## 1. Complete DevOps Life Cycle Architecture

The QueueLess project demonstrates a complete, closed-loop DevOps toolchain integrating Version Control, Build Automation, Continuous Integration, Automated Testing, Containerization, Container Deployment, and Software Configuration Management across all 10 laboratory experiments.

```mermaid
flowchart TD
    Dev[1. Developer Workstation] -->|git commit| GitLocal[2. Local Git Repo (C:\queuless)]
    GitLocal -->|git push origin main| GitHub[3. GitHub Remote Repository]
    GitHub -->|Webhook / Poll SCM| Jenkins[4. Jenkins CI/CD Automation Server]
    
    subgraph CI_Pipeline ["Jenkins Pipeline Workflow (Jenkinsfile)"]
        Jenkins -->|Stage 1| S1[Checkout Source & Extract Git Metadata]
        S1 -->|Stage 2| S2[Compile with Maven Wrapper]
        S2 -->|Stage 3| S3[Automated Testing - 31 JUnit 5 Tests]
        S3 -->|Quality Gate Pass| S4[Package Fat JAR]
        S3 -.->|Test Failure| Fail[Abort Pipeline & Prevent Deployment]
        S4 -->|Stage 5| S5[Build Versioned Docker Image]
        S5 -->|Stage 6| S6[Deploy Container with Volume Mount]
        S6 -->|Stage 7| S7[HTTP Healthcheck Verification]
        S7 -->|Stage 8| S8[Record Release in Version History API]
    end
    
    S8 --> DockerHost[5. Docker Engine Runtime (Port 8085, Volume /app/data)]
    DockerHost --> LiveApp[6. Functional Web App & Version History Dashboard]
    
    subgraph SCM ["Configuration Management"]
        Ansible[7. Ansible Control Node (WSL2)] -->|deploy.yml| HostConfig[Host & Container Provisioning]
        HostConfig --> DockerHost
    end
```

---

## 2. Stage-by-Stage Toolchain Mapping

| Pipeline Phase | Tool / Technology | Configuration File | Role in the Ecosystem | Experiment Ref |
|:---|:---|:---|:---|:---|
| **Version Control** | Git 2.40+ & GitHub | `.gitignore` | Tracks source code revisions, facilitates team collaboration, isolated feature branches, and establishes cryptographic SHA commit anchors. | Exp 1, 2, 3 |
| **Build Automation** | Apache Maven 3.9+ | `pom.xml`, `mvnw.cmd` | Manages project dependencies, standardizes build phases (`compile`, `test`, `package`), and bundles the Spring Boot executable fat JAR. | Exp 4, 5, 9 |
| **Automated Testing** | JUnit 5 & Spring Test | `QueueServiceTest.java`, `ReleaseHistoryServiceTest.java`, Controller tests | Executes 31 unit and integration tests. Serves as an automated quality gate that prevents broken releases from deploying. | Exp 5, 9 |
| **Continuous Integration** | Jenkins 2.400+ | `Jenkinsfile` | Automates multi-stage pipeline execution, archives build artifacts (`queueless-1.1.0.jar`, `release-metadata.json`), and renders test trend dashboards. | Exp 4, 5 |
| **Containerization** | Docker & Dockerfile | `Dockerfile`, `.dockerignore` | Packages application code, Java JRE runtime, persistent volume mount, and tags images with both `:latest` and `:${APP_VERSION}-${GIT_COMMIT}`. | Exp 6, 7 |
| **Container Deployment** | Docker Engine | `docker run -p 8085:8085 -v queueless-data:/app/data` | Deploys the isolated application container, maps network ports, attaches persistent data volume, and verifies health. | Exp 8 |
| **Release Auditing** | QueueLess REST API | `POST /api/version-history` | Stage 8 sends deployment provenance record (Git commit, branch, build number, test status) after healthcheck succeeds. | Exp 5, 8 |
| **Configuration Mgmt** | Ansible (core 2.14+) | `ansible/inventory.ini`, `ansible/deploy.yml` | Declaratively configures host dependencies, manages Docker daemon state, and automates multi-node deployments with idempotency. | Exp 10 |

---

## 3. Automated Quality Gate & Failure Protection

A cornerstone of Continuous Delivery is that **no deployment occurs when tests fail**:

1. When code is pushed to `main`, Jenkins triggers Stage 1 (Checkout & Git Metadata Extraction) and Stage 2 (Compile).
2. In Stage 3 (Run Automated Tests), Maven Surefire executes all 31 test cases (17 queue logic tests + 14 version history tests).
3. If all 31 tests pass (`BUILD SUCCESS`):
   - The pipeline continues through Packaging, Docker Build, Container Deployment, Healthcheck Verification, and Stage 8 (Record Release Metadata).
4. If any test fails (`BUILD FAILURE`):
   - Surefire outputs the exact assertion failure and stack trace.
   - Stage 3 terminates with an error code.
   - Jenkins immediately **aborts** the remainder of the pipeline.
   - Stages 4, 5, 6, 7, and 8 are completely skipped.
   - The previously running container remains active and untouched; broken builds are NEVER recorded as active releases in the Version History dashboard.

---

## 4. Controlled Rollback Procedure

When an operational rollback is required:
1. Identify previously verified releases using `GET /api/version-history` or `docker images queueless`.
2. Stop and remove the current container: `docker stop queueless-app && docker rm queueless-app`.
3. Start the previous known-good release image mounting the persistent volume:
   `docker run -d -p 8085:8085 -v queueless-data:/app/data --name queueless-app --restart unless-stopped queueless:<previous-tag>`
4. Verify health via `curl -f http://localhost:8085/api/tickets/stats`.
5. Rollback is confirmed only when the application healthcheck returns 200 OK.

---

## 4. Port Allocation Architecture

To prevent network port collisions in a multi-service development workstation:

| Service | Protocol | Port Assigned | Purpose |
|:---|:---|:---|:---|
| **Jenkins Automation Server** | HTTP | `8080` | Continuous Integration dashboard, agent communication, and pipeline execution. |
| **QueueLess Application** | HTTP | `8085` | Virtual queue customer portal, admin dashboard, and REST API. |
| **Docker Container (Internal)** | HTTP | `8085` | Internal listening port inside the `queueless-app` container. |
| **H2 Web Console** | HTTP | `8085/h2-console` | In-memory database inspection tool. |
