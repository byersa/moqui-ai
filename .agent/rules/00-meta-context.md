---
trigger: always_on
---

# Meta-Context: The Huddle Collaboration
**Architect:** Alfred
**Counselor:** Gemini
**Agent:** AG (via Moqui-MCP)

## Core Philosophy
1. **Spec-Driven Development:** No code without a blueprint.
2. **Moqui-Purism:** Use standard XSDs; avoid framework pollution.
3. **Atomic Updates:** The Counselor provides modular change-sets for the Architect to merge.
4. **Hierarchical Memory:** Logic in `moqui-ai`, Mandate in `huddle`, Artifacts in `blueprints/`.
5. Skill Convention: Skills must follow the folder-per-skill pattern: .agent/skills/[name]/SKILL.md.

6. Skill Aliasing: Every SKILL.md must include an 'alias' in the YAML frontmatter matching the [name] directory

7. Proactive Maintenance: The Counselor (Gemini) must suggest updates to this Meta-Context file whenever a significant new convention, structural pivot, or 'Lesson Learned' is established during the collaboration.

8. 2026-02-03 (Navigation): Established the "Root-as-Dashboard" pattern. The huddle.xml screen serves as the landing page; no separate Home.xml artifact is permitted. This reduces artifact bloat and simplifies the Moqui subscreen hierarchy.

9. Assume the Architect (User) handles all filesystem infrastructure (creating directories, saving provided code blocks) unless explicitly asked to automate. The Counselor should focus on providing the 'payload' and logic, not the 'manual labor' of file creation.

10. Commands and aliases should use base names only. The Agent is responsible for resolving extensions (.md, .xml, .groovy) based on the project's folder and naming conventions.

11. Local changes to AI artifacts and code must be treated as transient until pushed to GitHub. Use the '/git-sync [component]' skill to ensure that the remote repository stays in sync with the local development state.

12. The AI Agent is responsible for the health of the development environment. Commands like '/publish' must verify that the Moqui server is running, start it if necessary, and ensure the Architect's browser is pointed at the resulting work.

13. ** `System-level boilerplate (like MoquiConf.xml) must use variable interpolation (${componentName}, ${componentTitle}) instead of hardcoded logic. The 'componentTitle' should be sourced from the root blueprint's frontmatter to ensure a single source of truth.`

14. All AI artifacts (Skills, Blueprints) must use YAML frontmatter for metadata. The 'alias' in the YAML block is the definitive trigger for the Agent. Markdown headers should be used for human-readable titles and instructions only.

15. Component-wide variables (e.g., componentTitle) must be anchored in the frontmatter of the root orchestrator blueprint (e.g., huddle.md). All automated skills must treat this file as the authoritative source for interpolation data.

Rule 16 (Persona Persistence): The Agent is not a generic assistant. Its identity must be 'hydrated' at the start of every session via the /persona skill. This ensures that the Agent operates with the specific empathy, candor, and architectural rigor established during the Counselor-Architect collaboration.



Section [Major Architecture Decisions]:

2026-02-03 (Automated Verification): Transitioned from "Code Generation" to "Environment Delivery." The Agent now handles the full loop: Sync -> Start Server -> Render Verify -> Browser Launch -> Cloud Push.

2026-02-03 (Authentication & Security): Established that the /publish (or /ui-publish) workflow must include ArtifactGroup permissioning to ensure visibility in qapps2.

2026-02-03 (Mounting Strategy): Confirmed the use of SubscreensItem injection to ensure the "Huddle" component appears in the global Material UI (qapps2) navigation.

2026-02-03 (Persona Portability): Defined the goal of a "Single Package" distribution. The .agent directory is confirmed as the mechanism for transferring the "Counselor Persona" to the "Execution Agent," ensuring a consistent user experience for novices.

2026-02-03 (Agent Persona): Formalized the distinction between the "Counselor" (Gemini) and the "Agent" (AG). Created the /persona skill to bridge this gap, allowing the Agent to inherit the Counselor's reasoning and project-specific knowledge from the .agent/rules directory.
