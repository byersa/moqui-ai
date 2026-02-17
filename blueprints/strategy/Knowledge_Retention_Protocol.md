# Protocol: Knowledge Retention

**Goal**: Ensure that all "Operating Knowledge" (architectural decisions, functional logic, and state) is captured in persistent documents rather than ephemeral chat history.

## The Knowledge Hierarchy

1.  **Work History (`.agent/work-history/`)**: Chronological logs of *what* was done and *when*. Useful for forensic analysis.
2.  **Strategy Blueprints (`blueprints/strategy/`)**: High-level vision, rules, and protocols (like this one).
3.  **Functional Blueprints (`blueprints/component/`)**: The source of truth for *how* a component or system works. These evolve over time.
4.  **Instructions (`.agent/instructions/`)**: The constraints and preferences that guide the Agent's generation logic.

## The "Reflect" Protocol

After any significant implementation or refactor, the Agent and User must perform a **Reflect** task to synchronize the documentation with the code.

### 1. Identify Changes
What logic, architecture, or configuration was changed?

### 2. Update Functional Blueprints
Identify the relevant file in `blueprints/component/`. If it doesn't exist, create it.
Update the document to reflect the *new reality* of the code.

### 3. Update Instructions
Update any relevant `.agent/instructions/` files to ensure future generations follow the new pattern.

### 4. Link Code to Documentation
(Optional) Add `@instruction` or `@blueprint` tags in the source code to provide a direct link for future agents.

## Implementation Standard
- Documents should be written in Markdown.
- Use clickable `file:///` links to reference specific code files or templates.
- Avoid duplicate information; cross-reference instead.
