---
trigger: always_on
---

# Governance: Workspace Confinement

### Rule 1: No External File Generation
The Agent must NOT generate, create, or modify files outside of the current workspace directory (e.g., `/home/byersa/IdeaProjects/huddle-ai-project`).

### Rule 2: Exception for .agent Directory
The `.agent` directory within the workspace is the designated location for agent-related configuration, rules, and memory. System directories like `~/.gemini` should strictly be used for internal agent state and history, never for user-facing artifacts unless explicitly requested for archival.

### Rule 3: Explicit User Consent for System Directories
If a task requires interacting with files outside the workspace (e.g., system configuration, unrelated projects), the Agent must obtain explicit user consent before proceeding.

### Rule 4: File naming
Do not create files with spaces in their names. Use the "-" or "_" character to delineate. CamelCase naming is also acceptable.
