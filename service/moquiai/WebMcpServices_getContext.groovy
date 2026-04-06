import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// 1. Fetch current HIPAA encryption defaults from schema
def schemaLoc = "component://moqui-ai/schema/blueprint-schema.json"
def schemaText = ec.resource.getLocationText(schemaLoc, false)
def schema = schemaText ? new groovy.json.JsonSlurper().parseText(schemaText) : [:]

context.hipaaRules = [
    default_encryption: true,
    schema_required: schema?.required ?: [],
    audit: true
]

// 2. Resolve Macro Registry (placeholder or look for existing)
// In a real MCE, this would be the results of the RegistryServices scan.
context.macros = [
    "form-list": [component: "QTable", description: "Standard list form"],
    "display-field": [component: "QInput", description: "Read-only field"],
    "text-field": [component: "QInput", description: "Editable text field"]
]

// 3. Entity Metadata Explorer (The 'Brain' for the AI)
def targetEntity = "mantle.party.Person"
def ed = ec.entity.getEntityDefinition(targetEntity)
def entityMeta = [:]
if (ed) {
    ed.getFieldNames().each { fieldName ->
        def fieldNode = ed.getFieldNode(fieldName)
        entityMeta[fieldName] = [
            type: fieldNode?.attribute("type") ?: "unknown",
            isPk: ed.isPkField(fieldName),
            encrypt: fieldNode?.attribute("encrypt") == "true"
        ]
    }
}
context.entityMetadata = [
    (targetEntity): entityMeta
]

if (ec.logger.isInfoEnabled()) ec.logger.info("WebMCP Context: Knowledge Bundle generated for ${context.componentName}")

return context
