# Revamped .agent Directory Structure

## Goal
Make the `.agent` directory the central nervous system for the user's interaction with MoquiAi. It should be intuitive, self-documenting, and treat "instructions" as a form of source code.

## 1. Directory Structure

### A. For Platform Components (`moqui-ai`, `moqui-mcp`)
```
moqui-ai/
├── blueprints/             # Technical Specifications (Top-Level)
│   ├── strategy/           # High-level vision & goals.
│   └── component/          # Mirrored runtime structure (e.g., service/MyService.md).
├── .agent/                 # The "Factory" (Hidden)
│   ├── config/             # Settings (JSON/YAML).
│   ├── instructions/       # The "Code" that programs the Agent.
│   │   ├── rules/          # Mandatory constraints (Governance).
│   │   └── process/        # Procedure guides (History, Workflows).
│   ├── skills/             # Agent capabilities.
│   ├── work-history/       # Execution logs (YYYY/MM/YYYY-MM-DD.md).
│   └── tasks/              # Active task tracking.
└── component.xml           # Runtime definition.
```

### B. For Solution Components (`huddle`)
```
huddle/
├── blueprints/             # Technical Specifications (Top-Level)
│   └── component/huddle/   # Specific specs for this solution.
├── .agent/                 # The "Helper" (Hidden)
│   ├── config/             # Local settings.
│   ├── instructions/       # Local rules/overrides.
│   ├── work-history/       # Execution logs (YYYY/MM/DD/task.md).
│   └── tasks/              # Active task tracking.
├── screen/                 # The Code.
└── component.xml
```

### Key Principles
1.  **Blueprints are First-Class**: They live at the root, visible to humans.
2.  **Taxonomy**: 
    *   **Platform Components**: Define the rules and tools.
    *   **Solution Components**: Build the product using those tools.
3.  **History**: Stored in `.agent/work-history/YYYY/MM/` to keep the root clean.

## 2. "Instructions as Code"
We treat instructions not as chat messages, but as **source files** that are "compiled" into the final app.

- **Concept**: Instead of telling the agent "Change the button color to blue" in chat (which is ephemeral), you update `.agent/instructions/ui/theme.md` with "Primary buttons must be blue."
- **Compilation**: The agent reads these instruction files as its "System Prompt" or "Context" before performing tasks.
- **Benefit**:
    - **Persistence**: Preferences survive across chat sessions.
    - **Version Control**: Instructions can be committed to Git.
    - **Reproducibility**: Another agent can read these instructions and produce the same result.

## 3. Configuration & Local Parameters
- **`settings.json`**: Shared team settings (e.g., naming conventions, linting rules). Committed to Git.
- **`credentials.json`**: Local-only secrets (DB passwords, API keys). **NEVER** committed.
- **Setup**: The [README.md](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/component/moqui-ai/README.md) will include a "Getting Started" section that guides the user to copy `credentials.example.json` to `credentials.json` and fill it in.

## 4. Workflows & Skills
- **Workflows are "Macro-Skills"**: A workflow is a sequence of simpler skills.
- **Task Complexity**:
    - tasks can be recursive. A high-level task ("Build e-commerce site") breaks down into sub-tasks, each of which might trigger a workflow.
    - **Agent Autonomy**: The agent can be instructed to *refine* its own task list. "Agent, review `tasks/active/task.md` and break down item #3 into sub-steps based on `workflows/new-feature.md`."

## 5. Documentation
- **Start Here**: The root [README.md](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/component/moqui-ai/README.md) explains the directory structure and how to drive the agent.
- **Docs Folder**: `.agent/documentation/` contains generated documentation for the *agent's* operations, distinct from the *app's* documentation.
