# Dropdown Standards

### Basic Entity Dropdown

```xml
<drop-down>
    <entity-options key="${statusId}" text="${description}">
        <entity-find entity-name="moqui.basic.StatusItem">
            <econdition field-name="statusTypeId" value="OrderStatus"/>
            <order-by field-name="sequenceNum"/>
        </entity-find>
    </entity-options>
</drop-down>
```

### Entity-Options Requirements (CRITICAL)

**`entity-options` MUST be inside `entity-find`:**

```xml
<!-- CORRECT -->
<entity-options key="${partyId}" text="${organizationName}">
    <entity-find entity-name="mantle.party.PartyDetail">
        <econdition field-name="partyTypeEnumId" value="PtyOrganization"/>
    </entity-find>
</entity-options>

<!-- WRONG: entity-options without entity-find -->
<entity-options key="${partyId}" text="${organizationName}"
                entity-name="mantle.party.PartyDetail"/>
```

### Server-Search Dropdowns

**Use `server-search="true"` for large datasets (>100 options):**

```xml
<drop-down server-search="true" server-search-min-input-length="2">
    <entity-options key="${productId}" text="${productName} (${productId})">
        <entity-find entity-name="mantle.product.Product">
            <econdition field-name="productName" operator="like" value="%${term}%"
                        ignore="term == null"/>
            <econdition field-name="statusId" value="ProdActive"/>
            <order-by field-name="productName"/>
        </entity-find>
    </entity-options>
</drop-down>
```

**Server-search requirements:**
- Large data sets (>100 records)
- Filter by `${term}` parameter
- Set `server-search-min-input-length` (default: 3)

### Allow Empty Option

```xml
<!-- Allow blank selection -->
<drop-down allow-empty="true">
    <entity-options ...>
</drop-down>

<!-- No blank option (selection required) -->
<drop-down>
    <entity-options ...>
</drop-down>
```

### Static Options (List-Options)

```xml
<drop-down>
    <option key="HIGH" text="High Priority"/>
    <option key="MEDIUM" text="Medium Priority"/>
    <option key="LOW" text="Low Priority"/>
</drop-down>
```

### Dynamic List-Options

```xml
<drop-down>
    <list-options list="statusList" key="${statusId}" text="${description}"/>
</drop-down>
```

### Current Value Selection

**Ensure current value is in options:**

```xml
<drop-down current="selected">
    <entity-options key="${statusId}" text="${description}">
        <entity-find entity-name="moqui.basic.StatusItem">
            <!-- Filter may exclude current value if not careful -->
        </entity-find>
    </entity-options>
</drop-down>
```

### Dependent Dropdowns

```xml
<!-- Parent dropdown -->
<field name="categoryId"><default-field title="Category">
    <drop-down id="categoryDropdown">
        <entity-options key="${categoryId}" text="${categoryName}">
            <entity-find entity-name="Category"/>
        </entity-options>
    </drop-down>
</default-field></field>

<!-- Child dropdown depends on parent -->
<field name="subcategoryId"><default-field title="Subcategory">
    <drop-down depends-on="categoryId">
        <entity-options key="${subcategoryId}" text="${subcategoryName}">
            <entity-find entity-name="Subcategory">
                <econdition field-name="categoryId" from="categoryId"/>
            </entity-find>
        </entity-options>
    </drop-down>
</default-field></field>
```

### No-Current-Selected-Key Pattern

**For dependent dropdowns with no initial selection:**

```xml
<drop-down no-current-selected-key="" depends-on="categoryId">
```

### Entity-Options with Exclusion Filter (not-in)

**Exclude specific values from entity-options using `not-in` operator:**

```xml
<drop-down no-current-selected-key="PClsInsumo">
    <entity-options key="${enumId}" text="${description}">
        <entity-find entity-name="moqui.basic.Enumeration">
            <econdition field-name="enumTypeId" value="ProductClass"/>
            <econdition field-name="enumId" operator="not-in" value="PClsProduct,PClsService"/>
            <order-by field-name="description"/>
        </entity-find>
    </entity-options>
</drop-down>
```

**When to use:**
- An EnumerationType has legacy/demo values that should not appear in the UI
- You want to show a subset of values without creating a new EnumerationType
- Transitioning from old enum values to new ones (old ones deleted in seed-initial, but might still exist in database)

**Note:** The `not-in` operator accepts a comma-separated list of values. For single exclusions, use `operator="not-equals"` instead.

### Multiple Selection

```xml
<drop-down allow-multiple="true">
    <entity-options ...>
</drop-down>
```

### Dropdown with Custom Text Format

```xml
<drop-down>
    <entity-options key="${partyId}"
                    text="${organizationName} - ${city}, ${countryGeoId}">
        <entity-find entity-name="mantle.party.PartyDetail"/>
    </entity-options>
</drop-down>
```

### Anti-Patterns

```xml
<!-- WRONG: entity-options without entity-find wrapper -->
<entity-options entity-name="Entity" key="${id}" text="${name}"/>

<!-- WRONG: server-search on small dataset -->
<drop-down server-search="true">
    <entity-options>
        <entity-find>
            <!-- Only 10 options - don't need server-search -->
        </entity-find>
    </entity-options>
</drop-down>

<!-- WRONG: No filter for server-search -->
<drop-down server-search="true">
    <entity-options>
        <entity-find entity-name="Product">
            <!-- Missing ${term} filter - returns all records! -->
        </entity-find>
    </entity-options>
</drop-down>
```

### Heavy Dropdown in container-dialog (Performance Anti-Pattern)

**Problem**: A `container-dialog` renders its content on initial page load, even though the user hasn't opened the dialog yet. If the dialog contains a dropdown with `entity-options` loading a large dataset (e.g., all vendors, all products), the page load is blocked while the full dataset is fetched.

```xml
<!-- WRONG: All vendors loaded on page render, even if user never opens the dialog -->
<container-dialog id="AddDialog" button-text="Add">
    <form-single name="AddForm" transition="addRecord">
        <field name="vendorPartyId"><default-field title="Vendor"><drop-down>
            <entity-options key="${partyId}" text="PartyNameOnlyTemplate">
                <entity-find entity-name="mantle.party.PartyDetailAndRole">
                    <econdition field-name="roleTypeId" operator="in" value="Vendor,Supplier"/>
                </entity-find>
            </entity-options>
        </drop-down></default-field></field>
    </form-single>
</container-dialog>
```

**Solution**: Convert to `dynamic-dialog` (content loaded only when opened) and use `server-search="true"` with `dynamic-options` (results fetched as user types):

```xml
<!-- Parent screen: dialog content loaded on-demand -->
<dynamic-dialog id="AddDialog" button-text="Add" transition="AddRecord"/>
```

```xml
<!-- AddRecord.xml (standalone="true" subscreen) -->
<screen standalone="true">
    <transition name="getVendorList">
        <actions>
            <service-call name="{shared-utils}.OpenSearchServices.search#Party"
                          in-map="context+[requireRoleList:['Supplier', 'Vendor'],
                                           requireAllRoles:false,
                                           excludeNaOwnerPartyId:true,
                                           commonOwnerPartyIdList:'ChileCompra']"
                          out-map="context" web-send-json-response="partyList"/>
        </actions>
        <default-response type="none"/>
    </transition>

    <widgets>
        <form-single name="AddForm" transition="addRecord">
            <field name="vendorPartyId"><default-field title="Vendor"><drop-down>
                <dynamic-options transition="getVendorList" server-search="true"/>
            </drop-down></default-field></field>
        </form-single>
    </widgets>
</screen>
```

**When to apply this pattern**:
- Dropdown loads >100 records AND is inside a dialog
- The dialog is not opened on every page visit (optional action)
- The entity being searched has an OpenSearch index available

**See also**: `framework-guide.md` § "When to Use dynamic-dialog Instead of container-dialog"

### Server-Search Checklist

- [ ] Dataset has >100 potential options
- [ ] Filter includes `${term}` parameter
- [ ] `server-search-min-input-length` is appropriate
- [ ] Results are ordered logically
- [ ] Text format helps user identify correct option
