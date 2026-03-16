# Moqui Test Templates

## Unit Test Template (Spock)

```groovy
package [domain]

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll
import org.moqui.Moqui
import org.moqui.context.ExecutionContext

class [Service]ServiceTest extends Specification {
    
    @Shared ExecutionContext ec
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("[TEST_USER]", "[TEST_PASSWORD]", null)
    }
    
    def cleanupSpec() {
        ec.user.logoutUser()
        ec.destroy()
    }
    
    def setup() {
        // Load test data if needed
        ec.entity.loadData("data/Test[Domain]Data.xml")
    }
    
    def cleanup() {
        // Clean up test data
        ec.entity.deleteByCondition("[TestEntity]", null, false)
    }
    
    def "[BEHAVIOR_DESCRIPTION] should [EXPECTED_OUTCOME]"() {
        given: "[GIVEN_DESCRIPTION]"
        // Test data setup
        def [TEST_DATA] = [
            [FIELD_NAME]: "[TEST_VALUE]",
            [FIELD_NAME_2]: "[TEST_VALUE_2]"
        ]
        
        when: "[WHEN_DESCRIPTION]"
        Map result = ec.service.sync().name("[SERVICE_NAME]")
                .parameters([TEST_DATA])
                .call()
        
        then: "[THEN_DESCRIPTION]"
        result.[RESULT_FIELD] == [EXPECTED_VALUE]
        result.errors == null
        
        and: "[ADDITIONAL_VALIDATION]"
        def [CREATED_ENTITY] = ec.entity.find("[ENTITY_NAME]")
                .condition("[ID_FIELD]", result.[ID_FIELD])
                .one()
        [CREATED_ENTITY] != null
        [CREATED_ENTITY].[FIELD_NAME] == "[EXPECTED_FIELD_VALUE]"
    }
    
    @Unroll
    def "[PARAMETERIZED_BEHAVIOR] for #[PARAMETER] should be #[EXPECTED]"() {
        when:
        Map result = ec.service.sync().name("[SERVICE_NAME]")
                .parameters([INPUT_PARAMETER]: inputValue)
                .call()
        
        then:
        result.[OUTPUT_FIELD] == expectedValue
        
        where:
        inputValue || expectedValue
        [VALUE_1]  || [EXPECTED_1]
        [VALUE_2]  || [EXPECTED_2]
        [VALUE_3]  || [EXPECTED_3]
    }
    
    def "[ERROR_SCENARIO] should [ERROR_EXPECTATION]"() {
        given:
        def invalidData = [
            [INVALID_FIELD]: [INVALID_VALUE]
        ]
        
        when:
        Map result = ec.service.sync().name("[SERVICE_NAME]")
                .parameters(invalidData)
                .call()
        
        then:
        result.errors != null
        result.errors.size() > 0
        result.errors[0].contains("[EXPECTED_ERROR_MESSAGE]")
    }
}
```

## Integration Test Template

```groovy
package integration

import spock.lang.Specification
import spock.lang.Shared
import org.moqui.Moqui
import org.moqui.context.ExecutionContext

class [Workflow]IntegrationTest extends Specification {
    
    @Shared ExecutionContext ec
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("[TEST_USER]", "[TEST_PASSWORD]", null)
        
        // Load integration test data
        ec.entity.loadData("data/Test[Integration]Data.xml")
    }
    
    def cleanupSpec() {
        // Clean up integration test data
        ec.user.logoutUser()
        ec.destroy()
    }
    
    def "[INTEGRATION_SCENARIO] should [EXPECTED_WORKFLOW_OUTCOME]"() {
        given: "[INITIAL_STATE_DESCRIPTION]"
        // Setup initial state
        def initialData = setupWorkflowTestData()
        
        when: "[WORKFLOW_EXECUTION_DESCRIPTION]"
        // Execute complete workflow
        Map step1Result = ec.service.sync().name("[STEP_1_SERVICE]")
                .parameters(initialData)
                .call()
        
        Map step2Result = ec.service.sync().name("[STEP_2_SERVICE]")
                .parameters([step1Id: step1Result.[ID_FIELD]])
                .call()
        
        Map step3Result = ec.service.sync().name("[STEP_3_SERVICE]")
                .parameters([step2Id: step2Result.[ID_FIELD]])
                .call()
        
        then: "[FINAL_STATE_VALIDATION]"
        step1Result.errors == null
        step2Result.errors == null
        step3Result.errors == null
        
        and: "[DATA_STATE_VALIDATION]"
        def finalEntity = ec.entity.find("[RESULT_ENTITY]")
                .condition("[ID_FIELD]", step3Result.[ID_FIELD])
                .one()
        
        finalEntity != null
        finalEntity.[STATUS_FIELD] == "[EXPECTED_STATUS]"
        finalEntity.[RESULT_FIELD] == [EXPECTED_RESULT]
        
        and: "[SIDE_EFFECTS_VALIDATION]"
        def relatedEntities = ec.entity.find("[RELATED_ENTITY]")
                .condition("[PARENT_ID]", step3Result.[ID_FIELD])
                .list()
        relatedEntities.size() == [EXPECTED_COUNT]
        
        cleanup:
        cleanupWorkflowTestData(step1Result.[ID_FIELD])
    }
    
    def "[TRANSACTION_SCENARIO] should [TRANSACTION_EXPECTATION]"() {
        given:
        def transactionData = setupTransactionTestData()
        
        when:
        // Force transaction rollback scenario
        ec.transaction.begin()
        try {
            ec.service.sync().name("[SERVICE_1]")
                    .parameters(transactionData)
                    .call()
            
            // Simulate error that causes rollback
            ec.service.sync().name("[FAILING_SERVICE]")
                    .parameters(transactionData)
                    .call()
            
            ec.transaction.commit()
        } catch (Exception e) {
            ec.transaction.rollback(null, null)
        }
        
        then:
        // Verify rollback occurred
        def entity1 = ec.entity.find("[ENTITY_1]")
                .condition("[ID_FIELD]", transactionData.[ID_FIELD])
                .one()
        entity1 == null  // Should not exist due to rollback
    }
    
    private Map setupWorkflowTestData() {
        // Helper method to create test data
        def testData = [:]
        
        // Create test entities
        def testEntity = ec.entity.makeValue("[TEST_ENTITY]")
        testEntity.[ID_FIELD] = "[TEST_ID]"
        testEntity.[FIELD_NAME] = "[TEST_VALUE]"
        testEntity.create()
        
        testData.[ID_FIELD] = testEntity.[ID_FIELD]
        return testData
    }
    
    private void cleanupWorkflowTestData(String entityId) {
        // Helper method to clean up test data
        ec.entity.find("[TEST_ENTITY]")
                .condition("[ID_FIELD]", entityId)
                .deleteAll()
        
        ec.entity.find("[RELATED_ENTITY]")
                .condition("[PARENT_ID]", entityId)
                .deleteAll()
    }
}
```

## Data-Driven Test Template

```groovy
package [domain]

import spock.lang.Specification
import spock.lang.Shared
import spock.lang.Unroll
import org.moqui.Moqui
import org.moqui.context.ExecutionContext

class [Feature]DataDrivenTest extends Specification {
    
    @Shared ExecutionContext ec
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("[TEST_USER]", "[TEST_PASSWORD]", null)
    }
    
    def cleanupSpec() {
        ec.user.logoutUser()
        ec.destroy()
    }
    
    @Unroll
    def "[CALCULATION_DESCRIPTION] with #description should return #expectedResult"() {
        when:
        Map result = ec.service.sync().name("[CALCULATION_SERVICE]")
                .parameters([
                    [INPUT_PARAM_1]: inputValue1,
                    [INPUT_PARAM_2]: inputValue2,
                    [INPUT_PARAM_3]: inputValue3
                ])
                .call()
        
        then:
        result.[OUTPUT_FIELD] == expectedResult
        result.errors == null
        
        where:
        description                    | inputValue1 | inputValue2 | inputValue3 | expectedResult
        "[SCENARIO_1_DESCRIPTION]"    | [VALUE_1_1] | [VALUE_1_2] | [VALUE_1_3] | [EXPECTED_1]
        "[SCENARIO_2_DESCRIPTION]"    | [VALUE_2_1] | [VALUE_2_2] | [VALUE_2_3] | [EXPECTED_2]
        "[SCENARIO_3_DESCRIPTION]"    | [VALUE_3_1] | [VALUE_3_2] | [VALUE_3_3] | [EXPECTED_3]
        "[EDGE_CASE_DESCRIPTION]"     | [EDGE_1]    | [EDGE_2]    | [EDGE_3]    | [EDGE_EXPECTED]
    }
    
    @Unroll
    def "[VALIDATION_DESCRIPTION] with #invalidField = #invalidValue should fail with error"() {
        given:
        def testData = [
            [VALID_FIELD_1]: "[VALID_VALUE_1]",
            [VALID_FIELD_2]: "[VALID_VALUE_2]"
        ]
        testData[invalidField] = invalidValue
        
        when:
        Map result = ec.service.sync().name("[VALIDATION_SERVICE]")
                .parameters(testData)
                .call()
        
        then:
        result.errors != null
        result.errors.size() > 0
        result.errors.any { it.contains(expectedErrorKeyword) }
        
        where:
        invalidField      | invalidValue     | expectedErrorKeyword
        "[REQUIRED_FIELD]"| null            | "[REQUIRED_ERROR]"
        "[REQUIRED_FIELD]"| ""              | "[REQUIRED_ERROR]"
        "[FORMAT_FIELD]"  | "[INVALID_FMT]" | "[FORMAT_ERROR]"
        "[RANGE_FIELD]"   | [OUT_OF_RANGE]  | "[RANGE_ERROR]"
    }
}
```

## Performance Test Template

```groovy
package performance

import spock.lang.Specification
import spock.lang.Shared
import org.moqui.Moqui
import org.moqui.context.ExecutionContext

class [Service]PerformanceTest extends Specification {
    
    @Shared ExecutionContext ec
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("[TEST_USER]", "[TEST_PASSWORD]", null)
        
        // Load performance test data
        setupPerformanceTestData()
    }
    
    def cleanupSpec() {
        cleanupPerformanceTestData()
        ec.user.logoutUser()
        ec.destroy()
    }
    
    def "[PERFORMANCE_SCENARIO] should complete within acceptable time"() {
        given:
        def testData = [
            [INPUT_FIELD]: "[PERFORMANCE_VALUE]",
            [BATCH_SIZE]: [LARGE_BATCH_SIZE]
        ]
        
        when:
        long startTime = System.currentTimeMillis()
        
        Map result = ec.service.sync().name("[PERFORMANCE_SERVICE]")
                .parameters(testData)
                .call()
        
        long endTime = System.currentTimeMillis()
        long executionTime = endTime - startTime
        
        then:
        result.errors == null
        executionTime < [MAX_EXECUTION_TIME_MS]  // Maximum acceptable time
        result.[PROCESSED_COUNT] >= [MIN_PROCESSED_COUNT]
        
        and: "memory usage should be reasonable"
        Runtime runtime = Runtime.getRuntime()
        long usedMemory = runtime.totalMemory() - runtime.freeMemory()
        usedMemory < [MAX_MEMORY_USAGE_BYTES]
    }
    
    def "[CONCURRENT_SCENARIO] should handle concurrent execution"() {
        given:
        def threadCount = [THREAD_COUNT]
        def executionsPerThread = [EXECUTIONS_PER_THREAD]
        List<Thread> threads = []
        List<Exception> exceptions = Collections.synchronizedList([])
        List<Long> executionTimes = Collections.synchronizedList([])
        
        when:
        threadCount.times { threadIndex ->
            Thread thread = new Thread({
                executionsPerThread.times { executionIndex ->
                    try {
                        long startTime = System.currentTimeMillis()
                        
                        Map result = ec.service.sync().name("[CONCURRENT_SERVICE]")
                                .parameters([
                                    [THREAD_ID]: threadIndex,
                                    [EXECUTION_ID]: executionIndex
                                ])
                                .call()
                        
                        long endTime = System.currentTimeMillis()
                        executionTimes.add(endTime - startTime)
                        
                        if (result.errors) {
                            exceptions.add(new RuntimeException("Service errors: ${result.errors}"))
                        }
                    } catch (Exception e) {
                        exceptions.add(e)
                    }
                }
            })
            threads.add(thread)
            thread.start()
        }
        
        // Wait for all threads to complete
        threads.each { it.join([THREAD_TIMEOUT_MS]) }
        
        then:
        exceptions.isEmpty()
        executionTimes.size() == threadCount * executionsPerThread
        executionTimes.every { it < [MAX_CONCURRENT_EXECUTION_TIME_MS] }
        
        and: "average execution time should be reasonable"
        def averageTime = executionTimes.sum() / executionTimes.size()
        averageTime < [MAX_AVERAGE_EXECUTION_TIME_MS]
    }
    
    private void setupPerformanceTestData() {
        // Create large dataset for performance testing
        [LARGE_DATASET_SIZE].times { index ->
            def entity = ec.entity.makeValue("[PERFORMANCE_ENTITY]")
            entity.[ID_FIELD] = "PERF_${index}"
            entity.[DATA_FIELD] = "Performance test data ${index}"
            entity.create()
        }
    }
    
    private void cleanupPerformanceTestData() {
        // Clean up performance test data
        ec.entity.find("[PERFORMANCE_ENTITY]")
                .condition("[ID_FIELD]", ComparisonOperator.LIKE, "PERF_%")
                .deleteAll()
    }
}
```

## API Test Template

```groovy
package api

import spock.lang.Specification
import spock.lang.Shared
import org.moqui.Moqui
import org.moqui.context.ExecutionContext
import org.moqui.util.RestClient

class [API]RestTest extends Specification {
    
    @Shared ExecutionContext ec
    @Shared RestClient restClient
    @Shared String baseUrl
    @Shared String authToken
    
    def setupSpec() {
        ec = Moqui.getExecutionContext()
        ec.user.loginUser("[API_TEST_USER]", "[API_TEST_PASSWORD]", null)
        
        baseUrl = "[API_BASE_URL]"
        restClient = ec.service.rest()
        
        // Authenticate and get token
        authenticateApi()
    }
    
    def cleanupSpec() {
        ec.user.logoutUser()
        ec.destroy()
    }
    
    def "[API_ENDPOINT] GET should return [EXPECTED_RESPONSE]"() {
        when:
        RestClient.RestResponse response = restClient
                .uri("${baseUrl}/[API_ENDPOINT]")
                .header("Authorization", "Bearer ${authToken}")
                .get()
        
        then:
        response.statusCode == [EXPECTED_STATUS_CODE]
        response.jsonObject()[RESPONSE_FIELD] == [EXPECTED_VALUE]
        response.jsonObject().containsKey("[REQUIRED_FIELD]")
    }
    
    def "[API_ENDPOINT] POST should create [RESOURCE_TYPE]"() {
        given:
        def requestData = [
            [FIELD_1]: "[VALUE_1]",
            [FIELD_2]: "[VALUE_2]",
            [FIELD_3]: [VALUE_3]
        ]
        
        when:
        RestClient.RestResponse response = restClient
                .uri("${baseUrl}/[API_ENDPOINT]")
                .header("Authorization", "Bearer ${authToken}")
                .header("Content-Type", "application/json")
                .jsonObject(requestData)
                .post()
        
        then:
        response.statusCode == [CREATED_STATUS_CODE]
        response.jsonObject()[ID_FIELD] != null
        response.jsonObject()[FIELD_1] == "[VALUE_1]"
        
        and: "resource should be retrievable"
        def createdId = response.jsonObject()[ID_FIELD]
        RestClient.RestResponse getResponse = restClient
                .uri("${baseUrl}/[API_ENDPOINT]/${createdId}")
                .header("Authorization", "Bearer ${authToken}")
                .get()
        
        getResponse.statusCode == [SUCCESS_STATUS_CODE]
        getResponse.jsonObject()[FIELD_1] == "[VALUE_1]"
        
        cleanup:
        // Clean up created resource
        restClient.uri("${baseUrl}/[API_ENDPOINT]/${createdId}")
                .header("Authorization", "Bearer ${authToken}")
                .delete()
    }
    
    def "[API_ENDPOINT] PUT should update [RESOURCE_TYPE]"() {
        given:
        // Create test resource first
        def createData = [
            [FIELD_1]: "[ORIGINAL_VALUE]",
            [FIELD_2]: "[ORIGINAL_VALUE_2]"
        ]
        
        RestClient.RestResponse createResponse = restClient
                .uri("${baseUrl}/[API_ENDPOINT]")
                .header("Authorization", "Bearer ${authToken}")
                .header("Content-Type", "application/json")
                .jsonObject(createData)
                .post()
        
        def resourceId = createResponse.jsonObject()[ID_FIELD]
        
        def updateData = [
            [FIELD_1]: "[UPDATED_VALUE]",
            [FIELD_2]: "[UPDATED_VALUE_2]"
        ]
        
        when:
        RestClient.RestResponse response = restClient
                .uri("${baseUrl}/[API_ENDPOINT]/${resourceId}")
                .header("Authorization", "Bearer ${authToken}")
                .header("Content-Type", "application/json")
                .jsonObject(updateData)
                .put()
        
        then:
        response.statusCode == [SUCCESS_STATUS_CODE]
        response.jsonObject()[FIELD_1] == "[UPDATED_VALUE]"
        response.jsonObject()[FIELD_2] == "[UPDATED_VALUE_2]"
        
        cleanup:
        restClient.uri("${baseUrl}/[API_ENDPOINT]/${resourceId}")
                .header("Authorization", "Bearer ${authToken}")
                .delete()
    }
    
    def "[API_ENDPOINT] should handle authentication errors"() {
        when:
        RestClient.RestResponse response = restClient
                .uri("${baseUrl}/[PROTECTED_ENDPOINT]")
                .get()  // No auth token
        
        then:
        response.statusCode == [UNAUTHORIZED_STATUS_CODE]
        response.jsonObject().error.contains("[AUTH_ERROR_MESSAGE]")
    }
    
    def "[API_ENDPOINT] should validate input data"() {
        given:
        def invalidData = [
            [INVALID_FIELD]: [INVALID_VALUE]
        ]
        
        when:
        RestClient.RestResponse response = restClient
                .uri("${baseUrl}/[API_ENDPOINT]")
                .header("Authorization", "Bearer ${authToken}")
                .header("Content-Type", "application/json")
                .jsonObject(invalidData)
                .post()
        
        then:
        response.statusCode == [BAD_REQUEST_STATUS_CODE]
        response.jsonObject().errors != null
        response.jsonObject().errors.any { it.contains("[VALIDATION_ERROR_MESSAGE]") }
    }
    
    private void authenticateApi() {
        RestClient.RestResponse authResponse = restClient
                .uri("${baseUrl}/[AUTH_ENDPOINT]")
                .header("Content-Type", "application/json")
                .jsonObject([
                    username: "[API_TEST_USER]",
                    password: "[API_TEST_PASSWORD]"
                ])
                .post()
        
        authToken = authResponse.jsonObject().token
    }
}
```

## Test Data Helper Template

```groovy
package helpers

import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue

class [Domain]TestDataHelper {
    
    static Map create[Entity]TestData(ExecutionContext ec, Map overrides = [:]) {
        def defaultData = [
            [FIELD_1]: "[DEFAULT_VALUE_1]",
            [FIELD_2]: "[DEFAULT_VALUE_2]",
            [FIELD_3]: [DEFAULT_VALUE_3],
            [STATUS_FIELD]: "[DEFAULT_STATUS]"
        ]
        
        // Merge with overrides
        def testData = defaultData + overrides
        
        // Create the entity
        EntityValue entity = ec.entity.makeValue("[ENTITY_NAME]")
        entity.setAll(testData)
        entity.create()
        
        return testData + [[ID_FIELD]: entity.[ID_FIELD]]
    }
    
    static void cleanup[Entity]TestData(ExecutionContext ec, String entityId) {
        // Clean up related entities first
        ec.entity.find("[RELATED_ENTITY]")
                .condition("[PARENT_ID]", entityId)
                .deleteAll()
        
        // Clean up main entity
        ec.entity.find("[ENTITY_NAME]")
                .condition("[ID_FIELD]", entityId)
                .deleteAll()
    }
    
    static Map createComplete[Workflow]TestData(ExecutionContext ec) {
        // Create a complete test scenario
        def parentData = create[ParentEntity]TestData(ec)
        def childData = create[ChildEntity]TestData(ec, [
            [PARENT_ID]: parentData.[ID_FIELD]
        ])
        def relationshipData = create[Relationship]TestData(ec, [
            [ENTITY_1_ID]: parentData.[ID_FIELD],
            [ENTITY_2_ID]: childData.[ID_FIELD]
        ])
        
        return [
            parent: parentData,
            child: childData,
            relationship: relationshipData
        ]
    }
    
    static void cleanupComplete[Workflow]TestData(ExecutionContext ec, Map testData) {
        cleanup[Relationship]TestData(ec, testData.relationship.[ID_FIELD])
        cleanup[ChildEntity]TestData(ec, testData.child.[ID_FIELD])
        cleanup[ParentEntity]TestData(ec, testData.parent.[ID_FIELD])
    }
}
```

## Placeholders Reference

- `[DOMAIN]` - Domain package name (lowercase)
- `[Service]` - Service name (PascalCase)
- `[TEST_USER]` - Test user account
- `[TEST_PASSWORD]` - Test user password
- `[BEHAVIOR_DESCRIPTION]` - Test behavior description
- `[EXPECTED_OUTCOME]` - Expected test outcome
- `[SERVICE_NAME]` - Service to test
- `[RESULT_FIELD]` - Service result field
- `[EXPECTED_VALUE]` - Expected result value
- `[ENTITY_NAME]` - Entity name to test
- `[ID_FIELD]` - Primary key field
- `[FIELD_NAME]` - Entity field name
- `[TEST_VALUE]` - Test data value
- `[WORKFLOW]` - Workflow name (PascalCase)
- `[STEP_1_SERVICE]` - First workflow step service
- `[API_ENDPOINT]` - REST API endpoint
- `[API_BASE_URL]` - Base URL for API tests
- `[EXPECTED_STATUS_CODE]` - HTTP status code
- `[THREAD_COUNT]` - Number of concurrent threads
- `[MAX_EXECUTION_TIME_MS]` - Maximum execution time
- `[PERFORMANCE_SERVICE]` - Service for performance testing