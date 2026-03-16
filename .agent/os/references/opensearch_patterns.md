# Moqui OpenSearch/Elasticsearch Patterns

**Standards Reference**: For declarative conventions, see:
- `standards/backend/search-indexing.md` - DataDocument, DataFeed, index configuration

---

This reference provides comprehensive patterns and guidelines for integrating OpenSearch/Elasticsearch with Moqui Framework.

## Architecture Overview

Moqui provides a built-in abstraction for search functionality through:

1. **DataDocument** - Declarative entity-to-document mapping (RECOMMENDED)
2. **DataFeed** - Automatic real-time or periodic data synchronization
3. **SearchServices** - Framework services for indexing and searching
4. **ElasticFacade** - Low-level Java interface (for advanced use cases only)

```
┌─────────────────────────────────────────────────────────────────┐
│  Application Layer                                               │
│  ├── Screens (use SearchServices.search#DataDocuments)          │
│  └── Custom Services (wrap framework services)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  Framework Services (USE THESE)                                  │
│  ├── org.moqui.search.SearchServices.index#DataDocuments         │
│  ├── org.moqui.search.SearchServices.search#DataDocuments        │
│  ├── org.moqui.search.SearchServices.index#DataFeedDocuments     │
│  └── org.moqui.search.SearchServices.delete#DataDocument         │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  DataDocument / DataFeed (Declarative Configuration)             │
│  ├── moqui.entity.document.DataDocument                          │
│  ├── moqui.entity.document.DataDocumentField                     │
│  └── moqui.entity.feed.DataFeed                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  ElasticFacade (Low-level - rarely needed)                       │
│  └── ec.factory.elastic.getDefault() → ElasticClient             │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│  OpenSearch/Elasticsearch Cluster                                │
└─────────────────────────────────────────────────────────────────┘
```

## DataDocument Pattern (RECOMMENDED)

DataDocuments are the **idiomatic Moqui way** to define search indices. They provide:

- **90% less code** than low-level approach
- **Declarative** entity-to-document mapping in XML
- **Automatic** index creation and management
- **Real-time sync** via DataFeed
- **EntityFilter integration** for multi-tenant security
- **Computed fields** via Groovy expressions
- **Complex transformations** via `manualDataServiceName`
- **Custom mappings** via `manualMappingServiceName` (e.g., Spanish analyzer)

### DataDocument Definition (Compact Syntax)

```xml
<!-- data/MyAppSupportSearchData.xml -->
<entity-facade-xml type="seed">
    <!-- Work Order DataDocument -->
    <dataDocuments dataDocumentId="MyAppWorkOrder" indexName="myapp-workorder"
            documentName="Work Order" primaryEntityName="mantle.work.effort.WorkEffort"
            documentTitle="${workEffortName}"
            manualMappingServiceName="WorkOrderSearchServices.transform#WorkOrderMapping">
        <!-- Primary entity fields -->
        <fields fieldSeqId="01" fieldPath="workEffortId"/>
        <fields fieldSeqId="02" fieldPath="workEffortName" fieldNameAlias="title"/>
        <fields fieldSeqId="03" fieldPath="description"/>
        <fields fieldSeqId="04" fieldPath="statusId"/>
        <fields fieldSeqId="05" fieldPath="priority" fieldType="integer"/>
        <fields fieldSeqId="06" fieldPath="purposeEnumId"/>
        <fields fieldSeqId="07" fieldPath="ownerPartyId" fieldNameAlias="organizationId"/>
        <fields fieldSeqId="08" fieldPath="location"/>

        <!-- Related entity fields via relationships -->
        <fields fieldSeqId="10" fieldPath="status:description" fieldNameAlias="statusDescription"/>
        <fields fieldSeqId="11" fieldPath="purpose:description" fieldNameAlias="purposeDescription"/>

        <!-- Party relationships -->
        <fields fieldSeqId="20" fieldPath="parties:partyId"/>
        <fields fieldSeqId="21" fieldPath="parties:roleTypeId"/>
        <fields fieldSeqId="22" fieldPath="parties:person:firstName"/>
        <fields fieldSeqId="23" fieldPath="parties:person:lastName"/>
        <fields fieldSeqId="24" fieldPath="parties:organization:organizationName"/>

        <!-- Filter: only index support tickets -->
        <conditions conditionSeqId="01" fieldNameAlias="workEffortTypeEnumId" fieldValue="WetWorkOrder"/>
    </dataDocuments>

    <!-- DataFeed for real-time indexing -->
    <moqui.entity.feed.DataFeed dataFeedId="MyAppSupportSearch"
            dataFeedTypeEnumId="DTFDTP_RT_PUSH"
            feedName="Support Search Feed"
            indexOnStartEmpty="Y">
        <documents dataDocumentId="MyAppWorkOrder"/>
    </moqui.entity.feed.DataFeed>
</entity-facade-xml>
```

### Computed Fields via Groovy Expressions

DataDocuments support **inline Groovy expressions** for computed fields:

```xml
<!-- Computed field: combine multiple fields into one -->
<fields fieldSeqId="14"
        fieldPath="(&quot;${organizationName?:''}${firstName?:''}${lastName ? ' ' + lastName : ''}&quot;)"
        fieldNameAlias="combinedName" fieldType="text" sortable="Y"/>

<!-- Computed field: format a date -->
<fields fieldSeqId="15"
        fieldPath="(estimatedCompletionDate?.format('yyyy-MM-dd'))"
        fieldNameAlias="dueDate" fieldType="keyword"/>
```

### Complex Transformations via manualDataServiceName

For transformations that can't be done inline, use `manualDataServiceName`:

```xml
<!-- DataDocument with manual data service -->
<dataDocuments dataDocumentId="MantleParty"
        manualDataServiceName="{shared-utils}.OpenSearchServices.add#FormattedField">
    <!-- ... fields ... -->
</dataDocuments>
```

The service implements `org.moqui.EntityServices.add#ManualDocumentData`:

```xml
<service verb="add" noun="FormattedRut" authenticate="anonymous-all">
    <implements service="org.moqui.EntityServices.add#ManualDocumentData"/>
    <actions>
        <!-- document Map is passed in and can be modified -->
        <if condition="document.identifications">
            <iterate list="document.identifications" entry="identification">
                <if condition="identification.partyIdTypeEnumId in ['PtidNationalTaxId']">
                    <service-call name="mycompany.GeneralServices.format#Identifier"
                            in-map="[rut:identification.idValue]" out-map="formatted"/>
                    <set field="identification.formattedIdValue" from="formatted.rut"/>
                </if>
            </iterate>
        </if>
    </actions>
</service>
```

### Custom Index Mappings via manualMappingServiceName

For custom analyzer configuration (e.g., Spanish), use `manualMappingServiceName`:

```xml
<dataDocuments dataDocumentId="MyAppWorkOrder"
        manualMappingServiceName="WorkOrderSearchServices.transform#WorkOrderMapping">
    <!-- ... fields ... -->
</dataDocuments>
```

The service implements `org.moqui.EntityServices.transform#DocumentMapping`:

```xml
<service verb="transform" noun="WorkOrderMapping">
    <implements service="org.moqui.EntityServices.transform#DocumentMapping"/>
    <actions>
        <!-- Add Spanish analyzer settings -->
        <set field="settings" from="[
            analysis: [
                analyzer: [
                    spanish_custom: [
                        type: 'spanish',
                        stopwords: '_spanish_'
                    ]
                ]
            ],
            number_of_shards: 1,
            number_of_replicas: 1
        ]"/>

        <!-- Modify field mappings to use Spanish analyzer -->
        <if condition="mapping.properties?.title">
            <set field="mapping.properties.title.analyzer" value="spanish_custom"/>
        </if>
        <if condition="mapping.properties?.description">
            <set field="mapping.properties.description.analyzer" value="spanish_custom"/>
        </if>
    </actions>
</service>
```

### DataDocumentField Options

| Attribute | Description |
|-----------|-------------|
| `fieldPath` | Path: `fieldName`, `relationship:fieldName`, `rel1:rel2:fieldName`, or `(groovyExpression)` |
| `fieldNameAlias` | Output field name in document (must be unique, required for expressions) |
| `fieldType` | ElasticSearch type: `text`, `keyword`, `integer`, `long`, `double`, `date`, `boolean` |
| `sortable` | `Y` to add `.keyword` field for sorting text fields |
| `defaultDisplay` | `N` to exclude from default display |
| `functionName` | Aggregate function: `min`, `max`, `sum`, `avg`, `count` |

### DataFeed Types

| Type | Description |
|------|-------------|
| `DTFDTP_RT_PUSH` | **Real-time**: Index immediately when entity changes (recommended) |
| `DTFDTP_MAN_PULL` | **Manual**: Index via API call or scheduled job |

## Framework SearchServices

### Searching with DataDocuments

```xml
<service verb="search" noun="WorkOrders">
    <in-parameters>
        <parameter name="queryString" required="true"/>
        <parameter name="organizationId"/>
        <parameter name="pageIndex" type="Integer" default="0"/>
        <parameter name="pageSize" type="Integer" default="20"/>
    </in-parameters>
    <out-parameters>
        <parameter name="documentList" type="List"/>
        <parameter name="documentListCount" type="Integer"/>
        <parameter name="documentListPageMaxIndex" type="Integer"/>
    </out-parameters>
    <actions>
        <!-- Build filtered query string -->
        <set field="filteredQuery" from="queryString"/>
        <if condition="organizationId">
            <set field="filteredQuery" from="queryString + ' AND organizationId:' + organizationId"/>
        </if>

        <!-- Use framework service -->
        <service-call name="org.moqui.search.SearchServices.search#DataDocuments" out-map="context"
                in-map="[indexName:'myapp-workorder', documentType:'MyAppWorkOrder',
                        queryString:filteredQuery, pageIndex:pageIndex, pageSize:pageSize,
                        orderByFields:['-_score', '-lastUpdatedStamp']]"/>
    </actions>
</service>
```

### Indexing with DataFeed

```xml
<!-- Re-index all documents for a DataFeed -->
<service verb="reindex" noun="WorkOrders">
    <actions>
        <service-call name="org.moqui.search.SearchServices.index#DataFeedDocuments"
                in-map="[dataFeedId:'MyAppWorkOrderSearch']"/>
    </actions>
</service>
```

### Query String Syntax

The `queryString` uses Lucene query syntax:

```
# Simple term search
sistema lento

# Field-specific search
title:problema status:WeNew

# Boolean operators
sistema AND urgente
problema OR error
NOT resuelto

# Wildcards
sistem*
pro?lema

# Fuzzy search
sistema~2

# Range queries
priority:[1 TO 3]
createdDate:[2024-01-01 TO 2024-12-31]

# Phrase search
"error de sistema"
```

## When to Use Low-Level ElasticFacade

The low-level `ec.factory.elastic` API should **only** be used when:

1. **Non-entity data sources** - Indexing data from external APIs, files, etc.
2. **Advanced index operations** - Aliases, templates, index lifecycle management
3. **Custom bulk operations** - Optimized batch processing beyond DataFeed capabilities
4. **Direct cluster management** - Health checks, node info, etc.

### Low-Level Example (Advanced Use Case)

```groovy
// ONLY use for non-entity data or advanced operations
def client = ec.factory.elastic.getDefault()
if (client == null) {
    ec.message.addError("OpenSearch not available")
    return
}

// Example: Index external API data
List externalData = fetchFromExternalAPI()
List bulkDocs = externalData.collect { item ->
    [_id: item.externalId, title: item.name, content: item.body]
}
client.bulkIndex("external-data", "_id", bulkDocs)
```

## Multi-Tenant Search Filtering

With DataDocuments, multi-tenant filtering is handled at the search level:

```xml
<service verb="search" noun="MantleFiltered">
    <implements service="org.moqui.search.SearchServices.search#DataDocuments"/>
    <actions>
        <!-- Get organization context -->
        <set field="activeOrgId" from="ec.user.context?.activeOrgId"/>
        <set field="filterOrgIds" from="ec.user.context?.filterOrgIds"/>

        <!-- Add organization filter to query -->
        <if condition="filterOrgIds">
            <set field="queryString"
                 from="(queryString ? queryString + ' AND ' : '') + 'organizationId:(' + filterOrgIds.join(' OR ') + ')'"/>
        </if>

        <!-- Call framework search -->
        <service-call name="org.moqui.search.SearchServices.search#DataDocuments"
                in-map="context" out-map="context"/>
    </actions>
</service>
```

## Configuration

### Moqui Configuration (MoquiConf.xml)

```xml
<elastic-facade>
    <!-- Default cluster (localhost) -->
    <cluster name="default" url="${elasticsearch_url ?: 'http://127.0.0.1:9200'}"/>

    <!-- Secured cluster -->
    <cluster name="production" url="https://opensearch.example.com:9200"
             user="${elasticsearch_user}" password="${elasticsearch_password}"/>
</elastic-facade>
```

### Environment Variables

```bash
elasticsearch_url=http://localhost:9200
elasticsearch_user=admin
elasticsearch_password=secret
elasticsearch_index_prefix=myapp_
```

## Best Practices

### DO:
- **Use DataDocuments** for all entity-based indexing
- **Use DataFeeds** with `DTFDTP_RT_PUSH` for real-time sync
- **Use framework SearchServices** for searching
- **Use `manualDataServiceName`** for computed fields
- **Use `manualMappingServiceName`** for custom analyzers (Spanish, etc.)
- Filter by organization in multi-tenant applications
- Handle missing index gracefully

### DON'T:
- Use low-level ElasticFacade for entity indexing (use DataDocuments)
- Write custom indexing scripts when DataDocuments suffice
- Hardcode index names (use DataDocument.indexName)
- Skip organization filters in multi-tenant apps
- Create indices without proper mappings

## Migration from Low-Level to DataDocument

If you have existing low-level implementation:

1. **Create DataDocument definitions** for each index
2. **Add DataFeed** for real-time sync
3. **Create `manualMappingServiceName`** if custom analyzers needed
4. **Replace custom services** with framework SearchServices
5. **Remove old Groovy scripts**
6. **Re-index** via `index#DataFeedDocuments`

### Code Reduction Example

**Before (Low-level):** ~600 lines across 10 Groovy scripts
**After (DataDocument):** ~100 lines XML + ~30 lines for mapping service