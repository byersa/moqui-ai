# Playwright MCP Quick Start Guide

> Get started with Playwright MCP for visual browser development
> Created: 2025-10-01
> For: Moqui Framework Developers

## What is Playwright MCP?

Playwright MCP enables Claude Code to interact with browsers directly for:
- 🎨 **Visual Development**: Iterate on CSS with real-time feedback
- 🖼️ **Design Comparison**: Compare against reference images
- 🔍 **Element Inspection**: Debug layout and accessibility issues
- 🌐 **Cross-Browser Testing**: Verify consistency across Firefox and Chrome

## Quick Setup (5 minutes)

### Step 1: Verify Prerequisites

```bash
# Check Node.js and npx are installed
npx --version
# Should output version number (e.g., 10.9.3)
```

### Step 2: Test Playwright MCP

```bash
# Test the Playwright MCP server
npx @playwright/mcp@latest --help
# Should display help message with available options
```

### Step 3: Configure Claude Desktop

Create or edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

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

### Step 4: Restart Claude Desktop

After saving the configuration, **restart Claude Desktop** to load the MCP server.

### Step 5: Verify Installation

In Claude Code, try this command:
```
Open Firefox and navigate to https://www.example.com
```

If successful, you're ready to use Playwright MCP! 🎉

## Common Use Cases

### Use Case 1: Adjust CSS Spacing

**Goal:** Find the right padding for a header

**Prompt:**
```
Open localhost:8080/myapp/Dashboard in Firefox.
The header padding looks too tight. Try 10px, 15px, and 20px.
Show me screenshots of each option.
```

**Result:** You'll see three screenshots with different padding values to compare.

### Use Case 2: Compare Against Design

**Goal:** Match implementation to design mockup

**Prompt:**
```
Here's our approved design [attach image].
Open localhost:8080/products and compare the current implementation.
Show me what CSS needs to change to match the design.
```

**Result:** Specific CSS adjustments to match the design.

### Use Case 3: Cross-Browser Check

**Goal:** Verify UI looks the same in Firefox and Chrome

**Prompt:**
```
Open localhost:8080/myapp/ProductList in both Firefox and Chrome.
Take screenshots and show me any visual differences.
```

**Result:** Side-by-side comparison identifying any browser-specific rendering issues.

### Use Case 4: Debug Layout Issue

**Goal:** Understand why an element is misaligned

**Prompt:**
```
The submit button at localhost:8080/signup is overlapping the input field.
Inspect the button and tell me what CSS is causing this.
```

**Result:** Identification of the problematic CSS and suggested fix.

### Use Case 5: Test Responsive Design

**Goal:** Verify layout at different screen sizes

**Prompt:**
```
Test the dashboard at 1920px, 768px, and 375px widths.
Show me screenshots and identify any layout issues.
```

**Result:** Screenshots at each breakpoint with notes on responsive behavior.

## Common Workflows

### Workflow: CSS Iteration Loop

1. **Initial state**: Navigate to page and take screenshot
2. **Iterate**: Try different CSS values
3. **Compare**: Review screenshots
4. **Decide**: Pick the best option
5. **Implement**: Apply chosen CSS to source files

**Example:**
```
1. Open localhost:8080/dashboard
2. Try margin: 10px, 15px, 20px on .card elements
3. Show screenshots of all three options
4. [Review and decide]
5. Apply margin: 15px to the stylesheet
```

### Workflow: Design Implementation

1. **Reference**: Show design mockup or image
2. **Navigate**: Open the implementation
3. **Compare**: Identify differences
4. **Adjust**: Iterate on CSS to match
5. **Verify**: Confirm match with final screenshot

### Workflow: Cross-Browser Verification

1. **Firefox test**: Navigate and screenshot in Firefox
2. **Chrome test**: Navigate to same page in Chrome
3. **Compare**: Review both screenshots
4. **Fix**: Address any browser-specific issues
5. **Re-test**: Verify fixes work in both browsers

## Moqui URL Structure and Rendering Modes

Moqui provides three browser rendering modes, controlled by the URL prefix:

| Prefix | Mode | Description |
|--------|------|-------------|
| `/qapps` | **Quasar (Default)** | Vue.js + Quasar Material Design - recommended |
| `/vapps` | Vue.js | Standard Vue.js SPA |
| `/apps` | Server-rendered | Traditional HTML rendering |

**Always use `/qapps` for testing** unless specifically debugging a rendering mode issue.

### Correct URL Format

```
http://localhost:8080/qapps/{AppName}/{ScreenPath}

Examples:
http://localhost:8080/qapps/{AppName}/{ScreenPath}
http://localhost:8080/qapps/MyApp/Customers/FindCustomer
```

### Common Mistake

❌ **Wrong**: `localhost:8080/myapp/Dashboard` (missing `/qapps` prefix)
✅ **Correct**: `localhost:8080/qapps/MyApp/Dashboard`

For detailed information on rendering modes, see: `references/rendering_modes.md`

## Tips and Tricks

### 1. Use Clear Descriptions

❌ Bad: "Make it look better"
✅ Good: "Increase the top padding from 10px to 20px"

### 2. Test Incrementally

❌ Bad: Change 10 things at once
✅ Good: Change one thing, verify, then move to next

### 3. Provide Context

❌ Bad: "Fix the button"
✅ Good: "The submit button on /signup page overlaps the email input"

### 4. Specify URLs Completely

❌ Bad: "Open the dashboard"
✅ Good: "Open localhost:8080/myapp/Dashboard"

### 5. Request Screenshots

❌ Bad: "Change the color"
✅ Good: "Change the color and show me a screenshot"

## Advanced Techniques

### Multiple Viewport Testing

```
Test the product grid at these sizes:
- Desktop: 1920x1080
- Tablet: 768x1024
- Mobile: 375x667

Show me screenshots of all three.
```

### State-Based Testing

```
Show me the login form in these states:
1. Empty (initial state)
2. Validation error (try submitting empty)
3. Success (after successful login)
```

### Color Matching

```
Our brand blue is #1e40af.
Check all buttons on localhost:8080/dashboard and list any
that use a different blue color.
```

### Animation Testing

```
Open localhost:8080/modal-demo and test the modal animation.
Show me screenshots of:
1. Before opening
2. Mid-animation
3. Fully open
```

## Troubleshooting

### Issue: MCP Server Not Found

**Symptoms:** Claude Code doesn't recognize browser commands

**Solutions:**
1. Verify config file location: `~/Library/Application Support/Claude/claude_desktop_config.json`
2. Check config file syntax (valid JSON)
3. Restart Claude Desktop
4. Test npx command manually: `npx @playwright/mcp@latest --version`

### Issue: Browser Won't Open

**Symptoms:** Error messages about browser launch

**Solutions:**
1. Verify browser is installed (Firefox or Chrome)
2. Try different browser: change `--browser` to `"chrome"`
3. Check system permissions (macOS Security & Privacy)
4. Test browser manually

### Issue: Can't Access localhost

**Symptoms:** "Connection refused" or "Cannot reach"

**Solutions:**
1. Verify Moqui is running: `curl -k https://localhost:8443`
2. Check port number is correct (8443 for HTTPS, 8080 for HTTP)
3. Try full URL with protocol: `https://localhost:8443/myapp`
4. Accept certificate warnings if using HTTPS

### Issue: Screenshots Look Different Than Expected

**Symptoms:** Visual inconsistencies

**Solutions:**
1. Clear browser cache: restart browser
2. Verify correct URL (development vs staging vs production)
3. Check viewport size: specify exact dimensions
4. Wait for page load: "Wait for the page to fully load before taking screenshot"

### Issue: Slow Response

**Symptoms:** Long wait times for screenshots

**Solutions:**
1. Simplify request: focus on specific element
2. Reduce number of variants: test fewer options at once
3. Check network connection
4. Restart MCP server: restart Claude Desktop

## Keyboard Shortcuts & Commands

### Common Claude Code Phrases

```
# Navigation
"Open [URL] in Firefox"
"Navigate to localhost:8080/myapp"

# Screenshots
"Take a screenshot"
"Show me the current page"

# CSS Injection
"Try padding: 20px on .header"
"Change background to #1e40af"

# Element Inspection
"Inspect the submit button"
"Show me the computed styles for .card"

# Viewport
"Set viewport to 375x667"
"Test at mobile size"

# Comparison
"Open in both Firefox and Chrome"
"Compare these two screenshots"
```

## Integration with Moqui

### Moqui Screen Testing

```
Open https://localhost:8443/apps/myapp/EditProduct
[Authenticate if needed]
Test the form layout and field spacing
```

### Moqui REST API Testing

```
Open the browser console at localhost:8080
Execute this fetch request and show me the response:
fetch('/rest/{api-path}/v2/{resource}?limit=10')
```

### Multi-Tenant Testing

```
In Firefox (Development container):
1. Login as user from Organization A
2. Verify dashboard shows Organization A data

[Manually switch to different container]

3. Login as user from Organization B
4. Verify dashboard shows Organization B data
```

## Best Practices

### ✅ Do This

- Specify exact URLs with protocol and port
- Request screenshots to verify changes
- Test incrementally (one change at a time)
- Provide context about what you're trying to achieve
- Use specific CSS values and properties

### ❌ Avoid This

- Vague descriptions ("make it better")
- Making multiple changes without verifying
- Assuming changes work without screenshots
- Testing without clear success criteria
- Forgetting to specify the browser

## Next Steps

### Learn More

- **Visual Workflows**: See `.agent-os/playwright-visual-workflows.md` for detailed CSS iteration patterns
- **Cross-Browser**: See `.agent-os/playwright-cross-browser.md` for Firefox/Chrome testing
- **Setup Details**: See `.agent-os/playwright-mcp-setup.md` for advanced configuration
- **E2E Infrastructure**: See `.agent-os/playwright-e2e-infrastructure.md` for automated testing

### Practice Exercises

1. **CSS Spacing**: Adjust padding on a Moqui form header
2. **Color Matching**: Verify brand colors across multiple pages
3. **Responsive Test**: Test a dashboard at 3 different viewport sizes
4. **Cross-Browser**: Compare form rendering in Firefox and Chrome
5. **Debug Layout**: Fix an overlapping element issue

### Get Help

- **Framework Guide**: See `.agent-os/framework-guide.md`
- **Testing Strategy**: See `.agent-os/testing-guide.md`
- **Development Setup**: See `.agent-os/development-guide.md`

## Frequently Asked Questions

**Q: Can Playwright MCP edit source files directly?**
A: No. Playwright MCP is for browser testing only. Use Claude Code's Edit tool to modify source files.

**Q: Do changes persist after I close the browser?**
A: No. CSS injected via Playwright MCP is temporary. You must manually apply changes to source files.

**Q: Can I use Playwright MCP in CI/CD?**
A: No. Playwright MCP is for interactive development. Use traditional Playwright tests for CI/CD.

**Q: Does it work with Firefox Containers?**
A: Playwright MCP uses Firefox but doesn't directly control containers. Container switching must be done manually.

**Q: Can I test on real mobile devices?**
A: No. Playwright MCP uses desktop browsers with mobile viewport sizes. For real device testing, use traditional E2E tests.

**Q: How do I test across multiple browsers?**
A: Configure both Firefox and Chrome in your `claude_desktop_config.json`, then specify which browser in your prompt.

**Q: Can I run tests in parallel?**
A: Playwright MCP is for interactive, sequential testing. For parallel testing, use traditional Playwright test suites.

**Q: Does it support authentication?**
A: Yes. You can navigate to login pages and authenticate as needed during testing.

**Q: What about HTTPS certificate warnings?**
A: Playwright MCP can handle self-signed certificates in development. Use `--ignore-https-errors` if needed.

**Q: Can I test in production?**
A: Yes, but be careful. Playwright MCP can interact with any accessible URL, including production sites.

## Summary

**Key Takeaways:**
1. Playwright MCP enables interactive browser testing via Claude Code
2. Best for visual development, CSS iteration, and cross-browser verification
3. Complements (doesn't replace) traditional Playwright E2E tests
4. Requires minimal setup (5 minutes)
5. Works with any accessible URL (localhost, staging, production)

**When to Use:**
- 🎨 CSS debugging and iteration
- 🖼️ Design implementation comparison
- 🔍 Layout issue investigation
- 🌐 Cross-browser visual verification
- 🧪 Ad-hoc UI testing

**When Not to Use:**
- ❌ Automated regression tests (use traditional Playwright)
- ❌ CI/CD pipeline testing (use traditional Playwright)
- ❌ Large-scale parallel testing (use traditional Playwright)

Ready to start? Try the examples above and explore the detailed workflow documentation!
