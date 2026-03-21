# Deep Dive: Blueprint-Driven Development (BDD)

Commonly in the MoquiAi project, we use a **Spec-First** strategy known as **Blueprint-Driven Development (BDD)**. This approach centers on the use of the `blueprints/` directory within solution components to drive high-fidelity AI code generation.

---

## 1. Distinguishing the Two "Blueprints"

To avoid architectural confusion, we must distinguish between the two types of blueprints used in this ecosystem:

| Feature | **Development Blueprints** (Spec) | **Runtime Blueprints** (Render) |
| :--- | :--- | :--- |
| **Location** | `runtime/component/[name]/blueprints/` | Emitted by `DeterministicVueRenderer` |
| **Format** | Markdown (`.md`) | JSON-LD |
| **Purpose** | **Code Generation**: Instructions for the AI to build the `.xml` and `.groovy` files. | **UI Rendering**: Instructions for the Vue client to draw the reactive screen. |
| **Audience** | The AI Assistant (Authoring phase). | The Browser & Agent (Runtime phase). |

---

## 2. The Structure of a Development Blueprint
A development blueprint is a structured Markdown file that acts as a "Requirement Specification" for an AI agent. By providing this file, a human developer can ensure the AI generates code that follows the exact architecture and naming conventions of the project.

### Key Sections:
1. **Instructions for AI**: Global directives (e.g., "Prefer `<form-single>`", "Use `<form-query>`").
2. **Architecture Pattern**: Defines where this artifact fits in the hierarchy (e.g., `PatientMedicalRoot -> ClinicalDashboard`).
3. **Declarative Moqui Context**:
   - `parameter`: Input variables.
   - `actions`: Data fetching logic (Entity-Finds).
   - `widgets`: A high-level DSL layout using MoquiAi macro extensions.
4. **Vue/Pinia Integration**: Explicitly names the Pinia stores (e.g., `useMeetingsStore`) the screen must interact with.
5. **Quality Assurance**: A `test-plan` and `Implementation Checklist` to verify the generated code.

---

## 3. The BDD Workflow: From Spec to Solution
The workflow for adding a new feature follows this pipeline:

1. **Blueprint Creation**: The developer (or AI) drafts a `.md` file in the `blueprints/` folder.
2. **AI Generation**: An AI agent reads the blueprint and generates:
   - The Moqui XML Screen.
   - Any necessary Groovy Services.
   - Any custom CSS or JS fragments.
3. **Verification**: The generated code is verified against the `test-plan` section of the blueprint.
4. **Maintenance**: When a screen needs to change, the **Blueprint is updated first**, and the code is re-generated or patched accordingly. This ensures the documentation never drifts from the implementation.

---

## 4. Why BDD is Critical for Community Alignment
- **Instructional Anchoring**: Blueprints provide the "Final Word" on how a component should be built, overriding generic AI assumptions.
- **Taxonomy Mirroring**: By mirroring the `blueprints/` folder with the implementation folder (e.g., `blueprints/screen/` maps to `screen/`), the project becomes self-documenting.
- **Knowledge Transfer**: A developer joining the Moqui community can read the `blueprints/` folder to understand the *intent* and *logic* of a complex screen without parsing 500 lines of XML.

---

## 5. Conclusion: The Source of Truth
In the MoquiAi paradigm, the implementation file (`.xml`) is a **derivative artifact**. The **source of truth** is the Development Blueprint in the `blueprints/` folder. This ensures that the AI assistant remains a coordinated collaborator rather than a disconnected code generator.
