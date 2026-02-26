# MoquiAi Strategy: Blueprints & WebMCP Integration

## Executive Summary of Recent Conversations
This document summarizes the strategic evolution of the MoquiAi platform over the recent development cycle (February 2026), moving from a static rendering framework to an agentic, metadata-driven architecture.

### 1. Platform Taxonomy and Structure
The project established a clear separation between **Platform Components** (`moqui-ai`, `moqui-mcp`) and **Solution Components** (`huddle`, `aitree`). The `.agent` directory in `moqui-ai` was refactored to serve as a "compiled" source of truth for agent behavior and project strategy, moving beyond simple task lists.

### 2. The Birth of MoquiAi Blueprints
To solve the friction between server-side Moqui logic and modern Vue/Quasar frontends, the **MoquiAi Blueprint** system was developed. 
- **Core Engine:** `BlueprintClient.js` and the `DeterministicVueRenderer`.
- **Functionality:** Declarative XML tags (e.g., `<screen-layout>`, `<screen-header>`) are transformed into JSON-LD "Blueprints" on the server and rendered into reactive Quasar components on the client.
- **Outcome:** This provides a high-fidelity, SPA-like experience while maintaining the power of Moqui’s backend transition and security model.

### 3. Domain Model Evolution
Strategic work was performed on the meeting management system within the `aitree` and `huddle` solutions:
- **Agenda Management:** Transitioned to an EAV-style attribute model for `AgendaMessageAttribute`.
- **Meeting Navigation:** Implemented a two-level navigation system for "Active Meetings" vs. "Meeting History," keyed by `AgendaContainerID` and normalized with `fromDate`/`thruDate`.

### 4. Convergence: WebMCP & Blueprints
The most recent shift identifies **WebMCP** as the key to making MoquiAi "Zero-Day Agent Ready." By using Blueprints as the source of truth, the platform can automatically expose client-side tools to browser-native AI agents (like Edge Copilot or Gemini), allowing for seamless human-in-the-loop collaboration.

---

# Investigation Report: WebMCP & MoquiAi Blueprints

## Overview of WebMCP
[WebMCP](https://github.com/webmachinelearning/webmcp) is a proposed JavaScript API that allows web developers to expose their web application's functionality as "tools" to AI agents and assistive technologies directly within the browser. 

Unlike traditional out-of-band MCP integrations where an AI communicates directly with a backend server, WebMCP allows tools to execute on the *client side* within the context of the user's active session. This enables **human-in-the-loop collaborative workflows** where the human and the AI share the same visual interface and active state.

## Overview of MoquiAi Blueprints
Based on the codebase analysis, **MoquiAi Blueprints** (e.g., `BlueprintClient.js`) dynamically render UI components in the browser by transforming declarative JSON-LD representations (`ScreenBlueprint`, `FormList`, `FormField`, etc.) into reactive Vue/Quasar components. Blueprints serve as the bridge between Moqui's backend data definitions and the user's dynamic frontend experience.

## Potential Synergies and Integration Strategies

WebMCP is uniquely suited to complement the MoquiAi Blueprints architecture. Because blueprints already provide a dynamic, metadata-driven UI framework, they can easily be extended to automatically generate and register WebMCP tools for AI agents.

Here is how WebMCP could be used in conjunction with the "blueprints" work:

### 1. Declarative Tool Registration from Blueprints
Since the backend already sends structured JSON-LD blueprints to describe the UI, these blueprints could be extended to include WebMCP tool definitions. 
- **Implementation:** When `BlueprintClient.js` parses a `BlueprintNode`, it could look for an `@tools` or `agentCapabilities` property. It would then use the WebMCP API to register JavaScript functions that wrap the Vue component's methods.
- **Example:** A `FormList` showing active meetings could automatically expose a `filterMeetings(topic)` or `selectMeeting(id)` tool to the browser's AI agent.

### 2. Form and Action Automation
Moqui screens heavily rely on forms (`FormSingle`, `submit`, `text-line`). Filling out complex forms is a prime use case for AI assistance.
- **Implementation:** Whenever a form blueprint is rendered, a generic WebMCP tool like `fillForm(formId, data)` or `submitForm(formId)` could be exposed. 
- **Benefit:** Instead of the agent relying on brittle DOM actuation (guessing CSS selectors and typing), the agent can pass a structured JSON object directly to the client-side form manager, letting Vue handle the reactivity and validation, while the human watches the inputs populate in real-time.

### 3. Agent-Driven Navigation and Routing
Blueprints handle multi-level navigation (like the recent `SubscreensMenu` additions). 
- **Implementation:** Expose a `navigateBlueprint(route)` tool via WebMCP. 
- **Benefit:** If the user asks their browser agent, "Show me my past huddle meetings," the agent can invoke this tool directly. The Vue router will process the navigation, bringing both the human and the agent to the new blueprint screen concurrently.

### 4. Shared Context in "Human-in-the-Loop" Workflows
The WebMCP explainer highlights "creative" or "shopping" collaborative workflows. For MoquiAi applications like Huddle (which handles meeting agendas, observations, etc.), users and agents could collaborate.
- **Example Flow:** The user is looking at a `ScreenBlueprint` of a patient discussion. They ask the browser agent, "Summarize the last 3 meetings and draft a new agenda item." The agent fetches the data (via an exposed `getPastMeetings()` tool), drafts the text, and calls `addAgendaItemDraft(text)`. The Vue UI instantly updates with the drafted text, allowing the user to review, edit, and click "Save".

## Conclusion
Integrating WebMCP with MoquiAi Blueprints would bridge the gap between human-first UIs and agent-first APIs without duplicating effort. By adding WebMCP hook generation into the `BlueprintClient.js` renderer, **every Moqui screen built with blueprints would automatically become an interactive, agent-ready playground**. The AI interacts safely with the frontend architecture, while the user maintains final visual verification and control over the app state.
