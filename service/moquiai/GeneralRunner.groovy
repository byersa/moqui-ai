import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec

// Handle CORS pre-flight OPTIONS requests
if (ec.web && ec.web.request.method == "OPTIONS") {
    def response = ec.web.response
    def origin = ec.web.request.getHeader("Origin")
    
    // Explicit whitelist for HIPAA compliance
    def allowedOrigins = ["http://localhost:8080", "http://localhost:9000"]
    if (origin && allowedOrigins.contains(origin)) {
        response.addHeader("Access-Control-Allow-Origin", origin)
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, X-CSRF-Token")
        response.addHeader("Access-Control-Allow-Credentials", "true")
        response.setStatus(200)
    }
    return [:]
}

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
