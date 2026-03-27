Mandatory Terminal Synchronization Rules:

Non-Interactive Only: Never run commands that require user input. Always use flags like mv -f or npx --yes.

Fenced Execution: Never run raw commands. Wrap execution to redirect output to a buffer and append a completion marker:
{ ([COMMAND]) > /tmp/ag_out.txt 2>&1; echo "--DONE--" >> /tmp/ag_out.txt; } &.

Verification via Filesystem: Disregard the "Running..." state in the UI. Use view_file on /tmp/ag_out.txt. If it contains --DONE--, the task is finished.

Unbuffered Output: Prefix Python commands with PYTHONUNBUFFERED=1 to prevent stalling.