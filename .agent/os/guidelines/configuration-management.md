# Configuration Definition Management Guidelines

## Overview

This document establishes best practices for managing ALL configuration definitions (permissions, enumerations, settings, roles, groups, etc.) to prevent codebase bloat with unused configurations and maintain clean, maintainable systems.

**Scope**: These guidelines apply to ALL configuration entities including:
- **Security**: UserPermission, UserGroup, UserGroupPermission
- **Enumerations**: All Enumeration and EnumerationType definitions
- **Settings**: PartySettingType (database), System Properties (JVM/environment)
- **Roles & Types**: RoleType, PartyType, EntityType definitions
- **Workflows**: StatusType, StatusItem, StatusFlowTransition
- **System Config**: EmailTemplate, NotificationTopic, ServiceJob
- **Custom Entities**: Any configuration-oriented entity definitions

## Configuration Types in Moqui

### System Properties (Infrastructure Configuration)
**Access Method**: `org.moqui.util.SystemBinding.getPropOrEnv('property.name')`
**Storage**: JVM system properties or OS environment variables
**Use Cases**:
- Database connection URLs (vary by environment)
- External API endpoints (different per environment)
- Credentials and API keys (secure secrets)
- Feature flags for deployment control
- Infrastructure-level settings (ports, paths, etc.)
**NOT tenant-aware**: Same value for all tenants in the deployment

**Example in Service**:
```xml
<!-- Get database URL from system property or environment variable -->
<set field="dbUrl" from="org.moqui.util.SystemBinding.getPropOrEnv('database_url')"/>
<set field="apiKey" from="org.moqui.util.SystemBinding.getPropOrEnv('external_api_key')"/>
```

**Setting System Properties**:
```bash
# Via JVM argument
java -Ddatabase_url=jdbc:postgresql://localhost:5432/mydb ...

# Via environment variable
export database_url=jdbc:postgresql://localhost:5432/mydb
```

### default-property (MoquiConf.xml)

The `default-property` element in MoquiConf.xml defines default values that can be overridden by environment variables or JVM properties.

**Precedence Order** (highest to lowest):
1. **JVM System Property** (`-Dproperty_name=value`)
2. **Environment Variable** (`export PROPERTY_NAME=value`)
3. **default-property value** in MoquiConf.xml

**Key Behavior**:
- Default properties are **only set if no JVM property or environment variable exists** with the same name
- Environment variables are copied to JVM System properties at startup
- Values support `${other_property}` syntax for variable expansion

**Example in MoquiConf.xml**:
```xml
<!-- Default to development value, override with env var for production -->
<default-property name="EXTERNAL_API_ENDPOINT" value="https://sandbox.api.example.com"/>
<default-property name="DATABASE_URL" value="jdbc:h2:./runtime/db/h2/moqui"/>
```

**Production Deployment**:
```bash
# Override defaults with environment variables
export EXTERNAL_API_ENDPOINT="https://api.example.com"
export DATABASE_URL="jdbc:postgresql://prod-db:5432/mydb"
```

**Name Matching**:
- At startup, environment variable matching is **exact name only**
- When using `SystemBinding.getPropOrEnv()`, additional underscore→dot conversion is attempted
  - e.g., `getPropOrEnv('my_property')` also checks `my.property`

**Use Cases for default-property**:
- Providing development/test defaults that can be overridden in production
- Defining fallback values for optional configuration
- Centralizing default infrastructure settings
- Supporting different values per deployment environment without code changes

**Example Pattern - Storage Location Configuration**:
```xml
<!-- MoquiConf.xml: Default to database storage for development -->
<default-property name="DOCUMENT_STORAGE_LOCATION" value="dbresource://documents"/>

<!-- Production: Override via environment variable for S3 storage -->
<!-- export DOCUMENT_STORAGE_LOCATION="aws3://production-bucket/documents" -->
```

**Accessing in Services**:
```xml
<!-- Use SystemBinding.getPropOrEnv() to access the resolved value -->
<set field="storageLocation" from="org.moqui.util.SystemBinding.getPropOrEnv('DOCUMENT_STORAGE_LOCATION')"/>

<!-- Or use ec.resource.expand() if the value contains ${} references -->
<set field="expandedLocation" from="ec.resource.expand(storageLocation, '')"/>
```

**IMPORTANT**: The `default-property` mechanism is for infrastructure configuration that varies by deployment. For tenant-aware or user-specific configuration, use `PartySettingType` instead.

### PartySettingType (Application Configuration)
**Entity**: `mantle.party.PartySettingType`
**Access Method**: `ec.user.getPreference('SettingTypeId')`
**Storage**: Database (mantle.party.PartySettingType and mantle.party.PartySetting)
**Use Cases**:
- Business logic configuration
- UI preferences and customization
- Tenant-specific settings
- Workflow rules and thresholds
- Application-level settings
**Tenant-aware**: Different values per tenant/organization/user

**Example in Service**:
```xml
<!-- Get tenant-aware setting with fallback -->
<set field="maxPending" from="ec.user.getPreference('MaxPendingOrders') as Integer"/>
<if condition="!maxPending">
    <entity-find-one entity-name="mantle.party.PartySettingType" value-field="settingType">
        <field-map field-name="partySettingTypeId" value="MaxPendingOrders"/>
    </entity-find-one>
    <set field="maxPending" from="settingType?.defaultValue as Integer"/>
</if>
```

**CRITICAL WARNING**: The entity `moqui.entity.SystemProperty` or `moqui.system.SystemProperty` does NOT exist in Moqui Framework. If you need:
- **Infrastructure config** → Use `org.moqui.util.SystemBinding.getPropOrEnv()`
- **Application config** → Use `mantle.party.PartySettingType`

## Decision Guide: System Properties vs PartySettingType

**Use System Properties (SystemBinding.getPropOrEnv) when**:
- Setting varies by environment (dev, staging, production)
- Contains sensitive data (credentials, API keys)
- Infrastructure-level configuration (DB URLs, service endpoints)
- Set at deployment time and rarely changes
- Same value for all tenants in the deployment
- Needs to be configured without database access

**Use PartySettingType (database) when**:
- Setting should be tenant-specific
- Users/admins should be able to change via UI
- Business logic or workflow configuration
- Settings that vary by organization/department
- Requires hierarchical overrides (user → org → tenant → default)
- Application-level configuration

**Example Scenarios**:
- Database URL → **System Property** (environment-specific)
- Max pending orders per customer → **PartySettingType** (business rule, tenant-specific)
- SII API key → **System Property** (credential, environment-specific)
- Default currency → **PartySettingType** (tenant preference)
- Feature flag for new UI → **System Property** (deployment decision)
- User's theme preference → **PartySettingType** (user-specific)

## PartySettingType Usage Patterns

### Reading Settings in Services

Moqui provides two ways to read PartySettingType values:

**1. `ec.user.getPreference()`** — reads for the current user's party:
```xml
<set field="value" from="ec.user.getPreference('MySettingTypeId')"/>
```

**2. `mantle.party.PartyServices.get#PartySettingValue`** — reads for any party with hierarchical resolution (walks up the organization hierarchy to find the setting):
```xml
<service-call name="mantle.party.PartyServices.get#PartySettingValue"
    in-map="[partyId:organizationPartyId, partySettingTypeId:'MySettingTypeId']"
    out-map="settingMap"/>
<set field="effectiveValue" from="settingMap.settingValue ?: 'defaultFallback'"/>
```

Use `get#PartySettingValue` when reading settings for an organization party that may not be the current user, especially in batch processing or business rule services.

### Store/Delete Lifecycle Pattern

When building admin screens that manage PartySettingType values, use the **store/delete** pattern:

- **Store** when the user sets a value → creates or updates the `PartySetting` record
- **Delete** when the user clears the value → removes the `PartySetting` record so `get#PartySettingValue` falls back to the `defaultValue` defined in `PartySettingType`

```xml
<transition name="updateMySetting">
    <actions>
        <if condition="mySettingValue"><then>
            <service-call name="store#mantle.party.PartySetting"
                in-map="[partyId:partyId, partySettingTypeId:'MySettingTypeId', settingValue:mySettingValue]"/>
        </then><else>
            <service-call name="delete#mantle.party.PartySetting"
                in-map="[partyId:partyId, partySettingTypeId:'MySettingTypeId']"/>
        </else></if>
    </actions>
    <default-response url="."/>
</transition>
```

This pattern ensures that:
- Explicit values are stored as `PartySetting` records
- Clearing a value removes the record entirely, allowing the `PartySettingType.defaultValue` to take effect
- The hierarchical resolution in `get#PartySettingValue` works correctly (child orgs inherit parent settings unless overridden)

### Mid-Service Business Logic Branching

PartySettingType values can drive conditional behavior within service implementations. Read the setting mid-service and branch accordingly:

```xml
<service-call name="mantle.party.PartyServices.get#PartySettingValue"
    in-map="[partyId:organizationPartyId, partySettingTypeId:'MyBehaviorSetting']"
    out-map="behaviorMap"/>
<if condition="behaviorMap.settingValue == 'optionA'"><then>
    <!-- Behavior A -->
</then><else>
    <!-- Default behavior -->
</else></if>
```

**When to use this pattern:**
- The service behavior needs to vary per organization
- The choice between behaviors should be configurable by admins at runtime
- Both behaviors are valid — neither is a bug, just a policy decision

**Design considerations:**
- Always define a sensible `defaultValue` on the `PartySettingType` so the service works without explicit configuration
- Keep the number of options small (2-3) to avoid complexity
- Document the risk/tradeoff of each option in the admin screen (e.g., via `<label style="text-warning"/>`)

## Core Principles

### 1. Just-In-Time Configuration Creation
- **Create configurations ONLY when implementing the actual functionality**
- **Never create configurations for future or planned features**
- **Remove configurations immediately when functionality is removed**

### 2. Implementation-First Approach
- ✅ **DO**: Implement feature → Add configuration usage → Create configuration
- ❌ **DON'T**: Create configuration → Plan to implement later

### 3. Documentation vs Implementation
- **Specifications and ideas**: Document in design documents, not in code
- **Configuration requirements**: Keep in `.agent-os/` documentation until implementation
- **Future features**: Maintain in roadmap documents, not configuration definitions

### 4. Universal Usage Requirement
**Every configuration definition MUST have active usage:**
- **Permissions**: Service checks with `hasPermission()` or screen access control
- **Enumerations**: Entity field references or service logic usage
- **Settings (PartySettingType)**: Service calls to `ec.user.getPreference('SettingTypeId')`
- **Settings (System Properties)**: Service calls to `org.moqui.util.SystemBinding.getPropOrEnv('property_name')`
- **Roles**: PartyRole assignments or role-based logic
- **Status Types**: Entity status fields or workflow transitions
- **Templates**: Active email/notification sending processes

## Configuration Lifecycle

### Stage 1: Planning (Documentation Only)
```markdown
<!-- In design documents, NOT in XML -->
## Planned Configurations
- USER_PROFILE_ADMIN: Permission for full user profile management
- USER_NOTIFICATION_PREF: Setting type for notification preferences
- PROFILE_STATUS: Status enumeration for user profile states
- NOTIFICATION_TEMPLATE: Email template for profile updates
```

### Stage 2: Implementation
```xml
<!-- Only create when implementing the actual feature -->

<!-- Permission: Create AND use immediately -->
<moqui.security.UserPermission
    userPermissionId="USER_PROFILE_ADMIN"
    description="Full user profile management"/>

<service verb="update" noun="UserProfile">
    <actions>
        <if condition="!ec.user.hasPermission('USER_PROFILE_ADMIN')">
            <return error="true" message="Permission denied"/>
        </if>
    </actions>
</service>

<!-- Setting Type: Create AND use immediately -->
<mantle.party.PartySettingType
    partySettingTypeId="USER_NOTIFICATION_PREF"
    description="User notification preferences"/>

<service verb="get" noun="UserNotificationSettings">
    <actions>
        <set field="notifyPref" from="ec.user.getPreference('USER_NOTIFICATION_PREF')"/>
    </actions>
</service>

<!-- Enumeration: Create AND use immediately -->
<moqui.basic.Enumeration enumTypeId="ProfileStatus" enumId="PROF_ACTIVE"
                         description="Active Profile"/>

<entity entity-name="UserProfile">
    <field name="statusId" type="id"/>
    <!-- Used in status field -->
</entity>
```

### Stage 3: Usage Verification
- **Every configuration MUST have at least one active usage**
- **Usage types**: Service logic, entity references, screen controls, workflow processes
- **Audit regularly**: Remove configurations without active usage

### Stage 4: Removal
```xml
<!-- When feature is removed, immediately remove ALL related configurations -->
<!-- No orphaned configurations allowed -->
```

## Best Practices

### Naming Conventions
- **Use descriptive, purpose-oriented names**: `USER_LOGIN_KEY_ADMIN` not `ULK_ADMIN`
- **Follow consistent patterns**:
  - Permissions: `ENTITY_ACTION` format
  - Settings: `FUNCTIONAL_AREA_SETTING_NAME`
  - Enumerations: `CLEAR_CONTEXT_VALUE`
- **Include scope and purpose**: `ADMIN`, `VIEW`, `CREATE`, `UPDATE`, `DELETE`

### Granularity Guidelines
- **Start with broader configurations**: `USER_ADMIN` before `USER_CREATE`, `USER_UPDATE`
- **Split when needed**: Only create granular configurations when different contexts need different behavior
- **Avoid over-engineering**: Don't create configurations for every possible variation

### Assignment and Relationships
```xml
<!-- Only create relationships when actively used -->
<moqui.security.UserGroupPermission
    userGroupId="ADMIN"
    userPermissionId="USER_LOGIN_KEY_ADMIN"
    fromDate="0"/>

<mantle.party.PartySettingType
    partySettingTypeId="API_RATE_LIMIT"
    description="API rate limiting threshold per hour"/>
```

### Documentation Requirements
```xml
<!-- Every configuration must have clear, specific description -->
<moqui.security.UserPermission
    userPermissionId="USER_LOGIN_KEY_ADMIN"
    description="Full administration of UserLoginKey entities including creation, revocation, and updates"/>

<moqui.basic.Enumeration enumTypeId="ApiKeyStatus" enumId="AKS_ACTIVE"
                         description="API key is active and can be used for authentication"/>

<mantle.party.PartySettingType
    partySettingTypeId="EMAIL_NOTIFICATION_FREQ"
    description="Frequency for sending email notifications: IMMEDIATE, DAILY, WEEKLY"/>
```

## Code Review Checklist

### For New Configurations
- [ ] Is this configuration actively used in the same PR/commit?
- [ ] Is there specific code (service/entity/screen) that references this configuration?
- [ ] Could this be handled by an existing configuration?
- [ ] Is the description clear and specific?
- [ ] Is the naming consistent with existing patterns?
- [ ] Are all related configurations (groups, relationships) justified?

### For Configuration Removal
- [ ] Verified no services reference this configuration?
- [ ] Verified no entities use this enumeration/setting?
- [ ] Verified no screens or other components reference this configuration?
- [ ] Removed all associated relationships (UserGroupPermission, etc.)?
- [ ] Verified no data files reference this configuration?

### For Configuration Modifications
- [ ] All existing usages still valid with new description/name?
- [ ] Backward compatibility maintained where required?
- [ ] Documentation updated to reflect changes?
- [ ] No breaking changes to dependent configurations?

## Common Anti-Patterns

### ❌ Future-Proofing
```xml
<!-- DON'T: Creating configurations for planned features -->
<moqui.security.UserPermission userPermissionId="AI_MODEL_ADMIN"
    description="For future AI integration"/>
<mantle.party.PartySettingType partySettingTypeId="BLOCKCHAIN_CONFIG"
    description="When we add blockchain features"/>
<moqui.basic.Enumeration enumTypeId="FutureFeature" enumId="FF_COMING_SOON"
    description="Placeholder for future functionality"/>
```

### ❌ Over-Granularization
```xml
<!-- DON'T: Too many granular configurations without clear need -->
<moqui.security.UserPermission userPermissionId="CUSTOMER_VIEW_FIRST_NAME"/>
<moqui.security.UserPermission userPermissionId="CUSTOMER_VIEW_LAST_NAME"/>
<mantle.party.PartySettingType partySettingTypeId="CUSTOMER_FIRST_NAME_DISPLAY"/>
<mantle.party.PartySettingType partySettingTypeId="CUSTOMER_LAST_NAME_DISPLAY"/>
<!-- Better: CUSTOMER_VIEW permission and CUSTOMER_DISPLAY_PREFS setting -->
```

### ❌ Orphaned Configurations
```xml
<!-- DON'T: Configurations without any usage -->
<moqui.security.UserPermission userPermissionId="LEGACY_FEATURE_ADMIN"/>
<mantle.party.PartySettingType partySettingTypeId="ULK_AUTO_CREATE_AGENT"/>
<moqui.basic.Enumeration enumTypeId="UnusedType" enumId="UT_ORPHANED"/>
<!-- If no code references these, remove immediately -->
```

### ❌ Unclear Descriptions
```xml
<!-- DON'T: Vague descriptions -->
<moqui.security.UserPermission userPermissionId="ADMIN_STUFF"
    description="Admin things"/>
<mantle.party.PartySettingType partySettingTypeId="USER_CONFIG"
    description="User configuration"/>

<!-- DO: Specific descriptions -->
<moqui.security.UserPermission userPermissionId="USER_LOGIN_KEY_ADMIN"
    description="Create, revoke, and update UserLoginKey entities for API access"/>
<mantle.party.PartySettingType partySettingTypeId="EMAIL_NOTIFICATION_FREQ"
    description="Frequency for sending email notifications: IMMEDIATE, DAILY, WEEKLY"/>
```

## Audit Process

### Regular Configuration Audits (Quarterly)
1. **Scan all configuration definitions** in data files (permissions, enumerations, settings, etc.)
2. **Search for usage** of each configuration in services, entities, screens, and workflows
3. **Document findings** and create removal plan for unused configurations
4. **Review configuration descriptions** for clarity and accuracy
5. **Verify relationships** are still valid and necessary

### Automated Checks
```bash
# Example audit script concepts

# Check permissions
for permission in $(grep "userPermissionId=" *.xml); do
    usage_count=$(grep -r "$permission" services/ screens/ | wc -l)
    if [ $usage_count -eq 0 ]; then
        echo "UNUSED PERMISSION: $permission"
    fi
done

# Check settings
for setting in $(grep "partySettingTypeId=" *.xml); do
    usage_count=$(grep -r "getPreference.*$setting" services/ | wc -l)
    if [ $usage_count -eq 0 ]; then
        echo "UNUSED SETTING: $setting"
    fi
done

# Check enumerations
for enum in $(grep "enumId=" *.xml); do
    usage_count=$(grep -r "$enum" entity/ services/ | wc -l)
    if [ $usage_count -eq 0 ]; then
        echo "UNUSED ENUMERATION: $enum"
    fi
done
```

## Migration Strategy

### For Existing Unused Configurations
1. **Immediate removal** if no functionality depends on them
2. **Grace period** only if removal might break external integrations
3. **Clear timeline** for removal with stakeholder communication
4. **Document removal rationale** for future reference

### For New Development
1. **Start implementation** before creating configurations
2. **Add configuration usage** as part of the implementation
3. **Create configuration definition** only when usage is in place
4. **Test configuration behavior** before finalizing

## Integration with Agent OS

### Agent Guidelines
- **Agents should verify configuration usage** before creating new definitions
- **Agents should suggest configuration removal** when functionality is removed
- **Agents should follow naming conventions** and documentation standards
- **Agents should check for existing configurations** that might serve the same purpose
- **Agents should enforce just-in-time creation** for all configuration types

### Documentation Storage
- **Planned configurations**: Store in `.agent-os/planned-features.md`
- **Configuration requirements**: Include in feature specifications
- **Implementation notes**: Document in relevant agent reference files

## Configuration Types Covered

### Core System Configurations
- **Security**: UserPermission, UserGroup, UserGroupPermission
- **Enumerations**: Enumeration, EnumerationType
- **Settings**: PartySettingType (NOTE: SystemProperty does NOT exist in Moqui Framework)
- **Roles**: RoleType, PartyRole
- **Status Management**: StatusType, StatusItem, StatusFlowTransition

### Application Configurations
- **Communication**: EmailTemplate, NotificationTopic
- **Workflow**: ServiceJob, ScheduledJob
- **Content**: ContentType, DataDocumentType
- **Entity Extensions**: Custom entity definitions for configuration

This framework ensures that ALL configuration elements remain aligned with actual functionality and prevents accumulation of unused system artifacts across the entire codebase.