import org.moqui.context.ExecutionContext
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

ExecutionContext ec = context.ec

String prompt = context.prompt
String componentName = context.componentName
String screenPath = context.screenPath
String selectedId = context.selectedId
Map selectedProps = context.selectedProps

ec.logger.info("WebMcpServices_postPrompt received: prompt='${prompt}', component='${componentName}', screen='${screenPath}'")

// Clean up Java Metadata from Maps before saving to ensure JSON purity
def cleanMap(obj) {
    if (obj instanceof Map) {
        def clean = new LinkedHashMap()
        obj.each { k, v -> clean.put(k.toString(), cleanMap(v)) }
        return clean
    } else if (obj instanceof List) {
        return obj.collect { cleanMap(it) }
    }
    return obj
}

// FETCH LATEST BLUEPRINT FOR CONTEXT
def getRes = ec.service.sync().name("moquiai.BlueprintServices.get#Blueprint")
    .parameters([componentName: componentName, screenPath: screenPath]).call()
Map blueprint = getRes.blueprint ?: [structure: [], meta: [intent: ""]]
String projectIntent = blueprint.meta?.intent ?: ""

// 1. MOCK THE AI "THINKING" (The Mantle Suggester)
String aiResponseText = "ARCHITECT: I received your request but I'm not sure how to model it yet. Could you be more specific?"
String lowerPrompt = (prompt ?: "").trim().toLowerCase()
ec.logger.info("Normalized prompt: [${lowerPrompt}]")

// ONBOARDING TRIGGER (INIT)
if (lowerPrompt == "init") {
    def structSize = (blueprint.structure instanceof List) ? blueprint.structure.size() : -1
    ec.logger.info("INIT branch matched! Structure size: ${structSize}")
    
    if (structSize <= 0) {
        aiResponseText = "ARCHITECT: This is a fresh blueprint for '${screenPath}'. Shall we start with a header, or are we building a 'Data Entry' form for a Mantle Entity?"
    } else {
        aiResponseText = "ARCHITECT: Welcome back. We have ${structSize} components active. How should we proceed?"
    }
} else if (lowerPrompt.contains("data entry") || lowerPrompt.contains("form")) {
    aiResponseText = "ARCHITECT: Excellent. Which Mantle Entity are we tracking with this form? (e.g., Person, Facility, Asset, Order)"
} else if (lowerPrompt.contains("person") && (lowerPrompt.contains("intake") || lowerPrompt.contains("tracking"))) {
    // METADATA EXPLORER: Suggest common fields for mantle.party.Person
    aiResponseText = JsonOutput.toJson([
        action: "addMultipleComponents",
        components: [
            [ id: "firstName", component: "text-line", properties: [ label: "First Name", bind: "mantle.party.Person.firstName" ] ],
            [ id: "lastName", component: "text-line", properties: [ label: "Last Name", bind: "mantle.party.Person.lastName" ] ],
            [ id: "birthDate", component: "date-picker", properties: [ label: "Birth Date", bind: "mantle.party.Person.birthDate" ] ]
        ],
        message: "ARCHITECT: I've suggested a standard Person intake structure (First Name, Last Name, Birth Date). Use 'Confirm' to bulk inject."
    ])
} else if (lowerPrompt.contains("add") && (lowerPrompt.contains("birth date") || lowerPrompt.contains("birthday") || lowerPrompt.contains("birthdate"))) {
    aiResponseText = JsonOutput.toJson([
        action: "addComponent",
        component: "date-picker",
        id: "birthDate",
        properties: [ label: "Resident Birth Date", bind: "mantle.party.Person.birthDate" ]
    ])
} else if (lowerPrompt.contains("resident name") || lowerPrompt.contains("person name")) {
    aiResponseText = JsonOutput.toJson([
        action: "addComponent",
        component: "text-line",
        id: "residentName",
        properties: [ 
            label: "Resident Full Name", 
            bind: "mantle.party.Person.firstName", 
            required: true 
        ],
        message: "ARCHITECT: Added a 'Resident Name' field with a mandatory validation rule. (Shadow XML sync'd)"
    ])
} else if (lowerPrompt.contains("confirm") || lowerPrompt.contains("yes") || lowerPrompt.contains("ok")) {
    aiResponseText = "ARCHITECT: Strategic proposal confirmed. I have committed the structural updates to the '${screenPath}' blueprint. (Shadow XML sync'd)"
} else if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi")) {
    aiResponseText = "ARCHITECT: Hello! I am your AI Architectural Peer. How can I help you modify the '${screenPath}' blueprint today?"
} else {
    aiResponseText = "ARCHITECT: I received your request but I'm not sure how to model it yet. Could you clarify if we are adding a single component or modeling a new entity?"
}

// 2. THE "COMMAND PARSER" logic (recognizes structured response)
String responseMsg = aiResponseText

if (aiResponseText.trim().startsWith("{")) {
    try {
        def aiAction = new JsonSlurper().parseText(aiResponseText)
        
        if (aiAction.action == "addMultipleComponents") {
            if (!blueprint.structure) blueprint.structure = []
            aiAction.components.each { Map newItem ->
                boolean exists = blueprint.structure.any { it.id == newItem.id }
                if (!exists) {
                    blueprint.structure.add([
                        id: newItem.id,
                        component: newItem.component,
                        properties: cleanMap(newItem.properties ?: [:]),
                        children: cleanMap(newItem.children ?: [])
                    ])
                }
            }
            def saveRes = ec.service.sync().name("moquiai.BlueprintServices.save#Blueprint")
                .parameters([componentName: componentName, screenPath: screenPath, blueprint: blueprint]).call()
            responseMsg = aiAction.message ?: "ARCHITECT: Bulk injected ${aiAction.components.size()} components."
            
        } else if (aiAction.action == "addComponent") {
            if (!blueprint.structure) blueprint.structure = []
            
            boolean exists = blueprint.structure.any { it.id == aiAction.id }
            if (!exists) {
                // FORCE CLEAN MAPS to prevent jdk_map_althashing_sysprop contamination
                blueprint.structure.add([
                    id: aiAction.id,
                    component: aiAction.component,
                    properties: cleanMap(aiAction.properties ?: [:]),
                    children: cleanMap(aiAction.children ?: [])
                ])
                
                def saveRes = ec.service.sync().name("moquiai.BlueprintServices.save#Blueprint")
                    .parameters([componentName: componentName, screenPath: screenPath, blueprint: blueprint]).call()
                
                if (saveRes.success) {
                    responseMsg = aiAction.message ?: "ARCHITECT: Added '${aiAction.id}' component to the structure."
                } else {
                    responseMsg = "ARCHITECT ERROR: Failed to save: ${saveRes.message}"
                }
            } else {
                responseMsg = "ARCHITECT NOTICE: Component '${aiAction.id}' already exists."
            }
        }
    } catch (Exception e) {
        ec.logger.error("Failed to parse/execute AI action: ${e.message}", e)
        responseMsg = "ARCHITECT FATAL: Exception: ${e.message}"
    }
}

ec.logger.info("Aitree AI Prompt: ${prompt} (Final Result: [${responseMsg}])")
context.aiResponse = responseMsg
result.aiResponse = responseMsg
return result // Explicitly returning the result map containing our payload
