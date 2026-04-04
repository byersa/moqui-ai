import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec

// Security check
def parts = serviceName.split('\\.')
if (parts.length < 2) {
    ec.message.addError("Invalid service name: ${serviceName}")
    return
}
def prefix = parts[0]
if (prefix != "nursinghome" && prefix != "moquiai" && prefix != "McpServices") {
    ec.message.addError("Access denied: service must belong to nursinghome, moquiai, or McpServices nouns")
    return
}

// Execute the service
def serviceResult = ec.service.sync().name(serviceName).parameters(parameters ?: [:]).call()

// Return the result
return serviceResult
