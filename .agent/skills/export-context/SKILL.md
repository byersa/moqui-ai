---
alias: export-context
type: system-utility
version: 1.0
---

# Skill: Export-Context

## Purpose
Collects and packages all AI-related "Long-Term Memory" (rules, skills, blueprints) into a single portable zip file for context hydration in new sessions.

## Logic
1. **Target Identification:** Identify the `moqui-ai` component and any user-specified custom components (e.g., `huddle`).
2. **Directory Scan:** - Locate all `.agent/` directories within the target components.
   - Filter for `rules/`, `skills/`, and `blueprints/`.
3. **Exclusion Filter:** Ignore all non-agent artifacts (`.xml`, `.groovy`, `.java`, `build/`, `node_modules/`).
4. **Execution:**
   - Execute a shell command (via MCP) to `zip` the selected directories.
   - Name the output: `[component]-ai-context-[timestamp].zip`.
5. **Output:** Provide the file path to the user for download/upload.