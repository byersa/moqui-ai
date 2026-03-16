# Rich Text Editor Standard

This standard documents patterns for implementing WYSIWYG (rich text) editing in Moqui Framework screens, including HTML sanitization, storage, display, and PDF generation.

## Widget Patterns

### Primary Pattern: editor-type Attribute

The `editor-type` attribute on `<text-area>` enables WYSIWYG editing. The value `"html"` activates the rich text editor in Quasar rendering mode (/qapps).

```xml
<!-- WYSIWYG editor for rich text content -->
<field name="contentField">
    <default-field title="Content">
        <text-area rows="20" cols="80" editor-type="html"/>
    </default-field>
</field>
```

**Schema Reference**: See `framework/xsd/xml-form-3.xsd` (line ~1087):
```xml
<xs:attribute name="editor-type" type="xs:string">
    <xs:annotation><xs:documentation>Use a WYSIWYG editor, can be an expanded string expression (with ${}),
        such as 'html' or 'md', support depends on what the active text-area macro does.</xs:documentation></xs:annotation>
</xs:attribute>
```

### Supported Editor Types

| Type | Description |
|------|-------------|
| `html` | Rich text HTML editor (Quasar QEditor in /qapps) |
| `md` | Markdown editor (if supported by renderer) |

### Working Example (from SimpleScreens)

```xml
<!-- WikiBlogs.xml - Proven working pattern -->
<field name="blogText">
    <default-field>
        <text-area cols="120" rows="35" editor-type="html"/>
    </default-field>
</field>
```

### IMPORTANT: Invalid Attributes

**Do NOT use these non-existent attributes:**
- ~~`html-editor="true"`~~ - Does not exist in Moqui schema
- ~~`html-editor-options="..."`~~ - Does not exist in Moqui schema

These were incorrectly documented and will NOT enable the WYSIWYG editor.

## Storage Patterns

### Entity Field Type

Rich HTML content should use `text-very-long` field type:

```xml
<field name="contentText" type="text-very-long">
    <description>Rich HTML content</description>
</field>
```

### DbResource Storage

For document-like content (agendas, minutes, reports), store in DbResource:

```xml
<!-- Create DbResource -->
<service-call name="create#moqui.resource.DbResource" in-map="[
    filename: 'Document Title.html',
    mimeTypeId: 'text/html',
    resourceText: sanitizedHtmlContent
]" out-map="resourceResult"/>

<!-- Associate with WorkEffort -->
<service-call name="create#mantle.work.effort.WorkEffortContent" in-map="[
    workEffortId: workEffortId,
    contentLocation: 'dbresource://DbResource/' + resourceResult.resourceId,
    contentTypeEnumId: 'WectAgenda',
    fromDate: ec.user.nowTimestamp
]"/>
```

### Content Type Enumerations

| contentTypeEnumId | Purpose |
|-------------------|---------|
| `WectAgenda` | Meeting agenda content |
| `WectMinutes` | Meeting minutes content |
| `WectAgendaDocument` | Supporting documents |
| `WectGeneral` | General content |

## Display Patterns

### Read-Only HTML Display

Use `render-mode` to display HTML content without escaping:

```xml
<section name="ViewContentSection">
    <condition><expression>!canEdit &amp;&amp; contentText</expression></condition>
    <widgets>
        <container-box>
            <box-header title="Content"/>
            <box-body>
                <render-mode>
                    <text type="html,vuet,qvt">${contentText}</text>
                </render-mode>
            </box-body>
        </container-box>
    </widgets>
</section>
```

### Conditional Edit/View Mode

```xml
<!-- Edit mode for authorized users -->
<section name="EditContentSection">
    <condition><expression>canEdit</expression></condition>
    <widgets>
        <form-single name="ContentForm" transition="updateContent">
            <field name="contentField">
                <default-field title="Content">
                    <text-area rows="20" cols="80" default-value="${contentText ?: ''}"
                               editor-type="html"/>
                </default-field>
            </field>
            <field name="submitButton">
                <default-field title="Save"><submit/></default-field>
            </field>
        </form-single>
    </widgets>
</section>

<!-- View mode for others -->
<section name="ViewContentSection">
    <condition><expression>!canEdit &amp;&amp; contentText</expression></condition>
    <widgets>
        <container-box>
            <box-header title="Content"/>
            <box-body>
                <render-mode>
                    <text type="html,vuet,qvt">${contentText}</text>
                </render-mode>
            </box-body>
        </container-box>
    </widgets>
</section>
```

## HTML Sanitization (REQUIRED)

### Service Parameter Pattern

Always use `allow-html="any"` and sanitize manually:

```xml
<in-parameters>
    <parameter name="htmlContent" allow-html="any">
        <description>Rich HTML content - sanitized server-side</description>
    </parameter>
</in-parameters>
```

### JSoup Sanitization Code

```groovy
// Standard sanitization pattern for HTML content
org.jsoup.nodes.Document.OutputSettings outputSettings =
    new org.jsoup.nodes.Document.OutputSettings()
        .charset("UTF-8")
        .prettyPrint(true)
        .indentAmount(4)

org.jsoup.safety.Safelist safeList =
    org.jsoup.safety.Safelist.relaxed()
        .addTags("s", "del")  // Strikethrough support
        .addAttributes("table", "border", "border-bottom",
                      "border-top", "border-left", "border-right")

if (htmlContent) {
    htmlContent = org.jsoup.Jsoup.clean(htmlContent, "", safeList, outputSettings)
}
```

### Allowed Tags (Safelist.relaxed())

The relaxed safelist permits:
- **Block elements**: `blockquote`, `dd`, `dl`, `dt`, `li`, `ol`, `p`, `pre`, `ul`
- **Inline elements**: `a`, `b`, `cite`, `code`, `em`, `i`, `q`, `small`, `span`, `strike`, `strong`, `sub`, `sup`, `u`
- **Table elements**: `table`, `tbody`, `td`, `tfoot`, `th`, `thead`, `tr`
- **Other**: `br`

Custom additions:
- **Strikethrough**: `s`, `del`
- **Table borders**: border attributes on table element

### Security Considerations

1. **Never skip sanitization** - Even trusted users can have compromised sessions
2. **Sanitize server-side** - Client-side validation can be bypassed
3. **Use Safelist.relaxed()** - Balanced between functionality and security
4. **Test with payloads** - Verify `<script>` and event handlers are removed

## PDF Generation Patterns

### Using {utils-component} Conversion Services

```xml
<!-- Convert HTML fragment to XSL-FO -->
<service-call name="{shared-utils}.DocumentConversionServices.convert#HtmlToFop"
    in-map="[html:htmlContent, isFragment:true]" out-map="fopResult"/>

<!-- Use FOP fragment in XSL template -->
<set field="contentFop" from="fopResult.fop"/>
```

### Direct HTML to PDF

```xml
<!-- Complete HTML document to PDF -->
<service-call name="{shared-utils}.DocumentConversionServices.convert#HtmlToPdf"
    in-map="[html:fullHtmlDocument]" out-map="pdfResult"/>
<set field="pdfBytes" from="pdfResult.pdf"/>
```

### XSL-FO Template Integration

```groovy
// Build XML data document
def xmlData = """<?xml version="1.0" encoding="UTF-8"?>
<document>
    <title>${title}</title>
    <date>${date}</date>
    <content>${contentFop}</content>
</document>"""

// Transform to PDF (with proper stream handling)
ByteArrayOutputStream pdfOs = new ByteArrayOutputStream()
def xslStream = ec.resource.getLocationReference(xslTemplatePath).openStream()
try {
    ec.resource.xslFoTransform(
        new StreamSource(new ByteArrayInputStream(xmlData.bytes)),
        new StreamSource(xslStream),
        pdfOs,
        "application/pdf",
        [:]  // transformation parameters
    )
} finally {
    xslStream?.close()
    pdfOs.close()
}
pdfBytes = pdfOs.toByteArray()
```

## Best Practices

### DO
- Use `editor-type="html"` for rich text fields
- Always sanitize HTML server-side with JSoup
- Store document content in DbResource
- Use `render-mode` for displaying HTML
- Test with XSS payloads during development

### DON'T
- Trust client-side validation alone
- Use `allow-html="safe"` without understanding limitations
- Store unsanitized HTML
- Use `encode="false"` on display elements (use render-mode instead)
- Skip sanitization for "trusted" users

## Testing Rich Text Editors

For Playwright/E2E testing of CKEditor fields, see `testing-guide.md` section **"Special Component Testing"**.

**Key Points:**
- Never use `fill()` for CKEditor fields - bypasses event system
- Use `pressSequentially()` with delay for reliable event triggering
- Alternative: Use CKEditor API via `page.evaluate()` with `fire('change')`
- V-model binds to `formProps.fields.{fieldName}` in mForm component

## Related Standards

- `frontend/forms.md` - General form patterns
- `frontend/html-injection.md` - Render-mode usage
- `backend/services.md` - Service parameter patterns
- `backend/file-resources.md` - DbResource storage patterns (includes putText/putBytes)

## Framework Guide Reference

See `runtime/component/moqui-agent-os/framework-guide.md`:
- Section: **"## Screen Widget Best Practices"** - Widget configuration and patterns
- Section: **"## Documentation Resources"** - DbResource and content management
