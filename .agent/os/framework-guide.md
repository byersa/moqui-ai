# Moqui Framework Guide

This document provides comprehensive guidance for working with Moqui Framework projects.

## Moqui Framework Overview

Moqui Framework is a Java-based enterprise application framework with:
- **Build System**: Gradle with custom tasks for component management
- **Database**: Supports multiple databases (H2, Derby, PostgreSQL, etc.)
- **Search**: OpenSearch/ElasticSearch integration
- **Components**: Modular architecture with runtime components

**Framework Reference**: When referring to "the framework" in Moqui projects, this typically means the moqui-framework repository, which serves as the main directory structure for any Moqui project. The framework repository contains the core Moqui framework code and orchestrates the entire multi-repository project structure.

## Project Structure

All Moqui projects follow the standard multi-repository structure:

- **Framework Repository**: The entire repository (named after the main component)
  - Contains the main Moqui framework in `framework/` directory
  - Contains the runtime environment in `runtime/` directory
  - Orchestrates multiple git repositories through gradle tasks
  - **IMPORTANT**: Only files in the framework root are tracked in this repository

- **Runtime Repository**: `runtime/` directory
  - **Separate git repository** from the framework
  - Contains configuration files, logs, database, and runtime-specific content
  - Files added to `runtime/` (except components) are committed to the runtime repository

- **Main Component**: `runtime/component/{component-name}` 
  - This is the primary custom component being developed
  - **Separate git repository** with its own commit history
  - **This component's repository is the focus of development work**

- **Other Components**: Located in `runtime/component/`
  - Standard Moqui components (mantle-udm, SimpleScreens, etc.)
  - Third-party and custom components
  - **Each component is its own git repository** with independent versioning

## Agent OS Configuration Structure and Placement Guidelines

**CRITICAL: When modifying Agent OS files or Claude configurations, always evaluate:**

1. **Is the content framework-level (project-neutral)?**
   - Testing strategies, infrastructure patterns, general development workflows → `.agent-os/`
   - Framework-wide coding standards, commit guidelines → `.agent-os/`
   - IDE setup, deployment pipelines, multi-repository management → `.agent-os/`

2. **Is the content component-specific?**
   - Business domain, product mission, user personas → `runtime/component/{component}/.agent-os/`
   - Component-specific entities, services, or workflows → `runtime/component/{component}/.agent-os/`
   - Regulatory requirements, compliance details → `runtime/component/{component}/.agent-os/`

3. **Can content be split between both?**
   - General pattern/strategy → Framework `.agent-os/`
   - Specific implementation/examples → Component `.agent-os/`
   - Example: Testing strategy (framework) vs. component test structure (component)

**Before adding or modifying Agent OS content:**
- Check if similar content already exists in either location
- Ensure framework content remains project-neutral (use placeholders like `{component}`)
- Keep business-specific details in component directories only

## Multi-Repository File Management

**CRITICAL: Each directory level has its own git repository:**

### Framework Repository Files
- `framework/` - Framework code
- `build.gradle`, `settings.gradle` - Build configuration
- `Claude.md` - Framework-level documentation
- `.agent-os/` - Framework-level Agent OS configuration
- Root-level configuration files

### Runtime Repository Files  
- `runtime/conf/` - Configuration files
- `runtime/log/` - Application logs
- `runtime/db/` - Database files when using integrated DB
- Files directly under `runtime/` (not in component subdirectories)

### Component Repository Files
- `runtime/component/{component-name}/` - Each component is its own repository
- `runtime/component/{component-name}/.agent-os/` - Component-specific Agent OS configuration
- When adding files to a component, commit within that component's repository

### Adding Files - Repository Navigation
```bash
# Framework files - commit from framework root
git add Claude.md .agent-os/
git commit -m "framework change"

# Runtime files - navigate to runtime and commit there
cd runtime
git add conf/MoquiDevConf.xml
git commit -m "runtime config change"

# Component files - navigate to specific component
cd runtime/component/{component-name}
git add src/new-file.groovy .agent-os/
git commit -m "component change"
```

**Always check which repository you're in before committing:**
```bash
pwd  # Check current directory
git status  # Verify you're in the correct repository
```

## Key Directories

- `framework/` - Core Moqui framework code
- `runtime/component/{main-component}/` - **Main component** (primary development focus)
- `runtime/component/` - All modular components (both standard Moqui and custom)
- `runtime/conf/` - Configuration files
- `runtime/db/` - Database files
- `runtime/log/` - Application logs

## Common Development Commands

### Building and Running
```bash
# Build the application
./gradlew build

# Run in development mode
./gradlew run

# Run in production mode
./gradlew runProduction

# Clean build artifacts
./gradlew clean
```

### Data Management
```bash
# Load all data types
./gradlew load

# Load seed data only
./gradlew loadSeed

# Load seed and initial data
./gradlew loadSeedInitial

# Load production data
./gradlew loadProduction
```

### Component Management
```bash
# Get a specific component
./gradlew getComponent -Pcomponent=ComponentName

# Get all dependencies
./gradlew getDepends

# Get runtime if not present
./gradlew getRuntime
```

### Testing

**Multi-Layer Testing Strategy:**

**Unit & Integration Testing (Spock Framework):**
- Use mantle-usl as reference for comprehensive testing patterns
- **Each component with tests MUST have a build.gradle file configured for testing**
- Components without build.gradle cannot run tests
- **Always clean database before running tests to ensure isolation**

**End-to-End Testing (Playwright Framework):**
- Framework-level installation with component-specific configurations
- Located in `runtime/component/{component}/tests/` directories
- Multi-browser support (Chromium, Firefox, WebKit)
- See `PLAYWRIGHT-SETUP.md` for setup and `.agent-os/testing-guide.md` for comprehensive strategy

**Testing Requirements:**
1. `build.gradle` file in component root directory
2. `src/test/groovy/` directory structure
3. Test files ending with `*Tests.groovy` or `*BasicFlow.groovy`

**Critical Test Standards:**
- **Always use `java.sql.Timestamp`** instead of `java.util.Date` for date-time variables
- **Always call `ec.message.clearErrors()`** in test cleanup blocks to prevent error state leakage
- **Delete child entities before parent entities** to avoid foreign key constraint violations (especially for Party entities)
- **Always use transaction pattern**: `ec.transaction.begin(null)` in setup(), `ec.transaction.commit()` in cleanup()
4. Proper dependencies configured in build.gradle

**Recommended Test Execution Pattern:**
```bash
# ALWAYS clean database and load fresh data with ALL required types before tests
./gradlew cleanDb load :runtime:component:{component-name}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test

# Examples (note: -Ptypes is MANDATORY, never omit it):
./gradlew cleanDb load :runtime:component:{component-1}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test
./gradlew cleanDb load :runtime:component:{component-2}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-2}-test
./gradlew cleanDb load :runtime:component:{shared-component}:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{shared-component}-test
```

**Alternative Test Commands:**
```bash
# Run all tests for all components (after clean)
./gradlew cleanDb load test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test

# Run tests with specific data types (always include the full set)
./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test :runtime:component:{component-name}:test

# Start/stop OpenSearch for tests (if needed)
./gradlew startElasticSearch
./gradlew stopElasticSearch
```

**Testing Frameworks:**

**Spock (Unit & Integration):**
- Uses Spock testing framework (Groovy-based)
- Test files should end with `*Tests.groovy` or `*BasicFlow.groovy`
- Located in `src/test/groovy/` directory within each component
- Requires build.gradle configuration with test dependencies
- See `mantle-usl/src/test/groovy/` for comprehensive test examples

**Playwright (End-to-End):**
- JavaScript/TypeScript-based browser automation
- Test files should end with `*.spec.js` or `*.spec.ts`
- Located in `runtime/component/{component}/tests/` directories
- Component-specific configurations extend framework base config
- See existing component tests for domain-specific examples

**Essential build.gradle Configuration for Tests:**
```gradle
apply plugin: 'java-library'
apply plugin: 'groovy'

dependencies {
    api project(':framework')
    testImplementation project(':framework').configurations.testImplementation.allDependencies
    // Add component dependencies as needed
}

test {
    useJUnitPlatform()
    testLogging { events "passed", "skipped", "failed" }
    testLogging.showStandardStreams = true
    testLogging.showExceptions = true
    maxParallelForks 1

    dependsOn cleanTest
    include '**/*Tests.class'
    include '**/*BasicFlow.class'
    
    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
    // Load component-specific test data for isolation
    systemProperty 'moqui.load.data.types', 'seed,seed-initial,{project}-demo,{l10n}-install'
    maxHeapSize = "512M"
    
    classpath += files(sourceSets.main.output.classesDirs)
    classpath = classpath.filter { it.exists() }
    
    beforeTest { descriptor -> logger.lifecycle("Running test: ${descriptor}") }
}
```

### Git Operations
```bash
# Pull all repositories (framework, runtime, components)
./gradlew gitPullAll

# Check status of all git repositories
./gradlew gitStatusAll

# Checkout specific branch across all repos
./gradlew gitCheckoutAll -Pbranch=branch-name
```

### Database Operations
```bash
# Clean database
./gradlew cleanDb

# Save database state
./gradlew saveDb

# Clean and reload from saved state
./gradlew reloadSave
```

## Standard Moqui Components

- **mantle-udm**: Universal Data Model - core business entities
- **mantle-usl**: Universal Service Library - business logic services
- **SimpleScreens**: Standard business application screens
- **moqui-fop**: PDF generation using Apache FOP
- **moqui-poi**: Excel/Office document handling
- **moqui-hazelcast**: Distributed caching
- **moqui-metrics**: Performance monitoring

## Configuration Files

### Framework Configuration
- `MoquiInit.properties` - Initial runtime configuration
- `runtime/conf/MoquiDevConf.xml` - Development environment settings
- `runtime/conf/MoquiProductionConf.xml` - Production environment settings
- `addons.xml` - Available components and repositories
- `myaddons.xml` - Local component configuration (if exists)

### Main Component Configuration
- `runtime/component/{main-component}/component.xml` - Component definition and dependencies
- `runtime/component/{main-component}/MoquiConf.xml` - Component-specific Moqui configuration
- `runtime/component/{main-component}/data/` - Component data files (seed, demo, setup data)
- `runtime/component/{main-component}/entity/` - Entity definitions and EECAs
- `runtime/component/{main-component}/service/` - Service definitions and SECAs
- `runtime/component/{main-component}/screen/` - Screen definitions
- `runtime/component/{main-component}/resource/` - Static resources and templates

### MoquiConf.xml Merge Behavior

Moqui merges MoquiConf.xml files in order: framework defaults, then each component's MoquiConf.xml, then the runtime conf file (Dev/Production/etc). The merge strategy varies by element type:

**Keyed elements (additive merge)**: Elements with a natural key are merged by that key. A runtime conf entry with the same key updates the existing entry; new keys are added.
- `default-property` — merged by `name`
- `subscreens-item` — merged by `name` within a `screen` element
- `artifact-stats` — merged by `type` + `sub-type`
- `cache` — merged by `name`
- `datasource` — merged by `group-name`

**Single elements (attribute replacement)**: Elements that appear once (like `server-stats`) merge their attributes via `putAll` — later values **completely replace** earlier ones for the same attribute.

**Gotcha: `stats-skip-condition` override**

If a component defines:
```xml
<server-stats stats-skip-condition="pathInfo?.startsWith('/rpc') || pathInfo?.startsWith('/status')">
```

And a runtime conf (e.g., `MoquiProductionConf.xml`) defines:
```xml
<server-stats stats-skip-condition="pathInfo?.startsWith('/rpc') || pathInfo?.startsWith('/rest') || pathInfo?.startsWith('/status')"/>
```

The runtime conf's `stats-skip-condition` **completely replaces** the component-level one (via `putAll`). The component's `/status` condition is only preserved because the runtime conf happens to include it too. Any conditions present only in the component config (e.g., a new `/probe` path) would be silently lost.

**Implication**: When adding paths to `stats-skip-condition`, you must add them to every MoquiConf.xml that defines `server-stats` — not just the component config. Alternatively, only define `stats-skip-condition` in one place (typically the component config) and ensure runtime confs don't redefine it.

**Reference**: `ExecutionContextFactoryImpl.mergeConfigNodes()` (line ~1622)

## Standard Component Structure

Each Moqui component follows a standard directory structure:

### Core Component Files and Directories
- `component.xml` - Component definition, dependencies, and metadata (required)
- `MoquiConf.xml` - Component-specific Moqui configuration (optional)
- `data/` - Data files organized by type:
  - `seed/` - Essential system data required for operation
  - `seed-initial/` - Initial setup data for new installations  
  - `install/` - Installation-specific data
  - `demo/` - Demo/test data for development and training
- `entity/` - Entity definitions and Entity Event Condition Actions (EECAs)
- `service/` - Service definitions and Service Event Condition Actions (SECAs)
- `screen/` - Screen definitions for user interfaces
- `resource/` - Static resources, configuration files, and other assets
- `template/` - FreeMarker templates for screens, emails, reports, and forms

### Source Code Organization
- `src/main/groovy/` or `src/main/java/` - Main source code following Maven structure
- `src/test/groovy/` or `src/test/java/` - Test code
- `src/main/resources/` - Classpath resources

### Optional Component Directories
- `document/` - Markdown documentation files for in-app screen help (ScreenDocument)
- `lib/` or `librepo/` - Component-specific JAR files and dependencies
- `build.gradle` - Component-specific build configuration
- `.agent-os/` - Component-specific Agent OS configuration

### Data File Organization
Data files should be organized by type and loaded in the correct order:
1. `seed` - Core system data, always loaded first
2. `seed-initial` - Setup data for new installations
3. `install` - Installation-specific configuration
4. `demo` - Test and demonstration data
5. `test` - Component-specific test data loaded manually in tests

**Migrations for Running Systems:**
- Use the project's migration system for versioned, automatic data migrations on existing systems
- See `guidelines/data-updates.md` for the generic migration workflow
- Check the organization's overlay for implementation-specific migration conventions

**Test Data Isolation:**
- Each component should use component-specific test data files
- Create dedicated test data files in component's `data/` directory (e.g., `ComponentNameTestData.xml`)
- Load test data manually in test setupSpec() using `ec.entity.makeDataLoader().location("component://{component-name}/data/{TestDataFile}.xml").load()`
- This ensures test data doesn't interfere between components and provides full control over test data loading
- Test data files can use standard `<entity-facade-xml type="test-{component-name}">` format specifying a type that will not be loaded automatically

#### Seed Data Location Requirements

**CRITICAL: `<seed-data>` tags cannot exist at the root level of entity XML files.**

Seed data must be properly placed according to Moqui Framework structural requirements. The framework enforces strict separation between entity definitions and data content.

##### Common Anti-Pattern (Incorrect)
```xml
<entities>
    <entity entity-name="ApiKeyType" package="mycompany.dte.auth">
        <field name="apiKeyTypeId" type="id" is-pk="true"/>
        <field name="description" type="text-medium"/>
    </entity>
    
    <!-- ❌ INCORRECT: seed-data at root level inside <entities> -->
    <seed-data>
        <moqui.basic.EnumerationType enumTypeId="ApiKeyEventType" description="API Key Event Type"/>
        <moqui.basic.Enumeration enumId="ApiKeyEventCreated" enumTypeId="ApiKeyEventType" description="API Key Created" enumCode="CREATED"/>
        <moqui.basic.Enumeration enumId="ApiKeyEventRevoked" enumTypeId="ApiKeyEventType" description="API Key Revoked" enumCode="REVOKED"/>
    </seed-data>
</entities>
```

##### Correct Approaches

**Option 1: Inside Specific Entity Definitions (Limited Use)**
```xml
<entities>
    <entity entity-name="ApiKeyType" package="mycompany.dte.auth">
        <field name="apiKeyTypeId" type="id" is-pk="true"/>
        <field name="description" type="text-medium"/>
        
        <!-- ✅ CORRECT: seed-data inside entity definition -->
        <seed-data>
            <mycompany.dte.auth.ApiKeyType apiKeyTypeId="INTERNAL" description="Internal System API Key"/>
            <mycompany.dte.auth.ApiKeyType apiKeyTypeId="EXTERNAL" description="External Client API Key"/>
        </seed-data>
    </entity>
</entities>
```

**Option 2: Separate Data Files (Recommended)**
```xml
<!-- In data/SeedData.xml or data/ApiKeySeedData.xml -->
<entity-facade-xml type="seed">
    <!-- EnumerationType must be defined before Enumeration records -->
    <moqui.basic.EnumerationType enumTypeId="ApiKeyEventType" description="API Key Event Type"/>
    <moqui.basic.EnumerationType enumTypeId="ApiKeyPeriodType" description="API Key Statistics Period Type"/>
    
    <!-- Enumeration records -->
    <moqui.basic.Enumeration enumId="ApiKeyEventCreated" enumTypeId="ApiKeyEventType" description="API Key Created" enumCode="CREATED"/>
    <moqui.basic.Enumeration enumId="ApiKeyEventRevoked" enumTypeId="ApiKeyEventType" description="API Key Revoked" enumCode="REVOKED"/>
    <moqui.basic.Enumeration enumId="ApiKeyEventExpired" enumTypeId="ApiKeyEventType" description="API Key Expired" enumCode="EXPIRED"/>
    <moqui.basic.Enumeration enumId="ApiKeyEventUsed" enumTypeId="ApiKeyEventType" description="API Key Used" enumCode="USED"/>
    <moqui.basic.Enumeration enumId="ApiKeyEventUpdated" enumTypeId="ApiKeyEventType" description="API Key Updated" enumCode="UPDATED"/>
    
    <moqui.basic.Enumeration enumId="ApiKeyPeriodHour" enumTypeId="ApiKeyPeriodType" description="Hourly Period" enumCode="HOUR"/>
    <moqui.basic.Enumeration enumId="ApiKeyPeriodDay" enumTypeId="ApiKeyPeriodType" description="Daily Period" enumCode="DAY"/>
    <moqui.basic.Enumeration enumId="ApiKeyPeriodMonth" enumTypeId="ApiKeyPeriodType" description="Monthly Period" enumCode="MONTH"/>
</entity-facade-xml>
```

##### Why Location Requirements Matter

**Technical Requirements:**
- **Schema Validation**: Entity XML files follow strict XSD schemas that don't allow root-level `<seed-data>` elements
- **Framework Parsing**: Moqui's entity definition parser expects only entity and view-entity elements at the root level
- **Component Loading**: Data loading happens in separate phases from entity definition loading

**Architectural Benefits:**
- **Separation of Concerns**: Entity definitions focus on structure, data files manage content
- **Maintainability**: Separate data files are easier to manage, update, and version
- **Loading Control**: Data files provide better control over when, how, and in what order data is loaded
- **Environment Flexibility**: Different environments can load different data sets while sharing entity definitions

##### Best Practices for Seed Data Organization

**1. Use Separate Data Files (Recommended)**
- Create dedicated data files in component's `data/` directory
- Use descriptive names: `SeedData.xml`, `EnumerationData.xml`, `{Component}SeedData.xml`
- Always use `type="seed"` for framework-required data

**2. Data File Naming Conventions**

**Using 3-Letter Prefixes for Load Order Control**

Moqui loads data files in **alphabetical order**. Use 3-uppercase-letter prefixes to control the load sequence:

| Position | Purpose | Values |
|----------|---------|--------|
| 1st letter | Category | A = seed/install, D = demo, Z = test |
| 2nd letter | Ordering within category | A-Z (A loads before B, etc.) |
| 3rd letter | Sub-ordering | A-Z (for related files in same group) |

**Example File Structure:**
```
data/
├── AAASetup.xml                    # Initial setup (screens, roles, artifact groups)
├── ABAEngagementData.xml           # Base enumerations for engagements
├── ABBGovernanceEntities.xml       # Entity types, role types
├── ABCGovernanceSeedData.xml       # PartySettingTypes, preferences
├── ABDMatterSeedData.xml           # Matter-specific enumerations, permissions
├── ADAl10n.xml                     # Localization (after all seed data)
├── DAADemoUserPreferences.xml      # Demo data: user preferences
├── DABDemoOrganizations.xml        # Demo data: organizations
├── DAGDemoScenarios.xml            # Demo data: complex scenarios
└── ZAATestData.xml                 # Test-specific data
```

**Load Order Rules:**
- Files with prefix `AAA` load before `AAB`, which loads before `ABA`
- Enumerations (base types) should load before data that references them
- RoleTypes and PartyTypes should load before PartyRelationship data
- Demo data (`D*`) typically loads after all seed data (`A*`)

**3. Loading Order Considerations**
- EnumerationType records must be loaded before Enumeration records
- Reference data (lookup tables) should load before transactional entities
- Use 3-letter prefixes to enforce ordering within the same data type
- Use dependency ordering in component.xml when needed for cross-component dependencies

**4. When to Embed vs. Separate**
- **Embed**: Only for entity-specific default values that are tightly coupled to the entity structure
- **Separate**: For all enumerations, lookup data, configuration data, and any data shared across entities

**Migration Pattern for Existing Violations:**
1. Identify all `<seed-data>` blocks in entity files
2. Extract content to appropriate data files in `data/` directory  
3. Ensure proper `type="seed"` wrapper
4. Verify loading order dependencies
5. Register a migration in `MigrationRegistry.xml` if data transformation is needed for existing systems

**Best Practice**: Always use separate data files in the component's `data/` directory for all seed data rather than embedding it in entity definitions.

**Entity Operations in Tests - CRITICAL GUIDELINES:**

**NEVER use `ec.entity.makeValue(entityName, parameters)` with String entity names in tests:**
```groovy
// ❌ INCORRECT: Direct entity operations with string names
def cafValue = ec.entity.makeValue("mycompany.dte.Caf", [
    cafId: "TEST123",
    issuerPartyId: partyId
])
cafValue.create()
```

**ALWAYS use auto-service calls for entity operations in tests:**
```groovy
// ✅ CORRECT: Use auto-services for entity operations
def cafResult = ec.service.sync().name("create#mycompany.dte.Caf")
    .parameters([
        cafId: "TEST123", 
        issuerPartyId: partyId,
        fiscalTaxDocumentTypeEnumId: documentType
    ]).call()
```

**Why auto-services are mandatory in tests:**
- **Authentication & Authorization**: Proper handling of security contexts
- **Validation**: Business rule enforcement and data validation
- **Transaction Management**: Proper transaction boundaries and rollback handling
- **Error Handling**: Consistent error reporting and logging
- **Framework Compliance**: Full integration with Moqui's service layer
- **Type Safety**: Avoid runtime errors from string-based entity names

**Safe Array Access in Groovy Tests:**
```groovy
// ❌ Risky: Direct array indexing may cause getAt errors
result.listField[0].property

// ✅ Better: Use .get() method for type safety
result.listField.get(0).property

// ✅ Best: Check existence first
if (result.listField && result.listField.size() > 0) {
    result.listField.get(0).property
}
```

## ExecutionContext Quick Reference

The `ExecutionContext` (ec) is Moqui's central hub for accessing all framework capabilities. It is **request-scoped** — a new instance is created for each HTTP request, service call, or test execution.

### Facade Overview

| Facade | Purpose | Key Methods / Properties |
|--------|---------|--------------------------|
| `ec.user` | User info, authentication, permissions, filter context | `userId`, `username`, `userAccount?.partyId`, `nowTimestamp`, `hasPermission()`, `isUserInRole()`, `getPreference()`, `loginUser()`, `context` |
| `ec.entity` | Entity CRUD, queries, data loading | `find()`, `makeValue()`, `makeDataLoader()`, `tempSetSequencedIdPrimary()` |
| `ec.service` | Service invocation (sync, async, jobs) | `sync().name().parameters().call()`, `job().run()` |
| `ec.message` | Application messages and error handling | `addError()`, `addMessage()`, `hasError()`, `getErrorsString()`, `clearErrors()`, `clearAll()` |
| `ec.resource` | File and resource management | `getLocationReference()`, `expand()`, `xslFoTransform()` |
| `ec.web` | HTTP request/response access (web context only) | `sendJsonResponse()`, `sendResourceResponse()`, `sendError()`, `sessionAttributes`, `response`, `requestParameters` |
| `ec.logger` | Logging | `info()`, `warn()`, `error()`, `debug()`, `trace()` |
| `ec.l10n` | Localization and formatting | `format()`, `localize()` |
| `ec.transaction` | Transaction management | `begin()`, `commit()`, `rollback()` |
| `ec.artifactExecution` | Authorization control | `disableAuthz()`, `enableAuthz()`, `disableEntityDataFeed()` |
| `ec.factory` | ExecutionContextFactory (runtime introspection) | `getComponentBaseLocations()`, `getRuntimePath()`, `getConfPath()` |

### Context Scoping Rules

| Scope | Mechanism | Lifecycle | Use Case |
|-------|-----------|-----------|----------|
| Request | `ec.user.context` | Current HTTP request only | Filter context, passing data between services within one request |
| Session | `ec.web.sessionAttributes` | HTTP session (persists across requests) | User preferences, cached app state |
| Screen path | Screen context variables | Current request, screen-path dependent | App-specific settings set in always-actions |

**Key rule**: `ec.user.context` does **NOT** persist across requests. Filter context must be set up each request via a setup service. See [Context Persistence: Request vs Session Scope](#context-persistence-request-vs-session-scope) for details.

### Common Pitfalls

| Pitfall | Wrong | Correct |
|---------|-------|---------|
| Current user's party | `ec.user.partyId` | `ec.user.userAccount?.partyId` |
| Organization field | `ec.user.userAccount.organizationId` | Field doesn't exist — use party relationships |
| Timestamps | `new Date()` | `ec.user.nowTimestamp` |
| Resource exists check | `rr.exists()` (method) | `rr.exists` (property) |
| Entity creation in tests | `ec.entity.makeValue(name, params)` | `ec.service.sync().name("create#Entity")` |
| Filter without fail-safe | `filterMap="[id:filterIds]"` | `filterMap="[id: filterIds ?: ['-NO-MATCH-']]"` |

### Detailed Coverage

For in-depth documentation of each facade, see these sections:
- **User & Authentication**: [User and Party Identification](#user-and-party-identification), [Security and Authorization Patterns](#security-and-authorization-patterns)
- **Entity Access Control**: [Entity Access Control Patterns](#entity-access-control-patterns), [EntityFilter Setup Patterns](#entityfilter-setup-patterns)
- **Services**: [Entity Auto-Services](#entity-auto-services), [Service Definition Best Practices](#service-definition-best-practices)
- **Testing**: [Testing Framework Guidelines](#testing-framework-guidelines), [Service Error Handling in Tests](#service-error-handling-in-tests)
- **Factory API**: [ExecutionContext Factory API: Component Enumeration](#executioncontext-factory-api-component-enumeration)
- **Context Lifecycle**: [Context Persistence: Request vs Session Scope](#context-persistence-request-vs-session-scope)
- **Web Facade**: [File Download Transitions in Quasar Apps](#file-download-transitions-in-quasar-apps), [Screen Context and AJAX Transitions](#screen-context-and-ajax-transitions)

## Development Notes

### Component Dependencies
Components have dependency relationships managed through `component.xml` files. The build system automatically resolves and downloads dependencies.

### Component Version Management

The `version` attribute in `component.xml` uses semver. It is bumped at release time when migration registry files are relabeled from feature-scoped to semver-scoped versions.

**When to bump version:**
- Adding or changing entity definitions (new fields, new entities)
- Adding new seed data that existing systems need
- Any data transformation required for existing systems
- Adding features that change data handling behavior

**Semver convention:**
- **MAJOR** (X.0.0): Breaking changes to entities or service APIs
- **MINOR** (0.X.0): New features, new entities, new fields
- **PATCH** (0.0.X): Bug fixes, non-structural data updates

**Migration workflow (feature-scoped → release-scoped):**
1. During development: register migrations with ticket/feature ID as version identifier
2. Declare explicit cross-migration and cross-component dependencies
3. At release: relabel version to the release semver and bump `component.xml`
4. Migration IDs (primary keys) never change — only version is relabeled
5. See `guidelines/data-updates.md` for the generic development workflow
6. Check the organization's overlay for implementation-specific conventions

### Data Loading
The system uses a layered data approach:
- `seed` - Essential system data
- `seed-initial` - Initial setup data  
- `install` - Installation-specific data
- `demo` - Demo/test data

### Multi-tenancy
The system supports multi-tenant deployments with organization-specific data isolation.

### Code Style Guidelines
- Follow Moqui framework conventions
- Use existing Moqui utilities and patterns
- DO NOT ADD comments unless specifically requested
- Comprehensive audit trails for all actions when applicable
- Validate all user inputs and permissions
- Follow existing naming patterns in the codebase

## Configuration Definition Management

### Core Configuration Principles
- **Just-In-Time Creation**: Create configurations ONLY when implementing actual functionality
- **Implementation-First**: Never create configurations for planned/future features
- **Zero Orphans**: Every configuration must have active usage in code
- **Immediate Cleanup**: Remove configurations when functionality is removed

### Universal Scope
**Applies to ALL configuration definitions:**
- Security: UserPermission, UserGroup, UserGroupPermission
- Enumerations: Enumeration, EnumerationType definitions
- Settings: PartySettingType (database), System Properties (JVM/environment)
- Roles & Types: RoleType, PartyType, EntityType definitions
- Workflows: StatusType, StatusItem, StatusFlowTransition
- System Config: EmailTemplate, NotificationTopic, ServiceJob

**Configuration Type Distinction**:
- **System Properties**: JVM/environment variables via `org.moqui.util.SystemBinding.getPropOrEnv('property_name')` - for infrastructure settings (API endpoints, credentials, deployment flags)
- **PartySettingType**: Database entities via `ec.user.getPreference('SettingTypeId')` - for application settings (business rules, tenant preferences)
- **WARNING**: The entity `moqui.entity.SystemProperty` does NOT exist. Use appropriate method above.

### Configuration Lifecycle
1. **Planning Stage**: Document in `.agent-os/` files, NOT in data XML
2. **Implementation**: Create configuration AND usage simultaneously
3. **Verification**: Ensure active usage exists in services/entities/screens
4. **Cleanup**: Remove immediately when feature is removed

### Best Practices
- **Naming**: Use descriptive, purpose-oriented names (`USER_LOGIN_KEY_ADMIN`, `EMAIL_NOTIFICATION_FREQ`)
- **Granularity**: Start broad, split only when different contexts need different behavior
- **Documentation**: Clear, specific descriptions required for ALL configurations
- **Audit**: Regular quarterly reviews to remove unused configurations

**Reference**: See `.agent-os/configuration-definition-management-guidelines.md` for complete guidelines covering all configuration types.

### Date and Time Handling
- **Primary Date Type**: Use `java.sql.Timestamp` for all date/time operations in Moqui
- **Current Timestamp**: Always use `ec.user.nowTimestamp` to get the current timestamp

### Date/Time Format Standardization

**Problem**: Moqui Framework originally had hardcoded ISO date formats (`yyyy-MM-dd HH:mm`) and required explicit format attributes on every date/time field, leading to inconsistent formatting and maintenance burden.

**Solution**: Enhanced framework to support user preference-based default formats.

#### Framework Enhancement

**Modified Framework File**: `framework/src/main/groovy/org/moqui/impl/service/ParameterInfo.java`

**Enhancement Details**: Modified DATE, TIME, and TIMESTAMP parameter conversion to check user preferences when no explicit format is specified:

```java
case TIMESTAMP:
    String timestampFormat = format;
    // Use user preference DefaultDateTimeFormat if no explicit format is specified
    if (timestampFormat == null || timestampFormat.isEmpty()) {
        timestampFormat = eci.userFacade.getPreference("DefaultDateTimeFormat");
    }
    converted = eci.l10nFacade.parseTimestamp(valueStr, timestampFormat);
    // ... error handling
    break;
```

#### Moqui Preference System Architecture

**Preference Resolution Order** (from highest to lowest priority):
1. **Individual User Preference** - `moqui.security.UserPreference`
2. **User Group Preference** - `moqui.security.UserGroupPreference`
3. **System Property** - `default-property` in MoquiConf.xml
4. **Framework Default** - Hardcoded fallbacks

**Access Methods**:
- `ec.user.getPreference(key)` - Full preference hierarchy resolution (screens)
- `eci.userFacade.getPreference(key)` - Full preference hierarchy resolution (services)
- `eci.ecfi.getProperty(key)` - Only system properties (bypasses user preferences)

#### Configuration Strategy Decision Matrix

| Business Requirement | Implementation Approach | Configuration Method |
|----------------------|-------------------------|---------------------|
| **Organizational Standard** - All users must use same format | System Properties | `<default-property name="DefaultDateTimeFormat" value="dd/MM/yyyy HH:mm"/>` |
| **User Customization** - Users can choose their preferred format | User Group Preferences | `<user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateTimeFormat" preference-value="dd/MM/yyyy HH:mm"/>` |
| **Hybrid** - Organizational default with user override capability | Both | System property as fallback + User group preference as default |

#### Implementation Examples

**Service Parameters** - Remove explicit formats when using defaults:
```xml
<!-- Before: Explicit format required -->
<parameter name="startDate" type="Timestamp" format="dd/MM/yyyy HH:mm"/>

<!-- After: Framework uses user preference -->
<parameter name="startDate" type="Timestamp"/>
```

**Screen Fields** - Remove explicit formats when using defaults:
```xml
<!-- Before: Explicit format required -->
<date-time type="date-time" format="dd/MM/yyyy HH:mm"/>

<!-- After: Framework uses user preference -->
<date-time type="date-time"/>
```

**MoquiConf.xml Configuration**:
```xml
<!-- User Preferences Configuration for Default Date/Time Formats -->
<user-facade>
    <!-- Set system-wide default format preferences for all users -->
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultTimeFormat" preference-value="HH:mm"/>
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateFormat" preference-value="dd/MM/yyyy"/>
    <user-group-preference user-group-id="ALL_USERS" preference-key="DefaultDateTimeFormat" preference-value="dd/MM/yyyy HH:mm"/>
</user-facade>
```

#### Standard Preference Keys

- `DefaultTimeFormat` - Time-only fields (e.g., `HH:mm`)
- `DefaultDateFormat` - Date-only fields (e.g., `dd/MM/yyyy`)
- `DefaultDateTimeFormat` - Combined date/time fields (e.g., `dd/MM/yyyy HH:mm`)

#### When to Use Explicit Formats

**Keep explicit formats for**:
- **Log timestamps**: Need precise formats with milliseconds
- **API responses**: Specific ISO formats for interoperability
- **Import/Export**: External system compatibility
- **Special displays**: Formats that differ from data entry defaults

**Remove explicit formats for**:
- **Standard data entry forms**: Should use user preferences
- **General business displays**: Should use consistent defaults
- **Internal service parameters**: Framework handles conversion
- **Most screen fields**: Better user experience with preferences

#### Migration Best Practices

1. **Audit Current Usage**: Identify redundant explicit formats that match intended defaults
2. **Selective Removal**: Remove formats only where default behavior is desired
3. **Test Thoroughly**: Verify user preferences work correctly
4. **Document Changes**: Update component documentation
5. **User Training**: Provide guidance on personal format customization

#### Common Pitfall: `type="date"` Widget with `type="Timestamp"` Service Parameter

When a screen widget uses `date-time type="date"`, it sends a date-only value (e.g., `25/02/2026`). If the service parameter declares `type="Timestamp"`, the framework's `ParameterInfo.convertType` attempts to parse the value using the Timestamp format from user preferences (`dd/MM/yyyy HH:mm`), which fails because there is no time component.

**Error message**: `Value entered (25/02/2026) could not be converted to a Timestamp using format [dd/MM/yyyy HH:mm]`

**Root cause**: The `format` attribute on the **screen widget** only controls display. The parsing format is determined by the **service parameter's** `format` attribute, or falls back to the `DefaultDateTimeFormat` user preference.

**Fix**: Add `format="dd/MM/yyyy"` to the **service parameter** to tell the framework how to parse the date-only input:

```xml
<!-- Service parameter: explicit format matches the date-only widget input -->
<parameter name="fromDate" type="Timestamp" format="dd/MM/yyyy" required="true"/>
<parameter name="thruDate" type="Timestamp" format="dd/MM/yyyy"/>
```

Also remove any explicit `format` from the screen widget (unnecessary when using defaults):

```xml
<!-- Screen widget: no explicit format needed -->
<date-time type="date"/>
```

#### Inclusive thruDate Pattern (End-of-Day Adjustment)

When a service accepts a date-only `thruDate` (from a `date-time type="date"` widget), the value arrives as midnight (`00:00:00.000`) of the specified day. This means the date is effectively **exclusive** -- records are already expired at the start of that day.

Business users expect `thruDate` to be **inclusive**: specifying Feb 27 means "valid through the end of Feb 27."

**Pattern**: Adjust `thruDate` to end-of-day (`23:59:59.999`) in the service actions:

```xml
<if condition="thruDate">
    <set field="thruDate" from="new java.sql.Timestamp(thruDate.time + (24L * 60 * 60 * 1000) - 1)"/>
</if>
```

**When to apply**: Any service that accepts a date-only `thruDate` from a screen widget with `type="date"`, especially for date ranges where the end date should be inclusive (memberships, assignments, roles, subrogations).

**Reference Documentation**: See `.agent-os/date-time-format-standardization.md` for complete technical details and implementation guide.
- **Never use**: `new Date()`, `System.currentTimeMillis()`, or other direct date constructors
- **Rationale**: `ec.user.nowTimestamp` provides consistency, proper timezone handling, and allows for time manipulation in tests

## ElasticSearch/OpenSearch

The system can use either ElasticSearch or OpenSearch for full-text search and analytics:
- Download: `./gradlew downloadElasticSearch` or `./gradlew downloadOpenSearch`
- Start/Stop: `./gradlew startElasticSearch` / `./gradlew stopElasticSearch`
- Data location: `runtime/elasticsearch/` or `runtime/opensearch/`

## Legacy Deployment

### War File Deployment
```bash
# Create deployable war (for non-containerized deployments)
./gradlew build

# Create war with embedded runtime
./gradlew addRuntime
```

**Note**: For modern deployment practices (Kubernetes, CI/CD pipelines, Docker), see `development-guide.md`

## Documentation Resources

### Official Moqui Documentation
- Framework documentation: https://moqui.org/m/docs/framework
- IDE Setup guides: https://moqui.org/m/docs/framework/IDE+Setup


**Note**: For IDE configuration, project-specific documentation, and development workflows, see `development-guide.md`

## Issue Tracking

### Jira Integration
- **Instance URL**: https://{jira-instance}/
- **Project**: "{Project Development Team}" ({KEY})
- **Ticket References**: The prefix `{KEY}-` followed by a number (e.g., {KEY}-2589) refers to a Jira ticket in the project
- **Ticket URL Format**: https://{jira-instance}/browse/{KEY}-{number}
- Use these ticket references in commit messages and documentation for traceability
- Example: "Implement user authentication ({KEY}-2589)"

## Database Schema Management

### Entity-First Approach

**CRITICAL: Never use direct database commands for schema changes. Always use Moqui entity definitions.**

**PRIORITY: Always attempt to use existing standard entities before modifying or creating new entities.**

When implementing features that require data storage:

1. **Use existing entities first** - Leverage standard Moqui/Mantle entities whenever possible
2. **Evaluate configuration mechanisms** - Use PartySetting, PartyContent, PartyRole, or other configuration entities
   - **PartySetting**: Requires PartySettingType definition with partySettingTypeId, then use PartySetting with that type ID
3. **Consider view-entities** - Create view-entities to combine existing data without schema changes  
4. **Entity extension as last resort** - Only modify or extend entities when existing mechanisms are insufficient or too cumbersome

### Automatic Field Management

**CRITICAL: The `lastUpdatedStamp` field is automatically managed by Moqui Framework and should never be explicitly added to any entity definition.**

- **Framework Behavior**: Moqui automatically adds `lastUpdatedStamp` fields to all entities
- **Never Define Explicitly**: Do not include `lastUpdatedStamp` in entity field definitions
- **Automatic Population**: Framework automatically populates these fields during create/update operations
- **Query Usage**: You can still query and use these fields in conditions and results, they just shouldn't be defined in the entity XML

**❌ INCORRECT: Defining automatic fields explicitly**
```xml
<entity entity-name="CustomEntity" package="component.custom">
  <field name="customId" type="id" is-pk="true"/>
  <field name="partyId" type="id"/>
  <field name="customField" type="text-medium"/>
  <field name="lastUpdatedStamp" type="date-time"/>  <!-- NEVER DO THIS -->
</entity>
```

**✅ CORRECT: Let framework manage automatic fields**
```xml
<entity entity-name="CustomEntity" package="component.custom">
  <field name="customId" type="id" is-pk="true"/>
  <field name="partyId" type="id"/>
  <field name="customField" type="text-medium"/>
  <!-- lastUpdatedStamp is automatically added by framework -->
</entity>
```

**Other Automatic Fields Never to Define:**
- `lastUpdatedStamp` - Last update timestamp

### LocalizedEntityField Usage Patterns

**CRITICAL: LocalizedEntityField is for translating data values stored in the database, NOT for field name translations.**

LocalizedEntityField provides a mechanism to store translations of actual data values (content) that exist in entity fields. It should not be confused with field name or UI label translations, which belong in the presentation layer.

#### Correct Usage - Data Value Translation

**✅ CORRECT: Translating enumeration description values**
```xml
<!-- Entity definition with localized content -->
<entity entity-name="CustomStatusType" package="component.custom">
  <field name="statusTypeId" type="id" is-pk="true"/>
  <field name="description" type="text-medium"/>
  <field name="parentTypeId" type="id"/>
</entity>

<!-- LocalizedEntityField for translating the description data -->
<moqui.basic.LocalizedEntityField entity="CustomStatusType" 
                                  fieldName="description" 
                                  pkValue="CUSTOM_PENDING"
                                  locale="es"
                                  localized="Pendiente"/>
<moqui.basic.LocalizedEntityField entity="CustomStatusType" 
                                  fieldName="description" 
                                  pkValue="CUSTOM_PENDING"
                                  locale="en"
                                  localized="Pending"/>
```

**Real-world example - Enumeration translations:**
```xml
<!-- Standard Moqui Enumeration with localized descriptions -->
<moqui.basic.Enumeration enumId="AmsgType_Info" 
                        description="Info" 
                        enumTypeId="AutoMessageType"/>

<!-- Spanish translation for the description value -->
<moqui.basic.LocalizedEntityField entity="Enumeration" 
                                  fieldName="description" 
                                  pkValue="AmsgType_Info"
                                  locale="es"
                                  localized="Información"/>
```

#### Incorrect Usage - Field Name Translation

**❌ INCORRECT: Using LocalizedEntityField for field names/labels**
```xml
<!-- DON'T DO THIS - Field names belong in UI layer -->
<moqui.basic.LocalizedEntityField entity="CustomEntity" 
                                  fieldName="customerName"  <!-- WRONG: This is field metadata, not data content -->
                                  pkValue="field_label"
                                  locale="es"
                                  localized="Nombre del Cliente"/>
```

**✅ CORRECT: Handle field name translations in screen definitions**
```xml
<!-- Screen form with localized field labels -->
<form-single name="CustomForm" transition="updateCustom">
    <field name="customerName">
        <default-field title="${customerNameLabel}">  <!-- Use localized message -->
            <text-line/>
        </default-field>
    </field>
</form-single>

<!-- Properties file (MyMessages.properties) -->
<!-- customerNameLabel=Customer Name -->

<!-- Spanish properties file (MyMessages_es.properties) -->
<!-- customerNameLabel=Nombre del Cliente -->
```

#### Usage Guidelines

**Use LocalizedEntityField for:**
- Translating enumeration description values that are displayed to users
- Localizing content stored in entity fields (product descriptions, status messages, etc.)
- Multi-language support for data that changes based on user locale
- Content that is stored in the database and needs translation

**Do NOT use LocalizedEntityField for:**
- Field names, labels, or UI element descriptions
- Form field titles and placeholders
- Screen titles and static UI text
- Validation error messages
- Menu items and navigation labels

**Alternative approaches for UI translations:**
- Use message properties files for static UI text
- Implement form field `title` attributes with message references
- Use screen transition parameters for dynamic labels
- Leverage Moqui's built-in localization mechanisms for UI components

#### Best Practices

1. **Identify the Translation Scope**: Determine if you're translating stored data content or UI presentation elements
2. **Use Consistent Locale Codes**: Follow standard locale codes (en, es, fr, etc.)
3. **Maintain Translation Completeness**: Ensure all supported locales have translations for critical content
4. **Test with Multiple Locales**: Verify translations work correctly in different language contexts
5. **Document Translation Requirements**: Clearly specify which fields require localization support

When entity changes are truly necessary:

1. **Define entities in XML** using Moqui entity definitions
2. **Let Moqui manage schema** automatically through the framework
3. **Use data loading** mechanisms for data migration and seeding
4. **Never write direct SQL** for schema modifications (CREATE TABLE, ALTER TABLE, etc.)

**Example of using existing entities (preferred):**
```xml
<!-- PartySettingType: For APPLICATION configuration (business rules, tenant preferences) -->
<!-- First create PartySettingType -->
<mantle.party.PartySettingType partySettingTypeId="BuyerUnit"
                               description="Buyer Unit Configuration"
                               defaultValue="N"/>

<!-- Then use PartySetting with that type for tenant-specific override -->
<mantle.party.PartySetting partyId="ORG_PROCUREMENT"
                         partySettingTypeId="BuyerUnit"
                         settingValue="Y"/>

<!-- Access in services with tenant-aware lookup -->
<set field="buyerUnit" from="ec.user.getPreference('BuyerUnit')"/>
```

**For INFRASTRUCTURE configuration (API endpoints, credentials), use System Properties:**
```bash
# Set via environment variable or JVM argument
export buyer_api_endpoint=https://api.example.com
# Or: java -Dbuyer_api_endpoint=https://api.example.com ...
```

```xml
<!-- Access in services -->
<set field="apiEndpoint" from="org.moqui.util.SystemBinding.getPropOrEnv('buyer_api_endpoint')"/>
```

**Decision: PartySettingType vs System Properties**:
- **PartySettingType**: Business rules, tenant/user preferences, configurable via UI
- **System Properties**: Infrastructure settings, environment-specific, deployment-time configuration

### StatusItem vs Enumeration Usage

**CRITICAL DISTINCTION: Understanding when to use StatusItem vs Enumeration is essential for proper Moqui development.**

#### StatusItem Entity Usage
- **Use for**: Status values that represent states in a workflow or lifecycle
- **Features**: Supports StatusFlowTransition for validation and automated transitions
- **Examples**: WorkEffort status (New, InProgress, Completed), Party status, Order status, Invoice status
- **Storage**: Values stored in `moqui.basic.StatusItem` entity
- **Workflow Support**: Enables business process automation and validation

**Key characteristics of status values:**
- Can transition from one to another (New → InProgress → Completed)
- Have defined workflow sequences
- Support validation rules for allowed transitions
- Often drive business logic and automation

```xml
<!-- CORRECT: Status field using StatusItem -->
<field name="statusId" type="id"/>
<relationship type="one" related="moqui.basic.StatusItem" title="WorkEffortStatus"/>

<!-- StatusItem data would be: -->
<!-- statusId="WeStInProgress" statusTypeId="WorkEffortStatus" description="In Progress" -->
```

#### Enumeration Entity Usage
- **Use for**: Static classification values that don't follow workflow transitions
- **Features**: Simple lookup values for categorization and type classification
- **Examples**: Types, categories, purposes, classifications, fixed option lists
- **Storage**: Values stored in `moqui.basic.Enumeration` entity
- **No Transitions**: Values don't change state or follow workflows

**Key characteristics of enumeration values:**
- Static classification or categorization
- Don't transition between states
- Used for filtering, grouping, and categorization
- Provide consistent option lists

```xml
<!-- CORRECT: Type/Category field using Enumeration -->
<field name="workEffortTypeEnumId" type="id"/>
<relationship type="one" related="moqui.basic.Enumeration" title="WorkEffortType"/>

<!-- Enumeration data would be: -->
<!-- enumId="WetTask" enumTypeId="WorkEffortType" description="Task" -->
```

### Relationship Title Validation

**CRITICAL**: The relationship `title` attribute serves as both validation constraint and data filtering:

#### For StatusItem Relationships
- **Title must equal the `statusTypeId`** of StatusItem records
- Moqui uses this to:
  - Filter available status options in forms
  - Validate StatusFlowTransition rules
  - Ensure data integrity

```xml
<relationship type="one" related="moqui.basic.StatusItem" title="WorkEffortStatus"/>
<!-- Only StatusItem records with statusTypeId="WorkEffortStatus" are valid -->
```

#### For Enumeration Relationships  
- **Title must equal the `enumTypeId`** of Enumeration records
- Moqui uses this to:
  - Filter dropdown options in forms
  - Restrict valid enumeration values
  - Validate data entry

```xml
<relationship type="one" related="moqui.basic.Enumeration" title="WorkEffortType"/>
<!-- Only Enumeration records with enumTypeId="WorkEffortType" are valid -->
```

### Common Mistakes to Avoid

**❌ INCORRECT: Using Enumeration for status values**
```xml
<!-- DON'T DO THIS: Status should use StatusItem, not Enumeration -->
<field name="orderStatusEnumId" type="id"/>
<relationship type="one" related="moqui.basic.Enumeration" title="OrderStatus"/>
```

**❌ INCORRECT: Wrong relationship title**
```xml
<!-- DON'T DO THIS: Title doesn't match statusTypeId/enumTypeId -->
<field name="statusId" type="id"/>
<relationship type="one" related="moqui.basic.StatusItem" title="WrongTitle"/>
```

**✅ CORRECT: Status using StatusItem with proper title**
```xml
<field name="statusId" type="id"/>
<relationship type="one" related="moqui.basic.StatusItem" title="OrderStatus"/>
<!-- StatusItem records have statusTypeId="OrderStatus" -->
```

**✅ CORRECT: Type using Enumeration with proper title**
```xml
<field name="orderTypeEnumId" type="id"/>
<relationship type="one" related="moqui.basic.Enumeration" title="OrderType"/>
<!-- Enumeration records have enumTypeId="OrderType" -->
```

**Example of entity definition (when absolutely necessary):**
```xml
<entity entity-name="CustomEntity" package="component.custom">
  <field name="customId" type="id" is-pk="true"/>
  <field name="partyId" type="id"/>
  <field name="statusId" type="id"/>
  <field name="typeEnumId" type="id"/>
  <field name="customField" type="text-medium"/>
  
  <relationship type="one" related-entity-name="mantle.party.Party"/>
  <relationship type="one" related="moqui.basic.StatusItem" title="CustomStatus"/>
  <relationship type="one" related="moqui.basic.Enumeration" title="CustomType"/>
</entity>
```

**Moqui automatically handles:**
- Database table creation and schema updates
- Index creation for primary keys and relationships
- Constraint management and validation
- Cross-database compatibility (H2, PostgreSQL, MySQL, etc.)
- Migration between schema versions

**Data Migration Approach:**
- Use XML data files in component `data/` directories
- Leverage Moqui's data loading mechanisms (`./gradlew load`)
- Use seed data for essential system updates
- Use install data for deployment-specific changes
- **IMPORTANT: When adding seed data that existing systems need, register a migration (see `guidelines/data-updates.md`)**

### Entity Audit Logging Best Practices

**CRITICAL: Use Moqui's built-in audit logging instead of creating custom audit entities.**

Moqui Framework provides comprehensive audit logging through the `EntityAuditLog` entity. This built-in functionality automatically tracks changes to entity fields without requiring custom audit tables or complex tracking logic.

#### Enabling Audit Logging

Enable audit logging on specific entity fields using the `enable-audit-log="true"` attribute:

**✅ CORRECT: Using enable-audit-log attribute**
```xml
<entity entity-name="ApiKey" package="mycompany.myapp.api">
    <field name="apiKeyId" type="id" is-pk="true"/>
    <field name="partyId" type="id"/>
    <field name="keyValue" type="text-medium" enable-audit-log="true"/>
    <field name="statusId" type="id" enable-audit-log="true"/>
    <field name="description" type="text-medium"/>
    <field name="fromDate" type="date-time"/>
    <field name="thruDate" type="date-time" enable-audit-log="true"/>
    
    <relationship type="one" related-entity-name="mantle.party.Party"/>
    <relationship type="one" title="Status" related-entity-name="moqui.basic.StatusItem"/>
</entity>
```

**❌ INCORRECT: Creating custom audit entities**
```xml
<!-- DON'T DO THIS - Moqui provides built-in audit logging -->
<entity entity-name="ApiKeyHistory" package="mycompany.myapp.api">
    <field name="historyId" type="id" is-pk="true"/>
    <field name="apiKeyId" type="id"/>
    <field name="oldValue" type="text-medium"/>
    <field name="newValue" type="text-medium"/>
    <field name="changeDate" type="date-time"/>
    <field name="changedByUserId" type="id"/>
</entity>
```

#### Why Use Built-in Audit Logging

1. **Automatic Change Tracking**: Moqui automatically captures field changes without custom service calls
2. **Complete Context**: Includes user ID, timestamp, old and new values, and entity information
3. **Framework Integration**: Seamlessly integrates with Moqui's transaction and security systems
4. **No Custom Code**: Eliminates the need for custom audit services and entities
5. **Standard Query Interface**: Use standard entity operations to access audit data

#### Accessing Audit Log Data

Query audit logs using the built-in `moqui.entity.EntityAuditLog` entity:

**Basic Query Pattern:**
```xml
<!-- Find audit logs for specific entity -->
<entity-find entity-name="moqui.entity.EntityAuditLog" list="auditLogList">
    <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
    <econdition field-name="changedFieldName" value="isActive"/>
    <econdition field-name="pkPrimaryValue" from="loginKey"/>
    <order-by field-name="changedDate"/>
</entity-find>
```

**Service Implementation Example:**
```xml
<service verb="get" noun="ApiKeyAuditTrail">
    <in-parameters>
        <parameter name="userLoginKeyId" required="true"/>
        <parameter name="fromDate" type="Timestamp"/>
        <parameter name="thruDate" type="Timestamp"/>
    </in-parameters>
    <out-parameters>
        <parameter name="auditTrail" type="List"/>
    </out-parameters>
    <actions>
        <entity-find entity-name="moqui.entity.EntityAuditLog" list="auditTrail">
            <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
            <econdition field-name="pkPrimaryValue" from="userLoginKeyId"/>
            <econdition field-name="changedDate" operator="greater-equals" from="fromDate" ignore-if-empty="true"/>
            <econdition field-name="changedDate" operator="less-equals" from="thruDate" ignore-if-empty="true"/>
            <order-by field-name="changedDate"/>
        </entity-find>
    </actions>
</service>
```

**Groovy Service Implementation:**
```groovy
// Get audit trail for specific entity record
def getApiKeyAuditTrail(String userLoginKeyId, Timestamp fromDate = null, Timestamp thruDate = null) {
    def auditList = ec.entity.find("moqui.entity.EntityAuditLog")
        .condition("changedEntityName", "mycompany.myapp.api.ApiKeyMetadata")
        .condition("pkPrimaryValue", userLoginKeyId)
    
    if (fromDate) auditList.condition("changedDate", EntityCondition.GREATER_THAN_EQUAL_TO, fromDate)
    if (thruDate) auditList.condition("changedDate", EntityCondition.LESS_THAN_EQUAL_TO, thruDate)
    
    return auditList.orderBy("changedDate").list()
}
```

#### Audit Log Data Structure

The `EntityAuditLog` entity captures:
- **changedEntityName**: Full entity name (e.g., "mycompany.myapp.api.ApiKey")
- **changedFieldName**: Field that was changed
- **pkPrimaryValue**: Primary key value of the changed record
- **oldValueText**: Previous field value
- **newValueText**: New field value
- **changedDate**: When the change occurred
- **changedByUserId**: User who made the change
- **changedInVisitId**: Visit session information

#### Selective Field Auditing

Only enable audit logging for fields that require change tracking:

```xml
<entity entity-name="Contract" package="mycompany.myapp.contract">
    <field name="contractId" type="id" is-pk="true"/>
    <field name="contractNumber" type="text-short"/>
    
    <!-- Critical fields that need audit tracking -->
    <field name="statusId" type="id" enable-audit-log="true"/>
    <field name="contractAmount" type="currency-amount" enable-audit-log="true"/>
    <field name="approvalDate" type="date" enable-audit-log="true"/>
    
    <!-- Non-critical fields without audit -->
    <field name="description" type="text-long"/>
    <field name="internalNotes" type="text-long"/>
</entity>
```

#### Migrating from Custom Audit Entities

**Common Anti-Pattern - Custom Audit Entity:**
```xml
<!-- ❌ AVOID: Custom audit entity like ApiKeyHistory -->
<entity entity-name="ApiKeyHistory" package="mycompany.myapp.api">
    <field name="apiKeyHistoryId" type="id" is-pk="true"/>
    <field name="userLoginKeyId" type="text-long"/>
    <field name="eventType" type="text-short"/>
    <field name="eventDate" type="date-time"/>
    <field name="eventUserId" type="id"/>
    <field name="eventDescription" type="text-long"/>
    <!-- Additional tracking fields... -->
</entity>
```

### Historical Value Tracking with Temporal Primary Keys

**CRITICAL: When maintaining historical values in entities, use `fromDate` as part of the primary key to enable temporal versioning.**

This pattern allows storing multiple values over time while maintaining a complete history of changes. It's particularly useful for configuration entities, rate tables, and any data that needs historical tracking beyond simple audit logs.

#### Design Pattern for Historical Values

**✅ CORRECT: Using fromDate in primary key for historical tracking**
```xml
<entity entity-name="ConfigurationDetail" package="component.config">
    <field name="configurationId" type="id" is-pk="true"/>
    <field name="configurationTypeEnumId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>  <!-- Temporal PK component -->
    <field name="thruDate" type="date-time"/>
    <field name="configurationValue" type="text-medium"/>

    <relationship type="one" related="ConfigurationHeader">
        <key-map field-name="configurationId"/>
    </relationship>
</entity>
```

**❌ INCORRECT: Without fromDate in primary key**
```xml
<entity entity-name="ConfigurationDetail" package="component.config">
    <field name="configurationId" type="id" is-pk="true"/>
    <field name="configurationTypeEnumId" type="id"/>  <!-- Should be PK -->
    <field name="fromDate" type="date-time"/>           <!-- Should be PK -->
    <field name="thruDate" type="date-time"/>
    <field name="configurationValue" type="text-medium"/>
</entity>
```

#### Query Pattern for Historical Values

**IMPORTANT: When querying historical values, always order by `fromDate` DESC and take the first result to get the current value.**

**Service Implementation:**
```xml
<service verb="get" noun="CurrentConfigurationValue">
    <in-parameters>
        <parameter name="configurationId" required="true"/>
        <parameter name="configurationTypeEnumId" required="true"/>
        <parameter name="effectiveDate" type="Timestamp" default="ec.user.nowTimestamp"/>
    </in-parameters>
    <out-parameters>
        <parameter name="configurationValue"/>
    </out-parameters>
    <actions>
        <!-- Find current value by ordering by fromDate DESC -->
        <entity-find entity-name="ConfigurationDetail" list="configList" limit="1">
            <econdition field-name="configurationId"/>
            <econdition field-name="configurationTypeEnumId"/>
            <econdition field-name="fromDate" operator="less-equals" from="effectiveDate"/>
            <econdition field-name="thruDate" operator="greater" from="effectiveDate" or-null="true"/>
            <order-by field-name="fromDate" desc="true"/>  <!-- Most recent first -->
        </entity-find>
        <set field="configurationValue" from="configList?.first?.configurationValue"/>
    </actions>
</service>
```

**Groovy Implementation:**
```groovy
def getCurrentValue(String configurationId, String configurationTypeEnumId, Timestamp effectiveDate = null) {
    if (!effectiveDate) effectiveDate = ec.user.nowTimestamp

    def result = ec.entity.find("ConfigurationDetail")
        .condition("configurationId", configurationId)
        .condition("configurationTypeEnumId", configurationTypeEnumId)
        .condition("fromDate", EntityCondition.LESS_THAN_EQUAL_TO, effectiveDate)
        .conditionDate("fromDate", "thruDate", effectiveDate)  // Active on date
        .orderBy("-fromDate")  // Most recent first
        .limit(1)
        .one()

    return result?.configurationValue
}
```

#### Handling Overlapping Date Ranges

**Note:** This pattern may result in overlapping date ranges if `thruDate` is not properly managed. The query pattern handles this by:
1. Ordering by `fromDate` DESC to get the most recent record first
2. Using `limit="1"` to take only the first (most recent) result
3. Preferring the newest record when overlaps exist

#### Common Use Cases

**1. Configuration Values:**
```xml
<entity entity-name="GovernanceEntityEngagementConfigurationDetail">
    <field name="engagementConfigurationId" type="id" is-pk="true"/>
    <field name="configurationEnumId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>
    <field name="configurationValue" type="text-medium"/>
    <field name="thruDate" type="date-time"/>
</entity>
```

**2. Price/Rate Tables:**
```xml
<entity entity-name="ProductPrice">
    <field name="productId" type="id" is-pk="true"/>
    <field name="priceTypeEnumId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>
    <field name="price" type="currency-amount"/>
    <field name="thruDate" type="date-time"/>
</entity>
```

**3. Organization Settings:**
```xml
<entity entity-name="OrganizationSetting">
    <field name="organizationPartyId" type="id" is-pk="true"/>
    <field name="settingTypeId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>
    <field name="settingValue" type="text-medium"/>
    <field name="thruDate" type="date-time"/>
</entity>
```

#### Best Practices

1. **Always include fromDate in PK** for entities that need historical tracking
2. **Set fromDate to ec.user.nowTimestamp** when creating new records
3. **Query with ORDER BY fromDate DESC** and LIMIT 1 for current values
4. **Use date-filter or conditionDate** for point-in-time queries
5. **Consider thruDate management** - either set it when creating new versions or handle overlaps in queries
6. **Document the temporal nature** in entity descriptions for clarity

#### Migration from Non-Historical Entities

When converting existing entities to support historical tracking:

1. **Add fromDate to primary key** - Requires data migration
2. **Set initial fromDate** for existing records (e.g., system start date)
3. **Update all queries** to handle temporal aspect
4. **Update services** to create new records instead of updating
5. **Test thoroughly** with overlapping date scenarios

**✅ CORRECT: Use enable-audit-log on source entity:**
```xml
<entity entity-name="ApiKeyMetadata" package="mycompany.myapp.api">
    <field name="userLoginKeyId" type="text-long" is-pk="true"/>
    <field name="externalSystemName" type="text-medium"/>
    <field name="description" type="text-long"/>
    
    <!-- Enable audit on fields that need change tracking -->
    <field name="isActive" type="text-indicator" default="Y" enable-audit-log="true"/>
    <field name="lastUsedDate" type="date-time" enable-audit-log="true"/>
    <field name="usageCount" type="number-integer" enable-audit-log="true"/>
    <field name="rateLimitPerMinute" type="number-integer" enable-audit-log="true"/>
</entity>
```

**Migration Benefits:**
1. **Eliminate custom audit services** - no manual history record creation
2. **Automatic context capture** - user, timestamp, visit info included
3. **Reduced code complexity** - no custom audit logic needed
4. **Framework integration** - works with all Moqui security and transaction features
5. **Standard query interface** - use normal entity operations

#### Advanced Query Patterns

**Find all changes to a specific field:**
```xml
<entity-find entity-name="moqui.entity.EntityAuditLog" list="statusChanges">
    <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
    <econdition field-name="changedFieldName" value="isActive"/>
    <econdition field-name="newValueText" value="N"/> <!-- Find revocations -->
    <order-by field-name="changedDate"/>
</entity-find>
```

**Find changes by user:**
```xml
<entity-find entity-name="moqui.entity.EntityAuditLog" list="userChanges">
    <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
    <econdition field-name="changedByUserId" from="userId"/>
    <date-filter from-field-name="changedDate" thru-field-name="changedDate" from="fromDate" thru="thruDate"/>
    <order-by field-name="changedDate"/>
</entity-find>
```

**Create audit summary report:**
```xml
<service verb="get" noun="ApiKeyAuditSummary">
    <in-parameters>
        <parameter name="fromDate" type="Timestamp"/>
        <parameter name="thruDate" type="Timestamp"/>
    </in-parameters>
    <out-parameters>
        <parameter name="summaryList" type="List"/>
    </out-parameters>
    <actions>
        <entity-find entity-name="moqui.entity.EntityAuditLog" list="auditList">
            <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
            <econdition field-name="changedDate" operator="greater-equals" from="fromDate" ignore-if-empty="true"/>
            <econdition field-name="changedDate" operator="less-equals" from="thruDate" ignore-if-empty="true"/>
            <select-field field-name="changedFieldName"/>
            <select-field field-name="changedByUserId"/>
        </entity-find>
        
        <!-- Group and summarize audit data -->
        <script><![CDATA[
            summaryList = []
            def groupedChanges = auditList.groupBy { it.changedFieldName }
            groupedChanges.each { fieldName, changes ->
                summaryList.add([
                    fieldName: fieldName,
                    changeCount: changes.size(),
                    uniqueUsers: changes.collect { it.changedByUserId }.unique().size()
                ])
            }
        ]]></script>
    </actions>
</service>
```

#### Performance Considerations

- **Selective Auditing**: Only enable on fields that truly need change tracking
- **Storage Impact**: Each field change creates an audit log record
- **Query Performance**: Use appropriate indexes when querying audit logs frequently
- **Retention Policy**: Consider implementing audit log cleanup for older records
- **Field Granularity**: Audit individual fields rather than entire entities when possible

#### Audit Log View Entity Pattern

**Create view entities for enhanced audit reporting:**
```xml
<view-entity entity-name="ApiKeyAuditView" package="mycompany.myapp.api">
    <description>Enhanced audit view combining audit logs with user details</description>
    
    <member-entity entity-alias="AUD" entity-name="moqui.entity.EntityAuditLog"/>
    <member-entity entity-alias="UA" entity-name="moqui.security.UserAccount" join-from-alias="AUD" join-optional="true">
        <key-map field-name="changedByUserId" related="userId"/>
    </member-entity>
    <member-entity entity-alias="AKM" entity-name="mycompany.myapp.api.ApiKeyMetadata" join-from-alias="AUD" join-optional="true">
        <key-map field-name="pkPrimaryValue" related="userLoginKeyId"/>
    </member-entity>
    
    <alias-all entity-alias="AUD"/>
    <alias entity-alias="UA" name="changedByUsername" field="username"/>
    <alias entity-alias="UA" name="changedByEmail" field="emailAddress"/>
    <alias entity-alias="AKM" name="externalSystemName"/>
    <alias entity-alias="AKM" name="isActive"/>
    
    <entity-condition>
        <econdition field-name="changedEntityName" value="mycompany.myapp.api.ApiKeyMetadata"/>
    </entity-condition>
</view-entity>
```

#### Best Practices Summary

1. **Use `enable-audit-log="true"`** on entity fields requiring change tracking
2. **Never create custom audit entities** - use Moqui's built-in `EntityAuditLog`
3. **Query audit logs** using standard entity operations and view entities
4. **Enable selectively** only on fields that need tracking
5. **Leverage automatic context** - user ID, timestamp, and visit information are captured automatically
6. **Create view entities** for enhanced audit reporting and user-friendly queries
7. **Implement service patterns** for common audit queries and reporting needs
8. **Consider performance impact** and implement appropriate indexing and retention policies

## User and Party Identification

**CRITICAL: Do not confuse userId, username, and partyId - they serve different purposes**

### Core Concepts

1. **userId** (UserAccount.userId):
   - Internal entity identifier for a UserAccount
   - Example: "EX_JOHN_DOE", "ADMIN_USER", "SYSTEM"
   - Used in entity relationships and internal references
   - NOT used for login

2. **username** (UserAccount.username):
   - Login credential identifier
   - Example: "john.doe", "admin", "system"
   - Used with loginUser() method: `ec.user.loginUser(username, password)`
   - Displayed in UI and logs

3. **partyId** (Party.partyId):
   - Identifier for a Party (person or organization)
   - Example: "ORG_ACME", "PERSON_12345", "Rut12345678"
   - A user may be associated with a party via UserAccount.partyId
   - Not all parties have user accounts, not all users have parties

### Common Patterns

**Login Operations:**
```groovy
// CORRECT: Use username for login
ec.user.loginUser("john.doe", "password")

// INCORRECT: Do not use userId for login
ec.user.loginUser("EX_JOHN_DOE", "password") // WRONG!
```

**Entity Relationships:**
```xml
<!-- UserAccount definition -->
<UserAccount userId="EX_JOHN_DOE" username="john.doe" partyId="PERSON_12345"/>

<!-- References use userId -->
<relationship type="one" related="moqui.security.UserAccount">
    <key-map field-name="createdByUserId" related="userId"/>
</relationship>
```

**Test Data Setup:**
```xml
<!-- Define user account with clear distinction -->
<moqui.security.UserAccount 
    userId="TEST_USER_001"        <!-- Internal ID -->
    username="test.user"           <!-- Login name -->
    partyId="TEST_PARTY_001"       <!-- Associated party -->
    userFullName="Test User"/>
```

### Testing Best Practices

1. Name variables clearly:
   - Use `testUsername` for login credentials
   - Use `testUserId` for entity references
   - Use `testPartyId` for party associations

2. Document the distinction in test setup
3. Use meaningful prefixes in test data (e.g., "EX_" for example users)

### Resolving User's Organization

**CRITICAL: `moqui.security.UserAccount` does NOT have an `organizationId` field.** The Mantle UDM extension adds only `partyId` (linking to the person). Accessing `ec.user.userAccount.organizationId` throws an `EntityException`.

**Correct pattern**: Resolve the user's organization via `PartyRelationship`:

```xml
<!-- Find the organization the user belongs to -->
<entity-find entity-name="mantle.party.PartyRelationship" list="userOrgRelList" limit="1">
    <date-filter/>
    <econdition field-name="relationshipTypeEnumId" operator="in" value="PrtAgent,PrtEmployee,PrtMember"/>
    <econdition field-name="toRoleTypeId" value="OrgInternal"/>
    <econdition field-name="fromPartyId" from="ec.user.userAccount.partyId"/>
</entity-find>
<set field="organizationPartyId" from="userOrgRelList ? userOrgRelList[0].toPartyId : null"/>
```

For the full multi-tenant organization resolution pattern (including active organization preferences and child organization expansion), see `{shared-utils}.tenant.UserOrgInfoServices.get#UserOrganizationInfo`.

#### Role-Filtered Organization Lists (`agentRoleTypeId`)

When an application needs `filterOrgIds` restricted to organizations where the user holds a **specific role** (e.g., only Employee orgs or only Manager orgs), pass the `agentRoleTypeId` parameter (List) to `{shared-utils}.tenant.UserOrgInfoServices.setup#UserOrganizationInfo`:

```xml
<!-- Root screen always-actions: only include orgs where user is Employee -->
<service-call name="MyPermissionServices.setup#UserOrganizationInfo" out-map="context"
              in-map="[agentRoleTypeId:['Employee']]"/>

<!-- Root screen always-actions: only include orgs where user is Manager -->
<service-call name="MyPermissionServices.setup#UserOrganizationInfo" out-map="context"
              in-map="[agentRoleTypeId:['Manager']]"/>
```

**How it works**: The service filters `PartyRelationship.fromRoleTypeId` when building the user's organization list. For Manager roles, it also expands child organizations via `expand#ChildOrganizationList`. Without `agentRoleTypeId`, all relationship types are included (the default behavior).

**When to use**: Multi-app deployments where the same user has different roles in different organizations, and each app should only see organizations relevant to its role context.

**Common anti-pattern** (causes EntityException):
```groovy
// WRONG: organizationId does not exist on UserAccount
ec.user.userAccount.organizationId

// WRONG: ?. prevents the exception but always returns null
ec.user.userAccount?.organizationId
```

## Testing Framework Guidelines

**MANDATORY: Test-Code Co-location Rule**

**All tests MUST be implemented in the same repository as the code they test:**

1. **{localization-component} Repository Tests**:
   - Tests for `mycompany.*` services → `{localization-component}/src/test/groovy/`
   - Tests for {localization-component} entities → `{localization-component}/src/test/groovy/`
   - Tests for {localization-component} screens → Use Playwright tests in `{localization-component}/`

2. **Component Repository Tests**:
   - Tests for component-specific services → `{component}/src/test/groovy/`
   - Tests for component-specific entities → `{component}/src/test/groovy/`
   - Tests for component-specific screens → Use Playwright tests in `{component}/`

3. **Framework Repository Tests**:
   - Tests for framework utilities → `framework/src/test/groovy/` (if applicable)
   - Framework integration tests → Framework repository

**Prohibited Patterns:**
- Testing {localization-component} services from {component-name} repository
- Testing component code from external repositories
- Cross-repository test dependencies
- Tests that depend on code from different repositories

**Rationale:**
- Ensures tests run when code changes
- Maintains clear ownership and responsibility
- Enables independent component development

**Common Testing Gotchas:**
See `references/testing_patterns.md` section "Common Testing Gotchas" for:
- Auto-generated create services and user-provided PKs
- View entity joins causing empty results
- Timestamp ordering within transactions
- Prevents circular dependencies between repositories
- Supports proper CI/CD pipeline organization

**Enforcement:**
- All new tests must be validated for proper co-location
- Existing tests violating this rule must be moved to correct repositories
- Code reviews must verify test location compliance

**Testing Infrastructure Setup for Components**

### Required Configuration for Test Discovery
- Add `useJUnitPlatform()` to the test task in build.gradle
- Test files MUST end with "Tests.groovy" (not "Test.groovy") 
- Tests must be included in component test suites (e.g., ComponentSuite.groovy)
- Proper test user accounts with permissions must be configured

### Test Data Management Best Practices
1. **Use Seed Data for Test Infrastructure**:
   - Create test organizations, users, and base configuration in `data/Test{Domain}Data.xml`
   - Use seed-type data that loads automatically with the component

2. **Dynamic Data Generation**:
   - Create services to generate valid test data with current dates
   - Generate test configuration data with proper date ranges
   - Example: `TestDataServices.xml` for test data generation

3. **Test Organization Structure**:
   ```xml
   <!-- Test Organization -->
   <mantle.party.Party partyId="TEST-DOMAIN-ORG" partyTypeEnumId="PtyOrganization"/>
   <mantle.party.Organization partyId="TEST-DOMAIN-ORG" organizationName="Test Domain Organization"/>

   <!-- Domain Configuration -->
   <mycompany.myapp.DomainConfig partyId="TEST-DOMAIN-ORG" .../>
   <mycompany.myapp.DomainEntity entityId="TEST-DOMAIN-ORG-1" .../>
   ```

4. **Test User Configuration**:
   ```xml
   <moqui.security.UserAccount userId="domain-test-user" username="domain-test-user"
                               currentPassword="testpass123" passwordHashType="SHA" disabled="N"/>
   <moqui.security.UserGroupMember userId="domain-test-user" userGroupId="ADMIN"/>
   ```

### Example Test Structure
```groovy
class DomainFeatureTests extends Specification {
    @Shared ExecutionContext ec
    @Shared String testOrgPartyId = "TEST-DOMAIN-ORG" // Pre-configured

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("domain-test-user", "testpass123")
        initializeTestData() // Generate fresh test data
    }
    
    // ✅ CRITICAL: Use numbered prefixes for ordered execution
    def "001 - test basic functionality"() { }
    def "002 - test advanced features"() { }
    def "003 - test error handling"() { }
}
```

**Test Method Naming Convention:**
- **Numbered prefixes** (001, 002, 003...) ensure predictable execution order
- **Spock executes tests alphabetically** - numbering controls this order
- **Logical progression** from basic to complex scenarios
- **Consistent reports** across test runners and CI/CD systems

### Components with Working Test Infrastructure
- **{component-name}**: Full test suite with proper configuration
- **mantle-usl**: Reference implementation for Spock tests
- **{localization-component}**: Now configured with proper test discovery and infrastructure

## Entity Auto-Services

Moqui Framework automatically generates CRUD services for every entity defined in the system. These auto-services follow predictable naming patterns and are essential for entity operations.

### Auto-Service Naming Patterns

**Entity Auto-Services**:
```groovy
// For entity: mantle.product.Product
ec.service.sync().name("create#mantle.product.Product")     // Create new entity
ec.service.sync().name("update#mantle.product.Product")     // Update existing entity  
ec.service.sync().name("store#mantle.product.Product")      // Create or update entity
ec.service.sync().name("delete#mantle.product.Product")     // Delete entity
```

**Example Usage in Tests**:
```groovy
// ✅ CORRECT: Use entity auto-services
Map productResult = ec.service.sync().name("create#mantle.product.Product")
    .parameters([
        productName: "Test Product",
        productTypeEnumId: "PtService",
        description: "Test product description"
    ]).call()

// ❌ INCORRECT: Don't assume custom service exists
Map productResult = ec.service.sync().name("mantle.product.ProductServices.create#Product")
    .parameters([...]).call()  // Service may not exist
```

### Special Case: UserAccount Creation

**CRITICAL: Never use the direct entity auto-service for UserAccount creation. Always use the proper UserServices.**

**❌ INCORRECT: Direct entity service bypasses password validation**
```groovy
// DON'T DO THIS - Bypasses password validation and requirements
Map userResult = ec.service.sync().name("create#moqui.security.UserAccount")
    .parameters([
        username: "testuser",
        newPassword: "simple",  // Won't be validated
        userFullName: "Test User"
    ]).call()
```

**✅ CORRECT: Use UserServices for proper password validation**
```groovy
// DO THIS - Validates password requirements, checks complexity, etc.
Map userResult = ec.service.sync().name("org.moqui.impl.UserServices.create#UserAccount")
    .parameters([
        username: "testuser",
        newPassword: "ComplexPass123!",      // Must meet password requirements
        newPasswordVerify: "ComplexPass123!", // Confirmation required
        userFullName: "Test User",
        emailAddress: "test@example.com",
        disabled: "N"
    ]).call()
```

**Why UserServices is Required:**
- **Password Validation**: Enforces password complexity requirements (special characters, uppercase, numbers, etc.)
- **Security Compliance**: Ensures passwords meet organizational security policies
- **Proper Workflow**: Follows the complete user creation workflow with all validations
- **Configuration Aware**: Respects system password policy configuration

### Password Hashing for Seed Data (SHA-256)

When creating UserAccount records in seed data files, passwords must be stored as SHA-256 hashes. Moqui computes the hash by **prepending the salt directly to the password** (no separator).

**UserAccount attributes:**
- `passwordHashType="SHA-256"` — the hashing algorithm
- `passwordBase64="N"` — the hash is hex-encoded (not base64)
- `passwordSalt` — random 8-character string
- `currentPassword` — hex-encoded SHA-256 hash of `salt + password`

**Generating a password hash:**

1. Generate a random 8-character salt:
   ```bash
   openssl rand -base64 6
   ```

2. Compute the SHA-256 hash (salt prepended to password, no separator):
   ```bash
   printf '%s' '{salt}{password}' | shasum -a 256 | awk '{print $1}'
   ```

3. Place both values in the data file:
   ```xml
   <moqui.security.UserAccount userId="{ADMIN_USER_ID}"
           username="{admin-user}"
           passwordHashType="SHA-256" passwordBase64="N"
           passwordSalt="xK7mPq2R"
           currentPassword="a1b2c3d4e5f6...64-char-hex-string..."/>
   ```

**Important:** This is for seed data files only. At runtime, always use `org.moqui.impl.UserServices.create#UserAccount` which handles hashing automatically (see section above).

### Auto-Service Parameters

**Input Parameters**:
- Auto-services accept all entity fields as parameters
- Primary key fields are automatically generated if not provided
- Required fields must be specified (based on entity definition)
- Optional fields can be omitted

**Output Parameters**:

**create# Services** - Returns primary key fields based on these rules (evaluated in order):

1. **Sequenced Primary Keys**: Always returned when auto-generated
   ```groovy
   // Entity with sequenced PK (e.g., Party with partyId)
   def result = ec.service.sync().name("create#mantle.party.Party")
       .parameters([partyTypeEnumId: "PtyPerson"]).call()
   // Returns: result.partyId (auto-generated sequence)
   ```

2. **Primary Keys with Defaults**: **ALWAYS returned**, even in composite keys
   ```groovy
   // WorkEffortNote: Composite PK (workEffortId + noteDate with default)
   def result = ec.service.sync().name("create#mantle.work.effort.WorkEffortNote")
       .parameters([
           workEffortId: "MATTER-001",  // Provided (not returned)
           noteText: "Test note"
       ]).call()
   // Returns: result.noteDate (has default="ec.user.nowTimestamp")
   // Does NOT return: result.workEffortId (no default, was provided)
   ```

3. **All PK Fields Provided**: No explicit PK fields returned (unless they have defaults)
   ```groovy
   // WorkEffortParty: Composite PK with no defaults
   def result = ec.service.sync().name("create#mantle.work.effort.WorkEffortParty")
       .parameters([
           workEffortId: "MATTER-001",
           partyId: "PARTY-123",
           roleTypeId: "WeptAssigned",
           fromDate: ec.user.nowTimestamp
       ]).call()
   // Returns: Empty map or success indicator only
   // All PKs were provided and have no defaults
   ```

4. **Mixed Scenario**: Returns sequenced/defaulted PKs only
   ```groovy
   // Entity with composite key: sequenced + provided
   def result = ec.service.sync().name("create#some.Entity")
       .parameters([
           providedKey: "VALUE",  // Provided in parameters
           // sequencedKey not provided - will be generated
       ]).call()
   // Returns: result.sequencedKey (auto-generated)
   // Does NOT return: result.providedKey (was provided in input)
   ```

**CRITICAL Testing Implications**:
- **DO NOT assume** all PK fields are returned from create# services
- **DO NOT use** returned PKs to validate entity creation unless they have defaults or are sequenced
- **DO query** the entity after creation if you need all PK values for validation

```groovy
// ❌ WRONG: Assumes PK is returned when it's not
def result = ec.service.sync().name("create#mantle.work.effort.WorkEffortParty")
    .parameters([workEffortId: workEffortId, partyId: partyId,
                 roleTypeId: roleTypeId, fromDate: now]).call()
validateServiceResult(result, "WorkEffortParty creation")  // May fail - no PKs returned

// ✅ CORRECT: Don't rely on returned PKs for composite keys without defaults
def result = ec.service.sync().name("create#mantle.work.effort.WorkEffortParty")
    .parameters([workEffortId: workEffortId, partyId: partyId,
                 roleTypeId: roleTypeId, fromDate: now]).call()
// Simply verify no errors occurred
if (ec.message.hasError()) {
    throw new RuntimeException("WorkEffortParty creation failed")
}
```

**update# and store# Services**:
- Return success status
- May return updated field values in some cases

**delete# Services**:
- Return deletion confirmation
- No entity data returned

### Entity Auto-Service Best Practices

1. **Always Use Auto-Services for Basic CRUD Operations**:
   ```groovy
   // Entity creation
   ec.service.sync().name("create#mantle.party.Party")
       .parameters([partyTypeEnumId: "PtyPerson"]).call()
   
   // Entity update  
   ec.service.sync().name("update#mantle.party.Party")
       .parameters([partyId: "12345", comments: "Updated"]).call()
   
   // Entity deletion
   ec.service.sync().name("delete#mantle.party.Party")
       .parameters([partyId: "12345"]).call()
   ```

2. **Check Service Existence Before Use**:
   - Not all entities have custom services (like `EntityServices.create#Entity`)
   - Always verify service existence in service definition files
   - Fall back to auto-services for basic operations

3. **Parameter Validation**:
   ```groovy
   // Auto-services perform automatic validation
   Map result = ec.service.sync().name("create#mantle.product.Product")
       .parameters([
           productName: "Test Product",        // Required
           productTypeEnumId: "PtService",    // Required  
           description: "Optional field"      // Optional
       ]).call()
   
   // Check for validation errors
   if (ec.message.hasError()) {
       logger.error("Validation failed: ${ec.message.getErrorsString()}")
   }
   ```

### Association Entity sequenceNum Pattern

**IMPORTANT: When creating association records (like `WorkEffortAssoc`, `PartyRelationship`, etc.) that need ordering, explicitly calculate and set the `sequenceNum` field.**

Association entities often have a `sequenceNum` field to control display order. If not set, items may appear in arbitrary order (typically by creation time or primary key).

**❌ WRONG: Missing sequenceNum causes unpredictable ordering**
```xml
<!-- Items will appear in arbitrary order -->
<service-call name="create#mantle.work.effort.WorkEffortAssoc">
    <field-map field-name="workEffortId" from="itemId"/>
    <field-map field-name="toWorkEffortId" from="parentId"/>
    <field-map field-name="workEffortAssocTypeEnumId" value="WeatScheduledFor"/>
    <field-map field-name="fromDate" from="ec.user.nowTimestamp"/>
    <!-- Missing sequenceNum! -->
</service-call>
```

**✅ CORRECT: Calculate and set sequenceNum for proper ordering**
```xml
<!-- Get max sequence number from existing associations -->
<entity-find entity-name="mantle.work.effort.WorkEffortAssoc" list="existingAssocs">
    <econdition field-name="toWorkEffortId" from="parentId"/>
    <econdition field-name="workEffortAssocTypeEnumId" value="WeatScheduledFor"/>
    <econdition field-name="thruDate" operator="is-null"/>
    <order-by field-name="-sequenceNum"/>
</entity-find>
<set field="maxSeqNum" from="existingAssocs ? (existingAssocs[0].sequenceNum ?: 0) : 0"/>
<set field="newSeqNum" from="(maxSeqNum as Integer) + 10"/>

<!-- Create association with explicit sequenceNum -->
<service-call name="create#mantle.work.effort.WorkEffortAssoc">
    <field-map field-name="workEffortId" from="itemId"/>
    <field-map field-name="toWorkEffortId" from="parentId"/>
    <field-map field-name="workEffortAssocTypeEnumId" value="WeatScheduledFor"/>
    <field-map field-name="fromDate" from="ec.user.nowTimestamp"/>
    <field-map field-name="sequenceNum" from="newSeqNum"/>
</service-call>
```

**Best Practices:**
- **Increment by 10** to allow inserting items between existing ones later
- **Filter active records** (`thruDate is null`) when calculating max sequence
- **Order descending** (`-sequenceNum`) to get the max value as the first result
- **Handle null** when no existing records exist (`existingAssocs[0].sequenceNum ?: 0`)

**Common Association Entities with sequenceNum:**
- `mantle.work.effort.WorkEffortAssoc` - Work effort relationships
- `mantle.work.effort.WorkEffortContent` - Content attached to work efforts (see [Mantle Content Entity Architecture](#mantle-content-entity-architecture))
- `mantle.party.PartyContent` - Content attached to parties
- `mantle.product.ProductContent` - Content attached to products

**Note:** Content association entities store `contentLocation` directly. There is no centralized `Content` entity in Mantle. See [Mantle Content Entity Architecture](#mantle-content-entity-architecture) for details.

### Service Discovery in Component Development

**Finding Available Services**:
```bash
# Search for service definitions in component
find runtime/component/{component-name}/service -name "*.xml" -exec grep -l "verb.*noun" {} \;

# Search for specific service patterns
grep -r "verb=\"create\" noun=\"Product\"" runtime/component/*/service/
```

**Service Definition Locations**:
- `runtime/component/{component}/service/{namespace}/{ServiceName}.xml`
- Example: `runtime/component/mantle-usl/service/mantle/product/ProductServices.xml`
- Auto-services are generated automatically (no XML definition needed)

## Entity Query Operations

This section documents proper syntax and patterns for entity queries using `entity-find` and related operations.

### Order-By Syntax

**CRITICAL: Use prefix notation for order-by, not the order attribute.**

**❌ INCORRECT: Using order attribute**
```xml
<entity-find entity-name="moqui.service.job.ServiceJobRun" list="jobRuns">
    <econdition field-name="startTime" operator="greater-equals" from="timeThreshold"/>
    <order-by field-name="startTime" order="descending"/>
</entity-find>
```

**✅ CORRECT: Using prefix notation**
```xml
<entity-find entity-name="moqui.service.job.ServiceJobRun" list="jobRuns">
    <econdition field-name="startTime" operator="greater-equals" from="timeThreshold"/>
    <order-by field-name="-startTime"/>
</entity-find>
```

### Order-By Prefix Rules

**Prefix Notation:**
- **No prefix**: Ascending order (default)
- **Minus (-)**: Descending order
- **Plus (+)**: Explicit ascending order (optional, same as no prefix)

**Examples:**
```xml
<!-- Ascending order (default) -->
<order-by field-name="partyId"/>
<order-by field-name="+partyId"/>        <!-- explicit ascending -->

<!-- Descending order -->
<order-by field-name="-createdDate"/>    <!-- most recent first -->
<order-by field-name="-priority"/>       <!-- highest priority first -->
```

### Multiple Field Ordering

**Use comma-separated fields for multiple sort criteria:**
```xml
<!-- Multiple fields with mixed sort orders -->
<order-by field-name="priority,-lastProcessAttemptDate,partyId"/>

<!-- Equivalent to multiple order-by elements (but less efficient) -->
<order-by field-name="priority"/>
<order-by field-name="-lastProcessAttemptDate"/>
<order-by field-name="partyId"/>
```

### NULL Handling in Order-By

**Use NULLS FIRST or NULLS LAST for explicit null ordering:**
```xml
<!-- NULLs first, then ascending order -->
<order-by field-name="lastProcessAttemptDate NULLS FIRST"/>

<!-- NULLs last, then descending order -->  
<order-by field-name="-priority NULLS LAST"/>

<!-- Complex example with mixed null handling -->
<order-by field-name="priority,-lastProcessAttemptDate NULLS FIRST,-mercadoPublicoTaskId"/>
```

### Common Order-By Patterns

**Temporal Data (Most Recent First):**
```xml
<entity-find entity-name="moqui.basic.email.EmailMessage" list="recentEmails">
    <order-by field-name="-receivedDate"/>
</entity-find>
```

**Priority-Based Processing:**
```xml
<entity-find entity-name="mycompany.dte.DteEnvio" list="enviosToProcess">
    <econdition field-name="statusId" value="DteEnvioReceived"/>
    <order-by field-name="priority,-receivedDate NULLS LAST"/>
</entity-find>
```

**Job Run Queries (Common Pattern):**
```xml
<entity-find entity-name="moqui.service.job.ServiceJobRun" list="activeJobs">
    <econdition field-name="endTime" from="null"/>
    <order-by field-name="-startTime"/>  <!-- Most recent jobs first -->
</entity-find>
```

### Performance Considerations

**Order-By Performance Tips:**
1. **Index Alignment**: Ensure database indexes match your order-by fields
2. **Field Selection**: Only order by fields that have indexes when possible  
3. **Limit Results**: Use `limit` attribute to prevent large result sets
4. **Multiple Criteria**: Use single order-by with comma separation instead of multiple elements

**Example with Performance Optimization:**
```xml
<entity-find entity-name="moqui.service.job.ServiceJobRun" list="recentJobs" limit="50">
    <econdition field-name="startTime" operator="greater-equals" from="timeThreshold"/>
    <econdition field-name="endTime" from="null"/>
    <!-- Single order-by with indexed field first -->
    <order-by field-name="-startTime,jobName"/>
</entity-find>
```

### Best Practices Summary

1. **Always use prefix notation** (`-fieldName`) instead of order attribute
2. **Use comma-separated fields** for multiple sort criteria in single order-by element
3. **Specify NULL handling explicitly** when nulls affect business logic
4. **Align order-by fields with database indexes** for optimal performance
5. **Use descriptive ordering** that matches the business intent (most recent first, highest priority first, etc.)

## Entity Access Control Patterns

This section covers best practices for implementing data access control, tenant separation, and security constraints in Moqui applications.

### Core Principle: Separation of Concerns

**CRITICAL: Data access control and tenant separation should be implemented through EntityFilters, NOT through direct constraints in entity-find operations.**

This separation ensures:
- **Business Logic Clarity**: entity-find operations focus on business requirements
- **Security Centralization**: Access control is managed in one place
- **Framework Integration**: Leverages Moqui's built-in security infrastructure
- **Maintainability**: Security changes don't require touching business logic
- **Performance**: Framework optimizes EntityFilter SQL generation

### EntityFilters vs Direct Constraints

#### ❌ INCORRECT: Direct Access Control in Entity-Find

**Do NOT embed tenant or access control logic directly in entity-find operations:**

```xml
<!-- WRONG: Mixing business logic with access control -->
<entity-find entity-name="{entity-name}" list="{entity-list}">
    <econdition field-name="statusId" value="Active"/>  <!-- Business logic -->
    <econdition field-name="tenantId" from="ec.user.tenantId"/>  <!-- Access control - WRONG -->
    <econdition field-name="ownerPartyId" operator="in" from="ec.user.context.filterOrgIds"/>  <!-- Access control - WRONG -->
</entity-find>
```

**Problems with this approach:**
- Mixes business logic with security concerns
- Must be repeated in every query
- Easy to forget or implement inconsistently
- Harder to maintain and audit
- Bypasses framework security optimizations

#### ✅ CORRECT: EntityFilter-Based Access Control

**Use EntityFilters for access control, keep entity-find for business logic:**

```xml
<!-- CORRECT: Pure business logic in entity-find -->
<entity-find entity-name="{entity-name}" list="{entity-list}">
    <econdition field-name="statusId" value="Active"/>  <!-- Business logic only -->
    <econdition field-name="categoryId" from="categoryId"/>  <!-- Business logic only -->
</entity-find>
```

**EntityFilter handles access control automatically:**
```xml
<!-- EntityFilter definition (in data files) -->
<moqui.security.EntityFilter entityFilterId="{COMPONENT}_USER_ORG"
                              entityFilterSetId="{COMPONENT}_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[ownerPartyId:ec.user.context.filterOrgIds]"/>
```

### When to Use Each Approach

#### Use EntityFilters for:
- **Tenant separation** - Filtering data by tenant/organization
- **User-based access control** - Filtering data by user permissions
- **Security constraints** - Row-level security based on roles
- **Organization hierarchy** - Filtering based on organizational structure
- **Audit requirements** - Consistent access logging and control

#### Use Direct Constraints for:
- **Business logic** - Status filters, category filters, date ranges
- **Functional requirements** - Application-specific filtering logic
- **Query optimization** - Performance-critical business conditions
- **API parameters** - User-provided search criteria

### Multi-Tenancy Implementation

#### Framework Built-In Multi-Tenancy

Moqui provides built-in multi-tenancy support through:
- Automatic `tenantId` field handling
- Tenant-aware user sessions
- Tenant-specific data isolation
- Automatic tenant filtering

#### ✅ CORRECT: Tenant-Aware Entity Access

**Leverage framework multi-tenancy instead of manual tenant filtering:**

```xml
<!-- Framework automatically adds tenant filtering -->
<entity-find entity-name="{entity-name}" list="{entity-list}">
    <econdition field-name="statusId" value="Active"/>
    <!-- No manual tenantId condition needed -->
</entity-find>
```

**For cross-tenant access (admin scenarios), use explicit override:**
```xml
<!-- Explicit cross-tenant access when needed -->
<entity-find entity-name="{entity-name}" list="{entity-list}" use-clone="true">
    <econdition field-name="statusId" value="Active"/>
    <econdition field-name="tenantId" from="targetTenantId"/>  <!-- Explicit business requirement -->
</entity-find>
```

### EntityFilter Setup Patterns

#### Understanding Filter Context Population

**IMPORTANT**: EntityFilter `filterMap` uses `ec.user.context` values that are set **per-request** by services or screen actions. This is different from session persistence:

- **Filter context setup**: Done by services/REST APIs at the start of each request
- **Filter evaluation**: Happens during entity queries within the same request
- **No persistence needed**: Each request sets up its own filter context

**Typical filter context setup in REST API:**
```xml
<resource name="api">
    <pre-service>
        <service-call name="MyServices.setup#FilterContext" out-map="context"/>
    </pre-service>
    ...
</resource>
```

**Service that populates filter context:**
```xml
<service verb="setup" noun="FilterContext">
    <actions>
        <!-- Query user's organizations -->
        <entity-find entity-name="mantle.party.PartyRelationship" list="orgRelList">
            <econdition field-name="fromPartyId" from="ec.user.userAccount?.partyId"/>
            <econdition field-name="relationshipTypeEnumId" value="PrtEmployee"/>
            <date-filter/>
        </entity-find>
        <!-- Set filter context for this request -->
        <set field="ec.user.context.filterOrgIds" from="orgRelList*.toPartyId"/>
    </actions>
</service>
```

#### Basic EntityFilter Configuration

**1. EntityFilterSet Definition:**
```xml
<moqui.security.EntityFilterSet entityFilterSetId="{COMPONENT}_USER_ORG"/>
```

**2. EntityFilter Definition:**
```xml
<moqui.security.EntityFilter entityFilterId="{COMPONENT}_ORG_FILTER"
                              entityFilterSetId="{COMPONENT}_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[ownerPartyId:ec.user.context.filterOrgIds]"/>
```

**3. ArtifactGroup Association:**
```xml
<moqui.security.ArtifactGroup artifactGroupId="{COMPONENT}_SERVICES">
    <artifacts artifactName="{component}.{namespace}.*" artifactTypeEnumId="AT_SERVICE"/>
</moqui.security.ArtifactGroup>
```

**4. Authorization with Filter:**
```xml
<moqui.security.ArtifactAuthz artifactAuthzId="{COMPONENT}_USER_AUTHZ"
                              userGroupId="{USER_GROUP}"
                              artifactGroupId="{COMPONENT}_SERVICES"
                              authzTypeEnumId="AUTHZT_ALLOW"/>

<moqui.security.ArtifactAuthzFilter artifactAuthzId="{COMPONENT}_USER_AUTHZ"
                                    entityFilterSetId="{COMPONENT}_USER_ORG"/>
```

#### Common EntityFilter Patterns

**Organization-Based Filtering:**
```xml
<moqui.security.EntityFilter entityFilterId="ORG_BASED_ACCESS"
                              entityFilterSetId="{COMPONENT}_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[organizationPartyId:ec.user.context.activeOrgId]"/>
```

**Multi-Organization Filtering:**
```xml
<moqui.security.EntityFilter entityFilterId="MULTI_ORG_ACCESS"
                              entityFilterSetId="{COMPONENT}_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[organizationPartyId:ec.user.context.filterOrgIds]"/>
```

**User-Specific Filtering:**
```xml
<moqui.security.EntityFilter entityFilterId="USER_OWNED_DATA"
                              entityFilterSetId="{COMPONENT}_USER_DATA"
                              entityName="{entity-name}"
                              filterMap="[createdByUserId:ec.user.userId]"/>
```

### Testing EntityFilter-Enabled Services

#### Test Setup Requirements

**EntityFilter tests require special setup (opposite of normal test patterns):**

The test setup mimics what services/REST APIs do at the start of each request - populating `ec.user.context` with filter values. Since tests run within a single request, the context values remain available throughout the test.

```groovy
def setup() {
    // ENABLE authorization (opposite of normal tests)
    ec.artifactExecution.enableAuthz()

    // Set user context for filtering (mimics what REST pre-service would do)
    // NOTE: ec.user.context is per-request, not persistent across HTTP requests
    ec.user.context.activeOrgId = "TEST_ORG"
    ec.user.context.filterOrgIds = ["TEST_ORG"]
}

def cleanup() {
    // Clear filter context
    ec.user.context.remove('activeOrgId')
    ec.user.context.remove('filterOrgIds')

    // Disable authorization for cleanup
    ec.artifactExecution.disableAuthz()
}
```

#### Test Data Requirements

**EntityFilter tests need complete authorization setup:**
```xml
<!-- Test data must include EntityFilter configuration -->
<moqui.security.EntityFilterSet entityFilterSetId="TEST_USER_ORG"/>
<moqui.security.EntityFilter entityFilterId="TEST_ORG_FILTER"
                              entityFilterSetId="TEST_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[ownerPartyId:ec.user.context.filterOrgIds]"/>

<moqui.security.ArtifactAuthz artifactAuthzId="TEST_FILTER_AUTHZ"
                              userGroupId="ADMIN"
                              artifactGroupId="TEST_SERVICE_GROUP"
                              authzTypeEnumId="AUTHZT_ALLOW"/>

<moqui.security.ArtifactAuthzFilter artifactAuthzId="TEST_FILTER_AUTHZ"
                                    entityFilterSetId="TEST_USER_ORG"/>
```

### Best Practices Summary

#### DO:
- Use EntityFilters for access control and tenant separation
- Keep entity-find operations focused on business logic
- Leverage framework built-in multi-tenancy
- Test both filtered and unfiltered scenarios
- Document EntityFilter dependencies clearly

#### DON'T:
- Embed tenant or access control logic in entity-find operations
- Mix business logic with security constraints
- Bypass EntityFilter system for "simple" cases
- Forget to test authorization scenarios
- Hardcode security conditions in business queries

#### Architecture Principles:
1. **Separation of Concerns**: Business logic separate from access control
2. **Framework Integration**: Use Moqui's built-in security features
3. **Consistency**: Apply patterns uniformly across the application
4. **Auditability**: Centralized access control for security audits
5. **Maintainability**: Security changes in one place, not scattered throughout

## Framework Testing Patterns

Based on analysis of mantle-usl and successful test implementations:

### Integration Between Testing Frameworks

**When to Use Each Framework:**
- **Spock**: Service logic, entity operations, business rules, integration testing
- **Playwright**: User workflows, UI interactions, cross-browser testing, critical path validation

**Testing Pyramid Implementation:**
- 70% Spock tests (fast, comprehensive coverage)
- 25% Spock integration tests (service/data layer)
- 5% Playwright tests (critical user journeys)

### Test Data Generation Patterns

**For Spock Tests:**
```groovy
class ServiceTests extends Specification {
    def setupSpec() {
        // Use entity auto-services for test data creation
        ec.entity.tempSetSequencedIdPrimary("mantle.party.Party", 55000, 10)
        
        // Create test data using services
        def partyResult = ec.service.sync().name("create#mantle.party.Party")
                .parameters([partyTypeEnumId: "PtyOrganization"])
                .call()
    }
}
```

**For Playwright Tests:**
```javascript
// Use comprehensive test data strategy
const testEnv = await MoquiTestDataFactory.createBaseTestEnvironment({
  organizationName: 'Test Organization'
});

// Create scenario-specific data
const testData = await MoquiTestDataFactory.createTestScenario('Invoice', 'standard', {
  organizationPartyId: testEnv.organizationId
});
```

**See**: `.agent-os/e2e-test-data-strategy.md` for complete E2E test data patterns and implementation guidance.

### Authentication and Authorization Pattern

**Standard Test Authentication**:
```groovy
// ✅ CORRECT: Framework pattern from mantle-usl
def setupSpec() {
    ec = Moqui.getExecutionContext()
    ec.user.loginUser("john.doe", "moqui")  // Authenticate with framework user
}

def setup() {
    ec.artifactExecution.disableAuthz()     // Disable authorization for test calls
}

def cleanup() {
    ec.artifactExecution.enableAuthz()     // Re-enable authorization after test
}
```

**Why This Pattern Works**:
- **Authentication ≠ Authorization**: Services need authenticated users, not authorization checks in tests
- **Framework users** (john.doe, joe@public.com) have proper authentication setup
- **disableAuthz()** allows test access to services without complex permission configuration
- **Proven pattern**: Used throughout mantle-usl tests and now documented as best practice

## Security and Authorization Patterns

## Service Authorization Patterns

### CRITICAL: Entity Filters vs Static Group Checking

**❌ NEVER use static group checking in service logic - this violates Moqui's authorization architecture**

Static group checking bypasses Moqui's flexible, record-level security and creates rigid, unmaintainable authorization patterns.

#### ❌ INCORRECT: Static Group Checking in Services

```xml
<!-- DON'T DO THIS - Static group checking in service logic -->
<service verb="find" noun="ApiKeys">
    <actions>
        <!-- ❌ WRONG: Hard-coded group checking -->
        <if condition="ec.user.isInGroup('SITE_ADMIN')">
            <!-- Site admin logic -->
            <entity-find entity-name="ApiKeyView" list="apiKeyList"/>
        <else>
            <!-- Tenant admin logic -->
            <entity-find entity-name="ApiKeyView" list="apiKeyList">
                <econdition field-name="tenantId" from="ec.user.context.currentTenantId"/>
            </entity-find>
        </else></if>
    </actions>
</service>
```

**Problems with static group checking:**
- **Inflexible**: Cannot handle complex organizational hierarchies
- **Unmaintainable**: Business logic mixed with access control
- **Bypasses Framework**: Ignores Moqui's entity filtering capabilities
- **Hard to Audit**: Access control logic scattered throughout services
- **Violates Separation of Concerns**: Authorization mixed with business logic

#### ✅ CORRECT: Entity Filter-Based Authorization

```xml
<!-- ✅ CORRECT: Service with no access control logic -->
<service verb="find" noun="ApiKeys">
    <actions>
        <!-- Clean business logic - no authorization checks -->
        <entity-find entity-name="ApiKeyView" list="apiKeyList">
            <econdition field-name="tenantId" from="tenantId" ignore-if-empty="true"/>
            <econdition field-name="externalSystemName" operator="like" from="'%' + externalSystemName + '%'" ignore-if-empty="true"/>
            <order-by field-name="-createdDate"/>
        </entity-find>
    </actions>
</service>
```

**Authorization handled through EntityFilter configuration:**

```xml
<!-- EntityFilter automatically applies tenant filtering -->
<moqui.security.EntityFilter entityFilterId="API_KEY_TENANT_FILTER"
                              entityFilterSetId="API_KEY_USER_ACCESS"
                              entityName="mycompany.myapp.api.ApiKeyView"
                              filterMap="[tenantId:ec.user.context.filterOrgIds]"/>

<!-- Users get different filterOrgIds based on their roles -->
<moqui.security.ArtifactAuthzFilter artifactAuthzId="TENANT_ADMIN_AUTHZ"
                                    entityFilterSetId="API_KEY_USER_ACCESS"/>
```

#### Entity Filter Authorization Benefits

1. **Flexible Access Control**: Users can have multiple organizational affiliations
2. **Dynamic Filtering**: Access rights can change without code changes
3. **Framework Integration**: Leverages Moqui's optimized filtering system
4. **Centralized Security**: All access control in authorization configuration
5. **Auditable**: Clear separation between business logic and security
6. **Record-Level Security**: Fine-grained control at the data level

#### Proper Authorization Architecture

**Service Layer (Business Logic Only):**
```xml
<service verb="process" noun="ApiKeyRequest">
    <actions>
        <!-- Pure business logic -->
        <entity-find-one entity-name="mycompany.myapp.api.ApiKeyMetadata" value-field="apiKey">
            <field-map field-name="loginKey" from="loginKey"/>
        </entity-find-one>
        <!-- Framework automatically applies EntityFilter -->
    </actions>
</service>
```

**Authorization Layer (Security Configuration):**
```xml
<!-- Role-based EntityFilter sets -->
<moqui.security.EntityFilterSet entityFilterSetId="SITE_ADMIN_ACCESS"/>
<moqui.security.EntityFilterSet entityFilterSetId="TENANT_ADMIN_ACCESS"/>

<!-- Different filters for different roles -->
<moqui.security.EntityFilter entityFilterId="SITE_ADMIN_FILTER"
                              entityFilterSetId="SITE_ADMIN_ACCESS"
                              entityName="mycompany.myapp.api.ApiKeyView"
                              filterMap="[:]"/>  <!-- No filtering for site admin -->

<moqui.security.EntityFilter entityFilterId="TENANT_ADMIN_FILTER"
                              entityFilterSetId="TENANT_ADMIN_ACCESS"
                              entityName="mycompany.myapp.api.ApiKeyView"
                              filterMap="[tenantId:ec.user.context.filterOrgIds]"/>

<!-- User groups get appropriate filter sets -->
<moqui.security.ArtifactAuthzFilter artifactAuthzId="SITE_ADMIN_AUTHZ"
                                    entityFilterSetId="SITE_ADMIN_ACCESS"/>
<moqui.security.ArtifactAuthzFilter artifactAuthzId="TENANT_ADMIN_AUTHZ"
                                    entityFilterSetId="TENANT_ADMIN_ACCESS"/>
```

### Authorization Inheritance Model

Moqui Framework follows a **main artifact authorization pattern** where authorization is defined at the primary access point (typically a screen) and inherited by subordinate services and entities.

#### Best Practice: Main Artifact Authorization

```xml
<!-- ✅ CORRECT: Define authorization at main screen level -->
<moqui.security.ArtifactGroup artifactGroupId="DTE_ADMIN" description="DTE Administration"/>
<moqui.security.ArtifactGroupMember artifactGroupId="DTE_ADMIN" 
                                    artifactName="component://{component}/screen/{AdminScreen}/DomainAdmin.xml" 
                                    artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>

<moqui.security.ArtifactAuthz artifactAuthzId="DTE_ADMIN_ACCESS" 
                              userGroupId="{ProjectAdminUsers}" 
                              artifactGroupId="DTE_ADMIN" 
                              authzTypeEnumId="AUTHZT_ALLOW" 
                              authzActionEnumId="AUTHZA_ALL"/>
```

#### What NOT to Do: Over-Engineering Authorization

```xml
<!-- ❌ AVOID: Creating specific authorization for every service -->
<moqui.security.ArtifactGroup artifactGroupId="CAF_AUTO_REQUEST" description="CAF Auto Request Management"/>
<moqui.security.ArtifactGroupMember artifactGroupId="CAF_AUTO_REQUEST" 
                                    artifactName="mycompany.dte.DteFolioServices.check#FolioAvailability" 
                                    artifactTypeEnumId="AT_SERVICE" inheritAuthz="Y"/>
<moqui.security.ArtifactGroupMember artifactGroupId="CAF_AUTO_REQUEST" 
                                    artifactName="mycompany.dte.DteFolioServices.run#CafAutoRequest" 
                                    artifactTypeEnumId="AT_SERVICE" inheritAuthz="Y"/>
<!-- ... more unnecessary service authorizations ... -->

<moqui.security.ArtifactAuthz artifactAuthzId="CAF_AUTO_REQUEST_ADMIN" 
                              userGroupId="NON_EXISTENT_GROUP" 
                              artifactGroupId="CAF_AUTO_REQUEST" 
                              authzTypeEnumId="AUTHZT_ALWAYS" 
                              authzActionEnumId="AUTHZA_ALL"/>
```

### User Group Management

#### Use Existing User Groups

Always check for existing user groups before creating new ones:

```bash
# Search for existing user groups
grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml
```

**Standard Framework Groups**:
- `ADMIN` - Full system administrators
- `ADMIN_ADV` - Advanced administrators
- `ALL_USERS` - All authenticated users (default membership)

**Component-Specific Groups** (prefer existing):
- `{ProjectAdminUsers}` - Project-specific component administrators
- `TENANT_ADMIN` - Multi-tenant administrators

#### User Group Validation Pattern

```xml
<!-- ✅ CORRECT: Use existing groups -->
<moqui.security.ArtifactAuthz artifactAuthzId="SCREEN_ACCESS" 
                              userGroupId="{ProjectAdminUsers}" 
                              artifactGroupId="SCREEN_GROUP" 
                              authzTypeEnumId="AUTHZT_ALLOW" 
                              authzActionEnumId="AUTHZA_ALL"/>

<!-- ❌ AVOID: Referencing non-existent groups -->
<moqui.security.ArtifactAuthz artifactAuthzId="BROKEN_ACCESS" 
                              userGroupId="DTE_ADMIN" 
                              artifactGroupId="SOME_GROUP" 
                              authzTypeEnumId="AUTHZT_ALLOW" 
                              authzActionEnumId="AUTHZA_ALL"/>
```

### ServiceJob Authorization

ServiceJobs run in **system context** and typically don't need specific user group authorization:

```xml
<!-- ✅ CORRECT: ServiceJob without user authorization -->
<moqui.service.job.ServiceJob jobName="sii_dte_CafAutoRequest" 
                              serviceName="mycompany.dte.DteFolioServices.run#CafAutoRequest" 
                              cronExpression="0 0 6,12,18 * * ?" 
                              paused="N"
                              description="Monitor CAF folio availability"/>

<!-- Note: Services inherit authorization from screens that manage them -->
<!-- No specific ArtifactAuthz needed for system-context ServiceJobs -->
```

### Authorization Troubleshooting

#### Common Authorization Issues

1. **Non-existent User Group References**
   ```bash
   # Find all userGroupId references
   grep -r "userGroupId=" runtime/component/*/data/*.xml
   
   # Check if groups exist
   grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml
   ```

2. **Over-Complex Authorization Setup**
   - Look for unnecessary ArtifactGroups
   - Check if main screen already provides coverage
   - Validate that services really need specific authorization

3. **Missing Authorization Inheritance**
   - Ensure main screens have proper authorization
   - Verify `inheritAuthz="Y"` on artifact group members
   - Check that subordinate services rely on inheritance

#### Authorization Validation Checklist

- [ ] All `userGroupId` references point to existing user groups
- [ ] Main screens have comprehensive authorization coverage  
- [ ] Services inherit authorization from screens (avoid specific service authorization)
- [ ] ServiceJobs run in system context (no user group authorization needed)
- [ ] Existing user groups are preferred over creating new ones
- [ ] Authorization follows the principle of least privilege

### Entity Access Control with ownerPartyId

**CRITICAL: The ownerPartyId field is primarily used for entity filtering and access control in multi-tenant applications.**

The `ownerPartyId` field determines which party can view and access data records through Moqui's entity filtering mechanisms. This field should be set to the party that "owns" the data record from an access control perspective, not necessarily the physical owner.

#### Correct Usage - Access Control Ownership

**✅ CORRECT: Setting ownerPartyId for data access control**

```xml
<!-- For facilities used by client organizations -->
<mantle.facility.Facility facilityId="CLIENT_OFFICE_001"
                         facilityName="Client Office Space"
                         ownerPartyId="CLIENT_COMPANY_A"
                         facilityTypeEnumId="FcTpBuilding"/>
```

In this example:
- The facility is physically owned by the building owner
- But `ownerPartyId` is set to "CLIENT_COMPANY_A" (the party using the facility)
- This ensures only CLIENT_COMPANY_A can see this facility in their filtered views
- Multi-tenant application correctly isolates data access by party

#### Key Principles for ownerPartyId Usage

1. **Data Access Control**: ownerPartyId determines which party sees the record in filtered entity queries
2. **Multi-Tenant Isolation**: Essential for applications where multiple parties share the same database
3. **Business Context Ownership**: Set to the party that should have access, not physical ownership
4. **Entity Filter Integration**: Works with Moqui's built-in entity filtering mechanisms

#### Common Usage Patterns

**✅ CORRECT: Booking/Reservation Systems**
```xml
<!-- Meeting room reservation owned by booking party -->
<custom.BookingReservation reservationId="RES_001"
                          facilityId="CLIENT_OFFICE_001"
                          ownerPartyId="BOOKING_PARTY_A"
                          reservationDate="2024-01-15"/>
```

**✅ CORRECT: Document Management**
```xml  
<!-- Document accessible only to creating organization -->
<mantle.party.PartyContent partyContentId="DOC_001"
                          partyId="CLIENT_PARTY_A"
                          contentLocation="path/to/document.pdf"
                          ownerPartyId="CLIENT_PARTY_A"/>
```

**✅ CORRECT: Financial Records**
```xml
<!-- Invoice visible only to the billing organization -->
<mantle.account.invoice.Invoice invoiceId="INV_001"
                               fromPartyId="SERVICE_PROVIDER"  
                               toPartyId="CLIENT_PARTY_A"
                               ownerPartyId="SERVICE_PROVIDER"/>
```

#### Incorrect Usage Examples

**❌ INCORRECT: Using physical ownership instead of access control**
```xml
<!-- DON'T: Setting to building owner when tenant should see it -->
<mantle.facility.Facility facilityId="OFFICE_001"
                         facilityName="Rented Office Space"
                         ownerPartyId="BUILDING_OWNER"
                         facilityTypeEnumId="FcTpOffice"/>
<!-- This prevents the tenant from seeing their own office! -->
```

**❌ INCORRECT: Inconsistent ownership in related records**
```xml
<!-- DON'T: Mixed ownership breaking data relationships -->
<custom.ProjectBooking projectId="PROJ_001"
                      ownerPartyId="CLIENT_PARTY_A"/>

<custom.ProjectFacility projectId="PROJ_001"
                       facilityId="ROOM_001"
                       ownerPartyId="FACILITY_OWNER"/>
<!-- Client can see project but not its facility! -->
```

#### Multi-Tenant Best Practices

1. **Consistent Ownership**: Related records should typically have the same ownerPartyId
2. **Business Logic Ownership**: Consider who needs to access the data, not who technically owns it
3. **Relationship Integrity**: Ensure parent-child relationships maintain consistent access control
4. **Automatic Filtering**: Multi-tenant filtering is handled automatically by entity-filter configuration attached to authorization - **DO NOT** manually filter with `ec.user.context.activeOrgId`
5. **Test with Multiple Parties**: Always test data access with different party contexts

#### Entity-Filter Configuration vs Manual Filtering

**❌ INCORRECT**: Manual multi-tenant filtering in screens/services
```xml
<!-- DON'T: Manual filtering breaks entity-filter automation -->
<entity-find entity-name="mantle.facility.Facility" list="facilityList">
    <econdition field-name="disabled" operator="not-equals" value="Y"/>
    <econditions combine="or">
        <econdition field-name="ownerPartyId" from="ec.user.context.activeOrgId" ignore-if-empty="true"/>
        <econditions combine="and">
            <econdition field-name="ownerPartyId" operator="in" ignore-if-empty="true">
                <!-- Complex parent party filtering -->
            </econdition>
        </econditions>
    </econditions>
</entity-find>
```

**✅ CORRECT**: Let entity-filter configuration handle multi-tenancy
```xml
<!-- CORRECT: Framework applies entity-filter automatically -->
<entity-find entity-name="mantle.facility.Facility" list="facilityList">
    <econdition field-name="disabled" operator="not-equals" value="Y"/>
    <order-by field-name="facilityName"/>
</entity-find>
```

**Key Principles:**
- **Never use** `ec.user.context.activeOrgId` or `ec.user.context.filterOrgIds` in entity-finds
- **Entity-filter configuration** in authorization settings handles multi-tenancy automatically
- **Framework applies filtering** based on user's party context and authorization
- **Manual filtering** bypasses framework automation and creates maintenance burden
- **Trust the framework** - it knows how to filter based on entity-filter configuration

#### Troubleshooting ownerPartyId Issues

**Common Problems:**
- Records not appearing in filtered queries → Check ownerPartyId matches user's party context
- Related records showing inconsistently → Verify related entities have matching ownerPartyId
- Access denied errors → Ensure ownerPartyId is set for data access, not physical ownership

**Validation Commands:**
```bash
# Find entities with ownerPartyId fields
grep -r "ownerPartyId" runtime/component/*/entity/*.xml

# Check data records for consistent ownership
grep -r "ownerPartyId=" runtime/component/*/data/*.xml
```

**ownerPartyId Validation Checklist:**
- [ ] ownerPartyId set to party that needs data access, not physical owner
- [ ] Related records have consistent ownerPartyId values  
- [ ] Multi-tenant data isolation works correctly
- [ ] Entity queries filter properly by party context
- [ ] Business logic considers access control implications

### Service Error Handling in Tests

**Comprehensive Error Checking**:
```groovy
def "test service with potential errors"() {
    when:
    Map result = ec.service.sync().name("service.name")
        .parameters([param: "value"])
        .call()

    then:
    // Primary error detection - check Message Facade
    !ec.message.hasError()
    result.expectedField == "expectedValue"

    cleanup:
    ec.message.clearAll() // Clear all messages for next test
}
```

**Moqui Error Handling**:
- **Primary mechanism**: Errors go to `ec.message` (Message Facade)
- **Check for errors**: Use `ec.message.hasError()`
- **Get error text**: Use `ec.message.getErrorsString()`
- **Result map**: Will be empty or missing expected fields when errors occur
- **Service exceptions**: Thrown as exceptions (use try-catch if needed)
- **Special case only**: `result.errorMessage` exists only if explicitly defined as an out-parameter (rare)

### Sequence ID Management for Tests

**Test Isolation with Unique ID Ranges**:
```groovy
def setupSpec() {
    // Use unique ranges for different test classes to avoid conflicts
    ec.entity.tempSetSequencedIdPrimary("mantle.party.Party", 59000, 10)        // Component tests
    ec.entity.tempSetSequencedIdPrimary("mantle.product.Product", 55500, 10)    // mantle-usl pattern
    ec.entity.tempSetSequencedIdPrimary("mycompany.myapp.DomainEntity", 60000, 10) // Component-specific
}

def cleanupSpec() {
    // Always reset sequences after tests
    ec.entity.tempResetSequencedIdPrimary("mantle.party.Party")
    ec.entity.tempResetSequencedIdPrimary("mantle.product.Product") 
    ec.entity.tempResetSequencedIdPrimary("mycompany.myapp.DomainEntity")
}
```

**Sequence Range Guidelines**:
- **Framework tests**: 50000-54999
- **mantle-usl tests**: 55000-55999  
- **Component-specific tests**: 58000+ (use unique ranges per component)
- **Never overlap ranges** between different test classes or components

## Build Script Integration Patterns

### Framework Build Tasks

**SearchEngine Management**:
```groovy
// ✅ CORRECT: Reuse existing framework functions
def cleanElasticSearch(String moquiRuntime) {
    // Use existing stopSearch() and startSearch() functions
    stopSearch(moquiRuntime)    // Handles stale PIDs correctly
    
    delete file(workDir+'/data')
    if (file(workDir+'/logs').exists()) delete files(file(workDir+'/logs').listFiles())
    
    startSearch(moquiRuntime)
}

// ❌ AVOID: Reimplementing PID management
def cleanElasticSearch(String moquiRuntime) {
    // Don't reimplement complex PID checking logic
    String pid = new File(workDir+'/pid').text.trim()
    // ... complex PID management code
}
```

**Why Reuse Framework Functions**:
- Framework build scripts already have robust PID management
- Handle stale PID files, process checking, and error recovery
- Consistent behavior across all projects
- Less maintenance overhead

### Component Test Configuration

**Gradle Test Configuration**:
```groovy
test {
    useJUnitPlatform()
    maxParallelForks 1              // CRITICAL: Single-threaded execution
    include '**/MoquiComponentSuite.class'
    
    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
    
    // Component-specific test data loading
    systemProperty 'moqui.load.data.types', 'seed,seed-initial,{l10n}-install,component-demo'
}
```

## Test Data Management and Data Types

### Data Type Classifications

Moqui Framework uses a structured approach to data loading with specific data types that control when and how data is loaded:

#### Standard Data Types (Load Order)
1. **`seed`** - Core system data required for operation
   - Essential entities, enumerations, and base configuration
   - Always loaded first in any environment
   - Examples: Basic entity definitions, core enumerations, system settings

2. **`seed-initial`** - Initial setup data for new installations
   - Default users, organizations, and initial configuration
   - Loaded after seed data in fresh installations
   - Examples: Default admin users, initial organizational structure

3. **`install`** / **`{l10n}-install`** - Installation-specific configuration
   - Regional, localization, or deployment-specific data
   - Examples: Regional tax codes, locale settings, deployment-specific configurations

4. **`demo`** / **`{project}-demo`** - Demonstration and development data
   - Test organizations, sample products, demo scenarios
   - Used for development and training environments
   - Examples: Sample companies, demo products, test scenarios
   - **Date Refresh**: Use `@rel:` expressions in templates for time-sensitive demo data (see "Demo Data Date Refresh" section)

#### Project-Specific Data Types

5. **`{project}-test`** - Shared test infrastructure across project components
   - **Location**: `/runtime/component/{utils-component}/data/{Project}TestData.xml`
   - **Purpose**: Common test users, permissions, and shared test organizations
   - **Usage**: Loaded by all project component tests for consistent test infrastructure
   - **Content**:
     - Test users (EX_JOHN_DOE/john.doe, {PROJECT}_TEST_USER)
     - Shared test organizations and permissions
     - Cross-component test infrastructure

6. **`{component}-test`** - Component-specific test data
   - **Location**: `/runtime/component/{component}/data/Test{Domain}Data.xml`
   - **Purpose**: Domain-specific test organizations, configurations, and fixtures
   - **Content**: Test organizations, domain-specific configurations

### Test Data Creation Guidelines

#### Shared vs Component-Specific Data

**Use Shared Test Data (`{project}-test`) for:**
- Test user accounts that are used across multiple components
- Basic test organizations needed by multiple components
- Common permissions and roles for testing
- Cross-component test infrastructure

**Use Component-Specific Test Data for:**
- Business logic specific to the component
- Component-specific entities and configurations
- Test data that would conflict between components
- Domain-specific test scenarios

#### Data Storage Patterns

**1. Shared Test Infrastructure**
```xml
<!-- File: /runtime/component/{utils-component}/data/{Project}TestData.xml -->
<entity-facade-xml type="{project}-test">
    <!-- Primary test user shared across all project components -->
    <moqui.security.UserAccount userId="EX_JOHN_DOE" username="john.doe"
                                userFullName="John Doe" currentPassword="..."
                                currencyUomId="USD" locale="en" timeZone="US/Eastern"/>

    <!-- Common test organizations -->
    <mantle.party.Party partyId="TEST-CUSTOMER" partyTypeEnumId="PtyOrganization"/>
    <mantle.party.Organization partyId="TEST-CUSTOMER" organizationName="Test Customer Corp"/>
</entity-facade-xml>
```

**2. Component-Specific Test Data**
```xml
<!-- File: /runtime/component/{component}/data/Test{Domain}Data.xml -->
<entity-facade-xml type="{component}-test">
    <!-- Component-specific test organization -->
    <mantle.party.Party partyId="TEST-DOMAIN-ORG" partyTypeEnumId="PtyOrganization"/>
    <mantle.party.Organization partyId="TEST-DOMAIN-ORG" organizationName="Test Domain Organization"/>

    <!-- Domain-specific configuration -->
    <mycompany.myapp.DomainConfig partyId="TEST-DOMAIN-ORG" configParam="Test Domain Organization"/>
    <mycompany.myapp.DomainEntity entityId="TEST-DOMAIN-ORG-1" typeEnumId="ExampleType"/>
</entity-facade-xml>
```

#### Test Database Configuration

**REQUIRED: Use the Normal H2 Database for Testing**

Tests should always use the main H2 database (`runtime/db/h2/moqui`) rather than creating separate test databases. This approach provides simplicity, reliability, and ensures tests run against the same database structure used in development.

**✅ CORRECT: Standard Test Configuration**
```gradle
test {
    useJUnitPlatform()
    maxParallelForks 1
    
    // Use standard runtime and configuration
    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
    
    // Load test data into main database using entity_empty_db_load
    systemProperty 'entity_empty_db_load', 'seed,seed-initial,{l10n}-install,{project}-demo,{project}-test,{component}-test'
    
    // Component test suite inclusion
    include '**/ComponentSuite.class'
    
    // Hazelcast auto-increment port configuration to avoid conflicts
    systemProperty 'hazelcast.port.auto.increment', 'true'
    systemProperty 'hazelcast.port.start', '5701'  // Use unique port range per component
    systemProperty 'hazelcast.port.count', '20'
}
```

**❌ AVOID: Separate test databases**
```gradle
// DON'T DO THIS - Creates unnecessary complexity
systemProperty 'entity_ds_database', 'moqui_test_component'
systemProperty 'entity_ds_url', 'jdbc:h2:' + moquiDir.absolutePath + '/runtime/db/h2/moqui_test_component'
```

**Key Benefits of Using Main Database:**
- **Simplicity**: No need to manage separate database configurations
- **Reliability**: Tests run against the same database structure as development
- **Performance**: Faster test execution without database creation overhead
- **Consistency**: Same data loading mechanisms as development environment

#### Test Data Loading Strategy

**Required Data Types for Testing:**
```bash
./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{project}-test,{component}-test
```

**Data Loading Order and Purpose:**
1. `seed` - Core Moqui framework system data
2. `seed-initial` - Framework initial data and basic configurations
3. `{l10n}-install` - Project-specific localization (currency, geography, regional settings)
4. `{project}-demo` - Project demonstration data and basic setup
5. `{project}-test` - Shared project test infrastructure (john.doe user, test organizations)
6. `{component}-test` - Component-specific test data (test organizations, domain configurations, etc.)

**Test Class Data Usage:**
```groovy
class DomainFeatureTests extends Specification {
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        // Use shared test user from {project}-test data type
        ec.user.loginUser("john.doe", "moqui")

        // Component-specific test data is automatically loaded via build.gradle
        // TEST-DOMAIN-ORG and related data available from {component}-test type
    }
}
```

### Demo Data Date Refresh

Demo data with hardcoded dates becomes stale over time. Projects may implement their own demo data refresh system to keep time-sensitive data (meetings, deadlines, tasks) current.

A typical approach is to create XML templates with relative date expressions and a service that transforms them at load time. See your project's overlay documentation for implementation details.

## Data Validation Requirements

**CRITICAL: The moqui-data-specialist agent MUST ALWAYS validate all generated data against existing entity definitions before creating any data files.**

### Mandatory Validation Process

<data_validation_workflow>
  <step number="1" name="entity_verification">
    ### Step 1: Entity Definition Verification
    
    <verify_entity_definitions>
      BEFORE creating ANY data file:
      1. **Read and verify entity definitions** in `entity/` directories
      2. **Confirm entity names match exactly** - case-sensitive validation required
      3. **Verify all field names exist** in the target entity definition
      4. **Check field types and constraints** to ensure data compatibility
      5. **Validate primary key fields** are included and properly set
    </verify_entity_definitions>
  </step>
  
  <step number="2" name="field_validation">
    ### Step 2: Field Name and Type Validation
    
    <validate_fields>
      **Common Field Validation Errors to Avoid:**
      
      **Status Field Names:**
      - ❌ WRONG: `currentStatusId` (doesn't exist in most entities)
      - ✅ CORRECT: `statusId` (standard field name)
      
      **Status ID Values:**
      - ❌ WRONG: `WeCompleted` (non-existent status)
      - ✅ CORRECT: `WeComplete` (actual enumeration value)
      
      **Entity Name Accuracy:**
      - ❌ WRONG: `WorkEffort` (incorrect entity reference)
      - ✅ CORRECT: `mantle.work.effort.WorkEffort` (full entity name)
      
      **Field Existence Verification:**
      - ALWAYS verify field names against entity definition
      - Check for typos in field names (e.g., `partyId` vs `partyID`)
      - Validate field types match expected data (dates, IDs, text, etc.)
    </validate_fields>
  </step>
  
  <step number="3" name="reference_validation">
    ### Step 3: Reference and Relationship Validation
    
    <validate_references>
      **Status ID Validation:**
      - BEFORE referencing any status ID, verify it exists in StatusItem entity
      - Common status enumerations: `WeInPlanning`, `WeApproved`, `WeInProgress`, `WeComplete`, `WeCancelled`
      - Always check enumeration values in seed data or existing entity-facade-xml files
      
      **Foreign Key Validation:**
      - Verify referenced party IDs exist or will be created in the same data file
      - Ensure product IDs, facility IDs, and other references are valid
      - Check that parent records are created before child records in data files
      
      **Date and Time Validation:**
      - Use proper date formats: `yyyy-MM-dd HH:mm:ss.SSS`
      - Ensure date fields match expected types (date vs date-time)
      - Validate time zones when applicable
    </validate_references>
  </step>
</data_validation_workflow>

### Entity Definition Verification Process

**Required Steps Before Data Creation:**

1. **Locate Entity Definitions:**
   ```bash
   # Find entity definitions
   find runtime/component -name "*.xml" -path "*/entity/*" | grep -i [entity-name]
   ```

2. **Read Entity XML Files:**
   ```xml
   <!-- Example: Verify WorkEffort entity structure -->
   <entity entity-name="WorkEffort" package="mantle.work.effort">
       <field name="workEffortId" type="id" is-pk="true"/>
       <field name="workEffortTypeEnumId" type="id"/>
       <field name="statusId" type="id"/>  <!-- NOTE: statusId, NOT currentStatusId -->
       <field name="priority" type="number-integer"/>
       <field name="workEffortName" type="text-medium"/>
       <!-- ... more fields ... -->
   </entity>
   ```

3. **Validate Against Entity Schema:**
   - Confirm all fields in data records exist in entity definition
   - Verify field types match (id, text-medium, date-time, etc.)
   - Check required fields are included
   - Ensure primary key fields are properly set

### Entity Short-Alias Usage in Data Files

**CRITICAL: Data files can use either full entity names OR entity short-aliases defined in entity definitions.**

#### Valid Entity Reference Patterns

<entity_alias_usage>
  <full_entity_names>
    **Standard Full Entity Names:**
    ```xml
    <!-- Full entity name format -->
    <mantle.party.Party partyId="PARTY_001" partyTypeEnumId="PtyPerson"/>
    <mantle.party.Organization partyId="ORG_001" organizationName="Example Corp"/>
    <mantle.party.Person partyId="PERSON_001" firstName="John" lastName="Doe"/>
    <mantle.party.PartyRole partyId="PARTY_001" roleTypeId="Customer"/>
    ```
  </full_entity_names>
  
  <short_alias_names>
    **Short-Alias Entity Names (equally valid):**
    ```xml
    <!-- Using short-alias defined in entity tags -->
    <parties partyId="PARTY_001" partyTypeEnumId="PtyPerson"/>
    <organization partyId="ORG_001" organizationName="Example Corp"/>
    <person partyId="PERSON_001" firstName="John" lastName="Doe"/>
    <roles partyId="PARTY_001" roleTypeId="Customer"/>
    ```
  </short_alias_names>
  
  <relationship_aliases>
    **Relationship Short-Aliases (context-specific):**
    ```xml
    <!-- Within Party entity relationships, these aliases are valid: -->
    <fromRelationships partyId="PARTY_001" roleTypeId="Parent"/>
    <toRelationships partyId="PARTY_002" roleTypeId="Child"/>
    <identifications partyId="PARTY_001" partyIdTypeEnumId="PtyIdNationalId"/>
    <classifications partyId="PARTY_001" partyClassificationId="PC_001"/>
    ```
    
    **Note: Relationship short-aliases are ONLY valid within their specific relationship context**
  </relationship_aliases>
</entity_alias_usage>

#### Common Entity Short-Aliases Reference

<common_entity_aliases>
  **Mantle Party Entities:**
  - `mantle.party.Party` → `parties`
  - `mantle.party.Organization` → `organization`  
  - `mantle.party.Person` → `person`
  - `mantle.party.PartyRole` → `roles`
  - `mantle.party.PartyRelationship` → `relationships`
  - `mantle.party.PartyIdentification` → `identifications`
  - `mantle.party.PartyClassification` → `classifications`
  
  **Mantle Work Effort Entities:**
  - `mantle.work.effort.WorkEffort` → `efforts`
  - `mantle.work.effort.WorkEffortParty` → `parties` (context-specific)
  - `mantle.work.effort.WorkEffortProduct` → `products` (context-specific)
  
  **Note: Always verify short-alias names in entity definitions before use**
</common_entity_aliases>

#### Validation Requirements for Entity References

<entity_reference_validation>
  **MANDATORY Validation Steps:**
  
  1. **Verify Short-Alias Existence:**
     ```bash
     # Find entity definition and check for short-alias attribute
     grep -r "short-alias=" runtime/component/*/entity/
     ```
  
  2. **Validate in Entity Definition:**
     ```xml
     <!-- Example: Check entity definition -->
     <entity entity-name="Party" package="mantle.party" short-alias="parties">
         <!-- Entity fields... -->
     </entity>
     ```
  
  3. **Context-Specific Validation:**
     ```xml
     <!-- Relationship aliases only valid in relationship context -->
     <entity entity-name="Party" package="mantle.party">
         <relationship type="many" related="mantle.party.PartyRole" short-alias="roles">
             <!-- Only valid as <roles> within Party context -->
         </relationship>
     </entity>
     ```
  
  **CRITICAL: Both full entity names AND short-aliases are equally valid for data validation**
</entity_reference_validation>

### Common Data Validation Mistakes

**❌ CRITICAL ERRORS TO AVOID:**

1. **Incorrect Status Field Names:**
   ```xml
   <!-- WRONG: Using non-existent field name -->
   <mantle.work.effort.WorkEffort workEffortId="WE001" 
                                  currentStatusId="WeComplete"/>  <!-- FIELD DOESN'T EXIST -->
   
   <!-- CORRECT: Using actual field name -->
   <mantle.work.effort.WorkEffort workEffortId="WE001" 
                                  statusId="WeComplete"/>
   ```

2. **Non-Existent Status Values:**
   ```xml
   <!-- WRONG: Using non-existent status enumeration -->
   <mantle.work.effort.WorkEffort workEffortId="WE001" 
                                  statusId="WeCompleted"/>  <!-- STATUS DOESN'T EXIST -->
   
   <!-- CORRECT: Using actual status enumeration -->
   <mantle.work.effort.WorkEffort workEffortId="WE001" 
                                  statusId="WeComplete"/>
   ```

3. **Incorrect Entity References:**
   ```xml
   <!-- WRONG: Incorrect entity name -->
   <WorkEffort workEffortId="WE001"/>  <!-- INCOMPLETE ENTITY NAME -->
   
   <!-- CORRECT: Full entity name -->
   <mantle.work.effort.WorkEffort workEffortId="WE001"/>
   
   <!-- ALSO CORRECT: Using short-alias (if defined) -->
   <efforts workEffortId="WE001"/>  <!-- Using short-alias="efforts" -->
   ```

### Data Validation Checklist for moqui-data-specialist

**MANDATORY: Complete this checklist before creating any data files:**

<data_validation_checklist>
  <entity_verification>
    - [ ] Located and read all relevant entity definition files
    - [ ] Verified entity names match exactly (including package prefixes)
    - [ ] Validated entity short-aliases if using abbreviated entity names
    - [ ] Confirmed all field names exist in entity definitions
    - [ ] Validated field types match data being created
    - [ ] Checked primary key fields are included and unique
  </entity_verification>
  
  <field_validation>
    - [ ] Used `statusId` instead of `currentStatusId` 
    - [ ] Verified all status values exist in StatusItem enumerations
    - [ ] Checked for field name typos and case sensitivity
    - [ ] Validated date formats and field types
    - [ ] Ensured required fields are populated
  </field_validation>
  
  <reference_validation>
    - [ ] Verified all foreign key references exist or will be created
    - [ ] Checked status enumerations against existing seed data
    - [ ] Validated parent-child record creation order
    - [ ] Ensured data consistency across related entities
    - [ ] Confirmed no circular dependencies in data references
  </reference_validation>
  
  <data_structure>
    - [ ] Used proper XML structure with entity-facade-xml wrapper
    - [ ] Applied correct data type attribute (seed, demo, test, etc.)
    - [ ] Organized data records in logical dependency order
    - [ ] Included proper XML namespace and formatting
    - [ ] Validated XML syntax and structure
  </data_structure>
</data_validation_checklist>

### Entity Definition Research Commands

**Use these commands to verify entity structures:**

```bash
# Find entity definitions containing specific field names
grep -r "statusId" runtime/component/*/entity/ --include="*.xml"

# Find all WorkEffort-related entities
find runtime/component -name "*.xml" -path "*/entity/*" -exec grep -l "WorkEffort" {} \;

# Search for status enumerations
grep -r "WeComplete\|WeInProgress\|WeApproved" runtime/component/*/data/ --include="*.xml"

# Find entity with specific name
find runtime/component -name "*.xml" -path "*/entity/*" -exec grep -l "entity.*WorkEffort" {} \;
```

### Validation Error Prevention

**Before submitting any data file, the moqui-data-specialist MUST:**

1. **Read entity definitions** to confirm field names and types
2. **Verify status enumerations** exist in seed data or StatusItem records  
3. **Check foreign key references** are valid and will exist
4. **Validate XML structure** and syntax
5. **Test data loading** if possible before finalizing

**Remember: Entity field names and status values are case-sensitive and must match exactly. There is no room for assumptions - always verify against actual entity definitions.**

#### Dynamic Test Data Generation

**For Time-Sensitive Data:**
```groovy
private void generateTestCertificateAndCaf() {
    // Generate fresh certificates with current dates to avoid 6-month expiration issues
    Map certResult = ec.service.sync().name("mycompany.test.TestCertificateServices.generate#TestCertificate")
        .parameters([
            partyId: testIssuerPartyId,
            rut: "12345678-5", 
            validityDays: 365
        ]).call()
    
    // Create CAF with current date to ensure validity
    createTestCaf(certResult)
}
```

#### Test Data Best Practices

**1. Data Isolation:**
- Each component uses unique port ranges for Hazelcast and other services
- Test data isolation through specific data types loaded via `entity_empty_db_load`
- Components can use shared test infrastructure while maintaining test data separation

**2. Data Consistency:**
- Use shared test users (john.doe) across all components
- Consistent identifier formats and test organization patterns
- Standard test permissions and roles

**3. Data Maintenance:**
- Avoid hard-coded dates that expire
- Generate time-sensitive data dynamically in tests
- Use test services for certificate and CAF generation

**4. Data Organization:**
- Keep shared infrastructure in {utils-component}
- Keep domain-specific data in respective components
- Document data dependencies clearly

**5. Test Data Cleanup (Entity Deletion Order):**
- **CRITICAL**: When deleting Party entities in test cleanup, always delete child entities first
- **Required Order**: Delete Organization/Person before deleting Party
- **Reason**: Foreign key constraints prevent deletion of Party if child entities exist
- **Example Pattern**:
  ```groovy
  // ✅ CORRECT: Delete child entity first, then parent
  ec.service.sync().name("delete#mantle.party.Organization")
      .parameters([partyId: testPartyId]).disableAuthz().call()
  ec.service.sync().name("delete#mantle.party.Party")
      .parameters([partyId: testPartyId]).disableAuthz().call()
  
  // ❌ INCORRECT: Will fail with foreign key constraint violation
  ec.service.sync().name("delete#mantle.party.Party")
      .parameters([partyId: testPartyId]).disableAuthz().call()
  ec.service.sync().name("delete#mantle.party.Organization")
      .parameters([partyId: testPartyId]).disableAuthz().call()
  ```
- **Other Related Entities**: Also delete PartyIdentification, PartyContent, and other party-related data before deleting the Party itself

**6. Entity Relationship Deletion Limitations:**
- **CRITICAL**: Moqui entity definitions do NOT support cascade-delete functionality
- **No cascade-delete attribute**: Unlike some ORM frameworks, Moqui entities do not have a cascade-delete attribute or similar functionality
- **Manual deletion required**: All related record deletion must be handled explicitly in service logic or application code
- **Design implications**: When designing entity relationships, developers must plan for manual cleanup of related records
- **Database-level constraints**: Foreign key constraints are enforced at the database level, but cascade deletion is not automatically handled by the framework
- **Service patterns**: Use service composition or explicit deletion sequences to handle related record cleanup

**Example - No Cascade Delete Available:**
```xml
<!-- Moqui entity relationships do NOT support cascade delete -->
<entity entity-name="Order" package="mantle.order">
  <field name="orderId" type="id" is-pk="true"/>
  <field name="customerId" type="id"/>
  <!-- This relationship does NOT automatically delete Order when Customer is deleted -->
  <relationship type="one" related="mantle.party.Party">
    <key-map field-name="customerId" related="partyId"/>
  </relationship>
</entity>

<!-- Manual cleanup must be implemented in services -->
<service verb="delete" noun="Customer">
  <!-- Must explicitly delete or handle related orders first -->
  <actions>
    <!-- Handle related orders before deleting customer -->
    <entity-find entity-name="Order" list="orders">
      <econdition field-name="customerId" from="partyId"/>
    </entity-find>
    <iterate list="orders" entry="order">
      <service-call name="cancel#Order" parameters="[orderId: order.orderId]"/>
    </iterate>
    <!-- Then delete the customer -->
    <entity-delete-by-condition entity-name="mantle.party.Party">
      <econdition field-name="partyId"/>
    </entity-delete-by-condition>
  </actions>
</service>
```

### Component Testing Configuration Examples

**{component-1} Test Configuration:**
```gradle
// Database: Main H2 database (runtime/db/h2/moqui)
// Hazelcast: ports 5721-5740
// Data: seed,seed-initial,{l10n}-install,{project}-demo,{project}-test,{component-1}-test
```

**{component-2} Test Configuration:**
```gradle
// Database: Main H2 database (runtime/db/h2/moqui)
// Hazelcast: ports 5711-5730
// Data: seed,seed-initial,{l10n}-install,{project}-demo,{project}-test
```

**{utils-component} Test Configuration:**
```gradle
// Database: Main H2 database (runtime/db/h2/moqui)
// Hazelcast: ports 5701-5720
// Data: seed,seed-initial,{l10n}-install,{project}-demo,{project}-test,{utils-component}-test
```

This approach ensures proper test service isolation (via unique Hazelcast ports) while using the main database and shared test infrastructure that prevents duplication and test user conflicts across components.

## Service Definition Best Practices

### Service Message Localization

When implementing service messages:
- Use `ec.message.addMessage()` for informational messages
- Use `ec.message.addError()` for error messages
- Support parameterized messages with `ec.resource.expand()`
- Add corresponding LocalizedMessage entries for all user-facing messages

See `.agent-os/localization-guide.md` for detailed patterns.

## Service Definition Best Practices

**CRITICAL: Never use "id" as a type for service parameters**

The "id" type is ONLY valid for entity fields, NOT for service parameters. For service parameters that represent entity IDs, use "String" (or omit the type since String is the default).

```xml
<!-- ❌ INCORRECT: "id" type in service parameter -->
<service verb="get" noun="PartyInfo">
    <in-parameters>
        <parameter name="partyId" type="id" required="true"/>  <!-- WRONG! -->
    </in-parameters>
</service>

<!-- ✅ CORRECT: String type or omit type for ID parameters -->
<service verb="get" noun="PartyInfo">
    <in-parameters>
        <parameter name="partyId" required="true"/>  <!-- String is default -->
    </in-parameters>
</service>

<!-- ✅ CORRECT: "id" type in entity field -->
<entity entity-name="Party" package="mantle.party">
    <field name="partyId" type="id" is-pk="true"/>  <!-- Correct usage -->
</entity>
```

### Script-Type Services

When defining services of type "script" in Moqui, follow these critical guidelines:

1. **Script Location Parameter is Required**:
   ```xml
   <!-- ✅ CORRECT: Specify script location -->
   <service verb="generate" noun="TestCertificate" type="script"
            location="component://{component}/service/mycompany/test/TestCertificateServices.groovy">
       <in-parameters>
           <parameter name="partyId" required="true"/>
       </in-parameters>
   </service>
   
   <!-- ❌ INCORRECT: Missing location causes NullPointerException -->
   <service verb="generate" noun="TestCertificate" type="script">
       <actions>
           <!-- Script code here -->
       </actions>
   </service>
   ```

2. **Import Statements Must Be First**:
   ```xml
   <!-- ✅ CORRECT: Imports at the beginning of actions -->
   <actions>
       import mycompany.dte.DteUtils
       import mycompany.dte.provider.certification.DTE
       
       // Rest of the script code
       def utils = new DteUtils(ec)
   </actions>
   
   <!-- ❌ INCORRECT: Imports in the middle cause compilation errors -->
   <actions>
       def partyId = parameters.partyId
       import mycompany.dte.DteUtils  // Will fail
       def utils = new DteUtils(ec)
   </actions>
   ```

3. **Service Type Guidelines**:
   - **type="script"**: Requires `location` parameter pointing to a Groovy file
   - **type="inline"**: Use for inline code within `<actions>` tags
   - **type="entity-auto"**: For automatic CRUD operations
   - **type="interface"**: For defining service contracts (see below)

### Service Interface Pattern

Use `type="interface"` to define a service contract that other services must implement. The interface declares the required in/out parameters. Implementing services use `<implements service="..."/>` to inherit them.

```xml
<!-- Define the contract -->
<service verb="execute" noun="BatchProcess" type="interface">
    <in-parameters>
        <parameter name="batchSize" type="Integer" required="true"/>
        <parameter name="checkpoint" type="Map"/>
    </in-parameters>
    <out-parameters>
        <parameter name="processedCount" type="Integer" required="true"/>
        <parameter name="finished" type="Boolean" default="false"/>
    </out-parameters>
</service>

<!-- Implement the contract -->
<service verb="process" noun="MyDataBatch">
    <implements service="my.package.MyServices.execute#BatchProcess"/>
    <actions>
        <!-- Implementation here; all interface parameters are available -->
    </actions>
</service>
```

**When to use:**
- A scheduler/orchestrator calls different implementations with the same parameter contract
- Multiple services need a consistent signature (e.g., batch processors, event handlers)
- You want compile-time-like enforcement of parameter naming and types

### Common Service Definition Patterns

**External Script Service**:
```xml
<service verb="process" noun="Invoice" type="script"
         location="component://component-name/service/package/ProcessInvoice.groovy">
    <in-parameters>
        <parameter name="invoiceId" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="statusId"/>
    </out-parameters>
</service>
```

**Inline Service**:
```xml
<service verb="validate" noun="Document" type="inline">
    <in-parameters>
        <parameter name="documentId" required="true"/>
    </in-parameters>
    <actions>
        // No imports needed for simple operations
        def document = ec.entity.find("Document").condition("documentId", documentId).one()
        return [valid: document != null]
    </actions>
</service>
```

### Transaction Isolation for Batch Processing

When processing items in a loop (e.g., batch migrations, bulk imports), use `.requireNewTransaction(true)` on the service call so each item commits independently. Without this, a failure on item N rolls back all previous items in the same transaction.

```groovy
// Each batch runs in its own transaction — failures don't roll back prior batches
for (def item in itemList) {
    def result = ec.service.sync().name("my.Services.process#Item")
            .parameters([itemId: item.itemId])
            .requireNewTransaction(true)
            .call()
}
```

**When to use:**
- Batch/bulk processing where partial progress must be preserved
- Scheduler services processing items in a loop
- Any loop where one iteration's failure should not affect others

**When NOT to use:**
- Operations that must be atomic (all-or-nothing)
- Simple service calls outside of loops

**XML equivalent** for service definitions (not programmatic calls):
```xml
<service verb="process" noun="Item" transaction="force-new">
```

### forUpdate + requireNewTransaction Deadlock

**NEVER combine `forUpdate(true)` on a query with `requireNewTransaction(true)` on an update to the same row.** This creates a self-deadlock:

1. Outer TX acquires `FOR UPDATE` row lock via `forUpdate(true)`
2. Inner TX (from `requireNewTransaction(true)`) tries to `UPDATE` the same row
3. Inner TX waits for the outer TX's lock; outer TX waits for the inner `.call()` to return
4. Result: deadlock (PostgreSQL `55P03` lock timeout after waiting period)

```groovy
// WRONG: Self-deadlock — outer TX holds FOR UPDATE lock, inner TX can't acquire it
def record = ec.entity.find("my.Entity").condition("id", myId).forUpdate(true).one()
record.status = "Active"
ec.service.sync().name("update#my.Entity")
        .parameters(record.getMap()).requireNewTransaction(true).call()  // DEADLOCK

// CORRECT: Don't use forUpdate when updates use requireNewTransaction
def record = ec.entity.find("my.Entity").condition("id", myId).one()
record.status = "Active"
ec.service.sync().name("update#my.Entity")
        .parameters(record.getMap()).requireNewTransaction(true).call()  // OK
```

`forUpdate(true)` is only useful when the read and update happen in the **same** transaction. When updates use `requireNewTransaction(true)`, the lock from `forUpdate` is held by a different transaction and becomes counterproductive.

### Transaction Timeout Enforcement Is Lazy (Checkpoint-Based)

Moqui does **NOT** interrupt threads when a transaction timeout expires. Instead, the timeout is detected lazily at specific checkpoints:

| Checkpoint | Mechanism | Location |
|------------|-----------|----------|
| **Entity operations** | `EntityQueryBuilder` submits JDBC queries to a thread pool with `Future.get(remainingTimeoutMs)`. Throws `TimeoutException` if the query exceeds remaining time. | `EntityQueryBuilder.java:129-133` |
| **Service call entry** | `ServiceCallSyncImpl.callSingle()` checks `STATUS_MARKED_ROLLBACK` before executing. If the TX is marked for rollback, the service is **skipped** (returns null). | `ServiceCallSyncImpl.java:141-150` |
| **Transaction commit** | `TransactionFacadeImpl.commit()` detects `STATUS_MARKED_ROLLBACK` and calls `rollback()` instead of `commit()`. | `TransactionFacadeImpl.groovy:391-413` |

**What is NOT protected:**
- `Thread.sleep()` — timeout is invisible during sleep
- External HTTP calls — unless the HTTP client has its own connect/read timeout
- File I/O operations — no timeout enforcement
- Any computation that doesn't touch entities or call services

**Practical implication:** Setting `transactionTimeout` on a service call bounds entity operations but does NOT guarantee the service will complete within that time. Services performing external I/O **must** configure their own timeouts (HTTP client timeouts, socket timeouts, etc.).

**How the timeout propagates:**
1. `ut.setTransactionTimeout(timeout)` is called when beginning the transaction
2. The JTA TransactionManager marks the TX as `STATUS_MARKED_ROLLBACK` when the timeout expires
3. Moqui code detects this at the next checkpoint (entity op, service call, or commit)

```groovy
// Entity operations are protected — will timeout if TX expires
def result = ec.entity.find("SomeEntity").condition("id", id).one()  // protected

// External I/O is NOT protected — must manage own timeout
def connection = new URL(url).openConnection()
connection.connectTimeout = 10000  // YOU must set this
connection.readTimeout = 30000     // YOU must set this
```

## Framework Caveats and Known Issues

### CSRF Token Blocks Anonymous REST POST Requests

**Issue**: Setting `require-authentication="anonymous-all"` on a REST API resource does NOT bypass CSRF token validation for POST requests. The CSRF check happens at the screen transition level, before REST API routing resolves the resource's authentication setting.

**Framework Locations**:
- CSRF check: `framework/src/main/groovy/org/moqui/impl/screen/ScreenRenderImpl.groovy:429-444`
- Session token default: `framework/src/main/groovy/org/moqui/impl/screen/ScreenDefinition.groovy:834`
- Authenticated bypass: `framework/src/main/groovy/org/moqui/impl/context/UserFacadeImpl.groovy:689`

**Request flow for `POST /rest/s1/{api}/{resource}`**:
1. `MoquiServlet.service()` initializes the request and calls `ScreenRenderImpl.render()`
2. `ScreenRenderImpl` resolves the `s1` screen transition in `rest.xml`
3. **CSRF check fires** (line 429) because `s1` transition has `requireSessionToken=true` (the default)
4. If CSRF fails, `AuthenticationRequiredException` is thrown — request never reaches step 5
5. `handleServiceRestCall()` would evaluate `require-authentication` on the REST resource — but it's too late

**The CSRF check has five conditions (all must be true to enforce)**:
```groovy
if (request.getMethod().toLowerCase() != "get" &&           // POST/PUT/PATCH/DELETE
        webappInfo != null && webappInfo.requireSessionToken &&   // webapp-level setting
        targetTransition.getRequireSessionToken() &&              // transition-level (default: true)
        !"true".equals(request.getAttribute("moqui.session.token.created")) && // not a new session
        !"true".equals(request.getAttribute("moqui.request.authenticated")))   // not authenticated
```

**Key insight**: `moqui.request.authenticated` is set to `"true"` in `UserFacadeImpl:689` after successful login via Basic Auth, API key, or form login. This bypasses the CSRF check for all authenticated requests.

**Solutions for anonymous POST endpoints (e.g., health probes, webhooks)**:

1. **Use Basic Auth or API key** (recommended for REST endpoints):
   - Create a system user and authenticate with `-u username:password` or `api_key` header
   - Authentication sets `moqui.request.authenticated=true`, bypassing CSRF
   - The REST resource can still use `require-authentication="anonymous-all"` for authz bypass
   ```bash
   curl -X POST -u _probe:password http://localhost:8080/rest/s1/myapi/endpoint \
     -H "Content-Type: application/json" -d '{...}'
   ```

2. **Use a dedicated screen transition** with `require-session-token="false"`:
   - Like the `login` and `sm` transitions in `rest.xml` (lines 32, 392)
   - Only appropriate for truly public endpoints (system messages, login)

**Common mistake**: Adding `authenticate="anonymous-all"` to the service definition and `require-authentication="anonymous-all"` to the REST resource, then expecting anonymous POST to work. Neither attribute affects the CSRF check — authentication must happen first.

### Transition Service-Call Multi-Mode Processing

**Issue**: Direct service-call elements in transitions automatically get `multi="parameter"` processing, which can interfere with services that expect List parameters directly.

**Framework Location**: `framework/src/main/groovy/org/moqui/impl/screen/ScreenDefinition.groovy:860-861`

**Manifestation**: When using multi-row selection actions (row-selection with `multi="true"`), a direct service-call in a transition may fail if:
1. The service expects List parameters (like `envioIdList`)
2. The transition has a direct service-call (not wrapped in actions)
3. The framework automatically adds `multi="parameter"` which interferes with parameter processing

**Root Cause**: The framework automatically modifies direct service-call elements:
```groovy
if (!callServiceNode.attribute("multi") && !"true".equals(callServiceNode.attribute("async")))
    callServiceNode.attributes.put("multi", "parameter")
```

This causes the service to check `ec.web?.requestParameters?._isMulti == "true"` and attempt multi-row processing when `_isMulti=true` is present in multi-row selection requests.

**Solutions**:

1. **Wrap in actions tag** (recommended):
```xml
<!-- ✅ PREFERRED: Wrap service-call in actions to prevent auto multi="parameter" -->
<transition name="processEnvio">
    <actions>
        <service-call name="mycompany.dte.DteEnvioProcessingServices.process#PendingEnvioDte" 
                      in-map="context" out-map="context" disable-authz="true"/>
    </actions>
    <default-response url="." parameter-map="[envioId:envioId]"/>
</transition>
```

2. **Explicit multi="false"**:
```xml
<!-- ✅ ALTERNATIVE: Explicitly disable multi processing -->
<transition name="processEnvio">
    <service-call name="mycompany.dte.DteEnvioProcessingServices.process#PendingEnvioDte" 
                  in-map="context" out-map="context" disable-authz="true" multi="false"/>
    <default-response url="." parameter-map="[envioId:envioId]"/>
</transition>
```

**When This Occurs**:
- Multi-row selection actions in form-lists (`<row-selection>` with form submissions)
- Services that expect List parameters directly rather than multi-row processing
- Transitions with direct service-call elements (not wrapped in actions)

**Framework Improvement Opportunity**: The framework could be enhanced to detect when services expect List parameters and avoid automatically adding `multi="parameter"` in those cases.

### File Download Transitions in Quasar Apps

**Issue**: File downloads don't work when accessed through `/qapps/` (Quasar SPA) because the SPA intercepts all requests as AJAX and doesn't handle binary file responses.

**Solution**: Use `/apps/` path directly for file download links to bypass the SPA.

**Pattern for File Download Links**:
```xml
<!-- ✅ CORRECT: Use /apps/ path for file downloads in Quasar screens -->
<link url="/apps/Path/To/Screen/downloadTransition"
      text="Download PDF" btn-type="primary"
      parameter-map="[entityId:entityId, format:'pdf']"
      url-type="plain" link-type="anchor"/>

<!-- ❌ WRONG: Relative URLs go through /qapps/ and fail for file downloads -->
<link url="downloadTransition" text="Download PDF"
      parameter-map="[entityId:entityId]"/>
```

**Pattern for File Download Transitions**:
```xml
<transition name="downloadFile" read-only="true">
    <actions>
        <!-- Call service that returns file content -->
        <service-call name="...Services.generate#File"
                      in-map="context" out-map="result"/>

        <!-- Handle errors -->
        <if condition="ec.message.hasError()">
            <script>ec.web.sendError(500, ec.message.getErrorsString(), null)</script>
        <else>
            <!-- Write file to response -->
            <script><![CDATA[
                def response = ec.web.response
                response.setContentType(result.contentType ?: "application/octet-stream")
                response.setHeader("Content-Disposition",
                    "attachment; filename=\"${result.filename ?: 'download'}\"")

                def content = result.fileContent
                if (content instanceof byte[]) {
                    response.setContentLength(content.length)
                    response.outputStream.write(content)
                } else if (content != null) {
                    byte[] bytes = content.toString().getBytes("UTF-8")
                    response.setContentLength(bytes.length)
                    response.outputStream.write(bytes)
                }
                response.outputStream.flush()
            ]]></script>
        </else></if>
    </actions>
    <default-response type="none"/>
</transition>
```

**Key Points**:
1. Use `read-only="true"` to allow GET requests with URL parameters
2. Use `<actions>` block (not direct `<service-call>`) for response control
3. Use `<default-response type="none"/>` to prevent redirect after file is sent
4. Service returns content/bytes; transition handles HTTP response
5. For stored files, use `ec.web.sendResourceResponse(location)` instead

**Reference**: See your project's admin screen file download transitions for production examples.

### Jsoup.parse() Groovy Method Overloading Gotcha

**Issue**: `Jsoup.parse(html, null)` fails in Groovy with "Ambiguous method overloading" error.

**Root Cause**: When the second parameter is `null`, Groovy cannot determine which overloaded method to call:
- `parse(String html, String baseUri)`
- `parse(String html, Parser parser)`

**Error Message**:
```
groovy.lang.GroovyRuntimeException: Ambiguous method overloading for method org.jsoup.Jsoup#parse.
Cannot resolve which method to invoke for [class java.lang.String, null] due to overlapping prototypes
```

**Solution**: Use Elvis operator to provide empty string default:
```groovy
// ✅ CORRECT: Provide empty string when baseUrl might be null
org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html, baseUrl ?: "")

// ❌ WRONG: Null causes ambiguous method error
org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(html, baseUrl)
```

**Applies To**: Any Groovy code using Jsoup for HTML parsing, particularly in PDF generation services.

### Groovy 3+ `?[` Safe Indexing Operator Ambiguity

**Issue**: Expressions like `value?[list]:fallback` fail with `MultipleCompilationErrorsException` in Groovy 3+.

**Root Cause**: Groovy 3.0 introduced the **safe indexing operator** `?[]` (null-safe version of `[]`). The tokenizer now parses `?[` as a single token, so `value?[x,'y']:fallback` is interpreted as a safe-index access instead of a ternary conditional followed by a list literal.

```groovy
// Groovy 2.x: parsed as ternary ? with list literal [...]
activeOrgId?[activeOrgId,'_NA_']:null

// Groovy 3+: parsed as safe-index operator ?[...] — SYNTAX ERROR
// Groovy reads: activeOrgId?.[activeOrgId,'_NA_']  :null  ← invalid
```

**Solution**: Always use spaces around the ternary conditional operator when the true-branch starts with `[`:

```groovy
// CORRECT: spaces disambiguate the ternary operator
activeOrgId ? [activeOrgId,'_NA_'] : null

// WRONG: ?[ is tokenized as safe-index operator
activeOrgId?[activeOrgId,'_NA_']:null
```

**Applies To**: Any Groovy expression where a ternary's true-branch is a list/map literal `[...]`. Most commonly found in EntityFilter `filterMap` attributes, but can occur in any `from=` or `<set field=...>` expression.

### Java 21 XSLT BigDecimal Compatibility

**Issue**: When upgrading from Java 17 to Java 21, XSLT transformations using `format-number()` or `number()` functions fail with:
```
java.lang.RuntimeException: Invalid argument type 'java.math.BigDecimal' in call to 'number()'
```

**Root Cause**: Java 21's XSLTC (compiled XSLT processor) has stricter type checking and no longer accepts `BigDecimal` objects directly in XSLT numeric functions.

**Solution**: Convert BigDecimal values to Double before passing to XSLT transform parameters:

**Before (fails in Java 21)**:
```xml
<set field="transformParameters" from="[amount:entityValue.amount, percentComplete:entityValue.percentComplete]"/>
```

**After (works in Java 21)**:
```xml
<set field="transformParameters" from="[amount:entityValue.amount?.doubleValue(), percentComplete:entityValue.percentComplete?.doubleValue()]"/>
```

For integer values (like month numbers or counts):
```xml
<set field="transformParameters.monthNumber" from="entityValue.monthNumber?.intValue()"/>
```

**Affected Code**: Any service using `ec.resource.xslFoTransform()` that passes BigDecimal values in transform parameters.

**Common Symptoms**:
- PDF generation fails after upgrading to Java 21
- XSL-FO transformations throw type errors
- Works in Java 17/v3 but fails in Java 21/v4

### ExecutionContext Factory API: Component Enumeration

The `ec.factory` object provides access to Moqui's `ExecutionContextFactory`, which exposes runtime introspection APIs that are not widely documented.

**Enumerating loaded components:**
```groovy
// Get all loaded component base locations (returns Map<String, ResourceReference>)
def componentLocations = ec.factory.getComponentBaseLocations()

// Iterate components
componentLocations.each { componentName, resourceRef ->
    String path = resourceRef.getLocation()  // e.g., "runtime/component/{shared-component}"
    // Use path for further introspection
}
```

**Common use cases:**
- Runtime component discovery for configuration snapshots
- Build-time component version tracking (combine with git HEAD parsing)
- Dynamic feature detection based on which components are loaded

**Related APIs on `ec.factory`:**
- `getComponentBaseLocations()` - Map of component names to their ResourceReference locations
- `getRuntimePath()` - Runtime directory absolute path
- `getConfPath()` - Configuration directory path

**Note**: These APIs are available in service `<script>` blocks and Groovy test code where `ec` is in scope. They are NOT available in XML DSL actions.

### ToolFactory Lifecycle and Self-Bootstrapping

Tool factories (`ToolFactory<T>`) are initialized via `init(ExecutionContextFactory ecf)` during framework startup, ordered by their `init-priority` attribute in `MoquiConf.xml`:

```xml
<tools>
    <tool-factory class="com.example.MyToolFactory" init-priority="35"/>
</tools>
```

**Startup ordering — what is and isn't available during `init()`:**

| Available | Not Available |
|-----------|---------------|
| Entity facade (tables exist, CRUD works) | User context (no authenticated user) |
| Service facade (can call services) | Seed data records (StatusItem, Enumeration, etc.) |
| Resource facade (file access) | Other tool factories with higher priority numbers |
| Component locations (`getComponentBaseLocations()`) | Web/servlet context |

**Critical: Seed data is NOT automatically loaded on existing databases.** The framework only triggers automatic seed data loading when the database is empty (no `Enumeration` records exist). On an existing database, a tool factory's own seed data will never be loaded unless the tool factory bootstraps it.

#### Authorization in ToolFactory init()

Since no user is authenticated during `init()`, entity operations will fail artifact authorization checks. Wrap entity operations in the standard `disableAuthz` pattern:

```groovy
ExecutionContext ec = ecf.getExecutionContext()
boolean alreadyDisabled = ec.getArtifactExecution().disableAuthz()
try {
    // Entity operations here (finds, data loading, etc.)
} finally {
    if (!alreadyDisabled) ec.getArtifactExecution().enableAuthz()
}
```

#### DataFeed Suppression in ToolFactory init()

Entity updates performed during `init()` can trigger real-time DataFeed indexing (`DTFDTP_RT_PUSH`) on a background worker thread. If the ElasticSearch/OpenSearch ToolFactory has not yet been initialized (due to init-priority ordering), the `ElasticFacade` will be null and the indexing will fail with an NPE.

**Disable DataFeed when modifying entities that have real-time feeds configured** (e.g., Product, Order, Party):

```groovy
ExecutionContext ec = ecf.getExecutionContext()
boolean reenableDataFeed = !ec.getArtifactExecution().disableEntityDataFeed()
try {
    // Entity updates here — DataFeed triggers are suppressed
} finally {
    if (reenableDataFeed) ec.getArtifactExecution().enableEntityDataFeed()
}
```

This uses the same mechanism the framework's `EntityDataLoader` uses internally (see also `standards/backend/search-indexing.md` § Disabling DataFeed).

**When to use**: Any ToolFactory `init()` that performs `entity-update` or `entity-delete` on entities associated with a real-time DataFeed (DataDocuments like `MantleProduct`, `MantleOrder`, etc.).

#### Self-Bootstrapping Pattern

When a tool factory depends on its own seed data (e.g., StatusItems), it must check and load that data during `init()`. Use a guard check to avoid log noise on subsequent startups:

```groovy
// Check if seed data already exists (single fast query)
boolean seedLoaded = ecf.entity.find("moqui.basic.StatusItem")
        .condition("statusId", "MyStatusId").one() != null
if (!seedLoaded) {
    String compLocation = ecfi.getComponentBaseLocations().get("my-component")
    String seedPath = compLocation + "/data/MySeedData.xml"
    ResourceReference rr = ecfi.getResource().getLocationReference(seedPath)
    if (rr != null && rr.getExists()) {
        ecf.entity.makeDataLoader().location(seedPath).onlyCreate(true).load()
    }
}
```

#### Targeted Data Loading with location()

When loading specific data files programmatically, prefer `location()` over `dataTypes()`:

```groovy
// PREFERRED: Load a specific file directly
ecf.entity.makeDataLoader().location(specificFilePath).onlyCreate(true).load()

// AVOID: Scans ALL data files in ALL components just to filter by type
ecf.entity.makeDataLoader().dataTypes(new HashSet<String>(["mytype"])).onlyCreate(true).load()
```

The `dataTypes()` approach iterates every data file across every component (potentially 30+ files), opening each one to check its `type` attribute. Use `location()` combined with `getComponentBaseLocations()` to load only the files you need:

```groovy
for (Map.Entry<String, String> comp in ecfi.getComponentBaseLocations().entrySet()) {
    String filePath = comp.getValue() + "/data/MyRegistryFile.xml"
    ResourceReference rr = ecfi.getResource().getLocationReference(filePath)
    if (rr != null && rr.getExists()) {
        ecf.entity.makeDataLoader().location(filePath).onlyCreate(true).load()
    }
}
```

## Important Notes

- Always run `./gradlew getRuntime` first if the runtime directory doesn't exist
- The system requires Java 8+ and Gradle
- Components are downloaded from git repositories or zip archives as configured
- **Database schema is managed automatically through entity definitions - never use direct SQL**
- **Use entity auto-services (`create#`, `update#`, `delete#`) for basic CRUD operations**
- **Script-type services require a location parameter; never include script code in actions**
- **Place all import statements at the beginning of action blocks**
- **Reuse framework build functions rather than reimplementing complex logic**
- **Use shared test infrastructure ({project}-test data type) for consistent test user accounts**
- **Isolate test databases and ports per component to prevent conflicts**
- **Prefer Moqui's built-in caching over custom caching implementations** (see `.agent-os/moqui-caching-best-practices.md`)
- **Prefer XML DSL over script tags** in service definitions for maintainable, declarative logic
- **Use correct if/then/else structure** in XML DSL (else tags must be nested inside if tags with then tags)
- **Use set tags instead of script blocks** for simple field assignments and variable updates
- **CRITICAL: Always use complete service paths** - Never use shorthand patterns like `create#Entity`; always use full `namespace.ServiceFile.verb#noun` format
- **CRITICAL: Prefer in-map over field-map** for service-call parameters (use field-map only for name transformation, mixed sources, or type conversion)
- **Use 180-character line limit rule** for XML and code formatting decisions
- **Be aware of automatic multi="parameter" behavior** in transition service-calls with multi-row selections
- Logs are in `runtime/log/` directory
- Code style guidelines are defined in the agent-os code style configuration
- Commit message standards are defined in the agent-os best practices configuration

## Screen Widget Best Practices

**📋 CRITICAL REFERENCE**: For comprehensive syntax validation, see `.agent-os/moqui-screen-references/syntax-anti-patterns.md` - this guide catalogs common syntax errors including recently encountered agent mistakes: invalid attributes (`use-iterator="true"`, `show="${condition}"` in header-field), improper tag usage (`<limit from="X"/>` instead of `limit="X"` attribute), and missing field selections in form-list entity-find operations. **ALL SCREEN SPECIALISTS MUST REVIEW THE "RECENTLY ENCOUNTERED ERRORS" SECTION** before generating any screen code.

### Form-List Mandatory list Attribute

**❌ CRITICAL REQUIREMENT**: Form-list MUST ALWAYS have a `list` attribute, even when entity-find is inside the form-list tag.

```xml
<!-- ❌ INCORRECT: Missing mandatory list attribute -->
<form-list name="UserLoginKeyList" list-entry="userLoginKeyView" skip-form="true">
    <entity-find entity-name="mycompany.myapp.api.UserLoginKeyView" list="userLoginKeyViewList" limit="20">
        <econdition field-name="statusId" value="ACTIVE"/>
    </entity-find>
    <!-- fields -->
</form-list>

<!-- ✅ CORRECT: Has list attribute pointing to entity-find list -->
<form-list name="UserLoginKeyList" list="userLoginKeyViewList" list-entry="userLoginKeyView" skip-form="true">
    <entity-find entity-name="mycompany.myapp.api.UserLoginKeyView" list="userLoginKeyViewList" limit="20">
        <econdition field-name="statusId" value="ACTIVE"/>
        <select-field field-name="keyId"/>
        <select-field field-name="statusId"/>
    </entity-find>
    <!-- fields -->
</form-list>
```

**EXPLANATION**: The `list` attribute on form-list is MANDATORY and must reference the same list name used in the entity-find `list` attribute. The `list-entry` attribute defines the variable name for each row item.

### Form-List Entity-Find Field Selection

**CRITICAL: When entity-find is inside a form-list tag, only the fields explicitly added to the form-list will be fetched from the database.**

If you need to use additional fields (e.g., in links, row-actions, or any other part of the form-list), you MUST explicitly add them using the `select-field` tag within the entity-find.

```xml
<!-- ❌ INCORRECT: Missing fields for row-actions -->
<form-list name="PartyList" list="partyList" skip-form="true">
    <entity-find entity-name="mantle.party.Party" list="partyList">
        <econdition field-name="disabled" operator="not-equals" value="Y"/>
        <order-by field-name="organizationName,firstName,lastName"/>
        <!-- Missing select-field tags for fields used in row-actions -->
    </entity-find>
    
    <field name="organizationName"><default-field><display/></default-field></field>
    <field name="firstName"><default-field><display/></default-field></field>
    
    <field name="actions"><default-field>
        <!-- ERROR: partyId is not fetched because it's not in form-list fields -->
        <link url="editParty" text="Edit" parameter-map="[partyId:partyId]"/>
        <!-- ERROR: partyTypeEnumId is not fetched -->
        <link url="viewDetails" text="Details" condition="partyTypeEnumId == 'PtyOrganization'"/>
    </default-field></field>
</form-list>
```

```xml
<!-- ✅ CORRECT: Explicitly select fields needed for row-actions -->
<form-list name="PartyList" list="partyList" skip-form="true">
    <entity-find entity-name="mantle.party.Party" list="partyList">
        <econdition field-name="disabled" operator="not-equals" value="Y"/>
        <order-by field-name="organizationName,firstName,lastName"/>
        <!-- Explicitly select fields needed for row-actions -->
        <select-field field-name="partyId"/>
        <select-field field-name="partyTypeEnumId"/>
    </entity-find>
    
    <field name="organizationName"><default-field><display/></default-field></field>
    <field name="firstName"><default-field><display/></default-field></field>
    
    <field name="actions"><default-field>
        <!-- Now partyId is available -->
        <link url="editParty" text="Edit" parameter-map="[partyId:partyId]"/>
        <!-- Now partyTypeEnumId is available -->
        <link url="viewDetails" text="Details" condition="partyTypeEnumId == 'PtyOrganization'"/>
    </default-field></field>
</form-list>
```

**Why This Happens:**
- Moqui optimizes form-list queries by only selecting fields that are displayed
- Fields used in conditions, links, or row-actions are not automatically included
- This optimization reduces database load and improves performance

**Common Scenarios Requiring select-field:**
- **Row Actions**: Links that use entity IDs or status fields for parameters
- **Conditional Display**: Fields used in condition expressions for links or display logic
- **Dynamic Styling**: Fields referenced in depends-on attributes or conditional formatting
- **JavaScript Integration**: Fields accessed by client-side code or AJAX calls
- **Row-Actions Computed Fields**: View-entity fields used in `row-actions` to compute display values (see below)

#### Row-Actions Fields on View Entities Are Not Auto-Selected

**CRITICAL: When a `form-list` entity-find targets a view-entity, Moqui only SELECTs columns whose names match form field names. Fields used only in `row-actions` (to compute display values) are NOT automatically selected, resulting in null values at runtime.**

This is a subtle variant of the general field selection behavior: the optimization matches SQL SELECT columns against the `<field name="...">` declarations in the form-list. If a view-entity alias is only referenced inside `<row-actions>` (and no form field shares that exact name), the column is omitted from the query.

```xml
<!-- PROBLEM: processedItems, totalItems, batchSize are view-entity aliases used
     only in row-actions to compute "progress", "rate", "batchConfig" display fields.
     Since no form field is named "processedItems" etc., they are NOT selected. -->
<form-list name="JobList" list="jobList" skip-form="true">
    <entity-find entity-name="mycompany.myapp.JobDetailView" list="jobList">
        <econdition field-name="statusId" value="JobActive"/>
        <!-- bgStatusId IS selected because a form field has that name -->
    </entity-find>
    <row-actions>
        <!-- These fields are NULL because they were not selected! -->
        <set field="progress" from="totalItems ? (processedItems / totalItems * 100) : 0"/>
        <set field="rate" from="processedItems / elapsed"/>
        <set field="batchConfig" from="'Batch size: ' + batchSize"/>
    </row-actions>
    <field name="bgStatusId"><default-field title="Status"><display/></default-field></field>
    <field name="progress"><default-field title="Progress"><display/></default-field></field>
    <field name="rate"><default-field title="Rate"><display/></default-field></field>
    <field name="batchConfig"><default-field title="Config"><display/></default-field></field>
</form-list>
```

```xml
<!-- FIX: Add explicit select-field for all view-entity fields needed in row-actions -->
<form-list name="JobList" list="jobList" skip-form="true">
    <entity-find entity-name="mycompany.myapp.JobDetailView" list="jobList">
        <econdition field-name="statusId" value="JobActive"/>
        <!-- select-field needed for fields used in row-actions but not matching a form field name -->
        <select-field field-name="processedItems,totalItems,batchSize"/>
    </entity-find>
    <row-actions>
        <!-- Now these fields have values -->
        <set field="progress" from="totalItems ? (processedItems / totalItems * 100) : 0"/>
        <set field="rate" from="processedItems / elapsed"/>
        <set field="batchConfig" from="'Batch size: ' + batchSize"/>
    </row-actions>
    <field name="bgStatusId"><default-field title="Status"><display/></default-field></field>
    <field name="progress"><default-field title="Progress"><display/></default-field></field>
    <field name="rate"><default-field title="Rate"><display/></default-field></field>
    <field name="batchConfig"><default-field title="Config"><display/></default-field></field>
</form-list>
```

**Key Distinction:**
- `bgStatusId` IS auto-selected because a form field is named `bgStatusId`
- `processedItems` is NOT auto-selected because no form field has that name (it is used in `row-actions` to compute `progress`)
- This only affects **view entities** in form-list entity-finds. Regular (non-view) entities and `entity-find-one` always return all fields
- **Rule of thumb**: If a view-entity field is consumed in `row-actions` but has no matching form field name, add it to `select-field`

## XML DSL vs Script Tags Best Practice

**Prefer XML DSL over script tags** for service logic whenever possible. Moqui's XML DSL provides declarative, maintainable alternatives to imperative Groovy scripts.

### ✅ PREFERRED: XML DSL Approach
```xml
<service verb="check" noun="FolioAvailability">
    <actions>
        <!-- Initialize output parameters -->
        <set field="totalAvailableFolios" value="0" type="Integer"/>
        <set field="folioBreakdown" from="[]"/>
        
        <!-- Find all CAFs for this party and document type -->
        <entity-find entity-name="mycompany.dte.Caf" list="allCafs">
            <econdition field-name="issuerPartyId" from="partyId"/>
            <econdition field-name="fiscalTaxDocumentTypeEnumId" from="fiscalTaxDocumentTypeEnumId"/>
            <order-by field-name="fechaAutorizacion"/>
        </entity-find>
        
        <!-- Process each CAF -->
        <iterate list="allCafs" entry="caf">
            <set field="isActive" from="caf.activo == 'Y'"/>
            
            <if condition="isActive">
                <!-- Count used folios in this CAF range -->
                <entity-find-count entity-name="mycompany.dte.FiscalTaxDocument" count-field="usedFoliosCount">
                    <econdition field-name="issuerPartyId" from="partyId"/>
                    <econdition field-name="fiscalTaxDocumentNumber" operator="greater-equals" from="caf.desde"/>
                    <econdition field-name="fiscalTaxDocumentNumber" operator="less-equals" from="caf.hasta"/>
                    <econdition field-name="statusId" operator="not-equals" value="Ftd-NotIssued"/>
                </entity-find-count>
                
                <set field="availableFolios" from="(caf.hasta - caf.desde + 1) - usedFoliosCount"/>
                <set field="totalAvailableFolios" from="totalAvailableFolios + availableFolios"/>
                
                <!-- Only use script for complex list operations that can't be done in XML -->
                <script>folioBreakdown.add([cafId: caf.cafId, availableFolios: availableFolios])</script>
            </if>
        </iterate>
        
        <log message="Calculation complete: ${totalAvailableFolios} total available folios"/>
    </actions>
</service>
```

### XML DSL Benefits
- **Declarative**: Logic is expressed as configuration rather than imperative code
- **Maintainable**: Easier to read and modify without Groovy knowledge  
- **Framework Integration**: Better integration with Moqui's caching and optimization
- **Error Handling**: Framework provides automatic error handling and logging
- **Performance**: Framework can optimize XML DSL operations better than scripts

### When to Use Script Tags
- Complex list operations that can't be expressed in XML (like `list.add()`)
- Mathematical calculations requiring precise type handling
- Custom object instantiation and method calls
- Complex conditional logic that doesn't fit XML DSL patterns

**❌ AVOID: Script blocks for simple assignments**
```xml
<!-- DON'T DO THIS -->
<script><![CDATA[
    configResult.currentAvailable = currentAvailable
    configResult.activeCafCount = availabilityResult.activeCafCount ?: 0
    configResult.hasExpiredCafs = availabilityResult.hasExpiredCafs ?: false
]]></script>
```

**✅ PREFERRED: XML DSL set tags**
```xml
<!-- DO THIS INSTEAD -->
<set field="configResult.currentAvailable" from="currentAvailable"/>
<set field="configResult.activeCafCount" from="availabilityResult.activeCafCount ?: 0"/>
<set field="configResult.hasExpiredCafs" from="availabilityResult.hasExpiredCafs ?: false"/>
```

**Benefits of using XML DSL set tags:**
- **Better IDE support**: Syntax highlighting and validation
- **Framework optimization**: Moqui can optimize XML DSL operations
- **Readability**: Clearer intent and structure
- **Consistency**: Follows Moqui declarative patterns

### When Script Tags Should NOT Be Used

**CRITICAL: Avoid script tags for operations that have XML DSL equivalents.** Most service logic can be expressed declaratively using Moqui's XML DSL, which provides better maintainability, performance, and framework integration.

#### ❌ AVOID Script Tags For:

**1. Simple Variable Assignment** - Use `<set>` tags instead
```xml
<!-- ❌ WRONG: Script for assignments -->
<script><![CDATA[
    result = "success"
    isValid = true
    totalAmount = 0
]]></script>

<!-- ✅ CORRECT: XML DSL set tags -->
<set field="result" value="success"/>
<set field="isValid" value="true" type="Boolean"/>
<set field="totalAmount" value="0" type="BigDecimal"/>
```

**2. Entity Operations** - Use entity-find tags instead
```xml
<!-- ❌ WRONG: Script for entity queries -->
<script><![CDATA[
    EntityList productList = ec.entity.find("Product")
        .condition("productTypeId", "FINISHED_GOOD")
        .list()
    productCount = productList.size()
]]></script>

<!-- ✅ CORRECT: XML DSL entity operations -->
<entity-find entity-name="Product" list="productList">
    <econdition field-name="productTypeId" value="FINISHED_GOOD"/>
</entity-find>
<set field="productCount" from="productList.size()"/>
```

**3. Service Calls** - Use `<service-call>` tags instead
```xml
<!-- ❌ WRONG: Script for service calls -->
<script><![CDATA[
    Map callResult = ec.service.sync().name("create#Product")
        .parameter("productName", "Sample Product")
        .parameter("price", 99.99)
        .call()
    productId = callResult.productId
]]></script>

<!-- ✅ CORRECT: XML DSL service calls -->
<service-call name="mantle.product.ProductServices.create#Product" out-map="callResult">
    <field-map field-name="productName" value="Sample Product"/>
    <field-map field-name="price" from="99.99"/>
</service-call>
<set field="productId" from="callResult.productId"/>
```

**4. Conditional Logic** - Use `<if>/<then>/<else>` tags instead
```xml
<!-- ❌ WRONG: Script for conditionals -->
<script><![CDATA[
    if (orderTotal > 100) {
        discountRate = 0.10
        message = "Discount applied"
    } else {
        discountRate = 0.0
        message = "No discount"
    }
]]></script>

<!-- ✅ CORRECT: XML DSL conditional logic -->
<if condition="orderTotal > 100">
    <then>
        <set field="discountRate" from="0.10"/>
        <set field="message" value="Discount applied"/>
    </then>
    <else>
        <set field="discountRate" from="0.0"/>
        <set field="message" value="No discount"/>
    </else>
</if>
```

**5. Error Messages** - Use `<message>` tags instead
```xml
<!-- ❌ WRONG: Script for error handling -->
<script><![CDATA[
    if (!customerId) {
        ec.message.addError("Customer ID is required")
        return
    }
]]></script>

<!-- ✅ CORRECT: XML DSL message handling -->
<if condition="!customerId">
    <message error="true">Customer ID is required</message>
    <return/>
</if>
```

**6. Parameter Validation** - Use `<if>` conditions with `<return>` instead
```xml
<!-- ❌ WRONG: Script for validation -->
<script><![CDATA[
    if (!partyId || !documentType) {
        ec.message.addError("Required parameters missing")
        return
    }
]]></script>

<!-- ✅ CORRECT: XML DSL validation -->
<if condition="!partyId || !documentType">
    <message error="true">Required parameters missing</message>
    <return/>
</if>
```

**7. Basic String Operations** - Use expression syntax in XML attributes instead
```xml
<!-- ❌ WRONG: Script for string operations -->
<script><![CDATA[
    fullName = firstName + " " + lastName
    upperCaseCode = productCode.toUpperCase()
]]></script>

<!-- ✅ CORRECT: Expression syntax in XML -->
<set field="fullName" from="firstName + ' ' + lastName"/>
<set field="upperCaseCode" from="productCode?.toUpperCase()"/>
```

#### ✅ Only Use Script Tags For:

**1. Complex Data Structure Manipulation**
```xml
<!-- Complex map/list operations requiring loops -->
<script><![CDATA[
    resultMap = [:]
    for (item in itemList) {
        if (!resultMap[item.categoryId]) {
            resultMap[item.categoryId] = []
        }
        resultMap[item.categoryId].add([
            id: item.itemId,
            name: item.itemName,
            calculated: item.price * item.quantity * taxRate
        ])
    }
]]></script>
```

**2. Advanced Algorithm Implementation**
```xml
<!-- Mathematical calculations, parsing algorithms -->
<script><![CDATA[
    // Complex calculation that can't be expressed in single expression
    BigDecimal compound = principal
    for (int i = 0; i < periods; i++) {
        compound = compound.multiply(BigDecimal.ONE.add(rate))
    }
    finalAmount = compound.setScale(2, RoundingMode.HALF_UP)
]]></script>
```

**3. External Library Integration**
```xml
<!-- When XML DSL cannot access required functionality -->
<script><![CDATA[
    import some.external.Library
    Library.SpecialMethod specialProcessor = new Library.SpecialMethod()
    processedData = specialProcessor.transform(rawData)
]]></script>
```

**4. Dynamic Object Creation**
```xml
<!-- When declarative approach is insufficient -->
<script><![CDATA[
    Class<?> dynamicClass = Class.forName(className)
    Object instance = dynamicClass.getDeclaredConstructor().newInstance()
    result = instance.process(inputData)
]]></script>
```

#### Key Principles:

- **Start with XML DSL**: Always attempt XML DSL approach first
- **Script as Last Resort**: Only use scripts when XML DSL cannot express the logic
- **Maintainability**: XML DSL is easier to read and modify for most developers
- **Performance**: Framework optimizes XML DSL operations better than scripts
- **Consistency**: Following XML DSL patterns maintains codebase consistency

### Service-call Parameter Patterns

**Prefer `in-map` over `field-map` sub-tags** for service-call parameters in most cases, even when spanning multiple lines.

**✅ PREFERRED: in-map (even when multi-line)**
```xml
<!-- Simple parameters -->
<service-call name="mantle.order.OrderInfoServices.get#OrderDisplayInfo" in-map="[orderId:orderId]" out-map="context"/>

<!-- Multiple parameters - still use in-map -->
<service-call name="mantle.party.PartyServices.get#PartySettingValue" 
              in-map="[partySettingTypeId:'PayrollEmployeePartyIdType', partyId:timePeriod.partyId]" out-map="idSetting"/>

<!-- Even complex parameters spanning lines -->
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" 
              in-map="[partyId:partyId, fiscalTaxDocumentTypeEnumId:documentType]" out-map="availabilityResult"/>
```

**❌ AVOID: field-map sub-tags (unless special cases)**
```xml
<!-- Don't do this for simple parameter mapping -->
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" out-map="availabilityResult">
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="fiscalTaxDocumentTypeEnumId" from="documentType"/>
</service-call>
```

**Exceptions - Use field-map when:**
- **Complex conditional parameters** requiring inline logic
- **Dynamic parameter names** that can't be expressed in map syntax
- **Parameters with extensive inline comments** needed for clarity

**Key principles:**
- **Conciseness**: in-map reduces XML verbosity
- **Readability**: Direct parameter mapping is clearer
- **Consistency**: Matches Moqui team patterns in mantle-usl and SimpleScreens
- **Maintainability**: Easier to modify parameter lists

### Service Call Parameter Passing Best Practices

**Choose between `in-map` and `field-map` based on the parameter passing pattern:**

**✅ PREFERRED: Use `in-map` for simple parameter passing**
```xml
<!-- For straightforward parameter mapping -->
<service-call name="mycompany.dte.DteServices.create#CafAutoRequest" in-map="context"/>

<!-- For passing current context directly -->
<service-call name="mycompany.dte.DteServices.update#CafAutoRequest" in-map="context" out-map="context"/>

<!-- For simple parameter maps -->
<service-call name="service.name" in-map="[partyId: partyId, amount: 100]"/>
```

**✅ USE `field-map` when you need:**
- **Field name transformation**: Service parameter names differ from local variables
- **Complex parameter mapping**: Mixed value sources (variables, literals, expressions)
- **Type conversion**: Explicit type casting required
- **Conditional parameters**: Some parameters need conditional logic

```xml
<!-- Field name transformation -->
<service-call name="mantle.account.PaymentServices.process#Payment">
    <field-map field-name="paymentAmount" from="totalAmount"/>
    <field-map field-name="paymentMethodId" from="selectedMethod"/>
    <field-map field-name="currencyCode" value="USD"/>
</service-call>

<!-- Mixed parameter sources -->
<service-call name="mycompany.dte.DteServices.create#CafAutoRequestLog">
    <field-map field-name="logId" from="logId"/>
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="requestStatus" value="INITIATED"/>
    <field-map field-name="requestStartTime" from="ec.user.nowTimestamp"/>
    <field-map field-name="durationMs" from="durationMs" type="Integer"/>
</service-call>
```

**Decision Criteria:**
- **Use `in-map="context"`**: When service parameters match current context variables
- **Use `in-map="[key: value]"`**: For simple, direct parameter mapping (≤3 parameters)
- **Use `field-map`**: When parameter names don't match, mixing literals/variables, or need type conversion
- **Performance**: `in-map` is slightly more efficient for simple cases

### Service Call Naming and Parameter Best Practices

**CRITICAL: Always use complete service paths and prefer `in-map` over `field-map` for simple parameter passing**

This section addresses two critical issues commonly found in API service implementations:

1. **Incomplete service names** using shorthand patterns
2. **Inconsistent parameter passing** using `field-map` instead of preferred `in-map`

#### Service Naming Requirements

**✅ CORRECT: Always use complete service paths**

Use the full `namespace.ServiceFile.verb#noun` format for all service calls:

```xml
<!-- ✅ CORRECT: Complete service path -->
<service-call name="mycompany.myapp.api.ApiKeyEntityServices.create#ApiKeyMetadata" 
              in-map="[userLoginKeyId:userLoginKeyId, externalSystemName:externalSystemName, description:description]"/>

<!-- ✅ CORRECT: Complete service path with multiple parameters -->
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" 
              in-map="[partyId:partyId, fiscalTaxDocumentTypeEnumId:documentType]" out-map="availabilityResult"/>

<!-- ✅ CORRECT: Complete service path for mantle services -->
<service-call name="mantle.order.OrderInfoServices.get#OrderDisplayInfo" 
              in-map="[orderId:orderId]" out-map="context"/>
```

**❌ INCORRECT: Shorthand service names**

Never use incomplete service names that omit the full namespace path:

```xml
<!-- ❌ WRONG: Incomplete service name -->
<service-call name="create#ApiKeyMetadata">
    <field-map field-name="userLoginKeyId" from="userLoginKeyId"/>
    <field-map field-name="externalSystemName" from="externalSystemName"/>
</service-call>

<!-- ❌ WRONG: Missing namespace -->
<service-call name="check#FolioAvailability">
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="fiscalTaxDocumentTypeEnumId" from="documentType"/>
</service-call>
```

**Why complete service paths are required:**
- **Explicit dependencies**: Clear which service file contains the implementation
- **Namespace collision prevention**: Multiple components may have similar service names
- **IDE support**: Better code navigation and refactoring support
- **Documentation**: Self-documenting code that shows service locations
- **Debugging**: Easier to locate and troubleshoot service implementations

#### Parameter Passing Requirements

**✅ PREFERRED: Use `in-map` for simple parameter passing**

For straightforward parameter mapping, always prefer `in-map` over `field-map` sub-tags:

```xml
<!-- ✅ CORRECT: Simple parameters with in-map -->
<service-call name="mycompany.myapp.api.ApiKeyEntityServices.create#ApiKeyMetadata" 
              in-map="[userLoginKeyId:userLoginKeyId, externalSystemName:externalSystemName, description:description]"/>

<!-- ✅ CORRECT: Context passing -->
<service-call name="mycompany.dte.DteServices.process#Document" in-map="context" out-map="context"/>

<!-- ✅ CORRECT: Multi-line in-map for readability -->
<service-call name="complex.service.path.ComplexServices.process#ComplexOperation" 
              in-map="[param1:value1, param2:value2, param3:value3, 
                      longParameterName:longValue, anotherParam:anotherValue]" 
              out-map="result"/>
```

**❌ AVOID: Using `field-map` for simple parameter mapping**

Don't use `field-map` sub-tags when `in-map` can handle the parameter mapping:

```xml
<!-- ❌ WRONG: Unnecessary field-map sub-tags -->
<service-call name="mycompany.myapp.api.ApiKeyEntityServices.create#ApiKeyMetadata">
    <field-map field-name="userLoginKeyId" from="userLoginKeyId"/>
    <field-map field-name="externalSystemName" from="externalSystemName"/>
    <field-map field-name="description" from="description"/>
    <field-map field-name="isActive" from="isActive"/>
</service-call>

<!-- ❌ WRONG: field-map for direct parameter mapping -->
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" out-map="availabilityResult">
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="fiscalTaxDocumentTypeEnumId" from="documentType"/>
</service-call>
```

#### When to Use `field-map` (Exceptions)

Use `field-map` sub-tags ONLY when you need:

**1. Field name transformation** (service parameter names differ from local variables):
```xml
<service-call name="mantle.account.PaymentServices.process#Payment">
    <field-map field-name="paymentAmount" from="totalAmount"/>        <!-- Different names -->
    <field-map field-name="paymentMethodId" from="selectedMethod"/>   <!-- Different names -->
    <field-map field-name="currencyCode" value="USD"/>                <!-- Literal value -->
</service-call>
```

**2. Mixed parameter sources** (variables, literals, expressions):
```xml
<service-call name="example.audit.AuditServices.create#TransactionLog">
    <field-map field-name="transactionId" from="transactionId"/>           <!-- From variable -->
    <field-map field-name="status" value="PROCESSING"/>                    <!-- Literal value -->
    <field-map field-name="timestamp" from="ec.user.nowTimestamp"/>        <!-- Framework expression -->
    <field-map field-name="amount" from="orderTotal * 0.1" type="BigDecimal"/>  <!-- Calculation + type -->
</service-call>
```

**3. Type conversion** requirements:
```xml
<service-call name="example.data.DataServices.process#NumericData">
    <field-map field-name="count" from="stringCount" type="Integer"/>
    <field-map field-name="amount" from="stringAmount" type="BigDecimal"/>
</service-call>
```

**4. Conditional parameters** with inline logic:
```xml
<service-call name="example.workflow.WorkflowServices.conditional#Processing">
    <field-map field-name="processType" from="isUrgent ? 'URGENT' : 'NORMAL'"/>
    <field-map field-name="priority" from="customer.vipLevel ?: 1" type="Integer"/>
</service-call>
```

#### Decision Matrix

| Use Case | Recommended Approach | Example |
|----------|---------------------|---------|
| Direct parameter mapping | `in-map="[key:value]"` | `in-map="[orderId:orderId, customerId:customerId]"` |
| Context passing | `in-map="context"` | `in-map="context" out-map="context"` |
| Field name transformation | `<field-map>` | `<field-map field-name="paymentAmount" from="totalAmount"/>` |
| Mixed sources (vars + literals) | `<field-map>` | `<field-map field-name="status" value="ACTIVE"/>` |
| Type conversion needed | `<field-map>` | `<field-map field-name="count" from="stringValue" type="Integer"/>` |
| Complex expressions | `<field-map>` | `<field-map field-name="result" from="value1 + value2 * 0.1"/>` |

#### `field-map` Attribute Behavior (Technical Reference)

**Source: Framework XSD (xml-actions-3.xsd) and FTL template (XmlActions.groovy.ftl)**

The `field-map` element in `service-call` follows this attribute resolution order:

1. **`from` attribute specified** → Uses the expression from `from` as the value source
2. **`value` attribute specified** → Uses the literal string from `value`
3. **Neither `from` nor `value`** → **Automatically uses `field-name` as the variable name**

```xml
<!-- These are EQUIVALENT - 'from' is optional when field-name matches variable name -->
<field-map field-name="partyId" from="partyId"/>
<field-map field-name="partyId"/>  <!-- Implicit: uses 'partyId' variable from context -->
```

**Generated Groovy code for `<field-map field-name="partyId"/>`:**
```groovy
.parameter("partyId", partyId)
```

**⚠️ IMPORTANT: `ignore-if-empty` is NOT supported on `field-map` in service-call contexts**

The `ignore-if-empty` attribute is **silently ignored** when used on `field-map` sub-elements of `service-call`. This attribute only works with `date-filter` elements in entity operations.

```xml
<!-- ❌ WRONG: ignore-if-empty has NO EFFECT on field-map in service-call -->
<service-call name="update#MyEntity">
    <field-map field-name="optionalField" ignore-if-empty="true"/>  <!-- IGNORED! -->
</service-call>

<!-- ✅ CORRECT: Use conditional logic or handle in service -->
<service-call name="update#MyEntity" in-map="[entityId: entityId, optionalField: optionalField]"/>
```

If you need conditional parameter passing, use one of these approaches:
- Handle null/empty values in the target service logic
- Use `in-map` with a constructed map that excludes empty values
- Use conditional `<if>` blocks before the service-call

#### Benefits of This Approach

**Complete service paths provide:**
- Clear service location and namespace identification
- Prevention of service name collisions between components
- Better IDE support for navigation and refactoring
- Self-documenting code that shows dependencies
- Easier debugging and troubleshooting

**Preferred `in-map` usage provides:**
- **Conciseness**: Reduces XML verbosity significantly
- **Readability**: Direct parameter mapping is clearer to understand
- **Consistency**: Matches patterns used in Moqui framework and mantle-usl
- **Maintainability**: Easier to modify parameter lists
- **Performance**: Slightly more efficient than field-map sub-tags

#### Migration Pattern

When refactoring existing code, use this pattern:

```xml
<!-- BEFORE: Incomplete name + field-map -->
<service-call name="create#Entity">
    <field-map field-name="param1" from="param1"/>
    <field-map field-name="param2" from="param2"/>
</service-call>

<!-- AFTER: Complete name + in-map -->
<service-call name="namespace.EntityServices.create#Entity" 
              in-map="[param1:param1, param2:param2]"/>
```

### XML DSL If/Then/Else Structure

**CRITICAL**: When using `<else>` tags in XML DSL, you must use the proper nested structure with `<then>` tags.

**❌ WRONG: Separate if/else tags**
```xml
<if condition="condition">
    <!-- statements -->
</if>
<else>
    <!-- other statements -->
</else>
```

**✅ CORRECT: Nested if/then/else structure**
```xml
<if condition="condition">
    <then>
        <!-- statements when condition is true -->
    </then>
    <else>
        <!-- statements when condition is false -->
    </else>
</if>
```

**✅ CORRECT: Single if without else (no then needed)**
```xml
<if condition="condition">
    <!-- statements when condition is true -->
</if>
```

**✅ CORRECT: Multiple conditions with else-if**
```xml
<if condition="firstCondition">
    <then>
        <!-- statements for first condition -->
    </then>
    <else-if condition="secondCondition">
        <then>
            <!-- statements for second condition -->
        </then>
    </else-if>
    <else>
        <!-- statements when no conditions match -->
    </else>
</if>
```

**Real Example:**
```xml
<if condition="isActive &amp;&amp; !isExpired">
    <then>
        <!-- Process active CAF -->
        <set field="activeCafCount" from="activeCafCount + 1"/>
        <entity-find-count entity-name="mycompany.dte.FiscalTaxDocument" count-field="usedFoliosCount">
            <econdition field-name="statusId" operator="not-equals" value="Ftd-NotIssued"/>
        </entity-find-count>
        <set field="availableFolios" from="totalRange - usedFoliosCount"/>
    </then>
    <else>
        <!-- Handle inactive/expired CAF -->
        <set field="availableFolios" value="0"/>
        <log message="CAF ${caf.cafId} is inactive or expired"/>
    </else>
</if>
```

### Advanced XML DSL Conversion Patterns

Based on practical experience converting complex API services from script-heavy implementations to XML DSL, these patterns provide comprehensive guidance for complete script-to-XML conversion.

#### **Rule 1: All Variable Assignments Use `<set>` Tags**

Any operation that assigns a value to a variable can and should use `<set>` tags, including:

**✅ Simple Assignments:**
```xml
<!-- Instead of: apiKey = org.moqui.util.StringUtilities.getRandomString(40) -->
<set field="apiKey" from="org.moqui.util.StringUtilities.getRandomString(40)"/>

<!-- Instead of: hashedKey = ec.ecfi.getSimpleHash(apiKey, '', ec.ecfi.getLoginKeyHashType(), false) -->
<set field="hashedKey" from="ec.ecfi.getSimpleHash(apiKey, '', ec.ecfi.getLoginKeyHashType(), false)"/>

<!-- Instead of: currentTime = ec.user.nowTimestamp -->
<set field="currentTime" from="ec.user.nowTimestamp"/>
```

**✅ Date/Time Calculations:**
```xml
<!-- Instead of: expirationMs = expirationDays * 24 * 60 * 60 * 1000L -->
<set field="expirationMs" from="expirationDays * 24 * 60 * 60 * 1000L"/>

<!-- Instead of: expirationDate = new Timestamp(currentTime.getTime() + expirationMs) -->
<set field="expirationDate" from="new java.sql.Timestamp(currentTime.getTime() + expirationMs)"/>
```

**✅ Map Creation and Key Assignment:**
```xml
<!-- Instead of: Map updateParams = [userLoginKeyId: userLoginKeyId] -->
<set field="updateParams" from="[userLoginKeyId: userLoginKeyId]"/>

<!-- Instead of: updateParams.description = description -->
<set field="updateParams.description" from="description"/>
<set field="updateParams.permissions" from="permissions"/>
```

#### **Rule 2: Medium Complexity Conditionals in XML Attributes**

Complex boolean expressions can be used directly in XML condition attributes:

**✅ Complex Conditions:**
```xml
<!-- Instead of: if (!keyInfo || (!ec.user.isInGroup("SITE_ADMIN") && keyInfo.tenantId != ec.user.tenantId)) -->
<if condition="!keyInfo || (!ec.user.isInGroup('SITE_ADMIN') &amp;&amp; keyInfo.tenantId != ec.user.tenantId)">
    <message error="true">Access denied</message>
    <return/>
</if>

<!-- Instead of: if (keyInfo.thruDate && keyInfo.thruDate.before(now)) -->
<if condition="keyInfo.thruDate &amp;&amp; keyInfo.thruDate.before(now)">
    <set field="failureReason" value="API key has expired"/>
    <return/>
</if>
```

#### **Rule 3: Entity Operations Always Use XML DSL**

All entity queries and operations have XML equivalents:

**✅ Entity Find with Conditions:**
```xml
<!-- Instead of: def keyInfo = ec.entity.find("mycompany.myapp.api.ApiKeyView").condition("loginKey", hashedKey).condition("isActive", "Y").one() -->
<entity-find-one entity-name="mycompany.myapp.api.ApiKeyView" value-field="keyInfo">
    <field-map field-name="loginKey" from="hashedKey"/>
    <field-map field-name="isActive" value="Y"/>
</entity-find-one>
```

#### **Rule 4: Service Calls Always Use XML DSL**

All service invocations should use `<service-call>` with `in-map`:

**✅ Service Calls:**
```xml
<!-- Instead of: ec.service.sync().name("update#mycompany.myapp.api.ApiKeyMetadata").parameters(updateParams).call() -->
<service-call name="update#mycompany.myapp.api.ApiKeyMetadata" in-map="updateParams"/>

<!-- Instead of: ec.service.async().name("create#mycompany.myapp.api.ApiKeyHistory").parameter(...).call() -->
<service-call name="create#mycompany.myapp.api.ApiKeyHistory" async="true" 
              in-map="[userLoginKeyId:userLoginKeyId, eventType:'CREATED', eventDate:now]"/>
```

#### **EXCEPTIONS: When Script Tags Are Still Required**

**❌ Only Use Script Tags For:**

**1. Adding Elements to Collections (No Assignment)**
```groovy
// When modifying collections without assignment
someList.add(newElement)
someMap.put(key, value)  // when the put operation itself is the goal, not assignment
```

**2. Complex Loop Operations Without Direct XML Equivalent**
```groovy
// Complex iteration logic that can't be expressed with <iterate>
for (int i = 0; i < items.size(); i++) {
    if (complexCondition(items[i], i)) {
        processItem(items[i])
    }
}
```

**3. External Library Integration Beyond Moqui Framework**
```groovy
// When external libraries don't have XML DSL equivalents
SomeExternalLibrary.doComplexOperation()
```

#### **Key Principle: "Assignment = XML DSL"**

If the operation results in assigning a value to a variable or field, use XML DSL. Scripts should only contain operations that modify collections in-place or perform complex external library integrations.

### Implementation Example: CAF Auto Request Refactoring

The `run#CafAutoRequest` service was successfully refactored from a 200+ line script to a hybrid approach:
- **Core logic**: XML DSL for entity queries, conditionals, and service calls
- **Data structures**: Minimal script tags only for complex map operations
- **Result**: 70% reduction in script code while maintaining full functionality

## XML and Code Formatting Best Practice

### 180-Character Line Limit Rule

When generating XML and code with lists and maps content, use the following criteria to decide whether to use multiple lines or fit more than one command/value per line:

**Rule**: If the complete statement or expression (for XML, the whole tag + subtags and corresponding closings) fits within **180 characters** counting indentation and everything, keep it in a single line. Otherwise, separate it into multiple lines and apply the same criteria recursively to sub-expressions.

### Examples

**✅ SINGLE LINE: Complete expression under 180 characters**
```xml
<econdition field-name="issuerPartyId" from="partyId"/>
<econdition field-name="fiscalTaxDocumentTypeEnumId" from="fiscalTaxDocumentTypeEnumId"/>
<set field="isActive" from="caf.activo == 'Y'"/>
<service-call name="create#mycompany.dte.CafAutoRequestLog">
```

**✅ MULTI-LINE: Complete expression over 180 characters**
```xml
<entity-find-count entity-name="mycompany.dte.FiscalTaxDocument" count-field="usedFoliosCount">
    <econdition field-name="issuerPartyId" from="partyId"/>
    <econdition field-name="fiscalTaxDocumentNumber" operator="greater-equals" from="caf.desde"/>
    <econdition field-name="fiscalTaxDocumentNumber" operator="less-equals" from="caf.hasta"/>
    <econdition field-name="statusId" operator="not-equals" value="Ftd-NotIssued"/>
</entity-find-count>
```

**✅ MIXED: Apply rule recursively to sub-expressions**
```xml
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" out-map="availabilityResult">
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="fiscalTaxDocumentTypeEnumId" from="documentType"/>
</service-call>
```

### Benefits

- **Readability**: Consistent formatting improves code readability
- **Consistency**: Clear rule eliminates formatting decisions
- **Tool Integration**: Works well with IDE and editor line length settings
- **Maintenance**: Easier to review and modify consistently formatted code

## Localization (L10n) Best Practices

### CRITICAL: Always Add Translations

**Every time you add or modify text that will be displayed to users, you MUST add the corresponding translations.**

This includes:
- StatusItem descriptions
- Enumeration descriptions
- Screen labels and titles
- Form field labels
- Button text
- Error messages
- Any other user-visible text

### Localization File Structure

Localization files are organized in the {localization-component} component:
- **Location**: `runtime/component/{localization-component}/data/`
- **Naming Convention**: `l10n-{component-name}.xml`
- **Examples**:
  - `l10n-{component-1}.xml` - Component-1 translations
  - `l10n-{component-2}.xml` - Component-2 translations
  - `l10n-SimpleScreens.xml` - SimpleScreens translations
  - `l10n.xml` - General/shared translations

### CRITICAL: Determining Which Translation File to Use

**The translation file depends on WHERE the English text is defined:**

1. **Text in {localization-component} component itself** → Use `l10n.xml`
   - Example: Screens in `{localization-component}/screen/` → `l10n.xml`
   - There is NO `l10n-{localization-component}.xml` file

2. **Text in other components** → Use `l10n-{component-name}.xml`
   - Example: Text in `{component-1}/screen/` → `l10n-{component-1}.xml`
   - Example: Text in `SimpleScreens/screen/` → `l10n-SimpleScreens.xml`
   - Example: Text in `{component-2}/screen/` → `l10n-{component-2}.xml`

3. **How to identify the component:**
   - Look at the file path: `runtime/component/{component-name}/...`
   - The component name is the directory immediately under `runtime/component/`
   - Exception: {localization-component} itself uses `l10n.xml` not `l10n-{localization-component}.xml`

### Adding Translations

**CRITICAL: XML Pattern - Complete String Localization**

LocalizedMessage entries use simple string-to-string mapping of COMPLETE strings, including any template expressions:

✅ **CORRECT: Translate complete strings with template expressions intact**
```xml
<moqui.basic.LocalizedMessage
    original="Engagement Configurations for: ${ec.resource.expand('PartyNameOnlyTemplate', null, orderEntity)}"
    locale="es"
    localized="Configuración de Instancias de Participación para: ${ec.resource.expand('PartyNameOnlyTemplate', null, orderEntity)}"/>
```

✅ **CORRECT: Simple text without expressions**
```xml
<moqui.basic.LocalizedMessage original="Sales Opportunities" locale="es" localized="Oportunidades de Negocio"/>
```

❌ **INCORRECT: Do not translate partial strings or fragments**
```xml
<!-- WRONG: Don't translate fragments like " for " -->
<moqui.basic.LocalizedMessage original=" for " locale="es" localized=" para "/>
```

❌ **INCORRECT: Do not escape $ or use nested template calls**
```xml
<moqui.basic.LocalizedMessage original="English Text" locale="es" localized="\${ec.l10n.localize('Spanish')}"/>
```

**Key Localization Principles:**
- **Complete strings only**: Always translate the entire text content of a label, title, or message
- **Preserve template expressions**: Keep `${...}` expressions exactly as they are in the translation
- **No partial fragments**: Never translate partial strings like " for ", " and ", etc.
- **Direct mapping**: Simple pattern `original="Complete English String" locale="es" localized="Complete Spanish String"`

**1. For Simple Text/Labels:**
```xml
<moqui.basic.LocalizedMessage
    original="English Text"
    locale="es"
    localized="Texto en Español"/>
```

**2. For StatusItem Descriptions:**
```xml
<moqui.basic.LocalizedEntityField 
    entityName="moqui.basic.StatusItem" 
    fieldName="description" 
    locale="es" 
    pkValue="StatusId" 
    localized="Descripción en Español"/>
```

**3. For Enumeration Descriptions:**
```xml
<moqui.basic.LocalizedEntityField 
    entityName="moqui.basic.Enumeration" 
    fieldName="description" 
    locale="es" 
    pkValue="EnumId" 
    localized="Descripción en Español"/>
```

### Workflow for Adding Translations

1. **FIRST: Check if Translation Already Exists**
   - Search the appropriate l10n file for the English text
   - Common terms like "Yes", "No", "Active", "Delete" are likely already translated
   - Use grep or search to find: `grep -i "original=\"Your Text\"" l10n*.xml`
   - Only add new translation if it doesn't exist

2. **When Creating New Entities/Enums:**
   - Add the entity/enum definition in your component
   - Check if translation exists
   - If not, immediately add translation to appropriate `l10n-*.xml` file in {localization-component}

3. **When Adding Screen Labels:**
   - Use meaningful English text as the key
   - Check if translation exists
   - If not, add translation to the component's l10n file

4. **File Selection:**
   - Use existing component-specific file if available (e.g., `l10n-{component-name}.xml`)
   - Create new file following naming convention if needed
   - Use `l10n.xml` only for truly generic/shared translations

### Example: Checking for Existing Translations

**Before adding any translation, check if it exists:**
```bash
# Check for "Active" translation in all l10n files
cd runtime/component/{localization-component}/data
grep -i 'original="Active"' l10n*.xml

# Output shows it already exists:
# l10n.xml: <moqui.basic.LocalizedMessage original="Active" locale="es" localized="Activo"/>
# → Don't add it again!
```

### Example: Adding a New Status

**Step 1: Define Status (in your component)**
```xml
<!-- File: runtime/component/{component-name}/data/{Component}Data.xml -->
<moqui.basic.StatusItem statusId="ReqPending" statusTypeId="RequestStatus"
    description="Request Pending" sequenceNum="1"/>
```

**Step 2: Add Translation (choose file based on component location)**
```xml
<!-- Since status is defined in {component-name} component, use: -->
<!-- File: runtime/component/{localization-component}/data/l10n-{component-name}.xml -->
<moqui.basic.LocalizedEntityField 
    entityName="moqui.basic.StatusItem" 
    fieldName="description" 
    locale="es" 
    pkValue="ReqPending"
    localized="Solicitud Pendiente"/>
```

**Another Example - Text in {localization-component}:**
```xml
<!-- File: runtime/component/{localization-component}/screen/{AdminScreen}/SomeScreen.xml -->
<label text="Process Invoice" type="h3"/>

<!-- Translation goes in l10n.xml (NOT l10n-{localization-component}.xml): -->
<!-- File: runtime/component/{localization-component}/data/l10n.xml -->
<moqui.basic.LocalizedMessage 
    original="Process Invoice" 
    locale="es" 
    localized="Procesar Factura"/>
```

### Important Notes

- **Default Locale**: Configure based on project target market (e.g., `es` for Spanish, `fr` for French)
- **Always Use Seed Data Type**: Localization files should be `type="seed"`
- **Immediate Translation**: Don't defer translations - add them in the same commit
- **Context Matters**: Ensure translations are contextually appropriate for the target locale
- **Technical Terms**: Some domain-specific technical terms may remain untranslated

## Localization (L10n)

For comprehensive localization guidance, see:
- **Framework patterns**: `.agent-os/localization-guide.md`
- **Component requirements**: `runtime/component/{component}/.agent-os/localization-requirements.md`

### Key Concepts

- **LocalizedMessage**: For UI text, labels, menus, and messages
- **LocalizedEntityField**: For database content values (enumerations, status descriptions)
- **Component-based organization**: L10n files organized by component boundaries
- **Primary locale**: Configure based on project target market

### Quick L10n Checklist

When adding new features:
1. Check for existing translations: `grep -r 'original="Text"' */data/*l10n*.xml`
2. Add to appropriate l10n file based on component
3. Test with Spanish locale
4. Commit translations with feature

## Screen Naming Conventions

### Find*.xml Screen Standard

**Purpose**: Find*.xml screens are reserved for primary entity search and listing interfaces that serve as the main entry point for finding and accessing entities.

**When to Use Find*.xml Pattern**:
- Primary entity listing screens (main search interface for an entity type)
- Screens that include comprehensive search functionality
- Screens that link to detail/edit screens for individual records
- Entry points for entity management workflows

**When NOT to Use Find*.xml Pattern**:
- Sub-entity or related lists (use descriptive names like `ContentList.xml`, `FieldList.xml`)
- Simple report listings (use `ReportList.xml`)
- General search interfaces (use `Search.xml`)
- Configuration screens (use `Configuration.xml` or `Settings.xml`)

**Standard Find*.xml Structure**:

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-include="false" default-menu-title="Entity Search">

    <!-- Standard transitions -->
    <transition name="editEntity"><default-response url="../EditEntity"/></transition>
    <transition name="createEntity"><service-call name="path.to.create#Entity"/>
        <default-response url="../EditEntity"/></transition>

    <actions>
        <!-- Parameter processing and defaults -->
    </actions>

    <widgets>
        <!-- Creation dialog -->
        <container-dialog id="CreateEntityDialog" button-text="New Entity">
            <form-single name="CreateEntityForm" transition="createEntity">
                <!-- creation fields -->
            </form-single>
        </container-dialog>

        <!-- Main listing form -->
        <form-list name="EntityList" list="entityList" skip-form="true"
                  header-dialog="true" select-columns="true" saved-finds="true"
                  show-csv-button="true" show-xlsx-button="true">
            <entity-find entity-name="path.to.EntityFindView" list="entityList">
                <search-form-inputs default-order-by="-createdDate,entityId"/>
            </entity-find>
            <!-- field definitions with navigation to edit screens -->
        </form-list>
    </widgets>
</screen>
```

**Standard Features Required**:
- Advanced search capabilities (`header-dialog="true"`)
- Export functionality (`show-csv-button="true"`, `show-xlsx-button="true"`)
- User experience features (`saved-finds="true"`, `select-columns="true"`)
- Create actions (container dialogs for new entities)
- Navigation to detail/edit screens

**Relationship to Detail/Edit Screens**:
- Find screens link to Edit/Detail screens: `FindParty.xml` → `EditParty.xml`
- Both screens typically in same directory
- Standard transition naming: `editEntity` pointing to `../EditEntity`

**Examples from SimpleScreens**:
- `FindParty.xml` - Main party search interface
- `FindInvoice.xml` - Main invoice search interface
- `FindOrder.xml` - Main order search interface
- `FindAsset.xml` - Main asset search interface

## Screen Development Best Practices

### Form-List Best Practices

**1. Entity-Find Placement for Form-List**

Always place `entity-find` operations **inside** the `form-list` element when the data is only used by that form. This approach:
- Ensures only required fields are fetched from the database (better performance)
- Keeps data retrieval close to its usage (better maintainability)
- Automatically handles pagination through `search-form-inputs`

**❌ WRONG: Entity-find in actions section**
```xml
<actions>
    <entity-find entity-name="Party" list="partyList">
        <search-form-inputs default-order-by="partyId"/>
    </entity-find>
</actions>
<widgets>
    <form-list name="PartyList" list="partyList" paginate="true">
        <!-- fields -->
    </form-list>
</widgets>
```

**✅ CORRECT: Entity-find inside form-list**
```xml
<widgets>
    <form-list name="PartyList">
        <entity-find entity-name="Party" list="partyList">
            <search-form-inputs default-order-by="partyId"/>
        </entity-find>
        <!-- fields -->
    </form-list>
</widgets>
```

**2. Always Use search-form-inputs**

When using `entity-find` for a `form-list`, always include `<search-form-inputs>` to enable:
- Automatic pagination
- User-defined searches
- Column sorting
- Proper result limiting

**3. Leverage Moqui Framework Saved Search Functionality**

**IMPORTANT**: Always prefer the Moqui Framework's built-in saved search functionality over custom saved search entities.

The `<search-form-inputs>` element automatically provides:
- **User-defined searches**: Users can save and reload search criteria
- **Automatic state persistence**: Search parameters persist across page reloads
- **Built-in UI controls**: Save/load search functionality is automatically provided
- **User-scoped storage**: Searches are automatically scoped to the current user

**❌ AVOID: Creating custom saved search entities**
```xml
<!-- DON'T create custom entities like SavedEmailSearch, SavedInvoiceSearch, etc. -->
<entity entity-name="SavedEmailSearch" package="custom">
    <field name="searchId" type="id" is-pk="true"/>
    <field name="searchName" type="text-medium"/>
    <field name="searchParameters" type="text-long"/>
</entity>
```

**✅ CORRECT: Use framework's built-in functionality**
```xml
<form-list name="EmailMessageList">
    <entity-find entity-name="EmailMessageProcessingDetail" list="emailList">
        <!-- This automatically enables saved searches -->
        <search-form-inputs default-order-by="-receivedDate"/>
    </entity-find>
    <!-- Form fields here -->
</form-list>
```

**Benefits of Framework Approach**:
- **Zero additional code**: No custom entities, services, or screens needed
- **Consistent UX**: All form-lists behave the same way across the application
- **Automatic maintenance**: Framework updates improve functionality automatically
- **Performance optimized**: Framework handles caching and storage efficiently
- **Security integrated**: Access controls work automatically

**4. Field Selection Optimization**

When entity-find is inside form-list, only fields referenced in the form are fetched unless explicitly specified:
```xml
<form-list name="PartyList">
    <entity-find entity-name="Party" list="partyList">
        <search-form-inputs default-order-by="partyId"/>
        <!-- Only partyId and organizationName will be fetched by default -->
        <!-- To fetch additional fields not displayed: -->
        <select-field field-name="partyTypeEnumId"/>
    </entity-find>
    <field name="partyId"><default-field><display/></default-field></field>
    <field name="organizationName"><default-field><display/></default-field></field>
</form-list>
```

**5. search-form-inputs URL Parameter Operator Suffixes**

When `search-form-inputs` processes URL or form input parameters (in `EntityFindBase.processInputFields()`), each field name supports these suffixes:

- **`{field}_op`** — Operator to apply. Supported values:
  - `equals` (default) — exact match
  - `like` — SQL LIKE pattern match
  - `contains` — wraps value with `%` on both sides
  - `begins` — wraps value with `%` on right side
  - `empty` — checks if field is null/empty (value ignored)
  - `in` — comma-separated list matched with IN operator
- **`{field}_not`** — Set to `Y` or `true` to negate the condition
- **`{field}_ic`** — Set to `Y` or `true` for case-insensitive matching

**CRITICAL: There is NO `not-in` operator.** To achieve NOT IN semantics, use `_op=in` combined with `_not=Y`:

```xml
<!-- WRONG: not-in is not a valid operator -->
<parameter name="statusId_op" value="not-in"/>

<!-- CORRECT: use in + not -->
<parameter name="statusId" value="WeComplete,WeClosed,WeCancelled"/>
<parameter name="statusId_op" value="in"/>
<parameter name="statusId_not" value="Y"/>
```

This is particularly important when passing filter parameters through `<link>` elements to screens that use `search-form-inputs`, ensuring consistency between dashboard counts (which use `econdition operator="not-in"`) and filtered list views.

### Row-Actions for Complex Display Logic

**CRITICAL: Use `row-actions` instead of complex Groovy expressions in display fields.**

**Problem**: Complex database queries or calculations in display field expressions can fail and are performance-intensive when executed for every row during rendering.

**❌ WRONG: Complex expressions in display fields**
```xml
<form-list name="EngagementList" list="engagementList">
    <field name="participantCount">
        <default-field title="Participants">
            <!-- This fails and is inefficient -->
            <display text="${ec.entity.find('mantle.work.effort.WorkEffortParty').condition('workEffortId', workEffortId).filterByDate(null, null, null).count()} participants"/>
        </default-field>
    </field>
</form-list>
```

**✅ CORRECT: Use row-actions for complex processing**
```xml
<form-list name="EngagementList" list="engagementList">
    <row-actions>
        <!-- Calculate participant count for each row -->
        <entity-find-count entity-name="mantle.work.effort.WorkEffortParty" count-field="participantCount">
            <econdition field-name="workEffortId"/>
            <date-filter/>
        </entity-find-count>
    </row-actions>

    <field name="participantCount">
        <default-field title="Participants">
            <!-- Simple field reference -->
            <display text="${participantCount} participants"/>
        </default-field>
    </field>
</form-list>
```

**Key Benefits of row-actions:**

1. **Error Prevention**: Database operations are properly handled with error management
2. **Performance**: More efficient database access patterns
3. **Maintainability**: Clear separation between data processing and presentation
4. **Debugging**: Easier to trace and debug data processing logic
5. **Reusability**: Calculated values can be used in multiple fields

**When to Use row-actions:**

- **Database queries**: Any entity-find, entity-find-count, or entity-find-one operations
- **Service calls**: When you need to call services for each row
- **Complex calculations**: Mathematical operations involving multiple fields
- **Conditional logic**: Setting different values based on row data
- **Field transformations**: Converting or formatting data for display

**Note**: `row-actions` is only available in `form-list`, not in `form-single`. For single forms, perform complex operations in the screen's main `actions` section instead.

**Exception:** Keep entity-find in actions when the list is used by multiple widgets or dropdowns.

### Service-to-Screen Field Name Alignment

**CRITICAL: When a service returns data for a form-list, the field names in the returned maps must exactly match the field names defined in the form-list.**

If a service returns data with field names that don't match what the screen expects, the form will display blank values even though the data exists.

**❌ WRONG: Service field names don't match screen field names**
```xml
<!-- Service returns: workEffortName, workEffortId, estimatedWorkDuration -->
<script>resultList.add([
    workEffortId: item.workEffortId,
    workEffortName: item.workEffortName,
    estimatedWorkDuration: item.estimatedWorkDuration
])</script>

<!-- Screen expects: itemTitle, agendaItemId, estimatedDuration -->
<form-list name="AgendaItemsList" list="resultList">
    <field name="agendaItemId"><default-field><display/></default-field></field>
    <field name="itemTitle"><default-field><display/></default-field></field>
    <field name="estimatedDuration"><default-field><display/></default-field></field>
</form-list>
<!-- Result: All fields appear blank! -->
```

**✅ CORRECT: Service returns field names matching screen expectations**
```xml
<!-- Service returns fields with names matching the screen -->
<script>resultList.add([
    agendaItemId: item.workEffortId,
    workEffortId: item.workEffortId,        // Include both for flexibility
    itemTitle: item.workEffortName,
    workEffortName: item.workEffortName,    // Include both for flexibility
    estimatedDuration: (item.estimatedWorkDuration * 60).intValue(),
    estimatedWorkDuration: item.estimatedWorkDuration
])</script>

<!-- Screen gets data with matching field names -->
<form-list name="AgendaItemsList" list="resultList">
    <field name="agendaItemId"><default-field><display/></default-field></field>
    <field name="itemTitle"><default-field><display/></default-field></field>
    <field name="estimatedDuration"><default-field><display/></default-field></field>
</form-list>
<!-- Result: Fields display correctly -->
```

**Best Practices:**
- **Include both names** when a field has a semantic alias (e.g., `workEffortId` and `agendaItemId`)
- **Transform values** in the service when units differ (e.g., hours to minutes)
- **Document expected fields** in the service's out-parameters description
- **Use consistent naming** across services that feed similar screens

**4. Use `limit` Instead of `limit-range` for Simple Result Limiting**

When you need to limit the maximum number of results from an entity-find (without pagination), use the `limit` attribute instead of `limit-range`:

**❌ WRONG: Using limit-range for simple limiting**
```xml
<entity-find entity-name="LogEntry" list="recentLogs">
    <limit-range start="0" size="50"/>
    <order-by field-name="-timestamp"/>
</entity-find>
```

**✅ CORRECT: Using limit attribute**
```xml
<entity-find entity-name="LogEntry" list="recentLogs" limit="50">
    <order-by field-name="-timestamp"/>
</entity-find>
```

**When to use each:**
- `limit="n"` - Simple maximum result limit (most common case)
- `limit-range start="x" size="y"` - Manual pagination or specific offset needs (rarely needed with form-list)
- Inside form-list with `search-form-inputs` - Neither needed; pagination is automatic

### Screen Widget Best Practices

**Container-Panel Widget Schema Compliance**

Based on analysis of `/framework/xsd/xml-screen-3.xsd`, the `container-panel` element has specific schema requirements that must be followed:

**✅ CORRECT Structure:**
- Only allows these child elements: `panel-header`, `panel-left`, `panel-center` (required), `panel-right`, `panel-footer`
- Only has one attribute: `id` (optional)
- Does NOT have a `type` attribute
- Does NOT allow `container-row` elements directly inside

**Purpose:** Creates a panel with up to five areas (header, left, center, right, footer) where only center is required.

**❌ COMMON MISTAKES:**
```xml
<!-- WRONG: type="tab" attribute doesn't exist -->
<container-panel type="tab" id="MyPanel">

<!-- WRONG: container-row directly inside container-panel not allowed -->
<container-panel id="MyPanel">
    <container-row>
        <!-- This violates the schema -->
    </container-row>
</container-panel>
```

**✅ CORRECT Usage Pattern:**
```xml
<container-panel id="MyPanel">
    <panel-header>
        <label text="Header Content"/>
    </panel-header>
    <panel-left>
        <label text="Left Sidebar"/>
    </panel-left>
    <panel-center>
        <container-row>
            <!-- Tab-like content goes here -->
            <row-col style="col-md-6">
                <label text="Left Column"/>
            </row-col>
            <row-col style="col-md-6">
                <label text="Right Column"/>
            </row-col>
        </container-row>
    </panel-center>
    <panel-right>
        <label text="Right Sidebar"/>
    </panel-right>
    <panel-footer>
        <label text="Footer Content"/>
    </panel-footer>
</container-panel>
```

**Alternative for Tab-like Interfaces:** Use `container` with proper tab classes and JavaScript, or use forms with tab functionality.

**Container Element Type Attribute Restrictions**

Based on analysis of `/framework/xsd/xml-screen-3.xsd`, the `container` element's `type` attribute is restricted to enumerated values only:

**✅ VALID Container Types (from schema):**
- `div` (default)
- `span`
- `ul`, `ol`, `li` (list elements)
- `dl`, `dd` (definition list elements)
- `header`, `footer` (semantic elements)
- `code`, `pre` (code/formatting elements)
- `hr` (horizontal rule)
- `i` (italic element)

**❌ INVALID Types Found in Practice:**
- `h1`, `h2`, `h3`, `h4`, `h5`, `h6` - NOT valid (use `label` with appropriate `type` instead)
- `dt` - NOT valid (only `dd` is available for definition lists)
- Any other HTML element types not listed above

**❌ COMMON MISTAKES:**
```xml
<!-- WRONG: h4 type doesn't exist in container schema -->
<container type="h4"><label text="Title"/></container>

<!-- WRONG: dt type doesn't exist in container schema -->
<container type="dt"><label text="Term"/></container>
```

**✅ CORRECT Alternatives:**
```xml
<!-- RIGHT: Use label with heading type -->
<label text="Title" type="h4"/>

<!-- RIGHT: For definition list terms, use dd with styling -->
<container type="dd"><label text="Term" style="font-weight:bold;"/></container>

<!-- RIGHT: Or use proper CSS classes instead of forcing semantic types -->
<container style="heading-4"><label text="Title"/></container>
```

**Key Points:**
- Container type attribute has strict enumeration - invalid types will cause schema validation errors
- For headings, always use `label` element with appropriate `type` attribute (`h1`, `h2`, `h3`, `h4`, `h5`, `h6`)
- For definition list terms, only `dd` type is available (not `dt`)
- When semantic HTML structure is needed beyond available types, use CSS classes on `div` containers instead

### Duration Display Formatting Standard

**MANDATORY: When displaying duration values (hours/minutes), use dynamic unit formatting with locale-aware decimals.**

This standard applies to all projects displaying `estimatedWorkDuration` or any other duration field.

**Rules:**
1. **Never display decimals for sub-unit values** - When duration is less than 1 hour, display as minutes (no decimals)
2. **Use localized decimal separators** - Use `ec.l10n.format()` for decimal values to respect locale (e.g., `0,75h` for locales using comma separator instead of `0.75h`)
3. **Whole numbers need no decimals** - When duration is an exact hour (e.g., 2.0), display as `2 h` not `2,0 h`

**Examples:**
| Value (hours) | Display (en) | Display (es-CL) |
|---------------|--------------|-----------------|
| 0.25          | 15 min       | 15 min          |
| 0.5           | 30 min       | 30 min          |
| 0.75          | 45 min       | 45 min          |
| 1.0           | 1 h          | 1 h             |
| 1.5           | 1.5 h        | 1,5 h           |
| 2.0           | 2 h          | 2 h             |
| 2.25          | 2.25 h       | 2,25 h          |

**✅ CORRECT: Dynamic unit formatting with localization**
```xml
<display text="${estimatedWorkDuration != null &amp;&amp; estimatedWorkDuration != 0
    ? (estimatedWorkDuration &lt; 1
        ? ((estimatedWorkDuration * 60) as int) + ' min'
        : (estimatedWorkDuration == (estimatedWorkDuration as int)
            ? (estimatedWorkDuration as int) + ' h'
            : ec.l10n.format(estimatedWorkDuration, '#,##0.##') + ' h'))
    : '-'}"/>
```

**❌ WRONG: Fixed unit without locale**
```xml
<!-- Displays 0.75h instead of 45 min -->
<display text="${estimatedWorkDuration ? estimatedWorkDuration + 'h' : '-'}"/>

<!-- Wrong calculation - divides instead of multiplies -->
<display text="${estimatedWorkDuration ? (estimatedWorkDuration / 60) + ' min' : '-'}"/>

<!-- Doesn't use localized decimals -->
<display text="${estimatedWorkDuration ? estimatedWorkDuration + ' hours' : '-'}"/>
```

**Service for Programmatic Use:**

For service layer duration formatting, use `mycompany.myapp.DurationServices.format#Duration`:

```xml
<service-call name="mycompany.myapp.DurationServices.format#Duration"
              in-map="[durationHours:estimatedWorkDuration]" out-map="durationResult"/>
<set field="formattedDuration" from="durationResult.formattedDuration"/>
```

**Key Points:**
- `estimatedWorkDuration` is stored in decimal hours (0.5 = 30 minutes)
- Always multiply by 60 to convert hours to minutes (not divide)
- Use `ec.l10n.format()` with pattern `#,##0.##` for localized decimal formatting

### Screen Form Field Attribute Guidelines

**CRITICAL: Correct attribute usage for form fields based on XML schema requirements**

#### Drop-down Fields

**❌ WRONG: Using `required` attribute on drop-down**
```xml
<drop-down required="true">
    <entity-options>
        <entity-find entity-name="StatusItem">
            <econdition field-name="statusTypeId" value="InvoiceStatus"/>
        </entity-find>
    </entity-options>
</drop-down>
```

**✅ CORRECT: Use `allow-empty` attribute**
```xml
<drop-down allow-empty="false">
    <entity-options>
        <entity-find entity-name="StatusItem">
            <econdition field-name="statusTypeId" value="InvoiceStatus"/>
        </entity-find>
    </entity-options>
</drop-down>
```

**❌ WRONG: Using `default-value` attribute on drop-down**
```xml
<drop-down default-value="2">
    <option key="1" text="High"/>
    <option key="2" text="Medium"/>
    <option key="3" text="Low"/>
</drop-down>
```

**✅ CORRECT: Use `no-current-selected-key` attribute**
```xml
<drop-down no-current-selected-key="2">
    <option key="1" text="High"/>
    <option key="2" text="Medium"/>
    <option key="3" text="Low"/>
</drop-down>
```

*Explanation*: The `no-current-selected-key` attribute specifies which option should be selected by default when no value is currently set. This is different from `default-value` which doesn't exist for drop-down elements.

#### Invalid Attributes on Form Field Elements

**CRITICAL: The `<field>` element in screen forms has limited valid attributes**

According to `xml-form-3.xsd`, the only valid attributes on `<field>` are:
- `name` (required)
- `from`
- `hide`

**❌ WRONG: Using `required` or `type` on field element**
```xml
<field name="itemTitle" required="true">
    <default-field title="Item Title">
        <text-line size="50"/>
    </default-field>
</field>

<field name="estimatedDuration" type="Integer">
    <default-field title="Duration">
        <text-line size="10"/>
    </default-field>
</field>
```

**✅ CORRECT: Field element without invalid attributes**
```xml
<field name="itemTitle">
    <default-field title="Item Title">
        <text-line size="50"/>
    </default-field>
</field>

<field name="estimatedDuration">
    <default-field title="Duration">
        <text-line size="10"/>
    </default-field>
</field>
```

*Note*: Server-side validation should be handled by the service's `in-parameters` with `required="true"`, not in screen field elements. The invalid attributes are silently ignored by the framework.

#### Invalid btn-type Values

**Valid `btn-type` values are (from `color-context` type):**
- `default`
- `primary` (default if omitted)
- `success`
- `info`
- `warning`
- `danger`

**❌ WRONG: Using Bootstrap-style btn-type values**
```xml
<link url="action" text="Defer" btn-type="secondary"/>
<link url="action" text="Light" btn-type="light"/>
```

**✅ CORRECT: Use valid Moqui btn-type values**
```xml
<link url="action" text="Defer" btn-type="default"/>
<link url="action" text="Primary Action"/>  <!-- primary is default, can omit -->
```

#### Redundant Default Attribute Values

**Avoid specifying attributes that match their default values**

Common defaults to remember:
| Element | Attribute | Default Value |
|---------|-----------|---------------|
| `text-area` | `rows` | `3` |
| `text-area` | `cols` | `60` |
| `link` | `btn-type` | `primary` |
| `drop-down` | `allow-empty` | `true` |

**❌ WRONG: Redundant defaults**
```xml
<text-area rows="3" cols="50"/>
<link url="action" text="Submit" btn-type="primary"/>
```

**✅ CORRECT: Omit defaults, only specify non-default values**
```xml
<text-area cols="50"/>
<link url="action" text="Submit"/>
```

#### Dependent Dropdowns with `depends-on`

When a dropdown's options depend on another field's value, use `dynamic-options` with the `depends-on` child element. The dependent dropdown automatically refreshes when the parent field changes.

**✅ CORRECT: Using `depends-on` as child element**
```xml
<!-- Parent dropdown (organization) -->
<field name="organizationPartyId"><default-field title="Organization">
    <drop-down>
        <dynamic-options transition="getOrganizations"/>
    </drop-down>
</default-field></field>

<!-- Dependent dropdown (person) - refreshes when organization changes -->
<field name="partyId"><default-field title="Person">
    <drop-down>
        <dynamic-options transition="getPersonsByOrg" server-search="true" min-length="0">
            <depends-on field="organizationPartyId"/>
        </dynamic-options>
    </drop-down>
</default-field></field>
```

**❌ WRONG: Using `depends-on` as attribute**
```xml
<!-- This syntax is INVALID - depends-on is not an attribute -->
<dynamic-options transition="getPersonsByOrg" depends-on="organizationPartyId"/>
```

**Key `dynamic-options` Attributes and Elements:**

| Attribute/Element | Purpose |
|-------------------|---------|
| `transition` | Transition name that returns JSON options |
| `server-search="true"` | Enable server-side search (user types to filter) |
| `min-length="0"` | Show all options initially (without typing) |
| `depends-optional="true"` | Allow loading even when parent field is empty |
| `value-field` | JSON field for option value (default: `value`) |
| `label-field` | JSON field for option label (default: `label`) |
| `<depends-on field="..."/>` | **Child element** - field that triggers reload |

**Transition Response Format:**

The transition must return JSON array with `value` and `label` fields:
```groovy
// In transition actions
ec.web.sendJsonResponse(resultList.collect{[value:it.partyId, label:it.organizationName]})
```

**Multiple Dependencies:**

A dropdown can depend on multiple fields:
```xml
<dynamic-options transition="getFilteredOptions" server-search="true" min-length="0" depends-optional="true">
    <depends-on field="organizationPartyId"/>
    <depends-on field="departmentId"/>
</dynamic-options>
```

**Handling Empty Parent Values:**

When the parent field is empty, return an empty array immediately:
```xml
<transition name="getPersonsByOrg">
    <parameter name="organizationPartyId"/>
    <actions>
        <!-- Return empty if no organization selected -->
        <if condition="!organizationPartyId">
            <script>ec.web.sendJsonResponse([])</script>
            <return/>
        </if>

        <!-- Find related records -->
        <entity-find entity-name="SomeEntity" list="resultList">
            <econdition field-name="parentId" from="organizationPartyId"/>
        </entity-find>

        <!-- Also return empty if no results (avoid returning all with ignore-if-empty) -->
        <if condition="!resultList">
            <script>ec.web.sendJsonResponse([])</script>
            <return/>
        </if>

        <script>ec.web.sendJsonResponse(resultList.collect{[value:it.partyId, label:it.name]})</script>
    </actions>
    <default-response type="none"/>
</transition>
```

#### Screen Context and AJAX Transitions

##### Always-Actions Execution for AJAX Transitions

**Key Fact**: When an AJAX request is made to a screen transition (e.g., from `dynamic-options`), Moqui **traverses the full screen path** and **executes always-actions for each parent screen**. Context variables set in parent screen always-actions ARE available to the transition.

**Framework Code Reference** (`ScreenRenderImpl.groovy:730`):
```java
if (sd.alwaysActions != null) sd.alwaysActions.run(ec)
```

This executes for EVERY screen in the path, including for transition requests.

##### How Screen Path Traversal Works

```
AJAX Request: /qapps/Administracion/Task/FindTask/getOrganizations

Screen path execution order:
1. qapps (webroot)        → always-actions run (if any)
2. Administracion.xml     → always-actions run: sets SrceiAdminApp="true"
3. Task.xml               → always-actions run (if any)
4. FindTask.xml           → transition "getOrganizations" executes
                            ✓ Has access to SrceiAdminApp from step 2
```

**Result**: The `getOrganizations` transition can use `SrceiAdminApp` because it was set by a parent screen's always-actions during the same request.

##### Practical Example

**Parent Screen (Administracion.xml):**
```xml
<screen>
    <always-actions>
        <service-call name="MyServices.setup#UserOrganizationInfo" out-map="context"/>
        <set field="SrceiAdminApp" value="true"/>
    </always-actions>
    <subscreens>...</subscreens>
</screen>
```

**Child Screen Transition (FindTask.xml):**
```xml
<transition name="getOrganizations">
    <actions>
        <!-- SrceiAdminApp IS available here - set by parent's always-actions -->
        <if condition="SrceiAdminApp"><then>
            <!-- Admin gets all organizations -->
            <entity-find entity-name="mantle.party.PartyDetailAndRole" list="resultList">
                <econdition field-name="roleTypeId" value="OrgInternal"/>
            </entity-find>
        </then><else>
            <!-- Non-admin gets limited organizations -->
            <entity-find entity-name="mantle.party.PartyDetail" list="resultList">
                <econdition field-name="partyId" operator="in" from="jefaturaOrganizationPartyIdList"/>
            </entity-find>
        </else></if>
        <script>ec.web.sendJsonResponse(resultList.collect{[value:it.partyId, label:it.organizationName]})</script>
    </actions>
    <default-response type="none"/>
</transition>
```

##### When You Need Session Attributes Instead

Use `ec.web.sessionAttributes` when you need values that:
- Persist across multiple unrelated requests
- Are set by one screen path but needed by a different screen path
- Need to survive page refreshes or navigation

**Example - Values NOT Available via Always-Actions:**
```
User navigates: /qapps/Administracion/Settings → sets userPreference="dark"
Then navigates: /qapps/Jefatura/Task/FindTask   → userPreference NOT available

Why? Different screen paths - Administracion's always-actions don't run when accessing Jefatura
```

**Solution - Use Session Attributes:**
```xml
<!-- In Settings screen -->
<set field="ec.web.sessionAttributes.userPreference" value="dark"/>

<!-- In any other screen (different path) -->
<set field="userPref" from="ec.web.sessionAttributes.userPreference"/>
```

##### Debugging Context Issues

**Verification**: Add a log statement to confirm context availability:
```xml
<transition name="myTransition">
    <actions>
        <log level="info" message="Context check: SrceiAdminApp=${SrceiAdminApp}, jefaturaOrgList=${jefaturaOrganizationPartyIdList}"/>
        ...
    </actions>
</transition>
```

**Common Issues:**

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| Variable is null in transition | Different screen path than expected | Verify URL path matches parent screen hierarchy |
| Variable works in browser but not AJAX | N/A - always-actions run for both | Check for typos or conditional logic |
| Variable works in screen A, not B | Different parent screen paths | Use `ec.web.sessionAttributes` for cross-path values |

#### Context Persistence: Request vs Session Scope

**IMPORTANT: Understanding ExecutionContext Lifecycle**

The `ExecutionContext` (ec) is created **new for each HTTP request**. This means:
- `ec.user.context` does **NOT** persist across requests (it's recreated each time)
- `ec.user.context` is useful only within a single request for passing data between components

**For persistent context across requests**, use `ec.web.sessionAttributes`:

```xml
<!-- Store value in session (persists across requests) -->
<set field="ec.web.sessionAttributes.myPersistentValue" from="someValue"/>

<!-- Retrieve from session in any request -->
<set field="myValue" from="ec.web.sessionAttributes.myPersistentValue"/>
```

**For request-scoped context sharing**, use `ec.user.context`:

```xml
<!-- Store value for current request only -->
<set field="ec.user.context.tempValue" from="someValue"/>

<!-- Use within same request (e.g., between service calls) -->
<set field="myValue" from="ec.user.context.tempValue"/>
```

**When to use which:**
| Approach | Lifecycle | Use Case |
|----------|-----------|----------|
| Screen context variable | Current request, screen-path dependent | App-specific settings (e.g., `SrceiAdminApp`) |
| `ec.user.context` | Current request only (NOT persistent) | Passing data between services in same request |
| `ec.web.sessionAttributes` | HTTP session (persists across requests) | User preferences, cached data, app state |

**Framework Internals:**
- `ExecutionContextImpl` is created fresh for each request (`new ExecutionContextImpl(...)`)
- `UserFacadeImpl` and its `UserInfo.userContext` are part of the per-request ExecutionContext
- `ec.web.sessionAttributes` wraps the actual HTTP session (`HttpSession`) which persists

#### Form Field Requirements

**❌ WRONG: Using `required` attribute on field elements**
```xml
<form-single name="CreateForm" transition="createEntity">
    <field name="entityName" required="true">
        <default-field title="Name">
            <text-line size="60"/>
        </default-field>
    </field>
</form-single>
```

**✅ CORRECT: Requirements detected from service definition**
```xml
<!-- Screen: No required attribute needed -->
<form-single name="CreateForm" transition="createEntity">
    <field name="entityName">
        <default-field title="Name">
            <text-line size="60"/>
        </default-field>
    </field>
</form-single>

<!-- Service: Define required parameters here -->
<service verb="create" noun="Entity">
    <in-parameters>
        <parameter name="entityName" required="true"/>
        <parameter name="description"/>
    </in-parameters>
</service>
```

*Explanation*: Moqui automatically detects which fields are required by analyzing the service definition associated with the form's transition. The `required` attribute does not exist on `<field>` elements. If you need to make a field required, add `required="true"` to the corresponding `<parameter>` element in the service definition.

#### Text-line Fields

**❌ WRONG: Using `description` tag inside text-line**
```xml
<text-line>
    <description>Enter the API key name</description>
</text-line>
```

**✅ CORRECT: Use `default-value` or `prefix`**
```xml
<!-- Option 1: Default value -->
<text-line default-value="Enter API key name here"/>

<!-- Option 2: Prefix text -->
<text-line prefix="API-"/>
```

**❌ WRONG: Using `required` attribute on text-line**
```xml
<text-line required="true"/>
```

**✅ CORRECT: Handle requirements through service transitions**
```xml
<!-- The text-line itself has no required attribute -->
<text-line/>

<!-- Requirements are enforced in the transition service -->
<transition name="save">
    <service-call name="store#Entity"/>
    <!-- Service will enforce required fields -->
</transition>
```

#### Container-dialog Attributes

**❌ WRONG: Using `button-type` and `button-icon` attributes**
```xml
<container-dialog id="CreateDialog" button-type="primary" button-icon="fa fa-plus" button-text="Create">
    <!-- dialog content -->
</container-dialog>
```

**✅ CORRECT: Use `type` and `icon` attributes**
```xml
<container-dialog id="CreateDialog" type="primary" icon="fa fa-plus" button-text="Create">
    <!-- dialog content -->
</container-dialog>
```

#### Entity-options with Additional Options

**❌ WRONG: Placing `option` tags inside entity-options**
```xml
<entity-options>
    <entity-find entity-name="StatusItem">
        <econdition field-name="statusTypeId" value="OrderStatus"/>
    </entity-find>
    <option key="_NA_" text="Not Applicable"/>  <!-- WRONG placement -->
</entity-options>
```

**✅ CORRECT: Place `option` tags after entity-options**
```xml
<entity-options>
    <entity-find entity-name="StatusItem">
        <econdition field-name="statusTypeId" value="OrderStatus"/>
    </entity-find>
</entity-options>
<option key="_NA_" text="Not Applicable"/>  <!-- CORRECT placement -->
```

**Summary of Key Rules:**
1. **Drop-down fields**: Use `allow-empty="false"` instead of `required="true"`
2. **Text-line fields**:
   - Use `default-value` or `prefix` instead of `description` tag
   - No `required` attribute - handle in service/transition
3. **Container-dialog**: Use `type` and `icon` instead of `button-type` and `button-icon`
4. **Entity-options**: Place additional `option` tags after, not inside, `entity-options`

#### Dynamic-dialog vs Container-dialog Pattern

**IMPORTANT: When to Use dynamic-dialog Instead of container-dialog**

Use `dynamic-dialog` when you need the dialog content to be **rendered when the button is pressed**, not when the parent screen loads.

**Key Differences:**

| Feature | container-dialog | dynamic-dialog |
|---------|-----------------|----------------|
| **Rendering Time** | When parent screen loads | When button is clicked |
| **Default Values** | Set at parent screen render time | Set at button click time |
| **Performance** | Higher initial load time | Faster initial load, deferred dialog rendering |
| **Use Case** | Static content dialogs | Time-sensitive forms, dynamic content |

**When to Use dynamic-dialog:**

1. **Time-sensitive default values** (e.g., timestamps that should reflect "now" when dialog opens)
2. **Forms with many dynamic dialogs** (reduces parent screen rendering time)
3. **Content that depends on real-time data** (fetched when dialog opens)
4. **Resource-intensive dialogs** (complex forms, large datasets)

**❌ WRONG: Using container-dialog for timestamp-sensitive forms**

```xml
<!-- Parent Screen: EngagementDetail.xml -->
<field name="attendanceActions">
    <default-field>
        <container-dialog id="ArrivalDialog_${partyId}" button-text="Register Arrival" type="success">
            <form-single name="ArrivalForm_${partyId}" transition="recordArrival">
                <field name="arrivalTime">
                    <default-field title="Arrival Time">
                        <!-- This timestamp is set when the PARENT screen loads, not when button is clicked -->
                        <date-time type="timestamp" format="dd/MM/yyyy HH:mm" default-value="${ec.user.nowTimestamp}"/>
                    </default-field>
                </field>
                <field name="submitButton">
                    <default-field title="Record Arrival"><submit/></default-field>
                </field>
            </form-single>
        </container-dialog>
    </default-field>
</field>
```

**Problem:** If the parent screen (EngagementDetail) loads at 10:00 AM and the user clicks "Register Arrival" at 10:30 AM, the form will show 10:00 AM (parent screen load time) instead of 10:30 AM (current time).

**✅ CORRECT: Using dynamic-dialog with standalone screen**

```xml
<!-- Parent Screen: EngagementDetail.xml -->
<transition name="RegisterArrival">
    <default-response url="RegisterArrival"/>
</transition>

<field name="attendanceActions">
    <default-field>
        <dynamic-dialog id="ArrivalDialog_${partyId}"
                       button-text="Register Arrival"
                       transition="RegisterArrival"
                       parameter-map="[engagementId: engagementId, partyId: partyId]"
                       type="success"/>
    </default-field>
</field>
```

```xml
<!-- Standalone Dialog Screen: RegisterArrival.xml -->
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-include="false" standalone="true">

    <parameter name="engagementId" required="true"/>
    <parameter name="partyId" required="true"/>

    <transition name="recordArrival">
        <service-call name="mycompany.myapp.EngagementServices.record#Arrival"/>
        <default-response url="../EngagementDetail" parameter-map="[engagementId: engagementId]"/>
        <error-response url="."/>
    </transition>

    <actions>
        <!-- Get participant details for display -->
        <entity-find-one entity-name="mantle.party.PartyDetail" value-field="party">
            <field-map field-name="partyId" from="partyId"/>
        </entity-find-one>
        <set field="partyName" from="ec.resource.expand('PartyNameOnlyTemplate', null, party)"/>
    </actions>

    <widgets>
        <container-dialog id="RegisterArrivalDialog" button-text="Register Arrival" type="success">
            <form-single name="RegisterArrivalForm" transition="recordArrival">
                <field name="engagementId"><default-field><hidden/></default-field></field>
                <field name="partyId"><default-field><hidden/></default-field></field>

                <field name="participantName">
                    <default-field title="Participant">
                        <display text="${partyName}"/>
                    </default-field>
                </field>

                <field name="arrivalTime">
                    <default-field title="Arrival Time">
                        <!-- This timestamp is set when the DIALOG screen loads (button click time) -->
                        <date-time type="timestamp" format="dd/MM/yyyy HH:mm" default-value="${ec.user.nowTimestamp}"/>
                    </default-field>
                </field>

                <field name="submitButton">
                    <default-field title="Record Arrival"><submit/></default-field>
                </field>
            </form-single>
        </container-dialog>
    </widgets>
</screen>
```

**Benefits of dynamic-dialog Pattern:**

1. **Accurate Timestamps**: `${ec.user.nowTimestamp}` is evaluated when the dialog loads (button click), not when parent screen loads
2. **Reduced Parent Screen Load Time**: Dialog content isn't rendered until needed
3. **Fresh Data**: Any `entity-find` or computed values in the dialog screen's `actions` section run when dialog opens
4. **Resource Efficiency**: CPU and memory only used when user actually opens the dialog
5. **Better User Experience**: Timestamps and dynamic data reflect current state

**Standalone Screen Requirements for dynamic-dialog:**

1. **Must have** `standalone="true"` attribute
2. **Should have** `default-menu-include="false"` to prevent showing in navigation
3. **Must define** parameters passed from parent screen's `parameter-map`
4. **Must have** transition back to parent screen (or appropriate destination)

**Performance Considerations:**

- **Parent screen with 10 container-dialogs**: All 10 dialogs render on initial page load
- **Parent screen with 10 dynamic-dialogs**: Only active dialog renders when button is clicked
- **Impact**: In complex screens with many dialogs, dynamic-dialog can reduce initial page load time by 50-70%

**When container-dialog is Acceptable:**

1. Static content dialogs (help text, information displays)
2. Simple forms without time-sensitive defaults
3. Dialogs where pre-rendering improves UX (instant open on click)
4. Screens with only 1-2 dialogs (negligible performance impact)

### Screen Directory Structure and Subscreen Organization

**CRITICAL: Directory-Based Subscreens**

When organizing screens in directories with multiple subscreen files, Moqui requires a specific structure:

**✅ REQUIRED: Parent Screen File**
- **Any directory containing subscreens MUST have a parent screen file**
- **File name**: `{DirectoryName}.xml` (same as directory name + .xml)
- **Location**: Same level as the directory, not inside it
- **Purpose**: Defines how subscreens are organized and accessed

**Example Structure:**
```
screen/
├── admin/
│   ├── ApiKey/
│   │   ├── ApiKeyManagement.xml    # subscreen
│   │   ├── CreateApiKey.xml        # subscreen  
│   │   ├── ApiKeyDetail.xml        # subscreen
│   │   └── ApiKeyHistory.xml       # subscreen
│   └── ApiKey.xml                  # ← REQUIRED parent screen
```

**Parent Screen File Requirements**

**✅ MUST contain one of these subscreen organization tags:**

1. **`<subscreens-active>`** - Shows active subscreen based on path
```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">
    <subscreens default-item="ApiKeyManagement"/>
    <widgets>
        <subscreens-active/>
    </widgets>
</screen>
```

2. **`<subscreens-panel>`** - Shows subscreens in panel layout with navigation
```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">
    <subscreens default-item="ApiKeyManagement"/>
    <widgets>
        <subscreens-panel id="ApiKeyPanel"/>
    </widgets>
</screen>
```

**Common Mistakes to Avoid**

**❌ WRONG: Missing parent screen file**
```
# This will NOT work - no parent ApiKey.xml file
screen/admin/ApiKey/ApiKeyManagement.xml
screen/admin/ApiKey/CreateApiKey.xml
```

**❌ WRONG: Parent file inside directory**
```
# This will NOT work - parent file in wrong location
screen/admin/ApiKey/ApiKey.xml        # ← Wrong location
screen/admin/ApiKey/ApiKeyManagement.xml
```

**❌ WRONG: No subscreen organization tags**
```xml
<!-- This will NOT work - no subscreens-active or subscreens-panel -->
<screen>
    <widgets>
        <label text="API Key Management"/>
    </widgets>
</screen>
```

**Best Practices**

1. **Use `subscreens-active`** for most cases - simpler and more flexible
2. **Set `default-item`** to specify which subscreen loads by default  
3. **Keep parent screen minimal** - it's just for organization
4. **Use descriptive subscreen names** that match functionality

**Navigation Patterns**

**For URL routing:** `/admin/ApiKey/ApiKeyManagement`
- `/admin/ApiKey` - loads parent screen with default subscreen
- `/admin/ApiKey/CreateApiKey` - loads specific subscreen

### Screen URL Navigation Patterns

**CRITICAL: Understanding Moqui Screen URL Navigation**

Moqui screen navigation uses a path-based system where URL structure must match the screen file hierarchy. Understanding relative vs. absolute path patterns is essential for building working navigation links.

**Core Navigation Concepts**

1. **Screen Path Mapping**: URL paths directly map to screen file locations
2. **Relative Navigation**: Uses ".." for parent directories, like filesystem navigation
3. **Current Screen Context**: Links resolve relative to the current screen's location
4. **Parameter Passing**: Screen parameters are passed via URL parameters or parameter-map

**Navigation Path Types**

**✅ Relative Paths (Recommended for most cases)**

```xml
<!-- Sibling screens (same directory level) -->
<link url="../SiblingScreen" text="Go to Sibling"/>

<!-- Child screens (subdirectory) -->
<link url="ChildScreen" text="Go to Child"/>

<!-- Parent level screens (up one directory) -->
<link url="../../ParentScreen" text="Go to Parent"/>

<!-- Complex relative paths -->
<link url="../../../OtherArea/TargetScreen" text="Navigate to Other Area"/>
```

**✅ Absolute Paths (For cross-component navigation)**

```xml
<!-- Navigate to different component -->
<link url="/admin/system/Configuration" text="System Config"/>

<!-- Root-level screen -->
<link url="/dashboard" text="Main Dashboard"/>
```

**Common Navigation Scenarios**

**Scenario 1: Sibling Screen Navigation**
```
Current location: /screen/Admin/Governance/GovernanceEntity/FindEngagementTypeAssociation.xml
Target location:  /screen/Admin/Governance/GovernanceEntity/EngagementTypeAssociation.xml
URL pattern: ../EngagementTypeAssociation
```

```xml
<!-- ✅ CORRECT: Sibling screen navigation -->
<link url="../EngagementTypeAssociation" text="Edit Association"
      parameter-map="[entityId:entityId, configId:configId]"/>

<!-- ❌ WRONG: Missing relative path prefix -->
<link url="EngagementTypeAssociation" text="Edit Association"/>
```

**Scenario 2: Parent-Child Navigation**
```
Parent:  /screen/Admin/Governance/GovernanceEntity.xml
Child:   /screen/Admin/Governance/GovernanceEntity/FindEngagementTypeAssociation.xml
From parent to child: FindEngagementTypeAssociation
From child to parent: ../GovernanceEntity
```

```xml
<!-- ✅ CORRECT: Parent to child -->
<link url="FindEngagementTypeAssociation" text="Find Associations"/>

<!-- ✅ CORRECT: Child to parent -->
<link url="../GovernanceEntity" text="Back to Entity"/>
```

**Scenario 3: Cross-Directory Navigation**
```
Current: /screen/Admin/Governance/GovernanceEntity/Detail.xml
Target:  /screen/Admin/Reports/GovernanceReport.xml
URL: ../../Reports/GovernanceReport
```

```xml
<!-- ✅ CORRECT: Cross-directory navigation -->
<link url="../../Reports/GovernanceReport" text="View Reports"
      parameter-map="[entityId:entityId]"/>
```

**Screen Transition Navigation Patterns**

**Create/edit transitions MUST preserve the relevant entity ID in the response. Delete transitions should NOT (redirect to list instead).**

**Transitions use the same path resolution rules:**

```xml
<!-- ✅ CORRECT: Create redirects to detail with new ID -->
<transition name="createEntity">
    <service-call name="create#Entity"/>
    <default-response url="../EntityDetail" parameter-map="[entityId:entityId]"/>
    <error-response url="."/>
</transition>

<!-- ✅ CORRECT: Same-screen redirect preserving context ID -->
<transition name="addRelatedRecord">
    <service-call name="create#RelatedEntity"/>
    <default-response url=".">
        <parameter name="entityId"/>
    </default-response>
</transition>

<!-- ✅ CORRECT: Same-screen redirect mapping field name to screen parameter -->
<transition name="addExchangeRate">
    <service-call name="set#ExchangerateValue"/>
    <default-response url=".">
        <parameter name="uomId" from="fromCurrencyUomId"/>
    </default-response>
</transition>

<!-- ✅ CORRECT: Delete redirects to list without deleted ID -->
<transition name="deleteEntity">
    <service-call name="delete#Entity"/>
    <default-response url="../EntityList"/>
    <error-response url="."/>
</transition>

<!-- ✅ CORRECT: Complex navigation with multiple parameters -->
<transition name="processWorkflow">
    <service-call name="process#Workflow"/>
    <default-response url="../../Workflow/ProcessResult"
                     parameter-map="[workflowId:workflowId, resultId:resultId]"/>
</transition>
```

**Form and Widget Navigation**

**Form submissions and widget links follow the same patterns:**

```xml
<!-- ✅ CORRECT: Form navigation to sibling screen -->
<form-single name="EntityForm" transition="../saveEntity">
    <!-- form fields -->
</form-single>

<!-- ✅ CORRECT: Form-list with detail navigation -->
<form-list name="EntityList">
    <field name="actions">
        <default-field>
            <link url="../EntityDetail" text="View Details"
                  parameter-map="[entityId:entityId]"/>
        </default-field>
    </field>
</form-list>

<!-- ✅ CORRECT: Container dialog navigation -->
<container-dialog button-text="Add New">
    <form-single transition="../createEntity">
        <!-- form fields -->
    </form-single>
</container-dialog>
```

**Dynamic-Dialog URL Navigation Patterns**

**CRITICAL: Dynamic-dialog transitions to sibling screens MUST use "../" prefix**

When using `dynamic-dialog` to reference standalone dialog screens, the URL pattern depends on the screen location relative to the parent screen:

**❌ WRONG: Missing "../" for sibling screen**

```xml
<!-- Parent Screen: screen/Governance/Engagement/EngagementDetail.xml -->
<transition name="RegisterArrival">
    <!-- This looks for screen at: EngagementDetail/RegisterArrival.xml (WRONG - subdirectory) -->
    <default-response url="RegisterArrival"/>
</transition>

<dynamic-dialog id="ArrivalDialog"
               button-text="Register Arrival"
               transition="RegisterArrival"
               parameter-map="[engagementId: engagementId, partyId: partyId]"/>
```

**Problem:** Without "../", Moqui looks for the screen in a **subdirectory** named after the parent screen:
- Expected location: `screen/Governance/Engagement/EngagementDetail/RegisterArrival.xml`
- Actual location: `screen/Governance/Engagement/RegisterArrival.xml`
- Result: Screen not found (404 error)

**✅ CORRECT: Using "../" for sibling screen**

```xml
<!-- Parent Screen: screen/Governance/Engagement/EngagementDetail.xml -->
<!-- Sibling Screen: screen/Governance/Engagement/RegisterArrival.xml -->

<transition name="RegisterArrival">
    <!-- This looks for screen at: ../RegisterArrival.xml (CORRECT - sibling) -->
    <default-response url="../RegisterArrival"/>
</transition>

<dynamic-dialog id="ArrivalDialog"
               button-text="Register Arrival"
               transition="RegisterArrival"
               parameter-map="[engagementId: engagementId, partyId: partyId]"/>
```

**Directory Structure Example:**

```
screen/Governance/Engagement/
├── EngagementDetail.xml          # Parent screen
├── RegisterArrival.xml            # Sibling screen (dynamic-dialog)
├── RegisterDeparture.xml          # Sibling screen (dynamic-dialog)
└── EngagementList.xml             # Another sibling
```

**URL Resolution Rules for dynamic-dialog:**

| Screen Relationship | URL Pattern | Example |
|-------------------|-------------|---------|
| **Sibling screen** (same directory) | `../ScreenName` | `url="../RegisterArrival"` |
| **Subdirectory screen** | `ScreenName` or `SubDir/ScreenName` | `url="Create"` or `url="Detail/Edit"` |
| **Parent directory screen** | `../../ScreenName` | `url="../../EngagementList"` |
| **Different component** | `/ComponentPath/ScreenName` | `url="/admin/Configuration"` |

**Common dynamic-dialog Navigation Patterns:**

```xml
<!-- Pattern 1: Sibling standalone dialog screens -->
<transition name="EditDialog">
    <default-response url="../EditEntity"/>  <!-- Sibling -->
</transition>

<!-- Pattern 2: Subdirectory dialog screen -->
<transition name="DetailDialog">
    <default-response url="Detail/EntityDetail"/>  <!-- Subdirectory -->
</transition>

<!-- Pattern 3: Return to parent list from dialog -->
<!-- In standalone dialog screen: RegisterArrival.xml -->
<transition name="recordArrival">
    <service-call name="record#Arrival"/>
    <default-response url="../EngagementDetail" parameter-map="[engagementId: engagementId]"/>
    <error-response url="."/>
</transition>
```

**Real-World Example:**

```xml
<!-- Parent: screen/Governance/Engagement/EngagementDetail.xml -->
<transition name="RegisterArrival">
    <default-response url="../RegisterArrival"/>
</transition>

<transition name="RegisterDeparture">
    <default-response url="../RegisterDeparture"/>
</transition>

<!-- In participant list -->
<dynamic-dialog id="ArrivalDialog_${partyId}"
               button-text="Register Arrival"
               transition="RegisterArrival"
               parameter-map="[engagementId: engagementId, partyId: partyId]"
               type="success"/>
```

```xml
<!-- Standalone Dialog: screen/Governance/Engagement/RegisterArrival.xml -->
<screen standalone="true" default-menu-include="false">
    <parameter name="engagementId" required="true"/>
    <parameter name="partyId" required="true"/>

    <transition name="recordArrival">
        <service-call name="record#Arrival"/>
        <!-- Return to parent screen (sibling) -->
        <default-response url="../EngagementDetail" parameter-map="[engagementId: engagementId]"/>
        <error-response url="."/>
    </transition>

    <!-- Dialog content -->
</screen>
```

**Why This Matters:**

1. **Incorrect URLs cause 404 errors** - Dialog won't open or shows error
2. **Path resolution is relative to current screen** - Not to the application root
3. **Sibling screens require "../"** - To navigate to the parent directory first
4. **Subdirectory screens don't need "../"** - Direct child path works

**Quick Decision Guide:**

- Are the screens in the **same directory**? → Use `../ScreenName`
- Is the target screen in a **subdirectory** of the current screen? → Use `ScreenName` or `SubDir/ScreenName`
- Is the target screen in a **parent directory**? → Use `../../ScreenName`
- Is the target screen in a **different component**? → Use absolute path `/component/ScreenName`

**Best Practices for Screen Navigation**

1. **Use relative paths** for screens within the same component
2. **Use absolute paths** only for cross-component navigation
3. **Always test navigation links** to ensure they resolve correctly
4. **Be consistent with parameter naming** across related screens
5. **Use meaningful parameter names** that clearly identify the data being passed
6. **For dynamic-dialog to sibling screens**: Always use "../" prefix in transition URLs
6. **Document complex navigation flows** in component documentation

**Common Navigation Mistakes**

**❌ WRONG: Missing relative path prefix for sibling screens**
```xml
<!-- This tries to find a child screen, not a sibling -->
<link url="SiblingScreen" text="Navigate"/>
```

**❌ WRONG: Incorrect path depth**
```xml
<!-- Too many "../" or not enough -->
<link url="../../../../DeepScreen" text="Navigate"/>
```

**❌ WRONG: Mixing absolute and relative incorrectly**
```xml
<!-- Don't mix patterns - be consistent -->
<link url="/relative/path" text="Navigate"/>  <!-- Leading slash makes it absolute -->
```

**❌ WRONG: Hardcoded component paths**
```xml
<!-- Avoid hardcoding component names in paths -->
<link url="/component-name/screen/Detail" text="Navigate"/>
```

#### Transition Parameter Passing for Screen Navigation

**CRITICAL: When transitions redirect to screens requiring parameters, those parameters must be available in the form context.**

When a transition redirects to a screen (like a detail screen) that has required `<parameter>` definitions, the form submitting to that transition must include those parameter values as hidden fields or hidden-parameters.

**Required Parameter Flow:**

1. **Target Screen**: Has required parameters defined
2. **Source Form**: Must include parameter values
3. **Transition**: Passes parameters from form to target screen
4. **Result**: Target screen receives required parameters successfully

**✅ CORRECT: Form-single with hidden fields for required parameters**

```xml
<!-- Target screen requires: partyId, configurationId -->
<screen>
    <parameter name="partyId" required="true"/>
    <parameter name="configurationId" required="true"/>
</screen>

<!-- Source form must include these as hidden fields -->
<form-single name="EditForm" transition="saveAndGoToDetail">
    <!-- Required parameters as hidden fields -->
    <field name="partyId"><default-field><hidden/></default-field></field>
    <field name="configurationId"><default-field><hidden/></default-field></field>

    <!-- Visible form fields -->
    <field name="description">
        <default-field><text-line/></default-field>
    </field>

    <field name="submitButton">
        <default-field><submit text="Save & Continue"/></default-field>
    </field>
</form-single>

<!-- Transition that redirects to detail screen -->
<transition name="saveAndGoToDetail">
    <service-call name="update#MyEntity"/>
    <default-response url="MyEntityDetail" parameter-map="[partyId:partyId, configurationId:configurationId]"/>
</transition>
```

**✅ CORRECT: Form-list with hidden-parameters for required parameters**

```xml
<!-- Form-list for bulk operations redirecting to detail screen -->
<form-list name="EntityList" list="entityList">
    <!-- Required parameters for target screen -->
    <hidden-parameters>
        <parameter name="partyId" from="partyId"/>
        <parameter name="configurationId" from="configurationId"/>
    </hidden-parameters>

    <!-- Display fields -->
    <field name="name">
        <default-field><display/></default-field>
    </field>

    <!-- Action that redirects to detail screen -->
    <field name="actions">
        <default-field>
            <link url="viewDetail" text="View Details"
                  parameter-map="[partyId:partyId, configurationId:configurationId]"/>
        </default-field>
    </field>
</form-list>
```

**❌ INCORRECT: Missing required parameters causes navigation failure**

```xml
<!-- Target screen requires partyId, configurationId -->
<form-single name="EditForm" transition="saveAndGoToDetail">
    <!-- Missing hidden fields for required parameters -->

    <field name="description">
        <default-field><text-line/></default-field>
    </field>

    <field name="submitButton">
        <default-field><submit text="Save & Continue"/></default-field>
    </field>
</form-single>

<!-- Transition will fail because parameters are not available -->
<transition name="saveAndGoToDetail">
    <service-call name="update#MyEntity"/>
    <!-- ERROR: partyId and configurationId are not in context -->
    <default-response url="MyEntityDetail"/>
</transition>
```

**Parameter Passing Strategies:**

**Strategy 1: Include required parameters as hidden fields**
```xml
<field name="requiredParamId"><default-field><hidden/></default-field></field>
```

**Strategy 2: Use parameter-map in transition response**
```xml
<default-response url="targetScreen" parameter-map="[id:recordId, type:entityType]"/>
```

**Strategy 3: Use parameter-map in links**
```xml
<link url="detail" parameter-map="[partyId:partyId, configId:configId]" text="View"/>
```

**Strategy 4: Pass parameters through parent screen context**
```xml
<!-- Parent screen sets parameters available to child transitions -->
<parameter name="parentId" from="organizationId"/>
```

**Common Parameter Passing Patterns:**

| Form Type | Method | Usage |
|-----------|--------|-------|
| `form-single` | `<field><hidden/></field>` | Required parameters for transitions |
| `form-list` | `<hidden-parameters>` | Bulk operations with shared parameters |
| `link` | `parameter-map="[key:value]"` | Direct navigation with parameters |
| `transition` | `parameter-map` in response | Pass parameters to target screen |

**Debugging Parameter Issues:**

When navigation fails with "required parameter missing":

1. **Check target screen** parameter definitions
2. **Verify form includes** required parameters as hidden fields
3. **Confirm parameter names** match exactly between form and screen
4. **Test parameter values** are correctly set in form context
5. **Review transition** parameter-map mapping

**Testing Navigation Links**

Always verify navigation links work correctly:

1. **Manual testing**: Click through all navigation links
2. **Parameter validation**: Ensure required parameters are passed correctly
3. **Error scenarios**: Test navigation when data is missing or invalid
4. **Cross-browser testing**: Verify links work across different browsers
5. **Mobile testing**: Ensure navigation works on mobile devices

#### Dynamic Dialog Pattern for Complex Content

**CRITICAL: Use `dynamic-dialog` instead of `container-dialog` for complex content that requires its own screen definition.**

The `dynamic-dialog` element loads content from a separate standalone screen when the dialog button is clicked, providing better separation of concerns and more maintainable code.

**Dynamic Dialog Components:**

1. **Main Screen**: Contains the `dynamic-dialog` element with transition reference
2. **Dialog Screen**: Standalone screen file (marked with `standalone="true"`) containing dialog content
3. **Dialog Directory**: Screen file placed in subdirectory named after parent screen

**✅ CORRECT: Using dynamic-dialog for complex content**

```xml
<!-- Main Screen: ParentScreen.xml -->
<screen>
    <widgets>
        <form-list name="ItemList">
            <field name="actions">
                <default-field>
                    <!-- Dynamic dialog loads from separate screen file -->
                    <dynamic-dialog id="HistoryDialog_${itemId}" button-text="View History" type="info" width="800"
                                  transition="ViewHistory" parameter-map="[itemId:itemId]"/>
                </default-field>
            </field>
        </form-list>
    </widgets>
</screen>

<!-- Dialog Screen: ParentScreen/ViewHistory.xml -->
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        standalone="true">

    <parameter name="itemId" required="true"/>

    <actions>
        <!-- Complex logic for dialog content -->
        <entity-find entity-name="ItemHistory" list="historyList">
            <econdition field-name="itemId"/>
            <order-by field-name="-effectiveDate"/>
        </entity-find>
    </actions>

    <widgets>
        <label text="History for Item ${itemId}" type="h4"/>
        <form-list name="HistoryList" list="historyList">
            <field name="effectiveDate"><default-field><display format="dd/MM/yyyy HH:mm"/></default-field></field>
            <field name="value"><default-field><display/></default-field></field>
            <field name="status"><default-field><display/></default-field></field>
        </form-list>
    </widgets>
</screen>
```

**❌ INCORRECT: Using container-dialog for complex content**

```xml
<!-- Embeds complex content directly in main screen -->
<container-dialog id="HistoryDialog" button-text="View History">
    <!-- Complex form-list and actions embedded directly -->
    <actions>
        <!-- This breaks schema - actions not allowed in container-dialog -->
        <entity-find entity-name="ItemHistory" list="historyList"/>
    </actions>
    <form-list name="HistoryList" list="historyList">
        <!-- Inline content makes main screen complex and hard to maintain -->
    </form-list>
</container-dialog>
```

**Dynamic Dialog File Structure:**

```
screen/
├── ParentScreen.xml                    (Main screen with dynamic-dialog)
└── ParentScreen/                       (Dialog screens directory)
    ├── ViewHistory.xml                 (Dialog screen: standalone="true")
    ├── EditDetails.xml                 (Another dialog screen)
    └── ConfirmAction.xml               (Another dialog screen)
```

**Dynamic Dialog Attributes:**

- `id` (required) - Unique identifier for the dialog
- `button-text` (required) - Text displayed on the dialog trigger button
- `transition` (required) - Screen name or transition to load dialog content
- `parameter-map` - Parameters to pass to the dialog screen
- `width` - Dialog width (default: "760")
- `height` - Dialog height (default: "600")
- `type` - Button color type using color-context values

**Benefits of Dynamic Dialog Pattern:**

1. **Separation of Concerns**: Complex dialog logic in separate screen files
2. **Maintainability**: Easier to modify dialog content independently
3. **Reusability**: Dialog screens can be used from multiple parent screens
4. **Performance**: Dialog content loaded only when needed
5. **Schema Compliance**: Avoids embedding complex content in container-dialog

**When to Use Dynamic Dialog:**

- Dialog contains form-list with complex logic
- Dialog requires actions element for data preparation
- Dialog content is substantial enough to warrant separate screen
- Dialog might be reused from other screens
- Dialog has its own navigation or workflow

**When to Use Container Dialog:**

- Simple forms with few fields
- Static content that doesn't require server-side processing
- Quick edit dialogs with minimal logic
- Content that is tightly coupled to parent screen context

### Screen Extension Patterns (screen-extend)

**CRITICAL: Understanding Screen Extension Widget Behavior**

When extending screens from other components using `<screen-extend>`, the `<widgets>` section has **strict limitations**:

- **`<widgets>` can ONLY contain**: `form-single`, `form-list`, `section`, `section-iterate`
- These elements **ONLY modify existing widgets** with matching `name` attributes - they do NOT add new widgets
- You **CANNOT** add arbitrary elements like `container-dialog`, `container`, `link`, etc. in `<widgets>`

To add **any** new widgets (including sections, forms, dialogs, containers, links), you **MUST** use `<widgets-extend>`.

**Core Extension Concepts**

1. **Allowed in `<widgets>`** (merge/override by name only):
   - `form-single` - merges fields into existing form with same name
   - `form-list` - merges fields into existing form-list with same name
   - `section` - overrides existing section with same name
   - `section-iterate` - overrides existing section-iterate with same name
2. **Widget Insertion**: Use `<widgets-extend>` to insert **any** new widgets at specific locations
3. **Transition Extension**: Transitions can be added directly (no special syntax needed)
4. **Actions Extension**: Use `<actions-extend>` with `when` and `type` attributes

**Widget Override Pattern (Modifies Existing Widgets)**

```xml
<screen-extend xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">

    <widgets>
        <!-- This MODIFIES the existing EditTask form, doesn't add a new one -->
        <form-single name="EditTask">
            <field name="assignToPartyId"><default-field title="Assign To">
                <ignored/>
            </default-field></field>
        </form-single>
    </widgets>
</screen-extend>
```

**❌ WRONG: Adding New Widgets Without widgets-extend**

```xml
<screen-extend>
    <widgets>
        <!-- This dialog will NOT appear because it's a new widget, not a modification -->
        <container-dialog id="CloneTaskDialog" button-text="Clonar Tarea">
            <form-single name="CloneTask" transition="cloneTask">
                <!-- fields -->
            </form-single>
        </container-dialog>
    </widgets>
</screen-extend>
```

**✅ CORRECT: Using widgets-extend to Add New Widgets**

```xml
<screen-extend>
    <transition name="cloneTask">
        <service-call name="SRCeIProjectServices.clone#Task" in-map="context"/>
        <default-response url="."/>
    </transition>

    <actions>
        <!-- Actions to prepare data for the clone dialog -->
        <entity-find entity-name="mantle.work.effort.WorkEffortParty" list="taskAssigneeList" limit="1">
            <econdition field-name="workEffortId" from="workEffortId"/>
            <econdition field-name="roleTypeId" value="Assignee"/>
            <date-filter/>
            <order-by field-name="-fromDate"/>
        </entity-find>
        <set field="currentAssignee" from="taskAssigneeList?.getAt(0)"/>
    </actions>

    <!-- Use widgets-extend to INSERT the new dialog -->
    <widgets-extend name="EditTask" where="before">
        <container-dialog id="CloneTaskDialog" button-text="Clonar Tarea">
            <form-single name="CloneTask" transition="cloneTask" map="task">
                <field name="baseWorkEffortId" from="workEffortId"><default-field><hidden/></default-field></field>
                <field name="workEffortName"><default-field title="Nombre"><text-line size="60"/></default-field></field>
                <field name="assignToPartyId" from="currentAssignee?.partyId"><default-field title="Asignar a">
                    <drop-down allow-empty="true">
                        <dynamic-options transition="getProjectParties" value-field="partyId" label-field="name"
                                         server-search="true" min-length="0">
                            <depends-on field="rootWorkEffortId"/></dynamic-options>
                    </drop-down>
                </default-field></field>
                <field name="estimatedStartDate"><default-field title="Fecha de Inicio">
                    <date-time type="date" format="yyyy-MM-dd"/></default-field></field>
                <field name="estimatedCompletionDate"><default-field title="Fecha de Término">
                    <date-time type="date" format="yyyy-MM-dd"/></default-field></field>
                <field name="submitButton"><default-field title="Clonar Tarea">
                    <submit confirmation="¿Clonar la tarea?"/></default-field></field>
            </form-single>
        </container-dialog>
    </widgets-extend>

    <!-- Still use widgets section to modify existing widgets -->
    <widgets>
        <form-single name="EditTask">
            <field name="assignToPartyId"><default-field title="Assign To">
                <ignored/>
            </default-field></field>
        </form-single>
    </widgets>
</screen-extend>
```

**widgets-extend Attributes**

- `name` (required): Match against `name` or `id` attributes on elements in the base screen being extended
- `where` (optional, default: `"before"`): Where to insert relative to the named widget
  - `"before"`: Insert before the named widget
  - `"after"`: Insert after the named widget

**Common Use Cases**

1. **Adding Dialog Buttons**: Insert clone/copy dialogs near edit forms
   ```xml
   <widgets-extend name="EditProject" where="before">
       <container-dialog id="CloneProjectDialog" button-text="Clonar Proyecto">
           <!-- dialog content -->
       </container-dialog>
   </widgets-extend>
   ```

2. **Adding Action Buttons**: Insert buttons before or after forms
   ```xml
   <widgets-extend name="TaskList" where="after">
       <container>
           <link url="exportTasks" text="Export to Excel"/>
       </container>
   </widgets-extend>
   ```

3. **Adding Information Sections**: Insert help text or warnings
   ```xml
   <widgets-extend name="ImportForm" where="before">
       <container>
           <label text="Warning: This will overwrite existing data" type="warning"/>
       </container>
   </widgets-extend>
   ```

**Key Differences Summary**

| Extension Type | Purpose | Allowed Elements | Use Case |
|---------------|---------|------------------|----------|
| `<widgets>` | Modify existing widgets by name | `form-single`, `form-list`, `section`, `section-iterate` only | Override fields in existing forms, hide sections |
| `<widgets-extend>` | Insert new widgets at specific locations | Any widget element | Add dialogs, buttons, containers, links, new sections |
| `<transition>` | Add/override transitions | N/A | Add new service calls or screen navigation |
| `<actions-extend>` | Add actions before/after/replace | N/A | Prepare data needed by extended widgets |

**Best Practices**

1. **Always use widgets-extend for new widgets**: The `<widgets>` section can ONLY contain `form-single`, `form-list`, `section`, `section-iterate` - and only to modify existing ones
2. **Reference existing widget names**: The `name` attribute in `widgets-extend` must match a `name` or `id` attribute on an element in the base screen
3. **Check base screen structure**: Read the base screen to understand widget names and structure before extending
4. **Choose where carefully**: Choose `before`/`after` based on desired UI layout
5. **Combine both patterns**: Use `<widgets>` to modify existing widgets AND `<widgets-extend>` to add new ones in the same screen-extend file

**Troubleshooting**

**Problem**: Widget doesn't appear in extended screen
- **Check 1**: Is it a new widget? Use `<widgets-extend>` instead of `<widgets>`
- **Check 2**: Is the widget type allowed in `<widgets>`? Only `form-single`, `form-list`, `section`, `section-iterate` are allowed
- **Check 3**: Does the reference widget name exist in the base screen?
- **Check 4**: Is the `where` attribute valid (`before`/`after`)?

**Problem**: Widget appears but in wrong location
- **Check 1**: Verify reference widget `name` matches intended insertion point (matches `name` or `id` in base screen)
- **Check 2**: Try different `where` values (`before` vs `after`)
- **Check 3**: Review base screen structure to understand widget hierarchy

## Screen Documentation (In-App Help)

Moqui includes a built-in screen documentation system that attaches user-facing help content to screens. When documentation exists for a screen, a help button appears in the Quasar navbar.

### Entity: `moqui.screen.ScreenDocument`

| Field | Type | PK | Description |
|-------|------|----|-------------|
| `screenLocation` | text-medium | Yes | `component://` path to the screen XML file |
| `docIndex` | number-integer | Yes | Sort order and identifier within a screen |
| `locale` | text-short | No | Locale code (e.g., `es`); null = all locales |
| `docTitle` | text-medium | No | Display title; defaults to filename if omitted |
| `docLocation` | text-medium | No | `component://` path to the Markdown file |

### How the Framework Renders Screen Documentation

1. **Auto-registered `screenDoc` transition**: `ScreenDefinition` automatically registers a `screenDoc` transition on every screen (alongside `formSelectColumns`, `formSaveFind`, etc.)
2. **Document list query**: `getScreenDocumentInfoList()` queries `moqui.screen.ScreenDocument` by `screenLocation`, filters by user locale, and returns `[title, index]` pairs
3. **Navbar integration**: The `screenDocList` is included in the `navMenuList` response; the Quasar UI renders a help button when documents exist
4. **Content rendering**: Clicking a help topic calls the `screenDoc` transition with `?docIndex=N`; the framework loads the `docLocation` resource and renders it via `resourceFacade.template()`

### Directory Convention

Documentation files live in `{component}/document/` and mirror the `{component}/screen/` path structure:

```
{component}/
  screen/
    MyApp/
      FindOrder.xml
      OrderDetail.xml
  document/
    MyApp/
      FindOrder.md
      OrderDetail/
        ShippingInfo.md
        PaymentDetails.md
```

### Data Record Placement

`ScreenDocument` records are configuration data (`use="configuration"`) and belong in the component's setup data file (type `seed` or `seed-initial`):

```xml
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="1" docTitle="Help Topic Title"
    docLocation="component://{component-name}/document/{AppName}/{ScreenName}.md"/>
```

Multiple documents per screen are ordered by `docIndex`. When `docTitle` is omitted, the framework extracts the title from the filename in `docLocation`.

### Reusable General Documentation

Common form-list feature documentation can be shared across components. For example, general docs for column customization, saved searches, and Excel export can be referenced from any screen:

```xml
<moqui.screen.ScreenDocument screenLocation="..." docIndex="10"
    docTitle="Columnas" docLocation="component://moit-utils/document/General/Columnas.md"/>
<moqui.screen.ScreenDocument screenLocation="..." docIndex="11"
    docTitle="Busquedas Guardadas" docLocation="component://moit-utils/document/General/BusquedasGuardadas.md"/>
<moqui.screen.ScreenDocument screenLocation="..." docIndex="12"
    docTitle="XLS" docLocation="component://moit-utils/document/General/XLS.md"/>
```

### When to Add Screen Documentation

- Any screen with `form-list` that has `select-columns`, `saved-finds`, or `show-xlsx-button` should reference the corresponding general docs
- Screens with business-specific fields or complex workflows should have their own documentation
- Detail screens with multiple sections benefit from per-section help topics

**Standard Reference**: See `standards/frontend/screen-documents.md` for the complete convention, checklist, and docIndex numbering guidelines.

## XML DSL Default Attributes Best Practices

### Avoid Specifying Default Values

When using Moqui's XML DSL, **never specify attributes with their default values**. This reduces clutter, improves readability, and makes non-default values more obvious.

### Common Default Attributes to AVOID

**❌ DON'T specify these defaults:**

```xml
<!-- DON'T: level="info" is the default for log -->
<log level="info" message="Processing started"/>

<!-- DON'T: type="String" is the default for parameters -->
<parameter name="partyId" type="String"/>

<!-- DON'T: required="false" is the default -->
<parameter name="optionalParam" required="false"/>

<!-- DON'T: operator="equals" is the default for econdition -->
<econdition field-name="partyId" operator="equals" from="partyId"/>

<!-- DON'T: for-update="false" is the default -->
<entity-find entity-name="Party" for-update="false"/>

<!-- DON'T: distinct="false" is the default -->
<entity-find entity-name="Party" distinct="false"/>

<!-- DON'T: use-clone="false" is the default -->
<entity-find entity-name="Party" use-clone="false"/>

<!-- DON'T: transaction="use-or-begin" is the default -->
<service-call name="service.name" transaction="use-or-begin"/>

<!-- DON'T: authenticate="true" is the default -->
<service verb="get" noun="Data" authenticate="true"/>

<!-- DON'T: validate="true" is the default -->
<service verb="update" noun="Data" validate="true"/>

<!-- DON'T: paginate="true" is the default for form-list -->
<form-list name="PartyList" paginate="true">

<!-- DON'T: skip-form="false" is the default for form-list -->
<form-list name="PartyList" skip-form="false">

<!-- DON'T: header-dialog="false" is the default for form-list -->
<form-list name="PartyList" header-dialog="false">
```

#### Form-Single vs Form-List Attribute Differences

**CRITICAL: Some attributes are only valid for form-list and NOT for form-single.**

**❌ WRONG: Using form-list-only attributes in form-single**
```xml
<!-- skip-form is ONLY valid for form-list, NOT form-single -->
<form-single name="AuditForm" map="entityMap" skip-form="true">
    <!-- This will cause validation errors -->
</form-single>

<!-- header-dialog is ONLY valid for form-list, NOT form-single -->
<form-single name="CreateForm" header-dialog="true">
    <!-- This will cause validation errors -->
</form-single>

<!-- paginate is ONLY valid for form-list, NOT form-single -->
<form-single name="DetailForm" paginate="true">
    <!-- This will cause validation errors -->
</form-single>
```

**✅ CORRECT: Using proper attributes for each form type**
```xml
<!-- form-single: Use only supported attributes -->
<form-single name="AuditForm" map="entityMap">
    <!-- Display-only form without skip-form attribute -->
</form-single>

<!-- form-list: Can use all list-specific attributes -->
<form-list name="EntityList" list="entityList" skip-form="true" header-dialog="true" paginate="true">
    <!-- List form with all supported attributes -->
</form-list>
```

**Form-List Only Attributes:**
- `skip-form`: Controls whether form submission is enabled
- `header-dialog`: Enables header dialog for creation
- `paginate`: Controls pagination behavior
- `list`: Specifies the list data source

**Common Attributes (both form-single and form-list):**
- `name`: Form identifier
- `map`: Data source for form-single
- `transition`: Target transition for form submission

#### Screen/Transition Parameter vs Service Parameter Differences

**CRITICAL: Screen and transition `<parameter>` elements have different attributes than service `<parameter>` elements.**

Screen/transition parameters (in `<screen>` and `<transition>` elements) only support:
- `name` (required): Parameter name
- `required` (optional): Whether the parameter is required (boolean)

Service parameters (in `<service>` and `<in-parameters>`/`<out-parameters>` elements) support additional attributes:
- `name` (required): Parameter name
- `required` (optional): Whether the parameter is required (boolean)
- `type` (optional): Data type (String, Integer, Boolean, List, Map, etc.)
- `default` or `default-value` (optional): Default value
- `entity-name`, `field-name` (optional): Entity field reference
- And many others

**❌ WRONG: Using service parameter attributes in transition parameters**
```xml
<transition name="downloadDocument">
    <parameter name="signatureRequestId" required="true"/>
    <!-- WRONG: type and default are NOT valid for transition parameters -->
    <parameter name="signed" type="Boolean" default="false"/>
    <actions>
        <!-- ... -->
    </actions>
</transition>
```

**✅ CORRECT: Only use name and required for transition parameters**
```xml
<transition name="downloadDocument">
    <parameter name="signatureRequestId" required="true"/>
    <!-- Just name (and optionally required) for transition parameters -->
    <parameter name="signed"/>
    <actions>
        <!-- Handle string value comparison since all URL parameters are strings -->
        <set field="isSigned" from="signed == 'true'"/>
        <!-- ... -->
    </actions>
</transition>
```

**Note**: URL parameters always come as strings, so when checking boolean-like parameters, compare as strings: `signed == 'true'` not `signed == true`.

#### File Downloads in Transitions

To serve a file for download from a transition, use `ec.web.sendResourceResponse()` with `default-response type="none"`:

**Method Signatures:**
- `ec.web.sendResourceResponse(String location)` - Downloads with filename from resource
- `ec.web.sendResourceResponse(String location, boolean inline)` - `inline=true` displays in browser, `inline=false` downloads

**❌ WRONG: Invalid method signature**
```xml
<transition name="downloadFile">
    <actions>
        <!-- WRONG: sendResourceResponse does NOT accept a filename parameter -->
        <script>ec.web.sendResourceResponse(documentLocation, false, "custom-name.pdf")</script>
    </actions>
    <default-response type="none"/>
</transition>
```

**✅ CORRECT: Use proper method signature**
```xml
<transition name="downloadFile">
    <parameter name="documentId" required="true"/>
    <actions>
        <entity-find-one entity-name="MyDocument" value-field="document"/>
        <if condition="!document?.contentLocation">
            <return error="true" message="Document not found"/>
        </if>
        <set field="resourceRef" from="ec.resource.getLocationReference(document.contentLocation)"/>
        <if condition="!resourceRef?.exists">
            <return error="true" message="File not found"/>
        </if>
        <!-- Download file (filename comes from resource reference) -->
        <script>ec.web.sendResourceResponse(document.contentLocation, false)</script>
    </actions>
    <!-- type="none" prevents redirect after file is sent -->
    <default-response type="none"/>
</transition>
```

**Key points:**
- Always use `<default-response type="none"/>` to prevent redirect after sending file
- The filename for download is derived from the resource reference, not a parameter
- Use `inline=false` for download, `inline=true` for browser display (e.g., PDF viewer)
- Always validate resource exists before calling sendResourceResponse

#### Mantle Content Entity Architecture

**CRITICAL: Mantle does NOT have a centralized `Content` entity.** Each domain has its own content association entity that stores the `contentLocation` directly.

**❌ WRONG: Assuming a centralized Content entity exists**
```xml
<!-- This entity does NOT exist -->
<entity-find-one entity-name="mantle.party.content.Content" value-field="content">
    <field-map field-name="contentId"/>
</entity-find-one>
```

**Common Non-Existent Entity References to Avoid:**
- `mantle.party.content.Content` - Does not exist
- `mantle.content.Content` - Does not exist
- `moqui.content.Content` - Does not exist

**✅ CORRECT: Use the domain-specific content entity directly**

Each content association entity stores `contentLocation` directly (not via a reference to another entity):

| Entity | Purpose | Key Fields |
|--------|---------|------------|
| `mantle.work.effort.WorkEffortContent` | Work effort/ticket attachments | `workEffortContentId`, `workEffortId`, `contentLocation`, `description`, `contentDate` |
| `mantle.party.PartyContent` | Party documents/images | `partyContentId`, `partyId`, `contentLocation`, `contentTypeEnumId` |
| `mantle.product.ProductContent` | Product images/documents | `productContentId`, `productId`, `contentLocation`, `contentTypeEnumId` |
| `mantle.request.RequestContent` | Request attachments | `requestContentId`, `requestId`, `contentLocation` |
| `mantle.order.OrderContent` | Order documents | `orderContentId`, `orderId`, `contentLocation` |

**Example: Displaying WorkEffortContent attachments**
```xml
<!-- Query content directly from the association entity -->
<entity-find entity-name="mantle.work.effort.WorkEffortContent" list="attachmentsList">
    <econdition field-name="workEffortId" from="workEffortId"/>
    <order-by field-name="-contentDate"/>
</entity-find>

<!-- Display in form-list -->
<form-list name="AttachmentsList" list="attachmentsList" skip-form="true">
    <row-actions>
        <!-- Extract filename from contentLocation path -->
        <set field="fileName" from="contentLocation ? contentLocation.substring(contentLocation.lastIndexOf('/') + 1) : workEffortContentId"/>
    </row-actions>

    <field name="fileName">
        <default-field title="File Name">
            <display text="${description ?: fileName}"/>
        </default-field>
    </field>

    <field name="contentDate">
        <default-field title="Uploaded">
            <display format="dd/MM/yyyy HH:mm"/>
        </default-field>
    </field>

    <field name="download">
        <default-field title="">
            <link url="downloadAttachment" text="Download">
                <parameter name="contentLocation"/>
            </link>
        </default-field>
    </field>
</form-list>
```

**Download transition for content:**
```xml
<transition name="downloadAttachment" read-only="true">
    <parameter name="contentLocation" required="true"/>
    <actions>
        <script>ec.web.sendResourceResponse(contentLocation)</script>
    </actions>
    <default-response type="none"/>
</transition>
```

**Key points:**
- Content association entities store `contentLocation` directly (e.g., `dbresource://...`)
- There is no need to look up a separate Content entity
- Use `description` field for user-friendly display names, or extract filename from path
- The `contentDate` field records when content was attached (not `fromDate`)
- Always use `read-only="true"` on download transitions called via links

### Form Reuse Patterns

**CRITICAL: Moqui does NOT support:**
- `include-form` tag (doesn't exist)
- `subscreens-item` for forms (only for screens)
- Standalone form XML files in `/form/` directories
- Cross-file form extension using `component://` syntax

**Correct approaches for form reuse:**

#### Option 1: Form Extension Within Same Screen (RECOMMENDED)

Define a base form in the same screen file and extend it for variations:

```xml
<!-- In the same screen file -->
<widgets>
    <!-- Base form - not directly rendered -->
    <form-single name="BaseEngagementForm" transition="createEngagement">
        <field name="workEffortName">
            <default-field title="Title">
                <text-line size="60" maxlength="255"/>
            </default-field>
        </field>
        <field name="description">
            <default-field title="Description">
                <text-area rows="4" cols="80"/>
            </default-field>
        </field>
        <field name="estimatedStartDate">
            <default-field title="Start Date">
                <date-time/>
            </default-field>
        </field>
        <!-- More fields... -->
    </form-single>

    <!-- Quick form - extends base form -->
    <container-dialog id="QuickCreateDialog" button-text="Quick Create">
        <form-single name="QuickForm" extends="BaseEngagementForm">
            <!-- Override fields for quick entry -->
            <field name="description">
                <default-field title="Description">
                    <text-area rows="2" cols="40"/>  <!-- Smaller than base -->
                </default-field>
            </field>
            <!-- Hide optional fields -->
            <field name="estimatedStartDate"><default-field><ignored/></default-field></field>
        </form-single>
    </container-dialog>

    <!-- Full form - extends base form -->
    <container-box>
        <box-header title="Create Engagement"/>
        <box-body>
            <form-single name="FullForm" extends="BaseEngagementForm">
                <!-- Use all fields as-is, or override as needed -->
            </form-single>
        </box-body>
    </container-box>
</widgets>
```

**Key Points:**
- Base form and extending forms are in the **same screen XML file**
- Use `extends="BaseFormName"` (just the name, no path)
- Can override fields, hide fields with `<ignored/>`, or add new fields
- Cannot remove fields (only hide them)

#### Option 2: Subscreens with Parameters (Alternative Pattern)

For forms used across multiple screens, use subscreens with parameters:

```xml
<!-- In FormLibrary.xml screen -->
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-include="false" standalone="true">

    <parameter name="formMode" default-value="full"/>  <!-- Accept parameter -->

    <widgets>
        <section name="QuickFormSection">
            <condition><expression>formMode == 'quick'</expression></condition>
            <widgets>
                <form-single name="EngagementForm" transition="createEngagement">
                    <field name="workEffortName">
                        <default-field title="Title">
                            <text-line size="40"/>  <!-- Quick mode: smaller -->
                        </default-field>
                    </field>
                    <!-- Fewer fields for quick mode -->
                </form-single>
            </widgets>
        </section>

        <section name="FullFormSection">
            <condition><expression>formMode == 'full'</expression></condition>
            <widgets>
                <form-single name="EngagementForm" transition="createEngagement">
                    <field name="workEffortName">
                        <default-field title="Title">
                            <text-line size="60"/>  <!-- Full mode: larger -->
                        </default-field>
                    </field>
                    <!-- All fields for full mode -->
                </form-single>
            </widgets>
        </section>
    </widgets>
</screen>

<!-- Usage in other screens -->
<subscreens-panel id="QuickFormPanel" type="popup">
    <subscreens-item name="QuickCreate"
                     location="component://{component}/screen/FormLibrary.xml"
                     parameter-map="[formMode:'quick']"/>
</subscreens-panel>
```

**Key Points:**
- Forms are defined within reusable screen files
- Use parameters to control form behavior/appearance
- Include using `subscreens-item` or `subscreens-panel`
- More flexible than form extension but requires screen wrapper

#### Option 3: Duplicate Form Definitions (When Appropriate)

For simple forms or when customization is significant, duplicating the form definition may be cleaner:

```xml
<!-- In CreateEngagement.xml -->
<form-single name="CreateForm" transition="createEngagement">
    <!-- Full form definition -->
</form-single>

<!-- In Dashboard.xml -->
<form-single name="QuickCreateForm" transition="createEngagement">
    <!-- Similar but customized form definition -->
</form-single>
```

**When to use:**
- Forms are simple and short
- Each form needs significant customization
- Maintenance overhead is low

**✅ DO write clean, minimal XML:**

```xml
<!-- CORRECT: Use defaults implicitly -->
<log message="Processing started"/>
<parameter name="partyId"/>
<parameter name="optionalParam"/>
<econdition field-name="partyId" from="partyId"/>
<entity-find entity-name="Party"/>
<service-call name="service.name"/>
<service verb="get" noun="Data"/>
<service verb="update" noun="Data"/>
```

### When to Keep Default Values

**Keep explicit defaults only when:**

1. **Documentation purpose**: Making the default explicit for clarity in complex cases
2. **Template/example code**: When showing all available options
3. **Conditional defaults**: When the default might change based on context

```xml
<!-- OK: Explicit for documentation in template -->
<!-- This service demonstrates all available transaction options -->
<service verb="template" noun="Example" transaction="use-or-begin">
    <description>Template showing transaction options: use-or-begin, require-new, ignore</description>
</service>

<!-- OK: Non-default value -->
<log level="warn" message="Potential issue detected"/>
<parameter name="partyId" required="true"/>
<econdition field-name="amount" operator="greater-than" from="threshold"/>
```

### Schema References

Moqui XML schema files define these defaults:

- **xml-actions-3.xsd**: `<log level="info">`, `<econdition operator="equals">`, etc.
- **service-definition-3.xsd**: `<parameter type="String" required="false">`, `<service authenticate="true" validate="true">`, etc.
- **entity-definition-3.xsd**: Entity and view defaults

### Benefits of Avoiding Defaults

- **Cleaner code**: Less visual noise, easier to read
- **Focus on intent**: Non-default values stand out clearly  
- **Maintenance**: Schema changes to defaults don't break code
- **Consistency**: Uniform approach across the codebase
- **Performance**: Slightly faster XML parsing (minimal impact)

## XML Validation and Common Error Corrections

### Critical XML Structure Rules

When working with Moqui XML files (screens, services, entities, etc.), follow these validation rules to prevent parsing errors and ensure proper functionality:

#### 1. Entity Query Structure - No `<subselect>` Tag
**❌ INCORRECT**: Using `<subselect>` tag
```xml
<econdition field-name="ownerPartyId" operator="in" ignore-if-empty="true">
    <subselect entity-name="mantle.party.PartyRelationship">
        <econdition field-name="toPartyId" from="ec.user.context.activeOrgId"/>
        <econdition field-name="relationshipTypeEnumId" value="PrtParent"/>
        <date-filter/>
        <select-field field-name="fromPartyId"/>
    </subselect>
</econdition>
```

**✅ CORRECT**: Use separate queries with list access syntax
```xml
<!-- First: Get the related party IDs in actions section -->
<entity-find entity-name="mantle.party.PartyRelationship" list="parentRelationshipList">
    <econdition field-name="toPartyId" from="ec.user.context.activeOrgId"/>
    <econdition field-name="relationshipTypeEnumId" value="PrtParent"/>
    <date-filter/>
    <select-field field-name="fromPartyId"/>
</entity-find>

<!-- Then: Use the results in the main query -->
<entity-find entity-name="YourMainEntity" list="resultList">
    <econdition field-name="ownerPartyId" operator="in" from="parentRelationshipList*.fromPartyId" ignore-if-empty="true"/>
    <!-- other conditions -->
```

*Explanation*: `<subselect>` tags are not valid in Moqui Screen DSL. Instead, perform separate `entity-find` queries in sequence and use the list access syntax `listName*.fieldName` to access results. This approach is more readable, maintainable, and follows Moqui's design patterns.

#### 2. XML Character Escaping
**❌ INCORRECT**: Unescaped ampersands in XML content
```xml
<list-options list="facilityList" key="${facilityId}" text="${facilityName} (${description ?: 'No description'})"/>
```

**✅ CORRECT**: Escape ampersands as `&amp;`
```xml
<list-options list="facilityList" key="${facilityId}" text="${facilityName} (${description ?: 'No description'})"/>
```
*Note: In most template contexts, this is handled automatically, but in pure XML contexts, manual escaping is required.*

#### 3. Form Field Attribute Placement
**❌ INCORRECT**: `from` attribute on `<default-field>`
```xml
<field name="estimatedStartDate">
    <default-field title="Start Date &amp; Time" tooltip="When will this engagement begin?" from="defaultStartDate">
        <date-time format="dd/MM/yyyy HH:mm"/>
    </default-field>
</field>
```

**✅ CORRECT**: `from` attribute on `<field>` tag
```xml
<field name="estimatedStartDate" from="defaultStartDate">
    <default-field title="Start Date &amp; Time" tooltip="When will this engagement begin?">
        <date-time format="dd/MM/yyyy HH:mm"/>
    </default-field>
</field>
```

#### 4. Link Button Styling
**❌ INCORRECT**: Using `link-type` for styling colors
```xml
<link url="findEngagement" text="Cancel" link-type="secondary"/>
<link url="createNew" text="Create" link-type="success"/>
<link url="view" text="View" link-type="info" btn-type="default"/>
```

**✅ CORRECT**: Use `btn-type` for styling, `link-type` for HTML element type
```xml
<link url="findEngagement" text="Cancel" btn-type="default"/>
<link url="createNew" text="Create" btn-type="success"/>
<link url="view" text="View" link-type="anchor" btn-type="info"/>
```

*Explanation*: These are two separate attributes with different purposes:
- `link-type`: Controls the HTML element type generated (`"anchor"` for `<a>` tags, `"button"` for `<button>` tags, etc.)
- `btn-type`: Controls the visual styling/color scheme when the link appears as a button

**Valid `link-type` values:**
- `"anchor"` - Generates an `<a>` tag (default)
- `"button"` - Generates a `<button>` tag

**Valid `btn-type` values** (from `color-context` XSD type):
- `default` - Default/neutral styling
- `primary` - Primary action button (default)
- `success` - Success/positive action
- `info` - Informational button
- `warning` - Warning/caution button
- `danger` - Destructive/dangerous action

#### 5. Form Parameter Passing
**❌ INCORRECT**: Variable name in `pass-through-parameters`
```xml
<form-single name="InitialInvitationForm" transition="inviteInitialParticipants"
           pass-through-parameters="workEffortId">
```

**✅ CORRECT**: Boolean expression only
```xml
<form-single name="InitialInvitationForm" transition="inviteInitialParticipants"
           pass-through-parameters="true">
```

*Explanation*: `pass-through-parameters` can only contain `"true"` or `"false"`, not variable names.

#### 6. Conditional Field Structure
**❌ INCORRECT**: `<default-field>` inside `<conditional-field>`
```xml
<field name="durationMinutes">
    <conditional-field condition="false">
        <default-field>
            <hidden/>
        </default-field>
    </conditional-field>
</field>
```

**✅ CORRECT**: `<default-field>` after `<conditional-field>`
```xml
<field name="durationMinutes">
    <conditional-field condition="false">
        <hidden/>
    </conditional-field>
    <default-field>
        <hidden/>
    </default-field>
</field>
```

*Explanation*: A `<conditional-field>` cannot contain a `<default-field>` tag. The `<default-field>` must come after all conditional fields and is mandatory.

#### 7. Entity-Find Nesting Restrictions
**❌ INCORRECT**: Nested `<entity-find>` inside conditions
```xml
<entity-find entity-name="mantle.facility.Facility" list="facilityList">
    <econditions combine="or">
        <econdition field-name="ownerPartyId" from="ec.user.context.activeOrgId"/>
        <econditions combine="and">
            <entity-find entity-name="mantle.party.PartyRelationship" list="parentPartyList">
                <econdition field-name="toPartyId" from="ec.user.context.activeOrgId"/>
                <econdition field-name="relationshipTypeEnumId" value="PrtParent"/>
                <date-filter/>
                <select-field field-name="fromPartyId"/>
            </entity-find>
            <econdition field-name="ownerPartyId" operator="in" from="parentPartyList*.fromPartyId"/>
        </econditions>
    </econditions>
</entity-find>
```

**✅ CORRECT**: Separate entity-find queries, let framework handle multi-tenancy
```xml
<!-- CORRECT: Simple entity queries without manual multi-tenant filtering -->
<entity-find entity-name="mantle.facility.Facility" list="facilityList">
    <econdition field-name="disabled" operator="not-equals" value="Y"/>
    <order-by field-name="facilityName"/>
</entity-find>

<!-- Framework automatically applies entity-filter configuration for multi-tenancy -->
```

*Explanation*: `<entity-find>` elements cannot be nested inside `<econditions>` or other entity-find elements. Perform separate queries in sequence and use the results with the list access syntax `listName*.fieldName`.

#### 8. Container-Dialog Button Styling
**❌ INCORRECT**: Invalid `button-type` attribute
```xml
<container-dialog id="MyDialog" button-text="Open Dialog" button-type="warning">
```

**✅ CORRECT**: Use `type` attribute with color-context values
```xml
<container-dialog id="MyDialog" button-text="Open Dialog" type="warning">
```

*Explanation*: `container-dialog` uses `type` attribute, not `button-type`. Valid values are the same as `color-context`: `default`, `primary`, `success`, `info`, `warning`, `danger`.

#### 9. Drop-Down Options Structure
**❌ INCORRECT**: Nested `entity-options` inside `list-options`
```xml
<drop-down>
    <list-options list="statusList" key="${statusId}" text="${description}">
        <entity-options entity-name="moqui.basic.StatusItem">
            <entity-condition>
                <econdition field-name="statusId" operator="in" from="allowedStatuses"/>
            </entity-condition>
        </entity-options>
    </list-options>
</drop-down>
```

**✅ CORRECT**: Use `entity-options` directly with `entity-find`
```xml
<drop-down>
    <entity-options key="${statusId}" text="${description}">
        <entity-find entity-name="moqui.basic.StatusItem">
            <econdition field-name="statusId" operator="in" from="allowedStatuses"/>
        </entity-find>
    </entity-options>
</drop-down>
```

*Explanation*: `list-options`, `entity-options`, and `option` are alternative elements in `drop-down`, not nested structures. `entity-options` must contain an `entity-find` element, not reference `entity-name` directly.

#### 10. Entity-Options Field Attributes

**❌ INCORRECT**: Using `key-field-name` and `text-field-name` attributes
```xml
<entity-options key-field-name="enumId" text-field-name="description">
    <entity-find entity-name="moqui.basic.Enumeration">
        <econdition field-name="enumTypeId" value="WorkEffortType"/>
    </entity-find>
</entity-options>
```

**✅ CORRECT**: Use template expressions with `key` and `text` attributes
```xml
<entity-options key="${enumId}" text="${description}">
    <entity-find entity-name="moqui.basic.Enumeration">
        <econdition field-name="enumTypeId" value="WorkEffortType"/>
    </entity-find>
</entity-options>
```

*Explanation*: The `key-field-name` and `text-field-name` attributes are not valid in Moqui Screen DSL. Use template expressions `${fieldName}` with the `key` and `text` attributes instead.

#### 10b. Entity-Options with Nested Option Tag
**❌ INCORRECT**: Using nested `<option>` tag inside `<entity-options>`
```xml
<entity-options>
    <entity-find entity-name="moqui.basic.Enumeration">
        <econdition field-name="enumTypeId" value="DocumentType"/>
        <order-by field-name="description"/>
    </entity-find>
    <option key="${enumId}" text="${description}"/>
</entity-options>
```

**✅ CORRECT**: Put `key` and `text` attributes directly on `<entity-options>` tag
```xml
<entity-options key="${enumId}" text="${description}">
    <entity-find entity-name="moqui.basic.Enumeration">
        <econdition field-name="enumTypeId" value="DocumentType"/>
        <order-by field-name="description"/>
    </entity-find>
</entity-options>
```

*Explanation*: The `<option>` tag cannot be nested inside `<entity-options>`. The `key` and `text` attributes must be specified directly on the `<entity-options>` tag using template expressions. The `<option>` tag is only valid as a direct child of `<drop-down>` for static options, not inside `<entity-options>`.

#### 10c. Accessing Current User's PartyId
**❌ INCORRECT**: Using `ec.user.partyId`
```xml
<econdition field-name="partyId" from="ec.user.partyId"/>
```
```groovy
def userPartyId = ec.user.partyId
```

**✅ CORRECT**: Use `ec.user.userAccount?.partyId`
```xml
<econdition field-name="partyId" from="ec.user.userAccount?.partyId"/>
```
```groovy
def userPartyId = ec.user.userAccount?.partyId
```

*Explanation*: The `ec.user` object does not have a direct `partyId` property. The partyId is accessed through the `userAccount` object. Always use `ec.user.userAccount?.partyId` with safe navigation (`?.`) to handle cases where userAccount might be null (e.g., anonymous users).

**Common user context properties:**
- `ec.user.userAccount?.partyId` - Current user's party ID
- `ec.user.userAccount?.userId` - Current user's login ID
- `ec.user.userId` - Shorthand for user login ID (this one works directly)
- `ec.user.username` - Current user's username

#### 10d. View Entity Member Restrictions
**❌ INCORRECT**: Using a view entity as member-entity in another view entity
```xml
<view-entity entity-name="MyViewEntity" package="mypackage">
    <member-entity entity-alias="MAIN" entity-name="mypackage.MainEntity"/>
    <member-entity entity-alias="PARTY" entity-name="mantle.party.PartyDetail"
                   join-from-alias="MAIN" join-optional="true">
        <key-map field-name="partyId"/>
    </member-entity>
    <alias-all entity-alias="MAIN"/>
    <alias name="firstName" entity-alias="PARTY"/>
    <alias name="lastName" entity-alias="PARTY"/>
    <alias name="organizationName" entity-alias="PARTY"/>
</view-entity>
```

**✅ CORRECT**: Use base entities only, resolve display names in screens
```xml
<!-- In entity definition: use base entity only -->
<view-entity entity-name="MyViewEntity" package="mypackage">
    <member-entity entity-alias="MAIN" entity-name="mypackage.MainEntity"/>
    <!-- Only include partyId, not PartyDetail view -->
    <alias-all entity-alias="MAIN"/>
    <alias name="partyId" entity-alias="MAIN"/>
</view-entity>

<!-- In screen: use display-entity for party names -->
<field name="partyId">
    <default-field title="Party">
        <display-entity text="PartyNameOnlyTemplate" entity-name="mantle.party.PartyDetail"/>
    </default-field>
</field>
```

*Explanation*: View entities cannot use other view entities as member-entities. Only base entities (regular `<entity>` definitions) can be used as members. When you need to display derived fields like party names (firstName, lastName, organizationName from PartyDetail), include only the foreign key (partyId) in the view entity and use `<display-entity>` in screens to resolve the display value at render time.

**Common patterns for party name display:**
- `<display-entity text="PartyNameOnlyTemplate" entity-name="mantle.party.PartyDetail"/>` - Shows formatted party name
- `<display-entity text="${organizationName?:''} ${firstName?:''} ${lastName?:''}" entity-name="mantle.party.PartyDetail"/>` - Custom format

#### 11. Field Condition Attributes
**❌ INCORRECT**: `condition` attribute on `header-field` or `default-field`
```xml
<field name="actions">
    <header-field title="Actions" condition="canEdit"/>
    <default-field condition="canEdit">
        <link url="editRecord" text="Edit"/>
    </default-field>
</field>
```

**✅ CORRECT**: Use `conditional-field` with mandatory `default-field`
```xml
<field name="actions">
    <conditional-field condition="canEdit">
        <link url="editRecord" text="Edit"/>
    </conditional-field>
    <default-field title="Actions">
        <display text=""/>
    </default-field>
</field>
```

*Explanation*: Neither `header-field` nor `default-field` have a `condition` attribute. To conditionally display field content, use `conditional-field` with the condition, followed by a mandatory `default-field` for the alternative display.

#### 11. Check Element Default Values
**❌ INCORRECT**: `default-value` attribute on `check` element
```xml
<field name="sendNotification">
    <default-field title="Send Notifications">
        <check default-value="Y">
            <option key="Y" text="Send email notifications"/>
        </check>
    </default-field>
</field>
```

**✅ CORRECT**: Use `no-current-selected-key` for unchecked state
```xml
<field name="sendNotification">
    <default-field title="Send Notifications">
        <check no-current-selected-key="N">
            <option key="Y" text="Send email notifications"/>
        </check>
    </default-field>
</field>
```

*Explanation*: The `check` element does not have a `default-value` attribute. Use `no-current-selected-key` to specify the value when the checkbox is unchecked, or `all-checked` to specify an expression that determines if all options should be checked by default.

#### 12. Entity-Find-One vs Entity-Find Usage
**❌ INCORRECT**: `entity-find-one` with `date-filter` or non-primary key lookups
```xml
<entity-find-one entity-name="mantle.work.effort.WorkEffortParty" value-field="organizerCheck">
    <field-map field-name="workEffortId" from="workEffortId"/>
    <field-map field-name="partyId" from="ec.user.userAccount.partyId"/>
    <field-map field-name="roleTypeId" value="Organizer"/>
    <date-filter/>
</entity-find-one>
```

**✅ CORRECT**: Use `entity-find` when date-filter is needed or when not using primary key
```xml
<entity-find entity-name="mantle.work.effort.WorkEffortParty" list="organizerCheckList">
    <econdition field-name="workEffortId" from="workEffortId"/>
    <econdition field-name="partyId" from="ec.user.userAccount.partyId"/>
    <econdition field-name="roleTypeId" value="Organizer"/>
    <date-filter/>
</entity-find>
<set field="organizerCheck" from="organizerCheckList?.first"/>
```

*Explanation*: The `entity-find-one` element does not allow `date-filter` or other query constraints beyond simple field matching. Use `entity-find-one` only for exact primary key lookups. When date filtering, complex conditions, or non-primary key searches are needed, always use `entity-find` and extract the first result if needed.

#### 13. If-Then-Else XML Structure
**❌ INCORRECT**: Missing `<then>` tags with `<else>`
```xml
<if condition="reportFormat == 'HTML'">
    <set field="content" from="htmlContent"/>
<else>
    <set field="content" from="textContent"/>
</if>
```

**✅ CORRECT**: Mandatory `<then>` tags with proper closing
```xml
<if condition="reportFormat == 'HTML'"><then>
    <set field="content" from="htmlContent"/>
</then><else>
    <set field="content" from="textContent"/>
</else></if>
```

**❌ INCORRECT**: `<else-if>` without `<then>`
```xml
<if condition="status == 'active'">
    <set field="message" value="Active"/>
<else-if condition="status == 'inactive'">
    <set field="message" value="Inactive"/>
<else>
    <set field="message" value="Unknown"/>
</if>
```

**✅ CORRECT**: Proper `<then>` and `<else-if>` structure
```xml
<if condition="status == 'active'"><then>
    <set field="message" value="Active"/>
</then><else-if condition="status == 'inactive'"><then>
    <set field="message" value="Inactive"/>
</then><else>
    <set field="message" value="Unknown"/>
</else></if>
```

*Explanation*: In Moqui XML, when using `<else>` or `<else-if>` elements, the opening `<if>` condition MUST be followed by a `<then>` element. Every `<else-if>` must also have its own `<then>` element. This is valid XML structure and prevents parsing errors. Simple `<if>` without `<else>` can omit `<then>` tags.

#### 14. Conditional Econdition Application with `ignore` Attribute
**❌ INCORRECT**: Using non-existent `<econdition-apply>` tag
```xml
<entity-find entity-name="MyEntity" list="myList">
    <econdition field-name="statusId" operator="in" from="['StatusPending', 'StatusNew']">
        <econdition-apply>tabFilter == 'pending' || !tabFilter</econdition-apply>
    </econdition>
    <econdition field-name="statusId" value="StatusComplete">
        <econdition-apply>tabFilter == 'complete'</econdition-apply>
    </econdition>
</entity-find>
```

**✅ CORRECT**: Use `ignore` attribute with Groovy expression
```xml
<entity-find entity-name="MyEntity" list="myList">
    <econdition field-name="statusId" operator="in" from="['StatusPending', 'StatusNew']"
                ignore="tabFilter != 'pending' &amp;&amp; tabFilter"/>
    <econdition field-name="statusId" value="StatusComplete"
                ignore="tabFilter != 'complete'"/>
</entity-find>
```

*Explanation*: The `<econdition-apply>` tag does not exist in Moqui. To conditionally apply an econdition based on runtime state, use the `ignore` attribute with a Groovy expression. The condition is **ignored** (skipped) when the expression evaluates to `true`. Note that the logic is inverted: specify when to IGNORE the condition, not when to apply it.

**Key differences:**
- `ignore-if-empty="true"` - Ignores the condition when the `from` value is null or empty string
- `ignore="groovyExpression"` - Ignores the condition when the expression evaluates to `true`

**Common patterns:**
```xml
<!-- Tab-based filtering: apply condition only for specific tab -->
<econdition field-name="statusId" value="Active" ignore="tabFilter != 'active'"/>

<!-- Apply condition only when a parameter is present -->
<econdition field-name="categoryId" from="categoryId" ignore="!categoryId"/>

<!-- Multiple conditions for logic (remember: && must be escaped as &amp;&amp;) -->
<econdition field-name="dateField" operator="greater-equals" from="fromDate"
            ignore="!fromDate &amp;&amp; !filterByDate"/>
```

### Validation Checklist

Before committing XML changes, verify:

- [ ] No `<subselect>` tags - use proper `<econditions>` structure
- [ ] All `&` characters properly escaped as `&amp;` in XML content (especially `&&` → `&amp;&amp;` in conditions)
- [ ] Field attributes (`from`, `value`, etc.) on `<field>` tag, not `<default-field>`
- [ ] Link styling: Use `btn-type` for colors (`default`, `primary`, `success`, `info`, `warning`, `danger`), `link-type` for HTML type (`anchor`, `button`)
- [ ] No color values in `link-type` attribute (use `btn-type` instead)
- [ ] Form parameters use boolean values: `pass-through-parameters="true|false"`
- [ ] Conditional fields have `<default-field>` after, not inside, conditional conditions
- [ ] No nested `<entity-find>` elements - perform separate queries and use results
- [ ] No manual multi-tenant filtering with `ec.user.context.activeOrgId` - use entity-filter configuration
- [ ] Container-dialog uses `type` attribute, not `button-type`
- [ ] Drop-down options structure: `list-options`, `entity-options`, `option` are alternatives, not nested
- [ ] Entity-options contains `<entity-find>` element, not `entity-name` attribute
- [ ] Entity-options uses `key="${fieldName}"` and `text="${fieldName}"`, not `key-field-name` and `text-field-name` attributes
- [ ] No `<option>` tags nested inside `<entity-options>` - put `key` and `text` attributes on `<entity-options>` tag
- [ ] Use `ec.user.userAccount?.partyId` not `ec.user.partyId` for current user's party ID
- [ ] View entities can only use base entities as member-entities, not other view entities (use `display-entity` in screens for derived fields)
- [ ] No `condition` attribute on `header-field` or `default-field` - use `conditional-field`
- [ ] Check elements use `no-current-selected-key`, not `default-value`
- [ ] No `date-filter` on `entity-find-one` - use `entity-find` for date filtering or non-primary key queries
- [ ] If-then-else structure: `<if>` with `<else>` must use `<then>` tags and proper closing
- [ ] No `<econdition-apply>` tags - use `ignore` attribute on econdition for conditional application
- [ ] XML validates against Moqui XSD schemas

### XML Schema Validation

**CRITICAL: Always use the latest XSD schema files from the framework/xsd directory when validating Moqui XML.**

#### Schema File Locations and Usage

Moqui XML files should validate against their respective XSD schemas located in `framework/xsd/`:
- `xml-screen-3.xsd` for screen definitions
- `service-definition-3.xsd` for service definitions
- `entity-definition-3.xsd` for entity definitions
- `xml-actions-3.xsd` for action sequences
- `xml-form-3.xsd` for form element definitions (imported by screen schema)

#### Schema Validation Best Practices

**✅ MANDATORY: Use framework XSD files for accurate validation**
```bash
# Always reference the framework XSD directory for validation
xmllint --noout --schema framework/xsd/xml-screen-3.xsd path/to/screen.xml
```

**❌ AVOID: Using outdated or external XSD files**
- Never rely on cached or downloaded XSD files from other sources
- Always check the framework/xsd directory for the most current schema definitions
- Schema files are updated with framework releases and contain the authoritative element/attribute definitions

#### Link Element Attributes Reference

Based on the latest `xml-form-3.xsd` schema, the `link` element supports these key attributes:

**Required Attributes:**
- `url` (required) - The target URL or transition name

**Styling Attributes:**
- `btn-type` (default: "primary") - Visual styling using color-context values: `default`, `primary`, `success`, `info`, `warning`, `danger`
- `link-type` (default: "auto") - HTML element type: `auto`, `anchor`, `anchor-button`, `hidden-form`, `hidden-form-link`

**Common Attributes:**
- `text` - Display text for the link
- `confirmation` - Confirmation dialog message
- `parameter-map` - Parameters to pass with the link
- `condition` - Conditional rendering expression
- `icon` - Icon name/class for display

**✅ CORRECT: Using btn-type for link styling**
```xml
<link url="deleteRecord" text="Delete" btn-type="danger"
      confirmation="Are you sure you want to delete this record?"/>
<link url="../FindEngagement" text="Back" btn-type="default"/>
```

Use IDE validation or manual tools to check schema compliance before deployment.

#### Attribute Value Validation Requirements

**CRITICAL: Attribute values must match their XSD schema definitions exactly.**

Beyond just checking attribute names, you must validate that attribute **values** are within the allowed enumeration or format specified in the schema.

**✅ CORRECT: Valid attribute values per schema**
```xml
<!-- Container type must be HTML element names -->
<container style="row">                 <!-- CSS class goes in style -->
<container style="col-lg-12">          <!-- CSS class goes in style -->
<container type="div">                  <!-- Valid HTML element type -->

<!-- Color-context values: default|primary|success|info|warning|danger -->
<container-dialog type="success" button-text="Add"/>
<link btn-type="danger" text="Delete"/>

<!-- Date-time type values: timestamp|date-time|date|time -->
<date-time type="date-time" format="dd/MM/yyyy HH:mm"/>
```

**❌ INCORRECT: Invalid attribute values cause schema validation failures**
```xml
<!-- Invalid: Bootstrap classes in type attribute -->
<container type="row">                  <!-- "row" not in container type enum -->
<container type="col-lg-12">           <!-- "col-lg-12" not in container type enum -->
<container type="page-header">         <!-- "page-header" not in container type enum -->

<!-- Invalid: Wrong attribute name for container-dialog -->
<container-dialog button-type="primary"/>  <!-- Should be type="primary" -->

<!-- Invalid: Non-existent color-context values -->
<link btn-type="red" text="Delete"/>      <!-- "red" not in color-context enum -->
```

**Dynamic Attribute Value Exceptions:**

These attribute values are validated at runtime and are acceptable even if not in schema enums:

**✅ ALLOWED: Dynamic attribute values**
```xml
<!-- Variable substitution - resolved at runtime -->
<container-dialog id="EditDialog_${recordId}" type="primary"/>
<link url="detail" parameter-map="[id:${itemId}]" text="View"/>

<!-- Conditional expressions - evaluated at runtime -->
<field name="status" condition="${hasPermission}">
<display text="${value ?: 'Not set'}"/>

<!-- Entity/service names - validated at runtime -->
<entity-find entity-name="custom.MyEntity"/>
<service-call name="myservice.MyService"/>

<!-- Transition names - validated at runtime -->
<link url="myTransition" text="Go"/>

<!-- File paths and URLs - validated at runtime -->
<image url="images/${iconName}.png"/>
```

**Form-List Actions Placement:**

**CRITICAL: The `<actions>` element cannot be placed inside `<form-list>` elements.**

The `<actions>` element must be placed in the containing section or screen level, not inside form-list definitions.

**✅ CORRECT: Actions in containing section**
```xml
<section name="MySection">
    <actions>
        <!-- Data preparation actions -->
        <entity-find entity-name="MyEntity" list="myList"/>
        <set field="calculatedValue" from="someCalculation"/>
    </actions>
    <widgets>
        <form-list name="MyList" list="myList">
            <!-- Form fields only, no actions -->
            <field name="name"><default-field><display/></default-field></field>
        </form-list>
    </widgets>
</section>
```

**❌ INCORRECT: Actions inside form-list causes schema validation error**
```xml
<form-list name="MyList" list="myList">
    <actions>  <!-- INVALID: actions cannot be inside form-list -->
        <entity-find entity-name="MyEntity" list="myList"/>
    </actions>
    <field name="name"><default-field><display/></default-field></field>
</form-list>
```

**Common Validation Errors and Fixes:**

| Error | Fix |
|-------|-----|
| `<container type="row">` | Use `<container style="row">` |
| `<container-dialog button-type="primary">` | Use `<container-dialog type="primary">` |
| `<form-list><actions>` | Move `<actions>` to parent `<section>` |
| `<link btn-type="red">` | Use valid color: `btn-type="danger"` |

#### XML Entity Escaping in Attributes
**CRITICAL: When using logical operators in XML attribute values, proper XML entity escaping is required:**

- **Use `&amp;&amp;` for AND operations**: `condition="value1 &amp;&amp; value2"`
- **Use `&amp;` for single ampersand**: `condition="param &amp; mask"`  
- **Use `&lt;` for less than**: `condition="value &lt; 10"`
- **Use `&gt;` for greater than**: `condition="value &gt; 0"`

**✅ CORRECT: Proper XML entity escaping in conditions**
```xml
<if condition="quorumMet &amp;&amp; !['Y', 'N'].contains(quorumMet)">
    <return error="true" message="Invalid quorum status"/>
</if>

<if condition="arrivalTime &amp;&amp; departureTime &amp;&amp; arrivalTime &gt; departureTime">
    <return error="true" message="Invalid time range"/>
</if>
```

**❌ INCORRECT: Raw operators in XML attributes will cause schema validation errors**
```xml
<if condition="quorumMet && !['Y', 'N'].contains(quorumMet)">  <!-- Invalid XML -->
<if condition="value > 10">                                    <!-- Invalid XML -->
```

This applies to all XML attribute values in Moqui service definitions, screen definitions, and entity conditions.

## ServiceJob Authentication Requirements

### Services Called by ServiceJob

When creating services that will be executed by Moqui's ServiceJob (scheduled jobs), special authentication considerations apply since jobs run without a logged-in user context.

### Required Authentication Setting

**✅ REQUIRED: Use `authenticate="anonymous-all"` for ServiceJob services**

```xml
<!-- CORRECT: Service designed to run via ServiceJob -->
<service verb="run" noun="CafAutoRequest" authenticate="anonymous-all">
    <description>
        Execute CAF auto-request monitoring and fetching process.
        This service is called by ServiceJob and runs without user login.
    </description>
    <in-parameters>
        <parameter name="specificPartyId"/>
        <parameter name="dryRun" type="Boolean" default="false"/>
    </in-parameters>
    <actions>
        <!-- Service implementation -->
    </actions>
</service>
```

### Why `authenticate="anonymous-all"` is Required

1. **No User Context**: ServiceJob executes without a logged-in user
2. **System Operations**: Scheduled tasks need to perform system-level operations
3. **Default Behavior**: Services default to `authenticate="true"` which requires login
4. **Access Control**: `anonymous-all` allows execution without authentication but still respects entity permissions

### Authentication Options for ServiceJob

| Setting | Use Case | Description |
|---------|----------|-------------|
| `authenticate="anonymous-all"` | **Recommended** | Service can run without user, full entity access |
| `authenticate="anonymous-view"` | Read-only jobs | Service can run without user, read-only entity access |
| `authenticate="false"` | **Avoid** | No authentication or authorization checks (security risk) |

### ServiceJob Configuration Pattern

**Complete pattern for scheduled services:**

```xml
<!-- 1. Define the service with proper authentication -->
<service verb="run" noun="AutoProcessingJob" authenticate="anonymous-all">
    <description>Automated processing service called by ServiceJob</description>
    <in-parameters>
        <parameter name="dryRun" type="Boolean" default="false"/>
    </in-parameters>
    <actions>
        <!-- Implementation -->
    </actions>
</service>
```

```xml
<!-- 2. Configure the ServiceJob in data files -->
<moqui.service.job.ServiceJob 
    jobName="run_AutoProcessing_daily" 
    serviceName="namespace.run#AutoProcessingJob" 
    cronExpression="0 0 2 * * ?" 
    paused="N"
    description="Daily automated processing at 2 AM"/>

<!-- 3. Add job parameters if needed -->
<moqui.service.job.ServiceJobParameter 
    jobName="run_AutoProcessing_daily" 
    parameterName="dryRun" 
    parameterValue="false"/>
```

### Real Example: CAF Auto Request

The CAF auto-request service demonstrates this pattern:

```xml
<service verb="run" noun="CafAutoRequest" authenticate="anonymous-all">
    <description>
        Execute CAF auto-request monitoring and fetching process.
        This service checks all active CafAutoRequest configurations
        and triggers CAF fetching when thresholds are breached.
    </description>
    <!-- ... parameters and implementation ... -->
</service>
```

With corresponding ServiceJob configuration:

```xml
<moqui.service.job.ServiceJob 
    jobName="sii_dte_CafAutoRequest" 
    serviceName="mycompany.dte.DteFolioServices.run#CafAutoRequest" 
    cronExpression="0 0 6,12,18 * * ?" 
    paused="N"
    description="Monitor CAF folio availability and automatically request new CAFs"/>
```

### Security Considerations

- **Minimal Permissions**: Use `anonymous-view` when possible for read-only operations
- **Entity Security**: ServiceJob still respects entity-level security rules
- **Audit Logging**: Consider adding explicit audit logging for ServiceJob operations
- **Error Handling**: Implement robust error handling since no user is present to handle failures

### Testing ServiceJob Services

**❌ INCORRECT: `disableAuthz()` does not simulate no user context**

```groovy
// WRONG: This doesn't test ServiceJob conditions
def result = ec.service.sync()
    .name("namespace.run#ServiceJobService")
    .disableAuthz() // Only disables permissions, user still required
    .call()
```

**✅ CORRECT: Test by calling the service directly without user login**

```groovy
// Test ServiceJob service by ensuring it works without user context
// The service must have authenticate="anonymous-all" to work
def result = ec.service.sync()
    .name("namespace.run#ServiceJobService")
    .call()

assert result.success != false

// Or test by temporarily logging out (if applicable in test context)
ec.user.logoutUser()
def result = ec.service.sync()
    .name("namespace.run#ServiceJobService") 
    .call()
assert result.success != false
```

**Key Testing Points:**

1. **Authentication vs Authorization**: 
   - `authenticate="true"` requires user to be logged in (fails if no user)
   - `disableAuthz()` only skips permission checks, user still required
   - `authenticate="anonymous-all"` allows no user + full entity access

2. **ServiceJob Environment**: 
   - No user context available (`ec.user.userId` will be null)
   - Services must be designed to handle null user scenarios
   - Use `authenticate="anonymous-all"` for ServiceJob services

3. **Test Strategy**: Test the actual service configuration, not a simulation

### ServiceJob Runtime Management

**DO NOT create custom parameters** like `maxProcessingTimeMs` or `timeoutMs` for service jobs. Use the built-in ServiceJob entity fields for runtime control:

**✅ CORRECT: Use built-in ServiceJob fields**
```xml
<moqui.service.job.ServiceJob 
    jobName="sii_dte_CafAutoRequest" 
    serviceName="mycompany.dte.DteFolioServices.run#CafAutoRequest" 
    cronExpression="0 0 6,12,18 * * ?" 
    maxRetry="3"
    maxRuntime="1800"
    paused="N"
    description="Monitor CAF folio availability"/>
```

**❌ AVOID: Custom timeout parameters**
```xml
<!-- DON'T: Custom parameters duplicate framework functionality -->
<service verb="run" noun="CafAutoRequest" authenticate="anonymous-all">
    <in-parameters>
        <parameter name="maxProcessingTimeMs" type="Long"/>  <!-- Framework handles this -->
        <parameter name="timeoutMs" type="Long"/>            <!-- Use maxRuntime instead -->
    </in-parameters>
</service>
```

**Built-in ServiceJob Runtime Fields:**
- **`maxRetry`**: Maximum number of retry attempts (Integer)
- **`maxRuntime`**: Maximum runtime in seconds before job is considered failed (Integer) 
- **`cronExpression`**: Schedule for recurring jobs (standard cron format)
- **`paused`**: Enable/disable job execution ("Y" or "N")
- **`minRetryTime`**: Minimum minutes before retrying after error (Integer). When a service returns an error, the framework reschedules after this interval.
- **`expireLockTime`**: Minutes before a `ServiceJobRunLock` expires (Integer). Prevents stale locks from blocking execution across pods.

**Framework Capabilities:**
- The Moqui framework handles job timeout, retry logic, and monitoring automatically
- Custom timeout parameters duplicate framework functionality and should be avoided
- ServiceJob entity provides comprehensive runtime management without additional code

### ServiceJob Self-Management Pattern

Jobs can programmatically pause and unpause themselves by updating the `paused` field on the `ServiceJob` entity. This is useful for jobs that should only run when there is work to do.

**Pattern**: A job pauses itself when its work queue is empty, and external code (e.g., a ToolFactory or admin service) unpauses it when new work arrives.

```groovy
// Inside the job service: pause when done
def job = ec.entity.find("moqui.service.job.ServiceJob")
        .condition("jobName", "MyScheduler").one()
if (job && job.paused != "Y") {
    job.paused = "Y"
    job.update()
}

// External code (e.g., ToolFactory.init or admin service): unpause when work arrives
def job = ec.entity.find("moqui.service.job.ServiceJob")
        .condition("jobName", "MyScheduler").one()
if (job?.paused == "Y") {
    job.paused = "N"
    job.update()
}
```

**Key considerations:**
- Start the job `paused="Y"` in seed data so it doesn't run until work is registered
- Use `minRetryTime` with error returns to keep the job rescheduling while work remains (return an error message to trigger reschedule). See **Incremental Batch Processing Pattern** in `standards/backend/service-jobs.md` for the complete fail-to-retry pattern.
- `ServiceJobRunLock` with `expireLockTime` prevents concurrent execution across pods

## Variable Naming Conventions

### List Variable Naming

When working with List variables in Moqui services, follow these naming conventions for improved code readability and consistency.

### List Variable Names

**✅ REQUIRED: List variables should end with "List"**

```xml
<!-- CORRECT: List variables end with "List" -->
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigList">
    <econdition field-name="isActive" value="Y"/>
</entity-find>

<entity-find entity-name="mycompany.dte.Caf" list="allCafList">
    <econdition field-name="issuerPartyId" from="partyId"/>
</entity-find>

<set field="errorList" from="[]"/>
<set field="resultList" from="[]"/>
```

**❌ AVOID: Generic or unclear list names**

```xml
<!-- DON'T: Unclear that these are lists -->
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigs">
<entity-find entity-name="mycompany.dte.Caf" list="allCafs">
<set field="errors" from="[]"/>
<set field="results" from="[]"/>
```

### Iterator Variable Names

**✅ PREFERRED: Use the list name without "List" suffix for iteration**

```xml
<!-- CORRECT: Iterator name matches list name without "List" -->
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigList">
    <econdition field-name="isActive" value="Y"/>
</entity-find>

<iterate list="activeConfigList" entry="activeConfig">
    <log message="Processing config for party ${activeConfig.partyId}"/>
    <!-- Use activeConfig.* to access properties -->
</iterate>
```

```xml
<!-- CORRECT: Clear relationship between list and iterator -->
<entity-find entity-name="mycompany.dte.Caf" list="allCafList">
    <econdition field-name="issuerPartyId" from="partyId"/>
</entity-find>

<iterate list="allCafList" entry="caf">
    <set field="isActive" from="caf.activo == 'Y'"/>
    <log message="CAF ${caf.cafId} is ${isActive ? 'active' : 'inactive'}"/>
</iterate>
```

### Handling Name Collisions

**When iterator name would conflict with existing variables:**

```xml
<!-- OPTION 1: Use descriptive prefix -->
<iterate list="cafList" entry="currentCaf">
    <set field="cafId" from="currentCaf.cafId"/>
</iterate>

<!-- OPTION 2: Use context-specific name -->
<iterate list="partyList" entry="targetParty">
    <service-call name="process#Party" in-map="[partyId:targetParty.partyId]"/>
</iterate>

<!-- OPTION 3: Keep "List" in iterator name if necessary -->
<iterate list="configList" entry="configItem">
    <log message="Processing ${configItem.partyId}"/>
</iterate>
```

### Real Examples from CAF Auto Request

**Before (unclear naming):**
```xml
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigs">
    <econdition field-name="isActive" value="Y"/>
</entity-find>

<iterate list="activeConfigs" entry="config">
    <set field="partyId" from="config.partyId"/>
</iterate>
```

**After (clear naming):**
```xml
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigList">
    <econdition field-name="isActive" value="Y"/>
</entity-find>

<iterate list="activeConfigList" entry="activeConfig">
    <set field="partyId" from="activeConfig.partyId"/>
</iterate>
```

### Benefits of This Convention

1. **Clear Intent**: Immediately obvious that variable contains a list
2. **Consistent Patterns**: Predictable naming across the codebase
3. **Better IDE Support**: Autocomplete and refactoring tools work better
4. **Reduced Confusion**: No ambiguity about variable types
5. **Maintainability**: Easier to understand and modify code

### Additional List Naming Guidelines

- **Descriptive Names**: Use meaningful names that describe the list contents
- **Avoid Abbreviations**: Prefer `activeConfigList` over `activeCfgList`
- **Context Clarity**: Include context when needed (`customerPartyList`, `validCafList`)
- **Consistency**: Apply the same naming pattern throughout the service

## Automated Coding Practice Enforcement

### Coding Practice Automation Strategy

Given the numerous coding practices established for Moqui Framework development, implementing automated agents to enforce these practices provides significant value for maintaining code quality and consistency.

### Automatable Practices Identified

**1. XML DSL Default Attributes**
- Remove unnecessary `level="info"` from `<log>` elements
- Remove redundant `type="String"` from service parameters
- Remove default `required="false"` from parameters
- Remove default `operator="equals"` from `<econdition>`
- Remove default `for-update="false"`, `distinct="false"`, etc.

**2. Variable Naming Conventions**
- Enforce "List" suffix on list variables (`activeConfigList` vs `activeConfigs`)
- Validate iterator names match list names without "List" suffix
- Suggest improvements for unclear variable names

**3. Service Structure Patterns**
- Ensure ServiceJob services have `authenticate="anonymous-all"`
- Validate service parameter documentation exists
- Check proper use of `in-parameters` vs `out-parameters`

**4. Moqui Framework Best Practices**
- Prefer XML DSL over excessive script blocks
- Validate proper entity relationship usage
- Check transaction handling patterns

### Recommended Implementation: Specialized Agent Tools

**Agent Architecture:**

```javascript
// Claude Code MCP Server Configuration
{
  "mcpServers": {
    "moqui-coding-practices": {
      "command": "node",
      "args": ["runtime/component/{main-component}/tools/moqui-practices-agent.js"],
      "cwd": ".",
      "tools": {
        "moqui_lint_xml": {
          "description": "Fix XML DSL default attributes and naming conventions",
          "parameters": {
            "files": "Array of file paths to analyze and fix",
            "dryRun": "Boolean - preview changes without applying"
          }
        },
        "moqui_review_service": {
          "description": "Review service definitions for best practices",
          "parameters": {
            "servicePath": "Path to service XML file",
            "checkAuthentication": "Boolean - verify ServiceJob authentication"
          }
        },
        "moqui_format_code": {
          "description": "Apply consistent formatting and structure",
          "parameters": {
            "targetPath": "Directory or file path to format",
            "lineLength": "Number - maximum line length (default 180)"
          }
        }
      }
    }
  }
}
```

### Agent Capabilities

**1. `moqui-linter` Agent**
```javascript
// Example usage
await claudeCode.useAgent('moqui-linter', {
  task: 'Fix XML DSL coding practices',
  files: ['runtime/component/{component}/service/**/*.xml'],
  fixes: [
    'remove-default-attributes',
    'fix-list-naming',
    'validate-service-structure'
  ]
});
```

**2. `service-reviewer` Agent**
```javascript
// Example usage  
await claudeCode.useAgent('service-reviewer', {
  task: 'Review service for best practices',
  servicePath: 'runtime/component/{component}/service/mycompany/myapp/ExampleServices.xml',
  checks: [
    'authentication-requirements',
    'parameter-documentation', 
    'transaction-patterns'
  ]
});
```

### Integration Points

**1. Development Workflow**
```bash
# Interactive development assistance
claude-code --agent moqui-linter --watch runtime/component/

# Pre-commit hook integration
claude-code --agent service-reviewer --files $(git diff --cached --name-only "*.xml")
```

**2. CI/CD Pipeline Integration**
```yaml
# GitLab CI example
moqui-code-review:
  stage: quality
  script:
    - claude-code --agent moqui-linter --path runtime/component/ --fix
    - claude-code --agent service-reviewer --path runtime/component/ --report
  artifacts:
    reports:
      - moqui-practices-report.json
```

**3. IDE Integration**
```json
// VSCode tasks.json
{
  "label": "Moqui: Fix Coding Practices",
  "command": "claude-code",
  "args": ["--agent", "moqui-linter", "--file", "${file}"],
  "group": "build"
}
```

### Benefits of Agent Implementation

**1. Consistency Enforcement**
- Automatic application of established coding practices
- Reduced manual code review burden
- Consistent standards across team members

**2. Knowledge Transfer**
- New developers learn practices through automated suggestions
- Practices are enforced consistently without human oversight
- Documentation stays in sync with automated checks

**3. Quality Improvement**
- Early detection of practice violations
- Automatic fixes for common issues
- Reduced technical debt accumulation

**4. Development Efficiency**
- Less time spent on manual formatting
- Focus on business logic rather than style issues
- Faster onboarding for new team members

### Implementation Priority

**Phase 1: Core XML DSL Fixes** (High Impact, Low Complexity)
- Default attribute removal
- Variable naming conventions
- Basic service structure validation

**Phase 2: Advanced Service Review** (Medium Impact, Medium Complexity)
- Authentication pattern validation
- Service design pattern checks
- Entity relationship optimization

**Phase 3: Intelligent Suggestions** (High Impact, High Complexity)
- Context-aware refactoring suggestions
- Performance optimization recommendations
- Architecture pattern guidance

### Agent Development Guidelines

When implementing coding practice agents:

1. **Preserve Functionality**: Never change business logic, only style/structure
2. **Provide Explanations**: Include rationale for each suggested change
3. **Support Dry-Run**: Allow preview of changes before applying
4. **Maintain Context**: Understand the broader codebase context
5. **Handle Edge Cases**: Gracefully handle unusual or legacy code patterns
6. **Performance Aware**: Process large codebases efficiently

This automated approach ensures coding practices are consistently applied while allowing developers to focus on business value rather than manual style enforcement.

## Code Formatting and Style Guidelines

**CRITICAL: ALL specialist agents MUST follow these formatting and style guidelines.**

These guidelines ensure consistent, maintainable code across all Moqui Framework development. They reduce visual noise, improve readability, and make code intent clearer.

### 1. Default Values and Attributes

**CRITICAL: Do not add default values that are already defaults in Moqui Framework**

- DO NOT add `type="String"` for parameters (String is the default)
- DO NOT add `authenticate="true"` for services (true is the default)
- DO NOT add `transaction="use-or-begin"` for services (this is the default)
- DO NOT add `required="false"` for parameters (false is the default)
- DO NOT add `level="info"` for log statements (info is the default)
- DO NOT add `operator="equals"` for econditions (equals is the default)
- DO NOT add `for-update="false"` for entity-find (false is the default)
- Only add attributes when they differ from the framework defaults

**Examples:**
```xml
<!-- ❌ INCORRECT: Adding unnecessary defaults -->
<parameter name="description" type="String" required="false"/>
<service verb="get" noun="Info" authenticate="true" transaction="use-or-begin">
<log level="info" message="Processing started"/>
<econdition field-name="partyId" operator="equals" from="partyId"/>

<!-- ✅ CORRECT: Only non-default values -->
<parameter name="description"/>  <!-- String type is default -->
<parameter name="partyId" required="true"/>  <!-- String is default for service parameters -->
<service verb="get" noun="Info">  <!-- Uses framework defaults -->
<service verb="process" noun="Data" authenticate="anonymous-all">  <!-- Non-default specified -->
<log message="Processing started"/>  <!-- Uses default info level -->
<econdition field-name="partyId" from="partyId"/>  <!-- Uses default equals -->
```

**CRITICAL: Never use "id" as a type for service parameters**

The "id" type is ONLY valid for entity fields, NOT for service parameters. For service parameters that represent entity IDs, use "String" (or omit the type since String is the default).

```xml
<!-- ❌ INCORRECT: "id" type in service parameter -->
<parameter name="partyId" type="id" required="true"/>  <!-- WRONG! -->

<!-- ✅ CORRECT: String type or omit type for ID parameters -->
<parameter name="partyId" required="true"/>  <!-- String is default -->
```

### 2. XML Tag Formatting

**Single-line tags when they fit within max width**

When the opening tag, content, and closing tag fit within the defined maximum width (180 characters), use single-line formatting:

```xml
<!-- ✅ SINGLE LINE: Opening tag, content, and closing tag on one line -->
<field name="fiscalTaxDocumentNumber"><default-field><display/></default-field></field>
<parameter name="cafId" required="true"/>
<set field="statusId" value="Ftd-Active"/>
<econdition field-name="issuerPartyId" from="partyId"/>
```

**Multi-line formatting for complex or long content:**

```xml
<!-- ✅ MULTI-LINE: Complex nested structures -->
<field name="action">
    <conditional-field condition="statusId == 'Ftd-NotIssued'">
        <link url="blockFolio" text="Block" confirmation="Confirm block"/>
    </conditional-field>
</field>
```

**Exceptions to single-line rule:**
- Complex nested structures (3+ levels of nesting)
- Elements with multiple attributes (3+ attributes) 
- Conditional or loop structures
- Service calls and transitions

<!-- ❌ INCORRECT: Unnecessary line breaks -->
<set field="isActive" 
     from="true"/>
<set field="count" 
     value="0"/>
```

### 3. If-Then-Else Formatting

**CRITICAL: Do NOT use `<then>` tags when there is no `<else>` or `<else-if>` clause**

The `<then>` tag is ONLY required when there IS an `<else>` or `<else-if>` clause. Follow these exact rules:

1. **NO `<then>` tag** when there's no else/else-if clause
2. **REQUIRED `<then>` tag** when there IS an else/else-if clause
3. **Compact formatting** for conditional blocks with else clauses

**When NOT to use `<then>` (standalone if statements):**
```xml
<!-- ✅ CORRECT: No then tag when no else/else-if -->
<if condition="hasError">
    <log level="error" message="Error occurred: ${errorMessage}"/>
    <return error="true" message="Processing failed"/>
</if>

<!-- ✅ CORRECT: Single line for simple conditions without else -->
<if condition="debugEnabled"><log level="debug" message="Debug: ${debugInfo}"/></if>

<!-- ❌ INCORRECT: Unnecessary then tag without else -->
<if condition="hasError"><then>
    <log level="error" message="Error occurred: ${errorMessage}"/>
    <return error="true" message="Processing failed"/>
</then></if>
```

**When to use `<then>` (with else/else-if clauses):**

For conditional blocks with else clauses, compact formatting is **MANDATORY**:

1. **Opening tags**: `<if>` and `<then>` MUST be on the same line unless they exceed the maximum line width
2. **Closing-opening pairs**: `</then><else>`, `</then><else-if>`, `</else-if><then>`, `</else-if><else>` MUST be on the same line unless they exceed the maximum line width
3. **Final closing**: `</else></if>` or `</then></if>` MUST be on the same line

```xml
<!-- ✅ CORRECT: then is required when else exists -->
<if condition="isActive"><then>
    <set field="status" value="Active"/>
</then><else>
    <set field="status" value="Inactive"/>
</else></if>

<!-- ✅ CORRECT: Compact if-then-else-if formatting -->
<if condition="userType == 'admin'"><then>
    <set field="hasFullAccess" from="true"/>
    <log level="info" message="Admin user logged in"/>
</then><else-if condition="userType == 'user'"><then>
    <set field="hasFullAccess" from="false"/>
    <log level="info" message="Regular user logged in"/>  
</then><else>
    <set field="hasFullAccess" from="false"/>
    <log level="warn" message="Unknown user type"/>
</else></if>

<!-- ✅ CORRECT: Single statement with else -->
<if condition="isActive"><then><set field="status" value="Active"/></then><else><set field="status" value="Inactive"/></else></if>

<!-- ✅ CORRECT: Nested if statements -->
<if condition="hasPermission"><then>
    <if condition="isActive">
        <set field="canAccess" from="true"/>
    </if>
</then><else>
    <set field="canAccess" from="false"/>
</else></if>

<!-- ❌ INCORRECT: Tags on separate lines -->
<if condition="userType == 'admin'">
    <then>
        <set field="hasFullAccess" from="true"/>
    </then>
    <else>
        <set field="hasFullAccess" from="false"/>
    </else>
</if>

<!-- ❌ INCORRECT: Line breaks between closing-opening pairs -->
<if condition="userType == 'admin'"><then>
    <set field="hasFullAccess" from="true"/>
</then>
<else>
    <set field="hasFullAccess" from="false"/>
</else>
</if>
```

### 4. Parameter and Field Definitions

- Only specify non-default attributes
- Group related parameters together
- Add descriptions only when the parameter name isn't self-explanatory

**Examples:**
```xml
<!-- ✅ CORRECT -->
<in-parameters>
    <parameter name="partyId" required="true"/>  <!-- String is default for service parameters -->
    <parameter name="description"/>  <!-- String type is default -->
    <parameter name="amount" type="BigDecimal"/>
    <parameter name="isActive" type="Boolean" default-value="true"/>
</in-parameters>

<!-- ❌ INCORRECT -->
<in-parameters>
    <parameter name="partyId" type="id" required="true">  <!-- WRONG: "id" type invalid for service parameters -->
        <description>The party ID</description>  <!-- Redundant description -->
    </parameter>
    <parameter name="description" type="String" required="false"/>  <!-- Unnecessary defaults -->
</in-parameters>
```

### 5. Service Definition Guidelines

- Omit default authentication and transaction attributes
- Keep service opening tag compact
- Only add attributes that differ from defaults

**Examples:**
```xml
<!-- ✅ CORRECT -->
<service verb="get" noun="UserInfo">  <!-- Uses defaults -->
<service verb="create" noun="Order" transaction="force-new">  <!-- Non-default transaction -->
<service verb="public" noun="Search" authenticate="anonymous-all">  <!-- Non-default auth -->

<!-- ❌ INCORRECT -->
<service verb="get" noun="UserInfo" authenticate="true" transaction="use-or-begin">  <!-- Unnecessary defaults -->
```

### 6. List Variable Naming Convention

**REQUIRED: List variables must end with "List"**

```xml
<!-- ✅ CORRECT: List variables end with "List" -->
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigList">
    <econdition field-name="isActive" value="Y"/>
</entity-find>

<iterate list="activeConfigList" entry="activeConfig">
    <log message="Processing config for party ${activeConfig.partyId}"/>
</iterate>

<!-- ❌ INCORRECT: Generic or unclear list names -->
<entity-find entity-name="mycompany.dte.CafAutoRequest" list="activeConfigs">
<entity-find entity-name="mycompany.dte.Caf" list="allCafs">
```

### 7. Service Call Formatting

When writing Moqui service calls, follow these formatting rules for better readability:

#### Single-Line Formatting (Preferred)
Keep service calls on single lines when they fit comfortably (under 120 characters):

```xml
<!-- Good: Simple service calls on single line -->
<service-call name="mycompany.dte.DteInfoServices.get#DteGeneratorParties" out-map="dteGenResult"/>
<service-call name="example.Services.simple#Call" in-map="[param:value]" out-map="result"/>
```

#### Multi-Line Formatting (When Necessary)
Only break to multiple lines when the call would exceed readability limits or has many complex parameters:

```xml
<!-- Use multi-line only for complex calls -->
<service-call name="example.Services.complex#CallWithManyParameters" 
              in-map="[param1:value1, param2:value2, param3:value3, param4:value4, 
                      complexParam:complexValue]"
              out-map="complexResult"/>
```

#### Guidelines:
- Prioritize readability over strict line length rules for short service calls
- Follow existing code style in the file being modified
- Use consistent indentation (typically 12-16 spaces for service calls within actions)
- Keep parameter maps readable - break complex maps across lines if needed
- Align continuation lines with the opening attribute when using multi-line format

#### Avoid (for short calls):
```xml
<!-- Don't break simple calls unnecessarily -->
<service-call name="short.Service.name#method" 
              out-map="result"/>
```

#### Service Call Parameter Patterns

**Prefer `in-map` over `field-map` for simple parameter passing**

```xml
<!-- ✅ PREFERRED: in-map (even when multi-line) -->
<service-call name="mantle.order.OrderInfoServices.get#OrderDisplayInfo" in-map="[orderId:orderId]" out-map="context"/>

<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" 
              in-map="[partyId:partyId, fiscalTaxDocumentTypeEnumId:documentType]" out-map="availabilityResult"/>

<!-- ❌ AVOID: field-map sub-tags (unless special cases) -->
<service-call name="mycompany.dte.DteFolioServices.check#FolioAvailability" out-map="availabilityResult">
    <field-map field-name="partyId" from="partyId"/>
    <field-map field-name="fiscalTaxDocumentTypeEnumId" from="documentType"/>
</service-call>
```

### 8. XML DSL Over Script Tags

**Prefer XML DSL over script tags for service logic**

```xml
<!-- ✅ PREFERRED: XML DSL approach -->
<set field="totalAvailableFolios" value="0" type="Integer"/>
<set field="isActive" from="caf.activo == 'Y'"/>
<if condition="isActive">
    <entity-find-count entity-name="mycompany.dte.FiscalTaxDocument" count-field="usedFoliosCount">
        <econdition field-name="statusId" operator="not-equals" value="Ftd-NotIssued"/>
    </entity-find-count>
    <set field="availableFolios" from="(caf.hasta - caf.desde + 1) - usedFoliosCount"/>
</if>

<!-- ❌ AVOID: Script blocks for simple assignments -->
<script><![CDATA[
    totalAvailableFolios = 0
    isActive = (caf.activo == 'Y')
    if (isActive) {
        // ... complex entity operations that should be XML DSL
    }
]]></script>
```

### 9. 180-Character Line Limit Rule

**Decision criteria for single-line vs multi-line formatting:**

If the complete statement or expression (including XML tags and all attributes) fits within **180 characters** counting indentation, keep it in a single line. Otherwise, separate it into multiple lines.

```xml
<!-- ✅ SINGLE LINE: Complete expression under 180 characters -->
<econdition field-name="issuerPartyId" from="partyId"/>
<service-call name="mycompany.dte.DteServices.create#CafAutoRequestLog" in-map="context"/>

<!-- ✅ MULTI-LINE: Complete expression over 180 characters -->
<entity-find-count entity-name="mycompany.dte.FiscalTaxDocument" count-field="usedFoliosCount">
    <econdition field-name="issuerPartyId" from="partyId"/>
    <econdition field-name="fiscalTaxDocumentNumber" operator="greater-equals" from="caf.desde"/>
    <econdition field-name="fiscalTaxDocumentNumber" operator="less-equals" from="caf.hasta"/>
    <econdition field-name="statusId" operator="not-equals" value="Ftd-NotIssued"/>
</entity-find-count>
```

### 10. ServiceJob Authentication Requirements

**Services called by ServiceJob must use `authenticate="anonymous-all"`**

```xml
<!-- ✅ REQUIRED: Service designed to run via ServiceJob -->
<service verb="run" noun="CafAutoRequest" authenticate="anonymous-all">
    <description>
        Execute CAF auto-request monitoring and fetching process.
        This service is called by ServiceJob and runs without user login.
    </description>
    <actions>
        <!-- Service implementation -->
    </actions>
</service>
```

### 11. Common Type Mistakes

**Service Parameter Types vs Entity Field Types:**

- **Service parameters**: Use String, Integer, Long, BigDecimal, Boolean, Timestamp, Date, Time, List, Map
- **Entity fields**: Use id, id-long, text-short, text-medium, text-long, text-very-long, date, date-time, time, number-integer, number-decimal, number-float, currency-amount, currency-precise, binary-very-long

The "id" and "id-long" types are database column definitions that only make sense in entity contexts, not in service parameters.

**CRITICAL: 40-Character Maximum Length Constraint for "id" Type Fields**

Fields of type "id" in Moqui entities have a **maximum length of 40 characters**. This is a fundamental database constraint that applies to all id-type fields.

**Why This Matters:**
- Database column constraint: The underlying database column is created with VARCHAR(40)
- Primary key limitations: Many databases have index key length limits that this constraint helps avoid
- Framework consistency: Ensures consistent ID handling across all Moqui applications
- Performance optimization: Fixed-length ID fields optimize database indexing and joins

**Best Practices for ID Values:**
- Keep IDs concise and meaningful (preferably 20-30 characters)
- Use consistent naming patterns like `ENTITY_TYPE_DESCRIPTOR` 
- Avoid long concatenated values that might exceed the limit
- Consider using sequential IDs or UUIDs truncated to fit the constraint
- Test ID generation logic to ensure values stay within bounds

**Common Pitfalls to Avoid:**
- Concatenating multiple long values without length checking
- Using full UUIDs (36 chars + hyphens = 41+ chars) 
- Including long timestamps or descriptive text in IDs
- Assuming unlimited ID length when designing key generation logic

```xml
<!-- ❌ INCORRECT: ID value exceeding 40 characters -->
<create value-field="newRecord" entity-name="CustomEntity">
    <set field="customId" value="VERY_LONG_DESCRIPTIVE_IDENTIFIER_THAT_EXCEEDS_FORTY_CHARACTERS"/>  <!-- 61 chars - TOO LONG -->
    <set field="description" value="Some description"/>
</create>

<!-- ✅ CORRECT: ID value within 40-character limit -->
<create value-field="newRecord" entity-name="CustomEntity">
    <set field="customId" value="CUSTOM_ENTITY_001"/>  <!-- 18 chars - GOOD -->
    <set field="description" value="Some description"/>
</create>

<!-- ✅ CORRECT: Entity definition with proper id field -->
<entity entity-name="CustomEntity" package="component.custom">
    <field name="customId" type="id" is-pk="true"/>  <!-- Will be VARCHAR(40) in database -->
    <field name="description" type="text-medium"/>
</entity>
```

```xml
<!-- ❌ INCORRECT: Database column types in service parameters -->
<service verb="process" noun="Order">
    <in-parameters>
        <parameter name="orderId" type="id"/>  <!-- WRONG: id is for entity fields -->
        <parameter name="amount" type="currency-amount"/>  <!-- WRONG: currency-amount is for entity fields -->
    </in-parameters>
</service>

<!-- ✅ CORRECT: Java types for service parameters -->
<service verb="process" noun="Order">
    <in-parameters>
        <parameter name="orderId"/>  <!-- String is default -->
        <parameter name="amount" type="BigDecimal"/>  <!-- Correct Java type -->
    </in-parameters>
</service>

<!-- ✅ CORRECT: Database column types in entity fields -->
<entity entity-name="Order" package="mantle.order">
    <field name="orderId" type="id" is-pk="true"/>  <!-- Correct for entity -->
    <field name="amount" type="currency-amount"/>  <!-- Correct for entity -->
</entity>
```

### 12. Map and List Initialization

When working with maps of lists or nested data structures, always ensure proper initialization to prevent runtime errors:

**Problem:** 
```xml
<!-- This will cause NullPointerException if the list doesn't exist -->
<script>errorMessagesByTenant[tenantPartyId].add(messageObj)</script>
```

**Solution - Initialize on-the-fly:**
```xml
<!-- Safe: Initialize list if it doesn't exist -->
<if condition="!errorMessagesByTenant[tenantPartyId]">
    <set field="errorMessagesByTenant[tenantPartyId]" from="[]"/>
</if>
<script>errorMessagesByTenant[tenantPartyId].add(messageObj)</script>
```

**Solution - Bulk initialization:**
```xml
<!-- Initialize all expected keys upfront -->
<iterate list="tenantPartyIds" entry="tenantPartyId">
    <set field="errorMessagesByTenant[tenantPartyId]" from="[]"/>
    <set field="warningMessagesByTenant[tenantPartyId]" from="[]"/>
</iterate>
```

**Multi-Level Initialization:**
When working with maps of maps or complex nested structures, ensure ALL levels are initialized:

```xml
<!-- Problem: Neither the map nor the nested list may exist -->
<script>errorMessagesByTenant[tenantPartyId].add(messageObj)</script>

<!-- Solution: Check and initialize both levels -->
<if condition="!errorMessagesByTenant">
    <set field="errorMessagesByTenant" from="[:]"/>
</if>
<if condition="!errorMessagesByTenant[tenantPartyId]">
    <set field="errorMessagesByTenant[tenantPartyId]" from="[]"/>
</if>
<script>errorMessagesByTenant[tenantPartyId].add(messageObj)</script>

<!-- Alternative: Initialize parent map upfront -->
<set field="errorMessagesByTenant" from="errorMessagesByTenant ?: [:]"/>
<set field="warningMessagesByTenant" from="warningMessagesByTenant ?: [:]"/>
<!-- Then initialize child lists as needed -->
<if condition="!errorMessagesByTenant[tenantPartyId]">
    <set field="errorMessagesByTenant[tenantPartyId]" from="[]"/>
</if>
```

**Guidelines:**
- Always check if map entries exist before calling methods like `.add()`, `.size()`, etc.
- Use defensive initialization either upfront or on-demand
- For services implementing `StatusInterface`, ensure tenant message lists are initialized
- When in doubt, initialize - it's better to be safe than cause runtime errors
- Consider using `?:` operator for safe defaults: `messageList = messageMap[key] ?: []`
- Always check if parent maps exist before accessing nested elements
- For services implementing `StatusInterface`, the interface may or may not provide initialized maps - always check

**Common Patterns:**
```xml
<!-- Pattern 1: Check before use -->
<if condition="!messagesByTenant[partyId]">
    <set field="messagesByTenant[partyId]" from="[]"/>
</if>
<script>messagesByTenant[partyId].add(newMessage)</script>

<!-- Pattern 2: Safe assignment -->
<set field="messageList" from="messagesByTenant[partyId] ?: []"/>
<script>messageList.add(newMessage)</script>
<set field="messagesByTenant[partyId]" from="messageList"/>
```

### Compliance and Enforcement

**ALL specialist agents must:**

1. **Apply these guidelines consistently** across all generated and modified code
2. **Review existing examples** in the guide that may violate these rules and update them
3. **Prioritize readability and maintainability** over brevity
4. **Use XML DSL features** instead of script blocks whenever possible
5. **Follow variable naming conventions** especially for lists and iterators

These formatting rules must be consistently applied across all Moqui XML files (services, entities, screens, data files, etc.) to ensure a unified codebase that is easy to read, maintain, and understand.

## Inventory and Asset Model

Moqui's inventory system is built around the `Asset` entity in `mantle-udm`. Understanding this model is essential for WMS, ERP, and any product-tracking development.

### Serialized vs. Bulk Assets: The `hasQuantity` Field

The `Asset.hasQuantity` field is the core discriminator between serialized and bulk inventory:

| Field Value | Meaning | Use Case |
|-------------|---------|----------|
| `'N'` or null (default) | One physical item per Asset record | Serialized products (computers, vehicles, equipment) |
| `'Y'` | Bulk quantity tracked via `quantityOnHandTotal` | Consumables, raw materials, stocked goods |

**Serialized asset** (`hasQuantity = 'N'`):
- Each physical unit has its own `Asset` record
- `serialNumber` field stores the unit identifier
- Quantity is implicitly 1 — never update `quantityOnHandTotal` directly
- Receiving creates a new Asset record per serial number
- Issuance targets a specific `assetId`, not a quantity

**Bulk asset** (`hasQuantity = 'Y'`):
- One `Asset` record covers many units
- `quantityOnHandTotal` tracks total on-hand
- `availableToPromiseTotal` tracks uncommitted stock
- Receiving increments the totals via `AssetDetail`
- Issuance decrements by quantity from a specific Asset

**Receiving behavior**: When `serialNumber` is provided in `ShipmentDetail/ReceiveItem`, `quantityAccepted` defaults to 1 and a new Asset is created. Without a serial number, quantity accumulates on an existing bulk Asset.

**Key files**:
- `runtime/component/mantle-udm/entity/ProductAssetEntities.xml` — `Asset`, `AssetReceipt`, `AssetIssuance`, `Lot`
- `runtime/component/SimpleScreens/screen/SimpleScreens/Shipment/ShipmentDetail/ReceiveItem.xml`

### AssetDetail: Append-Only Ledger Pattern

Asset quantity totals are **never updated directly**. Instead, every inventory change appends an `AssetDetail` record with diff values, and totals are recalculated from the ledger.

```
Asset.quantityOnHandTotal  = SUM(AssetDetail.quantityOnHandDiff)
Asset.availableToPromiseTotal = SUM(AssetDetail.availableToPromiseDiff)
```

Each `AssetDetail` record captures:
- `quantityOnHandDiff` / `availableToPromiseDiff`: The signed change (positive = increase, negative = decrease)
- `effectiveDate`: When the change occurred
- Source document links: `assetReceiptId`, `assetIssuanceId`, `shipmentId`, `orderId`, `workEffortId`, etc.

The service `mantle.product.AssetServices.update#AssetFromDetail` recalculates Asset totals from the ledger and is triggered by EECA when AssetDetail is created.

**Why this matters**: Never write services that directly set `quantityOnHandTotal`. Always create `AssetDetail` records and let the framework recalculate totals. Use `recalculate#AllAssetTotals` to correct any drift.

**EECA trigger**: `AssetDetail` creation automatically fires EECA rules that call `update#AssetFromDetail`.

### AssetIdentification: Multiple Alternate Identifiers

The `AssetIdentification` entity allows an asset to have multiple alternate identifiers beyond `serialNumber`:

```xml
<AssetIdentification assetId="ASSET001" identificationTypeEnumId="AitMac" idValue="AA:BB:CC:DD:EE:FF"/>
<AssetIdentification assetId="ASSET001" identificationTypeEnumId="AitVin" idValue="1HGBH41JXMN109186"/>
```

**Composite PK**: `(assetId, identificationTypeEnumId)` — one value per type per asset.

**Standard identification types** (`identificationTypeEnumId`):

| Enum ID | Description |
|---------|-------------|
| `AitMfgSerial` | Manufacturer Serial Number |
| `AitMac` | Media Access Control (MAC) Address |
| `AitVendorInventory` | Vendor Tracking Number |
| `AitTrackingLabel` | Barcode / Label Number |
| `AitVin` | Vehicle Identification Number |
| `AitLicensePlate` | License Plate |
| `AitGasCard` | Gas Card Number |

Use `AssetIdentification` when an asset needs multiple tracking identifiers (e.g., a server with both a manufacturer serial and a MAC address), or when the identifier type must be explicitly categorized.

### Lot Tracking

The `Lot` entity provides batch/lot tracking orthogonal to serial numbers — a lot can contain many serialized or bulk items:

- `Lot.lotNumber` (audited, unique per manufacturer via `LOT_MFG_ID_LOT` index)
- `Lot.mfgPartyId`, `Lot.quantity`, `Lot.manufacturedDate`, `Lot.expirationDate`
- `Asset.lotId` links any asset (serialized or bulk) to its lot

**Lot vs. Serial**: A lot groups many physical items (e.g., a manufacturing batch); a serial number identifies one item within (or outside of) a lot.