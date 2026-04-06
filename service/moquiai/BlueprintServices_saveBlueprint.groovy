import org.moqui.context.ExecutionContext
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

ExecutionContext ec = context.ec

// 1. Logic: Normalize keys first
def normResult = ec.service.sync().name("moquiai.JsonServices.normalize#BlueprintKeys")
    .parameter("blueprintMap", blueprint).call()

if (!normResult.success) {
    context.success = false
    context.message = "Normalization failed: " + normResult.message
    return
}

Map normalizedMap = normResult.normalizedMap

// 2. Logic: HIPAA metadata bridge (Entity field detection)
// Detects fields that belong to 'mantle' package
def processHipaa(structure) {
    structure.each { Map compNode ->
        Map componentProps = (Map) compNode.get("properties")
        if (componentProps != null) {
            // Check for entity field pattern 'package.Entity.field'
            componentProps.each { key, value ->
                if (value instanceof String && value.startsWith("mantle.")) {
                    // It refers to a Mantle entity field, enforce encryption metadata
                    if (!componentProps.get("encrypt")) {
                        componentProps.put("encrypt", true)
                        ec.logger.info("HIPAA Reinforcement: Added encrypt: true for field ${key} referencing ${value}")
                    }
                }
            }
        }
        def children = compNode.get("children")
        if (children != null && children instanceof List) {
            processHipaa(children)
        }
    }
}

if (normalizedMap.structure) {
    processHipaa(normalizedMap.structure)
}

// 3. Logic: Save to file
def saveLocation = "component://${componentName}/screen/${screenPath}.json"
try {
    // Determine absolute path manually if possible, or use resource façade if supported for writing
    // Moqui ResourceReference for 'component://' location may not be writable by default without special care
    // Better to use File directly if possible to keep it simple for this task
    
    def runtimePath = ec.factory.runtimePath
    def targetFile = new File(runtimePath, "component/${componentName}/screen/${screenPath}.json")
    
    // Ensure parent directories exist
    targetFile.parentFile.mkdirs()
    
    targetFile.text = new JsonBuilder(normalizedMap).toPrettyString()
    
    // 4. Trigger SSE Broadcast for potential hot-reload
    context.success = true
    context.message = "Blueprint saved successfully to ${saveLocation}"
    
    ec.service.async().name("moquiai.WebMcpServices.push#BlueprintUpdate")
        .parameters([componentName: componentName, screenPath: screenPath])
        .call()
} catch (Exception e) {
    context.success = false
    context.message = "Error saving blueprint: " + e.message
}

return context
