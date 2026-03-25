
# Frontend Blueprint Rendering Debugging
**Date:** 2026-02-17
**Component:** `moqui-ai`
**Goal:** Debug and resolve issues preventing the Vue SPA from correctly loading and rendering JSON Blueprints for the "Staff Huddle" application.

## Summary of Work
The debugging session focused on enabling the Vue frontend to consume and render content from `moqui-ai`'s JSON Blueprints correctly. Several critical issues were addressed in the client-side logic, routing configuration, and blueprint rendering engine.

### Issues Resolved

1.  **Frontend Resource Loading (404/401 Errors)**
    *   **Problem:** The browser could not load `BlueprintClient.js` due to missing screen transitions and authorization requirements.
    *   **Resolution:**
        *   Added a `js` transition to `moquiai.xml` to expose the `js` directory.
        *   Relaxed authorization for the `moquiai` screen (`require-authentication="false"`) and the `js` transition (`require-session-token="false"`) to allow anonymous access to scripts.

2.  **Routing & Shell Architecture (Double Rendering)**
    *   **Problem:** The application suffered from "Double Shell" rendering, where the App Shell (Header/Drawer) appeared twice nested within itself. This occurred because `MoquiAiVue` was hydrating the server-rendered HTML shell while the Router was *also* rendering a full Shell from the JSON Blueprint response.
    *   **Resolution:**
        *   Updated `MoquiAiVue.qvt2.js` to explicitly set `template: '<m-subscreens-active></m-subscreens-active>'` on the root Vue instance. This forces Vue to replace the static HTML shell with the dynamic Blueprint-driven shell upon mounting, ensuring a single source of truth for the layout.
        *   Created `routes.js.ftl` to configure Vue Router with `createWebHistory()` *without* a base path. This allows the router to handle full absolute paths returned by Moqui without duplicating the base URL (fixing the `/huddle/huddle/Home` issue).

3.  **Missing Navigation Menu**
    *   **Problem:** The `m-subscreens-menu` component was failing to render or showing incomplete menu items.
    *   **Root Causes:**
        *   `BlueprintClient.js` lacked a handler for the `SubscreensMenu` blueprint node.
        *   The component was not reactive to asynchronous menu data updates.
        *   The rendering logic did not account for the hierarchical structure (e.g., "Huddle" containing "Dashboard").
        *   Text was invisible (white-on-white) due to inheritance from the header.
    *   **Resolution:**
        *   Added `SubscreensMenu` handler to `BlueprintClient.js`.
        *   Updated `m-subscreens-menu` in `MoquiAiVue.qvt2.js` to use `computed` properties for reactivity.
        *   Refactored the template to use `q-expansion-item` for hierarchical rendering.
        *   Explicitly set `text-grey-9` and `text-primary` classes to ensure visibility.

4.  **Label Styling**
    *   **Problem:** The "Staff Huddle" label in the header was unstyled or invisible.
    *   **Resolution:** Updated `BlueprintClient.js` to retrieve text from `node.text` (direct property) and improved class handling to respect `text-white` or header context.

### Verification
The application now correctly loads the "Staff Huddle" dashboard from `http://localhost:8080/huddle`.
- **Navigation:** Deep linking and menu navigation work correctly without URL duplication.
- **UI:** The App Shell renders once, and the menu is visible and expandable.
- **Data Flow:** The frontend successfully negotiates content via `Accept: application/json` and renders it using the `BlueprintRoute` component.

## Files Modified
*   `runtime/component/moqui-ai/screen/moquiai.xml`
*   `runtime/component/moqui-ai/template/screen/routes.js.ftl` (New)
*   `runtime/component/moqui-ai/screen/moquiai/js/MoquiAiVue.qvt2.js`
*   `runtime/component/moqui-ai/screen/moquiai/js/BlueprintClient.js`
*   `runtime/component/huddle/template/globals.qvt2.ftl`
