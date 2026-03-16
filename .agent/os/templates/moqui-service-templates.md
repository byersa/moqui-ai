# Moqui Service Templates

## Standard Service Template

```xml
<service verb="[VERB]" noun="[NOUN]" type="entity-auto">
    <description>[SERVICE_DESCRIPTION]</description>
    <in-parameters>
        <parameter name="[ENTITY_ID]" type="String" required="[REQUIRED]"/>
        <parameter name="[FIELD_NAME]" type="String"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[ENTITY_ID]" type="String"/>
        <parameter name="[RESULT_FIELD]" type="String"/>
    </out-parameters>
</service>
```

## Business Logic Service Template

```xml
<service verb="[VERB]" noun="[NOUN]">
    <description>[SERVICE_DESCRIPTION]</description>
    <in-parameters>
        <parameter name="[INPUT_PARAM]" type="String" required="true"/>
        <parameter name="[OPTIONAL_PARAM]" type="String"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[OUTPUT_PARAM]" type="String"/>
        <parameter name="[RESULT_MESSAGE]" type="String"/>
    </out-parameters>
    <actions>
        <script><![CDATA[
            // Input validation
            if (![INPUT_PARAM]) {
                ec.message.addError("Required parameter [INPUT_PARAM] is missing")
                return
            }
            
            // Business logic implementation
            [BUSINESS_LOGIC_CODE]
            
            // Set output parameters
            [OUTPUT_PARAM] = [CALCULATED_VALUE]
            [RESULT_MESSAGE] = "Operation completed successfully"
        ]]></script>
    </actions>
</service>
```

## Groovy Implementation Service Template

```xml
<service verb="[VERB]" noun="[NOUN]">
    <description>[SERVICE_DESCRIPTION]</description>
    <in-parameters>
        <parameter name="[INPUT_PARAM]" type="String" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[OUTPUT_PARAM]" type="String"/>
    </out-parameters>
    <actions>
        <script location="component://[COMPONENT]/service/[DOMAIN]/[ServiceName].groovy"/>
    </actions>
</service>
```

## REST Service Template

```xml
<resource name="[RESOURCE_NAME]" require-authentication="[TRUE/FALSE]">
    <method type="get">
        <service name="get#[ENTITY_NAME]"/>
    </method>
    <method type="post">
        <service name="create#[ENTITY_NAME]"/>
    </method>
    <method type="put">
        <service name="update#[ENTITY_NAME]"/>
    </method>
    <method type="delete">
        <service name="delete#[ENTITY_NAME]"/>
    </method>
</resource>
```

## Validation Service Template

```xml
<service verb="validate" noun="[ENTITY_NAME]">
    <description>Validate [ENTITY_NAME] data before processing</description>
    <in-parameters>
        <parameter name="[ENTITY_ID]" type="String"/>
        <parameter name="[VALIDATION_FIELDS]" type="Map"/>
    </in-parameters>
    <out-parameters>
        <parameter name="isValid" type="Boolean"/>
        <parameter name="validationMessages" type="List"/>
    </out-parameters>
    <actions>
        <script><![CDATA[
            import groovy.transform.Field
            
            @Field List<String> validationMessages = []
            @Field boolean isValid = true
            
            // Validation logic
            [VALIDATION_RULES]
            
            // Set results
            ec.context.isValid = isValid
            ec.context.validationMessages = validationMessages
        ]]></script>
    </actions>
</service>
```

## Transaction Management Service Template

```xml
<service verb="[VERB]" noun="[NOUN]" transaction="[REQUIRE-NEW/USE-OR-BEGIN/IGNORE]">
    <description>[SERVICE_DESCRIPTION]</description>
    <in-parameters>
        <parameter name="[INPUT_PARAMS]" type="String" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="[OUTPUT_PARAMS]" type="String"/>
    </out-parameters>
    <actions>
        <script><![CDATA[
            try {
                // Transaction-specific business logic
                [BUSINESS_LOGIC]
                
                // Commit or rollback logic
                [TRANSACTION_CONTROL]
                
            } catch (Exception e) {
                // Error handling and rollback
                ec.message.addError("Error in [SERVICE_NAME]: ${e.message}")
                throw e
            }
        ]]></script>
    </actions>
</service>
```

## Placeholders Reference

- `[VERB]` - Service action verb (create, update, delete, get, process, calculate, etc.)
- `[NOUN]` - Service target noun (Customer, Order, Payment, etc.)
- `[SERVICE_DESCRIPTION]` - Clear description of service purpose
- `[ENTITY_ID]` - Primary key parameter name (customerId, orderId, etc.)
- `[FIELD_NAME]` - Entity field name for input/output
- `[REQUIRED]` - Boolean true/false for required parameters
- `[INPUT_PARAM]` - Input parameter name
- `[OUTPUT_PARAM]` - Output parameter name
- `[BUSINESS_LOGIC_CODE]` - Groovy business logic implementation
- `[CALCULATED_VALUE]` - Result of business logic calculation
- `[COMPONENT]` - Component name for script location
- `[DOMAIN]` - Domain directory for script organization
- `[RESOURCE_NAME]` - REST resource name (plural form)
- `[ENTITY_NAME]` - Entity name for CRUD operations
- `[VALIDATION_FIELDS]` - Map of fields to validate
- `[VALIDATION_RULES]` - Specific validation logic
- `[TRANSACTION_CONTROL]` - Transaction management logic