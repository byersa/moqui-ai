# Moqui AI Platform Component

`moqui-ai` is a platform-level Moqui component designed to bridge the gap between Moqui Framework and agentic AI (using Antigravity and Moqui MCP). It provides the essential infrastructure for modern, AI-generated Single Page Applications (SPAs).

## Key Features for Moqui Developers
- **DeterministicVueRenderer**: A specialized `ScreenWidgetRender` implementation that produces a semantic JSON-LD "Blueprint" of screen widgets instead of raw HTML. This allows a client-side Vue/Quasar application to render the UI while preserving Moqui's server-side logic and security.
- **Enhanced Screen Macros**: Overrides standard Moqui HTML macros with Quasar 2 equivalent components (e.g., `q-btn`, `q-input`). Includes new high-level declarative tags like `<menu-item>` and `<menu-dropdown>`.
- **SPA App Shell**: Provides a unified Freemarker/Vue.js wrapper (`Quasar2Wrapper.qvt2.ftl`) that handles client-side routing via `vue-router` while adhering to the Moqui screen path structure.
- **Agent Intelligence (`.agent/`)**: Contains structured rules, design patterns, and "instructions-as-code" that allow AI agents to understand and build within the Moqui ecosystem accurately.
