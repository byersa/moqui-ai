---
description: Standard procedure for finishing a task, ensuring work is logged and synced.
---

# Finish Task Workflow

Follow this procedure when you believe you have completed a significant unit of work or are ending a session.

## 1. Log Work History
Before syncing code, update the daily work log. This ensures the commit history is enriched by the context of your work.

**Run the daily-summary skill:**
> Use the `daily-summary` skill to append your progress to `.agent/work-history/YYYY/MM/DD/summary.md`.

*If the skill is not available, manually create/update the file following the format in `.agent/instructions/process/work-logging.md`.*

## 2. Sync Repositories
Ensure your changes are safely pushed to the remote repository.

**Run the git-sync skill:**
> Use the `git-sync` skill for the relevant components (usually `moqui-ai` and `huddle`).

## 3. Update Status
Finalize the project tracking artifacts.

**Update task.md:**
- Mark completed items with `[x]`.
- Ensure the next steps are clear.

**Notify User:**
- Provide a summary of the work done.
- Confirm that work history and git sync are complete.
