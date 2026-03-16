# Moqui Entity Templates

## Standard Entity Template

```xml
<entity entity-name="[ENTITY_NAME]" package="[PACKAGE_NAME]">
    <!-- Primary key -->
    <field name="[ENTITY_ID]" type="id" is-pk="true"/>
    
    <!-- Foreign key relationships -->
    <field name="[PARENT_ID]" type="id"/>
    
    <!-- Business fields -->
    <field name="name" type="text-medium"/>
    <field name="description" type="text-long"/>
    
    <!-- Status and lifecycle -->
    <field name="statusId" type="id"/>
    <field name="fromDate" type="date-time"/>
    <field name="thruDate" type="date-time"/>
    
    <!-- Audit fields -->
    <field name="lastUpdatedStamp" type="date-time"/>
    <field name="lastUpdatedByUser" type="id-ne"/>
    <field name="createdStamp" type="date-time"/>
    <field name="createdByUser" type="id-ne"/>
    
    <!-- Relationships -->
    <relationship type="one" related="[PARENT_ENTITY]" short-alias="parent"/>
    <relationship type="one" title="Status" related="moqui.basic.StatusItem"/>
    <relationship type="many" related="[CHILD_ENTITY]" short-alias="children">
        <key-map field-name="[ENTITY_ID]" related-field-name="[PARENT_ID]"/>
    </relationship>
    
    <!-- Indexes for performance -->
    <index name="[ENTITY_NAME]StatusIdx">
        <index-field name="statusId"/>
    </index>
</entity>
```

## Entity View Template

```xml
<view-entity entity-name="[ENTITY_NAME]DetailView" package="[PACKAGE_NAME]">
    <!-- Main entity -->
    <member-entity entity-alias="MAIN" entity-name="[ENTITY_NAME]"/>
    
    <!-- Related entities -->
    <member-entity entity-alias="PARENT" entity-name="[PARENT_ENTITY]" join-from-alias="MAIN">
        <key-map field-name="[PARENT_ID]" related-field-name="[PARENT_ID]"/>
    </member-entity>
    
    <member-entity entity-alias="STATUS" entity-name="moqui.basic.StatusItem" join-from-alias="MAIN">
        <key-map field-name="statusId"/>
    </member-entity>
    
    <!-- Select fields -->
    <alias-all entity-alias="MAIN"/>
    <alias entity-alias="PARENT" name="parentName" field="name"/>
    <alias entity-alias="STATUS" name="statusDescription" field="description"/>
    
    <!-- Calculated fields -->
    <alias name="isActive" entity-alias="MAIN" function="case">
        <complex-alias operator="case">
            <case-when operator="equals" compare-field-name="statusId" compare-value="ACTIVE" return-value="Y"/>
            <case-else return-value="N"/>
        </complex-alias>
    </alias>
</view-entity>
```

## Entity Extension Template

```xml
<extend-entity entity-name="[EXISTING_ENTITY]" package="[PACKAGE_NAME]">
    <!-- Add new fields -->
    <field name="[NEW_FIELD]" type="text-medium"/>
    <field name="[CUSTOM_CONFIG]" type="text-short"/>
    
    <!-- Add new relationships -->
    <relationship type="one" related="[NEW_RELATED_ENTITY]"/>
</extend-entity>
```

## Placeholders Reference

- `[ENTITY_NAME]` - PascalCase entity name (e.g., CustomerOrder, ProductCategory)
- `[PACKAGE_NAME]` - Lowercase package path (e.g., mantle.order, custom.product)
- `[ENTITY_ID]` - Primary key field name (e.g., orderId, productId)
- `[PARENT_ID]` - Foreign key field name (e.g., customerId, categoryId)
- `[PARENT_ENTITY]` - Related parent entity name
- `[CHILD_ENTITY]` - Related child entity name
- `[NEW_FIELD]` - Additional field name for extensions
- `[CUSTOM_CONFIG]` - Configuration field name
- `[NEW_RELATED_ENTITY]` - New relationship target entity