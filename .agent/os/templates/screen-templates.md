# Moqui Screen Templates

This document provides Moqui-compliant XML templates for screen development. All templates follow the critical XML schema patterns defined in the moqui-screen-specialist agent.

## Critical XML Schema Compliance Notes

**IMPORTANT: All templates use xml-screen-3.xsd schema and follow these patterns:**

### Correct entity-options Pattern
```xml
<!-- ✅ CORRECT: Use key and text attributes with entity-find -->
<entity-options key="${statusId}" text="${description}">
    <entity-find entity-name="moqui.basic.StatusItem">
        <econdition field-name="statusTypeId" value="MyStatusType"/>
        <order-by field-name="sequenceNum"/>
    </entity-find>
</entity-options>

<!-- ❌ WRONG: Do NOT use entity-constraint/entity-order-by tags -->
<entity-options entity-name="moqui.basic.StatusItem" text="${description}">
    <entity-constraint name="statusTypeId" value="MyStatusType"/>
    <entity-order-by field-name="sequenceNum"/>
</entity-options>
```

### Schema Version
- Always use `xml-screen-3.xsd` (current version)
- Never use outdated versions like `xml-screen-2.1.xsd`

### Container Layout
```xml
<!-- ✅ CORRECT: Use lg, md, sm, xs attributes for responsive columns -->
<container-row>
    <row-col md="6" lg="4">Content here</row-col>
    <row-col md="6" lg="8">Content here</row-col>
</container-row>

<!-- ❌ WRONG: Do NOT use style attribute for column sizing -->
<container-row>
    <row-col style="col-md-6">Content here</row-col>
</container-row>
```
- Use `container-row` with `row-col` for grid layouts
- Use numeric attributes: `lg`, `md`, `sm`, `xs` (values 1-12)
- Never use `style="col-md-*"` for column sizing

## Standard Screen Template

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
        <set field="[FIELD_NAME]" value="[VALUE]"/>
    </actions>
    
    <widgets>
        <container>
            <label text="[SCREEN_CONTENT]" type="h1"/>
            <section name="[SECTION_NAME]">
                <widgets>
                    <!-- Screen content here -->
                </widgets>
            </section>
        </container>
    </widgets>
</screen>
```

## List/Detail Screen Template

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="[SCREEN_TITLE]">
    
    <parameter name="[ID_PARAMETER]"/>
    
    <transition name="create[ENTITY_NAME]">
        <service-call name="create#[ENTITY_NAME]"/>
        <default-response url="../[ENTITY_NAME]Detail">
            <parameter name="[ID_PARAMETER]" from="[ID_PARAMETER]"/>
        </default-response>
    </transition>
    
    <transition name="update[ENTITY_NAME]">
        <service-call name="update#[ENTITY_NAME]"/>
        <default-response url="."/>
    </transition>
    
    <transition name="[ENTITY_NAME]Detail">
        <default-response url="../[ENTITY_NAME]Detail"/>
    </transition>
    
    <widgets>
        <container>
            <container-dialog id="Create[ENTITY_NAME]Dialog" button-text="Create [ENTITY_NAME]">
                <form-single name="Create[ENTITY_NAME]Form" transition="create[ENTITY_NAME]">
                    <field name="[FIELD_NAME]">
                        <default-field title="[FIELD_TITLE]">
                            <text-line size="[SIZE]"/>
                        </default-field>
                    </field>
                    <field name="submitButton">
                        <default-field title="Create">
                            <submit/>
                        </default-field>
                    </field>
                </form-single>
            </container-dialog>
            
            <form-list name="[ENTITY_NAME]List" skip-form="true" header-dialog="true">
                <entity-find entity-name="[ENTITY_NAME]" list="[ENTITY_NAME_LC]List">
                    <search-form-inputs default-order-by="[ORDER_FIELD]"/>
                </entity-find>
                <field name="[FIELD_NAME]">
                    <header-field title="[HEADER_TITLE]" show-order-by="true"/>
                    <default-field>
                        <link url="[ENTITY_NAME]Detail" text="${[FIELD_NAME]}" parameter-map="[[ID_PARAMETER]:[ID_PARAMETER]]"/>
                    </default-field>
                </field>
                
                <field name="[STATUS_FIELD]">
                    <header-field title="Status" show-order-by="true"/>
                    <default-field>
                        <display text="${[STATUS_FIELD]}"/>
                    </default-field>
                </field>
                
                <field name="[ACTION_FIELD]">
                    <header-field title="Actions"/>
                    <default-field>
                        <link url="[ENTITY_NAME]Detail" text="Edit" parameter-map="[[ID_PARAMETER]:[ID_PARAMETER]]"/>
                    </default-field>
                </field>
            </form-list>
        </container>
    </widgets>
</screen>
```

## Detail Edit Screen Template

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="[ENTITY_NAME] Detail">
    
    <parameter name="[ID_PARAMETER]" required="true"/>
    
    <transition name="update[ENTITY_NAME]">
        <service-call name="update#[ENTITY_NAME]"/>
        <default-response url="."/>
    </transition>
    
    <transition name="delete[ENTITY_NAME]">
        <service-call name="delete#[ENTITY_NAME]"/>
        <default-response url="../[ENTITY_NAME]"/>
    </transition>
    
    <actions>
        <entity-find-one entity-name="[ENTITY_NAME]" value-field="[ENTITY_NAME_LC]"/>
        <if condition="![ENTITY_NAME_LC]">
            <return error="true" message="[ENTITY_NAME] not found with ID ${[ID_PARAMETER]}"/>
        </if>
    </actions>
    
    <widgets>
        <container>
            <container-row>
                <row-col md="8">
                    <form-single name="[ENTITY_NAME]DetailForm" map="[ENTITY_NAME_LC]" transition="update[ENTITY_NAME]">
                        <field name="[ID_PARAMETER]">
                            <default-field><hidden/></default-field>
                        </field>
                        
                        <field name="[FIELD_NAME]">
                            <default-field title="[FIELD_TITLE]">
                                <text-line size="[SIZE]"/>
                            </default-field>
                        </field>
                        
                        <field name="[DESCRIPTION_FIELD]">
                            <default-field title="Description">
                                <text-area cols="60" rows="3"/>
                            </default-field>
                        </field>
                        
                        <field name="[STATUS_FIELD]">
                            <default-field title="Status">
                                <drop-down>
                                    <entity-options key="${statusId}" text="${description}">
                                        <entity-find entity-name="moqui.basic.StatusItem">
                                            <econdition field-name="statusTypeId" value="[STATUS_TYPE_ID]"/>
                                            <order-by field-name="sequenceNum"/>
                                        </entity-find>
                                    </entity-options>
                                </drop-down>
                            </default-field>
                        </field>
                        
                        <field name="submitButton">
                            <default-field title="Update">
                                <submit/>
                            </default-field>
                        </field>
                        
                        <field name="deleteButton">
                            <default-field title="">
                                <link url="delete[ENTITY_NAME]" text="Delete" 
                                      parameter-map="[[ID_PARAMETER]:[ID_PARAMETER]]"
                                      confirmation="Are you sure you want to delete this [ENTITY_NAME]?"/>
                            </default-field>
                        </field>
                    </form-single>
                </row-col>
                
                <row-col md="4">
                    <section name="[ENTITY_NAME]InfoSection">
                        <widgets>
                            <container>
                                <label text="[ENTITY_NAME] Information" type="h3"/>
                                <label text="Created: ${ec.l10n.format([ENTITY_NAME_LC].createdStamp, null)}" type="p"/>
                                <label text="Last Updated: ${ec.l10n.format([ENTITY_NAME_LC].lastUpdatedStamp, null)}" type="p"/>
                            </container>
                        </widgets>
                    </section>
                </row-col>
            </container-row>
        </container>
    </widgets>
</screen>
```

## Modal Dialog Screen Template

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        require-authentication="[TRUE/FALSE]">
    
    <parameter name="[PARAMETER_NAME]"/>
    
    <transition name="[ACTION_NAME]">
        <service-call name="[SERVICE_NAME]"/>
        <default-response url="[RESPONSE_URL]"/>
        <error-response url="."/>
    </transition>
    
    <actions>
        <!-- Prepare data for modal -->
        <set field="[DATA_FIELD]" value="[VALUE]"/>
    </actions>
    
    <widgets>
        <form-single name="[FORM_NAME]" transition="[ACTION_NAME]">
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
    </widgets>
</screen>
```

## Dashboard Screen Template

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="[DASHBOARD_TITLE]">
    
    <actions>
        <!-- Dashboard data preparation -->
        <entity-find entity-name="[ENTITY_NAME]" list="[SUMMARY_LIST]">
            <select-field field-name="[GROUP_BY_FIELD]"/>
            <select-field field-name="[COUNT_FIELD]" function="count"/>
        </entity-find>
    </actions>
    
    <widgets>
        <container>
            <label text="[DASHBOARD_TITLE]" type="h1"/>
            
            <container-row>
                <row-col md="3">
                    <container type="panel">
                        <container-panel type="header">
                            <label text="[METRIC_1_TITLE]" type="h4"/>
                        </container-panel>
                        <container>
                            <label text="${[METRIC_1_VALUE]}" type="h2"/>
                        </container>
                    </container>
                </row-col>
                
                <row-col md="3">
                    <container type="panel">
                        <container-panel type="header">
                            <label text="[METRIC_2_TITLE]" type="h4"/>
                        </container-panel>
                        <container>
                            <label text="${[METRIC_2_VALUE]}" type="h2"/>
                        </container>
                    </container>
                </row-col>
                
                <row-col md="6">
                    <section name="[CHART_SECTION]">
                        <widgets>
                            <!-- Chart or detailed information -->
                            <form-list name="[SUMMARY_LIST_NAME]" list="[SUMMARY_LIST]" skip-form="true">
                                <field name="[GROUP_FIELD]">
                                    <default-field title="[GROUP_TITLE]">
                                        <display/>
                                    </default-field>
                                </field>
                                <field name="[COUNT_FIELD]">
                                    <default-field title="Count">
                                        <display/>
                                    </default-field>
                                </field>
                            </form-list>
                        </widgets>
                    </section>
                </row-col>
            </container-row>
        </container>
    </widgets>
</screen>
```

## Search/Filter Screen Template

```xml
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        default-menu-title="Search [ENTITY_NAME]">
    
    <transition name="search[ENTITY_NAME]">
        <default-response url="."/>
    </transition>
    
    <widgets>
        <container>
            <form-single name="Search[ENTITY_NAME]Form" transition="search[ENTITY_NAME]">
                <field name="[SEARCH_FIELD]">
                    <default-field title="[SEARCH_TITLE]">
                        <text-line size="20"/>
                    </default-field>
                </field>
                
                <field name="[STATUS_FILTER]">
                    <default-field title="Status">
                        <drop-down allow-empty="true">
                            <entity-options key="${statusId}" text="${description}">
                                <entity-find entity-name="moqui.basic.StatusItem">
                                    <econdition field-name="statusTypeId" value="[STATUS_TYPE_ID]"/>
                                    <order-by field-name="sequenceNum"/>
                                </entity-find>
                            </entity-options>
                        </drop-down>
                    </default-field>
                </field>
                
                <field name="[FROM_DATE_FIELD]">
                    <default-field title="From Date">
                        <date-time type="date"/>
                    </default-field>
                </field>
                
                <field name="[THRU_DATE_FIELD]">
                    <default-field title="Through Date">
                        <date-time type="date"/>
                    </default-field>
                </field>
                
                <field name="submitButton">
                    <default-field title="Search">
                        <submit/>
                    </default-field>
                </field>
            </form-single>
            
            <form-list name="Search[ENTITY_NAME]Results" skip-form="true">
                <entity-find entity-name="[ENTITY_NAME]" list="[ENTITY_NAME_LC]List">
                    <search-form-inputs default-order-by="[ORDER_FIELD]"/>
                    <date-filter from-field-name="[FROM_DATE_FIELD]" thru-field-name="[THRU_DATE_FIELD]"/>
                </entity-find>
                <field name="[FIELD_NAME]">
                    <header-field title="[HEADER_TITLE]" show-order-by="true"/>
                    <default-field>
                        <display text="${[FIELD_NAME]}"/>
                    </default-field>
                </field>
                
                <field name="[DATE_FIELD]">
                    <header-field title="Date" show-order-by="true"/>
                    <default-field>
                        <display text="${ec.l10n.format([DATE_FIELD], null)}"/>
                    </default-field>
                </field>
                
                <field name="actions">
                    <header-field title="Actions"/>
                    <default-field>
                        <link url="[DETAIL_URL]" text="View" parameter-map="[[ID_PARAMETER]:[ID_PARAMETER]]"/>
                    </default-field>
                </field>
            </form-list>
        </container>
    </widgets>
</screen>
```

## Placeholders Reference

- `[SCREEN_TITLE]` - Display title for the screen
- `[INDEX]` - Menu index number for ordering
- `[PARAMETER_NAME]` - Screen parameter name
- `[TRANSITION_NAME]` - Transition action name
- `[SERVICE_NAME]` - Service to call in transition
- `[RESPONSE_URL]` - Success response URL
- `[ERROR_URL]` - Error response URL
- `[ENTITY_NAME]` - PascalCase entity name
- `[ENTITY_NAME_LC]` - camelCase entity name for variables
- `[FIELD_NAME]` - Entity field name
- `[FIELD_TITLE]` - Display title for field
- `[VALUE]` - Field value or expression
- `[SIZE]` - Input field size
- `[ID_PARAMETER]` - Primary key parameter name
- `[ORDER_FIELD]` - Default ordering field
- `[HEADER_TITLE]` - Column header title
- `[STATUS_FIELD]` - Status field name
- `[STATUS_TYPE_ID]` - Status type identifier
- `[ACTION_FIELD]` - Actions column identifier
- `[DESCRIPTION_FIELD]` - Description field name
- `[BUTTON_TEXT]` - Button display text
- `[FORM_NAME]` - Form identifier
- `[ACTION_NAME]` - Action transition name
- `[DATA_FIELD]` - Data preparation field
- `[DASHBOARD_TITLE]` - Dashboard page title
- `[METRIC_1_TITLE]` - First metric display title
- `[METRIC_1_VALUE]` - First metric value expression
- `[SEARCH_FIELD]` - Search input field name
- `[SEARCH_TITLE]` - Search field display title
- `[FROM_DATE_FIELD]` - Date range start field
- `[THRU_DATE_FIELD]` - Date range end field
- `[CHART_SECTION]` - Chart or summary section name