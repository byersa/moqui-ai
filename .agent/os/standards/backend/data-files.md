# Data File Standards

### When Data Files Load (and When They Don't)

**Data files are NOT automatically loaded on application startup.** They are only loaded when:

1. **Empty database on first startup** — Moqui loads types listed in the `entity_empty_db_load` system property (default: `seed,seed-initial,install`)
2. **Explicit command-line invocation** — `./gradlew load -Ptypes=seed,seed-initial,install` (or any subset of types)

**This means:** Adding a new record to a seed data file (e.g., a new `ServiceJobParameter`, enumeration, or `SubscreensItem`) will NOT appear on existing running systems until the data is explicitly loaded or a migration is created.

**For existing environments, use the project's migration system** to deploy data changes automatically at startup. See the [Data Updates Management](../../guidelines/data-updates.md) guideline for the complete workflow: update the seed data file (for new installs) AND register a migration (for existing systems).

### Data Type Categories

| Type | Purpose | Load Order | Cache |
|------|---------|------------|-------|
| `seed` | Framework configuration, enums, status items | First | Often |
| `seed-initial` | Initial config, loaded once | After seed | Yes |
| `install` | Base application data | After seed-initial | No |
| `demo` | Demo/test data | After install | No |
| `migration` | Migration registry (organization-specific) | Never auto-loaded | No |

> **Reminder:** None of these types load automatically on startup. See "When Data Files Load" above.

### File Naming Convention

```
data/
├── {Component}SeedData.xml           # Framework configuration
├── {Component}SecurityData.xml       # Security: groups, permissions
├── {Component}EnumerationData.xml    # Enumerations (if separate)
├── {Component}StatusData.xml         # Status types and items
├── {Component}TypeData.xml           # Type definitions
├── {Component}InstallData.xml        # Initial application data
├── {Component}DemoData.xml           # Demo/test data
├── {Component}L10nData_es.xml        # Spanish translations
└── {Component}L10nData_pt.xml        # Portuguese translations
```

### Data Load Order

**CRITICAL: The order of data types in load parameters has NO effect on file loading order.**

The `entity_empty_db_load` system property (or `-Ptypes` gradle parameter) specifies **which data types to include**, not their loading order. For example:
```bash
# These produce IDENTICAL loading order:
-Ptypes=seed,demo,{project}-test
-Ptypes={project}-test,demo,seed
```

**Actual Loading Order Mechanism:**

Data files load based on TWO factors only:

1. **Component Order** (determined by `depends-on` declarations):
   - Components are sorted topologically based on `component.xml` dependencies
   - Dependencies always load **before** dependents
   - Example: If {component-name} depends on {shared-component}, all {shared-component} data loads first

2. **File Name Order** (alphabetical within each component):
   - Files in each component's `data/` directory are sorted alphabetically by filename
   - Use numeric prefixes to control order: `AAA-First.xml`, `AAB-Second.xml`, `ZZZ-Last.xml`

**The data type attribute** (`type="seed"`, `type="demo"`, etc.) only determines **whether a file is included** based on the requested types - it does NOT affect loading sequence.

**Example Loading Sequence:**
```
Component: moqui-framework (no dependencies)
  → AaaSeedData.xml
  → SecurityData.xml
  → ZzzDemoData.xml

Component: mantle-usl (depends-on: moqui-framework)
  → AccountingSeedData.xml
  → PartyDemoData.xml

Component: {shared-component} (depends-on: mantle-usl)
  → SharedSeedData.xml
  → SharedTestData.xml

Component: {component-name} (depends-on: {shared-component})
  → AAA-UserPermission.xml
  → ABBProjectSetup.xml
  → MyComponentWorkModalityData.xml
```

**Controlling Load Order:**

To ensure specific files load before others:

```xml
<!-- component.xml - declare dependencies -->
<component name="my-component" version="1.0">
    <depends-on name="moqui-framework"/>
    <depends-on name="mantle-usl"/>
    <depends-on name="another-component"/>  <!-- ensures another-component loads first -->
</component>
```

**File naming for order within component:**
```
data/
├── AAA-CoreSeedData.xml      # Loads first (alphabetically)
├── AAB-SecuritySeedData.xml  # Loads second
├── ABC-EnumerationData.xml   # Loads third
├── MyComponentData.xml       # Loads later (S comes after A)
└── ZZZ-DemoData.xml          # Loads last
```

**Common Pitfall:**
If seed data from one file depends on seed data from another file in the SAME component, the dependent file must have a filename that sorts AFTER the dependency file alphabetically.

### Entity Data File Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- Component Header Comment -->

    <!-- ============================================ -->
    <!-- Section: Status Types and Items -->
    <!-- ============================================ -->

    <moqui.basic.StatusType statusTypeId="OrderStatus" description="Order Status"/>
    <moqui.basic.StatusItem statusId="OrdPlaced" statusTypeId="OrderStatus"
                            description="Placed" sequenceNum="1"/>

</entity-facade-xml>
```

### ID Value Conventions

| Data Type | ID Format | Example |
|-----------|-----------|---------|
| Configuration/Reference | ALL_CAPS_SNAKE | `ORDER_STATUS_PLACED` |
| Enumerations | PascalCase or ALL_CAPS | `OrderPlaced` or `ORD_PLACED` |
| User Groups | ALL_CAPS_SNAKE | `DTE_ADMIN` |
| Permissions | ALL_CAPS_SNAKE | `INVOICE_APPROVE` |
| Status Items | PascalCase prefix | `OrdPlaced` |

### Security Data Patterns

```xml
<!-- User Group -->
<moqui.security.UserGroup userGroupId="ORDER_MANAGERS"
                          description="Order Management Users"/>

<!-- Permission -->
<moqui.security.UserPermission userPermissionId="ORDER_VIEW"/>
<moqui.security.UserPermission userPermissionId="ORDER_CREATE"/>

<!-- Group-Permission Assignment -->
<moqui.security.UserGroupPermission userGroupId="ORDER_MANAGERS"
                                     userPermissionId="ORDER_VIEW"/>
```

### Enumeration Patterns

```xml
<!-- Enumeration Type -->
<moqui.basic.EnumerationType enumTypeId="OrderType" description="Order Types"/>

<!-- Enumeration Values -->
<moqui.basic.Enumeration enumId="OtSales" enumTypeId="OrderType"
                         description="Sales Order" sequenceNum="1"/>
<moqui.basic.Enumeration enumId="OtPurchase" enumTypeId="OrderType"
                         description="Purchase Order" sequenceNum="2"/>
```

### Status Flow Patterns

```xml
<!-- Status Type -->
<moqui.basic.StatusType statusTypeId="OrderStatus" description="Order Status"/>

<!-- Status Items -->
<moqui.basic.StatusItem statusId="OrdDraft" statusTypeId="OrderStatus"
                        description="Draft" sequenceNum="1"/>
<moqui.basic.StatusItem statusId="OrdPlaced" statusTypeId="OrderStatus"
                        description="Placed" sequenceNum="2"/>
<moqui.basic.StatusItem statusId="OrdShipped" statusTypeId="OrderStatus"
                        description="Shipped" sequenceNum="3"/>

<!-- Status Transitions (Valid Flow) -->
<moqui.basic.StatusFlowTransition statusFlowId="Default"
                                   statusId="OrdDraft"
                                   toStatusId="OrdPlaced"/>
<moqui.basic.StatusFlowTransition statusFlowId="Default"
                                   statusId="OrdPlaced"
                                   toStatusId="OrdShipped"/>
```

### Service Calls in Data Files (CRITICAL)

**When calling services from XML data files, replace `#` with `-` in service names.**

The service name becomes the XML tag itself (NOT using `<service-call>`):

```xml
<!-- CORRECT: Service name as tag with dash replacing hash -->
<example.OrderServices.create-Order orderId="ORD001" customerName="Test Customer"/>

<!-- WRONG: Do NOT use service-call tag in data files -->
<service-call name="example.OrderServices.create#Order"/>
```

**Service job definitions also use dash:**
```xml
<!-- CORRECT: Use dash in serviceName attribute -->
<moqui.service.job.ServiceJob jobName="DailyReport" serviceName="report.ReportServices.generate-DailyReport"
                              cronExpression="0 0 6 * * ?"/>

<!-- WRONG: Hash character not valid in XML attribute -->
<moqui.service.job.ServiceJob serviceName="report.ReportServices.generate#DailyReport"/>
```

**Note:** In code (Groovy/Java) and screen XML, continue using `#` as normal:
```groovy
ec.service.sync().name("example.OrderServices.create#Order").call()
```

### Environment-Specific Data

**Demo data only for development:**
```xml
<entity-facade-xml type="demo">
    <!-- Demo customers, orders, etc. -->
    <!-- NEVER include production credentials -->
</entity-facade-xml>
```

**Use seed-initial for one-time setup:**
```xml
<entity-facade-xml type="seed-initial">
    <!-- Initial admin user (will not override if exists) -->
</entity-facade-xml>
```

### SubscreensItem: Always Use ALL_USERS

> **IMPORTANT**: The `userGroupId` field on `moqui.screen.SubscreensItem` is **DEPRECATED** for access control. The entity description states: *"While still supported, to control access to subscreens use ArtifactAuthz and related records instead."*

**Always use one SubscreensItem per screen with `userGroupId="ALL_USERS"`.** Although `ALL_USERS` is the entity default, `userGroupId` is a **primary key field** and must be explicit in seed data (see `xml-defaults.md` § "Exception: PK Fields in Seed Data Records"). Control access exclusively through ArtifactAuthz records — ArtifactAuthz controls both access AND menu visibility.

```xml
<!-- Correct: Single entry, userGroupId explicit (PK field) -->
<moqui.screen.SubscreensItem screenLocation="component://webroot/screen/webroot/apps.xml"
        subscreenName="MyApp" userGroupId="ALL_USERS"
        subscreenLocation="component://my-app/screen/MyApp.xml"
        menuTitle="My App" menuIndex="60" menuInclude="Y"/>
```

**Anti-pattern (DO NOT USE):** Multiple SubscreensItem entries with different `userGroupId` values. While technically valid (userGroupId is part of the composite PK), this creates confusion and duplicates what ArtifactAuthz already handles.

See `security.md` → "Screen Access Control (SubscreensItem + ArtifactAuthz)" for the complete authorization pattern.

### Data Type Dependency for ArtifactAuthz

> **CRITICAL**: ArtifactAuthz records that reference a UserGroup **must be in the same data type (or a later-loading type) as the UserGroup definition**. If a UserGroup is defined in `{project}-demo` type data, its ArtifactAuthz entries must also be in `{project}-demo` (or later), not in `seed` — because the group does not exist when seed data loads.

```xml
<!-- WRONG: UserGroup in demo data, ArtifactAuthz in seed data -->
<!-- File: DemoData.xml type="{project}-demo" -->
<moqui.security.UserGroup userGroupId="DEMO_TEAM" description="Demo Team"/>

<!-- File: SeedSetup.xml type="seed" — FK to DEMO_TEAM will FAIL -->
<moqui.security.ArtifactAuthz artifactAuthzId="MY_AUTHZ" userGroupId="DEMO_TEAM" .../>

<!-- CORRECT: Both in same data type -->
<!-- File: DemoData.xml type="{project}-demo" -->
<moqui.security.UserGroup userGroupId="DEMO_TEAM" description="Demo Team"/>
<moqui.security.ArtifactAuthz artifactAuthzId="MY_AUTHZ" artifactGroupId="MY_APP"
       userGroupId="DEMO_TEAM" authzTypeEnumId="AUTHZT_ALWAYS" authzActionEnumId="AUTHZA_ALL"/>
```

This applies to all FK-dependent data: if the parent record is in demo data, the child record must also be in demo data (or later).

### Site Component Conventions

When a project is deployed as a separate site (e.g., `{project}-site`), certain data must live in the **site component** as `seed` type, not in the feature component.

**Screen mount and authorization — always seed in the site component:**
```xml
<!-- {project}-site/data/ProjectScreenMountSetup.xml type="seed" -->
<moqui.screen.SubscreensItem screenLocation="component://webroot/screen/webroot/apps.xml"
        subscreenName="{AppName}" userGroupId="ALL_USERS"
        subscreenLocation="component://{project}/screen/{AppName}.xml"
        menuTitle="{Menu Title}" menuIndex="60" menuInclude="Y"/>

<userGroups userGroupId="{PROJECT}_ADMIN" description="{Project} Administrators"/>

<artifactGroups artifactGroupId="{PROJECT}_APP" description="{Project} Access">
    <moqui.security.ArtifactGroupMember artifactName="component://{project}/screen/{AppName}.xml"
               artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
    <moqui.security.ArtifactAuthz artifactAuthzId="{PROJECT}_ADMIN" userGroupId="ADMIN"
           authzTypeEnumId="AUTHZT_ALWAYS" authzActionEnumId="AUTHZA_ALL">
        <filters entityFilterSetId="MANTLE_ACTIVE_ORG"/>
    </moqui.security.ArtifactAuthz>
</artifactGroups>
```

**{ADMIN_USER} user override — seed in site component `AAASetupData.xml`:**

Each site component overrides the shared {ADMIN_USER} user with a project-specific username. The `userId` is always `{ADMIN_USER}`; the `username` varies per project.

```xml
<!-- {project}-site/data/AAASetupData.xml type="seed" -->
<moqui.security.UserAccount userId="{ADMIN_USER}"
        username="{projectshort}" userFullName="{Project} Admin"
        emailAddress="{project}@{domain}" timeZone="Chile/Continental"
        locale="es_CL" currencyUomId="CLP"
        passwordHashType="SHA-256" passwordBase64="N"
        passwordSalt="{8-char-salt}"
        currentPassword="{sha256-hex-hash}"/>

<moqui.security.UserGroupMember userGroupId="ADMIN" userId="{ADMIN_USER}" fromDate="2024-01-01"/>
<moqui.security.UserGroupMember userGroupId="{PROJECT}_ADMIN" userId="{ADMIN_USER}" fromDate="2024-01-01"/>
<moqui.security.UserGroupMember userGroupId="MY_ACCOUNT_USERS" userId="{ADMIN_USER}" fromDate="2024-01-01"/>
```

**Rules:**
- Do NOT set `partyId` on the {ADMIN_USER} user unless the project specifically requires a party association (rare exception)
- Each project gets a unique username, salt, and password hash
- Group memberships include ADMIN, the project-specific admin group, and MY_ACCOUNT_USERS

### Data File Error Handling (Cascade Failure)

**CRITICAL: A single error in a data file causes the framework to skip loading the ENTIRE file.**

If any record in a data file fails (FK violation, invalid field value, constraint error), Moqui rolls back the entire file's transaction. No records from that file will be loaded — not even the valid ones before the error.

**Implications:**
- Keep data files focused and small; don't bundle unrelated data in one file
- Place critical seed data (status types, enumerations) in separate files from records that depend on external data
- When debugging missing data, check the log for errors in ANY record within the same data file
- Use alphabetical file naming prefixes (`AAA-`, `AAB-`) to separate independent data that must not cascade-fail together

### Migration Registry Files

Files with `type="migration"` are **never auto-loaded** by the framework. They are loaded exclusively by the project's migration system at startup. The migration tool iterates over all components looking for migration registry files and loads them directly. See `framework-guide.md` § "ToolFactory Lifecycle and Self-Bootstrapping" for the patterns used.

**Key rules:**
- Migration ID format: `{component}-{YYYYMMDD}-{ticket}-{sequence}` (convention; check overlay for specifics)
- Version field: ticket ID during development, relabeled to semver at release
- Dependencies between migrations can be declared for explicit ordering
- See `guidelines/data-updates.md` for the generic migration workflow
- Check the organization's overlay for implementation-specific registry format and conventions

### Anti-Patterns

```xml
<!-- WRONG: Production credentials in data files -->
<moqui.security.UserAccount username="admin" currentPassword="secret123"/>

<!-- WRONG: Mixed data types in one file -->
<entity-facade-xml type="seed">
    <moqui.basic.Enumeration .../>  <!-- seed data -->
    <example.Customer .../>          <!-- should be install or demo -->
</entity-facade-xml>

<!-- WRONG: Hard-coded environment-specific data -->
<SystemProperty propertyValue="https://production.example.com"/>
```
