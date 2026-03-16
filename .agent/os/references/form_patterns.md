# Moqui Form Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/frontend/forms.md` - Form types, limitations, reserved names
- `standards/frontend/dropdowns.md` - Server-search rules, entity-options patterns
- `standards/frontend/form-widgets.md` - Widget attributes (btn-type vs link-type)
- `standards/frontend/row-actions.md` - Per-row data fetching in form-lists
- `standards/frontend/rich-text-editor.md` - WYSIWYG editor patterns, HTML sanitization

---

## Form Templates

### Form-Single Template
```xml
<form-single name="[FORM_NAME]" map="[ENTITY_MAP]" transition="[TRANSITION_NAME]">
    <field name="[ID_FIELD]">
        <default-field><hidden/></default-field>
    </field>

    <field name="[FIELD_NAME]">
        <default-field title="[FIELD_TITLE]">
            <text-line size="[SIZE]"/>
        </default-field>
    </field>

    <field name="submitButton">
        <default-field title="[BUTTON_TEXT]">
            <submit/>
        </default-field>
    </field>
</form-single>
```

### Form-List Template
```xml
<form-list name="[FORM_NAME]" list="[LIST_NAME]" skip-form="true" header-dialog="true">
    <entity-find entity-name="[ENTITY_NAME]" list="[LIST_NAME]">
        <search-form-inputs default-order-by="[ORDER_FIELD]"/>
        <select-field field-name="[FIELD_NAME]"/>
    </entity-find>

    <field name="[FIELD_NAME]">
        <header-field title="[HEADER_TITLE]" show-order-by="true"/>
        <default-field>
            <display text="${[FIELD_NAME]}"/>
        </default-field>
    </field>
</form-list>
```

---

## Nested Data Display Pattern

### Problem: Nested form-lists NOT supported

Use `row-actions` + `section-iterate` for displaying related data within a form-list row.

### Solution Template
```xml
<form-list name="OrderList" list="orderList">
    <row-actions>
        <!-- Fetch related data for this row -->
        <entity-find entity-name="OrderItem" list="orderItems">
            <econdition field-name="orderId"/>
            <order-by field-name="sequenceNum"/>
        </entity-find>
        <set field="itemCount" from="orderItems?.size() ?: 0"/>
    </row-actions>

    <field name="orderId"><default-field><display/></default-field></field>

    <field name="items">
        <default-field title="Items">
            <container-dialog id="ItemsDialog${orderId}" button-text="View (${itemCount})" title="Order Items">
                <section-iterate name="OrderItemSection" list="orderItems" entry="orderItem">
                    <widgets>
                        <container style="border-bottom: 1px solid #ddd; padding: 5px 0;">
                            <label text="${orderItem.productName}" type="span"/>
                            <label text=" - Qty: ${orderItem.quantity}" type="span"/>
                        </container>
                    </widgets>
                </section-iterate>
                <section name="NoItemsSection" condition="!orderItems">
                    <widgets>
                        <label text="No items found." type="p" style="text-muted"/>
                    </widgets>
                </section>
            </container-dialog>
        </default-field>
    </field>
</form-list>
```

---

## Server-Search Transition Pattern

### CRITICAL: Handle Both ID Lookup and Text Search

Server-search transitions are called in TWO scenarios:
1. **User typing**: `term` contains the search text
2. **Page reload**: `term` contains the selected ID(s)

### Complete Transition Template
```xml
<transition name="getPartyList">
    <actions>
        <entity-find entity-name="mantle.party.PartyDetail" list="partyList">
            <econditions combine="or">
                <!-- ID-based lookup for page reload (term contains the ID) -->
                <econdition field-name="partyId" operator="in" from="term" ignore-if-empty="true"/>
                <!-- Text-based search for user typing -->
                <econdition field-name="firstName" operator="like" value="%${term}%" ignore-case="true" ignore="!term"/>
                <econdition field-name="lastName" operator="like" value="%${term}%" ignore-case="true" ignore="!term"/>
                <econdition field-name="organizationName" operator="like" value="%${term}%" ignore-case="true" ignore="!term"/>
            </econditions>
        </entity-find>
        <set field="resultList" from="[]"/>
        <iterate list="partyList" entry="party">
            <script>resultList.add([partyId:party.partyId, partyName:ec.resource.expand('PartyNameTemplate', null, party)])</script>
        </iterate>
        <script>ec.web.sendJsonResponse(resultList)</script>
    </actions>
    <default-response type="none"/>
</transition>
```

---

## Multi-Select Server-Search Workaround

### Problem
Multi-select server-search dropdowns do NOT automatically fetch labels on page reload.

### Solution: Hybrid list-options + dynamic-options

**Step 1: Screen Actions - Fetch labels for pre-selected values**
```xml
<actions>
    <if condition="customerPartyId">
        <entity-find entity-name="mantle.party.PartyDetail" list="selectedCustomerList">
            <econdition field-name="partyId" operator="in" from="customerPartyId"/>
        </entity-find>
    <else>
        <set field="selectedCustomerList" from="[]"/>
    </else></if>
</actions>
```

**Step 2: Drop-down - Combine list-options with dynamic-options**
```xml
<field name="customerPartyId">
    <header-field title="Customer">
        <drop-down allow-empty="true" allow-multiple="true">
            <!-- list-options provides labels for pre-selected values (server-rendered) -->
            <list-options list="selectedCustomerList" key="${partyId}"
                          text="${ec.resource.expand('PartyNameTemplate', null, it)}"/>
            <!-- dynamic-options provides search functionality -->
            <dynamic-options transition="getCustomerPartyList" server-search="true" min-length="2"/>
        </drop-down>
    </header-field>
</field>
```

---

## Field Type Templates

### Text Input Fields
```xml
<!-- Single line text -->
<field name="name">
    <default-field title="Name">
        <text-line size="30" maxlength="100"/>
    </default-field>
</field>

<!-- Multi-line text -->
<field name="description">
    <default-field title="Description">
        <text-area rows="5" cols="50"/>
    </default-field>
</field>
```

### Date and Time Fields
```xml
<!-- Date only -->
<field name="startDate">
    <default-field title="Start Date">
        <date-time type="date"/>
    </default-field>
</field>

<!-- Date and time -->
<field name="createdDate">
    <default-field title="Created">
        <date-time/>
    </default-field>
</field>
```

### Selection Fields
```xml
<!-- Simple drop-down -->
<field name="status">
    <default-field title="Status">
        <drop-down>
            <option key="ACTIVE" text="Active"/>
            <option key="INACTIVE" text="Inactive"/>
        </drop-down>
    </default-field>
</field>

<!-- Checkbox (Y/N) -->
<field name="isActive">
    <default-field title="Active">
        <check><option key="Y" text=" "/></check>
    </default-field>
</field>
```

### Rich Text Fields (WYSIWYG Editor)
```xml
<!-- WYSIWYG editor for rich text content -->
<field name="contentField">
    <default-field title="Content">
        <text-area rows="20" cols="80" editor-type="html"/>
    </default-field>
</field>

<!-- Smaller editor for notes -->
<field name="notesField">
    <default-field title="Notes">
        <text-area rows="8" cols="80" editor-type="html"/>
    </default-field>
</field>
```

**IMPORTANT**: Use `editor-type="html"` (NOT `html-editor="true"` which does not exist).
See `standards/frontend/rich-text-editor.md` for complete patterns.

**Testing Note**: For Playwright E2E tests, never use `fill()` on CKEditor fields - use `pressSequentially()` instead. See `testing-guide.md` section "Special Component Testing".

### Rich Text Display (Read-Only)
```xml
<!-- Display HTML content without escaping -->
<section name="ViewContentSection">
    <condition><expression>contentText</expression></condition>
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

**IMPORTANT**: When accepting HTML input, services MUST sanitize using JSoup.
See `standards/frontend/rich-text-editor.md` for sanitization patterns.

---

## Hidden Fields Template

**CORRECT Pattern:**
```xml
<field name="entityId" from="entityId">
    <default-field><hidden/></default-field>
</field>

<!-- Static value -->
<field name="statusId" from="'ACTIVE'">
    <default-field><hidden/></default-field>
</field>

<!-- Dynamic value -->
<field name="userId" from="ec.user.userId">
    <default-field><hidden/></default-field>
</field>
```

---

## Conditional Fields Template

```xml
<field name="value">
    <conditional-field condition="typeId == 'TEXT'">
        <text-line/>
    </conditional-field>
    <conditional-field condition="typeId == 'NUMBER'">
        <text-line format="0.00"/>
    </conditional-field>
    <conditional-field condition="typeId == 'DATE'">
        <date-time type="date"/>
    </conditional-field>
    <default-field>
        <text-line/>
    </default-field>
</field>
```

---

## Row Selection Template (Multi-Action)

```xml
<form-list name="DocumentList" list="documentList">
    <row-selection id-field="documentId">
        <action>
            <dialog button-text="Delete Selected" title="Delete Documents"/>
            <form-single name="DeleteForm" transition="deleteDocuments" pass-through-parameters="true">
                <field name="documentId"><default-field><hidden/></default-field></field>
                <field name="confirmDelete">
                    <default-field title="Confirm">
                        <submit confirmation="Delete all selected documents?"/>
                    </default-field>
                </field>
            </form-single>
        </action>
    </row-selection>
    <!-- Fields -->
</form-list>
```

---

## Quality Checklist

**Form Structure:**
- [ ] Form has appropriate name attribute
- [ ] form-list has mandatory `list` attribute
- [ ] **NO nested form-lists** - use section-iterate instead
- [ ] Transitions specified for submit actions
- [ ] Required fields marked with `required="true"`

**Field Definitions:**
- [ ] Hidden fields use `from` attribute at field level
- [ ] Entity-options wrapped in entity-find
- [ ] Conditional fields use `<conditional-field>` blocks
- [ ] All referenced fields in select-field for form-list
- [ ] **Avoid reserved variable names** (`result`, `messages`, `errors`)

**Server-Search:**
- [ ] ID-based condition with `operator="in" from="term" ignore-if-empty="true"`
- [ ] Text conditions with `ignore="!term"`
- [ ] `combine="or"` on econditions block
- [ ] Test: page reload displays selected value's label