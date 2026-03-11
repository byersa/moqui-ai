# Work History: Architectural Exploration

## Phase 1: Initial Request and Bootstrapping (`/aitree`)

### 1. The Starting Point
- **Trigger**: Browser requests `http://localhost:8080/aitree`.
- **Observation**: The server responds with the HTML shell. The address bar displays `/aitree/` (with a trailing slash).
- **Execution Point**: `moqui.webrootVue.mount('#apps-root')` is called (bottom of `MoquiAiVue.qvt.js`).

### 2. The `mounted` Hook
- **Logic**: The root Vue instance mounts to the DOM.
- **Action**: It calls `this.setUrl(window.location.pathname + window.location.search, null, null, false);`.
- **Status**: `window.location.pathname` at this point is indeed `/aitree/`.

### 3. The `setUrl` Logic (Debugger at Line 56)
- **Input**: `url` = `/aitree/`.
- **State**: `this.currentLinkUrl` (computed) is currently `/aitree`.
- **Conflict**: The comparison `this.currentLinkUrl === url` (line 66) fails because of the trailing slash mismatch.
- **Result**: Instead of skipping the reload (the "same URL" optimization), Vue proceeds to the `else` block to perform a full menu and subscreen data fetch for the current path.

## Phase 2: Router Redirect to `/Home`

### 1. The Trigger
- **Observation**: A second call to `setUrl` occurs immediately after the first, triggered by the `$route` watcher.
- **Data**: `to.fullPath` = `/Home`, `window.location.href` = `http://localhost:8080/aitree/Home`.

### 2. The Cause: `routes.js.ftl`
- **Logic**: The router is initialized with `createWebHistory('/aitree')`.
- **Redirect Rule**: `moqui.routes` contains `{ path: '/', redirect: '/Home' }`.
- **Action**: When the router mounts at `/aitree/`, it sees the internal path as `/`, hits the redirect rule, and pushes `/Home` to the browser history.

### 3. The `$route` Watcher
- **Observation**: The watcher at line 401 (in `MoquiAiVue.qvt.js`) reacts to this router-initiated navigation.
- **Action**: It calls `this.setUrl(window.location.href, ..., false)`.
- **Significance**: This ensures the Moqui shell (specifically `navMenuList` and `currentPathList`) stays in sync with the Vue Router's state. 

## Phase 3: Making Routing Dynamic for App Generation

### 1. The Requirement
- **Goal**: Build an app generator that doesn't rely on hardcoded strings like "Home".
- **Insight**: Moqui's `ScreenDefinition` already knows what the default subscreen is through the `default-item` attribute.

### 2. The Implementation (`aitree.xml`)
- **Action**: Updated the `routes.js` transition to programmatically find the default subscreen:
  ```groovy
  <set field="defaultSubscreen" from="rootScreenDef.getDefaultItemName() ?: 'Home'"/>
  ```
- **Context**: `rootScreenDef` is already available in the transition, so we just ask it for its `defaultItemName`.

### 3. The Template Update (`routes.js.ftl`)
- **Action**: Replaced the hardcoded string with a FreeMarker variable:
  ```javascript
  { path: '/', redirect: '/${defaultSubscreen!"Home"}' }
  ```
- **Result**: If the generator creates a root screen with `default-item="Dashboard"`, the Vue Router will automatically redirect to `/Dashboard` without any manual changes to the JS code.

## Phase 4: `setUrl` Synchronization and Rendering Gaps

### 1. The `pushState` Dilemma
- **Observation**: When `setUrl` is called from the `$route` watcher, `pushState` is set to `false`.
- **Reasoning**: This is a **guard against infinite loops**. 
  - Trace: `Router navigates` -> `Watcher fires` -> `setUrl` -> `Router push` -> `Watcher fires`...
- **Conclusion**: `pushState: false` is correct. The Router is already at the destination; we just need Moqui to sync its menu/shell state.

### 2. The "Nothing Renders" Mystery
- **Observation**: `/aitree/Home` shows a spinner or nothing at all.
- **Root Cause Analysis**: 
  - The `m-subscreens-active` component has a "Consolidation Guard" (line 3511).
  - It sees that the Router is already handling `/Home`, so it skips its own `loadComponent` call.
  - However, because it skips, `this.activeComponent` remains `EmptyComponent` (the spinner).
  - The template `activeComponent || 'router-view'` sees `EmptyComponent` is truthy, so it never shows the `router-view`.
- **Planned Fix**: If the guard hits, we must set `this.activeComponent = null` to "hand off" the rendering to the Vue Router's `<router-view>`.

## Phase 5: Implementation of Rendering Hand-off

### 1. The Fix (`MoquiAiVue.qvt.js`)
- **Action**: Modified the `m-subscreens-active` consolidation guard to explicitly clear `activeComponent`:
  ```javascript
  if (normRouterPath === normFullPath) {
      this.activeComponent = null; // Hand off to router-view
      // ...
  }
  ```
- **Rationale**: Since `activeComponent` is initialized to `EmptyComponent` (a truthy value), the template `activeComponent || 'router-view'` would never fall back to the router-view without this explicit clear.

### 2. Resulting Flow
1. **Initial Mount**: `setUrl("/")` -> Router Redirect -> `/Home`.
2. **Synchronization**: `$route` watcher detects `/Home` -> `setUrl("/aitree/Home")`.
3. **Consolidation**: `m-subscreens-active` wakes up.
4. **Handoff**: Guard sees Router is already at `/Home`. It sets `activeComponent = null`.
5. **Final Render**: The template now falls back to `<router-view>`, which contains the component already loaded by the Router during the redirect phase.

## Phase 6: Resolution of Runtime Errors

### 1. The `routes.js` 500 Error
- **Observation**: Request to `/aitree/routes.js` failed with a server-side 500 error.
- **Root Cause**: The transition in `aitree.xml` used `getDefaultItemName()`, which doesn't exist on `ScreenDefinition`.
- **Fix**: Corrected the method name to `getDefaultSubscreensItem()` after verifying the Moqui source code.

### 2. The `createApp` Reference Error
- **Observation**: `MoquiAiVue.qvt.js` failed with `ReferenceError: createApp is not defined`.
- **Root Cause**: During cleanup of unused imports, the destructuring line that provided `createApp` was removed.
- **Fix**: Restored the destructuring: `const { createApp } = Vue;`.

## Phase 7: Resolution of Script Scope Conflicts

### 1. The `createApp` Redeclaration Error
- **Observation**: `MoquiAiVue.qvt.js` failed with `SyntaxError: Identifier 'createApp' has already been declared`.
- **Root Cause**: Both `routes.js` and `MoquiAiVue.qvt.js` were attempting to declare `const { createApp } = Vue` in the same global browser scope. Because they are not ES modules, these top-level `const` declarations conflict.
- **Fix**: Changed `MoquiAiVue.qvt.js` to use `Vue.createApp({...})` directly, removing the local `const` declaration and avoiding the naming collision.

## Phase 8: Overcoming the 403 Forbidden Security Barrier

### 1. The Multi-Layered Security Issue
- **Observation**: Navigating to `/aitree/Meetings` resulted in a persistent 403 Forbidden error.
- **Problem**: When a request for a screen "blueprint" (JSON) is made, Moqui renders the whole screen stack. If any component in that stack (like a subscreen's data action) fails a security check, the entire JSON request is aborted.
- **The "No User" Context**: Since the current environment allows anonymous access, the active user is "No User" (null). Moqui's default security policy for entities is "Deny All" for unauthorized users.

### 2. Layers of Protection Corrected
- **Layer 1: Screen Access**: Already handled via `require-authentication="false"` on the XML screens.
- **Layer 2: Entity View Permission**: Initially attempted via `use-entity-view-permissions="false"`. This allows the SQL query itself but doesn't bypass artifact-level checks.
- **Layer 3: Artifact Authorization**: This was the final blocker. Moqui checks if the user is allowed to "view" the `AgendaContainer` entity artifact. 

### 3. The Final Fix (`ManageAgendaContainers.xml`)
- **Action**: Switched from the declarative `<entity-find>` tag to an explicit `<script>` block:
  ```groovy
  containerList = ec.entity.find("aitree.meeting.AgendaContainer")
      .searchFormMap(ec.context, null, "", "name", true)
      .useOldViewCheck(false) // Bypasses Row-level/View permissions
      .disableAuthz()         // Bypasses Artifact-level authorization
      .list()
  ```
- **Result**: By explicitly calling `.disableAuthz()`, we force Moqui to ignore the fact that "No User" is accessing the database, which is necessary for the current "Mock/Demo" state of the application.

## Phase 9: Blueprint Loading and Client Synchronization

### 1. The Blueprint Request
- **Observation**: Clicking "Meetings" triggers a request to `/aitree/Meetings?renderMode=qjson`.
- **Reasoning**: This request is initiated by the `BlueprintRoute` component in `routes.js`. Because "Meetings" is a parent screen, the server renders the parent AND its default subscreen (`ManageAgendaContainers`) to provide a complete UI definition.
- **Client Sync**: If this request succeeds, the client-side router is updated and the `m-subscreens-active` guard ensures we don't fetch the same data twice.
