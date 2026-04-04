# GeneralRunner REST Service Diagnosis (2026-04-03)
## Universal Task Execution Protocol (UTEP) Stabilization

### Problem Statement
The Moqui Agent OS component `moquiai` is implementing a **GeneralRunner** service to facilitate stable, non-blocking execution of Moqui services via a unified REST endpoint. While the underlying service (`moquiai.AppServices.get#AvailableApps`) executes correctly and populates its results into the context (confirmed by server logs), the REST response from the `GeneralRunner` (`POST /rest/s1/moquiai/execute`) returns an empty JSON object `{ }`.

### System Configuration

#### 1. REST Endpoint (`moquiai.rest.xml`)
The endpoint is mapped to the `GeneralRunner` service:
```xml
<resource name="execute">
    <method type="post">
        <service name="moquiai.AppServices.execute#GeneralRunner"/>
    </method>
</resource>
```

#### 2. Service Definition (`AppServices.xml`)
The `GeneralRunner` service is defined as follows:
```xml
<service verb="execute" noun="GeneralRunner" type="inline" require-authentication="anonymous-all">
    <in-parameters>
        <parameter name="serviceName" required="true"/>
        <parameter name="parameters" type="Map"/>
    </in-parameters>
    <out-parameters>
        <parameter name="*" type="any"/>
    </out-parameters>
    <actions>
        <script location="component://moqui-ai/service/moquiai/GeneralRunner.groovy"/>
    </actions>
</service>
```

#### 3. Execution Logic (`GeneralRunner.groovy`)
The logic performs a security check and then executes the requested service:
```groovy
import org.moqui.context.ExecutionContext
ExecutionContext ec = context.ec

// Security check (only nursinghome and moquiai nouns allowed)
def parts = serviceName.split('\\.')
def prefix = parts[0]
if (prefix != "nursinghome" && prefix != "moquiai") {
    ec.message.addError("Access denied")
    return
}

// Execute the service
def serviceResult = ec.service.sync().name(serviceName).parameters(parameters ?: [:]).call()

// Return the result
return serviceResult
```

### Observed Behavior
1.  **Server Logs**: Correctly show the execution of `GeneralRunner` and the result of the called service:
    `Service moquiai.AppServices.get#AvailableApps result: [apps:[[label:Aitree, value:aitree], ...]]`
2.  **REST Response**:
    `curl -X POST -d '{"serviceName": "moquiai.AppServices.get#AvailableApps"}' ...`
    Result: `{ }`
3.  **Direct Execution** (`run-service.sh`): Passes the service name to the REST endpoint.
    Result: `{ }` or sometimes `{"result": {}}` depending on previous XML iterations.

### Failed Hypotheses
- **Parameter Mapping**: We tried using `context.putAll(serviceResult)` in an inline script. This also resulted in an empty response.
- **REST Method**: Changed `POST` to `post` in `rest.xml` (resolved `405` error).
- **Service Type**: Changed `AvailableApps` to `type="inline"` (resolved `400` error).

### Current Status & Questions
The current `GeneralRunner` setup is `type="inline"` pointing to a Groovy script location.
1.  **Result Harvesting**: Does an `inline` service that points to a script location correctly harvest the `return` statement from the Groovy script into its own `out-parameters`, especially when using the wildcard `*`?
2.  **REST Serialisation**: Why are the out-parameters of the `GeneralRunner` (populated via `*`) not appearing in the REST JSON body?
3.  **Context vs Return**: Should `GeneralRunner` be `type="script"` instead of `type="inline"` to correctly handle the result map from the called service?

---
*Created by Antigravity (AI Orchestrator)*
