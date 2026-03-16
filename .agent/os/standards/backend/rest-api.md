# REST API Standards

### Endpoint Structure
- **Resource Files**: Define endpoints in `*.rest.xml` files **directly in `service/`** — NOT in subdirectories. The framework only scans direct children of `service/` for `*.rest.xml` files; files in subdirectories will NOT be discovered. A component can have multiple `.rest.xml` files named by API endpoint group (e.g., `orders.rest.xml`, `inventory.rest.xml`).
- **Path Convention**: Use CamelCase paths aligned with Moqui conventions (e.g., `/moqui/orders`, `/mycompany/dte`)
- **Versioning**: Include version in path (e.g., `/rest/s1/`, `/rest/v2/`)

### HTTP Methods
- **GET**: Read operations (entity-find, service calls returning data)
- **POST**: Create operations and complex queries
- **PUT**: Full update operations
- **PATCH**: Partial update operations
- **DELETE**: Remove operations

### Authentication
- **UserLoginKey**: Primary API authentication mechanism
- **Basic Auth**: Username/password for simple integrations
- **API Key Rotation**: Implement key rotation policies
- **Role-Based Access**: Map API keys to UserGroups with appropriate permissions

### Filter Context (CRITICAL)
- **Mandatory Setup**: Every REST service querying filtered entities MUST call filter context setup
- **Example Pattern**:
```xml
<service-call name="setup#FilterContext"
              in-map="context" out-map="context" disable-authz="true"/>
```
- **Fail-Safe**: Use Elvis operator `(filterOrgIds ?: [])` in EntityFilter definitions

### Response Standards
- **JSON Format**: Default response format is JSON
- **Status Codes**: Use appropriate HTTP status codes (200, 201, 400, 401, 403, 404, 500)
- **Error Responses**: Include meaningful error messages in response body
- **Pagination**: Use `pageIndex`, `pageSize`, `totalCount` for list endpoints

### Request Validation
- **Parameter Types**: Specify types in service definitions
- **Required Fields**: Mark required parameters explicitly
- **Input Sanitization**: Validate and sanitize all input data

### Documentation
- **Service Documentation**: Include `description` attributes on services
- **Parameter Documentation**: Document all parameters with descriptions
- **Error Codes**: Document possible error responses

### URL Structure Standards

**Resource paths follow Moqui conventions:**
```
/rest/s1/{component}/{Resource}
/rest/s1/example/Orders
/rest/s1/example/Orders/{orderId}
/rest/s1/example/Orders/{orderId}/Items
```

**Version prefix:**
- `s1` = Schema version 1 (standard)
- Use version for breaking API changes

### Date/Time Format Standards

**Always use ISO 8601 for API exchanges:**

| Type | Format | Example |
|------|--------|---------|
| Date | `YYYY-MM-DD` | `2024-01-15` |
| DateTime | `YYYY-MM-DDTHH:MM:SSZ` | `2024-01-15T14:30:00Z` |
| DateTime+TZ | `YYYY-MM-DDTHH:MM:SS±HH:MM` | `2024-01-15T14:30:00-03:00` |

### HTTP Status Code Reference

| Code | Meaning | Service Behavior |
|------|---------|------------------|
| `200` | OK | Successful GET/PUT/PATCH |
| `201` | Created | Successful POST |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Validation error |
| `401` | Unauthorized | Auth required/failed |
| `403` | Forbidden | Authenticated but not authorized |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Duplicate/state conflict |
| `422` | Unprocessable | Semantic validation error |
| `500` | Server Error | Unexpected failure |

### Standard Response Format

**Success with data:**
```json
{
  "orderId": "ORD001",
  "orderDate": "2024-01-15T14:30:00Z",
  "status": "placed"
}
```

**List with pagination:**
```json
{
  "data": [...],
  "pageIndex": 0,
  "pageSize": 20,
  "totalCount": 150
}
```

**Error response:**
```json
{
  "errors": ["Order not found with ID: ORD123"],
  "errorCode": "NOT_FOUND"
}
```