---
name: Daily Summary Reader
description: Reads and displays the daily work journal from the moqui-ai component.
alias: daily-summary
---

# Daily Summary Reader

This skill reads the formatted daily journal file for a specific date.

## Usage

`/daily-summary [date]`

-   `date` (optional): The date to retrieve the summary for, in `YYYY-MM-DD` format. Defaults to the current date if omitted.

## Instructions

1.  **Determine the Target Date:**
    -   If the user provides a date argument (e.g., `2026-02-05`), use it.
    -   If no date is provided, interpret "today" using the `current_time` from the system prompt/metadata.

2.  **Construct the File Path:**
    -   Extract the Year and Month (`YYYY-MM`) from the target date.
    -   Path pattern: `runtime/component/moqui-ai/.agent/work-history/[YYYY-MM]/[YYYY-MM-DD].md`
    -   Example: `runtime/component/moqui-ai/.agent/work-history/2026-02/2026-02-06.md`

3.  **Action:**
    -   Use `run_command` to check if the file exists (e.g., `ls [path]`).
    -   If it exists, use `view_file` to read the content.
    -   If it does *not* exist, inform the user: "No daily summary found for [date]."

4.  **Presentation:**
    -   Display the content of the file to the user.
    -   If the user asked for a summary of the summary, you may summarize it further, but primarily you should show the log.
