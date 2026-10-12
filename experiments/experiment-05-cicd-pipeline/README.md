# Experiment 5: CI/CD Pipeline in Jenkins

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  
**Pipeline Item Name:** `QueueLess-CICD`  
**Target Repository:** `https://github.com/Gokul7176/QueueLess.git`  
**Branch:** `main`  
**Pipeline Script:** `Jenkinsfile`  

---

## 1. Aim
To create, configure, and execute a complete 7-stage Continuous Integration and Continuous Delivery (CI/CD) Declarative Pipeline named `QueueLess-CICD` in Jenkins, pulling from `https://github.com/Gokul7176/QueueLess.git`, automating compilation, enforcing an automated test quality gate, packaging the Spring Boot fat JAR, building an immutable Docker container image, deploying the application container, verifying HTTP endpoint responsiveness, and archiving JUnit test reports.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 11 (Running Jenkins Windows Service at `http://localhost:8080`)
- **Docker Engine:** Docker Desktop (v24.x+) running with Docker daemon active
- **JDK:** OpenJDK 21 LTS (`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`)
- **Build Tool:** Project Maven Wrapper (`mvnw.cmd`)
- **Repository URL:** `https://github.com/Gokul7176/QueueLess.git`
- **Deployment Host Port:** `8085` (default) or configurable to `8086` to avoid conflicts.

---

## 3. Theory

### Declarative Pipeline as Code:
In enterprise DevOps, Continuous Delivery pipelines are defined as version-controlled code inside a `Jenkinsfile` rather than configured via fragile web forms.

### 7-Stage Architectural Pipeline Workflow:

```
[ GitHub: https://github.com/Gokul7176/QueueLess.git ]
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│ 1. CHECKOUT       : Clones repository commit on main   │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 2. COMPILE        : Compiles source (call mvnw.cmd)    │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 3. TEST           : Executes 17 JUnit 5 test cases     │ ───► [FAIL: Quality Gate HALTS Pipeline]
└──────────────────────────┬─────────────────────────────┘
                           ▼ (All Tests Pass)
┌────────────────────────────────────────────────────────┐
│ 4. PACKAGE        : Bundles target/queueless-1.0.0.jar │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 5. DOCKER BUILD   : Builds image queueless:latest      │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 6. DEPLOY         : Launches container on Port 8085    │
└──────────────────────────┬─────────────────────────────┘
                           ▼
┌────────────────────────────────────────────────────────┐
│ 7. VERIFY DEPLOY  : Polls http://localhost:8085/stats  │
└────────────────────────────────────────────────────────┘
```

### Safety Principles Implemented:
1. **Automated Quality Gate:** If any test fails in Stage 3, the pipeline terminates immediately. Downstream packaging and Docker deployment stages are skipped, preventing defective code from reaching production.
2. **Safe Container Management:** Stage 6 gracefully stops and removes existing containers with the same name (`docker stop queueless-app >nul 2>&1`), avoiding container name collision errors.
3. **True Application Healthcheck:** Stage 7 polls the live Spring Boot REST API (`/api/tickets/stats`) using a retry loop, verifying that the embedded Tomcat server is listening and database transactions succeed.

---

## 4. Step-by-Step Jenkins Configuration Guide

### Step 1: Open Jenkins Dashboard
1. Open your browser and navigate to:
   ```
   http://localhost:8080/
   ```
2. Log in with your Jenkins credentials.

### Step 2: Create the Pipeline Project
1. Click **New Item** on the left dashboard menu.
2. Enter item name: **`QueueLess-CICD`**.
3. Select **Pipeline** (not Freestyle).
4. Click **OK**.

### Step 3: Configure Pipeline Parameters (Optional / Recommended)
1. Check the box **This project is parameterized**.
2. Click **Add Parameter** -> **String Parameter**:
   - Name: `APP_PORT`
   - Default Value: `8085`
   - Description: `Host port to bind the QueueLess container (use 8086 if 8085 is in use)`
3. Click **Add Parameter** -> **String Parameter**:
   - Name: `CONTAINER_NAME`
   - Default Value: `queueless-app`

### Step 4: Configure Pipeline from SCM
1. Scroll down to the **Pipeline** configuration section.
2. In the **Definition** dropdown, select **Pipeline script from SCM**.
3. In the **SCM** dropdown, select **Git**.
4. In **Repository URL**, enter:
   ```
   https://github.com/Gokul7176/QueueLess.git
   ```
5. **Credentials:** Leave as `- none -` (for public repo) or select your GitHub Personal Access Token.
6. **Branch Specifier:**
   ```
   */main
   ```
7. **Script Path:**
   ```
   Jenkinsfile
   ```
8. Click **Save** to complete project configuration.

---

## 5. Pipeline Execution & Stage Monitoring

1. In the `QueueLess-CICD` project page, click **Build with Parameters** (or **Build Now**).
2. Review the parameters (`APP_PORT=8085`, `CONTAINER_NAME=queueless-app`) and click **Build**.
3. Under **Build History**, click **#1** to monitor progress.
4. Click **Pipeline Console** to observe the real-time stage execution.

### Expected Stage View Dashboard:

```
[1. Checkout]   [2. Compile]   [3. Test]   [4. Package]   [5. Docker Build]   [6. Deploy]   [7. Verify Deploy]
    1.2s            8.4s         18.2s         3.1s             4.5s             2.1s              11.2s
   SUCCESS         SUCCESS      SUCCESS       SUCCESS          SUCCESS          SUCCESS           SUCCESS
```

### Stage 7 Verification Output in Console Log:
```text
[Pipeline] { (7. Verify Deployment) }
Verifying application health and responsiveness at http://localhost:8085/api/tickets/stats...
[QueueLess-CICD] $ cmd /c powershell -NoProfile -ExecutionPolicy Bypass -Command ...
Verification successful: {"success":true,"message":"Operation successful","data":{"waitingCount":2,"servingCount":1,"completedCount":1,"cancelledCount":0,"totalCount":4,"estimatedWaitMinutes":10}}

[Pipeline] stage
[Pipeline] { (Declarative: Post Actions) }
Recording test results
Archiving artifacts
========================================================================
SUCCESS: QueueLess v1.0.0 was successfully built, tested, and deployed!
Access URL: http://localhost:8085/
========================================================================
Finished: SUCCESS
```

---

## 6. Actual Verification Method

1. **Jenkins Stage View:** All 7 stages display bright green tiles indicating success.
2. **Docker Container Verification:**
   ```powershell
   docker ps --filter "name=queueless-app"
   ```
   Output confirms `STATUS: Up (healthy)` and port mapping `0.0.0.0:8085->8085/tcp`.
3. **Application Verification:** Open `http://localhost:8085` in Google Chrome and verify that the QueueLess home page is live.
4. **API Verification:**
   ```powershell
   Invoke-RestMethod -Uri "http://localhost:8085/api/tickets/stats" -Method Get
   ```
5. **Quality Gate Halt Test:** Introduce an assertion failure in `QueueServiceTest.java`. Trigger a build. Confirm that the pipeline turns red at Stage 3 (**Test**) and that Stages 4, 5, 6, and 7 are automatically skipped.

---

## 7. Screenshot Checklist

Capture these visual evidence items for your laboratory record:

- [ ] `exp05_01_pipeline_job_creation.png`: New Item page showing `QueueLess-CICD` selected as Pipeline.
- [ ] `exp05_02_pipeline_scm_config.png`: Pipeline script from SCM configuration pointing to `https://github.com/Gokul7176/QueueLess.git` and `Jenkinsfile`.
- [ ] `exp05_03_stage_view_7_green.png`: Jenkins Pipeline Stage View matrix showing all 7 green blocks with execution durations.
- [ ] `exp05_04_docker_build_log.png`: Console output snippet showing `docker build -t queueless:latest .`.
- [ ] `exp05_05_deploy_verification_log.png`: Console output showing Stage 7 PowerShell healthcheck returning `"success":true`.
- [ ] `exp05_06_quality_gate_halt.png`: Stage View showing a failed test halting the pipeline at Stage 3 while Deploy is skipped.

---

## 8. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `docker is not recognized as an internal or external command` | Docker CLI not found by Jenkins agent user. | Ensure Docker Desktop is running and `C:\Program Files\Docker\Docker\resources\bin` is added to system PATH. |
| Stage 7 Fails with `Connection refused` | Spring Boot took longer than 10 seconds to warm up. | Stage 7 includes a 5-iteration retry loop with 3-second intervals to allow full embedded Tomcat bootstrap. |
| `Bind for 0.0.0.0:8085 failed: port is already allocated` | Another local process or container holds port 8085. | Pass build parameter `APP_PORT=8086` when clicking **Build with Parameters**, or stop the conflicting container. |
| `ERROR: Could not find ... in Global Tool Configuration` | Missing global tool definitions. | The QueueLess `Jenkinsfile` uses the self-contained project Maven Wrapper (`mvnw.cmd`), requiring zero manual tool configuration in Jenkins. |

---

## 9. Student Experiment Result Record

**Date of Execution:** ___________________  
**Student Name:** _______________________  
**Roll / Register No:** _________________  
**Staff Signature:** ____________________  

**Result:**  
A production-grade 7-stage CI/CD Declarative Pipeline named `QueueLess-CICD` was created and executed in Jenkins. Automated Git checkout, compilation, JUnit 5 continuous testing, artifact packaging, Docker containerization, local container deployment, and HTTP healthcheck smoke testing were successfully demonstrated and validated.

---

## 10. Viva Questions and Answers

1. **What is "Pipeline as Code" and why is it superior to manual Jenkins configuration?**  
   *Answer:* Pipeline as Code means defining the software delivery pipeline in a version-controlled text file (`Jenkinsfile`) stored in the project repository. It provides version history, pull-request code reviews for build modifications, instant disaster recovery, and reproducible CI/CD across environments.

2. **What is an "Automated Quality Gate" in Continuous Integration?**  
   *Answer:* A quality gate is an automated enforcement point in the pipeline (such as automated unit/integration tests or code coverage checks). If any test fails, the quality gate immediately fails the build and aborts all subsequent stages, ensuring that broken code is never packaged, containerized, or deployed.

3. **How does the Declarative Pipeline handle multi-platform agents (Windows vs Linux)?**  
   *Answer:* The Declarative Pipeline uses the Groovy `isUnix()` condition inside a `script { ... }` block. If `isUnix()` is true, it executes shell scripts (`sh`); if false (such as on a Windows host), it executes Windows batch commands (`bat 'call mvnw.cmd ...'`).

4. **Why is an application-level healthcheck (`/api/tickets/stats`) better than just checking `docker ps`?**  
   *Answer:* A container can be in the `running` state even if the application inside has crashed (e.g., Spring Boot failed to connect to database or crashed during Tomcat startup). An application-level HTTP healthcheck queries the live REST API, confirming that the entire application stack is truly functional.

5. **What is the difference between Continuous Integration, Continuous Delivery, and Continuous Deployment?**  
   *Answer:*  
   - **Continuous Integration (CI):** Automates building and testing code on every commit.  
   - **Continuous Delivery (CD):** Automatically packages and deploys passing builds to testing/staging environments, requiring a manual trigger to release to production.  
   - **Continuous Deployment:** Automatically releases every passing build directly to production with zero manual intervention.
