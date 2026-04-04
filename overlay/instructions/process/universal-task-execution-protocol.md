# Universal Task Execution Protocol (UTEP)

This protocol is mandatory for all shell-based operations to prevent terminal hangs and ensure reliable task verification.

## 1. Terminal Sanitization (Safety First)
*   **Non-Interactive Execution**: Do not execute any interactive bash commands. Always use non-interactive flags (e.g., `-y`, `-f`).
*   **Environment Setup**: Prefix every bash command with `TERM=dumb` and set `JAVA_OPTS` for Java 21 compatibility:
    ```bash
    TERM=dumb export JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED" && <command>
    ```
*   **Fenced Execution Pattern**: For any file move, shell script, or command-line task that might take time, wrap the command in a subshell:
    ```bash
    { (<command>) > tmp/task_output.txt 2>&1; echo "--DONE--" >> tmp/task_output.txt; } &
    ```
*   **Status Verification**: Instead of waiting for the terminal tool's return status, verify completion by reading `tmp/task_output.txt` with `view_file`. If the file contains `--DONE--`, the task is complete. Always use the local **workspace root `tmp/`** directory.
*   **Unbuffered Output**: Prefix utility commands with `PYTHONUNBUFFERED=1` or equivalent to ensure real-time capture.

## 2. Universal REST Service Runner
Instead of using `gradlew runService` for every task (which is heavy and prone to hangs), use the `GeneralRunner` REST endpoint when the server is running.

*   **Endpoint**: `POST /rest/s1/moquiai/execute`
*   **Authentication**: Basic Auth (`john.doe:moqui`)
*   **Security**: Restricted to `nursinghome` and `moquiai` service nouns.

### Example Usage (with curl)
```bash
curl -u john.doe:moqui -X post -H "Content-Type: application/json" \
    -d '{ "serviceName": "nursinghome.DeveloperServices.get#CompiledSpecs", "parameters": { "specPath": "component://aitree/screen/aitree/specs/Meetings.md" } }' \
    http://localhost:8080/rest/s1/moquiai/execute
```

## 3. Automation Policy
The AI should prioritize triggering system actions (compilation, discovery, database updates) via the REST endpoint rather than local shell commands when the Moqui server is running.

## 4. How to Find This Protocol
This document is located at: `runtime/component/moqui-ai/overlay/instructions/process/universal-task-execution-protocol.md`. 
AI agents should always check the `overlay/instructions` directory for operational protocols before performing system-level tasks.
