# Entity Index Standards

### Index Naming Convention

```xml
<!-- Format: {ENTITY}_{FIELD1}[_{FIELD2}...] -->
<index name="ORDER_STATUS">
    <index-field name="statusId"/>
</index>

<index name="ORDER_DATE_STATUS">
    <index-field name="orderDate"/>
    <index-field name="statusId"/>
</index>
```

### When to Add Indexes

**Always index:**
- Foreign key columns
- Fields used in `econdition` filters
- Fields used in `order-by` clauses
- Status fields
- Date fields used in range queries

**Consider indexing:**
- Fields used in `like` queries (prefix-only patterns)
- Fields with high selectivity (many unique values)
- Fields used in joins

**Avoid indexing:**
- Boolean/indicator fields with low selectivity
- Text fields used for full-text search (use OpenSearch)
- Frequently updated fields (index maintenance overhead)

### Index Placement

**Define indexes in entity definition:**
```xml
<entity entity-name="Order" package="example">
    <field name="orderId" type="id" is-pk="true"/>
    <field name="customerId" type="id"/>
    <field name="statusId" type="id"/>
    <field name="orderDate" type="date-time"/>

    <!-- Foreign key indexes -->
    <index name="ORDER_CUSTOMER">
        <index-field name="customerId"/>
    </index>

    <!-- Query pattern indexes -->
    <index name="ORDER_STATUS">
        <index-field name="statusId"/>
    </index>

    <index name="ORDER_DATE">
        <index-field name="orderDate"/>
    </index>
</entity>
```

### Composite Index Guidelines

**Field order matters - most selective first:**
```xml
<!-- For queries: WHERE statusId = ? AND orderDate >= ? -->
<index name="ORDER_STATUS_DATE">
    <index-field name="statusId"/>
    <index-field name="orderDate"/>
</index>
```

**Composite index supports prefix queries:**
```xml
<!-- This index supports: -->
<!-- - WHERE statusId = ? -->
<!-- - WHERE statusId = ? AND orderDate >= ? -->
<!-- But NOT: -->
<!-- - WHERE orderDate >= ? (alone) -->
<index name="ORDER_STATUS_DATE">
    <index-field name="statusId"/>
    <index-field name="orderDate"/>
</index>
```

### Unique Indexes

```xml
<index name="CUSTOMER_EMAIL" unique="true">
    <index-field name="emailAddress"/>
</index>

<!-- Composite unique constraint -->
<index name="PARTY_IDENT_UNIQUE" unique="true">
    <index-field name="partyId"/>
    <index-field name="partyIdTypeEnumId"/>
</index>
```

### Index Performance Considerations

| Scenario | Recommendation |
|----------|----------------|
| Frequent reads, rare writes | More indexes OK |
| Frequent writes | Minimize indexes |
| Large tables (>1M rows) | Critical to index properly |
| Small lookup tables | Primary key index sufficient |

### Anti-Patterns

```xml
<!-- WRONG: Index on low-selectivity boolean -->
<index name="ORDER_ARCHIVED">
    <index-field name="isArchived"/>
</index>

<!-- WRONG: Too many single-column indexes -->
<!-- Create composite indexes for common query patterns instead -->

<!-- WRONG: Index on large text field -->
<index name="ORDER_NOTES">
    <index-field name="notes"/>
</index>
```

### View Entity Index Hints

For view entities, ensure base entity indexes support the query patterns:

```xml
<view-entity entity-name="OrderAndCustomer" package="example">
    <member-entity entity-alias="O" entity-name="Order"/>
    <member-entity entity-alias="C" entity-name="Customer" join-from-alias="O">
        <key-map field-name="customerId"/>
    </member-entity>
    <!-- Ensure Order.customerId is indexed -->
</view-entity>
```
