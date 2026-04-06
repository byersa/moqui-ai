import org.moqui.context.ExecutionContext
import groovy.json.JsonSlurper

ExecutionContext ec = context.ec

def location = "component://${componentName}/screen/${screenPath}.json"
def fileRef = ec.resource.getLocationReference(location)

if (fileRef.getExists()) {
    context.blueprint = new JsonSlurper().parseText(fileRef.getText())
} else {
    // Default empty blueprint if file not found
    context.blueprint = [
        meta: [title: screenPath, intent: "empty", hipaa_audit: true],
        structure: []
    ]
}

return context
