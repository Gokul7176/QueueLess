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
- A responsive, modern single-page web application (Customer Portal, Administrator Console, and Public Live Announcement Board).
- A robust, layered Java 17+ and Spring Boot 3.3.4 backend with REST APIs and embedded H2 database persistence.
- An automated test suite featuring **17 JUnit 5 and MockMvc test cases** enforcing FIFO order, atomic status transitions, and input validation.
- Complete version control workflows in Git (tracking, feature branching, and merge conflict resolution).
- Continuous Integration and Continuous Delivery via **Jenkins** (Freestyle jobs and a version-controlled 7-stage `Jenkinsfile` Declarative Pipeline).
- Containerization using a lightweight Alpine-based **Dockerfile** with non-root security and automated container health checks.
- Automated Infrastructure as Code (IaC) and deployment orchestration using **Ansible**.

---

## 2. Technology Stack

| Domain | Technology / Tool | Version / Spec | Purpose in Ecosystem |
|:---|:---|:---|:---|
| **Frontend** | HTML5, CSS3, Vanilla ES6 JavaScript | Semantic, Responsive | Client check-in, live status boarding pass, admin desk dispatcher, real-time polling. |
| **Backend** | Java OpenJDK & Spring Boot | Java 17 / 21, Spring Boot 3.3.4 | RESTful APIs, business logic, validation, error handling, static resource routing. |
| **Persistence** | H2 Database Engine | 2.x (In-memory & File) | Embedded transactional data storage and live web administration console. |
| **Build Tool** | Apache Maven & Maven Wrapper | 3.9.6 (`mvn`, `mvnw`, `mvnw.cmd`)| Dependency resolution, compilation, packaging, and Surefire test reporting. |
| **Continuous Testing** | JUnit 5, Mockito, Spring Boot Test | JUnit Jupiter 5.10 | 17 unit and integration tests acting as an automated CI quality gate. |
| **Version Control** | Git & GitHub | Git 2.40+ | Version control, branch isolation, and multi-developer sync. |
| **CI / CD Automation** | Jenkins Automation Server | Jenkins 2.400+ | Freestyle build jobs, JUnit parsing, and 7-stage Declarative Pipeline. |
| **Containerization** | Docker Engine & Dockerfile | Docker 24+ | Multi-platform container packaging using `eclipse-temurin:17-jre-alpine`. |
| **Configuration Mgmt** | Ansible | Core 2.14+ (Ubuntu on WSL2) | Automated provisioning, host dependency setup, and container deployment. |

---

## 3. Mandatory 10 DevOps Experiments Mapping Table

Each of the ten laboratory syllabus experiments is implemented, documented, and reproducible within this repository:

| Exp # | Syllabus Experiment Title | Primary Source Files & Configs | Key Commands Executed | Lab Guide Location |
|:---:|:---|:---|:---|:---|
| **01** | Git Installation and Local/Remote Repositories | `.gitignore`, `.git/` | `git init -b main`, `git config`, `git add .`, `git commit`, `git remote add`, `git push` | [Experiment 1 Guide](experiments/experiment-01-git/README.md) |
| **02** | Fetch and Synchronize Git Repositories | Git remote tracking refs | `git clone`, `git remote -v`, `git fetch origin`, `git diff HEAD..origin/main`, `git pull` | [Experiment 2 Guide](experiments/experiment-02-sync/README.md) |
| **03** | Git Branching and Merging | `docs/queue-policy.txt` | `git switch -c feature/...`, `git merge`, `git log --graph --oneline` (Conflict simulation & fix) | [Experiment 3 Guide](experiments/experiment-03-branching/README.md) |
| **04** | Jenkins Build Job Integrated with Git | `pom.xml`, Jenkins Tool Config | Jenkins Freestyle Job, Maven goals `clean compile test package`, JUnit XML publisher | [Experiment 4 Guide](experiments/experiment-04-jenkins-job/README.md) |
| **05** | CI/CD Pipeline in Jenkins | `Jenkinsfile` | 7-Stage Declarative Pipeline: Checkout, Compile, Test, Package, Docker Build, Deploy, Healthcheck | [Experiment 5 Guide](experiments/experiment-05-cicd-pipeline/README.md) |
| **06** | Installing Docker and Basic Commands | Docker Engine CLI | `docker --version`, `pull`, `images`, `run`, `ps`, `ps -a`, `logs`, `stop`, `start`, `rm` | [Experiment 6 Guide](experiments/experiment-06-docker-commands/README.md) |
| **07** | Creating Containers from Dockerfiles | `Dockerfile`, `.dockerignore` | `docker build -t queueless:latest .`, Alpine JRE base, non-root `appuser`, `HEALTHCHECK` | [Experiment 7 Guide](experiments/experiment-07-dockerfile/README.md) |
| **08** | Deploying & Managing Application in Docker | Docker Runtime, Port 8085 | `docker run -d -p 8085:8085 --name queueless-app`, live log streaming, container lifecycle | [Experiment 8 Guide](experiments/experiment-08-docker-deployment/README.md) |
| **09** | Continuous Testing | `QueueServiceTest.java`, `QueueControllerIntegrationTest.java` | `mvn test`, 17 JUnit 5 tests, Surefire XML reports, automated pipeline failure halting | [Experiment 9 Guide](experiments/experiment-09-continuous-testing/README.md) |
| **10** | Configuration Management Using Ansible | `ansible/inventory.ini`, `ansible/deploy.yml` | `ansible -m ping`, `ansible-playbook --syntax-check`, `ansible-playbook deploy.yml` (WSL2) | [Experiment 10 Guide](experiments/experiment-10-ansible/README.md) |

---

## 4. Application Architecture & Features

QueueLess operates on a clear, layered Spring Boot architecture (`Controller` -> `Service` -> `Repository` -> `H2 Database`).

```
                ┌──────────────────────────────────────────────┐
                │             CLIENT APPLICATIONS              │
                │ Customer Portal • Admin Console • Live Board │
                └──────────────────────┬───────────────────────┘
                                       │ HTTP / JSON REST APIs
                                       ▼
                ┌──────────────────────────────────────────────┐
                │          SPRING BOOT REST CONTROLLER         │
                │        /api/tickets (GET, POST, CORS)        │
                └──────────────────────┬───────────────────────┘
                                       │
                                       ▼
                ┌──────────────────────────────────────────────┐
                │              QUEUE SERVICE LAYER             │
                │  FIFO Ordering • Position Calc • Wait Times  │
                └──────────────────────┬───────────────────────┘
                                       │
                                       ▼
                ┌──────────────────────────────────────────────┐
                │             DATA ACCESS LAYER (JPA)          │
                │           QueueTicketRepository (H2)         │
                └──────────────────────────────────────────────┘
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

---

## 5. Repository Structure

```
QueueLess/
├── .dockerignore                            # Excludes build context files from Docker
├── .gitignore                              # Git exclusion rules for Java/Maven/IDE
├── Dockerfile                              # Production container definition (Alpine JRE)
├── Jenkinsfile                             # Declarative 7-stage CI/CD pipeline
├── mvnw                                    # Linux / macOS Maven wrapper script
├── mvnw.cmd                                # Windows Maven wrapper batch script
├── pom.xml                                 # Maven dependencies and Surefire test configuration
├── README.md                               # Complete project documentation
│
├── ansible/                                # Experiment 10: Configuration Management
│   ├── deploy.yml                          # Deployment and orchestration playbook
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
│   │   │   ├── config/DataInitializer.java # Seeds initial demonstration tickets
│   │   │   ├── controller/QueueController.java # REST API endpoints
│   │   │   ├── dto/                        # Request / Response Transfer Objects
│   │   │   ├── exception/                  # Global exception handler & custom exceptions
│   │   │   ├── model/                      # JPA Entities (QueueTicket, TicketStatus)
│   │   │   ├── repository/                 # Spring Data JPA Repository
│   │   │   └── service/                    # Business logic and queue algorithms
│   │   │
│   │   └── resources/
│   │       ├── application.properties      # Server port 8085, H2 settings, JPA config
│   │       └── static/                     # Frontend Web Assets (No Node.js needed)
│   │           ├── index.html              # Responsive single-page web application
│   │           ├── css/style.css           # Modern design system & styles
│   │           └── js/app.js               # Vanilla JavaScript application controller
│   │
│   └── test/java/com/queueless/
│       ├── QueueServiceTest.java           # 9 Unit tests with Mockito (FIFO, states, edge cases)
│       └── QueueControllerIntegrationTest.java# 8 Integration tests with MockMvc (REST API, validation)
```

---

## 6. Build, Test, and Execution Guide

### Option 1: Native Execution with Maven

```powershell
# Step 1: Navigate to repository
cd C:\queuless

# Step 2: Execute automated test suite (17 tests)
mvn test
# (Or using Maven wrapper: .\mvnw.cmd test)

# Step 3: Package Spring Boot executable fat JAR
mvn clean package -DskipTests=true

# Step 4: Run application on port 8085
java -jar target\queueless-1.0.0.jar
```

Access the application in your browser at: **`http://localhost:8085/`**  
Access the H2 Database Console at: **`http://localhost:8085/h2-console`**

---

### Option 2: Production Container Deployment with Docker

```powershell
# Step 1: Build the Docker image
docker build -t queueless:latest .

# Step 2: Run container mapped to port 8085
docker run -d -p 8085:8085 --name queueless-app queueless:latest

# Step 3: Verify container health and logs
docker ps --filter "name=queueless-app"
docker logs --tail 20 queueless-app

# Step 4: Verify live endpoint via HTTP
curl.exe -s http://localhost:8085/api/tickets/stats
```

---

### Option 3: Automated CI/CD Pipeline via Jenkins

1. Open Jenkins at `http://localhost:8080`.
2. Configure **Global Tool Configuration** with `JDK-17` and `Maven-3.9.6`.
3. Create a **New Item -> Pipeline** pointing to `Jenkinsfile` from Git.
4. Click **Build Now** to execute the 7 automated stages:
   - Stage 1: Checkout Source Code
   - Stage 2: Compile Application
   - Stage 3: Run Automated Tests (JUnit 5)
   - Stage 4: Package Fat JAR
   - Stage 5: Build Docker Image
   - Stage 6: Deploy Container (Port 8085)
   - Stage 7: Verify Deployment (HTTP Healthcheck)

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

The test suite contains **17 automated test cases** executed via Maven Surefire:

```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.queueless.QueueControllerIntegrationTest
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.420 s
Running com.queueless.QueueServiceTest
Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.446 s

Results:
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
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
  - `queueless_ui_verification.webp` (Full interactive browser session recording)

---

## 9. Academic Submission & Laboratory Evaluation Details

- **College / Institution:** Department of Information Technology
- **Course Name & Code:** 23IT723 — DevOps Laboratory
- **Candidate / Student:** DevOps Engineering Student
- **Project Evaluation Status:** All 10 experiments verified, reproducible, and ready for viva-voce examination.
