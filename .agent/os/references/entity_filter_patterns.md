# Moqui EntityFilter Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/entity-filters.md` - EntityFilter fail-safe patterns and context naming

---

## Complete Authorization Setup Guide

This guide provides step-by-step instructions for implementing row-level security using EntityFilters.

### Step 1: Define EntityFilterSet (Data File)

```xml
<!-- In data/ComponentSecurityData.xml -->
<entity-facade-xml type="seed">
    <!-- Define the filter set -->
    <moqui.security.EntityFilterSet entityFilterSetId="COMPONENT_USER_ORG"
                                    description="Filter by user's organization"
                                    applyCodition="true"
                                    allowMissingAlias="false"/>

    <!-- Define individual entity filters with FAIL-SAFE pattern -->
    <moqui.security.EntityFilter entityFilterSetId="COMPONENT_USER_ORG"
                                 entityName="example.Order"
                                 filterMap="[ownerPartyId:(filterOrgIds ?: [])]"/>

    <moqui.security.EntityFilter entityFilterSetId="COMPONENT_USER_ORG"
                                 entityName="example.Customer"
                                 filterMap="[customerPartyId:(filterOrgIds ?: [])]"/>
</entity-facade-xml>
```

### Step 2: Assign Filter to User Group

```xml
<!-- Assign filter set to user group -->
<moqui.security.UserGroupEntityFilter userGroupId="COMPONENT_USER"
                                       entityFilterSetId="COMPONENT_USER_ORG"/>
```

### Step 3: Create Filter Context Setup Service

```xml
<service verb="setup" noun="FilterContext">
    <description>
        Sets up the filter context with user's organization IDs.
        MUST be called in root screen's always-actions or REST API pre-filter.
    </description>
    <actions>
        <!-- Get user's organizations -->
        <entity-find entity-name="mantle.party.PartyRelationship" list="orgRelList">
            <econdition field-name="fromPartyId" from="ec.user.userAccount.partyId"/>
            <econdition field-name="relationshipTypeEnumId" value="PrtMember"/>
            <date-filter/>
        </entity-find>

        <!-- Set filter context variable -->
        <set field="filterOrgIds" from="orgRelList*.toPartyId"/>
        <if condition="!filterOrgIds">
            <set field="filterOrgIds" from="[ec.user.userAccount.partyId]"/>
        </if>

        <!-- Store in user context for EntityFilter access -->
        <script>ec.user.context.put('filterOrgIds', filterOrgIds)</script>

        <!-- Log for debugging -->
        <log message="Filter context setup: filterOrgIds=${filterOrgIds}" level="debug"/>
    </actions>
</service>
```

### Step 4: Call Setup Service in Root Screen

```xml
<!-- In root screen's always-actions -->
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <always-actions>
        <if condition="ec.user.userId">
            <service-call name="component.Services.setup#FilterContext"/>
        </if>
    </always-actions>
    <!-- ... rest of screen -->
</screen>
```

### Step 5: Call Setup Service in REST API

```xml
<!-- In REST API service -->
<service verb="get" noun="DteList">
    <actions>
        <!-- MANDATORY: Setup filter context for REST APIs -->
        <service-call name="setup#FilterContext" disable-authz="true"/>

        <!-- Now entity-find will be properly filtered -->
        <entity-find entity-name="FiscalTaxDocument" list="dteList"/>
    </actions>
</service>
```

---

## Debugging EntityFilter Issues

### Common Symptoms

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| All data showing | Context not set | Add setup service call |
| No data showing | Context empty or wrong | Check filterOrgIds value |
| Wrong data showing | Filter field mismatch | Verify filterMap field names |
| Works in screen, fails in REST | Missing REST context setup | Add setup call to service |

### Debugging Steps

**Step 1: Verify filter context is populated**
```xml
<log message="DEBUG: filterOrgIds = ${ec.user.context.filterOrgIds}"/>
<log message="DEBUG: userId = ${ec.user.userId}"/>
```

**Step 2: Add screen-level debug section**
```xml
<section name="DebugFilterContext" condition="ec.user.hasPermission('DEBUG_FILTERS')">
    <widgets>
        <container-box>
            <box-header title="Filter Debug"/>
            <box-body>
                <label text="filterOrgIds: ${ec.user.context.filterOrgIds}"/>
                <label text="userId: ${ec.user.userId}"/>
            </box-body>
        </container-box>
    </widgets>
</section>
```

**Step 3: Test with/without filter**
```xml
<!-- Temporarily disable authz to compare results -->
<entity-find entity-name="Order" list="allOrders" disable-authz="true"/>
<entity-find entity-name="Order" list="filteredOrders"/>
<log message="All: ${allOrders.size()}, Filtered: ${filteredOrders.size()}"/>
```

---

## Filter Context in Different Contexts

### Screen Context
```xml
<!-- In screen always-actions -->
<if condition="ec.user.userId">
    <service-call name="setup#FilterContext"/>
</if>
```

### REST API Context
```xml
<!-- In REST service - MANDATORY -->
<service-call name="setup#FilterContext" disable-authz="true"/>
```

### Spock Test Context
```groovy
def setupSpec() {
    ec = Moqui.getExecutionContext()
    ec.user.loginUser("testuser", "password")
    // CRITICAL: Set up filter context for tests
    ec.service.sync().name("setup#FilterContext").call()
}
```

### ServiceJob Context
```xml
<service verb="process" noun="BatchJob" authenticate="anonymous-all">
    <actions>
        <!-- ServiceJobs run as anonymous - may need special handling -->
        <if condition="ec.user.userId">
            <service-call name="setup#FilterContext"/>
        </if>
    </actions>
</service>
```

---

## View Entity Filter Matching

### How Filters Apply to View Entities

EntityFilters match on the **alias field name** in view-entities, not the source entity field.

```xml
<view-entity entity-name="FiscalTaxDocumentAndParties">
    <member-entity entity-alias="FTD" entity-name="FiscalTaxDocument"/>

    <!-- These aliases will be matched by filters -->
    <alias entity-alias="FTD" name="issuerPartyId"/>
    <alias entity-alias="FTD" name="receiverPartyId"/>
</view-entity>
```

### Common Pitfalls

**Pitfall 1: Alias name mismatch**
```xml
<!-- Filter uses 'partyId' but alias is 'issuerPartyId' - WON'T MATCH -->
<filters filterMap="[partyId:filterOrgIds]"/>  <!-- WRONG -->
<filters filterMap="[issuerPartyId:filterOrgIds]"/>  <!-- CORRECT -->
```

**Pitfall 2: Missing alias in view-entity**
```xml
<!-- If issuerPartyId is not aliased, filter can't find it -->
<view-entity entity-name="MyView">
    <member-entity entity-alias="FTD" entity-name="FiscalTaxDocument"/>
    <!-- No alias for issuerPartyId = filter won't apply! -->
</view-entity>
```

---

## Authorization Testing Patterns

### Testing with Authorization ENABLED

```groovy
def "filtered query returns only user's organization data"() {
    given: "authenticated user with organization access"
    ec.user.loginUser('testuser', 'password')
    ec.service.sync().name("setup#FilterContext").call()

    when: "querying filtered entity"
    def orders = ec.entity.find("Order").list()

    then: "only user's organization orders returned"
    orders.size() > 0
    orders.every { it.ownerPartyId in ec.user.context.filterOrgIds }
}

def "user without filter access gets empty results"() {
    given: "user with no organization access"
    ec.user.loginUser('restricted_user', 'password')
    ec.service.sync().name("setup#FilterContext").call()

    when: "querying filtered entity"
    def results = ec.entity.find("Order").list()

    then: "no results returned (fail-safe behavior)"
    results.size() == 0
}
```

---

## Quality Checklist

- [ ] EntityFilterSet defined with clear description
- [ ] All filters use fail-safe pattern `(variable ?: [])`
- [ ] Context setup called in REST services
- [ ] Filtered fields have database indexes
- [ ] Tested with different user roles
- [ ] Screen impact analyzed (root screen context setup)
- [ ] View-entity aliases verified for filter matching