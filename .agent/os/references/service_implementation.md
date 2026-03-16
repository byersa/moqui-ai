# Moqui Service Implementation Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/xml-dsl-vs-script.md` - When to use XML DSL vs scripts
- `standards/backend/cdata-scripts.md` - When to use CDATA blocks for Groovy scripts
- `standards/backend/error-handling.md` - Error handling and transaction behavior
- `standards/backend/integration-patterns.md` - External API calls, RestClient, retry patterns
- `standards/backend/seca-patterns.md` - Service Event Condition Actions
- `standards/backend/datetime-handling.md` - Date/time parsing, formatting, and arithmetic patterns
- `standards/backend/file-resources.md` - Database resources, file storage, and content location patterns
- `standards/backend/service-jobs.md` - Job scheduling, cron patterns, and notifications
- `standards/backend/email-notifications.md` - EmailTemplate patterns and email ECA handling
- `standards/global/xml-formatting.md` - XML formatting standards

**Framework Guide Reference**: For detailed patterns, see `runtime/component/moqui-agent-os/framework-guide.md`:
- **Jsoup Gotcha**: Search for "### Jsoup.parse() Groovy Method Overloading Gotcha" - Null parameter ambiguity in HTML parsing

---

## Resource and File Operations

### Reading File Content from ResourceReference

**CRITICAL: Use `openStream().readAllBytes()` pattern for reading file bytes.**

The `ResourceReference` class (especially `DbResourceReference`) does NOT have a `getBytes()` method.

**IMPORTANT**: Always close streams from `openStream()` to prevent resource leaks (especially with S3/cloud storage):

```xml
<!-- ✅ CORRECT: Use openStream() with proper stream closing -->
<script><![CDATA[
    ResourceReference rr = ec.resource.getLocationReference(contentLocation)
    def stream = rr.openStream()
    try {
        fileBytes = stream.readAllBytes()
    } finally {
        stream?.close()
    }
]]></script>

<!-- ❌ WRONG: Stream not closed - causes resource leaks -->
<script><![CDATA[
    byte[] fileBytes = rr.openStream().readAllBytes()  // Stream leaked!
]]></script>

<!-- ❌ WRONG: getBytes() method does NOT exist -->
<script>
    byte[] fileBytes = rr.getBytes()  // Throws NoSuchMethodException!
</script>
```

See `standards/backend/file-resources.md` § Stream Management for complete patterns.

### File Versioning with Timestamps

When storing files that might be replaced, use timestamps to prevent overwrites:

```xml
<!-- Generate timestamp-based filename -->
<set field="timestamp" from="ec.l10n.format(ec.user.nowTimestamp, 'yyyyMMdd-HHmmss')"/>
<set field="contentLocation" from="'dbresource://component/path/' + entityId + '/signed-' + timestamp + '.pdf'"/>

<!-- Store new version -->
<set field="docRr" from="ec.resource.getLocationReference(contentLocation)"/>
<script>docRr.putBytes(pdfBytes)</script>
```

### Expiring Previous Content Records

When adding new versioned content, expire (don't delete) previous records using `thruDate`:

```xml
<!-- Find existing content records -->
<entity-find entity-name="mantle.work.effort.WorkEffortContent" list="existingContentList">
    <econdition field-name="workEffortId"/>
    <econdition field-name="contentTypeEnumId" value="WectSignedDocument"/>
    <econdition field-name="thruDate" operator="is-null"/>
</entity-find>

<!-- Expire previous records (set thruDate to now) -->
<iterate list="existingContentList" entry="existingContent">
    <service-call name="update#mantle.work.effort.WorkEffortContent"
        in-map="[workEffortContentId: existingContent.workEffortContentId, thruDate: ec.user.nowTimestamp]"/>
</iterate>

<!-- Create new content record -->
<service-call name="create#mantle.work.effort.WorkEffortContent"
    in-map="[workEffortId: workEffortId, contentTypeEnumId: 'WectSignedDocument',
             contentLocation: contentLocation, fromDate: ec.user.nowTimestamp]"/>
```

---

## Date/Time Operations in XML DSL

### Generating Date Ranges and Week Lists

```xml
<!-- ✅ GOOD: XML DSL with inline Groovy expressions -->
<set field="weekList" from="[]"/>

<!-- Initialize calendar with locale -->
<set field="cal" from="java.util.Calendar.getInstance(new java.util.Locale('es', 'CL'))"/>
<script>cal.setMinimalDaysInFirstWeek(5)</script>

<!-- Get current year and week -->
<set field="currentYear" from="cal.get(java.util.Calendar.YEAR)"/>
<set field="currentWeek" from="cal.get(java.util.Calendar.WEEK_OF_YEAR)"/>

<!-- Generate range of week offsets -->
<set field="weekOffsets" from="(0..52).toList()"/>

<!-- Iterate and build week strings -->
<iterate list="weekOffsets" entry="offset">
    <set field="weekCal" from="java.util.Calendar.getInstance(new java.util.Locale('es', 'CL'))"/>
    <script>
        weekCal.setMinimalDaysInFirstWeek(5)
        weekCal.add(java.util.Calendar.WEEK_OF_YEAR, -offset)
    </script>
    <set field="year" from="weekCal.get(java.util.Calendar.YEAR)"/>
    <set field="week" from="weekCal.get(java.util.Calendar.WEEK_OF_YEAR)"/>
    <set field="weekStr" from="String.format('%d-W%02d', year, week)"/>
    <script>weekList.add(weekStr)</script>
</iterate>
```

---

## Batch Processing Patterns

### Transaction Isolation for Multi-Step Operations

**CRITICAL: Use `transaction="force-new"` when each step must persist independently.**

```xml
<!-- Step 1: Copy content (separate transaction) -->
<service verb="copy" noun="DocumentContent" transaction="force-new">
    <in-parameters>
        <parameter name="sourceLocation" required="true"/>
        <parameter name="targetLocation" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="success" type="Boolean"/>
        <parameter name="errorMessage"/>
    </out-parameters>
    <actions>
        <script><![CDATA[
            try {
                sourceRr = ec.resource.getLocationReference(sourceLocation)
                if (!sourceRr.exists) {
                    success = false
                    errorMessage = "Source file does not exist: ${sourceLocation}"
                    return
                }
                byte[] sourceBytes = sourceRr.openStream().withStream { it.bytes }
                targetRr = ec.resource.getLocationReference(targetLocation)
                targetRr.putBytes(sourceBytes)
                success = true
            } catch (Exception e) {
                success = false
                errorMessage = "Copy failed: ${e.message}"
            }
        ]]></script>
    </actions>
</service>

<!-- Orchestrator: calls each step, handling failures gracefully -->
<service verb="migrate" noun="SingleDocument">
    <actions>
        <service-call name="copy#DocumentContent" out-map="copyResult"/>
        <if condition="!copyResult.success">
            <set field="errorMessage" from="copyResult.errorMessage"/>
            <return/>
        </if>

        <service-call name="verify#DocumentCopy" out-map="verifyResult"/>
        <if condition="!verifyResult.verified">
            <set field="errorMessage" from="verifyResult.errorMessage"/>
            <return/>
        </if>
    </actions>
</service>
```

### Error Isolation in Batch Loops

**CRITICAL: Clear errors at the start of each record iteration to prevent cascading failures.**

```xml
<script><![CDATA[
    transactionDead = false

    for (entityName in entityNames) {
        if (transactionDead) break

        for (record in recordList) {
            // Check if transaction is still active
            if (ec.transaction.isTransactionInPlace() && !ec.transaction.isTransactionActive()) {
                ec.logger.warn("Transaction is no longer active (likely timeout), stopping batch")
                transactionDead = true
                break
            }

            try {
                // CRITICAL: Clear errors from previous iterations
                ec.message.clearErrors()

                // Process record...
                pathResult = ec.service.sync().name(pathServiceName)
                    .parameters([partyId: partyId]).call()

                // Use null-safe operators after service calls
                storagePath = pathResult?.storagePath

                if (!storagePath) {
                    skippedCount++
                    continue
                }

                // Continue processing...

            } catch (Exception e) {
                errorCount++
                errors.add("${entityName} ${primaryKey}: ${e.message}")
            }
        }
    }
]]></script>
```

**Key Patterns:**
- `ec.message.clearErrors()` - Clears message errors at start of each iteration
- `transactionDead` flag - Breaks out of nested loops when transaction dies
- `ec.transaction.isTransactionActive()` - Detects transaction timeout/rollback
- `pathResult?.storagePath` - Null-safe access prevents NPE when service fails

### Service Parameter Type Limitations

**CRITICAL: Moqui does NOT support `byte[]` as a service parameter type.**

```xml
<!-- ❌ WRONG: byte[] parameter will cause runtime error -->
<out-parameters>
    <parameter name="sourceBytes" type="byte[]"/>  <!-- Throws IllegalArgumentException -->
</out-parameters>

<!-- ✅ CORRECT: Use Base64 string or store bytes in ResourceReference -->
<out-parameters>
    <parameter name="bytesBase64" type="String"/>  <!-- Base64 encoded -->
</out-parameters>
```

### ServiceJob Batch Processing Pattern

**Pattern for batch processing with ServiceJob that reschedules itself:**

```xml
<service verb="migrate" noun="DocumentStorageBatch" authenticate="anonymous-all">
    <description>
        Designed for ServiceJob. Returns error when pending, causing job reschedule.
        Returns success when migration is complete.
    </description>
    <in-parameters>
        <parameter name="fromPrefix" required="true"/>
        <parameter name="batchSize" type="Integer" default="100"/>
    </in-parameters>
    <out-parameters>
        <parameter name="migratedCount" type="Integer"/>
        <parameter name="pendingCount" type="Integer"/>
        <parameter name="migrationComplete" type="Boolean"/>
    </out-parameters>
    <actions>
        <service-call name="migrate#DocumentStorage" in-map="context" out-map="result"/>

        <set field="migratedCount" from="result.migratedCount"/>
        <set field="pendingCount" from="result.pendingCount"/>
        <set field="migrationComplete" from="pendingCount == 0"/>

        <!-- Return error if pending, causing job to reschedule -->
        <if condition="pendingCount > 0">
            <return error="true" message="Migration in progress: ${migratedCount} migrated, ${pendingCount} pending"/>
        </if>

        <log message="Document storage migration complete. Total migrated: ${migratedCount}"/>
    </actions>
</service>
```

---

## Batch Processing Checklist

Before implementing batch processing, verify:

- [ ] **Step Isolation**: Each critical step uses `transaction="force-new"`
- [ ] **Error Clearing**: `ec.message.clearErrors()` at start of each record iteration
- [ ] **Timeout Detection**: Check `ec.transaction.isTransactionActive()` in loops
- [ ] **Null Safety**: Use `?.` operator on service call results
- [ ] **Loop Breaking**: Use a flag to break out of nested loops on fatal errors
- [ ] **Parameter Types**: No `byte[]` parameters - use Base64 or separate reads
- [ ] **Exception Handling**: `try/catch` in loops to continue on individual failures
- [ ] **Progress Tracking**: Count success, skipped, error, and pending records
- [ ] **ServiceJob Pattern**: Return error to reschedule, success to complete