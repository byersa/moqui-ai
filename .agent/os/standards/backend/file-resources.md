# File and Resource Handling Patterns

Database resources, file storage, and content location patterns.

## Resource Location Protocols

| Protocol | Description | Example |
|----------|-------------|---------|
| `component://` | Component-relative path | `component://example/template/report.ftl` |
| `dbresource://` | Database-stored resource | `dbresource://example/uploads/file.pdf` |
| `classpath://` | Classpath resource | `classpath://org/example/template.xml` |
| `content://` | Content repository | `content://example/documents/123` |

## Storing Files in Database (DbResource)

```xml
<service verb="store" noun="UploadedFile">
    <in-parameters>
        <parameter name="filename" required="true"/>
        <parameter name="fileData" type="byte[]" required="true"/>
        <parameter name="mimeType"/>
        <parameter name="parentPath" default-value="uploads"/>
    </in-parameters>
    <out-parameters>
        <parameter name="resourceId"/>
    </out-parameters>
    <actions>
        <!-- Find or create parent folder -->
        <service-call name="org.moqui.impl.WikiServices.get#DbResourceId"
            in-map="[pathList:parentPath.split('/') as List, createIfMissing:true]"
            out-map="parentResult"/>

        <!-- Create file resource -->
        <service-call name="create#moqui.resource.DbResource" out-map="context"
            in-map="[parentResourceId:parentResult.resourceId, filename:filename, isFile:'Y']"/>

        <!-- Store file data -->
        <service-call name="create#moqui.resource.DbResourceFile"
            in-map="[resourceId:resourceId, fileData:fileData, mimeType:mimeType,
                    versionName:'1.0', rootVersionName:'1.0']"/>
    </actions>
</service>
```

## Simplified Storage with putText/putBytes

For simpler file storage that automatically creates the directory hierarchy:

### putText() for Text Content

```groovy
// Automatically creates directory hierarchy in DbResource
def contentLocation = "dbresource://example/documents/${documentId}/content.html"
def resourceRef = ec.resource.getLocationReference(contentLocation)
resourceRef.putText(htmlContent)

// Read back
def text = ec.resource.getLocationText(contentLocation, true)
```

### putBytes() for Binary Content

```groovy
// Store binary content
def pdfLocation = "dbresource://documents/${documentId}/report.pdf"
def resourceRef = ec.resource.getLocationReference(pdfLocation)
resourceRef.putBytes(pdfBytes)

// Read back (with proper stream closing)
def stream = resourceRef.openStream()
try {
    bytes = stream.readAllBytes()
} finally {
    stream?.close()
}
```

### When to Use

| Pattern | Use Case |
|---------|----------|
| **putText()/putBytes()** | Simple storage, single files, auto-create directories |
| **WikiServices + entity creation** | Custom versioning, explicit control, complex metadata |

**Note**: putText()/putBytes() creates all parent directories automatically - no need to call WikiServices.get#DbResourceId first.

## Reading Database Resources

```groovy
// Read file from dbresource (with proper stream closing)
def resourceRef = ec.resource.getLocationReference("dbresource://example/uploads/file.pdf")
if (resourceRef.exists) {
    def stream = resourceRef.openStream()
    try {
        bytes = stream.readAllBytes()
    } finally {
        stream?.close()
    }
    def mimeType = resourceRef.contentType
}
```

## Content Location Field Pattern

```xml
<entity entity-name="Document" package="example">
    <field name="documentId" type="id" is-pk="true"/>
    <field name="contentLocation" type="text-medium"/>
    <!-- Store as: dbresource://documents/${documentId}/file.pdf -->
</entity>
```

## File Upload in Screen

```xml
<form-single name="UploadForm" transition="uploadFile">
    <field name="file">
        <default-field title="Select File">
            <file/>
        </default-field>
    </field>
    <field name="submit"><default-field><submit text="Upload"/></default-field></field>
</form-single>
```

```xml
<transition name="uploadFile" method="post">
    <actions>
        <set field="filename" from="file.filename"/>
        <set field="fileBytes" from="file.fileItem.get()"/>
        <set field="mimeType" from="file.contentType"/>

        <service-call name="example.FileServices.store#UploadedFile"
            in-map="[filename:filename, fileData:fileBytes, mimeType:mimeType]"/>
    </actions>
    <default-response url="."/>
</transition>
```

## Serving Files

```xml
<transition name="downloadFile">
    <actions>
        <entity-find-one entity-name="example.Document" value-field="doc"/>
        <script>
            def resourceRef = ec.resource.getLocationReference(doc.contentLocation)
            ec.web.response.setContentType(resourceRef.contentType ?: "application/octet-stream")
            ec.web.response.setHeader("Content-Disposition",
                "attachment; filename=\"${doc.filename}\"")
            resourceRef.openStream().withStream { ec.web.response.outputStream << it }
        </script>
    </actions>
    <default-response type="none"/>
</transition>
```

## Resource Expansion

```xml
<!-- Expand resource location with variables -->
<set field="templateLocation" value="component://example/template/${templateType}.ftl"/>
<set field="templateText" from="ec.resource.getLocationText(templateLocation, true)"/>

<!-- Render FTL template -->
<set field="rendered" from="ec.resource.expand(templateLocation, '', context)"/>
```

## File History (Versioning)

DbResource supports automatic versioning:

```xml
<!-- File history is tracked in DbResourceFileHistory -->
<entity-find entity-name="moqui.resource.DbResourceFileHistory" list="historyList">
    <econdition field-name="resourceId" from="resourceId"/>
    <order-by field-name="versionDate" descending="true"/>
</entity-find>
```

## Key Rules

1. **Use protocols** - `component://`, `dbresource://` for portability
2. **Store location, not data** - Entity fields store location references
3. **DbResource for user files** - User uploads go to database storage
4. **Component for templates** - Static templates in component files
5. **Set mime type** - Always store/return correct content type
6. **Handle large files** - Stream instead of loading into memory
7. **Prefer putText()/putBytes()** - Use simpler methods unless you need custom versioning
8. **Never hardcode storage base URLs** - Always use a storage path service or environment variable lookup. Hardcoding `dbresource://...` as a fallback bypasses production configuration (e.g., `{BUCKET_LOCATION}` env var pointing to `aws3://`). Even for edge cases (unknown party, orphaned records), resolve the base URL from configuration.
9. **Always close streams** - See Stream Management section below

## Stream Management and Resource Cleanup

**CRITICAL**: Streams opened via `ResourceReference.openStream()` MUST be explicitly closed. This is especially important for S3/cloud storage backends where unclosed streams cause:
- Connection pool exhaustion
- Memory pressure from held references
- Warning logs: `S3StreamWrapper finalized without being closed!`

### The Anti-Pattern (NEVER DO THIS)

```groovy
// ❌ BAD: Stream is never closed - causes resource leaks
def bytes = resourceRef.openStream().readAllBytes()

// ❌ BAD: Stream passed to parser may not be closed on error
def xml = new XmlParser().parse(resourceRef.openStream())

// ❌ BAD: Base64 encoding with unclosed stream
def encoded = Base64.encoder.encodeToString(resourceRef.openStream().readAllBytes())
```

### The Correct Pattern (ALWAYS USE THIS)

```groovy
// ✅ GOOD: Always use try/finally to close streams
def stream = resourceRef.openStream()
try {
    bytes = stream.readAllBytes()
} finally {
    stream?.close()
}

// ✅ GOOD: For XML parsing
def stream = resourceRef.openStream()
try {
    xml = new XmlParser(false, false).parse(stream)
} finally {
    stream?.close()
}

// ✅ GOOD: For Base64 encoding
def stream = resourceRef.openStream()
try {
    encoded = Base64.encoder.encodeToString(stream.readAllBytes())
} finally {
    stream?.close()
}
```

### Groovy withStream Alternative

Groovy's `withStream` closure automatically closes the stream:

```groovy
// ✅ GOOD: withStream auto-closes
resourceRef.openStream().withStream { stream ->
    ec.web.response.outputStream << stream
}
```

### In Moqui XML Services

When using `openStream()` in XML service definitions, wrap in a script block:

```xml
<!-- ✅ GOOD: Proper stream handling in XML service -->
<script><![CDATA[
    def stream = resourceRef.openStream()
    try {
        content = Base64.encoder.encodeToString(stream.readAllBytes())
    } finally {
        stream?.close()
    }
]]></script>

<!-- ❌ BAD: Inline expression leaks stream -->
<set field="content" from="Base64.encoder.encodeToString(resourceRef.openStream().readAllBytes())"/>
```

### Common Scenarios Requiring Stream Closure

| Scenario | Pattern |
|----------|---------|
| Reading file bytes | `try/finally` with `readAllBytes()` |
| XML parsing | `try/finally` with `XmlParser.parse()` |
| Base64 encoding | `try/finally` with encoder |
| HTTP file upload | `try/finally` before building request body |
| XSL-FO transformation | Store stream in variable, close in finally |
| Streaming to response | Use `withStream` or explicit close |

### Why This Matters for S3/Cloud Storage

The `S3StreamWrapper` class tracks stream lifecycle. When streams are garbage collected without being closed:
1. A warning is logged with the location and creation stack trace
2. The underlying HTTP connection may not be properly released
3. Under load, this leads to connection pool exhaustion

The warning message format:
```
S3StreamWrapper finalized without being closed! Location: aws3://bucket/path/file.xml,
Version: null, Age: 50668ms. Created at: org.codehaus.groovy.vmplugin.v8.IndyInterface.fromCache(...)
```

### getText() Is Safe

The `ResourceReference.getText()` method handles stream closure internally for most implementations, so it's safe to use directly:

```groovy
// ✅ SAFE: getText() manages stream internally
def text = resourceRef.getText()
def template = ec.resource.getLocationText(location, true)
```

### Storage URL Anti-pattern

```groovy
// BAD: hardcoded fallback ignores production S3 config
if (!receiverPartyId) {
    storageBaseUrl = "dbresource://{shared-component}/dte/unknown"
}

// GOOD: use a dedicated service that reads env var / config
if (!receiverPartyId) {
    result = ec.service.sync().name("...get#UnrelatedDocumentStoragePath")
        .parameters([envioId:envioId, documentType:docType]).call()
    storagePath = result.storagePath
}
```

## Hierarchical Storage Path Patterns

For document types with potentially large numbers of files, use hierarchical directory structures based on ID subdivision to limit files per directory.

### Thousands-Based Hierarchy

Divide numeric IDs by 1000 to create subdirectories:

```groovy
// envioId 330219 → directory "330"
// envioId 42 → directory "0"
long absId = Math.abs(envioId)
int thousands = (int)(absId / 1000)
storagePath = "${baseUrl}/envio/${thousands}"
fullPath = "${storagePath}/${cleanRut}-${envioId}-${enumCode}.xml"
```

**Result**: `envio/330/76896389-330219-EnvioDte.xml`

### RUT-Based Hierarchy

For Chilean RUT numbers, parse the formatted RUT to create natural groupings:

```groovy
// RUT 76.896.389-5 → hierarchy "76/896"
def parts = rutEmisor.split(/[.\-]/)
rutHierarchy = "${parts[0]}/${parts[1]}"
storagePath = "${baseUrl}/certificadoCesion/${rutHierarchy}/${assignmentThousands}"
```

**Result**: `certificadoCesion/76/896/12/12345-42.html`

### Multi-Format Documents (Extension Parameter)

When a document exists in multiple formats (HTML/PDF), use an extension parameter to store them side-by-side:

```xml
<parameter name="extension" default="html">
    <description>File extension: html (default) or pdf</description>
</parameter>
```

```groovy
// Ensure extension has a value (script-level fallback for safety)
if (!extension) extension = 'html'

fullPath = "${storagePath}/${documentId}-${folio}.${extension}"
```

**Benefits**:
- Same storage path service for both formats
- Files stored in same directory for easy correlation
- Caller controls format without path logic duplication

### Storage Path Service Pattern

Create dedicated services that return both `storagePath` (directory) and `fullPath` (complete location):

```xml
<service verb="get" noun="DocumentStoragePath">
    <in-parameters>
        <parameter name="partyId" required="true"/>
        <parameter name="documentId" required="true"/>
        <parameter name="extension" default="html"/>
    </in-parameters>
    <out-parameters>
        <parameter name="storagePath"/>
        <parameter name="fullPath"/>
        <parameter name="thousands" type="Integer"/>
    </out-parameters>
    <actions>
        <!-- Build hierarchical path -->
    </actions>
</service>
```

## Explicit Content Location Override

When storing content, services may accept an optional `contentLocation` parameter to allow callers to specify exact storage location instead of auto-generating.

### Pattern: Optional contentLocation Parameter

```xml
<service verb="store" noun="DocumentContent">
    <in-parameters>
        <parameter name="documentId" required="true"/>
        <parameter name="documentContent" type="Object" required="true"/>
        <parameter name="contentLocation">
            <description>Optional: explicit storage location. When provided, skips automatic path generation.</description>
        </parameter>
    </in-parameters>
    <actions>
        <!-- Use explicit contentLocation if provided, otherwise generate -->
        <if condition="!contentLocation">
            <service-call name="...get#DocumentStoragePath" out-map="pathResult"/>
            <set field="contentLocation" from="pathResult.fullPath"/>
        </if>

        <set field="resourceRef" from="ec.resource.getLocationReference(contentLocation)"/>
        <script>resourceRef.putBytes(documentContent)</script>
    </actions>
</service>
```

### Use Case: Derived Paths

When storing a related document (e.g., PDF version of HTML), derive the path from the original:

```groovy
// HTML stored at: certificadoCesion/76/896/12/12345-42.html
// Derive PDF path by replacing extension
def pdfLocation = htmlLocation.replaceAll('\\.html$', '.pdf')

// Pass explicit location to storage service
ec.service.sync().name("store#DocumentContent")
    .parameters([documentId: docId, documentContent: pdfBytes, contentLocation: pdfLocation])
    .call()
```

**Benefits**:
- Ensures related files are co-located
- Avoids re-querying metadata to generate path
- Caller has full control when needed
- Backward compatible (parameter is optional)