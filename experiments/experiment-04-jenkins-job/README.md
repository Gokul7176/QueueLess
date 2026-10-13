# Experiment 4: Jenkins Build Job Integrated with Git

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System
**Job Name:** `QueueLess-Build`
**Target Repository:** `https://github.com/Gokul7176/QueueLess.git`
**Branch:** `main`

---

## 1. Aim
To configure a Jenkins Freestyle build job named `QueueLess-Build` connected to the official GitHub repository `https://github.com/Gokul7176/QueueLess.git`, automate code checkout on branch `main`, compile and execute the 17 JUnit 5 automated test cases using the project's native Maven Wrapper (`mvnw.cmd`), publish JUnit XML test reports, inspect console outputs, and verify failure detection.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 11 (Host running Jenkins Windows Service on Port 8080)
- **Jenkins Version:** Jenkins 2.573+ (Running at `http://localhost:8080`)
- **JDK:** OpenJDK 21 LTS (`C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`)
- **Build Tool:** Project Maven Wrapper (`mvnw.cmd` with built-in JDK auto-detection)
- **Version Control:** Git 2.55+ installed at `C:\Program Files\Git\cmd\git.exe`
- **Application Port:** QueueLess runs on port **8085** to completely eliminate port conflicts with Jenkins on **8080**.

---

## 3. Theory

### What is a Jenkins Freestyle Project?
A Jenkins Freestyle project is a configurable automation job created through the Jenkins web interface. It provides a structured GUI to:
1. Define Source Code Management (SCM) triggers and repository endpoints.
2. Schedule automated polling (Poll SCM) or webhook triggers.
3. Execute operating-system-specific build steps (such as Windows Batch `bat` commands or Linux shell `sh`).
4. Execute automated test suites and parse standardized test output schemas (JUnit XML).
5. Enforce build failure criteria: if the compiler fails or any test assertion fails, the job immediately terminates with build status `FAILURE` (Red/Weather icon alert).

### Maven Wrapper in CI/CD Environments:
Instead of requiring Jenkins administrators to manually install and configure specific Maven tool versions under "Global Tool Configuration", QueueLess bundles the official cross-platform **Maven Wrapper** (`mvnw` for Linux/macOS and `mvnw.cmd` for Windows). The wrapper guarantees reproducible builds across any agent machine by downloading the exact required Maven binaries and automatically locating the active JDK installation.

---

## 4. Step-by-Step Jenkins Configuration Guide

Follow these exact steps in your web browser:

### Step 1: Open Jenkins Dashboard
1. Open Google Chrome or Microsoft Edge and navigate to:
   ```
   http://localhost:8080/
   ```
2. Log in using your Jenkins administrator credentials.

### Step 2: Create the Freestyle Job
1. On the left navigation sidebar, click **New Item**.
2. Enter item name: **`QueueLess-Build`**.
3. Select **Freestyle project**.
4. Click **OK** at the bottom of the page.

### Step 3: Configure Source Code Management (Git)
1. On the job configuration screen, scroll down to **Source Code Management**.
2. Select the **Git** radio button.
3. In the **Repository URL** field, enter:
   ```
   https://github.com/Gokul7176/QueueLess.git
   ```
4. **Credentials:** If the repository is public, leave as `- none -`. If private, select your GitHub Personal Access Token credential.
5. In **Branches to build**, set the **Branch Specifier**:
   ```
   */main
   ```

### Step 4: Configure Build Triggers (Optional)
- Check **GitHub hook trigger for GITScm polling** (for instant webhook builds) OR:
- Check **Poll SCM** and enter `H/5 * * * *` (polls every 5 minutes for new commits).

### Step 5: Configure Build Steps (Windows Batch Command)
1. Scroll down to the **Build Steps** section.
2. Click **Add build step** -> Select **Execute Windows batch command**.
3. In the **Command** text area, enter:
   ```cmd
   call mvnw.cmd clean compile test
   ```
   *(Note: The `call` prefix ensures that `mvnw.cmd` returns control to Jenkins so post-build actions and exit codes are recorded accurately).*

### Step 6: Configure Post-build Actions (JUnit Test Reporting)
1. Click **Add post-build action** -> Select **Publish JUnit test result report**.
2. In **Test report XMLs**, enter:
   ```
   target/surefire-reports/*.xml
   ```
3. Click **Save** to store the job configuration.

---

## 5. Build Execution & Log Verification

1. On the **`QueueLess-Build`** project page, click **Build Now** on the left menu.
2. Under **Build History**, click the flashing blue ball for **Build #1**.
3. Click **Console Output** to monitor the live build log.

### Expected Console Output Snippet:
```text
Started by user admin
Running as SYSTEM
Building in workspace C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build
[WS-CLEANUP] Deleting project workspace...
Cloning the remote Git repository
Cloning repository https://github.com/Gokul7176/QueueLess.git
 > git.exe init C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build
 > git.exe fetch --tags --force --progress -- https://github.com/Gokul7176/QueueLess.git +refs/heads/*:refs/remotes/origin/*
 > git.exe checkout -f refs/remotes/origin/main
Commit message: "ci: enhance Jenkinsfile and auto-detect JAVA_HOME in mvnw.cmd for Windows Jenkins agent"

[QueueLess-Build] $ cmd /c call mvnw.cmd clean compile test
[INFO] Scanning for projects...
[INFO] Building QueueLess 1.0.0
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ queueless ---
[INFO] Compiling 16 source files
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ queueless ---
[INFO] Running com.queueless.QueueControllerIntegrationTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.queueless.QueueServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
Recording test results
Finished: SUCCESS
```

---

## 6. Failure Detection & Quality Gate Verification

To demonstrate that `QueueLess-Build` properly reports failures when bugs or syntax errors occur:

1. In `src/test/java/com/queueless/QueueServiceTest.java`, edit a test assertion to fail:
   ```java
   assertEquals(TicketStatus.COMPLETED, createdTicket.getStatus()); // Intentionally wrong
   ```
2. Commit and push to GitHub:
   ```powershell
   git commit -am "test: intentional assertion failure"
   git push origin main
   ```
3. Trigger **Build Now** in Jenkins.
4. Verify that Jenkins outputs:
   ```text
   [ERROR] Failures:
   [ERROR]   QueueServiceTest.testJoinQueue_Success:54 expected: <COMPLETED> but was: <WAITING>
   [INFO] BUILD FAILURE
   Build step 'Execute Windows batch command' marked build as failure
   Finished: FAILURE
   ```
5. Revert the test assertion and push to restore stable green status.

---

## 7. Screenshot Checklist

Capture the following screenshots from your Jenkins interface for your lab record:

- [ ] `exp04_01_freestyle_item_creation.png`: New Item screen showing `QueueLess-Build` selected as Freestyle project.
- [ ] `exp04_02_git_scm_config.png`: Job configuration showing Git URL `https://github.com/Gokul7176/QueueLess.git` and branch `*/main`.
- [ ] `exp04_03_batch_build_step.png`: Build step showing `call mvnw.cmd clean compile test`.
- [ ] `exp04_04_junit_action_config.png`: Post-build Action showing `target/surefire-reports/*.xml`.
- [ ] `exp04_05_console_build_success.png`: Console Output page ending in `BUILD SUCCESS` and `Finished: SUCCESS`.
- [ ] `exp04_06_test_result_trend.png`: Jenkins Test Result Trend graph showing 17 passed tests with 0 failures.

---

## 8. Common Errors and Troubleshooting

| Error Encountered | Root Cause | Exact Solution |
|:---|:---|:---|
| `The filename, directory name, or volume label syntax is incorrect` | Shell command syntax entered instead of Windows batch. | Select **Execute Windows batch command** (not Execute shell), and use `call mvnw.cmd`. |
| `Error: JAVA_HOME not found in your environment` | Jenkins Windows service runs under `LocalSystem` without user environment variables. | QueueLess's updated `mvnw.cmd` automatically auto-detects `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`. |
| `Cannot connect to repository: Permission denied` | Private repository with missing credentials. | Generate a GitHub Personal Access Token (classic) with `repo` scope and configure it under Jenkins Credentials. |
| `Port 8080 was already in use` | Trying to run Spring Boot on 8080 while Jenkins uses 8080. | QueueLess is explicitly pre-configured to port **8085** to eliminate port collisions. |

---

---

## 9. Connection to QueueLess Version History & Release Management

Experiment 4 establishes the Continuous Integration (CI) foundation for the Version History module:
- **Build Number Attribution:** Every Jenkins build run increments the official sequential build number (e.g., `#1`, `#2`). QueueLess Version History records this exact `jenkinsBuildNumber` and provides clickable links to the Jenkins run (`jenkinsBuildUrl`).
- **Automated Test Quality Gate:** Jenkins collects test metrics from `target/surefire-reports/*.xml`. If any of the 31 automated tests fail, the build fails immediately. Broken code is NEVER recorded as a deployed release.
- **Verification Procedure:**
  ```powershell
  # Step 1: Open the Jenkins build history
  # Navigate to: http://localhost:8080/job/QueueLess-Build/

  # Step 2: Note the latest successful build number (e.g., #1)

  # Step 3: Open QueueLess Version History Dashboard:
  # http://localhost:8085/#version-view
  # Observe the Jenkins Build # and Test Quality Gate badges reflecting CI execution!
  ```

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
A Jenkins Freestyle job named `QueueLess-Build` was successfully created and connected to GitHub repository `https://github.com/Gokul7176/QueueLess.git`. Automated source checkout, compilation, test execution using `mvnw.cmd`, and JUnit test report publication were verified with zero failures, establishing CI build provenance for the QueueLess release tracking system.

---

## 11. Viva Questions and Answers

1. **What is the difference between a Jenkins Freestyle Project and a Jenkins Pipeline Project?**
   *Answer:* A Freestyle project is configured manually through the Jenkins GUI web forms and stores its configuration as an XML file on the Jenkins server. A Pipeline project is defined as code in a version-controlled `Jenkinsfile` (Pipeline-as-Code), allowing multi-stage workflows, parallel execution, and automated deployment pipelines tracked in Git.

2. **Why do we use `call mvnw.cmd` instead of just `mvnw.cmd` in a Windows batch build step?**
   *Answer:* In Windows Command Prompt (`cmd.exe`), executing another `.cmd` or `.bat` script without the `call` keyword causes the parent batch process to transfer execution permanently to the target script and terminate immediately upon completion, skipping all subsequent Jenkins steps. `call` ensures execution returns to Jenkins.

3. **What is the function of the Maven Surefire plugin and where does it output test reports?**
   *Answer:* The `maven-surefire-plugin` executes JUnit test classes during the `test` phase. It outputs machine-readable XML test reports to `target/surefire-reports/*.xml`, containing execution duration, passed tests, and failure stack traces.

4. **How does Jenkins know whether a build has Succeeded or Failed?**
   *Answer:* Jenkins monitors the exit code returned by the executed process. An exit code of `0` indicates success. Any non-zero exit code (such as exit code `1` returned by Maven when compilation or unit tests fail) marks the build as `FAILURE`.

5. **Why is the Maven Wrapper (`mvnw.cmd`) preferred over installing global Maven on the Jenkins server?**
   *Answer:* The Maven Wrapper bundles the exact Maven version required by the application within the project repository. It eliminates tool version discrepancies between developer laptops, CI build servers, and Docker containers, ensuring fully reproducible builds without requiring administrative tool configuration on Jenkins.
