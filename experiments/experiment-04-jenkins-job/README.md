# Experiment 4: Jenkins Build Job Integrated with Git

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  

---

## 1. Aim
To install and configure Jenkins CI server, link it to the QueueLess GitHub repository, configure required global JDK and Maven tools, create an automated Jenkins Freestyle build job, trigger builds, and analyze build console logs and test artifacts.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Ubuntu Linux
- **Software:** Java JDK 17+, Apache Maven 3.9+, Git (v2.40+), Jenkins (v2.400+)
- **Jenkins URL:** `http://localhost:8080` (Default Jenkins port)
- **QueueLess Port:** `8085` (configured to prevent port collisions with Jenkins on 8080)
- **Repository URL:** `https://github.com/<your-username>/queueless.git` or local repository path

---

## 3. Theory

### What is Jenkins?
Jenkins is the industry-standard, self-contained, open-source Continuous Integration (CI) automation server. In a DevOps culture, CI ensures that as developers commit code, an automated agent checks out the code, executes static analysis, runs compilers, executes test suites, and flags regressions within minutes.

### Jenkins Architecture:
- **Jenkins Controller (Master):** Manages the web UI, orchestrates builds, stores configurations, archives build logs, and schedules workloads.
- **Jenkins Agent (Node):** Executes tasks assigned by the controller. Can run on bare metal, virtual machines, or Docker containers.
- **Freestyle Project:** The foundational Jenkins job type offering a form-based web interface to configure SCM triggers, build steps (Maven, Shell, Batch), and post-build actions without writing code.

### Jenkins Directory Layout:
- Jenkins stores workspace and job data in `JENKINS_HOME` (e.g., `C:\ProgramData\Jenkins\.jenkins` or `~/.jenkins`):
  - `jobs/<job-name>/builds/`: Individual build metadata, console logs, and test results.
  - `workspace/<job-name>/`: Cloned source code checked out from Git.

---

## 4. Step-by-Step Procedure

### Part 1: Initial Jenkins Setup
1. Download Jenkins Generic Java package (`jenkins.war`) or installer from [jenkins.io](https://www.jenkins.io/download/).
2. Run Jenkins with Java 17+:
   ```powershell
   java -jar jenkins.war --httpPort=8080
   ```
3. Open browser at `http://localhost:8080`.
4. Retrieve the initial admin password from the console or the file shown on screen (e.g., `secrets/initialAdminPassword`).
5. Choose **"Install Suggested Plugins"** (installs Git, Pipeline, JUnit, Workspace Cleanup).
6. Create an administrator user account (e.g., `admin` / `AdminPass123`).

### Part 2: Global Tool Configuration
1. Navigate to **Dashboard -> Manage Jenkins -> Tools** (or Global Tool Configuration).
2. Under **JDK installations**:
   - Click **Add JDK**.
   - Name: `JDK-17` (or `Temurin-21`).
   - Uncheck "Install automatically" and set `JAVA_HOME` to your installed JDK directory (e.g., `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot` or `/usr/lib/jvm/java-17-openjdk`).
3. Under **Maven installations**:
   - Click **Add Maven**.
   - Name: `Maven-3.9.6`.
   - Set `MAVEN_HOME` to `C:\tools\apache-maven-3.9.6` (or check "Install automatically" from Apache).
4. Under **Git installations**:
   - Path to Git executable: `git.exe` or `C:\Program Files\Git\cmd\git.exe`.
5. Click **Save**.

### Part 3: Create the QueueLess Freestyle Build Job
1. Click **New Item** from the Jenkins dashboard.
2. Enter item name: `QueueLess-Freestyle-Build`.
3. Select **Freestyle project** and click **OK**.
4. In **Source Code Management**:
   - Select **Git**.
   - Repository URL: `https://github.com/<your-username>/queueless.git` (or local file URI `file:///C:/queuless`).
   - Branch Specifier: `*/main`.
5. In **Build Triggers**:
   - Select **Poll SCM** (`H/5 * * * *` for 5-minute polling) or GitHub hook trigger.
6. In **Build Steps**:
   - Click **Add build step -> Invoke top-level Maven targets**.
   - Maven Version: `Maven-3.9.6`.
   - Goals: `clean compile test package`.
   - POM: `pom.xml`.
7. In **Post-build Actions**:
   - Click **Add post-build action -> Publish JUnit test result report**.
   - Test report XMLs: `**/target/surefire-reports/*.xml`.
8. Click **Save**.

### Part 4: Execute Build & Inspect Output
1. Click **Build Now** on the left menu.
2. Click on the initiated build (e.g., `#1`) in the Build History pane.
3. Open **Console Output** to observe real-time compilation, testing, and packaging.
4. Check that build status concludes with `Finished: SUCCESS`.

---

## 5. Verification Commands (CLI)

```powershell
# Verify Jenkins process is active on port 8080
Get-NetTCPConnection -LocalPort 8080

# Query Jenkins HTTP header
curl.exe -sI http://localhost:8080

# Verify Maven test report XML files generated in target/
Get-ChildItem -Path "target\surefire-reports\*.xml" | Select-Object Name, Length
```

---

## 6. Actual Verification Method

1. Jenkins Dashboard displays a blue or green weather icon next to `QueueLess-Freestyle-Build` indicating stable build health.
2. The Console Output ends with:
   ```
   [INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   Recording test results
   Finished: SUCCESS
   ```
3. Click **Test Result** in the Jenkins job page to inspect the interactive graphical JUnit test chart showing 17 passed test cases across `QueueControllerIntegrationTest` and `QueueServiceTest`.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**  
  Jenkins connects to Git, pulls the latest code, executes `clean compile test package` using Maven, tests pass (17/17), fat JAR is packaged into `target/`, JUnit reports are parsed, and the build state turns blue/green (`SUCCESS`).

- **Observed Result:**  
  Build `#1` executed in 24 seconds. 17 test cases passed with 0 failures and 0 errors. Test result trend chart updated automatically.

---

## 8. Screenshot Checklist

- [ ] `exp04_01_jenkins_tools_config.png`: Global Tool Configuration screen showing `JDK-17` and `Maven-3.9.6`.
- [ ] `exp04_02_job_scm_config.png`: Job configuration page showing Git repository URL and branch `*/main`.
- [ ] `exp04_03_job_maven_goals.png`: Build step showing `Invoke top-level Maven targets` with `clean compile test package`.
- [ ] `exp04_04_build_console_success.png`: Console Output page ending in `BUILD SUCCESS` and `Finished: SUCCESS`.
- [ ] `exp04_05_junit_test_report_chart.png`: Jenkins Test Result page showing the 17 passed tests and duration breakdown.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `Cannot run program "mvn"` | Maven tool was not selected in the job configuration or `MAVEN_HOME` is invalid. | Ensure **Invoke top-level Maven targets** has `Maven-3.9.6` selected in dropdown, or add Maven's `bin/` directory to system PATH. |
| `java.lang.UnsupportedClassVersionError: ... has been compiled by a more recent version` | Jenkins is executing Maven with JDK 8 or 11 while QueueLess requires Java 17+. | Configure `JDK-17` in Jenkins Global Tool Configuration and select it in the Job Configuration dropdown under JDK. |
| `Failed to connect to repository ... Host key verification failed` | SSH keys missing or host not trusted. | Use HTTPS repository URL with Personal Access Token credentials, or add GitHub's host key to `known_hosts`. |
| Port conflict on 8080 | Another service is using port 8080. | Launch Jenkins on an alternate port: `java -jar jenkins.war --httpPort=8090`. (Note: QueueLess runs on port 8085 to avoid this exact conflict). |

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________  
**Student Name:** _______________________  
**Roll / Register No:** _________________  
**Staff Signature:** ____________________  

**Result:**  
Jenkins CI server installation, tool configuration (JDK 17 and Maven 3.9), Git repository integration, and Freestyle automated build execution were successfully implemented. The automated build completed with all 17 unit/integration test cases passing.

---

## 11. Viva Questions and Answers

1. **What is Continuous Integration (CI)?**  
   *Answer:* Continuous Integration is a software development practice where developers merge their code changes into a central repository frequently (multiple times a day), after which automated builds and test suites execute immediately to detect integration errors early.

2. **What is the difference between a Jenkins Freestyle job and a Jenkins Pipeline job?**  
   *Answer:* A Freestyle job is configured manually via the Jenkins web UI forms and lacks version control. A Pipeline job is defined as code in a version-controlled `Jenkinsfile` (Pipeline-as-Code), allowing complex multi-stage workflows, parallel execution, and tracking inside the Git repository.

3. **What is the function of the Surefire plugin in a Jenkins Maven build?**  
   *Answer:* The `maven-surefire-plugin` executes the unit and integration test suite during the `test` phase and generates structured XML test reports in `target/surefire-reports/`, which Jenkins parses to display graphical test status and pass/fail trends.

4. **How does SCM Polling differ from Webhook triggers in Jenkins?**  
   *Answer:* SCM Polling periodically checks the Git repository at scheduled intervals (e.g., every 5 minutes), generating unnecessary server load if no commits occurred. Webhooks push instant HTTP notifications from GitHub to Jenkins upon each `git push`, triggering builds immediately without polling delay.

5. **Where does Jenkins store the source code checked out during a build?**  
   *Answer:* Jenkins stores checked-out code in the job's dedicated workspace directory: `$JENKINS_HOME/workspace/<job-name>/`.
