---
name: moqui-l10n
description: |
  Moqui Framework localization and internationalization patterns including LocalizedMessage, LocalizedEntityField, translation files, and English-first development.

  Use this skill when:
  - Creating localization files (*L10n*.xml in data/ directories)
  - Adding translations for UI labels and messages
  - Translating enumeration and status descriptions
  - Working with LocalizedMessage vs LocalizedEntityField
  - Following English-first development convention
---

# Moqui Localization Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/l10n_patterns.md` | LocalizedMessage, LocalizedEntityField, translation workflow |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Localization (L10n) Best Practices"** - CRITICAL: Always add translations, file structure
- **"### LocalizedEntityField Usage Patterns"** - Correct vs incorrect usage, guidelines
- **"### CRITICAL: Determining Which Translation File to Use"** - File selection criteria
- **"### Workflow for Adding Translations"** - Step-by-step translation process
- **"### Example: Adding a New Status"** - Complete l10n example with StatusItem

## Quick Reference

### LocalizedMessage (UI Text)
```xml
<moqui.basic.LocalizedMessage original="Customer Name"
                              locale="es"
                              localized="Nombre del Cliente"/>
```

### LocalizedEntityField (Record Values)
```xml
<moqui.basic.LocalizedEntityField entityName="moqui.basic.Enumeration"
                                  fieldName="description"
                                  pkValue="FtdtInvoice"
                                  locale="es"
                                  localized="Factura Electrónica"/>
```

## Key Principles

1. **English First**: Write screens/services in English, add translations separately
2. **LocalizedMessage**: For UI labels, field titles, error messages
3. **LocalizedEntityField**: For entity record values (requires pkValue!)
4. **Dynamic Parameters**: Preserve ${var} placeholders in translations