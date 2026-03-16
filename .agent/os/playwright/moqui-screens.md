# Playwright MCP for Moqui Screen Development

> Workflows for visual screen development, debugging, and validation
> Created: 2025-12-21
> Status: Active

## Overview

This guide provides specific workflows for using Playwright MCP when developing, debugging, and validating Moqui screens. It complements the moqui-screen-specialist agent by providing visual verification capabilities.

## Prerequisites

- Playwright MCP configured (see `playwright-mcp-claude-code.md`)
- Moqui server running (typically started by user)
- Valid test credentials for authentication

## Server Status Handling

**Important:** Moqui server is assumed to be already running by the user.

### If Connection Fails

When Playwright MCP reports "connection refused" or timeout:

1. **Inform the user**: "Moqui appears to be down at localhost:8443"
2. **Ask for confirmation**: "Should I start it with `./gradlew run`?"
3. **Never auto-start** without explicit user approval

### Verifying Server Status

```bash
# Quick check if Moqui is running
curl -k -s -o /dev/null -w "%{http_code}" https://localhost:8443/status
# Returns 200 if running
```

## Workflow A: Screen Debugging

Use this workflow when a screen isn't working as expected.

### Step 1: Navigate to the Screen

```
Open Firefox and navigate to https://localhost:8443/apps/{app-name}/{ScreenName}
```

### Step 2: Authenticate (if needed)

If redirected to login:
```
Enter username: {test-user}
Enter password: {test-password}
Click the login button
```

**Note:** For automated testing, the MCP session maintains cookies, so login persists for the session.

### Step 3: Identify Visual Issues

```
Take a screenshot and describe what you see
Identify any layout issues, missing data, or unexpected behavior
```

### Step 4: Inspect Specific Elements

For layout issues:
```
Inspect the element with class ".form-group" and show computed styles
What CSS is causing the submit button to overlap the input?
```

For missing data:
```
Check if the data table is rendering
Show me the contents of the #entityList element
```

### Step 5: Document Findings

After debugging, document:
- What the issue was
- What CSS/element inspection revealed
- Suggested fix approach

## Workflow B: Screen Validation

Use this workflow to verify a screen matches requirements.

### Step 1: Establish Reference

Either:
- Provide a design mockup image
- Describe the expected behavior
- Reference a similar working screen

### Step 2: Navigate and Capture

```
Open https://localhost:8443/apps/{app-name}/EditProduct
Take a screenshot of the full page
```

### Step 3: Compare Against Requirements

```
Compare this implementation against the design mockup I provided
List any differences in:
1. Layout and spacing
2. Typography and colors
3. Form field arrangement
4. Button placement
```

### Step 4: Verify Responsive Behavior

```
Test this screen at:
1. Desktop (1920x1080)
2. Tablet (768x1024)
3. Mobile (375x667)

Show me screenshots at each size and identify any layout breaks
```

### Step 5: Cross-Browser Check (Optional)

```
Open this same URL in Chrome and compare with the Firefox version
Show me any rendering differences
```

## Workflow C: CSS Iteration

Use this workflow when fine-tuning screen styling.

### Step 1: Identify Target Element

```
Navigate to https://localhost:8443/apps/{app-name}/Dashboard
Show me the header section
```

### Step 2: Test CSS Variations

```
Try these padding values on the .card-header:
1. padding: 10px
2. padding: 15px
3. padding: 20px

Show me a screenshot of each option
```

### Step 3: Compare and Decide

```
Show me all three variations side by side
Which one has the best visual balance?
```

### Step 4: Apply to Source

Once satisfied, apply the chosen CSS to the source files:

```xml
<!-- In the Moqui screen XML or associated CSS file -->
<style>
.card-header { padding: 15px; }
</style>
```

### Step 5: Verify Applied Changes

```
Refresh the page and verify the CSS change was applied correctly
Take a final screenshot for reference
```

## Workflow D: EntityFilter Debugging

When data appears/disappears unexpectedly due to EntityFilter issues.

### Step 1: Establish Baseline

```
Login as admin user (no filters applied)
Navigate to https://localhost:8443/apps/{app-name}/ProductList
Count how many products are visible
```

### Step 2: Test Filtered View

```
Login as restricted user
Navigate to the same ProductList screen
Count how many products are visible now
```

### Step 3: Identify Filter Effects

```
Compare the two screenshots
Show me which products are missing in the filtered view
```

### Step 4: Correlate with EntityFilter

Cross-reference with EntityFilter configuration:
- Which filter is applied for this user/role?
- Does the filter criteria match what's being hidden?
- Are there unexpected records being filtered?

## Workflow E: Form Functionality

Testing form behavior and validation.

### Step 1: Test Empty Submission

```
Navigate to the create form
Click submit without filling any fields
Show me the validation errors that appear
```

### Step 2: Test Invalid Input

```
Enter "invalid-email" in the email field
Tab to next field
Show me any inline validation
```

### Step 3: Test Successful Submission

```
Fill the form with valid test data:
- Name: Test Product
- Code: TEST001
- Price: 100.00

Submit and show me the result
```

### Step 4: Verify Data Persistence

```
Navigate to the list view
Search for the record we just created
Confirm it appears with correct data
```

## Common Moqui Screen Patterns

### Bootstrap-based Forms

Moqui typically uses Bootstrap styling. Common selectors:

```css
/* Form controls */
.form-control           /* Input fields */
.form-group            /* Field containers */
.btn .btn-primary      /* Primary buttons */
.btn .btn-secondary    /* Secondary buttons */

/* Layout */
.container-fluid       /* Full-width container */
.row                   /* Grid row */
.col-*                 /* Grid columns */

/* Tables */
.table                 /* Data tables */
.table-striped         /* Striped tables */
.table-hover           /* Hover effect */
```

### Moqui-Specific Elements

```css
/* Screen sections */
.screen-section        /* Section containers */
.section-header        /* Section headers */

/* Form widgets */
.widget-box            /* Widget containers */
.widget-header         /* Widget headers */
.widget-body           /* Widget body */

/* Notifications */
.alert                 /* Alert messages */
.alert-success         /* Success messages */
.alert-danger          /* Error messages */
```

## Integration with Screen Specialist

### When to Use Playwright MCP

1. **After screen changes** - Visually verify modifications work
2. **For CSS debugging** - Test values before committing
3. **For layout issues** - Inspect element relationships
4. **For data issues** - Visually confirm data display

### When to Use moqui-screen-specialist

1. **For XML structure changes** - Screen definitions, forms, transitions
2. **For service integration** - Connecting screens to services
3. **For navigation setup** - Menu items, transitions, actions
4. **For form configuration** - Field definitions, validation rules

### Typical Workflow

1. **moqui-screen-specialist** creates/modifies screen XML
2. **Playwright MCP** verifies the screen renders correctly
3. **moqui-screen-specialist** fixes any structural issues
4. **Playwright MCP** confirms fixes visually
5. Iterate until screen is complete

## Example Debugging Session

### Problem: Table not showing data

**Step 1: Visual Check**
```
Navigate to https://localhost:8443/apps/{app-name}/ProductList
Take a screenshot
Is the table visible? Does it show "No records found"?
```

**Step 2: Inspect Element**
```
Inspect the #productTable element
Show me its contents and any error messages in the console
```

**Step 3: Check Data Source**
```
The table shows "No records found"
Let me check if the service is returning data...
[Run curl to test the REST endpoint]
```

**Step 4: Identify Root Cause**
- If REST returns data but table is empty → Screen binding issue
- If REST returns empty → Service or EntityFilter issue
- If error in console → JavaScript or rendering issue

**Step 5: Fix and Verify**
```
After fixing the issue, refresh and take a new screenshot
Confirm the table now shows the expected data
```

## Troubleshooting

### Screen Shows Login Instead of Content

1. Session may have expired
2. Re-authenticate via MCP
3. Check if the screen requires specific permissions

### CSS Changes Not Visible

1. Browser may have cached styles
2. Request: "Hard refresh the page (clear cache)"
3. Verify CSS file is being served

### JavaScript Errors

1. Request: "Open browser console and show any errors"
2. Identify the failing script
3. Check if required JS libraries are loaded

### Slow Screen Loading

1. Check if data query is slow
2. Request: "Time how long the page takes to fully load"
3. May need to optimize service or entity queries

## Related Documentation

- Claude Code CLI Setup: `playwright-mcp-claude-code.md`
- Visual Workflows: `playwright-visual-workflows.md`
- Screen Specialist: Use via Task tool with `moqui-screen-specialist`
- Testing Guide: `testing-guide.md`
