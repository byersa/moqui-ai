# Testing Guide - Moqui Framework

Comprehensive testing strategy for Moqui Framework projects covering unit, integration, and end-to-end testing approaches, including implementation patterns and best practices.

## CRITICAL: Gradle Must Run from Root Project Directory

**ALWAYS run gradle commands from the Moqui Framework root project directory (moqui-framework repo), NOT from component directories.**

```bash
# CORRECT - Run from root directory with ALL required data types
cd /path/to/moqui-framework
./gradlew cleanDb load runtime:component:{component-name}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test

# WRONG - Do NOT run from component directory
cd /path/to/moqui-framework/runtime/component/{component-name}
../../../gradlew test  # This will fail due to classpath and data loading issues

# WRONG - Do NOT omit -Ptypes or use incomplete types
./gradlew cleanDb load runtime:component:{component-name}:test  # Missing -Ptypes!
./gradlew cleanDb load runtime:component:{component-name}:test -Ptypes=seed,seed-initial  # Missing test data types!
```

**Why This Matters:**
- **Classpath Resolution**: Component-level gradle execution doesn't properly resolve all tool factory dependencies (moqui-fop, moqui-wikitext, etc.)
- **Seed Data Loading**: The `entity_empty_db_load` property only works correctly when the full Moqui environment is initialized from root
- **ServiceLoader Issues**: Many Moqui extensions use ServiceLoader which requires the complete runtime classpath
- **H2 Server**: The H2 database server needs proper initialization from the framework level

### CRITICAL: `entity_empty_db_load` in Component build.gradle Does NOT Work

**DO NOT configure `entity_empty_db_load` in component-level `build.gradle` files.**

```groovy
// WRONG - This setting is IGNORED when running component tests
test {
    systemProperty 'entity_empty_db_load', 'seed,seed-initial,{project}-test'  // ❌ IGNORED
}
```

**Why It Doesn't Work:**

Data loading is performed at the **framework level** during Moqui initialization, which happens **before** component-level test configuration is processed. The framework reads `entity_empty_db_load` from:

1. `MoquiDefaultConf.xml` (framework default: `seed,seed-initial,install`)
2. Environment-specific conf file (e.g., `MoquiDevConf.xml`)
3. System properties passed via root-level gradle command

By the time a component's `build.gradle` test block executes, the database has already been initialized with whatever data types were configured at the framework level.

**Correct Approach - Always specify data types on the command line:**

```bash
# Data types MUST be specified when running the load command from root
./gradlew cleanDb load runtime:component:{component-name}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test
```

**What to Put in Component build.gradle:**

Keep test configuration minimal - only settings that affect test execution, not data loading:

```groovy
test {
    useJUnitPlatform()
    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
    // Do NOT add entity_empty_db_load here - it will be ignored
}
```

## CRITICAL: Test Execution Requirements

**ALWAYS run tests with `cleanDb` and `load` commands to ensure database consistency.**

Running tests without cleaning and reloading the database can cause:
- Unpredictable test failures due to stale data
- False positives from leftover test data
- False negatives from missing seed data
- Intermittent failures that are hard to reproduce

### Standard Test Execution Pattern

```bash
# Standardized pattern for all project components
./gradlew cleanDb load runtime:component:{component-name}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test
```

**Pattern Explanation:**
- `{component-name}`: The name of the component being tested (e.g., `{component-name}`, `{localization-component}`)
- `{component-name}-test`: Component-specific test data type (always include, harmless if no files exist)

**Component-Specific Examples:**

```bash
# For {shared-component} component
./gradlew cleanDb load runtime:component:{shared-component}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{shared-component}-test

# For {localization-component} component
./gradlew cleanDb load runtime:component:{localization-component}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component}-test

# For {utils-component} component
./gradlew cleanDb load runtime:component:{utils-component}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{utils-component}-test

# For another-component component
./gradlew cleanDb load runtime:component:another-component:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,another-component-test
```

**Data Type Usage:**

Standard data types (required for all components):
- **`seed,seed-initial`**: Framework core seed data (required for all components)
- **`{component-name}-test`**: Component-specific test data (**ALWAYS included** - harmless if no files exist)

Project-specific data types (customize per project):
- **`{l10n}-install`**: Project-specific installation data (e.g., localized chart of accounts, replaces `install`)
- **`{project}-demo`**: Project-specific demo data (use instead of `demo` if project overrides it)
- **`{component}-test`**: Shared component test data (e.g., localization test data, **ALWAYS included** - standard dependency)
- **`{project}-test`**: Shared test infrastructure for all project components (users, organizations)

**IMPORTANT: Custom `install` vs Standard `install` Data Types**

When a project provides a custom install type (e.g., `{l10n}-install`), it and the standard `install` are **mutually exclusive** - do not use both together.

- **`install`**: Standard mantle-usl installation data with US-centric ledger accounts and configuration
- **`{l10n}-install`**: Project-specific replacement that:
  - Provides project-localized configuration (e.g., chart of accounts)
  - Selectively includes specific `install` data files by name (party types, enumerations like `PtidNationalTaxId`, UoM definitions)
  - **Excludes** standard configurations that would conflict with project-specific setup
  - Is **required** for all project components

**For projects with custom install, always use `{l10n}-install` instead of `install`:**
```bash
# CORRECT for project with custom install
-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test

# INCORRECT - do not mix custom install and standard install
-Ptypes=seed,seed-initial,install,{l10n}-install,{project}-demo,{project}-test
```

**CRITICAL: Never Use `demo` Type When Project Provides Custom Demo**

The `demo` data type from mantle-usl may be **incompatible** with custom install types due to conflicting configurations. Always use the project-specific demo type instead:

```bash
# CORRECT for projects with custom demo type
-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test

# INCORRECT - demo may conflict with custom install
-Ptypes=seed,seed-initial,{l10n}-install,demo,{project}-test
```

### Data Type Contents Reference

Understanding what each data type provides helps diagnose test failures from missing data.

| Data Type | Source Component | Key Contents |
|-----------|------------------|--------------|
| `seed` | moqui-framework | Core enumerations, status types, basic configuration |
| `seed-initial` | moqui-framework | Initial system setup, default locale settings |
| `install` | mantle-usl | US-centric ledger accounts, party types, enumerations (`PtidNationalTaxId`, etc.), UoM definitions. **Do not use with `{l10n}-install`** |
| `{l10n}-install` | {localization-component} | Project-specific installation data (e.g., localized chart of accounts, integration setup). **Includes necessary `install` files** (party types, enumerations) while excluding conflicting standard configurations |
| `demo` | mantle-usl | Demo organizations, sample data for testing |
| `{project}-test` | {utils-component} | **Test user accounts** (e.g., `john.doe`/`EX_JOHN_DOE`), test organizations, base test configuration |
| `{component}-test` | {localization-component} | Test data specific to localization component tests (e.g., tax document scenarios) |
| `{component-name}-test` | {component-name} | Test data specific to component tests (e.g., signature test data, CRC test scenarios) |

**Component-Specific Test Data Types:**

Each component defines its own `{component-name}-test` data type containing test fixtures required by the tests within that component. These are **not interchangeable** - you need the specific test data type for the component you're testing:

- `{component}-test` → Required for `runtime/component/{localization-component}/src/test/groovy/` tests
- `{component-name}-test` → Required for `runtime/component/{component-name}/src/test/groovy/` tests
- `{utils-component}-test` → Required for `runtime/component/{utils-component}/src/test/groovy/` tests

**CRITICAL: Common Test Failures from Missing Data Types**

| Error Message | Missing Data Type | Solution |
|---------------|-------------------|----------|
| `No account found for username john.doe` | `{project}-test` | Add `{project}-test` to `-Ptypes=` |
| `PtidNationalTaxId` FK violation | `{l10n}-install` (or `install` for standard) | Add `{l10n}-install` to `-Ptypes=` |
| Project-specific type not found | `{l10n}-install` | Add `{l10n}-install` to `-Ptypes=` |
| `_NA_` party not found | `seed` | Ensure `seed,seed-initial` are first in types |
| Status type not found | `seed` or component seed | Check component's seed data files |
| Test-specific entity not found | `{component}-test` | Add component's test data type |

**Quick Reference - Standard Data Types by Component:**

```bash
# Standard pattern for all project component tests
-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test

# Specific examples (replace placeholders with your project values):
# {shared-component}:          seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{shared-component}-test
# {localization-component}:    seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component}-test
# {utils-component}:           seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{utils-component}-test
# {component-name}:            seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test
```

**Why include `{component}-test` even if no files exist?**
- Harmless when no matching files exist
- Ready for future component-specific test data
- Consistent command pattern across all components

**Minimum for standard mantle-based tests (reference only):**
```bash
-Ptypes=seed,seed-initial,install,demo
```

**Troubleshooting: `loadSeed` vs Full Test Data**

| Command | What It Loads | Use Case |
|---------|---------------|----------|
| `./gradlew loadSeed` | Only `seed` type | Production setup, minimal data |
| `./gradlew loadSeedInitial` | `seed,seed-initial` | Production installation |
| `./gradlew load` | All data types from all components | Development, may include demo data |
| `./gradlew cleanDb load -Ptypes=...` | Specified types only | **Recommended for tests** |

### When You Can Skip cleanDb/load

- **Active development**: When running the same test repeatedly within minutes
- **Debugging**: When iterating on a specific test with known database state
- **Never in CI/CD**: Always use full cleanDb/load in automated pipelines

```bash
# WARNING: Only use during active debugging with known clean database state
./gradlew runtime:component:{component-name}:test
```

### Why This Matters

- **Database State**: Each test run needs a clean, known database state
- **Test Isolation**: Previous test runs may leave data that affects current tests
- **Seed Data**: Framework and component seed data must be loaded fresh
- **Reproducibility**: Ensures tests can be reproduced on any environment
- **Search Indices**: cleanDb also clears ElasticSearch/OpenSearch indices

## Testing Philosophy

Follow the **Testing Pyramid** approach:
- **70% Unit Tests** - Fast, isolated, comprehensive coverage
- **25% Integration Tests** - Service and data layer testing
- **5% End-to-End Tests** - Critical user workflows

## Testing Technologies

### Spock Framework (Unit & Integration Testing)
Primary testing framework for Moqui applications using Groovy.

**Use Spock for:**
- Service logic testing
- Entity operations
- Business rule validation
- Integration testing with database
- API endpoint testing

**Key Features:**
- Data-driven testing with `@Unroll`
- Powerful assertion syntax
- Mock and stub capabilities
- Integration with Moqui's test framework

### Playwright (End-to-End Testing)
Browser automation framework for critical user workflows.

**Use Playwright for:**
- Complete user journey testing
- Cross-browser compatibility
- UI interaction validation
- Complex workflow integration
- Visual regression testing (when needed)

**Key Features:**
- Multi-browser support (Chromium, Firefox, WebKit)
- Auto-wait capabilities
- Debugging tools and trace viewer
- Component-specific configurations

### Playwright MCP (Interactive Visual Testing)

Interactive browser testing via Claude Code for ad-hoc visual development and debugging.

**Use Playwright MCP for:**
- CSS iteration and fine-tuning
- Visual debugging during development
- Design comparison and validation
- Screen element inspection
- EntityFilter visual debugging

**Key Differences from E2E Tests:**

| Aspect | Playwright MCP | Playwright E2E Tests |
|--------|----------------|----------------------|
| Purpose | Interactive development | Automated regression |
| Execution | Manual via Claude Code | CI/CD automated |
| Persistence | Session-only | Repeatable test files |
| Best for | CSS iteration, debugging | User journey validation |

**Configuration:**
- Configured via `.mcp.json` in project root
- See `playwright-mcp-claude-code.md` for setup
- See `playwright-moqui-screens.md` for Moqui-specific workflows

**When to Use Each:**

1. **During Development** → Playwright MCP
   - Quick visual checks
   - CSS adjustments
   - Layout debugging

2. **Before Commit** → Both
   - MCP for final visual verification
   - E2E tests for regression validation

3. **In CI/CD** → Playwright E2E Tests only
   - Automated, repeatable
   - No interactive tools in CI

## Critical Implementation Patterns

### 1. User Authentication Strategy

**Problem**: Test user authentication is the #1 source of test failures and rework. Services require authenticated users but tests may fail due to authorization restrictions.

**Solution**: Follow the mantle-usl pattern: authenticate user + disable authorization for test service calls.

```groovy
// CORRECT: Framework pattern from mantle-usl tests
def setupSpec() {
    ec = Moqui.getExecutionContext()
    ec.user.loginUser("john.doe", "moqui")  // Authenticate with framework user
    // Or: ec.user.loginUser("joe@public.com", "moqui")
}

def setup() {
    ec.message.clearAll()                   // Clear ALL messages from previous tests
    ec.artifactExecution.disableAuthz()     // Disable authorization for test calls
    ec.transaction.begin(null)
}

def cleanup() {
    ec.artifactExecution.enableAuthz()      // Re-enable authorization after test
    ec.transaction.commit()
}
```

**Why**:
- **Authentication ≠ Authorization**: Services need authenticated users, but not authorization checks in tests
- **Framework users** (john.doe, joe@public.com) are pre-configured with proper authentication
- **disableAuthz()** allows test access to services without complex permission setup
- This matches the proven pattern used throughout mantle-usl tests
- **Avoid** creating custom authorization records - too complex for testing

### 2. Test Suite Organization

**Problem**: Running individual test files causes isolation issues and resource conflicts.

**Solution**: Use JUnit 5 Suite Pattern

```groovy
// MoquiComponentSuite.groovy
@Suite
@SelectClasses([
    CategoryA.TestClass1.class,
    CategoryB.TestClass2.class
])
class MoquiComponentSuite {
    @AfterAll
    static void destroyMoqui() {
        Moqui.destroyActiveExecutionContextFactory()
    }
}
```

**build.gradle Configuration**:
```gradle
test {
    useJUnitPlatform()
    maxParallelForks 1  // Critical: Single-threaded execution
    include '**/MoquiComponentSuite.class'  // Suite-only pattern

    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
}
```

### 3. Sequence ID Management

**Problem**: Test data conflicts with production sequences.

**Solution**: Use temporary sequence ranges

```groovy
def setupSpec() {
    // Reserve test-specific ID ranges
    ec.entity.tempSetSequencedIdPrimary("entity.name", 58000, 10)
}

def cleanupSpec() {
    // Reset sequences
    ec.entity.tempResetSequencedIdPrimary("entity.name")
}
```

### 4. Jakarta API Imports (Java 21+)

**Problem**: Java 21+ uses Jakarta EE instead of javax packages. Using `javax.transaction.Status` causes `No such property: javax` errors.

**Solution**: Import Jakarta packages for transaction status constants

```groovy
// CORRECT: Java 21+ (Jakarta EE)
import jakarta.transaction.Status

def cleanup() {
    if (ec.transaction.isTransactionInPlace()) {
        def txStatus = ec.transaction.getStatus()
        if (txStatus == Status.STATUS_MARKED_ROLLBACK) {
            ec.transaction.rollback("Transaction marked for rollback", null)
        } else if (txStatus == Status.STATUS_ACTIVE) {
            ec.transaction.commit()
        }
    }
}

// INCORRECT: Java 8-17 style (fails in Java 21+)
// import javax.transaction.Status  // Does not exist in Java 21+
```

**Why**: Moqui Framework 4.0+ requires Java 21+ which uses Jakarta EE 9+ namespace migration.

### 5. When NOT to Use Transaction Isolation

**Problem**: Tests that only query existing demo/seed data fail when wrapped in transactions because they cannot see data loaded before the transaction started.

**Solution**: Read-only tests should NOT use transaction management base classes

```groovy
// CORRECT: Read-only test extends Specification directly
class ListCustomerPlansTests extends Specification {
    @Shared ExecutionContext ec

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("{admin-user}", "{admin-password}")
    }

    def setup() {
        ec.artifactExecution.disableAuthz()
        // NO transaction.begin() - allows seeing pre-loaded demo data
    }

    def cleanup() {
        ec.artifactExecution.enableAuthz()
    }

    def "Query existing demo data"() {
        when:
        def result = ec.service.sync().name("get#ExistingData")
            .parameters([id: "DEMO_ID"]).call()

        then:
        result != null  // Can see demo data loaded before test
    }
}

// INCORRECT: Read-only test wrapped in transaction
class ListCustomerPlansTests extends TransactionTestBase {
    // setup() calls ec.transaction.begin(null)
    // Test CANNOT see demo data loaded before transaction started!
}
```

**When to use transaction isolation**:
- Tests that CREATE new entities
- Tests that MODIFY existing data
- Tests that need rollback on failure

**When NOT to use transaction isolation**:
- Tests that only QUERY existing demo/seed data
- Tests that verify data loaded by `-Ptypes=...`
- Tests that check configuration or setup data

## Unit Test Constraints

**CRITICAL: Unit tests in Moqui Framework cannot make external network connections.**

Unit tests (Spock tests in `src/test/groovy/`) run in an isolated environment that does not allow outbound HTTP/HTTPS connections. This includes:

- **HTTP REST API calls** to localhost or external servers
- **WebSocket connections** to any host
- **External service integrations** requiring network access
- **Email sending** via SMTP
- **Database connections** to external databases

**Why This Constraint Exists:**
- **Test isolation**: Unit tests must be fully self-contained
- **Performance**: No network I/O ensures fast test execution
- **Reliability**: No dependency on external services or network conditions
- **CI/CD compatibility**: Tests run in restricted environments

**What to Do Instead:**

| Test Scenario | Solution |
|--------------|----------|
| REST API authentication | Mock the authentication service or move to E2E tests |
| External API integration | Mock external responses or use integration tests with stubs |
| Email delivery testing | Test email generation logic, move delivery to E2E tests |
| HTTP endpoint testing | Test service logic directly, move HTTP layer to E2E tests |

**Example - INCORRECT (causes test failure):**
```groovy
def "Test REST API with HTTP call"() {
    when: "Make HTTP REST call"
    def client = new org.moqui.util.RestClient()
        .uri("http://localhost:8080/rest/api/endpoint")
        .method(RestClient.POST)
        .call()  // FAILS - No external connections allowed

    then:
    client.statusCode == 200
}
```

**Example - CORRECT (test service logic directly):**
```groovy
def "Test API service logic without HTTP"() {
    when: "Call service directly"
    def result = ec.service.sync()
        .name("your.api.Service")
        .parameters([param: "value"])
        .call()  // WORKS - Direct service call

    then:
    result.success
    result.data != null
}
```

## Testing Strategy by Layer

### 1. Unit Testing with Spock

**Location**: `src/test/groovy/`

**Patterns**:
```groovy
class ServiceTests extends Specification {
    @Shared ExecutionContext ec

    def setupSpec() {
        ec = Moqui.getExecutionContext()
    }

    @Unroll
    def "Calculate tax for amount #amount with rate #rate"() {
        when:
        def result = ec.service.sync().name("calculate#Tax")
                .parameters([amount: amount, rate: rate])
                .call()

        then:
        result.taxAmount == expectedTax

        where:
        amount | rate | expectedTax
        1000   | 0.19 | 190
        5000   | 0.19 | 950
    }
}
```

**Test Categories**:
- Entity CRUD operations
- Service parameter validation
- Business logic calculations
- Data transformation
- Security and permissions

### 2. Integration Testing with Spock

**Focus**: Cross-service integration, database operations, external API integration.

**Example**:
```groovy
def "Integration: Create customer and invoice workflow"() {
    when:
    def customerResult = ec.service.sync().name("create#Customer")
            .parameters([customerName: "Test Customer", rut: "13477571-8"])
            .call()

    def invoiceResult = ec.service.sync().name("create#Invoice")
            .parameters([customerId: customerResult.customerId, amount: 1000])
            .call()

    then:
    customerResult.customerId
    invoiceResult.invoiceId

    cleanup:
    // Clean up test data
}
```

### 3. End-to-End Testing with Playwright

**Location**: `runtime/component/{component}/tests/`

**Focus**: Complete user workflows, critical business processes.

**Example**:
```javascript
test('Complete invoice creation workflow', async ({ page }) => {
    // Navigate to application
    await page.goto('/YourApp');

    // Create customer
    await page.click('[data-test="create-customer"]');
    await page.fill('[data-test="customer-name"]', 'Test Customer');
    await page.fill('[data-test="customer-rut"]', '13477571-8');
    await page.click('[data-test="save-customer"]');

    // Create invoice
    await page.click('[data-test="create-invoice"]');
    await page.selectOption('[data-test="customer"]', 'Test Customer');
    await page.fill('[data-test="amount"]', '1000');
    await page.click('[data-test="save-invoice"]');

    // Verify success
    await expect(page.locator('.success-message')).toBeVisible();
});
```

### Special Component Testing

#### Testing Rich Text Editors (CKEditor)

When testing screens with `editor-type="html"` fields (CKEditor), standard Playwright methods require special handling.

##### Problem: fill() Bypasses CKEditor Events

```typescript
// WRONG: fill() bypasses CKEditor's iframe event system
await page.fill('[name="agendaContent"]', 'Test content');
// V-model won't update - content appears but form data is empty!
```

CKEditor renders content in an iframe. Playwright's `fill()` directly sets the value but doesn't trigger CKEditor's `change` events, so Vue's v-model binding never updates.

##### Solution: Use pressSequentially for Event Triggering

```typescript
// CORRECT: pressSequentially triggers proper events
// First, find the CKEditor editable area
const editor = page.locator('.cke_editable');
await editor.click();
await editor.pressSequentially('Test content for CKEditor', { delay: 50 });
```

##### Alternative: Execute CKEditor API Directly

```typescript
// CORRECT: Use CKEditor's API via evaluate
await page.evaluate(() => {
  const editorInstance = CKEDITOR.instances['agendaContent'];
  if (editorInstance) {
    editorInstance.setData('<p>Test content</p>');
    editorInstance.fire('change');  // Explicitly trigger change event
  }
});
```

##### V-Model Binding Path

In Moqui's Quasar template, CKEditor content binds via:
- `formProps.fields.{fieldName}` in the mForm Vue component
- The `m-ck-editor` component emits `input` events to update this binding

When debugging form data issues, check:
```javascript
// In browser console
console.log(formProps.fields.agendaContent);  // Should contain HTML content
```

##### Key Points

1. **Never use `fill()`** for CKEditor fields - it won't trigger change events
2. **Use `pressSequentially()`** with a delay for reliable event triggering
3. **Alternative**: Use CKEditor API via `page.evaluate()` with explicit `fire('change')`
4. **Debug binding**: Check `formProps.fields.{fieldName}` in browser console

## Test Organization

### Directory Structure
```
project-root/
├── src/test/groovy/           # Spock unit/integration tests
│   ├── entity/                # Entity tests
│   ├── service/               # Service tests
│   └── integration/           # Integration tests
└── runtime/component/{name}/tests/  # Playwright E2E tests
    ├── functional/            # User workflow tests
    ├── api/                   # API endpoint tests
    └── performance/           # Performance tests
```

### Test Naming Conventions

**Spock Tests**:
- `{Entity}Tests.groovy` - Entity-focused tests
- `{Service}Tests.groovy` - Service-focused tests
- `{Feature}IntegrationTests.groovy` - Integration tests

**Playwright Tests**:
- `{feature}-{action}.spec.js` - Feature-specific tests
- `{workflow}-integration.spec.js` - Workflow tests
- `{api}-endpoints.spec.js` - API tests

## Decision Matrix: When to Use Which Test Type

| Scenario | Spock Unit | Spock Integration | Playwright E2E |
|----------|------------|-------------------|----------------|
| Service logic validation | Primary | | |
| Database operations | Basic | Primary | |
| Multi-service workflows | | Primary | Critical paths |
| UI interactions | | | Primary |
| Cross-browser compatibility | | | Primary |
| Performance validation | Unit level | Service level | Full stack |
| Regression testing | Core logic | Integration | Key workflows |

## Test Data Management

### Cross-Test-Class Data Collisions

When multiple test classes in a suite use the same identifiers (RUTs, entity IDs, party IDs), they collide because `setupSpec()` data persists across test classes. Even with `cleanupSpec()`, incomplete cleanup leaves stale records that cause duplicate key violations in subsequent test classes.

**Anti-pattern:** Two test classes using the same receiver RUT:
```groovy
// DteGenerationTests.groovy
String receiverRut = "87654321-4"  // Creates PartyIdentification

// DteEnvioSignatureTests.groovy (runs after DteGenerationTests in suite)
String receiverRut = "87654321-4"  // FAILS: "Duplicated Rut" from check#Rut
```

**Rules:**
- **Each test class must use unique fixture identifiers** that cannot collide with other test classes in the same suite
- **Use test-class-specific prefixes** for RUTs, party IDs, and entity IDs (e.g., `TEST_ENVIO_SIG_` vs `TEST_DTE_`)
- **Use valid but uncommon RUTs** — avoid well-known RUTs that appear in seed/demo data (e.g., `66666666-6` is a special SII RUT loaded as seed data)
- **Deterministic IDs must include class-specific segments**: If using deterministic `FiscalTaxDocument` IDs like `{type}-{rut}-{folio}`, the RUT portion must be class-unique

**Correct pattern:**
```groovy
// DteGenerationTests.groovy
String receiverRut = "87654321-4"

// DteEnvioSignatureTests.groovy — use a DIFFERENT valid RUT
String receiverRut = "76086428-5"
```

### Critical Data Isolation Strategy

- **Shared Infrastructure Data**: Load in `setupSpec()` - login data, base organizations, basic configuration
- **Test-Specific Data**: Create per-test in `setup()` or within individual test methods
- **Per-test cleanup**: Always clean up test-specific data in `cleanup()` or test-specific cleanup blocks

**Best Practice Pattern:**
```groovy
class ExampleTests extends Specification {
    @Shared String testInfrastructureId
    @Shared List<String> createdIds = []

    def setupSpec() {
        // ONLY shared infrastructure (authentication, base config)
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("john.doe", "moqui")
        ec.entity.tempSetSequencedIdPrimary("EntityName", 58000, 10)
    }

    def cleanupSpec() {
        // Clean up in reverse order of creation
        createdIds.reverse().each { id ->
            try {
                ec.service.sync().name("delete#EntityName")
                    .parameters([id: id])
                    .disableAuthz().call()
            } catch (Exception e) {
                logger.warn("Cleanup failed: ${e.message}")
            }
        }
        ec.entity.tempResetSequencedIdPrimary("EntityName")
        ec.destroy()
    }

    def setup() {
        ec.message.clearAll()  // CRITICAL: Clear all messages from previous tests
        ec.artifactExecution.disableAuthz()
        ec.transaction.begin(null)
    }

    def cleanup() {
        // Clean up test-specific data created in this test
        cleanupTestSpecificData()

        ec.artifactExecution.enableAuthz()
        ec.transaction.commit()
    }
}
```

### Why This Pattern is Critical

- **Test Independence**: Each test can run in isolation without depending on data from other tests
- **Debugging**: Failed tests don't contaminate subsequent tests
- **Parallel Execution**: Tests can run in parallel without data conflicts
- **Reliability**: Eliminates mysterious test failures from shared data state

## Error Handling Best Practices

### CRITICAL: Entity Auto-Service Silent Failures

**Problem**: Entity auto-services (like `create#EntityName`) add errors to `ec.message` but **do NOT throw exceptions** when FK constraints or validation fails. This causes tests to continue with broken state, leading to confusing cascading failures.

**Failure Chain Example**:
1. `create#OrderHeader` fails (e.g., missing `OrderOpen` status in seed data)
2. Error added to `ec.message.errors` but **no exception thrown**
3. Test code continues to next line
4. `create#OrdenCompra` fails (FK to OrderHeader doesn't exist)
5. `create#CertificadoRecepcionConforme` fails (FK to OrdenCompra doesn't exist)
6. **Visible error**: FK constraint on CertificadoRecepcionConforme - misleading!

**Root Cause**: The actual failure was in step 1 (missing seed data), but the visible error is from step 5.

**Solution**: Add error checking after every entity creation in test setup:

```groovy
/**
 * Helper method to check for errors after service calls.
 * Throws RuntimeException if errors exist to fail the test immediately.
 */
private void checkForErrors(String operationName) {
    if (ec.message.hasError()) {
        def errors = ec.message.errorsString
        ec.message.clearErrors()
        throw new RuntimeException("Failed during ${operationName}: ${errors}")
    }
}

private void createTestInfrastructure() {
    // Create entity using auto-service
    ec.service.sync().name("create#mantle.order.OrderHeader")
        .parameters([
            orderId: testOrderId,
            orderName: "Test Order",
            currencyUomId: "CLP",
            statusId: "OrderOpen"
        ])
        .call()
    checkForErrors("create OrderHeader ${testOrderId}")  // CRITICAL: Check immediately!
    createdEntityIds.add("OrderHeader:${testOrderId}")

    // Continue with dependent entities...
    ec.service.sync().name("create#mycompany.myapp.ExampleEntity")
        .parameters([orderId: testOrderId])
        .call()
    checkForErrors("create ExampleEntity ${testOrderId}")  // CRITICAL: Check before FK-dependent entities
    createdEntityIds.add("ExampleEntity:${testOrderId}")
}
```

**Why This Pattern is Critical**:
- **Fail Fast**: Tests fail immediately at the point of failure with clear error message
- **Clear Errors**: Error says "Failed during create OrderHeader" not "FK violation on CertificadoRecepcionConforme"
- **Reveals Root Cause**: Missing seed data (OrderOpen status, CLP UOM) is immediately visible
- **Prevents Cascading Failures**: Stops before creating dependent entities that will also fail

**Common Causes of Silent Failures**:
| Entity Creation | Common Missing Seed Data |
|-----------------|--------------------------|
| `create#mantle.order.OrderHeader` | `OrderOpen` status, `CLP` currency UOM |
| `create#mantle.party.Party` | `PtyOrganization` party type |
| `create#mantle.work.effort.WorkEffort` | `WetTask` type, `WeInPlanning` status |
| `create#moqui.security.UserAccount` | Locale settings, timezone |

**Related**: See "Data Type Contents Reference" section for required seed data types.

### Understanding Moqui's Error Handling Mechanism

**CRITICAL: Moqui's error handling is primarily based on the Message Facade, NOT on service result maps.**

Moqui services handle errors through two distinct mechanisms:

1. **Primary Mechanism: Message Facade (ec.message)**
   - **Most common**: Errors are added to the ExecutionContext's MessageFacade
   - Services call `ec.message.addError()` to record errors
   - Check for errors using `ec.message.hasError()`
   - Retrieve error messages with `ec.message.getErrorsString()` or `ec.message.getErrorMessages()`
   - **Empty result map**: When a service encounters an error, it typically returns an empty map `[:]`
   - **This is the expected behavior** - don't expect `result.errors` to be populated

2. **Secondary Mechanism: Result Map `errors` Field (RARE)**
   - **Exceptional cases only**: Some services populate a `result.errors` field
   - This is NOT the standard Moqui pattern
   - Only used in specific custom services or legacy code
   - **Do not rely on this** in general testing

**Correct Error Detection Pattern:**
```groovy
def "Test service error handling - CORRECT"() {
    when:
    def result = ec.service.sync().name("create#Entity")
        .parameters([invalidData: "bad"]).call()

    then: "Check Message Facade for errors"
    ec.message.hasError()  // CORRECT - Primary error detection
    !result.entityId       // CORRECT - Result map will be empty or missing expected fields

    cleanup:
    ec.message.clearAll()  // Clear the expected error
}
```

**Incorrect Error Detection Pattern:**
```groovy
def "Test service error handling - INCORRECT"() {
    when:
    def result = ec.service.sync().name("create#Entity")
        .parameters([invalidData: "bad"]).call()

    then: "DON'T do this"
    result.errors          // WRONG - This field is rarely populated
    result && result.errors // WRONG - Unreliable error detection
}
```

### Message Clearing Strategy

**CRITICAL: Always clear ALL messages at the beginning of each test:**

```groovy
def setup() {
    ec.message.clearAll()  // MUST be first - clears ALL messages from previous tests
    ec.artifactExecution.disableAuthz()
    ec.transaction.begin(null)
}
```

**When to Clear Messages:**
- **In setup()**: Always clear at the start of each test
- **In test cleanup blocks**: After intentionally triggering errors that you've validated
- **In cleanup()**: Never clear here - let unexpected errors fail the test
- **In cleanupSpec()**: Only if absolutely necessary, with a warning log

**Example Pattern for Tests That Expect Errors:**
```groovy
def "Test that validates error handling"() {
    when: "Trigger an expected error"
    service.callThatShouldFail()

    then: "Verify the error"
    ec.message.hasError()
    ec.message.getErrorsString().contains("Expected error")

    cleanup: "Clear the expected error"
    ec.message.clearAll()  // Only clear messages you intentionally triggered
}
```

## Test Cleanup Best Practices

### Manual Entity Deletion in Dependency Order

For comprehensive test data cleanup, delete entities manually in the correct dependency order:

```groovy
private void cleanupTestData() {
    ec.artifactExecution.disableAuthz()

    try {
        // Delete in correct dependency order: child entities first, parent entities last

        // 1. Delete child entities that reference parent entities
        ec.entity.find("ChildEntity")
            .condition("parentId", testEntityId)
            .deleteAll()

        // 2. Delete related entities with foreign key dependencies
        ec.entity.find("RelatedEntity")
            .condition("entityId", testEntityId)
            .deleteAll()

        // 3. Delete the main entity last (after all references are removed)
        ec.entity.find("MainEntity")
            .condition("entityId", testEntityId)
            .deleteAll()

        logger.info("Deleted test entities in dependency order: ${testEntityId}")
    } catch (Exception e) {
        logger.warn("Error during manual cleanup: ${e.message}")
    } finally {
        ec.artifactExecution.enableAuthz()
    }
}
```

### Transaction Isolation for Cleanup

To prevent cleanup errors from propagating to other test suites:

```groovy
def cleanup() {
    // Commit main test transaction
    ec.transaction.commit()

    // Create new transaction for cleanup
    ec.transaction.begin(null)
    try {
        cleanupTestSpecificData()
        ec.message.clearAll()
        ec.transaction.commit()
    } catch (Exception e) {
        logger.warn("Error during cleanup: ${e.message}")
        ec.transaction.rollback(null, null)
    }
}
```

### Using Delete Services (Preferred)

**Prefer using auto-delete services over direct entity deletion:**

```groovy
def cleanup() {
    try {
        createdEntityIds.each { id ->
            ec.service.sync().name("delete#EntityName")
                .parameters([entityId: id])
                .disableAuthz().call()
        }
    } catch (Exception e) {
        logger.warn("Cleanup failed: ${e.message}")
    }
}
```

**Why Use Delete Services:**
- **Failure Resilience**: Service failures don't throw exceptions that stop cleanup execution
- **Business Logic**: Delete services handle cascading deletes and business rules properly
- **Consistency**: Same deletion logic used in production code
- **Authorization**: Can disable authorization specifically for cleanup operations

## EntityFilter Testing Strategy

**Problem**: Testing features that rely on EntityFilters requires proper authorization setup.

### Core Testing Principles

**CRITICAL: EntityFilter testing is opposite of normal test patterns:**
- **Normal tests**: Disable authorization for simpler testing
- **EntityFilter tests**: Enable authorization to activate filters
- **Both approaches are valid** depending on what you're testing

### When to Test With EntityFilters

**Test WITH EntityFilters (authorization enabled) when:**
- Testing access control and security constraints
- Validating tenant separation and data isolation
- Testing user-specific data filtering
- Verifying organizational hierarchy access

**Test WITHOUT EntityFilters (authorization disabled) when:**
- Testing pure business logic
- Testing service functionality independent of access control
- Performance testing without security overhead
- Integration testing with external systems

### EntityFilter Test Setup Pattern

```groovy
class EntityFilterServiceTest extends Specification {
    ExecutionContext ec

    def setupSpec() {
        ec = Moqui.getExecutionContext()
    }

    def setup() {
        // ENABLE authorization (opposite of normal tests)
        ec.artifactExecution.enableAuthz()

        // Set user context for filtering
        ec.user.context.activeOrgId = "TEST_ORG"
        ec.user.context.filterOrgIds = ["TEST_ORG"]

        // Login as specific user with proper permissions
        ec.user.loginUser("john.doe", "moqui", null)
    }

    def cleanup() {
        // Clear filter context
        ec.user.context.remove('activeOrgId')
        ec.user.context.remove('filterOrgIds')

        // Logout if logged in
        if (ec.user.userId) {
            ec.user.logoutUser()
        }

        // Disable authorization for cleanup
        ec.artifactExecution.disableAuthz()
    }
}
```

## Common Pitfalls and Solutions

### Pitfall 0: Entity Auto-Service Silent Failures (Most Common)
**Symptom**: FK constraint violation on entity C, but you created entities A → B → C in correct order
**Cause**: Entity A or B creation failed silently (added error to ec.message but didn't throw exception)
**Solution**: Add `checkForErrors()` after each entity creation in test setup
**See**: "CRITICAL: Entity Auto-Service Silent Failures" in Error Handling Best Practices section

### Pitfall 1: Service Not Found
**Symptom**: "Service not found" errors
**Cause**: Component not properly loaded
**Solution**: Verify component dependencies in component.xml

### Pitfall 2: Authorization Failures
**Symptom**: "User does not have permission" errors
**Solution**: Use `ec.artifactExecution.disableAuthz()` in setup()

### Pitfall 2a: Multi-Group Permission Testing
**Symptom**: Permission removal test still passes when it should fail
**Cause**: Test user (e.g., `john.doe`) has the same permission from multiple groups. For example, `john.doe` is in both `ADMIN` and `BG_ADMIN` groups, and both may have `MATTER_VIEW` permission.
**Solution**: When testing permission denial:
1. Query ALL groups that grant the permission
2. Remove the permission from ALL groups, not just one
3. Re-login to refresh the user's session cache

```groovy
// WRONG: Only removes from one group
ec.service.sync().name("delete#moqui.security.UserGroupPermission")
    .parameters([userGroupId: "BG_ADMIN", userPermissionId: "MATTER_VIEW", fromDate: bgAdminFromDate])
    .call()

// CORRECT: Remove from ALL groups that have this permission
def bgAdminFromDate = ec.entity.find("moqui.security.UserGroupPermission")
    .condition("userGroupId", "BG_ADMIN").condition("userPermissionId", "MATTER_VIEW").one()?.fromDate
def adminFromDate = ec.entity.find("moqui.security.UserGroupPermission")
    .condition("userGroupId", "ADMIN").condition("userPermissionId", "MATTER_VIEW").one()?.fromDate

if (bgAdminFromDate) {
    ec.service.sync().name("delete#moqui.security.UserGroupPermission")
        .parameters([userGroupId: "BG_ADMIN", userPermissionId: "MATTER_VIEW", fromDate: bgAdminFromDate]).call()
}
if (adminFromDate) {
    ec.service.sync().name("delete#moqui.security.UserGroupPermission")
        .parameters([userGroupId: "ADMIN", userPermissionId: "MATTER_VIEW", fromDate: adminFromDate]).call()
}

// CRITICAL: Clear ALL entity caches (not just entity.record) and re-login
ec.cache.clearCachesByPrefix("entity")  // hasPermission() uses list cache
ec.user.logoutUser()
ec.user.loginUser("john.doe", "moqui")
ec.cache.clearCachesByPrefix("entity")  // Clear again after login
```

**Note**: If this still doesn't work, see Pitfall 2c about duplicate permission records with different `fromDate` values.

### Pitfall 2b: Cleanup After Permission Removal
**Symptom**: Cleanup fails with "User is not authorized for Create on Entity moqui.security.UserGroupPermission"
**Cause**: After removing user permissions in a test, the cleanup block can't restore them because the user no longer has permission to create `UserGroupPermission` records.
**Solution**: Use `.disableAuthz()` on all cleanup service calls that modify security records:

```groovy
cleanup:
// WRONG: Will fail if user permissions were removed during test
ec.service.sync().name("create#moqui.security.UserGroupPermission")
    .parameters([...]).call()

// CORRECT: Bypass authorization for cleanup
ec.service.sync().name("create#moqui.security.UserGroupPermission").disableAuthz()
    .parameters([
        userGroupId: "BG_ADMIN",
        userPermissionId: "MATTER_VIEW",
        fromDate: bgAdminFromDate
    ]).call()
```

### Pitfall 2c: Duplicate Permission Records with Different fromDates
**Symptom**: Permission removal test still fails even after removing permission from multiple groups and clearing cache
**Cause**: There may be MULTIPLE `UserGroupPermission` records for the same permission with DIFFERENT `fromDate` values (e.g., one from seed data with epoch 0, another created by test setup/cleanup with a different timestamp). Querying with `.one()` only finds one record.
**Solution**: Query for ALL permission records using `.list()` and delete ALL of them:

```groovy
// WRONG: Only finds and deletes ONE record
def bgAdminFromDate = ec.entity.find("moqui.security.UserGroupPermission")
    .condition("userGroupId", "BG_ADMIN").condition("userPermissionId", "MATTER_VIEW").one()?.fromDate

// CORRECT: Find and delete ALL matching records
def bgAdminPermList = ec.entity.find("moqui.security.UserGroupPermission")
    .condition("userGroupId", "BG_ADMIN")
    .condition("userPermissionId", "MATTER_VIEW")
    .disableAuthz().useCache(false).list()

bgAdminPermList.each { perm ->
    ec.service.sync().name("delete#moqui.security.UserGroupPermission")
        .disableAuthz().requireNewTransaction(true)
        .parameters([userGroupId: "BG_ADMIN", userPermissionId: "MATTER_VIEW", fromDate: perm.fromDate])
        .call()
}

// Clear ALL entity caches and re-login
ec.cache.clearCachesByPrefix("entity")  // Clears both entity.record AND entity.list caches
ec.user.logoutUser()
ec.user.loginUser("john.doe", "moqui")
ec.cache.clearCachesByPrefix("entity")  // Clear again after login to be safe
```

Key points:
- Use `.useCache(false)` to get fresh data from database
- Use `.requireNewTransaction(true)` to ensure deletes are committed immediately
- Clear cache with `"entity"` prefix (not `"entity.record"`) to clear ALL entity caches including list caches used by `hasPermission()`
- Clear cache again AFTER login since login may re-cache permissions

### Pitfall 2d: ArtifactAuthorizationException vs Permission Check
**Symptom**: Test expects service to return error result, but `ArtifactAuthorizationException` is thrown instead
**Cause**: Services can be protected by BOTH Moqui's artifact authorization (ArtifactAuthz records) AND internal permission checks (using `hasPermission()`). When `ec.artifactExecution.enableAuthz()` is called, artifact authorization runs BEFORE the service code executes.
**Solution**: Test should handle both scenarios:

```groovy
def result = null
def authzExceptionThrown = false

try {
    ec.artifactExecution.enableAuthz()
    result = ec.service.sync().name("MyService.myMethod").parameters([...]).call()
} catch (org.moqui.context.ArtifactAuthorizationException e) {
    authzExceptionThrown = true  // Service blocked by artifact authorization
} finally {
    ec.artifactExecution.disableAuthz()
}

then: "service should be blocked by authorization"
authzExceptionThrown || validateServiceResult(result, "myMethod without permission", true)
```

### Pitfall 3: Transaction Deadlocks
**Symptom**: Tests timeout or deadlock
**Solution**:
- Use unique database/transaction resources per component
- Set `maxParallelForks 1` in build.gradle
- Use unique transaction log filenames

### Pitfall 4: Validation vs Execution Errors
**Symptom**: Tests expect errorMessage but get null
**Cause**: Validation errors go to message context, not return map
**Solution**: Check `ec.message.hasError()` first

### Pitfall 5: Incorrect Entity Deletion Syntax
**Symptom**: "No such method: ec.entity.delete(entity)" errors
**Cause**: Using non-existent `ec.entity.delete(entity)` method
**Solution**: Call `delete()` method directly on the entity value
```groovy
// INCORRECT - This method doesn't exist
ec.entity.delete(entityValue)

// CORRECT - Call delete() on the entity value directly
entityValue.delete()

// CORRECT - Example with find and delete
def party = ec.entity.find("mantle.party.Party").condition("partyId", testId).one()
if (party) party.delete()
```

## Test Implementation Template

```groovy
package com.example

import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import spock.lang.Shared
import spock.lang.Specification

class ExampleTests extends Specification {
    @Shared ExecutionContext ec
    @Shared List<String> createdIds = []

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("john.doe", "moqui")
        ec.entity.tempSetSequencedIdPrimary("EntityName", 58000, 10)
    }

    def cleanupSpec() {
        // Cleanup in reverse order of creation
        createdIds.reverse().each { id ->
            try {
                ec.service.sync().name("delete#EntityName")
                    .parameters([id: id])
                    .disableAuthz().call()
            } catch (Exception e) {
                logger.warn("Cleanup failed: ${e.message}")
            }
        }
        ec.entity.tempResetSequencedIdPrimary("EntityName")
        ec.destroy()
    }

    def setup() {
        ec.message.clearAll()               // Clear all messages for clean state
        ec.artifactExecution.disableAuthz()
        ec.transaction.begin(null)
    }

    def cleanup() {
        ec.artifactExecution.enableAuthz()
        ec.transaction.commit()
    }

    def "test example"() {
        when:
        Map result = ec.service.sync().name("service.name")
            .parameters([param: "value"])
            .call()
        if (result?.id) createdIds.add(result.id)

        then:
        !ec.message.hasError()  // Check errors without clearing
        result?.errorMessage == null
        result.expectedField == "expectedValue"
    }
}
```

## Quick Test Implementation Checklist

Before implementing tests, verify:

- [ ] **Authentication Strategy**: Use john.doe or custom user with proper setup
- [ ] **Data Loading**: Run `./gradlew cleanDb load` with appropriate data types first
- [ ] **Suite Structure**: Create ComponentSuite.groovy
- [ ] **Build Configuration**: Set maxParallelForks=1
- [ ] **Sequence Management**: Use tempSetSequencedIdPrimary
- [ ] **Error Handling**: Check ec.message.hasError() for errors
- [ ] **Cleanup Strategy**: Track created IDs for cleanup
- [ ] **Authorization Strategy**: Choose based on test type:
  - [ ] **Business Logic Tests**: Disable authz in setup/cleanup
  - [ ] **EntityFilter Tests**: Enable authz + proper ArtifactAuthz setup
  - [ ] **Security Tests**: Enable authz + test both scenarios

## Playwright Setup

Framework-level installation with component-specific configurations.

**Setup**: See `PLAYWRIGHT-SETUP.md` for detailed installation and configuration instructions.

**Basic Usage**:
```bash
# Run all E2E tests
npm test

# Run component-specific tests
npm run test:{component-name}

# Debug mode
npm run test:debug
```

### Playwright MCP Setup

Playwright MCP enables Claude Code to interact with browsers directly for visual development, CSS iteration, and cross-browser verification.

**Key Capabilities:**
- **Visual Development**: Iterate on CSS styling with real-time feedback
- **Reference Image Comparison**: Compare implementations against design mockups
- **Element Inspection**: Debug layout issues and accessibility
- **Cross-Browser Testing**: Verify UI consistency between Firefox and Chrome

**Setup**: See `.agent-os/playwright-mcp-setup.md` for installation and configuration.

## Continuous Integration

### Pipeline Strategy
1. **Fast Feedback**: Run unit tests first
2. **Integration Validation**: Run integration tests after unit tests pass
3. **E2E Validation**: Run critical E2E tests as final step
4. **Parallel Execution**: Run tests in parallel where possible

### Test Reporting
- Unit/Integration: JUnit XML reports from Gradle
- E2E: HTML reports from Playwright
- Coverage: Jacoco reports for code coverage
- Artifacts: Store test reports and traces for failed tests

## Performance Considerations

### Test Execution Speed
- **Unit tests**: Should complete in seconds
- **Integration tests**: Should complete within minutes
- **E2E tests**: Acceptable to take longer, but optimize critical paths

### Performance Optimization Tips
1. **Minimize Framework Restarts**: Use Suite pattern to initialize once
2. **Batch Test Data Creation**: Create shared test data in setupSpec()
3. **Avoid Database Resets**: Use unique IDs instead of clearing tables
4. **Disable Unnecessary Features**: Use `moqui.init.static=true`
5. **Use In-Memory Database**: H2 for tests, not production DB

## Troubleshooting

### Debugging Failed Tests

When tests fail:

1. **Check Authentication First**: 90% of failures are auth-related
2. **Verify Data Loading**: Ensure seed data exists
3. **Examine Service Definitions**: Confirm service exists and parameters match
4. **Review Error Messages**: Check ec.message.errors
5. **Inspect Transaction Logs**: Look for deadlocks or timeouts
6. **Enable Debug Logging**: `testLogging.showStandardStreams = true`

### Common Issues

**Spock Tests**:
- Database connection issues: Verify main database configuration
- Service not found: Check service registration and imports
- Data inconsistency: Ensure proper test data cleanup and transaction management
- Commented-out demo data: See "Commented-Out Demo Data" section below

### Bypassing EECAs in Test Setup with `disableEntityEca()`

When multiple test classes share entity data (e.g., PartyIdentification RUTs) and cleanup between classes is imperfect, validation EECAs may reject test data creation. Use `ec.artifactExecution.disableEntityEca()` to bypass EECAs during test setup:

```groovy
// Bypass check#Rut EECA: duplicate RUT may exist from other test classes
boolean wasDisabled = ec.artifactExecution.disableEntityEca()
ec.service.sync().name("create#mantle.party.PartyIdentification")
    .parameters([partyId: testPartyId, partyIdTypeEnumId: "PtidNationalTaxId",
        idValue: "88888888-8"]).call()
if (!wasDisabled) ec.artifactExecution.enableEntityEca()
```

**When to use**: Only in `setupSpec()`/`initializeTestData()` for shared test infrastructure, never in actual test methods. The pattern preserves the previous EECA state (in case it was already disabled).

**Root cause**: Suite-based test execution runs all test classes in a single Moqui instance. If class A creates PartyIdentification with RUT `88888888-8` and cleanup fails to delete it, class B's `create#PartyIdentification` triggers the `check#Rut` EECA which rejects the duplicate.

**Playwright Tests**:
- Element not found: Verify selectors and wait conditions
- Test flakiness: Add proper waits and reduce timing dependencies
- Browser issues: Update browser installations

### Commented-Out Demo Data

**Problem**: Tests fail with "configuration not found" or "entity not found" errors even when demo data files exist.

**Cause**: Demo data XML files may contain large sections wrapped in XML comments (`<!-- ... -->`), causing the data to not be loaded.

**Symptoms**:
- Tests pass in some environments but fail in others
- "No configuration found for X" errors
- Entity queries return null for IDs that appear in data files
- Tests worked previously but now fail after "no code changes"

**Diagnosis**:
```bash
# Check for commented sections in data files
grep -n "<!--\|-->" runtime/component/{component}/data/*.xml

# Verify specific entity data is being loaded
grep -n "entityName.*entityId" runtime/component/{component}/data/*.xml
```

**Example**: BookingGroupRule data commented out
```xml
<!-- Line 45: Comment starts here
    <mycompany.myapp.ExampleEntity facilityGroupId="10000" ... />
    ... 100+ lines of booking configuration ...
--> <!-- Line 163: Comment ends here -->
```

**Solution**:
1. Uncomment the required data sections, OR
2. Add `@Ignore` to tests with explanation of missing data, OR
3. Create separate test data file with required entities

**Prevention**:
- When commenting out demo data, add a note about affected tests
- Use separate files for optional demo data vs required test data
- Document data dependencies in test class comments

## Related Documentation

- **E2E Test Data Strategy**: See `.agent-os/e2e-test-data-strategy.md`
- **Moqui Caching Best Practices**: See `.agent-os/moqui-caching-best-practices.md`
- **Detailed Playwright Setup**: See `PLAYWRIGHT-SETUP.md`
- **Playwright MCP Screen Validation**: See `.agent-os/playwright-mcp-screen-validation.md`
- **Moqui Framework Guide**: See `.agent-os/framework-guide.md`
- **Development Environment**: See `.agent-os/development-guide.md`
- **Commit Guidelines**: See `.agent-os/commit-guidelines.md`

---

*This guide provides a unified testing strategy for all Moqui Framework projects. Component-specific testing patterns should be documented in the component's `.agent-os/` directory.*