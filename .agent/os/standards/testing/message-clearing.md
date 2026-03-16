# Test Message Clearing

Clear execution context messages between tests to prevent pollution.

## Problem
- Previous test errors leak into `ec.message.hasError()` checks
- Messages from one test pollute assertions in another
- False positives/negatives in test results

## Solution
Call `ec.message.clearAll()` at the start of each test:

```groovy
def setup() {
    ec.message.clearAll()  // CRITICAL: First line of setup
    ec.artifactExecution.disableAuthz()
    ec.transaction.begin(null)
}
```

## Order Matters
1. Clear messages (prevents pollution)
2. Disable authz (allow test operations)
3. Begin transaction (isolate database changes)

## What Gets Cleared
- Error messages (`ec.message.errors`)
- Warning messages
- Info messages
- Validation errors

## When to Use
- **Always** in base test class setup
- **Always** before assertions that check `hasError()`
- After service calls if checking subsequent message state