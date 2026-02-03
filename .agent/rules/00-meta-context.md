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

