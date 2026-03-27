# Universal Task Execution Protocol (UTEP)

This protocol is mandatory for all shell-based operations to prevent terminal hangs and ensure reliable task verification.

## Core Rules
1. **Non-Interactive Execution**: Do not execute any interactive bash commands. Always use non-interactive flags (e.g., `-y`, `-f`).
2. **Fenced Execution Pattern**: For any file move, shell script, or command-line task, wrap the command in a subshell that appends a `--DONE--` marker to a temporary file.

### Implementation Example
```bash
{ ([COMMAND]) > /home/byersa/IdeaProjects/aitree-project/tmp/task_output.txt 2>&1; echo "--DONE--" >> /home/byersa/IdeaProjects/aitree-project/tmp/task_output.txt; } &
```

3. **Status Verification**: Instead of waiting for the terminal tool's return status (which may hang or be inaccurate in some environments), verify completion by reading the temporary file with `view_file`. If the file contains `--DONE--`, the task is complete. Always use the local **workspace root `tmp/`** directory to avoid redundant authorization prompts.
4. **Unbuffered Output**: Prefix utility commands (like Python or long-running scripts) with `PYTHONUNBUFFERED=1` or equivalent to ensure output is captured in real-time.

