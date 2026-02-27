# Moqui Subscreen Rendering & Blueprint Architecture Strategy

During the implementation of a client-side routing strategy backed by dynamically generated JSON-LD Blueprints in Moqui via a custom `DeterministicVueRenderer`, a number of vital architectural lessons were learned regarding Vue 3 integration, single page application (SPA) state manipulation, and Moqui's server-side subscreen logic.

## 1. Vue SPA Router and Component Initialization

### State Management is Crucial for Initialization
When a single page Vue application initializes inside an existing HTML envelope (like Moqui's traditional `renderMode=html`), it needs to kick off the routing event to realize where it is.
The `setUrl` method must be explicitly called on the main Vue application instance (e.g., `window.moqui.webrootVue` or equivalent) inside its `mounted` hook to populate the `currentPathList` so nested routers (like `<m-subscreens-active>`) have paths to attach to.

### Reactive Component State Dependencies
Any Vue component definition passing an object back in the `data` method *must* provide reactivity for all top-level attributes used in its `template`. If a component (e.g., a dynamic dialog component) utilizes reactive properties like `isShown` or `curComponent` in its template, they must be fully declared in the `data: function() { return { ... } }` object. 

Failure to do so does not always throw an explicit error in production builds but results in "silent component failure." The element will simply fail to render its children or appear on the screen at all.

### Deterministic Loading Order
If a global Vue app script (`MoquiAiVue.qvt2.js`) mounts elements, and a custom Blueprint Renderer script (`BlueprintClient.js`) depends on registering custom Vue components globally (via `app.component(...)`), you cannot load them async randomly (e.g. `$.getScript()`). They must load deterministically via `footer_scripts` definitions within a PreActions script to avoid race conditions.

Fallback mechanism for Vue instance availability can be designed via timeout logic:
```javascript
if (moqui.webrootVue && moqui.webrootVue.component) {
    moqui.webrootVue.component('m-blueprint-node', BlueprintNode);
} else {
    setTimeout(() => moqui.registerBlueprintNode(moqui.webrootVue), 500);
}
```

## 2. Server-Side Screen Logic vs. Client-Side Rendering

When turning an XML Screen definition into a structural JSON "Blueprint" document using a `ScreenWidgetRender` implementation (like `DeterministicVueRenderer`):

### Server-Side Condition Evaluation
The framework renderer walks through the `node.getChildren()` of `<widgets>`. When it encounters control structures—specifically the `<section>` and `<condition>` components—the renderer must evaluate these expressions **on the server side**, exactly how Moqui does inherently. 

If the renderer merely maps `<condition>` tags to JSON elements, the frontend Vue client receives raw `<condition>` and `<expression>` elements. The Vue SPA will panic with warnings `Failed to resolve component: m-condition` and will completely abort the render tree for that branch.
You must wrap them in the standard Groovy condition logic:
```groovy
boolean conditionPassed = sri.ec.resource.condition(node.attribute("condition"), "section.condition")
// ... conditionAction evaluation ...
if (conditionPassed) {
    walkWidgets(node.first("widgets"), children, sri, depth)
} else {
    walkWidgets(node.first("fail-widgets"), children, sri, depth)
}
```

## 3. Moqui's `lastStandalone` Path Resolution

When nesting SPA subscreens asynchronously via XHR requests, fetching the literal HTML/JSON payload required by a child screen presents a challenge in Moqui:

- If you fetch `/aitree/ActiveMeetings/MeetingDiscussion` with `lastStandalone=true` (or `lastStandalone=1`), Moqui returns *only* the `MeetingDiscussion` leaf node. **It will skip any ancestor screen components.**
- If you have an intermediate `ActiveMeetings` wrapper that has tabs for its default `MeetingDiscussion` subscreen, `lastStandalone=true` bypasses the tab wrapper completely.
- Moqui provides a mechanism to prune the top `N` screens instead of fetching only the last leaf: **Negative Standalone Parameters**. 

By calculating what the SPA already has rendered using relative indices (`basePathSize` and `pathIndex`), you can compute an inversion value:
`lastStandalone: -(pathIndex + root.basePathSize)`

This instructs the `ScreenUrlInfo` processor on the server: "Skip exactly `-X` ancestor screens from the root of the hierarchy, but render everything recursively from this level downwards, including my default subscreens!"
This successfully mounts intermediate containers like tabs or navigation drawers that house the ultimate default subscreen.
