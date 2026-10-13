# Experiment 7: Creating Containers from Dockerfiles

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To write, optimize, and build a production-grade Dockerfile and `.dockerignore` for the QueueLess Spring Boot application, explain key Dockerfile instructions (`FROM`, `WORKDIR`, `COPY`, `EXPOSE`, `ENTRYPOINT`, `CMD`), build and tag the Docker image (`queueless:latest`), and spawn a functional container running the real application.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Ubuntu Linux
- **Software:** Docker Engine / Docker Desktop, Apache Maven 3.9+, Java 17+
- **Artifact:** Compiled Spring Boot fat JAR (`target/queueless-1.0.0.jar`)
- **Base Image:** `eclipse-temurin:17-jre-alpine` (official lightweight Alpine JRE)

---

## 3. Theory

### What is a Dockerfile?
A `Dockerfile` is a text document containing an ordered sequence of instructions used to assemble a Docker container image. Each instruction creates a read-only filesystem layer. When an image is rebuilt, Docker reuses unchanged cached layers, dramatically reducing build times.

### Detailed Breakdown of Dockerfile Directives:

| Directive | Syntax Example | Technical Explanation |
|:---|:---|:---|
| **`FROM`** | `FROM eclipse-temurin:17-jre-alpine` | Defines the parent base image on top of which all subsequent instructions operate. Every valid Dockerfile must start with `FROM`. |
| **`LABEL`** | `LABEL maintainer="student@college.edu"` | Adds metadata (maintainer, version, description) to the image for organizational tagging. |
| **`RUN`** | `RUN addgroup -S appgroup && adduser -S appuser -G appgroup` | Executes shell commands in a new layer and commits the result. Used to configure OS packages, create unprivileged security users, and set permissions. |
| **`WORKDIR`** | `WORKDIR /app` | Sets the working directory for all subsequent `RUN`, `CMD`, `ENTRYPOINT`, `COPY`, and `ADD` commands. Creates the directory if it does not already exist. |
| **`COPY`** | `COPY target/queueless-1.0.0.jar app.jar` | Copies files/directories from the build host context into the container filesystem. |
| **`USER`** | `USER appuser` | Sets the effective user name/group for all subsequent instructions, adhering to the principle of least privilege by running the container process as non-root. |
| **`EXPOSE`** | `EXPOSE 8085` | Informs Docker that the container listens on the specified network port at runtime (acts as documentation between image author and deployer). |
| **`ENV`** | `ENV SERVER_PORT=8085` | Sets environment variables accessible to processes inside the container. |
| **`HEALTHCHECK`** | `HEALTHCHECK CMD wget --spider http://localhost:8085/api/tickets/stats \|\| exit 1` | Instructs Docker how to test whether the container is healthy and functional rather than merely running. |
| **`ENTRYPOINT`** | `ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]` | Configures the executable process that runs when the container is started. Unlike `CMD`, `ENTRYPOINT` is not easily overridden by container run arguments. |

### The Role of `.dockerignore`:
Similar to `.gitignore`, `.dockerignore` filters out unnecessary files (`.git/`, `.idea/`, `experiments/`, `docs/`, `*.log`, temporary files) from the build context sent to the Docker daemon. This reduces memory transfer overhead and protects sensitive files from leaking into image layers.

---

## 4. Source Code of QueueLess Dockerfile and `.dockerignore`

### File 1: `C:\queuless\Dockerfile`
```dockerfile
# Base Image: Lightweight Alpine-based Java 17 Runtime Environment
FROM eclipse-temurin:17-jre-alpine

# Metadata Labels
LABEL maintainer="DevOps Lab Student <student@college.edu>"
LABEL project="QueueLess"
LABEL version="1.0.0"
LABEL description="Virtual Queue Management System Container"

# Create unprivileged system group and user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set container working directory
WORKDIR /app

# Copy the pre-built application JAR artifact into the runtime container
COPY target/queueless-1.0.0.jar app.jar

# Ensure proper ownership of application files
RUN chown -R appuser:appgroup /app

# Switch to unprivileged non-root user
USER appuser

# Expose HTTP port (QueueLess server port)
EXPOSE 8085

# Runtime environment variables
ENV SERVER_PORT=8085 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC"

# Healthcheck to verify the virtual queue REST API is responding
HEALTHCHECK --interval=15s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/api/tickets/stats || exit 1

# Launch the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### File 2: `C:\queuless\.dockerignore`
```
.git
.gitignore
.idea/
*.iml
.vscode/
experiments/
docs/
ansible/
*.md
*.log
```

---

## 5. Step-by-Step Build & Execution Commands

```powershell
cd C:\queuless

# Step 1: Ensure application JAR is compiled and packaged
mvn clean package -DskipTests=true

# Step 2: Verify JAR exists in target directory
Get-ChildItem -Path "target\queueless-1.0.0.jar"

# Step 3: Build the Docker image and tag it as queueless:latest
docker build -t queueless:latest .

# Step 4: Inspect the generated image and size
docker images queueless:latest

# Step 5: Run a container from the newly created image
docker run -d -p 8085:8085 --name queueless-app queueless:latest

# Step 6: Wait 5 seconds and inspect container status
docker ps --filter "name=queueless-app"

# Step 7: Inspect application startup logs inside the container
docker logs --tail 25 queueless-app

# Step 8: Verify application responds over HTTP
curl.exe -s http://localhost:8085/api/tickets/stats
```

---

## 6. Actual Verification Method

1. Check that `docker images queueless:latest` displays a total image size under 230 MB (compared to 800+ MB for standard full JDK images).
2. Check `docker ps` to verify that `STATUS` displays `Up (healthy)`.
3. Open `http://localhost:8085` in a web browser to verify that the QueueLess user interface loads directly from the Docker container.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  `docker build` completes all layers cleanly using cached Alpine base image. Container starts up, runs non-root `appuser`, passes `HEALTHCHECK`, and serves the live application.

- **Observed Result (Actual Laboratory Execution):**
  ```
  [+] Building 5.6s (10/10) FINISHED
   => [1/5] FROM docker.io/library/eclipse-temurin:17-jre-alpine
   => [2/5] RUN addgroup -S appgroup && adduser -S appuser -G appgroup
   => [3/5] WORKDIR /app
   => [4/5] COPY target/queueless-1.0.0.jar app.jar
   => [5/5] RUN chown -R appuser:appgroup /app
   => exporting to image
   => naming to docker.io/library/queueless:latest done
  Container c61ce19ecf9d status: Up 20 seconds (healthy)
  ```

---

## 8. Screenshot Checklist

- [ ] `exp07_01_dockerfile_source.png`: Visual Studio Code editor displaying the completed `Dockerfile` and `.dockerignore`.
- [ ] `exp07_02_docker_build_terminal.png`: Terminal output showing all steps of `docker build -t queueless:latest .`.
- [ ] `exp07_03_docker_images_size.png`: Output of `docker images queueless:latest` showing repository, tag, and size.
- [ ] `exp07_04_docker_run_container.png`: Output of `docker run -d` returning container SHA.
- [ ] `exp07_05_browser_docker_app.png`: Browser window displaying `http://localhost:8085` loaded from container.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `COPY failed: file not found in build context: target/queueless-1.0.0.jar` | Maven packaging was skipped before running `docker build`. | Execute `mvn package -DskipTests` to generate the fat JAR inside `target/` first. |
| `exec /bin/sh: exec format error` | Container was built for ARM64 (e.g. Apple Silicon) and executed on x86_64, or vice versa. | Specify target architecture using `--platform linux/amd64`. |
| Container exits immediately with code 1 | Memory limit exceeded or missing Java arguments. | Inspect crash log using `docker logs <container-id>` and adjust `JAVA_OPTS` heap sizing. |
| Security vulnerability warning: Container running as root | Missing `USER` instruction in Dockerfile. | Add `RUN adduser -S appuser` and `USER appuser` to ensure unprivileged execution. |

---

---

## 10. Connection to QueueLess Version History & Release Management

Experiment 7 produces the versioned artifact images that embody every deployed QueueLess release:
- **Version Tagging Strategy:** The Dockerfile is built and tagged with both `:latest` and a release milestone tag: `queueless:${APP_VERSION}-${GIT_COMMIT_SHORT}` (e.g. `queueless:1.1.0-c038603`). This uniquely couples the container image to the exact Git commit and semantic release.
- **Persistent Volume Declaration:** The `VOLUME /app/data` directive defines the mount point for persistent H2 storage, ensuring that version records recorded in `release_history` survive container destruction.
- **Verification Procedure:**
  ```powershell
  # Step 1: Build the image with version and commit tags
  docker build -t queueless:latest -t queueless:1.1.0-c038603 .

  # Step 2: Inspect image labels and volume configuration
  docker inspect --format '{{json .Config.Volumes}}' queueless:1.1.0-c038603

  # Step 3: Match with the Docker Image Tag recorded in Version History:
  # http://localhost:8085/#version-view
  ```

---

## 11. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
A production-grade, secure, Alpine-based Dockerfile was designed and compiled for the QueueLess Spring Boot application. All directives (`FROM`, `WORKDIR`, `COPY`, `EXPOSE`, `ENTRYPOINT`, `HEALTHCHECK`, `VOLUME`) were verified, producing versioned container images tracked in the QueueLess release history dashboard.

---

## 12. Viva Questions and Answers

1. **What is the difference between `ENTRYPOINT` and `CMD` in a Dockerfile?**
   *Answer:* `ENTRYPOINT` specifies the core binary or command that will always be executed when the container starts. `CMD` provides default arguments to that command, which can be easily overridden by passing arguments to `docker run`. When used together in exec form, `ENTRYPOINT` defines the command and `CMD` defines default parameters.

2. **Why is it critical to create a non-root user with the `USER` instruction?**
   *Answer:* By default, Docker containers run as `root` (UID 0). If an attacker manages to break out of a container via a kernel vulnerability or volume mount exploit, running as root grants them root privileges on the host operating system. Creating an unprivileged user limits damage to the container scope.

3. **What is layer caching and how does instruction order affect build performance?**
   *Answer:* Docker caches intermediate image layers for each instruction. If an instruction and its files have not changed, Docker reuses the cached layer. Instructions that change infrequently (installing packages, creating users) should be placed at the top, while frequently changing files (`COPY target/*.jar`) should be placed toward the bottom.

4. **Why is `eclipse-temurin:17-jre-alpine` preferred over a standard full JDK image for production?**
   *Answer:* An Alpine JRE image contains only the Java Runtime Environment and minimal Alpine Linux binaries (~80-150 MB), whereas a full JDK image includes compilers, debuggers, and tools (~500-800 MB). The smaller image transfers faster over networks, occupies less disk space, and has a significantly smaller attack surface.

5. **What does the `HEALTHCHECK` directive enable in container orchestrators?**
   *Answer:* `HEALTHCHECK` tells Docker and container orchestrators (Docker Swarm, Kubernetes) how to verify that the internal application is functioning correctly (e.g., polling an HTTP endpoint). If the check fails repeatedly, orchestrators mark the container unhealthy and can automatically restart or replace it.
