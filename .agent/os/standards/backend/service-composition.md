# Service Composition

Patterns for calling services within services.

## When to Extract Services
- **Reusability**: Logic used in 2+ places
- **Transaction control**: Need different transaction behavior
- **Readability**: Break complex services into smaller units

## Output Handling Patterns

**Use `out-map="context"` when:**
- Task is simple, argument passing is straightforward
- Outputs should flow naturally to next service

```xml
<service-call name="get#Entity" in-map="context" out-map="context"/>
```

**Use named `out-map` when:**
- Preventing variable overwrites
- Need control over which params pass to subsequent calls
- Avoiding hard-to-debug parameter leakage

```xml
<service-call name="get#RelatedData" out-map="relatedResult"
              out-map-add-to-existing="false"/>
<set field="targetField" from="relatedResult.value"/>
```

## `out-map-add-to-existing`
- Only applies when using named map (not context)
- `true` (default): Merge results into existing map
- `false`: Replace map entirely with service results

## Service Component Placement

**A service MUST live in the same component as the screen that uses it, unless the service is project-specific.**

| Criterion | Place in `{shared-component}` (shared) | Place in project component |
|-----------|------------------------------|----------------------------|
| Logic is generic (framework entities, Uom, Enumeration, etc.) | Yes | No |
| Screen that calls it is in `{shared-component}` | Yes | No |
| Logic references project-specific entities or business rules | No | Yes |
| Another project reusing `{shared-component}` would need this service | Yes | No |
| Service only makes sense in this project's context | No | Yes |

**Naming convention by component:**
- `{shared-component}`: `{shared}.erp.{Domain}Services.{verb}#{Noun}` (file at `service/{shared}/erp/{Domain}Services.xml`)
- Project component: `{component-package}.{Prefix}{Domain}Services.{verb}#{Noun}` (file at `service/{Prefix}{Domain}Services.xml`)

**Gotcha**: If a screen in `{shared-component}` calls a service in a project component, reusing `{shared-component}` in another project will break because the service won't exist. Always check: *does the screen's component match the service's component?*

## Cross-Component Service Reuse

**Services from other components can be called directly by their full path. No wrappers needed.**

Moqui resolves service names using the pattern: `{package}.{ServiceFile}.{verb}#{Noun}`, where the package maps to the component's `service/` directory structure.

### Calling Services from Another Component

```xml
<!-- From a screen transition (e.g., in {shared-component} screen) calling a {localization-component} service -->
<transition name="addExchangeRate">
    <service-call name="mycompany.CurrencyServices.set#ExchangerateValue"/>
    <default-response url="."/>
</transition>

<!-- From a service in another component calling a {localization-component} service -->
<service-call name="mycompany.CurrencyServices.get#ExchangeRate"
              in-map="[fromCurrencyUomId:fromCurrencyUomId, toCurrencyUomId:'USD', date:rateDate]"
              out-map="rateResult"/>
```

### When to Wrap vs. Call Directly

| Scenario | Approach |
|----------|----------|
| Simple pass-through from screen to service | Call directly from transition |
| Need to add validation or pre-processing | Create wrapper service |
| Need to combine multiple cross-component calls | Create orchestration service |
| Need different error messages | Create wrapper service |

### Component Dependencies

When reusing services across components, the providing component must be listed as a dependency. Verify the component is loaded by checking `component.xml` or the application's `component/` directory structure.

**Common cross-component service sources:**
- `mycompany.*` - {localization-component} (currency, DTE, SII)
- `{shared-utils}.*` - Shared Utilities (party, org structure, config)
- `mantle.*` - Mantle Business Artifacts (orders, products, shipments)