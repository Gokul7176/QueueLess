# Experiment 2: Fetch and Synchronize Git Repositories

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To clone a remote Git repository, examine the difference between `git fetch` and `git pull`, inspect incoming commits and file differences from a remote tracking branch, and perform bi-directional synchronization between local and remote repositories.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux
- **Software:** Git (v2.40+), Terminal (PowerShell or Bash)
- **Repository:** An existing QueueLess remote GitHub repository
- **Temporary Test Directory:** `C:\queuless-clone` (used for dual-clone synchronization simulation)

---

## 3. Theory

### Git Synchronization Architecture:
When working in team DevOps environments, multiple engineers and automated CI/CD runners interact with the shared central repository (`origin`).

```
Local Working Directory  <--->  Local Repository (main)
                                      ^
                                      |  (git merge / git pull)
                                      v
                               Remote Tracking Branch (origin/main)
                                      ^
                                      |  (git fetch / git push)
                                      v
                               GitHub Remote Repository (origin)
```

### Key Commands:
1. **`git clone <URL> [DIR]`**: Creates a full local copy of a remote repository, sets up `origin` as the default remote name, and creates a local tracking branch (typically `main`) tracking `origin/main`.
2. **`git fetch`**: Downloads new commits, files, and refs from the remote repository into local remote-tracking branches (`origin/main`), but **does NOT modify** your working directory or local branches.
3. **`git pull`**: Performs two operations sequentially: `git fetch` followed by `git merge FETCH_HEAD` (or `git rebase` if configured). This automatically updates your working directory with remote changes.
4. **`git remote -v`**: Lists all configured remote repository aliases and their corresponding fetch and push URLs.
5. **`git diff HEAD..origin/main`**: Compares your current local commit against the latest fetched remote commit before applying merges.

### Critical Distinction: `git fetch` vs `git pull`:
- **`git fetch` is safe and non-destructive.** It lets you review changes (`git log origin/main`, `git diff`) before integrating them.
- **`git pull` integrates immediately.** If there are uncommitted local edits or conflicting commits, a merge conflict or merge commit may be triggered immediately.

---

## 4. Step-by-Step Procedure

The experiment is conducted in two distinct phases to demonstrate both public remote tracking and real commit integration without publishing unapproved commits:

### Phase 1: GitHub Remote Synchronization (`origin`)
1. **Clone Remote Repository to Isolated Directory**: Clone `https://github.com/Gokul7176/QueueLess.git` into `C:\queuless-clone` to represent a secondary collaborator workstation without touching `C:\queuless`.
2. **Inspect Remote and Branch References**: Verify configured remote URLs with `git remote -v` and all branch references with `git branch -a`.
3. **Execute Non-Destructive Fetch**: Run `git fetch origin` to inspect whether remote tracking references have changed without altering working files.
4. **Inspect Differences**: Check `git log HEAD..origin/main --oneline` and `git diff HEAD..origin/main`.
5. **Execute Pull**: Run `git pull origin main` and observe the honest `Already up to date` status when remote contains no unintegrated commits.

### Phase 2: Local Cross-Repository Synchronization (`local-demo`)
1. **Add Local Demonstration Remote**: Configure `local-demo` inside `C:\queuless-clone` pointing to `C:\queuless` (which contains unpushed local commit `497283a`).
2. **Fetch from Local Remote**: Run `git fetch local-demo` to download tracking reference `local-demo/main` without affecting local files.
3. **Inspect Incoming Commits**: Run `git log HEAD..local-demo/main --oneline` to review commit details before merging.
4. **Inspect File Differences**: Run `git diff HEAD..local-demo/main --stat` to review changed files and lines.
5. **Fast-Forward Integration**: Run `git merge local-demo/main --ff-only` to fast-forward the working branch.
6. **Verify Final State**: Check `git log -1 --oneline` and `git status` to confirm synchronized state.

---

## 5. Exact Commands & Observed Outputs

### Phase 1: GitHub Remote Synchronization (`origin`)

#### Step 1: Clone into Isolated Demonstration Workspace
```powershell
git clone https://github.com/Gokul7176/QueueLess.git C:\queuless-clone
```
*Observed Output:*
```text
Cloning into 'C:\queuless-clone'...
```

#### Step 2: Navigate to Cloned Directory and Inspect Remotes
```powershell
cd C:\queuless-clone
git remote -v
```
*Observed Output:*
```text
origin	https://github.com/Gokul7176/QueueLess.git (fetch)
origin	https://github.com/Gokul7176/QueueLess.git (push)
```

#### Step 3: Inspect Branch References
```powershell
git branch -a
```
*Observed Output:*
```text
* main
  remotes/origin/HEAD -> origin/main
  remotes/origin/main
```

#### Step 4: Verify Initial Commit & Working Tree Status
```powershell
git log -1 --oneline
git status
```
*Observed Output:*
```text
8b8297d fix(pipeline): sanitize json release metadata and secure process token passing
On branch main
Your branch is up to date with 'origin/main'.

nothing to commit, working tree clean
```

#### Step 5: Execute Non-Destructive Fetch from GitHub
```powershell
git fetch origin
```
*Observed Output:*
```text
(Exit code: 0 - no new remote refs to download)
```

#### Step 6: Inspect Incoming Commits and Diff
```powershell
git log HEAD..origin/main --oneline
git diff HEAD..origin/main
```
*Observed Output:*
```text
(Empty - HEAD is identical to origin/main at commit 8b8297d)
```

#### Step 7: Execute Pull from GitHub
```powershell
git pull origin main
```
*Observed Output:*
```text
From https://github.com/Gokul7176/QueueLess
 * branch            main       -> FETCH_HEAD
Already up to date.
```

---

### Phase 2: Local Cross-Repository Synchronization (`local-demo`)

#### Step 8: Add Local Demonstration Remote Pointing to Primary Workspace
```powershell
git remote add local-demo C:\queuless
git remote -v
```
*Observed Output:*
```text
local-demo	C:\queuless (fetch)
local-demo	C:\queuless (push)
origin	https://github.com/Gokul7176/QueueLess.git (fetch)
origin	https://github.com/Gokul7176/QueueLess.git (push)
```

#### Step 9: Fetch Commits from Local Remote
```powershell
git fetch local-demo
```
*Observed Output:*
```text
From C:\queuless
 * [new branch]      main       -> local-demo/main
```

#### Step 10: Inspect Incoming Commits Before Merging
```powershell
git log HEAD..local-demo/main --oneline
```
*Observed Output:*
```text
497283a docs(ansible): record verified QueueLess deployment and configuration
```

#### Step 11: Inspect Diff Statistics Before Merging
```powershell
git diff HEAD..local-demo/main --stat
```
*Observed Output:*
```text
 ansible/deploy.yml                          |  65 +++++++--
 ansible/inventory.ini                       |   4 +-
 experiments/experiment-10-ansible/README.md | 216 ++++++++++++++++++++++++++--
 3 files changed, 261 insertions(+), 24 deletions(-)
```

#### Step 12: Fast-Forward Merge Local Tracking Branch
```powershell
git merge local-demo/main --ff-only
```
*Observed Output:*
```text
Updating 8b8297d..497283a
Fast-forward
 ansible/deploy.yml                          |  65 +++++++--
 ansible/inventory.ini                       |   4 +-
 experiments/experiment-10-ansible/README.md | 216 ++++++++++++++++++++++++++--
 3 files changed, 261 insertions(+), 24 deletions(-)
```

#### Step 13: Verify Synchronized State in Clone
```powershell
git log -1 --oneline
git status
```
*Observed Output:*
```text
497283a docs(ansible): record verified QueueLess deployment and configuration
On branch main
Your branch is ahead of 'origin/main' by 1 commit.
  (use "git push" to publish your local commits)

nothing to commit, working tree clean
```

---

## 6. Actual Verification Method

1. **Verify Isolation**: Confirmed that `C:\queuless` was never modified, reset, or pushed during the exercise.
2. **Verify Clone Tracking**: `C:\queuless-clone` successfully tracked `origin` (`https://github.com/Gokul7176/QueueLess.git`) at commit `8b8297d`.
3. **Verify Fetch Non-Destructiveness**: Running `git fetch local-demo` updated `local-demo/main` in the clone without altering the working directory files until explicit integration.
4. **Verify Pre-Merge Inspection**: `git log HEAD..local-demo/main --oneline` and `git diff HEAD..local-demo/main --stat` accurately displayed the incoming commit `497283a` and changed lines.
5. **Verify Fast-Forward Sync**: `git merge local-demo/main --ff-only` successfully fast-forwarded `8b8297d..497283a` with zero merge conflicts.
6. **Verify Primary Workspace Status**: `git status` in `C:\queuless` confirmed working tree clean, commit `497283a` intact, and zero unauthorized remote pushes.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  `git clone` produces an isolated copy of QueueLess. Querying `origin` correctly reports `Already up to date` when GitHub has no newer commits. Fetching from `local-demo` downloads commit `497283a` into remote tracking refs without altering local files. Pre-merge inspection displays the diff stat, and fast-forward merge advances the branch to `497283a` with zero conflicts.

- **Observed Result:**
  All commands executed exactly as expected. Phase 1 accurately demonstrated synchronized state with GitHub (`origin/main` at `8b8297d`). Phase 2 demonstrated live tracking, non-destructive fetching, diff inspection, and clean fast-forward merging from `local-demo` to update the clone to `497283a`. Both workspaces remain clean, healthy, and completely isolated.

---

## 8. Screenshot Checklist

- [ ] `exp02_01_git_clone.png`: Terminal output of `git clone https://github.com/Gokul7176/QueueLess.git C:\queuless-clone`.
- [ ] `exp02_02_git_remote_v.png`: Terminal output of `git remote -v` showing `origin` and `local-demo` remotes.
- [ ] `exp02_03_git_fetch.png`: Terminal output of `git fetch local-demo` creating `local-demo/main` reference.
- [ ] `exp02_04_git_diff_incoming.png`: Terminal output of `git log HEAD..local-demo/main --oneline` and `git diff HEAD..local-demo/main --stat`.
- [ ] `exp02_05_git_pull_fastforward.png`: Terminal output of `git merge local-demo/main --ff-only` completing the fast-forward update.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `error: Your local changes to the following files would be overwritten by merge` | Local unstaged/uncommitted edits conflict with remote changes. | Commit your changes (`git commit -am "wip"`), stash them (`git stash`), or discard unneeded local edits (`git restore <file>`). |
| `fatal: refusing to merge unrelated histories` | Two repositories with separate root commits are being merged. | Execute `git pull origin main --allow-unrelated-histories`. |
| `fatal: couldn't find remote ref main` | Remote default branch is named `master` instead of `main`. | Check remote branches with `git branch -r` and pull from `origin/master`. |

---

---

## 10. Connection to QueueLess Version History & Release Management

Experiment 2 guarantees synchronization between developer workstations, central GitHub repositories, and CI/CD automation runners:
- **Distributed Synchronization:** When engineers push commits to GitHub, the remote repository becomes the single source of truth for release candidates.
- **CI/CD Pipeline Triggers:** Jenkins checkout stages (`checkout scm`) fetch and pull these exact remote refs. The QueueLess pipeline captures the fetched `GIT_COMMIT` and `GIT_BRANCH` metadata during build execution.
- **Verification Procedure:**
  ```powershell
  # Step 1: Fetch and verify current remote tracking branch status
  git fetch origin
  git status -uno

  # Step 2: Compare local HEAD commit with the remote origin commit
  git rev-parse HEAD
  git rev-parse origin/main

  # Step 3: Check QueueLess Version History API to verify that the deployed version matches origin/main
  curl -s http://localhost:8085/api/version-history/current
  ```

---

## 11. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
Repository cloning, remote tracking configuration, non-destructive fetching, diff analysis, and bi-directional repository synchronization were successfully demonstrated and validated, ensuring consistent source-code tracking across local environments, remote GitHub repositories, and automated CI/CD release pipelines.

---

## 12. Viva Questions and Answers

1. **What is the exact functional equation describing `git pull`?**
   *Answer:* `git pull = git fetch + git merge` (or `git pull --rebase = git fetch + git rebase`). It retrieves the latest commits from the remote repository and immediately integrates them into the current active branch.

2. **Why is it recommended in enterprise DevOps pipelines to run `git fetch` before merging?**
   *Answer:* `git fetch` downloads metadata and objects without touching working trees or index states. This allows automated scripts or engineers to inspect incoming commits (`git log HEAD..origin/main`), run static analysis or diff checks, and verify integrity before merging.

3. **What is a "Remote Tracking Branch"? Give an example.**
   *Answer:* A remote tracking branch is a read-only local reference that mirrors the state of a branch on the remote server at the time of the last `git fetch` or `git pull`. Example: `refs/remotes/origin/main` (displayed as `origin/main`).

4. **What does a Fast-Forward merge mean?**
   *Answer:* A fast-forward merge occurs when the target branch has no divergent commits of its own since the branch point. Git simply moves the current branch pointer forward to point to the incoming commit without creating an extra merge commit.

5. **How does `git remote add` differ from `git clone`?**
   *Answer:* `git clone` downloads an entire repository, initializes a new working directory and `.git` database, and automatically configures `origin`. `git remote add` simply registers a new remote URL and nickname inside an already existing local Git repository.
