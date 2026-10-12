# Ansible Configuration Management Guide for QueueLess

## 1. Overview and Theory

Ansible is an open-source IT automation engine that automates cloud provisioning, configuration management, application deployment, and intra-service orchestration. It uses an **agentless** architecture over SSH (or local IPC) and declarative YAML playbooks.

### Key Architectural Concepts:
- **Control Node**: The machine where Ansible is installed and commands (`ansible`, `ansible-playbook`) are executed.
- **Managed Node (Host)**: The target system(s) being configured.
- **Inventory (`inventory.ini`)**: A file specifying the hostnames, IP addresses, groups, and connection parameters for managed nodes.
- **Playbook (`deploy.yml`)**: An ordered list of tasks written in YAML that maps declarations to state changes.
- **Modules**: Reusable, standalone scripts that Ansible runs on your behalf (e.g., `apt`, `file`, `service`, `command`, `uri`).
- **Idempotence**: Running the playbook multiple times produces the same desired system state without redundant modifications.

---

## 2. Prerequisites & Environment Setup

On a Windows development workstation, the recommended Ansible environment is **Ubuntu on WSL2 (Windows Subsystem for Linux)**.

### Step 1: Open Ubuntu Terminal in WSL2
```bash
wsl -d Ubuntu
```

### Step 2: Install Ansible & Python Dependencies
```bash
sudo apt update
sudo apt install -y ansible python3 python3-pip curl
```

### Step 3: Verify Ansible Installation
```bash
ansible --version
```
Expected output displays: `ansible [core 2.14+]` or later with Python 3.

---

## 3. Directory Layout

```
ansible/
├── inventory.ini    # Host definitions and connection variables
├── deploy.yml       # Deployment and orchestration playbook
└── README.md        # Execution and architecture documentation
```

---

## 4. Execution Procedures

Navigate to the project's Ansible directory from within WSL:

```bash
cd /mnt/c/queuless/ansible
```

### Step 1: Validate Playbook Syntax
Before running, verify that the YAML formatting and module names are error-free:
```bash
ansible-playbook -i inventory.ini deploy.yml --syntax-check
```
*Expected Result:*
```
playbook: deploy.yml
```

### Step 2: Execute Dry-Run (Check Mode)
To inspect tasks without applying persistent modifications:
```bash
ansible-playbook -i inventory.ini deploy.yml --check
```

### Step 3: Execute the Deployment Playbook
Run the playbook to configure the environment and deploy QueueLess:
```bash
ansible-playbook -i inventory.ini deploy.yml
```

---

## 5. Playbook Task Breakdown

| Task # | Task Name | Module Used | Purpose |
|:---|:---|:---|:---|
| 1 | Test connectivity | `ansible.builtin.ping` | Verifies control-to-managed node communication |
| 2 | Create deployment directory | `ansible.builtin.file` | Ensures `/opt/queueless` exists with permissions `0755` |
| 3 | Install dependencies | `ansible.builtin.apt` | Installs curl, docker.io, python3-pip |
| 4 | Ensure Docker service | `ansible.builtin.service` | Enables and starts the Docker daemon |
| 5 | Stop old container | `ansible.builtin.command` | Removes conflicting `queueless-app` container if present |
| 6 | Run application container | `ansible.builtin.command` | Launches `queueless:latest` mapped to host port 8085 |
| 7 | Warm-up pause | `ansible.builtin.pause` | Grants 10 seconds for Spring Boot container bootstrap |
| 8 | Smoke test endpoint | `ansible.builtin.uri` | Queries `http://localhost:8085/api/tickets/stats` with retries |
| 9 | Output summary | `ansible.builtin.debug` | Displays status banner, container name, and access URL |

---

## 6. Verification and Troubleshooting

### Verification:
After the playbook completes with `failed=0`, verify the running container in your terminal:
```bash
docker ps --filter "name=queueless-app"
curl http://localhost:8085/api/tickets/stats
```

### Common Errors and Solutions:

1. **`Permission denied` while connecting to Docker socket**:
   - Cause: Non-root user lacks permission to `/var/run/docker.sock`.
   - Solution: Add the user to the docker group: `sudo usermod -aG docker $USER` and log back in, or run with `become: yes`.

2. **`Port 8085 already in use`**:
   - Cause: Another application or manual container is holding port 8085.
   - Solution: Stop conflicting process: `docker stop queueless-app` or specify an alternate port via `-e app_port=8086`.

3. **`ansible-playbook: command not found` on Windows CMD**:
   - Cause: Ansible does not run natively on Windows host shells.
   - Solution: Execute inside Ubuntu WSL: `wsl -d Ubuntu -e ansible-playbook -i inventory.ini deploy.yml`.
