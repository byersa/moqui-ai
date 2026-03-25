# Deep Dive: Moqui MCP and the MARIA Semantic Layer

Ean’s **`moqui-mcp`** component provides the core infrastructure for connecting AI agents directly to the Moqui eco-system. The breakthrough insight in this component is the **MARIA** format, which fundamentally changes how we think about "AI-Ready" user interfaces.

---

## 1. The Vision: AI as an Accessibility-Challenged User
Traditional AI-UI interaction (like Playwright or Vision-based models) treats the AI as a "Human Mimic" that scrapes the DOM or looks at screenshots. This is high-latency, expensive, and fragile.

The **MARIA (MCP Accessible Rich Internet Applications)** philosophy flips this:
- **Insight**: AI agents cannot "see" pixels, interpret CSS layouts, or understand visual hierarchy. They are effectively "accessibility-challenged."
- **Solution**: Just as we use ARIA for screen readers, we use **MARIA** to provide a structured **Accessibility Tree** in JSON format.
- **Benefit**: The agent gets pure semantics (Roles, Names, States, Actions) without the "noise" of HTML/CSS.

---

## 2. The MARIA Identifier: Why it is Mandatory
For an agent to navigate a Moqui screen via `moqui-mcp`, every UI artifact MUST have high-fidelity identifiers. 

### Why IDs and Names Matter:
- **Navigation**: Without a clear `name` or `id`, the agent cannot distinguish between multiple instances of a component (e.g., three different "Submit" buttons).
- **Action Binding**: The MCP tool `moqui_browse_screens` relies on MARIA roles (e.g., `button`, `textbox`, `grid`) to understand what actions are possible.
- **Stability**: While CSS classes and HTML structure may change, the MARIA identifier acts as a **Stable Semantic Contract**.

### Developer Requirement:
When building screens or Blueprints in the MoquiAi ecosystem, you MUST decorate your XML and JSON-LD with:
1. **`role`**: What is this? (e.g., `form`, `grid`, `heading`).
2. **`name`**: A unique, human-readable label (e.g., `CreatePersonForm`).
3. **`id`**: A unique machine-readable key (e.g., `partyIdField`).

---

## 3. `moqui-mcp` Architecture
The `moqui-mcp` component is more than just a data exporter; it’s an **Agent Runtime**:
- **JSON-RPC Bridge**: Provides a standardized endpoint for any model (antigravity, GPT, Ollama) to interact with Moqui.
- **Secure Impersonation**: Agents execute tools by impersonating a Moqui user, ensuring that the AI never bypasses existing Permission logic (RBAC).
- **Self-Guided Narratives**: Screens include `uiNarrative` blocks—natural language descriptions that guide the AI on how to use the screen.

---

## 4. The Unified Alignment (The Three-Tier Model)

With the addition of `moqui-mcp`, we now have a complete, cohesive community strategy:

| Tier | Component | Analogy | Responsibility |
| :--- | :--- | :--- | :--- |
| **Logic/Grammar** | `moqui-agent-os` (Jens) | The Brain | Foundation, Entity patterns, technical standards. |
| **Interface/DSL** | `moqui-ai` (Us) | The Body | Blueprints, Macro extensions, WebMCP bridge. |
| **Protocol/Senses** | `moqui-mcp` (Ean) | The Voice | Connectivity, MARIA semantics, Agent impersonation. |

---

## 5. Conclusion: "The Identifier is the Map"
In a MARIA-powered world, **the identifier is the agent's map.** If our UI artifacts don't have clear, semantic identifiers, the agent is effectively "blind." By standardizing on the MARIA format across all Moqui components, we ensure that AI agents can perform "real jobs in real business systems" with the same precision—and security—as a human user.
