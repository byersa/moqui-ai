# Configuration Types Quick Reference

## Two Distinct Configuration Mechanisms in Moqui

### System Properties (Infrastructure Configuration)

**Storage**: JVM system properties or OS environment variables (NOT in database)

**Access Method**:
```xml
<set field="value" from="org.moqui.util.SystemBinding.getPropOrEnv('property_name')"/>
```

**Setting Values**:
```bash
# Via environment variable
export property_name=value

# Via JVM argument
java -Dproperty_name=value ...
```

**Use When**:
- Setting varies by environment (dev/staging/prod)
- Contains sensitive data (credentials, API keys)
- Infrastructure-level (database URLs, file paths, ports)
- Set at deployment time and rarely changes
- Same value for all tenants in deployment
- Needs to be configured before database is available

**Examples**:
- Database connection URLs
- External API endpoints (per environment)
- API keys and credentials
- Feature flags for deployment
- Infrastructure paths

---

### PartySettingType (Application Configuration)

**Storage**: Database entities (mantle.party.PartySettingType, mantle.party.PartySetting)

**Access Methods**:
```xml
<!-- For current user's party -->
<set field="value" from="ec.user.getPreference('SettingTypeId')"/>

<!-- For any party with hierarchical resolution (walks up org hierarchy) -->
<service-call name="mantle.party.PartyServices.get#PartySettingValue"
    in-map="[partyId:organizationPartyId, partySettingTypeId:'SettingTypeId']"
    out-map="settingMap"/>
```

**Defining Settings**:
```xml
<mantle.party.PartySettingType partySettingTypeId="SettingId"
                               description="Clear description"
                               defaultValue="default"/>

<!-- Optional: Tenant-specific override -->
<mantle.party.PartySetting partyId="TENANT_ID"
                          partySettingTypeId="SettingId"
                          settingValue="tenant_value"/>
```

**Use When**:
- Setting should vary by tenant/organization/user
- Admins/users should configure via UI
- Business logic or workflow configuration
- Settings that vary by organization/department
- Requires hierarchical overrides (user → org → tenant → default)
- Application-level preferences

**Examples**:
- Business rule thresholds
- UI preferences
- Default currency per tenant
- Workflow settings
- Notification preferences

---

## Decision Matrix

| Configuration Need | Type | Access Method | Example |
|-------------------|------|---------------|---------|
| Database URL | System Property | `SystemBinding.getPropOrEnv('database_url')` | jdbc:postgresql://prod-db:5432/mydb |
| API Endpoint (env-specific) | System Property | `SystemBinding.getPropOrEnv('api_endpoint')` | https://api.production.com |
| API Key/Secret | System Property | `SystemBinding.getPropOrEnv('api_key')` | sk_live_abc123... |
| Feature Flag (deployment) | System Property | `SystemBinding.getPropOrEnv('feature_enabled')` | true/false |
| Max Orders Per Customer | PartySettingType | `ec.user.getPreference('MaxOrdersPerCustomer')` | 100 (varies by tenant) |
| Email Notification Freq | PartySettingType | `ec.user.getPreference('EmailNotificationFreq')` | DAILY (user preference) |
| Default Currency | PartySettingType | `ec.user.getPreference('DefaultCurrency')` | CLP (tenant setting) |
| Business Rule Threshold | PartySettingType | `ec.user.getPreference('ApprovalThreshold')` | 1000000 (org-specific) |

---

## Common Mistakes

### WRONG: Storing infrastructure config in database
```xml
<!-- DON'T: API endpoint should be system property -->
<mantle.party.PartySettingType partySettingTypeId="ApiEndpoint"
    description="External API endpoint URL"
    defaultValue="https://api.example.com"/>
<!-- Problem: Can't vary by environment without modifying database -->
```

### CORRECT: Use system property for infrastructure
```bash
# Set via environment or JVM arg
export external_api_endpoint=https://api.production.com
```
```xml
<set field="endpoint" from="org.moqui.util.SystemBinding.getPropOrEnv('external_api_endpoint')"/>
```

### WRONG: Using system property for tenant-specific config
```bash
# DON'T: Can't vary by tenant with system property
export max_orders_per_customer=100
```

### CORRECT: Use PartySettingType for tenant config
```xml
<mantle.party.PartySettingType partySettingTypeId="MaxOrdersPerCustomer"
    description="Maximum orders per customer"
    defaultValue="100"/>

<!-- Tenant override -->
<mantle.party.PartySetting partyId="PREMIUM_TENANT"
    partySettingTypeId="MaxOrdersPerCustomer"
    settingValue="500"/>

<set field="maxOrders" from="ec.user.getPreference('MaxOrdersPerCustomer') as Integer"/>
```

---

## Critical Warning

The entity `moqui.entity.SystemProperty` or `moqui.system.SystemProperty` does NOT exist in Moqui Framework.

If you need:
- **Infrastructure config** → Use `org.moqui.util.SystemBinding.getPropOrEnv()`
- **Application config** → Use `mantle.party.PartySettingType`

---

## Complete Service Example

```xml
<service verb="integrate" noun="WithExternalApi">
    <in-parameters>
        <parameter name="orderId" type="String" required="true"/>
    </in-parameters>
    <actions>
        <!-- INFRASTRUCTURE CONFIG (System Properties) -->
        <!-- Varies by environment, credentials, deployment settings -->
        <set field="apiEndpoint" from="org.moqui.util.SystemBinding.getPropOrEnv('external_api_endpoint')"/>
        <set field="apiKey" from="org.moqui.util.SystemBinding.getPropOrEnv('external_api_key')"/>
        <set field="featureEnabled" from="org.moqui.util.SystemBinding.getPropOrEnv('api_feature_enabled') ?: 'true'"/>

        <!-- APPLICATION CONFIG (PartySettingType) -->
        <!-- Business rules, tenant preferences, UI settings -->
        <set field="timeout" from="ec.user.getPreference('ApiTimeout') as Integer ?: 30"/>
        <set field="maxRetries" from="ec.user.getPreference('ApiMaxRetries') as Integer ?: 3"/>
        <set field="mappingMode" from="ec.user.getPreference('ApiMappingMode') ?: 'STANDARD'"/>

        <!-- Use the configuration... -->
        <service-call name="org.moqui.impl.SystemServices.http#HttpRequest" in-map="[
            url: apiEndpoint + '/orders/' + orderId,
            headers: ['Authorization': 'Bearer ' + apiKey],
            connectTimeout: timeout * 1000
        ]" out-map="response"/>
    </actions>
</service>
```

---

For detailed documentation, see:
- `.agent-os/guidelines/configuration-management.md` - Complete guidelines including store/delete lifecycle and mid-service branching patterns
- `.agent-os/moqui-service-references/examples.md` - Service integration examples
- `.agent-os/moqui-data-references/patterns.md` - Data configuration patterns
- `.agent-os/framework-guide.md` - Framework-level guidance
