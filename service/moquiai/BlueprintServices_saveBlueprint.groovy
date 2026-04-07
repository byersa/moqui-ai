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
try {
    def runtimePath = ec.factory.runtimePath
    
    // Resolve subfolder and filename
    def subFolder = "screen"
    def fileName = screenPath
    if (screenPath.contains("/")) {
        def parts = screenPath.split("/")
        subFolder = parts[0]
        fileName = parts[1]
    }
    
    def targetDir = new File(runtimePath, "component/${componentName}/${subFolder}")
    def targetFile = new File(targetDir, "${fileName}.json")
    def shadowFile = new File(targetDir, "${fileName}.xml")
    
    // Ensure parent directories exist
    targetDir.mkdirs()
    
    // Save JSON Source of Truth
    // Save JSON Source of Truth
    targetFile.text = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(normalizedMap))
    
    // Generate XML Shadow if it doesn't exist (Moqui legacy compatibility)
    if (!shadowFile.exists()) {
        def xmlContent = ""
        switch(subFolder) {
            case "screen":
                xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">
    <!-- Shadow for ${fileName}.json -->
    <widgets>
        <label text="Rendering via Aitree Blueprint..."/>
    </widgets>
</screen>"""
                break
            case "entity":
                xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<entities xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/entity-definition-3.xsd">
    <!-- Shadow for ${fileName}.json -->
</entities>"""
                break
            case "service":
                xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<services xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-definition-3.xsd">
    <!-- Shadow for ${fileName}.json -->
</services>"""
                break
        }
        if (xmlContent) shadowFile.text = xmlContent
    }
    
    // 4. Trigger SSE Broadcast for potential hot-reload
    context.success = true
    context.message = "Blueprint saved successfully to component://${componentName}/${subFolder}/${fileName}.json"
    
    ec.service.async().name("moquiai.WebMcpServices.push#BlueprintUpdate")
        .parameters([componentName: componentName, screenPath: screenPath])
        .call()
} catch (Exception e) {
    context.success = false
    context.message = "Error saving blueprint: " + e.message
}

return context
