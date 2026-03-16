# E2E Test Data Strategy - Moqui Framework

Comprehensive strategy for generating, managing, and cleaning up test data for End-to-End testing in Moqui Framework projects.

## Overview

End-to-End testing requires realistic, complex test data that represents complete business workflows. This strategy provides a framework-agnostic approach that leverages Moqui's service infrastructure while ensuring test isolation and performance.

## Core Principles

### 1. **Multi-Layer Architecture**
Organize test data into distinct layers based on lifecycle and scope:

- **Foundation Layer**: Base system configuration, users, reference data
- **Scenario Layer**: Feature-specific entities and business domain data  
- **Transaction Layer**: Test-specific transactional data created during test execution

### 2. **Service-First Approach**
Leverage Moqui's existing service infrastructure for complex entity creation:

- Use entity auto-services for standard CRUD operations
- Extend existing services for test-specific data generation
- Maintain proper entity relationships and business rule validation

### 3. **Test Isolation**
Ensure tests can run independently and in parallel:

- Implement proper database isolation strategies
- Use unique identifiers and avoid shared state
- Provide automatic cleanup mechanisms

### 4. **Performance Optimization**
Balance test reliability with execution speed:

- Cache expensive operations like certificate generation
- Use bulk operations for large datasets
- Implement intelligent data reuse patterns

## Implementation Architecture

### Test Data Factory Pattern

#### Service-Based Data Generation

**Primary Factory Interface:**
```javascript
class MoquiTestDataFactory {
  // Create complete test environment with all dependencies
  static async createBaseTestEnvironment(options = {}) {
    const config = {
      organizationName: options.orgName || `Test Org ${Date.now()}`,
      includeUsers: options.users !== false,
      includeReferenceData: options.refData !== false,
      ...options
    };
    
    // Call Moqui service for complex setup
    return await callMoquiService('create#TestEnvironment', config);
  }
  
  // Generate scenario-specific test data
  static async createTestScenario(entityType, scenario = 'standard', options = {}) {
    const scenarioConfig = this.getScenarioConfig(entityType, scenario);
    return await callMoquiService(`create#Test${entityType}`, {
      ...scenarioConfig,
      ...options
    });
  }
  
  // Create test data with proper relationships
  static async createEntityWithDependencies(entityName, data, options = {}) {
    // Ensure all dependent entities exist
    await this.ensureDependencies(entityName, data);
    
    // Create main entity using auto-service
    return await callMoquiService(`create#${entityName}`, data);
  }
}
```

**Configuration-Driven Generation:**
```javascript
// Test data configuration (project-specific)
const testDataConfig = {
  entities: {
    'Party': {
      service: 'mantle.party.PartyServices.create#Organization',
      dependencies: ['PartyType', 'StatusItem'],
      scenarios: {
        'standard': { partyTypeEnumId: 'PtyOrganization' },
        'person': { partyTypeEnumId: 'PtyPerson' },
        'internal': { partyTypeEnumId: 'PtyOrganization', roleTypeId: 'OrgInternal' }
      }
    },
    'Product': {
      service: 'mantle.product.ProductServices.create#Product',
      dependencies: ['ProductType', 'UomDimension'],
      scenarios: {
        'standard': { productTypeEnumId: 'PtPhysical' },
        'service': { productTypeEnumId: 'PtService' },
        'digital': { productTypeEnumId: 'PtDigital' }
      }
    }
  }
};

class ConfigDrivenFactory {
  static async createEntityData(entityName, scenario = 'standard', overrides = {}) {
    const config = testDataConfig.entities[entityName];
    if (!config) {
      throw new Error(`No test data configuration for entity: ${entityName}`);
    }
    
    // Ensure dependencies exist
    await this.ensureDependencies(config.dependencies);
    
    // Get scenario configuration
    const scenarioData = config.scenarios[scenario] || config.scenarios['standard'];
    
    // Merge with overrides
    const finalData = { ...scenarioData, ...overrides };
    
    // Call appropriate service
    return await callMoquiService(config.service, finalData);
  }
}
```

### Database Isolation Strategies

#### Strategy 1: Transaction-Based Isolation (Recommended)

**Implementation:**
```javascript
class TransactionManager {
  static async beginTestTransaction() {
    return await callMoquiService('begin#TestTransaction', {
      transactionTimeout: 300, // 5 minutes
      readOnly: false
    });
  }
  
  static async rollbackTestTransaction(transactionId) {
    return await callMoquiService('rollback#TestTransaction', {
      transactionId: transactionId
    });
  }
}

// Usage in tests
beforeEach(async ({ page }) => {
  // Start isolated transaction
  testTransactionId = await TransactionManager.beginTestTransaction();
  
  // Generate test data within transaction
  testData = await MoquiTestDataFactory.createTestScenario('Invoice', 'standard');
});

afterEach(async () => {
  // Rollback all changes
  await TransactionManager.rollbackTestTransaction(testTransactionId);
});
```

#### Strategy 2: Prefix-Based Isolation

**Implementation:**
```javascript
class PrefixManager {
  static getTestPrefix() {
    return `TEST-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`;
  }
  
  static async createEntityWithPrefix(entityName, data) {
    const prefix = this.getTestPrefix();
    const prefixedData = {
      ...data,
      [this.getIdField(entityName)]: `${prefix}-${data[this.getIdField(entityName)] || 'AUTO'}`
    };
    
    return await callMoquiService(`create#${entityName}`, prefixedData);
  }
  
  static async cleanupByPrefix(prefix) {
    return await callMoquiService('cleanup#TestDataByPrefix', { prefix: prefix });
  }
}
```

#### Strategy 3: Isolated Test Schemas

**Implementation:**
```javascript
class SchemaManager {
  static async createTestSchema() {
    const schemaName = `test_schema_${Date.now()}`;
    return await callMoquiService('create#TestSchema', { schemaName: schemaName });
  }
  
  static async populateTestSchema(schemaName, baseDataTypes = ['seed', 'seed-initial']) {
    return await callMoquiService('populate#TestSchema', {
      schemaName: schemaName,
      dataTypes: baseDataTypes
    });
  }
  
  static async dropTestSchema(schemaName) {
    return await callMoquiService('drop#TestSchema', { schemaName: schemaName });
  }
}
```

### Time-Sensitive Data Management

#### Dynamic Date Generation

```javascript
class TestDateManager {
  static getCurrentDate() {
    return new Date();
  }
  
  static getFutureDate(daysFromNow = 365) {
    const date = new Date();
    date.setDate(date.getDate() + daysFromNow);
    return date;
  }
  
  static getPastDate(daysAgo = 30) {
    const date = new Date();
    date.setDate(date.getDate() - daysAgo);
    return date;
  }
  
  static getValidityPeriod(startDays = 0, endDays = 365) {
    return {
      fromDate: this.getFutureDate(startDays),
      thruDate: this.getFutureDate(endDays)
    };
  }
  
  static formatForMoqui(date) {
    // Return date in Moqui's expected format
    return date.toISOString().split('T')[0]; // YYYY-MM-DD
  }
}
```

#### Sequential ID Management

```javascript
class SequenceManager {
  static async reserveIdRange(sequenceName, rangeSize = 100) {
    return await callMoquiService('reserve#SequenceRange', {
      sequenceName: sequenceName,
      rangeSize: rangeSize,
      requestId: `TEST-${Date.now()}`
    });
  }
  
  static async getNextTestId(entityName) {
    const sequenceName = `${entityName}TestSequence`;
    const result = await callMoquiService('get#NextSequenceValue', {
      sequenceName: sequenceName
    });
    return `TEST-${result.sequenceValue}`;
  }
  
  static async releaseReservedRanges(requestId) {
    return await callMoquiService('release#SequenceRanges', {
      requestId: requestId
    });
  }
}
```

### Moqui Service Integration

#### REST API Integration Pattern

```javascript
// Base service call function
async function callMoquiService(serviceName, parameters = {}, options = {}) {
  const response = await page.request.post(`/rest/s1/${serviceName}`, {
    data: parameters,
    headers: {
      'Authorization': `Bearer ${await getTestToken()}`,
      'Content-Type': 'application/json',
      'X-Test-Context': 'E2E-Testing'
    },
    timeout: options.timeout || 30000
  });
  
  if (!response.ok()) {
    const errorText = await response.text();
    throw new Error(`Service call failed: ${serviceName} - ${response.status()} - ${errorText}`);
  }
  
  const result = await response.json();
  
  // Check for Moqui service errors
  if (result.errorMessage) {
    throw new Error(`Moqui service error: ${result.errorMessage}`);
  }
  
  return result;
}

// Service call with retry logic
class ServiceCallManager {
  static async safeServiceCall(serviceName, parameters, maxRetries = 3) {
    let lastError;
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        return await callMoquiService(serviceName, parameters);
      } catch (error) {
        lastError = error;
        if (attempt < maxRetries) {
          await this.delay(attempt * 1000); // Exponential backoff
        }
      }
    }
    
    throw new Error(`Service call failed after ${maxRetries} attempts: ${lastError.message}`);
  }
  
  static async delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
```

#### Batch Operations

```javascript
class BatchDataManager {
  static async createBulkTestData(entityName, dataArray, batchSize = 10) {
    const results = [];
    
    for (let i = 0; i < dataArray.length; i += batchSize) {
      const batch = dataArray.slice(i, i + batchSize);
      const batchResult = await callMoquiService(`createBulk#${entityName}`, {
        entityDataList: batch
      });
      results.push(...batchResult.entityDataList);
    }
    
    return results;
  }
  
  static async deleteBulkTestData(entityName, idList, batchSize = 50) {
    for (let i = 0; i < idList.length; i += batchSize) {
      const batch = idList.slice(i, i + batchSize);
      await callMoquiService(`deleteBulk#${entityName}`, {
        entityIdList: batch
      });
    }
  }
}
```

### Test Data Lifecycle Management

#### Pre-Test Setup

```javascript
// Global setup
export async function globalSetup() {
  // Initialize test environment
  await TestEnvironmentManager.initialize();
  
  // Create shared base data if needed
  await BaseDataManager.ensureBaseTestData();
  
  // Start monitoring services
  await TestMonitoringService.start();
}

// Per-test setup
beforeEach(async ({ page }) => {
  // Start test isolation
  testContext = await TestIsolationManager.beginTestContext();
  
  // Generate test-specific data
  testData = await MoquiTestDataFactory.createTestScenario(
    testEntityType, 
    testScenario, 
    testParameters
  );
  
  // Navigate to test starting point
  await page.goto(getTestUrl(testData));
  
  // Authenticate if required
  if (testData.requiresAuth) {
    await authenticateUser(page, testData.testUser);
  }
});
```

#### Post-Test Cleanup

```javascript
// Per-test cleanup
afterEach(async () => {
  // Clean up test-specific data
  await TestIsolationManager.endTestContext(testContext);
  
  // Clear browser state
  await clearBrowserState();
  
  // Release resources
  await ResourceManager.releaseTestResources(testContext.resourceId);
});

// Global cleanup
export async function globalTeardown() {
  // Clean up persistent test data
  await TestDataCleanup.removeExpiredTestData();
  
  // Reset sequences
  await SequenceManager.resetTestSequences();
  
  // Clear caches
  TestDataCache.clear();
  
  // Stop monitoring
  await TestMonitoringService.stop();
}
```

### Performance Optimization

#### Data Caching Strategy

```javascript
class TestDataCache {
  private static cache = new Map();
  private static ttl = new Map();
  
  static async getOrCreate(key, generator, ttlMinutes = 60) {
    // Check if cached data is still valid
    if (this.cache.has(key) && !this.isExpired(key)) {
      return this.cache.get(key);
    }
    
    // Generate new data
    const data = await generator();
    
    // Cache with TTL
    this.cache.set(key, data);
    this.ttl.set(key, Date.now() + (ttlMinutes * 60 * 1000));
    
    return data;
  }
  
  static isExpired(key) {
    const expiry = this.ttl.get(key);
    return !expiry || Date.now() > expiry;
  }
  
  static clear() {
    this.cache.clear();
    this.ttl.clear();
  }
  
  static async clearExpired() {
    for (const [key] of this.cache) {
      if (this.isExpired(key)) {
        this.cache.delete(key);
        this.ttl.delete(key);
      }
    }
  }
}
```

#### Resource Management

```javascript
class ResourceManager {
  private static resources = new Map();
  
  static async allocateResource(type, config) {
    const resourceId = `${type}-${Date.now()}-${Math.random().toString(36).substr(2, 5)}`;
    
    const resource = await this.createResource(type, config);
    this.resources.set(resourceId, {
      type: type,
      resource: resource,
      allocated: Date.now(),
      config: config
    });
    
    return resourceId;
  }
  
  static async releaseResource(resourceId) {
    const resourceInfo = this.resources.get(resourceId);
    if (resourceInfo) {
      await this.cleanupResource(resourceInfo);
      this.resources.delete(resourceId);
    }
  }
  
  static async releaseAllResources() {
    const releasePromises = Array.from(this.resources.keys()).map(id => 
      this.releaseResource(id)
    );
    await Promise.all(releasePromises);
  }
}
```

### Test Data Validation

#### Data Integrity Checks

```javascript
class TestDataValidator {
  static async validateTestData(entityName, entityData) {
    // Validate required fields
    const requiredFields = await this.getRequiredFields(entityName);
    for (const field of requiredFields) {
      if (!entityData[field]) {
        throw new Error(`Required field missing: ${field} for entity ${entityName}`);
      }
    }
    
    // Validate relationships
    await this.validateRelationships(entityName, entityData);
    
    // Validate business rules
    await this.validateBusinessRules(entityName, entityData);
  }
  
  static async validateTestEnvironment() {
    // Check database connectivity
    await this.checkDatabaseConnection();
    
    // Verify required services are available
    await this.checkServiceAvailability();
    
    // Validate user permissions
    await this.checkUserPermissions();
    
    // Verify reference data exists
    await this.checkReferenceData();
  }
  
  static async validateTestCleanup(testContext) {
    // Verify test data was properly cleaned up
    const remainingData = await this.checkRemainingTestData(testContext);
    if (remainingData.length > 0) {
      console.warn(`Test cleanup incomplete. Remaining entities: ${remainingData}`);
    }
  }
}
```

### Error Handling and Recovery

#### Robust Error Handling

```javascript
class TestDataErrorHandler {
  static async handleServiceError(error, serviceName, parameters) {
    console.error(`Service error in ${serviceName}:`, error);
    
    // Log context for debugging
    await this.logErrorContext(serviceName, parameters, error);
    
    // Attempt recovery if possible
    if (this.isRecoverableError(error)) {
      return await this.attemptRecovery(serviceName, parameters, error);
    }
    
    // Re-throw if not recoverable
    throw error;
  }
  
  static isRecoverableError(error) {
    // Define recoverable error conditions
    const recoverableErrors = [
      'timeout',
      'temporary_service_unavailable',
      'deadlock'
    ];
    
    return recoverableErrors.some(type => 
      error.message.toLowerCase().includes(type)
    );
  }
  
  static async attemptRecovery(serviceName, parameters, error) {
    // Wait before retry
    await TestDataCache.delay(2000);
    
    // Retry with fresh connection/context
    return await ServiceCallManager.safeServiceCall(serviceName, parameters, 1);
  }
}
```

## Usage Examples

### Basic Test Data Generation

```javascript
test('should create invoice with valid data', async ({ page }) => {
  // Generate complete test environment
  const testEnv = await MoquiTestDataFactory.createBaseTestEnvironment({
    orgName: 'Test Invoice Company'
  });
  
  // Create specific test scenario
  const invoiceData = await MoquiTestDataFactory.createTestScenario('Invoice', 'standard', {
    organizationPartyId: testEnv.organizationId
  });
  
  // Navigate and perform test
  await page.goto(`/Invoice/create`);
  await page.fill('#customerId', invoiceData.customerId);
  // ... rest of test
});
```

### Complex Multi-Entity Scenarios

```javascript
test('should handle complete order-to-invoice workflow', async ({ page }) => {
  // Create comprehensive test data
  const testData = await ConfigDrivenFactory.createEntityData('Order', 'complete', {
    includeCustomer: true,
    includeProducts: true,
    includeShipping: true,
    orderItems: [
      { productId: 'TEST-PRODUCT-1', quantity: 2 },
      { productId: 'TEST-PRODUCT-2', quantity: 1 }
    ]
  });
  
  // Execute multi-step workflow
  await page.goto(`/Order/${testData.orderId}`);
  // ... workflow steps
});
```

### Performance-Optimized Testing

```javascript
test.describe('Bulk operations', () => {
  let sharedTestData;
  
  test.beforeAll(async () => {
    // Create shared data once for all tests in describe block
    sharedTestData = await TestDataCache.getOrCreate(
      'bulk-test-environment',
      () => MoquiTestDataFactory.createBaseTestEnvironment({
        includeProducts: 100,
        includeCustomers: 50
      }),
      120 // 2 hour TTL
    );
  });
  
  test('should handle large product catalog', async ({ page }) => {
    // Use shared test data
    await page.goto(`/Product/catalog`);
    // ... test with pre-created products
  });
});
```

## Best Practices

### 1. **Test Data Design**
- Create realistic data that represents actual business scenarios
- Use meaningful names and identifiers for debugging
- Include edge cases and boundary conditions
- Maintain referential integrity and business rule compliance

### 2. **Performance Considerations**
- Cache expensive operations like certificate generation
- Use bulk operations for large datasets
- Implement data reuse strategies for expensive setup
- Monitor test execution time and optimize bottlenecks

### 3. **Isolation and Cleanup**
- Always clean up test data to prevent accumulation
- Use proper database isolation strategies
- Avoid dependencies between tests
- Implement robust cleanup even when tests fail

### 4. **Maintainability**
- Use configuration-driven approaches for flexibility
- Document test data requirements and dependencies
- Implement clear error messages and debugging support
- Regularly review and refactor test data generation code

### 5. **Debugging and Monitoring**
- Log test data creation and cleanup operations
- Provide clear error messages with context
- Monitor resource usage and performance metrics
- Implement health checks for test environment

## Integration with Moqui Testing Framework

This E2E test data strategy is designed to complement Moqui's existing testing infrastructure:

- **Unit Tests (Spock)**: Continue using entity auto-services and direct database operations
- **Integration Tests**: Use lightweight service calls for cross-service testing
- **E2E Tests (Playwright)**: Use this comprehensive data strategy for full workflow testing

The strategy leverages Moqui's strengths while providing the additional structure needed for reliable E2E testing at scale.

---

*This strategy provides a foundation that can be adapted to any Moqui Framework project by configuring entity-specific generators, business domain scenarios, and cleanup procedures appropriate to the specific application domain.*