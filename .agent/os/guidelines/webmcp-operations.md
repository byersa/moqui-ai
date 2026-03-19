# WebMCP Operations Guide

**Purpose**: Correct usage of WebMCP for browser-to-AI communication in Moqui AI projects.

---

## Critical Distinction: Three Different "MCP" Systems

| System | Purpose | How to Use | Requires Token |
|--------|---------|------------|----------------|
| **webmcp** | WebSocket bridge: Qwen Code ↔ Browser widget | Start server, get token, user pastes in blue square | ✅ YES |
| **moqui-mcp** | AI agents ↔ Moqui ERP (browse screens, execute transitions) | HTTP JSON-RPC to `/mcp` endpoint | ❌ No (uses Moqui auth) |
| **Direct HTTP** | Simple web requests (curl, fetch) | Just use curl/HTTP | ❌ No |

**Common Mistake**: Don't confuse webmcp (browser widget) with moqui-mcp (ERP agent) or direct HTTP requests!

---

## WebMCP Workflow (Browser Widget)

### When to Use

- Need to **see what the user sees** in the browser
- Need to **interact with rendered Vue components**
- Need to **test UI behavior** (tabs, forms, dynamic content)
- Need to **diagnose rendering issues** that HTTP can't reveal

### Step 1: Check if Server is Running

```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js
```

If not running, you'll see a message to start it.

### Step 2: Start Server with MCP Enabled

```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --mcp
```

This runs as a **long-running background process**.

### Step 3: Generate Connection Token

```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --new
```

Output will be something like:
```
New connection token: abc123xyz789
```

### Step 4: User Pastes Token in Browser

1. User opens Moqui AI page in browser (e.g., `http://localhost:8080/aitree`)
2. User sees **blue square widget** (WebMCP connection UI)
3. User pastes the token
4. Connection established

### Step 5: AI Can Now Use MCP Tools

Once connected, the AI can:
- Inspect the rendered page
- Query Vue component state
- Trigger browser events
- Diagnose UI issues

---

## What WebMCP CANNOT Do

❌ **Bypass server restart requirements** - If Moqui cache is stale, webmcp can't fix it
❌ **Modify server-side files** - Still need file write tools
❌ **Replace HTTP debugging** - Sometimes curl is faster for simple checks
❌ **Work without user action** - User MUST paste the token

---

## Common Mistakes to Avoid

### Mistake 1: Using HTTP When WebMCP is Needed

**Wrong**: Using `curl` to diagnose UI rendering when you need to see the actual browser DOM

**Right**: Use webmcp when you need to:
- Check if Vue components rendered correctly
- Inspect Quasar component state
- Verify tab bar visibility in the actual browser

### Mistake 2: Confusing webmcp with moqui-mcp

**Wrong**: "Let me use moqui-mcp to check the browser rendering"

**Right**: 
- **webmcp** = Browser widget connection (this guide)
- **moqui-mcp** = ERP agent tools (different system, see `moqui-mcp/README.md`)

### Mistake 3: Not Starting the Server

**Wrong**: Generating a token when the server isn't running

**Right**: Always check/start server first, then generate token

### Mistake 4: Expecting Automatic Connection

**Wrong**: "I gave the token, why isn't it connected?"

**Right**: User must manually paste token in blue square widget - AI cannot do this

---

## Troubleshooting

### Token Doesn't Work
1. Verify server is running: `node websocket-server.js`
2. Generate fresh token: `node websocket-server.js --new`
3. Check browser console for errors
4. Verify blue square widget is visible

### Server Won't Start
1. Check if port is already in use
2. Stop existing instance: `node websocket-server.js --quit`
3. Try again: `node websocket-server.js --mcp`

### Connection Lost
1. Tokens may expire - generate new one
2. Server may have restarted - check status
3. Browser may need refresh

---

## When to Use Each Approach

| Task | Use This | Why |
|------|----------|-----|
| Check if page loads | Direct HTTP (curl) | Fast, no setup |
| Check JSON response | Direct HTTP with `?renderMode=qjson` | See raw blueprint |
| Check Vue component rendering | **webmcp** | Need browser DOM |
| Check tab bar visibility | **webmcp** | UI element in browser |
| Check screen cache issue | Direct HTTP + server restart | Server-side problem |
| Execute Moqui transitions | moqui-mcp | ERP agent operations |
| Modify files | File write tools | Not an MCP use case |

---

## Quick Reference Commands

```bash
# Check server status
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js

# Start server with MCP
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --mcp

# Generate new token
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --new

# Stop server
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --quit

# Test HTTP (no webmcp needed)
curl -s "http://localhost:8080/aitree/Meetings/ActiveScreens?renderMode=qjson"
```

---

## Related Documents

- `../workflows/webmcp.md` - Workflow definitions for webmcp management
- `../../moqui-mcp/README.md` - Moqui MCP (ERP agent system, different from webmcp)
- `./screen-blueprint-authoring.md` - Screen blueprint guidelines
- `./screen-syntax-checklist.md` - XML syntax validation

---

## Memory Note for AI

**CRITICAL**: If you say you're "using webmcp" but you're actually using `curl`, you're doing it wrong!

**Real webmcp usage requires**:
1. ✅ WebSocket server running
2. ✅ Connection token generated
3. ✅ User pasted token in blue square widget
4. ✅ MCP tools available for browser interaction

**If you skip these steps, you're just using HTTP** - which is fine for some tasks, but don't call it webmcp!
