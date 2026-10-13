# Experiment 3: Git Branching and Merging

**Course Code:** 23IT723 — DevOps Laboratory
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System

---

## 1. Aim
To create, isolate, and switch between feature branches for queue functionality, commit independent changes on separate branches, perform clean merges into the `main` branch, and demonstrate a controlled merge-conflict generation, inspection, and resolution workflow within an isolated demonstration workspace.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux
- **Software:** Git (v2.40+), Terminal (PowerShell or Bash)
- **Primary Workspace:** `C:\queuless` (preserved unchanged)
- **Demonstration Workspace:** `C:\queuless-clone` (used for all branching and merge experiments)

---

## 3. Theory

### What is a Branch in Git?
In Git, a branch is an extremely lightweight, movable pointer to a specific commit. Unlike older VCS systems where branching created complete directory copies, Git branching requires only 41 bytes of storage (a commit SHA hash inside `.git/refs/heads/`).

### Branching Workflows in DevOps:
1. **Trunk-Based Development:** Developers push short-lived feature branches directly into `main` after automated test suites pass in CI.
2. **Feature Branching:** Every new feature, bugfix, or refactor is developed in an isolated branch (`feature/priority-queue`), preventing untested code from polluting `main`.

### Merging Mechanisms:
- **Fast-Forward Merge:** Target branch has no new commits since branching; the branch pointer is simply advanced forward to the feature branch commit.
- **Three-Way Merge (True Merge):** Both branches have diverged with independent commits. Git identifies the common ancestor commit, combines changes from both tips, and creates a new **merge commit** with two parents.
- **Merge Conflict:** Occurs when changes on both branches modify the exact same lines of code within the same file differently. Git pauses the merge and inserts conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>>`), delegating resolution to the developer.

---

## 4. Step-by-Step Procedure

The experiment is conducted inside the isolated clone `C:\queuless-clone` across two distinct parts:

### Part B1: Feature Branch Isolation & Clean Merge
1. **Create and Switch to Feature Branch:** From `main`, create branch `feature/priority-queue` using `git switch -c feature/priority-queue`.
2. **Add Documentation Specification:** Create `docs/priority-queue-spec.md` with priority queue allocation and SLA rules without modifying production application source code.
3. **Commit on Feature Branch:** Stage and commit the specification file (`docs(git): add priority queue specification`).
4. **Switch to Main & Merge:** Switch back to `main` with `git switch main` and merge `feature/priority-queue` via fast-forward.
5. **Verify Branch State:** Check `git status` and `git log` to confirm clean integration.

### Part B2: Controlled Merge-Conflict Generation & Resolution
1. **Establish Base Policy File:** On `main`, create `docs/queue-policy.txt` with base wait policy:
   `Queue wait policy: review unresolved waits after 30 minutes.`
   Commit on `main` (`docs: establish base queue wait policy`).
2. **Branch A Modification:** From base commit, create `conflict-branch-a`, update wait policy to 15 minutes, and commit (`policy: update wait policy to 15 minutes`).
3. **Branch B Modification:** Switch back to `main` (pointing to base commit), create `conflict-branch-b`, update the exact same line to 45 minutes, and commit (`policy: update wait policy to 45 minutes`).
4. **Merge Branch A into Main:** Switch to `main` and merge `conflict-branch-a` (fast-forwards cleanly).
5. **Attempt Merge of Branch B (Trigger Conflict):** Run `git merge conflict-branch-b`. Because both branches modified the same line independently, Git halts with `CONFLICT (content)`.
6. **Inspect Conflict Markers:** Inspect conflict state with `git status` and review conflict markers in `docs/queue-policy.txt`.
7. **Reconcile & Resolve Conflict:** Replace conflict markers with a unified policy:
   `Queue wait policy: notify users after 15 minutes and escalate unresolved waits after 45 minutes.`
8. **Finalize Merge Commit:** Stage the resolved file with `git add docs/queue-policy.txt` and commit:
   `merge: reconcile queue wait policy conflict`.
9. **Verify Converged History Graph:** Run `git log --graph --oneline --decorate -n 15` to confirm the true diamond merge graph.

---

## 5. Exact Commands & Observed Outputs

### Part B1: Feature Branch Creation & Clean Merge

#### Step 1: Verify Initial State & Create Feature Branch
```powershell
cd C:\queuless-clone
git status
git switch -c feature/priority-queue
git branch
```
*Observed Output:*
```text
On branch main
nothing to commit, working tree clean
Switched to a new branch 'feature/priority-queue'
* feature/priority-queue
  main
```

#### Step 2: Add Priority Queue Specification and Commit
```powershell
# Create docs/priority-queue-spec.md
git add docs/priority-queue-spec.md
git commit -m "docs(git): add priority queue specification"
```
*Observed Output:*
```text
[feature/priority-queue 044230e] docs(git): add priority queue specification
 1 file changed, 16 insertions(+)
 create mode 100644 docs/priority-queue-spec.md
```

#### Step 3: Switch to Main and Execute Fast-Forward Merge
```powershell
git switch main
git merge feature/priority-queue
```
*Observed Output:*
```text
Switched to branch 'main'
Updating 497283a..044230e
Fast-forward
 docs/priority-queue-spec.md | 16 ++++++++++++++++
 1 file changed, 16 insertions(+)
 create mode 100644 docs/priority-queue-spec.md
```

---

### Part B2: Controlled Conflict Generation & Resolution

#### Step 4: Establish Base Policy File on Main
```powershell
Set-Content -Path "docs\queue-policy.txt" -Value "Queue wait policy: review unresolved waits after 30 minutes."
git add docs/queue-policy.txt
git commit -m "docs: establish base queue wait policy"
```
*Observed Output:*
```text
[main e0bfd83] docs: establish base queue wait policy
 1 file changed, 1 insertion(+)
 create mode 100644 docs/queue-policy.txt
```

#### Step 5: Branch A (15-Minute Policy)
```powershell
git switch -c conflict-branch-a
Set-Content -Path "docs\queue-policy.txt" -Value "Queue wait policy: review unresolved waits after 15 minutes."
git commit -am "policy: update wait policy to 15 minutes"
```
*Observed Output:*
```text
Switched to a new branch 'conflict-branch-a'
[conflict-branch-a 07c4e93] policy: update wait policy to 15 minutes
 1 file changed, 1 insertion(+), 1 deletion(-)
```

#### Step 6: Branch B (45-Minute Policy)
```powershell
git switch main
git switch -c conflict-branch-b
Set-Content -Path "docs\queue-policy.txt" -Value "Queue wait policy: review unresolved waits after 45 minutes."
git commit -am "policy: update wait policy to 45 minutes"
```
*Observed Output:*
```text
Switched to branch 'main'
Switched to a new branch 'conflict-branch-b'
[conflict-branch-b 8e84dd5] policy: update wait policy to 45 minutes
 1 file changed, 1 insertion(+), 1 deletion(-)
```

#### Step 7: Merge Branch A into Main
```powershell
git switch main
git merge conflict-branch-a
```
*Observed Output:*
```text
Switched to branch 'main'
Updating e0bfd83..07c4e93
Fast-forward
 docs/queue-policy.txt | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

#### Step 8: Merge Branch B into Main (Triggers Content Conflict)
```powershell
git merge conflict-branch-b
```
*Observed Output:*
```text
Auto-merging docs/queue-policy.txt
CONFLICT (content): Merge conflict in docs/queue-policy.txt
Automatic merge failed; fix conflicts and then commit the result.
```

#### Step 9: Inspect Conflict Status and Conflict Markers
```powershell
git status
Get-Content docs\queue-policy.txt
```
*Observed Output:*
```text
On branch main
You have unmerged paths.
  (fix conflicts and run "git commit")
  (use "git merge --abort" to abort the merge)

Unmerged paths:
  (use "git add <file>..." to mark resolution)
	both modified:   docs/queue-policy.txt

  <<<<<<< HEAD
  Queue wait policy: review unresolved waits after 15 minutes.
  =======
  Queue wait policy: review unresolved waits after 45 minutes.
  >>>>>>> conflict-branch-b
```

#### Step 10: Resolve Conflict with Unified Threshold Policy
```powershell
Set-Content -Path "docs\queue-policy.txt" -Value "Queue wait policy: notify users after 15 minutes and escalate unresolved waits after 45 minutes."
git add docs/queue-policy.txt
git commit -m "merge: reconcile queue wait policy conflict"
```
*Observed Output:*
```text
[main 8cf59b4] merge: reconcile queue wait policy conflict
```

#### Step 11: Display Converged History Graph
```powershell
git log --graph --oneline --decorate -n 15
```
*Observed Output:*
```text
*   8cf59b4 (HEAD -> main) merge: reconcile queue wait policy conflict
|\
| * 8e84dd5 (conflict-branch-b) policy: update wait policy to 45 minutes
* | 07c4e93 (conflict-branch-a) policy: update wait policy to 15 minutes
|/
* e0bfd83 docs: establish base queue wait policy
* 044230e (feature/priority-queue) docs(git): add priority queue specification
* 497283a (local-demo/main, local-demo/HEAD) docs(ansible): record verified QueueLess deployment and configuration
* 8b8297d (origin/main, origin/HEAD) fix(pipeline): sanitize json release metadata and secure process token passing
* 84b4a48 fix(docker): specify executable jar filename to avoid wildcard collision
* 20d1dd2 feat(version-history): add release monitoring and CI/CD integration
* c038603 feat: release v1.1.0 with CI/CD status badge and automated deployment footer
* 4bd1506 ci: include Docker Desktop bin in PATH for Jenkins Windows service
* 4ec99c1 docs: align Experiment 4 and 5 manuals with QueueLess-Build and QueueLess-CICD
* 8451579 ci: enhance Jenkinsfile and auto-detect JAVA_HOME in mvnw.cmd for Windows Jenkins agent
* 1252977 docs: complete all 10 DevOps experiment manuals, architecture, and verification documentation
* 0ad2b04 feat: initial commit for QueueLess Virtual Queue Management System
```

---

## 6. Actual Verification Method

1. **Verify Isolation**: Confirmed that primary workspace `C:\queuless` remained completely isolated and untouched throughout the branch operations.
2. **Verify Feature Merge**: Checked that `docs/priority-queue-spec.md` exists on `main` at commit `044230e`.
3. **Verify Conflict Markers**: Confirmed Git inserted standard markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> conflict-branch-b`) when competing edits targeted the same line.
4. **Verify Clean Resolution**: Verified that `diff --cc docs/queue-policy.txt` showed only the reconciled policy line and zero markers before committing.
5. **Verify Commit Topology**: `git log --graph` confirmed the two-parent merge commit `8cf59b4` reconciling `conflict-branch-a` (`07c4e93`) and `conflict-branch-b` (`8e84dd5`).

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**
  Part B1 feature branch fast-forwards cleanly into `main`. Part B2 merge of `conflict-branch-b` into `main` encounters a content conflict in `docs/queue-policy.txt`, displays conflict markers, and produces a two-parent merge commit upon manual resolution.

- **Observed Result:**
  All operations succeeded exactly as expected. Feature branch `feature/priority-queue` merged cleanly at commit `044230e`. Merging `conflict-branch-b` generated the expected conflict, was resolved with a combined policy, and produced merge commit `8cf59b4`.

---

## 8. Screenshot Checklist

- [ ] `exp03_01_git_branch_list.png`: Output of `git branch` showing `feature/priority-queue` branch created.
- [ ] `exp03_02_feature_merge.png`: Terminal output of `git merge feature/priority-queue` showing fast-forward merge.
- [ ] `exp03_03_conflict_detected.png`: Terminal output showing `CONFLICT (content): Merge conflict in docs/queue-policy.txt`.
- [ ] `exp03_04_conflict_markers.png`: Terminal output displaying conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> conflict-branch-b`).
- [ ] `exp03_05_resolved_git_log_graph.png`: Terminal output of `git log --graph --oneline --decorate` showing the diamond merge commit `8cf59b4`.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `error: you need to resolve your current index first` | Attempted a new merge or branch switch while a merge conflict is unresolved. | Resolve conflict markers, run `git add <file>`, and complete `git commit`. Or abort the merge using `git merge --abort`. |
| `error: The branch '...' is not fully merged` | Tried deleting an unmerged branch with `git branch -d`. | If deliberate, force deletion using `git branch -D <branch-name>`. |
| Conflict markers left in source files | Incomplete resolution before committing. | Search for `<<<<<<<` across all project files before committing to prevent syntax errors. |

---

## 10. Connection to QueueLess Version History & Release Management

Experiment 3 demonstrates parallel feature branch workflows and release branch integration:
- **Feature Isolation:** Feature development (such as adding the Version History module) occurs on isolated branches (e.g., `feature/version-history`) without risking production stability.
- **Branch Tracking in Releases:** The QueueLess Version History Monitoring entity (`ReleaseHistory.gitBranch`) records the originating branch name for every release. The UI displays branch badges (`🌿 main`, `🌿 feature/...`) alongside every deployment.
- **Release Merge Milestones:** Merging feature branches into `main` creates stable milestone commits that Jenkins builds and deploys as official releases.
- **Verification Procedure:**
  ```powershell
  # Step 1: List all local branches and active branch
  git branch -v

  # Step 2: Query the QueueLess Version History endpoint
  # Observe the gitBranch property recorded for the deployed release
  curl -s http://localhost:8085/api/version-history/current
  ```

---

## 11. Student Experiment Result Record

**Date of Execution:** ___________________
**Student Name:** _______________________
**Roll / Register No:** _________________
**Staff Signature:** ____________________

**Result:**
Git branch creation, switching, isolated commits, clean merging, and controlled merge-conflict generation and resolution were successfully executed and documented in an isolated demonstration clone, demonstrating how parallel feature branches integrate into verified deployment releases tracked in the Version History dashboard.

---

## 12. Viva Questions and Answers

1. **What is HEAD in Git?**
   *Answer:* `HEAD` is a symbolic pointer that references the currently checked-out branch or commit in your working directory. It typically points to a branch name (e.g., `refs/heads/main`), which in turn points to the latest commit.

2. **What is a "Detached HEAD" state?**
   *Answer:* A detached HEAD occurs when you check out a specific commit hash or tag directly (`git checkout <commit-hash>`) instead of a branch. Commits made in this state do not belong to any branch and can be lost if you switch branches without creating a new branch pointer.

3. **What are the three parts of a Git merge conflict marker?**
   *Answer:*
   - `<<<<<<< HEAD`: Indicates changes on your current active branch.
   - `=======`: The separator dividing the two conflicting versions.
   - `>>>>>>> <branch-name>`: Indicates changes coming from the branch being merged in.

4. **What is the command to completely cancel a merge that resulted in conflicts?**
   *Answer:* `git merge --abort`. It safely resets the index and working tree back to the pre-merge state.

5. **How does `git merge` differ from `git rebase`?**
   *Answer:* `git merge` preserves complete history by creating a dedicated merge commit with multiple parent references, maintaining true branch chronology. `git rebase` re-applies commits from one branch on top of another base tip, creating a linear history without merge commits, but altering commit hashes.
