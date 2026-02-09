import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec

// 1. Validate Inputs
if (!inputName) {
    ec.message.addError("Missing required input: inputName")
    return
}

// 2. Prepare Context (e.g. fetch data)
def data = [:]

// 3. Call LLM (Optional)
// def llmResult = ec.service.call("ai.core.Generate", [prompt: ...])

// 4. Return Output
result = [
    outputName: "Result value"
]
