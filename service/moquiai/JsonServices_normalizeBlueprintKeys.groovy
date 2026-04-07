import org.moqui.context.ExecutionContext
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
// Note: Attempting to use networknt validator as requested
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage

ExecutionContext ec = context.ec

// 1. Logic: Recursive normalization (Keys to lowercase)
def normalizeKeys(input) {
    if (input instanceof Map) {
        if (input.containsKey("map") && input.size() <= 2) {
             // Corruption recovery: unwrap redundant moqui/jdk map wrappers
             return normalizeKeys(input.get("map"))
        }
        Map normalizedResult = new LinkedHashMap()
        input.each { k, v ->
            String keyStr = k.toString().toLowerCase()
            if (keyStr != "jdk_map_althashing_sysprop") {
                normalizedResult.put(keyStr, normalizeKeys(v))
            }
        }
        return normalizedResult
    } else if (input instanceof List) {
        return input.collect { normalizeKeys(it) }
    } else {
        return input
    }
}

Map blueprintMap = context.blueprintMap
if (!blueprintMap) {
    ec.message.addError("No blueprintMap provided")
    return
}

Map normalizedMap = normalizeKeys(blueprintMap)
context.normalizedMap = normalizedMap

// 2. Validation against blueprint-schema.json
try {
    def schemaLocation = "component://moqui-ai/schema/blueprint-schema.json"
    def schemaRef = ec.resource.getLocationReference(schemaLocation)
    if (!schemaRef.getExists()) {
        ec.message.addError("Schema not found: ${schemaLocation}")
        return
    }
    String schemaContent = schemaRef.getText()

    // Use Jackson to parse into JsonNode for the validator
    ObjectMapper mapper = new ObjectMapper()
    JsonNode schemaNode = mapper.readTree(schemaContent)
    JsonNode dataNode = mapper.valueToTree(normalizedMap)

    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
    def schema = factory.getSchema(schemaNode)
    
    Set<ValidationMessage> errors = schema.validate(dataNode)

    if (errors.isEmpty()) {
        context.success = true
        context.message = "Blueprint normalized and validated successfully."
    } else {
        context.success = false
        context.message = "Validation failed: " + errors.collect { it.message }.join("; ")
        ec.message.addError(context.message)
    }
} catch (NoClassDefFoundError | ClassNotFoundException e) {
    // If the library is missing, provide a clear error message or try a fallback
    context.success = false
    context.message = "JSON Schema validator library (networknt) not found in classpath. Please add dependency. Error: ${e.message}"
    ec.message.addError(context.message)
} catch (Exception e) {
    context.success = false
    context.message = "Error during validation: ${e.message}"
    ec.message.addError(context.message)
}

return context
