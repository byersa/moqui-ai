import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { SSEServerTransport } from "@modelcontextprotocol/sdk/server/sse.js";
import express from "express";

const app = express();
const server = new McpServer({
    name: "Moqui-AI-Architect",
    version: "1.0.0"
});

// 1. Define the 'Tool' that the AI uses to change your screen
server.tool("executeCommand", {
    action: "string",
    payload: "object",
    componentName: "string",
    screenPath: "string"
}, async ({ action, payload, componentName, screenPath }) => {
    // This calls your WebMcpServices.xml -> post#Command service
    const response = await fetch("http://localhost:8080/rest/s1/moquiai/postCommand", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ action, payload, componentName, screenPath })
    });
    return { content: [{ type: "text", text: `Command ${action} pushed to Moqui.` }] };
});

// 2. Establish the SSE transport bridge
app.get("/sse", async (req, res) => {
    const transport = new SSEServerTransport("/messages", res);
    await server.connect(transport);
});

app.post("/messages", async (req, res) => {
    // Handle incoming JSON-RPC messages from the AI
});

app.listen(3000, () => {
    console.log("Moqui-AI MCP Host running on http://localhost:3000");
});