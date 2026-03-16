# Moqui XML Best Practices

**Standards Reference**: For declarative conventions, see:
- `standards/global/xml-defaults.md` - Default value omission rules
- `standards/global/xml-formatting.md` - XML formatting standards, element ordering

---

## Common Anti-Patterns and Solutions

### 1. ignore-if-empty vs ignore

**Problem:** `ignore-if-empty` evaluates the literal string, not the variable.

```xml
<!-- BAD: "%${var}%" is never empty, so never ignored -->
<econdition field-name="name" operator="like" value="%${name}%" ignore-if-empty="true"/>

<!-- GOOD: Explicit null check -->
<econdition field-name="name" operator="like" value="%${name}%" ignore="name == null"/>
```

**When to use each:**
- `ignore-if-empty="true"`: Direct field comparisons without string manipulation
- `ignore="varName == null"`: String patterns with interpolation

### 2. Single-Quoted String Interpolation

**Problem:** Single quotes prevent variable interpolation in Groovy.

```xml
<!-- BAD: Literals, no interpolation -->
<set field="url" from="'${baseUrl}/api/${id}'"/>

<!-- GOOD: Use concatenation -->
<set field="url" from="baseUrl + '/api/' + id"/>

<!-- GOOD: Use script block for complex maps -->
<script><![CDATA[
    urls = [
        xml: "${baseUrl}/api/${id}/xml",
        pdf: "${baseUrl}/api/${id}/pdf"
    ]
]]></script>
```

### 3. Wrong Field Type (RoleType vs Enumeration)

**Problem:** Using Enumerations when RoleType is required.

```xml
<!-- BAD: Using Enumeration for roleTypeId -->
<moqui.basic.Enumeration enumId="WeptOriginator"/>
<WorkEffortParty roleTypeId="WeptOriginator"/>  <!-- WRONG! -->

<!-- GOOD: Use RoleType for roleTypeId -->
<mantle.party.RoleType roleTypeId="MatterOriginator"/>
<WorkEffortParty roleTypeId="MatterOriginator"/>  <!-- CORRECT -->
```

**Field type rules:**
- `roleTypeId` → References `mantle.party.RoleType` entity
- `statusId` → References status enumeration
- `*EnumId` → References enumeration

### 4. Silent Failure

```xml
<!-- BAD -->
<entity-find-one entity-name="Customer" value-field="customer"/>
<!-- What if null? -->

<!-- GOOD -->
<entity-find-one entity-name="Customer" value-field="customer"/>
<if condition="!customer">
    <return error="true" message="Customer ${customerId} not found"/>
</if>
```

---

## Error Handling Pattern

### Meaningful Error Messages
```xml
<if condition="!emailAddress?.matches(emailRegex)">
    <return error="true"
            message="Invalid email format. Must be: user@domain.com"
            type="validation"
            public="true"/>
</if>
```

### Error Types
- `validation` - Input validation errors
- `not_found` - Resource not found
- `authorization` - Permission denied (use `public="false"`)

---

## Security Patterns

### Input Sanitization
```xml
<parameter name="notes" allow-html="none"/>
<parameter name="email" required="true">
    <matches regexp="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"/>
</parameter>
```

### Authorization Checks
```xml
<if condition="!ec.user.hasPermission('CUSTOMER_ADMIN')">
    <return error="true" message="Insufficient permissions" type="authorization"/>
</if>
```

---

## Performance Patterns

### Select Only Needed Fields
```xml
<entity-find entity-name="Customer" list="customerList">
    <select-field field-name="customerId"/>
    <select-field field-name="emailAddress"/>
    <econdition field-name="statusId" value="ACTIVE"/>
</entity-find>
```

### Avoid N+1 Queries
```xml
<!-- BAD: Query in loop -->
<iterate list="customerList" entry="customer">
    <entity-find entity-name="Order" list="orders">
        <econdition field-name="customerId" from="customer.customerId"/>
    </entity-find>
</iterate>

<!-- GOOD: Use view-entity -->
<entity-find entity-name="CustomerOrderView" list="orderList">
    <econdition field-name="customerId" from="customerId"/>
</entity-find>
```

---

## Quality Checklist

- [ ] No explicit default values (see standards)
- [ ] Consistent 4-space indentation
- [ ] Meaningful, descriptive names
- [ ] Proper error handling with clear messages
- [ ] Security considerations addressed
- [ ] Performance optimized (caching, queries)
- [ ] Consistent element ordering (see standards)