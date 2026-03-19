# WebMCP Usage Guide for Qwen Code

## Quick Start

**To use WebMCP tools, I need:**

1. **Server running** with `--mcp` flag
2. **Browser connected** with token (blue square widget)
3. **Use the CLI bridge** to call tools

## Commands

### 1. Check Server Status
```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js
```

### 2. Start Server (if not running)
```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --mcp
```

### 3. Generate Connection Token
```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --new
```

### 4. List Available Tools
```bash
node runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js list-tools
```

### 5. Call Tools
```bash
# Ping browser to verify connection
node runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js call-tool localhost_8080-ping_browser

# Navigate to a page
node runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js call-tool localhost_8080-navigate '{"path": "/aitree/Home"}'

# Click element by MARIA ID
node runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js call-tool localhost_8080-click_element '{"mariaId": "btn-submit"}'

# Set form field value
node runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js call-tool localhost_8080-set_field_value '{"mariaId": "name", "value": "John"}'
```

## Available Tools

| Tool | Description | Parameters |
|------|-------------|------------|
| `localhost_8080-ping_browser` | Verify browser connection and get current URL | none |
| `localhost_8080-navigate` | Navigate to a different screen | `{"path": "/aitree/..."}` |
| `localhost_8080-click_element` | Click button/link by MARIA ID | `{"mariaId": "..."}` |
| `localhost_8080-set_field_value` | Set form field value | `{"mariaId": "...", "value": "..."}` |

## Architecture

```
Qwen Code (shell commands)
    ↓
webmcp-cli.js (CLI bridge)
    ↓ WebSocket (ws://localhost:4797/mcp)
websocket-server.js --mcp
    ↓ WebSocket channel
Browser (webmcp.js widget)
```

## Key Files

- **CLI Bridge**: `runtime/component/moqui-ai/screen/moquiai/js/webmcp-cli.js`
- **WebSocket Server**: `runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js`
- **Browser Widget**: `runtime/component/moqui-ai/screen/moquiai/js/webmcp.js`
- **Logs**: `webmcp.log` (project root)

## Troubleshooting

### Tools not found / channel empty
- Browser disconnected - generate new token and reconnect
- Check logs: `tail -30 webmcp.log`

### Server not running
```bash
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --quit
node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --mcp
```

### Token invalid
- Server was restarted - generate fresh token
- Browser needs page refresh before pasting new token

## Connection Flow

1. Start server: `--mcp` flag
2. Generate token: `--new`
3. User pastes token in blue square widget (bottom-right of browser)
4. Widget shows "Connected" with registered tools listed
5. CLI can now call tools via MCP path

## Important Notes

- Tool names include channel prefix: `localhost_8080-<tool>`
- MCP path (`/mcp`) doesn't require auth token
- Browser channel (`/localhost_8080`) requires valid token
- Tools timeout after 30 seconds
- Browser must stay on page for tools to work

## Created: 2026-03-18
## Author: Qwen Code (via discovery session with user)
