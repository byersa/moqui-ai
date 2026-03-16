# Render Mode / HTML Injection

Inject raw HTML when Moqui widgets can't achieve the result.

## When to Use
- **Last resort**: No Moqui widget supports the requirement
- **Layout**: Bootstrap/CSS layout beyond container-row capabilities
- **Integration**: Third-party JS components or custom HTML

## Render-Mode Pattern
```xml
<render-mode>
    <text type="html,vuet,qvt"><![CDATA[<div class="row">]]></text>
</render-mode>
<!-- Moqui widgets here -->
<render-mode>
    <text type="html,vuet,qvt"><![CDATA[</div>]]></text>
</render-mode>
```

## Text Element Pattern
```xml
<text type="html,vuet,qvt"><![CDATA[<br/>]]></text>
```

## Type Attribute Values
| Value | Context |
|-------|--------|
| `html` | Standard HTML rendering |
| `vuet` | Vue.js templates |
| `qvt` | Quasar Vue templates |
| `html,vuet,qvt` | Works in all modes (safest) |

## Prefer Widgets When Possible
```xml
<!-- Prefer this (widget) -->
<container-row><row-col lg="6">...</row-col></container-row>

<!-- Over this (raw HTML) -->
<text type="html"><![CDATA[<div class="row"><div class="col-lg-6">...]]></text>
```

## Localization in CDATA Blocks

`LocalizedMessage` **cannot** translate text inside CDATA/render-mode blocks. Pre-compute localized strings in the `<actions>` block and reference them as FreeMarker variables:

```xml
<actions>
    <set field="phaseWord" from="ec.l10n.localize('Phase')"/>
</actions>
<widgets>
    <render-mode><text type="html"><![CDATA[
        <h3>${phaseName} ${phaseWord}</h3>
    ]]></text></render-mode>
</widgets>
```

> **Full guidance**: See `standards/global/localization.md` § "Localizing Text in CDATA/Render-Mode Blocks".