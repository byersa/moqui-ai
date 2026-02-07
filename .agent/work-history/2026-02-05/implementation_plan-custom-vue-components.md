# Implementation Plan - Custom Screen Layout Tags & Vue Components

The goal is to establish a "Moqui Macro Blueprint" where:
1.  **XSD**: Custom XML tags are defined (`<screen-layout>`, etc.).
2.  **FTL**: Macros translate these tags into **Custom Vue Components** (`<m-screen-layout>`, etc.).
3.  **Vue/JS**: Custom Vue components are defined in `HuddleVue.qvt2.js` to wrap the underlying Quasar components, providing a layer of abstraction and customization.

## User Review Required
> [!IMPORTANT]
> This plan involves defining new global Vue components in `HuddleVue.qvt2.js`. This serves as the "plumbing" between the FTL macros and the Quasar UI.

## Proposed Changes

### 1. Framework Configuration (Completed)
#### [No Change Needed] [xml-screen-3.xsd](file:///home/byersa/IdeaProjects/huddle-ai-project/framework/xsd/xml-screen-3.xsd)
The XSD has already been updated with `screen-layout`, `screen-header`, `screen-drawer`, `screen-toolbar`, and `screen-content`.

### 2. Frontend Logic (New)
#### [MODIFY] [HuddleVue.qvt2.js](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/component/huddle/screen/huddlejs/HuddleVue.qvt2.js)
Register the custom Vue components on the `moqui.webrootVue` instance.

```javascript
// ... existing code ...
moqui.webrootVue = createApp({ ... });

// REGISTER CUSTOM LAYOUT COMPONENTS
moqui.webrootVue.component('m-screen-layout', {
    props: { view: { type: String, default: 'hHh lpR fFf' } },
    template: '<q-layout :view="view" class="bg-grey-1"><slot></slot></q-layout>'
});

moqui.webrootVue.component('m-screen-header', {
    props: { elevated: { type: Boolean, default: true } },
    template: '<q-header :elevated="elevated" class="bg-primary text-white"><slot></slot></q-header>'
});

moqui.webrootVue.component('m-screen-toolbar', {
    template: '<q-toolbar><slot></slot></q-toolbar>'
});

moqui.webrootVue.component('m-screen-content', {
    template: '<q-page-container><q-page class="q-pa-md"><m-subscreens-active></m-subscreens-active><slot></slot></q-page></q-page-container>'
});
// ...
```

### 3. Global Templates (Update)
#### [MODIFY] [DefaultScreenMacros.qvt2.ftl](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/template/screen-macro/DefaultScreenMacros.qvt2.ftl)
Update the macros to render the **custom Vue components** instead of raw Quasar tags.

```freemarker
<#macro "screen-layout">
    <m-screen-layout view="${.node["@view"]!"hHh lpR fFf"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-layout>
</#macro>

<#macro "screen-header">
    <m-screen-header <#if .node["@elevated"]! != "false">elevated</#if> class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-header>
</#macro>

<#macro "screen-toolbar">
    <m-screen-toolbar class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-toolbar>
</#macro>

<#macro "screen-content">
    <m-screen-content class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-content>
</#macro>
```

### 4. Application Code (Refactor)
#### [MODIFY] [huddle.xml](file:///home/byersa/IdeaProjects/huddle-ai-project/runtime/component/huddle/screen/huddle.xml)
Ensure `huddle.xml` adheres to the new tags (already done, but double check).

## Verification Plan

### Manual Verification
1.  **Code Inspection**: Check `HuddleVue.qvt2.js` for component definitions and `DefaultScreenMacros` for correct tag names.
2.  **Runtime Verification**:
    -   Fetch rendered HTML. It should now show `<m-screen-layout>` tags (which Vue will then mount).
    -   **Browser Test**: Verify the page still renders correctly. If the Vue components aren't registered properly, the layout will break (elements won't render or will show as unknown tags).
