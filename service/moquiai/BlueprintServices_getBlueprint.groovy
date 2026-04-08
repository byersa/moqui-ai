import org.moqui.context.ExecutionContext
import groovy.json.JsonSlurper

ExecutionContext ec = context.ec

def subFolder = "screen"
def fileName = screenPath
if (screenPath.contains("/")) {
    def parts = screenPath.split("/", 2)
    subFolder = parts[0]
    fileName = parts[1]
}

ec.logger.info("MCE Load: requesting ${componentName}/${subFolder}/${fileName}.json")

// 1. Try component:// (Standard Moqui registered components)
def location = "component://${componentName}/${subFolder}/${fileName}.json"
def fileRef = ec.resource.getLocationReference(location)

// 2. Fallback to file:// (For on-the-fly created components not yet in Moqui's component list)
if (!fileRef || !fileRef.getExists()) {
    def runtimePath = ec.factory.runtimePath
    def fileLocation = "file:${runtimePath}/component/${componentName}/${subFolder}/${fileName}.json"
    fileRef = ec.resource.getLocationReference(fileLocation)
}

if (fileRef && fileRef.getExists()) {
    try {
        def raw = new JsonSlurper().parseText(fileRef.getText())
        // Cleanup: Ensure it's a plain map, not a lazy map or entity value
        context.blueprint = raw instanceof Map ? new HashMap(raw) : [:]

        // Ensure Shadow XML exists (Required for some Moqui render paths)
        def jsonFile = new File(fileRef.getLocation().replace("file:", ""))
        def xmlFile = new File(jsonFile.absolutePath.replace(".json", ".xml"))
        if (!xmlFile.exists()) {
            xmlFile.text = """<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">
    <widgets><label text="Aitree Visual Shell Shadow Artifact"/></widgets>
</screen>"""
            ec.logger.info("Created shadow XML for ${screenPath}: ${xmlFile.absolutePath}")
        }
    } catch (Exception e) {
        ec.logger.error("Failed to parse blueprint JSON at ${fileRef.getLocation()}: ${e.message}")
        context.blueprint = [meta: [title: "Error Parsing JSON", error: e.message]]
    }
} else {
    // Default empty blueprint if file not found
    context.blueprint = [
        meta: [title: fileName, intent: "empty", hipaa_audit: true]
    ]
}

return context
