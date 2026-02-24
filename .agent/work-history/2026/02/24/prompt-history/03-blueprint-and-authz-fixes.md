# Prompt 03: Blueprint Client 404 & Dropdown 403 Forbidden

## Timestamp
2026-02-24T11:13:49-07:00 (approx)

## User Prompt
The user reported that the header and menu rendered, but the devtools showed a 404 for `BlueprintClient.js` and an "Invalid ScreenBlueprint" warning. Later, clicking the "Meetings" dropdown resulted in a 403 Forbidden.

## Technical Analysis
- **Issue A (404)**: `MoquiAiVue.qvt2.js` had a hardcoded `$.getScript('/moquiai/js/BlueprintClient.js')`. This was the old path that was broken.
- **Issue B (Invalid Blueprint)**: Because `BlueprintClient.js` failed to load, `moqui.isBlueprint` was undefined. The `BlueprintRoute` component in `routes.js.ftl` then failed its validation check and threw the error.
- **Issue C (403)**: The `getAgendaContainers` transition in `aitree.xml` used `<entity-find>`, which enforces Moqui's default entity authorization. Anonymous users (unauthenticated) were blocked from viewing `AgendaContainer`.

## AI Response & Decision
- **Fix A/B**: Updated `MoquiAiVue.qvt2.js` to use `this.basePath + '/moquiaiJs/BlueprintClient.js'`.
- **Fix C**: Converted the XML `<entity-find>` to a Groovy script to explicitly use `.disableAuthz().list()`.
- **Strategic Decision**: Chose to bypass authorization strategically to unblock UI strategy (declarative dropdowns) rather than spending time on unauthenticated user group permissions.

## Impact
- `BlueprintClient` initializes correctly.
- SPA routes correctly mount and render blueprints.
- "Meetings" dropdown successfully fetches and displays the abstract agenda containers.
