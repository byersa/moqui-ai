import org.moqui.context.ExecutionContext
import java.util.concurrent.LinkedBlockingQueue

ExecutionContext ec = context.ec
def sseCache = ec.cache.getCache("blueprint-sse-listeners")

if (sseCache != null) {
    def commandMessage = groovy.json.JsonOutput.toJson([
        action: context.action,
        payload: context.payload,
        timestamp: System.currentTimeMillis()
    ])
    // Prefix so registerClient knows to map it to the 'command' EventSource listener.
    def payloadString = "COMMAND:" + commandMessage
    
    int count = 0
    sseCache.each { entry ->
        def queue = entry.value
        if (queue instanceof LinkedBlockingQueue) {
            queue.offer(payloadString)
            count++
        }
    }
    
    if (ec.logger.isInfoEnabled()) ec.logger.info("WebMCP Execute: Pushed internal command '${context.action}' to ${count} listeners.")
}

return context
