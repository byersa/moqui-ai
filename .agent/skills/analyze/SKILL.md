# Skill: Huddle Dashboard Analysis
**Alias:** `/analyze-dashboard`

## Protocol
1. **Session Check:** Read the current `sessionId` from `runtime/mcp-session.json`.
2. **Visual Request:** Call `moqui_browse_screens` via MCP with `path: "huddle"`.
3. **Semantic Parsing:** Use `jq` to filter for high-value ARIA roles (grids, headings, buttons).
4. **Insight:** Describe the current UI state of the Nursing Home system.

```bash
# 1. Fetch Session Data
SESSION_FILE="../../../mcp-session.json"
SID=$(jq -r '.sessionId' $SESSION_FILE)
URL=$(jq -r '.url' $SESSION_FILE)
AUTH=$(jq -r '.auth' $SESSION_FILE)

# 2. Call the MARIA Browser
RESPONSE=$(curl -s -X POST "$URL" \
  -u "$AUTH" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -H "Mcp-Session-Id: $SID" \
  -d '{
    "jsonrpc": "2.0", 
    "method": "tools/call", 
    "params": {
      "name": "moqui_browse_screens",
      "arguments": { "path": "huddle", "renderMode": "aria" }
    }, 
    "id": 2
  }')

# 3. Extract the ARIA Tree for the AI to summarize
echo "$RESPONSE" | jq '.result.content[0].text | fromjson'