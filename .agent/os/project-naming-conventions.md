# Project Naming Conventions

This file defines how projects configure their naming conventions for services, entities, and data files.

## Project-Specific Configuration

Each project defines its naming conventions in:
```
runtime/component/{main-component}/.agent-os/naming-conventions.md
```

This file contains the concrete values for all placeholders used below.

## Convention Structure

### Service Path Prefix

Services are organized under a dot-notation path prefix:
```
{company}.{project}.{domain}
```

**Example**: `mycompany.myapp.inventory`

Service names follow: `{prefix}.ServiceName.verb#Noun`

### Entity Package Prefix

Entities use a similar dot-notation:
```
{company}.{project}.{domain}
```

### Data File Naming

| Data Type | File Naming Pattern |
|-----------|-------------------|
| Seed data | `{ComponentName}Data.xml` |
| Demo data | `{ComponentName}DemoData.xml` |
| L10n data | `{ComponentName}L10nData_es.xml` |
| Security | `{ComponentName}SecurityData.xml` |
| Test data | `{ComponentName}TestData.xml` |

### Service File Organization

```
service/
├── {company}/{project}/{domain}/
│   ├── EntityNameServices.xml      (CRUD services)
│   ├── DomainWorkflowServices.xml  (business workflows)
│   └── DomainIntegrationServices.xml (external integrations)
```

## Standard Naming Rules

### Service Verbs (Framework Standard)

| Verb | Purpose | Example |
|------|---------|---------|
| `create` | Create new record | `create#Order` |
| `update` | Modify existing record | `update#Customer` |
| `store` | Upsert (create or update) | `store#Setting` |
| `delete` | Delete record | `delete#Item` |
| `get` | Retrieve single record | `get#OrderDetail` |
| `find` | Search/list records | `find#Products` |
| `process` | Business logic | `process#Payment` |
| `validate` | Validation only | `validate#Invoice` |
| `setup` | Context/environment setup | `setup#FilterContext` |

### Entity Field Naming

| Pattern | Example | Usage |
|---------|---------|-------|
| `{entityName}Id` | `orderId` | Primary keys |
| `{relatedEntity}Id` | `customerPartyId` | Foreign keys |
| `{field}TypeEnumId` | `statusTypeEnumId` | Type references |
| `fromDate`/`thruDate` | — | Validity periods |
| `{field}Date` | `invoiceDate` | Date fields |

### CamelCase Convention
- **Entity Names**: CamelCase (e.g., `FiscalTaxDocument`, `PartyRelationship`)
- **Field Names**: camelCase (e.g., `partyId`, `organizationName`)
- **Service Nouns**: CamelCase (e.g., `verb#ServiceNoun`)

## How to Use

1. **At task start**: Read `runtime/component/{main-component}/.agent-os/naming-conventions.md`
2. **Verify prefix**: Confirm the service/entity path prefix for the current project
3. **Apply rules**: Use the project-specific prefix with the standard naming patterns above
4. **Validate**: Check that all new names follow both project prefix and framework conventions
