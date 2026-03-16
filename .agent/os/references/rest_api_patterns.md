# Moqui REST API Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/rest-api.md` - REST API URL and response standards
- `standards/backend/http-standards.md` - HTTP status codes, date formats

---

## Resource Definition Templates

### Basic Resource Definition
```xml
<resource name="customers">
    <method type="get"><service name="get#CustomerList"/></method>
    <method type="post"><service name="create#Customer"/></method>

    <id name="customerId">
        <method type="get"><service name="get#Customer"/></method>
        <method type="put"><service name="update#Customer"/></method>
        <method type="patch"><service name="patch#Customer"/></method>
        <method type="delete"><service name="delete#Customer"/></method>
    </id>
</resource>
```

**NOTE**: `require-authentication="true"` is the DEFAULT. Only specify when setting to `"false"` or `"anonymous-all"`.

---

## Pagination Implementation

### Offset-Based Pagination Service
```xml
<service verb="get" noun="CustomerList">
    <in-parameters>
        <parameter name="offset" type="Integer" default="0"/>
        <parameter name="limit" type="Integer" default="20"/>
        <parameter name="maxLimit" type="Integer" default="100"/>
    </in-parameters>
    <out-parameters>
        <parameter name="customerList" type="List"/>
        <parameter name="totalCount" type="Long"/>
        <parameter name="offset" type="Integer"/>
        <parameter name="limit" type="Integer"/>
    </out-parameters>
    <actions>
        <if condition="limit &gt; maxLimit"><set field="limit" from="maxLimit"/></if>

        <entity-find entity-name="Customer" list="customerList" offset="offset" limit="limit">
            <order-by field-name="lastName"/>
        </entity-find>

        <entity-find-count entity-name="Customer" count-field="totalCount"/>
    </actions>
</service>
```

---

## Authentication Patterns

### Public Endpoints
```xml
<resource name="status" require-authentication="false">
    <method type="get"><service name="get#ApiStatus"/></method>
</resource>

<resource name="public-data" require-authentication="anonymous-view">
    <method type="get"><service name="get#PublicData"/></method>
</resource>
```

### Custom API Key Authentication
```xml
<service verb="authenticate" noun="ApiKey">
    <actions>
        <set field="apiKey" from="ec.web.request.getHeader('X-API-Key')"/>
        <if condition="!apiKey">
            <set field="apiKey" from="ec.web.request.getHeader('Authorization')?.replace('Bearer ', '')"/>
        </if>

        <entity-find-one entity-name="moqui.security.UserLoginKey" value-field="userLoginKey">
            <field-map field-name="loginKey" from="apiKey"/>
            <field-map field-name="fromDate" operator="less-equals" from="ec.user.nowTimestamp"/>
            <field-map field-name="thruDate" operator="greater" from="ec.user.nowTimestamp" or-null="true"/>
        </entity-find-one>

        <if condition="!userLoginKey">
            <return error="true" message="Invalid API key" type="authentication"/>
        </if>

        <set field="ec.user.userId" from="userLoginKey.userId"/>
    </actions>
</service>
```

---

## File Download Pattern

```xml
<service verb="get" noun="DteXml">
    <in-parameters>
        <parameter name="dteId" required="true"/>
    </in-parameters>
    <actions>
        <entity-find-one entity-name="mycompany.myapp.Document" value-field="dte">
            <field-map field-name="fiscalTaxDocumentId" from="dteId"/>
        </entity-find-one>
        <if condition="!dte">
            <return error="true" message="DTE not found"/>
        </if>

        <set field="fileName" value="${dte.issuerPartyIdValue}-${dte.documentType.enumCode}-${dte.fiscalTaxDocumentNumber}.xml"/>

        <script><![CDATA[
            ec.web.response.setContentType('application/xml; charset=UTF-8')
            ec.web.response.setHeader('Content-Disposition', "attachment; filename=\"${fileName}\"")
            ec.web.response.setHeader('Cache-Control', 'public, max-age=3600')

            def contentRef = ec.resource.getLocationReference(content.contentLocation)
            InputStream inputStream = contentRef.openStream()
            try {
                org.moqui.util.ObjectUtilities.copyStream(inputStream, ec.web.response.outputStream)
            } finally {
                inputStream.close()
                ec.web.response.outputStream.close()
            }
        ]]></script>
    </actions>
</service>
```

---

## API Versioning Pattern

### URL Path Versioning (Recommended)
```xml
<resource name="api">
    <resource name="v1">
        <resource name="customers">
            <!-- v1 implementation -->
        </resource>
    </resource>
    <resource name="v2">
        <resource name="customers">
            <!-- v2 implementation -->
        </resource>
    </resource>
</resource>
```

### Deprecation Headers
```xml
<set field="ec.web.response.headers.Warning" value="299 - 'This version will be deprecated on 2025-12-31'"/>
<set field="ec.web.response.headers.Sunset" value="Sat, 31 Dec 2025 23:59:59 GMT"/>
```

---

## Security Headers Pattern

```xml
<service verb="set" noun="SecurityHeaders">
    <actions>
        <set field="ec.web.response.headers['X-Content-Type-Options']" value="nosniff"/>
        <set field="ec.web.response.headers['X-Frame-Options']" value="DENY"/>
        <set field="ec.web.response.headers['X-XSS-Protection']" value="1; mode=block"/>
    </actions>
</service>
```

---

## CORS Configuration Pattern

```xml
<service verb="configure" noun="Cors">
    <actions>
        <set field="origin" from="ec.web.request.getHeader('Origin')"/>
        <set field="allowedOrigins" from="['https://app.example.com']"/>

        <if condition="allowedOrigins.contains(origin)">
            <set field="ec.web.response.headers['Access-Control-Allow-Origin']" from="origin"/>
        </if>

        <set field="ec.web.response.headers['Access-Control-Allow-Methods']" value="GET,POST,PUT,PATCH,DELETE,OPTIONS"/>
        <set field="ec.web.response.headers['Access-Control-Allow-Headers']" value="Content-Type,Authorization,X-API-Key"/>
        <set field="ec.web.response.headers['Access-Control-Max-Age']" value="3600"/>
    </actions>
</service>
```

---

## Rate Limiting Pattern

```xml
<service verb="check" noun="RateLimit">
    <in-parameters>
        <parameter name="identifier" required="true"/>
        <parameter name="maxRequests" type="Integer" default="100"/>
        <parameter name="windowSeconds" type="Integer" default="3600"/>
    </in-parameters>
    <out-parameters>
        <parameter name="allowed" type="Boolean"/>
        <parameter name="remaining" type="Integer"/>
    </out-parameters>
    <actions>
        <!-- Rate limiting implementation -->
        <if condition="!allowed">
            <set field="ec.web.response.headers['X-RateLimit-Remaining']" value="0"/>
            <set field="ec.web.response.headers['Retry-After']" value="${resetSeconds}"/>
            <return error="true" message="Rate limit exceeded"/>
        </if>
    </actions>
</service>
```

---

## Quality Checklist

**Design:**
- [ ] Resource-oriented URLs
- [ ] Appropriate HTTP methods
- [ ] Correct status codes
- [ ] RFC 3339 date formats
- [ ] Avoid reserved parameter names (`result`, `messages`, `errors`)

**Security:**
- [ ] Authentication configured
- [ ] Rate limiting implemented
- [ ] Security headers set
- [ ] CORS properly configured

**Performance:**
- [ ] Pagination for lists
- [ ] Caching headers for downloads
- [ ] Stream large files

**Documentation:**
- [ ] API versioning strategy
- [ ] Error response format
- [ ] Deprecation notices