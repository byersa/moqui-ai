# Architecture: Moqui Core

8. **2026-02-03 (Navigation):** Established the "Root-as-Dashboard" pattern. The `huddle.xml` screen serves as the landing page; no separate `Home.xml` artifact is permitted. This reduces artifact bloat and simplifies the Moqui subscreen hierarchy.

13. **System-level boilerplate** (like `MoquiConf.xml`) must use variable interpolation (`${componentName}`, `${componentTitle}`) instead of hardcoded logic. The `componentTitle` should be sourced from the root blueprint's frontmatter to ensure a single source of truth.

15. **Component-wide variables** (e.g., `componentTitle`) must be anchored in the frontmatter of the root orchestrator blueprint (e.g., `huddle.md`). All automated skills must treat this file as the authoritative source for interpolation data.
