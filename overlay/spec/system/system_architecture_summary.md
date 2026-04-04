# Meta-Description: Moqui-AI Visual Orchestrator Architecture

**Target Audience**: AI Brainstorming Agents / Large Language Models
**System Context**: A Low-Code/No-Code visual design environment for the Moqui Framework and Quasar UI.

---

## 1. The Core Architecture: "Blueprint as Source of Truth"
The system is built on the principle that a **Markdown-based Specification (`.md`)** is the definitive source for both the visual design and the production execution of a screen.

### The Source (`.md` file)
- Contains human-readable requirements (text, lists).
- Contains **Machine-Readable DSL blocks** (fenced XML or Groovy blocks).
- Example: `Home.md` contains the structural layout for the dashboard.

---

## 2. The Execution Pipeline (MD to UI)
1. **Extraction**: A Groovy-based engine (`ScreenBuilder.xml`) reads the Markdown and extracts the delimited XML/Groovy blocks.
2. **Transformation**: These blocks are parsed into a **Normalized JSON UI Tree**, which represents the screen's architecture (widgets, attributes, and relationships).
3. **Distribution**: This JSON (`screenDataJson`) is served via a Moqui JSON (`.qjson`) endpoint to the client-side orchestrator.
4. **Hydration**: 
   - In **Architect Mode**, the JSON is rendered onto a **Konva.js Canvas** (`moqui-canvas-editor`), where nodes are represented by interactive visual blocks.
   - In **Production Mode**, the JSON is rendered into a **Live Vue 3 Component Tree** (`m-blueprint-node`), where nodes are mapped to real Quasar widgets (`q-card`, `q-btn`, etc.).

---

## 3. The Dual-Mode Viewport (`m-architect-view-port`)
We use a global Vue component that intelligently toggles between two high-fidelity views based on a shared Pinia state (`AiTreeStore.isArchitectMode`).

- **Production Mode (Live Preview)**: A premium "Live Badge" interface. Logic for loading dynamic panels (like split-screens) is executed here using `m-dynamic-container`.
- **Architect Mode (Visual Canvas)**: An interactive design environment. It uses specialized color-coding (e.g., Orange for splitters, Azure for containers) to help the architect navigate the hierarchy.

---

## 4. Bi-Directional Synchronization
- **Canvas to Spec**: Dragging a node in the Architect View triggers a `syncCanvas` AJAX call to a Moqui service (`SyncCanvasToSpec.groovy`). This service performs a regex-based string replacement in the original `.md` file to update the `location="[x, y]"` attribute without destroying the human notes or other DSL blocks.
- **Spec to Canvas**: Updating the `.md` file (manually or via AI) instantly updates the JSON output, causing the Visual Canvas to "hot-reload" its nodes.

---

## 5. Implementation Status: Home.md
As of now, the `Home.md` file has been reverse-engineered from the original `Home.xml`. 
- **The Architect Mode** can now "see" the `Home` dashboard layout.
- **The Production Mode** can "live preview" that same layout.
- **Brainstorming Goal**: Directing the AI to process `Home.md` should focus on adding complex interactive widgets (e.g., real-time feed containers, search bars) into the fence-delimited XML blocks while maintaining a clean, human-style documentation format in the markdown.

---

## 6. Key Context for the Brainstorming Agent
When advising on `Home.md` processing, consider:
- **Widget Mapping**: Ensuring new tags (like `screen-split`) are mapped to their Quasar equivalents in `BlueprintClient.js`.
- **Parameter Passing**: How variables like `agendaContainerId` are prop-drilled through the blueprint tree.
- **State Management**: Using the Pinia store to cross-communicate between the canvas and the live panels.
