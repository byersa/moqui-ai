---
name: moqui-opensearch
description: |
  Moqui Framework OpenSearch/Elasticsearch integration patterns including DataDocuments, DataFeeds, search services, and index configuration.

  Use this skill when:
  - Creating DataDocument definitions for search indexing
  - Configuring DataFeeds for real-time sync
  - Implementing search services using SearchServices
  - Adding custom analyzers (Spanish, etc.) via manualMappingServiceName
  - Creating computed fields via manualDataServiceName
  - Integrating multi-tenant search filtering
---

# Moqui OpenSearch Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/opensearch_patterns.md` | DataDocuments, DataFeeds, SearchServices, ElasticFacade, custom mappings |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## ElasticSearch/OpenSearch"** - Framework integration overview, JVM optimization

## Quick Reference

### DataDocument Definition
```xml
<dataDocuments dataDocumentId="MyAppWorkOrder" indexName="myapp-workorder"
        primaryEntityName="mantle.work.effort.WorkEffort" documentTitle="${workEffortName}">
    <fields fieldSeqId="01" fieldPath="workEffortId"/>
    <fields fieldSeqId="02" fieldPath="workEffortName" fieldNameAlias="title"/>
    <fields fieldSeqId="03" fieldPath="status:description" fieldNameAlias="statusDescription"/>
    <conditions conditionSeqId="01" fieldNameAlias="workEffortTypeEnumId" fieldValue="WetWorkOrder"/>
</dataDocuments>
```

## Key Principles

1. **DataDocuments First**: Use declarative DataDocuments over low-level ElasticFacade
2. **Real-time Sync**: Use DataFeed with `DTFDTP_RT_PUSH` for automatic indexing
3. **Framework Services**: Use SearchServices.search#DataDocuments, not direct API calls
4. **Multi-tenant**: Filter by organization in search queries