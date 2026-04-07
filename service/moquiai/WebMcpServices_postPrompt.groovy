import org.moqui.context.ExecutionContext
import groovy.json.JsonOutput

ExecutionContext ec = context.ec

// Basic AI Mock Response Logic
String responseMsg = "Ready to assist."
String lowerPrompt = (prompt ?: "").toLowerCase()
String label = (selectedProps?.label ?: "").toString().toLowerCase()

if (lowerPrompt.contains("ssn") || label.contains("ssn")) {
    responseMsg = "I see you're adding a Social Security Number. Should I apply the 'sensitive-field' macro and enable HIPAA auditing for this?"
} else if (lowerPrompt.contains("resident first name")) {
    responseMsg = "Label updated to 'Resident First Name'."
} else if (lowerPrompt.contains("first name")) {
    responseMsg = "I see you're updating the name field. Would you like me to auto-bind this to a Party entity?"
}

ec.logger.info("Aitree AI Prompt: ${prompt} (Selected ID: ${selectedId}, Selected Label: ${label})")

return [result: responseMsg]
