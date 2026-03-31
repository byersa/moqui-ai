# Moqui-AI: Client Architecture Specification

This document provides a detailed overview of the two-tier client-side architecture in the Moqui-AI framework, focusing on the roles of `MoquiAiVue.qvt.js` and `BlueprintClient.js`.

---

## 1. Overview: The Hybrid Rendering Engine

Moqui-AI employs a hybrid frontend architecture that blends **Server-Side Macro Transformation** with **Client-Side Reactive Hydration**. It supports two distinct rendering modes through the same SPA shell:

1.  **QVT (Quasar Vue Template) Mode**: XML screens are pre-transformed on the server into custom HTML tags (`m-form`, `m-screen-layout`).
2.  **Blueprint (JSON-LD) Mode**: XML screens are delivered as raw metadata (Blueprints), which the client dynamically builds into a UI tree.

---

## 2. MoquiAiVue.qvt.js: The Master Coordinator

`MoquiAiVue.qvt.js` is the core of the Single Page Application (SPA). It initializes the Vue 3 app, manages the global state, and coordinates all subscreen loading.

### Key Functions
- **SPA Routing & Navigation**:
    - `setUrl(url, bodyParameters, ...)`: Intercepts all internal link clicks, updates the browser history via `webrootRouter`, and triggers subscreen reloads without a full page refresh.
    - `loadActive()`: Orchestrates the loading of subscreen segments based on the `currentPathList`.
- **Component Registry**:
    - It registers the entire library of `m-*` components (e.g., `m-form-query`, `m-drop-down`, `m-container-box`).
    - These components map standard Moqui UI concepts to **Quasar v2** widgets.
- **State Management**:
    - **`currentPathList`**: Tracks the breadcrumb/hierarchy of active screens in the SPA.
    - **`currentParameters`**: Stores reactive search parameters and form inputs.
    - **`activeSubscreens`**: Maintains references to currently mounted subscreen containers for recursive reloads.
- **Systemic AJAX Security**:
    - Injects CSRF and Session tokens (`X-CSRF-Token`) into every jQuery-based AJAX request via `$.ajaxSetup`.

---

## 3. BlueprintClient.js: The Dynamic HUD Engine

`BlueprintClient.js` is an extension that allows Moqui to function as a **Headless UI** engine. It is activated when `MoquiAiVue` receives a JSON response instead of a tagged template.

### Key Functions
- **JSON-LD Identification**:
    - `moqui.isBlueprint(obj)`: Detects if a server response is a structural JSON blueprint (checking for `@type: ScreenBlueprint`).
- **Dynamic Component Generation**:
    - `moqui.makeBlueprintComponent(blueprint, sourceUrl)`: Transforms a static JSON tree into a reactive Vue component object.
    - It performs a "Pre-Scan" of the blueprint to initialize the **Reactive Context**, ensuring that all fields in the JSON have a corresponding slot in the Vue data object.
- **Recursive Rendering (`BlueprintNode`)**:
    - The core of the client is the `BlueprintNode` component. It walk the JSON tree recursively:
        - Maps JSON `@type` keys to registered Vue components (e.g., `FormSingle` -> `m-form-single`).
        - Handles **Reactive Visibility**: Interprets `condition` attributes in the JSON using a dynamic `new Function()` bridge.
        - **Event Binding**: Transforms string-based JS patterns (like `onchange: ...`) into executable functions within the Vue context.
- **Automatic Form Handling**:
    - `submitForm()`: Inside a `FormSingle`, the client automatically handles serialization and AJX POSTing to the server, including error handling and notification updates.

---

## 4. The Interaction Loop

1.  **The Request**: `m-subscreens-active` (in `MoquiAiVue`) triggers a request for a subscreen. It often appends **`lastStandalone=-2`** and **`renderMode=qjson`** to get a targeted partial response.
2.  **The Detection**: `moqui.loadComponent` receives the response.
    - **If QVT (HTML tags)**: The content is injected into a `compObj.template` and rendered directly by Vue.
    - **If JSON (Blueprint)**: It calls `BlueprintClient` to generate a "Virtual" component from the JSON metadata.
3.  **The Result**: The user sees the same high-quality Quasar UI regardless of whether the structure was generated on the server (FTL) or on the client (Blueprint).

---

## 5. Comparison: QVT vs. Blueprint

| Feature | QVT (Tag-Based) | Blueprint (JSON-LD) |
| :--- | :--- | :--- |
| **Delivery Format** | HTML (e.g., `<m-form>`) | JSON (e.g., `{"@type": "FormSingle"}`) |
| **Transformation Site** | Server (FreeMarker Macros) | Client (BlueprintNode) |
| **Flexibility** | High (Pre-defined components) | Extreme (Dynamic structural changes) |
| **Speed** | Faster initial render | Better for highly interactive AI-driven UIs |
| **Primary Logic** | `MoquiAiVue.qvt.js` | `BlueprintClient.js` |

---

## 6. Location & Usage
- **Source**: `runtime/component/moqui-ai/screen/moquiai/js/`
- **Reference Location**: `runtime/component/moqui-ai/overlay/references/client/architecture.md`
- **Developer Rule**: When adding new custom widgets, always register them in `MoquiAiVue.qvt.js` and then add a mapping case in `BlueprintClient.js` to ensure they are available in both rendering modes.
