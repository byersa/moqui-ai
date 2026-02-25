# Prompt History: Logging and Sync Automation

- **Timestamp**: 2026-02-24T14:49:12-07:00
- **User Prompt**: "create a git-sync.sh file", "change our prompt-history strategy to be to archive only when I ask", "add a workflow for me to call when I know that a session is over"

## Technical Analysis
The user identified three friction points in the existing workflow:
1. **Log Noise**: Automatic "significant" prompt archiving was inconsistent and lead to duplication across components (`moqui-ai` vs `aitree`).
2. **Git Friction**: Running raw git pulls during active development often failed due to unstaged changes, requiring manual intervention.
3. **Session Handover**: Lack of a standardized "shutdown" procedure slowed down the end of the day and resulted in inconsistent `summary.md` updates.

## Decision & Rationale
1. **Slash Command Architecture**: Shifted from implicit to explicit archiving. By implementing `/savePrompt`, the user retains control over which technical cycles are worth a permanent technical archive. The logic was codified in a new `moqui-ai` workflow.
2. **Commit-Before-Pull Scripting**: Wrote `git-sync.sh` to automate the `add -> commit -> pull -> push` sequence. This specifically prevents the `error: cannot pull with rebase: You have unstaged changes` error that frequently blocks rapid synchronization.
3. **Component Separation Policy**: Updated `work-history-standards.md` to mandate that platform-level changes (XSD, Macros, Vue Components) live in `moqui-ai` while solution-level changes live in the relevant app folder. This preserves the "Moqui-AI as a Framework" abstraction.
4. **Automated Session Lifecycle**: Created `/endSession` to chain documentation, synchronization, and model/mode switching (Gemini Flash / Fast mode) into a single command, reducing developer cognitive load at the end of a shift.

## Impact
- **Sanitized Work History**: Future agents will see a cleaner log of architectural decisions without redundant solution-specific noise.
- **Operational Bypass**: The user can now sync components independently of the agent using the provided shell script.
- **Improved SPA Reliability**: The underlying work on Pinia and Quasar Tabs (summarized today) is now correctly documented across the distinct component boundaries.
