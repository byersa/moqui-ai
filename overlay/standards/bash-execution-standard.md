# Bash Execution Standard (BES)

To ensure terminal stability, all bash execution MUST adhere to the Universal Task Execution Protocol (UTEP) defined in `/home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/overlay/universal-task-execution-protocol.md`.

## Mandatory Practices
1. **Always use Fenced Execution**: Never call raw commands.
2. **Standard Output Silencing**: Redirect stdout and stderr to a file buffer.
3. **Task Completion Marker**: Always check for `--DONE--`.
4. **Non-Interactive**: Never use commands like `cp -i` or `rm -i`. Use `-f` and `-y` by default.

5. **STRICT PROHIBITION**: Never use shell commands to poll GET /mcp/sse. Always use JSON-RPC over HTTP POST for tool checks to ensure the command has a finite termination point.