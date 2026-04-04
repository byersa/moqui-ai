# Moqui MCP Integration Protocol

This document serves as the **Source of Truth** for utilizing the `moqui-mcp` (Model Context Protocol) component within the Moqui-AI ecosystem. It defines the interface for intelligent retrieval, secure CRUD operations, and semantic discovery.

## 1. Executive Summary
The `moqui-mcp` component transforms Moqui's internal state into a structured "USB-C for AI" interface. While the **Universal REST Runner** (defined in UTEP) handles the **Push** (writing and compiling code), `moqui-mcp` provides the **Pull**—allowing Antigravity (AGY) to perform intelligent retrieval and secure operations across all entities and services.

## 2. Key Capabilities

### MARIA Render Mode (Moqui AI Rendering Interface for Accessibility)
*   **Purpose:** Provides a JSON-based "accessibility tree" of Moqui screens.
*   **Utility:** Allows AGY to "see" the UI hierarchy, form fields, and layout constraints without parsing complex HTML.
*   **Usage:** When navigating or auditing screens, request the `MARIA` or `ARIA` render modes via the `McpServices.mcp#BrowseScreens` or `McpServices.mcp#RenderScreen` services.

### Entity Access Tools
*   **Purpose:** Enables secure `entity_find`, `create`, and `update` operations.
*   **Security:** Implements full permission validation, essential for handling sensitive data (e.g., HIPAA-compliant resident records).
*   **Tooling:** Use `McpServices.mcp#ResourcesRead` and `McpServices.mcp#ResourcesList` for data discovery.

### Semantic Search & Discovery
*   **Purpose:** Allows searching for business logic and data patterns across the entire framework.
*   **Search Services:**
    *   `McpServices.mcp#SearchScreens`: Locates screens by name or path.
    *   `McpServices.mcp#SearchServices`: Finds logic implemented in Moqui services.
    *   `McpServices.mcp#SearchEntities`: Identifies data models and their field definitions.

## 3. The Moqui-AI Macro Registry

The Macro Registry provides a standardized mapping of UI components for AI code generation.

### Registry Locations
*   **Global Registry:** `runtime/component/moqui-ai/registry.json` (Core Moqui macros).
*   **Local Overrides:** `runtime/component/<app-name>/registry.json` (App-specific specialized macros).

### Effective Registry Resolution
The system uses a merged registry approach. To retrieve the combined set of macros for an app:
1.  Use the `moquiai.RegistryServices.get#EffectiveRegistry` service.
2.  Input Parameter: `app` (e.g., `aitree`).
3.  REST Endpoint: `GET /rest/s1/moquiai/effective-registry?app=<app_name>`.

## 4. Integration with Universal Task Execution Protocol (UTEP)

All `moqui-mcp` service calls should prioritize the **Universal REST Runner** when the Moqui server is active.

### Execution Pattern
*   **Service Name:** `moquiai.AppServices.execute#GeneralRunner`
*   **Target McpServices:**
    *   `McpServices.mcp#SearchScreens`
    *   `McpServices.mcp#ResourcesList`
    *   `McpServices.mcp#ResourcesRead`
    *   `McpServices.mcp#BrowseScreens`
*   **Temp Payloads:** All intermediate results must be written to `runtime/component/moqui-ai/tmp/`.

## 5. Operational Guidelines
- **Zero-Wait:** Use background execution in the shell for long-running discovery tasks (refer to UTEP.md).
- **HIPAA Compliance:** When auditing entities, always check the `encrypt` status of fields in the entity definition before reading or logging data.
- **MARIA First:** Before generating screen specifications, always use MARIA mode to map the hierarchy of existing screens.
