---
trigger: always_on
---

# Meta-Context: The Huddle Collaboration
**Architect:** Alfred
**Counselor:** Gemini
**Agent:** AG (via Moqui-MCP)

## Core Philosophy (Federated)
Rule 21 (Federated Rule Structure): The Agent must respect the domain-driven rule hierarchy located in `.agent/rules/`.
- `00-meta-context.md`: The Root Index.
- `governance/`: Philosophy, Behavior, and Persona rules.
- `architecture/`: Technical Constraints (Moqui, Frontend).
- `process/`: Workflows, Git, and Artifact management.

Redundant rules should be avoided. Specific guidelines are authoritative within their respect subdirectories.

Section [Major Architecture Decisions]:
2026-02-03 (Automated Verification): Transitioned from "Code Generation" to "Environment Delivery." The Agent now handles the full loop: Sync -> Start Server -> Render Verify -> Browser Launch -> Cloud Push.

2026-02-03 (Authentication & Security): Established that the /publish (or /ui-publish) workflow must include ArtifactGroup permissioning to ensure visibility in qapps2.

2026-02-03 (Mounting Strategy): Confirmed the use of SubscreensItem injection to ensure the "Huddle" component appears in the global Material UI (qapps2) navigation.

2026-02-03 (Persona Portability): Defined the goal of a "Single Package" distribution. The .agent directory is confirmed as the mechanism for transferring the "Counselor Persona" to the "Execution Agent," ensuring a consistent user experience for novices.

2026-02-03 (Agent Persona): Formalized the distinction between the "Counselor" (Gemini) and the "Agent" (AG). Created the /persona skill to bridge this gap, allowing the Agent to inherit the Counselor's reasoning and project-specific knowledge from the .agent/rules directory.
