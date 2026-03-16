# Security Standards

### Authentication Levels

| Attribute | Behavior |
|-----------|----------|
| `authenticate="true"` | User must be logged in (default) |
| `authenticate="anonymous-all"` | No login required |
| `authenticate="anonymous-view"` | No login for read-only |

```xml
<!-- Default: requires login -->
<service verb="create" noun="Order">

<!-- Public endpoint -->
<service verb="get" noun="PublicInfo" authenticate="anonymous-all">

<!-- Public read-only -->
<service verb="find" noun="PublicData" authenticate="anonymous-view">
```

### Role-Based Access

```xml
<!-- Requires specific role -->
<service verb="admin" noun="Users" authenticate="true" require-all-roles="ADMIN">

<!-- Requires ALL specified roles -->
<service verb="approve" noun="Order" authenticate="true" require-all-roles="MANAGER,APPROVER">

<!-- Requires ANY of specified roles -->
<service verb="review" noun="Order" authenticate="true" require-any-roles="MANAGER,REVIEWER">
```

### Permission Checking

```xml
<service verb="delete" noun="Order">
    <actions>
        <!-- Check permission -->
        <set field="hasPermission" from="ec.user.hasPermission('ORDER_DELETE')"/>
        <if condition="!hasPermission">
            <return error="true" message="Permission denied"/>
        </if>
    </actions>
</service>
```

### User Context Access

```groovy
// Basic user info
ec.user.userId
ec.user.username
ec.user.userAccount?.partyId

// Role check
ec.user.isUserInRole('ADMIN')

// Permission check
ec.user.hasPermission('ORDER_CREATE')

// User preferences
ec.user.getPreference('theme')

// Current timestamp
ec.user.nowTimestamp
```

### Row-Level Security with EntityFilters

```xml
<!-- EntityFilter definition -->
<moqui.entity.EntityFilterSet entityFilterSetId="ORDER_ORG_FILTER">
    <filters entityFilterId="ORDER_ORG"
             entityName="example.Order"
             filterMap="[ownerPartyId:filterOrgIds ?: ['-NO-MATCH-']]"/>
</moqui.entity.EntityFilterSet>

<!-- Assign to user group -->
<moqui.security.UserGroupEntityFilterSet
    userGroupId="ORDER_USERS"
    entityFilterSetId="ORDER_ORG_FILTER"/>
```

### Security Data Patterns

```xml
<!-- User Group -->
<moqui.security.UserGroup userGroupId="ORDER_MANAGERS"
                          description="Order Management Users"/>

<!-- Permissions -->
<moqui.security.UserPermission userPermissionId="ORDER_VIEW"/>
<moqui.security.UserPermission userPermissionId="ORDER_CREATE"/>
<moqui.security.UserPermission userPermissionId="ORDER_DELETE"/>

<!-- Group-Permission Assignment -->
<moqui.security.UserGroupPermission userGroupId="ORDER_MANAGERS"
                                     userPermissionId="ORDER_VIEW"/>
<moqui.security.UserGroupPermission userGroupId="ORDER_MANAGERS"
                                     userPermissionId="ORDER_CREATE"/>
```

### Input Validation

```xml
<in-parameters>
    <parameter name="email" type="String" required="true">
        <text-email/>
    </parameter>
    <parameter name="phone" type="String">
        <matches regexp="^\+?[\d\s\-\(\)]+$" message="Invalid phone"/>
    </parameter>
    <parameter name="notes" allow-html="none"/>
</in-parameters>
```

### SQL Injection Prevention

**SAFE: Use parameterized queries:**
```xml
<entity-find entity-name="Order" list="orderList">
    <econdition field-name="description" operator="like" value="${searchText}%"/>
</entity-find>
```

**AVOID: String concatenation in SQL:**
```groovy
// WRONG: SQL injection vulnerability
def sql = "SELECT * FROM Order WHERE description LIKE '${searchText}%'"
```

### Audit Logging

```xml
<!-- Enable audit logging on sensitive fields -->
<field name="statusId" type="id" enable-audit-log="true"/>
<field name="amount" type="currency-amount" enable-audit-log="true"/>
```

### Sensitive Data Handling

```xml
<!-- Encrypt sensitive fields -->
<field name="creditCardNumber" type="text-medium" encrypt="true"/>
<field name="ssn" type="text-medium" encrypt="true"/>
```

### API Authentication

**UserLoginKey (recommended):**
```xml
<resource name="orders" require-authentication="true">
    <!-- Uses X-API-Key header or basic auth -->
</resource>
```

### Screen Access Control (SubscreensItem + ArtifactAuthz)

Screen menu visibility and access is controlled by **ArtifactAuthz**, not by SubscreensItem `userGroupId`.

**The `userGroupId` field on `moqui.screen.SubscreensItem` is DEPRECATED.** The entity definition states:
> "DEPRECATED. While still supported, to control access to subscreens use ArtifactAuthz and related records instead."

**Correct pattern:** One SubscreensItem per screen with `userGroupId="ALL_USERS"` + ArtifactAuthz records to control who can see/access each screen.

```xml
<!-- Step 1: Mount screens with ALL_USERS (PK field — must be explicit) -->
<moqui.screen.SubscreensItem screenLocation="component://webroot/screen/webroot/apps.xml"
    subscreenName="MyApp" userGroupId="ALL_USERS"
    subscreenLocation="component://my-component/screen/MyApp.xml"
    menuTitle="My App" menuIndex="50" menuInclude="Y"/>

<!-- Step 2: Define ArtifactGroup for the screen -->
<artifactGroups artifactGroupId="MY_APP_ACCESS" description="My App Access">
    <moqui.security.ArtifactGroupMember
        artifactName="component://my-component/screen/MyApp.xml"
        artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>

    <!-- Step 3: Grant access per UserGroup via ArtifactAuthz -->
    <moqui.security.ArtifactAuthz artifactAuthzId="MY_APP_ADMIN"
        userGroupId="ADMIN" authzTypeEnumId="AUTHZT_ALWAYS"
        authzActionEnumId="AUTHZA_ALL">
        <filters entityFilterSetId="MANTLE_ACTIVE_ORG"/>
    </moqui.security.ArtifactAuthz>
    <moqui.security.ArtifactAuthz artifactAuthzId="MY_APP_USERS"
        userGroupId="MY_APP_USERS" authzTypeEnumId="AUTHZT_ALWAYS"
        authzActionEnumId="AUTHZA_ALL">
        <filters entityFilterSetId="MANTLE_ACTIVE_ORG"/>
    </moqui.security.ArtifactAuthz>
</artifactGroups>
```

**Rules**:
- **Always set `userGroupId="ALL_USERS"` on SubscreensItem** — it is a PK field and must be explicit in seed data (see `xml-defaults.md` § "Exception: PK Fields in Seed Data Records")
- **One SubscreensItem per screen** — always `ALL_USERS`; never create multiple entries with different userGroupId values
- **ArtifactAuthz controls both access AND menu visibility** — if a user lacks ArtifactAuthz for a screen, it won't appear in their app list
- **Use `inheritAuthz="Y"`** on ArtifactGroupMember so child screens inherit the parent's authorization
- **Separate seed data from demo data** — put ArtifactGroup/ArtifactAuthz for core groups (ADMIN, etc.) in seed data; put demo-specific group authz in demo data files

**Anti-pattern (DO NOT USE):**
```xml
<!-- WRONG: Multiple SubscreensItem entries per userGroup -->
<moqui.screen.SubscreensItem screenLocation="..." subscreenName="MyApp"
    subscreenLocation="..." userGroupId="GROUP_A" .../>
<moqui.screen.SubscreensItem screenLocation="..." subscreenName="MyApp"
    subscreenLocation="..." userGroupId="GROUP_B" .../>
```

### UserGroup-Based Capability Checks

Use `UserGroupMember` with `date-filter` for tier-based capability authorization
(e.g., L1/L2/L3 support levels, approval tiers). Higher tiers qualify for lower tier work.

```xml
<!-- Check active membership in a capability group -->
<entity-find-count entity-name="moqui.security.UserGroupMember" count-field="memberCount">
    <econdition field-name="userId"/>
    <econdition field-name="userGroupId" value="CAPABILITY_GROUP_ID"/>
    <date-filter/>
</entity-find-count>

<!-- Short-circuit pattern: check highest tier first -->
<entity-find-count entity-name="moqui.security.UserGroupMember" count-field="l3Count">
    <econdition field-name="userId"/>
    <econdition field-name="userGroupId" value="TIER_L3"/>
    <date-filter/>
</entity-find-count>
<if condition="l3Count > 0">
    <set field="userMaxLevel" from="3"/>
    <set field="qualified" from="userMaxLevel >= requiredLevel"/>
    <return/>
</if>
<!-- Continue checking L2, then L1... -->
```

**Rules**:
- **Always use `date-filter`** to check active membership (respects `fromDate`/`thruDate`)
- **Check highest tier first** and short-circuit for performance
- **Higher qualifies for lower**: A tier-3 member qualifies for tier-1 and tier-2 work
- **Define capability groups in seed data** (e.g., `SUPPORT_L1_TEAM`, `SUPPORT_L2_TEAM`, `SUPPORT_L3_TEAM`)
- **Separate from permissions**: This pattern is for _capability_ (what work a user can do), not _authorization_ (what actions they can perform)

### Security Checklist

- [ ] All sensitive services require authentication
- [ ] Permissions defined for all operations
- [ ] EntityFilters configured for multi-tenant data
- [ ] Input validation on all parameters
- [ ] Audit logging on sensitive fields
- [ ] Sensitive data encrypted
- [ ] SQL injection prevented
- [ ] HTTPS enforced in production
