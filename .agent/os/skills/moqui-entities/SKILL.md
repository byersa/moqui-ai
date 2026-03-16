---
name: moqui-entities
description: |
  Moqui Framework entity development patterns including entity definitions, relationships, view-entities, field types, indexes, and caching configuration.

  Use this skill when:
  - Creating or modifying entity definitions (*.xml in entity/ directories)
  - Designing entity relationships (one, many, one-nofk)
  - Creating view-entities for complex queries
  - Writing entity-find queries with complex filtering or aggregation
  - Selecting appropriate field types (id, text-medium, currency-amount, etc.)
  - Configuring entity indexes for performance
  - Setting up entity caching
  - Implementing EECA (Entity Event Condition Actions)
---

# Moqui Entity Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/entity_patterns.md` | Entity definitions, relationships, views, field types, indexes |
| `../../references/query_examples.md` | Entity-find query patterns, filtering, aggregation, data manipulation |
| `../../references/caching_best_practices.md` | Framework caching patterns, avoiding custom caching |
| `../../references/mantle_udm.md` | Mantle UDM business model, Party/Product/Order/Shipment/Invoice/Payment flows |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Entity Auto-Services"** - Auto-service naming patterns, UserAccount creation special case
- **"## Entity Query Operations"** - Order-by syntax, prefix rules, NULL handling, performance
- **"### Entity Audit Logging Best Practices"** - Built-in audit logging, selective field auditing
- **"### Historical Value Tracking with Temporal Primary Keys"** - Design patterns for temporal data
- **"### StatusItem vs Enumeration Usage"** - When to use StatusItem vs Enumeration
- **"### Relationship Title Validation"** - Validating StatusItem and Enumeration relationships
- **"## Mantle Business Model"** - Party, Product, Order, Shipment, Invoice, Payment entity relationships and flows (in `../../references/mantle_udm.md`)

## Quick Reference

### Entity Definition Pattern
```xml
<entity entity-name="EntityName" package="moqui.example" cache="true">
    <field name="entityId" type="id" is-pk="true"/>
    <field name="name" type="text-medium"/>
    <field name="statusId" type="id"/>
    <relationship type="one" related="moqui.basic.StatusItem" short-alias="status"/>
</entity>
```

## Key Principles

1. **Relationship Naming**: Use meaningful short-alias for relationships
2. **Index Placement**: Define indexes INSIDE entity definitions
3. **Field Types**: Use entity field types (id, text-medium), not service parameter types
4. **Caching**: Prefer framework caching over custom implementations