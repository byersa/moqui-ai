# Data Updates Management

This document describes how data changes are applied to existing running environments using a **migration system**.

## Overview

When modifying data files in a Moqui project, changes need to reach both new installations and existing deployed environments:

- **New installations**: Seed data files are loaded via `./gradlew load` — no extra work needed
- **Existing environments**: Data changes must be registered as **migrations** so they execute automatically at startup

**Key Principle**: Seed data files are for fresh installs. Migrations are for updating existing systems. Both must be maintained.

## Migration System Concepts

A migration system provides versioned, automatic data migration:

- **Versioned**: Tied to component semver versions
- **Ordered**: Sorted by component, version, then sequence number
- **Idempotent**: Each migration executes exactly once (tracked in database)
- **Isolated**: Each migration runs in its own transaction
- **Automatic**: Executes pending migrations at application startup

> **Implementation Note**: The specific migration tool (e.g., component, configuration, commands) is organization-specific. Check the organization's overlay documentation for the concrete migration system used in this project.

## When to Create a Migration

**Always register a migration when:**

1. **Adding new seed data** that existing systems need — enumerations, status items, SubscreensItem entries, settings
2. **Modifying existing seed data** — changing descriptions, values, or relationships
3. **Data transformations** — populating new fields, restructuring data
4. **Removing obsolete data** — deleting deprecated records
5. **Any data change** that must be applied to running environments

**No migration needed for:**

- Demo/test data (only used in development)
- Initial data loads (only for fresh installs)
- Entity schema changes (handled automatically by Moqui Framework DDL)

## Development Workflow

### Step 1: Create or update the seed data file (single source of truth)

The seed data file is the **canonical** definition. It serves both new installations (via `gradlew load`) and existing systems (via migration). Always create the seed data first:

```xml
<!-- In data/AABProbeUser.xml (type="seed-initial") -->
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed-initial">
    <moqui.security.UserAccount userId="SYS_PROBE"
            username="_probe" userFullName="Kubernetes Probe Agent"
            currentPassword="3df7d50f8bf1a6aa6c53b6505ccbedd18f5f78a1"
            passwordHashType="SHA" passwordSalt=""
            requirePasswordChange="N" disabled="N"/>
</entity-facade-xml>
```

### Step 2: Register the migration

Register a migration entry pointing to the seed data file or a migration service, depending on the migration system used. The organization's overlay provides the specific migration registry format and conventions.

**Preferred pattern**: Use a declarative data file reference (upsert semantics) when the migration is purely data. Use a service-based migration when conditional logic, iteration, or deletion is needed.

### Step 3: Bump component version

Update `component.xml` version if not already bumped for this release:

```xml
<component name="{component-name}" version="{new-version}">
```

## Migration Types

### Declarative data migrations

Use when the migration is purely entity data (enumerations, translations, configuration records) with no conditional logic. The migration system loads the data file using upsert semantics.

### Service-based migrations

Use when the migration requires conditional logic, iteration, data transformation, deletion, or DDL operations that cannot be expressed as declarative entity data.

```xml
<service verb="migrate" noun="AddNewContentType">
    <actions>
        <entity-find-one entity-name="moqui.basic.Enumeration" value-field="existing">
            <field-map field-name="enumId" value="WectNewType"/>
        </entity-find-one>
        <if condition="!existing">
            <service-call name="create#moqui.basic.Enumeration"
                in-map="[enumId:'WectNewType', enumTypeId:'WorkEffortContentType',
                         description:'New Content Type']"/>
        </if>
    </actions>
</service>
```

### When to use which

| Use service-based when... | Use data file when... |
|---------------------------|----------------------|
| Conditional logic is needed | Creating/updating entity records |
| Iterating over existing records | Loading enumerations, translations |
| Deleting records | Inserting configuration data |
| Data transformations or calculations | Bulk data population |

## Best Practices

### 1. Make migrations idempotent

**Pattern A: Explicit guard** — check before creating:

```xml
<entity-find-one entity-name="moqui.basic.Enumeration" value-field="existing">
    <field-map field-name="enumId" value="WectNewType"/>
</entity-find-one>
<if condition="!existing">
    <service-call name="create#moqui.basic.Enumeration"
        in-map="[enumId:'WectNewType', enumTypeId:'WorkEffortContentType',
                 description:'New Content Type']"/>
</if>
```

**Pattern B: Natural idempotency via old-data query** — for data transformation migrations, query old-format data and use `store#` (upsert) for new records. If the migration already ran, the query returns empty and the service is a no-op:

```xml
<!-- Query old-format records (empty if already migrated) -->
<entity-find entity-name="mantle.party.PartySetting" list="oldSettings">
    <econdition field-name="partySettingTypeId" value="OldSettingType"/>
</entity-find>
<iterate list="oldSettings" entry="old">
    <!-- store# = upsert, safe to re-run -->
    <service-call name="store#mantle.party.PartySetting"
        in-map="[partyId:old.partyId, partySettingTypeId:'NewSettingType',
                 settingValue:old.settingValue]"/>
    <!-- delete old record -->
    <service-call name="delete#mantle.party.PartySetting"
        in-map="[partyId:old.partyId, partySettingTypeId:'OldSettingType']"/>
</iterate>
```

Pattern B is simpler for data transformations because it requires no explicit idempotency checks — the old data query is the check.

### 2. Keep migrations focused

Each migration should do one thing. Multiple changes = multiple migration entries with increasing sequence numbers.

### 3. Never modify executed migrations

Once a migration has been deployed and executed, never change its service logic. Create a new migration instead.

### 4. Document deletions carefully

```xml
<service verb="migrate" noun="RemoveDeprecatedEnum">
    <description>Remove WectOldType (replaced by WectNewType in v1.2.0)</description>
    <actions>
        <service-call name="delete#moqui.basic.Enumeration"
            in-map="[enumId:'WectOldType']"/>
    </actions>
</service>
```

## Multi-Phase Migrations (Rolling Deployments)

When deploying with rolling updates where old and new pods coexist, a single migration that deletes old data can break old pods. In this case, split the migration into two phases across two releases:

1. **Expand (Release N)**: Create new data, keep old data. New code reads new; old code reads old.
2. **Cleanup (Release N+1)**: Delete old data. Remove fallback code.

If you control deployment (maintenance window, single instance), a single-phase migration is simpler.

## Integration with Development Process

### For Claude Code / AI Assistants

**MANDATORY**: When implementing features that modify data files:

1. Add/modify data in the source seed data file (single source of truth)
2. Register a migration using the organization's migration system
3. Bump `component.xml` version if needed

### Checklist for Data Changes

Before completing a feature that includes data changes:

- [ ] Data added/modified in source seed data file (single source of truth)
- [ ] Migration registered using the project's migration system
- [ ] Component version bumped if needed
- [ ] Migration tested locally (restart Moqui, verify auto-execution in logs)
- [ ] Commit includes seed data file and migration changes

## Migration Preparation Workflow

The migration preparation process (analyzing changes, creating migration files, validating dependencies) is organization-specific. Check the organization's overlay for:

- Migration preparation commands and workflows
- Migration registry format and conventions
- Helper scripts for repository scanning
- Migration ID naming conventions

## Related Documentation

- **Data File Patterns**: See `moqui-data` skill for data file naming and load order
- **Commit Guidelines**: See `commit-guidelines.md` for including data changes in commits
- **Testing Guide**: See `testing-guide.md` for testing data migrations