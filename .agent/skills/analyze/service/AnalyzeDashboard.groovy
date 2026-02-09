import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// 1. Validate Inputs
String path = context.path ?: "huddle"
String renderMode = context.renderMode ?: "aria"

// 2. Render the Screen (Mocking the logic for now, or calling internal API)
// In a real implementation, this would call the ScreenRenderService or similar.
// For now, we stub it to match the previous bash script's intent, assuming internal access.

// TODO: Replace with actual ScreenRender call
def screenRender = ec.service.sync().name("org.moqui.impl.ScreenServices.renderScreenPath")
    .parameters([rootScreenLocation: "component://webroot/screen/webroot.xml", screenPathList: path.split('/'), renderMode: renderMode])
    .call()

String ariaTree = screenRender?.textOut ?: "Error: Could not render screen."

// 3. Return Output
result = [
    ariaTree: ariaTree
]
