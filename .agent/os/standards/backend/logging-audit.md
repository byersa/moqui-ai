# Logging and Audit Patterns

Structured logging, audit trails, and artifact tracking.

## Logging Levels

```groovy
ec.logger.trace("Detailed trace info")    // Development only
ec.logger.debug("Debug: value=${val}")    // Development debugging
ec.logger.info("Processing started")      // Normal operations
ec.logger.warn("Unexpected state: ${x}")  // Non-critical issues
ec.logger.error("Failed: ${e.message}")   // Errors requiring attention
```

## Logging Level Guidelines

| Level | Use Case | Production |
|-------|----------|------------|
| `trace` | Very detailed debugging | Disabled |
| `debug` | Variable values, flow tracing | Disabled |
| `info` | Operational milestones | Enabled |
| `warn` | Recoverable issues | Enabled |
| `error` | Failures requiring attention | Enabled |

## Structured Logging in Services

```xml
<service verb="process" noun="Order">
    <actions>
        <log level="info" message="Processing order ${orderId} for customer ${customerId}"/>

        <!-- Include context in errors -->
        <if condition="ec.message.hasError()"><then>
            <log level="error" message="Order ${orderId} failed: ${ec.message.getErrorsString()}"/>
        </then></if>

        <log level="info" message="Order ${orderId} completed in ${duration}ms"/>
    </actions>
</service>
```

## Entity Audit Logging

```xml
<!-- Enable audit on sensitive fields -->
<entity entity-name="Order" package="example">
    <field name="orderId" type="id" is-pk="true"/>
    <field name="statusId" type="id" enable-audit-log="true"/>
    <field name="amount" type="currency-amount" enable-audit-log="true"/>
    <field name="approvedByUserId" type="id" enable-audit-log="true"/>
</entity>
```

Audit logs are stored in `moqui.entity.EntityAuditLog`:
- `changedEntityName`, `changedFieldName`
- `pkPrimaryValue`, `pkSecondaryValue`
- `oldValueText`, `newValueText`
- `changedDate`, `changedByUserId`

## Querying Audit Logs

### All changes to a specific record

```xml
<entity-find entity-name="moqui.entity.EntityAuditLog" list="auditList">
    <econdition field-name="changedEntityName" value="example.Order"/>
    <econdition field-name="pkPrimaryValue" from="orderId"/>
    <order-by field-name="-changedDate"/>
</entity-find>
```

### When a specific field was changed to a specific value

Use `changedFieldName` and `newValueText` to find the exact timestamp when a field transitioned to a known value. Order by `-changedDate` and use `one()` / `limit="1"` to get the most recent occurrence.

```xml
<!-- Find when a Party was disabled (disabled field set to 'Y') -->
<entity-find-one entity-name="moqui.entity.EntityAuditLog" value-field="auditEntry">
    <field-map field-name="changedEntityName" value="mantle.party.Party"/>
    <field-map field-name="changedFieldName" value="disabled"/>
    <field-map field-name="pkPrimaryValue" from="partyId"/>
    <field-map field-name="newValueText" value="Y"/>
</entity-find-one>
<!-- NOTE: entity-find-one returns the first match but does NOT support order-by.
     If multiple transitions exist (disabled/re-enabled/disabled again), use entity-find: -->

<entity-find entity-name="moqui.entity.EntityAuditLog" list="auditList" limit="1">
    <econdition field-name="changedEntityName" value="mantle.party.Party"/>
    <econdition field-name="changedFieldName" value="disabled"/>
    <econdition field-name="pkPrimaryValue" from="partyId"/>
    <econdition field-name="newValueText" value="Y"/>
    <order-by field-name="-changedDate"/>
</entity-find>
<set field="disabledDate" from="auditList ? auditList[0].changedDate : ec.user.nowTimestamp"/>
```

**Available fields for filtering:**
- `changedEntityName` — Full entity name (e.g., `mantle.party.Party`)
- `changedFieldName` — The specific field that changed (e.g., `disabled`, `statusId`)
- `pkPrimaryValue` — Primary key value of the changed record
- `newValueText` — The value the field was changed TO
- `oldValueText` — The value the field was changed FROM
- `changedDate` — Timestamp of the change
- `changedByUserId` — User who made the change

**Indexed columns** (efficient for queries): `changedEntityName` + `changedFieldName` + `pkPrimaryValue` (composite index `ENTAUDLOG_FLD1PK`)

## Artifact Hit Tracking

Framework tracks screen/service usage in `moqui.server.ArtifactHit`:

```xml
<!-- Query artifact usage -->
<entity-find entity-name="moqui.server.ArtifactHit" list="hitList">
    <econdition field-name="artifactType" value="AT_SERVICE"/>
    <econdition field-name="artifactName" operator="like" value="example.%"/>
    <econdition field-name="startDateTime" operator="greater" from="fromDate"/>
</entity-find>
```

## Cleanup of Historical Data

```xml
<!-- Scheduled job to purge old data -->
<service verb="purge" noun="ArtifactHits">
    <in-parameters>
        <parameter name="daysToKeep" type="Integer" default="30"/>
    </in-parameters>
    <actions>
        <set field="cutoffDate" from="ec.user.nowTimestamp - daysToKeep"/>

        <script><![CDATA[
            int removed = ec.entity.find("moqui.server.ArtifactHit")
                .condition("startDateTime", EntityCondition.LESS_THAN, cutoffDate)
                .disableAuthz().limit(10000).deleteAll()
            ec.logger.info("Removed ${removed} ArtifactHit records older than ${daysToKeep} days")
        ]]></script>
    </actions>
</service>
```

## Custom Event Logging

```xml
<entity entity-name="EventLog" package="example">
    <field name="eventLogId" type="id" is-pk="true"/>
    <field name="eventTypeId" type="id"/>
    <field name="eventDate" type="date-time"/>
    <field name="userId" type="id"/>
    <field name="entityName" type="text-medium"/>
    <field name="entityId" type="text-medium"/>
    <field name="description" type="text-long"/>
    <field name="eventData" type="text-very-long"/>  <!-- JSON payload -->
</entity>
```

```xml
<service verb="log" noun="Event" transaction="force-new">
    <in-parameters>
        <parameter name="eventTypeId" required="true"/>
        <parameter name="entityName"/>
        <parameter name="entityId"/>
        <parameter name="description"/>
        <parameter name="eventData" type="Map"/>
    </in-parameters>
    <actions>
        <service-call name="create#example.EventLog" in-map="context + [
            eventDate:ec.user.nowTimestamp,
            userId:ec.user.userId,
            eventData:eventData ? groovy.json.JsonOutput.toJson(eventData) : null
        ]"/>
    </actions>
</service>
```

**Note**: Use `transaction="force-new"` so logs persist even if calling transaction rolls back.

## Key Rules

1. **Log at appropriate level** - info for operations, error for failures
2. **Include context** - Always include entity IDs in log messages
3. **Use entity audit** - Enable `enable-audit-log` on sensitive fields
4. **Isolate logging transactions** - Use `force-new` for critical logs
5. **Purge old data** - Schedule cleanup of artifact hits and old logs
6. **Structured data** - Store complex data as JSON in `text-very-long` fields