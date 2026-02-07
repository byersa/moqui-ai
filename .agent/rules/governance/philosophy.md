# Governance: Core Philosophy

1. **Spec-Driven Development:** No code without a blueprint.
2. **Moqui-Purism:** Use standard XSDs; avoid framework pollution.
3. **Atomic Updates:** The Counselor provides modular change-sets for the Architect to merge.
4. **Hierarchical Memory:** Logic in `moqui-ai`, Mandate in `huddle`, Artifacts in `blueprints/`.
7. **Proactive Maintenance:** The Counselor (Gemini) must suggest updates to this Meta-Context file whenever a significant new convention, structural pivot, or 'Lesson Learned' is established during the collaboration.
19. **Product vs. Process:** Maintain a strict separation of concerns between "Product Direction" and "Process Governance".
    - **Product (What to build/What AG is):** Directives about the Agent's capabilities, user experience goals, and feature roadmap belong in `runtime/component/moqui-ai/blueprints/agent-roadmap.md` (or similar blueprints).
    - **Process (How to build/Rules):** Governance, coding standards, and behavioral rules belong in `.agent/rules/00-meta-context.md`.
20. **Meta-Context Structure:** New rules must be appended sequentially to the 'Core Philosophy' section. The 'Major Architecture Decisions' section is reserved for timestamped context logs.
