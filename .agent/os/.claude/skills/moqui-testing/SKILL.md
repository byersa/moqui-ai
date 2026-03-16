---
name: moqui-testing
description: |
  Moqui Framework testing patterns using Spock framework including test structure, test data creation, service testing, entity testing, and authorization testing.

  Use this skill when:
  - Writing Spock tests for Moqui services (.groovy in test/ directories)
  - Creating test data factories and builders
  - Testing service success and error scenarios
  - Testing entity relationships and constraints
  - Testing with authorization enabled (EntityFilter context)
  - Setting up test transactions and rollback
---

# Moqui Testing Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/testing_patterns.md` | Spock tests, test data creation, service testing, authorization testing |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Testing Framework Guidelines"** - Required configuration, test discovery, Spock setup
- **"### Test Data Management Best Practices"** - Data isolation, builder patterns
- **"### Testing EntityFilter-Enabled Services"** - Authorization testing setup, filter context in tests
- **"### Service Error Handling in Tests"** - Testing error scenarios, message validation
- **"### Sequence ID Management for Tests"** - Avoiding sequence conflicts in test data
- **"## Framework Testing Patterns"** - Integration between Spock and Playwright
- **"### Authentication and Authorization Pattern"** - Testing with different user contexts

### E2E Testing Reference (testing-guide.md)

For Playwright browser testing, see `runtime/component/moqui-agent-os/testing-guide.md`:
- **"### Special Component Testing"** - CKEditor, rich text fields, pressSequentially patterns

## Quick Reference

### Test Class Template
```groovy
class ServiceNameTests extends Specification {
    @Shared ExecutionContext ec
    @Shared ServiceFacade sf

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        sf = ec.service
    }

    def cleanupSpec() { ec.destroy() }

    def setup() { ec.transaction.begin(null) }

    def cleanup() { ec.transaction.rollback() }

    def "service creates valid entity"() {
        when:
        def result = sf.sync().name("create#Entity").parameters([name: "Test"]).call()

        then:
        !sf.isError(result)
        result.entityId != null
    }
}
```

## Key Principles

1. **Transaction Rollback**: Use transaction begin/rollback for test isolation
2. **Clear Messages**: Call `ec.message.clearErrors()` in batch test loops
3. **Authorization Testing**: Call filter context setup service before testing filtered queries