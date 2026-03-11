# Implementing WebMCP in the Moqui Ecosystem

This report details the integration of **WebMCP** within a Moqui environment. It explains how WebMCP extends the core **moqui-mcp** infrastructure to provide a bi-directional communication bridge between Moqui's server-side logic and the user's browser context.

## 1. The Architecture: moqui-mcp vs. webmcp

To understand the system, we must distinguish between the two core components:

### A. moqui-mcp (The Backend)
Provides the server-side infrastructure for AI interaction.
- **EnhancedMcpServlet**: Exposes standard MCP endpoints (`/mcp/sse`, `/mcp/message`).
- **Semantic Rendering**: Includes custom FTL templates that transform Moqui screens into AI-optimized Markdown/JSON structures (Unified Semantic Layer).
- **Security**: Implements `McpAuthFilter` to protect endpoints.

### B. webmcp (The Frontend Bridge)
Provides the browser-side presence and the communication bridge.
- **webmcp.js**: A client-side library that renders the interactive AI widget (the "Blue Square") and handles browser-specific tool execution (DOM reading, screenshotting, navigation).
- **websocket-server.js**: A Node.js bridge that allows external AI agents to connect to the browser session using secure tokens.

---

## 2. Installation Instructions

### Step 1: Component Dependencies
Ensure both `moqui-mcp` and `moqui-ai` are present in your `runtime/component` directory.

### Step 2: Global Resource Mounting
The modern way to install WebMCP is to mount it as a global subscreen in your root `MoquiConf.xml`. This ensures that JavaScript resources and Pinia stores are available across all Moqui components (e.g., HiveMind, PopCommerce, Aitree).

Add the following to your `runtime/component/moqui-ai/MoquiConf.xml`:

```xml
<screen-facade>
    <!-- Mount webmcp resources into the standard webroot context -->
    <screen location="component://webroot/screen/webroot.xml">
        <subscreens-item name="moqui-ai" menu-include="false"
                         location="component://moqui-ai/screen/moquiai.xml"/>
    </screen>
</screen-facade>
```

### Step 3: Serving JS Resources
Modify `moqui-ai/screen/moquiai.xml` to serve nested JavaScript files (required for modular stores and utilities):

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        allow-extra-path="true">
    <transitions>
        <transition name="js">
            <parameter name="extraPathNameList"/>
            <actions>
                <script>
                    def path = sri.screenUrlInfo.extraPathNameList.join("/")
                    ec.web.sendResourceResponse("component://moqui-ai/screen/moquiai/js/${path}")
                </script>
            </actions>
            <default-response type="none"/>
        </transition>
    </transitions>
</screen>
```

### Step 4: UI Initialization
To activate the WebMCP widget on a screen, include the `webmcp.js` script. For a global application (like a Vue 3/Quasar app), add this to your PreActions or Main screen:

```groovy
// In AitreePreActions.groovy or similar
def webmcpUrl = "/moqui-ai/js/webmcp.js"
if (footer_scripts_list != null) footer_scripts_list.add(webmcpUrl)
```

### Step 5: Start the Bridge Server
WebMCP requires a small Node.js process to bridge the AI agent to the browser:

```bash
cd runtime/component/moqui-ai/screen/moquiai/js
node websocket-server.js --mcp
```

---

## 3. Benefits of WebMCP

Going forward, the WebMCP + moqui-mcp combination provides several strategic advantages for the Moqui community:

### 1. Unified Semantic Layer
By using Moqui's `screen-text-output` logic (provided by `moqui-mcp`), the AI doesn't just "see" HTML. It receives a semantic representation of the screen—entities, fields, and actions—making code generation and debugging significantly more accurate.

### 2. "Eyes and Ears" for the AI
Standard MCP servers are blind. They can only see the data the server sends them. **WebMCP gives the AI vision.** It can:
- Capture screenshots to verify UI layout.
- Inspect the live DOM to see how Vue/Quasar rendered a component.
- "Push" actions into the browser (like opening a specific modal or tab) to guide the developer.

### 3. Decoupled Tooling
Because WebMCP uses a token-based WebSocket bridge, developers can use a variety of AI clients (VS Code, web-based LLMs, custom agents) to collaborate on the same browser session without modifying Moqui's core security model.

### 4. Component Portability
By mounting WebMCP at the `webroot` level, any component in the Moqui ecosystem can immediately benefit from AI-assisted debugging and interaction by simply referencing the global `/moqui-ai/js/` path.

---

## 4. Conclusion
WebMCP is not just a widget; it is the **Interactive Development Environment (IDE) for the AI Era** inside Moqui. By bridging the gap between Moqui's rich entity-driven backend and the modern reactive frontend, it enables a level of AI pairing that was previously impossible.
