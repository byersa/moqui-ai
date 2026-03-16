# XML DSL vs Script

### Three-Tier Approach

**Priority order for service implementation:**

1. **XML DSL** (preferred) - Moqui's declarative XML elements
2. **External Groovy file** - When majority of logic is Groovy
3. **Inline script blocks** (discouraged) - Only for small calculations

### When to Use XML DSL

**Always prefer XML DSL for:**
- Entity operations (`entity-find`, `entity-find-one`, `entity-create`, `entity-update`)
- Service calls (`service-call`)
- Conditionals with simple logic (`if`, `else`, `else-if`)
- Iteration (`iterate`)
- Variable setting (`set`)
- Error returns (`return`)
- Messages (`message`)

```xml
<!-- CORRECT: XML DSL for entity operations -->
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" value="OrderPlaced"/>
    <order-by field-name="orderDate"/>
</entity-find>

<!-- CORRECT: XML DSL for conditionals -->
<if condition="order == null">
    <return error="true" message="Order not found"/>
</if>
```

### When to Use External Groovy

**Move to external Groovy file when:**
- Service is >80% Groovy code
- Complex algorithm implementation
- Heavy string manipulation
- External library integration
- Complex data transformations

```xml
<!-- Service definition -->
<service verb="process" noun="ComplexData" location="component://example/service/ProcessingScript.groovy" method="processComplexData">
    <in-parameters>
        <parameter name="inputData" type="List"/>
    </in-parameters>
    <out-parameters>
        <parameter name="result" type="Map"/>
    </out-parameters>
</service>
```

### When to Use Inline Script

**Only use inline script for:**
- Small calculations (1-3 lines)
- Date/timestamp manipulation
- Simple list/map transformations
- String formatting

```xml
<!-- ACCEPTABLE: Small calculation -->
<script>totalAmount = quantity * unitPrice</script>

<!-- ACCEPTABLE: Timestamp conversion -->
<script><![CDATA[
    if (fromDate) {
        fromDateTs = java.sql.Timestamp.valueOf(fromDate + ' 00:00:00')
    }
]]></script>
```

### Anti-Patterns to Avoid

```xml
<!-- WRONG: Entity operations in script -->
<script><![CDATA[
    def order = ec.entity.find("Order")
        .condition("orderId", orderId).one()
    if (order == null) {
        ec.message.addError("Order not found")
        return
    }
]]></script>

<!-- CORRECT: Use XML DSL -->
<entity-find-one entity-name="Order" value-field="order"/>
<if condition="order == null">
    <return error="true" message="Order not found"/>
</if>
```

```xml
<!-- WRONG: Service call in script -->
<script>ec.service.sync().name("create#Customer").parameters(params).call()</script>

<!-- CORRECT: Use service-call element -->
<service-call name="create#Customer" in-map="params"/>
```

### Script Block Best Practices

**When script is necessary:**

```xml
<!-- Use CDATA for scripts with special characters -->
<script><![CDATA[
    def result = items.findAll { it.quantity > 0 }
        .collect { [id: it.itemId, total: it.quantity * it.price] }
]]></script>

<!-- Keep scripts short and focused -->
<script>formattedDate = dateValue?.format('dd/MM/yyyy') ?: 'N/A'</script>
```

### `filter-map-list` Does NOT Support `operator` on `field-map`

Unlike `econdition` (which supports `operator="in"`, `operator="not-in"`, etc.), the `field-map` element inside `filter-map-list` only supports **equality matching**. There is no `operator` attribute.

```xml
<!-- WRONG: field-map does not support operator -->
<filter-map-list list="partyCandidateList">
    <field-map field-name="partyId" operator="in" from="validPartyIds"/>
</filter-map-list>

<!-- CORRECT: Use Groovy for "in" filtering -->
<script>partyCandidateList.retainAll { it.partyId in validPartyIds }</script>

<!-- CORRECT: Or use removeAll for exclusion -->
<script>itemList.removeAll { it.statusId in excludedStatusIds }</script>
```

**`filter-map-list` is limited to equality**:
```xml
<!-- This works: equality match -->
<filter-map-list list="items"><field-map field-name="statusId" value="Active"/></filter-map-list>

<!-- For anything else (in, not-in, greater-than, etc.), use script -->
```

### Groovy Operator Precedence in XML `set` and `condition`

**Critical**: Groovy's Elvis operator `?:` has **lower precedence** than arithmetic operators (`+`, `*`). This means expressions in `set` field values and `if` conditions can silently misparse:

```xml
<!-- WRONG: Parses as a ?: (0 + subitem.amount * subitem.quantity) -->
<set field="item.total" from="item.total?:0 + subitem.amount*subitem.quantity"/>
<!-- If item.total is non-null, the entire RHS (0 + amount*qty) is IGNORED -->

<!-- CORRECT: Parentheses force the Elvis to resolve first -->
<set field="item.total" from="(item.total?:0) + subitem.amount*subitem.quantity"/>
```

**Why this is dangerous**: The expression produces correct results when the left operand is `null` (falls through to `0 + amount*qty`), but silently ignores the addition when the operand is non-null. This means the bug only manifests when accumulating across multiple iterations — the first iteration works, subsequent ones don't add.

**Rule**: Always wrap `?:` (Elvis) and `?:` (safe-navigation fallback) in parentheses when combined with arithmetic:

```xml
<!-- Safe patterns -->
<set field="total" from="(amount?:0) + tax"/>
<set field="count" from="(count?:0) + 1"/>
<set field="price" from="(basePrice?:0) * quantity"/>

<!-- Also applies in if conditions -->
<if condition="(item.amount?:0) + adjustment > threshold">
```

### Decision Matrix

| Scenario | Recommended Approach |
|----------|---------------------|
| Entity query | XML DSL (`entity-find`) |
| Service call | XML DSL (`service-call`) |
| Simple condition | XML DSL (`if`) |
| Date formatting | Inline script |
| Complex algorithm | External Groovy |
| List transformation | Inline script (if simple) or External Groovy |
| Error handling | XML DSL (`return error="true"`) |
| Logging | Inline script (`ec.logger.info()`) |
