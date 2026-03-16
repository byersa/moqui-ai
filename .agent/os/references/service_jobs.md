# Moqui Service Jobs Reference

**Standards Reference**: For declarative conventions, see:
- `standards/backend/service-jobs.md` - Cron configuration, data file patterns

---

## Core Entities

### moqui.service.job.ServiceJob

Main configuration entity for service jobs.

| Field | Type | Description |
|-------|------|-------------|
| `jobName` (PK) | text-short | Unique identifier for the job |
| `serviceName` | text-medium | Fully qualified service name to execute |
| `cronExpression` | text-short | Quartz-style cron expression (null = ad-hoc only) |
| `paused` | text-indicator | Y/N - If Y, scheduled execution is disabled |
| `transactionTimeout` | number-integer | Transaction timeout in seconds |
| `expireLockTime` | number-integer | Minutes before lock is considered stale (default: 1440) |
| `minRetryTime` | number-integer | Minimum minutes between retries after error |
| `priority` | number-integer | Lower numbers run first |
| `localOnly` | text-indicator | Y/N - If Y, runs only on local server |
| `topic` | text-medium | NotificationTopic for sending results |
| `fromDate` / `thruDate` | date-time | Active date range |
| `repeatCount` | number-integer | Number of times to repeat |

### moqui.service.job.ServiceJobParameter

Parameters passed to the service when the job runs.

| Field | Type |
|-------|------|
| `jobName` (PK) | text-short |
| `parameterName` (PK) | text-short |
| `parameterValue` | text-medium |

### moqui.service.job.ServiceJobUser

Users who receive notifications when the job completes.

| Field | Type |
|-------|------|
| `jobName` (PK) | text-short |
| `userId` (PK) | id |
| `receiveNotifications` | text-indicator |

### moqui.service.job.ServiceJobRun

Runtime records for each job execution.

| Field | Type | Description |
|-------|------|-------------|
| `jobRunId` (PK) | id | |
| `jobName` | text-short | |
| `userId` | id | User who triggered the job |
| `parameters` | text-long | Serialized input parameters |
| `results` | text-very-long | Serialized results |
| `messages` | text-long | Service messages |
| `hasError` | text-indicator | Y if job failed |
| `errors` | text-long | Error details |
| `hostAddress` / `hostName` | text-short/medium | Server that ran the job |
| `runThread` | text-medium | Thread name |
| `startTime` / `endTime` | date-time | Execution timestamps |

### moqui.service.job.ServiceJobRunLock

Prevents concurrent execution of the same job.

| Field | Type |
|-------|------|
| `jobName` (PK) | text-short |
| `jobRunId` | id |
| `lastRunTime` | date-time |

---

## ServiceCallJob Programmatic API

### Interface

```java
public interface ServiceCallJob extends ServiceCall, Future<Map<String, Object>> {
    ServiceCallJob parameters(Map<String, Object> context);
    ServiceCallJob parameter(String name, Object value);
    ServiceCallJob localOnly(boolean local);
    String run() throws ServiceException;  // Returns jobRunId
}
```

### Running Jobs Programmatically

```groovy
// Simple job execution
String jobRunId = ec.service.job("MyJobName").run()

// With parameters
String jobRunId = ec.service.job("MyJobName")
    .parameter("param1", "value1")
    .parameter("param2", "value2")
    .run()

// With map of parameters
String jobRunId = ec.service.job("MyJobName")
    .parameters([param1: "value1", param2: "value2"])
    .run()

// Local only execution (no distributed)
String jobRunId = ec.service.job("MyJobName")
    .localOnly(true)
    .run()
```

### Checking Job Results

Since `ServiceCallJob` implements `Future<Map<String, Object>>`:

```groovy
def jobCall = ec.service.job("MyJobName")
String jobRunId = jobCall.run()

// Option 1: Wait for results (blocking)
Map results = jobCall.get()

// Option 2: Check job run record
def jobRun = ec.entity.find("moqui.service.job.ServiceJobRun")
    .condition("jobRunId", jobRunId).one()
if (jobRun.hasError == "Y") {
    logger.error("Job failed: ${jobRun.errors}")
} else {
    logger.info("Job completed: ${jobRun.results}")
}
```

---

## Framework Jobs Inventory

### Cleanup Jobs

| Job Name | Service | Default Schedule | Description |
|----------|---------|-----------------|-------------|
| `clean_ArtifactData_daily` | `ServerServices.clean#ArtifactData` | `0 0 2 * * ?` | Clean ArtifactHit/ArtifactHitBin (90 days) |
| `clean_PrintJobData_daily` | `ServerServices.clean#PrintJobData` | `0 0 2 * * ?` | Clean print job data (7 days) |
| `clean_ServiceJobRun_daily` | `ServiceServices.clean#ServiceJobRun` | `0 0 2 * * ?` | Clean job run history (30 days) |
| `clean_ElasticSearchLogMessages_daily` | `SearchServices.delete#Documents` | `0 0 2 * * ?` (paused) | Clean ES log messages (90 days) |

### Integration Jobs

| Job Name | Service | Default Schedule | Description |
|----------|---------|-----------------|-------------|
| `run_EntitySyncAll_frequent` | `EntitySyncServices.run#EntitySyncAll` | `0 0/15 * * * ?` (paused) | Entity synchronization |
| `send_AllProducedSystemMessages_frequent` | `SystemMessageServices.send#AllProducedSystemMessages` | `0 0/15 * * * ?` (paused) | Send produced system messages |
| `consume_AllReceivedSystemMessages_frequent` | `SystemMessageServices.consume#AllReceivedSystemMessages` | `0 0/15 * * * ?` (paused) | Consume received system messages |
| `poll_EmailServer_frequent` | `EmailServices.poll#EmailServer` | `0 0/15 * * * ?` (paused) | Poll email server for new messages |
| `render_ScheduledScreens_frequent` | `ScreenServices.render#ScheduledScreens` | `0 0/15 * * * ?` | Render scheduled screen outputs |

---

## Best Practices

1. **Always set `transactionTimeout`** for long-running jobs
2. **Set `expireLockTime`** higher than expected job duration
3. **Use `minRetryTime`** to prevent rapid retry loops on transient errors
4. **Start jobs paused** (`paused="Y"`) and enable per-environment
5. **Make jobs idempotent** - safe to run multiple times
6. **Use NotificationTopics** for important job results

### Troubleshooting

**Job not running**: Check `paused` field, `cronExpression` validity, `fromDate`/`thruDate` range, `ServiceJobRunLock` records.

**Stuck lock**: Either wait for `expireLockTime` to pass, or clear the lock manually:
```groovy
def lock = ec.entity.find("moqui.service.job.ServiceJobRunLock")
    .condition("jobName", "stuck_job_name").one()
if (lock) { lock.set("jobRunId", null); lock.update() }
```

**Monitoring**: Access job management at `/vapps/system/ServiceJob/Jobs`. Check `ServiceJobRun` records for `hasError`, `errors`, and `messages` fields.