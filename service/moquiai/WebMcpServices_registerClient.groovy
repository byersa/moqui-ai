import org.moqui.context.ExecutionContext
import java.util.concurrent.LinkedBlockingQueue
import jakarta.servlet.AsyncContext
import jakarta.servlet.AsyncEvent
import jakarta.servlet.AsyncListener

ExecutionContext ec = context.ec
def request = ec.web.request
def response = ec.web.response

// 1. Set SSE Headers
response.setContentType("text/event-stream")
response.setCharacterEncoding("UTF-8")
response.setHeader("Cache-Control", "no-cache")
response.setHeader("Connection", "keep-alive")
response.setHeader("Access-Control-Allow-Origin", "*")

String sessionId = ec.web.session.getId()
String screenKey = "${context.componentName}:${context.screenPath}"

// 2. Register this session's outgoing queue
def sseCache = ec.cache.getCache("blueprint-sse-listeners")
if (sseCache == null) {
    sseCache = ec.cache.makeCache("blueprint-sse-listeners")
}

def queue = new LinkedBlockingQueue<String>()
sseCache.put(sessionId, queue)

def out = response.writer
out.write("event: connected\ndata: {\"sessionId\":\"${sessionId}\"}\n\n")
out.flush()
response.flushBuffer()

// 3. Drop to Async Context (safely overriding Jetty's network timeouts)
def asyncContext = null
if (request.isAsyncSupported()) {
    asyncContext = request.startAsync()
    asyncContext.setTimeout(-1) // Infinite timeout overrides the network severed errors
}

try {
    int pings = 0
    // Run the loop synchronously so Moqui doesn't close the REST stream early
    while (!Thread.currentThread().isInterrupted() && pings < 1200) { // Safety limit ~ 5 hours
        // Check for updates
        String message = queue.poll(5, java.util.concurrent.TimeUnit.SECONDS)
        
        if (message) {
            if (message.startsWith("COMMAND:")) {
                out.write("event: command\ndata: ${message.substring(8)}\n\n")
            } else {
                out.write("event: update\ndata: ${message}\n\n")
            }
            out.flush()
            response.flushBuffer()
        } else {
            // Heartbeat
            out.write(": keep-alive\n\n")
            out.flush()
            response.flushBuffer()
        }
        pings++
    }
} catch (Exception e) {
    if (ec.logger.isDebugEnabled()) ec.logger.debug("SSE Connection closed for ${sessionId}: ${e.message}")
} finally {
    sseCache.remove(sessionId)
    if (asyncContext) {
        try { asyncContext.complete() } catch (Exception ignore) {}
    }
}

return null
