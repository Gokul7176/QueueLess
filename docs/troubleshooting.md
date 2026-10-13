# QueueLess DevOps Troubleshooting & FAQ

**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  
**Course Code:** 23IT723 — DevOps Laboratory  

---

## 1. Network & Port Conflicts

### Issue: Port 8080 collision with Jenkins
- **Symptom:** Starting Spring Boot on default port 8080 throws `Web server failed to start. Port 8080 was already in use.`
- **Cause:** Jenkins Automation Server defaults to port 8080.
- **Solution:** QueueLess is pre-configured to run on port `8085` in `src/main/resources/application.properties` via `server.port=${SERVER_PORT:8085}`.
- **Command to check active port owners:**
  ```powershell
  Get-NetTCPConnection -LocalPort 8080, 8085
  ```

### Issue: Port 8085 already bound by an orphaned container
- **Symptom:** `docker run` throws `Bind for 0.0.0.0:8085 failed: port is already allocated.`
- **Solution:** Identify and stop conflicting container:
  ```powershell
  docker ps --filter "publish=8085"
  docker stop queueless-app
  docker rm queueless-app
  ```

---

## 2. Docker & Container Issues

### Issue: Docker daemon connection failure
- **Symptom:** `error during connect: ... The system cannot find the file specified.`
- **Cause:** Docker Desktop is not started or Docker daemon service is paused.
- **Solution:** Launch Docker Desktop from the Start Menu, wait until the tray icon turns steady green (Engine running), and verify with `docker version`.

### Issue: `dial tcp: lookup registry-1.docker.io: no such host`
- **Symptom:** Image pull fails with DNS resolution error.
- **Cause:** Transient DNS lookup failure in Docker Desktop network stack.
- **Solution:** In Docker Desktop Settings -> Docker Engine, add DNS servers:
  ```json
  {
    "dns": ["8.8.8.8", "1.1.1.1"]
  }
  ```
  Click **Apply & Restart**.

### Issue: Missing non-root permissions in Linux/WSL
- **Symptom:** `permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock`
- **Solution:** Add current user to `docker` group:
  ```bash
  sudo usermod -aG docker $USER
  newgrp docker
  ```

---

## 3. Git Version Control Issues

### Issue: GitHub password authentication rejected
- **Symptom:** `fatal: Authentication failed for 'https://github.com/...'`
- **Cause:** GitHub deprecated password authentication in favor of Personal Access Tokens (PAT).
- **Solution:**
  1. Open GitHub Settings -> Developer settings -> Personal access tokens (classic).
  2. Generate new token with `repo` scope.
  3. Paste the generated token into the password prompt when running `git push`.

### Issue: Merge Conflict detected
- **Symptom:** `CONFLICT (content): Merge conflict in <filename>`
- **Solution:**
  1. Inspect conflicted file: `git status`.
  2. Open file in editor and locate `<<<<<<< HEAD`, `=======`, `>>>>>>>`.
  3. Edit content to desired state and delete conflict marker lines.
  4. Finalize resolution: `git add <file>` and `git commit -m "resolve: merge conflict"`.

---

## 4. Maven & Testing Issues

### Issue: `mvn` not recognized in fresh PowerShell terminal
- **Symptom:** `The term 'mvn' is not recognized as the name of a cmdlet...`
- **Solution:** Use the cross-platform Maven wrapper included in the project:
  ```powershell
  .\mvnw.cmd test
  ```
  Or add `C:\tools\apache-maven-3.9.6\bin` permanently to your User PATH environment variable.

### Issue: Tests fail due to pre-existing data in H2
- **Symptom:** Ticket counter starts at an unexpected sequence number.
- **Solution:** `QueueControllerIntegrationTest` and `ReleaseHistoryControllerIntegrationTest` include `@BeforeEach` cleanup methods to guarantee complete database isolation before each test method runs.

---

## 5. Version History & Release Monitoring Issues

### Issue: POST `/api/version-history` returns 401 Unauthorized
- **Symptom:** Jenkins pipeline Stage 8 prints `401 Unauthorized: Invalid or missing release authentication token`.
- **Cause:** `RELEASE_TRACKER_TOKEN` is configured on the Spring Boot server, but the Jenkins job does not provide the matching `X-Release-Token` header.
- **Solution:**
  1. Add a **Secret text** credential in Jenkins with ID `RELEASE_TRACKER_TOKEN`.
  2. Set the same secret token on the container via `-e RELEASE_TRACKER_TOKEN="<token>"`.
  3. When testing locally without tokens, leave `RELEASE_TRACKER_TOKEN` unset to allow open local requests.

### Issue: Version history records lost after Docker container deletion
- **Symptom:** Container recreation deletes previous release records.
- **Cause:** Container was started without mounting the persistent storage volume.
- **Solution:** Always run the container with the named volume `-v queueless-data:/app/data`:
  ```powershell
  docker run -d -p 8085:8085 -v queueless-data:/app/data --name queueless-app queueless:latest
  ```

### Issue: Rollback to an older release required
- **Symptom:** Newly deployed release has an issue and must be rolled back immediately.
- **Solution:**
  1. Check previous image tags: `docker images queueless`.
  2. Stop and remove the current container:
     ```powershell
     docker stop queueless-app
     docker rm queueless-app
     ```
  3. Start the previous known-good image with the persistent volume:
     ```powershell
     docker run -d -p 8085:8085 -v queueless-data:/app/data --name queueless-app --restart unless-stopped queueless:1.0.0
     ```
  4. Verify health: `curl -f http://localhost:8085/api/tickets/stats`.
  5. Check `http://localhost:8085/#version-view` to confirm active release.
