# Process: Artifacts

14. All AI artifacts (Skills, Blueprints) must use YAML frontmatter for metadata. The 'alias' in the YAML block is the definitive trigger for the Agent. Markdown headers should be used for human-readable titles and instructions only.

17. **Continuous Journaling:** The Agent must maintain a persistent daily journal of all "Thinking Tasks" (coherent units of work).
    - **Target Directory:** `runtime/component/moqui-ai/.agent/work-history/[YYYY-MM]/`
    - **File Name:** `[YYYY-MM-DD].md` (Daily Journal)
    - **Trigger:** At the conclusion of every `VERIFICATION` phase (or when a significant logical task is completed).
    - **Action:** Append a new section to the daily journal file summarizing the task, the changes made, and the outcome. This ensures a granular, searchable history of the project's evolution.
    - **Artifact Mirroring:** In addition to the journal entry, specific artifacts (`implementation_plan.md`, `walkthrough.md`) used during the task must be copied to the same directory, named with the task slug (e.g., `[YYYY-MM-DD]-walkthrough-fix-fop-error.md`).
