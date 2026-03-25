# Architecture: Frontend Routing & SPA Data Loading

## SPA Routing using Vue Router
The Moqui AI stack uses Vue Router to manage client-side state without triggering full-page server reloads, creating a true Single Page Application experience inside the Moqui shell.

### Initialization Workflow:
1. **Entry Point:** The main wrapper template (`Quasar2Wrapper.qvt2.ftl`) injects the baseline libraries and triggers a request to the `routes.js` transition of the current app root (e.g. `aitree.xml`).
2. **Route Generation:** The `routes.js` transition in `aitree.xml` (or `webroot.xml`) runs a recursive Groovy script that traverses Moqui's `ScreenDefinition` tree. It produces a `routes.js` payload that pre-maps all reachable subscreens to a `BlueprintRoute` component.
3. **MoquiAiVue Execution:** `MoquiAiVue.qvt2.js` runs, binds to `#apps-root`, and hooks the generated `moqui.webrootRouter` to the active Vue application.

### Script Inclusion Traps:
- **Do not use hardcoded paths for JS resources** inside Moqui. If a script (like `BlueprintClient.js`) cannot be resolved from the webroot context natively, Moqui's `sri.buildUrl()` will default to `#` and append its cache buster, creating a `<script src="#?v=...">` tag. This invalid tag triggers dangerous `<text/html>` execution errors in the browser.
- **Solution:** Expose dedicated script-serving transitions (like `<transition name="moquiaiJs">`) on the root application screen (`aitree.xml`) to explicitly route JavaScript files that sit inside protected component directories.

## Dynamic Menu Dropdowns
To provide database-driven navigation, use the semantic `<menu-dropdown>` tag rather than hard-coding tabs.

### Tag Syntax
```xml
<menu-dropdown name="Meetings" transition="getAgendaContainers" label-field="name" key-field="agendaContainerId" url-parameter="agendaContainerId"/>
```
- **`name`**: Maps to an existing subscreen (e.g. `Meetings.xml`). Used as the target base URL.
- **`transition`**: The data source. An AJAX GET request will hit this transition when the dropdown is expanded.
- **`label-field`** & **`key-field`**: The JSON keys from the returned list to populate the dropdown label and URL query parameter dynamically.

### Fixing "403 Forbidden" on Transitions
If your dropdown AJAX transition needs to fetch data anonymously (without requiring the user to be logged in), Moqui's native `<entity-find>` will often reject the query due to strict entity authorization defaults, even if `require-session-token="false"`.

**Correct pattern for unauthenticated data querying:**
Use inline Groovy to explicitly strip authorization checks:
```xml
<transition name="getAgendaContainers" read-only="true" require-session-token="false">
    <actions>
        <script>
            def records = ec.entity.find("aitree.meeting.AgendaContainer")
                .condition("containerTypeEnumId", "AitContainerAbstract")
                .disableAuthz().list()
            ec.web.sendJsonResponse(records)
        </script>
    </actions>
    <default-response type="none"/>
</transition>
```

## Blueprint Loading & Content Negotiation
When fetching screen blueprints, ensure the server returns JSON and not the HTML shell.

### Forcing JSON Output
Browser `Accept: application/json` headers are sometimes ignored if a session `renderMode` is already established. Always append `?renderMode=qjson` to fetch URLs in the client-side router to force the `DeterministicVueRenderer`.

### Shell Collision Guard
The catch-all router (`/:pathMatch(.*)*`) will attempt to fetch a blueprint for every URL. If the user navigates to the app root (e.g., `/aitree/`), fetching this will return the full HTML shell, causing a JSON parsing error.
**Solution:** Implement a guard in the router to intercept root path requests and redirect them to a specific subscreen (e.g., `/Home`) that is guaranteed to return a blueprint.

## Boolean Prop Type Handling
Vue 3/Quasar 2 components strictly validate prop types. Moqui attributes like `flat="true"` are rendered as strings by default, triggering Vue warnings.
- **Solution:** Use Vue's colon-prefix (`:`) in Freemarker macros for all boolean attributes (e.g., `:flat="true"`).
- **Macro Logic:** In macros, handle defaults dynamically: `:elevated="${(.node["@elevated"]! != "false")?string}"`.

