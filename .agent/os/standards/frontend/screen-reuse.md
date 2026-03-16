# Screen Reuse Patterns

DRY principles for screens: avoid duplication, ensure consistency, single point of change.

## Transition Include
Reuse transitions across screens:
```xml
<transition-include name="searchPartyList"
    location="component://SimpleScreens/template/party/PartyForms.xml"/>
```

## Section Include
Reuse sections (widgets + actions):
```xml
<section-include name="BookingFacilityTypeSection"
    location="component://{component-name}/screen/BookingAdmin/FindBooking.xml"/>
```

## Form Extends
Extend and customize forms:
```xml
<form-single name="CreateCustomerForm"
    extends="component://{shared-component}/template/account/FindAccountTemplate.xml#CreateAccountForm">
    <field name="accountManagerId"><default-field><hidden/></default-field></field>
</form-single>
```

## Location Patterns
| Scope | Location | Example |
|-------|----------|--------|
| Cross-component | `template/` | Generic party forms |
| Within-component | `screen/` | App-specific sections |
| Framework-level | `SimpleScreens/template/` | Common widgets |

## Multi-App Screen Reuse via subscreens-item

When a base screen (e.g., `SimpleScreens/Task/TaskSummary.xml`) is mounted as a subscreen in multiple apps, any `screen-extend` transitions with **relative URLs resolve against the host app's screen tree at runtime**, not the original component's tree.

**Gotcha**: If the transition target exists in one app but not another, Moqui **silently disables the link** — no error, no log, just a grayed-out button.

Example: A `screen-extend` adds a transition `../../AuditLog`. Both AppA and AppB mount `TaskSummary` via `subscreens-item`, but only AppA has `AuditLog.xml` as a sibling screen. The link works in AppA but is silently disabled in AppB.

**Fix**: Add a `subscreens-item` with `menu-include="false"` in the app missing the target screen:

```xml
<!-- AppB.xml — mount the shared screen so relative transition URLs resolve -->
<subscreens default-item="Dashboard">
    <subscreens-item name="Task" location="component://SimpleScreens/screen/SimpleScreens/Task.xml"/>
    <subscreens-item name="AuditLog" location="component://my-component/screen/AppA/AuditLog.xml"
                     menu-include="false"/>
</subscreens>
```

**Diagnosis checklist** for disabled links in reused screens:
1. Identify the transition's relative URL in the `screen-extend`
2. Trace the `../..` path from the current screen in the **host app's** tree
3. Verify the target screen exists at that resolved path
4. If missing, add a hidden `subscreens-item` pointing to the existing screen

## Conditional Field Visibility in Shared Screens

When a shared screen is mounted in multiple apps, some fields may need to be visible in one app but hidden in others. Use **parent context variables** combined with `conditional-field` to control per-app field visibility.

**Pattern**: The parent app screen sets a context variable in `always-actions`. The shared child screen uses `conditional-field` with that variable to show or hide the field.

```xml
<!-- Parent app screen (e.g., AppAdmin.xml) -->
<always-actions>
    <set field="showDetailColumns" value="true"/>
</always-actions>
<subscreens>
    <subscreens-item name="Contacts" location="component://shared/screen/FindContact.xml"/>
</subscreens>
```

```xml
<!-- Shared screen (FindContact.xml) -->
<field name="taxIdentifier">
    <header-field show-order-by="true" title="Tax ID"><text-find/></header-field>
    <conditional-field title="Tax ID" condition="showDetailColumns"><display/></conditional-field>
    <default-field title="${showDetailColumns ? 'Tax ID' : ''}"><ignored/></default-field>
</field>
```

**How it works**:
- `conditional-field condition="showDetailColumns"` — renders the column only when the variable is truthy
- `default-field` with `<ignored/>` — completely hides the column (including header) in all other apps
- Ternary title `${showDetailColumns ? 'Tax ID' : ''}` — prevents an empty column header from appearing

**Why not `hide="true"`?** The `hide` attribute is static and unconditional. Using `conditional-field` allows the same screen definition to adapt per-app without duplication or screen-extend overrides.

## Best Practices
- Prefer `template/` for truly generic pieces
- Use `screen/` references for app-specific reuse within same component
- Document included items with comments for discoverability
- When mounting shared screens in multiple apps, verify all `screen-extend` transition targets resolve in each app's tree
- For per-app field visibility in shared screens, prefer `conditional-field` over `hide` attribute or screen-extend overrides