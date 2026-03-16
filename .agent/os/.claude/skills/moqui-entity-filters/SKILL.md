---
name: moqui-entity-filters
description: |
  Moqui Framework EntityFilter patterns for row-level security including filter definitions, context setup, view-entity propagation, and debugging missing data issues.

  Use this skill when:
  - Configuring EntityFilterSet and EntityFilter definitions in data files
  - Setting up filter context in REST APIs and services
  - Debugging missing data issues in screens (filter context not set)
  - Testing with authorization enabled
  - Understanding view-entity filter propagation
  - Implementing fail-safe filter patterns
---

# Moqui EntityFilter Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/entity_filter_patterns.md` | EntityFilter configuration, context setup, view-entity propagation, debugging, testing |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Entity Access Control Patterns"** - Core principle of separation of concerns
- **"### EntityFilters vs Direct Constraints"** - When to use each approach
- **"### EntityFilter Setup Patterns"** - Basic configuration, common patterns
- **"#### Understanding Filter Context Population"** - Per-request context setup, service patterns
- **"### Testing EntityFilter-Enabled Services"** - Test setup, authorization-enabled testing
- **"## Security and Authorization Patterns"** - Entity Filter vs Static Group Checking
- **"### Entity Access Control with ownerPartyId"** - Correct usage, common patterns, troubleshooting

## Quick Reference

### EntityFilter Definition (Data File)
```xml
<moqui.security.EntityFilterSet entityFilterSetId="USER_ORG_FILTER"
                                description="Filter by user's organization"/>
<moqui.security.EntityFilter entityFilterSetId="USER_ORG_FILTER"
                             entityName="example.Order"
                             filterMap="[ownerPartyId:(filterOrgIds ?: [])]"/>
```

### Context Setup Service
```xml
<service verb="setup" noun="FilterContext">
    <actions>
        <entity-find entity-name="mantle.party.PartyRelationship" list="orgRelList">
            <econdition field-name="fromPartyId" from="ec.user.userAccount.partyId"/>
        </entity-find>
        <set field="filterOrgIds" from="orgRelList*.toPartyId"/>
        <script>ec.user.context.put('filterOrgIds', filterOrgIds)</script>
    </actions>
</service>
```

## Key Principles

1. **Fail-Safe Pattern**: Use `(filterOrgIds ?: [])` to return empty results if context missing
2. **REST API Context**: MUST call setup service in REST services (screens handle via always-actions)
3. **View-Entity Matching**: Filters match on alias names, not source entity field names
4. **Test Setup**: Call filter context setup in Spock tests before querying filtered entities