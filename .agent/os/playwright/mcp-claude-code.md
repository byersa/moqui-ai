# Playwright MCP for Claude Code CLI

> Configuration and usage guide for Playwright MCP with Claude Code CLI
> Created: 2025-12-21
> Status: Active

## Overview

This guide covers using Playwright MCP specifically with Claude Code CLI (the terminal-based Claude). For Claude Desktop app configuration, see `playwright-mcp-setup.md`.

## Configuration

### Project-Scoped Configuration (.mcp.json)

Claude Code CLI uses a `.mcp.json` file in the project root for MCP server configuration. This is the recommended approach as it:
- Makes the configuration version-controlled and shareable
- Automatically applies when working in the project directory
- Doesn't require global configuration changes

**Current configuration** (`.mcp.json` in project root):

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp@latest"]
    }
  }
}
```

### Configuration Options

| Option | Description |
|--------|-------------|
| `command` | The executable to run (`npx`) |
| `args` | Arguments passed to npx |
| `-y` | Auto-confirm npx prompts |
| `@playwright/mcp@latest` | Use latest Playwright MCP package |

### Adding Browser Specification

To specify a default browser:

```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": ["-y", "@playwright/mcp@latest", "--browser", "firefox"]
    }
  }
}
```

**Available browsers:** `chrome`, `firefox`, `webkit`, `msedge`

## Verification

### Check MCP Status

Run in Claude Code CLI:
```
/mcp
```

**Expected output:**
- Server "playwright" should show as connected
- If not connected, Claude Code may need a restart

### Test Basic Functionality

Ask Claude to:
1. "Open Firefox and navigate to https://example.com"
2. "Take a screenshot of the current page"
3. "Inspect the h1 element on the page"

## Differences from Claude Desktop

| Aspect | Claude Code CLI | Claude Desktop |
|--------|-----------------|----------------|
| Config location | `.mcp.json` (project root) | `~/Library/Application Support/Claude/claude_desktop_config.json` |
| Scope | Per-project | Global |
| Version control | Yes (checked into repo) | No |
| Restart required | Sometimes (on config change) | Always |

## Common Commands

### Navigation
```
Open Firefox and navigate to https://localhost:8443/apps/myapp
```

### Screenshots
```
Take a screenshot of the current page
Show me the login form
```

### CSS Testing
```
Inject CSS to change the header background to #1e40af
Try padding: 20px on the .card elements
```

### Element Inspection
```
Inspect the submit button and show me its computed styles
What's causing the overlap between these elements?
```

### Viewport Testing
```
Set viewport to 375x667 (mobile)
Show me the page at tablet size (768px wide)
```

## Troubleshooting

### MCP Server Not Loading

1. **Verify configuration syntax**
   ```bash
   cat .mcp.json | jq .
   ```

2. **Check npx availability**
   ```bash
   which npx
   npx --version
   ```

3. **Test Playwright MCP manually**
   ```bash
   npx -y @playwright/mcp@latest --help
   ```

4. **Restart Claude Code CLI session**
   - Exit and restart the terminal session
   - The MCP server loads when Claude Code starts

### Browser Won't Open

1. **Verify browser is installed**
   ```bash
   # For Firefox
   which firefox || which firefox-esr

   # For Chrome
   which google-chrome || which chromium
   ```

2. **Check system permissions** (macOS)
   - System Settings → Privacy & Security → Automation
   - Ensure Terminal/iTerm has permission to control browsers

3. **Try different browser**
   - Modify `.mcp.json` to use `--browser chrome` instead

### Connection Refused to Localhost

1. **Verify Moqui is running**
   ```bash
   curl -k https://localhost:8443/status
   ```

2. **Check correct port**
   - HTTPS: `8443` (default for Moqui)
   - HTTP: `8080`

3. **Accept self-signed certificates**
   - Playwright MCP handles this by default
   - If issues persist, try `--ignore-https-errors` flag

### Slow Performance

1. **First run is slow** - Playwright downloads browser binaries
2. **Subsequent runs** should be faster
3. **If consistently slow** - Check network connection and available disk space

## Best Practices

### 1. Use Explicit URLs
```
# Good
Open https://localhost:8443/apps/{app-name}/Dashboard

# Avoid
Open the dashboard
```

### 2. Request Screenshots for Verification
```
# Good
Change the header color to blue and show me a screenshot

# Avoid
Change the header color to blue (no verification)
```

### 3. One Change at a Time
```
# Good
First, adjust the padding to 20px. Then show me the result.

# Avoid
Change padding, margin, font-size, and colors all at once.
```

### 4. Provide Context
```
# Good
The submit button on /signup overlaps the email input. Fix it.

# Avoid
Fix the button.
```

## Integration with Screen Development

When developing Moqui screens:

1. **After making screen changes** - Use Playwright MCP to visually verify
2. **For CSS adjustments** - Test values in browser before committing
3. **For layout debugging** - Inspect elements to understand CSS conflicts
4. **For EntityFilter issues** - Visually confirm data appears/disappears correctly

See `playwright-moqui-screens.md` for detailed screen development workflows.

## Security Considerations

### Local Development Only
The default configuration works with localhost. For security:
- Avoid committing configurations that access production URLs
- Be cautious with authentication on non-local sites

### Restricting Hosts (Optional)
```json
{
  "mcpServers": {
    "playwright": {
      "command": "npx",
      "args": [
        "-y", "@playwright/mcp@latest",
        "--allowed-hosts", "localhost", "127.0.0.1"
      ]
    }
  }
}
```

## Related Documentation

- Setup Guide: `playwright-mcp-setup.md` (includes Claude Desktop config)
- Quick Start: `playwright-quick-start.md`
- Visual Workflows: `playwright-visual-workflows.md`
- Screen Development: `playwright-moqui-screens.md`
- Testing Guide: `testing-guide.md`
