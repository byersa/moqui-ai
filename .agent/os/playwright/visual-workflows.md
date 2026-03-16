# Playwright MCP Visual Development Workflows

> Framework-level guide for visual iteration and CSS debugging
> Created: 2025-10-01
> Status: Active

## Overview

Playwright MCP enables Claude Code to perform visual development tasks by directly interacting with browsers. This guide documents workflows for CSS iteration, reference image comparison, and element inspection.

## Core Capabilities

### 1. CSS Injection and Iteration

Modify styles in real-time and verify changes visually.

#### Workflow: Adjust Spacing

**Goal:** Find the correct margin/padding values to match a design

**Steps:**
1. Navigate to the page in browser via Claude Code
2. Inject CSS with candidate values
3. Take screenshot
4. Iterate on values based on visual feedback
5. Document final values

**Example Prompt:**
```
Open localhost:8080/myapp/Dashboard in Firefox.
The header spacing looks too tight. Try increasing the top padding
from 10px to 15px, 20px, and 25px. Show me screenshots of each
so I can pick the best one.
```

**Implementation Pattern:**
```javascript
// Claude Code executes via Playwright MCP
await page.evaluate(() => {
  const style = document.createElement('style');
  style.textContent = '.header { padding-top: 20px !important; }';
  document.head.appendChild(style);
});
await page.screenshot({ path: 'header-20px.png' });
```

#### Workflow: Color Adjustments

**Goal:** Fine-tune colors to match brand guidelines

**Steps:**
1. Show reference color palette or image
2. Inject CSS with color candidates
3. Compare screenshots against reference
4. Iterate until match is achieved

**Example Prompt:**
```
Here's our brand color palette [attach image].
Open the login page and adjust the button background from #2563eb
to match our brand blue. Try #1e40af, #1d4ed8, and #2563eb variants.
```

### 2. Reference Image Comparison

Compare implementation against design mockups.

#### Workflow: Design Implementation Verification

**Goal:** Ensure implementation matches design specifications

**Steps:**
1. Provide reference image (design mockup, screenshot, etc.)
2. Navigate to the implemented page
3. Take screenshot of current state
4. Identify visual discrepancies
5. Iterate on CSS until match is achieved

**Example Prompt:**
```
Here's the approved design for our product card [attach mockup].
Open localhost:8080/products and compare the current implementation.
Identify spacing, sizing, and alignment differences, then help me
adjust the CSS to match the design.
```

#### Workflow: Cross-Viewport Consistency

**Goal:** Verify responsive design across different screen sizes

**Steps:**
1. Set viewport size (desktop, tablet, mobile)
2. Take screenshots at each breakpoint
3. Compare layouts
4. Adjust responsive CSS as needed

**Example Prompt:**
```
Test the dashboard layout at 1920x1080, 768x1024, and 375x667.
Show me screenshots and identify any layout issues at each size.
```

**Implementation Pattern:**
```javascript
// Claude Code executes via Playwright MCP
const viewports = [
  { width: 1920, height: 1080, name: 'desktop' },
  { width: 768, height: 1024, name: 'tablet' },
  { width: 375, height: 667, name: 'mobile' }
];

for (const viewport of viewports) {
  await page.setViewportSize(viewport);
  await page.screenshot({ path: `layout-${viewport.name}.png` });
}
```

### 3. Element Inspection

Identify and analyze specific page elements.

#### Workflow: Debug Layout Issues

**Goal:** Understand why an element is positioned incorrectly

**Steps:**
1. Navigate to the page
2. Inspect element properties (computed styles, box model)
3. Identify conflicting CSS rules
4. Propose fixes

**Example Prompt:**
```
The submit button on the form at localhost:8080/signup is
overlapping the text input. Inspect the button and tell me
what CSS is causing this issue.
```

**Implementation Pattern:**
```javascript
// Claude Code executes via Playwright MCP
const element = await page.locator('button[type="submit"]');
const box = await element.boundingBox();
const styles = await page.evaluate((selector) => {
  const el = document.querySelector(selector);
  return window.getComputedStyle(el);
}, 'button[type="submit"]');
```

#### Workflow: Accessibility Analysis

**Goal:** Verify accessibility properties of elements

**Steps:**
1. Navigate to the page
2. Inspect accessibility tree
3. Verify ARIA attributes, roles, labels
4. Identify accessibility issues

**Example Prompt:**
```
Check the accessibility of the navigation menu at localhost:8080.
Verify that all interactive elements have proper ARIA labels and
keyboard navigation works correctly.
```

### 4. State-Based Testing

Test visual appearance across different application states.

#### Workflow: Form Validation States

**Goal:** Verify error, success, and loading states

**Steps:**
1. Navigate to form
2. Trigger each state (error, success, loading)
3. Take screenshots of each state
4. Verify visual feedback is clear

**Example Prompt:**
```
Open the login form and show me:
1. Initial empty state
2. Validation error state (try submitting empty)
3. Loading state (mock network delay)
4. Success state (after successful login)
```

#### Workflow: Interactive Component States

**Goal:** Test hover, focus, active, and disabled states

**Steps:**
1. Navigate to component
2. Trigger each interaction state
3. Capture screenshots
4. Verify visual consistency

**Example Prompt:**
```
Test all button states on the dashboard:
- Default
- Hover
- Focus (keyboard navigation)
- Active (being clicked)
- Disabled

Show screenshots and verify the visual hierarchy is clear.
```

## Best Practices

### 1. Iterative Refinement

Start with broad adjustments, then refine:
- Begin with large value changes (e.g., 10px → 20px → 30px)
- Narrow down to optimal range
- Fine-tune with small increments (e.g., 20px → 22px → 21px)

### 2. Screenshot Organization

Maintain clear naming conventions:
- Include component/page name: `dashboard-header-spacing.png`
- Include variant/iteration: `button-hover-v2.png`
- Include viewport for responsive: `layout-mobile-375w.png`

### 3. CSS Specificity

Use `!important` sparingly during iteration:
- Good for quick testing
- Remove before committing code
- Identify and fix specificity conflicts properly

### 4. Browser Consistency

Test critical changes in both Firefox and Chrome:
- Firefox for development iteration
- Chrome for production verification
- Note browser-specific rendering differences

### 5. Performance Considerations

Be mindful of iteration cost:
- Screenshots are fast but accumulate storage
- Multiple viewport tests multiply execution time
- Use selective testing for frequently changing designs

## Common Use Cases

### Use Case 1: Match Design System

**Scenario:** Implementing components from a design system

**Workflow:**
1. Reference design system documentation/images
2. Implement base HTML/CSS
3. Use Playwright MCP to iteratively adjust:
   - Spacing (margins, padding, gaps)
   - Typography (sizes, weights, line-height)
   - Colors (exact hex values)
   - Borders (radius, width, style)
4. Screenshot final implementation for approval

### Use Case 2: Fix Production Issues

**Scenario:** CSS bug reported in production

**Workflow:**
1. Navigate to production URL (or staging)
2. Reproduce the issue visually
3. Inject CSS fixes to test solutions
4. Screenshot before/after
5. Document the fix for implementation

### Use Case 3: Responsive Design Testing

**Scenario:** Verify layout works across devices

**Workflow:**
1. Define target viewports (mobile, tablet, desktop)
2. Test each breakpoint
3. Identify layout breaks
4. Adjust responsive CSS
5. Re-test all viewports

### Use Case 4: UI Polish

**Scenario:** Fine-tuning spacing and alignment before release

**Workflow:**
1. Walk through entire user flow
2. Screenshot each screen
3. Identify micro-adjustments needed
4. Iterate on spacing, alignment, sizing
5. Document all CSS changes

## Integration with Moqui

### Testing Moqui Screens

Navigate to Moqui applications running locally:

```
Open Firefox and go to https://localhost:8443/apps/myapp/Dashboard
[Authenticate if needed]
Test the form layout and spacing
```

### Moqui-Specific Considerations

- **Port:** Default Moqui port is 8443 (HTTPS) or 8080 (HTTP)
- **Authentication:** May need to handle login screens
- **Screen Rendering:** Moqui uses server-side rendering with HTML/CSS
- **Responsive:** Verify Bootstrap grid or custom responsive CSS

## Limitations

### What Playwright MCP Can Do
- Navigate to any accessible URL
- Inject CSS for testing
- Take screenshots
- Inspect elements and styles
- Interact with page elements
- Test across viewports

### What Playwright MCP Cannot Do
- Directly edit source files (use Claude Code Edit tool)
- Persist changes automatically (CSS must be manually applied)
- Access browser extensions (e.g., Firefox Containers)
- Modify browser profiles persistently

## Example Workflows

### Example 1: Increase Button Size

**Prompt:**
```
Open localhost:8080/myapp and find the "Submit" button.
It looks too small. Try increasing the padding from 8px 16px
to 12px 24px and 16px 32px. Show me both options.
```

**Expected Outcome:**
- Two screenshots showing different button sizes
- Recommendation on which size has better visual hierarchy
- Specific CSS values to implement

### Example 2: Align Form Fields

**Prompt:**
```
The form fields on /signup are misaligned. Inspect the layout
and show me what's causing the issue. Then adjust the CSS to
make all fields align properly.
```

**Expected Outcome:**
- Identification of problematic CSS (e.g., inconsistent widths)
- Screenshot showing misalignment
- Adjusted CSS injection
- Screenshot showing corrected alignment
- Specific CSS changes to implement

### Example 3: Color Consistency

**Prompt:**
```
Our brand blue is #1e40af. Check all buttons on the dashboard
and verify they use this exact color. If not, show me which
ones are different and what they're currently using.
```

**Expected Outcome:**
- List of buttons with current colors
- Identification of inconsistencies
- Screenshot highlighting differences
- CSS changes needed for consistency

## Quick Reference

### Common Playwright MCP Commands

```javascript
// Navigate
await page.goto('http://localhost:8080');

// Inject CSS
await page.evaluate(() => {
  const style = document.createElement('style');
  style.textContent = '.my-class { color: red; }';
  document.head.appendChild(style);
});

// Screenshot
await page.screenshot({ path: 'screenshot.png' });

// Set viewport
await page.setViewportSize({ width: 1280, height: 720 });

// Inspect element
const element = await page.locator('.my-selector');
const styles = await element.evaluate((el) =>
  window.getComputedStyle(el)
);

// Get bounding box
const box = await element.boundingBox();
```

### Standard Viewports

```javascript
const viewports = {
  mobile: { width: 375, height: 667 },    // iPhone SE
  tablet: { width: 768, height: 1024 },   // iPad
  desktop: { width: 1920, height: 1080 }, // Full HD
  laptop: { width: 1440, height: 900 }    // MacBook Pro
};
```

## Related Documentation

- Setup Guide: `.agent-os/playwright-mcp-setup.md`
- Cross-Browser Guide: `.agent-os/playwright-cross-browser.md` (Task 3)
- Testing Guide: `.agent-os/testing-guide.md`
- Spec: `.agent-os/specs/2025-10-01-playwright-framework-integration/`
