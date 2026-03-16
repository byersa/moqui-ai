# XML Formatting

**Line length limit: 180 characters maximum**

### Opening Tag Formatting (MANDATORY)

**Rule: Keep opening tag with all attributes on a SINGLE LINE when under 180 characters**

```xml
<!-- CORRECT: Single line when it fits -->
<entity-find entity-name="{shared-utils}.seq.SequenceAllocation" list="recyclableList" limit="1" for-update="true">
<entity-find-one entity-name="{shared-utils}.seq.SequenceAllocation" value-field="allocation" for-update="true">
<service-call name="create#Entity" in-map="[field1:value1, field2:value2]" out-map="context"/>

<!-- WRONG: Unnecessary line breaks when it fits in 180 chars -->
<entity-find entity-name="{shared-utils}.seq.SequenceAllocation" list="recyclableList"
             limit="1" for-update="true">
```

**Only split when exceeding 180 characters:**
```xml
<!-- Multi-line only when necessary (over 180 chars) -->
<service-call name="create#mantle.party.PartyIdentification"
              in-map="[partyId:partyId, partyIdTypeEnumId:'PtidUniqueNationalId', idValue:normalizedRun.idValue, otherField:someValue]"/>
```

### If-Then-Else Compact Formatting (MANDATORY)

**Rule 1: No `<then>` tag when there's no else/else-if clause**
```xml
<!-- CORRECT: No then tag without else -->
<if condition="hasError">
    <return error="true" message="Error occurred"/>
</if>
```

**Rule 2: REQUIRED `<then>` tag when there IS an else/else-if clause**
- `<if condition="..."><then>` MUST be on same line
- `</then><else>` MUST be on same line
- `</then><else-if condition="...">` MUST be on same line
- `</else-if></if>` MUST be on same line
- `</else></if>` MUST be on same line
- Content inside branches uses ONE indentation level increase

```xml
<!-- CORRECT: Compact formatting with else -->
<if condition="runValue"><then>
    <service-call name="normalize#Value" in-map="[value:runValue]" out-map="result"/>
</then><else>
    <set field="result" from="null"/>
</else></if>

<!-- CORRECT: Multiple branches -->
<if condition="status == 'A'"><then>
    <set field="label" value="Active"/>
</then><else-if condition="status == 'I'">
    <set field="label" value="Inactive"/>
</else-if><else>
    <set field="label" value="Unknown"/>
</else></if>
```

### Service Call Formatting

**Single Line**: When under 180 characters
```xml
<service-call name="create#Entity" in-map="[field1:value1, field2:value2]"/>
```

**Multi-Line**: With aligned continuation for longer calls
```xml
<service-call name="create#mantle.party.PartyIdentification" in-map="[partyId:partyId,
                         partyIdTypeEnumId:'PtidUniqueNationalId', idValue:normalizedRun.idValue]"/>
```

### Entity Data Formatting

**Single Line**: Simple entities under 180 characters
```xml
<moqui.security.UserGroup userGroupId="DTE_USERS" description="DTE application users"/>
```

**Multi-Line**: Complex entities with many attributes
```xml
<moqui.security.ArtifactAuthz artifactAuthzId="ORDER_MANAGER_ACCESS"
                              userGroupId="ORDER_MANAGERS"
                              artifactGroupId="ORDER_MANAGEMENT"
                              authzTypeEnumId="AUTHZT_ALLOW"
                              authzActionEnumId="AUTHZA_ALL"/>
```

### Attribute Alignment
- Align continuation attributes with first attribute
- Use consistent indentation (4 spaces)
- Group related attributes together

### Never Specify Default Values
- Omit attributes when using framework defaults
- Example: Don't specify `transaction="use-or-begin"` (it's the default)

### XML Schema Compliance
- Follow Moqui XSD schemas for all XML files
- Use proper namespace declarations
- Validate XML structure before committing

### Element Ordering Standards

**Services:**
1. `<description>`
2. `<in-parameters>`
3. `<out-parameters>`
4. `<actions>`

**Entities:**
1. `<field>` elements (primary key first)
2. `<relationship>` elements
3. `<index>` elements

**Screens:**
1. `<parameter>` elements
2. `<always-actions>`
3. `<pre-actions>`
4. `<transition>` elements
5. `<actions>`
6. `<widgets>`

**entity-find (child elements):**
1. `<econdition>`, `<econditions>`, `<date-filter>`, `<having-econditions>`
2. `<select-field>`
3. `<order-by>`
4. `<limit-range>`, `<limit-view>`, `<use-iterator>`

This ordering is enforced by the Moqui XSD schema. Placing elements out of order (e.g., `select-field` before `econdition` or after `order-by`) causes validation errors.

### ID Value Conventions

| Data Type | Format | Example |
|-----------|--------|---------|
| Configuration | ALL_CAPS_SNAKE | `ORDER_MANAGER_GROUP` |
| Enumerations | PascalCase or ALL_CAPS | `OrderPlaced` |
| User Groups | ALL_CAPS_SNAKE | `DTE_ADMIN` |
| Permissions | ALL_CAPS_SNAKE | `INVOICE_APPROVE` |
| Status Items | PascalCasePrefix | `OrdPlaced` |
| Artifact Auth | ALL_CAPS_SNAKE | `ORDER_SCREEN_ACCESS` |

**Configuration/Reference Data (seed):**
```xml
<moqui.security.UserGroup userGroupId="ORDER_MANAGERS"/>
<moqui.security.UserPermission userPermissionId="ORDER_VIEW"/>
<moqui.basic.Enumeration enumId="OtSalesOrder"/>
```

**Transactional Data (camelCase):**
```xml
<field name="customerId" type="id"/>
<field name="orderDate" type="date-time"/>
```

### Common Anti-Pattern Corrections

**ignore-if-empty vs ignore:**
```xml
<!-- WRONG: "%${var}%" is never empty -->
<econdition field-name="name" operator="like" value="%${name}%"
            ignore-if-empty="true"/>

<!-- CORRECT: Explicit null check -->
<econdition field-name="name" operator="like" value="%${name}%"
            ignore="name == null"/>
```

**Single-quoted string interpolation:**
```xml
<!-- WRONG: No interpolation in single quotes -->
<set field="url" from="'${baseUrl}/api/${id}'"/>

<!-- CORRECT: Use concatenation -->
<set field="url" from="baseUrl + '/api/' + id"/>
```