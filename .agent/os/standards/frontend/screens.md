# Screen Standards

### Screen Structure
- **XML Definition**: Screens defined in `.xml` files in `screen/` directories
- **Hierarchy**: Organize screens in logical hierarchy with subscreens
- **Root Screen**: Application root screens set up filter context in `always-actions`

### Container Types
- **container**: Basic content grouping
- **container-box**: Grouped content with headers and borders
- **container-row**: Bootstrap grid system (row-col with lg, md, sm, xs)
- **container-dialog**: Modal dialogs and popups

### Modal Dialogs
- **Type Attribute**: Use `type` attribute for button styling (NOT `button-type`)
- **Valid Types**: `default`, `primary`, `success`, `info`, `warning`, `danger`
- **Width**: Specify appropriate `width` for content

### Form Standards
- **Form Types**:
  - `form-single`: Single record create/update
  - `form-list`: Multi-record display with search, pagination
  - `form-multi`: Bulk editing of multiple records
- **List Attribute**: `form-list` MUST have `list` attribute pointing to entity-find result
- **Entity-Options**: MUST be wrapped in `entity-find` element

### Form-List Data Population

**Entity-find can be INSIDE or OUTSIDE form-list. Both require `list` attribute.**

#### Inside Form-List (Recommended for most cases)

```xml
<form-list name="OrderList" list="orderList" skip-form="true">
    <entity-find entity-name="OrderHeader" list="orderList">
        <search-form-inputs default-order-by="orderDate"/>
        <!-- Add select-field for values used but NOT defined as form fields -->
        <select-field field-name="internalNotes"/>
    </entity-find>

    <field name="orderId"><default-field><display/></default-field></field>
    <field name="orderDate"><header-field show-order-by="true"/><default-field><display/></default-field></field>
    <!-- internalNotes used in template below but not a form field -->
    <field name="notes"><default-field><display text="${internalNotes ?: 'None'}"/></default-field></field>
</form-list>
```

#### Outside Form-List (Complex logic, reusable data)

```xml
<actions>
    <entity-find entity-name="OrderHeader" list="orderList">
        <search-form-inputs/>
        <select-field field-name="orderId,orderDate,internalNotes"/>
    </entity-find>
</actions>
<widgets>
    <form-list name="OrderList" list="orderList" skip-form="true">
        <!-- fields -->
    </form-list>
</widgets>
```

#### Field Selection Rule

**Form-list auto-includes fields defined as `<field>` elements. Explicitly add `select-field` for:**
- Values used in display templates (`${fieldName}`)
- Values used in row-actions
- Values used in conditional-field conditions
- Values passed to transitions but not displayed

### Field Standards
- **conditional-field**: REQUIRES mandatory `default-field` following it
- **default-field**: Does NOT support `condition` attribute
- **header-field**: Does NOT support `show` attribute

### Link Widget Attributes
- **btn-type**: Visual styling (colors) - `default`, `primary`, `success`, `info`, `warning`, `danger`
- **link-type**: HTML element type - `auto`, `anchor`, `anchor-button`, `hidden-form`
- **Common Mistake**: Using `link-type="info"` instead of `btn-type="info"`
- **CRITICAL - link-type and service-call transitions**: `anchor` and `anchor-button` generate GET requests. Transitions with `service-call` require POST to execute the service. **GET silently skips service-call actions** — no error is shown, the transition just redirects without running the service. Use `link-type="hidden-form"` for any link that targets a transition containing a `service-call`.

```xml
<!-- WRONG: GET request, service-call is silently skipped -->
<link url="assignToMe" text="Assign" link-type="anchor-button">
    <parameter name="workEffortId"/>
</link>

<!-- CORRECT: POST request via hidden form, service-call executes -->
<link url="assignToMe" text="Assign" link-type="hidden-form">
    <parameter name="workEffortId"/>
</link>
```

- **`hidden-form` parameter pitfall**: `link-type="hidden-form"` only submits parameters explicitly listed in `parameter-map` (or `<parameter>` child elements). **Screen-level parameters** declared with `<parameter>` on the screen element are NOT automatically included in the form submission. If the transition needs screen parameters (e.g., for conditional redirect logic), add them explicitly to `parameter-map`.

```xml
<!-- WRONG: historyPartyId is a screen parameter but NOT submitted with the form -->
<link url="reactivate" text="Reactivate" link-type="hidden-form"
      parameter-map="[recordId:recordId]"/>
<!-- Transition cannot access historyPartyId for redirect logic -->

<!-- CORRECT: explicitly include screen parameters needed by the transition -->
<link url="reactivate" text="Reactivate" link-type="hidden-form"
      parameter-map="[recordId:recordId, historyPartyId:historyPartyId]"/>
```

### Transitions
- **Service Integration**: Call services via `service-call` in transitions
- **Error Response**: Include `error-response` for form validation
- **Parameter Validation**: Validate required parameters

#### Preserving Context ID in Responses

**MANDATORY: Create and edit transitions MUST pass the relevant entity ID back in the response.** When a transition uses `default-response url="."` to redirect back to the same screen, any parameters needed to maintain screen context must be explicitly included. Otherwise the redirect loses them and the screen renders without context.

**The rule by operation type:**
- **Create/Edit**: Always pass the ID back so the screen returns with the record selected
- **Delete**: Do NOT pass the ID back — the record no longer exists (or redirect to a list screen)
- **Selection-only**: Transitions with no service-call just pass the parameter through naturally

```xml
<!-- WRONG: uomId is lost, screen loses currency selection context -->
<transition name="addExchangeRate">
    <service-call name="set#ExchangerateValue"/>
    <default-response url="."/>
</transition>

<!-- CORRECT: uomId is passed back, screen returns with currency selected -->
<transition name="addExchangeRate">
    <service-call name="set#ExchangerateValue"/>
    <default-response url=".">
        <parameter name="uomId" from="fromCurrencyUomId"/>
    </default-response>
</transition>

<!-- CORRECT: Create transition preserves new record ID -->
<transition name="createRecord">
    <service-call name="create#MyEntity"/>
    <default-response url="."><parameter name="recordId"/></default-response>
</transition>

<!-- CORRECT: Update transition preserves current record ID -->
<transition name="updateStatus">
    <service-call name="update#TicketStatus"/>
    <default-response url="."><parameter name="workEffortId"/></default-response>
    <error-response url="." save-current-screen="true"><parameter name="workEffortId"/></error-response>
</transition>

<!-- CORRECT: Delete redirects to list without the deleted ID -->
<transition name="deleteRecord">
    <service-call name="delete#MyEntity"/>
    <default-response url="../RecordList"/>
</transition>
```

**Note**: When the form's field name differs from the screen parameter name (e.g., the form sends `fromCurrencyUomId` but the screen uses `uomId`), use the `from` attribute to map: `<parameter name="uomId" from="fromCurrencyUomId"/>`.

### Entity-Find-One Null Safety

**MANDATORY: Always check for null after `entity-find-one` before accessing properties**, especially when the lookup depends on a screen parameter that could be missing or invalid.

```xml
<!-- WRONG: NullPointerException if workEffortId is missing or invalid -->
<entity-find-one entity-name="mantle.work.effort.WorkEffort" value-field="ticket">
    <field-map field-name="workEffortId"/>
</entity-find-one>
<entity-find-one entity-name="moqui.basic.StatusItem" value-field="statusItem">
    <field-map field-name="statusId" from="ticket.statusId"/>  <!-- NPE here -->
</entity-find-one>

<!-- CORRECT: Guard against null before accessing properties -->
<entity-find-one entity-name="mantle.work.effort.WorkEffort" value-field="ticket">
    <field-map field-name="workEffortId"/>
</entity-find-one>
<if condition="!ticket">
    <message error="true">Record ${workEffortId} not found</message>
    <return/>
</if>
```

### Filter Context
- **Root Screen Setup**: Call filter context setup in `always-actions`
- **Data Display Issues**: Check filter context when data is missing
- **Debugging**: Log `ec.user.context.filterOrgIds` to diagnose issues

### Date Range Parameters

**MANDATORY: Use `fromDate` and `thruDate` for date range parameters.**

- **Parameter Names**: Always use `fromDate`/`thruDate`, NEVER `dateFrom`/`dateThru`
- **Type Conversion**: Date inputs from forms come as Strings; convert to Timestamp for entity queries:

```xml
<!-- Convert date strings to Timestamps for proper SQL comparison -->
<script><![CDATA[
    import java.sql.Timestamp
    import java.text.SimpleDateFormat
    def sdf = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')
    if (fromDate) {
        fromDateTs = new Timestamp(sdf.parse(fromDate + ' 00:00:00').getTime())
    }
    if (thruDate) {
        thruDateTs = new Timestamp(sdf.parse(thruDate + ' 23:59:59').getTime())
    }
]]></script>

<!-- Use converted Timestamps in econditions -->
<econdition field-name="estimatedStartDate" operator="greater-equals" from="fromDateTs" ignore-if-empty="true"/>
<econdition field-name="estimatedStartDate" operator="less-equals" from="thruDateTs" ignore-if-empty="true"/>
```

- **End of Day**: For `thruDate`, always add `23:59:59` to include the entire end date
- **PostgreSQL Compatibility**: Without conversion, comparing timestamp fields to string values causes SQL type errors

### Dynamic Dialogs (`dynamic-dialog`)

Dynamic dialogs load their content lazily from a separate screen when opened. Use them for heavy content that shouldn't slow down the parent screen render, or for reusable dialog screens shared across multiple parents.

#### Auto-Resolution Pattern

A `dynamic-dialog` `transition` attribute auto-resolves to a subscreen with the same name. No explicit `<transition>` element is needed.

```xml
<!-- Parent screen widgets -->
<dynamic-dialog id="HistoryDialog" button-text="Show History" transition="RelationshipHistory"
                parameter-map="[entityPartyId:partyId]"/>

<!-- Subscreen file: ParentScreen/RelationshipHistory.xml -->
<!-- Moqui auto-resolves "RelationshipHistory" to the subscreen file -->
```

The subscreen file should use `standalone="true"` and `default-menu-include="false"`:

```xml
<screen standalone="true" default-menu-include="false">
    <parameter name="entityPartyId"/>
    <!-- dialog content -->
</screen>
```

#### Sharing a Dynamic Dialog Across Screens

When multiple screens need the same dialog, place the screen file in one location and mount it as a subscreen in others using `subscreens-item`:

```xml
<!-- In a screen-extend, mount a shared dialog screen as a subscreen -->
<subscreens>
    <subscreens-item name="RelationshipHistory" menu-include="false"
                     location="component://{component}/screen/{path}/RelationshipHistory.xml"/>
</subscreens>

<!-- Then use it normally as a dynamic-dialog -->
<dynamic-dialog id="HistoryDialog" button-text="History" transition="RelationshipHistory"
                parameter-map="[historyPartyId:partyId]"/>
```

This works in `screen-extend` files, allowing you to add subscreens to screens defined in other components.

#### Parameter Naming Collision Pitfall

> **CRITICAL**: `dynamic-dialog` screens inherit the parent screen's context. If a dialog parameter name matches a variable already set in the parent context, the parent's value silently overrides the intended parameter.

```xml
<!-- WRONG: Parent screen already has organizationPartyId in context -->
<dynamic-dialog transition="History" parameter-map="[organizationPartyId:partyId]"/>
<!-- The dialog receives the parent's organizationPartyId, not partyId -->

<!-- CORRECT: Use unique prefixed names to avoid collision -->
<dynamic-dialog transition="History" parameter-map="[historyOrganizationPartyId:partyId]"/>
```

**Rule**: Always prefix `dynamic-dialog` parameter names with a qualifier (e.g., `history*`, `dialog*`) when there's any risk the name exists in the parent context.

#### Context-Aware Redirects in Shared Dialogs

When a shared `dynamic-dialog` screen is used from multiple parents, its transitions may need to redirect to different screens depending on the caller. Use `conditional-response` with the dialog's context parameters to determine the correct return path.

```xml
<!-- Shared dialog screen used from ContactDetail and OrganizationDetail -->
<parameter name="historyContactPartyId"/>
<parameter name="historyOrganizationPartyId"/>

<transition name="reactivateRecord">
    <service-call name="MyServices.reactivate#Record"/>
    <!-- From ContactDetail: go back to parent screen -->
    <conditional-response url=".." parameter-map="[partyId:historyContactPartyId]">
        <condition><expression>historyContactPartyId</expression></condition>
    </conditional-response>
    <!-- From OrganizationDetail: go to sibling FindOrganization (two levels up) -->
    <default-response url="../../FindOrganization" parameter-map="[partyId:historyOrganizationPartyId]"/>
</transition>
```

**Key points:**
- `url=".."` from a dialog subscreen navigates to the parent screen, closing the dialog
- URL depth depends on where the dialog is mounted (natural subscreen vs `subscreens-item`)
- The context parameters (`historyContactPartyId` vs `historyOrganizationPartyId`) double as both query filters and redirect discriminators
- Links in the dialog must use `link-type="hidden-form"` and explicitly include these parameters in `parameter-map` (see hidden-form parameter pitfall above)

### Responsive Design
- **Bootstrap Grid**: Use `row-col` with breakpoint specifications
- **Mobile First**: Design for mobile, enhance for larger screens
- **Breakpoints**: xs (<576px), sm (576px), md (768px), lg (992px)

### Header-Dialog Pattern

**Use for create/edit dialogs in list screens:**

```xml
<form-list name="OrderList" list="orderList" skip-form="true" header-dialog="true">
    <!-- Header fields for search (shown in dialog) -->
    <field name="statusId"><header-field title="Status">
        <drop-down allow-empty="true">
            <entity-options .../>
        </drop-down>
    </header-field></field>

    <!-- Default fields for display -->
    <field name="orderId"><default-field title="Order ID">
        <display/>
    </default-field></field>
</form-list>
```

### Form-List Searchable Configuration

**Enable search functionality:**

```xml
<form-list name="OrderList" list="orderList" skip-form="true"
           header-dialog="true" select-columns="true" saved-finds="true">
```

| Attribute | Purpose |
|-----------|---------|
| `header-dialog="true"` | Search fields in dialog |
| `select-columns="true"` | Column visibility toggle |
| `saved-finds="true"` | Save search configurations |
| `show-csv-button="true"` | CSV export option |
| `show-xlsx-button="true"` | Excel export option |

### Section Patterns

```xml
<!-- Conditional section -->
<section name="OrderDetails" condition="order != null">
    <widgets>
        <!-- Content shown when condition is true -->
    </widgets>
    <fail-widgets>
        <!-- Content shown when condition is false -->
    </fail-widgets>
</section>

<!-- Section with dynamic content -->
<section-iterate name="ItemList" list="itemList" entry="item">
    <widgets>
        <!-- Repeated for each item -->
    </widgets>
</section-iterate>
```

### Container-Box Pattern

> **MUST**: Every `<container-box>` MUST include a `<box-header>` element. In the Quasar renderer (`/qapps`), omitting `<box-header>` causes an FTL template error: `Expected a node, but this has evaluated to a sequence+extended_hash+string` in `DefaultScreenMacros.qvt.ftl`. This is because the `#recurse boxHeader` macro requires a node, not an empty value.

```xml
<container-box>
    <box-header title="Order Details"/>
    <box-body>
        <form-single name="OrderForm">
            <!-- Form content -->
        </form-single>
    </box-body>
    <box-body-nopad>
        <form-list name="ItemList">
            <!-- List content -->
        </form-list>
    </box-body-nopad>
</container-box>
```

### Container-Row Split Layout with Conditional Panel

**Master-detail pattern using container-row with conditional right panel:**

```xml
<container-row>
    <row-col lg="5">
        <container-box>
            <box-header title="Master List"/>
            <box-toolbar>
                <container-dialog id="CreateDialog" button-text="Create New">
                    <form-single name="CreateForm" transition="createRecord">
                        <!-- Create form fields -->
                    </form-single>
                </container-dialog>
            </box-toolbar>
            <box-body>
                <form-list name="MasterList" list="masterList" skip-form="true">
                    <field name="recordId"><default-field title="ID">
                        <link url="selectRecord" text="${recordId}" link-type="anchor">
                            <parameter name="recordId"/>
                        </link>
                    </default-field></field>
                    <field name="description"><default-field><display/></default-field></field>
                </form-list>
            </box-body>
        </container-box>
    </row-col>
    <row-col lg="7">
        <section name="DetailSection" condition="recordId &amp;&amp; selectedRecord">
            <widgets>
                <container-box>
                    <box-header title="Detail: ${selectedRecord.description}"/>
                    <box-body>
                        <!-- Detail content -->
                    </box-body>
                </container-box>
            </widgets>
        </section>
    </row-col>
</container-row>
```

**Key elements:**
- **lg attribute**: Specifies Bootstrap grid columns at large breakpoint (total=12). Common splits: `5+7`, `4+8`, `6+6`
- **Section with condition**: Right panel only renders when a record is selected via parameter
- **Navigation link**: Master list items link to same screen with parameter to select the record
- **selectRecord transition**: A parameter-passing transition (`<default-response url="."/>`) with no service-call

**When to use:**
- Admin/configuration screens where you select from a list and see related details
- Master-detail patterns where the detail panel is optional
- Screens like currency management (list currencies + exchange rates for selected currency)