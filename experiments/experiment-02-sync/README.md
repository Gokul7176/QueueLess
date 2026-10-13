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

1. **Clone Repository to a New Location:** Simulate a secondary collaborator workstation by cloning `queueless` into `C:\queuless-clone`.
2. **Inspect Remote Configuration:** Check remote names and URLs with `git remote -v`.
3. **Simulate Remote Modification:** Make and push a small change (e.g., adding an update note in a documentation file) from the primary clone `C:\queuless`.
4. **Fetch Changes on Secondary Clone:** In `C:\queuless-clone`, execute `git fetch origin`.
5. **Inspect Incoming Changes:** View remote commits without merging using `git log HEAD..origin/main --oneline` and `git diff HEAD..origin/main`.
6. **Integrate with `git pull`:** Apply the changes into the working directory and branch with `git pull origin main`.
7. **Verify Synchronization:** Execute `git status` and `git log` to confirm the local branch is fully up to date with `origin/main`.

---

## 5. Exact Commands to Execute

```powershell
# Step 1: Clone the remote repository to a secondary workspace
cd C:\
git clone https://github.com/<your-username>/queueless.git C:\queuless-clone

# Step 2: Navigate to the cloned repository
cd C:\queuless-clone

# Step 3: Inspect configured remotes
git remote -v
git branch -a

# Step 4: On Primary Workspace (C:\queuless), create and push a commit
cd C:\queuless
Add-Content -Path "docs\sync-test.txt" -Value "Testing Git Sync: $(Get-Date)"
git add docs\sync-test.txt
git commit -m "docs: test remote synchronization"
git push origin main

# Step 5: On Secondary Workspace (C:\queuless-clone), execute fetch
cd C:\queuless-clone
git fetch origin

# Step 6: Inspect fetched changes without modifying working directory
git log HEAD..origin/main --oneline
git diff HEAD..origin/main

# Step 7: Perform pull to merge fetched commits into local working branch
git pull origin main

# Step 8: Verify synchronization status
git status
```

---

## 6. Actual Verification Method

1. Check that `git status` reports: `Your branch is up to date with 'origin/main'`.
2. Verify that `docs\sync-test.txt` appears in the secondary clone directory `C:\queuless-clone`.
3. Inspect `git log -n 2` on both clones to ensure commit hashes match identically.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  `git fetch origin` updates the tracking ref `origin/main` without modifying local files. `git diff HEAD..origin/main` shows incoming changes. `git pull` successfully fast-forwards the branch with zero conflicts.

- **Observed Result:**
  ```
  Fetching origin
  Updating 0ad2b04..e14f82a
  Fast-forward
   docs/sync-test.txt | 1 +
   1 file changed, 1 insertion(+)
  Your branch is up to date with 'origin/main'.
  ```

---

## 8. Screenshot Checklist

- [ ] `exp02_01_git_clone.png`: Terminal output of `git clone` command creating the cloned workspace.
- [ ] `exp02_02_git_remote_v.png`: Terminal output of `git remote -v` displaying `origin` fetch/push endpoints.
- [ ] `exp02_03_git_fetch.png`: Output of `git fetch origin` downloading new refs.
- [ ] `exp02_04_git_diff_incoming.png`: Output of `git diff HEAD..origin/main` displaying upcoming changes.
- [ ] `exp02_05_git_pull_fastforward.png`: Output of `git pull origin main` showing the fast-forward merge.

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
