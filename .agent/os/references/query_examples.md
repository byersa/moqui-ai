# Moqui Entity Query Examples

**Standards Reference**: For declarative conventions, see:
- `standards/backend/entity-queries.md` - Entity query patterns and condition reference
- `standards/backend/performance-optimization.md` - Query tuning, select-field, batch operations

---

## Basic Query Patterns

### Find Single Record
```xml
<entity-find-one entity-name="com.example.Product" value-field="product">
    <field-map field-name="productId" from="productId"/>
</entity-find-one>
```

### Find Multiple Records
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <econdition field-name="statusId" value="PsActive"/>
    <order-by field-name="productName"/>
</entity-find>
```

### Find with Count
```xml
<entity-find entity-name="com.example.Product" list="productList" count="totalCount">
    <econdition field-name="statusId" value="PsActive"/>
</entity-find>
```

---

## Filtering Patterns

### Ignore-If-Empty (Optional Filters)
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <econdition field-name="statusId" from="statusId" ignore-if-empty="true"/>
    <econdition field-name="productTypeEnumId" from="productTypeEnumId" ignore-if-empty="true"/>
    <order-by field-name="productName"/>
</entity-find>
```

### Like/Contains
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <econdition field-name="description" operator="like" value="%${searchTerm}%"/>
    <order-by field-name="description"/>
</entity-find>
```

### In-List
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <econdition field-name="statusId" in="statusIdList"/>
    <order-by field-name="description"/>
</entity-find>
```

### Between (Range)
```xml
<entity-find entity-name="com.example.Order" list="orderList">
    <econdition field-name="orderDate" operator="greater-equals" from="fromDate"/>
    <econdition field-name="orderDate" operator="less" from="thruDate"/>
    <order-by field-name="orderDate"/>
</entity-find>
```

---

## Date Filtering

### Standard date-filter
```xml
<!-- Filters on fromDate/thruDate fields automatically -->
<entity-find entity-name="com.example.Agreement" list="agreementList">
    <date-filter valid-date="effectiveDate"/>
    <order-by field-name="description"/>
</entity-find>
```

### Custom Date Range
```xml
<entity-find entity-name="com.example.Agreement" list="agreementList">
    <econdition field-name="fromDate" operator="less-equals" from="effectiveDate"/>
    <econdition field-name="thruDate" operator="greater" from="effectiveDate"
                ignore-if-empty="true"/>
    <order-by field-name="description"/>
</entity-find>
```

### Recent Records
```xml
<entity-find entity-name="com.example.AuditLog" list="recentLogs">
    <econdition field-name="createdDate" operator="greater-equals"
                from="${ec.user.nowTimestamp - 7}"/>
    <order-by field-name="createdDate" descending="true"/>
</entity-find>
```

---

## Ordering and Pagination

### Multiple Field Ordering
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <order-by field-name="sequenceNum"/>
    <order-by field-name="productName"/>
</entity-find>
```

### Descending Order
```xml
<entity-find entity-name="com.example.Order" list="orderList">
    <order-by field-name="orderDate" descending="true"/>
</entity-find>
```

### Limit Results
```xml
<entity-find entity-name="com.example.Product" list="productList">
    <econdition field-name="statusId" value="PsActive"/>
    <order-by field-name="productName"/>
    <limit-range start="0" size="100"/>
</entity-find>
```

### Manual Pagination
```xml
<entity-find entity-name="com.example.Product" list="productList" count="totalCount">
    <econdition field-name="statusId" value="PsActive"/>
    <order-by field-name="productName"/>
</entity-find>

<script>
def startIdx = pageIndex * pageSize
def endIdx = startIdx + pageSize
productList = productList.subList(startIdx, Math.min(endIdx, productList.size()))
</script>
```

---

## Aggregation Queries

### Count with Group-By
```xml
<entity-find entity-name="com.example.Product" list="statusCounts">
    <select-field field-name="statusId"/>
    <select-field field-name="productId" function="count"/>
    <order-by field-name="statusId"/>
</entity-find>
<!-- Note: group-by is implicit when using select-field with function -->
```

### Sum with Group-By (View Entity)
```xml
<!-- Define view entity for aggregation -->
<view-entity entity-name="OrderSummaryByCustomer" package="com.example">
    <member-entity entity-alias="OH" entity-name="com.example.OrderHeader"/>
    <alias entity-alias="OH" name="customerId" group-by="true"/>
    <alias entity-alias="OH" name="orderCount" field="orderId" function="count"/>
    <alias entity-alias="OH" name="totalAmount" field="grandTotal" function="sum"/>
</view-entity>

<!-- Query the view -->
<entity-find entity-name="com.example.OrderSummaryByCustomer" list="summaryList">
    <order-by field-name="-totalAmount"/>
</entity-find>
```

---

## Data Manipulation

### Create Record
```xml
<entity-create entity-name="com.example.Product" include-nonpk="true"/>
```

### Update Record
```xml
<entity-update entity-name="com.example.Product" include-nonpk="true"/>
```

### Delete Record
```xml
<entity-delete entity-name="com.example.Product"/>
```

### Delete by Condition
```xml
<!-- WARNING: Does NOT trigger EECAs. Call update services manually if needed. -->
<entity-delete-by-condition entity-name="com.example.Product">
    <econdition field-name="statusId" value="PsDraft"/>
</entity-delete-by-condition>
```

### Bulk Update
```xml
<entity-find entity-name="com.example.Product" list="productsToUpdate">
    <econdition field-name="statusId" value="PsDraft"/>
</entity-find>

<iterate list="productsToUpdate" entry="product">
    <set field="product.statusId" from="'PsActive'"/>
    <entity-update value-field="product"/>
</iterate>
```

---

## Advanced Patterns

### Hierarchical Query (Recursive)
```groovy
def findDescendants(parentId, result = []) {
    def children = ec.entity.find("com.example.Category")
        .condition("parentCategoryId", parentId).list()
    for (child in children) {
        result.add(child)
        findDescendants(child.categoryId, result)
    }
    return result
}
def allDescendants = findDescendants(rootCategoryId)
```

### Mantle Entity Hierarchy Queries

Mantle uses deep entity hierarchies. Example with Shipment:

```
ShipmentHeader (mantle.shipment.Shipment)
└── ShipmentRouteSegment (mantle.shipment.ShipmentRouteSegment)
    └── ShipmentPackageRouteSeg (mantle.shipment.ShipmentPackageRouteSeg)
```

```xml
<!-- Find shipment with route segments -->
<entity-find-one entity-name="mantle.shipment.Shipment" value-field="shipment">
    <field-map field-name="shipmentId"/>
</entity-find-one>

<entity-find entity-name="mantle.shipment.ShipmentRouteSegment" list="routeSegments">
    <econdition field-name="shipmentId"/>
    <order-by field-name="shipmentRouteSegmentSeqId"/>
</entity-find>

<!-- Find package-level tracking for a route segment -->
<entity-find entity-name="mantle.shipment.ShipmentPackageRouteSeg" list="packageSegments">
    <econdition field-name="shipmentId"/>
    <econdition field-name="shipmentRouteSegmentSeqId"/>
</entity-find>
```

---

## Performance Patterns

### Select Specific Fields
```xml
<!-- CRITICAL for view-entities with alias-all: prevents loading all columns -->
<entity-find entity-name="com.example.ProductAndStatus" list="productList">
    <select-field field-name="productId"/>
    <select-field field-name="productName"/>
    <select-field field-name="statusId"/>
    <econdition field-name="statusId" value="PsActive"/>
    <order-by field-name="productName"/>
</entity-find>
```

### Use Cache for Frequently Accessed Data
```xml
<entity-find entity-name="moqui.basic.Enumeration" list="enumList" cache="true">
    <econdition field-name="enumTypeId" value="ProductType"/>
    <order-by field-name="sequenceNum,description"/>
</entity-find>
```

### Error Handling
```xml
<entity-find-one entity-name="com.example.Product" value-field="product"/>
<if condition="product == null">
    <return error="true" message="Product not found with ID: ${productId}"/>
</if>
```

---

## Element Order Reference

The correct element order inside `<entity-find>`:
1. `econdition` / `econditions` / `date-filter` / `having-econditions`
2. `select-field`
3. `order-by`
4. `limit-range` / `limit-view` / `use-iterator`

---

## Entity Facade Groovy API

```groovy
// Find one
def order = ec.entity.find("mantle.order.OrderHeader")
    .condition("orderId", orderId).one()

// Find list
def items = ec.entity.find("mantle.order.OrderItem")
    .condition("orderId", orderId)
    .conditionDate("fromDate", "thruDate", ec.user.nowTimestamp)
    .orderBy("orderItemSeqId")
    .limit(100)
    .list()

// Iterator (for large result sets)
def iter = ec.entity.find("mantle.order.OrderItem")
    .condition("orderId", orderId).iterator()
try {
    while (iter.hasNext()) {
        def item = iter.next()
        // process item
    }
} finally {
    iter.close()
}

// Count
long count = ec.entity.find("mantle.order.OrderItem")
    .condition("orderId", orderId).count()

// Create
def newEntity = ec.entity.makeValue("mycomp.myapp.MyEntity")
newEntity.setAll(context)
newEntity.setSequencedIdPrimary()
newEntity.create()

// Update
order.statusId = "OrderApproved"
order.update()

// Store (create or update)
ec.entity.makeValue("mycomp.myapp.MyEntity").setAll(context).createOrUpdate()

// Delete
order.delete()
```