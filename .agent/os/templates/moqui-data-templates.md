# Moqui Data Templates

## Data File Organization Guidelines

**CRITICAL RULE**: Do not create separate data files for single features or small additions.

### When to Create New Data Files
- **Sub-component**: When implementing a distinct sub-component with multiple related features
- **Multiple features**: When implementing a related group of features that justify their own file
- **Large datasets**: When implementing substantial data sets that would overwhelm existing files

### When to Use Existing Data Files
- **Single features**: CAF Auto Request, notifications, single service configurations
- **Related functionality**: Add to domain-specific files (e.g., DTE features → ACADteData.xml)
- **Small additions**: Enumerations, system properties, single ServiceJobs

### Existing Data File Purposes
- `AAAInstallData.xml` - Core installation data
- `ACADteData.xml` - DTE-related functionality (ServiceJobs, enumerations, settings)
- `ScreenSetup.xml` - Screen and UI authorization
- `DemoData.xml` - Demo and test data

**ALWAYS check existing files first before creating new ones.**

## Seed Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- [DATA_PURPOSE]: Essential system configuration -->
    
    <!-- Status and Type Definitions -->
    <StatusType statusTypeId="[STATUS_TYPE_ID]" description="[STATUS_TYPE_DESCRIPTION]"/>
    <StatusItem statusId="[STATUS_ID]" statusTypeId="[STATUS_TYPE_ID]" 
                sequenceNum="[SEQUENCE]" description="[STATUS_DESCRIPTION]"/>
    
    <!-- Configuration Settings - Use PartySettingType for party-specific configuration -->
    <mantle.party.PartySettingType partySettingTypeId="[SETTING_TYPE_ID]" 
                                   description="[SETTING_DESCRIPTION]" 
                                   defaultValue="[DEFAULT_VALUE]"/>
    
    <!-- Example party-specific setting -->
    <mantle.party.PartySetting partyId="[PARTY_ID]" 
                               partySettingTypeId="[SETTING_TYPE_ID]" 
                               settingValue="[SETTING_VALUE]"/>
    
    <!-- Reference Data -->
    <Enumeration enumId="[ENUM_ID]" enumTypeId="[ENUM_TYPE_ID]" 
                description="[ENUM_DESCRIPTION]" sequenceNum="[SEQUENCE]"/>
    
    <!-- Entity Type Definitions -->
    <EntityType entityTypeId="[ENTITY_TYPE_ID]" description="[ENTITY_TYPE_DESCRIPTION]"/>
    
    <!-- Security Permissions -->
    <UserPermission userPermissionId="[PERMISSION_ID]" description="[PERMISSION_DESCRIPTION]"/>
    
    <!-- User Groups (check existing groups first!) -->
    <!-- grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml -->
    <UserGroup userGroupId="[USER_GROUP_ID]" description="[GROUP_DESCRIPTION]" groupTypeEnumId="[GROUP_TYPE]"/>
    
    <!-- Screen Authorization (Main Artifact Pattern) -->
    <ArtifactGroup artifactGroupId="[SCREEN_GROUP_ID]" description="[SCREEN_GROUP_DESCRIPTION]"/>
    <ArtifactGroupMember artifactGroupId="[SCREEN_GROUP_ID]" 
                         artifactName="component://[COMPONENT]/screen/[SCREEN_PATH].xml" 
                         artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
    
    <ArtifactAuthz artifactAuthzId="[AUTHZ_ID]" 
                   userGroupId="[EXISTING_USER_GROUP]" 
                   artifactGroupId="[SCREEN_GROUP_ID]" 
                   authzTypeEnumId="AUTHZT_ALLOW" 
                   authzActionEnumId="AUTHZA_ALL"/>
    
    <!-- Entity Filters (when needed for multi-tenancy) -->
    <ArtifactAuthzFilter artifactAuthzId="[AUTHZ_ID]" 
                         entityFilterSetId="[FILTER_SET]" 
                         applyCond="[FILTER_CONDITION]"/>
    
    <!-- Audit Log Configuration -->
    <ArtifactAuditLog artifactName="[ARTIFACT_NAME]" 
                      artifactTypeEnumId="[ARTIFACT_TYPE]"
                      logLevel="[LOG_LEVEL]"/>
</entity-facade-xml>
```

## Demo Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="demo">
    <!-- [SCENARIO_PURPOSE]: Realistic business scenario demonstration -->
    
    <!-- Primary Business Entity -->
    <[ENTITY_NAME] [PRIMARY_KEY]="[DEMO_ID]" 
                   [FIELD_NAME]="[DEMO_VALUE]"
                   [TIMESTAMP_FIELD]="[CURRENT_TIMESTAMP]"
                   [STATUS_FIELD]="[ACTIVE_STATUS]"/>
    
    <!-- Related Business Data -->
    <[RELATED_ENTITY] [PRIMARY_KEY]="[RELATED_ID]" 
                      [FOREIGN_KEY]="[DEMO_ID]"
                      [FIELD_NAME]="[RELATED_VALUE]"
                      [DESCRIPTION_FIELD]="[DESCRIPTION]"/>
    
    <!-- Business Relationship Data -->
    <[RELATIONSHIP_ENTITY] [KEY_1]="[DEMO_ID]" 
                           [KEY_2]="[RELATED_ID]"
                           [STATUS_FIELD]="[ACTIVE_STATUS]"
                           fromDate="[FROM_DATE]"/>
    
    <!-- Additional Demo Context -->
    <Party partyId="[PARTY_ID]" partyTypeId="[PARTY_TYPE]"/>
    <Person partyId="[PARTY_ID]" firstName="[FIRST_NAME]" lastName="[LAST_NAME]"/>
    <PartyRole partyId="[PARTY_ID]" roleTypeId="[ROLE_TYPE]"/>
    
    <!-- Demo Product/Service Data -->
    <Product productId="[PRODUCT_ID]" productName="[PRODUCT_NAME]" 
             productTypeEnumId="[PRODUCT_TYPE]" statusId="[PRODUCT_STATUS]"/>
    
    <!-- Demo Order Data -->
    <OrderHeader orderId="[ORDER_ID]" orderTypeEnumId="[ORDER_TYPE]" 
                 statusId="[ORDER_STATUS]" entryDate="[ENTRY_DATE]"
                 customerPartyId="[CUSTOMER_ID]"/>
    
    <OrderItem orderId="[ORDER_ID]" orderItemSeqId="[ITEM_SEQ]" 
               productId="[PRODUCT_ID]" quantity="[QUANTITY]" 
               unitAmount="[UNIT_PRICE]"/>
</entity-facade-xml>
```

## Configuration Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- [CONFIGURATION_PURPOSE]: System operational settings -->
    
    <!-- Configuration using PartySetting for party-specific values -->
    <mantle.party.PartySettingType partySettingTypeId="[CONFIG_KEY]" 
                                   description="[CONFIG_DESCRIPTION]" 
                                   defaultValue="[DEFAULT_VALUE]"/>
    
    <!-- Optional: Party-specific configuration override -->
    <mantle.party.PartySetting partyId="[PARTY_ID]" 
                               partySettingTypeId="[CONFIG_KEY]" 
                               settingValue="[PARTY_SPECIFIC_VALUE]"/>
    
    <!-- Feature Configuration -->
    <[CONFIG_ENTITY] [CONFIG_ID]="[FEATURE_ID]"
                     [ENABLED_FIELD]="[TRUE/FALSE]"
                     [CONFIG_VALUE_FIELD]="[CONFIG_VALUE]"
                     description="[FEATURE_DESCRIPTION]"/>
    
    <!-- Environment-Specific Settings -->
    <[ENV_CONFIG_ENTITY] environmentId="[ENV_ID]"
                         [CONFIG_KEY]="[CONFIG_VALUE]"
                         [OVERRIDE_FIELD]="[Y/N]"/>
    
    <!-- Database Configuration -->
    <DatabaseConf databaseConfName="[DB_CONFIG_NAME]"
                  jdbcDriver="[JDBC_DRIVER]"
                  jdbcUri="[JDBC_URI]"
                  jdbcUsername="[DB_USERNAME]"/>
    
    <!-- Email Configuration -->
    <EmailTemplate emailTemplateId="[TEMPLATE_ID]"
                   description="[TEMPLATE_DESCRIPTION]"
                   subject="[EMAIL_SUBJECT]"
                   bodyText="[EMAIL_BODY]"/>
    
    <!-- Workflow Configuration -->
    <WorkflowType workflowTypeId="[WORKFLOW_TYPE]" 
                  description="[WORKFLOW_DESCRIPTION]"/>
    
    <!-- Integration Configuration -->
    <SystemMessage systemMessageId="[MESSAGE_ID]"
                   description="[MESSAGE_DESCRIPTION]"
                   systemMessageTypeId="[MESSAGE_TYPE]"/>
</entity-facade-xml>
```

## Test Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="test">
    <!-- [TEST_PURPOSE]: Test scenario specific data -->
    
    <!-- Test Users -->
    <UserAccount userId="[TEST_USER_ID]" username="[TEST_USERNAME]" 
                 userFullName="[TEST_FULL_NAME]" currentPassword="[TEST_PASSWORD]"
                 passwordBase64="[BASE64_PASSWORD]" requirePasswordChange="N"/>
    
    <UserGroupMember userId="[TEST_USER_ID]" userGroupId="[TEST_GROUP]"
                     fromDate="[FROM_DATE]"/>
    
    <!-- Test Entities -->
    <[TEST_ENTITY] [PRIMARY_KEY]="[TEST_ID]" 
                   [FIELD_NAME]="[TEST_VALUE]"
                   [STATUS_FIELD]="[TEST_STATUS]"
                   createdStamp="[TEST_TIMESTAMP]"/>
    
    <!-- Test Relationships -->
    <[TEST_RELATIONSHIP] [KEY_1]="[TEST_ID_1]" 
                         [KEY_2]="[TEST_ID_2]"
                         relationshipTypeEnumId="[RELATIONSHIP_TYPE]"/>
    
    <!-- Test Configuration Override -->
    <mantle.party.PartySetting partyId="[TEST_PARTY_ID]" 
                               partySettingTypeId="[TEST_CONFIG_KEY]" 
                               settingValue="[TEST_CONFIG_VALUE]"/>
    
    <!-- Test Data Sets -->
    <DataSet dataSetId="[TEST_DATASET_ID]" 
             dataSetName="[TEST_DATASET_NAME]"
             description="[TEST_DATASET_DESCRIPTION]"/>
    
    <!-- Mock External Data -->
    <[MOCK_ENTITY] [MOCK_ID]="[MOCK_VALUE]"
                   [EXTERNAL_REFERENCE]="[MOCK_EXTERNAL_ID]"
                   [MOCK_RESPONSE]="[MOCK_RESPONSE_DATA]"/>
</entity-facade-xml>
```

## Migration Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed-initial">
    <!-- [MIGRATION_PURPOSE]: Version-specific migration data -->
    
    <!-- Schema Version Tracking -->
    <SchemaVersion componentName="[COMPONENT_NAME]" 
                   versionName="[VERSION_NUMBER]"
                   installedDate="[INSTALL_DATE]"/>
    
    <!-- Data Migration Records -->
    <DataMigration migrationId="[MIGRATION_ID]"
                   migrationName="[MIGRATION_NAME]" 
                   fromVersion="[FROM_VERSION]"
                   toVersion="[TO_VERSION]"
                   migrationDate="[MIGRATION_DATE]"/>
    
    <!-- Legacy Data Mapping -->
    <LegacyMapping legacyId="[LEGACY_ID]"
                   legacyType="[LEGACY_TYPE]"
                   newId="[NEW_ID]"
                   newType="[NEW_TYPE]"
                   mappingDate="[MAPPING_DATE]"/>
    
    <!-- Migration Configuration -->
    <MigrationConfig configKey="[MIGRATION_CONFIG_KEY]"
                     configValue="[MIGRATION_CONFIG_VALUE]"
                     description="[MIGRATION_DESCRIPTION]"/>
    
    <!-- Data Cleanup Records -->
    <CleanupLog cleanupId="[CLEANUP_ID]"
                cleanupType="[CLEANUP_TYPE]"
                recordsAffected="[RECORD_COUNT]"
                cleanupDate="[CLEANUP_DATE]"/>
    
    <!-- Rollback Information -->
    <RollbackData rollbackId="[ROLLBACK_ID]"
                  migrationId="[MIGRATION_ID]"
                  rollbackScript="[ROLLBACK_SCRIPT]"
                  rollbackData="[ROLLBACK_DATA]"/>
</entity-facade-xml>
```

## Install Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed-initial">
    <!-- [INSTALL_PURPOSE]: Initial installation setup data -->
    
    <!-- Component Registration -->
    <ComponentInfo componentName="[COMPONENT_NAME]"
                   componentVersion="[COMPONENT_VERSION]"
                   description="[COMPONENT_DESCRIPTION]"
                   installDate="[INSTALL_DATE]"/>
    
    <!-- Initial Admin User -->
    <UserAccount userId="[ADMIN_USER_ID]" username="[ADMIN_USERNAME]"
                 userFullName="[ADMIN_FULL_NAME]" 
                 currentPassword="[ADMIN_PASSWORD]"
                 requirePasswordChange="Y"/>
    
    <UserGroupMember userId="[ADMIN_USER_ID]" userGroupId="ADMIN"
                     fromDate="[INSTALL_DATE]"/>
    
    <!-- Basic Organization Setup -->
    <Party partyId="[ORG_PARTY_ID]" partyTypeId="ORGANIZATION"/>
    <Organization partyId="[ORG_PARTY_ID]" 
                  organizationName="[ORGANIZATION_NAME]"/>
    
    <!-- Default Settings -->
    <PartySettingType partySettingTypeId="[SETTING_TYPE]"
                      description="[SETTING_DESCRIPTION]"/>
    
    <PartySetting partyId="[ORG_PARTY_ID]"
                  partySettingTypeId="[SETTING_TYPE]"
                  settingValue="[SETTING_VALUE]"/>
    
    <!-- License and Legal -->
    <Agreement agreementId="[LICENSE_AGREEMENT_ID]"
               agreementTypeEnumId="LICENSE"
               description="[LICENSE_DESCRIPTION]"
               fromDate="[INSTALL_DATE]"/>
    
    <!-- Initial Data Quality Rules -->
    <DataQualityRule ruleId="[QUALITY_RULE_ID]"
                     ruleName="[QUALITY_RULE_NAME]"
                     ruleExpression="[QUALITY_EXPRESSION]"
                     isActive="Y"/>
</entity-facade-xml>
```

## Bulk Data Loading Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="[DATA_TYPE]">
    <!-- [BULK_PURPOSE]: Efficient bulk data loading -->
    
    <!-- Batch Processing Configuration -->
    <BatchJob batchJobId="[BATCH_JOB_ID]"
              jobName="[BATCH_JOB_NAME]"
              description="[BATCH_DESCRIPTION]"
              batchSize="[BATCH_SIZE]"/>
    
    <!-- Bulk Entity Records -->
    <entity-find entity-name="[SOURCE_ENTITY]" list="[SOURCE_LIST]" cache="false">
        <select-field field-name="[FIELD_1]"/>
        <select-field field-name="[FIELD_2]"/>
        <order-by field-name="[ORDER_FIELD]"/>
    </entity-find>
    
    <!-- Conditional Data Loading -->
    <iterate list="[SOURCE_LIST]" entry="[SOURCE_ITEM]">
        <if condition="[CONDITION_EXPRESSION]">
            <create value="[TARGET_MAP]"/>
        </if>
    </iterate>
    
    <!-- Performance Optimized Loading -->
    <entity-find entity-name="[ENTITY_NAME]" list="[ENTITY_LIST]" 
                 cache="false" use-clone="false">
        <search-form-inputs skip-fields="[SKIP_FIELDS]"/>
        <select-field field-name="[ESSENTIAL_FIELD_1]"/>
        <select-field field-name="[ESSENTIAL_FIELD_2]"/>
        <order-by field-name="[PERFORMANCE_ORDER_FIELD]"/>
    </entity-find>
    
    <!-- Memory Management for Large Sets -->
    <entity-find entity-name="[LARGE_ENTITY]" list="[LARGE_LIST]" 
                 limit="[BATCH_LIMIT]" offset="[BATCH_OFFSET]">
        <date-filter from-field-name="[FROM_DATE]" thru-field-name="[THRU_DATE]"/>
    </entity-find>
</entity-facade-xml>
```

## Authorization Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- [AUTHORIZATION_PURPOSE]: Screen and service authorization setup -->
    
    <!-- Step 1: Check for existing user groups FIRST -->
    <!-- Command: grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml -->
    
    <!-- Step 2: Use existing groups when possible -->
    <!-- Standard groups: ADMIN, ADMIN_ADV, ALL_USERS -->
    <!-- Component groups: {ProjectAdminUsers}, TENANT_ADMIN -->
    
    <!-- Step 3: Only create new groups if absolutely necessary -->
    <UserGroup userGroupId="[NEW_GROUP_ID]" description="[GROUP_DESCRIPTION]" groupTypeEnumId="UgtMoquiAdmin"/>
    
    <!-- Step 4: Define main artifact group for screens -->
    <ArtifactGroup artifactGroupId="[MAIN_SCREEN_GROUP]" description="[MAIN_FUNCTIONALITY] Screens"/>
    <ArtifactGroupMember artifactGroupId="[MAIN_SCREEN_GROUP]" 
                         artifactName="component://[COMPONENT]/screen/[MAIN_SCREEN].xml" 
                         artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
    
    <!-- Step 5: Grant access to user group -->
    <ArtifactAuthz artifactAuthzId="[MAIN_SCREEN]_ACCESS" 
                   userGroupId="[EXISTING_USER_GROUP]" 
                   artifactGroupId="[MAIN_SCREEN_GROUP]" 
                   authzTypeEnumId="AUTHZT_ALLOW" 
                   authzActionEnumId="AUTHZA_ALL"/>
    
    <!-- Step 6: Add entity filters for multi-tenancy (if needed) -->
    <ArtifactAuthzFilter artifactAuthzId="[MAIN_SCREEN]_ACCESS" 
                         entityFilterSetId="MANTLE_USER_ORG" 
                         applyCond="activeOrgId"/>
    
    <!-- Step 7: Services inherit authorization - NO SPECIFIC SERVICE AUTHORIZATION NEEDED -->
    <!-- ❌ AVOID: Creating ArtifactGroupMembers for individual services -->
    <!-- ❌ AVOID: Creating specific service authorization when screen already covers it -->
    
    <!-- Step 8: ServiceJobs run in system context - NO USER GROUP AUTHORIZATION NEEDED -->
    <ServiceJob jobName="[JOB_NAME]" 
                serviceName="[SERVICE_NAME]" 
                cronExpression="[CRON_EXPRESSION]" 
                paused="N"
                description="[JOB_DESCRIPTION]"/>
    <!-- Note: ServiceJobs inherit authorization through service calls, no ArtifactAuthz needed -->
</entity-facade-xml>
```

## Screen Setup Data Template

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- [SCREEN_SETUP_PURPOSE]: Screen registration and navigation setup -->
    
    <!-- Main screen artifact group -->
    <ArtifactGroup artifactGroupId="[COMPONENT]Admin" description="[COMPONENT] Administration Screens"/>
    <ArtifactGroupMember artifactGroupId="[COMPONENT]Admin" 
                         artifactName="component://[COMPONENT]/screen/[MainScreen].xml" 
                         artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
    
    <!-- User group for component admin access -->
    <UserGroup userGroupId="[COMPONENT]AdminUsers" description="Users with Admin access for [COMPONENT] Screens"/>
    
    <!-- Authorization for component admin users -->
    <ArtifactAuthz artifactAuthzId="[COMPONENT]Admin[COMPONENT]AdminUsers" 
                   artifactGroupId="[COMPONENT]Admin" 
                   userGroupId="[COMPONENT]AdminUsers"
                   authzTypeEnumId="AUTHZT_ALLOW" 
                   authzActionEnumId="AUTHZA_ALL"/>
    <ArtifactAuthzFilter artifactAuthzId="[COMPONENT]Admin[COMPONENT]AdminUsers" 
                         entityFilterSetId="MANTLE_ACTIVE_ORG" 
                         applyCond="activeOrgId"/>
    
    <!-- Authorization for system admins -->
    <ArtifactAuthz artifactAuthzId="[COMPONENT]AdminAdminUsers" 
                   artifactGroupId="[COMPONENT]Admin" 
                   userGroupId="ADMIN"
                   authzTypeEnumId="AUTHZT_ALLOW" 
                   authzActionEnumId="AUTHZA_ALL"/>
    <ArtifactAuthzFilter artifactAuthzId="[COMPONENT]AdminAdminUsers" 
                         entityFilterSetId="MANTLE_USER_ORG"/>
    
    <!-- Screen navigation registration (userGroupId is a PK field — must be explicit; access controlled by ArtifactAuthz) -->
    <SubscreensItem subscreenName="[ComponentScreen]"
                    userGroupId="ALL_USERS"
                    menuIndex="[MENU_INDEX]" menuInclude="Y"
                    menuTitle="[MENU_TITLE]"
                    subscreenLocation="component://[COMPONENT]/screen/[MainScreen].xml"
                    screenLocation="component://webroot/screen/webroot/apps.xml"/>
    
    <!-- Entity filters for component-specific entities -->
    <EntityFilter entityFilterSetId="MANTLE_USER_ORG" 
                  entityFilterId="MANTLE_USER_ORG_[COMPONENT]_1" 
                  entityName="[component].[entity].[EntityName]" 
                  filterMap="[filterField:filterOrgIds]"/>
    <EntityFilter entityFilterSetId="MANTLE_ACTIVE_ORG" 
                  entityFilterId="MANTLE_ACTIVE_ORG_[COMPONENT]_1" 
                  entityName="[component].[entity].[EntityName]" 
                  filterMap="[filterField:activeOrgId]"/>
</entity-facade-xml>
```

## Authorization Anti-Patterns to AVOID

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- ❌ ANTI-PATTERN: Over-engineering service authorization -->
    
    <!-- DON'T: Create specific groups for every service -->
    <ArtifactGroup artifactGroupId="EVERY_SERVICE_GROUP" description="Unnecessary service group"/>
    <ArtifactGroupMember artifactGroupId="EVERY_SERVICE_GROUP" 
                         artifactName="service.name.individual#Service1" 
                         artifactTypeEnumId="AT_SERVICE" inheritAuthz="Y"/>
    <ArtifactGroupMember artifactGroupId="EVERY_SERVICE_GROUP" 
                         artifactName="service.name.individual#Service2" 
                         artifactTypeEnumId="AT_SERVICE" inheritAuthz="Y"/>
    <!-- ... dozens more services ... -->
    
    <!-- DON'T: Reference non-existent user groups -->
    <ArtifactAuthz artifactAuthzId="BROKEN_AUTHZ" 
                   userGroupId="NON_EXISTENT_GROUP" 
                   artifactGroupId="SOME_GROUP" 
                   authzTypeEnumId="AUTHZT_ALWAYS" 
                   authzActionEnumId="AUTHZA_ALL"/>
    
    <!-- DON'T: Create authorization for ServiceJobs -->
    <ArtifactGroup artifactGroupId="SERVICE_JOB_GROUP" description="Unnecessary job authorization"/>
    <ArtifactGroupMember artifactGroupId="SERVICE_JOB_GROUP" 
                         artifactName="scheduled.job.service#JobService" 
                         artifactTypeEnumId="AT_SERVICE" inheritAuthz="Y"/>
    <ArtifactAuthz artifactAuthzId="JOB_AUTHZ" 
                   userGroupId="SOME_GROUP" 
                   artifactGroupId="SERVICE_JOB_GROUP" 
                   authzTypeEnumId="AUTHZT_ALWAYS" 
                   authzActionEnumId="AUTHZA_ALL"/>
    
    <!-- DON'T: Duplicate screen authorization -->
    <ArtifactGroup artifactGroupId="DUPLICATE_SCREEN_GROUP" description="Already covered by main screen"/>
    <ArtifactGroupMember artifactGroupId="DUPLICATE_SCREEN_GROUP" 
                         artifactName="component://Component/screen/SubScreen.xml" 
                         artifactTypeEnumId="AT_XML_SCREEN" inheritAuthz="Y"/>
    <!-- This is unnecessary if SubScreen is already covered by main screen authorization -->
</entity-facade-xml>
```

## Placeholders Reference

- `[DATA_PURPOSE]` - Description of the data file's purpose
- `[STATUS_TYPE_ID]` - Status type identifier
- `[STATUS_ID]` - Individual status identifier  
- `[SEQUENCE]` - Sequence number for ordering
- `[RESOURCE_ID]` - System resource identifier
- `[PROPERTY_ID]` - System property key
- `[PROPERTY_VALUE]` - System property value
- `[ENUM_ID]` - Enumeration identifier
- `[ENUM_TYPE_ID]` - Enumeration type identifier
- `[ENTITY_NAME]` - Target entity name
- `[PRIMARY_KEY]` - Primary key field name
- `[DEMO_ID]` - Demo record identifier
- `[FIELD_NAME]` - Generic field name
- `[DEMO_VALUE]` - Demo field value
- `[CURRENT_TIMESTAMP]` - Current timestamp expression
- `[ACTIVE_STATUS]` - Active status value
- `[COMPONENT_NAME]` - Component identifier
- `[CONFIG_KEY]` - Configuration key
- `[CONFIG_VALUE]` - Configuration value
- `[TEST_USER_ID]` - Test user identifier
- `[TEST_USERNAME]` - Test username
- `[MIGRATION_ID]` - Migration identifier
- `[VERSION_NUMBER]` - Version identifier
- `[BATCH_SIZE]` - Batch processing size
- `[CONDITION_EXPRESSION]` - Conditional loading expression