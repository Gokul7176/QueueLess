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
        Jenkins -->|Stage 1| S1[Checkout Source]
        S1 -->|Stage 2| S2[Compile with Maven]
        S2 -->|Stage 3| S3[Automated Testing - JUnit 5]
        S3 -->|Quality Gate Pass| S4[Package Fat JAR]
        S3 -.->|Test Failure| Fail[Abort Pipeline & Notify]
        S4 -->|Stage 5| S5[Build Docker Image]
        S5 -->|Stage 6| S6[Deploy Docker Container]
        S6 -->|Stage 7| S7[HTTP Healthcheck Verification]
    end
    
    S7 --> DockerHost[5. Docker Engine Runtime (Port 8085)]
    DockerHost --> LiveApp[6. Functional Web App (Browser / REST)]
    
    subgraph SCM ["Configuration Management"]
        Ansible[7. Ansible Control Node (WSL2)] -->|deploy.yml| HostConfig[Host & Container Provisioning]
        HostConfig --> DockerHost
    end
```

---

## 2. Stage-by-Stage Toolchain Mapping

| Pipeline Phase | Tool / Technology | Configuration File | Role in the Ecosystem | Experiment Ref |
|:---|:---|:---|:---|:---|
| **Version Control** | Git 2.40+ & GitHub | `.gitignore` | Tracks source code revisions, facilitates team collaboration, isolated feature branches, and merge dispute resolution. | Exp 1, 2, 3 |
| **Build Automation** | Apache Maven 3.9+ | `pom.xml`, `mvnw` | Manages project dependencies, standardizes build phases (`compile`, `test`, `package`), and bundles the Spring Boot executable fat JAR. | Exp 4, 5, 9 |
| **Automated Testing** | JUnit 5 & Spring Test | `QueueServiceTest.java`, `QueueControllerIntegrationTest.java` | Executes 17 unit and integration tests. Serves as an automated quality gate that prevents broken releases from deploying. | Exp 5, 9 |
| **Continuous Integration** | Jenkins 2.400+ | `Jenkinsfile` | Automates multi-stage pipeline execution, archives build artifacts (`queueless-1.0.0.jar`), and renders graphical test trend dashboards. | Exp 4, 5 |
| **Containerization** | Docker & Dockerfile | `Dockerfile`, `.dockerignore` | Packages application code, Java JRE runtime, and system dependencies into an immutable, lightweight (~150MB) container image. | Exp 6, 7 |
| **Container Deployment** | Docker Engine | `docker run -p 8085:8085` | Deploys the isolated application container, maps network ports, monitors real-time logs, and enforces restart policies. | Exp 8 |
| **Configuration Mgmt** | Ansible (core 2.14+) | `ansible/inventory.ini`, `ansible/deploy.yml` | Declaratively configures host dependencies, manages Docker daemon state, and automates multi-node deployments with idempotency. | Exp 10 |

---

## 3. Automated Quality Gate Safeguard

A cornerstone of Continuous Delivery is that **no deployment occurs when tests fail**.

1. When a developer pushes code to `main`, Jenkins triggers Stage 1 (Checkout) and Stage 2 (Compile).
2. In Stage 3 (Run Automated Tests), Maven Surefire executes all 17 test cases.
3. If all 17 tests pass (`BUILD SUCCESS`):
   - The pipeline continues to Stage 4 (Package), Stage 5 (Docker Build), Stage 6 (Deploy Container), and Stage 7 (Verify).
4. If any test fails (`BUILD FAILURE`):
   - Surefire outputs the exact assertion failure and stack trace.
   - Stage 3 terminates with an error code.
   - Jenkins immediately **aborts** the remainder of the pipeline.
   - The Docker build and deployment stages are completely skipped.
   - The existing stable running container is left untouched, preventing customer-facing downtime.

---

## 4. Port Allocation Architecture

To prevent network port collisions in a multi-service development workstation:

| Service | Protocol | Port Assigned | Purpose |
|:---|:---|:---|:---|
| **Jenkins Automation Server** | HTTP | `8080` | Continuous Integration dashboard, agent communication, and pipeline execution. |
| **QueueLess Application** | HTTP | `8085` | Virtual queue customer portal, admin dashboard, and REST API. |
| **Docker Container (Internal)** | HTTP | `8085` | Internal listening port inside the `queueless-app` container. |
| **H2 Web Console** | HTTP | `8085/h2-console` | In-memory database inspection tool. |
