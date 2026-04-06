import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec
String sessionId = ec.web.session.getId()
def tokenCache = ec.cache.getCache("mcp-connection-tokens")
if (tokenCache == null) {
    tokenCache = ec.cache.makeCache("mcp-connection-tokens")
}

String token = tokenCache.get(sessionId)
if (!token) {
    token = java.util.UUID.randomUUID().toString()
    // Optional: bind to active user if logged in, but UUID is unique anyway
    tokenCache.put(sessionId, token)
}

context.connectionToken = token
return context
