# QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

[![Java](https://img.shields.io/badge/Java-17%20%2F%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Apache_Maven-3.9.6-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.10-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Docker](https://img.shields.io/badge/Docker-24%2B-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-D24939?style=for-the-badge&logo=jenkins&logoColor=white)](https://www.jenkins.io/)
[![Ansible](https://img.shields.io/badge/Ansible-Automation-EE0000?style=for-the-badge&logo=ansible&logoColor=white)](https://www.ansible.com/)

**Course Code:** 23IT723 — DevOps Laboratory
**Academic Degree:** B.Tech / B.E. Information Technology
**Project Role:** Senior DevOps Engineer, Full-Stack Java Developer & Software Tester

---

## 1. Executive Summary

**QueueLess** is a fully functional, enterprise-grade virtual queue management system engineered specifically to address all **10 prescribed experiments** in the **23IT723 DevOps Laboratory syllabus**.

Rather than presenting an isolated frontend prototype or unverified scripts, QueueLess integrates:
- A responsive, modern single-page web application (Customer Portal, Administrator Console, Public Live Announcement Board, and **Version History Monitoring Dashboard**).
- A robust, layered Java 17+ and Spring Boot 3.3.4 backend with REST APIs and persistent H2 database storage (`./data/queuelessdb` and Docker volume `queueless-data:/app/data`).
- An automated test suite featuring **31 JUnit 5 and MockMvc test cases** enforcing FIFO order, atomic status transitions, input validation, and release metadata persistence with idempotency.
- Complete version control workflows in Git (tracking, feature branching, merge conflict resolution, and immutable commit tracking).
- Continuous Integration and Continuous Delivery via **Jenkins** (Freestyle jobs and an **8-stage Declarative Pipeline** with automated deployment health check and release recording).
- Containerization using a lightweight Alpine-based **Dockerfile** with non-root security, persistent database volumes, and automated container health checks.
- Automated Infrastructure as Code (IaC) and deployment orchestration using **Ansible**.

---

## 2. Technology Stack

| Domain | Technology / Tool | Version / Spec | Purpose in Ecosystem |
|:---|:---|:---|:---|
| **Frontend** | HTML5, CSS3, Vanilla ES6 JavaScript | Semantic, Responsive | Client check-in, live boarding pass, admin dispatch, live board, and **Version History Dashboard**. |
| **Backend** | Java OpenJDK & Spring Boot | Java 17 / 21, Spring Boot 3.3.4 | RESTful APIs (`/api/tickets`, `/api/version-history`), business logic, validation, static resource routing. |
| **Persistence** | H2 Database Engine | 2.x (Persistent File & In-Memory for Tests) | Persistent file storage at `./data/queuelessdb` mounted via Docker volume `queueless-data:/app/data`. |
| **Build Tool** | Apache Maven & Maven Wrapper | 3.9.6 (`mvn`, `mvnw`, `mvnw.cmd`)| Dependency resolution, compilation, packaging, and Surefire test reporting. |
| **Continuous Testing** | JUnit 5, Mockito, Spring Boot Test | JUnit Jupiter 5.10 | **31 unit and integration tests** acting as an automated CI quality gate. |
| **Version Control** | Git & GitHub | Git 2.40+ | Version control, branch isolation, commit history, and release provenance tracking. |
| **CI / CD Automation** | Jenkins Automation Server | Jenkins 2.400+ | Freestyle build jobs, JUnit parsing, and **8-stage Declarative Pipeline** with release recording. |
| **Containerization** | Docker Engine & Dockerfile | Docker 24+ | Multi-platform container packaging using `eclipse-temurin:17-jre-alpine` with version tagging. |
| **Configuration Mgmt** | Ansible | Core 2.14+ (Ubuntu on WSL2) | Automated provisioning, host dependency setup, volume mounting, and container deployment. |

---

## 3. Mandatory 10 DevOps Experiments Mapping Table

Each of the ten laboratory syllabus experiments is implemented, documented, and reproducible within this repository, with direct integration into the **QueueLess Version History & Release Management** ecosystem:

| Exp # | Syllabus Experiment Title | Primary Source Files & Configs | Role in Version History & Release Management | Lab Guide Location |
|:---:|:---|:---|:---|:---|
| **01** | Git Installation and Local/Remote Repositories | `.gitignore`, `.git/`, `DataInitializer.java` | Establishes root Git repository; records Git author metadata and short commit SHA displayed on Version History cards. | [Experiment 1 Guide](experiments/experiment-01-git/README.md) |
| **02** | Fetch and Synchronize Git Repositories | Git remote tracking refs (`origin/main`) | Synchronizes remote releases and ensures deployment pipeline checks out exact commit hashes for auditability. | [Experiment 2 Guide](experiments/experiment-02-sync/README.md) |
| **03** | Git Branching and Merging | `docs/queue-policy.txt`, Git merge history | Tracks feature branch origin and merge commit SHAs (`main` vs feature branches) in release audit logs. | [Experiment 3 Guide](experiments/experiment-03-branching/README.md) |
| **04** | Jenkins Build Job Integrated with Git | `pom.xml`, Jenkins Tool Config | SCM polling triggers automated builds, linking Jenkins build numbers directly to Git commit SHAs. | [Experiment 4 Guide](experiments/experiment-04-jenkins-job/README.md) |
| **05** | CI/CD Pipeline in Jenkins | `Jenkinsfile` (8 stages) | Executes 8-stage pipeline; Stage 8 securely registers release metadata via authenticated `POST /api/version-history`. | [Experiment 5 Guide](experiments/experiment-05-cicd-pipeline/README.md) |
| **06** | Installing Docker and Basic Commands | Docker Engine CLI | CLI inspection of versioned container runtime (`docker images`, `docker ps`, `docker inspect`). | [Experiment 6 Guide](experiments/experiment-06-docker-commands/README.md) |
| **07** | Creating Containers from Dockerfiles | `Dockerfile`, `.dockerignore` | Builds immutable container images tagged with version and Git commit hash (`queueless:1.1.0-c038603`). | [Experiment 7 Guide](experiments/experiment-07-dockerfile/README.md) |
| **08** | Deploying & Managing Application in Docker | Docker Runtime, `queueless-data` volume | Manages container lifecycle and mounts persistent volume `/app/data` to preserve release history across restarts. | [Experiment 8 Guide](experiments/experiment-08-docker-deployment/README.md) |
| **09** | Continuous Testing | `31 JUnit 5 Tests`, Surefire XML | Enforces quality gate in Stage 3; records test count and status (`PASSED`) in the Version History table. | [Experiment 9 Guide](experiments/experiment-09-continuous-testing/README.md) |
| **10** | Configuration Management Using Ansible | `ansible/inventory.ini`, `ansible/deploy.yml` | Automates release rollouts, volume mounting, and post-deployment version health check assertions. | [Experiment 10 Guide](experiments/experiment-10-ansible/README.md) |

---

## 4. Application Architecture & Features

QueueLess operates on a clear, layered Spring Boot architecture (`Controller` -> `Service` -> `Repository` -> `H2 Database`).

```
                ┌─────────────────────────────────────────────────────────────┐
                │                     CLIENT APPLICATIONS                     │
                │ Customer Portal • Admin Console • Live Board • Version View │
                └──────────────────────────────┬──────────────────────────────┘
                                               │ HTTP / JSON REST APIs
                                               ▼
                ┌─────────────────────────────────────────────────────────────┐
                │                 SPRING BOOT REST CONTROLLERS                │
                │      /api/tickets (GET, POST)  •  /api/version-history      │
                └──────────────────────────────┬──────────────────────────────┘
                                               │
                                               ▼
                ┌─────────────────────────────────────────────────────────────┐
                │                     SERVICE LOGIC LAYER                     │
                │  QueueService (FIFO)  •  ReleaseHistoryService (Idempotent) │
                └──────────────────────────────┬──────────────────────────────┘
                                               │
                                               ▼
                ┌─────────────────────────────────────────────────────────────┐
                │                   DATA ACCESS LAYER (JPA)                   │
                │      QueueTicketRepository  •  ReleaseHistoryRepository     │
                └──────────────────────────────┬──────────────────────────────┘
                                               │
                                               ▼
                ┌─────────────────────────────────────────────────────────────┐
                │                     H2 DATABASE STORAGE                     │
                │   Test: mem:queuelessdb  •  Prod: file:/app/data/queuelessdb│
                │             (Persisted via Docker Volume: queueless-data)   │
                └─────────────────────────────────────────────────────────────┘
```

### Module Features:
1. **Customer Module:**
   - Real-time registration form (Customer Name, Service Department selection).
   - Generates unique sequential ticket numbers (e.g., `QL-0001`, `QL-0002`).
   - Airport-boarding-pass style virtual ticket showing status (`WAITING`, `SERVING`, `COMPLETED`), queue position, and estimated wait time (~5 mins per person).
   - Instant ticket lookup and ticket cancellation.
2. **Admin Operations Module:**
   - Real-time KPI summary bar: Total Waiting, Currently Serving, Completed Today, Total Handled.
   - Active counter designation selector (`Counter 1`, `Counter 2`, `Counter 3`, `Desk 4`).
   - Single-click "Call Next Customer" button (respects FIFO order and transitions ticket to `SERVING`).
   - "Complete Ticket" action button (safely completes ticket and logs completion timestamp).
   - Filterable data table (All, Waiting, Serving, Completed).
   - Auto-refresh toggle (polls every 5s) and administrative queue reset button.
3. **Live Public Display Board:**
   - Waiting room display monitor with large "Now Serving" counter cards and "Up Next" waiting list.
4. **Version History Monitoring Dashboard:**
   - Live KPI cards: Active Deployed Version, Git Short Commit Hash with copy pill, Quality Gate status, and Total Releases.
   - Comprehensive release history table detailing version, Git commit & message, Git branch, Jenkins build number & build URL, automated test results, deployment environment & timestamp, Docker image tag, and deployment status.
   - Interactive filtering by status (All, Deployed, Failed) and live text search across commit messages, branches, and versions.
   - Persistent storage via file-backed H2 database mounted to Docker volume `queueless-data:/app/data`.
   - Token-secured REST endpoint (`POST /api/version-history`) for Jenkins CI/CD pipeline automation with full idempotency support.

---

## 5. Repository Structure

```
QueueLess/
├── .dockerignore                            # Excludes build context files from Docker
├── .gitignore                              # Git exclusion rules for Java/Maven/IDE and db files
├── Dockerfile                              # Production container definition (Alpine JRE + volume)
├── Jenkinsfile                             # Declarative 8-stage CI/CD pipeline with release recording
├── mvnw                                    # Linux / macOS Maven wrapper script
├── mvnw.cmd                                # Windows Maven wrapper batch script
├── pom.xml                                 # Maven dependencies, v1.1.0, and Surefire test configuration
├── README.md                               # Complete project documentation
│
├── ansible/                                # Experiment 10: Configuration Management
│   ├── deploy.yml                          # Deployment and orchestration playbook with volume mount
│   ├── inventory.ini                       # Managed host definitions and variables
│   └── README.md                           # Ansible WSL2 execution guide
│
├── docs/                                   # Project Architectural Documentation
│   ├── architecture.md                     # System design, state machine, and REST APIs
│   ├── devops-workflow.md                  # Comprehensive CI/CD workflow lifecycle
│   ├── screenshot-checklist.md             # Master screenshot verification checklist
│   ├── setup-guide.md                      # Detailed setup and installation instructions
│   └── troubleshooting.md                  # Common errors, port resolution, and FAQ
│
├── experiments/                            # All 10 Laboratory Experiment Manuals
│   ├── experiment-01-git/README.md         # Exp 1: Git Installation and Repositories
│   ├── experiment-02-sync/README.md        # Exp 2: Fetch and Synchronize Repositories
│   ├── experiment-03-branching/README.md   # Exp 3: Git Branching and Merging (Conflicts)
│   ├── experiment-04-jenkins-job/README.md # Exp 4: Jenkins Freestyle Build Job
│   ├── experiment-05-cicd-pipeline/README.md# Exp 5: Declarative CI/CD Pipeline
│   ├── experiment-06-docker-commands/README.md# Exp 6: Docker CLI Basic Commands
│   ├── experiment-07-dockerfile/README.md  # Exp 7: Containerization via Dockerfile
│   ├── experiment-08-docker-deployment/README.md# Exp 8: Docker App Deployment & Volumes
│   ├── experiment-09-continuous-testing/README.md# Exp 9: Continuous Testing (JUnit 5)
│   └── experiment-10-ansible/README.md     # Exp 10: SCM Using Ansible (WSL2)
│
├── src/
│   ├── main/
│   │   ├── java/com/queueless/
│   │   │   ├── QueueLessApplication.java   # Spring Boot Application entry point
│   │   │   ├── config/DataInitializer.java # Seeds initial queue tickets and baseline release record
│   │   │   ├── controller/
│   │   │   │   ├── QueueController.java    # Queue REST API endpoints
│   │   │   │   └── ReleaseHistoryController.java # Version history REST endpoints
│   │   │   ├── dto/
│   │   │   │   ├── CurrentVersionDTO.java  # Active deployed version response
│   │   │   │   ├── QueueRequest.java       # Ticket check-in request payload
│   │   │   │   └── ReleaseRecordRequest.java # Jenkins deployment payload
│   │   │   ├── exception/                  # Global exception handler & custom exceptions
│   │   │   ├── model/
│   │   │   │   ├── QueueTicket.java        # Ticket JPA Entity
│   │   │   │   ├── TicketStatus.java       # Ticket lifecycle enum
│   │   │   │   ├── ReleaseHistory.java     # Version history JPA Entity
│   │   │   │   └── DeploymentStatus.java   # Deployment status enum
│   │   │   ├── repository/
│   │   │   │   ├── QueueTicketRepository.java # Ticket Spring Data JPA repository
│   │   │   │   └── ReleaseHistoryRepository.java # Version history JPA repository
│   │   │   └── service/
│   │   │       ├── QueueService.java / QueueServiceImpl.java # Queue business logic
│   │   │       └── ReleaseHistoryService.java / ReleaseHistoryServiceImpl.java # Idempotent release tracking
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Server port 8085, persistent file H2, token config
│   │       └── static/                     # Frontend Web Assets (No Node.js needed)
│   │           ├── index.html              # Responsive single-page web app with Version tab
│   │           ├── css/style.css           # Design system, KPI cards, and responsive tables
│   │           └── js/app.js               # Tab navigation, real-time polling, and version view
│   │
│   └── test/
│       ├── java/com/queueless/
│       │   ├── QueueServiceTest.java           # 9 Unit tests with Mockito (FIFO, states)
│       │   ├── QueueControllerIntegrationTest.java # 8 Integration tests with MockMvc
│       │   ├── ReleaseHistoryServiceTest.java  # 6 Unit tests (idempotency, failure isolation)
│       │   └── ReleaseHistoryControllerIntegrationTest.java # 8 Integration tests (security, CRUD)
│       └── resources/
│           └── application.properties          # Isolated in-memory H2 configuration for tests
```

---

## 6. Build, Test, and Execution Guide

### Option 1: Native Execution with Maven

```powershell
# Step 1: Navigate to repository
cd C:\queuless

# Step 2: Execute automated test suite (31 tests)
.\mvnw.cmd test

# Step 3: Package Spring Boot executable fat JAR
.\mvnw.cmd clean package -DskipTests=true

# Step 4: Run application on port 8085
java -jar target\queueless-1.1.0.jar
```

Access the application in your browser at: **`http://localhost:8085/`**
Access the H2 Database Console at: **`http://localhost:8085/h2-console`**

---

### Option 2: Production Container Deployment with Docker

```powershell
# Step 1: Build the Docker image tagged with version and commit
docker build -t queueless:1.1.0-c038603 -t queueless:latest .

# Step 2: Run container mapped to port 8085 with persistent data volume
docker run -d -p 8085:8085 `
  --name queueless-app `
  -v queueless-data:/app/data `
  -e RELEASE_TRACKER_TOKEN="devops-secret-release-token-2026" `
  queueless:1.1.0-c038603

# Step 3: Verify container health and logs
docker ps --filter "name=queueless-app"
docker logs --tail 20 queueless-app

# Step 4: Verify live version endpoint via HTTP
curl.exe -s http://localhost:8085/api/version-history/current
```

#### Controlled Rollback Procedure:
If a newly deployed image exhibits runtime regression:
```powershell
# 1. Stop and remove the faulty container
docker stop queueless-app && docker rm queueless-app

# 2. Re-launch the verified previous image with the same persistent volume
docker run -d -p 8085:8085 `
  --name queueless-app `
  -v queueless-data:/app/data `
  queueless:1.0.0

# 3. Verify health before declaring rollback successful
curl.exe -s http://localhost:8085/actuator/health || curl.exe -s http://localhost:8085/api/tickets/stats
```

---

### Option 3: Automated CI/CD Pipeline via Jenkins

1. Open Jenkins at `http://localhost:8080`.
2. Configure **Global Tool Configuration** with `JDK-17` and `Maven-3.9.6`.
3. Add a Secret Text Credential named `queueless-release-token`.
4. Create a **New Item -> Pipeline** pointing to `Jenkinsfile` from Git.
5. Click **Build Now** to execute the 8 automated stages:
   - Stage 1: Checkout Source Code (records `GIT_COMMIT`, `GIT_BRANCH`, `GIT_COMMIT_MSG`)
   - Stage 2: Compile Application (`mvn compile`)
   - Stage 3: Run Automated Tests (enforces JUnit 5 quality gate)
   - Stage 4: Package Fat JAR (`mvn package -DskipTests=true`)
   - Stage 5: Build Docker Image (tagged `queueless:${APP_VERSION}-${GIT_SHORT_COMMIT}`)
   - Stage 6: Deploy Container (volume-mounted to `queueless-data:/app/data`)
   - Stage 7: Verify Deployment (HTTP 200 health check)
   - Stage 8: Record Deployment & Archive Metadata (posts to `/api/version-history` and archives `target/release-metadata.json`)

---

### Option 4: Software Configuration Management via Ansible

```bash
# Launch Ubuntu on WSL2
wsl -d Ubuntu

# Navigate to Ansible directory
cd /mnt/c/queuless/ansible

# Validate playbook syntax
ansible-playbook -i inventory.ini deploy.yml --syntax-check

# Execute deployment playbook
ansible-playbook -i inventory.ini deploy.yml
```

---

## 7. Automated Test Suite Verification

The test suite contains **31 automated test cases** executed via Maven Surefire:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.queueless.QueueControllerIntegrationTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.812 s
Running com.queueless.QueueServiceTest
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.320 s
Running com.queueless.ReleaseHistoryControllerIntegrationTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.154 s
Running com.queueless.ReleaseHistoryServiceTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.185 s

Results:
Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

- **Quality Gate Demonstration:** If any test fails, the build halts with `BUILD FAILURE`, Jenkins aborts the pipeline at Stage 3, and deployment is safely prevented.

---

## 8. Verification Evidence & Screenshots

All visual evidence has been captured and cross-referenced in the master checklist:
- Master Checklist: [`docs/screenshot-checklist.md`](docs/screenshot-checklist.md)
- Visual browser session recordings and captures saved in artifact storage:
  - `queueless_home_1791820357017.png` (Customer Check-in Portal)
  - `ticket_generated_1791820388523.png` (Live Ticket Pass)
  - `admin_console_1791820406033.png` (Admin Operations Dashboard)
  - `admin_console_called_1791820420882.png` (Live Desk Dispatch Announcement)
  - `live_queue_board_1791820438046.png` (Public Waiting Room Announcement Display)
  - `version_history_dashboard.png` (Version History Monitoring Dashboard)
  - `version_history_api_response.png` (REST API Release Metadata Endpoint)
  - `exp05_06_version_history_recording.png` (Jenkins Stage 8 Deployment Recording Log)
  - `exp08_06_docker_volume_persistence.png` (Docker Volume Persistence Verification)
  - `exp09_06_version_history_test_suite.png` (Maven 31-Test Suite Quality Gate Output)

---

## 9. Academic Submission & Laboratory Evaluation Details

- **College / Institution:** Department of Information Technology
- **Course Name & Code:** 23IT723 — DevOps Laboratory
- **Candidate / Student:** DevOps Engineering Student
- **Project Evaluation Status:** All 10 experiments verified, reproducible, and ready for viva-voce examination.
