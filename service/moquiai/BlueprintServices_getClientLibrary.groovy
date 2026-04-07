import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// Logic: Use ec.resource.getLocationText() to load the code.
def scriptText = ec.resource.getLocationText("component://moqui-ai/webroot/js/BlueprintClient.js", false)

if (scriptText) {
    // Manual Header Override
    ec.web.response.setContentType("application/javascript")
    ec.web.response.setHeader("Content-Disposition", "inline; filename=\"BlueprintClient.js\"")
    
    // Output: Write the script text directly to the response stream.
    ec.web.sendTextResponse(scriptText, "application/javascript", null)
} else {
    ec.web.response.setStatus(404)
}

return context
