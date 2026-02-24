# 2026-02-24: SPA Routing and Dropdown Fixes

## Objective
The primary objective of this session was to resolve systemic routing and client-side execution errors in the `moqui-ai` Vue/Quasar Single Page Application (SPA), specifically fixing a `text/html` script execution error (`#?v=...`) and implementing a dynamic, API-driven `<m-menu-dropdown>` component.

## Changes Made
1. **Removed Legacy Navigation Macros:** Replaced `<subscreens-active/>` with `<router-view></router-view>` and migrated from static `<subscreens-menu>` to explicitly-defined `<menu-item>` elements.
2. **Fixed Vue Router Integration:** Updated `MoquiAiVue.qvt2.js` to rely on `this.$router.push()` instead of raw `window.history.pushState` when navigating, fixing double-routing glitches.
3. **Repaired SPA Component Loading:** 
   - Found that `AitreePreActions.groovy` was trying to load `MoquiAiVue.qvt2.js` from a virtual path that Moqui could not resolve natively. Moqui handled the 404 by returning `#` and appending a cache-busting suffix `?v=...` which the browser interpreted as an inline script tag.
   - **Fix:** Explicitly defined a `<transition name="moquiaiJs">` in `aitree.xml` to serve static JS files directly from the `moqui-ai` component block securely without `#?v=` corruption.
4. **Decoupled BlueprintClient.js:**
   - Modified the `$.getScript` call inside `MoquiAiVue.qvt2.js` to rely on the dynamic `this.basePath` when fetching `BlueprintClient.js`, curing a static 404 error that prevented `moqui.isBlueprint()` from initializing correctly.
5. **Introduced Dynamic `<menu-dropdown>`:**
   - Implemented a Server-side FTL macro `<#macro "menu-dropdown">` and a matching Client-side Vue component (`<m-menu-dropdown>`).
   - The component lazily-loads its list items via AJAX `transition-url` (e.g., `/aitree/getAgendaContainers`) when expanded.
   - It appends a chosen variable (like `?agendaContainerId=100`) to the target URL when an item is selected based on `key-field`.
6. **Bypassed Extraneous Entity Authz:**
   - When calling `getAgendaContainers` from the dropdown component anonymously, Moqui threw a 403 Forbidden.
   - **Fix:** Converted the XML `<entity-find>` into a Groovy script to explicitly invoke `.disableAuthz()` on the entity query, allowing all users to view available meeting containers.
   - **Strategic Note**: The user noted concern about continuously bypassing authorization requirements whenever they arise, as it could accumulate technical debt. However, for a proof of concept, this tactical downgrade was approved because it prevents security blockers from slowing down the more important, foundational work of refining macro strategies (particularly for complex dynamic components like `<discussion-tree>`). Proper authz will be revisited later once the core architecture is stable.

## Impact
The `/aitree/Home` single page application now reliably loads `routes.js`, boots Vue Router, correctly mounts the dynamic Blueprint components, and navigates between subscreens without throwing DOM or network errors. The "Meetings" dropdown acts as the template for future database-driven navigation elements.
