---
description: Test generation workflow for existing project components
globs:
alwaysApply: false
version: 1.0
encoding: UTF-8
---

# Create Test Workflow

## Overview

Generate comprehensive test coverage for existing project components by:
1. Identifying what code needs testing through interactive selection
2. Analyzing the code structure and dependencies
3. Coordinating specialized agents to generate appropriate tests
4. Following Moqui testing best practices with Spock framework
5. Ensuring comprehensive coverage including edge cases and error scenarios

## Workflow

### Step 0: Test Target Selection (Interactive)

First, I'll help you identify what code needs test coverage:

**"What would you like to create tests for?"**
1. Specific service(s) - Test business logic and service operations
2. Entity operations - Test CRUD operations and validations
3. Screen workflows - Test UI interactions and form submissions
4. Complete feature - Test all components of a feature end-to-end
5. Integration scenarios - Test component interactions
6. Recent changes - Test files modified in recent commits

Based on your selection, I'll:
- Find the relevant code components
- Analyze existing test coverage
- Identify testing gaps
- Determine the appropriate testing strategy

### Step 1: Code Analysis and Planning

I'll analyze the selected code to understand:
- **Dependencies**: Services called, entities used, external integrations
- **Business Rules**: Validations, calculations, state transitions
- **Data Flow**: Input/output parameters, transformations
- **Error Scenarios**: Exception handling, edge cases
- **Existing Tests**: Current coverage and gaps

### Step 2: Test Structure Setup

Using the **moqui-test-specialist** agent to create:
- Proper test file structure in `src/test/groovy/`
- Spock specification setup with Moqui context
- Shared test data and fixtures
- Helper methods and utilities

Example structure:
```groovy
class ComponentNameSpec extends Specification {
    @Shared ExecutionContext ec
    @Shared Map<String, Object> testData = [:]

    def setupSpec() {
        ec = Moqui.getExecutionContext()
        // Setup test data
    }
}
```

### Step 3: Service Test Generation

If testing services, the **moqui-service-specialist** will create:
- **Parameter validation tests**: Invalid inputs, null handling
- **Business logic tests**: Rules, calculations, transformations
- **Integration tests**: Service chains, transactions
- **Error handling tests**: Exception scenarios, rollbacks
- **Performance tests**: Load handling, concurrent execution

### Step 4: Entity Test Generation

If testing entities, the **moqui-entity-specialist** will create:
- **CRUD operation tests**: Create, read, update, delete
- **Validation tests**: Constraints, required fields, data types
- **Relationship tests**: Foreign keys, cascades, associations
- **Query tests**: Complex finds, views, aggregations
- **Performance tests**: Bulk operations, indexes

### Step 5: Screen/UI Test Generation

If testing screens, the **moqui-screen-specialist** will create:
- **Render tests**: Screen loads without errors
- **Form tests**: Validation, submission, error display
- **Transition tests**: Navigation, parameter passing
- **Authorization tests**: Access control, permissions
- **E2E tests**: Complete user workflows (using Playwright if needed)

### Step 6: Integration Test Generation

For feature or integration testing:
- **Workflow tests**: Complete business processes
- **Cross-component tests**: Entity-service-screen integration
- **Error scenarios**: Failure handling, recovery
- **Transaction tests**: Rollbacks, consistency
- **Performance tests**: End-to-end response times

### Step 7: Test Data and Fixtures

Create comprehensive test data:
- **Test data files**: `data/ComponentTestData.xml`
- **Data builders**: Fluent APIs for test data creation
- **Mock services**: External service simulations
- **Stub implementations**: Third-party integrations

### Step 8: Test Optimization

Optimize the test suite for:
- **Performance**: Shared setup, parallel execution
- **Maintainability**: Clear naming, good organization
- **Stability**: Avoid brittle tests, handle timing issues
- **Documentation**: Meaningful descriptions, comments

### Step 9: Test Execution and Validation

Execute and validate tests:
- Run tests with `./gradlew test`
- Analyze coverage metrics (target: 80% line, 70% branch)
- Fix failing tests
- Add missing scenarios for coverage gaps
- Configure CI/CD integration

### Step 10: Documentation and Handoff

Provide comprehensive documentation:
- Test suite README with running instructions
- Inline documentation for complex tests
- Maintenance guidelines
- Coverage reports and recommendations

## Test Quality Standards

All generated tests will follow the **FIRST** principles:
- **Fast**: Quick execution, minimal dependencies
- **Independent**: No test order dependencies
- **Repeatable**: Consistent, deterministic results
- **Self-validating**: Clear pass/fail, no manual checks
- **Timely**: Maintained with code changes

## Coverage Targets

- **Line coverage**: 80% minimum
- **Branch coverage**: 70% minimum
- **Critical paths**: 100% coverage
- **Error handling**: 90% coverage

## Agent Coordination

This command coordinates multiple specialized agents:
- **moqui-test-specialist**: Overall test strategy and Spock framework
- **moqui-service-specialist**: Service testing expertise
- **moqui-entity-specialist**: Entity and data testing
- **moqui-screen-specialist**: UI and workflow testing
- **moqui-build-coordinator**: Test execution and CI/CD

## Best Practices Applied

- **Data-driven testing**: Parameterized tests with multiple scenarios
- **Test isolation**: Each test independent and repeatable
- **Mock strategies**: Proper isolation from external dependencies
- **Performance focus**: Efficient test execution
- **Maintenance focus**: Clear, maintainable test code
- **Documentation**: Well-documented test purposes and scenarios

## Example Usage

When you run `/create-test`, I'll:
1. Ask what you want to test (e.g., "CafAutoRequestServices")
2. Analyze the service structure and dependencies
3. Check existing test coverage
4. Generate comprehensive Spock tests
5. Create necessary test data
6. Ensure all paths are covered
7. Provide documentation and run instructions

The result will be production-ready tests that ensure your code works correctly and catches regressions early.
