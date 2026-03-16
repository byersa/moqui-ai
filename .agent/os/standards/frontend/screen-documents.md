# Screen Documents (In-App Help)

Moqui provides a built-in **Screen Document** system for attaching user-facing help documentation to screens. Documents are displayed via a help button in the Quasar navbar and rendered as Markdown in a dialog.

## Entity: `moqui.screen.ScreenDocument`

| Field | Type | PK | Description |
|-------|------|----|-------------|
| `screenLocation` | text-medium | Yes | `component://` path to the screen XML file |
| `docIndex` | number-integer | Yes | Sort order and identifier within a screen |
| `locale` | text-short | No | Locale code (e.g., `es`); null = all locales |
| `docTitle` | text-medium | No | Display title in help menu; defaults to filename if omitted |
| `docLocation` | text-medium | No | `component://` path to the Markdown file |

**Cache**: Entity is cached (`cache="true"`) and read with `useCache(true).disableAuthz()`.

## Directory Convention

Documentation files live in `{component}/document/` and mirror the `{component}/screen/` path structure:

```
{component}/
  screen/
    MyApp/
      FindOrder.xml
      OrderDetail.xml
  document/
    MyApp/
      FindOrder.md              # Help for FindOrder screen
      OrderDetail/
        ShippingInfo.md         # Help topic for OrderDetail screen
        PaymentDetails.md       # Another help topic
```

## Data Record Format

ScreenDocument records go in the component's **setup data file** (type `seed` or `seed-initial`):

```xml
<!-- Single document for a screen -->
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="1" docTitle="Help Topic Title"
    docLocation="component://{component-name}/document/{AppName}/{ScreenName}.md"/>

<!-- Multiple documents for a screen (ordered by docIndex) -->
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="1" docTitle="Screen Overview"
    docLocation="component://{component-name}/document/{AppName}/{ScreenName}/Overview.md"/>
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="2" docTitle="Columnas"
    docLocation="component://moit-utils/document/General/Columnas.md"/>
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="3" docTitle="Busquedas Guardadas"
    docLocation="component://moit-utils/document/General/BusquedasGuardadas.md"/>
<moqui.screen.ScreenDocument
    screenLocation="component://{component-name}/screen/{AppName}/{ScreenName}.xml"
    docIndex="4" docTitle="XLS"
    docLocation="component://moit-utils/document/General/XLS.md"/>
```

### docIndex Numbering Convention

- **1-99**: Screen-specific documentation (business context, workflows)
- **100+**: Reference documentation (used by SimpleScreens for status references, GL account class references)
- General docs (Columnas, BusquedasGuardadas, XLS) are typically placed after screen-specific docs

### docTitle Default Behavior

When `docTitle` is omitted, the framework extracts the title from the filename in `docLocation` (strips path and extension). SimpleScreens uses this pattern for reference docs:

```xml
<!-- docTitle defaults to "GL Account Class Reference" from filename -->
<moqui.screen.ScreenDocument
    screenLocation="component://SimpleScreens/screen/SimpleScreens/Accounting/GlAccount/FindGlAccount.xml"
    docIndex="100"
    docLocation="component://SimpleScreens/document/account/GL Account Class Reference.md"/>
```

## Reusable General Documentation

Cross-component documentation for common form-list features lives in a shared component (e.g., `moit-utils/document/General/`):

| File | Explains |
|------|----------|
| `Columnas.md` | Column customization in `select-columns` form-lists |
| `BusquedasGuardadas.md` | Saved search/find functionality |
| `XLS.md` | Excel export via `show-xlsx-button` |

Reference these from any component using `component://moit-utils/document/General/{Name}.md`.

## Locale Support

The `locale` field enables locale-specific documentation:

```xml
<!-- Spanish documentation -->
<moqui.screen.ScreenDocument screenLocation="..." docIndex="1" locale="es"
    docTitle="Ayuda" docLocation="component://.../help_es.md"/>

<!-- English documentation -->
<moqui.screen.ScreenDocument screenLocation="..." docIndex="1" locale="en"
    docTitle="Help" docLocation="component://.../help_en.md"/>
```

When `locale` is null, the document is shown for all locales. The framework filters by the user's current locale, matching both full locale (e.g., `es_CL`) and language-only (e.g., `es`).

## How It Works (Framework Internals)

1. **Auto-registered transition**: Every screen automatically gets a `screenDoc` transition (added by `ScreenDefinition` if not already present)
2. **Document list**: `getScreenDocumentInfoList()` queries `moqui.screen.ScreenDocument` for the screen's location, filters by locale, and returns `[title, index]` pairs
3. **Navbar integration**: The `screenDocList` is passed to the Quasar navbar via `navMenuList`, which renders a help button when documents exist
4. **Document rendering**: Clicking a help topic calls the `screenDoc` transition with `docIndex` parameter; the framework loads the `docLocation` resource and renders it as a template

## When to Create Screen Documents

| Screen Feature | Documentation Needed |
|----------------|---------------------|
| `form-list` with `select-columns="true"` | Reference `Columnas.md` general doc |
| `form-list` with `saved-finds="true"` | Reference `BusquedasGuardadas.md` general doc |
| `form-list` with `show-xlsx-button="true"` | Reference `XLS.md` general doc |
| Business-specific fields or workflows | Create screen-specific doc explaining the fields/workflow |
| Complex detail screens with multiple sections | Create per-section docs explaining each area |
| Screens where users need onboarding guidance | Create overview doc explaining screen purpose and common tasks |

## Checklist for Adding Screen Documentation

1. Create Markdown file(s) in `{component}/document/` matching screen path
2. Add `moqui.screen.ScreenDocument` records to the component's setup data file
3. Include general docs (Columnas, BusquedasGuardadas, XLS) for form-lists with those features enabled
4. Place screen-specific docs first (low docIndex), general docs after
5. Run `./gradlew load` to load the data records (or restart server if already loaded)
