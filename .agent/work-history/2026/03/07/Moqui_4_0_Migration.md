# Moqui 4.0 SPA Migration Effort

**Date:** March 7, 2026

## Overview
This document summarizes the steps taken to successfully migrate and restore the `aitree` and `moqui-ai` Vue 3 / Quasar Single Page Application components to work natively with Moqui 4.0. The transition away from legacy fallbacks required several backend and frontend adjustments.

## Migration Steps Completed

1. **API Signature Updates**
   - Fixed a `groovy.lang.MissingMethodException` triggered by Moqui 4.0's updated `sri.getMenuData()` Java API signature. Updated the `menuDataQvt` transition in `aitree.xml` to match the new method arguments.

2. **Template Modernization (`qvt2` to `qvt`)**
   - Moqui 4.0 dropped support for the `.qvt2.ftl` fallback template macro. Performed a systematic renaming of all `*qvt2*` files (e.g., `MoquiAiVue.qvt2.js`, `Quasar2Wrapper.qvt2.ftl`) to `*qvt*` and updated internal references across the `moqui-ai` and `aitree` resources.

3. **Resolved Infinite Redirect Loop in Vue Router**
   - The Home page became stuck in an infinite page-refresh cycle during blueprint loading. 
   - **Fix:** Switched the data fetching mode in `routes.js.ftl` (`BlueprintRoute`) from `last=true` to `lastStandalone=true`. The previous parameter recursively fetched the entire application wrapper along with the nested screen, breaking the layout and entering a loop.

4. **Moqui 4.0 Strict Authentication Handling**
   - In Moqui 4.0, calling framework methods like `getMenuData` strictly enforces a `401 Unauthorized` error if the user is completely anonymous. This broke the Vue app's initial routing payload.
   - **Fix:** Added a fallback in the `menuDataQvt` transition in `aitree.xml` to gracefully return an empty array `[]` (`ec.user.userId ? sri.getMenuData(fullPath) : []`) if no user session is active, preventing the 401 error and allowing client-side logic to proceed.

5. **Fixed Quasar Vue 3 Notification Crash**
   - The AJAX error handler in `MoquiAiVue.qvt.js` crashed during `401 Unauthorized` intercepts due to using Vue 2 `.notify` syntax. In Vue 3, `moqui.webrootVue` is an "application instance", not a root component that inherits mixins, rendering `moqui.webrootVue.addNotify` undefined.
   - **Fix:** Shimmed crucial root component functions (`addNotify`, `reLoginCheckShow`, `getCsrfToken`, `setUrl`) back onto the global `moqui.webrootVue` instance right after Vue mounts, ensuring background helpers still function normally under Vue 3 without massive refactors.
   - Replaced usages of the legacy `moqui.webrootVue.$q.notify` with `Quasar.Notify.create` to ensure the Vue 3 notification plugin logic fires globally.

6. **Restored Quasar Stylesheets and Fonts**
   - The final screen successfully parsed but was entirely unstyled. When `Quasar2Wrapper.qvt2.ftl` was deprecated, the CDN imports were moved to `AitreePreActions.groovy`, but the CSS stylesheets were accidentally omitted.
   - **Fix:** Injected the missing `quasar.prod.css`, `Material+Icons`, and `Material+Symbols+Outlined` into the `html_stylesheets` list in `AitreePreActions` to hydrate Quasar's UI components with proper styling.

## Outcome
The application shell successfully initializes, parses remote blueprints, authenticates appropriately, and renders styled Quasar components entirely under the Moqui 4.0 runtime API.
