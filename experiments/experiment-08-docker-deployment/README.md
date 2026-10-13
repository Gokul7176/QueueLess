# Experiment 8: Deploying and Managing the Java/Web Application Using Docker

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To deploy the QueueLess virtual queue management system inside a production Docker container with external port mapping, verify web UI and REST API operations, manage container lifecycle and inspect real-time logs, analyze database persistence behavior across container restarts, and configure persistent volume storage.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux
- **Software:** Docker Engine / Docker Desktop (v24+), Web Browser (Chrome, Edge, Firefox), Terminal
- **Docker Image:** `queueless:latest` built from Experiment 7
- **Host Port:** `8085` (or any available port) mapped to Container Port `8085`

---

## 3. Theory

### Containerized Application Deployment Architecture:

```
[ Web Browser / REST Client ]
              │
              ▼ HTTP (http://localhost:8085)
┌────────────────────────────────────────────────────────┐
│ Host Operating System (Windows / Linux)                │
│ Port 8085 (Host Interface)                             │
└───────────────────────────┬────────────────────────────┘
                            │ Docker Bridge Network Proxy
                            ▼
┌────────────────────────────────────────────────────────┐
│ Docker Container (queueless-app)                       │
│ ┌────────────────────────────────────────────────────┐ │
│ │ Non-root User: appuser (UID 1000)                  │ │
│ │ Process: java -jar app.jar (PID 1)                 │ │
│ │ Listening Port: 8085 (Internal)                    │ │
│ │ Database: In-Memory / File-based H2 Database       │ │
│ └────────────────────────────────────────────────────┘ │
│                           │                            │
│                           ▼ (Optional Named Volume)    │
│                  /app/data/queueless.mv.db             │
└────────────────────────────────────────────────────────┘
```

### Database Persistence Across Container Lifecycles:
1. **Default In-Memory H2 Database (`jdbc:h2:mem:queuelessdb`):**
   - The database resides entirely in RAM inside the container process memory.
   - When a container is stopped (`docker stop`) and started (`docker start`), the in-memory data remains intact because the container's PID memory is suspended.
   - When a container is removed (`docker rm`) and recreated (`docker run`), all in-memory data is completely reset to initial pre-seeded values.
2. **Persistent Embedded File H2 Database with Docker Named Volumes:**
   - To persist ticket records permanently across container deletions, configure H2 to write to disk: `jdbc:h2:file:/app/data/queuelessdb`.
   - Mount an external Docker Named Volume to `/app/data`:
     ```powershell
     docker run -d -p 8085:8085 -v queueless_db_data:/app/data --name queueless-app queueless:latest
     ```
   - Now, even if the container is destroyed (`docker rm -f queueless-app`) and a brand new container is spawned using the same volume, all historical customer tickets and queue metrics remain preserved.

---

## 4. Step-by-Step Deployment Procedure

### Step 1: Run the QueueLess Application Container
Spawn the container mapping host port 8085 to container port 8085:
```powershell
docker run -d -p 8085:8085 --name queueless-app queueless:latest
```

### Step 2: Verify Running Status and Health
Check container state:
```powershell
docker ps --filter "name=queueless-app"
```

### Step 3: Monitor Live Application Logs
Stream live container logs:
```powershell
docker logs -f --tail 30 queueless-app
```
*(Press `Ctrl+C` to exit log streaming)*

### Step 4: Verify REST API Endpoints Over HTTP
Query queue statistics and join a test customer:
```powershell
# Query stats
Invoke-RestMethod -Uri "http://localhost:8085/api/tickets/stats" -Method Get | ConvertTo-Json -Depth 4

# Join queue
Invoke-RestMethod -Uri "http://localhost:8085/api/tickets/join" -Method Post -ContentType "application/json" -Body '{"customerName":"Bruce Banner","serviceType":"Technical Support"}' | ConvertTo-Json -Depth 4
```

### Step 5: Test Container Lifecycle (Stop, Start, Restart)
Demonstrate container control:
```powershell
# Stop container
docker stop queueless-app

# Verify stopped status
docker ps -a --filter "name=queueless-app"

# Start container back up
docker start queueless-app

# Verify resumed operation
docker ps --filter "name=queueless-app"
```

### Step 6: Configure Persistent Volume Storage (Bonus / Lab Task)
Create a persistent Docker volume and mount it into a new container:
```powershell
# Stop and remove existing container
docker rm -f queueless-app

# Create named volume
docker volume create queueless_data

# Run container with volume mounted
docker run -d -p 8085:8085 -v queueless_data:/app/data --name queueless-app queueless:latest

# Inspect volume metadata
docker volume inspect queueless_data
```

---

## 5. Actual Verification Method

1. **Browser UI Verification:** Open `http://localhost:8085` in Google Chrome or Microsoft Edge. Verify that the QueueLess home page loads, the green "System Online" badge appears, and the live system clock updates.
2. **Interactive Ticket Generation:** On the Customer Portal, enter "Elena Rostova", select "Account Services", and click "Generate Queue Ticket". Verify that the digital queue pass appears with ticket number `QL-0005`.
3. **Admin Operations:** Switch to "Admin Console". Click "Call Next Waiting Customer". Verify that the status updates to `SERVING` and Counter 1 is assigned.
4. **API Verification:** Run `curl.exe http://localhost:8085/api/tickets/stats` to verify that counts reflect `waitingCount: 2, servingCount: 1, completedCount: 1`.

---

## 6. Expected Result vs. Observed Result

- **Expected Result:**
  Container `queueless-app` runs in detached mode on port 8085. Browser access works seamlessly. REST API responds with JSON envelopes. Container logs output Spring Boot startup. Stop and start retain container state.

- **Observed Result (Actual Laboratory Execution):**
  ```
  CONTAINER ID   IMAGE              STATUS                    PORTS                    NAMES
  c61ce19ecf9d   queueless:latest   Up 20 seconds (healthy)   0.0.0.0:8085->8085/tcp   queueless-app
  HTTP 200 OK
  {"waitingCount":2,"servingCount":1,"completedCount":1,"totalCount":4}
  Browser UI: Verified on Chrome (Customer, Admin, and Live Board views functional).
  ```

---

## 7. Screenshot Checklist

- [ ] `exp08_01_docker_run_success.png`: Terminal command `docker run -d -p 8085:8085` returning container SHA.
- [ ] `exp08_02_docker_ps_healthy.png`: Terminal output of `docker ps` showing status `Up (healthy)` and port mapping `0.0.0.0:8085->8085/tcp`.
- [ ] `exp08_03_docker_logs_startup.png`: Output of `docker logs queueless-app` showing Tomcat started on port 8085.
- [ ] `exp08_04_browser_ui_running.png`: Full browser window showing QueueLess Customer Portal loaded at `http://localhost:8085`.
- [ ] `exp08_05_admin_console_live.png`: Browser window of Admin Console showing live KPI metrics (Waiting, Serving, Completed).

---

## 8. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `Bind for 0.0.0.0:8085 failed: port is already allocated` | Another local process or container is using port 8085. | Identify the conflicting process (`Get-NetTCPConnection -LocalPort 8085`), or change the host port mapping to `-p 8086:8085`. |
| Container status shows `Exited (1)` | Out of memory error or port crash inside container. | Run `docker logs queueless-app` to read exception stack trace. Adjust memory settings in `JAVA_OPTS`. |
| `localhost:8085` shows `Site can't be reached` | Port mapping omitted (ran `docker run` without `-p`). | Containers have isolated IP addresses. Port mapping (`-p 8085:8085`) is mandatory to expose services to the host browser. |
| In-memory database lost after recreating container | Normal container ephemeral lifecycle behavior. | Use Docker Volumes (`-v queueless_data:/app/data`) for persistent data retention. |

---

---

## 9. Connection to QueueLess Version History & Release Management

Experiment 8 governs containerized deployment execution, persistent storage volumes, and controlled release rollback:
- **Persistent Release History Retention:** By mounting the Docker volume `-v queueless-data:/app/data`, the embedded H2 file database (`/app/data/queuelessdb.mv.db`) persists across container upgrades, restarts, and re-deployments. Version history records are never lost when containers are updated.
- **Controlled Rollback Procedure:**
  When a newly deployed release exhibits unexpected runtime issues, DevOps engineers execute a rapid, controlled rollback to the previous verified Docker image:
  ```powershell
  # Step 1: List all available local QueueLess release images
  docker images queueless

  # Step 2: Stop and remove the current malfunctioning container
  docker stop queueless-app
  docker rm queueless-app

  # Step 3: Run the previous verified release with persistent volume
  docker run -d -p 8085:8085 -v queueless-data:/app/data --name queueless-app --restart unless-stopped queueless:1.0.0

  # Step 4: Verify application health
  curl -f http://localhost:8085/api/tickets/stats

  # Step 5: Verify that the Version History dashboard reports the rollback state
  # http://localhost:8085/#version-view
  ```

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
The QueueLess Java web application was successfully deployed inside an isolated Docker container with port mapping on host port 8085, persistent volume attachment (`queueless-data:/app/data`), and verified release rollback mechanics connected to the Version History Monitoring module.

---

## 11. Viva Questions and Answers

1. **What is the difference between Container Ephemeral Storage and Docker Volumes?**
   *Answer:* Container ephemeral storage exists in the container's top-level writable layer and is permanently destroyed when the container is removed (`docker rm`). Docker Volumes are managed storage directories stored directly on the host filesystem outside the container union filesystem, decoupled from container lifecycles and safely shared across multiple containers.

2. **How does Docker handle port forwarding (`-p 8085:8085`) internally?**
   *Answer:* Docker uses a built-in proxy process (`docker-proxy`) and Linux `iptables` / Windows NAT forwarding rules to route network packets arriving at the host's network interface on port 8085 into the virtual network interface inside the container namespace.

3. **What is the purpose of the `docker logs -f` command?**
   *Answer:* The `-f` (follow) flag streams live log output from the container's `stdout` and `stderr` directly to the terminal in real time, similar to the Unix `tail -f` command, enabling continuous monitoring of application runtime events.

4. **Why is it essential to run Spring Boot inside Docker with `SERVER_PORT` configured?**
   *Answer:* In microservice and containerized architectures, configuring the port via environment variables (`${SERVER_PORT:8085}`) allows orchestration systems (Kubernetes, Docker Compose, Ansible) to inject port assignments dynamically without requiring recompilation of source code.

5. **What is the purpose of the `restart: unless-stopped` policy in container deployments?**
   *Answer:* The `unless-stopped` restart policy ensures that if the container crashes, or if the Docker daemon or host operating system reboots, Docker will automatically restart the application container, guaranteeing high availability unless an administrator explicitly stopped it.
