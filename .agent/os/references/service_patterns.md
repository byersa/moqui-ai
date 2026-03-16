# Moqui Service Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/services.md` - Service naming and definition
- `standards/backend/service-parameters.md` - Parameter types and validation
- `standards/backend/service-composition.md` - Service chaining and output handling patterns
- `standards/backend/logging-audit.md` - Structured logging, entity audit, and artifact tracking
- `standards/backend/performance-optimization.md` - Service performance tuning and batch operations

---

## Service Design Principles

### Single Responsibility
- Each service should have one clear, well-defined purpose
- Avoid creating monolithic services that handle multiple unrelated operations
- Split complex workflows into smaller, focused services

### Interface Design
- Use clear, descriptive service names following verb-noun pattern
- Design parameters to be self-documenting
- Provide meaningful error messages and return values
- Consider backward compatibility when modifying existing services

### Stateless Design
- Services should not rely on external state between calls
- All required data should be passed as parameters
- Use context variables appropriately within service execution

## Service Organization

### Project Naming Conventions (CRITICAL)

**MANDATORY**: Before creating services, read the project's naming conventions file:

📄 **Configuration**: `runtime/component/{main-component}/.agent-os/naming-conventions.md`
📄 **Framework Guide**: `runtime/component/moqui-agent-os/project-naming-conventions.md`

The naming-conventions.md file defines:
- **Service path prefix** (e.g., `mycompany/myapp/inventory`)
- **Service dot notation prefix** (e.g., `mycompany.myapp.inventory`)
- **Domain hierarchy** for organizing related services
- **File organization patterns** for service XML files

### Package Structure
```
service/
├── [path-prefix]/              # e.g., mycompany/myapp/inventory/
│   ├── [DomainArea]/           # e.g., product/, role/
│   │   ├── [EntityName]Services.xml
│   │   └── [WorkflowName]Services.xml
│   └── [IntegrationArea]/
│       └── [ExternalSystem]Services.xml
```

### Naming Conventions
- **Verb-Noun Pattern**: `create#Customer`, `update#Order`, `process#Payment`
- **Full Service Names**: Always use fully qualified names with project prefix
- **Domain Grouping**: Group related services in the same file
- **Clear Descriptive Names**: Services should be self-documenting

### Service Name Examples

```xml
<!-- CORRECT: Fully qualified service name with project prefix -->
<service-call name="mycompany.myapp.inventory.product.ProductServices.create#Product"/>

<!-- WRONG: Service in root directory without prefix -->
<service-call name="ProductServices.create#Product"/>
```

## Critical: Service Definition Architecture

### Services MUST Be Defined in XML (CRITICAL)

**NEVER create standalone Groovy files that attempt to define services.** This is a fundamental architecture violation.

#### Anti-Pattern: Standalone Groovy Service File (WRONG)

```groovy
// WRONG: ProductSearchServices.groovy
// This file should NOT exist - services cannot be defined this way

class ProductSearchServices {
    Map create_ProductIndex() {  // WRONG: No class methods
        ExecutionContext ec = context.ec
        // ... logic ...
        return [success: true, message: "Done"]  // WRONG: No return Maps
    }
}
```

**Why this is wrong:**
- Services MUST be defined in XML files
- Groovy classes with methods are NOT how Moqui services work
- Returning Maps bypasses the framework's parameter handling
- The framework won't recognize these as services

#### Correct Pattern: XML Definition + Optional Script Reference

**Option 1: XML DSL (Preferred)**
```xml
<!-- service/ProductSearchServices.xml -->
<service verb="create" noun="ProductIndex">
    <out-parameters>
        <parameter name="success" type="Boolean"/>
        <parameter name="message"/>
    </out-parameters>
    <actions>
        <set field="elasticClient" from="ec.factory.elastic.getDefault()"/>
        <if condition="elasticClient == null">
            <set field="success" from="false"/>
            <set field="message" value="OpenSearch not available"/>
            <return/>
        </if>
        <!-- More XML DSL logic -->
        <set field="success" from="true"/>
        <set field="message" value="Index created"/>
    </actions>
</service>
```

**Option 2: External Script Reference (for complex logic)**
```xml
<!-- service/ProductSearchServices.xml -->
<service verb="create" noun="ProductIndex" type="script"
         location="component://{component-name}/service/scripts/CreateProductIndex.groovy">
    <out-parameters>
        <parameter name="success" type="Boolean"/>
        <parameter name="message"/>
    </out-parameters>
</service>
```

```groovy
// service/scripts/CreateProductIndex.groovy
// This is a FLAT SCRIPT, not a class!
// Input parameters are ALREADY available as variables
// Output parameters are SET as variables, not returned

import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec

def elasticClient = ec.factory.elastic.getDefault()
if (elasticClient == null) {
    success = false
    message = "OpenSearch not available"
    return  // Early return is OK, but don't return a value
}

// ... implementation logic ...

// Set output parameters directly - NO return statement with Map
success = true
message = "Index created successfully"
```

#### Script File Rules

| Rule | Description |
|------|-------------|
| No class definition | Script is flat code, not `class MyService { }` |
| No method definitions | No `Map myMethod() { }` - just flat code |
| No return with value | Use `return` for early exit, but never `return [...]` |
| Parameters pre-bound | Input params already exist as variables |
| Set output directly | Assign to variable names matching out-parameters |

#### File Organization for Script Services

```
service/
├── ProductSearchServices.xml      # Service definitions (REQUIRED)
└── scripts/                           # Script implementations (optional)
    ├── CreateProductIndex.groovy
    ├── IndexProduct.groovy
    └── SearchProducts.groovy
```

## Critical: Service Element Structure

### `description` Is a Child Element, NOT an Attribute (CRITICAL)

Per the XSD (`service-definition-3.xsd`), `<description>` is a **child element** of `<service>`, not an attribute. The `<service>` element only accepts these attributes: `verb`, `noun`, `displayName`, `type`, `location`, `method`, `authenticate`, `allow-remote`, `validate`, `transaction`, `no-tx-cache`, `semaphore`, `semaphore-parameter`, `semaphore-timeout`, `semaphore-sleep`, `semaphore-ignore`, `max-retry`.

```xml
<!-- WRONG: description as attribute (invalid per XSD) -->
<service verb="migrate" noun="OrphanedRecords"
         description="Fix orphaned records after deletion">
    <actions><!-- ... --></actions>
</service>

<!-- CORRECT: description as child element -->
<service verb="migrate" noun="OrphanedRecords">
    <description>Fix orphaned records after deletion</description>
    <actions><!-- ... --></actions>
</service>
```

**The same rule applies to `<parameter>`** — its `description` is also a child element:
```xml
<!-- WRONG -->
<parameter name="partyId" description="The party identifier"/>

<!-- CORRECT -->
<parameter name="partyId">
    <description>The party identifier</description>
</parameter>
```

---

## Critical: Default Value Handling

### Transaction Attribute (CRITICAL)
**NEVER explicitly specify the default transaction value**

```xml
<!-- INCORRECT: Explicitly specifying default transaction value -->
<service verb="reroute" noun="DteViaApi" transaction="use-or-begin">
    <!-- This is redundant - use-or-begin is the default -->
</service>

<!-- CORRECT: Omit transaction attribute for default behavior -->
<service verb="reroute" noun="DteViaApi">
    <!-- use-or-begin is applied automatically -->
</service>

<!-- CORRECT: Only specify when non-default behavior is needed -->
<service verb="processLongRunning" noun="Report" transaction="ignore">
    <!-- Explicitly needed: run without transaction -->
</service>
```

**Valid Transaction Values:**
- `use-or-begin` (DEFAULT) - Use existing transaction or start new one. Best for most services.
- `ignore` - Run without transaction. Use for long-running operations or read-only queries.
- `force-new` - Always start new transaction, suspend existing. Use when sub-operation must be isolated.
- `cache` - Use transaction cache. Rarely needed.
- `force-cache` - Force transaction cache. Rarely needed.

**INVALID Values:**
- `require-new` - This is NOT a valid Moqui value (causes XML validation error)
- `required` - This is NOT a valid Moqui value
- `requires_new` - This is NOT a valid Moqui value

### Default Attribute Values (CRITICAL)
**NEVER explicitly specify default attribute values - they cause redundant default warnings**

```xml
<!-- INCORRECT: Specifying default type for String parameters -->
<out-parameters>
    <parameter name="message" type="String">  <!-- String is default! -->
        <description>Result message</description>
    </parameter>
</out-parameters>

<!-- CORRECT: Omit type attribute for String parameters -->
<out-parameters>
    <parameter name="message">  <!-- String is assumed -->
        <description>Result message</description>
    </parameter>
</out-parameters>
```

**Common Default Values to NEVER Specify:**
- `type="String"` on parameters (String is the default type)
- `transaction="use-or-begin"` on services (use-or-begin is the default)
- `required="false"` on parameters (false is the default)

## Parameter Patterns

### Auto-Parameters Usage
```xml
<!-- Use auto-parameters for entity operations -->
<in-parameters>
    <auto-parameters entity-name="Customer" include="pk" required="true"/>
    <auto-parameters entity-name="Customer" include="nonpk"/>
</in-parameters>
```

### Parameter Validation
```xml
<in-parameters>
    <parameter name="emailAddress" required="true">
        <matches regexp="^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"/>
    </parameter>
    <parameter name="amount" type="BigDecimal" required="true">
        <number-range min="0.01" max="999999.99"/>
    </parameter>
</in-parameters>
```

### Service Parameter Types (CRITICAL)
**NEVER use "id" as a type for service parameters**

```xml
<!-- INCORRECT: "id" type in service parameter -->
<service verb="get" noun="PartyInfo">
    <in-parameters>
        <parameter name="partyId" type="id" required="true"/>  <!-- WRONG! -->
    </in-parameters>
</service>

<!-- CORRECT: String type or omit type for ID parameters -->
<service verb="get" noun="PartyInfo">
    <in-parameters>
        <parameter name="partyId" required="true"/>  <!-- String is default -->
    </in-parameters>
</service>
```

**Service Parameter Types vs Entity Field Types:**
- Service parameters: Use String, Integer, Long, BigDecimal, Boolean, Timestamp, Date, Time, List, Map
- Entity fields: Use id, id-long, text-short, text-medium, text-long, text-very-long, date, date-time, time, number-integer, number-decimal, number-float, currency-amount, currency-precise, binary-very-long

## Error Handling Patterns

### Validation with Error Messages
```xml
<actions>
    <if condition="!emailAddress">
        <return error="true" message="Email address is required"/>
    </if>

    <if condition="amount &lt;= 0">
        <return error="true" message="Amount must be greater than zero"/>
    </if>
</actions>
```

### Exception Handling
```xml
<actions>
    <try>
        <service-call name="external.api.call#Service" in-map="context"/>
        <catch>
            <log level="warn" message="External service call failed: ${exception.message}"/>
            <return error="true" message="External service temporarily unavailable"/>
        </catch>
    </try>
</actions>
```

## Transaction Patterns

### Transaction Boundary Design
- **Atomic Operations**: Keep transactions as short as possible while maintaining consistency
- **Resource Minimization**: Minimize the number of entities and operations within a single transaction
- **Rollback Strategy**: Always design comprehensive rollback procedures for complex operations

### Transaction Configuration Guidelines
- **force-new**: Use for independent operations (audit logs, notifications)
- **use-or-begin**: Default for most business operations requiring consistency
- **ignore**: Use only for read-only operations with no data modifications
- **Manual Control**: Use transaction-begin/commit/rollback for complex multi-step operations

### Explicit Transaction Control
```xml
<service verb="process" noun="ComplexWorkflow">
    <in-parameters>
        <parameter name="workflowData" type="Map" required="true"/>
    </in-parameters>
    <actions>
        <transaction-begin/>
        <try>
            <service-call name="step1#ProcessData" in-map="workflowData"/>
            <service-call name="step2#ValidateData" in-map="workflowData"/>
            <service-call name="step3#SaveData" in-map="workflowData"/>
            <transaction-commit/>
            <catch>
                <transaction-rollback/>
                <return error="true" message="Workflow processing failed: ${exception.message}"/>
            </catch>
        </try>
    </actions>
</service>
```

## Primary Key Handling (CRITICAL)

**CRITICAL: Let services generate and return primary keys**

```xml
<!-- CORRECT: Let services generate and return primary keys -->
<service-call name="create#Product" out-map="createResult">
    <field-map field-name="productName" value="Sample Product"/>
    <field-map field-name="productTypeId" value="FINISHED_GOOD"/>
    <field-map field-name="price" from="99.99"/>
</service-call>

<!-- Use the returned PK for subsequent operations -->
<service-call name="create#ProductFeature">
    <field-map field-name="productId" from="createResult.productId"/>  <!-- PK from previous call -->
    <field-map field-name="featureTypeId" value="COLOR"/>
    <field-map field-name="description" value="Blue"/>
</service-call>

<!-- INCORRECT: Explicitly setting primary key before creation -->
<set field="productId" value="PROD_12345"/>  <!-- WRONG! -->
<service-call name="create#Product">
    <field-map field-name="productId" from="productId"/>  <!-- Explicitly setting PK -->
</service-call>
```

**Problems with explicit PK assignment:**
- Bypasses automatic ID generation mechanisms
- Can cause ID collisions and constraint violations
- Makes services less reusable and harder to test
- Violates Moqui's service contract patterns

## Groovy Out-Parameter Patterns (CRITICAL)

**Most common mistake: Type declarations in Groovy services return null out-parameters**

```groovy
// NEVER declare variable types in Groovy service scripts - This will return null!
// These will ALL return null as out-parameters!
BigDecimal rate = 1.5
String message = "Processing complete"
List resultList = []
Map responseData = [:]
Boolean isValid = true

// CORRECT: Use implicit typing (no type declaration)
// These will correctly return as out-parameters
rate = 1.5
message = "Processing complete"
resultList = []
responseData = [:]
isValid = true
```

**Why type declarations fail:**
- Type declarations (like `BigDecimal rate`) create LOCAL variables
- Local variables shadow the implicit binding variables
- Moqui out-parameters use the implicit binding, not local variables
- The service will return null for all typed variable out-parameters

**This is the #1 source of "service returns null" issues in Moqui!**

## Time Calculation Patterns (CRITICAL)

**Use explicit time calculations for better readability and maintainability**

```xml
<!-- CORRECT: Explicit time calculations -->
<set field="sixMonthsMs" from="6*30*24*60*60*1000L" type="Long"/>
<set field="oneHourMs" from="60*60*1000L" type="Long"/>
<set field="threeDaysMs" from="3*24*60*60*1000L" type="Long"/>

<!-- INCORRECT: Hardcoded time values -->
<set field="sixMonthsMs" from="15552000000" type="Long"/><!-- What period is this? -->
<set field="someTimeMs" from="3600000" type="Long"/><!-- Need to calculate to understand -->
```

**Standard Time Patterns:**
```xml
<!-- Minutes -->
<set field="oneMinuteMs" from="60*1000" type="Long"/>
<set field="fiveMinutesMs" from="5*60*1000" type="Long"/>

<!-- Hours -->
<set field="oneHourMs" from="60*60*1000" type="Long"/>
<set field="twelveHoursMs" from="12*60*60*1000" type="Long"/>

<!-- Days -->
<set field="oneDayMs" from="24*60*60*1000L" type="Long"/>
<set field="oneWeekMs" from="7*24*60*60*1000L" type="Long"/>

<!-- Months (approximate, 30 days) -->
<set field="oneMonthMs" from="30*24*60*60*1000L" type="Long"/>
<set field="sixMonthsMs" from="6*30*24*60*60*1000L" type="Long"/>

<!-- Years (approximate, 365 days) -->
<set field="oneYearMs" from="365*24*60*60*1000L" type="Long"/>
```

## XML DSL Enforcement in Service Implementations (CRITICAL)

Service actions MUST use Moqui's XML DSL elements for standard operations. Inline `<script>` blocks should only be used for logic that **cannot** be expressed in XML DSL (recursion, closures, complex Groovy transformations). See `standards/backend/xml-dsl-vs-script.md` for the complete decision matrix.

### What MUST Use XML DSL

| Operation | XML DSL Element | Never Use |
|-----------|----------------|-----------|
| Entity query (list) | `<entity-find>` | `ec.entity.find(...).list()` |
| Entity query (one) | `<entity-find-one>` | `ec.entity.find(...).one()` |
| Entity create | `<service-call name="create#...">` | `ec.entity.makeValue(...).create()` |
| Entity update | `<service-call name="update#...">` | `ec.entity.find(...).updateAll(...)` |
| Service call | `<service-call>` | `ec.service.sync().name(...).call()` |
| Conditional | `<if>` / `<else-if>` / `<else>` | `if (...) { }` in script |
| Loop | `<iterate>` | `for (def x in list) { }` in script |
| Variable | `<set>` | `x = value` in script |
| Logging | `<log>` | `ec.logger.info(...)` |
| Error return | `<return error="true">` | `ec.message.addError(...)` |
| Skip iteration | `<continue/>` | `continue` in script |

### Hybrid Pattern: XML DSL with Script for Complex Logic

When a service needs both XML DSL and script (e.g., recursion, closures), define the script-only parts first, then use XML DSL for the rest:

```xml
<actions>
    <!-- Script ONLY for what XML DSL cannot express (closures, recursion) -->
    <script><![CDATA[
        Closure processChildren
        processChildren = { String parentId ->
            // Recursive logic that XML DSL cannot express
        }
    ]]></script>

    <!-- XML DSL for the main flow -->
    <entity-find entity-name="example.Parent" list="parents">
        <econdition field-name="statusId" value="Active"/>
    </entity-find>
    <iterate list="parents" entry="parent">
        <script>processChildren(parent.parentId)</script>
    </iterate>

    <log level="info" message="Processed ${parents.size()} parents"/>
</actions>
```

### Anti-Pattern: Entire Service in Script

```xml
<!-- WRONG: Everything in a single script block -->
<actions>
    <script><![CDATA[
        def items = ec.entity.find("example.Item").condition("status", "Active").list()
        for (def item in items) {
            def detail = ec.entity.find("example.ItemDetail")
                .condition("itemId", item.itemId).one()
            if (!detail) continue
            ec.logger.info("Processing ${item.itemId}")
            ec.service.sync().name("update#example.Item")
                .parameters([itemId: item.itemId, processed: "Y"]).call()
        }
    ]]></script>
</actions>

<!-- CORRECT: XML DSL for all standard operations -->
<actions>
    <entity-find entity-name="example.Item" list="items">
        <econdition field-name="status" value="Active"/>
    </entity-find>
    <iterate list="items" entry="item">
        <entity-find-one entity-name="example.ItemDetail" value-field="detail">
            <field-map field-name="itemId" from="item.itemId"/>
        </entity-find-one>
        <if condition="!detail"><continue/></if>

        <log level="info" message="Processing ${item.itemId}"/>
        <service-call name="update#example.Item"
            in-map="[itemId: item.itemId, processed: 'Y']"/>
    </iterate>
</actions>
```

---

## Data Access Patterns

### Entity Operations
```xml
<!-- Find single record -->
<entity-find-one entity-name="Customer" value-field="customer">
    <field-map field-name="customerId" from="customerId"/>
</entity-find-one>

<!-- Find multiple records with conditions -->
<entity-find entity-name="Order" list="orderList">
    <econdition field-name="customerId" from="customerId"/>
    <econdition field-name="statusId" value="OrderOpen"/>
    <order-by field-name="orderDate"/>
</entity-find>

<!-- Create with auto-sequencing -->
<entity-sequenced-id-primary entity-name="Order" value-field="order"/>
<set field="order.customerId" from="customerId"/>
<entity-create value-field="order"/>
```

### Party Entity Type Hierarchy (CRITICAL)

`mantle.party.Party` is a **supertype** entity. It contains ALL party types (organizations, persons, etc.) differentiated by `partyTypeEnumId`. When querying Party, **always filter by type** if you only need one kind:

```xml
<!-- WRONG: Queries ALL parties — includes persons, orgs, and other types -->
<entity-find entity-name="mantle.party.Party" list="disabledOrgs">
    <econdition field-name="disabled" value="Y"/>
</entity-find>

<!-- CORRECT: Filter by party type to get only organizations -->
<entity-find entity-name="mantle.party.Party" list="disabledOrgs">
    <econdition field-name="disabled" value="Y"/>
    <econdition field-name="partyTypeEnumId" value="PtyOrganization"/>
</entity-find>
```

**Common `partyTypeEnumId` values:**
- `PtyOrganization` — Organizations (companies, departments, teams)
- `PtyPerson` — Individual people

**Why this matters:**
- Without the type filter, queries intended for organizations will also return persons (and vice versa)
- Relationship queries (e.g., `PrtOrgRollup`, `PrtEmployee`) combined with unfiltered Party queries can produce false matches across party types
- Migration and cleanup scripts are especially vulnerable — processing the wrong party type can corrupt data

### Bulk Operations
```xml
<service verb="process" noun="OrderItemsBulk">
    <actions>
        <!-- Process in batches to avoid memory issues -->
        <set field="batchSize" value="100"/>
        <set field="offset" value="0"/>

        <while condition="true">
            <entity-find entity-name="OrderItem" list="itemBatch" limit="batchSize" offset="offset">
                <econdition field-name="statusId" value="ItemPending"/>
            </entity-find>

            <if condition="!itemBatch">
                <break/>
            </if>

            <iterate list="itemBatch" entry="item">
                <service-call name="process#OrderItem" in-map="[orderItemSeqId: item.orderItemSeqId]"/>
            </iterate>

            <set field="offset" from="offset + batchSize"/>
        </while>
    </actions>
</service>
```

## Service Templates

### Basic Service Template
```xml
<service verb="[VERB]" noun="[NOUN]">
    <description>[Service description]</description>
    <in-parameters>
        <parameter name="[PARAMETER_NAME]" required="[true/false]"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[PARAMETER_NAME]"/>
    </out-parameters>
    <actions>
        <!-- Service implementation here -->
    </actions>
</service>
```

### CRUD Service Templates

**Create Service:**
```xml
<service verb="create" noun="[Entity]">
    <description>Create a new [Entity] record</description>
    <in-parameters>
        <auto-parameters entity-name="[EntityName]" include="nonpk"/>
    </in-parameters>
    <out-parameters>
        <auto-parameters entity-name="[EntityName]" include="pk"/>
    </out-parameters>
    <actions>
        <service-call name="create#[EntityName]" in-map="context" out-map="context"/>
    </actions>
</service>
```

**Update Service:**
```xml
<service verb="update" noun="[Entity]">
    <description>Update an existing [Entity] record</description>
    <in-parameters>
        <auto-parameters entity-name="[EntityName]" include="pk" required="true"/>
        <auto-parameters entity-name="[EntityName]" include="nonpk"/>
    </in-parameters>
    <actions>
        <service-call name="update#[EntityName]" in-map="context"/>
    </actions>
</service>
```

### Business Logic Service Template
```xml
<service verb="[VERB]" noun="[NOUN]">
    <description>[SERVICE_DESCRIPTION]</description>
    <in-parameters>
        <parameter name="[INPUT_PARAM]" required="true"/>
        <parameter name="[OPTIONAL_PARAM]"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[OUTPUT_PARAM]"/>
        <parameter name="messages" type="List"/>
    </out-parameters>
    <actions>
        <script><![CDATA[
            // CRITICAL: NO type declarations for out-parameters!
            // WRONG: String result = "value"  (returns null)
            // CORRECT: result = "value"       (returns proper value)

            // Input validation
            if (![INPUT_PARAM]) {
                ec.message.addError("Required parameter [INPUT_PARAM] is missing")
                return
            }

            // Business logic implementation
            [BUSINESS_LOGIC_CODE]

            // Set output parameters (no type declaration!)
            [OUTPUT_PARAM] = [CALCULATED_VALUE]
        ]]></script>
    </actions>
</service>
```

---

## Provider/Plugin Pattern with Dynamic Service Dispatch

When implementing extensible provider/plugin patterns (e.g., signature providers, payment gateways), use dynamic service dispatch instead of SECA.

### Why NOT to use SECA for Provider Patterns

SECA (Service Event-Condition-Action) rules execute **in addition to** the main service, not **instead of** it. This means:
- The main service's actions always execute after the SECA
- A `return error="true"` in the main service will always fail, even if SECA succeeded
- There's no way to prevent the main service from running

### Correct Pattern: Dynamic Service Dispatch via Enumeration

**Step 1: Data Definition (Enumeration)**
```xml
<!-- Register provider with service name in enumCode -->
<moqui.basic.EnumerationType enumTypeId="SignatureProvider" description="Signature service provider"/>
<moqui.basic.Enumeration enumTypeId="SignatureProvider" enumId="SigProvFirmaGob"
                         description="FirmaGob (Chile)"
                         enumCode="cl.gob.digital.firma.FirmaGobServices.sign#ForRequest"/>
```

**Step 2: Dispatcher Service**
```xml
<service verb="execute" noun="ProviderSignature">
    <description>Dispatcher service for provider-specific execution.
        The provider service name is stored in the Enumeration's enumCode field.
        Provider services must implement the same in/out parameter interface.</description>
    <in-parameters>
        <parameter name="providerEnumId" required="true"/>
        <!-- ... other parameters passed to provider -->
    </in-parameters>
    <out-parameters>
        <!-- ... out-parameters returned by provider -->
    </out-parameters>
    <actions>
        <entity-find-one entity-name="moqui.basic.Enumeration" value-field="providerEnum">
            <field-map field-name="enumId" from="providerEnumId"/>
        </entity-find-one>
        <if condition="!providerEnum">
            <return error="true" message="Provider not found: ${providerEnumId}"/>
        </if>
        <set field="providerServiceName" from="providerEnum.enumCode"/>
        <if condition="!providerServiceName">
            <return error="true" message="Provider ${providerEnumId} has no service configured"/>
        </if>

        <!-- Call the provider service dynamically -->
        <service-call name="${providerServiceName}" out-map="context">
            <!-- Pass all required parameters -->
        </service-call>
    </actions>
</service>
```

**Step 3: Provider Implementation**
```xml
<service verb="sign" noun="ForRequest">
    <description>Provider implementation. Must match dispatcher's parameter interface.</description>
    <in-parameters>
        <!-- Same parameters as dispatcher passes -->
    </in-parameters>
    <out-parameters>
        <!-- Same out-parameters as dispatcher expects -->
    </out-parameters>
    <actions>
        <!-- Provider-specific implementation -->
    </actions>
</service>
```

---

## Service Parameter Pass-Through Pattern

**Parameters are ONLY passed to called services if explicitly declared in the intermediate service's in-parameters**

```xml
<!-- Service C (final destination) expects additionalRoleTypeIdList -->
<service verb="add" noun="PartyInternal">
    <in-parameters>
        <parameter name="organizationName" required="true"/>
        <parameter name="additionalRoleTypeIdList" type="List"/>
    </in-parameters>
</service>

<!-- CORRECT: Service B declares the parameter so it can be passed through -->
<service verb="add" noun="TenantChild">
    <in-parameters>
        <parameter name="organizationName" required="true"/>
        <parameter name="additionalRoleTypeIdList" type="List"/>  <!-- Declared here -->
    </in-parameters>
    <actions>
        <!-- Now additionalRoleTypeIdList will be in context and passed to PartyInternal -->
        <service-call name="add#PartyInternal" in-map="context"/>
    </actions>
</service>
```

**Framework guarantee — parameter stripping:** When `validate="true"` (the default), Moqui's `convertValidateCleanParameters` creates a new `HashMap` containing **only** declared `in-parameters`. All undeclared keys from the incoming map are silently dropped. This means `in-map="context"` is safe even when the caller's context contains many extra variables — only the called service's declared parameters are passed through. When `validate="false"`, undeclared parameters are passed through unfiltered.

**Best practices:**
- Document which parameters need to flow through the entire chain
- Ensure consistent parameter names across all services in the chain
- Explicitly declare all pass-through parameters in intermediate services
- `in-map="context"` does NOT leak accumulated state to called services (parameters are stripped by default)

---

## Party Name Formatting Pattern

**Always use PartyNameOnlyTemplate or PartyNameTemplate for displaying party names**

```xml
<!-- CORRECT: Use PartyNameOnlyTemplate for name only -->
<entity-find-one entity-name="mantle.party.PartyDetail" value-field="partyDetail">
    <field-map field-name="partyId"/>
</entity-find-one>
<set field="partyName" from="ec.resource.expand('PartyNameOnlyTemplate', null, partyDetail)"/>

<!-- CORRECT: Use PartyNameTemplate for name with identifier -->
<set field="partyNameWithId" from="ec.resource.expand('PartyNameTemplate', null, partyDetail)"/>
```

---

## Time Calculation Patterns

**Use explicit time calculations for better readability:**
```xml
<!-- Standard Time Patterns -->
<set field="oneMinuteMs" from="60*1000" type="Long"/>
<set field="oneHourMs" from="60*60*1000" type="Long"/>
<set field="oneDayMs" from="24*60*60*1000L" type="Long"/>
<set field="oneWeekMs" from="7*24*60*60*1000L" type="Long"/>
<set field="oneMonthMs" from="30*24*60*60*1000L" type="Long"/>
<set field="sixMonthsMs" from="6*30*24*60*60*1000L" type="Long"/>
```

---

## Configuration Access Patterns

| Configuration Need | Type | Access Method |
|-------------------|------|---------------|
| Database URL | System Property | `SystemBinding.getPropOrEnv('database_url')` |
| API Endpoint | System Property | `SystemBinding.getPropOrEnv('api_endpoint')` |
| API Key/Secret | System Property | `SystemBinding.getPropOrEnv('api_key')` |
| Max Orders Per Customer | PartySettingType | `ec.user.getPreference('MaxOrdersPerCustomer')` |

```xml
<!-- Infrastructure Config (System Properties) -->
<set field="apiEndpoint" from="org.moqui.util.SystemBinding.getPropOrEnv('external_api_endpoint')"/>

<!-- Application Config (PartySettingType) -->
<set field="maxOrders" from="ec.user.getPreference('MaxOrdersPerCustomer') as Integer"/>
```

---

## Store Verb (Upsert Pattern)

The `store` verb performs an upsert: creates a new record if the PK is absent, updates if present.

```xml
<service verb="store" noun="ProductSetting" type="entity-auto">
    <in-parameters>
        <auto-parameters entity-name="ProductSetting" include="all"/>
    </in-parameters>
</service>
```

---

## Async Service Calls

### XML Declaration
```xml
<service-call name="process#LargeDataSet" async="true"/>
```

### Programmatic
```groovy
ec.service.async().name("process#LargeDataSet")
    .parameter("dataSetId", dataSetId)
    .call()
```

---

## Service Behavior Gotchas

### Critical Service Name Distinctions

Services can have misleading names. Always verify actual behavior:

| Service | What It Actually Does | When to Use |
|---------|----------------------|-------------|
| `ship#OrderPart` | Creates NEW shipment + packs items + marks shipped | No existing shipment |
| `create#OrderPartShipment` | Creates shipment record only | Need custom packing logic |
| `ship#Shipment` | Marks existing shipment as shipped | Shipment exists, needs status change |
| `update#OrderStatus` | Changes order status only | Status transition needed |

### Service Verification Workflow
1. **Read service implementation** in XML files
2. **Check service description** and parameters
3. **Look for entity-auto** vs custom implementations
4. **Test service behavior** in development
5. **Document actual behavior** for future reference

---

## Groovy Closure Scope Bug

`.each{}` closures cannot access outer scope variables like `logger`:

```groovy
// BROKEN: logger not accessible in closure
existingItems.each { item ->
    logger.info("Item: ${item.itemId}") // NullPointerException!
}

// CORRECT: Use traditional for loop
for (EntityValue item in existingItems) {
    logger.info("Item: ${item.itemId}") // Works!
}
```

**Rule**: Prefer `for (item in list)` loops when accessing outer variables (`logger`, `ec`, etc.).

---

## User Context Access

```groovy
// Current timestamp
ec.user.nowTimestamp

// User preferences
ec.user.getPreference('settingName')

// Role check
ec.user.isUserInRole('ADMIN')
```

---

## Utility Operations

```groovy
// Name conversion
prettyName = org.moqui.util.StringUtilities.prettyToCamelCase("Some Name")

// Random string
randomStr = org.moqui.util.StringUtilities.getRandomString(20)

// Hashing
hash = ec.ecfi.getSimpleHash(inputString, "SHA-256", "hex")

// Collection filtering
activeItems = itemList.findAll { it.statusId == 'Active' }

// Date arithmetic
sevenDaysAgo = new Timestamp(ec.user.nowTimestamp.getTime() - 7*24*60*60*1000L)
```

---

## Systematic Debugging Approach

### Priority Order
1. **Syntax errors** - Must compile before logic testing
2. **Entity field names** - Validate against definitions
3. **Service behavior** - Verify what services actually do
4. **Logic flow** - Check business logic after basics work
5. **Performance issues** - Optimize after functionality works

### Refactoring Mindset

Before writing custom logic, ask:
1. **Is there a Moqui service that does this?** Check mantle-* components
2. **Am I duplicating existing functionality?** Review existing services
3. **Is this getting too complex?** More than 20 lines = consider refactoring

---

## Common Pitfalls Checklist

1. Missing dependencies in component.xml
2. Incorrect field types (use `id`, `text-medium`, not `String`)
3. Missing relationships for foreign keys
4. No authentication set on services
5. Hardcoded values instead of enumerations
6. Raw SQL instead of entity-find with econdition
7. Wrong transaction attributes (omit for defaults)
8. Missing localization on user-facing text
9. Missing audit fields on transactional entities
10. Service names not following verb-noun convention
11. Invalid entity field names (always validate first)
12. Service behavior misunderstanding (verify before using)
13. Closure scope issues (use for loops for outer variables)
14. Over-engineering (prefer existing services over custom logic)

---

## Service Types Reference

```xml
<!-- inline: XML Actions (most common) -->
<service verb="get" noun="Data" type="inline">
    <actions><!-- XML Actions --></actions>
</service>

<!-- script: Groovy or other script -->
<service verb="process" noun="Data" type="script"
    location="component://MyComp/script/ProcessData.groovy"/>

<!-- java: Java/Groovy class method -->
<service verb="process" noun="Data" type="java"
    location="mycomp.MyServiceClass" method="processData"/>

<!-- entity-auto: Auto CRUD for entities -->
<!-- These exist automatically for ALL entities, no definition needed:
     create#package.EntityName
     update#package.EntityName
     delete#package.EntityName
     store#package.EntityName
-->

<!-- interface: Define contract, no implementation -->
<service verb="receive" noun="DataFeed" type="interface">
    <in-parameters>
        <parameter name="dataFeedId" required="true"/>
        <parameter name="feedStamp" type="Timestamp"/>
        <parameter name="documentList" type="List" required="true"/>
    </in-parameters>
</service>

<!-- remote-xml-rpc / remote-json-rpc -->
<service verb="get" noun="ExternalData" type="remote-json-rpc"
    location="http://remote-host/rpc" method="getExternalData"/>

<!-- remote-rest: Call external REST API -->
<service verb="get" noun="ExternalResource" type="remote-rest"
    location="http://remote-host/api/resource" method="get"/>

<!-- camel: Apache Camel integration -->
<service verb="process" noun="CamelRoute" type="camel"
    location="camelRouteId" method="processRoute"/>
```

---

## Service Facade Groovy API

```groovy
// Synchronous call
Map result = ec.service.sync().name("mantle.order.OrderServices.place#Order")
    .parameter("orderId", orderId)
    .call()

// With map of parameters
Map result = ec.service.sync().name("mycomp.MyServices.process#Data")
    .parameters([param1: "value1", param2: "value2"])
    .call()

// Async call
ec.service.async().name("mycomp.MyServices.send#Notification")
    .parameter("orderId", orderId)
    .call()

// Async distributed (persisted)
ec.service.async().name("mycomp.MyServices.send#Notification")
    .parameter("orderId", orderId)
    .distribute(true)
    .call()

// Special calls
ec.service.special().name("mycomp.MyServices.on#Commit")
    .parameter("orderId", orderId)
    .registerOnCommit()  // Run after current transaction commits
```

---

## Mantle USL Key Services Reference

### Order Services (mantle.order.OrderServices)
```
create#Order          - Create OrderHeader + first OrderPart
create#OrderPart      - Add order part (ship group)
create#OrderItem      - Add item to order
update#OrderItem      - Update item qty/price
delete#OrderItem      - Remove item
add#OrderPartPayment  - Add payment method/info to order
place#Order           - Place order (triggers reservation)
approve#Order         - Approve order
cancel#Order          - Cancel order
cancel#OrderItem      - Cancel specific item
complete#OrderPart    - Complete order part
clone#Order           - Clone an existing order
```

### Order Info Services (mantle.order.OrderInfoServices)
```
get#OrderDisplayInfo  - Complete order info for display
```

### Shipment Services (mantle.shipment.ShipmentServices)
```
create#Shipment          - Create shipment
create#ShipmentItem      - Add item to shipment
create#ShipmentPackage   - Create package
pack#ShipmentProduct     - Pack product into package
pack#Shipment            - Mark shipment as packed (triggers invoicing)
ship#Shipment            - Mark as shipped
receive#EntireShipment   - Receive all items
```

### Product Services (mantle.product.ProductServices)
```
clone#Product             - Clone product
create#VariantProducts    - Create variants from features
find#ProductByIdValue     - Find by UPC, SKU, etc.
find#VariantProduct       - Find variant by feature combo
```

### Party Services (mantle.party.PartyServices)
```
create#Person              - Create person
create#PersonCustomer      - Create person + customer role + contact info
find#Party                 - Search parties
search#Party               - ElasticSearch party search
```

### Contact Services (mantle.party.ContactServices)
```
get#PartyContactInfo       - Get party's contact details
get#PartyContactInfoList   - Get all contact info for party
get#PrimaryEmailAddress    - Get primary email
store#PartyContactMech     - Add/update contact mech
delete#PartyContactMech    - Expire contact mech
```

### Payment Services (mantle.account.PaymentServices)
```
update#Payment             - Update payment details
```

### Asset/Inventory Services (mantle.product.AssetServices)
```
receive#Asset              - Receive inventory
get#AvailableInventory     - Check availability
move#AssetReservation      - Move reservation to different asset
```

### Price Services (mantle.product.PriceServices)
```
get#ProductPrice           - Calculate product price
```