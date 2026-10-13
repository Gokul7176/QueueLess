# Comprehensive Laboratory Screenshot Checklist

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

This master checklist details all required visual evidence items to include in your printed or digital college DevOps Laboratory Record.

---

## Master Screenshot Verification Matrix

| Exp # | Experiment Title | Screenshot File Name | Content to Capture in Screenshot | Status |
|:---|:---|:---|:---|:---|
| **01** | Git Installation & Repositories | `exp01_01_git_version.png` | Terminal showing `git --version` and `git config --list`. | [x] |
| **01** | Git Installation & Repositories | `exp01_02_git_status_untracked.png` | Terminal showing `git status` with untracked files before staging. | [x] |
| **01** | Git Installation & Repositories | `exp01_03_git_commit.png` | Terminal output of `git commit` showing root commit SHA and file counts. | [x] |
| **01** | Git Installation & Repositories | `exp01_04_git_remote.png` | Terminal output of `git remote -v` displaying `origin` URLs. | [x] |
| **01** | Git Installation & Repositories | `exp01_05_github_repo_page.png` | Web browser showing GitHub repository page with file tree and commit message. | [x] |
| **02** | Fetch and Synchronize Repositories | `exp02_01_git_clone.png` | Terminal showing `git clone <URL> C:\queuless-clone` execution. | [x] |
| **02** | Fetch and Synchronize Repositories | `exp02_02_git_remote_v.png` | Terminal showing `git remote -v` on cloned repository. | [x] |
| **02** | Fetch and Synchronize Repositories | `exp02_03_git_fetch.png` | Terminal showing `git fetch origin` downloading remote refs. | [x] |
| **02** | Fetch and Synchronize Repositories | `exp02_04_git_diff_incoming.png` | Terminal showing `git diff HEAD..origin/main` before merging. | [x] |
| **02** | Fetch and Synchronize Repositories | `exp02_05_git_pull_fastforward.png` | Terminal showing `git pull origin main` completing fast-forward sync. | [x] |
| **03** | Branching and Merging | `exp03_01_git_branch_list.png` | Terminal showing `git branch` with feature branch created. | [x] |
| **03** | Branching and Merging | `exp03_02_feature_merge.png` | Terminal showing `git merge feature/priority-queue` clean merge. | [x] |
| **03** | Branching and Merging | `exp03_03_conflict_detected.png` | Terminal showing `CONFLICT (content)` message during merge. | [x] |
| **03** | Branching and Merging | `exp03_04_conflict_markers.png` | Code editor showing `<<<<<<< HEAD`, `=======`, and `>>>>>>>` markers. | [x] |
| **03** | Branching and Merging | `exp03_05_resolved_git_log_graph.png` | Terminal output of `git log --graph --oneline` showing converged merge commit. | [x] |
| **04** | Jenkins Build Job Integrated with Git | `exp04_01_jenkins_tools_config.png` | Jenkins Global Tool Configuration showing `JDK-17` and `Maven-3.9.6`. | [x] |
| **04** | Jenkins Build Job Integrated with Git | `exp04_02_job_scm_config.png` | Freestyle job configuration showing Git SCM repository URL. | [x] |
| **04** | Jenkins Build Job Integrated with Git | `exp04_03_job_maven_goals.png` | Freestyle build step showing Maven goals `clean compile test package`. | [x] |
| **04** | Jenkins Build Job Integrated with Git | `exp04_04_build_console_success.png` | Console Output ending with `BUILD SUCCESS` and `Finished: SUCCESS`. | [x] |
| **04** | Jenkins Build Job Integrated with Git | `exp04_05_junit_test_report_chart.png` | Jenkins Test Result Trend page showing 17 passed test cases. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_01_pipeline_scm_config.png` | Pipeline configuration showing "Pipeline script from SCM" pointing to `Jenkinsfile`. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_02_stage_view_green.png` | Jenkins Stage View dashboard showing all 7 green stages. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_03_docker_build_log.png` | Pipeline console log showing Stage 5 `docker build -t queueless:latest .`. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_04_healthcheck_verification.png` | Pipeline console log showing Stage 7 successful HTTP 200 verification. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_05_failure_halt_safety.png` | Pipeline Stage View showing failed test halting pipeline before deployment. | [x] |
| **06** | Installing Docker & Basic Commands | `exp06_01_docker_version.png` | Terminal showing `docker --version` and `docker info`. | [x] |
| **06** | Installing Docker & Basic Commands | `exp06_02_docker_pull.png` | Terminal showing `docker pull eclipse-temurin:17-jre-alpine` layer downloads. | [x] |
| **06** | Installing Docker & Basic Commands | `exp06_03_docker_images.png` | Terminal showing `docker images` table with repository, tag, and size. | [x] |
| **06** | Installing Docker & Basic Commands | `exp06_04_docker_ps_running.png` | Terminal showing `docker ps` with active container on port 8085. | [x] |
| **06** | Installing Docker & Basic Commands | `exp06_05_docker_lifecycle.png` | Terminal showing `docker stop`, `docker ps -a`, and `docker rm`. | [x] |
| **07** | Creating Containers from Dockerfile | `exp07_01_dockerfile_source.png` | Code editor showing completed `Dockerfile` and `.dockerignore`. | [x] |
| **07** | Creating Containers from Dockerfile | `exp07_02_docker_build_terminal.png` | Terminal output of `docker build -t queueless:latest .` completing all layers. | [x] |
| **07** | Creating Containers from Dockerfile | `exp07_03_docker_images_size.png` | Terminal showing `queueless:latest` image size in `docker images`. | [x] |
| **07** | Creating Containers from Dockerfile | `exp07_04_docker_run_container.png` | Terminal showing `docker run -d -p 8085:8085` returning container ID. | [x] |
| **07** | Creating Containers from Dockerfile | `exp07_05_browser_docker_app.png` | Web browser loading `http://localhost:8085` from the Docker container. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_01_docker_run_success.png` | Terminal showing container started in detached mode on port 8085. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_02_docker_ps_healthy.png` | Terminal showing `docker ps` status `Up (healthy)`. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_03_docker_logs_startup.png` | Terminal showing `docker logs` with Tomcat started on port 8085. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_04_browser_ui_running.png` | Browser window showing Customer Portal with ticket pass generated. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_05_admin_console_live.png` | Browser window showing Admin Console with live KPIs and tickets table. | [x] |
| **09** | Continuous Testing | `exp09_01_mvn_test_success.png` | Terminal showing `mvn test` output with 17 passed tests and `BUILD SUCCESS`. | [x] |
| **09** | Continuous Testing | `exp09_02_surefire_xml_files.png` | Directory listing of `target/surefire-reports/` showing XML reports. | [x] |
| **09** | Continuous Testing | `exp09_03_jenkins_test_trend.png` | Jenkins Test Result page showing test breakdown and duration chart. | [x] |
| **09** | Continuous Testing | `exp09_04_jenkins_failed_test_pipeline.png` | Jenkins Stage View showing Stage 3 red failure and Deploy stage skipped. | [x] |
| **09** | Continuous Testing | `exp09_05_jenkins_test_failure_details.png` | Jenkins Test Failure diagnosis showing assertion error and stack trace. | [x] |
| **05** | CI/CD Pipeline in Jenkins | `exp05_06_version_history_recording.png` | Pipeline console log showing Stage 8 deployment recorded via POST /api/version-history and artifact archiving. | [x] |
| **08** | Deploying & Managing App in Docker | `exp08_06_docker_volume_persistence.png` | Terminal showing `docker volume inspect queueless-data` ensuring database persistence across container restarts. | [x] |
| **09** | Continuous Testing | `exp09_06_version_history_test_suite.png` | Terminal showing `mvn test` executing all 31 unit and integration tests (including ReleaseHistory) with 0 failures. | [x] |
| **10** | Software Configuration Mgmt (Ansible) | `exp10_01_ansible_version.png` | Ubuntu WSL2 terminal showing `ansible --version`. | [x] |
| **10** | Software Configuration Mgmt (Ansible) | `exp10_02_ansible_ping.png` | Terminal showing `ansible -i inventory.ini all -m ping` returning pong. | [x] |
| **10** | Software Configuration Mgmt (Ansible) | `exp10_03_syntax_check.png` | Terminal showing `ansible-playbook deploy.yml --syntax-check` success. | [x] |
| **10** | Software Configuration Mgmt (Ansible) | `exp10_04_playbook_execution.png` | Terminal showing live execution of tasks with yellow/green status changes. | [x] |
| **10** | Software Configuration Mgmt (Ansible) | `exp10_05_play_recap_success.png` | Terminal showing PLAY RECAP with `failed=0` and deployed application URL. | [x] |
| **UI** | Application User Interface | `queueless_home_1791820357017.png` | Customer portal check-in screen with form inputs and navigation tabs. | [x] |
| **UI** | Application User Interface | `ticket_generated_1791820388523.png` | Digital ticket pass showing generated ticket number and queue position. | [x] |
| **UI** | Application User Interface | `admin_console_1791820406033.png` | Admin console displaying KPI metric cards and tickets data table. | [x] |
| **UI** | Application User Interface | `admin_console_called_1791820420882.png` | Admin console toast notification when next waiting customer is called. | [x] |
| **UI** | Application User Interface | `live_queue_board_1791820438046.png` | Live Public Display Board showing "Now Serving" counters and "Up Next". | [x] |
| **UI** | Version History Dashboard | `version_history_dashboard.png` | Version History UI dashboard displaying active release card, commit hash pill, branch, Jenkins build number, deployment status, and releases table. | [x] |
| **API**| Version History REST API | `version_history_api_response.png` | REST API response from `GET /api/version-history` showing JSON release metadata. | [x] |
