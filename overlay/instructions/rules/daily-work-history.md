# Rule: Daily Work History Protocol

**Status**: MANDATORY LAW

## Context
We maintain a detailed, structured history of all work performed in the `moqui-ai` component. This ensures continuity and provides a clear audit trail of changes, decisions, and accomplishments.

## The Rule
1.  **Daily Entry**: At the end of every work session (or day), you MUST create or update a work history entry.
2.  **Location**: The entry must be located at `runtime/component/moqui-ai/.agent/work-history/YYYY/MM/DD/`.
3.  **Naming Convention**: The file should be named descriptively, e.g., `topic-of-work.md`.
4.  **Content**: The entry must include:
    *   **Objectives**: What were you trying to do?
    *   **Accomplishments**: What did you actually do? (Be specific about files changed, bugs fixed, architectural decisions).
    *   **Next Steps**: What is left to do?
5.  **Git Commit**: You must commit and push this file to the `moqui-ai` repository.

## Enforcement
*   Before closing a session or responding to a "goodbye"/"end of day" message, check if the work history has been updated.
*   If not, update it immediately.
