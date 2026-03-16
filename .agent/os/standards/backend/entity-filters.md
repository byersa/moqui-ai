# EntityFilter Standards

### Fail-Safe Pattern (CRITICAL)

**Always use Elvis operator in EntityFilter definitions:**

```xml
<!-- CORRECT: Fail-safe with empty list fallback -->
<moqui.entity.EntityFilterSet entityFilterSetId="ORDER_ORG_FILTER"
                               applyCachedFinds="true">
    <filters entityFilterId="ORDER_ORG" entityName="example.Order"
             filterMap="[ownerPartyId:filterOrgIds ? filterOrgIds : ['-NO-MATCH-']]"/>
</moqui.entity.EntityFilterSet>

<!-- WRONG: No fallback - returns ALL data if filterOrgIds is null -->
<filters filterMap="[ownerPartyId:filterOrgIds]"/>
```

**Why fail-safe matters:**
- If `filterOrgIds` is null/empty, filter becomes no-op
- User sees ALL data instead of NO data
- Security breach in multi-tenant systems

### Filter Context Naming

**Standard context variable names:**
- `filterOrgIds` - Organization/tenant IDs (List)
- `filterPartyId` - Single party filter
- `filterStoreIds` - Store/location IDs

### Filter Setup Service Pattern

```xml
<service verb="setup" noun="FilterContext" authenticate="anonymous-all">
    <actions>
        <!-- Get user's organizations -->
        <entity-find entity-name="mantle.party.PartyRelationship" list="partyRelList">
            <econdition field-name="toPartyId" from="ec.user.partyId"/>
            <econdition field-name="relationshipTypeEnumId" value="PrtEmployee"/>
            <date-filter/>
        </entity-find>

        <!-- Set filter context -->
        <set field="filterOrgIds" from="partyRelList*.fromPartyId"/>
        <set field="ec.user.context.filterOrgIds" from="filterOrgIds"/>
    </actions>
</service>
```

### EntityFilterSet Definition

```xml
<!-- In data file -->
<moqui.entity.EntityFilterSet entityFilterSetId="EXAMPLE_ORG_FILTER"
                               description="Filter by organization"
                               applyCachedFinds="true">
    <filters entityFilterId="EXAMPLE_ORG"
             entityName="example.Order"
             filterMap="[ownerPartyId:filterOrgIds ?: ['-NO-MATCH-']]"/>
</moqui.entity.EntityFilterSet>

<!-- UserGroup assignment -->
<moqui.security.UserGroupEntityFilterSet userGroupId="ORDER_USERS"
                                          entityFilterSetId="EXAMPLE_ORG_FILTER"/>
```

### View Entity Filter Propagation

**Alias names MUST match for filters to propagate:**

```xml
<view-entity entity-name="OrderAndItem" package="example">
    <member-entity entity-alias="ORD" entity-name="example.Order"/>
    <member-entity entity-alias="ITM" entity-name="example.OrderItem" join-from-alias="ORD">
        <key-map field-name="orderId"/>
    </member-entity>

    <!-- Alias MUST be named "ownerPartyId" for filter to work -->
    <alias entity-alias="ORD" name="ownerPartyId"/>
</view-entity>
```

### EntityFilter Trimming by Used Aliases

**EntityFilters on member entities are only applied if the member entity's alias is actually used in the query.**

When a view-entity query is built, the framework determines which member entities are actually needed based on selected fields, conditions, and order-by clauses (`entityAliasUsedSet` in `EntityFindBuilder`). Member entities not in this set are **not joined** and their EntityFilters are **skipped**.

This means adding a member entity to a view-entity "for EntityFilter purposes" does **not** work if no fields from that member are selected or used in conditions.

```xml
<!-- The Party EntityFilter (ownerPartyId) will NOT apply here -->
<view-entity entity-name="OrderSummary" package="example">
    <member-entity entity-alias="ORD" entity-name="example.Order"/>
    <!-- PTY joined but no field from it is ever selected or filtered on -->
    <member-entity entity-alias="PTY" entity-name="mantle.party.Party" join-from-alias="ORD">
        <key-map field-name="customerPartyId" related="partyId"/>
    </member-entity>
    <alias-all entity-alias="ORD"/>
    <alias-all entity-alias="PTY" prefix="customer"/>
    <!-- If the query only selects ORD fields (orderId, orderDate, etc.),
         PTY is trimmed from the join and its EntityFilter is skipped -->
</view-entity>
```

**How it works** (`ArtifactExecutionFacadeImpl.groovy:636-662`):
1. Framework builds `entityAliasUsedSet` from fields in SELECT, WHERE, and ORDER BY
2. For each EntityFilter targeting a member entity, it checks if that member's alias is in the used set
3. If the alias is NOT in the set, the filter is silently skipped

**Implications:**
- `alias-all` with `prefix` can create aliases that are never used — the member entity gets trimmed
- Name conflicts from `alias-all` are resolved by keeping the first alias and dropping duplicates (no warning in most cases)
- To ensure an EntityFilter applies, either: (a) select a field from that member, or (b) define the filter on the view-entity itself instead of relying on member-entity propagation

**See also**: `entities.md` § "Prefer Explicit Aliases Over alias-all"

### REST API Filter Context

**MANDATORY: Call filter setup before filtered entity queries:**

```xml
<resource name="orders" require-authentication="true">
    <method type="get">
        <service call="example.OrderServices.find#Orders"/>
    </method>
</resource>

<!-- In service -->
<service verb="find" noun="Orders">
    <actions>
        <!-- CRITICAL: Setup filter context first -->
        <service-call name="setup#FilterContext" in-map="context" out-map="context" disable-authz="true"/>

        <!-- Now query filtered entities -->
        <entity-find entity-name="example.Order" list="orderList">
            <econdition field-name="statusId" value="OrdActive"/>
        </entity-find>
    </actions>
</service>
```

### Screen Filter Context

**Root screen setup in always-actions:**

```xml
<screen>
    <always-actions>
        <service-call name="setup#FilterContext" in-map="context" out-map="context"/>
    </always-actions>

    <!-- Subscreens inherit filter context -->
</screen>
```

### Debugging Filter Issues

**When data is missing, check:**

1. **Filter context is set:**
   ```groovy
   ec.logger.info("filterOrgIds: ${ec.user.context.filterOrgIds}")
   ```

2. **User group has EntityFilterSet assigned:**
   ```sql
   SELECT * FROM moqui.security.UserGroupEntityFilterSet
   WHERE userGroupId = 'USER_GROUP';
   ```

3. **View entity aliases match:**
   ```xml
   <!-- Check that filtered field alias exists and matches -->
   ```

4. **Filter setup was called:**
   - REST: Check service calls filter setup
   - Screen: Check always-actions in root screen

### Common Mistakes

```xml
<!-- WRONG: Missing fail-safe -->
<filters filterMap="[ownerPartyId:filterOrgIds]"/>

<!-- WRONG: Alias name doesn't match filtered field -->
<alias entity-alias="ORD" name="organizationId"/>  <!-- Should be ownerPartyId -->

<!-- WRONG: Filter setup not called in REST service -->
<entity-find entity-name="example.Order" list="orderList"/>
<!-- Data returned without filter! -->

<!-- WRONG: ?[ parsed as Groovy 3+ safe-index operator, not ternary -->
<filters filterMap="[ownerPartyId:activeOrgId?[activeOrgId,'_NA_']:null]"/>

<!-- CORRECT: spaces around ternary operator -->
<filters filterMap="[ownerPartyId: activeOrgId ? [activeOrgId,'_NA_'] : null]"/>
```

> **Groovy 3+ Gotcha**: Always use spaces around the ternary operator (`?` and `:`) in `filterMap` expressions when the true-branch is a list literal `[...]`. Without spaces, Groovy 3+ tokenizes `?[` as the safe-indexing operator, causing a `MultipleCompilationErrorsException`. See `framework-guide.md` § "Groovy 3+ `?[` Safe Indexing Operator Ambiguity" for full details.
