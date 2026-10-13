# Experiment 6: Installing Docker and Executing Basic Commands

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To verify Docker installation and daemon health, execute and document core Docker command-line operations (`docker --version`, `docker pull`, `docker images`, `docker run`, `docker ps`, `docker ps -a`, `docker logs`, `docker stop`, `docker start`, `docker rm`), and understand container lifecycle management.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 with WSL2 backend or Ubuntu Linux
- **Software:** Docker Desktop (v24.x+) or Docker Engine on Linux
- **Privileges:** Administrator access / user added to `docker` group
- **Internet Connectivity:** Access to Docker Hub container registry

---

## 3. Theory

### What is Docker?
Docker is an open-source platform that automates the deployment of applications inside lightweight, portable, self-sufficient containers. Unlike hypervisor-based virtual machines (VMs) which emulate hardware and run complete guest operating systems, Docker containers share the host operating system kernel and isolate processes using Linux namespaces and control groups (`cgroups`).

### Key Concepts:
1. **Docker Daemon (`dockerd`):** The background service managing Docker objects (images, containers, networks, volumes).
2. **Docker Client (`docker`):** The CLI tool used by developers to communicate with the daemon via REST API.
3. **Image:** A read-only template with instructions for creating a Docker container (composed of stacked filesystem layers).
4. **Container:** A runnable instance of an image. When started, a thin writable container layer is added on top of the underlying image layers.
5. **Registry:** A stateless, scalable server-side application that stores and lets you distribute Docker images (e.g., Docker Hub).

### Container State Lifecycle:

```
[ docker pull ] ──► [ Local Image ]
                           │
                           ▼ [ docker run ]
                     ┌───────────┐
      ┌─────────────►│  RUNNING  │◄─────────────┐
      │              └─────┬─────┘              │
[ docker start ]           │ [ docker stop ]    │
      │                    ▼                    │
      └──────────────┌───────────┐              │
                     │  STOPPED  │──────────────┘
                     └─────┬─────┘
                           │ [ docker rm ]
                           ▼
                      ( DESTROYED )
```

---

## 4. Step-by-Step Procedure & Executed Commands

This experiment was conducted using an isolated, disposable container named `queueless-docker-basics-demo` to preserve host runtime stability and prevent interference with the live `queueless-app` container.

### Phase 1: Host Environment Inspection
Inspect the active Docker daemon and host resources:

```powershell
docker --version
docker version
docker info
docker ps
docker ps -a
docker images
docker volume ls
```

*Observed Host Baseline:*
- Docker Engine: `29.6.2` (API `1.55`, build `dfc4efb`) on Docker Desktop `4.83.0` (WSL2 Linux backend).
- Active Container: `queueless-app` (`queueless:1.1.0-8b8297d`, Up 2 hours, healthy, port `8085`).
- Active Volume: `queueless-data` (stores persistent QueueLess SQLite/H2 data).

---

### Phase 2: Dedicated Disposable Container Setup
Verify that no conflicting container named `queueless-docker-basics-demo` exists before creation:

```powershell
docker ps -a --filter "name=^/queueless-docker-basics-demo$"
```

Pull the lightweight `nginx:alpine` image from Docker Hub:

```powershell
docker pull nginx:alpine
docker images --filter "reference=nginx:alpine"
```

*Observed Pull Result:*
- Digest: `sha256:df221db836e1754089190208cee7eeda94f233197056426eda74a43ab1abeac2`
- Image Size: Content size `27.2MB`, Disk usage `94.4MB`.

Run the disposable container in detached mode **without publishing host ports** (preventing port collisions with port 80 or 8085):

```powershell
docker run -d --name queueless-docker-basics-demo nginx:alpine
```
*Resulting Container ID:* `5e3908a40ddaf50f7dbb1240b356e85adf9b929ab555970d3d5b805cddfa40b1`

Inspect the running container and its process logs:

```powershell
docker ps --filter "name=queueless-docker-basics-demo"
docker inspect --format "Name={{.Name}} Image={{.Config.Image}} Status={{.State.Status}} Running={{.State.Running}} IPAddress={{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} Ports={{json .NetworkSettings.Ports}}" queueless-docker-basics-demo
docker logs queueless-docker-basics-demo
```

*Observed Container State:*
- Status: `running` (`Up 11 seconds`), IPAddress: `172.17.0.3`, Ports: `{"80/tcp":null}` (unmapped to host).
- Logs: Nginx entrypoint initialization completed (`/docker-entrypoint.sh: Configuration complete; ready for start up`, `nginx/1.31.6`, workers started).

---

### Phase 3: Container Lifecycle Operations

1. **Stop the Container:**
   ```powershell
   docker stop queueless-docker-basics-demo
   docker ps -a --filter "name=queueless-docker-basics-demo"
   ```
   *Result:* Process terminated gracefully; state transitioned to `Exited (0)`.

2. **Restart the Container:**
   ```powershell
   docker start queueless-docker-basics-demo
   docker ps --filter "name=queueless-docker-basics-demo"
   ```
   *Result:* Re-activated existing container with identical container ID and layer state (`Up 15 seconds`).

3. **Stop and Permanently Remove the Container:**
   ```powershell
   docker stop queueless-docker-basics-demo
   docker rm queueless-docker-basics-demo
   docker ps -a --filter "name=queueless-docker-basics-demo"
   ```
   *Result:* Container deleted from disk; query returns 0 matching containers. Only the disposable demonstration container was removed; cached images and other containers were left intact.

---

### Phase 4: QueueLess Runtime Safety Re-Verification
Verify that the production application container and data volumes were completely untouched:

```powershell
docker ps --filter "name=queueless-app"
docker volume ls
curl.exe -s http://localhost:8085/api/tickets/stats
curl.exe -s http://localhost:8085/api/version-history
```

*Safety Verification Results:*
- Container `queueless-app` (`29ea59ae2cae`): Running continuously (`Up 2 hours (healthy)` on port `8085`).
- Volume `queueless-data`: Untouched, active data mount.
- HTTP `/api/tickets/stats`: `HTTP 200` (`totalCount: 4`, `waitingCount: 2`).
- HTTP `/api/version-history`: `HTTP 200` (Current active release: `8b8297d`, status `VERIFIED`).

---

## 5. Summary of Core Docker Concepts Demonstrated

| Concept | Command Demonstrated | Role in Container Architecture |
|:---|:---|:---|
| **Image** | `docker pull`, `docker images` | Immutable, layered read-only templates cached locally from Docker Hub. |
| **Container** | `docker run -d` | Sandboxed, runnable process instance with a thin writable container layer. |
| **Listing** | `docker ps`, `docker ps -a` | Inspection of running vs. stopped processes across the host namespace. |
| **Logging** | `docker logs` | Multiplexed capture of standard output (`stdout`) and standard error (`stderr`). |
| **Stopping** | `docker stop` | Graceful process termination using POSIX `SIGTERM`, falling back to `SIGKILL`. |
| **Restarting** | `docker start` | Resuming execution of an exited container without creating a new container ID. |
| **Removal** | `docker rm` | Deletion of the stopped container and its ephemeral writable layer. |

---

## 6. Actual Observed Terminal Output Record

```text
=== Environment Inspection ===
> docker --version
Docker version 29.6.2, build dfc4efb

> docker ps
CONTAINER ID   IMAGE                     STATUS                 PORTS                                         NAMES
29ea59ae2cae   queueless:1.1.0-8b8297d   Up 2 hours (healthy)   0.0.0.0:8085->8085/tcp, [::]:8085->8085/tcp   queueless-app

=== Image Pull & Container Launch ===
> docker pull nginx:alpine
Status: Downloaded newer image for nginx:alpine
docker.io/library/nginx:alpine

> docker run -d --name queueless-docker-basics-demo nginx:alpine
5e3908a40ddaf50f7dbb1240b356e85adf9b929ab555970d3d5b805cddfa40b1

> docker ps --filter "name=queueless-docker-basics-demo"
CONTAINER ID   IMAGE          COMMAND                  STATUS          PORTS     NAMES
5e3908a40dda   nginx:alpine   "/docker-entrypoint.…"   Up 11 seconds   80/tcp    queueless-docker-basics-demo

> docker inspect --format "Name={{.Name}} Status={{.State.Status}} Ports={{json .NetworkSettings.Ports}}" queueless-docker-basics-demo
Name=/queueless-docker-basics-demo Status=running Ports={"80/tcp":null}

=== Lifecycle Transitions ===
> docker stop queueless-docker-basics-demo
queueless-docker-basics-demo

> docker ps -a --filter "name=queueless-docker-basics-demo"
CONTAINER ID   IMAGE          STATUS                      NAMES
5e3908a40dda   nginx:alpine   Exited (0) 12 seconds ago   queueless-docker-basics-demo

> docker start queueless-docker-basics-demo
queueless-docker-basics-demo

> docker ps --filter "name=queueless-docker-basics-demo"
CONTAINER ID   IMAGE          STATUS          NAMES
5e3908a40dda   nginx:alpine   Up 15 seconds   queueless-docker-basics-demo

> docker stop queueless-docker-basics-demo && docker rm queueless-docker-basics-demo
queueless-docker-basics-demo
queueless-docker-basics-demo

> docker ps -a --filter "name=queueless-docker-basics-demo"
CONTAINER ID   IMAGE     COMMAND   CREATED   STATUS    PORTS     NAMES
(empty - container cleanly removed)

=== Runtime Safety Check ===
> curl.exe -s http://localhost:8085/api/tickets/stats
{"success":true,"message":"Operation successful","data":{"waitingCount":2,"servingCount":1,"completedCount":1,"cancelledCount":0,"totalCount":4,"estimatedWaitMinutes":10}}
```

---

## 7. Practical Record Evidence Checklist

The following items should be captured from your terminal for your printed/digital lab record:

- [ ] `exp06_01_docker_version.png`: Terminal output showing `docker --version`, `docker version`, and `docker info`.
- [ ] `exp06_02_docker_pull.png`: Terminal output showing `docker pull nginx:alpine` layer download progress and completion digest.
- [ ] `exp06_03_docker_images.png`: Terminal output showing `docker images` listing `nginx:alpine` and local images.
- [ ] `exp06_04_docker_run_inspect.png`: Terminal output showing `docker run -d`, `docker ps`, and `docker inspect` for `queueless-docker-basics-demo`.
- [ ] `exp06_05_docker_lifecycle.png`: Terminal output showing the lifecycle sequence (`stop`, `ps -a` Exited, `start`, `stop`, `rm`, and empty `ps -a`).
- [ ] `exp06_06_queueless_safety.png`: Terminal output showing `queueless-app` remaining healthy and responding to HTTP health checks.

---

## 8. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `error during connect: ... The system cannot find the file specified` | Docker Desktop daemon service is not running. | Start Docker Desktop from the Start Menu or run `Start-Service docker` in PowerShell as Administrator. |
| `Conflict. The container name "/..." is already in use` | Another container exists with the exact same name. | Check with `docker ps -a --filter "name=..."` and remove the old container using `docker rm -f <container-name>`. |
| `port is already allocated` | Another application or container is currently listening on the specified host port. | Select an unused host port (e.g., `-p 8087:8080`) or run without host port mapping (`-d` without `-p`). |
| `permission denied while trying to connect to the Docker daemon socket` | On Linux, non-root user is not in the `docker` user group. | Run `sudo usermod -aG docker $USER` and open a fresh shell. |

---

## 9. Connection to QueueLess Version History & Release Management

Experiment 6 provides runtime inspection and container command tools essential for managing deployed QueueLess releases:
- **Inspecting Active Release Images:** Commands like `docker ps` and `docker images` reveal the exact image tags (`queueless:1.1.0-8b8297d`) corresponding to the entries recorded in the QueueLess Version History table.
- **Log Inspection for Provenance Audit:** Running `docker logs queueless-app` prints the Spring Boot startup logs containing the active datasource URL and release token verification notices.
- **Verification Procedure:**
  ```powershell
  # Step 1: List running QueueLess release containers
  docker ps --filter "name=queueless"

  # Step 2: Inspect image labels and release tag
  docker inspect --format '{{.Config.Image}}' queueless-app

  # Step 3: Match the container tag with the Docker Tag displayed on the Version History dashboard:
  # http://localhost:8085/#version-view
  ```

---

## 10. Student Experiment Result Record

**Date of Execution:** 13-October-2026
**Student Name:** Gokul
**Course Code:** 23IT723 — DevOps Laboratory

**Result:**
Docker installation and daemon status were verified on Docker Desktop 29.6.2 (WSL2 Linux backend). Core container lifecycle commands (`docker --version`, `docker pull`, `docker images`, `docker run -d`, `docker ps`, `docker inspect`, `docker logs`, `docker stop`, `docker start`, `docker rm`) were safely executed on a dedicated disposable container (`queueless-docker-basics-demo` running `nginx:alpine`). All container state transitions were confirmed with zero disruptions to the live running `queueless-app` production container on port 8085 and persistent database volume `queueless-data`.

---

## 11. Viva Questions and Answers

1. **What is the fundamental difference between a Docker Image and a Docker Container?**
   *Answer:* A Docker image is a read-only, immutable snapshot/template containing application code, libraries, and runtime dependencies. A container is a runnable, isolated instance of an image with a thin, read-write layer added on top.

2. **What does the flag `-d` signify in `docker run -d`?**
   *Answer:* The `-d` flag stands for "detached mode". It instructs Docker to run the container in the background and print the newly created Container ID to standard output, returning control of the terminal to the user immediately.

3. **What is the syntax format of port mapping `-p 8085:8080` in `docker run`?**
   *Answer:* The format is `-p <Host_Port>:<Container_Port>`. Requests arriving at port 8085 on the host machine's physical network adapter are forwarded by the Docker proxy into port 8080 inside the container's isolated network namespace.

4. **What happens to data written inside a container when the container is deleted (`docker rm`)?**
   *Answer:* Any data written to the container's default writable layer is permanently deleted and lost upon container removal, unless that data was stored in an attached Docker Volume or Bind Mount.

5. **How does `docker stop` differ from `docker kill`?**
   *Answer:* `docker stop` sends a `SIGTERM` signal to the container's root process, allowing it to perform graceful shutdown tasks (closing DB connections, flushing buffers) before issuing `SIGKILL` after 10 seconds. `docker kill` sends `SIGKILL` immediately, terminating the process without graceful cleanup.
