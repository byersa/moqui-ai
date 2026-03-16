# Status and Workflow Patterns

Status types, items, and flow transitions for state machines.

## Status Type Definition

```xml
<!-- Define a status type (category) -->
<moqui.basic.StatusType statusTypeId="OrderStatus"
    description="Order Status" parentTypeId=""/>
```

## Status Items

```xml
<!-- Define status values within a type -->
<moqui.basic.StatusItem statusTypeId="OrderStatus" statusId="OrdCreated"
    description="Created" sequenceNum="1"/>
<moqui.basic.StatusItem statusTypeId="OrderStatus" statusId="OrdApproved"
    description="Approved" sequenceNum="2"/>
<moqui.basic.StatusItem statusTypeId="OrderStatus" statusId="OrdShipped"
    description="Shipped" sequenceNum="3"/>
<moqui.basic.StatusItem statusTypeId="OrderStatus" statusId="OrdCompleted"
    description="Completed" sequenceNum="4"/>
<moqui.basic.StatusItem statusTypeId="OrderStatus" statusId="OrdCancelled"
    description="Cancelled" sequenceNum="99"/>
```

## Status Naming Convention

| Prefix | Domain | Example |
|--------|--------|---------|
| `Ord` | Orders | `OrdApproved` |
| `Inv` | Invoices | `InvPaid` |
| `Smsg` | System Messages | `SmsgSent` |
| `Es` | Entity Sync | `EsRunning` |

## Status Flow Transitions

Define valid transitions between statuses:

```xml
<!-- Valid transitions (statusFlowId="Default" is standard) -->
<moqui.basic.StatusFlowTransition statusFlowId="Default"
    statusId="OrdCreated" toStatusId="OrdApproved" transitionName="Approve"/>
<moqui.basic.StatusFlowTransition statusFlowId="Default"
    statusId="OrdApproved" toStatusId="OrdShipped" transitionName="Ship"/>
<moqui.basic.StatusFlowTransition statusFlowId="Default"
    statusId="OrdShipped" toStatusId="OrdCompleted" transitionName="Complete"/>

<!-- Allow cancel from multiple states -->
<moqui.basic.StatusFlowTransition statusFlowId="Default"
    statusId="OrdCreated" toStatusId="OrdCancelled" transitionName="Cancel"/>
<moqui.basic.StatusFlowTransition statusFlowId="Default"
    statusId="OrdApproved" toStatusId="OrdCancelled" transitionName="Cancel"/>
```

## Validating Status Transitions

```xml
<service verb="update" noun="OrderStatus">
    <in-parameters>
        <parameter name="orderId" required="true"/>
        <parameter name="statusId" required="true"/>
    </in-parameters>
    <actions>
        <entity-find-one entity-name="example.Order" value-field="order"/>

        <!-- Check valid transition -->
        <entity-find-one entity-name="moqui.basic.StatusFlowTransition" value-field="transition">
            <field-map field-name="statusFlowId" value="Default"/>
            <field-map field-name="statusId" from="order.statusId"/>
            <field-map field-name="toStatusId" from="statusId"/>
        </entity-find-one>

        <if condition="transition == null">
            <return error="true"
                message="Cannot transition from ${order.statusId} to ${statusId}"/>
        </if>

        <set field="order.statusId" from="statusId"/>
        <entity-update value-field="order"/>
    </actions>
</service>
```

## Status Dropdown in Screens

```xml
<!-- Show only valid next statuses -->
<field name="statusId">
    <default-field title="Status">
        <drop-down allow-empty="false">
            <entity-options key="${toStatusId}" text="${description}">
                <entity-find entity-name="moqui.basic.StatusFlowTransitionAndTo">
                    <econdition field-name="statusFlowId" value="Default"/>
                    <econdition field-name="statusId" from="currentStatusId"/>
                    <order-by field-name="sequenceNum"/>
                </entity-find>
            </entity-options>
        </drop-down>
    </default-field>
</field>
```

## Entity Relationship to Status

```xml
<entity entity-name="Order" package="example">
    <field name="orderId" type="id" is-pk="true"/>
    <field name="statusId" type="id"/>

    <!-- Relationship for status lookup -->
    <relationship type="one" title="Order" related="moqui.basic.StatusItem" short-alias="status">
        <key-map field-name="statusId"/>
    </relationship>
</entity>
```

## Key Rules

1. **Use StatusFlowTransition** - Don't allow arbitrary status changes
2. **Prefix by domain** - `OrdApproved`, not just `Approved`
3. **sequenceNum for ordering** - Lower numbers first in dropdowns
4. **Error/Cancel states last** - Use sequenceNum 98/99
5. **Always validate** - Check transition exists before updating