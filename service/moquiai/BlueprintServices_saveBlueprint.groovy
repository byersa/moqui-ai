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

// 2. Logic: Process and Update
// If selectedId provided, we perform a targeted update on either structure or actions
if (context.selectedId && context.properties) {
    def targetId = context.selectedId
    def newProps = context.properties
    boolean isParamUpdate = context.isParameter ?: false

    if (isParamUpdate || screenPath.startsWith("service/")) {
        // Update Logic Actions
        def actions = normalizedMap.actions ?: []
        def targetAction = actions.find { it.id == targetId }
        if (targetAction) {
            if (isParamUpdate) {
                if (!targetAction.parameters) targetAction.parameters = [:]
                targetAction.parameters.putAll(newProps)
            } else {
                targetAction.putAll(newProps)
            }
            ec.logger.info("Service Logic Updated: Action ${targetId}")
        }
    } else {
        // Update UI Components
        def findAndStats
        findAndStats = { list ->
            for (comp in list) {
                if (comp.id == targetId) {
                    if (!comp.properties) comp.properties = [:]
                    comp.properties.putAll(newProps)
                    return true
                }
                if (comp.children && findAndStats(comp.children)) return true
            }
            return false
        }
        findAndStats(normalizedMap.structure ?: [])
    }
}

// HIPAA metadata bridge (Entity field detection)
def processHipaa(structure) {
    if (!structure) return
    structure.each { Map compNode ->
        Map componentProps = (Map) compNode.get("properties")
        if (componentProps != null) {
            boolean hasMantleRef = componentProps.any { k, v -> v instanceof String && v.startsWith("mantle.") }
            if (hasMantleRef && !componentProps.get("encrypt")) {
                componentProps.put("encrypt", true)
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
    // type (subFolder) is passed from caller or inferred from path
    def subFolder = context.type ?: "screen"
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
    
    // Save JSON Source of Truth (using JsonOutput for cleaner baseline serialization)
    targetFile.text = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(normalizedMap))
    
    // Generate XML Shadow (Compiler Edition)
    // For service mode, we always re-generate to reflect logic changes
    if (!shadowFile.exists() || subFolder == "service") {
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
                def inParams = normalizedMap.inparameters ?: []
                def outParams = normalizedMap.outparameters ?: []
                def actions = normalizedMap.actions ?: []

                def inXml = inParams.collect { "            <parameter name=\"${it.name}\" type=\"${it.type ?: 'String'}\" required=\"${it.required ?: false}\"/>" }.join("\n")
                def outXml = outParams.collect { "            <parameter name=\"${it.name}\" type=\"${it.type ?: 'String'}\"/>" }.join("\n")
                def actionsXml = actions.collect { act ->
                    def attrs = act.parameters ? act.parameters.collect { k, v -> " ${k}=\"${v}\"" }.join("") : ""
                    "            <${act.type ?: 'script'}${attrs}/>" 
                }.join("\n")

                xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<services xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-definition-3.xsd">
    <service verb="call" noun="${fileName}">
        <in-parameters>
${inXml}
        </in-parameters>
        <out-parameters>
${outXml}
        </out-parameters>
        <actions>
${actionsXml}
        </actions>
    </service>
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
