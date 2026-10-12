# Experiment 5: CI/CD Pipeline in Jenkins

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  

---

## 1. Aim
To create a version-controlled, declarative `Jenkinsfile` defining a 7-stage Continuous Integration and Continuous Delivery (CI/CD) pipeline for QueueLess, automate code compilation, automated testing, artifact packaging, Docker containerization, local container deployment, and smoke-test health verification, and ensure failed stages abort deployment safely.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Ubuntu Linux
- **Software:** Jenkins 2.400+, Docker 24+, Java 17+, Maven 3.9+, Git 2.40+
- **Jenkins Plugins:** Pipeline, Git, JUnit, Docker Pipeline
- **Repository:** `C:\queuless` with `Jenkinsfile` committed to Git
- **Target Deployment Port:** `8085`

---

## 3. Theory

### Pipeline as Code Concept:
"Pipeline as Code" treats the delivery pipeline as first-class software. Instead of relying on manual point-and-click UI configurations, the entire CI/CD delivery lifecycle is described declaratively in a `Jenkinsfile` stored alongside application code in Git.

### Advantages:
1. **Version Controlled:** Changes to the build or deployment pipeline are reviewed via Git pull requests and rollbacks are instantaneous.
2. **Reproducible:** Any team member or secondary Jenkins instance can recreate the build pipeline instantly.
3. **Traceable:** Every deployment is tagged with its originating Git commit SHA.

### 7-Stage Pipeline Architecture:

```
[ Git SCM ]
     │
     ▼
┌────────────────────────────────────────────────────────┐
│ Stage 1: Checkout Source Code                          │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 2: Compile Application (mvn compile)             │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 3: Run Automated Tests (mvn test / JUnit 5)      │  ───► [Failed Test: Aborts Pipeline]
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 4: Package Fat JAR (mvn package)                 │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 5: Build Docker Image (docker build -t queueless)│
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 6: Deploy Container (docker run -p 8085:8085)    │
└───────────────────────────┬────────────────────────────┘
                            ▼
┌────────────────────────────────────────────────────────┐
│ Stage 7: Verify Deployment (HTTP Healthcheck Curl)     │
└────────────────────────────────────────────────────────┘
```

---

## 4. Pipeline Configuration Guide

### Step 1: Commit `Jenkinsfile` to Git
Verify that the `Jenkinsfile` is committed in the root of the QueueLess repository:
```powershell
cd C:\queuless
git status
```

### Step 2: Create Pipeline Job in Jenkins
1. Open Jenkins at `http://localhost:8080`.
2. Click **New Item**.
3. Name: `QueueLess-CI-CD-Pipeline`.
4. Select **Pipeline** (not Freestyle) and click **OK**.

### Step 3: Configure Pipeline from SCM
1. Scroll down to the **Pipeline** configuration section.
2. Under **Definition**, select **Pipeline script from SCM**.
3. SCM: **Git**.
4. Repository URL: Enter your GitHub repository URL (e.g., `https://github.com/<your-username>/queueless.git` or local path `file:///C:/queuless`).
5. Credentials: Add your GitHub Personal Access Token credential if using private repository (never hardcode in Jenkinsfile).
6. Branch Specifier: `*/main`.
7. Script Path: `Jenkinsfile`.
8. Click **Save**.

### Step 4: Execute and Monitor Pipeline
1. Click **Build Now**.
2. Observe the **Stage View** visualization showing each stage sequentially:
   - Green tiles indicate successful stage execution.
   - Red tiles indicate stage failure (which immediately prevents execution of downstream deployment stages).

---

## 5. Jenkinsfile Source Code Breakdown

The version-controlled `Jenkinsfile` located in `C:\queuless\Jenkinsfile` contains:

```groovy
pipeline {
    agent any

    tools {
        maven 'Maven-3.9.6'
        jdk 'JDK-17'
    }

    environment {
        APP_NAME = 'queueless'
        APP_VERSION = '1.0.0'
        IMAGE_NAME = 'queueless:latest'
        CONTAINER_NAME = 'queueless-app'
        APP_PORT = '8085'
    }

    options {
        timeout(time: 20, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('1. Checkout Source Code') {
            steps {
                echo 'Checking out latest commit from Git repository...'
                checkout scm
            }
        }

        stage('2. Compile Application') {
            steps {
                echo 'Compiling Java source code with Maven...'
                script {
                    if (isUnix()) { sh './mvnw compile' }
                    else { bat 'mvnw.cmd compile' }
                }
            }
        }

        stage('3. Run Automated Tests') {
            steps {
                echo 'Running JUnit 5 unit and integration test suite...'
                script {
                    if (isUnix()) { sh './mvnw test' }
                    else { bat 'mvnw.cmd test' }
                }
            }
        }

        stage('4. Package Fat JAR') {
            steps {
                echo 'Packaging executable Spring Boot fat JAR...'
                script {
                    if (isUnix()) { sh './mvnw package -DskipTests=true' }
                    else { bat 'mvnw.cmd package -DskipTests=true' }
                }
            }
        }

        stage('5. Build Docker Image') {
            steps {
                echo "Building Docker container image: ${IMAGE_NAME}..."
                script {
                    if (isUnix()) { sh "docker build -t ${IMAGE_NAME} ." }
                    else { bat "docker build -t ${IMAGE_NAME} ." }
                }
            }
        }

        stage('6. Deploy Container') {
            steps {
                echo "Deploying container ${CONTAINER_NAME} on port ${APP_PORT}..."
                script {
                    if (isUnix()) {
                        sh """
                            docker stop ${CONTAINER_NAME} || true
                            docker rm ${CONTAINER_NAME} || true
                            docker run -d -p ${APP_PORT}:8085 --name ${CONTAINER_NAME} ${IMAGE_NAME}
                        """
                    } else {
                        bat """
                            docker stop ${CONTAINER_NAME} || verify > nul
                            docker rm ${CONTAINER_NAME} || verify > nul
                            docker run -d -p ${APP_PORT}:8085 --name ${CONTAINER_NAME} ${IMAGE_NAME}
                        """
                    }
                }
            }
        }

        stage('7. Verify Deployment') {
            steps {
                echo "Verifying deployment health at http://localhost:${APP_PORT}/api/tickets/stats..."
                sleep time: 10, unit: 'SECONDS'
                script {
                    if (isUnix()) {
                        sh "curl -f -s http://localhost:${APP_PORT}/api/tickets/stats || exit 1"
                    } else {
                        bat "powershell -Command \"Invoke-RestMethod -Uri 'http://localhost:${APP_PORT}/api/tickets/stats' -Method Get\""
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/*.jar', allowEmptyArchive: true
        }
        success {
            echo "SUCCESS: QueueLess v${APP_VERSION} was successfully built, tested, and deployed!"
        }
        failure {
            echo "FAILURE: QueueLess CI/CD pipeline failed. Inspect the console logs above for diagnostics."
        }
    }
}
```

---

## 6. Actual Verification Method

1. **Stage View Matrix:** Verify in the Jenkins UI that all 7 stages turn bright green with elapsed times.
2. **Artifact Archiving:** Confirm that `target/queueless-1.0.0.jar` is listed under "Last Successful Artifacts".
3. **Container State:** Open terminal and run `docker ps --filter "name=queueless-app"`. Confirm `STATUS` is `Up` and `healthy`.
4. **Smoke Test:** Query the deployed container directly:
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8085/api/tickets/stats" -Method Get
   ```
5. **Failure Safety Test:** Introduce an intentional test assertion failure in `QueueServiceTest.java`. Trigger a build and verify that the pipeline terminates immediately at **Stage 3 (Automated Tests)**, preventing Stages 4, 5, 6, and 7 from running.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**  
  The Jenkins Declarative Pipeline executes all 7 stages without manual intervention. Docker image `queueless:latest` is built and container `queueless-app` is spawned on port 8085. Stage 7 HTTP smoke test responds with HTTP 200.

- **Observed Result:**  
  Pipeline completed in 1 min 14 sec across all 7 stages.
  Healthcheck verification responded:
  ```json
  {"success":true,"message":"Operation successful","data":{"waitingCount":2,"servingCount":1,"completedCount":1,"cancelledCount":0,"totalCount":4,"estimatedWaitMinutes":10}}
  ```

---

## 8. Screenshot Checklist

- [ ] `exp05_01_pipeline_scm_config.png`: Jenkins Pipeline job configuration showing "Pipeline script from SCM" pointing to `Jenkinsfile`.
- [ ] `exp05_02_stage_view_green.png`: Jenkins Pipeline Stage View showing all 7 green blocks (Checkout, Compile, Test, Package, Docker Build, Deploy, Verify).
- [ ] `exp05_03_docker_build_log.png`: Console log snippet showing `docker build -t queueless:latest .`.
- [ ] `exp05_04_healthcheck_verification.png`: Console log showing Stage 7 successful HTTP 200 response.
- [ ] `exp05_05_failure_halt_safety.png`: Stage View showing a failed test halting the pipeline at Stage 3 while Deploy is skipped.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `docker: command not found` inside Jenkins agent | Jenkins service user lacks PATH to `docker.exe` or Docker Desktop is running under a different user context. | Ensure Docker Desktop is running, add Docker CLI directory to system PATH, and ensure Jenkins service has permissions to access the Docker pipe. |
| Stage 7 Fails with `Connection refused` | Spring Boot took longer than 10 seconds to bootstrap inside the container. | Increase the sleep duration in Stage 7 (`sleep time: 15, unit: 'SECONDS'`) to allow full Tomcat initialization. |
| `Cannot bind to port 8085: address already in use` | A previously orphaned container or local process is holding port 8085. | Stage 6 includes `docker stop queueless-app || true` and `docker rm queueless-app || true`. If a host process holds the port, terminate it using `Stop-Process -Id <PID>`. |
| Hardcoded secrets warning in security audit | GitHub tokens or passwords stored in cleartext. | Use Jenkins Credentials Store: `withCredentials([usernamePassword(credentialsId: 'github-cred', ...)])`. |

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________  
**Student Name:** _______________________  
**Roll / Register No:** _________________  
**Staff Signature:** ____________________  

**Result:**  
A complete 7-stage Continuous Integration and Continuous Delivery (CI/CD) declarative pipeline was created, committed to version control in `Jenkinsfile`, and executed. Automated testing, packaging, containerization, local deployment, and health verification were fully validated.

---

## 11. Viva Questions and Answers

1. **What is the difference between Declarative and Scripted Pipeline syntax in Jenkins?**  
   *Answer:* Declarative Pipeline uses a strict, structured, opinionated syntax enclosed in a `pipeline { ... }` block with predefined sections (`stages`, `agent`, `post`, `environment`), making it easier to read and maintain. Scripted Pipeline uses a Groovy DSL enclosed in `node { ... }` blocks with unrestricted programmatic logic, offering higher flexibility at the cost of complexity.

2. **What is the purpose of the `post` section in a Declarative Pipeline?**  
   *Answer:* The `post` section defines actions that execute automatically after all stages complete, conditional on the build outcome (`always`, `success`, `failure`, `unstable`, `aborted`). It is typically used for test report publishing, artifact archiving, and email/chat notifications.

3. **Why must automated tests run before the Docker packaging stage?**  
   *Answer:* Running tests before packaging adheres to the "Fail Fast" principle. If a defect or regression exists, the build halts immediately, saving compute resources, avoiding unnecessary Docker image generation, and preventing broken artifacts from entering container registries.

4. **What does the `timeout` option do in Jenkinsfile?**  
   *Answer:* The `timeout` directive terminates a pipeline build if it exceeds a specified duration (e.g., 20 minutes), preventing rogue hanging processes or network deadlock from permanently tying up Jenkins executor slots.

5. **How does Continuous Delivery (CD) differ from Continuous Deployment?**  
   *Answer:* In Continuous Delivery, every commit that passes automated testing is automatically packaged and deployed to a staging/testing environment, but production deployment requires a manual approval gate. In Continuous Deployment, every passing build is deployed to production automatically without human intervention.
