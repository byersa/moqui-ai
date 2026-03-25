# MoquiAi .agent Directory

This directory contains agent-specific logic, knowledge, and guidelines for the **MoquiAi** project.

## Reference Library
- [Moqui AI Community Alignment](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/.agent/references/moqui-ai-community-alignment.md): The core blueprint for unified development.
- [Screen Macro Extensions](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/.agent/references/moqui-ai-macro-extensions.md): Technical dive into the custom XML DSL.
- [MCP & MARIA Infrastructure](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/.agent/references/moqui-mcp-and-maria-infrastructure.md): Semantics and agent connectivity.
- [Blueprint-Driven Development (BDD)](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/.agent/references/moqui-ai-blueprint-driven-development.md): The spec-to-code generation workflow.

## Relationship with `moqui-agent-os`

This project follows a **Shadowing / Overlay Pattern** relative to [moqui-agent-os](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-agent-os).

- **Global Context**: Base framework guides, coding standards, and generic templates are sourced from `runtime/component/moqui-agent-os`.
- **Local Context**: Project-specific strategies (e.g., Blueprints, WebMCP integration, Aitree-specific logic) are contained in this directory.

### Structural Alignment
The directory structure mirrors `moqui-agent-os` to make it easy to find corresponding local overrides:
- `guidelines/`: MoquiAi-specific architectural patterns.
- `instructions/`: Setup and integration guides for MoquiAi features.
- `standards/`: Local coding and security standards.
- `templates/`: Blueprints and code snippets specific to this project.
- `references/`: Unique knowledge items (KIs) and research notes.

### Agent Protocol
When working in this project, AI agents should:
1. Load base context from `moqui-agent-os`.
2. Load local context from this `.agent` directory.
3. If a local file exists with a similar purpose to a global one, prioritize the local logic but **notify the user** if there is a significant contradiction or if the global "OS" version appears more up-to-date and superior.
