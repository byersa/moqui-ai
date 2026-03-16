# Entity Field Types

### Primary Key Types

| Type | Length | Usage |
|------|--------|-------|
| `id` | 32 chars | Standard primary keys |
| `id-long` | 64 chars | Very large datasets |

```xml
<field name="exampleId" type="id" is-pk="true"/>
<field name="transactionId" type="id-long" is-pk="true"/>
```

### Text Types

| Type | Max Length | Usage |
|------|------------|-------|
| `text-short` | 50 chars | Codes, abbreviations |
| `text-medium` | 255 chars | Names, titles, emails |
| `text-long` | 4000 chars | Descriptions, notes |
| `text-very-long` | Unlimited (CLOB) | Documents, JSON, HTML |
| `text-indicator` | 1 char | Y/N flags |

```xml
<field name="code" type="text-short"/>
<field name="name" type="text-medium" enable-localization="true"/>
<field name="description" type="text-long"/>
<field name="content" type="text-very-long"/>
<field name="isActive" type="text-indicator" default="'Y'"/>
```

### Numeric Types

| Type | Precision | Usage |
|------|-----------|-------|
| `number-integer` | 32-bit signed | Counts, sequences |
| `number-float` | Single precision | Measurements, percentages |
| `number-decimal` | 18 digits, 6 decimal | Precise calculations |
| `currency-amount` | 18 digits, 6 decimal | Money values |

```xml
<field name="sequenceNum" type="number-integer"/>
<field name="percentage" type="number-float"/>
<field name="taxRate" type="number-decimal"/>
<field name="amount" type="currency-amount"/>
```

### Date/Time Types

| Type | Format | Usage |
|------|--------|-------|
| `date` | YYYY-MM-DD | Dates only |
| `time` | HH:MM:SS | Times only |
| `date-time` | YYYY-MM-DD HH:MM:SS.SSS | Timestamps |

```xml
<field name="birthDate" type="date"/>
<field name="openingTime" type="time"/>
<field name="createdDate" type="date-time"/>
```

### Boolean Types

| Type | Values | Usage |
|------|--------|-------|
| `text-indicator` | 'Y'/'N' | Database-compatible flags |

**Prefer `text-indicator` over `boolean` for database compatibility:**
```xml
<field name="isEnabled" type="text-indicator" default="'N'"/>
```

### Binary Types

| Type | Usage |
|------|-------|
| `binary-very-long` | Files, images (BLOB) |

```xml
<field name="attachment" type="binary-very-long"/>
```

### Field Attributes

| Attribute | Values | Purpose |
|-----------|--------|---------|
| `is-pk` | true/false | Primary key marker |
| `not-null` | true/false | NOT NULL constraint |
| `enable-audit-log` | true/false | Change tracking |
| `enable-localization` | true/false | Multi-language support |
| `encrypt` | true/false | Data encryption |
| `default` | expression | Default value |

```xml
<field name="statusId" type="id" enable-audit-log="true"/>
<field name="description" type="text-medium" enable-localization="true"/>
<field name="creditCard" type="text-medium" encrypt="true"/>
<field name="isActive" type="text-indicator" default="'Y'" not-null="true"/>
```

### Field Naming Conventions

| Pattern | Usage | Example |
|---------|-------|---------|
| `{entity}Id` | Primary key | `orderId` |
| `{related}Id` | Foreign key | `customerId` |
| `{field}TypeEnumId` | Enumeration type | `productTypeEnumId` |
| `statusId` | Status reference | `statusId` |
| `fromDate`/`thruDate` | Date ranges | Standard names |
| `lastUpdatedStamp` | Auto-timestamp | Framework-managed |

### Standard Audit Fields

> **IMPORTANT**: Only `lastUpdatedStamp` is automatically managed by the Moqui framework on every entity. The fields `createdDate` and `createdByUserAccountId` are **NOT** auto-generated — they must be explicitly defined on each entity that needs them. Do not assume `createdDate` exists on framework entities like `WorkEffort`, `Party`, etc. When querying for creation time on entities without an explicit `createdDate` field, use `lastUpdatedStamp` as an approximation (it equals creation time for records that have never been updated).

```xml
<!-- Include in all transactional entities that need creation tracking -->
<field name="createdDate" type="date-time" default="ec.user.nowTimestamp"/>
<field name="createdByUserAccountId" type="id"/>
<!-- lastUpdatedStamp is framework-managed and always present — do NOT define it -->
```

### Type Selection Guidelines

| Scenario | Type |
|----------|------|
| Primary key | `id` |
| Money/currency | `currency-amount` |
| Percentage/rate | `number-decimal` |
| Count/quantity | `number-integer` |
| Y/N flag | `text-indicator` |
| Short code (<50 chars) | `text-short` |
| Name/title (<255 chars) | `text-medium` |
| Long text (<4000 chars) | `text-long` |
| Document/JSON | `text-very-long` |
| File attachment | `binary-very-long` |
| Timestamp | `date-time` |
| Date only | `date` |
