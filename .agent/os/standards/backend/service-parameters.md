# Service Parameters

### Parameter Types

| Type | Java Type | Usage |
|------|-----------|-------|
| `String` | String | Text values (default) |
| `Integer` | Integer | Whole numbers |
| `Long` | Long | Large integers |
| `BigDecimal` | BigDecimal | Currency, precise decimals |
| `Timestamp` | java.sql.Timestamp | Date-time values |
| `List` | List | Collections |
| `Map` | Map | Key-value structures |
| `Object` | Object | Flexible types |

### Reserved Parameter Names

**Never use these names for service parameters:**
- `context` - Reserved for execution context
- `ec` - Reserved for ExecutionContext
- `result` - Reserved for service results
- `parameters` - Reserved for parameter map

### Required vs Optional Parameters

```xml
<!-- Required parameter -->
<parameter name="orderId" type="String" required="true"/>

<!-- Optional with literal default -->
<parameter name="pageSize" type="Integer" default-value="20"/>

<!-- Optional without default -->
<parameter name="notes" type="String"/>
```

### `default` vs `default-value` — Critical Distinction

| Attribute | Behavior | Example |
|-----------|----------|---------|
| `default-value` | **Literal string** — used as-is | `default-value="newest-wins"` → `"newest-wins"` |
| `default` | **Groovy expression** — evaluated at runtime | `default="ec.user.nowTimestamp"` → current timestamp |

**GOTCHA**: Using `default` with a value containing operators causes Groovy evaluation errors:
```xml
<!-- WRONG: Groovy evaluates "newest" minus "wins" → NullPointerException -->
<parameter name="strategy" default="newest-wins"/>

<!-- CORRECT: Treated as literal string -->
<parameter name="strategy" default-value="newest-wins"/>
```

**Rule of thumb**: Use `default-value` for literal strings and numbers. Only use `default` when you need a Groovy expression (e.g., `ec.user.nowTimestamp`, `ec.user.userAccount.partyId`).

### Binary Data Parameters

**CRITICAL**: Binary data (files, attachments) requires special handling:

```xml
<!-- Binary parameter - type is Object, NOT byte[] -->
<parameter name="attachment" type="Object">
    <description>Binary file content</description>
</parameter>

<!-- Also accept filename and content type -->
<parameter name="attachmentFilename" type="String"/>
<parameter name="attachmentContentType" type="String"/>
```

### Parameter Validation

**Built-in Validators** (prefer over manual validation):
```xml
<parameter name="email" type="String" required="true">
    <text-email/>
</parameter>

<parameter name="phone" type="String">
    <matches regexp="^\+?[\d\s\-\(\)]+$" message="Invalid phone number"/>
</parameter>

<parameter name="description" type="String">
    <text-length min="1" max="255"/>
</parameter>

<parameter name="quantity" type="Integer">
    <number-range min="1" max="1000"/>
</parameter>
```

### Auto-Parameters Pattern

**Use for entity-based services:**
```xml
<in-parameters>
    <!-- Auto-map from entity fields -->
    <auto-parameters entity-name="example.Entity" include="pk"/>
    <auto-parameters entity-name="example.Entity" include="nonpk"/>
</in-parameters>
```

### Parameter Documentation

```xml
<parameter name="orderId" type="String" required="true">
    <description>The unique order identifier</description>
</parameter>
```

### Allow-HTML Attribute

**Security control for HTML content:**

| Value | Behavior |
|-------|----------|
| `none` | Strip all HTML (default, safest) |
| `safe` | Allow safe HTML subset |
| `any` | Allow all HTML (dangerous) |

```xml
<!-- Secure: no HTML allowed -->
<parameter name="name" allow-html="none"/>

<!-- Rich text fields only -->
<parameter name="htmlContent" allow-html="safe"/>
```

### Entity vs Service Parameter Differences

| Aspect | Entity Field | Service Parameter |
|--------|--------------|-------------------|
| Type syntax | `type="id"` | `type="String"` |
| Required | `not-null="true"` | `required="true"` |
| Default (literal) | `default="'N'"` | `default-value="N"` |
| Default (expression) | `default="'N'"` | `default="ec.user.nowTimestamp"` |

### Parameter Relaxation in Replacement Services

When a downstream component replaces a base service (same `verb#noun` and package, loaded later by component order), it may need to handle broader use cases. If the base service has `required="true"` on a parameter that the replacement needs to accept as optional, **both the base and replacement service definitions must be updated**.

**Why both must change:** Moqui validates parameters against the service definition it loads. If the base definition still has `required="true"`, callers that don't provide the parameter will fail validation even if the replacement logic handles null.

**Pattern:**

```xml
<!-- Base service: make parameter optional, document the null behavior -->
<service verb="get" noun="StorageBaseUrl">
    <in-parameters>
        <parameter name="partyId">
            <description>When not provided, returns raw base URL without tenant prefix.</description>
        </parameter>
    </in-parameters>
    <actions>
        <set field="baseUrl" from="System.getenv('BUCKET_LOCATION') ?: 'dbresource://default'"/>
    </actions>
</service>

<!-- Replacement service (downstream component, same package): guard dependent logic -->
<service verb="get" noun="StorageBaseUrl">
    <in-parameters>
        <parameter name="partyId">
            <description>Optional. When null, skips tenant resolution.</description>
        </parameter>
    </in-parameters>
    <actions>
        <set field="tenantPartyId" from="null"/>
        <set field="rawBaseUrl" from="null"/>
        <if condition="partyId">
            <!-- Tenant resolution logic only when partyId is available -->
            <service-call name="get#TopAncestor" .../>
            <service-call name="get#PartyOrParentSetting" .../>
        </if>
        <if condition="!rawBaseUrl">
            <set field="rawBaseUrl" from="System.getenv('BUCKET_LOCATION') ?: 'dbresource://default'"/>
        </if>
    </actions>
</service>
```

**Rules:**
- **Both base and replacement must agree**: Remove `required="true"` in both definitions
- **Guard dependent logic**: Wrap party/tenant-dependent logic in `<if condition="partyId">` so null is handled
- **Document the null case**: Update descriptions to explain behavior when the parameter is omitted
