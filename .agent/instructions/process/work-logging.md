# Process: History Archival

This instruction defines how the Agent maintains a persistent history of its work.

## 1. Trigger
This process is triggered when a top-level objective in `tasks/active/task.md` is fully completed (all items checked `[x]`).

## 2. Archival Procedure
Make a folder for the completed task in `tasks/archive/`:
- **Format**: `YYYY-MM-DD-TaskName` (e.g., `2023-10-25-Implement-Login`)
- **Move**: Move `tasks/active/task.md` and any specific context files to this new folder.

## 3. Work History Entry
Create or append to a daily log file in `work-history/YYYY/MM/`:
- **Directory**: `work-history/YYYY/MM/` (e.g., `work-history/2026/02/`)
- **File**: `YYYY-MM-DD.md` (e.g., `2026-02-09.md`)
- **Entry Format**:
  ```markdown
  ## [Time] Task Name
  - **Goal**: Brief description.
  - **Outcome**: Success/Failure status.
  - **Key Artifacts**: [Link to archived task.md]
  ```

## 4. Reset
Create a fresh, empty `tasks/active/task.md` template for the next objective.
