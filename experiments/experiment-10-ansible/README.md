# Experiment 10: Software Configuration Management Using Ansible

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To create an Ansible automation structure (`inventory.ini`, `deploy.yml`), explain Software Configuration Management (SCM) concepts (control node, managed node, inventory, playbooks, tasks, modules, and idempotence), validate playbook syntax, and execute an Ansible playbook (using Ubuntu on WSL2) to automate host provisioning, Docker setup, container deployment, and verification for QueueLess.

---

## 2. Requirements & Prerequisites
- **Control Node Environment:** Ubuntu 22.04 / 24.04 LTS running on **WSL2 (Windows Subsystem for Linux)**
- **Software on Control Node:** Ansible (core 2.14+), Python 3.10+, `pip`, `curl`
- **Managed Node:** `localhost` (connection: `local`) or remote Ubuntu server reachable over SSH
- **Playbook Directory:** `C:\queuless\ansible\` (mounted inside WSL as `/mnt/c/queuless/ansible`)

---

## 3. Theory

### What is Software Configuration Management (SCM)?
Software Configuration Management is the discipline of maintaining systems, software, and environments in a known, desired, consistent, and reproducible state over time. Rather than relying on error-prone manual server administration ("Snowflake Servers"), SCM tools practice **Infrastructure as Code (IaC)**.

### Ansible Architecture:

```
┌────────────────────────────────────────────────────────┐
│ ANSIBLE CONTROL NODE (Ubuntu on WSL2)                  │
│                                                        │
│ ┌──────────────────┐           ┌─────────────────────┐ │
│ │  inventory.ini   │           │     deploy.yml      │ │
│ │  (Managed Hosts) │           │ (Declarative Tasks) │ │
│ └─────────┬────────┘           └──────────┬──────────┘ │
│           │                               │            │
│           └───────────────┬───────────────┘            │
│                           ▼                            │
│                     Ansible Engine                     │
└───────────────────────────┬────────────────────────────┘
                            │ SSH / Local Connection
                            ▼
┌────────────────────────────────────────────────────────┐
│ MANAGED NODE (Target Server / Localhost)               │
│ - Installs Docker & System Dependencies                │
│ - Creates Deployment Directory (/opt/queueless)        │
│ - Launches Container: queueless-app (Port 8085)        │
│ - Executes Smoke Test Verification                     │
└────────────────────────────────────────────────────────┘
```

### Core Terminology:
1. **Control Node:** The system running Ansible. Does not require a database or long-running daemons.
2. **Managed Node:** Target systems managed by Ansible. No agent is required; Ansible connects over standard SSH or local process invocation.
3. **Inventory (`inventory.ini`):** A configuration file grouping target hosts and defining connection parameters and variables.
4. **Playbook (`deploy.yml`):** A human-readable YAML document mapping hosts to tasks.
5. **Task:** A discrete action within a play that pairs a human description with an Ansible module invocation.
6. **Module:** Built-in code units (e.g., `ansible.builtin.apt`, `ansible.builtin.file`, `ansible.builtin.command`, `ansible.builtin.uri`) that perform state enforcement.
7. **Idempotence:** The defining principle of configuration management: running an Ansible playbook once or a hundred times results in the exact same system state without unintended side effects.

---

## 4. Directory Structure and Configuration Files

### File 1: `ansible/inventory.ini`
```ini
# ==============================================================================
# QueueLess — Ansible Inventory File
# College DevOps Laboratory Project — 23IT723 (Experiment 10)
# ==============================================================================

[local]
# Local deployment target using Ubuntu on WSL2
localhost ansible_connection=local ansible_python_interpreter=/usr/bin/python3

[webservers]
# Remote managed node target (for college lab network)
# 192.168.1.100 ansible_user=ubuntu ansible_ssh_private_key_file=~/.ssh/id_rsa

[all:vars]
ansible_python_interpreter=/usr/bin/python3
app_name=queueless
app_version=1.1.0
app_port=8085
container_name=queueless-app
image_name=queueless:1.1.0-8b8297d
deploy_dir=/opt/queueless
```

### File 2: `ansible/deploy.yml`
```yaml
---
# ==============================================================================
# QueueLess — Ansible Deployment Playbook
# College DevOps Laboratory Project — 23IT723 (Experiment 10)
# Automates host provisioning, Docker setup, container lifecycle & verification
# ==============================================================================

- name: Deploy QueueLess Virtual Queue Management Application
  hosts: all
  become: yes
  vars:
    app_name: queueless
    app_version: "1.1.0"
    app_port: 8085
    container_port: 8085
    container_name: queueless-app
    image_name: "queueless:1.1.0-8b8297d"
    deploy_dir: /opt/queueless
    health_check_url: "http://localhost:{{ app_port }}/api/tickets/stats"
    version_check_url: "http://localhost:{{ app_port }}/api/version-history/current"
    release_token: "{{ lookup('ansible.builtin.env', 'RELEASE_TRACKER_TOKEN') | default(release_tracker_token | default(''), true) }}"

  tasks:
    # --------------------------------------------------------------------------
    # Task 1: Verify Host Connectivity
    # --------------------------------------------------------------------------
    - name: Test connectivity to target node
      ansible.builtin.ping:

    # --------------------------------------------------------------------------
    # Task 2: Create Deployment Directory Structure
    # --------------------------------------------------------------------------
    - name: Create application deployment directory
      ansible.builtin.file:
        path: "{{ deploy_dir }}"
        state: directory
        mode: '0755'

    # --------------------------------------------------------------------------
    # Task 3: Ensure System Dependencies & Prerequisites
    # --------------------------------------------------------------------------
    - name: Install required system packages
      ansible.builtin.apt:
        name:
          - curl
          - ca-certificates
          - python3
          - python3-pip
          - docker.io
        state: present
        update_cache: yes
      when: ansible_facts['os_family'] == "Debian"

    # --------------------------------------------------------------------------
    # Task 4: Ensure Docker Daemon is Active
    # --------------------------------------------------------------------------
    - name: Ensure Docker daemon service is started and enabled
      ansible.builtin.service:
        name: docker
        state: started
        enabled: yes
      ignore_errors: yes

    # --------------------------------------------------------------------------
    # Task 5: Pre-flight Safety Check (Token Discovery & No-Log Protection)
    # --------------------------------------------------------------------------
    - name: Query existing container for RELEASE_TRACKER_TOKEN if unset in environment
      ansible.builtin.shell: >
        docker inspect {{ container_name }} --format '{{range .Config.Env}}{{println .}}{{end}}' 2>/dev/null | grep '^RELEASE_TRACKER_TOKEN=' | head -n 1 | cut -d= -f2- || true
      register: container_env_token
      when: release_token | length == 0
      failed_when: false
      changed_when: false
      check_mode: false
      no_log: true

    - name: Establish effective release token
      ansible.builtin.set_fact:
        effective_release_token: "{{ release_token if (release_token | length > 0) else (container_env_token.stdout | default('')) }}"
      no_log: true

    - name: Validate that effective release token is present
      ansible.builtin.assert:
        that:
          - effective_release_token is defined
          - effective_release_token | length > 0
        fail_msg: >
          CRITICAL: RELEASE_TRACKER_TOKEN is not supplied in the environment and could not be discovered from the active container.
          To prevent creating an unauthenticated release environment, supply the token securely via:
          export RELEASE_TRACKER_TOKEN='...' or passing --extra-vars "release_tracker_token=..." with no_log.
        quiet: yes
      no_log: true

    # --------------------------------------------------------------------------
    # Task 6: Stop and Remove Existing Container (Preserving Volume)
    # --------------------------------------------------------------------------
    - name: Stop and remove existing QueueLess container if present
      ansible.builtin.command:
        cmd: "docker rm -f {{ container_name }}"
      register: remove_container_result
      failed_when: false
      changed_when: remove_container_result.rc == 0

    # --------------------------------------------------------------------------
    # Task 7: Run Application Container with Persistent Storage and Secure Token
    # --------------------------------------------------------------------------
    - name: Start QueueLess container
      ansible.builtin.command:
        cmd: >
          docker run -d
          --name {{ container_name }}
          --restart unless-stopped
          -p {{ app_port }}:{{ container_port }}
          -v queueless-data:/app/data
          -e SERVER_PORT={{ container_port }}
          -e RELEASE_TRACKER_TOKEN={{ effective_release_token }}
          {{ image_name }}
      register: docker_run_result
      changed_when: docker_run_result.rc == 0
      no_log: true

    # --------------------------------------------------------------------------
    # Task 8: Wait for Application Warm-up
    # --------------------------------------------------------------------------
    - name: Pause 10 seconds for Spring Boot container bootstrap
      ansible.builtin.pause:
        seconds: 10

    # --------------------------------------------------------------------------
    # Task 9: Healthcheck Smoke Test
    # --------------------------------------------------------------------------
    - name: Verify application endpoint is healthy
      ansible.builtin.uri:
        url: "{{ health_check_url }}"
        method: GET
        status_code: 200
        return_content: yes
      register: health_check
      retries: 5
      delay: 5
      until: health_check.status == 200

    # --------------------------------------------------------------------------
    # Task 10: Verify Version History Endpoint
    # --------------------------------------------------------------------------
    - name: Verify Version History endpoint reports active release
      ansible.builtin.uri:
        url: "{{ version_check_url }}"
        method: GET
        status_code: 200
        return_content: yes
      register: version_check
      retries: 5
      delay: 3
      until: version_check.status == 200

    # --------------------------------------------------------------------------
    # Task 11: Output Deployment Summary
    # --------------------------------------------------------------------------
    - name: Display deployment success information
      ansible.builtin.debug:
        msg:
          - "=========================================================="
          - "QueueLess Application Deployment Successful!"
          - "Container Name : {{ container_name }}"
          - "Image Tag      : {{ image_name }}"
          - "Access URL     : http://localhost:{{ app_port }}/"
          - "Health Status  : {{ health_check.json.message | default('OK') }}"
          - "Active Release : {{ version_check.json.data.deploymentId | default('N/A') }}"
          - "Git Commit     : {{ version_check.json.data.gitCommitShort | default('N/A') }}"
          - "=========================================================="
```

---

## 5. Step-by-Step Procedure (Ubuntu on WSL2)

### Step 1: Launch Ubuntu in WSL2
From Windows PowerShell or Command Prompt, launch the Ubuntu shell:
```powershell
wsl -d Ubuntu
```

### Step 2: Install Ansible on Ubuntu
Inside the WSL2 terminal, update package indices and install Ansible:
```bash
sudo apt update
sudo apt install -y ansible python3 python3-pip curl
```

### Step 3: Verify Ansible Installation
```bash
ansible --version
```

### Step 4: Navigate to Project Ansible Directory
In WSL2, Windows drives are mounted under `/mnt/`:
```bash
cd /mnt/c/queuless/ansible
ls -la
```

### Step 5: Test Node Ping Connectivity
```bash
ansible -i inventory.ini local -m ping
```
*Observed Output:*
```json
localhost | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
```

### Step 6: Validate Playbook Syntax
Before executing, check that YAML indentation and module parameters are valid:
```bash
ansible-playbook -i inventory.ini deploy.yml --syntax-check
```
*Observed Output:*
```text
playbook: deploy.yml
```

### Step 7: Execute Playbook Dry Run (Check Mode)
```bash
ansible-playbook -i inventory.ini deploy.yml --check
```
*Observed Output:*
```text
PLAY RECAP *********************************************************************
localhost                  : ok=10   changed=0    unreachable=0    failed=0    skipped=4    rescued=0    ignored=0
```

### Step 8: Execute Deployment Playbook
```bash
ansible-playbook -i inventory.ini deploy.yml
```
*Observed Output:*
```text
TASK [Display deployment success information] **********************************
ok: [localhost] => {
    "msg": [
        "==========================================================",
        "QueueLess Application Deployment Successful!",
        "Container Name : queueless-app",
        "Image Tag      : queueless:1.1.0-8b8297d",
        "Access URL     : http://localhost:8085/",
        "Health Status  : Operation successful",
        "Active Release : QueueLess-CICD-7",
        "Git Commit     : 8b8297d",
        "=========================================================="
    ]
}

PLAY RECAP *********************************************************************
localhost                  : ok=14   changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

---

## 6. Actual Verification Method

1. **Verify Ansible Play Recap**:
   Ansible reports `failed=0`, `unreachable=0`, and `changed=2` (container replacement and start):
   ```text
   PLAY RECAP *********************************************************************
   localhost                  : ok=14   changed=2    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
   ```

2. **Verify Container State**:
   ```bash
   docker ps --filter "name=queueless-app"
   ```
   *Observed Container State:*
   ```text
   CONTAINER ID   IMAGE                     STATUS                    PORTS                                         NAMES
   29ea59ae2cae   queueless:1.1.0-8b8297d   Up 23 seconds (healthy)   0.0.0.0:8085->8085/tcp, [::]:8085->8085/tcp   queueless-app
   ```

3. **Verify Application Health & Active Queue State**:
   ```bash
   curl -s http://localhost:8085/api/tickets/stats
   ```
   *Observed Response:*
   ```json
   {
     "success": true,
     "message": "Operation successful",
     "data": {
       "waitingCount": 2,
       "servingCount": 1,
       "completedCount": 1,
       "cancelledCount": 0,
       "totalCount": 4,
       "estimatedWaitMinutes": 10
     }
   }
   ```

4. **Verify Active Version History Endpoint**:
   ```bash
   curl -s http://localhost:8085/api/version-history/current
   ```
   *Observed Response:*
   ```json
   {
     "success": true,
     "message": "Operation successful",
     "data": {
       "version": "1.1.0",
       "gitCommit": "8b8297d02712ef89e21caff5f3d0baf5c1a2669c",
       "gitCommitShort": "8b8297d",
       "gitBranch": "origin/main",
       "commitMessage": "fix(pipeline): sanitize json release metadata and secure process token passing",
       "jenkinsBuildNumber": "7",
       "jenkinsBuildUrl": "http://localhost:8080/job/QueueLess-CICD/7/",
       "testResult": "PASSED (31/31 tests)",
       "deploymentStatus": "VERIFIED",
       "environment": "production",
       "dockerImageTag": "queueless:1.1.0-8b8297d",
       "pipelineJob": "QueueLess-CICD",
       "deploymentId": "QueueLess-CICD-7",
       "deployed": true
     }
   }
   ```

5. **Verify Database Persistence (`queueless-data`)**:
   Querying `GET /api/version-history` confirms that all previously recorded releases (Build #1 and Jenkins Build #7) remain preserved across container replacement without data loss.

6. **Verify Token Security & Release Authentication**:
   - Unauthorized POST to `/api/version-history` with an invalid token returns `HTTP 401 Unauthorized: Invalid or missing release authentication token`.
   - Authorized POST with valid `RELEASE_TRACKER_TOKEN` returns `HTTP 200/201 Success: Release deployment record successfully recorded`.
   - Security enforced: No secret tokens are hardcoded, printed to stdout, or recorded in Ansible logs (`no_log: true`).

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  Ansible connects to the local target node via Ubuntu WSL2, verifies system packages and Docker daemon, validates the presence of `RELEASE_TRACKER_TOKEN`, re-creates `queueless-app` using the versioned Docker image `queueless:1.1.0-8b8297d` while preserving persistent volume `queueless-data`, verifies application health (`/api/tickets/stats`) and version monitoring (`/api/version-history/current`), and reports `failed=0`.

- **Observed Result:**
  Playbook syntax validation passed (`playbook: deploy.yml`). Dry-run check mode passed with `ok=10`, `skipped=4`, `failed=0`. Full deployment executed cleanly with `ok=14`, `changed=2`, `failed=0`. The container booted in 10 seconds, health check returned HTTP 200, version history returned active release `QueueLess-CICD-7` (commit `8b8297d`), persistent queue data was preserved, and release-tracking security remained fully functional.

---

## 8. Screenshot Checklist

- [ ] `exp10_01_ansible_version.png`: Output of `ansible --version` in Ubuntu WSL2 terminal.
- [ ] `exp10_02_ansible_ping.png`: Output of `ansible -i inventory.ini all -m ping` showing `"ping": "pong"`.
- [ ] `exp10_03_syntax_check.png`: Output of `ansible-playbook --syntax-check` confirming valid YAML.
- [ ] `exp10_04_playbook_execution.png`: Live execution of all 9 tasks with colored output (changed=yellow, ok=green).
- [ ] `exp10_05_play_recap_success.png`: PLAY RECAP summary showing `failed=0` and debug task output showing Access URL.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `ansible-playbook: command not found` | Attempting to run Ansible directly inside Windows PowerShell instead of Ubuntu WSL. | Launch Ubuntu WSL first: `wsl -d Ubuntu`, then execute commands inside the Linux environment. |
| `ERROR! syntax error while parsing YAML` | Improper indentation, tabs instead of spaces, or unquoted variables (`{{ ... }}`). | YAML requires spaces (not tabs). Quote variable expressions: `url: "{{ health_check_url }}"`. |
| `Permission denied (publickey)` | Missing SSH keys when connecting to remote managed node. | Generate SSH key with `ssh-keygen` and copy public key to target: `ssh-copy-id ubuntu@<ip>`. |
| `Failed to connect to docker daemon` | Docker Desktop integration with WSL2 is not enabled. | Open Docker Desktop Settings -> Resources -> WSL Integration -> Enable toggle for "Ubuntu". |

---

---

## 10. Connection to QueueLess Version History & Release Management

Experiment 10 automates the configuration management and infrastructure provisioning layer that hosts QueueLess releases:
- **Consistent Environment Provisioning:** Ansible guarantees that target host machines have required packages, Docker runtime, persistent volume configurations (`queueless-data:/app/data`), and appropriate port mappings.
- **Automated Configuration Audit:** Ansible tasks query both the healthcheck endpoint (`/api/tickets/stats`) and the Version History endpoint (`GET /api/version-history/current`) to programmatically verify that the deployed host is actively running the desired release version and Git commit.
- **Verification Procedure:**
  ```bash
  # Inside Ubuntu WSL2 terminal:
  cd /mnt/c/queuless/ansible

  # Run the playbook
  ansible-playbook -i inventory.ini deploy.yml

  # Query the deployed version metadata
  curl -s http://localhost:8085/api/version-history/current
  ```

---

## 11. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
Ansible Software Configuration Management was installed and configured on Ubuntu WSL2. The inventory and declarative deployment playbook were created, validated with `--syntax-check`, and executed. Automated provisioning, persistent volume container orchestration, and application health and version verification were successfully achieved with zero failures.

---

## 12. Viva Questions and Answers

1. **Why is Ansible referred to as an "Agentless" configuration management tool?**
   *Answer:* Unlike Chef or Puppet which require installing and maintaining background daemon agents on every managed node, Ansible requires only standard SSH (or WinRM on Windows) and Python on the managed host. The control node pushes tasks and self-executing Python modules over SSH and cleans them up immediately after execution.

2. **What does "Idempotency" mean in configuration management, and why is it important?**
   *Answer:* An operation is idempotent if executing it multiple times produces the exact same end state without performing unnecessary duplicate operations. For example, if a package or user already exists, an idempotent Ansible task does nothing (`ok`), avoiding downtime, configuration drift, and redundant changes.

3. **What is an Ansible Inventory file and what formats are supported?**
   *Answer:* The inventory file defines the list of target hosts that Ansible manages, grouped logically (e.g., `[webservers]`, `[databases]`), along with connection credentials and group variables. Supported formats include INI (`inventory.ini`) and YAML (`inventory.yml`), as well as dynamic inventory scripts for cloud environments (AWS, Azure, GCP).

4. **What is the difference between an Ansible Task, a Play, and a Playbook?**
   *Answer:*
   - A **Task** is a single actionable step utilizing an Ansible module (e.g., start a service).
   - A **Play** is a mapped set of tasks targeted to a specific host group.
   - A **Playbook** is the top-level YAML file that can contain one or more plays.

5. **What is the purpose of the `become: yes` directive in an Ansible playbook?**
   *Answer:* The `become` directive instructs Ansible to elevate privileges on the target node (typically via `sudo` on Linux) to execute administrative tasks requiring root access, such as installing system packages or restarting system services.
