# Row Actions Pattern

Per-row data fetching in form-lists when views aren't sufficient.

## When to Use
- Multiple results per row need aggregation (comma-separated, iterated display)
- Data can't be joined in a single view
- Derived calculations based on row values

## Trade-offs
| Aspect | Entity-View | Row-Actions |
|--------|-------------|-------------|
| Performance | Single query | N+1 queries |
| Sort/Filter | Supported | Not supported on derived fields |
| Complexity | Requires view definition | Inline in screen |

## Pattern
```xml
<form-list name="CustomerList" list="customerList" skip-form="true">
    <row-actions>
        <!-- Fetch related data for this row -->
        <entity-find entity-name="mantle.order.OrderHeader" list="orderList">
            <econdition field-name="customerPartyId" from="partyId"/>
            <econdition field-name="statusId" value="OrderApproved"/>
        </entity-find>
        <set field="orderCount" from="orderList.size()"/>
        <set field="latestOrder" from="orderList.first"/>
    </row-actions>

    <field name="partyId">...</field>
    <field name="orderCount"><default-field><display/></default-field></field>
</form-list>
```

## Prefer Views When Possible
```xml
<!-- Prefer: Use entity-view in main actions -->
<entity-find entity-name="CustomerWithOrderCount" list="customerList">

<!-- Avoid: N+1 query pattern unless aggregation needed -->
<row-actions><entity-find ...>
```