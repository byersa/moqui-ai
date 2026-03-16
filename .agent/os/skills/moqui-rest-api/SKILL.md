---
name: moqui-rest-api
description: |
  Moqui Framework REST API development patterns including resource definitions, authentication, pagination, file downloads, and API versioning.

  Use this skill when:
  - Creating or modifying REST API endpoints (*.rest.xml files)
  - Designing REST resource hierarchies
  - Implementing API authentication (API keys, tokens)
  - Adding pagination to list endpoints
  - Creating file download endpoints
  - Configuring CORS and security headers
  - Implementing API versioning
---

# Moqui REST API Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/rest_api_patterns.md` | REST endpoints, authentication, pagination, file downloads, versioning |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"#### Understanding Filter Context Population"** - Setting up filter context in REST pre-service
- **"#### File Downloads in Transitions"** - Streaming files, Content-Disposition headers
- **"## ServiceJob Authentication Requirements"** - Authentication options for API services
- **"#### Context Persistence: Request vs Session Scope"** - Understanding request vs session context in APIs

## Quick Reference

### Resource Definition Pattern
```xml
<resource name="customers">
    <method type="get"><service name="get#CustomerList"/></method>
    <method type="post"><service name="create#Customer"/></method>
    <id name="customerId">
        <method type="get"><service name="get#Customer"/></method>
        <method type="put"><service name="update#Customer"/></method>
        <method type="delete"><service name="delete#Customer"/></method>
    </id>
</resource>
```

## Key Principles

1. **Authentication Default**: `require-authentication="true"` is default (omit unless changing)
2. **Pagination**: Return offset, limit, totalCount for list endpoints
3. **File Downloads**: Set Content-Disposition and stream with ObjectUtilities.copyStream
4. **Versioning**: Use URL path versioning (/api/v1/, /api/v2/)