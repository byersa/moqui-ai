# App Registration & Home Screen

## Visibility Conditions

The Moqui home screen (`/qapps`) displays app tiles for each app registered under `apps.xml`. For an app to be visible, **three conditions** must be met (checked in `AppList.xml`):

1. **Screen is found** — The screen XML file exists and is properly mounted
2. **No required parameters** — The root app screen must NOT have `<parameter required="true"/>`
3. **`isPermitted()` returns true** — Artifact authorization seed data must grant access

## Mounting a Standalone App

Mount your app under `apps.xml` (NOT `webroot.xml`) in your `MoquiConf.xml`:

```xml
<moqui-conf xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/moqui-conf-3.xsd">
    <screen-facade>
        <!-- Standalone app on home screen (alongside MarbleERP, System, Tools) -->
        <screen location="component://webroot/screen/webroot/apps.xml">
            <subscreens-item name="myapp" menu-title="My Application" menu-index="20"
                location="component://MyComponent/screen/MyApp.xml"/>
        </screen>
    </screen-facade>
</moqui-conf>
```

## Root App Screen Requirements

A standalone app's root screen needs specific attributes to work with the Quasar SPA:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
    default-menu-title="My Application"
    menu-image="fa fa-cogs" menu-image-type="icon"
    server-static="vuet,qvt">

    <always-actions>
        <service-call name="mantle.party.PartyServices.setup#UserOrganizationInfo" out-map="context"/>
    </always-actions>

    <subscreens default-item="Dashboard" always-use-full-path="true"/>

    <widgets>
        <subscreens-panel id="MyAppPanel" type="popup" title="My Application"/>
    </widgets>
</screen>
```

### Key Attributes

| Attribute | Purpose |
|-----------|---------|
| `server-static="vuet,qvt"` | Required for Quasar/Vue UI — pre-renders static screen structure |
| `subscreens-panel type="popup"` | Creates the left-drawer navigation menu (standard for standalone apps) |
| `always-use-full-path="true"` | Ensures URL paths always include the subscreen name |
| `menu-image` | Icon class shown on the home screen app tile (FontAwesome) |
| `menu-image-type="icon"` | Specifies the image is an icon class, not a URL |

**Do NOT add `<parameter>` elements to root app screens** — the home screen skips apps with required parameters.

## Artifact Authorization (Required!)

Without artifact authorization, the app will NOT appear on the home screen. Create a seed data file:

```xml
<!-- data/MyComponentSetupData.xml -->
<entity-facade-xml type="seed">
    <artifactGroups artifactGroupId="MY_APP" description="My App (via root screen)">
        <artifacts artifactName="component://MyComponent/screen/MyApp.xml"
            artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
        <authz artifactAuthzId="MY_APP_ADMIN" userGroupId="ADMIN"
            authzTypeEnumId="AUTHZT_ALWAYS" authzActionEnumId="AUTHZA_ALL"/>
    </artifactGroups>

    <!-- REST API access (if applicable) -->
    <artifactGroups artifactGroupId="MY_API" description="My App REST API">
        <artifacts artifactTypeEnumId="AT_REST_PATH" inheritAuthz="Y" artifactName="/myapp"/>
        <authz artifactAuthzId="MY_API_ADMIN" userGroupId="ADMIN"
            authzTypeEnumId="AUTHZT_ALWAYS" authzActionEnumId="AUTHZA_ALL"/>
    </artifactGroups>
</entity-facade-xml>
```

### Authorization Rules

- `artifactGroupId` and `artifactAuthzId` must be globally unique
- `inheritAuthz="Y"` means all subscreens/sub-endpoints inherit the authorization
- Add `<filters entityFilterSetId="MANTLE_ACTIVE_ORG"/>` if the app needs org-level data filtering
- Add multiple `<authz>` elements to grant access to different user groups
- Data type should be `seed` (reloaded every time) for authorization records
- After creating/modifying, reload data: `java -jar moqui.war load types=seed`

## Mounting Inside MarbleERP vs Standalone

| Goal | Mount Under | URL Pattern |
|------|------------|-------------|
| Standalone app on home screen | `component://webroot/screen/webroot/apps.xml` | `/qapps/myapp` |
| New tab inside MarbleERP | `component://MarbleERP/screen/marble.xml` | `/qapps/marble/MyModule` |
| Screen under existing MarbleERP module | `component://MarbleERP/screen/marble/Customer.xml` | `/qapps/marble/Customer/MyScreen` |

## How Quasar SPA Navigation Works

The Quasar UI (`/qapps/`) is a single-page application:
- `basePath` = `/apps` (used for server data fetching — the SPA fetches screen data from `/apps/...`)
- `linkBasePath` = `/qapps` (used for displayed URLs in the browser address bar)
- The `menuData` transition on `webroot.xml` provides the navigation menu structure to the SPA
- `qapps.xml` has `allow-extra-path="true"` and renders screens from the shared `apps.xml` screen tree