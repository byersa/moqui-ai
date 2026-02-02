# Skill: MCP Bridge Connection
**Alias:** `/connect`

## Protocol
1. **Handshake:** Perform `initialize` POST to Moqui.
2. **Persistence:** Save the resulting `sessionId` to `runtime/mcp-session.json`.
3. **Verification:** Call `tools/list` to confirm the bridge is healthy.

```bash
# Perform the handshake
RESPONSE=$(curl -s -X POST http://localhost:8080/mcp \
  -u "john.doe:moqui" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"jsonrpc": "2.0", "method": "initialize", "params": {"protocolVersion": "2024-11-05", "capabilities": {}, "clientInfo": {"name": "ag-architect", "version": "1.0.0"}}, "id": 0}')

# Extract the session ID (Assuming 'jq' is installed, otherwise use sed)
SESSION_ID=$(echo $RESPONSE | jq -r '.result.sessionId')

# Cache it for the rest of the sprint
echo "{\"sessionId\": \"$SESSION_ID\", \"timestamp\": \"$(date)\"}" > runtime/mcp-session.json

echo "Connected! Session ID: $SESSION_ID is now cached."

