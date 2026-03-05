# MCP AI Integration Bug Fixes - 2026-03-04

We completed testing the end-to-end integration between Moqui and a local Ollama agent (`qwen2.5-coder:7b`). During testing of the `moqui-mcp` module, we discovered several bugs that impacted its ability to receive and process REST requests, properly compile service arguments, and interface with the caching/background threading.

### Changes Migrated to `moqui-ai`

Since `moqui-mcp` is an upstream / separate module, we decoupled the deployment-specific files from it:

1. **REST Definition (`agent.rest.xml`)**:
   - The REST endpoint mapping that exposes `AgentServices.process#LLMRequest` was moved out of `moqui-mcp` and placed in `moqui-ai/service/agent.rest.xml`.

2. **Security & Artifact Registration (`AgentSecurityData.xml`)**:
   - Creating the `AgentRestPaths` artifact group and authorizing access by `ADMIN` and `McpUser` was explicitly abstracted into `moqui-ai/data/AgentSecurityData.xml`. This prevents us from having to hardcode security modifications into `McpSecuritySeedData.xml` directly.

### Changes Retained in `moqui-mcp` (Pending Upstream Merge)

We left patches in `moqui-mcp`'s core files because they represent legitimate bugs in Ean's code. These have been exported to `moqui-mcp-bug-fixes.patch` so they can be communicated to the maintainer later.

**`service/AgentServices.xml`**
- **Invalid Types:** Core parameters like `productStoreId` and `systemMessageId` were set to `type="id"`. In Moqui XML actions, `id` is not a valid Groovy/Java type class, causing `500 java.lang.IllegalArgumentException: Cannot find class for type: id`. We mutated all `type="id"` entries to `type="String"`.
- **Method Signature Bug:** Validation code was using `ec.service.isServiceExists()`. We upgraded to the valid `ec.service.isServiceDefined()` to avoid `400 Bad Request` exceptions.

**`screen/McpTestScreen.xml`**
- **Deprecated WebFacade Methods**: Properties such as `ec.web?.sessionId` and `ec.web?.webappName` were throwing `MissingPropertyException / MissingMethodException`. We migrated to modern equivalents: `ec.web?.session?.id` and `ec.web?.getWebappMoquiName()`.
- **Testing Utility**: Added a `<transition name="loadData">` to allow direct injection of setup seed data from within the test screen.

### Result
The background queue `AgentQueuePoller` correctly picks up processing tasks, invokes Ollama locally, and successfully persists context back to Moqui.
