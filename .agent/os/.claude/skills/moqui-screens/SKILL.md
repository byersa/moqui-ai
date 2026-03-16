---
name: moqui-screens
description: |
  Moqui Framework screen and form development patterns including screen layout, forms, navigation, transitions, widgets, and rendering modes.

  Use this skill when:
  - Creating or modifying screen definitions (*.xml in screen/ directories)
  - Building form-single and form-list forms
  - Implementing screen transitions and navigation
  - Working with container-box, container-dialog, and other widgets
  - Configuring subscreens and menu navigation
  - Implementing server-search dropdowns
  - Working with rendering modes (/apps, /qapps - Quasar is the standard)
  - Debugging missing data in screens (EntityFilter context issues)
---

# Moqui Screen Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/screen_patterns.md` | Screen layout, containers, navigation, transitions, server-search |
| `../../references/form_patterns.md` | Form fields, validation, entity-options, dynamic forms, row-actions |
| `../../references/rendering_modes.md` | Browser rendering modes, URL prefixes, SPA vs server-rendered |
| `../../references/marble_erp.md` | MarbleERP architecture, modules, extension patterns, SimpleScreens reuse |

### Deep Reference (framework-guide.md)

For advanced topics, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"#### Dependent Dropdowns with `depends-on`"** - `depends-on` syntax, `dynamic-options`, cascading selects
- **"#### Screen Context and AJAX Transitions"** - always-actions execution, context availability in AJAX calls
- **"#### Context Persistence: Request vs Session Scope"** - when to use `ec.web.sessionAttributes` vs `ec.user.context`
- **"### File Download Transitions in Quasar Apps"** - `/apps/` vs `/qapps/` paths, binary file responses, `sendResourceResponse`
- **"## Screen Documentation (In-App Help)"** - ScreenDocument entity, document/ directory, Quasar help button
- **"## Extending MarbleERP"** - MoquiConf.xml mounting patterns, screen overrides, standalone apps (in `../../references/marble_erp.md`)

### Standards Reference (Declarative)

For declarative conventions, see these standards:
- `standards/frontend/rich-text-editor.md` - WYSIWYG editor patterns, HTML sanitization, PDF generation
- `standards/frontend/forms.md` - Form types, limitations, reserved names
- `standards/frontend/dropdowns.md` - Server-search rules, entity-options patterns
- `standards/frontend/screen-documents.md` - In-app help documentation for screens

## Quick Reference

### Screen Definition Pattern
```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="Screen Title">
    <transition name="action">
        <service-call name="component.ServiceName#action"/>
        <default-response url="."/>
    </transition>
    <widgets>
        <container-box>
            <box-header title="Section Title"/>
            <box-body><!-- Content --></box-body>
        </container-box>
    </widgets>
</screen>
```

## Key Principles

1. **No Nested form-lists**: Use section-iterate for nested data display
2. **Server-search**: Handle both ID lookup and text search in transitions
3. **Render Modes**: Specify all modes (html,vuet,qvt) for raw HTML content
4. **Subscreens**: Container screens at same level as subdirectory