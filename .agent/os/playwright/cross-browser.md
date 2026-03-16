# Playwright MCP Cross-Browser Verification Guide

> Framework-level guide for testing across Firefox and Chrome
> Created: 2025-10-01
> Status: Active

## Overview

Cross-browser verification ensures consistent user experience across different browsers. This guide covers testing patterns for Firefox (development) and Chrome (production) using Playwright MCP.

## Browser Configuration Strategy

### Development: Firefox with Multi-Account Containers

**Purpose:** Isolate testing contexts for different organizations/users

**Configuration:**
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

**Firefox Containers Setup:**
- Firefox Multi-Account Containers must be configured manually in Firefox
- Playwright MCP uses the default Firefox profile
- Containers provide cookie/session isolation within the browser
- Useful for testing multi-tenant applications

**Limitations:**
- Container selection not directly controllable via Playwright MCP
- Manual container switching required during testing
- Consider using separate Firefox profiles for different test scenarios

### Production: Chrome

**Purpose:** Verify production-like behavior and rendering

**Configuration:**
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

**Chrome Profile Management:**
- Playwright uses temporary profiles by default
- Persistent profiles can be used for authenticated sessions
- Chrome DevTools integration available

### Dual Browser Setup

Run both browsers simultaneously:

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

**Usage:**
```
Open the dashboard in both Firefox and Chrome, show me screenshots
of both to compare rendering differences.
```

## Cross-Browser Testing Workflows

### Workflow 1: Visual Consistency Check

**Goal:** Ensure UI looks the same in both browsers

**Steps:**
1. Navigate to page in Firefox
2. Take screenshot
3. Navigate to same page in Chrome
4. Take screenshot
5. Compare screenshots side-by-side
6. Identify differences

**Example Prompt:**
```
Open localhost:8080/myapp/ProductList in both Firefox and Chrome.
Take screenshots and identify any visual differences in:
- Layout/spacing
- Font rendering
- Colors
- Border rendering
- Shadows/gradients
```

**Common Differences to Check:**
- Font rendering (antialiasing, weights)
- Border radius rendering
- Shadow rendering
- Gradient rendering
- Flexbox/Grid layout edge cases
- CSS custom properties
- Transform/animation behavior

### Workflow 2: Functionality Verification

**Goal:** Ensure interactive features work in both browsers

**Steps:**
1. Test feature in Firefox (development)
2. Verify expected behavior
3. Test same feature in Chrome
4. Compare behavior
5. Document any browser-specific issues

**Example Prompt:**
```
Test the file upload feature in both Firefox and Chrome:
1. Select a PDF file
2. Verify upload progress indicator
3. Check success message
4. Verify file appears in list

Show me any differences in behavior or UI feedback.
```

**Areas to Test:**
- Form submissions
- File uploads
- Drag and drop
- Keyboard navigation
- Focus management
- Modal dialogs
- Dropdown menus
- AJAX requests
- WebSocket connections

### Workflow 3: Performance Comparison

**Goal:** Identify performance differences between browsers

**Steps:**
1. Load page in Firefox and measure
2. Load same page in Chrome and measure
3. Compare metrics
4. Identify bottlenecks

**Example Prompt:**
```
Load the dashboard at localhost:8080/myapp in Firefox and Chrome.
Measure and compare:
- Page load time
- Time to interactive
- Resource loading
- JavaScript execution time
```

### Workflow 4: Responsive Behavior

**Goal:** Verify responsive design works across browsers

**Steps:**
1. Test multiple viewports in Firefox
2. Test same viewports in Chrome
3. Compare layout behavior at breakpoints
4. Identify browser-specific responsive issues

**Example Prompt:**
```
Test the product grid at 1920px, 1280px, 768px, and 375px widths
in both Firefox and Chrome. Identify any differences in how the
layout adapts at each breakpoint.
```

## Browser-Specific Considerations

### Firefox Considerations

**Rendering Differences:**
- Different font rendering engine (may appear smoother/sharper)
- Subtle differences in border-radius on small elements
- Transform/animation timing may differ slightly
- Flexbox gap property has different rounding behavior

**Development Advantages:**
- Better DevTools for CSS Grid
- Network throttling in DevTools
- Responsive design mode
- Accessibility inspector

**Testing Focus:**
- Layout consistency
- CSS Grid behavior
- Font rendering
- Border/shadow rendering

### Chrome Considerations

**Rendering Differences:**
- Chromium rendering engine (Blink)
- May handle subpixel rendering differently
- Better performance with complex animations
- Different scrollbar styling

**Production Advantages:**
- Wider user base (most common browser)
- Better JavaScript performance
- Consistent with Edge (also Chromium)
- Chrome DevTools features

**Testing Focus:**
- Production-like performance
- JavaScript execution
- Memory usage
- Resource loading

### Common Browser Issues

| Issue | Firefox | Chrome | Solution |
|-------|---------|--------|----------|
| Font rendering | Smoother antialiasing | Sharper rendering | Accept difference or use web fonts |
| Scrollbars | Native OS style | Custom Chrome style | Use CSS scrollbar styling or accept |
| Flexbox gap | Slightly different rounding | Standard rounding | Use margin fallback for critical layouts |
| Date inputs | Native datepicker | Chrome datepicker | Use custom datepicker for consistency |
| File inputs | Different button style | Different button style | Custom file input styling |
| Select dropdowns | Native dropdown | Native dropdown | Custom select component if needed |

## Testing Patterns

### Pattern 1: Parallel Comparison

Test both browsers simultaneously:

**Prompt Template:**
```
Open [URL] in both Firefox and Chrome.
Test [feature/interaction].
Show me screenshots from both browsers and list any differences.
```

**Use Cases:**
- Quick visual comparison
- Initial implementation verification
- Regression testing

### Pattern 2: Sequential Deep Dive

Test one browser thoroughly, then verify in other:

**Prompt Template:**
```
1. Open [URL] in Firefox
2. Test complete [workflow]
3. Document all steps and outcomes
4. Repeat exact same steps in Chrome
5. Identify any behavioral differences
```

**Use Cases:**
- Complex multi-step workflows
- Form submission flows
- Authentication processes

### Pattern 3: Issue Reproduction

Reproduce reported browser-specific bug:

**Prompt Template:**
```
User reports [issue] in [browser].
1. Reproduce in reported browser
2. Verify issue exists
3. Test in alternate browser
4. Determine if browser-specific
5. Propose solution
```

**Use Cases:**
- Bug investigation
- Browser compatibility issues
- User-reported problems

### Pattern 4: Release Verification

Pre-release cross-browser check:

**Prompt Template:**
```
Before releasing [feature]:
1. Test critical user paths in Firefox
2. Test same paths in Chrome
3. Verify responsive behavior in both
4. Check performance in both
5. Document any issues
```

**Use Cases:**
- Pre-release validation
- Feature completion check
- QA handoff

## Multi-Tenant Testing with Firefox Containers

### Container Benefits

Firefox Multi-Account Containers enable:
- Session isolation (different users simultaneously)
- Cookie separation (multiple logins)
- Development/staging/production separation
- Client A vs Client B testing

### Container Setup

**Manual Setup Required:**
1. Install Firefox Multi-Account Containers extension
2. Create containers for different contexts:
   - "Development" (local testing)
   - "Staging" (staging environment)
   - "Production" (production environment)
   - "Client A", "Client B", etc. (multi-tenant testing)

### Testing Multi-Tenant Apps

**Example Prompt:**
```
I'm testing a multi-tenant Moqui app at localhost:8080.

In Firefox (Development container):
1. Login as user from Organization A
2. Verify dashboard shows Organization A data
3. Take screenshot

Then (manually switch to "Client B" container):
4. Login as user from Organization B
5. Verify dashboard shows Organization B data
6. Take screenshot

Compare the two screenshots to verify data isolation.
```

**Note:** Container switching must be done manually as Playwright MCP doesn't directly control Firefox Containers.

## Moqui-Specific Cross-Browser Testing

### Moqui Application Testing

**Common Moqui UI Elements to Test:**
- Form layouts (Moqui auto-generated forms)
- Grid layouts (entity lists)
- Screen transitions
- Modal dialogs
- Date/time pickers
- File uploads
- Nested screens

**Example Prompt:**
```
Test the Moqui form at https://localhost:8443/apps/myapp/EditProduct
in both Firefox and Chrome:

1. All form fields render correctly
2. Validation messages appear consistently
3. Submit button behavior is identical
4. Success redirect works in both
5. Date picker works in both browsers
```

### Moqui REST API Testing

While MCP focuses on browser UI, API testing can be done:

**Example Prompt:**
```
Open the browser console in both Firefox and Chrome.
Navigate to localhost:8080/rest/{api-path}/v2/{resource}
Execute this fetch request:
```javascript
fetch('/rest/{api-path}/v2/{resource}?limit=10', {
  headers: { 'Authorization': 'Bearer [token]' }
}).then(r => r.json()).then(console.log)
```
Verify response is identical in both browsers.
```

### SSL/HTTPS Behavior

Moqui default development uses HTTPS (port 8443):

**Certificate Warnings:**
- Firefox and Chrome handle self-signed certificates differently
- Both require manual acceptance in development
- Test with valid certificates in staging/production

## Automation Considerations

### When to Automate

Use Playwright MCP for:
- ✅ Quick visual comparisons
- ✅ Interactive debugging
- ✅ CSS iteration
- ✅ One-off verifications

Use traditional Playwright for:
- ✅ Automated CI/CD testing
- ✅ Regression test suites
- ✅ Scheduled testing
- ✅ Large-scale parallel testing

### Hybrid Approach

Combine both approaches:

1. **Development Phase:** Use Playwright MCP for visual iteration
2. **Implementation Phase:** Write traditional Playwright tests
3. **CI/CD Phase:** Run automated Playwright tests
4. **Maintenance Phase:** Use MCP for investigating issues

## Performance Profiling

### Firefox DevTools

**Example Prompt:**
```
Open localhost:8080/dashboard in Firefox.
Open DevTools and profile:
1. Initial page load
2. Interaction with product grid
3. Modal dialog open/close

Show me performance bottlenecks.
```

### Chrome DevTools

**Example Prompt:**
```
Open localhost:8080/dashboard in Chrome.
Use Performance panel to record:
1. Page load
2. Scroll through product list
3. Apply filters

Compare with Firefox performance.
```

## Accessibility Testing

### Cross-Browser Accessibility

**Example Prompt:**
```
Test keyboard navigation at localhost:8080/myapp in both browsers:

1. Tab through all interactive elements
2. Verify focus indicators
3. Test Enter/Space on buttons
4. Test Escape to close modals
5. Verify screen reader announcements

Document any browser differences.
```

### ARIA Support

Different browsers may handle ARIA differently:
- Test with screen readers (NVDA in Firefox, ChromeVox in Chrome)
- Verify ARIA live regions work in both
- Check ARIA label support

## Common Testing Checklist

### Pre-Release Cross-Browser Verification

- [ ] Visual consistency across browsers
- [ ] All forms submit correctly
- [ ] Navigation works identically
- [ ] Modals/dialogs function properly
- [ ] File uploads work
- [ ] Date/time pickers work
- [ ] Responsive layouts adapt correctly
- [ ] No console errors in either browser
- [ ] Performance acceptable in both
- [ ] Accessibility features work in both

### Browser-Specific Bug Investigation

- [ ] Reproduce issue in reported browser
- [ ] Attempt to reproduce in alternate browser
- [ ] Identify browser-specific CSS/JS
- [ ] Check browser version differences
- [ ] Review browser console for errors
- [ ] Test with browser DevTools
- [ ] Document workaround if needed

## Troubleshooting

### Different Rendering

**Issue:** Element looks different between browsers

**Investigation:**
1. Inspect computed styles in both browsers
2. Check for vendor prefixes
3. Verify CSS features support
4. Check font rendering differences
5. Review subpixel rendering

**Solutions:**
- Add vendor prefixes if needed
- Use feature detection
- Normalize font rendering with CSS
- Accept minor visual differences

### JavaScript Errors in One Browser

**Issue:** Code works in one browser but not another

**Investigation:**
1. Check browser console
2. Verify JavaScript feature support
3. Check polyfill requirements
4. Review async/timing issues
5. Test with different browser versions

**Solutions:**
- Add polyfills for missing features
- Use feature detection
- Transpile modern JavaScript
- Add browser-specific fixes

### Performance Differences

**Issue:** Page slow in one browser

**Investigation:**
1. Profile in both browsers
2. Compare network timing
3. Check JavaScript execution
4. Review rendering performance
5. Identify browser-specific bottlenecks

**Solutions:**
- Optimize critical rendering path
- Reduce JavaScript execution
- Optimize resource loading
- Consider browser-specific optimizations

## Best Practices

### 1. Test in Both Browsers Regularly

Don't wait until the end to test cross-browser:
- Test during development (Firefox)
- Verify in Chrome before committing
- Catch issues early

### 2. Document Browser Differences

Keep a log of known differences:
- Font rendering variations (acceptable)
- Scrollbar styling (acceptable)
- Critical functional issues (must fix)

### 3. Use Feature Detection

Don't assume feature availability:
```javascript
if ('IntersectionObserver' in window) {
  // Use IntersectionObserver
} else {
  // Fallback
}
```

### 4. Normalize Where Possible

Use CSS resets/normalizers:
- Normalize.css
- CSS reset
- Browser-specific resets

### 5. Accept Minor Differences

Not everything needs to be pixel-perfect:
- Font rendering ✓ Accept
- Scrollbar styling ✓ Accept
- Layout differences ✗ Fix
- Functional issues ✗ Fix

## Related Documentation

- Setup Guide: `.agent-os/playwright-mcp-setup.md`
- Visual Workflows: `.agent-os/playwright-visual-workflows.md`
- Testing Guide: `.agent-os/testing-guide.md`
- Spec: `.agent-os/specs/2025-10-01-playwright-framework-integration/`
