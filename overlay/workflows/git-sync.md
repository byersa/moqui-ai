---
description: Synchronize a Git repository for a specific component.
---

This workflow automates the process of committing and pushing changes for a Moqui component, ensuring remote changes are integrated first using a rebase.

// turbo-all
1. Use the `git-sync` skill found in `runtime/component/moqui-ai/.agent/skills/git-sync/` to synchronize the repository.

If you don't use the skill directly, follow these steps:
1. Navigate to the component directory: `cd runtime/component/{{component}}`
2. Check git status: `git status`
3. Stage all changes: `git add .`
4. Review the staged changes: `git diff --cached --stat`
5. Commit the changes: `git commit -m "{{message}}"`
6. Fetch remote changes: `git fetch --all`
7. Pull and rebase to integrate remote changes: `git pull --rebase`
8. Push the changes: `git push`
9. Report the final status and the commit hash.
