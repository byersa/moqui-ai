---
alias: git-sync
type: utility
version: 1.0
---

# Skill: Git-Sync

## Purpose
Automates the staging, committing, and pushing of changes for a specific Moqui component to GitHub.

## Logic
1. **Target Identification:** Identify the component directory from the parameter (e.g., `huddle`).
2. **Status Check:** Run `git status` to ensure there are changes to commit.
3. **Staging:** Execute `git add .` within the component directory.
4. **Commit:** Prompt the user for a message (or generate a summary based on the Blueprints) and execute `git commit -m "[message]"`.
5. **Push:** Execute `git push origin [current-branch]`.
6. **Verification:** Report the commit hash and confirm the push was successful.