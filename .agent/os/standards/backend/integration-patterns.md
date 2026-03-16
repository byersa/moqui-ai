# Integration Patterns

External API calls, message queuing, retry handling, and transaction isolation.

## RestClient Usage

```groovy
// Basic pattern
org.moqui.util.RestClient restClient = ec.service.rest()
    .method(org.moqui.util.RestClient.POST)
    .uri(url)
    .addHeader("Content-Type", "application/json")
    .text(jsonBody)
    .basicAuth(username, password)

org.moqui.util.RestClient.RestResponse response = restClient.call()
if (response.statusCode < 200 || response.statusCode >= 300) {
    ec.message.addError("API error (${response.statusCode}): ${response.text()}")
    return
}
def result = response.jsonObject()

// Quick pattern for simple calls
def data = ec.service.rest().method("get").uri(url).call().checkError().jsonObject()
```

## Transaction Isolation for External Calls

**Always use `transaction="force-new"` for external API calls.**

```xml
<!-- Status update before call (isolated TX) -->
<service-call name="update#Entity" transaction="force-new"
    in-map="[id:id, statusId:'Processing']"/>

<!-- External call (isolated TX) -->
<service-call name="send#Message" transaction="force-new"
    in-map="[id:id]" out-map="sendOut"/>

<!-- Status update after (isolated TX) -->
<service-call name="update#Entity" transaction="force-new"
    in-map="[id:id, statusId:'Completed']"/>
```

**Why**: If external call succeeds but later code fails, you need to know the call happened.

## Retry Patterns

### Application-Level Retry (Recommended for Critical Operations)

```xml
<entity-find entity-name="OutboundMessage" list="messageList" limit="200">
    <econdition field-name="statusId" value="MsgProduced"/>
    <econdition field-name="lastAttemptDate" operator="less" from="retryTimestamp" or-null="true"/>
    <order-by field-name="initDate"/>
</entity-find>
<iterate list="messageList" entry="msg">
    <if condition="msg.failCount < retryLimit">
        <service-call name="send#Message" in-map="[messageId:msg.messageId]" async="true"/>
    <else>
        <!-- Move to error status after limit -->
        <service-call name="update#Entity" transaction="force-new"
            in-map="[messageId:msg.messageId, statusId:'MsgError']"/>
    </else>
    </if>
</iterate>
```

Key fields: `failCount`, `lastAttemptDate`, `retryLimit` (default: 24), `retryMinutes` (default: 60)

### RestClient Built-in Retry

```groovy
// Retry with exponential backoff (2s initial, 5 max retries)
ec.service.rest().uri(url).retry().call()

// Custom retry settings
ec.service.rest().uri(url).retry(3.0F, 10).call()  // 3s initial, 10 retries

// Retry on timeout
ec.service.rest().uri(url).timeoutRetry(true).call()
```

## Timeout Handling

```groovy
// Default: 30 seconds
// For long operations:
ec.service.rest().uri(url).timeout(120).call()  // 2 minutes

// For quick health checks:
ec.service.rest().uri(url).timeout(5).call()    // 5 seconds
```

For service-level timeouts:
```xml
<service verb="consume" noun="Message" transaction-timeout="1800">
    <!-- 30 minute timeout for long-running operations -->
</service>
```

## Error Logging Pattern

```xml
<if condition="ec.message.hasError()"><then>
    <set field="errorText" from="ec.message.getErrorsString()"/>
    <script>ec.message.clearErrors()</script>

    <!-- Update status (isolated TX) -->
    <service-call name="update#Entity" transaction="force-new"
        in-map="[id:id, statusId:initialStatusId, lastAttemptDate:nowDate,
                failCount:((entity.failCount ?: 0) + 1)]"/>

    <!-- Log error (isolated TX) -->
    <service-call name="create#EntityError" transaction="force-new"
        in-map="[id:id, errorDate:nowDate, errorText:errorText]"/>
</then></if>
```

## SystemMessage Pattern

For reliable async messaging, use the framework's SystemMessage infrastructure:
- Status machine: Produced → Sending → Sent (or Error)
- Automatic retry with configurable limits
- Error tracking per message
- Configurable send/receive/consume services per type

## Key Rules

1. **Isolate external calls** - Always `transaction="force-new"`
2. **Track attempts** - Store `lastAttemptDate` and `failCount`
3. **Clear errors before continuing** - `ec.message.clearErrors()` after logging
4. **Use async for sends** - `async="true"` for non-blocking operations
5. **Set appropriate timeouts** - Match expected response times
6. **Log errors with context** - Include message ID, status, timestamp