# Deep Dive: MoquiAi Screen Macro Extensions

The MoquiAi project extends the standard Moqui XML Screen DSL (via `moqui-ai-screen.xsd`) to create an **Interactive Bridge** between Moqui’s server-side logic and modern reactive frontends. This strategy is critical for making Moqui applications "Agent-Ready."

## 1. The Strategy: Instructions-as-UI
Standard Moqui HTML rendering produces complex DOM trees that are difficult for AI agents to reason about. The MoquiAi macro extensions solve this by:
- **Declarative Intent**: Using semantic tags (e.g., `<screen-header>`, `<form-query>`) instead of generic `<div>` or `<span>` blocks.
- **Blueprint Emission**: The `DeterministicVueRenderer` transforms these macros into **JSON-LD Blueprints**, which provide a clean, structured representation of the UI for both the Vue client and the AI agent.

---

## 2. Key Macro Patterns

### A. The `<form-query>` Pattern
In traditional Moqui, search forms are often tightly coupled to the table rendering. The `<form-query>` macro creates a standalone, client-side filtering container.
- **Functionality**: Defines a set of search fields (`<form-query-field>`) that sync with a `form-list`. 
- **Agent Benefit**: When an AI sees a `<form-query>`, it immediately knows exactly which parameters can be filtered and which transitions (`options-url`) yield valid search criteria.
- **Implementation**: It supports `enum-type-id` and `status-type-id` directly, allowing the AI to populate dropdowns without complex entity-find boilerplate.

### B. The State Bridge: `<bp-parameter>`
The biggest hurdle in building SPAs with Moqui is syncing the server-side `ec.context` with the client-side **Pinia store**.
- **The Bridge**: `<bp-parameter>` takes a server-side value (e.g., `${agendaContainerId}`) and maps it directly to a named field in a specific Pinia store (e.g., `useMeetingsStore.activeContainerId`).
- **Interactive Benefit**: This allows the AI to "know" the state of the application in the browser and manipulate it by updating the store, which in turn triggers reactive UI updates.

### C. Layout & Responsive Grid
- **`<container-row>` and `<row-col>`**: These map Moqui’s logical structure directly to Quasar’s flex grid. 
- **`<screen-split>`**: A resizable splitter that supports dynamic component loading. This is ideal for "Master-Detail" views where the AI needs to swap out content on the right panel based on a selection on the left.

---

## 3. Semantic Mapping with `semantic-handle`
Nearly every custom macro supports a `semantic-handle` attribute.
- **Concept**: Links a UI element to a known Mantle entity, an AI intent, or a "domain concept" from the `moqui-agent-os` references.
- **Example**: `<screen-content semantic-handle="PatientClinicalDashboard">`.
- **Outcome**: The AI agent doesn't just see a "page container"; it sees a **Clinical Dashboard** with specific expected behaviors and data patterns.

---

## 4. Integration with Community Strategy
While Jens’s `moqui-agent-os` provides the **Foundational OS** (patterns for entities/services) and Hans provides **Prompting Workflows** (how to talk to the AI), these macro extensions provide the **Implementation DSL**.

| Component | Role |
| :--- | :--- |
| **`moqui-agent-os`** | The "Grammar" (Standards and Guides). |
| **Hans's Work** | The "Communication Style" (Pattern Reference Prompting). |
| **MoquiAi Macros** | The "Vocab" (Declarative UI tags and State Bridging). |

## 5. Conclusion
Extending the Moqui Screen DSL is not an aesthetic choice; it is a **structural necessity** for AI collaboration. By using these macros, developers can build UIs that are natively "Agent-Readable." The AI no longer has to guess where a button is or how a search works—it interacts with the DSL, and the `DeterministicVueRenderer` handles the complex bridging to the browser.
