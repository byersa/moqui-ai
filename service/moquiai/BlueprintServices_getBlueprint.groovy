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

def location = "component://${componentName}/${subFolder}/${fileName}.json"
def fileRef = ec.resource.getLocationReference(location)

if (fileRef.getExists()) {
    context.blueprint = new JsonSlurper().parseText(fileRef.getText())
} else {
    // Default empty blueprint if file not found
    context.blueprint = [
        meta: [title: fileName, intent: "empty", hipaa_audit: true]
    ]
}

return context
