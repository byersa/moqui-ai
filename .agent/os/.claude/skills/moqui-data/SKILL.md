---
name: moqui-data
description: |
  Moqui Framework data file patterns including seed data, demo data, configuration data, enumerations, status workflows, and security configuration.

  Use this skill when:
  - Creating or modifying data files (*.xml in data/ directories)
  - Defining enumerations (EnumerationType, Enumeration)
  - Creating status workflows (StatusType, StatusItem, StatusFlowTransition)
  - Configuring user groups and permissions (UserGroup, ArtifactGroup, ArtifactAuthz)
  - Setting up PartySettingType for application configuration
  - Creating seed data vs demo data files
  - Creating demo data templates with relative date expressions (@rel:, @epoch:)
---

# Moqui Data Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/data_patterns.md` | Seed data, configuration data, status workflows, security configuration |
| `../../standards/backend/demo-data-refresh.md` | Demo data date refresh with relative date expressions |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Data Validation Requirements"** - Mandatory validation process, entity definition verification
- **"### Entity Short-Alias Usage in Data Files"** - Valid entity reference patterns, common aliases
- **"## Test Data Management and Data Types"** - Data type classifications, load order, storage patterns
- **"### StatusItem vs Enumeration Usage"** - When to use each, relationship patterns
- **"### Common Data Validation Mistakes"** - Avoiding data file errors
- **"### Data Validation Checklist for moqui-data-specialist"** - Pre-commit validation steps

## Quick Reference

### Data File Pattern
```xml
<entity-facade-xml type="seed">
    <moqui.basic.EnumerationType enumTypeId="EnumType"
                                 description="Enumeration Type Description"/>
    <moqui.basic.Enumeration enumId="EnumValue" enumTypeId="EnumType"
                             description="Enum Description" sequenceNum="10"/>
</entity-facade-xml>
```

### Status Workflow Pattern
```xml
<moqui.basic.StatusType statusTypeId="OrderStatus" description="Order Status"/>
<moqui.basic.StatusItem statusId="OrderPlaced" statusTypeId="OrderStatus" sequenceNum="10"/>
<moqui.basic.StatusFlowTransition statusFlowId="Default" statusId="OrderPlaced"
                                  toStatusId="OrderProcessing" transitionName="Process"/>
```

## Key Principles

1. **Data Types**: Use `type="seed"` for required data, `type="demo"` or `type="{project}-demo"` for sample data
2. **Configuration**: Use PartySettingType for app settings, SystemBinding for infrastructure
3. **Security**: Define UserGroups, ArtifactGroups, and ArtifactAuthz for permissions
4. **Screen Documentation**: Use `moqui.screen.ScreenDocument` records in setup data files to attach in-app help to screens (see `standards/frontend/screen-documents.md`)
5. **Demo Data Freshness**: Use `@rel:` expressions in demo templates to keep dates relative to today (see `standards/backend/demo-data-refresh.md`)