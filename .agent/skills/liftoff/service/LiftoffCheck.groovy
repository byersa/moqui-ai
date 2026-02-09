import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// Check for critical components
boolean hasMcp = new File("runtime/component/moqui-mcp").exists()
boolean hasBaseScript = new File("start-huddle.sh").exists() // simplified check

def missing = []
if (!hasMcp) missing.add("moqui-mcp")
if (!hasBaseScript) missing.add("start-script")

result = [
    status: missing ? "incomplete" : "ready",
    missingComponents: missing
]
