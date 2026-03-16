# Moqui Screen Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/frontend/screens.md` - Screen structure, containers, form-list configuration
- `standards/frontend/forms.md` - Form types, limitations, reserved names
- `standards/frontend/subscreens.md` - Auto-discovery rules, navigation patterns
- `standards/frontend/html-injection.md` - Render-mode and raw HTML injection patterns
- `standards/frontend/screen-reuse.md` - Transition-include, section-include, form extends patterns

**Framework Guide Reference**: For detailed patterns and explanations, see `runtime/component/moqui-agent-os/framework-guide.md`:
- **Dependent Dropdowns**: Search for "#### Dependent Dropdowns with `depends-on`" - `depends-on` syntax, `dynamic-options`, server-search
- **AJAX Transitions & Context**: Search for "#### Screen Context and AJAX Transitions" - always-actions execution, context availability
- **Context Persistence**: Search for "#### Context Persistence: Request vs Session Scope" - `ec.web.sessionAttributes` vs `ec.user.context`
- **Form Field Patterns**: Search for "#### Form Field Requirements" - validation, required fields, entity-options
- **File Downloads in Quasar**: Search for "### File Download Transitions in Quasar Apps" - `/apps/` vs `/qapps/`, response handling, `sendResourceResponse`

---

## Screen Templates

### Standard Screen Template
```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="[SCREEN_TITLE]" default-menu-index="[INDEX]">

    <parameter name="[PARAMETER_NAME]" required="[true/false]"/>

    <transition name="[TRANSITION_NAME]">
        <service-call name="[SERVICE_NAME]"/>
        <default-response url="[RESPONSE_URL]"/>
        <error-response url="[ERROR_URL]"/>
    </transition>

    <actions>
        <entity-find-one entity-name="[ENTITY_NAME]" value-field="[FIELD_NAME]"/>
    </actions>

    <widgets>
        <container>
            <!-- Screen content here -->
        </container>
    </widgets>
</screen>
```

### Modal Dialog Screen Template
```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-include="false" standalone="true">

    <parameter name="[PARAMETER_NAME]" required="true"/>

    <transition name="[SUBMIT_TRANSITION]">
        <service-call name="[SERVICE_NAME]"/>
        <default-response url="."/>
    </transition>

    <widgets>
        <container-dialog id="[DIALOG_ID]" button-text="[BUTTON_TEXT]">
            <form-single name="[FORM_NAME]" transition="[SUBMIT_TRANSITION]">
                <!-- Form fields -->
            </form-single>
        </container-dialog>
    </widgets>
</screen>
```

---

## Mounted Screen Sections Pattern

### When to Use This Pattern

Use when adding a new section to an application screen that has explicit `subscreens-item` declarations (NOT auto-discovery).

### Pattern Structure

```
ParentScreen.xml                  # Parent with subscreens-item declarations
ParentScreen/                     # Directory for subscreens
├── NewSection.xml                # Container screen for new section
├── NewSection/                   # Directory for auto-discovered subscreens
│   ├── ChildScreen1.xml          # Auto-discovered by NewSection.xml
│   ├── ChildScreen2.xml          # Auto-discovered by NewSection.xml
│   └── ChildScreen3.xml          # Auto-discovered by NewSection.xml
└── ExistingScreen.xml            # Other screens
```

### Step-by-Step Implementation

**1. Create Container Screen** (`ParentScreen/NewSection.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="Nueva Sección" default-menu-index="20">

    <subscreens default-item="DefaultChild"/>

    <widgets>
        <subscreens-panel id="NuevaSectionPanel"/>
    </widgets>
</screen>
```

**2. Create Subdirectory** (`ParentScreen/NewSection/`):
```bash
mkdir ParentScreen/NewSection/
```

**3. Create Child Screens** (`ParentScreen/NewSection/DefaultChild.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="Child Screen" default-menu-index="1">

    <widgets>
        <!-- Screen content -->
    </widgets>
</screen>
```

**4. Register in Parent Screen** (`ParentScreen.xml`):
```xml
<subscreens default-item="ExistingDefault">
    <subscreens-item name="ExistingSection" location="..." menu-index="10"/>
    <subscreens-item name="NewSection" location="component://mycomp/screen/ParentScreen/NewSection.xml" menu-index="20"/>
</subscreens>
```

### Common Mistake

**WRONG: Container screen inside subdirectory**
```
Administracion/
└── Usuarios/
    ├── Usuarios.xml           # WRONG location - won't be found
    └── UserList.xml
```

**CORRECT: Container screen at same level as subdirectory**
```
Administracion/
├── Usuarios.xml               # Container screen
└── Usuarios/                  # Child screens directory
    └── UserList.xml
```

---

## Server-Search Transition Pattern

### CRITICAL: Handle Both ID Lookup and Text Search

Server-search transitions are called in TWO scenarios:
1. **User typing**: `term` contains the search text
2. **Page reload**: `term` contains the selected ID(s)

**CORRECT Pattern:**
```xml
<transition name="getPartyList">
    <actions>
        <entity-find entity-name="mantle.party.PartyDetail" list="partyList">
            <econditions combine="or">
                <!-- ID-based lookup (term contains the ID on page reload) -->
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

### Key Pattern Elements

| Element | Purpose |
|---------|---------|
| `<econdition field-name="partyId" operator="in" from="term" ignore-if-empty="true"/>` | Matches by ID on page reload |
| `ignore="!term"` | Skips text conditions when term is empty |
| `combine="or"` | Matches if term is ID OR matches name fields |

---

## Multi-Select Server-Search Workaround

### Problem
Multi-select server-search dropdowns do NOT automatically fetch labels on page reload.

### Solution: Hybrid list-options + dynamic-options

**1. Screen Actions:**
```xml
<actions>
    <!-- Fetch labels for pre-selected values -->
    <if condition="customerPartyId">
        <entity-find entity-name="mantle.party.PartyDetail" list="selectedCustomerList">
            <econdition field-name="partyId" operator="in" from="customerPartyId"/>
        </entity-find>
    <else>
        <set field="selectedCustomerList" from="[]"/>
    </else></if>
</actions>
```

**2. Drop-down:**
```xml
<field name="customerPartyId">
    <header-field title="Customer">
        <drop-down allow-empty="true" allow-multiple="true">
            <!-- list-options provides labels for pre-selected values -->
            <list-options list="selectedCustomerList" key="${partyId}"
                          text="${ec.resource.expand('PartyNameTemplate', null, it)}"/>
            <!-- dynamic-options provides search functionality -->
            <dynamic-options transition="getCustomerPartyList" server-search="true" min-length="2"/>
        </drop-down>
    </header-field>
</field>
```

---

## Render Mode Pattern

### Multi-Render Mode HTML Content

**CRITICAL**: Must specify ALL render modes where content should appear.

```xml
<!-- WRONG (content won't show in Vuet/Qvt): -->
<render-mode><text type="html"><![CDATA[
    <div class="alert alert-warning">Warning</div>
]]></text></render-mode>

<!-- CORRECT (shows in all modes): -->
<render-mode><text type="html,vuet,qvt"><![CDATA[
    <div class="alert alert-warning">Warning</div>
]]></text></render-mode>
```

---

## Date Range Parameters Pattern

### Type Conversion for Timestamp Comparison

Date inputs from forms come as Strings. Convert to Timestamp:

```xml
<actions>
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

    <entity-find entity-name="EntityName" list="resultList">
        <econdition field-name="createdDate" operator="greater-equals" from="fromDateTs" ignore-if-empty="true"/>
        <econdition field-name="createdDate" operator="less-equals" from="thruDateTs" ignore-if-empty="true"/>
    </entity-find>
</actions>
```

---

## Chart.js Drill-Down Navigation Pattern

For interactive charts with clickable data points that navigate to detail screens:

### Server-Side: JSON Data Serialization
```xml
<actions>
    <script><![CDATA[
        import groovy.json.JsonOutput

        // Build chart data with navigation URLs
        def chartLabels = []
        def chartData = []
        def chartUrls = []

        for (item in summaryList) {
            chartLabels.add(item.label)
            chartData.add(item.count)
            // Build drill-down URL
            def params = new URLSearchParams()
            params.append("statusId", item.statusId)  // append(), not set()
            chartUrls.add("DetailScreen?" + params.toString())
        }

        chartLabelsJson = JsonOutput.toJson(chartLabels)
        chartDataJson = JsonOutput.toJson(chartData)
        chartUrlsJson = JsonOutput.toJson(chartUrls)
    ]]></script>
</actions>
```

### Client-Side: Chart with Click Handler
```xml
<render-mode>
    <text type="html"><![CDATA[
        <canvas id="myChart" width="400" height="300"></canvas>
        <script>
            var ctx = document.getElementById('myChart').getContext('2d');
            var labels = ${chartLabelsJson};
            var data = ${chartDataJson};
            var urls = ${chartUrlsJson};

            var chart = new Chart(ctx, {
                type: 'bar',
                data: { labels: labels, datasets: [{ data: data }] },
                options: {
                    onClick: function(evt, elements) {
                        if (elements.length > 0) {
                            var idx = elements[0].index;
                            window.location.href = urls[idx];
                        }
                    }
                }
            });
        </script>
    ]]></text>
</render-mode>
```

**Key points:**
- Use `groovy.json.JsonOutput.toJson()` for server-side serialization
- Use `URLSearchParams.append()` (not `set()`) for Moqui multi-value parameters
- In `render-mode` templates, Groovy `${}` interpolation works (the template is processed server-side)
- `chartjs-plugin-datalabels` can be added for inline data labels

---

## Quality Checklist

**Structure:**
- [ ] Correct XML namespace declaration
- [ ] Every transition has `<default-response>`
- [ ] Parameters defined for required inputs

**Navigation:**
- [ ] Subscreens use automatic discovery when possible
- [ ] `subscreens-item` only used for external screens (with `location` attribute)
- [ ] Container screens at same level as subdirectory (NOT inside)

**Forms:**
- [ ] form-list has mandatory `list` attribute
- [ ] Hidden fields use `from` attribute at field level
- [ ] Server-search transitions handle both ID lookup and text search
- [ ] Multi-select uses hybrid list-options + dynamic-options pattern

**Date Ranges:**
- [ ] Use `fromDate`/`thruDate` naming
- [ ] Convert String dates to Timestamp before comparing
- [ ] Use `23:59:59` for thruDate to include entire end date