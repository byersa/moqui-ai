# Playwright MCP Setup Guide

> Framework-level infrastructure for visual browser development and testing
> Created: 2025-10-01
> Updated: 2025-12-21
> Status: Active

## Overview

Playwright MCP (Model Context Protocol) enables Claude Code to interact with browsers directly for visual development, CSS iteration, and cross-browser verification. This is a framework-level tool usable across all Moqui components.

## Quick Links by Environment

| Environment | Configuration Guide |
|-------------|---------------------|
| **Claude Code CLI** | See `playwright-mcp-claude-code.md` (uses `.mcp.json`) |
| **Claude Desktop App** | Continue reading this document |

**For Moqui screen development workflows**, see `playwright-moqui-screens.md`.

## Prerequisites

- Node.js and npm/npx installed
- Claude Desktop application
- Firefox and/or Chrome browsers

## Installation

### 1. Install Playwright MCP Server

The official Microsoft Playwright MCP server is available via npx (no global installation needed):

```bash
# Test installation
npx @playwright/mcp@latest --help
```

### 2. Configure Claude Desktop

Create or edit the Claude Desktop configuration file:

**macOS Location:** `~/Library/Application Support/Claude/claude_desktop_config.json`

**Configuration:**

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "firefox"
      ]
    }
  }
}
```

### 3. Restart Claude Desktop

After adding the configuration, restart Claude Desktop to load the MCP server.

## Browser Configuration

### Firefox with Multi-Account Containers

For development work using Firefox Multi-Account Containers:

```json
{
  "mcpServers": {
    "playwright-firefox": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "firefox"
      ]
    }
  }
}
```

**Note:** Firefox Multi-Account Containers are a Firefox extension feature and require manual browser profile management. The MCP server will use the default Firefox profile.

### Chrome for Production Testing

For production-like testing with Chrome:

```json
{
  "mcpServers": {
    "playwright-chrome": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "chrome"
      ]
    }
  }
}
```

### Multi-Browser Configuration

You can configure both browsers simultaneously:

```json
{
  "mcpServers": {
    "playwright-firefox": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "firefox"
      ]
    },
    "playwright-chrome": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "chrome"
      ]
    }
  }
}
```

## Available Browsers

Playwright MCP supports:
- `chrome` - Chromium-based browser
- `firefox` - Mozilla Firefox
- `webkit` - Safari's engine (macOS)
- `msedge` - Microsoft Edge
- `chrome-beta`, `chrome-dev`, `chrome-canary` - Chrome channels

## Security Options

### Restricting Hosts

Limit which hosts the browser can access:

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "firefox",
        "--allowed-hosts", "localhost", "127.0.0.1"
      ]
    }
  }
}
```

### Blocking Origins

Block specific origins or service workers:

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "@playwright/mcp@latest",
        "--browser", "firefox",
        "--blocked-origins", "https://ads.example.com;https://tracking.example.com",
        "--block-service-workers"
      ]
    }
  }
}
```

## Verification

After configuration, verify the MCP server is available in Claude Code:

1. Open Claude Code
2. Check for Playwright tools in the available tools list
3. Test with a simple command like "Open Firefox and navigate to localhost:8080"

## Troubleshooting

### MCP Server Not Loading

1. Verify the configuration file path is correct
2. Check that npx is in your PATH: `which npx`
3. Test the server manually: `npx @playwright/mcp@latest --version`
4. Review Claude Desktop logs for errors

### Browser Not Launching

1. Ensure the browser is installed
2. Try a different browser with `--browser` flag
3. Check browser permissions on macOS (System Preferences > Security & Privacy)

### Connection Refused

If Claude Code reports connection errors:
1. Restart Claude Desktop
2. Verify no other process is using the browser
3. Check firewall settings

## Use Cases

### Visual Development
- Iterate on CSS styling with real-time feedback
- Compare designs against reference images
- Test responsive layouts across viewports

### Cross-Browser Verification
- Verify UI consistency between Firefox and Chrome
- Test browser-specific features
- Validate production behavior

### API Testing
- Test Moqui REST endpoints in browser context
- Verify authentication flows
- Debug CORS and cookie behavior

## Related Documentation

- Claude Code CLI Setup: `playwright-mcp-claude-code.md`
- Moqui Screen Development: `playwright-moqui-screens.md`
- Quick Start Guide: `playwright-quick-start.md`
- Visual Workflows: `playwright-visual-workflows.md`
- Testing Guide: `testing-guide.md`

## Limitations

- Framework infrastructure only (no component-specific tests)
- Manual browser profile management for containers
- MCP server runs per-session (no persistent state)
- Some Firefox features (like containers) require manual setup

## Next Steps

1. Review visual development workflows documentation (Task 2)
2. Set up cross-browser verification patterns (Task 3)
3. Integrate with existing testing guide (Task 4)
4. Explore E2E infrastructure setup (Task 5)
