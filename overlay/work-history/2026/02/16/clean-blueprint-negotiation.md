# Work History: 2026-02-16 - Clean Blueprint Negotiation Implementation

## Objective
Implement a non-invasive blueprint negotiation system that eliminates the need for `?renderMode=qjson` query parameters, ensuring clean URLs and adherence to web standards.

## Key Accomplishments

### 1. Server-Side Content Negotiation
- **Component**: `moqui-ai`
- **File**: `MoquiConf.xml`
- **Change**: Added a `before-request` hook to `webapp-list/webapp[@name='webroot']`.
  - Logic: Inspects `Accept` header. If it contains `application/json` and no `renderMode` is set, defaults to `qjson`.
- **Global Override**: Overrode the `html` render mode configuration to use `MoquiAiShellMacros.html.ftl`. This ensures that semantic tags (`<screen-layout>`, etc.) are rendered correctly even in standard HTML mode, preventing the "Doing nothing..." macro pollution caused by the previous `qvt2` inclusion.

### 2. Frontend Negotiation
- **Component**: `moqui-ai`
- **File**: `screen/moquiai/js/MoquiAiVue.qvt2.js`
- **Change**: Updated component loading logic:
  - Default `renderModes` to `['qjson', 'qvt2']`.
  - This ensures that when the SPA initializes and requests sub-screens, it sends `Accept: application/json`, triggering the server-side negotiator.
  - Removed code that appended `?renderMode=qjson` to URLs.

### 3. Shell Implementation Fixes
- **Component**: `moqui-ai`
- **New File**: `template/MoquiAiShellMacros.html.ftl`
  - Created a clean HTML macro template for semantic tags.
  - Includes standard `DefaultScreenMacros.html.ftl`.
  - Defines pass-through macros for `<screen-layout>`, `<screen-header>`, etc., allowing Quasar components to render without triggering the `qvt2` catch-all macro.
- **Component**: `huddle`
- **File**: `screen/huddle.xml`
- **Change**: Cleaned up the `render-mode` block. Semantic tags are now handled globally via `MoquiConf.xml` override, so manual inclusion was removed.
- **File**: `script/HuddlePreActions.groovy`
- **Change**: Removed the legacy `bp=1` redirect hack.

## Verification
- **Browser**: Accessing `/huddle` correctly renders the HTML shell (via clean macros) and then loads the JSON blueprint (via AJAX negotiation).
- **Curl**:
  - `curl -H "Accept: text/html" ...` -> Returns HTML Shell.
  - `curl -H "Accept: application/json" ...` -> Returns JSON Blueprint.

## Repository Status
- **Synced**: `moqui-ai` and `huddle` repositories have been committed and pushed to `main`.
