# Moqui Framework Caching Best Practices

**Standards Reference**: For declarative conventions, see:
- `standards/backend/caching.md` - Framework caching rules and guidelines

---

## Core Principle: Prefer Framework Caching

**Best Practice**: Always rely on Moqui Framework's built-in entity caching and service caching mechanisms rather than implementing custom caching logic in services.

---

## Implementation Patterns

### Entity Caching Configuration

Configure caching at the entity level:
```xml
<entity entity-name="Caf" package="mycompany.myapp" cache="true">
    <!-- Entity fields -->
</entity>
```

### Service-Level Caching

For expensive service operations:
```xml
<service verb="calculate" noun="ComplexData" cache="true" cache-timeout="300">
    <!-- Service definition -->
</service>
```

---

## Anti-Pattern: Custom Caching Logic

### ❌ Avoid Custom Caching Implementation
```groovy
// DON'T DO THIS - Custom caching implementation
def cacheKey = "folio-availability-${partyId}-${documentType}"
def cachedResult = ec.cache.get(cacheKey)
if (!cachedResult) {
    cachedResult = calculateAvailability()
    ec.cache.put(cacheKey, cachedResult, 300)
}
return cachedResult
```

### ✅ Prefer Framework Caching
```groovy
// DO THIS - Let Moqui handle caching
def result = calculateAvailability()
return result

// Moqui's entity caching automatically optimizes the queries inside calculateAvailability()
```

---

## Cache Configuration

### MoquiConf.xml Settings
```xml
<cache-list>
    <cache name="entity.definition" expire-time="3600"/>
    <cache name="entity.data" expire-time="600"/>
    <cache name="service.result" expire-time="300"/>
</cache-list>
```

### Environment-Specific Tuning
- **Development**: Shorter cache timeouts for faster testing cycles
- **Production**: Longer cache timeouts for better performance
- **Testing**: May disable caching for consistent test results

---

## Performance Optimization Strategies

### Entity Query Optimization
1. **Use Proper Indexes**: Ensure database indexes support frequent queries
2. **Limit Result Sets**: Use appropriate conditions to limit query results
3. **Avoid N+1 Queries**: Use proper entity relationships and joins

### Service Design for Caching
1. **Stateless Services**: Design services to be stateless for better caching
2. **Idempotent Operations**: Ensure repeated calls return consistent results
3. **Minimal Side Effects**: Reduce operations that invalidate caches

---

## Cached Entity Values Are Immutable

**Standards Reference**: `standards/backend/caching.md` — "Cached Values Are Immutable"

Entity values returned from cache have `mutable = false`. Directly setting fields on them throws `EntityException`. This only affects direct entity value manipulation (`set` + `entity-update`), not auto-services like `update#EntityName`.

```xml
<!-- WRONG: cached value is immutable -->
<entity-find-one entity-name="moqui.service.job.ServiceJob" value-field="job">
    <field-map field-name="jobName" from="jobName"/>
</entity-find-one>
<set field="job.paused" value="N"/>  <!-- throws! -->

<!-- FIX: bypass cache -->
<entity-find-one entity-name="..." value-field="job" cache="false">
```

## Debugging Cache Issues

When debugging caching problems:
1. Check entity cache configuration
2. Verify transaction boundaries
3. Monitor cache invalidation events
4. Use logging to track cache behavior

---

## Quality Checklist

### Do
- ✅ Use Moqui's built-in entity caching
- ✅ Configure caching at entity and service level
- ✅ Design stateless, idempotent services
- ✅ Use proper database indexes
- ✅ Monitor cache performance

### Don't
- ❌ Implement custom caching logic in services
- ❌ Use manual cache management (ec.cache.put/get)
- ❌ Ignore cache invalidation requirements
- ❌ Cache data that changes frequently
- ❌ Assume cache will always be available