---
description: Archive the current prompt/response cycle into prompt-history
---
# /savePrompt <targetComponent>

This workflow archives the technical details of the current prompt and its resolution into the `prompt-history` of the specified component.

1. **Parameters**:
   - `<targetComponent>`: (Optional) The name of the Moqui component (e.g., `moqui-ai`, `aitree`). Defaults to `moqui-ai` if not provided.

2. **Actions**:
   - Create a new markdown file in `runtime/component/<targetComponent>/.agent/work-history/YYYY/MM/DD/prompt-history/`.
   - Name the file with an incremental prefix (e.g., `04-feature-name.md`).
   - Populate the file with:
     - **Timestamp**: Current ISO timestamp.
     - **User Prompt**: Summary of the user's intent.
     - **Technical Analysis**: Explanation of the technical challenge or root cause.
     - **Decision & Rationale**: Why the specific implementation or fix was chosen.
     - **Impact**: Success criteria or changes to the system.

3. **Guideline**:
   - Only use this when explicitly invoked by the user.
   - For all other interactions, update the `summary.md` at the end of the session to reflect changes made.
