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
    def subFolder = context.type ?: "screen"
    def fileName = screenPath
    if (screenPath.contains("/")) {
        def parts = screenPath.split("/", 2)
        subFolder = parts[0]
        fileName = parts[1]
    }

    def targetComp = componentName
    def targetBase = fileName
    def targetSrv = null

    if (subFolder == "service" && fileName.contains(".")) {
        def dots = fileName.split("\\.")
        if (dots.length >= 3) {
            targetComp = dots[0]
            targetBase = dots[1]
            targetSrv = dots[2]
        }
    }

    def targetDir = new File(runtimePath, "component/${targetComp}/${subFolder}")
    def targetFile = new File(targetDir, "${targetBase}.json")
    def shadowFile = new File(targetDir, "${targetBase}.xml")
    
    // Ensure parent directories exist
    targetDir.mkdirs()

    def finalOutputMap = normalizedMap

    // If we are targeting a specific service in a multi-service file
    if (targetSrv && targetFile.exists()) {
        try {
            def originalRaw = new JsonSlurper().parseText(targetFile.text)
            if (originalRaw instanceof Map) {
                if (originalRaw.services) {
                    // Update existing entry
                    int idx = originalRaw.services.findIndexOf { s -> 
                         (s.serviceName == targetSrv) || ("${s.verb}${s.noun}" == targetSrv) || ("${s.verb}#${s.noun}" == targetSrv)
                    }
                    if (idx >= 0) {
                        originalRaw.services[idx] = normalizedMap
                    } else {
                        originalRaw.services.add(normalizedMap)
                    }
                } else {
                    // Single service file being updated or converted?
                    // For now, if it's single, we just overwrite as we are editing it.
                    // But if it's a named service being added to a formerly single file, 
                    // maybe we should convert? 
                    // The directive says "operate on a definition", so we just replace.
                    originalRaw = normalizedMap
                }
                finalOutputMap = originalRaw
            }
        } catch (Exception e) { ec.logger.error("Error patching multi-service JSON: ${e.message}") }
    }
    
    // Save JSON Source of Truth (using JsonOutput for cleaner baseline serialization)
    targetFile.text = groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(finalOutputMap))
    
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
                def serviceList = normalizedMap.services ?: [normalizedMap]
                def servicesXml = ""

                serviceList.each { srv ->
                    def verb = srv.verb ?: "call"
                    def noun = srv.noun ?: fileName
                    def typeAttr = srv.type ? " type=\"${srv.type}\"" : ""
                    
                    def inParams = srv.inparameters ?: srv.parameters?.in ?: []
                    def outParams = srv.outparameters ?: srv.parameters?.out ?: []
                    def actions = srv.actions ?: []

                    def inXml = inParams.collect { p -> 
                        if (p instanceof Map && p.containsKey("auto-parameters")) {
                            def ap = p["auto-parameters"]
                            return "            <auto-parameters entity-name=\"${ap['entity-name']}\" include=\"${ap['include'] ?: 'all'}\"/>"
                        }
                        "            <parameter name=\"${p.name}\" type=\"${p.type ?: 'String'}\" required=\"${p.required ?: false}\"/>" 
                    }.join("\n")
                    
                    def outXml = outParams.collect { p -> "            <parameter name=\"${p.name}\" type=\"${p.type ?: 'String'}\"/>" }.join("\n")
                    
                    def recursiveActions
                    recursiveActions = { actList, indent ->
                        actList.collect { act ->
                            if (act.containsKey("service-call")) {
                                def sc = act["service-call"]
                                def inMap = sc["in-map"] ? " in-map=\"${sc['in-map']}\"" : ""
                                def outMap = sc["out-map"] ? " out-map=\"${sc['out-map']}\"" : ""
                                return "${indent}<service-call name=\"${sc.name}\"${inMap}${outMap}/>"
                            } else if (act.containsKey("set")) {
                                def s = act["set"]
                                def from = s.from ? " from=\"${s.from}\"" : ""
                                def value = s.value ? " value=\"${s.value}\"" : ""
                                return "${indent}<set field=\"${s.field}\"${from}${value}/>"
                            } else if (act.containsKey("iterate")) {
                                def it = act["iterate"]
                                def inner = recursiveActions(it.actions ?: [], indent + "    ")
                                return "${indent}<iterate list=\"${it.list}\" entry=\"${it.entry}\">\n${inner}\n${indent}</iterate>"
                            } else if (act.containsKey("if")) {
                                def i = act["if"]
                                def inner = recursiveActions(i.actions ?: [], indent + "    ")
                                return "${indent}<if condition=\"${i.condition}\">\n${inner}\n${indent}</if>"
                            } else {
                                // Default fallback for generic attributes
                                def tagName = act.type ?: "script"
                                def attrs = act.findAll { k, v -> k != 'type' && k != 'actions' }.collect { k, v -> " ${k}=\"${v}\"" }.join("")
                                return "${indent}<${tagName}${attrs}/>"
                            }
                        }.join("\n")
                    }
                    def actionsXml = recursiveActions(actions, "            ")

                    servicesXml += """    <service verb="${verb}" noun="${noun}"${typeAttr}>
        <in-parameters>
${inXml}
        </in-parameters>
        <out-parameters>
${outXml}
        </out-parameters>
        <actions>
${actionsXml}
        </actions>
    </service>\n"""
                }

                xmlContent = """<?xml version="1.0" encoding="UTF-8"?>
<services xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-definition-3.xsd">
${servicesXml}</services>"""
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
