# Entity Queries

### Find Single Record

```xml
<entity-find-one entity-name="example.Order" value-field="order">
    <field-map field-name="orderId" from="orderId"/>
</entity-find-one>

<!-- Shorter form when field name matches variable -->
<entity-find-one entity-name="example.Order" value-field="order"/>
```

### Find Multiple Records

```xml
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
    <order-by field-name="orderDate"/>
</entity-find>
```

### Condition Operators

| Operator | Usage |
|----------|-------|
| `equals` (default) | Exact match |
| `not-equals` | Not equal |
| `less` | Less than |
| `less-equals` | Less than or equal |
| `greater` | Greater than |
| `greater-equals` | Greater than or equal |
| `like` | Pattern match (%, _) |
| `not-like` | Negative pattern match |
| `in` | In list |
| `not-in` | Not in list |
| `is-null` | Is null |
| `is-not-null` | Is not null |

### Ignore Empty Conditions

```xml
<!-- Direct field comparison - use ignore-if-empty -->
<econdition field-name="statusId" from="statusId" ignore-if-empty="true"/>

<!-- String pattern - use ignore with explicit check -->
<econdition field-name="name" operator="like" value="%${name}%"
            ignore="name == null"/>
```

**CRITICAL**: `ignore-if-empty` evaluates the literal string, not the variable:
```xml
<!-- WRONG: "%${name}%" is never empty -->
<econdition field-name="name" operator="like" value="%${name}%" ignore-if-empty="true"/>

<!-- CORRECT: Explicit null check -->
<econdition field-name="name" operator="like" value="%${name}%" ignore="name == null"/>
```

### Date Filtering

```xml
<!-- date-filter: checks fromDate/thruDate against provided date -->
<entity-find entity-name="example.Order" list="orderList">
    <date-filter valid-date="effectiveDate"/>
</entity-find>

<!-- Date range query -->
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="orderDate" operator="greater-equals" from="fromDate"/>
    <econdition field-name="orderDate" operator="less" from="thruDate"/>
</entity-find>
```

### In-List Conditions

```xml
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" operator="in" from="statusIdList"/>
</entity-find>
```

### Ordering

```xml
<!-- Single field -->
<order-by field-name="orderDate"/>

<!-- Descending -->
<order-by field-name="orderDate" descending="true"/>

<!-- Multiple fields -->
<order-by field-name="statusId"/>
<order-by field-name="orderDate" descending="true"/>
```

### Pagination

```xml
<entity-find entity-name="example.Order" list="orderList" count="totalCount">
    <econdition field-name="statusId" value="OrdActive"/>
    <order-by field-name="orderDate"/>
</entity-find>

<!-- Apply offset/limit in screen or service -->
<set field="pageSize" value="20" type="Integer"/>
<set field="pageOffset" from="(pageIndex ?: 0) * pageSize"/>
```

### Select Specific Fields (Performance)

```xml
<!-- select-field must come AFTER econditions (XSD schema ordering) -->
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
    <select-field field-name="orderId"/>
    <select-field field-name="orderDate"/>
    <select-field field-name="statusId"/>
</entity-find>
```

### Limit Results

```xml
<entity-find entity-name="example.Order" list="orderList" limit="100">
```

### Cache Query Results

```xml
<!-- Only for reference/configuration data -->
<entity-find entity-name="moqui.basic.Enumeration" list="enumList" cache="true">
    <econdition field-name="enumTypeId" value="ProductType"/>
</entity-find>
```

### Entity CRUD Operations

```xml
<!-- Create -->
<service-call name="create#example.Order" in-map="orderData" out-map="result"/>

<!-- Update -->
<service-call name="update#example.Order" in-map="orderData"/>

<!-- Delete -->
<service-call name="delete#example.Order" in-map="[orderId:orderId]"/>
```

**Never use manual entity operations:**
```groovy
// WRONG
def order = ec.entity.makeValue("Order", data)
order.create()

// CORRECT
ec.service.sync().name("create#Order").parameters(data).call()
```

### Null Check Pattern

```xml
<entity-find-one entity-name="example.Order" value-field="order"/>
<if condition="order == null">
    <return error="true" message="Order ${orderId} not found"/>
</if>
```

### Aggregation

```xml
<entity-find entity-name="example.Order" list="statusCounts">
    <select-field field-name="statusId"/>
    <select-field field-name="orderId" function="count"/>
    <group-by field-name="statusId"/>
</entity-find>
```

### View Entity Queries

```xml
<!-- Query view entity like regular entity -->
<entity-find entity-name="example.OrderAndCustomer" list="orderList">
    <econdition field-name="customerName" operator="like" value="${name}%"
                ignore="name == null"/>
    <order-by field-name="orderDate"/>
</entity-find>
```

### Anti-Patterns

```xml
<!-- WRONG: Query in loop (N+1 problem) -->
<iterate list="customerList" entry="customer">
    <entity-find entity-name="Order" list="orders">
        <econdition field-name="customerId" from="customer.customerId"/>
    </entity-find>
</iterate>

<!-- CORRECT: Use view-entity or join -->
<entity-find entity-name="CustomerOrderView" list="customerOrders"/>
```

### Role Type Consistency

When multiple services operate on the same entity records using `roleTypeId`,
all services MUST use the same condition pattern. Mismatches cause silent bugs
where one service creates records another service cannot find.

```xml
<!-- WRONG: Service A creates with specific role, Service B looks for generic role -->
<!-- Service A (assignment) -->
<service-call name="create#WorkEffortParty"
    in-map="[roleTypeId: 'SupportAgentL1', ...]"/>

<!-- Service B (escalation) - WILL NOT FIND the record above -->
<entity-find entity-name="WorkEffortParty" list="assignments">
    <econdition field-name="roleTypeId" value="Worker"/>
</entity-find>

<!-- CORRECT: Use like operator matching the role prefix convention -->
<entity-find entity-name="WorkEffortParty" list="assignments">
    <econdition field-name="roleTypeId" operator="like" value="Support%"/>
    <econdition field-name="thruDate" from="null"/>
</entity-find>
```

**Rule**: When role types follow a prefix convention (e.g., `SupportAgentL1`,
`SupportAgentL2`, `SupportAgentL3`), use `operator="like" value="Support%"` in
all queries that need to find any role in the family. Never use a different role
string (like `Worker`) to look for records created with specific role types.
