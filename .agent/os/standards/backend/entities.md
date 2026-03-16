# Entity Standards

### Entity Naming Convention
- **CamelCase Names**: Use CamelCase for entity names (e.g., `FiscalTaxDocument`, `PartyRelationship`)
- **No Plurals**: Entity names are singular (e.g., `Order` not `Orders`)
- **Domain Prefixes**: Use domain prefixes for grouping (e.g., `mantle.party.Party`, `moqui.security.UserAccount`)

### Field Standards
- **Primary Keys**: Use `id` suffix for primary keys (e.g., `orderId`, `partyId`)
- **Foreign Keys**: Match the referenced entity's primary key name
- **Timestamps**: Include `lastUpdatedStamp` (framework handles automatically)
- **Appropriate Types**: Use Moqui field types (`id`, `id-long`, `text-medium`, `text-long`, `currency-amount`, `date-time`)

### Relationship Standards
- **Meaningful Aliases**: Use `short-alias` for relationship navigation
- **Relationship Types**: `one` (many-to-one), `many` (one-to-many), `one-nofk` (no foreign key)
- **Key Maps**: Define explicit `key-map` elements for relationships

### View Entities
- **Alias Naming**: Alias names must match for EntityFilter propagation
- **Member Entities**: Use meaningful `entity-alias` values
- **Join Types**: Specify appropriate join types for optional relationships

### Access Control
- **No Inline Constraints**: Do NOT include tenant/access constraints in entity definitions
- **Use EntityFilters**: Row-level security through EntityFilter system
- **Separation of Concerns**: Entity definitions focus on data structure, not access control

### Indexes
- **Foreign Keys**: Index all foreign key columns
- **Frequently Queried**: Index fields used in common queries
- **Composite Indexes**: Create for multi-field query patterns

### Data Integrity
- **Database Constraints**: Use NOT NULL, UNIQUE at database level
- **Referential Integrity**: Define proper foreign key relationships
- **Validation**: Implement validation in services, not entities

### Relationship Type Reference

| Type | Cardinality | FK Location | Usage |
|------|-------------|-------------|-------|
| `one` | Many-to-one | This entity | FK to parent |
| `many` | One-to-many | Related entity | Child collection |
| `one-nofk` | Many-to-one | No FK constraint | Loose coupling |

```xml
<!-- one: FK in this entity pointing to parent -->
<relationship type="one" related="Customer">
    <key-map field-name="customerId"/>
</relationship>

<!-- many: Children reference this entity -->
<relationship type="many" related="OrderItem" short-alias="items">
    <key-map field-name="orderId"/>
</relationship>

<!-- one-nofk: Reference without FK constraint -->
<relationship type="one-nofk" related="moqui.basic.Enumeration" short-alias="status">
    <key-map field-name="statusId" related="enumId"/>
</relationship>
```

> **When to use `one-nofk`**: Beyond Enumeration references, use `one-nofk` when the related entity's referenced columns are covered by a unique index but NOT by a PRIMARY KEY or explicit UNIQUE constraint. H2 (and some other databases) require a PK or UNIQUE constraint — not just a unique index — as the target of a foreign key. If a `type="one"` relationship fails with an FK constraint error referencing a unique-indexed column, change to `type="one-nofk"` to maintain the logical relationship without the DB-level FK.

### Entity Package Organization

| Package Pattern | Content |
|-----------------|---------|
| `{company}.{domain}` | Core domain entities |
| `{company}.{domain}.view` | View entities |
| `moqui.basic` | Framework reference data |
| `mantle.party` | Party/organization entities |

### View Entity Patterns

```xml
<view-entity entity-name="OrderAndCustomer" package="example.view">
    <member-entity entity-alias="O" entity-name="example.Order"/>
    <member-entity entity-alias="C" entity-name="mantle.party.Party" join-from-alias="O">
        <key-map field-name="customerId" related="partyId"/>
    </member-entity>

    <!-- Alias for EntityFilter propagation (name MUST match) -->
    <alias entity-alias="O" name="ownerPartyId"/>

    <!-- Select specific fields -->
    <alias entity-alias="O" name="orderId"/>
    <alias entity-alias="O" name="orderDate"/>
    <alias entity-alias="C" name="customerName" field="organizationName"/>
</view-entity>
```

### View Entity Gotchas

#### entity-condition on the Primary Member-Entity is Silently Ignored

**CRITICAL**: `<entity-condition>` inside the first `<member-entity>` (the one without `join-from-alias`) is **silently ignored** by the framework. No error or warning is logged.

The framework only processes member-entity `entity-condition` in two cases:
- **Joined members** (with `join-from-alias`): condition is added to the JOIN ON clause (`EntityFindBuilder.appendJoinConditions`)
- **Sub-select members** (with `sub-select="true"`): condition is added to the sub-select WHERE clause (`EntityFindBuilder.makeSqlMemberSubSelect`)

The primary member is added as a bare FROM table with no condition processing.

```xml
<!-- WRONG: entity-condition is silently ignored on the primary member -->
<view-entity entity-name="ActiveOrders" package="example">
    <member-entity entity-alias="ORD" entity-name="example.Order">
        <entity-condition>
            <econdition field-name="statusId" value="OrdActive"/>  <!-- NEVER APPLIED -->
        </entity-condition>
    </member-entity>
    <member-entity entity-alias="ITM" entity-name="example.OrderItem" join-from-alias="ORD">
        <key-map field-name="orderId"/>
    </member-entity>
</view-entity>

<!-- CORRECT: Use top-level entity-condition on the view-entity -->
<view-entity entity-name="ActiveOrders" package="example">
    <member-entity entity-alias="ORD" entity-name="example.Order"/>
    <member-entity entity-alias="ITM" entity-name="example.OrderItem" join-from-alias="ORD">
        <key-map field-name="orderId"/>
    </member-entity>
    <entity-condition>
        <econdition field-name="statusId" value="OrdActive"/>  <!-- Applied as WHERE clause -->
    </entity-condition>
</view-entity>
```

**Where to put conditions:**

| Condition Target | Placement | Effect |
|------------------|-----------|--------|
| Primary member field | Top-level `<entity-condition>` | WHERE clause |
| Joined member field | Inside `<member-entity>` with `join-from-alias` | JOIN ON clause |
| Any member (sub-select) | Inside `<member-entity>` with `sub-select="true"` | Sub-select WHERE |

#### Prefer Explicit Aliases Over alias-all

Using `<alias-all>` pulls in all fields from a member entity. This can cause unintended side effects:
- Unnecessary columns in SELECT
- Name conflicts with other member entities (duplicates are silently dropped)
- Potential EntityFilter propagation issues (see `entity-filters.md` § EntityFilter Trimming)

```xml
<!-- Prefer explicit aliases -->
<alias name="orderId" entity-alias="ORD"/>
<alias name="orderDate" entity-alias="ORD"/>
<alias name="ownerPartyId" entity-alias="ORD"/>

<!-- Avoid alias-all unless you need most fields -->
<alias-all entity-alias="ORD"/>
```

#### complex-alias expression Breaks select-columns / FormConfig Column Selection

**CRITICAL**: When an alias uses `<complex-alias expression="...">` (raw SQL) that references member entity table aliases (e.g., `MDP.PAYMENT_DATE`), Moqui's query builder **cannot determine which member entities are needed**. The alias lacks an `entity-alias` attribute, so the framework doesn't know which JOINs are required.

This causes no issue when all fields are selected. But when a form-list uses `select-columns="true"` and a user configures their visible columns (FormConfig), Moqui only joins member entities referenced by the selected aliases. If no selected alias has the necessary `entity-alias`, the member entity is omitted from the FROM/JOIN clause -- but the raw SQL expression still references it, causing a database error:

```
ERROR: missing FROM-clause entry for table "mdp" [42P01]
```

**Pattern to avoid:**

```xml
<view-entity entity-name="DetailView" package="example">
    <member-entity entity-alias="MAIN" entity-name="example.MainEntity"/>
    <member-entity entity-alias="EXT" entity-name="example.ExtEntity" join-from-alias="MAIN" join-optional="true">
        <key-map field-name="mainId"/>
    </member-entity>
    <alias-all entity-alias="MAIN"/>
    <alias-all entity-alias="EXT"/>
    <!-- DANGEROUS: raw SQL references EXT but alias has no entity-alias attribute -->
    <alias name="computedField" type="text-medium">
        <complex-alias expression="CASE WHEN EXT.SOME_FIELD IS NOT NULL THEN 'Yes' ELSE 'No' END"/>
    </alias>
</view-entity>
```

If a user's FormConfig hides all regular EXT fields but keeps `computedField` visible, the query will fail because Moqui doesn't join `EXT`.

**Fix**: In the screen's entity-find, always include a proper alias (with explicit `entity-alias`) in `<select-field>` to force the JOIN:

```xml
<entity-find entity-name="example.DetailView" list="detailList">
    <search-form-inputs/>
    <!-- Force EXT join: extStatusId has entity-alias="EXT", ensuring EXT is always joined
         even when computedField (complex-alias expression) is the only EXT-dependent field selected -->
    <select-field field-name="computedField,extStatusId"/>
</entity-find>
```

**Why this works**: `extStatusId` is a regular alias with `entity-alias="EXT"`, so Moqui knows to include `EXT` in the JOIN. The `computedField` raw SQL can then safely reference `EXT.*`.

### Never Modify Framework or Mantle Source (CRITICAL)

**NEVER edit files inside `framework/`, `mantle-udm/`, `mantle-usl/`, `SimpleScreens/`, or other shared components.** These are upstream repositories that receive version updates. Any direct modifications will be lost on upgrade and create merge conflicts.

Instead, use the Moqui extension patterns in your custom component:

| Need | Extension Pattern |
|------|------------------|
| Add fields to existing entities | `extend-entity` (see below) |
| React to entity changes | EECA rules (`*.eecas.xml`) |
| React to service execution | SECA rules (`*.secas.xml`) |
| Override screen behavior | `screen-extend` or mount subscreens via `MoquiConf.xml` |
| Add new functionality | New services/entities in your custom component |

### Extending Entities from Other Components

Use `extend-entity` to add fields to entities defined in other components (e.g., Mantle, Moqui Framework) without modifying the original definition. The extended fields are stored in the same database table.

```xml
<!-- Add project-specific fields to a Mantle entity -->
<extend-entity entity-name="mantle.account.invoice.Invoice" package="mantle.account.invoice">
    <field name="montosBrutos" type="text-indicator">
        <description>Whether the DTE uses gross amounts (Y=gross, N=net)</description>
    </field>
    <field name="codigoSii" type="text-short"/>
</extend-entity>
```

**When to use `extend-entity`:**

| Scenario | Approach |
|----------|----------|
| Add domain-specific fields to a framework/Mantle entity | `extend-entity` |
| Add relationships to an existing entity | `extend-entity` with `<relationship>` |
| Track component-specific state on shared entities | `extend-entity` with a new field |
| Create an entirely new entity | Regular `<entity>` definition |
| Need a separate table with 1:1 relationship | New entity + `one-nofk` relationship |

**Guidelines:**

1. **Package must match**: The `package` attribute in `extend-entity` must match the original entity's package exactly
2. **Field names**: Choose names that won't collide with future framework fields — use a domain-specific prefix if needed
3. **Field types**: Follow the same Moqui field type conventions (`text-indicator`, `text-short`, `id`, etc.)
4. **Placement**: Define `extend-entity` in your component's entity definition files (e.g., `entity/DteEntities.xml`), not in the original component
5. **DB schema**: The new columns are added to the original table — use `gradle load` or runtime schema update to apply

```xml
<!-- Extend with a relationship -->
<extend-entity entity-name="mantle.account.invoice.Invoice" package="mantle.account.invoice">
    <field name="fiscalDocumentId" type="id"/>
    <relationship type="one-nofk" related="mycompany.dte.FiscalTaxDocument">
        <key-map field-name="fiscalDocumentId"/>
    </relationship>
</extend-entity>
```

**Gotcha**: If a `extend-entity` field has type `text-indicator`, its Groovy values are the strings `"Y"` and `"N"`, not booleans. Always compare with `== 'Y'` or `== 'N'`, and use `? 'Y' : 'N'` when setting.