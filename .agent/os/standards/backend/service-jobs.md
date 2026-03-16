# Service Job Standards

### Job Configuration Entity

```xml
<moqui.service.job.ServiceJob
    jobName="example_ProcessOrders_daily"
    description="Process pending orders daily"
    serviceName="example.OrderServices.process#PendingOrders"
    cronExpression="0 0 2 * * ?"
    transactionTimeout="1800"
    expireLockTime="120"
    paused="Y"/>
```

### Cron Expression Reference

| Expression | Description |
|------------|-------------|
| `0 0 2 * * ?` | Daily at 2:00 AM |
| `0 0/15 * * * ?` | Every 15 minutes |
| `0 30 8 * * ?` | Daily at 8:30 AM |
| `0 0 3 ? * MON-FRI` | Weekdays at 3:00 AM |
| `0 0 0 1 * ?` | First day of month at midnight |

### Cron Field Order

```
Seconds Minutes Hours DayOfMonth Month DayOfWeek [Year]
```

### Special Characters

| Char | Meaning | Example |
|------|---------|---------|
| `*` | All values | Every minute |
| `?` | No specific value | Day of month/week |
| `-` | Range | `1-5` (Mon-Fri) |
| `,` | List | `MON,WED,FRI` |
| `/` | Increment | `0/15` (every 15) |
| `L` | Last | Last day of month |

### Job Attributes

| Attribute | Purpose | Default |
|-----------|---------|---------|
| `transactionTimeout` | Timeout in seconds | Framework default |
| `expireLockTime` | Lock expiry (minutes) | 1440 (24h) |
| `minRetryTime` | Min minutes between retries | null |
| `priority` | Execution priority (lower = first) | null |
| `localOnly` | Run on local server only | N |
| `paused` | Disable scheduled execution | N |

### Job with Parameters

```xml
<moqui.service.job.ServiceJob
    jobName="clean_ArtifactData_daily"
    serviceName="org.moqui.impl.ServerServices.clean#ArtifactData"
    cronExpression="0 0 2 * * ?"
    paused="N">
    <parameters parameterName="daysToKeep" parameterValue="90"/>
</moqui.service.job.ServiceJob>
```

### Job with Notifications

```xml
<!-- Define notification topic -->
<moqui.security.user.NotificationTopic
    topic="ExportComplete"
    description="Export Completed"
    typeString="success"
    showAlert="Y"
    persistOnSend="Y"
    titleTemplate="Exported ${results.recordCount} records"
    errorTitleTemplate="Error in export: ${errors}"/>

<!-- Assign topic to job -->
<moqui.service.job.ServiceJob
    jobName="export_Data"
    serviceName="example.ExportServices.export#Data"
    topic="ExportComplete"
    transactionTimeout="3600"/>
```

### Programmatic Job Execution

```groovy
// Simple execution
String jobRunId = ec.service.job("MyJobName").run()

// With parameters
String jobRunId = ec.service.job("MyJobName")
    .parameter("param1", "value1")
    .parameter("param2", "value2")
    .run()

// Local only
String jobRunId = ec.service.job("MyJobName")
    .localOnly(true)
    .run()
```

### Checking Job Results

```groovy
def jobRun = ec.entity.find("moqui.service.job.ServiceJobRun")
    .condition("jobRunId", jobRunId).one()

if (jobRun.hasError == "Y") {
    logger.error("Job failed: ${jobRun.errors}")
} else {
    logger.info("Job completed: ${jobRun.results}")
}
```

### Best Practices

| Practice | Reason |
|----------|--------|
| Start paused (`paused="Y"`) | Enable in production after testing |
| Set `transactionTimeout` | Prevent timeout for long jobs |
| Set `expireLockTime` > expected duration | Allow lock recovery |
| Use `minRetryTime` | Prevent rapid retry loops |
| Make jobs idempotent | Safe to run multiple times |

### Pausing Framework Jobs

```xml
<!-- Override framework job to pause it -->
<moqui.service.job.ServiceJob jobName="render_ScheduledScreens_frequent" paused="Y"/>
```

### Ad-hoc Job (No Schedule)

```xml
<moqui.service.job.ServiceJob
    jobName="MigrationTask"
    serviceName=""
    description="Manual migration tasks"
    transactionTimeout="960"
    paused="Y"
    expireLockTime="30"/>
```

### Multi-Tenant Organization Scoping for Jobs

Batch jobs that need to operate across all tenant organizations must build an `orgPartyIdList` containing all active tenants and their child organizations. This list is used to filter entity queries by `customerPartyId` or `receiverPartyId`.

**Pattern: Build flat org list from all tenants**

```xml
<!-- Get all active tenant party IDs -->
<service-call name="{shared-utils}.tenant.MultiTenantServices.get#SiteInfo" out-map="siteInfoMap"/>
<script>orgPartyIdList = []</script>
<!-- Expand each tenant to include child organizations -->
<iterate list="siteInfoMap.tenantPartyIds" entry="tenantPartyId">
    <service-call name="mantle.ledger.LedgerServices.expand#ChildOrganizationList"
                  in-map="[organizationPartyId:tenantPartyId]"
                  out-map="expandResult" out-map-add-to-existing="false"/>
    <script>orgPartyIdList.addAll(expandResult.orgPartyIdList)</script>
</iterate>
<!-- Use orgPartyIdList to filter queries -->
<entity-find entity-name="example.SomeDetail" list="resultList">
    <econdition field-name="customerPartyId" operator="in" from="orgPartyIdList" ignore-if-empty="true"/>
</entity-find>
```

**Services involved:**
- `{shared-utils}.tenant.MultiTenantServices.get#SiteInfo`: Returns `tenantPartyIds` — the list of active tenant party IDs based on `PrtSiteTenant` relationships to the site owner.
- `mantle.ledger.LedgerServices.expand#ChildOrganizationList`: Recursively expands an organization party ID into itself plus all child organizations via `PrtOrgRollup` relationships. Results are cached.

**When to use each pattern:**
- **Per-org jobs** (implementing `run#DteProcessingJob`): Receive `organizationPartyId` as input, call `expand#ChildOrganizationList` once.
- **Cross-org "Common" jobs** (standalone cron): No org input, must iterate all tenants using the pattern above.
- **Always use `ignore-if-empty="true"`** on the econdition as safety fallback in case the list is empty.

### Incremental Batch Processing Pattern (Fail-to-Retry)

For long-running jobs that process many items with external API calls or sleeps, avoid a single multi-hour transaction. Instead, use short runs that process as many items as possible within a time budget, then fail intentionally to reschedule via `minRetryTime`.

**How it works:**
1. `transactionTimeout` limits each run (e.g., 30 minutes)
2. The loop checks `isTransactionActive()` — returns false when the transaction times out
3. After the loop, the service compares processed vs total items
4. If items remain, `<return error="true">` triggers a reschedule after `minRetryTime` minutes
5. `expireLockTime` must exceed `transactionTimeout` to allow graceful cleanup

**Job configuration:**

```xml
<moqui.service.job.ServiceJob jobName="processLargeQueue"
    serviceName="example.BatchServices.process#LargeQueue"
    transactionTimeout="1800"
    expireLockTime="40"
    minRetryTime="10"
    paused="N" cronExpression="0 0 20 * * ?"/>
```

**Service implementation:**

```xml
<service verb="process" noun="LargeQueue" authenticate="anonymous-all">
    <in-parameters>
        <parameter name="delayMilliseconds" type="Long" default="3500L"/>
    </in-parameters>
    <actions>
        <entity-find entity-name="example.PendingItem" list="itemList">
            <econdition field-name="statusId" value="Pending"/>
            <select-field field-name="itemId"/>
        </entity-find>
        <set field="processedCount" from="0" type="Integer"/>
        <set field="totalCount" from="itemList.size()" type="Integer"/>

        <iterate list="itemList" entry="item">
            <if condition="!ec.transaction.isTransactionActive()">
                <log message="Transaction timed out, stopping"/>
                <break/>
            </if>
            <service-call name="example.BatchServices.process#SingleItem"
                          in-map="[itemId:item.itemId]"
                          transaction="force-new" ignore-error="true"/>
            <set field="processedCount" from="processedCount+1"/>
            <script>Thread.sleep(delayMilliseconds)</script>
        </iterate>

        <set field="pendingCount" from="totalCount - processedCount" type="Integer"/>
        <log message="Processed: ${processedCount}, pending: ${pendingCount}"/>
        <!-- Return error to trigger reschedule when items remain -->
        <if condition="pendingCount > 0">
            <return error="true" message="In progress: ${processedCount} processed, ${pendingCount} pending. Will retry."/>
        </if>
    </actions>
</service>
```

**Key design points:**
- Each item is processed in `transaction="force-new"` so completed work is committed regardless of the outer transaction timing out
- `ignore-error="true"` on each call prevents one failed item from stopping the batch
- The `isTransactionActive()` check provides a graceful stop instead of a hard timeout error
- Query filters (e.g., `lastRefresh < threshold`) ensure already-processed items are skipped on retry
- The job completes successfully (no error return) only when all items are processed

**Sizing guidelines:**

| Attribute | Purpose | Typical Value |
|-----------|---------|---------------|
| `transactionTimeout` | Max run duration per attempt | 1800 (30 min) |
| `expireLockTime` | Lock expiry, must exceed timeout | timeout/60 + 10 min |
| `minRetryTime` | Pause between retries | 10 min |

### Durable State Updates in ServiceJobs

When a ServiceJob service must persist state (progress tracking, checkpoints, counters) that **must survive** regardless of the parent transaction's outcome, use `requireNewTransaction(true)` for each state update — not direct `entity.update()`.

**Problem:** Direct `entity.update()` shares the ServiceJob's transaction. If anything later marks the transaction as rollback-only (e.g., `ec.message.addError()`, a timed-out transaction, an unhandled exception), **all** entity updates in that transaction are rolled back — including progress tracking.

**Pattern: Independent state updates via service calls**

```groovy
// WRONG: shares the scheduler's transaction — lost on rollback
bgState.processedItems = newCount
bgState.update()

// CORRECT: commits in its own transaction — survives parent rollback
ec.service.sync().name("update#moqui.example.ProgressState")
        .parameters(bgState.getMap())
        .requireNewTransaction(true).disableAuthz().call()
```

**When to use this pattern:**
- Progress tracking in long-running scheduler services
- Checkpoint persistence for resumable batch processing
- Failure counters and error recording that must survive even if the run fails
- Any state that is "write-ahead" — records what happened before the overall outcome is known

**When NOT to use:**
- Normal service implementations where all-or-nothing semantics are desired
- State that should roll back if the operation fails (e.g., order totals during order processing)

**Combine with `ec.message.clearErrors()`** (see `error-handling.md`) to prevent child errors from tainting the parent transaction:

```groovy
// 1. Run batch in its own transaction
def result = ec.service.sync().name("process#Batch")
        .parameters([...]).requireNewTransaction(true).call()
// 2. Clear propagated errors
ec.message.clearErrors()
// 3. Update state durably
ec.service.sync().name("update#ProgressState")
        .parameters([processedItems: result.count])
        .requireNewTransaction(true).call()
```

### Troubleshooting Jobs

**Job not running:**
1. Check `paused` is `N`
2. Verify `cronExpression` is valid
3. Check `fromDate`/`thruDate` range
4. Look for lock in `ServiceJobRunLock`

**Clear stuck lock:**
```groovy
def lock = ec.entity.find("moqui.service.job.ServiceJobRunLock")
    .condition("jobName", "stuck_job").one()
if (lock) {
    lock.set("jobRunId", null)
    lock.update()
}
```

### Job Monitoring

- **Admin UI**: `/vapps/system/ServiceJob/Jobs`
- **Run History**: `/vapps/system/ServiceJob/JobRuns`
- **Run Details**: `/vapps/system/ServiceJob/JobRuns/JobRunDetail?jobRunId={id}`
