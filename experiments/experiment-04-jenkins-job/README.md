# Experiment 4: Jenkins Build Job Integrated with Git

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System
**Job Name:** `QueueLess-Build`
**Target Repository:** `https://github.com/Gokul7176/QueueLess.git`
**Branch:** `main`

---

## 1. Aim
To configure a Jenkins Freestyle build job named `QueueLess-Build` connected to the official GitHub repository `https://github.com/Gokul7176/QueueLess.git`, automate code checkout on branch `main`, compile and execute the automated JUnit 5 test suite (17 baseline tests in v1.0.0, expanding to 33 tests in v1.1.0) using the project's native Maven Wrapper (`mvnw.cmd`), package the production application JAR, archive build artifacts, publish JUnit XML test reports, inspect console outputs, and verify quality gate failure detection.

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
   ```bat
   @echo off
   echo ===== Git version =====
   git --version

   echo ===== Java version =====
   java -version

   echo ===== Build, test and package =====
   call mvnw.cmd -B clean test package
   ```
   *(Note: The `call` prefix ensures that `mvnw.cmd` returns control to Jenkins so post-build actions and exit codes are recorded accurately without early process termination).*

### Step 6: Configure Post-build Actions (JUnit Test Reporting & Artifact Archiving)
1. Click **Add post-build action** -> Select **Publish JUnit test result report**.
   - In **Test report XMLs**, enter:
     ```
     target/surefire-reports/*.xml
     ```
2. Click **Add post-build action** -> Select **Archive the artifacts**.
   - In **Files to archive**, enter:
     ```
     target/*.jar,target/surefire-reports/*.xml
     ```
3. Click **Save** to store the job configuration.

---

## 5. Build Execution & Log Verification

### Build History Summary
The `QueueLess-Build` Freestyle project tracks sequential CI builds triggered against `origin/main`:

| Build # | Trigger Timestamp | Checkout Git Commit | Duration | Automated Tests | Build Result | Artifacts Archived |
|:---|:---|:---|:---|:---|:---|:---|
| **#1** | 12-Oct-2026 22:29 | `c038603f4a6d` | 1 min 22 s | 17 passed (0 failed, 0 skipped) | `SUCCESS` | Surefire XML reports |
| **#2** | 13-Oct-2026 13:18 | `8b8297d02712` | 52.34 s (52,342 ms) | 33 passed (0 failed, 0 skipped) | `SUCCESS` | JAR (`46.70 MiB`) + 4 XML reports |

---

### Build #2 Detailed Verification

1. **Trigger & SCM Checkout:**
   - Started by user `admin` via Jenkins UI **Build Now**.
   - Remote repository: `https://github.com/Gokul7176/QueueLess.git`
   - Target branch: `*/main`
   - Revision checked out: `8b8297d02712ef89e21caff5f3d0baf5c1a2669c` (`origin/main`)
   - Commit message: `"fix(pipeline): sanitize json release metadata and secure process token passing"`

2. **Environment Diagnostics & Tool Verification:**
   - **Git Version:** `git version 2.55.0.windows.2`
   - **Java Version:** `openjdk version "21.0.11" 2026-04-21 LTS` (Temurin-21.0.11+10, 64-Bit Server VM)
   - **Maven Goals Executed:** `call mvnw.cmd -B clean test package`

3. **Automated Test Results (33 Tests Executed, 0 Failures):**
   - `com.queueless.QueueControllerIntegrationTest`: 8 tests run, 0 failures, 0 errors, 0 skipped (20.64 s)
   - `com.queueless.QueueServiceTest`: 9 tests run, 0 failures, 0 errors, 0 skipped (2.057 s)
   - `com.queueless.ReleaseHistoryControllerIntegrationTest`: 10 tests run, 0 failures, 0 errors, 0 skipped (0.944 s)
   - `com.queueless.ReleaseHistoryServiceTest`: 6 tests run, 0 failures, 0 errors, 0 skipped (0.336 s)
   - **Overall Test Result:** `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0` (100% passing)

4. **Packaging & Artifact Archiving:**
   - Built JAR: `target/queueless-1.1.0.jar` (48,971,765 bytes / 46.70 MiB)
   - Archived Artifacts:
     - `target/queueless-1.1.0.jar`
     - `target/surefire-reports/TEST-com.queueless.QueueControllerIntegrationTest.xml` (34.50 KiB)
     - `target/surefire-reports/TEST-com.queueless.QueueServiceTest.xml` (29.10 KiB)
     - `target/surefire-reports/TEST-com.queueless.ReleaseHistoryControllerIntegrationTest.xml` (30.60 KiB)
     - `target/surefire-reports/TEST-com.queueless.ReleaseHistoryServiceTest.xml` (28.87 KiB)

5. **JUnit Test Trend Status:**
   - Test Result Trend updated dynamically in Jenkins UI from **17 passed** (#1) to **33 passed** (#2).
   - Test Health: 100% Sunny weather report, zero failures, zero regressions.

### Actual Build #2 Console Output Snippet:
```text
Started by user admin
Running as SYSTEM
Building in workspace C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build
[WS-CLEANUP] Deleting project workspace...
Cloning the remote Git repository
Cloning repository https://github.com/Gokul7176/QueueLess.git
 > git.exe init C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build # timeout=10
Fetching upstream changes from https://github.com/Gokul7176/QueueLess.git
 > git.exe --version # timeout=10
 > git --version # 'git version 2.55.0.windows.2'
 > git.exe fetch --tags --force --progress -- https://github.com/Gokul7176/QueueLess.git +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git.exe rev-parse "refs/remotes/origin/main^{commit}" # timeout=10
Checking out Revision 8b8297d02712ef89e21caff5f3d0baf5c1a2669c (refs/remotes/origin/main)
 > git.exe config core.sparsecheckout # timeout=10
 > git.exe checkout -f 8b8297d02712ef89e21caff5f3d0baf5c1a2669c # timeout=10
Commit message: "fix(pipeline): sanitize json release metadata and secure process token passing"
 > git.exe rev-list --no-walk 4ec99c15b43fd8e4070f594c82fafb9a7cd1e2cf # timeout=10
[QueueLess-Build] $ cmd /c call C:\WINDOWS\TEMP\jenkins5050843533870433493.bat
===== Git version =====
git version 2.55.0.windows.2
===== Java version =====
openjdk version "21.0.11" 2026-04-21 LTS
OpenJDK Runtime Environment Temurin-21.0.11+10 (build 21.0.11+10-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.11+10 (build 21.0.11+10-LTS, mixed mode, sharing)
===== Build, test and package =====
[INFO] Scanning for projects...
[INFO]
[INFO] -----------------------< com.queueless:queueless >-----------------------
[INFO] Building queueless 1.1.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ queueless ---
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ queueless ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 21 source files with javac [debug release 21] to target\classes
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ queueless ---
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ queueless ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 4 source files with javac [debug release 21] to target\test-classes
[INFO] --- maven-surefire-plugin:3.2.5:test (default-test) @ queueless ---
[INFO] Running com.queueless.QueueControllerIntegrationTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 20.64 s -- in com.queueless.QueueControllerIntegrationTest
[INFO] Running com.queueless.QueueServiceTest
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 2.057 s -- in com.queueless.QueueServiceTest
[INFO] Running com.queueless.ReleaseHistoryControllerIntegrationTest
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.944 s -- in com.queueless.ReleaseHistoryControllerIntegrationTest
[INFO] Running com.queueless.ReleaseHistoryServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.336 s -- in com.queueless.ReleaseHistoryServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] --- maven-jar-plugin:3.4.1:jar (default-jar) @ queueless ---
[INFO] Building jar: C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build\target\queueless-1.1.0.jar
[INFO] --- spring-boot-maven-plugin:3.3.0:repackage (repackage) @ queueless ---
[INFO] Replacing main artifact C:\ProgramData\Jenkins\.jenkins\workspace\QueueLess-Build\target\queueless-1.1.0.jar with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  47.973 s
[INFO] Finished at: 2026-10-13T13:19:14+05:30
[INFO] ------------------------------------------------------------------------
Archiving artifacts
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

The following screenshots from the Jenkins web interface have been captured, visually verified, and archived in [`docs/screenshots/experiment-04/`](../../docs/screenshots/experiment-04/):

- [x] [`exp04_01_jenkins_dashboard.png`](../../docs/screenshots/experiment-04/exp04_01_jenkins_dashboard.png): Jenkins dashboard showing `QueueLess-Build` listed among active jobs with a sunny weather report, blue/green status ball, and Build #2 metrics.
- [x] [`exp04_02_job_config_scm_step.png`](../../docs/screenshots/experiment-04/exp04_02_job_config_scm_step.png): Freestyle job configuration showing Git SCM (`*/main`), Windows batch build step (`call mvnw.cmd -B clean test package`), and post-build archiving/JUnit actions.
- [x] [`exp04_03_build_page.png`](../../docs/screenshots/experiment-04/exp04_03_build_page.png): Project status page for `QueueLess-Build` displaying Build #2, Test Result Trend graph (rising from 17 to 33 passed tests), and archived artifacts list.
- [x] [`exp04_04_console_output.png`](../../docs/screenshots/experiment-04/exp04_04_console_output.png): Build #2 Console Output verifying Git version, Java 21 LTS version, Maven test execution across all 4 suites, and packaging completion.
- [x] [`exp04_05_junit_test_report.png`](../../docs/screenshots/experiment-04/exp04_05_junit_test_report.png): Published JUnit test report for Build #2 showing 33 passed tests (0 failures, 0 skipped, +16 tests) across the `com.queueless` package.
- [x] [`exp04_06_archived_artifacts.png`](../../docs/screenshots/experiment-04/exp04_06_archived_artifacts.png): Build #2 execution page highlighting the "Build Artifacts" section listing `queueless-1.1.0.jar` (46.70 MiB) and Surefire XML test reports.

---

## 8. Common Errors and Troubleshooting

| Error Encountered | Root Cause | Exact Solution |
|:---|:---|:---|
| `The filename, directory name, or volume label syntax is incorrect` | Shell command syntax entered instead of Windows batch. | Select **Execute Windows batch command** (not Execute shell), and use `call mvnw.cmd`. |
| `Error: JAVA_HOME not found in your environment` | Jenkins Windows service runs under `LocalSystem` without user environment variables. | QueueLess's updated `mvnw.cmd` automatically auto-detects `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`. |
| `Cannot connect to repository: Permission denied` | Private repository with missing credentials. | Generate a GitHub Personal Access Token (classic) with `repo` scope and configure it under Jenkins Credentials. |
| `Port 8080 was already in use` | Trying to run Spring Boot on 8080 while Jenkins uses 8080. | QueueLess is explicitly pre-configured to port **8085** to eliminate port collisions. |

---

## 9. Connection to QueueLess Version History & Release Management

Experiment 4 establishes the Continuous Integration (CI) foundation for the Version History module:
- **Build Number Attribution:** Every Jenkins build run increments the official sequential build number (e.g., `#1`, `#2`). QueueLess Version History records this exact `jenkinsBuildNumber` and provides clickable links to the Jenkins run (`jenkinsBuildUrl`).
- **Automated Test Quality Gate:** Jenkins collects test metrics from `target/surefire-reports/*.xml`. If any of the 33 automated tests fail, the build fails immediately. Broken code is NEVER recorded as a deployed release.
- **Verification Procedure:**
  ```powershell
  # Step 1: Open the Jenkins build history
  # Navigate to: http://localhost:8080/job/QueueLess-Build/

  # Step 2: Note the latest successful build number (e.g., #2)

  # Step 3: Open QueueLess Version History Dashboard:
  # http://localhost:8085/#version-view
  # Observe the Jenkins Build # and Test Quality Gate badges reflecting CI execution!
  ```

---

## 10. Student Experiment Result Record

**Date of Execution:** 13-October-2026
**Student Name:** Gokul
**Course Code:** 23IT723 — DevOps Laboratory

**Result:**
A Jenkins Freestyle job named `QueueLess-Build` was successfully configured, integrated with GitHub repository `https://github.com/Gokul7176/QueueLess.git`, and verified on branch `main` (commit `8b8297d`). Execution of Build #2 demonstrated automated source checkout, JDK 21 LTS environment validation, clean test and package execution via `mvnw.cmd -B clean test package`, JAR packaging (`queueless-1.1.0.jar`, 46.70 MiB), artifact archiving, and JUnit test reporting across 33 test cases with 100% pass rate and zero failures (build duration: 52.34s). The job operated safely and independently without disrupting the running production container (`queueless-app` on port 8085) or database volume (`queueless-data`), successfully completing the experiment objectives.

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
