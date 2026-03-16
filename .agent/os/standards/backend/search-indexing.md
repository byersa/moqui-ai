# Search Indexing Patterns (OpenSearch/Elasticsearch)

DataDocuments, DataFeeds, and search integration.

## DataDocument Definition

Define what data to index:

```xml
<moqui.entity.document.DataDocument dataDocumentId="MantleOrder"
    indexName="mantle" documentName="Order"
    primaryEntityName="mantle.order.OrderHeader">

    <!-- Fields to index from primary entity -->
    <fields dataDocumentFieldId="MOrdOrderId" fieldPath="orderId"/>
    <fields dataDocumentFieldId="MOrdPlacedDate" fieldPath="placedDate"/>
    <fields dataDocumentFieldId="MOrdStatusId" fieldPath="statusId"/>
    <fields dataDocumentFieldId="MOrdGrandTotal" fieldPath="grandTotal"/>

    <!-- Fields from related entities (dot notation) -->
    <fields dataDocumentFieldId="MOrdCustName" fieldPath="OrderPart.Party.organizationName"/>
    <fields dataDocumentFieldId="MOrdCustPartyId" fieldPath="OrderPart.customerPartyId"/>

    <!-- Nested document for order items -->
    <fields dataDocumentFieldId="MOrdItems" fieldPath="OrderPart.OrderItem:*"
        fieldNameAlias="items"/>
</moqui.entity.document.DataDocument>
```

## DataDocument Conditions

Filter what gets indexed:

```xml
<moqui.entity.document.DataDocumentCondition dataDocumentId="MantleOrder"
    conditionSeqId="01" fieldNameAlias="statusId"
    operator="in" fieldValue="OrderPlaced,OrderApproved,OrderCompleted"/>
```

## DataFeed Configuration

Connect documents to the search engine:

```xml
<!-- Real-time feed (updates index on entity changes) -->
<moqui.entity.feed.DataFeed dataFeedId="MantleSearch"
    dataFeedTypeEnumId="DTFDTP_RT_PUSH"
    feedName="Mantle Search"
    feedReceiveServiceName="org.moqui.search.SearchServices.index#DataDocuments"
    feedDeleteServiceName="org.moqui.search.SearchServices.delete#DataDocument"
    indexOnStartEmpty="Y">
    <documents dataDocumentId="MantleOrder"/>
    <documents dataDocumentId="MantleProduct"/>
</moqui.entity.feed.DataFeed>
```

## Feed Types

| Type | Description |
|------|-------------|
| `DTFDTP_RT_PUSH` | Real-time push on entity changes |
| `DTFDTP_MAN_PULL` | Manual pull through API |

## Searching Documents

```xml
<service verb="search" noun="Orders">
    <in-parameters>
        <parameter name="queryString"/>
        <parameter name="pageIndex" type="Integer" default="0"/>
        <parameter name="pageSize" type="Integer" default="20"/>
    </in-parameters>
    <out-parameters>
        <parameter name="documentList" type="List"/>
        <parameter name="documentListCount" type="Integer"/>
    </out-parameters>
    <actions>
        <service-call name="org.moqui.search.SearchServices.search#DataDocuments"
            in-map="[queryString:queryString, indexName:'mantle',
                    documentType:'Order', pageIndex:pageIndex, pageSize:pageSize]"
            out-map="context"/>
    </actions>
</service>
```

## Direct Elastic API Access

```groovy
// Using ElasticFacade
def elastic = ec.factory.elastic

// Search with query DSL
def result = elastic.search("mantle", [
    query: [
        bool: [
            must: [
                [match: [statusId: "OrderPlaced"]]
            ]
        ]
    ],
    size: 20
])

// Access hits
result.hits.hits.each { hit ->
    def order = hit._source
    ec.logger.info("Found order: ${order.orderId}")
}
```

## Manual Indexing

```xml
<!-- Index all documents for a feed -->
<service-call name="org.moqui.search.SearchServices.index#DataFeedDocuments"
    in-map="[dataFeedId:'MantleSearch', fullIndex:true]"/>

<!-- Index specific document type -->
<service-call name="org.moqui.search.SearchServices.index#DataDocuments"
    in-map="[dataDocumentId:'MantleOrder']"/>
```

## User Group Access

Restrict document access by user group:

```xml
<moqui.entity.document.DataDocumentUserGroup
    dataDocumentId="MantleOrder"
    userGroupId="ORDER_MANAGERS"/>
```

## Field Path Patterns

| Pattern | Description |
|---------|-------------|
| `fieldName` | Field from primary entity |
| `Relationship.fieldName` | Field from related entity |
| `Rel1.Rel2.fieldName` | Nested relationship |
| `Relationship:*` | All fields as nested document |
| `Relationship:field1,field2` | Specific fields as nested |

## Index Naming Convention

| Index Name | Content |
|------------|---------|
| `mantle` | Core business data (orders, products, parties) |
| `moqui` | Framework data (users, audit) |
| `{component}` | Component-specific data |

## Disabling DataFeed

The framework provides `ArtifactExecutionFacade.disableEntityDataFeed()` / `enableEntityDataFeed()` to suppress real-time DataFeed triggers. Use this when performing bulk entity modifications where indexing should be deferred or skipped:

```groovy
boolean reenableDataFeed = !ec.getArtifactExecution().disableEntityDataFeed()
try {
    // Entity updates here — real-time DataFeed triggers are suppressed
} finally {
    if (reenableDataFeed) ec.getArtifactExecution().enableEntityDataFeed()
}
```

**Common use cases**:
- **ToolFactory init()**: ElasticFacade may not be available yet during startup (see `framework-guide.md` § DataFeed Suppression in ToolFactory init())
- **Data migrations**: Bulk updates should not trigger per-record indexing
- **EntityDataLoader**: Uses this internally via `disableDataFeed(true)`

## Key Rules

1. **Use DataDocuments** - Don't index entities directly
2. **Real-time feeds for search** - `DTFDTP_RT_PUSH` for immediate updates
3. **Set indexOnStartEmpty** - Auto-index on server start if index missing
4. **Restrict access** - Use `DataDocumentUserGroup` for sensitive data
5. **Use fieldNameAlias** - Create readable field names in documents
6. **Nested for collections** - Use `:*` for one-to-many relationships