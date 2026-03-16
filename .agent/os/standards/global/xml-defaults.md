# XML Default Values

### Core Principle

**NEVER explicitly set attributes to their default values.**

Setting defaults explicitly creates verbosity, confusion, and maintenance burden.

### Service Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `type` | `inline` | `type="inline"` |
| `authenticate` | `true` | `authenticate="true"` |
| `transaction` | `use-or-begin` | `transaction="use-or-begin"` |

```xml
<!-- BAD: Explicit defaults -->
<service verb="create" noun="Customer" type="inline" authenticate="true"
         transaction="use-or-begin">

<!-- GOOD: Omit defaults -->
<service verb="create" noun="Customer">
```

### Parameter Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `required` | `false` | `required="false"` |
| `type` | `String` | `type="String"` |
| `allow-html` | `none` | `allow-html="none"` |

```xml
<!-- BAD -->
<parameter name="email" type="String" required="false"/>

<!-- GOOD -->
<parameter name="email"/>
```

### Entity Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `cache` | `never` | `cache="never"` |
| `authorize-skip` | `false` | `authorize-skip="false"` |
| `is-pk` | `false` | `is-pk="false"` |
| `enable-audit-log` | `false` | `enable-audit-log="false"` |

```xml
<!-- BAD -->
<entity entity-name="Customer" cache="never" authorize-skip="false">
    <field name="customerId" type="id" is-pk="true"/>
    <field name="name" type="text-medium" is-pk="false" enable-audit-log="false"/>
</entity>

<!-- GOOD -->
<entity entity-name="Customer">
    <field name="customerId" type="id" is-pk="true"/>
    <field name="name" type="text-medium"/>
</entity>
```

### Screen Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `require-authentication` | `true` | `require-authentication="true"` |
| `standalone` | `false` | `standalone="false"` |

```xml
<!-- BAD -->
<screen require-authentication="true" standalone="false">

<!-- GOOD -->
<screen>
```

### Transition Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `method` | `any` | `method="any"` |
| `require-session-token` | `true` (for POST) | `require-session-token="true"` |

```xml
<!-- BAD -->
<transition name="create" method="post" require-session-token="true">

<!-- GOOD -->
<transition name="create" method="post">
```

### REST API Defaults

| Attribute | Default | Don't Specify |
|-----------|---------|---------------|
| `require-authentication` | `true` | `require-authentication="true"` |

```xml
<!-- BAD -->
<resource name="orders" require-authentication="true">

<!-- GOOD -->
<resource name="orders">

<!-- Only specify when non-default -->
<resource name="public-info" require-authentication="false">
```

### Transaction Management (98.5% Rule)

**98.5% of services should omit the `transaction` attribute.**

Only specify for:
- Audit logging: `transaction="require-new"`
- Notifications: `transaction="require-new"`
- Read-only reports: `transaction="ignore"`
- Long-running tasks: manual control

```xml
<!-- Default (omit) - 98.5% of cases -->
<service verb="create" noun="Order">

<!-- Exception: Audit logging -->
<service verb="log" noun="AuditEvent" transaction="require-new">
```

### Exception: PK Fields in Seed Data Records

**Primary key fields with defaults MUST be explicit in seed data records**, even when the value matches the entity's default. This is the ONE exception to the "don't specify defaults" rule.

PK fields define the record's identity. The data loader uses all PK fields for create-or-update matching during `gradlew load`. Omitting a PK field makes the record ambiguous and can cause loading issues.

```xml
<!-- BAD: Omitting PK field with default in seed data -->
<moqui.screen.SubscreensItem screenLocation="component://tools/screen/System.xml"
        subscreenName="MyAdmin"
        subscreenLocation="component://my-component/screen/MyAdmin.xml"/>
<!-- userGroupId is a PK with default 'ALL_USERS' — MUST be explicit -->

<!-- GOOD: All PK fields explicit -->
<moqui.screen.SubscreensItem screenLocation="component://tools/screen/System.xml"
        subscreenName="MyAdmin" userGroupId="ALL_USERS"
        subscreenLocation="component://my-component/screen/MyAdmin.xml"/>
```

**This rule applies ONLY to PK fields in entity data records.** Non-PK fields and XML schema attributes (entity definitions, service definitions, screen attributes) still follow the "omit defaults" rule.

### Why This Matters

1. **Readability**: Non-default values stand out as intentional choices
2. **Maintenance**: Fewer attributes to review and update
3. **Framework updates**: Defaults may change; explicit values become incorrect
4. **Code review**: Easier to spot actual configuration decisions

### Quick Reference

| Context | Only Specify When |
|---------|-------------------|
| Services | Non-default auth, transaction, location |
| Parameters | Required=true, non-String type, validation |
| Entities | cache=true, authorize-skip=true |
| Screens | Anonymous access, standalone |
| REST APIs | Anonymous access |
