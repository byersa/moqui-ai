# Error Handling

Patterns for service errors, user messages, and transaction behavior.

## Service Error Return
```xml
<return error="true" message="Validation failed: missing required field"/>
```
- Halts service execution immediately
- Adds error to message facade
- Triggers transaction rollback (if service began TX) or rollback-only (if joined)

## User Messages (Non-Halting)
```xml
<message type="warning">Data may be incomplete</message>
<message type="danger">Critical issue detected</message>
<message type="success">Operation completed</message>
<message type="info">Processing 50 records</message>
```
- Displayed to user but execution continues
- Does NOT affect transaction state

## Transaction + Error Behavior

| Scenario | Transaction Effect |
|----------|-------------------|
| Error in service that began TX | TX rolled back |
| Error in service that joined TX | TX marked rollback-only |
| `ignore-error="true"` | Errors cleared, execution continues, **TX still rolls back** |
| `transaction="force-new"` | Isolated TX, parent unaffected |
| `force-new` + `ignore-error` | Complete isolation (TX AND execution) |

## Complete Error Isolation Pattern
```xml
<service-call name="risky.Service" out-map="result"
              transaction="force-new" ignore-error="true"/>
<if condition="result == null">
    <log level="warn" message="Service failed, proceeding anyway"/>
</if>
```

## Logging Levels
```groovy
ec.logger.info("Processing started")   // Normal operations
ec.logger.warn("Unexpected state")     // Non-critical issues
ec.logger.error("Failed: ${e.message}") // Errors requiring attention
ec.logger.debug("Variable: ${val}")    // Development debugging
```

## Utility Services: Use Out-Parameters, Not addError

Utility/helper services that check preconditions (e.g., path existence, duplicate detection) should return status via **out-parameters**, not `ec.message.addError()`. The caller decides the error strategy.

**Why:** `addError()` in a joined transaction marks it rollback-only. The caller cannot recover — even if the "error" is a normal case (e.g., content already exists and should be reused).

```xml
<!-- WRONG: utility service decides the error policy -->
<script><![CDATA[
    if (locationRef.exists) {
        ec.message.addError("Content already exists at ${fullPath}")
        return
    }
]]></script>

<!-- CORRECT: utility returns a flag, caller decides -->
<out-parameters>
    <parameter name="existsAtPath" type="Boolean"/>
</out-parameters>
<actions>
    <script><![CDATA[
        existsAtPath = ec.resource.getLocationReference(fullPath).exists
    ]]></script>
</actions>
```

Caller handles it:
```groovy
result = ec.service.sync().name("get#StoragePath").parameters([..., checkExists:true]).call()
if (result.existsAtPath) {
    // Reuse existing content, skip creation
} else {
    // Store new content
}
```

**Rule:** Only the service that "owns" the business decision should use `addError` or `<return error="true"/>`.

## Batch Processing with Error Signaling (Groovy Scripts)

When a script-type service processes items in a batch and needs to signal an error afterwards (e.g., to trigger job scheduler retry), `addError()` at the end would roll back the **entire** transaction — undoing all successfully processed items. This is especially dangerous when the processing involves non-transactional side effects (IMAP flags, external API calls, file operations) that cannot be rolled back.

**Pattern: Isolate batch work in `runRequireNew`, then signal error in the outer transaction.**

```groovy
int processedCount = 0
boolean batchLimitReached = false

Closure processBatch = {
    for (item in items) {
        if (maxItems > 0 && processedCount >= maxItems) {
            batchLimitReached = true
            break
        }
        // ... process item (DB writes, external calls) ...
        processedCount++
    }
}

// Sub-transaction commits independently; outer TX has no DB work to lose
if (maxItems > 0) {
    ec.transaction.runRequireNew(timeout, "Error processing batch", processBatch)
} else {
    processBatch()  // Unlimited mode: use the service's own transaction
}

// Safe: addError rolls back only the empty outer TX; batch is already committed
if (batchLimitReached) {
    ec.message.addError("Processed ${processedCount} items, limit reached. Remaining items on next run.")
}
```

**Why this works:**
1. `runRequireNew` suspends the service runner's transaction, runs the closure in a new transaction, and commits it
2. The original transaction resumes with no database work done in it
3. `addError()` triggers rollback of the original (empty) transaction — harmless
4. The job is marked as failed → scheduler retries sooner (respects `minRetryTime`)

**When `maxItems == 0`** (unlimited): the closure runs directly in the service's existing transaction — fully backward compatible, no sub-transaction overhead.

## `ec.message` Errors Propagate Across `requireNewTransaction` Boundaries

`requireNewTransaction(true)` creates an independent **transaction**, but `ec.message` is attached to the **ExecutionContext**, which is shared. Errors added by a child service via `ec.message.addError()` persist in the parent context after the child returns — even though the child's transaction committed or rolled back independently.

**Why this matters:** In a ServiceJob scheduler that calls batch services with `requireNewTransaction(true)`, errors from the batch accumulate in the scheduler's `ec.message`. When the scheduler's own transaction tries to commit, these errors mark it as rollback-only — wiping all entity updates made in the scheduler's transaction (progress tracking, state updates, etc.).

**Pattern: Clear errors after each `requireNewTransaction` call**

```groovy
// Call batch in its own transaction
def batchResult = ec.service.sync().name("my.Services.process#Batch")
        .parameters([...])
        .requireNewTransaction(true).call()

// CRITICAL: clear any error messages from batch — they run in their own tx,
// but errors propagate to our context and can taint our transaction
ec.message.clearErrors()

// Now safe to update state in our own transaction
bgState.processedItems = batchResult.processedCount
bgState.update()
```

**When to use `clearErrors()`:**
- After any `requireNewTransaction(true)` call where the child might add errors
- In scheduler/orchestrator services that must persist state regardless of child failures
- In batch loops where one iteration's errors should not affect subsequent iterations

**When NOT to use `clearErrors()`:**
- In normal service calls where errors should propagate naturally
- When the caller needs to inspect and act on the child's errors (read them first, then clear)

## Key Rules
- `ignore-error` does NOT prevent rollback—only clears errors and allows continued execution
- Once rollback-only, nested services won't execute unless they use `force-new`
- Use `force-new + ignore-error` for complete isolation
- Utility services: return status flags via out-parameters, never `addError()`
- Batch processing with post-loop `addError`: isolate batch in `runRequireNew` first
- After `requireNewTransaction` calls: use `ec.message.clearErrors()` if error isolation is needed