import org.moqui.context.ExecutionContext
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

ExecutionContext ec = context.ec

// 1. Check if session exists
File sessionFile = new File("runtime/mcp-session.json")
if (sessionFile.exists()) {
    def cached = new JsonSlurper().parse(sessionFile)
    // TODO: Verify if session is still valid (ping)
    result = [sessionId: cached.sessionId, status: "cached"]
    return
}

// 2. If not, we need to instruct the Agent to run the curl command.
// Since this service runs ON the server, we might be able to do the HTTP call directly?
// But the bridge might be local to the user.
// For now, we return 'missing' to trigger the system prompt fallback.

result = [status: "missing"]
