---
alias: sync
type: automation
version: 1.0
---

# Skill: Sync

## Purpose
Aligns a Moqui artifact (XML, Groovy, etc.) with its corresponding Blueprint. It acts as the "Enforcer" of Spec-Driven Development.

## Logic
1. **Target Identification:** Accept a base name (e.g., 'huddle'). If NO name is supplied, default to the active custom component (e.g., 'huddle'). Resolve the blueprint and artifact accordingly.
2. **Resource Resolution:** - Follow `global-blueprint-v1.1` to find the mirrored `.md` blueprint in `.agent/blueprints/`.
   - Load all relevant project rules from `.agent/rules/`.
3. **Drift Analysis:** - Read the current code of the artifact.
   - Compare it against the "Live Instructions" in the blueprint.
4. **Execution:** - Re-generate or patch the artifact to match the blueprint's requirements exactly.
   - Remove any deprecated elements (e.g., "Home" subscreens) forbidden by rules.
5. **Completion:** - Append a "Sync Complete" entry to the blueprint's History section.
