# Moqui Localization (L10n) Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/global/localization.md` - English-first convention, terminology standards

---

## Development Workflow

### English-First Development

```
1. Write screen/service → English labels, titles, messages
2. Test functionality → Verify in English
3. Add translations → Create LocalizedMessage entries for Spanish (es/es_CL)
4. Verify translations → Test with locale set to Spanish
```

### Correct Pattern
```xml
<!-- ✅ CORRECT: English in screen definition -->
<field name="customerName">
    <default-field title="Customer Name">
        <text-line/>
    </default-field>
</field>

<!-- ✅ CORRECT: English in service message -->
<return error="true" message="Party ID is required"/>

<!-- Then add translation in l10n file -->
<moqui.basic.LocalizedMessage original="Customer Name" locale="es" localized="Nombre del Cliente"/>
<moqui.basic.LocalizedMessage original="Party ID is required" locale="es" localized="El ID de la Parte es requerido"/>
```

### Exception: Legacy Code
When modifying existing code that uses Spanish directly:
- **Small changes**: Match existing style for consistency within the file
- **Significant changes**: Consider refactoring to English + translation
- **New features**: Always use English first

---

## Localization Entity Patterns

### LocalizedMessage vs LocalizedEntityField

| Entity | Purpose | Key Field |
|--------|---------|-----------|
| `LocalizedMessage` | Translate UI text, labels, field names | `original` (text to translate) |
| `LocalizedEntityField` | Translate specific record values | `pkValue` (primary key of record) |

---

## LocalizedMessage Templates

### UI Text and Labels
```xml
<moqui.basic.LocalizedMessage original="Customer Name"
                              locale="es"
                              localized="Nombre del Cliente"/>

<moqui.basic.LocalizedMessage original="Save"
                              locale="es"
                              localized="Guardar"/>
```

### Error and Validation Messages
```xml
<moqui.basic.LocalizedMessage original="Field is required"
                              locale="es"
                              localized="Campo requerido"/>

<moqui.basic.LocalizedMessage original="Invalid email format"
                              locale="es"
                              localized="Formato de correo electrónico inválido"/>
```

### Dynamic Messages with Parameters
```xml
<moqui.basic.LocalizedMessage original="Document ${documentNumber} created successfully"
                              locale="es"
                              localized="Documento ${documentNumber} creado exitosamente"/>
```

---

## LocalizedEntityField Templates

**IMPORTANT: Requires `pkValue` (primary key of the specific record)**

### Enumeration Descriptions
```xml
<moqui.basic.LocalizedEntityField entityName="moqui.basic.Enumeration"
                                  fieldName="description"
                                  pkValue="FtdtInvoice"
                                  locale="es"
                                  localized="Factura Electrónica"/>
```

### Status Item Descriptions
```xml
<moqui.basic.LocalizedEntityField entityName="moqui.basic.StatusItem"
                                  fieldName="description"
                                  pkValue="FtdDraft"
                                  locale="es"
                                  localized="Borrador"/>
```

---

## Common Mistake to Avoid

**WRONG - Using LocalizedEntityField without pkValue:**
```xml
<!-- This will FAIL with integrity constraint violation -->
<moqui.basic.LocalizedEntityField entityName="Entity"
                                  fieldName="fieldName"
                                  locale="es"
                                  localized="Translation"/>
<!-- Missing required pkValue! -->
```

**CORRECT - Use LocalizedMessage for field names:**
```xml
<moqui.basic.LocalizedMessage original="Log ID"
                              locale="es"
                              localized="ID de Registro"/>
```

---

## File Organization

### Naming Convention
```
data/
├── {ComponentName}L10nData_es.xml    # Spanish translations
├── {ComponentName}L10nData_pt.xml    # Portuguese translations
└── {ComponentName}L10nData_en.xml    # English overrides (if needed)
```

### File Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- {COMPONENT_NAME} Localization - Spanish (Chilean) -->
    <!-- Component: {component-name} -->
    <!-- Last Updated: {date} -->

    <!-- =========================== -->
    <!-- UI Text and Labels -->
    <!-- =========================== -->
    <moqui.basic.LocalizedMessage original="..." locale="es" localized="..."/>

    <!-- =========================== -->
    <!-- Entity Field Translations -->
    <!-- =========================== -->
    <moqui.basic.LocalizedEntityField entityName="..." fieldName="..." pkValue="..." locale="es" localized="..."/>

    <!-- =========================== -->
    <!-- Error Messages -->
    <!-- =========================== -->
    <moqui.basic.LocalizedMessage original="..." locale="es" localized="..."/>
</entity-facade-xml>
```

---

## Accessing Translations in Code

### In Service Actions
```xml
<actions>
    <!-- Get localized message -->
    <set field="message" from="ec.l10n.localize('Document created successfully')"/>

    <!-- Format localized date -->
    <set field="formattedDate" from="ec.l10n.format(dateField, 'dd/MM/yyyy')"/>
</actions>
```

### In Groovy Scripts
```groovy
// Get localized message
String message = ec.l10n.localize("Original text")

// Format with locale
String formattedNumber = ec.l10n.format(amount, "#,##0.00")
String formattedDate = ec.l10n.format(timestamp, "dd 'de' MMMM 'de' yyyy")
```

---

## Locale Resolution Priority

Moqui resolves the active locale in this order:
1. **User preference** (`PreferredLocale` in UserPreference)
2. **Session attribute** (`moqui.locale` session attribute)
3. **Request parameter** (`moqui.locale` request parameter)
4. **Accept-Language header** (from HTTP request)
5. **System default locale** (`default-locale` in moqui-conf)

---

## Quality Checklist

**File Organization:**
- [ ] L10n files follow naming convention
- [ ] Translations grouped by category
- [ ] Comments indicate purpose

**LocalizedMessage:**
- [ ] All UI labels translated (static text)
- [ ] All dynamic labels translated (`${}` strings with English text)
- [ ] Error messages translated
- [ ] Dynamic parameters preserved (${var}) — only translate static text portions
- [ ] Conditional fallback text checked (e.g., `${val ?: 'English'}` → translate 'English')

**LocalizedEntityField:**
- [ ] pkValue specified for all entries
- [ ] Enumeration descriptions translated
- [ ] Status items translated

**Consistency:**
- [ ] Terminology consistent across files
- [ ] Regional conventions followed
- [ ] No duplicate entries