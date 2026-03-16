# Moqui Entity Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/entities.md` - Entity naming, relationships, and view patterns
- `standards/backend/field-types.md` - Complete field type reference
- `standards/backend/entity-indexes.md` - Index naming and placement
- `standards/backend/entity-queries.md` - Entity query patterns and condition reference
- `standards/backend/eeca-patterns.md` - Entity Event Condition Actions
- `standards/backend/performance-optimization.md` - Query tuning, select-field, and batch operations

---

## Entity Templates

### Standard Entity Template
```xml
<entity entity-name="{EntityName}" package="{package.name}">
    <!-- Primary key -->
    <field name="{entityId}" type="id" is-pk="true"/>

    <!-- Foreign key relationships -->
    <field name="{parentId}" type="id"/>

    <!-- Business fields -->
    <field name="name" type="text-medium"/>
    <field name="description" type="text-long"/>

    <!-- Status and lifecycle -->
    <field name="statusId" type="id"/>
    <field name="fromDate" type="date-time"/>
    <field name="thruDate" type="date-time"/>

    <!-- Relationships -->
    <relationship type="one" related="{ParentEntity}" short-alias="parent"/>
    <relationship type="one" title="Status" related="moqui.basic.StatusItem"/>

    <!-- Indexes for performance -->
    <index name="{EntityName}StatusIdx">
        <index-field name="statusId"/>
    </index>
</entity>
```

### Entity with Audit Fields Template
```xml
<entity entity-name="[EntityName]" package="[package.name]">
    <field name="[entityNameId]" type="id" is-pk="true"/>
    <!-- Business fields -->
    <field name="[businessField]" type="text-medium"/>

    <!-- Audit fields (standard pattern) -->
    <field name="lastUpdatedStamp" type="date-time"/>
    <field name="createdDate" type="date-time" default="ec.user.nowTimestamp"/>
    <field name="createdByUserId" type="id" default="ec.user.userId"/>
    <field name="lastModifiedDate" type="date-time"/>
    <field name="lastModifiedByUserId" type="id"/>
</entity>
```

### Extended Entity Template
```xml
<extend-entity entity-name="[ExistingEntity]" package="[existing.package]">
    <field name="[newField]" type="text-medium"/>

    <relationship type="one" related="[RelatedEntity]">
        <key-map field-name="[fieldName]"/>
    </relationship>

    <index name="[IDX_NAME]">
        <index-field name="[fieldName]"/>
    </index>
</extend-entity>
```

---

## View Entity Templates

### Simple Join View
```xml
<view-entity entity-name="PartyAndStatus" package="mycompany.myapp">
    <member-entity entity-alias="P" entity-name="mycompany.myapp.Party"/>
    <member-entity entity-alias="S" entity-name="moqui.basic.StatusItem" join-from-alias="P">
        <key-map field-name="statusId"/>
    </member-entity>

    <alias-all entity-alias="P"/>
    <alias entity-alias="S" name="statusDescription" field="description"/>
</view-entity>
```

### Outer Join View
```xml
<view-entity entity-name="PartyWithOptionalContact" package="mycompany.myapp">
    <member-entity entity-alias="P" entity-name="mycompany.myapp.Party"/>
    <member-entity entity-alias="PCM" entity-name="mycompany.myapp.PartyContactMech"
                   join-from-alias="P" join-optional="true">
        <key-map field-name="partyId"/>
    </member-entity>

    <alias-all entity-alias="P"/>
    <alias entity-alias="PCM" name="contactMechId"/>
</view-entity>
```

### Aggregation View
```xml
<view-entity entity-name="PartyDocumentSummary" package="mycompany.myapp">
    <member-entity entity-alias="FTD" entity-name="mycompany.myapp.FiscalTaxDocument"/>

    <alias entity-alias="FTD" name="issuerPartyId" group-by="true"/>
    <alias entity-alias="FTD" name="fiscalTaxDocumentTypeEnumId" group-by="true"/>
    <alias entity-alias="FTD" name="documentCount" field="fiscalTaxDocumentId" function="count"/>
    <alias entity-alias="FTD" name="totalAmount" field="totalAmount" function="sum"/>
    <alias entity-alias="FTD" name="maxDocumentDate" field="documentDate" function="max"/>
</view-entity>
```

---

## Entity Design Patterns

### Master-Detail Pattern
```xml
<!-- Master entity -->
<entity entity-name="Order" package="mycompany.myapp">
    <field name="orderId" type="id" is-pk="true"/>
    <!-- Header-level fields -->
    <master>
        <detail entity-name="OrderItem" relationship="items"/>
        <detail entity-name="OrderPayment" relationship="payments"/>
    </master>
</entity>

<!-- Detail entity -->
<entity entity-name="OrderItem" package="mycompany.myapp">
    <field name="orderId" type="id" is-pk="true"/>
    <field name="orderItemSeqId" type="id" is-pk="true"/>
    <!-- Line item fields -->
    <relationship type="one" related="mycompany.myapp.Order"/>
</entity>
```

### Effective Dating Pattern
```xml
<entity entity-name="PartyRole" package="mycompany.myapp">
    <field name="partyId" type="id" is-pk="true"/>
    <field name="roleTypeId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>
    <field name="thruDate" type="date-time"/>

    <!-- Find current roles: thruDate IS NULL -->
    <!-- Find historical roles: thruDate IS NOT NULL -->
</entity>
```

### Audit Trail Pattern
```xml
<entity entity-name="PartyAudit" package="mycompany.myapp">
    <field name="auditId" type="id" is-pk="true"/>
    <field name="partyId" type="id" not-null="true"/>
    <field name="auditDate" type="date-time" default="ec.user.nowTimestamp"/>
    <field name="userId" type="id"/>
    <field name="operationType" type="text-short"/>  <!-- CREATE, UPDATE, DELETE -->
    <field name="fieldName" type="text-medium"/>
    <field name="oldValue" type="text-long"/>
    <field name="newValue" type="text-long"/>
</entity>
```

---

## Relationship Patterns

### Multiple Relationships to Same Entity
```xml
<entity entity-name="FiscalTaxDocument">
    <field name="issuerPartyId" type="id"/>
    <field name="receiverPartyId" type="id"/>

    <relationship type="one" title="Issuer" related="mycompany.myapp.Party">
        <key-map field-name="issuerPartyId" related="partyId"/>
    </relationship>
    <relationship type="one" title="Receiver" related="mycompany.myapp.Party">
        <key-map field-name="receiverPartyId" related="partyId"/>
    </relationship>
</entity>
```

### Self-Referencing Relationships
```xml
<entity entity-name="Party">
    <field name="partyId" type="id" is-pk="true"/>
    <field name="parentPartyId" type="id"/>

    <relationship type="one" title="Parent" related="mycompany.myapp.Party">
        <key-map field-name="parentPartyId" related="partyId"/>
    </relationship>
</entity>
```

### Association Entity Pattern (Many-to-Many)
```xml
<entity entity-name="PartyRole" package="mycompany.myapp">
    <field name="partyId" type="id" is-pk="true"/>
    <field name="roleTypeId" type="id" is-pk="true"/>
    <field name="fromDate" type="date-time" is-pk="true"/>
    <field name="thruDate" type="date-time"/>

    <relationship type="one" related="mycompany.myapp.Party"/>
    <relationship type="one" title="RoleType" related="moqui.basic.Enumeration"/>
</entity>
```

---

## Mantle Entity Hierarchies

Mantle uses deep entity hierarchies. Key patterns:

### Shipment Hierarchy
```
Shipment (mantle.shipment.Shipment)
├── ShipmentRouteSegment
│   ├── masterTrackingCode    ← Overall shipment tracking number
│   ├── masterTrackingUrl     ← Overall tracking URL
│   └── carrierPartyId
└── ShipmentPackageRouteSeg
    ├── trackingCode          ← Package-level tracking number
    ├── trackingUrl           ← Package-level tracking URL
    └── shipmentPackageSeqId
```

### Key Relationship Patterns

| Parent | Child | Key Field |
|--------|-------|-----------|
| `OrderHeader` | `OrderPart` | `orderId` |
| `Shipment` | `ShipmentRouteSegment` | `shipmentId` |
| `ShipmentRouteSegment` | `ShipmentPackageRouteSeg` | `shipmentId` + `shipmentRouteSegmentSeqId` |

### Tracking Field Confusion Table

| Intended Data | Route Segment Field | Package Segment Field |
|--------------|--------------------|-----------------------|
| Tracking number | `masterTrackingCode` | `trackingCode` |
| Tracking URL | `masterTrackingUrl` | `trackingUrl` |
| External order ref | `otherPartyOrderId` (on OrderPart) | N/A |

### Entity Field Validation Workflow

Before using field names in code:
1. **Check entity XML** for correct field names
2. **Use field mapping tables** above when similar fields exist
3. **Validate with entity-find-one** before using in queries
4. **Test field access** in development environment

---

## View-Entity Gotchas

### LEFT JOIN Becomes INNER JOIN

Adding an `econdition` on an `optional-join` member's field silently converts the LEFT JOIN into an effective INNER JOIN — rows without a match are excluded.

```xml
<!-- WRONG: This filters out rows where OptionalMember has no match -->
<entity-find entity-name="MyViewEntity">
    <econdition field-name="optionalField" value="someValue"/>
</entity-find>

<!-- CORRECT: Move the condition into the view-entity's member-entity -->
<member-entity entity-alias="OPT" entity-name="OptionalEntity" join-from-alias="MAIN"
               join-optional="true">
    <key-map field-name="someId"/>
    <entity-condition>
        <econdition field-name="optionalField" value="someValue"/>
    </entity-condition>
</member-entity>
```

### complex-alias + select-columns Gotcha

A `<complex-alias expression="...">` using raw SQL referencing a member entity alias (e.g., `MDP.PAYMENT_DATE`) does NOT tell Moqui which JOINs are needed. When used with `select-columns="true"` and FormConfig, if no regular alias with that `entity-alias` is selected, the JOIN is omitted causing SQL errors.

**Fix**: Add a regular alias for the member entity to `select-field` to force the JOIN:
```xml
<!-- Force the JOIN by selecting a regular alias from the same member entity -->
<select-field field-name="regularFieldFromMDP"/>
```

---

## Quality Checklist

**Entity Design:**
- [ ] Entity name follows naming conventions
- [ ] All fields have appropriate types and sizes
- [ ] Primary key is properly defined
- [ ] Foreign key relationships are correct

**Indexes:**
- [ ] Indexes defined INSIDE entity definition
- [ ] Indexes support expected query patterns
- [ ] Unique constraints enforce business rules
- [ ] No duplicate index names

**Relationships:**
- [ ] All relationships have proper key-maps
- [ ] Use `title` for multiple relationships to same entity
- [ ] Short-alias provided for commonly accessed relationships