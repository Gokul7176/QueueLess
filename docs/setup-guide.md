# QueueLess Installation & Setup Guide

**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  
**Course Code:** 23IT723 — DevOps Laboratory  

---

## 1. Prerequisites

Before setting up QueueLess, ensure the following core tools are installed on your workstation:

| Requirement | Minimum Version | Verification Command |
|:---|:---|:---|
| **Java JDK** | 17+ (Tested on OpenJDK 21) | `java -version` |
| **Apache Maven** | 3.9+ (or use included `mvnw`) | `mvn -version` or `.\mvnw -version` |
| **Git** | 2.40+ | `git --version` |
| **Docker Desktop / Engine** | 24+ | `docker --version` |
| **WSL2 (Ubuntu)** *(for Ansible)* | Ubuntu 22.04+ | `wsl -l -v` |

---

## 2. Quick Start: Option A (Local Native Java)

### Step 1: Clone or Navigate to the Project
```powershell
cd C:\queuless
```

### Step 2: Compile & Run Automated Tests
```powershell
mvn test
# Or using Maven wrapper:
.\mvnw.cmd test
```

### Step 3: Package Executable Fat JAR
```powershell
mvn clean package -DskipTests=true
```

### Step 4: Run the Application
```powershell
java -jar target/queueless-1.0.0.jar
```

### Step 5: Access the Web Application
Open your browser and navigate to:
- **Application URL:** `http://localhost:8085/`
- **H2 Database Console:** `http://localhost:8085/h2-console`
  - JDBC URL: `jdbc:h2:mem:queuelessdb`
  - Username: `sa`
  - Password: `password`

---

## 3. Quick Start: Option B (Docker Container)

### Step 1: Build the Docker Image
```powershell
cd C:\queuless
docker build -t queueless:latest .
```

### Step 2: Launch the Docker Container
```powershell
docker run -d -p 8085:8085 --name queueless-app queueless:latest
```

### Step 3: Verify Container Health
```powershell
docker ps --filter "name=queueless-app"
docker logs --tail 20 queueless-app
```

### Step 4: Stop and Remove Container
```powershell
docker stop queueless-app
docker rm queueless-app
```

---

## 4. Quick Start: Option C (Jenkins Pipeline)

1. Launch Jenkins on `http://localhost:8080`.
2. Ensure `JDK-17` and `Maven-3.9.6` are registered under **Manage Jenkins -> Tools**.
3. Create a **New Item -> Pipeline** named `QueueLess-Pipeline`.
4. Under Pipeline definition, select **Pipeline script from SCM**, choose **Git**, specify your repository URL, and set script path to `Jenkinsfile`.
5. Click **Build Now** to trigger the 7-stage automated build, test, and container deployment.

---

## 5. Quick Start: Option D (Ansible on Ubuntu WSL2)

1. Open Ubuntu terminal:
   ```powershell
   wsl -d Ubuntu
   ```
2. Navigate to mounted project directory:
   ```bash
   cd /mnt/c/queuless/ansible
   ```
3. Validate playbook syntax:
   ```bash
   ansible-playbook -i inventory.ini deploy.yml --syntax-check
   ```
4. Run the deployment playbook:
   ```bash
   ansible-playbook -i inventory.ini deploy.yml
   ```
