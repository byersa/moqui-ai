# Localization

### English-First Development (CRITICAL)

**All screens and services MUST be implemented in English first, then translated.**

```xml
<!-- CORRECT: English in screen definition -->
<default-field title="Customer Name">
    <text-line/>
</default-field>

<!-- CORRECT: English in service message -->
<return error="true" message="Party ID is required"/>

<!-- Then add translation in l10n file -->
<moqui.basic.LocalizedMessage original="Customer Name" locale="es"
                              localized="Nombre del Cliente"/>
```

**WRONG: Hardcoded Spanish:**
```xml
<!-- NEVER do this -->
<default-field title="Nombre del Cliente">
```

### Localization Entities

| Entity | Purpose | Key Field |
|--------|---------|-----------|
| `LocalizedMessage` | UI text, labels, messages | `original` |
| `LocalizedEntityField` | Specific record values | `pkValue` |

### LocalizedMessage Pattern

```xml
<!-- UI labels -->
<moqui.basic.LocalizedMessage original="Customer Name" locale="es"
                              localized="Nombre del Cliente"/>

<!-- Error messages -->
<moqui.basic.LocalizedMessage original="Field is required" locale="es"
                              localized="Campo requerido"/>

<!-- Dynamic messages (preserve ${} variables) -->
<moqui.basic.LocalizedMessage original="Document ${documentNumber} created"
                              locale="es"
                              localized="Documento ${documentNumber} creado"/>
```

### LocalizedEntityField Pattern (CRITICAL)

**REQUIRES `pkValue` (primary key):**

```xml
<!-- CORRECT: With pkValue -->
<moqui.basic.LocalizedEntityField entityName="moqui.basic.Enumeration"
                                  fieldName="description"
                                  pkValue="PtFinishedGood"
                                  locale="es"
                                  localized="Producto Terminado"/>

<!-- WRONG: Missing pkValue -->
<moqui.basic.LocalizedEntityField entityName="Entity"
                                  fieldName="fieldName"
                                  locale="es"
                                  localized="Translation"/>
```

### File Organization

**Standard (small-to-medium components):**
```
data/
├── {Component}L10nData_es.xml    # Spanish translations
├── {Component}L10nData_pt.xml    # Portuguese translations
└── {Component}L10nData_en.xml    # English overrides (if needed)
```

**Area-based splitting (large components with 200+ translatable strings):**

When a single L10n file would exceed ~200 entries, split by functional area for maintainability:

```
data/
├── {Component}L10n{Locale}_{Area1}.xml   # e.g., {Component}L10nEs_Portal.xml
├── {Component}L10n{Locale}_{Area2}.xml   # e.g., {Component}L10nEs_Desk.xml
└── {Component}L10n{Locale}_{Area3}.xml   # e.g., {Component}L10nEs_Services.xml
```

**Naming convention**: `{Component}L10n{Locale}_{Area}.xml`
- `{Component}`: Component name (e.g., `MyComponent`)
- `{Locale}`: Language code (e.g., `Es`, `Pt`)
- `{Area}`: Functional area (e.g., `Portal`, `Desk`, `Admin`, `Services`)

**Guidelines for area splitting:**
- Group by user-facing area (portal vs back-office vs services)
- Each file is `type="seed"` (same as single-file approach)
- Organize entries within each file with XML comments by screen/service
- Deduplicate: strings shared across areas go in the file for the primary area
- Moqui matches `original` text globally, so duplicates across files are harmless but wasteful

### L10n File Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- {COMPONENT} Localization - Spanish -->

    <!-- UI Labels -->
    <moqui.basic.LocalizedMessage original="Save" locale="es" localized="Guardar"/>
    <moqui.basic.LocalizedMessage original="Cancel" locale="es" localized="Cancelar"/>

    <!-- Error Messages -->
    <moqui.basic.LocalizedMessage original="Field is required" locale="es"
                                  localized="Campo requerido"/>

    <!-- Enumeration Descriptions -->
    <moqui.basic.LocalizedEntityField entityName="moqui.basic.Enumeration"
                                      fieldName="description"
                                      pkValue="EnumId"
                                      locale="es"
                                      localized="Traducción"/>
</entity-facade-xml>
```

### Proper Spanish Orthography (CRITICAL)

**All Spanish translations MUST use correct diacritical marks (tildes/acentos).** Omitting accents is an orthographic error that produces incorrect Spanish text.

Required characters: **á, é, í, ó, ú, ñ, ü** — use them wherever Spanish orthography demands.

Common words that REQUIRE accents:
- Número, Código, Categoría, Descripción, Dirección, Ubicación
- Información, Configuración, Operación, Recepción, Facturación
- Artículo, Vehículo, Último, Económico, Público
- Búsqueda, Página, También, Más, Está, Será
- Almacén, Según, Después, Además

**WRONG:**
```xml
<moqui.basic.LocalizedMessage original="Serial Number" locale="es" localized="Numero de Serie"/>
<moqui.basic.LocalizedMessage original="Category" locale="es" localized="Categoria"/>
```

**CORRECT:**
```xml
<moqui.basic.LocalizedMessage original="Serial Number" locale="es" localized="Número de Serie"/>
<moqui.basic.LocalizedMessage original="Category" locale="es" localized="Categoría"/>
```

### Standard Terminology (Spanish/Chilean)

| English | Spanish |
|---------|---------|
| Save | Guardar |
| Cancel | Cancelar |
| Delete | Eliminar |
| Edit | Editar |
| Search | Buscar |
| Create | Crear |
| Update | Actualizar |
| Customer | Cliente |
| Invoice | Factura |
| Order | Pedido |
| Status | Estado |
| Date | Fecha |
| Amount | Monto |
| Number | Número |
| Code | Código |
| Category | Categoría |
| Description | Descripción |
| Address | Dirección |
| Location | Ubicación |
| Information | Información |
| Configuration | Configuración |
| Reception | Recepción |
| Page | Página |
| Article | Artículo |
| Vehicle | Vehículo |
| Warehouse | Almacén |

### Chilean Conventions

| Category | Convention |
|----------|------------|
| Formal "you" | Use "usted" forms |
| Date format | DD/MM/YYYY |
| Currency | $X.XXX (dot for thousands) |
| Numbers | Comma for decimals |
| Button labels | Infinitive verbs |

### Accessing Translations in Code

**In screen XML:**
```xml
<!-- Automatic via l10n mechanism -->
<default-field title="Customer Name">
```

**In services:**
```xml
<set field="message" from="ec.l10n.localize('Document created successfully')"/>
```

**In Groovy:**
```groovy
String message = ec.l10n.localize("Original text")
String formattedDate = ec.l10n.format(timestamp, "dd 'de' MMMM 'de' yyyy")
```

### Localizing Text in CDATA/Render-Mode Blocks

Moqui's `LocalizedMessage` **cannot intercept** text inside `<render-mode><text type="html"><![CDATA[...]]>` blocks. These are FreeMarker templates processed outside the localization pipeline.

**Pattern**: Pre-compute localized words in the screen's `<actions>` block, then use the variables in the FreeMarker template:

```xml
<actions>
    <!-- Pre-compute localized words for use in CDATA blocks -->
    <set field="phaseWord" from="ec.l10n.localize('Phase')"/>
    <set field="statusLabel" from="ec.l10n.localize('Status')"/>
</actions>

<widgets>
    <render-mode><text type="html"><![CDATA[
        <h3>${currentPhaseName} ${phaseWord}</h3>
        <span>${statusLabel}: ${statusDescription}</span>
    ]]></text></render-mode>
</widgets>
```

Each localized word needs a corresponding `LocalizedMessage` entry:
```xml
<moqui.basic.LocalizedMessage original="Phase" locale="es" localized="Fase"/>
```

> **See also**: `standards/frontend/html-injection.md` for general render-mode patterns.

### Anti-Pattern: String Manipulation for Display Names

**NEVER** derive display names from entity IDs using string manipulation:

```xml
<!-- WRONG: Produces non-localizable English text -->
<set field="phaseName" from="statusId?.replace('WeGe', '')"/>
<!-- Result: "Draft", "Preparation" — not translatable -->
```

**Instead**, look up the entity's `description` field, which Moqui auto-localizes via `LocalizedEntityField`:

```xml
<!-- CORRECT: Auto-localized via LocalizedEntityField -->
<entity-find-one entity-name="moqui.basic.StatusItem" value-field="statusItem">
    <field-map field-name="statusId"/>
</entity-find-one>
<set field="phaseName" from="statusItem?.description ?: 'Unknown'"/>
<!-- Result: "Borrador" for Spanish users (auto-localized) -->
```

This applies to `StatusItem`, `Enumeration`, `RoleType`, and any entity with `LocalizedEntityField` translations.

### Exception: Legacy Code

When modifying existing code with hardcoded Spanish:
- **Small changes**: Match existing style
- **Significant changes**: Refactor to English + translation
- **New features**: Always use English first

### Gender-Neutral Language

- Use "(a)" suffix for roles: Director(a), Secretario(a)
- Alternative: Use slash format: Director/a, Secretario/a
- Consider context and legal requirements

---

## Translation Maintenance

### Finding Labels That Need Translation

Labels requiring translation appear in multiple locations. Use this systematic approach to find all untranslated text in a component:

#### 1. Screen Labels (Primary Source)

Search for common patterns in screen XML files:

```bash
# Empty state messages ("No ... yet", "No ... available")
grep -r "No.*yet\|No.*available\|No.*attached" runtime/component/{component}/screen/ --include="*.xml"

# Static label text elements
grep -rE '<label text="[A-Z][^$]*"' runtime/component/{component}/screen/ --include="*.xml"

# Container box headers
grep -rE 'box-header title="[^$]*"' runtime/component/{component}/screen/ --include="*.xml"

# Form field titles
grep -rE 'title="[A-Z][^$]*"' runtime/component/{component}/screen/ --include="*.xml" | grep -v "\.groovy"

# Button text
grep -rE 'button-text="[^$]*"\|text="[A-Z]' runtime/component/{component}/screen/ --include="*.xml"

# Confirmation messages
grep -rE 'confirmation="[^$]*"' runtime/component/{component}/screen/ --include="*.xml"
```

#### 2. Service Messages

```bash
# Error messages in services
grep -rE 'message="[A-Z][^$]*"' runtime/component/{component}/service/ --include="*.xml"

# Validation messages
grep -rE 'error-.*="[^$]*"' runtime/component/{component}/service/ --include="*.xml"
```

#### 3. Entity Data (Enumerations, Status Items)

```bash
# Enumeration descriptions
grep -rE '<Enumeration.*description="[^$]*"' runtime/component/{component}/data/ --include="*.xml"

# Status item descriptions
grep -rE '<StatusItem.*description="[^$]*"' runtime/component/{component}/data/ --include="*.xml"
```

#### 4. Cross-Reference with Existing Translations

After finding labels, verify they're not already translated:

```bash
# Search existing L10n files for a specific original text
grep -r 'original="Your text here"' runtime/component/{component}/data/ --include="*l10n*.xml" --include="*L10n*.xml"
```

#### 5. Common Patterns Often Missed

| Pattern | Example | Why Missed |
|---------|---------|------------|
| Empty state messages | "No items yet" | In `<fail-widgets>` sections |
| Confirmation dialogs | "Are you sure..." | In `confirmation=` attributes |
| Dynamic labels | `"${var} Phase Actions"` | Audit scripts excluded `${}` strings |
| Conditional fallbacks | `${val ?: 'Not Assigned'}` | English text hidden inside expression |
| Nested sections | Headers inside sections | Deep in XML hierarchy |
| Form list headers | `<header-field title=...>` | Separate from default-field |
| Error messages with vars | `"Not found: ${id}"` | In `<return message=...>` elements |
| Display text with vars | `"For: ${x} \| Against: ${y}"` | In `<display text=...>` elements |

#### 6. Comprehensive Audit Script

```bash
#!/bin/bash
# Find all translatable text in a component's screens
cd runtime/component/{component}

# ---- STATIC TEXT (no ${} expressions) ----
grep -hroE '(title|text|button-text|confirmation)="[^"$]+[A-Z][^"$]*"' screen/ | \
  sed 's/.*="\([^"]*\)"/\1/' | \
  sort -u > /tmp/static_labels.txt

# ---- DYNAMIC TEXT (WITH ${} expressions) ----
# These are the most commonly missed — they ALSO need LocalizedMessage entries
grep -hrnE '(title|text|button-text|confirmation|message)="[^"]*\$\{[^"]*"' screen/ | \
  grep -i '[a-z]' > /tmp/dynamic_labels_with_context.txt

# Extract just the string values for cross-reference
grep -hroE '(title|text|button-text|confirmation|message)="[^"]*\$\{[^"]*"' screen/ | \
  sed 's/.*="\([^"]*\)"/\1/' | \
  sort -u > /tmp/dynamic_labels.txt

# ---- ALREADY TRANSLATED ----
grep -hroE 'original="[^"]*"' data/*l10n*.xml data/*L10n*.xml 2>/dev/null | \
  sed 's/original="\([^"]*\)"/\1/' | \
  sort -u > /tmp/translated.txt

# ---- REPORT ----
echo "=== Untranslated STATIC Labels ==="
comm -23 /tmp/static_labels.txt /tmp/translated.txt

echo ""
echo "=== Untranslated DYNAMIC Labels ==="
comm -23 /tmp/dynamic_labels.txt /tmp/translated.txt

echo ""
echo "=== Dynamic Labels with File/Line Context ==="
cat /tmp/dynamic_labels_with_context.txt
```

> **IMPORTANT**: The dynamic text audit (`${}` strings) is critical. Moqui's `LocalizedMessage`
> matches the template string *before* variable substitution, so `original="${var} Phase Actions"`
> works correctly. Every dynamic string with English text must have a corresponding entry.

### Adding New Translations

1. **Find the correct L10n file** using the component's file organization
2. **Check if translation exists** in any L10n file (avoid duplicates)
3. **Match text exactly** - the `original` attribute must match screen text precisely, including:
   - Whitespace
   - Punctuation
   - Template expressions (`${...}`)
   - Decorative/formatting characters (bullets `•`, arrows `→`, dashes `—`, etc.)

   > **Gotcha**: Localization silently fails on mismatch — no error is logged. If a screen label uses `"• Some text"` but the L10n entry has `"Some text"`, the translation is never applied. Always copy the exact string from the screen XML `text` attribute.
4. **Add to appropriate section** within the file (organized by screen/function)
5. **Test immediately** - clear cache and verify in browser with target locale

### Updating Existing Translations

1. Search all L10n files for current translation
2. Update in appropriate file
3. Clear cache and test
4. Document reason for change if terminology update

### Version Control

- Commit translations with related feature
- Use descriptive commit messages mentioning L10n
- Tag releases that include significant translation updates

## Translation Testing Checklist

### Screen Coverage
- [ ] All menu items display in target language
- [ ] All form labels translated
- [ ] All buttons show translated text
- [ ] Error messages appear in target language
- [ ] Email templates use target language

### Entity Field Coverage
- [ ] All enumerations show translated descriptions
- [ ] Status items display in target language
- [ ] Role types show translated names
- [ ] Party classifications translated

### Special Characters
- [ ] Verify accented characters display correctly
- [ ] Check PDF generation with special characters
- [ ] Validate email encoding for target language text
