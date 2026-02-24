# Blueprint: Clean Content Negotiation

**Date**: 2026-02-16
**Status**: Implemented

## Goal
To enable a seamless, non-invasive way for the MoquiAi frontend (Vue/Quasar) to request data and blueprints from the Moqui server without requiring "hacky" query parameters in the URL (e.g., `?renderMode=qjson`).

## Architecture

### 1. Server-Side Negotiation (`MoquiConf.xml`)
The Moqui server uses a `before-request` hook to inspect the incoming `Accept` header.

- **Hook Location**: `webapp-list/webapp[@name='webroot']`
- **Logic**:
    - If `Accept` header contains `application/json` AND `renderMode` is not explicitly set in the request parameters.
    - Set `renderMode` to `qjson`.
- **Outcome**: The same URL (`/huddle`) returns HTML for a browser page load and JSON for an AJAX component load.

### 2. Shell Rendering Fix
When a user visits `/huddle` in a browser, they receive an HTML shell. To prevent the "polluted" macro output ("Doing nothing...") that occurs when unknown semantic tags are encountered in standard HTML mode:

- **Template**: `template/MoquiAiShellMacros.html.ftl`
- **Override**: In `MoquiConf.xml`, the `html` render-mode configuration for the `webroot` webapp is overridden to include this template.
- **Function**: This template includes `DefaultScreenMacros.html.ftl` and provides "Empty" or "Wrapper" definitions for custom tags like `<screen-layout>`, `<screen-header>`, etc.
- **SSR Trap**: It specifically overrides the `subscreens-active` macro to output only the Vue mount point (`<m-subscreens-active></m-subscreens-active>`), preventing the server from rendering sub-screen content into the initial HTML shell.

### 3. Frontend Implementation (`MoquiAiVue.qvt2.js`)
The Vue SPA is configured to prefer JSON negotiation.

- **Configuration**: `renderModes` is defaulted to `['qjson', 'qvt2']`.
- **Behavior**: When the `MoquiAiVue` component initializes or navigates, it sends requests with `Accept: application/json`. The server responds with the `qjson` representation (the blueprint).

## Advantages
- **Clean URLs**: No more `?renderMode=qjson` visible to users.
- **Standards Compliant**: Uses standard HTTP Content Negotiation.
- **Robust Shell**: Standard Moqui HTML rendering works correctly without macro errors.
