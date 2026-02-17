# MoquiAi Repository Structure

**Goal**: Organize the code to facilitate easy adoption, core development, and examples, using a clear **Platform vs. Solution** taxonomy.

## 1. Platform Components (The Foundation)
These components provide the infrastructure and tools. They are dependencies, not end-products.

### `moqui-ai` (Core Platform)
The heart of the system.
-   **Type**: **Platform Component**.
-   **Contents**:
    -   `component.xml`: Defines dependencies on `moqui-framework`.
    -   `screen/`: The Vue/Quasar core library, `jsec` implementation.
    -   `.agent/`: The "System Agent" template (base instructions).
    -   `skills/`: The Standard Library of AI Skills.
    -   `service/`: Backend support services.

### `moqui-mcp` (Bridge)
-   **Type**: **Platform Component**.
-   **Role**: Bridges Moqui with the Model Context Protocol (MCP).

## 2. Solution Components (The End Products)
These are the applications built *on top* of the platform.

### `huddle` (Reference Solution)
-   **Type**: **Solution Component**.
-   **Role**: A production-grade application for Healthcare Coordination. Verified to work with `moqui-ai`.

### `moqui-ai-example` (Learning Solution)
-   **Type**: **Solution Component**.
-   **Role**: A "Kitchen Sink" demo application to showcase features.

## 3. Templates

### `moqui-ai-base` (Starter Kit)
-   **Type**: **Template Repository**.
-   **Usage**: `git clone moqui-ai-base my-new-app`.
-   **Contents**: Pre-configured `build.gradle` and `.agent` structure.

## Relationship Diagram
```mermaid
graph TD
    Runtime[Moqui Runtime] --> Framework[Moqui Framework]
    Runtime --> Core[moqui-ai (Platform)]
    Runtime --> MCP[moqui-mcp (Platform)]
    
    Runtime --> Huddle[Huddle (Solution)]
    Huddle -.-> Core
    Huddle -.-> MCP
    
    Runtime --> Example[Example App (Solution)]
    Example -.-> Core
```
