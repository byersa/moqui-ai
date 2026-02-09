# MoquiAi General Direction & Architecture

## Vision: The Moqui + Agent Hybrid
MoquiAi is not just an application *built on* Moqui; it is the **successor** to Moqui. It fundamentally reimagines the framework by embedding Agentic AI directly into the runtime and development lifecycle.

### Core Philosophy
- **Hybrid Intelligence**: The system is aware of both deterministic business logic (Moqui services/entities) and probabilistic agentic reasoning (LLMs).
- **"Living" Applications**: Apps created in MoquiAi are not static; they include an "Agent Layer" that observes, assists, and optimizes user workflows in real-time.
- **Self-Documenting & Self-Marketing**: The application knows *what* it is and can generate its own marketing materials, documentation, and sales collateral.

## 1. Translator Strategy (The "Bridge")
A robust migration path for existing Moqui and OFBiz applications.
- **Goal**: Convert legacy XML screens/forms and Java/Groovy services into modern MoquiAi equivalents (Vue components, AI-enhanced services).
- **Mechanism**:
    - **AST Transformation**: Parse original XML/Java to an abstract syntax tree.
    - **LLM Refinement**: Use LLMs to "reason" about intent where direct translation is ambiguous (e.g., converting a complex OFBiz minilang-service to a clean Groovy method).
    - **Gradual Adoption**: Support "hybrid" apps where legacy screens run alongside new MoquiAi screens during transition.

## 2. Divergence from Standard Moqui
MoquiAi will diverge to support modern, AI-first paradigms.
- **Screen Tags 2.0**:
    - New tags specifically for AI interaction (e.g., `<ai-assistant-output>`, `<smart-form-field>`).
    - Deprecation of server-side heavy rendering in favor of client-side data binding.
- **Client-Side First**:
    - **Vue/Quasar native**: Move away from jQuery/macro-heavy rendering.
    - **Client-Side Execution Context**: A lightweight JS-based context (similar to standard `ec`) to manage state, user preferences, and AI interactions on the client.
- **AI as a First-Class Citizen**:
    - "AI Agents" are deployable components, just like Services or Screen definitions.

## 3. Automated Marketing & Business Artifacts
MoquiAi apps should generate their own "Business Wrapper".
- **Concept**: Metadata in the app definition drives external content generation.
- **Artifacts Generated**:
    - **Landing Page Content**: SEO-optimized copy describing features based on the underlying service definitions.
    - **Schema.org Metadata**: Automatically injected JSON-LD for products, services, and organization details.
    - **License Agreements**: Generated based on configured business rules.
    - **Commerce Strategy**: Toggles for "SaaS Mode" (subscriptions) vs "Purchase Mode" (one-time), generating appropriate billing logic and pricing pages automatically.

## 4. Suggestions for Improvement
- **"Agent Bus" Architecture**: Implement an event bus where AI agents can subscribe to specific user events (e.g., "User opened Invoice #123") and proactively offer context-sensitive actions ("Draft reminder email?").
- **Dynamic Micro-Frontends**:Allow AI-generated components to be injected into the UI at runtime without a full redeploy, enabling "instant" feature prototyping by the agent.
- **Unified Knowledge Graph**: Instead of just a database, maintain a semantic Knowledge Graph of the application's domain, which powers both the AI agents and the marketing content generation.
