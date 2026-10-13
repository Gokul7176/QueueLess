# Experiment 1: Git Installation and Local/Remote Repositories

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To install, configure, initialize, and manage local Git version control for the QueueLess project, stage and commit project source files, link the local repository to a remote GitHub repository, and push code to the remote origin.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux / macOS
- **Software:** Git (v2.40+), Terminal (PowerShell or Bash)
- **Account:** Active GitHub account with SSH key or Personal Access Token (PAT) configured
- **Repository Path:** `C:\queuless` (or Linux equivalent `/path/to/queueless`)

---

## 3. Theory

### What is Git?
Git is a distributed version control system (DVCS) designed to track changes in source code over time. Unlike centralized systems (e.g., SVN), every Git clone is a full-fledged repository with complete history and tracking capabilities.

### Key Conceptual Areas:
1. **Working Directory:** The local filesystem directory containing the project source code, templates, and configurations.
2. **Staging Area (Index):** An intermediate holding zone where changes are formatted and prepared before being committed into the permanent repository.
3. **Local Repository (`.git`):** The internal database storing all committed snapshots, branch references, and metadata.
4. **Remote Repository (GitHub):** A cloud-hosted copy of the repository that enables team collaboration, automated CI/CD webhooks, and offsite disaster recovery.

### Identity & Config Hierarchy:
Git stores configuration at three levels:
- **System:** Applied machine-wide (`/etc/gitconfig`).
- **Global:** User-specific settings (`~/.gitconfig`), set with `--global`.
- **Local:** Repository-specific settings (`.git/config`), set with `--local`.

---

## 4. Step-by-Step Procedure

1. **Verify Git Installation:** Check the installed version to ensure Git is in the system PATH.
2. **Configure Identity:** Set `user.name` and `user.email` globally or locally.
3. **Initialize Repository:** Run `git init -b main` in the root folder of the QueueLess application.
4. **Inspect Repository State:** Run `git status` to view untracked and modified files.
5. **Stage Files:** Use `git add .` to add all project source files, configurations, and wrappers to the staging area.
6. **Commit Snapshot:** Record the staged snapshot into the repository history with `git commit -m "<message>"`.
7. **Link Remote Repository:** Add the remote GitHub URL using `git remote add origin <URL>`.
8. **Push Changes:** Push the committed code to GitHub using `git push -u origin main`.
9. **Verify Remote Sync:** Verify the repository status on the GitHub web interface.

---

## 5. Exact Commands to Execute

```powershell
# Step 1: Verify Git version
git --version

# Step 2: Configure identity
git config --global user.name "Your Name"
git config --global user.email "your.email@college.edu"

# Step 3: Verify configuration settings
git config --list

# Step 4: Initialize local repository on branch main
cd C:\queuless
git init -b main

# Step 5: Check repository status
git status

# Step 6: Stage all source files (respecting .gitignore)
git add .

# Step 7: Commit changes
git commit -m "feat: initial commit for QueueLess Virtual Queue Management System"

# Step 8: View commit history
git log -n 1 --stat

# Step 9: Connect to remote GitHub repository
git remote add origin https://github.com/<your-username>/queueless.git

# Step 10: Verify remote configuration
git remote -v

# Step 11: Push branch to GitHub
git push -u origin main
```

---

## 6. Actual Verification Method

1. Check that the `.git/` hidden directory exists in `C:\queuless`.
2. Execute `git log -n 1` to verify the root commit hash, author, date, and commit message.
3. Execute `git remote -v` to ensure `origin` is mapped correctly.
4. Visit `https://github.com/<your-username>/queueless` in a browser to confirm all files (including `pom.xml`, `src/`, `Dockerfile`, `Jenkinsfile`) are visible.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  `git --version` returns Git 2.x+. `git init` creates an empty repository on branch `main`. Staging and committing stores 34+ files. `git push` successfully uploads the repository to GitHub without errors.

- **Observed Result (Actual Laboratory Execution):**
  ```
  git version 2.55.0.windows.2
  Initialized empty Git repository in C:/queuless/.git/
  [main (root-commit) 0ad2b04] feat: initial commit for QueueLess Virtual Queue Management System
   34 files changed, 4606 insertions(+)
  ```

---

## 8. Screenshot Checklist

Capture the following screenshots and store them in `docs/screenshots/` (or your lab record document):

- [ ] `exp01_01_git_version.png`: Terminal output of `git --version` and `git config --list`.
- [ ] `exp01_02_git_status_untracked.png`: Output of `git status` showing untracked files before `git add .`.
- [ ] `exp01_03_git_commit.png`: Terminal output of `git commit` showing root commit hash and file insertion counts.
- [ ] `exp01_04_git_remote.png`: Output of `git remote -v` showing fetch and push URLs.
- [ ] `exp01_05_github_repo_page.png`: Browser window of the remote GitHub repository showing the file tree and commit message.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `git is not recognized as an internal or external command` | Git is not installed or `C:\Program Files\Git\cmd` is missing from system `PATH`. | Reinstall Git with "Add to PATH" checked, or restart the terminal session. |
| `fatal: remote origin already exists` | Remote `origin` was previously configured with another URL. | Run `git remote set-url origin <new-url>` or `git remote remove origin`. |
| `fatal: Authentication failed for 'https://github.com/...'` | GitHub no longer accepts account passwords for Git operations. | Generate a Personal Access Token (PAT) under GitHub Settings -> Developer settings -> Tokens (classic) with `repo` scope, and use it as your password. |
| `error: failed to push some refs to '...'` | Remote contains commits that do not exist locally (e.g., remote README). | Run `git pull origin main --rebase` before running `git push`. |

---

---

## 10. Connection to QueueLess Version History & Release Management

In the QueueLess architecture, Experiment 1 provides the foundational cryptographic anchors for all release tracking:
- **Source Code Provenance:** Every Git commit (`git commit -m ...`) generates a unique SHA-1 hash (e.g., `c038603`) that represents an immutable snapshot of the virtual queue application.
- **Auditability in Version History:** The QueueLess Version History Monitoring module (`GET /api/version-history`) reads and displays this exact Git short commit hash, commit message, and author timestamp for every release deployed to production.
- **Verification Procedure:**
  ```powershell
  # Step 1: Inspect the latest Git commit hash and message
  git log -n 1 --oneline

  # Step 2: Open QueueLess Version History Dashboard
  # Navigate to: http://localhost:8085/#version-view
  # Verify that the active release displays the exact matching Git commit short hash!
  ```

---

## 11. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
The Git version control environment was successfully installed and configured. The QueueLess virtual queue management codebase was initialized, staged, committed, and synchronized with the remote GitHub repository according to the laboratory syllabus specifications, establishing the baseline Git provenance required for automated CI/CD version history monitoring.

---

## 12. Viva Questions and Answers

1. **What is the difference between a Centralized Version Control System (CVCS) and a Distributed Version Control System (DVCS)?**
   *Answer:* In a CVCS (e.g., SVN), a single central server holds all versions of files, and clients check out single snapshots. If the server fails, history is inaccessible. In a DVCS (e.g., Git), every client clones the entire repository with full history, allowing offline operations, local branching, and distributed redundancy.

2. **What does the Staging Area (Index) do in Git?**
   *Answer:* The staging area serves as an intermediate buffer between the working directory and the permanent commit log. It allows developers to selectively review, organize, and craft cohesive, atomic commits rather than committing all modified files indiscriminately.

3. **What is the function of the `.gitignore` file?**
   *Answer:* `.gitignore` specifies untracked file patterns (e.g., compiled `.class` files, `target/` directories, IDE metadata, API keys, and OS temporary files) that Git should deliberately ignore, preventing repository bloat and credential exposure.

4. **What does the command `git commit -m` do internally?**
   *Answer:* Git takes the snapshot of files currently in the staging area, creates blob objects for changed contents, builds a tree object representing the directory structure, writes a commit object containing author metadata, timestamp, commit message, and pointer to parent commit(s), and advances the current branch pointer.

5. **How does HTTPS authentication differ from SSH authentication when communicating with GitHub?**
   *Answer:* HTTPS uses standard web ports (443) and authenticates using GitHub Personal Access Tokens (PAT). SSH uses asymmetric public/private key pairs (typically port 22 or 443) to authenticate cryptographically without requiring tokens or credentials to be entered per session.
