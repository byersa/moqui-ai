# HTTP Standards

### HTTP Status Codes

| Code | Meaning | When to Use |
|------|---------|-------------|
| `200` | OK | Successful GET, PUT, PATCH |
| `201` | Created | Successful POST (new resource) |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Validation error, malformed request |
| `401` | Unauthorized | Authentication required/failed |
| `403` | Forbidden | Authenticated but not authorized |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Duplicate, state conflict |
| `422` | Unprocessable Entity | Semantic validation error |
| `500` | Internal Server Error | Server-side failure |

### Date Formats

**ISO 8601 for API exchanges:**
```
Date: 2024-01-15
DateTime: 2024-01-15T14:30:00Z
DateTime with offset: 2024-01-15T14:30:00-03:00
```

**Timestamp fields:**
```json
{
  "createdDate": "2024-01-15T14:30:00Z",
  "lastUpdatedStamp": "2024-01-15T15:45:30Z"
}
```

### Request/Response Format

**Always JSON:**
```
Content-Type: application/json
Accept: application/json
```

### Pagination Response

```json
{
  "data": [...],
  "pageIndex": 0,
  "pageSize": 20,
  "totalCount": 150,
  "pageCount": 8
}
```

### Error Response Format

```json
{
  "errors": ["Order not found with ID: ORD123"],
  "errorCode": "NOT_FOUND"
}
```

**Validation errors:**
```json
{
  "errors": [
    "Field 'email' is required",
    "Field 'quantity' must be positive"
  ],
  "errorCode": "VALIDATION_ERROR"
}
```

### URL Standards

**Resource naming:**
- Use plural nouns: `/orders`, `/customers`
- Use kebab-case for multi-word: `/order-items`
- Nest for relationships: `/orders/{orderId}/items`

**Moqui convention:**
- CamelCase paths: `/rest/s1/example/Orders`
- Version in path: `/rest/s1/` (s1 = schema version 1)

### Method Usage

| Method | Purpose | Request Body | Response |
|--------|---------|--------------|----------|
| GET | Read | No | Resource data |
| POST | Create | Yes | Created resource + ID |
| PUT | Full update | Yes | Updated resource |
| PATCH | Partial update | Yes | Updated resource |
| DELETE | Remove | No | 204 No Content |

### Query Parameters

**Standard parameters:**
```
GET /orders?statusId=OrdActive&pageIndex=0&pageSize=20&orderByField=orderDate
```

| Parameter | Purpose |
|-----------|---------|
| `pageIndex` | Page number (0-based) |
| `pageSize` | Items per page |
| `orderByField` | Sort field |
| `orderByDescending` | Sort direction |

### Authentication Headers

**API Key:**
```
X-API-Key: your-api-key
```

**Basic Auth:**
```
Authorization: Basic base64(username:password)
```

### Response Headers

```
X-Total-Count: 150
X-Page-Size: 20
X-Page-Index: 0
```

### CORS Headers (when needed)

```
Access-Control-Allow-Origin: https://trusted-domain.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, X-API-Key
```

### Service-to-HTTP Mapping

```xml
<service verb="find" noun="Orders">
    <out-parameters>
        <parameter name="orderList" type="List"/>
        <parameter name="totalCount" type="Integer"/>
    </out-parameters>
    <actions>
        <!-- 200 OK with data -->
        <!-- 400 Bad Request on validation error -->
        <!-- 500 on server error -->
    </actions>
</service>
```

**Error return maps to 400/422:**
```xml
<return error="true" message="Invalid order status"/>
```

### Best Practices

- Use appropriate status codes (not 200 for everything)
- Return meaningful error messages
- Include pagination metadata for lists
- Use ISO 8601 for dates
- Version your API
- Document all endpoints
