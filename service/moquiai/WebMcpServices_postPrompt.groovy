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

// 1. DOMAIN MAP (Blueprint Suggester)
Map domainMap = [
    allergies: [
        entity: "mantle.party.MedicalCondition",
        message: "ARCHITECT: I'm modeling an Allergies section using the MedicalCondition entity pattern. Should I include 'Severity' and 'Reaction Type' fields?",
        suggestion: [
            action: "addMultipleComponents",
            components: [
                [ id: "allergyHeader", component: "header", properties: [ text: "Patient Allergies" ] ],
                [ id: "allergyName", component: "text-line", properties: [ label: "Allergen", bind: "mantle.party.MedicalCondition.description", required: true ] ],
                [ id: "severityLevel", component: "text-line", properties: [ label: "Severity (Low/High)", bind: "mantle.party.MedicalCondition.statusId" ] ]
            ]
        ]
    ],
    silverston_allergy: [
        entity: "mantle.party.MedicalCondition",
        source: "moqui-medical-lib",
        message: "ARCHITECT: pulling Silverston pattern for Allergy/MedicalCondition from 'moqui-medical-lib'. Commit these 3 library-standard fields?",
        suggestion: [
            action: "addMultipleComponents",
            components: [
                [ id: "allergyLibHeader", component: "header", properties: [ text: "Standardized Allergy Intake" ] ],
                [ id: "rootConditionEnumId", component: "text-line", properties: [ label: "Allergy Category", bind: "mantle.party.MedicalCondition.rootConditionEnumId", required: true ] ],
                [ id: "onsetDate", component: "date-picker", properties: [ label: "Onset Date", bind: "mantle.party.MedicalCondition.onsetDate" ] ],
                [ id: "severityEnumId", component: "text-line", properties: [ label: "Severity Level", bind: "mantle.party.MedicalCondition.severityEnumId" ] ]
            ]
        ]
    ],
    prescriptions: [
        entity: "mantle.product.asset.Asset",
        message: "ARCHITECT: Modeling Prescriptions via the Asset entity. Should I include 'Dosage' and 'Frequency' instructions?",
        suggestion: [
            action: "addMultipleComponents",
            components: [
                [ id: "medHeader", component: "header", properties: [ text: "Active Medications" ] ],
                [ id: "medName", component: "text-line", properties: [ label: "Medication Name", bind: "mantle.product.asset.Asset.assetName", required: true ] ],
                [ id: "dosageInst", component: "text-line", properties: [ label: "Dosage Instructions", bind: "mantle.product.asset.Asset.quantityOnHandTotal" ] ]
            ]
        ]
    ],
    vitals: [
        entity: "mantle.party.PersonDetail",
        message: "ARCHITECT: Mapping Vitals to the PersonDetail health profile. Shall I include 'Blood Pressure' and 'Weight' fields?",
        suggestion: [
            action: "addMultipleComponents",
            components: [
                [ id: "vitalsHeader", component: "header", properties: [ text: "Clinical Vitals" ] ],
                [ id: "bpField", component: "text-line", properties: [ label: "Blood Pressure", bind: "mantle.party.PersonDetail.height" ] ],
                [ id: "weightField", component: "text-line", properties: [ label: "Current Weight", bind: "mantle.party.PersonDetail.weight" ] ]
            ]
        ]
    ]
]

String lowerPrompt = (prompt ?: "").toLowerCase().trim()
String aiResponseText = ""
ec.logger.info("Normalized prompt: [${lowerPrompt}]")

// Logic Path Detection
def matchedDomain = domainMap.find { k, v -> 
    String key = k.toString().replace("_", " ")
    lowerPrompt.contains(key) || lowerPrompt.contains(k.toString()) 
}

if (!matchedDomain && lowerPrompt.contains("silverston")) {
    matchedDomain = domainMap.entrySet().find { it.key.startsWith("silverston") }
}

if (matchedDomain) {
    def domainData = matchedDomain.value instanceof Map ? matchedDomain.value : matchedDomain
    ec.logger.info("Matched domain: ${matchedDomain.key}. Action: ${domainData.suggestion?.action}")
    aiResponseText = JsonOutput.toJson([
        action: domainData.suggestion.action,
        components: domainData.suggestion.components,
        message: domainData.message
    ])
} else if (lowerPrompt.contains("init") || lowerPrompt.contains("start") || lowerPrompt === "") {
    def structSize = (blueprint.structure instanceof List) ? blueprint.structure.size() : -1
    if (structSize <= 0) {
        aiResponseText = "ARCHITECT: This is a fresh blueprint for '${screenPath}'. Shall we start with a header, or are we building a 'Data Entry' form for a Mantle Entity?"
    } else {
        aiResponseText = "ARCHITECT: Welcome back. We have ${structSize} components active. How should we proceed?"
    }
} else if (lowerPrompt.contains("data entry") || lowerPrompt.contains("form") || lowerPrompt.contains("mantle")) {
    aiResponseText = "ARCHITECT: Excellent. Which Mantle Entity are we tracking with this form? (e.g., Person, Facility, Asset, Order)"
} else if (lowerPrompt.contains("resident name") || lowerPrompt.contains("person name")) {
    aiResponseText = JsonOutput.toJson([
        action: "addComponent",
        component: "text-line",
        id: "residentName",
        properties: [ label: "Resident Full Name", bind: "mantle.party.Person.firstName", required: true ],
        message: "ARCHITECT: Added a 'Resident Name' field with a mandatory validation rule. (Shadow XML sync'd)"
    ])
} else if (lowerPrompt.contains("person") && (lowerPrompt.contains("intake") || lowerPrompt.contains("tracking"))) {
    aiResponseText = JsonOutput.toJson([
        action: "addMultipleComponents",
        components: [
            [ id: "firstName", component: "text-line", properties: [ label: "First Name", bind: "mantle.party.Person.firstName", required: true ] ],
            [ id: "lastName", component: "text-line", properties: [ label: "Last Name", bind: "mantle.party.Person.lastName", required: true ] ],
            [ id: "birthDate", component: "date-picker", properties: [ label: "Birth Date", bind: "mantle.party.Person.birthDate", required: true ] ]
        ],
        message: "ARCHITECT: I've suggested a standard Person intake structure (First Name, Last Name, Birth Date). Use 'Confirm' to bulk inject."
    ])
} else if (lowerPrompt.contains("confirm") || lowerPrompt.contains("yes") || lowerPrompt.contains("ok")) {
    aiResponseText = "ARCHITECT: Strategic proposal confirmed. I have committed the structural updates to the '${screenPath}' blueprint. (Shadow XML sync'd)"
} else if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi")) {
    aiResponseText = "ARCHITECT: Hello! I am your AI Architectural Peer. How can I help you modify the '${screenPath}' blueprint today?"
} else if (screenPath.startsWith("service/") && (lowerPrompt.contains("parameter") || lowerPrompt.contains("logic") || lowerPrompt.contains("step"))) {
    // SERVICE LOGIC MODELER
    if (lowerPrompt.contains("output") || lowerPrompt.contains("out-parameter")) {
        def name = lowerPrompt.split(" ").find { it != "parameter" && it != "output" && it != "as" && it != "add" && it != "than" && it != "specify" }?.replace("\"", "") ?: "result"
        aiResponseText = JsonOutput.toJson([
            action: "updateServiceMetadata",
            type: "outParameter",
            id: name,
            message: "ARCHITECT: Added '${name}' as an output parameter to the service contract. (Shadow XML sync'd)"
        ])
    } else if (lowerPrompt.contains("input") || lowerPrompt.contains("in-parameter")) {
        def name = lowerPrompt.split(" ").find { it != "parameter" && it != "input" && it != "as" && it != "add" && it != "specify" }?.replace("\"", "") ?: "input"
        aiResponseText = JsonOutput.toJson([
            action: "updateServiceMetadata",
            type: "inParameter",
            id: name,
            message: "ARCHITECT: Added '${name}' as an input parameter to the service contract. (Shadow XML sync'd)"
        ])
        } else if (lowerPrompt.contains("explain")) {
            // Dynamically build an explanation from the blueprint we fetched at line 30
            def srvName = blueprint.meta?.title ?: "this service"
            def actionCount = (blueprint.actions ?: []).size()
            def paramCount = (blueprint.parameters?.in ?: []).size()
    
            aiResponseText = "ARCHITECT: I am analyzing ${srvName}. It currently defines ${paramCount} input parameters and ${actionCount} logic steps in the pipeline. How would you like me to refactor this flow?"
    } else {
        aiResponseText = "ARCHITECT: I see you want to modify the service logic. Should we add an 'Input Parameter', 'Output Parameter', or a 'Logic Action' like an 'entity-create'?"
    }
} else {
    aiResponseText = "ARCHITECT: I am not sure how to model that domain yet. Should we focus on 'Allergies', 'Vitals', or 'Person Intake'?"
}

// 2. THE "COMMAND PARSER" logic (recognizes structured response)
String responseMsg = aiResponseText

if (aiResponseText.trim().startsWith("{")) {
    try {
        def aiAction = new JsonSlurper().parseText(aiResponseText)
        ec.logger.info("[AI PARSER] Processing action: ${aiAction.action}")
        
        if (aiAction.action == "addMultipleComponents") {
            if (!blueprint.structure) blueprint.structure = []
            int addedCount = 0
            aiAction.components.each { Map newItem ->
                boolean exists = blueprint.structure.any { it.id == newItem.id }
                if (!exists) {
                    blueprint.structure.add([
                        id: newItem.id,
                        component: newItem.component,
                        properties: cleanMap(newItem.properties ?: [:]),
                        children: cleanMap(newItem.children ?: [])
                    ])
                    addedCount++
                }
            }
            ec.logger.info("[AI PARSER] Bulk injecting ${addedCount} (new) components.")
            def saveRes = ec.service.sync().name("moquiai.BlueprintServices.save#Blueprint")
                .parameters([componentName: componentName, screenPath: screenPath, blueprint: blueprint]).call()
            
            responseMsg = aiAction.message ?: "ARCHITECT: Bulk injected ${addedCount} components."
            
        } else if (aiAction.action == "addComponent") {
            if (!blueprint.structure) blueprint.structure = []
            
            boolean exists = blueprint.structure.any { it.id == aiAction.id }
            if (!exists) {
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
        } else if (aiAction.action == "updateServiceMetadata") {
            if (aiAction.type == "inParameter") {
                if (!blueprint.inparameters) blueprint.inparameters = []
                boolean exists = blueprint.inparameters.any { it.name == aiAction.id }
                if (!exists) {
                    blueprint.inparameters.add([ name: aiAction.id, type: "String", required: false ])
                }
            } else if (aiAction.type == "outParameter") {
                if (!blueprint.outparameters) blueprint.outparameters = []
                boolean exists = blueprint.outparameters.any { it.name == aiAction.id }
                if (!exists) {
                    blueprint.outparameters.add([ name: aiAction.id, type: "String" ])
                }
            }
            
            ec.logger.info("[AI PARSER] Updating Service Metadata: ${aiAction.type} -> ${aiAction.id}")
            def saveRes = ec.service.sync().name("moquiai.BlueprintServices.save#Blueprint")
                .parameters([componentName: componentName, screenPath: screenPath, blueprint: blueprint]).call()
            
            if (saveRes.success) {
                responseMsg = aiAction.message ?: "ARCHITECT: Service metadata updated."
            } else {
                responseMsg = "ARCHITECT ERROR: Failed to save logic: ${saveRes.message}"
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
