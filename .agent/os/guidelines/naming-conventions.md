# Project Naming Conventions Guide

This document defines the configuration system for entity and service naming conventions in Moqui Framework projects. Each project must define its naming hierarchy to ensure consistent and discoverable code organization.

## Overview

Moqui Framework uses hierarchical package/path naming for entities and services. Projects must configure their naming conventions at initialization to guide agents and developers.

## Configuration Location

**Project-specific naming configuration MUST be defined in:**
```
runtime/component/{main-component}/.agent-os/naming-conventions.md
```

## Naming Convention Configuration Schema

Each project's `naming-conventions.md` file should define:

### 1. Entity Naming

```markdown
## Entity Naming

### Package Prefix
- **Primary Package**: `{package.prefix}` (e.g., `mycompany.myapp.inventory`)
- **Alternative Roots**: (list any special cases)

### Entity Hierarchy
| Domain | Package | Example Entity |
|--------|---------|----------------|
| {domain1} | {package.prefix}.{domain1} | {EntityName} |
| {domain2} | {package.prefix}.{domain2} | {EntityName} |

### Entity File Organization
- `entity/{Domain}Entities.xml` - Main entity definitions
- `entity/{Domain}ViewEntities.xml` - View entities
```

### 2. Service Naming

```markdown
## Service Naming

### Service Path Prefix
- **Primary Path**: `{path/prefix}` (e.g., `mycompany/myapp/inventory`)
- **Dot Notation**: `{path.prefix}` (e.g., `mycompany.myapp.inventory`)

### Service Hierarchy
| Domain | Path | Example Service |
|--------|------|-----------------|
| {domain1} | {path/prefix}/{domain1}/ | {verb}#{Noun} |
| {domain2} | {path/prefix}/{domain2}/ | {verb}#{Noun} |

### Service File Organization
- `service/{path/prefix}/{Domain}Services.xml` - Service definitions
```

## Common Hierarchy Patterns

### Simple Project
For single-domain projects:
```
Entity Package: com.example.project
Service Path: com/example/project
```

### Multi-Domain Project
For complex projects with multiple domains:
```
Entity Package: com.example.project.{domain}
Service Path: com/example/project/{domain}
```

### Enterprise Namespace (mycompany.myapp pattern)
For enterprise applications:
```
Entity Package: mycompany.myapp.{application}.{domain}
Service Path: mycompany/myapp/{application}/{domain}
```

## Agent Integration Requirements

### For Entity Agents (moqui-entity-specialist)

When creating or modifying entities:

1. **MUST** read `runtime/component/{main-component}/.agent-os/naming-conventions.md`
2. **MUST** use the configured package prefix for all new entities
3. **MUST** follow the documented hierarchy for domain organization
4. **MUST** validate entity names against the convention before creating

### For Service Agents (moqui-service-definition-specialist, moqui-service-implementation-specialist)

When creating or modifying services:

1. **MUST** read `runtime/component/{main-component}/.agent-os/naming-conventions.md`
2. **MUST** place service files in the configured path prefix directories
3. **MUST** use the configured dot notation for full service names
4. **MUST** organize services by domain according to the hierarchy

### For Data Agents (moqui-data-specialist)

When creating data files:

1. **MUST** reference entities using fully qualified names with the configured package prefix
2. **MUST** follow the documented hierarchy for data file organization

## Verification Checklist

When setting up a new project or validating an existing project:

- [ ] `naming-conventions.md` exists in the component's `.agent-os/` directory
- [ ] Entity package prefix is clearly defined
- [ ] Service path prefix is clearly defined (both path and dot notation)
- [ ] Domain hierarchy is documented with examples
- [ ] File organization patterns are specified
- [ ] Alternative roots (if any) are documented

## Auto-Detection for New Projects

When no `naming-conventions.md` exists, agents should:

1. **Analyze existing code** to detect patterns
2. **Propose conventions** based on detected patterns
3. **Create the configuration file** with discovered conventions
4. **Validate with the user** before applying

## Error Prevention

### Common Mistakes

| Mistake | Correct Approach |
|---------|-----------------|
| Service in root `service/` directory | Use configured path prefix (e.g., `service/mycompany/myapp/inventory/`) |
| Entity without package prefix | Use full package (e.g., `mycompany.myapp.inventory.EntityName`) |
| Inconsistent domain organization | Follow documented hierarchy |
| Mixed naming conventions | Standardize across project |

### Validation Queries

Agents should validate new artifacts:

```
Entity: Does package start with configured prefix?
Service: Is file path under configured prefix directory?
Service call: Does full name use configured dot notation prefix?
```

## Cross-Reference

- **Entity Patterns**: `runtime/component/moqui-agent-os/standards/backend/entities.md`
- **Service Patterns**: `runtime/component/moqui-agent-os/standards/backend/services.md`
- **Framework Guide**: `runtime/component/moqui-agent-os/framework-guide.md`