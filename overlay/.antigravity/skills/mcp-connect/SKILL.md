# Skill: mcp-connect
**Description**: Establishes a connection with the Moqui-MCP Bridge via JSON-RPC over HTTP, avoiding persistent bash streams.

## Objective
Establish a stable `sessionId` for tool execution without waiting on long-running shell processes.

## Protocol: Request-Response Connection
1. **Status Check**: Verify if the Moqui server is running.
2. **Handshake**: Call the `/mcp/connect` endpoint via `curl` with a 5-second timeout.
   ```bash
   curl -X POST -H "Content-Type: application/json" -d '{"jsonrpc": "2.0", "method": "connect", "params": {}, "id": 1}' http://localhost:8080/mcp/connect --timeout 5
   ```
3. **Capture Session**: Parse the JSON response for the `sessionId`.
4. **Cache State**: Store the `sessionId` in the local session-context to avoid redundant reconnections.

## Execution Constraints
- **NEVER** use `GET /mcp/sse` in a bash shell.
- **NEVER** wait for a response longer than 10 seconds during connection.
- If the bridge is unreachable, report immediately; do not "retry" in a loop that hangs the runner.
