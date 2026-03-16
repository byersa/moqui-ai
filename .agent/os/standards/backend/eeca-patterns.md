# EECA Patterns (Entity Event Condition Actions)

Trigger actions automatically on entity create/update/delete.

## File Naming Convention

```
entity/
├── OrderEntities.xml      # Entity definitions
└── Order.eecas.xml        # EECAs for order entities
```

## Basic EECA Structure

```xml
<eecas xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/entity-eca-3.xsd">

    <eeca id="OrderItemChangeHandler" entity="example.OrderItem"
          on-create="true" on-update="true" on-delete="true"
          run-on-error="false" get-entire-entity="true" get-original-value="true">
        <actions>
            <service-call name="example.OrderServices.handle#OrderItemChange" in-map="context"/>
        </actions>
    </eeca>

</eecas>
```

## Trigger Attributes

| Attribute | Description |
|-----------|-------------|
| `on-create="true"` | Trigger on entity create |
| `on-update="true"` | Trigger on entity update |
| `on-delete="true"` | Trigger on entity delete |
| `on-find-one="true"` | Trigger on find-one (rare) |
| `on-find-list="true"` | Trigger on find-list (rare) |

## Data Access Attributes

```xml
<!-- Get full entity data in context -->
<eeca id="FullEntityAccess" entity="example.Order"
      get-entire-entity="true"
      get-original-value="true">
```

| Attribute | Description |
|-----------|-------------|
| `get-entire-entity="true"` | Load all fields into context |
| `get-original-value="true"` | Provide `originalValue` Map with pre-update values |

## Condition Patterns

```xml
<!-- Simple field condition -->
<eeca id="OnApproved" entity="example.Order" on-update="true">
    <condition><expression>statusId == 'OrdApproved'</expression></condition>
</eeca>

<!-- Detect field change -->
<eeca id="OnStatusChange" entity="example.Order" on-update="true"
      get-entire-entity="true" get-original-value="true">
    <condition><expression>statusId != originalValue?.statusId</expression></condition>
</eeca>

<!-- Only on specific transition -->
<eeca id="OnApprovalTransition" entity="example.Order" on-update="true"
      get-entire-entity="true" get-original-value="true">
    <condition><expression>
        originalValue?.statusId == 'OrdDraft' &amp;&amp; statusId == 'OrdApproved'
    </expression></condition>
</eeca>
```

## Revision Tracking Pattern

Track changes across related entities:

```xml
<!-- Increment revision on any related entity change -->
<eeca id="OrderItemRevision" entity="example.OrderItem"
      on-create="true" on-update="true" on-delete="true" run-on-error="false">
    <actions>
        <service-call name="example.OrderServices.increment#OrderRevision" in-map="context"/>
    </actions>
</eeca>

<eeca id="OrderNoteRevision" entity="example.OrderNote"
      on-create="true" on-update="true" on-delete="true" run-on-error="false">
    <actions>
        <service-call name="example.OrderServices.increment#OrderRevision" in-map="context"/>
    </actions>
</eeca>
```

## Available Context Variables

| Variable | Description |
|----------|-------------|
| Entity field names | All PK and changed fields |
| `originalValue` | Map of pre-update values (if `get-original-value="true"`) |
| `entityValue` | Full EntityValue object (if `get-entire-entity="true"`) |
| `ec` | Execution context |

## EECA vs SECA Decision

| Use EECA When | Use SECA When |
|---------------|---------------|
| Trigger on any entity change | Trigger on specific service call |
| Entity changed by multiple services | Need service input/output context |
| Low-level data integrity | Business workflow logic |
| Audit/revision tracking | Status transition actions |

## Best Practices

1. **Keep EECAs lightweight** - Heavy logic belongs in services
2. **Use `run-on-error="false"`** - Prevent cascade failures
3. **Avoid circular triggers** - EECA → service → entity update → EECA...
4. **Group by entity domain** - `Order.eecas.xml` for order-related entities
5. **Use SECAs for business logic** - EECAs for data-level concerns
6. **Test carefully** - EECAs fire on ALL entity operations

## EECA-Aware Operation Ordering

**When a service needs to delete records and create/store replacements on an entity with a validation EECA, the delete must happen BEFORE the store.**

An EECA that validates uniqueness (e.g., duplicate checking) fires synchronously on the store/create operation. If the old records haven't been deleted yet, the EECA sees them and rejects the new record.

```xml
<!-- ❌ WRONG: EECA fires on store, sees siblings not yet deleted → duplicate error -->
<if condition="allSiblingsShareSameValue">
    <service-call name="store#example.EntityIdentification"
                  in-map="[entityId:parentId, typeEnumId:'IdType', idValue:sharedValue]"/>
    <entity-delete-by-condition entity-name="example.EntityIdentification">
        <econdition field-name="entityId" operator="in" from="childIdList"/>
        <econdition field-name="typeEnumId" value="IdType"/>
    </entity-delete-by-condition>
</if>

<!-- ✅ CORRECT: Delete children first, then store on parent -->
<if condition="allSiblingsShareSameValue">
    <entity-delete-by-condition entity-name="example.EntityIdentification">
        <econdition field-name="entityId" operator="in" from="childIdList"/>
        <econdition field-name="typeEnumId" value="IdType"/>
    </entity-delete-by-condition>
    <service-call name="store#example.EntityIdentification"
                  in-map="[entityId:parentId, typeEnumId:'IdType', idValue:sharedValue]"/>
</if>
```

**General rule**: When consolidating data from children to a parent entity that has a uniqueness EECA, always remove from children first, then store on parent.

## entity-delete-by-condition Does NOT Trigger EECAs

**`entity-delete-by-condition` operates at the SQL level and bypasses EECA handlers entirely.** Only individual entity-value operations (`entityValue.delete()`, `entity-delete-related`) trigger EECAs.

This means:
- **On-delete EECAs won't fire** for batch deletions
- **Derived/computed fields that depend on EECAs won't be updated** (e.g., `Invoice.invoiceTotal` which is recalculated by an EECA on `InvoiceItem` delete)

```xml
<!-- ❌ EECA on InvoiceItem.delete does NOT fire here — invoiceTotal stays stale -->
<entity-delete-by-condition entity-name="mantle.account.invoice.InvoiceItem">
    <econdition field-name="invoiceId"/>
    <econdition field-name="itemTypeEnumId" value="ItemDteRoundingAdjust"/>
</entity-delete-by-condition>

<!-- ✅ Manually trigger the recalculation that the EECA would have done -->
<service-call name="mantle.account.InvoiceServices.update#InvoiceTotals"
    in-map="[invoiceId:invoiceId]"/>
```

**Rule**: After using `entity-delete-by-condition` on entities that have on-delete EECAs with side effects, manually invoke the service that the EECA would have called.

## Import Service Call Ordering with EECA Dependencies

**Services that depend on parent-child relationships must be called AFTER the relationship is established, not before.**

When an import service creates entities in multiple steps (e.g., create child, create parent, link them), any service that needs the relationship context to make correct decisions must be deferred until after the relationship exists.

```xml
<!-- ❌ WRONG: validateOrCreate called before parent relationship exists -->
<service-call name="create#Organization" out-map="childMap"/>  <!-- child created -->
<service-call name="validateOrCreate#SharedIdentification"
              in-map="[childId:childMap.partyId, idValue:sharedValue]"/>  <!-- no parent context! -->
<!-- ... later: find/create parent and establish relationship ... -->

<!-- ✅ CORRECT: Defer identification logic until after relationship exists -->
<service-call name="create#Organization" out-map="childMap"/>  <!-- child created -->
<!-- ... find/create parent ... -->
<service-call name="create#PartyRelationship"
              in-map="[fromPartyId:childMap.partyId, toPartyId:parentId, ...]"/>
<service-call name="validateOrCreate#SharedIdentification"
              in-map="[childId:childMap.partyId, idValue:sharedValue]"/>  <!-- has parent context -->
```

Without the relationship, the validation service cannot determine whether an identifier belongs on the child or the parent, leading to duplicate records that later trigger EECA conflicts.

## EECA run-before Validation and Screen Transaction Noise

When an EECA with `run-before="true"` performs validation and returns an error, the framework rolls back the in-progress transaction. If the screen transition then attempts the entity update, the user sees confusing technical errors alongside the actual validation message:

1. "Cannot enlist: no transaction manager or transaction" (framework noise)
2. "Error updating EntityName [pkValue]" (framework noise)
3. "Actual validation message" (the real error)

**Solution**: Add pre-validation in the screen transition *before* the entity update. Keep the EECA as a safety net for non-screen update paths (API calls, other services).

```xml
<!-- Screen transition with pre-validation -->
<transition name="updateStatus">
    <actions>
        <if condition="statusId == 'TargetStatus'">
            <service-call name="MyServices.check#Completable" in-map="[entityId:entityId]"/>
            <if condition="ec.message.hasError()"><return/></if>
        </if>
        <service-call name="update#my.Entity" in-map="[entityId:entityId, statusId:statusId]"/>
    </actions>
    <default-response url="."/>
</transition>
```

Key elements:

1. **Conditional check** — Only validate for the specific status transition that needs it
2. **`ec.message.hasError()` guard** — If validation adds errors, return before the entity update is attempted
3. **EECA remains** — The EECA stays as a safety net for updates originating outside screens

## Anti-Patterns

```xml
<!-- AVOID: Heavy processing in EECA -->
<eeca id="BadPattern" entity="example.Order" on-update="true">
    <actions>
        <!-- This should be a SECA on a specific service instead -->
        <service-call name="heavy#ProcessingService"/>
        <service-call name="another#HeavyService"/>
    </actions>
</eeca>

<!-- AVOID: No condition on frequent entity -->
<eeca id="AlwaysFires" entity="moqui.server.ArtifactHit" on-create="true">
    <!-- Will fire on every artifact hit! -->
</eeca>
```