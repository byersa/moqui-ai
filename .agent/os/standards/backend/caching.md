# Caching Standards

### Entity Caching

**When to enable entity caching (`cache="true"`):**
- Configuration/reference data (enums, status items, types)
- Small lookup tables (<1000 records)
- Rarely changing data
- Frequently read data

**When NOT to cache:**
- Transactional data (orders, invoices)
- Large tables
- Frequently updated data
- User-specific data

```xml
<!-- CORRECT: Cache configuration entity -->
<entity entity-name="ProductType" cache="true">

<!-- CORRECT: Don't cache transactional entity -->
<entity entity-name="Order">
```

### Entity Usage Attribute

| Value | Caching | Purpose |
|-------|---------|---------|
| `configuration` | Enabled | System config, rarely changes |
| `transactional` | Disabled | Business data, frequent changes |
| `nontransactional` | Varies | Logging, analytics |

```xml
<entity entity-name="SystemProperty" package="moqui.basic"
        use="configuration" cache="true">

<entity entity-name="Order" package="example"
        use="transactional">
```

### Query Caching

**Cache simple lookups:**
```xml
<entity-find entity-name="moqui.basic.Enumeration" list="enumList" cache="true">
    <econdition field-name="enumTypeId" value="ProductType"/>
</entity-find>
```

**Don't cache:**
- User-specific queries
- Queries with filter context
- Complex queries with dynamic conditions

### Cache Invalidation

**Framework handles automatically for:**
- Entity create/update/delete
- Service calls that modify data

**Manual invalidation when needed:**
```groovy
ec.cache.getCache("entity.list").remove(cacheKey)
ec.cache.getCache("entity.one").clear()
```

### Cache Configuration

**In `MoquiConf.xml`:**
```xml
<cache-list>
    <cache name="entity.record.one" expire-time-idle="600"/>
    <cache name="entity.record.list" expire-time-idle="600" max-elements="5000"/>
    <cache name="service.sync" expire-time-idle="300"/>
</cache-list>
```

### Service Caching

**Cache read-only services:**
```xml
<service verb="get" noun="ProductTypes" cache="true">
```

**Never cache:**
- Services that modify data
- Services with side effects
- User-specific results

### Cache-Aware Development

**Test with cache enabled:**
- Run tests with production-like cache settings
- Verify cache invalidation works correctly
- Check for stale data issues

**Common cache issues:**
1. Stale data after update - check invalidation
2. Memory issues - review `max-elements`
3. Performance degradation - check cache hit ratio

### Cached Values Are Immutable

**CRITICAL**: When `entity-find-one` or `entity-find` returns values from the entity cache, those values have `mutable = false`. Attempting to **directly set a field on the entity value object** and then call `entity-update` throws:

```
EntityException: Cannot set field [fieldName], this entity value is not mutable (it is read-only)
```

**What "modify" means here**: This applies to **direct entity value manipulation** — using `<set field="entityValue.fieldName" .../>` followed by `<entity-update>`. It does **not** affect auto-services like `update#EntityName`, which internally create a fresh mutable entity value from the input parameters.

**Framework mechanism** (`EntityValueBase.java`):
1. `EntityCache.putInOneCache()` calls `entityValue.setFromCache()` → sets `mutable = false`
2. `EntityValueBase.putKnownField()` checks `if (!mutable) throw EntityException`

**When this matters**: Any service that fetches a cached entity (e.g., `ServiceJob`, `Enumeration`, `StatusItem`) via `entity-find-one` and then directly sets fields on the returned value.

```xml
<!-- WRONG: Fetches from cache → immutable → throws on set -->
<entity-find-one entity-name="moqui.service.job.ServiceJob" value-field="job">
    <field-map field-name="jobName" from="jobName"/>
</entity-find-one>
<set field="job.paused" value="N"/>
<entity-update value-field="job"/>

<!-- CORRECT: Bypass cache when you need to directly modify the entity value -->
<entity-find-one entity-name="moqui.service.job.ServiceJob" value-field="job" cache="false">
    <field-map field-name="jobName" from="jobName"/>
</entity-find-one>
<set field="job.paused" value="N"/>
<entity-update value-field="job"/>

<!-- ALSO CORRECT: Auto-service works fine regardless of cache (creates its own value) -->
<service-call name="update#moqui.service.job.ServiceJob"
        in-map="[jobName: jobName, paused: 'N']"/>
```

**Rule**: Use `cache="false"` on `entity-find-one` / `entity-find` when you will directly set fields on the returned entity value and call `entity-update`.

### Anti-Patterns

```xml
<!-- WRONG: Caching user-specific query -->
<entity-find entity-name="Order" list="orderList" cache="true">
    <econdition field-name="userId" from="ec.user.userId"/>
</entity-find>

<!-- WRONG: Caching filtered entity query -->
<entity-find entity-name="Order" list="orderList" cache="true">
    <!-- EntityFilter will not apply to cached result! -->
</entity-find>

<!-- WRONG: Caching transactional entity -->
<entity entity-name="OrderItem" cache="true">
```

### Best Practices Summary

| Scenario | Cache? | Reason |
|----------|--------|--------|
| Enum lookup | Yes | Small, static |
| Status items | Yes | Small, static |
| Product types | Yes | Reference data |
| Order list | No | Transactional |
| User's orders | No | User-specific |
| Filtered data | No | Context-dependent |
| Large table | No | Memory concerns |
