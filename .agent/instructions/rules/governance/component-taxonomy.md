# Governance: Component Taxonomy

To ensure clarity in architecture and dependency management, all components within the MoquiAi ecosystem are classified into two distinct types.

## 1. Platform Components
**Definition**: Fundamental building blocks that provide infrastructure, tools, services, or entities used by other components. They are "enablers" and typically do not deliver a standalone end-user business solution.
*   **Role**: Infrastructure / Framework / Library.
*   **Examples**:
    *   `moqui-ai` (The Agentic Framework).
    *   `moqui-mcp` (Model Context Protocol bridge).
    *   `mantle-usl` (Universal Data Model).
    *   `moqui-framework` (The Core Runtime).
*   **Rule**: Platform components should NOT depend on Solution components.

## 2. Solution Components
**Definition**: The "End Product" built to solve a specific business problem. These components consume Platform components to deliver value to the user.
*   **Role**: Application / Product / Vertical.
*   **Examples**:
    *   `huddle` (Healthcare Coordination App).
    *   `popc` (Point of Care Application).
*   **Rule**: Solution components MAY depend on Platform components and other Solution components (if modular).

## Naming Convention
*   **Platform**: Often prefixed with `moqui-` or `mantle-` (e.g., `moqui-ai`).
*   **Solution**: Named after the product or domain (e.g., `huddle`).
