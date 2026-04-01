# The Life of a Request: `/huddle`

This document details the technical flow triggered when a user enters `http://localhost:8080/huddle` into the browser. It explains the "Hybrid" architecture where Moqui serves a static HTML shell, and the client subsequently fetches dynamic Blueprints.

## Phase 1: The Initial Page Load (Server-Side)

1.  **Browser Request**:
    *   **URL**: `/huddle`
    *   **Method**: `GET`
    *   **Header**: `Accept: text/html` (Standard browser navigation)

2.  **Moqui Servlet Routing**:
    *   Moqui receives the request.
    *   It matches the path `/huddle` to the screen defined at `component://huddle/screen/huddle.xml`.

3.  **Screen Rendering (The Shell)**:
    *   Moqui renders `huddle.xml`.
    *   **Macro Selection**: Based on the configuration (likely `qvt2`), it uses the `DefaultScreenMacros.qvt2.ftl`.
    *   **Shell Template**: The specific template `runtime/component/moqui-ai/template/MoquiAiShellMacros.html.ftl` is invoked to render the HTML structure.
    *   **Key Action**: The server renders the `<head>` (CSS, titles) and the `<body>` containing:
        *   Library definitions (`vue.global.js`, `quasar.umd.js`).
        *   The application scripts (`MoquiAiVue.qvt2.js`, `BlueprintClient.js`).
        *   The Router configuration (`routes.js` generated dynamically).
        *   **Crucially**: An empty/minimal mounting point for Vue (`<div id="app">`).

4.  **Response**:
    *   The server returns a standard **HTML** document to the browser.

## Phase 2: Client-Side Bootstrap (The Browser)

5.  **JavaScript Execution**:
    *   The browser parses the HTML and executes the scripts.
    *   `MoquiAiVue.qvt2.js` runs:
        *   Initializes `moqui.webrootVue` (The Vue App).
        *   Installs `VueRouter`.
        *   Mounts the Vue App to `#app`.

6.  **Router Activation**:
    *   The Vue Router analyzes the current URL: `/huddle`.
    *   It matches the "Catch-All" route defined in `routes.js`: `path: '/:pathMatch(.*)*'`.
    *   This route maps to the `BlueprintRoute` component.

## Phase 3: The Blueprint Handshake (Content Negotiation)

7.  **BlueprintRoute Mounting**:
    *   The `BlueprintRoute` Vue component mounts.
    *   It detects it has no data for `/huddle` yet.
    *   It triggers `loadBlueprint('/huddle')`.

8.  **The Fetch Request**:
    *   **URL**: `/huddle` (Same URL as before!)
    *   **Method**: `GET`
    *   **Header**: `Accept: application/ld+json` (Crucial Differentiator)
    *   This request is sent asynchronously (AJAX) to the Moqui server.

9.  **Server-Side Content Negotiation**:
    *   Moqui receives the request for `/huddle`.
    *   It sees the `Accept: application/ld+json` header.
    *   **Switch**: Instead of using the HTML macros, it switches to **JSON Macros** (`ComponentClientJsonMacros.ftl`).
    *   It renders `huddle.xml` again, but this time transforms the XML screen definition (and its sub-screens like `stfhdl.xml`) into a **JSON-LD Blueprint** tree.

10. **Blueprint Response**:
    *   The server returns a **JSON Object** describing the UI structure (e.g., `{"@type": "ScreenBlueprint", "children": [...]}`).

## Phase 4: Dynamic Rendering (The Client)

11. **Blueprint Interpretation**:
    *   `BlueprintRoute` receives the JSON.
    *   It passes the root node to the `<m-blueprint-node>` component (implemented in `BlueprintClient.js`).

12. **Recursive Transformation**:
    *   `BlueprintClient.js` walks the JSON tree.
    *   **Mapping**: It maps JSON types to Vue components:
        *   `screen-layout` -> `<m-screen-layout>`
        *   `screen-header` -> `<m-screen-header>`
        *   `SubscreensMenu` -> `<m-subscreens-menu>`
    *   **Props**: It converts attributes (e.g., `elevated="true"`) into Vue props.
    *   **Context**: It passes context (like `parentType="screen-toolbar"`) down to children to handle layout logic.

13. **Final Paint**:
    *   Vue updates the DOM.
    *   The user sees the "Staff Huddle" header, the horizontal menu, and the content forms.

## Summary
The URL `/huddle` serves two purposes:
1.  **HTML Shell** (for Humans/Browsers): "Here is the empty app structure."
2.  **JSON Blueprint** (for the App): "Here is the actual content and layout definition."
