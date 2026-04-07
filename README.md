# Moqui AI Platform Component

`moqui-ai` is a platform-level Moqui component designed to bridge the gap between Moqui Framework and agentic AI. It provides the essential infrastructure for modern, AI-generated SPAs and automated Mantle UDM integration.

## Key Features for Moqui Developers

### 1. Deterministic Semantic Rendering
- **DeterministicVueRenderer**: A specialized `ScreenWidgetRender` that produces a semantic JSON "Blueprint" instead of raw HTML. This allows the AI to "read" the screen structure as data rather than parsing messy DOM elements.
- **Aitree Macro Registry**: Maps high-level semantic components (e.g., `text-line`, `sensitive-field`) to Quasar 2 equivalents. This decouples business intent from UI implementation.

### 2. Intelligent Mantle Integration
- **Deep Metadata Explorer**: Automatically inspects Mantle UDM entities to identify primary keys, data types, and security requirements (e.g., `encrypt="true"`).
- **Automated Entity Hydration**: A generic data-binding layer that automatically populates UI components from Mantle entities using secure, server-side REST services.
- **HIPAA-Ready by Default**: Native enforcement of encryption and audit-logging based on Moqui entity definitions.

### 3. The MCE (Model Context Environment)
- **Live-Streaming Architect**: A three-pane IDE shell (Navigator | Preview | Inspector) powered by a non-blocking Server-Sent Events (SSE) bridge.
- **Bi-Directional State**: Property changes made in the UI Inspector persist to the server-side Blueprint and immediately re-sync across all connected clients.
- **Agentic Command Pipe**: A dedicated WebMCP-style channel allowing AI agents to "beam" new components and services directly into the Moqui environment.

## Agent Intelligence (`.agent/`)
Contains structured rules, design patterns, and "instructions-as-code" that allow AI agents to understand and build within the Moqui ecosystem accurately. This includes:
- **Nursing Home Domain Rules**: Standardized patterns for Resident Intake, Clinical Data, and HIPAA enforcement.
- **Service Generation Patterns**: Templates for creating CRUD services that adhere to Mantle UDM standards.