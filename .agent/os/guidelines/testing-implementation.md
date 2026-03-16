# Moqui Testing Implementation Guide

This guide documents critical lessons learned from implementing tests in Moqui Framework projects, designed to accelerate future test development and minimize rework.

## Critical Success Factors

### 1. User Authentication Strategy

**Problem**: Test user authentication is the #1 source of test failures and rework. Services require authenticated users but tests may fail due to authorization restrictions.

**Solution**: Follow the mantle-usl pattern: authenticate user + disable authorization for test service calls.

```groovy
// ✅ CORRECT: Framework pattern from mantle-usl tests
def setupSpec() {
    ec = Moqui.getExecutionContext()
    ec.user.loginUser("john.doe", "moqui")  // Authenticate with framework user
    // Or: ec.user.loginUser("joe@public.com", "moqui")
}

def setup() {
    ec.artifactExecution.disableAuthz()  // Disable authorization for test calls
}

def cleanup() {
    ec.artifactExecution.enableAuthz()   // Re-enable authorization after test
}
```

**Why**: 
- **Authentication ≠ Authorization**: Services need authenticated users, but not authorization checks in tests
- **Framework users** (john.doe, joe@public.com) are pre-configured with proper authentication
- **disableAuthz()** allows test access to services without complex permission setup
- This matches the proven pattern used throughout mantle-usl tests
- **Avoid** creating custom authorization records - too complex for testing

### 2. Data Loading Requirements

**Problem**: Tests fail when expected seed data isn't loaded.

**Automatic Data Loading via build.gradle (Preferred)**:

Each component's `build.gradle` should configure `entity_empty_db_load` in the test block:
```gradle
test {
    // ... other config ...
    systemProperty 'entity_empty_db_load', 'seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component}-test'
}
```

**How it works**:
- When `moqui.init.static=true` is set, tests use a fresh H2 in-memory database
- Moqui automatically loads the data types specified in `entity_empty_db_load` when the database is empty
- No need for manual `cleanDb load -Ptypes=...` commands

**Running tests**:
```bash
# Simple - data types are loaded automatically from build.gradle
./gradlew :runtime:component:ComponentName:test

# Or with explicit data loading (only if overriding build.gradle config)
./gradlew cleanDb load :runtime:component:ComponentName:test \
  -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component}-test
```

**Standardized Data Type Pattern**:
```
seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test,{component-name}-test
```
- `seed,seed-initial` - Framework core data
- `{l10n}-install` - Chilean localization (**NOT** `install`)
- `{project}-demo` - Project demo data (**NOT** `demo` - incompatible with {l10n}-install)
- `{component}-test` - {localization-component} test dependency (always included)
- `{project}-test` - Shared project test users
- `{component-name}-test` - Component-specific (harmless if no files exist)

**Test Data Strategy**:
```groovy
def setupSpec() {
    ec = Moqui.getExecutionContext()
    
    // Option 1: Use existing framework demo data
    ec.user.loginUser("john.doe", "moqui")
    
    // Option 2: Create test data programmatically with authz disabled
    ec.artifactExecution.disableAuthz()
    try {
        // Create test data here
    } finally {
        ec.artifactExecution.enableAuthz()
    }
}
```

### 3. Service Error Handling Patterns

**Problem**: Services return errors differently based on validation vs execution failures. Additionally, clearing errors immediately after checking them hides valuable debugging information when tests fail.

**CRITICAL: Error Clearing Strategy**:
- **✅ DO**: Clear errors only in `setup()` method for clean test initialization
- **✅ DO**: Clear errors only in `cleanup()` method when errors are expected in the test
- **❌ DON'T**: Clear errors immediately after checking them in test assertions
- **❌ DON'T**: Hide error information that helps with debugging test failures

**Pattern for Robust Error Checking**:
```groovy
def setup() {
    ec.artifactExecution.disableAuthz()
    ec.message.clearErrors()  // ✅ Clear at start for clean state
}

def cleanup() {
    ec.artifactExecution.enableAuthz()
    // ✅ Only clear errors here if test expects errors
    // For normal successful tests, leave errors visible for debugging
}

def "test service success case"() {
    when:
    Map result = ec.service.sync().name("service.name")
        .parameters([param: "value"])
        .call()

    then:
    !ec.message.hasError()  // ✅ Check errors without clearing
    result?.errorMessage == null
    result.expectedField == "expectedValue"
}

def "test service validation failure"() {
    when:
    Map result = ec.service.sync().name("service.name")
        .parameters([invalidParam: "badValue"])
        .call()

    then:
    ec.message.hasError() || result?.errorMessage != null
    // Specific error assertions here

    cleanup:
    ec.message.clearErrors()  // ✅ Clear only when errors are expected
}
```

**Benefits of This Approach**:
- **Debugging**: Errors remain visible in test output when tests fail unexpectedly
- **Isolation**: Each test starts with clean error state via `setup()`
- **Clarity**: Explicit error clearing only when errors are expected
- **Maintenance**: Easier to diagnose test failures and service issues

### 4. Test Suite Organization

**Problem**: Running individual test files causes isolation issues and resource conflicts.

**Solution**: Use JUnit 5 Suite Pattern
```groovy
// MoquiComponentSuite.groovy
@Suite
@SelectClasses([
    CategoryA.TestClass1.class,
    CategoryB.TestClass2.class
])
class MoquiComponentSuite {
    @AfterAll
    static void destroyMoqui() {
        Moqui.destroyActiveExecutionContextFactory()
    }
}
```

**build.gradle Configuration**:
```gradle
test {
    useJUnitPlatform()
    maxParallelForks 1  // Critical: Single-threaded execution
    include '**/MoquiComponentSuite.class'  // Suite-only pattern
    
    systemProperty 'moqui.runtime', moquiDir.absolutePath + '/runtime'
    systemProperty 'moqui.conf', 'conf/MoquiDevConf.xml'
    systemProperty 'moqui.init.static', 'true'
}
```

### 5. Sequence ID Management

**Problem**: Test data conflicts with production sequences.

**Solution**: Use temporary sequence ranges
```groovy
def setupSpec() {
    // Reserve test-specific ID ranges
    ec.entity.tempSetSequencedIdPrimary("entity.name", 58000, 10)
}

def cleanupSpec() {
    // Reset sequences
    ec.entity.tempResetSequencedIdPrimary("entity.name")
}
```

### 6. Test Data Cleanup Strategy

**Problem**: Failed cleanup leaves orphan data affecting subsequent runs.

**Important Note**: Moqui tests typically run without transactions in place, so transaction rollback handling is not needed in cleanup operations.

**Robust Cleanup Pattern**:
```groovy
@Shared List<String> createdEntityIds = []

def "test that creates data"() {
    when:
    Map result = createSomething()
    if (result.entityId) createdEntityIds.add(result.entityId)
    
    then:
    // assertions
}

def cleanupSpec() {
    createdEntityIds.each { id ->
        try {
            ec.service.sync().name("delete#EntityName")
                .parameters([entityId: id])
                .disableAuthz().call()
        } catch (Exception e) {
            logger.warn("Cleanup failed for ${id}: ${e.message}")
        }
    }
}
```

**Recommended Cleanup Pattern**:
Since tests generally run without transactions, cleanup can be straightforward. **Prefer using auto-delete services over direct entity deletion** to avoid exceptions stopping cleanup execution:

```groovy
def cleanup() {
    // Clean up test data using delete services (preferred)
    try {
        createdEntityIds.each { id ->
            ec.service.sync().name("delete#EntityName")
                .parameters([entityId: id])
                .disableAuthz().call()
        }
    } catch (Exception e) {
        logger.warn("Cleanup failed: ${e.message}")
    }
}
```

**Why Use Delete Services:**
- **Failure Resilience**: Service failures don't throw exceptions that stop cleanup execution
- **Business Logic**: Delete services handle cascading deletes and business rules properly
- **Consistency**: Same deletion logic used in production code
- **Authorization**: Can disable authorization specifically for cleanup operations

## Common Pitfalls and Solutions

### Pitfall 1: Service Not Found
**Symptom**: "Service not found" errors
**Cause**: Component not properly loaded
**Solution**: Verify component dependencies in component.xml

### Pitfall 2: Authorization Failures
**Symptom**: "User does not have permission" errors
**Solution**: Add permission to user

### Pitfall 3: Transaction Deadlocks
**Symptom**: Tests timeout or deadlock
**Solution**: 
- Use unique database/transaction resources per component
- Set `maxParallelForks 1` in build.gradle
- Use unique transaction log filenames

### Pitfall 4: Validation vs Execution Errors
**Symptom**: Tests expect errorMessage but get null
**Cause**: Validation errors go to message context, not return map
**Solution**: Check both `ec.message.hasError()` and `result?.errorMessage` when the service returns that parameter

### Pitfall 5: Incorrect Entity Deletion Syntax
**Symptom**: "No such method: ec.entity.delete()" or similar method not found errors
**Cause**: Using non-existent `ec.entity.delete(entity)` method
**Solution**: Call `delete()` method directly on the entity value
```groovy
// ❌ INCORRECT - This method doesn't exist
ec.entity.delete(entityValue)

// ✅ CORRECT - Call delete() on the entity value directly
entityValue.delete()

// ✅ CORRECT - Example in cleanup
def party = ec.entity.find("mantle.party.Party").condition("partyId", testId).one()
if (party) party.delete()

// ✅ CORRECT - Example with list iteration
ec.entity.find("mantle.party.PartyIdentification")
    .condition("partyId", testId).list().each { 
    it.delete() 
}
```

## EntityFilter Testing Strategy

**Problem**: Testing features that rely on EntityFilters requires proper authorization setup and understanding of when to enable vs disable authorization.

### Core Testing Principles

**CRITICAL: EntityFilter testing is opposite of normal test patterns:**
- **Normal tests**: Disable authorization for simpler testing
- **EntityFilter tests**: Enable authorization to activate filters
- **Both approaches are valid** depending on what you're testing

### When to Test With EntityFilters

**Test WITH EntityFilters (authorization enabled) when:**
- Testing access control and security constraints
- Validating tenant separation and data isolation
- Testing user-specific data filtering
- Verifying organizational hierarchy access
- Testing complete end-to-end user scenarios

**Test WITHOUT EntityFilters (authorization disabled) when:**
- Testing pure business logic
- Testing service functionality independent of access control
- Performance testing without security overhead
- Testing data creation and manipulation logic
- Integration testing with external systems

### EntityFilter Test Setup Patterns

#### Pattern 1: Basic EntityFilter Test Setup

```groovy
class EntityFilterServiceTest extends Specification {
    ExecutionContext ec

    def setupSpec() {
        // Standard test framework setup
        ec = Moqui.getExecutionContext()
    }

    def setup() {
        // ENABLE authorization (opposite of normal tests)
        ec.artifactExecution.enableAuthz()

        // Set user context for filtering
        ec.user.context.activeOrgId = "TEST_ORG"
        ec.user.context.filterOrgIds = ["TEST_ORG"]

        // Optional: Login as specific user with proper permissions
        ec.user.loginUser("john.doe", "moqui", null)
    }

    def cleanup() {
        // Clear filter context
        ec.user.context.remove('activeOrgId')
        ec.user.context.remove('filterOrgIds')

        // Logout if logged in
        if (ec.user.userId) {
            ec.user.logoutUser()
        }

        // Disable authorization for cleanup
        ec.artifactExecution.disableAuthz()
    }
}
```

#### Pattern 2: Multi-Tenant EntityFilter Testing

```groovy
def "test tenant separation with EntityFilter"() {
    given: "Two different tenant contexts"
    def tenant1Data = []
    def tenant2Data = []

    when: "Testing with first tenant context"
    ec.user.context.filterOrgIds = ["TENANT1_ORG"]
    def result1 = ec.service.sync().name("{component}.{service}").call()
    tenant1Data = result1.dataList

    and: "Testing with second tenant context"
    ec.user.context.filterOrgIds = ["TENANT2_ORG"]
    def result2 = ec.service.sync().name("{component}.{service}").call()
    tenant2Data = result2.dataList

    then: "Data should be properly isolated by tenant"
    tenant1Data.size() > 0
    tenant2Data.size() > 0

    // Verify no cross-tenant data access
    !tenant1Data.any { item ->
        item.ownerPartyId in ["TENANT2_ORG"]
    }
    !tenant2Data.any { item ->
        item.ownerPartyId in ["TENANT1_ORG"]
    }
}
```

#### Pattern 3: Testing Both Filtered and Unfiltered Scenarios

```groovy
def "test service behavior with and without EntityFilters"() {
    given: "Test data in multiple organizations"
    // Create test data spanning multiple organizations

    when: "Testing without EntityFilter (disabled authz)"
    ec.artifactExecution.disableAuthz()
    def unfilteredResult = ec.service.sync().name("{component}.{service}").call()

    and: "Testing with EntityFilter (enabled authz)"
    ec.artifactExecution.enableAuthz()
    ec.user.context.filterOrgIds = ["TARGET_ORG"]
    def filteredResult = ec.service.sync().name("{component}.{service}").call()

    then: "Unfiltered should return all data"
    unfilteredResult.totalCount > filteredResult.totalCount

    and: "Filtered should only return organization-specific data"
    filteredResult.dataList.every { item ->
        item.ownerPartyId == "TARGET_ORG"
    }
}
```

### EntityFilter Test Data Requirements

#### Complete EntityFilter Configuration

**EntityFilter tests require comprehensive authorization setup:**

```xml
<!-- EntityFilterSet Definition -->
<moqui.security.EntityFilterSet entityFilterSetId="TEST_USER_ORG"/>

<!-- EntityFilter for specific entity -->
<moqui.security.EntityFilter entityFilterId="TEST_ORG_FILTER"
                              entityFilterSetId="TEST_USER_ORG"
                              entityName="{entity-name}"
                              filterMap="[ownerPartyId:ec.user.context.filterOrgIds]"/>

<!-- ArtifactGroup for services/entities being tested -->
<moqui.security.ArtifactGroup artifactGroupId="TEST_SERVICE_GROUP"
                              description="Test Service Group for EntityFilter Testing">
    <artifacts artifactName="{component}.{namespace}.*" artifactTypeEnumId="AT_SERVICE"/>
    <artifacts artifactName="{entity-name}" artifactTypeEnumId="AT_ENTITY"/>
</moqui.security.ArtifactGroup>

<!-- ArtifactAuthz linking user group to artifact group -->
<moqui.security.ArtifactAuthz artifactAuthzId="TEST_FILTER_AUTHZ"
                              userGroupId="ADMIN"
                              artifactGroupId="TEST_SERVICE_GROUP"
                              authzTypeEnumId="AUTHZT_ALLOW"/>

<!-- ArtifactAuthzFilter applying EntityFilter to authorization -->
<moqui.security.ArtifactAuthzFilter artifactAuthzId="TEST_FILTER_AUTHZ"
                                    entityFilterSetId="TEST_USER_ORG"/>

<!-- Test organizations for tenant testing -->
<mantle.party.Party partyId="TEST_ORG" partyTypeEnumId="PtyOrganization"/>
<mantle.party.Party partyId="TENANT1_ORG" partyTypeEnumId="PtyOrganization"/>
<mantle.party.Party partyId="TENANT2_ORG" partyTypeEnumId="PtyOrganization"/>
```

#### User Setup for EntityFilter Testing

```xml
<!-- Test user with proper group membership -->
<moqui.security.UserAccount userId="test.user" username="test.user"/>
<moqui.security.UserGroupMember userGroupId="ADMIN" userId="test.user"
                                fromDate="2024-01-01 00:00:00"/>

<!-- Optional: Organization-specific user if needed -->
<mantle.party.PartyRole partyId="TEST_ORG" roleTypeId="OrgInternal"/>
<mantle.humanres.employment.Employment employeePartyId="test.user"
                                      organizationPartyId="TEST_ORG"/>
```

### Common EntityFilter Testing Patterns

#### Testing Organization-Based Access

```groovy
def "test organization-based data access"() {
    when: "User has access to specific organization"
    ec.user.context.activeOrgId = "USER_ORG"
    ec.user.context.filterOrgIds = ["USER_ORG"]
    def result = ec.service.sync().name("{component}.{service}").call()

    then: "Only organization data is returned"
    result.success
    result.dataList.every { item ->
        item.organizationPartyId == "USER_ORG"
    }
}
```

#### Testing Multi-Organization Access

```groovy
def "test multi-organization data access"() {
    when: "User has access to multiple organizations"
    ec.user.context.filterOrgIds = ["ORG1", "ORG2", "ORG3"]
    def result = ec.service.sync().name("{component}.{service}").call()

    then: "Data from all authorized organizations is returned"
    def returnedOrgIds = result.dataList.collect { it.organizationPartyId }.unique()
    returnedOrgIds.containsAll(["ORG1", "ORG2", "ORG3"])
    !returnedOrgIds.contains("UNAUTHORIZED_ORG")
}
```

#### Testing Access Control Failure

```groovy
def "test unauthorized access blocked by EntityFilter"() {
    when: "User attempts to access unauthorized data"
    ec.user.context.filterOrgIds = ["AUTHORIZED_ORG"]

    // Attempt to access data from unauthorized organization
    def result = ec.service.sync().name("{component}.{service}")
                   .parameter("organizationPartyId", "UNAUTHORIZED_ORG")
                   .call()

    then: "Access should be blocked or data filtered out"
    result.dataList.empty ||
    result.dataList.every { item ->
        item.organizationPartyId != "UNAUTHORIZED_ORG"
    }
}
```

### Best Practices for EntityFilter Testing

#### DO:
- Test both filtered and unfiltered scenarios to validate business logic
- Use realistic test data that spans multiple tenants/organizations
- Verify that EntityFilters properly isolate data between tenants
- Test edge cases like empty filter contexts and unauthorized access
- Include performance testing for EntityFilter overhead

#### DON'T:
- Mix EntityFilter testing patterns with normal business logic testing
- Forget to enable authorization when testing EntityFilters
- Assume EntityFilters work without proper test data setup
- Skip testing cross-tenant access scenarios
- Test only the "happy path" - include negative test cases

#### Testing Strategy:
1. **Unit Tests**: Test business logic without EntityFilters (authorization disabled)
2. **Integration Tests**: Test complete service behavior with EntityFilters (authorization enabled)
3. **Security Tests**: Test access control and tenant separation scenarios
4. **Performance Tests**: Validate EntityFilter performance impact
5. **End-to-End Tests**: Test complete user workflows with proper filtering

## Quick Test Implementation Checklist

Before implementing tests, verify:

- [ ] **Authentication Strategy**: Use john.doe or custom user with proper setup
- [ ] **Data Loading**: Run `./gradlew load` first
- [ ] **Suite Structure**: Create ComponentSuite.groovy
- [ ] **Build Configuration**: Set maxParallelForks=1
- [ ] **Sequence Management**: Use tempSetSequencedIdPrimary
- [ ] **Error Handling**: Check both message context and result map
- [ ] **Cleanup Strategy**: Track created IDs for cleanup
- [ ] **Authorization Strategy**: Choose based on test type:
  - [ ] **Business Logic Tests**: Disable authz in setup/cleanup for simpler testing
  - [ ] **EntityFilter Tests**: Enable authz + proper ArtifactAuthz setup when testing access control
  - [ ] **Security Tests**: Enable authz + test both authorized and unauthorized scenarios

## Test Implementation Template

```groovy
package com.example

import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import spock.lang.Shared
import spock.lang.Specification

class ExampleTests extends Specification {
    @Shared ExecutionContext ec
    @Shared List<String> createdIds = []
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("john.doe", "moqui")
        ec.entity.tempSetSequencedIdPrimary("EntityName", 58000, 10)
    }
    
    def cleanupSpec() {
        // Cleanup in reverse order of creation
        createdIds.reverse().each { id ->
            try {
                ec.service.sync().name("delete#EntityName")
                    .parameters([id: id])
                    .disableAuthz().call()
            } catch (Exception e) {
                logger.warn("Cleanup failed: ${e.message}")
            }
        }
        ec.entity.tempResetSequencedIdPrimary("EntityName")
        ec.destroy()
    }
    
    def setup() {
        ec.artifactExecution.disableAuthz()
        ec.message.clearErrors()  // ✅ Clear at start for clean state
    }

    def cleanup() {
        ec.artifactExecution.enableAuthz()
        // ✅ Only clear errors here if test expects errors
        // For normal successful tests, leave errors visible for debugging
    }

    def "test example"() {
        when:
        Map result = ec.service.sync().name("service.name")
            .parameters([param: "value"])
            .call()
        if (result?.id) createdIds.add(result.id)

        then:
        !ec.message.hasError()  // ✅ Check errors without clearing
        result?.errorMessage == null
        result.expectedField == "expectedValue"
    }
}
```

## Performance Optimization Tips

1. **Minimize Framework Restarts**: Use Suite pattern to initialize once
2. **Batch Test Data Creation**: Create shared test data in setupSpec()
3. **Avoid Database Resets**: Use unique IDs instead of clearing tables
4. **Disable Unnecessary Features**: Use `moqui.init.static=true`
5. **Use In-Memory Database**: H2 for tests, not production DB

## Debugging Failed Tests

When tests fail:

1. **Check Authentication First**: 90% of failures are auth-related
2. **Verify Data Loading**: Ensure seed data exists
3. **Examine Service Definitions**: Confirm service exists and parameters match
4. **Review Error Messages**: Check both ec.message.errors and result.errorMessage
5. **Inspect Transaction Logs**: Look for deadlocks or timeouts
6. **Enable Debug Logging**: `testLogging.showStandardStreams = true`

## Migration from Legacy Tests

When modernizing existing tests:

1. **Consolidate Test Files**: Merge related tests into fewer files
2. **Replace Custom Users**: Switch to framework test users
3. **Update Error Assertions**: Handle both validation and execution errors
4. **Add Proper Cleanup**: Track and clean all created data
5. **Create Test Suite**: Organize with JUnit 5 @Suite
6. **Document Service Dependencies**: Note which services tests depend on

This guide should be referenced at the start of any test implementation to avoid common pitfalls and accelerate development.