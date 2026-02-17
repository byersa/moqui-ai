# Walkthrough: Custom Screen Layout Tags & Vue Components

I have successfully implemented the custom screen layout tags, backed by custom Vue components.

## Changes

### 1. Framework XSD
Added the following tags to `framework/xsd/xml-screen-3.xsd`:
- `<screen-layout>`
- `<screen-header>`
- `<screen-drawer>`
- `<screen-toolbar>`
- `<screen-content>`

### 2. Vue Components
Registered the following components in `runtime/component/huddle/screen/huddlejs/HuddleVue.qvt2.js`:
- `m-screen-layout`
- `m-screen-header`
- `m-screen-drawer`
- `m-screen-toolbar`
- `m-screen-content`

These components wrap the standard Quasar components (e.g., `q-layout`, `q-header`) to enforce consistent behavior and styling defaults.

### 3. Screen Macros
Updated `runtime/template/screen-macro/DefaultScreenMacros.qvt2.ftl` to render the **custom Vue components** (e.g., `<m-screen-layout>`) instead of raw Quasar tags.

### 4. Application Code
Refactored `runtime/component/huddle/screen/huddle.xml` to use the new XML tags.

## Verification

### Manual Verification Required
**Steps:**
1.  **Restart Moqui** (or clear caches) to pick up the XSD, FTL updates, and updated JS file.
2.  **Navigate to Huddle**: Open the application.
3.  **Inspect DOM**: You should see `<m-screen-layout>` in the DOM (in Vue devtools) or the resulting `q-layout` structure rendered by Vue.

### Code Verification
Verified that `HuddleVue.qvt2.js` contains the new component definitions and `DefaultScreenMacros.qvt2.ftl` references them.
