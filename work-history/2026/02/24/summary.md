# 2026-02-24: SPA Routing, Dynamic Components, and Global State

## Objective
The primary objective of this session was to resolve systemic routing and client-side execution errors in the `moqui-ai` Vue/Quasar Single Page Application (SPA), implement dynamic database-driven UI components, and establish a global state management architecture.

## Changes Made
### 1. Re-engineered Navigation and Routing
- **Removed Legacy Navigation Macros:** Replaced `<subscreens-active/>` with `<router-view></router-view>` and migrated from static `<subscreens-menu>` to explicitly-defined `<menu-item>` elements.
- **Fixed Vue Router Integration:** Updated `MoquiAiVue.qvt2.js` to rely on `this.$router.push()` instead of raw `window.history.pushState` when navigating, fixing double-routing glitches.
- **Decoupled BlueprintClient.js:** Modified the `$.getScript` call inside `MoquiAiVue.qvt2.js` to rely on the dynamic `this.basePath` when fetching `BlueprintClient.js`, curing a static 404 error that prevented `moqui.isBlueprint()` from initializing correctly.

### 2. Introduced Dynamic Menu Dropdowns
- Implemented a Server-side FTL macro `<#macro "menu-dropdown">` and a matching Client-side Vue component `<m-menu-dropdown>`.
- The component lazily-loads its list items via AJAX `transition-url` when expanded, and appends a target parameter based on `key-field` when selected.
- Later updated both the `<m-menu-dropdown>` and its FTL macro to accept `pinia-store` and `pinia-list` properties, enabling the component to dynamically bind directly to global store arrays instead of firing redundant AJAX fetches.

### 3. Integrated Pinia State Management
- Registered Pinia globally within the `moqui-ai` component (`MoquiAiVue.qvt2.js`), establishing it as the standard for cross-component and deeply nested state sharing.
- Created `meetingsStore.js` to track user-selected active meetings globally.

### 4. Established Tab-Oriented Architecture
- Added `<q-tabs>` and `<q-tab>` directly into `moqui-ai-screen.xsd`. 
- Implemented corresponding Freemarker macros in `MoquiAiScreenMacros.qvt2.ftl` to compile them into `<m-q-tabs>` and `<m-q-tab>`.
- Built the frontend Vue components utilizing Quasar's powerful Vue Router integration feature (`<q-route-tab>`), effortlessly syncing the UI state with the active URL Route.

### 5. Automated Workflow Standards
- Created `/savePrompt` and `/endSession` workflows to standardize technical logging and session wrap-ups.
- Implemented a `git-sync.sh` script to automate repository synchronization and avoid rebase conflicts.
- Codified "Component Separation" rules in `work-history-standards.md` to keep platform and application logs distinct.

## Impact
The core SPA shell (`moqui-ai`) now reliably supports Vue Router + Pinia, natively renders deeply integrated Tab Navigation, and provides a structured automation layer for sustainable development across multiple components.
