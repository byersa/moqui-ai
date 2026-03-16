# Form Widget Standards

### Input Widgets

| Widget | Purpose | Common Attributes |
|--------|---------|-------------------|
| `text-line` | Single line text | `size`, `maxlength`, `input-type` |
| `text-area` | Multi-line text | `rows`, `cols`, `maxlength` |
| `text-find` | Search input | `size`, `hide-options` |
| `display` | Read-only display | `text`, `also-hidden` |
| `hidden` | Hidden field | - |

### Text Input Types

```xml
<!-- Email validation -->
<text-line input-type="email"/>

<!-- Number input -->
<text-line input-type="number"/>

<!-- Password field -->
<text-line input-type="password"/>

<!-- Telephone -->
<text-line input-type="tel"/>

<!-- URL validation -->
<text-line input-type="url"/>
```

### Date/Time Widgets

```xml
<!-- Date only -->
<date-time type="date"/>

<!-- Date and time -->
<date-time type="date-time"/>

<!-- Time only -->
<date-time type="time"/>
```

### Selection Widgets

| Widget | Purpose |
|--------|---------|
| `drop-down` | Single/multi selection |
| `check` | Boolean checkbox |
| `radio` | Radio button group |

```xml
<drop-down allow-empty="true">
    <entity-options ...>
</drop-down>

<check>
    <option key="Y" text="Yes"/>
</check>

<radio>
    <option key="A" text="Option A"/>
    <option key="B" text="Option B"/>
</radio>
```

### File Widgets

```xml
<file/>

<!-- With specific accept types -->
<file accept="image/*"/>
<file accept=".pdf,.doc,.docx"/>
```

### Link Widget Attributes

**CRITICAL: Distinguish btn-type from link-type:**

| Attribute | Purpose | Values |
|-----------|---------|--------|
| `btn-type` | Visual styling (colors) | `default`, `primary`, `success`, `info`, `warning`, `danger` |
| `link-type` | HTML element type | `auto`, `anchor`, `anchor-button`, `hidden-form` |

```xml
<!-- CORRECT: Blue button styling -->
<link url="submit" text="Submit" btn-type="primary"/>

<!-- CORRECT: Green success button -->
<link url="approve" text="Approve" btn-type="success"/>

<!-- WRONG: Using link-type for styling -->
<link url="submit" text="Submit" link-type="info"/>
```

### Submit Widget

```xml
<!-- Basic submit -->
<submit/>

<!-- With text -->
<submit text="Save Changes"/>

<!-- With confirmation -->
<submit text="Delete" confirmation="Are you sure?"/>
```

### Display Widget Options

```xml
<!-- Basic display -->
<display/>

<!-- With custom text -->
<display text="${formatDate(orderDate, 'dd/MM/yyyy')}"/>

<!-- Also create hidden field -->
<display also-hidden="true"/>
```

### Container-Dialog Pattern

**Use `type` attribute (NOT `button-type`):**

```xml
<container-dialog id="CreateDialog" button-text="Create New" type="primary" width="600">
    <form-single name="CreateForm" transition="create">
        <!-- Form fields -->
    </form-single>
</container-dialog>
```

### Image Widget

```xml
<image url="productImage.png" url-type="screen"/>
<image url="${imageUrl}" url-type="plain"/>
```

### Label Widget

```xml
<label text="Section Header" type="h4"/>
<label text="Warning message" style="text-warning"/>
```

### Required Fields

```xml
<default-field title="Email" required="required">
    <text-line input-type="email"/>
</default-field>
```

### Field Title vs Tooltip

```xml
<default-field title="Customer Name" tooltip="Enter the full legal name">
    <text-line/>
</default-field>
```

### Conditional Display

```xml
<field name="approvalNotes">
    <conditional-field condition="order?.statusId == 'OrdPendingApproval'" title="Notes">
        <text-area rows="3"/>
    </conditional-field>
    <default-field title="Notes">
        <display/>
    </default-field>
</field>
```

### Widget Anti-Patterns

```xml
<!-- WRONG: link-type used for styling -->
<link link-type="info" url="..."/>

<!-- CORRECT: btn-type for styling -->
<link btn-type="info" url="..."/>

<!-- WRONG: button-type on container-dialog -->
<container-dialog button-type="primary">

<!-- CORRECT: type on container-dialog -->
<container-dialog type="primary">
```

### Common Widget Combinations

```xml
<!-- Search field with clear button -->
<text-find size="30" hide-options="ignore-case"/>

<!-- Currency display -->
<display text="${ec.l10n.formatCurrency(amount, currencyUomId)}"/>

<!-- Formatted date -->
<display text="${ec.l10n.format(orderDate, 'dd/MM/yyyy')}"/>
```
