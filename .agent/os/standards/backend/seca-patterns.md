# SECA Patterns (Service Event Condition Actions)

Trigger actions automatically when services are called.

## File Naming Convention

```
service/
├── OrderServices.xml      # Service definitions
└── Order.secas.xml        # SECAs for order services
```

## Basic SECA Structure

```xml
<secas xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-eca-3.xsd">

    <seca id="UniqueId" service="update#example.Order" when="post-service">
        <description>Trigger action after order update</description>
        <condition><expression>statusChanged &amp;&amp; statusId == 'OrdApproved'</expression></condition>
        <actions>
            <service-call name="example.NotificationServices.send#OrderApprovalNotice"
                in-map="[orderId:orderId]"/>
        </actions>
    </seca>

</secas>
```

## When Attribute Options

| Value | Description |
|-------|-------------|
| `pre-validate` | Before input validation |
| `pre-auth` | Before authorization check |
| `pre-service` | Before service execution |
| `post-service` | After service execution (most common) |
| `post-commit` | After transaction commit |
| `tx-commit` | On transaction commit (for notifications) |

## Common Condition Patterns

```xml
<!-- Status change detection -->
<condition><expression>statusChanged &amp;&amp; statusId == 'OrdApproved'</expression></condition>

<!-- From specific status -->
<condition><expression>statusChanged &amp;&amp; oldStatusId == 'OrdDraft' &amp;&amp; statusId == 'OrdSubmitted'</expression></condition>

<!-- Multiple valid statuses -->
<condition><expression>statusChanged &amp;&amp; statusId in ['OrdApproved', 'OrdCompleted']</expression></condition>

<!-- Field value check -->
<condition><expression>amount &gt; 1000</expression></condition>

<!-- Combined conditions -->
<condition><expression>statusChanged &amp;&amp; statusId == 'OrdApproved' &amp;&amp; orderType == 'Sales'</expression></condition>
```

## Priority for Multiple SECAs

```xml
<!-- Lower priority runs first -->
<seca id="ValidateFirst" service="update#Order" when="post-service" priority="1">
    <actions><!-- Validation logic --></actions>
</seca>

<seca id="ProcessSecond" service="update#Order" when="post-service" priority="2">
    <actions><!-- Processing logic --></actions>
</seca>

<seca id="NotifyLast" service="update#Order" when="post-service" priority="10">
    <actions><!-- Notification logic --></actions>
</seca>
```

## Error Handling

```xml
<!-- Don't run if service had errors -->
<seca id="OnlyOnSuccess" service="update#Order" when="post-service" run-on-error="false">

<!-- Return error to stop processing -->
<seca id="ValidationSeca" service="update#Order" when="post-service">
    <condition><expression>amount &lt; 0</expression></condition>
    <actions>
        <return error="true" message="Amount cannot be negative"/>
    </actions>
</seca>
```

## Notifications on Commit

```xml
<!-- Send email only after transaction commits successfully -->
<seca id="SendApprovalEmail" service="update#Order" when="tx-commit">
    <condition><expression>statusChanged &amp;&amp; statusId == 'OrdApproved'</expression></condition>
    <actions>
        <service-call name="org.moqui.impl.EmailServices.send#EmailTemplate"
            in-map="[emailTemplateId:'ORDER_APPROVED', toAddresses:customerEmail,
                    bodyParameters:[orderId:orderId]]"/>
    </actions>
</seca>
```

## Available Context Variables

In SECA actions, you have access to:
- All service input parameters
- All service output parameters
- `statusChanged` - Boolean (for entity-auto update services)
- `oldStatusId` - Previous status value
- `ec` - Execution context

## Batch vs Per-Item Triggering

**CRITICAL**: When attaching SECAs, understand the transaction boundaries to avoid unintended multiple triggers.

### The Problem

When a SECA uses `when="post-commit"` on a per-item service that's called with `transaction="force-new"`, the SECA fires once **per item**, not once per batch:

```
poll#EmailServer (batch orchestrator)
  └─► EMECA triggers process#IncomingDteMessage (force-new) ─► SECA fires ─► Item 1
  └─► EMECA triggers process#IncomingDteMessage (force-new) ─► SECA fires ─► Item 2
  └─► EMECA triggers process#IncomingDteMessage (force-new) ─► SECA fires ─► Item 3
```

Each `force-new` creates a separate transaction, so `post-commit` fires three times.

### The Anti-Pattern

```xml
<!-- ❌ BAD: SECA on per-item service fires multiple times per batch -->
<seca id="ProcessAfterEmail" service="example.process#IncomingMessage" when="post-commit">
    <actions>
        <!-- This runs once PER EMAIL, not once per batch! -->
        <service-call name="example.process#AllNewMessages" transaction="force-new"/>
    </actions>
</seca>
```

### The Solution

Attach the SECA to the batch orchestrator service instead, with a condition to filter by context:

```xml
<!-- ✅ GOOD: SECA on batch service fires once per run -->
<seca id="ProcessAfterEmailBatch" service="org.moqui.impl.EmailServices.poll#EmailServer" when="post-commit">
    <condition><expression>emailServerId == 'myEmailServer'</expression></condition>
    <actions>
        <!-- This runs once after ALL emails are processed -->
        <service-call name="example.process#AllNewMessages" transaction="force-new"/>
    </actions>
</seca>
```

### When to Use Each Pattern

| Trigger Point | Use Case |
|--------------|----------|
| **Per-item service** | Actions that must run for each item (audit log, item-specific notification) |
| **Batch orchestrator** | Actions that should run once after all items (summary report, batch cleanup, aggregate processing) |

### Common Batch Orchestrators

| Domain | Batch Service | Per-Item Service |
|--------|---------------|------------------|
| Email processing | `poll#EmailServer` | `process#IncomingMessage` |
| Data import | `import#BatchFile` | `process#ImportRow` |
| Scheduled jobs | The scheduled service itself | Services called in a loop |

### Key Insight

`post-commit` fires after **that service's transaction** commits, not after "all related work" completes. When services use `transaction="force-new"`, each gets its own commit point.

## Best Practices

1. **Use descriptive IDs** - `InvoiceFinalizedCreatePayment` not `Seca1`
2. **Include description** - Document what the SECA does
3. **Use `tx-commit` for notifications** - Ensures data is persisted
4. **Set `run-on-error="false"`** - Unless you need cleanup on failure
5. **Use priority** - When order matters between SECAs
6. **Keep actions simple** - Delegate to dedicated services
7. **Consider trigger granularity** - Attach to batch orchestrator for once-per-batch actions