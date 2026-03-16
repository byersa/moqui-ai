# Service Standards

### Service Naming Convention
- **Verb-Noun Pattern**: Use `verb#Noun` format (e.g., `create#Order`, `update#Customer`, `get#ProductList`)
- **Common Verbs**: `create`, `update`, `store`, `delete`, `get`, `find`, `process`, `validate`, `setup`
- **CamelCase Nouns**: Service nouns use CamelCase (e.g., `FiscalTaxDocument`, `PartyRelationship`)

### Service Definition Structure
- **XML DSL Preference**: Use Moqui's declarative XML elements over inline Groovy scripts
- **Three-Tier Script Approach**:
  1. XML DSL (preferred) - Use Moqui's declarative XML elements
  2. External Groovy file - When majority of service logic is Groovy
  3. Inline script blocks (discouraged) - Only for small calculations

### Parameter Standards
- **Explicit Types**: Always specify parameter types (`String`, `Integer`, `List`, `Map`, `Timestamp`)
- **Required Fields**: Mark required parameters with `required="true"`
- **Default Values**: Use `default` attribute for optional parameters with defaults
- **Validation**: Use built-in validation attributes (`min-length`, `max-length`, `regexp`) over manual checks

### Transaction Management
- **Default Behavior**: 98.5% of services use default transaction behavior (omit `transaction` attribute)
- **Explicit Transactions Only For**:
  - Audit logging (`transaction="require-new"`)
  - Notifications (`transaction="require-new"`)
  - Read-only reports (`transaction="ignore"`)
  - Long-running tasks (manual control)

### Filter Context Setup
- **REST APIs**: MUST call filter context setup service before querying filtered entities
- **Scheduled Jobs**: Must either login as user or use `disable-authz="true"`
- **Screen-Called Services**: Inherit context from root screen (no setup needed)

### Error Handling
- **Return Statements**: Use `<return error="true" message="..."/>` for validation failures
- **User Messages**: Use `<message type="success|warning|info">` for user feedback
- **No Silent Failures**: Always provide meaningful error messages

### Auto-Services
- **Entity Operations**: Always use auto-services (`create#Entity`, `update#Entity`, `delete#Entity`)
- **Never Use**: `ec.entity.makeValue()` with manual `.create()` calls

### Default Attribute Omission Rule

**Never specify attributes that are already the default** — they cause redundant warnings. See `global/xml-defaults.md` for complete reference.

Note: This applies to XML element attributes (e.g., `type="inline"`, `authenticate="true"`), NOT to parameter `default` values which are valid and encouraged (see Parameter Standards above).

```xml
<!-- BAD: Redundant default attributes -->
<service verb="create" noun="Order" type="inline" authenticate="true">

<!-- GOOD: Omit attributes that match defaults -->
<service verb="create" noun="Order">
```

### Configuration Access Patterns

**Accessing system properties:**
```xml
<set field="configValue" from="ec.artifactExecution.getProperty('config.key')"/>
```

**Environment-specific configuration:**
```xml
<set field="instancePurpose" from="System.getProperty('instance_purpose')"/>
<if condition="instancePurpose == 'production'">
    <!-- Production-specific logic -->
</if>
```

### Service Location Patterns

| Location Type | When to Use |
|---------------|-------------|
| Inline (default) | Standard services |
| External Groovy | Complex algorithms, heavy Groovy |
| Java class | Performance-critical, legacy integration |

```xml
<!-- Inline (default, omit location) -->
<service verb="create" noun="Order">

<!-- External Groovy -->
<service verb="process" noun="ComplexData"
         location="component://example/service/ProcessingScript.groovy"
         method="processComplexData">

<!-- Java class -->
<service verb="calculate" noun="Tax"
         location="com.example.TaxService"
         method="calculateTax">
```

### Service Authentication Levels

| Value | Behavior |
|-------|----------|
| `true` (default) | User must be logged in |
| `anonymous-all` | No authentication required |
| `anonymous-view` | No auth for read-only |

### Common Service Patterns

**Store service (create or update):**
```xml
<service verb="store" noun="Customer">
    <in-parameters>
        <auto-parameters entity-name="Customer" include="nonpk"/>
        <parameter name="customerId"/>
    </in-parameters>
    <actions>
        <service-call name="store#Customer" in-map="context" out-map="context"/>
    </actions>
</service>
```

### Duplicate Validation Before Create

**Use entity-find-one + return error to prevent duplicates before creation:**

```xml
<service verb="create" noun="Currency">
    <in-parameters>
        <parameter name="uomId" required="true"/>
        <parameter name="description" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="uomId"/>
    </out-parameters>
    <actions>
        <entity-find-one entity-name="moqui.basic.Uom" value-field="existingRecord">
            <field-map field-name="uomId"/>
            <field-map field-name="uomTypeEnumId" value="UT_CURRENCY_MEASURE"/>
        </entity-find-one>

        <if condition="existingRecord">
            <return error="true" message="El registro '${uomId}' ya existe"/>
        </if>

        <service-call name="create#moqui.basic.Uom"
                in-map="[uomId:uomId, uomTypeEnumId:'UT_CURRENCY_MEASURE', description:description]"
                out-map="context"/>
    </actions>
</service>
```

**When to use:**
- Creating records with user-supplied primary keys (not auto-generated)
- Business rules require a specific error message for duplicates
- The entity has a composite key where only some fields are user-provided

**Notes:**
- Use `entity-find-one` (not `entity-find`) for PK-based lookups
- Include ALL relevant key fields in the duplicate check, not just the PK
- Error messages should be in the user-facing language (Spanish for this project)
- The `return error="true"` stops execution and displays the message to the user