# Blueprint: Moqui AI Community Alignment Strategy

This document synthesizes the strategic work of the **MoquiAi** project with the foundational patterns of **Jens’s `moqui-agent-os`** and the practical prompting workflows suggested by **Hans**. It serves as a blueprint for unified AI-driven development in the Moqui ecosystem.

## 1. Executive Summary: The Three Pillars of Alignment

To achieve high-fidelity AI collaboration across the Moqui community, we propose a three-pillar approach:
1. **Foundation (The OS):** Standardize on `moqui-agent-os` for core framework patterns and XML/Groovy syntax rules.
2. **Interactive Bridge (The UI):** Standardize on **MoquiAi Macro Extensions** (`form-query`, `bp-parameter`) and **WebMCP** for metadata-driven, agent-ready user interfaces.
3. **Workflow (The Loop):** Adopt **Pattern Reference Prompting** and **Closing-the-Loop Documentation** as the standard developer-AI interaction model.

---

## 2. Comparative Analysis

| Feature | Jens (`moqui-agent-os`) | Hans (Suggestions) | MoquiAi (Blueprints/WebMCP) |
| :--- | :--- | :--- | :--- |
| **Anchoring** | Overlay system & symlinks to `CLAUDE.md`. | Root `CLAUDE.md`/`GEMINI.md` as "Brain". | Internal `.agent` directory with shadowing protocol. |
| **Patterns** | Domain-specific `references/` guides. | "Pattern Reference Prompting" (Mantle UDM). | JSON-LD "Blueprints" for UI consistency. |
| **Iteration** | Universal Task Execution Protocol. | "Closing the Loop" via post-task docs. | "Shadowing" for local vs global logic. |
| **Interaction** | Command-based (slash commands). | High-fidelity CRUD and logic cloning. | WebMCP interactive browser bridge. |

### Points of Convergence
- **Context Management:** All parties emphasize that AI must be "anchored" with project guidelines (`CLAUDE.md`, `README.md`) to prevent hallucinations.
- **Pattern-First logic:** Standardizing on existing "Gold Standards" (like `mantle-udm` or `Example` apps) rather than writing from scratch.
- **Tiered Knowledge:** Recognizing that foundational framework knowledge should be separated from specific business domain logic.

---

## 3. The Unified Community Blueprint

### A. The Directory Taxonomy (Mirroring Jens's OS)
All community-aligned Moqui components should adopt a standardized `.agent` (or `.agent-os`) directory structure:
- `guidelines/`: Architectural strategy (e.g., Blueprint definitions).
- `instructions/`: Workflow "how-tos" (e.g., WebMCP setup).
- `standards/`: Declarative rules (e.g., Groovy usage, Security).
- `templates/`: XML/Groovy snippets (e.g., Entity/Service CRUD patterns).
- `references/`: Domain-specific pattern guides.

### B. Standard Prompting Workflows (Integrating Hans's Strategy)
- **The Pattern Reference:** Always instruct the AI to "look at" a specific Mantle or Framework file before generating new code.
- **The CRUD Clone:** When building services, explicitly refer to high-fidelity service patterns (e.g., `update#Product`).
- **The Closing Loop:** Every significant task should end with the AI generating a technical summary in `docs/features/` or a specialized KI.

### C. The Interactive Layer (Integrating MoquiAi Strategy)
- **Extensible Macro DSL:** Use custom tags like `<form-query>`, `<menu-dropdown>`, and `<bp-parameter>` (defined in `moqui-ai-screen.xsd`) to bridge Moqui logic with reactive state. See [moqui-ai-macro-extensions.md](file:///home/byersa/IdeaProjects/aitree-project/runtime/component/moqui-ai/.agent/references/moqui-ai-macro-extensions.md) for a deep dive.
- **Blueprints as Source of Truth:** Move away from raw HTML/CSS generation. The AI should generate **Blueprints** (JSON-LD) which are rendered by the `DeterministicVueRenderer`.
- **WebMCP for Verification:** Use the WebMCP bridge to allow the AI to "see" the rendered output, take screenshots, and interact with the DOM during the **VALIDATE** phase of the task.

---

## 4. Implementation Guidelines for Developers

1. **Bootstrap:** Install `moqui-agent-os` as a foundational component in your Moqui runtime.
2. **Overlay:** Create your local project `.agent` folder. Add your unique business rules in `guidelines/` and `standards/`.
3. **Anchor:** Use a root `CLAUDE.md` or `GEMINI.md` that directs the AI to prioritize the local `.agent` folder over the global `moqui-agent-os` instructions.
4. **Notify on Conflict:** If a global standard (from Jens) conflicts with a local requirement, explicitly document it in the project's `standards/` folder so the AI knows which "branch" to follow.

---

## 5. Conclusion: A Shared Vision
By meshing Jens's structural foundation, Hans's practical prompting workflows, and the MoquiAi interactive bridge, we move from "AI as a code generator" to **"AI as a collaborative system architect."** This unified approach ensures that code remains consistent, documentation stays current, and the UI is natively agent-ready from day zero.
