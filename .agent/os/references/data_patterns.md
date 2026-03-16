# Moqui Data Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/data-files.md` - Data file categories, naming, load order, **when data files load**
- `standards/backend/data-types-custom.md` - Custom data types for environment/load control
- `guidelines/data-updates.md` - **Deploying data changes to existing environments**
- `standards/backend/status-workflow.md` - Status types, items, and flow transitions
- `standards/backend/security.md` - Authentication, authorization, and row-level security

---

## Configuration Access Patterns

### System Properties (Infrastructure Configuration)
- **Storage**: JVM system properties or OS environment variables (NOT in database)
- **Access**: `org.moqui.util.SystemBinding.getPropOrEnv('property_name')`
- **Use For**: Environment-specific settings (API endpoints, credentials)

```bash
# Set via environment variables
export sii_api_endpoint=https://palena.sii.cl/DTEWS
export sii_api_key=secret123
```

```xml
<!-- Access in services -->
<set field="endpoint" from="org.moqui.util.SystemBinding.getPropOrEnv('sii_api_endpoint')"/>
```

### PartySettingType (Application Configuration)
- **Storage**: Database entities (tenant-aware)
- **Access**: `ec.user.getPreference('SettingTypeId')`
- **Use For**: Business rules, tenant/user preferences

```xml
<!-- Define setting type with default -->
<mantle.party.PartySettingType partySettingTypeId="DteAutoSendEnabled"
                               description="Enable automatic sending of DTE documents"
                               defaultValue="true"/>
```

**WARNING**: The entity `moqui.entity.SystemProperty` does NOT exist in Moqui Framework!

---

## Service Calls in Data Files (CRITICAL)

**When calling services from XML data files, replace `#` with `-` in service names.**

The `#` character is NOT valid in XML element names. In data files, the service name becomes the XML element tag directly, so the verb-noun separator must be a hyphen (`-`) instead of hash (`#`).

```xml
<!-- CORRECT: Service name as tag with dash replacing hash -->
<example.OrderServices.create-Order orderId="ORD001" customerName="Test Customer"/>
<my.namespace.DemoDataServices.load-MyComponentDemoData/>
<imi.wms.ProductServices.migrate-ProductClassEnumeration/>

<!-- WRONG: Hash is NOT valid in XML element names - will cause parse errors -->
<example.OrderServices.create#Order orderId="ORD001"/>
```

**Note**: In service-call `name=""` attributes (inside screens, services, etc.), continue using `#`:
```xml
<service-call name="example.OrderServices.create#Order" in-map="context"/>
```

**REFERENCE**: See `standards/backend/data-files.md` section "Service Calls in Data Files (CRITICAL)" for complete documentation including ServiceJob patterns.

---

## Data Templates

### Basic Seed Data File Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- Enumeration Type -->
    <moqui.basic.EnumerationType enumTypeId="[EnumTypeId]"
                                 description="[Type Description]"/>

    <!-- Enumeration Values -->
    <moqui.basic.Enumeration enumId="[EnumId]"
                             enumTypeId="[EnumTypeId]"
                             enumCode="[CODE]"
                             description="[Description]"
                             sequenceNum="10"/>

    <!-- Application Configuration -->
    <mantle.party.PartySettingType partySettingTypeId="[SettingId]"
                                   description="[Setting Description]"
                                   defaultValue="[DefaultValue]"/>
</entity-facade-xml>
```

### Status Flow Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="seed">
    <!-- Status Type -->
    <moqui.basic.StatusType statusTypeId="[StatusType]"
                            description="[Status Type Description]"/>

    <!-- Status Items -->
    <moqui.basic.StatusItem statusId="[StatusType][Status1]"
                            statusTypeId="[StatusType]"
                            statusCode="[CODE1]"
                            description="[Description]"
                            sequenceNum="10"/>

    <!-- Transitions -->
    <moqui.basic.StatusFlowTransition statusFlowId="Default"
                                      statusId="[StatusType][Status1]"
                                      toStatusId="[StatusType][Status2]"
                                      transitionName="[TransitionName]"/>
</entity-facade-xml>
```

### Demo Data Template (Static)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="demo">
    <!-- Demo Organizations -->
    <mantle.party.Party partyId="DEMO_ORG_001"
                        partyTypeEnumId="PtyOrganization"/>
    <mantle.party.Organization partyId="DEMO_ORG_001"
                               organizationName="Demo Organization"/>

    <!-- Demo Users -->
    <moqui.security.UserAccount userId="demo.user"
                                 username="demouser"
                                 userFullName="Demo User"
                                 requirePasswordChange="N"/>

    <moqui.security.UserGroupMember userGroupId="[GroupId]"
                                    userId="demo.user"
                                    fromDate="[Date]"/>
</entity-facade-xml>
```

### Demo Data with Date Refresh

Projects may implement demo data date refresh systems for time-sensitive scenarios. See your project's overlay documentation for the specific implementation pattern and available services.

---

## Security Data Patterns

### User Groups (Roles)
```xml
<moqui.security.UserGroup userGroupId="DTE_ADMIN"
                          description="DTE System Administrator"/>

<moqui.security.UserGroup userGroupId="DTE_USER"
                          description="DTE End User"/>
```

### Artifact Groups (Permissions)
```xml
<moqui.security.ArtifactGroup artifactGroupId="DTE_ALL_ACCESS"
                              description="Full DTE system access"/>

<moqui.security.ArtifactGroupMember artifactGroupId="DTE_ALL_ACCESS"
                                    artifactName="mycompany.myapp.*"
                                    nameIsPattern="Y"
                                    inheritAuthz="Y"/>
```

### Authorization Rules
```xml
<moqui.security.ArtifactAuthz artifactAuthzId="DTE_ADMIN_ALL"
                              userGroupId="DTE_ADMIN"
                              artifactGroupId="DTE_ALL_ACCESS"
                              authzTypeEnumId="AUTHZT_ALLOW"
                              authzActionEnumId="AUTHZA_ALL"/>
```

### Entity Filters (Data-Level Security)
```xml
<moqui.security.EntityFilterSet entityFilterSetId="USER_COMPANY_FILTER"
                                description="Filter by user's company"/>

<moqui.security.EntityFilter entityFilterSetId="USER_COMPANY_FILTER"
                             entityName="mycompany.myapp.FiscalTaxDocument"
                             filterMap='{"issuerPartyId":"${ec.user.userAccount?.partyId}"}'/>
```

---

## Localization Data Patterns

### Localized Messages
```xml
<moqui.basic.LocalizedMessage original="Factura Electrónica"
                              locale="en"
                              localized="Electronic Invoice"/>
```

### Entity Field Localization
```xml
<moqui.basic.LocalizedEntityField entityName="FiscalTaxDocument"
                                  fieldName="documentDate"
                                  locale="es"
                                  localizedLabel="Fecha del Documento"/>
```

---

## Service Job Configuration

```xml
<moqui.service.job.ServiceJob jobName="DteProcessingJob"
                              description="DTE Document Processing"
                              serviceName="mycompany.myapp.DteProcessingServices.process-Documents"
                              cronExpression="0 */5 * * * ?"
                              paused="N"/>

<moqui.service.job.ServiceJobParameter jobName="DteProcessingJob"
                                       parameterName="batchSize"
                                       parameterValue="100"/>
```

---

## Quality Checklist

**Data Quality:**
- [ ] All required fields are populated
- [ ] Foreign key references are valid
- [ ] Enumeration values follow conventions
- [ ] Status flows are complete and logical

**Configuration:**
- [ ] System Properties documented for infrastructure
- [ ] PartySettingType used for application settings
- [ ] Default values provided

**Security:**
- [ ] User groups properly defined
- [ ] Permissions appropriately assigned
- [ ] Entity filters configured for data-level security

**Organization:**
- [ ] Files numbered for correct load order
- [ ] Descriptive file names used
- [ ] Documentation comments included