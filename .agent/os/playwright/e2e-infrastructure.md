# Playwright E2E Infrastructure Guide

> Framework-level infrastructure for future end-to-end testing
> Created: 2025-10-01
> Status: Placeholder Infrastructure

## Overview

This document describes the minimal infrastructure for traditional Playwright E2E testing in Moqui Framework projects. This is **placeholder documentation** for future implementation and does not include component-specific tests.

## Purpose

Provide a framework-level foundation for Moqui components to implement their own E2E tests using Playwright. This infrastructure establishes:
- Standard directory structure
- Configuration patterns
- Best practices for Moqui-specific E2E testing
- Integration with CI/CD pipelines

## Recommended Directory Structure

```
moqui-project/
├── runtime/component/{component}/
│   └── tests/                        # Component E2E tests
│       ├── playwright.config.js      # Component-specific config
│       ├── package.json              # Component test dependencies
│       ├── functional/               # User workflow tests
│       │   ├── login.spec.js
│       │   └── dashboard.spec.js
│       ├── api/                      # API endpoint tests
│       │   └── rest-endpoints.spec.js
│       ├── fixtures/                 # Test data and helpers
│       │   ├── moqui-auth.js         # Authentication helper
│       │   └── test-data.js          # Test data factory
│       └── utils/                    # Test utilities
│           ├── moqui-helpers.js      # Moqui-specific helpers
│           └── selectors.js          # Element selectors
```

## Configuration Template

### Component playwright.config.js

```javascript
// runtime/component/{component}/tests/playwright.config.js
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',

  use: {
    // Moqui default development URL
    baseURL: process.env.BASE_URL || 'https://localhost:8443',

    // Accept self-signed certificates in development
    ignoreHTTPSErrors: true,

    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
  },

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
    // Add more browsers as needed
  ],

  // Development server configuration (optional)
  webServer: process.env.CI ? undefined : {
    command: 'cd ../../../.. && ./gradlew run',
    url: 'https://localhost:8443',
    ignoreHTTPSErrors: true,
    timeout: 120 * 1000,
    reuseExistingServer: !process.env.CI,
  },
});
```

### Component package.json

```json
{
  "name": "@moqui/{component}-tests",
  "version": "1.0.0",
  "private": true,
  "scripts": {
    "test": "playwright test",
    "test:headed": "playwright test --headed",
    "test:debug": "playwright test --debug",
    "test:ui": "playwright test --ui",
    "test:report": "playwright show-report"
  },
  "devDependencies": {
    "@playwright/test": "^1.40.0"
  }
}
```

## Moqui-Specific Patterns

### Authentication Helper

```javascript
// runtime/component/{component}/tests/fixtures/moqui-auth.js
import { test as base } from '@playwright/test';

export const test = base.extend({
  authenticatedPage: async ({ page }, use) => {
    // Navigate to login page
    await page.goto('/Login');

    // Login with test credentials
    await page.fill('[name="username"]', process.env.TEST_USERNAME || 'john.doe');
    await page.fill('[name="password"]', process.env.TEST_PASSWORD || 'moqui');
    await page.click('[type="submit"]');

    // Wait for navigation to complete
    await page.waitForURL('/apps/**');

    await use(page);
  },
});
```

### Moqui Screen Navigation

```javascript
// runtime/component/{component}/tests/utils/moqui-helpers.js

export class MoquiPage {
  constructor(page) {
    this.page = page;
  }

  /**
   * Navigate to Moqui screen by path
   */
  async goto(screenPath) {
    await this.page.goto(`/apps/${screenPath}`);
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * Submit Moqui form
   */
  async submitForm(formName) {
    await this.page.click(`form[name="${formName}"] button[type="submit"]`);
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * Wait for Moqui notification
   */
  async waitForNotification(type = 'success') {
    await this.page.waitForSelector(`.alert-${type}`, { state: 'visible' });
  }

  /**
   * Check for Moqui error messages
   */
  async hasErrorMessage() {
    return await this.page.locator('.alert-danger').isVisible();
  }
}
```

### Test Data Factory

```javascript
// runtime/component/{component}/tests/fixtures/test-data.js

export class MoquiTestDataFactory {
  constructor(request) {
    this.request = request;
    this.baseURL = process.env.BASE_URL || 'https://localhost:8443';
  }

  /**
   * Create test data via Moqui REST API
   */
  async createEntity(entityName, data) {
    const response = await this.request.post(
      `${this.baseURL}/rest/s1/moqui/${entityName}`,
      {
        data,
        headers: {
          'Authorization': `Bearer ${process.env.TEST_API_TOKEN}`,
        },
      }
    );

    return response.json();
  }

  /**
   * Clean up test data
   */
  async deleteEntity(entityName, entityId) {
    await this.request.delete(
      `${this.baseURL}/rest/s1/moqui/${entityName}/${entityId}`,
      {
        headers: {
          'Authorization': `Bearer ${process.env.TEST_API_TOKEN}`,
        },
      }
    );
  }
}
```

## Example Test Structure

### Functional Test Example

```javascript
// runtime/component/{component}/tests/functional/dashboard.spec.js
import { test, expect } from '../fixtures/moqui-auth';
import { MoquiPage } from '../utils/moqui-helpers';

test.describe('Dashboard', () => {
  test('should display user dashboard after login', async ({ authenticatedPage }) => {
    const moqui = new MoquiPage(authenticatedPage);

    // Navigate to dashboard
    await moqui.goto('myapp/Dashboard');

    // Verify dashboard elements
    await expect(authenticatedPage.locator('h1')).toContainText('Dashboard');
    await expect(authenticatedPage.locator('[data-test="user-info"]')).toBeVisible();
  });
});
```

### API Test Example

```javascript
// runtime/component/{component}/tests/api/rest-endpoints.spec.js
import { test, expect } from '@playwright/test';

test.describe('REST API Endpoints', () => {
  const baseURL = process.env.BASE_URL || 'https://localhost:8443';
  const apiToken = process.env.TEST_API_TOKEN;

  test('should return DTE list', async ({ request }) => {
    const response = await request.get(`${baseURL}/rest/{api-path}/v2/{resource}`, {
      headers: {
        'Authorization': `Bearer ${apiToken}`,
      },
    });

    expect(response.ok()).toBeTruthy();

    const data = await response.json();
    expect(data).toHaveProperty('dtes');
    expect(data).toHaveProperty('totalCount');
  });
});
```

## CI/CD Integration

### GitHub Actions Example

```yaml
# .github/workflows/e2e-tests.yml
name: E2E Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_PASSWORD: moqui
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'

      - name: Install Playwright
        working-directory: runtime/component/{component}/tests
        run: |
          npm ci
          npx playwright install --with-deps

      - name: Build and Start Moqui
        run: |
          ./gradlew cleanDb load
          ./gradlew run &
          sleep 60  # Wait for Moqui to start

      - name: Run E2E Tests
        working-directory: runtime/component/{component}/tests
        run: npm test
        env:
          BASE_URL: https://localhost:8443
          TEST_USERNAME: john.doe
          TEST_PASSWORD: moqui

      - name: Upload Test Report
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: runtime/component/{component}/tests/playwright-report
```

## Best Practices

### 1. Test Independence

Each test should be independent and not rely on other tests:
- Create test data in `test.beforeEach()`
- Clean up test data in `test.afterEach()`
- Use unique identifiers for test data

### 2. Reliable Selectors

Use stable selectors for elements:
- Prefer `data-test` attributes
- Use ARIA labels and roles
- Avoid CSS selectors that depend on styling

### 3. Wait Strategies

Use appropriate wait strategies:
- `waitForLoadState('networkidle')` for Moqui screen transitions
- `waitForSelector()` for dynamic elements
- `waitForResponse()` for API calls

### 4. Authentication Management

- Use fixtures for authenticated sessions
- Store credentials in environment variables
- Reuse sessions across tests in same worker

### 5. Test Data Management

- Create test data via API when possible
- Use unique identifiers (UUIDs, timestamps)
- Always clean up test data
- Consider test data isolation strategies

## Moqui-Specific Considerations

### Self-Signed Certificates

Development environments use self-signed certificates:
```javascript
use: {
  ignoreHTTPSErrors: true,
}
```

### Default Ports

- HTTPS: 8443 (default development)
- HTTP: 8080 (alternative)

### Screen Paths

Moqui screens use path-based routing:
```
/apps/{component}/{ScreenName}
```

### Form Submissions

Moqui forms use server-side rendering:
- Wait for full page load after submit
- Use `waitForLoadState('networkidle')`
- Check for notification messages

### REST API Testing

Moqui REST APIs follow patterns:
```
/rest/{app-name}/{version}/{resource}
```

Authentication via Bearer tokens or session cookies.

## Initialization Scripts

### Initial Setup Script

```bash
#!/bin/bash
# runtime/component/{component}/tests/scripts/setup.sh

echo "Setting up Playwright E2E tests..."

# Install dependencies
npm install

# Install browsers
npx playwright install --with-deps chromium firefox

# Create environment file
cat > .env <<EOF
BASE_URL=https://localhost:8443
TEST_USERNAME=john.doe
TEST_PASSWORD=moqui
TEST_API_TOKEN=
EOF

echo "Setup complete!"
echo "Edit .env file with your test credentials"
```

## Troubleshooting

### Common Issues

**Certificate Errors:**
- Solution: Set `ignoreHTTPSErrors: true` in config

**Timeout on Startup:**
- Solution: Increase `webServer.timeout` in config
- Verify Moqui is running: `curl -k https://localhost:8443`

**Authentication Failures:**
- Solution: Verify test credentials exist in database
- Check that authorization is not blocking test user

**Test Data Conflicts:**
- Solution: Use unique identifiers for test data
- Implement proper cleanup in `afterEach()`

## Future Implementation

This infrastructure is a **placeholder** for component teams to implement their own E2E tests. Components should:

1. Create `runtime/component/{component}/tests/` directory
2. Copy configuration templates
3. Implement component-specific tests
4. Integrate with CI/CD pipeline
5. Document component-specific patterns

## Related Documentation

- Playwright MCP Setup: `.agent-os/playwright-mcp-setup.md`
- Visual Workflows: `.agent-os/playwright-visual-workflows.md`
- Cross-Browser Testing: `.agent-os/playwright-cross-browser.md`
- Testing Guide: `.agent-os/testing-guide.md`
- Official Playwright Docs: https://playwright.dev

---

**Note**: This is framework-level infrastructure documentation. Component-specific E2E tests should be implemented by component teams based on their specific requirements.
