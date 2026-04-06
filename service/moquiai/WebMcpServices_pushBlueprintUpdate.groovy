import org.moqui.context.ExecutionContext
import groovy.json.JsonOutput
import java.util.concurrent.LinkedBlockingQueue

ExecutionContext ec = context.ec

String componentName = context.componentName
String screenPath = context.screenPath
String screenKey = "${componentName}:${screenPath}"

// 1. Send the ping to all registered SSE queues in the Moqui cache
def sseCache = ec.cache.getCache("blueprint-sse-listeners")

if (sseCache != null) {
    def updateMessage = JsonOutput.toJson([
        event: "update",
        screen: screenPath,
        component: componentName,
        timestamp: System.currentTimeMillis()
    ])

    int count = 0
    sseCache.each { entry ->
        def sessionId = entry.key
        def queue = entry.value
        // For this phase, we push the update to all active blueprint listeners.
        if (queue instanceof LinkedBlockingQueue) {
            queue.offer(updateMessage)
            count++
        }
    }
    
    if (ec.logger.isInfoEnabled()) ec.logger.info("Blueprint Broadcast: Pushed update for ${screenKey} to ${count} active listeners.")
} else {
    if (ec.logger.isDebugEnabled()) ec.logger.debug("Blueprint Broadcast: No active listeners for ${screenKey}")
}

return context
