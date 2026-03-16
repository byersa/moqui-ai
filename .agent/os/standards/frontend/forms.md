# Form Standards

### Form Types

| Type | Purpose | Usage |
|------|---------|-------|
| `form-single` | Single record create/edit | Detail screens, dialogs |
| `form-list` | Multi-record display | Search results, listings |
| `form-multi` | Bulk record editing | Batch updates |

### Form-Single Pattern

```xml
<form-single name="OrderForm" transition="createOrder">
    <field name="customerId"><default-field title="Customer">
        <drop-down>
            <entity-options key="${partyId}" text="${firstName} ${lastName}">
                <entity-find entity-name="mantle.party.Person"/>
            </entity-options>
        </drop-down>
    </default-field></field>

    <field name="orderDate"><default-field>
        <date-time type="date"/>
    </default-field></field>

    <field name="submitButton"><default-field title="Create">
        <submit/>
    </default-field></field>
</form-single>
```

### Form-List Pattern (CRITICAL)

**`form-list` MUST have `list` attribute:**

```xml
<!-- CORRECT: list attribute specified -->
<form-list name="OrderList" list="orderList" skip-form="true">
    <field name="orderId"><default-field title="ID">
        <link url="orderDetail" text="${orderId}"/>
    </default-field></field>
</form-list>

<!-- WRONG: missing list attribute -->
<form-list name="OrderList">
```

### Form-List with Search

```xml
<form-list name="OrderList" list="orderList" skip-form="true">
    <!-- Search header fields -->
    <field name="statusId"><header-field show-order-by="true">
        <drop-down allow-empty="true">
            <entity-options key="${statusId}" text="${description}">
                <entity-find entity-name="moqui.basic.StatusItem">
                    <econdition field-name="statusTypeId" value="OrderStatus"/>
                </entity-find>
            </entity-options>
        </drop-down>
    </header-field></field>

    <!-- Display fields -->
    <field name="orderId"><default-field title="Order ID">
        <display/>
    </default-field></field>
</form-list>
```

### search-form-inputs URL Parameter Conventions

When `search-form-inputs` processes URL/form parameters, each field supports operator suffixes:

| Suffix | Purpose | Values |
|--------|---------|--------|
| `{field}_op` | Operator | `equals` (default), `like`, `contains`, `begins`, `empty`, `in` |
| `{field}_not` | Negate condition | `Y` or `true` |
| `{field}_ic` | Ignore case | `Y` or `true` |

**CRITICAL: There is no `not-in` operator.** Use `_op=in` + `_not=Y` instead.

```xml
<!-- Passing filter parameters via link -->
<link url="targetScreen" text="Excluded Items">
    <parameter name="statusId" value="StComplete,StClosed"/>
    <parameter name="statusId_op" value="in"/>
    <parameter name="statusId_not" value="Y"/>
</link>

<!-- The target screen's entity-find with search-form-inputs automatically applies the filter -->
<entity-find entity-name="example.Item" list="itemList">
    <search-form-inputs default-order-by="-lastUpdatedStamp"/>
</entity-find>
```

### Reserved Field Names

**Never use these names for form fields:**
- `_formName` - Internal form tracking
- `_isMulti` - Multi-form indicator
- `_formMap` - Form data map
- `moquiSessionToken` - CSRF protection

### Field Structure Requirements

**conditional-field REQUIRES default-field:**
```xml
<!-- CORRECT -->
<field name="statusId">
    <conditional-field condition="order?.statusId == 'OrdDraft'" title="Status">
        <drop-down>...</drop-down>
    </conditional-field>
    <default-field title="Status">
        <display/>
    </default-field>
</field>

<!-- WRONG: conditional-field without default-field -->
<field name="statusId">
    <conditional-field condition="order?.statusId == 'OrdDraft'">
        <drop-down>...</drop-down>
    </conditional-field>
</field>
```

### Variable Scoping in Form Contexts

**`conditional-field condition=` CANNOT access form-list `list-entry` variables.**

In a `form-list`, the `list-entry` variable (e.g., `fieldDef`) is available in `row-actions` and `from=` attributes, but NOT in `conditional-field condition=` expressions. Only context variables (set via `row-actions`, screen `<actions>`, or `<set>`) are accessible in conditions.

```xml
<!-- CORRECT: set flag in row-actions, use flag in condition -->
<form-list name="FieldList" list="fieldList" list-entry="fieldDef">
    <row-actions>
        <set field="isTextField" from="fieldDef.fieldTypeEnumId == 'TypeText'"/>
    </row-actions>
    <field name="value">
        <conditional-field condition="isTextField" title="Value">
            <text-line/>
        </conditional-field>
        <default-field title="Value">
            <display/>
        </default-field>
    </field>
</form-list>

<!-- WRONG: accessing list-entry variable in conditional-field condition -->
<form-list name="FieldList" list="fieldList" list-entry="fieldDef">
    <field name="value">
        <conditional-field condition="fieldDef.fieldTypeEnumId == 'TypeText'" title="Value">
            <text-line/>
        </conditional-field>
    </field>
</form-list>
<!-- Results in: [Template Error: Error in condition [...] from []] -->
```

**Note:** `<section condition=...>` inside `section-iterate` CAN access the entry variable. This distinction only affects `conditional-field`.

### Field Discovery in form-single with section-iterate

**Fields nested inside `form-single > section-iterate > section > field` are NOT discovered by Moqui's form builder.** The form builder scans for `<field>` elements at specific nesting depths and misses deeply nested ones.

**Fix:** Use `section-iterate` directly inside `form-single` with `<actions>` to extract entry data into context variables, then use a single `<field>` with multiple `conditional-field` branches:

```xml
<!-- CORRECT: flat structure with conditional-field branches -->
<form-single name="DetailForm" transition="saveValues">
    <section-iterate name="FieldSection" list="fieldList" entry="fieldEntry">
        <actions>
            <set field="cfType" from="fieldEntry.fieldTypeEnumId"/>
            <set field="isText" from="cfType == 'TypeText'"/>
            <set field="isCheckbox" from="cfType == 'TypeCheckbox'"/>
            <set field="cfLabel" from="fieldEntry.fieldLabel"/>
        </actions>
        <widgets>
            <field name="field_${fieldEntry.fieldId}">
                <conditional-field condition="isText" title="${cfLabel}">
                    <text-line/>
                </conditional-field>
                <conditional-field condition="isCheckbox" title="${cfLabel}">
                    <check><option key="Y" text=" "/></check>
                </conditional-field>
                <default-field title="${cfLabel}">
                    <display/>
                </default-field>
            </field>
        </widgets>
    </section-iterate>
</form-single>

<!-- WRONG: deeply nested fields not discovered -->
<form-single name="DetailForm" transition="saveValues">
    <section-iterate name="FieldSection" list="fieldList" entry="fieldEntry">
        <widgets>
            <section name="TypeSection" condition="fieldEntry.fieldTypeEnumId == 'TypeText'">
                <widgets>
                    <field name="field_${fieldEntry.fieldId}">
                        <default-field title="${fieldEntry.fieldLabel}">
                            <text-line/>
                        </default-field>
                    </field>
                </widgets>
            </section>
        </widgets>
    </section-iterate>
</form-single>
<!-- Fields render as empty - only the submit button appears -->
```

### Field Attributes Restrictions

| Element | NOT Supported |
|---------|---------------|
| `default-field` | `condition` attribute |
| `header-field` | `show` attribute |

```xml
<!-- WRONG: condition on default-field -->
<default-field condition="someCondition">

<!-- CORRECT: use conditional-field instead -->
<conditional-field condition="someCondition">
```

### Form Transition Pattern

```xml
<transition name="createOrder">
    <service-call name="example.OrderServices.create#Order"/>
    <default-response url="." parameter-map="[orderId:orderId]"/>
    <error-response url="."/>
</transition>
```

### Hidden Fields

```xml
<field name="orderId"><default-field>
    <hidden/>
</default-field></field>
```

### Form Validation

```xml
<field name="email"><default-field title="Email" required="required">
    <text-line input-type="email"/>
</default-field></field>

<field name="quantity"><default-field title="Quantity" required="required">
    <text-line input-type="number"/>
</default-field></field>
```

### Skip-Form Attribute

**Use `skip-form="true"` for display-only form-lists:**
```xml
<form-list name="OrderList" list="orderList" skip-form="true">
    <!-- No form submission, just display -->
</form-list>
```

### Multi-Form Pattern

```xml
<form-list name="ItemList" list="itemList" multi="true" transition="updateItems">
    <field name="itemId"><default-field>
        <hidden/>
    </default-field></field>

    <field name="quantity"><default-field>
        <text-line input-type="number"/>
    </default-field></field>

    <field name="submitButton"><default-field title="Update All">
        <submit/>
    </default-field></field>
</form-list>
```

### Multi Form-List Transition Pattern

**Multi form-list transitions MUST use `<service-call multi="true"/>` on the transition**, not manual `_keyList` iteration. The framework automatically handles row parameter extraction.

```xml
<!-- CORRECT: framework handles multi-row extraction automatically -->
<transition name="saveConfigurations">
    <service-call name="example.Services.store#Configuration" multi="true"/>
    <default-response url="."/>
    <error-response url="." save-current-screen="true"/>
</transition>

<form-list name="ConfigList" list="configList" list-entry="configEntry"
           transition="saveConfigurations" multi="true">
    <field name="configId"><default-field><hidden/></default-field></field>
    <field name="isEnabled"><default-field title="Enabled">
        <drop-down current="selected">
            <option key="Y" text="Yes"/>
            <option key="N" text="No"/>
        </drop-down>
    </default-field></field>
    <field name="submitButton"><default-field title="">
        <submit text="Save" type="success"/>
    </default-field></field>
</form-list>

<!-- WRONG: manual _keyList iteration - does not work -->
<transition name="saveConfigurations">
    <actions>
        <iterate list="configId_keyList" entry="keyValue">
            <script>rowIsEnabled = context.get("isEnabled_" + keyValue)</script>
            <service-call name="example.Services.store#Configuration"
                          in-map="[configId:keyValue, isEnabled:rowIsEnabled]"/>
        </iterate>
    </actions>
    <default-response url="."/>
</transition>
<!-- Saves have no effect - parameter extraction fails silently -->
```

### container-dialog Inside form-list Fields

`container-dialog` can be used inside form-list `conditional-field` or `default-field` to show overflow content (long text, details) in a popup dialog. Moqui automatically appends the row index to the dialog `id` attribute, preventing ID collisions across rows (e.g., `MyDialog_0`, `MyDialog_1`, etc.).

**Pattern: Truncated text with "show more" dialog**

Use `row-actions` to compute truncation flags (since `conditional-field condition=` cannot access list-entry variables directly):

```xml
<form-list name="LogList" list="logList">
    <entity-find entity-name="example.LogEntry" list="logList">
        <search-form-inputs/>
    </entity-find>
    <row-actions>
        <set field="contentLong" from="content?.length() > 500"/>
        <set field="contentShort" from="contentLong ? content.substring(0, 450) + '...' : (content ?: '')"/>
    </row-actions>
    <field name="content">
        <header-field><text-find size="20" hide-options="true"/></header-field>
        <conditional-field condition="contentLong">
            <display text="${contentShort}"/>
            <container-dialog id="FullContentDialog" button-text="Show All">
                <display text="${content}"/>
            </container-dialog>
        </conditional-field>
        <default-field><display/></default-field>
    </field>
</form-list>
```

**Key points:**
- The `id` on `container-dialog` must be unique within the form definition (Moqui handles per-row uniqueness automatically)
- Use `row-actions` to set boolean flags for `conditional-field condition=` (known form-list scoping limitation)
- This pattern works in both Quasar (qapps) and FTL rendering modes

### View-Entity Row-Actions Field Selection

**Fields used only in `row-actions` are NOT auto-selected when entity-find targets a view-entity.**

Moqui optimizes form-list queries by matching SQL SELECT columns against form field names. View-entity aliases consumed only in `row-actions` (to compute display values) have no matching form field name, so they are silently omitted from the query, resulting in null values at runtime.

```xml
<!-- WRONG: processedItems, totalItems not selected (no matching form field names) -->
<form-list name="JobList" list="jobList" skip-form="true">
    <entity-find entity-name="mycompany.myapp.JobDetailView" list="jobList">
        <econdition field-name="statusId" value="JobActive"/>
    </entity-find>
    <row-actions>
        <set field="progress" from="totalItems ? (processedItems / totalItems * 100) : 0"/>
    </row-actions>
    <field name="progress"><default-field><display/></default-field></field>
</form-list>

<!-- CORRECT: explicitly select view-entity fields used in row-actions -->
<form-list name="JobList" list="jobList" skip-form="true">
    <entity-find entity-name="mycompany.myapp.JobDetailView" list="jobList">
        <econdition field-name="statusId" value="JobActive"/>
        <select-field field-name="processedItems,totalItems"/>
    </entity-find>
    <row-actions>
        <set field="progress" from="totalItems ? (processedItems / totalItems * 100) : 0"/>
    </row-actions>
    <field name="progress"><default-field><display/></default-field></field>
</form-list>
```

**Note:** This only affects view entities. Regular entities and `entity-find-one` always return all fields.

### Anti-Patterns

```xml
<!-- WRONG: form-list without list attribute -->
<form-list name="OrderList">

<!-- WRONG: condition on default-field -->
<default-field condition="something">

<!-- WRONG: show on header-field -->
<header-field show="false">

<!-- WRONG: conditional-field without default-field -->
<conditional-field condition="x">...</conditional-field>

<!-- WRONG: accessing list-entry variable in conditional-field condition -->
<conditional-field condition="listEntry.someField == 'value'">
<!-- Fix: set a flag in row-actions and use the flag in condition -->

<!-- WRONG: deeply nested field inside form-single > section-iterate > section -->
<form-single><section-iterate><widgets><section><widgets><field>
<!-- Fix: flatten to section-iterate > field with conditional-field branches -->

<!-- WRONG: manual _keyList iteration for multi form-list save -->
<iterate list="fieldId_keyList" entry="key">
    <script>val = context.get("fieldName_" + key)</script>
<!-- Fix: use <service-call multi="true"/> on the transition -->

<!-- WRONG: view-entity fields used only in row-actions without select-field -->
<entity-find entity-name="my.ViewEntity" list="myList">
    <!-- processedItems used in row-actions but not a form field name — will be NULL -->
</entity-find>
<!-- Fix: add <select-field field-name="processedItems"/> -->
```
