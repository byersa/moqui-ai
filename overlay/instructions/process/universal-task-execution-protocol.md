# Universal Task Execution Protocol (UTEP)

This protocol is mandatory for all shell-based operations to prevent terminal hangs and ensure reliable task verification. **Failure to follow these protocols is considered a system failure.**

## 1. Local Workspace Tmp Directory
To avoid permission issues and ensure persistent access without user approval for each write, all temporary files, logs, and command outputs MUST be written to the local component directory:
`runtime/component/moqui-ai/tmp/`

**NEVER use `/tmp/`**. 

## 2. Terminal Sanitization (Mandatory)
*   **Non-Interactive Execution**: Do not execute any interactive bash commands. Always use non-interactive flags (e.g., `-y`, `-f`).
*   **Environment Setup**: Prefix every bash command with `TERM=dumb` and set `JAVA_OPTS` for Java 21 compatibility.
*   **Fenced Execution Pattern**: For any command-line task, wrap the command in a subshell that writes to a uniquely named file in the local `moqui-ai/tmp/` directory:
    ```bash
    TERM=dumb export JAVA_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED" && { (<command>) > runtime/component/moqui-ai/tmp/<task_uuid>_output.txt 2>&1; echo "--DONE--" >> runtime/component/moqui-ai/tmp/<task_uuid>_output.txt; } &
    ```
*   **Status Verification**: Instead of waiting for the terminal tool's return status, verify completion by reading the specific output file in `runtime/component/moqui-ai/tmp/` using `view_file`. If the file contains `--DONE--`, the task is complete.
*   **Unbuffered Output**: Prefix utility commands with `PYTHONUNBUFFERED=1` or equivalent to ensure real-time capture.

## 3. Universal REST Service Runner
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

## 4. Automation Policy
The AI should prioritize triggering system actions (compilation, discovery, database updates) via the REST endpoint rather than local shell commands when the Moqui server is running.

## 5. How to Find This Protocol
This document is located at: `runtime/component/moqui-ai/overlay/instructions/process/universal-task-execution-protocol.md`. 
AI agents should always check the `overlay/instructions` directory for operational protocols before performing system-level tasks.
