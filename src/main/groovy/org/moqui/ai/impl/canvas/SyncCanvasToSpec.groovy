import org.moqui.resource.ResourceReference

try {
    // Input parameters: specPath (the .md spec location), widgetId, newX, newY
    def mdRef = ec.resource.getLocationReference(specPath)
    if (!mdRef || !mdRef.getExists()) {
        ec.web.sendJsonResponse([status: "error", message: "Spec file not found at ${specPath}"])
        return
    }
    String content = mdRef.getText()
    
    // Safety check: only replace the first occurrence to avoid corrupting Markdown documentation text
    // Enabling Dot-all mode (?s) to handle multi-line XML tags or DSL blocks
    
    // 1. Precise Match (Update if already exists)
    // DSL: name: 'x' ... location: [x: ..., y: ...]
    def dslLocationRegex = /(?s)(name\s*:\s*["']${widgetId}["'][^)]*?)\s*location\s*:\s*\[x:.*?, y:.*?\]/
    // XML: <... name='x' ... location='...' />
    def xmlLocationRegex = /(?s)(<[^>]*name\s*=\s*["']${widgetId}["'][^>]*?)\s*location\s*=\s*["']\[x:.*?, y:.*?\]["']/
    
    String updatedContent = ""
    if (content =~ dslLocationRegex) {
        updatedContent = content.replaceFirst(dslLocationRegex, "\$1 location: [x: ${newX}, y: ${newY}]")
    } else if (content =~ xmlLocationRegex) {
        updatedContent = content.replaceFirst(xmlLocationRegex, "\$1 location=\"[x: ${newX}, y: ${newY}]\"")
    } else {
        // 2. Append if missing (Restrict to tag/block structure using ?s for multiline)
        // DSL match: must have 'name:' before the ID
        def dslMatchRegex = /(?s)(name\s*:\s*["']${widgetId}["'][^)]*)/
        // XML match: must have a tag opener '<' and name attribute before the ID
        def xmlMatchRegex = /(?s)(<[^>]*name\s*=\s*["']${widgetId}["'][^>\/]*)/
        
        if (content =~ dslMatchRegex) {
            updatedContent = content.replaceFirst(dslMatchRegex, "\$1 location: [x: ${newX}, y: ${newY}]")
        } else if (content =~ xmlMatchRegex) {
            updatedContent = content.replaceFirst(xmlMatchRegex, "\$1 location=\"[x: ${newX}, y: ${newY}]\"")
        } else {
            ec.web.sendJsonResponse([status: "error", message: "Widget ID ${widgetId} not found in code blocks of spec."])
            return
        }
    }

    if (content != updatedContent) {
        mdRef.putText(updatedContent)
        ec.web.sendJsonResponse([status: "success", message: "Spec updated successfully."])
    } else {
        ec.web.sendJsonResponse([status: "error", message: "No change made to spec content."])
    }
} catch (Exception e) {
    ec.logger.error("SyncCanvasToSpec Error: " + e.message, e)
    ec.web.sendJsonResponse([status: "error", message: "Internal Sync Error: " + e.message])
}