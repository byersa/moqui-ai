# Process: Work Logging

This instruction defines how the Agent maintains a **continuous, running history** of its work, organized by day.

## 1. Triggers (When to Log)
1.  **End of Session**: Before calling `notify_user` to return control to the human, you MUST append a progress entry.
2.  **Task Completion**: When a top-level item in `task.md` is finished.

## 2. The Daily Log
**Location**: `.agent/work-history/YYYY/MM/DD/` (e.g., `2026/02/09/`).
-   **Primary Log**: `summary.md`
-   **Artifacts**: Place any day-specific artifacts (diagrams, screenshots, dumps) in this folder.

### Entry Format (`summary.md`)
Append to the file:
```markdown
### [Timestamp] Action/Update
- **Activity**: [Brief description of what was done]
- **Status**: [Blocked / In Progress / Completed]
- **Changes**:
    - Modified `Blueprints/...`
    - Created `instructions/...`
```

## 3. Archival (Cleanup)
*Only* when a major implementation task is *fully* complete:
1.  Move the specific `tasks/active/task.md` to `tasks/archive/YYYY-MM-DD-TaskName/`.
2.  Reset `tasks/active/task.md` for the next objective.
