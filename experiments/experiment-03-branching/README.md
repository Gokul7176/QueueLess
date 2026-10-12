# Experiment 3: Git Branching and Merging

**Course Code:** 23IT723 — DevOps Laboratory  
**Project:** QueueLess — End-to-End CI/CD Powered Virtual Queue Management System  

---

## 1. Aim
To create, isolate, and switch between feature branches for queue functionality, commit independent changes on separate branches, perform clean merges into the `main` branch, and demonstrate a controlled merge-conflict generation and resolution workflow.

---

## 2. Requirements & Prerequisites
- **Operating System:** Windows 10/11 or Linux
- **Software:** Git (v2.40+), Terminal (PowerShell or Bash)
- **Repository:** Active `C:\queuless` local repository on branch `main`

---

## 3. Theory

### What is a Branch in Git?
In Git, a branch is an extremely lightweight, movable pointer to a specific commit. Unlike older VCS systems where branching created complete directory copies, Git branching requires only 41 bytes of storage (a commit SHA-1/SHA-256 hash inside `.git/refs/heads/`).

### Branching Workflows in DevOps:
1. **Trunk-Based Development:** Developers push short-lived feature branches directly into `main` after automated test suites pass in CI.
2. **Feature Branching:** Every new feature, bugfix, or refactor is developed in an isolated branch (`feature/queue-priority`, `feature/notification-service`), preventing untested code from polluting `main`.

### Merging Mechanisms:
- **Fast-Forward Merge:** Target branch has no new commits since branching; the branch pointer is simply moved forward.
- **Three-Way Merge (True Merge):** Both branches have diverged with independent commits. Git identifies the common ancestor commit, combines changes from both tips, and creates a new **merge commit** with two parents.
- **Merge Conflict:** Occurs when changes on both branches modify the exact same lines of code within the same file differently. Git pauses the merge and inserts conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`), delegating manual resolution to the developer.

---

## 4. Step-by-Step Procedure

### Part A: Feature Branch Isolation & Clean Merge
1. Create a feature branch named `feature/priority-queue`.
2. Switch to the feature branch using `git switch` or `git checkout`.
3. Add a new configuration parameter in `src/main/resources/application.properties` (e.g., `queueless.priority-mode=standard`).
4. Commit changes on the feature branch.
5. Switch back to `main`.
6. Merge `feature/priority-queue` into `main`.
7. Delete the merged feature branch to keep the branch namespace tidy.

### Part B: Controlled Merge-Conflict Generation & Resolution
1. Create a feature branch `conflict-branch-a` and modify a specific line in `docs/queue-policy.txt`. Commit on `conflict-branch-a`.
2. Switch to `main`, create `conflict-branch-b`, and modify the exact same line in `docs/queue-policy.txt` with different content. Commit on `conflict-branch-b`.
3. Merge `conflict-branch-a` into `main` (merges cleanly).
4. Attempt to merge `conflict-branch-b` into `main` (triggers `CONFLICT (content)`).
5. Inspect conflict markers with `git status` and a text editor.
6. Resolve the conflict by choosing the intended content and removing the Git markers.
7. Stage the resolved file with `git add` and finalize with `git commit`.

---

## 5. Exact Commands to Execute

### Part A: Feature Branch Creation & Clean Merge
```powershell
cd C:\queuless

# Step 1: Create and switch to feature branch
git switch -c feature/priority-queue

# Step 2: Verify active branch
git branch

# Step 3: Add feature configuration
Add-Content -Path "src\main\resources\application.properties" -Value "`n# Priority Queue Feature Toggle`nqueueless.feature.priority-queue.enabled=true"

# Step 4: Stage and commit on feature branch
git add src\main\resources\application.properties
git commit -m "feat: add priority queue configuration toggle"

# Step 5: Switch back to main branch
git switch main

# Step 6: Merge feature branch into main
git merge feature/priority-queue

# Step 7: Delete merged branch
git branch -d feature/priority-queue
```

### Part B: Controlled Conflict Simulation & Resolution
```powershell
# Step 1: Create base file on main
Set-Content -Path "docs\queue-policy.txt" -Value "Queue Policy: Maximum wait time is 30 minutes."
git add docs\queue-policy.txt
git commit -m "docs: establish base queue policy"

# Step 2: Branch A modifies line
git switch -c conflict-branch-a
Set-Content -Path "docs\queue-policy.txt" -Value "Queue Policy: Maximum wait time is 15 minutes (Express Policy)."
git commit -am "policy: update wait time to 15 mins"

# Step 3: Switch back to main and create Branch B
git switch main
git switch -c conflict-branch-b
Set-Content -Path "docs\queue-policy.txt" -Value "Queue Policy: Maximum wait time is 45 minutes (Standard Policy)."
git commit -am "policy: update wait time to 45 mins"

# Step 4: Merge Branch A into main
git switch main
git merge conflict-branch-a

# Step 5: Merge Branch B into main (TRIGGERS CONFLICT)
git merge conflict-branch-b

# Step 6: Check conflict status
git status

# Step 7: View conflict markers in file
Get-Content docs\queue-policy.txt

# Step 8: Resolve conflict by writing desired combined policy
Set-Content -Path "docs\queue-policy.txt" -Value "Queue Policy: Express tickets 15 mins, Standard tickets 45 mins (Resolved)."

# Step 9: Stage resolved file and finalize merge commit
git add docs\queue-policy.txt
git commit -m "merge: resolve queue policy wait time conflict between branches A and B"

# Step 10: Clean up temporary branches
git branch -d conflict-branch-a conflict-branch-b
```

---

## 6. Actual Verification Method

1. Run `git log --graph --oneline -n 5` to view the branch convergence and resolution commit.
2. Run `git status` to ensure `working tree clean`.
3. Inspect `docs/queue-policy.txt` to verify conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`) are completely removed.

---

## 7. Expected Result vs. Observed Result

- **Expected Result:**  
  Part A merges with Fast-Forward. In Part B, `git merge conflict-branch-b` halts with `CONFLICT (content): Merge conflict in docs/queue-policy.txt. Automatic merge failed; fix conflicts and then commit the result.` After staging and committing, `git status` reports working tree clean.

- **Observed Result:**  
  ```
  Auto-merging docs/queue-policy.txt
  CONFLICT (content): Merge conflict in docs/queue-policy.txt
  Automatic merge failed; fix conflicts and then commit the result.
  [main a37b98f] merge: resolve queue policy wait time conflict between branches A and B
  ```

---

## 8. Screenshot Checklist

- [ ] `exp03_01_git_branch_list.png`: Output of `git branch` showing current branch highlighted.
- [ ] `exp03_02_feature_merge.png`: Output of `git merge feature/priority-queue` showing clean merge.
- [ ] `exp03_03_conflict_detected.png`: Terminal output of `git merge conflict-branch-b` showing `CONFLICT (content)`.
- [ ] `exp03_04_conflict_markers.png`: Text editor or terminal showing `<<<<<<< HEAD`, `=======`, and `>>>>>>> conflict-branch-b`.
- [ ] `exp03_05_resolved_git_log_graph.png`: Terminal output of `git log --graph --oneline` showing the diamond merge graph.

---

## 9. Common Errors and Troubleshooting

| Error | Root Cause | Solution |
|:---|:---|:---|
| `error: you need to resolve your current index first` | Attempted a new merge or branch switch while a merge conflict is unresolved. | Resolve conflict markers, run `git add <file>`, and complete `git commit`. Or abort the merge using `git merge --abort`. |
| `error: The branch '...' is not fully merged` | Tried deleting an unmerged branch with `git branch -d`. | If deliberate, force deletion using `git branch -D <branch-name>`. |
| Conflict markers left in source files | Incomplete resolution before committing. | Search for `<<<<<<<` across all project files before committing to prevent syntax errors. |

---

## 10. Student Experiment Result Record

**Date of Execution:** ___________________  
**Student Name:** _______________________  
**Roll / Register No:** _________________  
**Staff Signature:** ____________________  

**Result:**  
Git branch creation, switching, isolated commits, clean three-way merging, and controlled merge-conflict generation and resolution were successfully executed and documented.

---

## 11. Viva Questions and Answers

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
