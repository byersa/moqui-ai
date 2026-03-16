# Performance Optimization Patterns

Query tuning, selective loading, locking, and batch operations.

## Select Only Needed Fields

```xml
<!-- GOOD: Select specific fields for large tables -->
<!-- Note: select-field must come AFTER econditions (XSD schema ordering) -->
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
    <select-field field-name="orderId"/>
    <select-field field-name="orderDate"/>
    <select-field field-name="statusId"/>
</entity-find>

<!-- AVOID: Selecting all fields when you only need a few -->
<entity-find entity-name="example.Order" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
</entity-find>
```

**When to use select-field:**
- Large text/binary fields exist but aren't needed
- Fetching lists for dropdowns (only need ID + description)
- High-volume batch operations
- **View-entities with `alias-all`**: When a view-entity uses `alias-all` on multiple member entities, all columns from all joined tables are loaded by default. Adding `select-field` overrides this and fetches only the fields you need, drastically reducing memory usage.

### select-field Overrides alias-all in View-Entities

View-entities that use `alias-all` on multiple member entities can be very expensive when queried without `select-field`. The database loads every column from every joined table into memory per row.

```xml
<!-- BAD: Loads ALL fields from 3 joined tables per row -->
<entity-find entity-name="example.OrderDetailView" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
</entity-find>

<!-- GOOD: Only loads the 2 fields actually used in the loop -->
<entity-find entity-name="example.OrderDetailView" list="orderList">
    <econdition field-name="statusId" value="OrdActive"/>
    <select-field field-name="orderId,externalId"/>
</entity-find>
```

This is especially important in batch jobs that load hundreds or thousands of rows — the memory savings compound per row and can significantly reduce GC pressure.

## Pessimistic Locking (for-update)

```xml
<!-- Lock row for concurrent modification -->
<entity-find-one entity-name="example.Sequence" value-field="seq" for-update="true"/>
<set field="seq.nextValue" from="seq.nextValue + 1"/>
<entity-update value-field="seq"/>

<!-- Lock during batch processing -->
<entity-find entity-name="example.QueueItem" list="itemList" for-update="true" limit="100">
    <econdition field-name="statusId" value="Pending"/>
    <order-by field-name="createdDate"/>
</entity-find>
```

**Use for-update when:**
- Incrementing sequence counters
- Processing queue items (prevent double-processing)
- Updating shared resources
- Implementing optimistic locking fallback

## Pagination

```xml
<!-- Service with pagination support -->
<service verb="find" noun="Orders">
    <in-parameters>
        <parameter name="pageIndex" type="Integer" default="0"/>
        <parameter name="pageSize" type="Integer" default="20"/>
    </in-parameters>
    <out-parameters>
        <parameter name="orderList" type="List"/>
        <parameter name="orderListCount" type="Integer"/>
        <parameter name="orderListPageIndex" type="Integer"/>
        <parameter name="orderListPageSize" type="Integer"/>
        <parameter name="orderListPageMaxIndex" type="Integer"/>
    </out-parameters>
    <actions>
        <entity-find entity-name="example.Order" list="orderList" count="orderListCount"
                     offset="pageIndex * pageSize" limit="pageSize">
            <order-by field-name="orderDate" descending="true"/>
        </entity-find>

        <set field="orderListPageIndex" from="pageIndex"/>
        <set field="orderListPageSize" from="pageSize"/>
        <set field="orderListPageMaxIndex"
             from="((orderListCount as BigDecimal) - 1).divide(pageSize as BigDecimal, 0, java.math.RoundingMode.DOWN) as int"/>
    </actions>
</service>
```

## Batch Processing

```xml
<!-- Process in batches to avoid memory issues -->
<service verb="process" noun="LargeDataSet">
    <in-parameters>
        <parameter name="batchSize" type="Integer" default="200"/>
    </in-parameters>
    <actions>
        <set field="processed" from="0" type="Integer"/>
        <set field="hasMore" from="true" type="Boolean"/>

        <while condition="hasMore">
            <entity-find entity-name="example.PendingItem" list="itemList" limit="batchSize">
                <econdition field-name="statusId" value="Pending"/>
                <order-by field-name="createdDate"/>
            </entity-find>

            <if condition="itemList.isEmpty()">
                <set field="hasMore" from="false"/>
                <continue/>
            </if>

            <iterate list="itemList" entry="item">
                <!-- Process each item in separate transaction -->
                <service-call name="example.ItemServices.process#Item"
                    in-map="[itemId:item.itemId]" transaction="force-new" ignore-error="true"/>
                <set field="processed" from="processed + 1"/>
            </iterate>
        </while>

        <log level="info" message="Processed ${processed} items"/>
    </actions>
</service>
```

### Infinite Loop Prevention for Skipped Records

When a batch job queries records by a condition (e.g., `contentLocation LIKE 'dbresource://%'`) and **skips** some without modifying them, the next batch re-fetches the same records, creating an infinite loop where `skippedCount` grows but `migratedCount` stays at zero.

**Anti-pattern:**
```xml
<!-- Query finds records matching old prefix -->
<entity-find entity-name="Document" list="records" limit="100">
    <econdition field-name="location" operator="like" value="dbresource://%"/>
</entity-find>
<iterate list="records" entry="record">
    <!-- Some records can't be processed (e.g., missing party) -->
    <if condition="!canProcess(record)">
        <set field="skippedCount" from="skippedCount + 1"/>
        <continue/>  <!-- Record unchanged, will be found again next batch! -->
    </if>
</iterate>
```

**Solutions:**
1. **Provide a fallback path** so all records can be processed (preferred):
   ```xml
   <!-- Instead of skipping, use a degraded/fallback approach -->
   <if condition="!partyId">
       <set field="useUnrelatedPath" from="true"/>
       <!-- Process with fallback logic instead of skipping -->
   </if>
   ```

2. **Track processed IDs** to exclude from subsequent queries:
   ```xml
   <entity-find ...>
       <econdition field-name="recordId" operator="not-in" from="processedIds"/>
   </entity-find>
   ```

3. **Detect stalled batches** and stop the job:
   ```groovy
   if (migratedCount == 0 && skippedCount > 0) {
       ec.logger.error("Migration stalled: ${skippedCount} records skipped, none migrated")
       return  // Stop rescheduling
   }
   ```

4. **Progressive offset recovery** — advance past un-processable records using a growing query offset:
   ```xml
   <!-- Orchestrator service calls batch service with growing offset -->
   <set field="queryOffset" from="0"/>
   <set field="consecutiveSkipBatches" from="0"/>

   <while condition="pendingCount &gt; 0 &amp;&amp; batchCount &lt; maxBatches">
       <service-call name="process#Batch"
           in-map="context + [queryOffset: queryOffset]" out-map="result"/>

       <if condition="result.migratedCount &gt; 0"><then>
           <!-- Progress: reset offset (migrated records drop out of query) -->
           <set field="queryOffset" from="0"/>
           <set field="consecutiveSkipBatches" from="0"/>
       </then><else-if condition="result.skippedCount &gt; 0">
           <!-- No progress: advance offset past skipped records -->
           <set field="queryOffset" from="queryOffset + result.skippedCount"/>
           <set field="consecutiveSkipBatches" from="consecutiveSkipBatches + 1"/>
           <if condition="consecutiveSkipBatches &gt;= maxSkipBatches"><break/></if>
       </else-if><else>
           <break/><!-- Nothing to process -->
       </else></if>
   </while>
   ```
   The batch service adds `.offset(queryOffset)` to its entity query. When migrated records use a different URL prefix (e.g., `aws3://` vs `dbresource://`), they drop out of the `LIKE` filter, so offset resets to 0 after progress. The `maxSkipBatches` cap (default 10) prevents scanning the entire table when all records are un-processable. **Always log skip reasons** at the batch level so the cause is visible in production logs.

### Batch Size for Cleanup / deleteAll Operations

`deleteAll()` with `.limit(N)` translates to `SELECT primary_keys LIMIT N` followed by `DELETE WHERE pk IN (...)`. Large batch sizes create enormous IN clauses that cause:
- **High PostgreSQL CPU** from parsing and planning large IN lists
- **JVM memory pressure** from holding large ID lists in memory
- **Long-running transactions** that hold locks

```xml
<!-- BAD: 100,000 row IN clause overwhelms PostgreSQL -->
<service verb="cleanup" noun="OldRecords">
    <in-parameters>
        <parameter name="batchSize" type="Integer" default="100000"/>
    </in-parameters>
    <actions>
        <entity-find ... list="recordList" limit="batchSize"/>
        <set field="recordList.deleteAll()"/>  <!-- DELETE WHERE id IN (100K ids) -->
    </actions>
</service>

<!-- GOOD: Small batches with job re-scheduling -->
<service verb="cleanup" noun="OldRecords">
    <in-parameters>
        <parameter name="batchSize" type="Integer" default="5000"/>
    </in-parameters>
    <!-- ... -->
</service>
```

**Recommended batch sizes for cleanup operations:**
- `deleteAll()` operations: **5,000 max** (avoids IN clause bloat)
- General iterative processing: 100-200 (as documented above)

### While-Loop Memory Accumulation Anti-Pattern

Parent services that loop calling batch services accumulate ExecutionContext state across iterations, causing memory to skyrocket after ~20-25 iterations.

```xml
<!-- BAD: While loop accumulates state, memory grows linearly with iterations -->
<service verb="cleanup" noun="OldRecords">
    <actions>
        <set field="hasMore" from="true"/>
        <while condition="hasMore">
            <service-call name="cleanup#OldRecordsBatch" out-map="batchResult"
                          transaction="force-new"/>
            <set field="hasMore" from="batchResult.deletedCount > 0"/>
            <!-- Each iteration adds to ExecutionContext: entity cache, query plans, etc. -->
        </while>
    </actions>
</service>

<!-- GOOD: Single-batch-per-invocation, let the job scheduler handle re-invocation -->
<service verb="cleanup" noun="OldRecordsBatch">
    <actions>
        <!-- Process one batch -->
        <entity-find ... list="recordList" limit="batchSize"/>
        <if condition="recordList.isEmpty()"><return/></if>
        <set field="recordList.deleteAll()"/>
        <!-- Return error to trigger job re-scheduling via minRetryTime -->
        <return error="true" message="Cleanup in progress, ${recordList.size()} deleted"/>
    </actions>
</service>
```

**Key points:**
- Each job invocation gets a **fresh ExecutionContext** (no state accumulation)
- The `minRetryTime` on the ServiceJob definition (e.g., `minRetryTime="60"`) spaces out batches, giving PostgreSQL breathing room
- Use `return error="true"` to trigger re-scheduling when more records remain
- Use `return` (success) when the job is complete (no more records)

### Alternative: `maxBatches` Cap for While-Loop Patterns

When refactoring to single-batch-per-invocation isn't practical (e.g., existing services already use the while-loop pattern), add a `maxBatches` parameter to cap the number of iterations per job run. This bounds both runtime and memory accumulation.

```xml
<service verb="cleanup" noun="OldRecords" transaction-timeout="600">
    <in-parameters>
        <parameter name="retentionDays" type="Integer" default="30"/>
        <parameter name="batchSize" type="Integer" default="5000"/>
        <parameter name="maxBatches" type="Integer" default="20"/>
    </in-parameters>
    <actions>
        <set field="batchRunCount" from="0"/>
        <set field="totalDeleted" from="0L"/>
        <!-- Count once upfront -->
        <script>removePending = ec.entity.find("...").condition(...).disableAuthz().count()</script>

        <while condition="removePending &gt; totalDeleted &amp;&amp; batchRunCount &lt; maxBatches">
            <set field="batchRunCount" from="batchRunCount+1"/>
            <service-call name="cleanup#OldRecordsBatch" in-map="context"
                          out-map="batchResult" transaction="force-new"/>
            <!-- Zero-progress guard: prevent infinite loop if batch deletes nothing -->
            <if condition="batchResult.batchDeleted == 0">
                <log level="warn" message="Batch ${batchRunCount} deleted 0 records, stopping"/>
                <break/>
            </if>
            <set field="totalDeleted" from="totalDeleted + batchResult.batchDeleted"/>
        </while>

        <!-- Distinguish "paused at limit" vs "fully completed" -->
        <if condition="removePending &gt; totalDeleted">
            <log message="Paused after ${batchRunCount} batches: ${totalDeleted}/${removePending}. Remaining cleaned next run."/>
            <else><log message="Completed: deleted ${totalDeleted} records"/></else>
        </if>
    </actions>
</service>
```

**Key points:**
- Default `maxBatches=20` with `batchSize=5000` = **100K records per job run** — bounded and predictable
- **Zero-progress guard**: If a batch deletes 0 records, `<break/>` prevents an infinite loop (can happen with concurrent modifications or data inconsistencies)
- Remaining records are cleaned on the next daily job run — large backlogs drain incrementally over days
- The `maxBatches` value can be overridden via ServiceJob parameters for specific environments

## Transaction Strategy for Long-Running Batch Jobs

When a batch job iterates over many records with external API calls or slow operations per item, use `transaction="force-new"` on each item's service call. This prevents a single failure from rolling back all previous work and allows the main transaction to remain lightweight.

```xml
<iterate list="itemList" entry="item">
    <!-- Each item processed in its own transaction -->
    <service-call name="example.Services.process#Item"
                  in-map="[itemId:item.itemId]"
                  transaction="force-new" ignore-error="true"
                  out-map="result" out-map-add-to-existing="false"/>
    <!-- out-map-add-to-existing="false" prevents accumulation of previous iteration data -->
</iterate>
```

**Key considerations:**
- **`transaction="force-new"`**: Isolates each item so failures don't cascade. Essential when calling external APIs with unpredictable latency/errors.
- **`ignore-error="true"`**: Prevents one failed item from aborting the entire batch. Track success/fail counts and report at the end.
- **`out-map-add-to-existing="false"`**: Each iteration gets a clean output map instead of accumulating keys from all previous iterations — reduces memory pressure.
- **Rate limiting with `Thread.sleep()`**: When calling rate-limited external APIs, add sleep between iterations. Note that the main transaction remains open during sleep, so set `transactionTimeout` accordingly on the job definition.
- **Transaction timeout**: For jobs that may run for hours, set `transactionTimeout` on the `ServiceJob` definition (e.g., `transactionTimeout="10800"` for 3 hours).

## Limiting Results

```xml
<!-- Always limit when you only need a few -->
<entity-find entity-name="example.Order" list="recentOrders" limit="10">
    <econdition field-name="customerId" from="customerId"/>
    <order-by field-name="orderDate" descending="true"/>
</entity-find>

<!-- Limit batch job processing -->
<entity-find entity-name="example.EmailQueue" list="pendingList" limit="maxBatchSize">
    <econdition field-name="statusId" value="ES_READY"/>
</entity-find>
```

## Caching Reference Data

```xml
<!-- Cache lookup/reference data -->
<entity-find entity-name="moqui.basic.Enumeration" list="statusList" cache="true">
    <econdition field-name="enumTypeId" value="OrderStatus"/>
    <order-by field-name="sequenceNum"/>
</entity-find>

<!-- Cache single lookups -->
<entity-find-one entity-name="moqui.basic.StatusItem" value-field="status" cache="true"/>
```

**Cache only:**
- Enumeration/type tables
- Configuration data
- Rarely changing reference data

**Never cache:**
- Transactional data
- User-specific data
- Frequently updated data

## Avoiding N+1 Queries

```xml
<!-- BAD: N+1 query pattern -->
<entity-find entity-name="example.Order" list="orderList"/>
<iterate list="orderList" entry="order">
    <entity-find-one entity-name="example.Customer" value-field="customer">
        <field-map field-name="customerId" from="order.customerId"/>
    </entity-find-one>
</iterate>

<!-- GOOD: Use view entity -->
<entity-find entity-name="example.OrderAndCustomer" list="orderList"/>
```

## Performance Checklist

| Scenario | Solution |
|----------|----------|
| Large tables | Use `select-field` |
| Concurrent updates | Use `for-update` |
| Large result sets | Use pagination |
| Memory constraints | Process in batches |
| Cleanup/deleteAll | Use batchSize ≤ 5,000 |
| Multi-batch parent | Single-batch + job re-scheduling |
| Reference data | Use `cache="true"` |
| Related data | Use view entities |
| Unknown result size | Always use `limit` |