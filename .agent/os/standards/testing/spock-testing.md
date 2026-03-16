# Spock Testing Standards

### Test Structure

**Specification Class**:
```groovy
class EntityServiceTests extends Specification {
    protected static ExecutionContext ec

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("testuser", "password")
        ec.service.sync().name("setup#FilterContext").call()
    }

    def cleanupSpec() {
        ec.destroy()
    }

    def setup() {
        ec.artifactExecution.disableAuthz()
        ec.transaction.begin(null)
    }

    def cleanup() {
        ec.transaction.rollback(null, null, null)
        ec.artifactExecution.enableAuthz()
    }
}
```

### Test Naming Convention
- **Format**: `"NNN - descriptive test name"`
- **Sequential Numbering**: Use 001, 002, etc. for predictable execution order
- **Descriptive Names**: Clearly describe what's being tested

```groovy
def "001 - create order with valid data"() { ... }
def "002 - update order status to shipped"() { ... }
def "010 - entity filter restricts data to user organization"() { ... }
```

### Transaction Handling
- **Automatic Rollback**: Use `setup()` to begin transaction, `cleanup()` to rollback
- **Test Isolation**: Each test runs in isolated transaction
- **No Cleanup Needed**: Rollback ensures no test data persists

### Entity Operations (CRITICAL)
**Always use auto-services, NEVER use makeValue**:
```groovy
// WRONG - Never use this pattern
def value = ec.entity.makeValue("EntityName", [field1: "value"])
value.create()

// CORRECT - Always use auto-services
def result = ec.service.sync().name("create#EntityName")
    .parameters([field1: "value"]).call()
```

### Authorization Testing (EntityFilters)
**Test with authorization ENABLED**:
```groovy
def "010 - entity filter restricts data to user organization"() {
    given: "authenticated user with organization access"
    ec.user.loginUser('testuser', 'password')
    ec.service.sync().name("setup#FilterContext").call()

    when: "querying filtered entity"
    def results = ec.entity.find("Order").list()

    then: "only user's organization orders returned"
    results.size() > 0
    results.every { it.ownerPartyId in ec.user.context.filterOrgIds }
}
```

### User Context Lifecycle in Tests

When testing services that depend on the logged-in user, use `loginUser` /
`logoutUser` / `loginAnonymousIfNoUser` to manage user context:

```groovy
void "service rejects unauthorized user"() {
    given: "a user without required capability"
    ec.user.loginUser("limited_user", "moqui")
    ec.message.clearAll()

    when: "calling restricted service"
    ec.service.sync().name("example.Services.take#RestrictedAction")
            .parameters([itemId: itemId]).call()

    then: "service returns an error"
    ec.message.hasError()

    cleanup:
    // CRITICAL: restore anonymous context for subsequent tests
    ec.user.logoutUser()
    ec.user.loginAnonymousIfNoUser()
    ec.artifactExecution.disableAuthz()
    ec.message.clearAll()
}
```

**Rules**:
- **Always restore context in `cleanup:`** block (runs even if test fails)
- **`logoutUser()` first**, then `loginAnonymousIfNoUser()` to get a valid anonymous session
- **Re-disable authz** after login change (`disableAuthz()` is lost on logout)
- **Clear messages** to prevent error state leaking to next test

### Data-Driven Testing
```groovy
@Unroll
def "#testNum - create entity with #scenario"() {
    given: "entity setup"
    def params = [field1: value1, field2: value2]

    when: "creating entity"
    def result = ec.service.sync().name("create#Entity")
        .parameters(params).call()

    then: "entity created successfully"
    result.entityId != null

    where:
    testNum | scenario      | value1  | value2
    "001"   | "valid data"  | "test"  | 100
    "002"   | "edge case"   | ""      | 0
}
```

### Safe Access Patterns
```groovy
// WRONG - May throw NPE
def value = someList[0]

// CORRECT - Null-safe access
def value = someList?.getAt(0)
```

### Test Data Types
- **{component}-test**: Chile-specific test data
- **{project}-test**: Generic test data
- Configure in `entity_empty_db_load` Gradle settings

### What to Test
- **Core User Flows**: Critical paths and primary workflows
- **Business Logic**: Complex calculations and validations
- **Authorization**: EntityFilter behavior with different contexts
- **Integration Points**: Service interactions and API endpoints

### What NOT to Test During Development
- Every intermediate step (test at completion points)
- Non-critical utilities
- Edge cases (defer to dedicated testing phases)

### Testing Pyramid Ratios

| Level | Ratio | Characteristics |
|-------|-------|-----------------|
| Unit Tests | 70% | Fast, isolated, focused |
| Integration Tests | 20% | Component interactions |
| E2E Tests | 10% | Complete user scenarios |

### FIRST Principles

| Principle | Meaning |
|-----------|---------|
| **F**ast | Execute quickly |
| **I**solated | No dependency on other tests |
| **R**epeatable | Consistent results |
| **S**elf-Validating | Clear pass/fail |
| **T**imely | Written with code |

### Test Organization Best Practices

```groovy
class OrderServiceTests extends Specification {
    // Shared setup - runs once
    def setupSpec() { ... }
    def cleanupSpec() { ... }

    // Per-test setup - runs before each test
    def setup() { ... }
    def cleanup() { ... }

    // Tests grouped by feature
    def "001 - create order with valid data"() { ... }
    def "002 - create order validates required fields"() { ... }

    // Error scenarios
    def "010 - reject order with invalid customer"() { ... }
}
```

### Test Data Management

**Use transaction rollback for isolation:**
```groovy
def setup() {
    ec.transaction.begin(null)
}

def cleanup() {
    ec.transaction.rollback(null, null, null)
}
```

**Test data factory pattern:**
```groovy
def createTestOrder(Map overrides = [:]) {
    def defaults = [statusId: 'OrdDraft', orderDate: new Date()]
    def params = defaults + overrides
    return ec.service.sync().name("create#Order")
        .parameters(params).call()
}
```

### Integration Test Patterns

```groovy
def "complete order workflow executes correctly"() {
    given: "test customer and product"
    def customer = createTestCustomer()
    def product = createTestProduct()

    when: "creating and fulfilling order"
    def order = createTestOrder([customerId: customer.customerId])
    ec.service.sync().name("order.fulfill#Order")
        .parameter("orderId", order.orderId).call()

    then: "order is fulfilled"
    def updated = ec.entity.find("Order")
        .condition("orderId", order.orderId).one()
    updated.statusId == "OrdFulfilled"
}
```