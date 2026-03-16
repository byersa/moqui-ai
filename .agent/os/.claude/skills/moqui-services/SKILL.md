---
name: moqui-services
description: |
  Moqui Framework service development patterns including service definitions, parameters, transactions, error handling, and implementation patterns.

  Use this skill when:
  - Creating or modifying service definitions (*.xml in service/ directories)
  - Implementing business logic in service actions
  - Designing service parameters (in-parameters, out-parameters)
  - Handling service errors and validation
  - Working with service transactions (force-new, use-or-begin)
  - Implementing SECA (Service Event Condition Actions)
  - Creating batch processing or scheduled job services
---

# Moqui Service Patterns

## References

| Reference | Description |
|-----------|-------------|
| `../../references/service_patterns.md` | Service definitions, parameters, transactions, CRUD patterns, provider/plugin patterns |
| `../../references/service_implementation.md` | XML DSL vs scripts, ResourceReference patterns, batch processing, file versioning |
| `../../references/service_jobs.md` | ServiceJob entities, programmatic job API, framework jobs inventory |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Service Definition Best Practices"** - Message localization, common patterns
- **"## XML DSL vs Script Tags Best Practice"** - Three-tier preference (XML DSL → External Groovy → Inline script)
- **"### Service-call Parameter Patterns"** - Parameter passing best practices, when to use field-map
- **"### Service Call Naming and Parameter Best Practices"** - Naming requirements, parameter passing rules
- **"## ServiceJob Authentication Requirements"** - `authenticate="anonymous-all"` for scheduled jobs
- **"### Script-Type Services"** - External Groovy file patterns, location references
- **"### Jsoup.parse() Groovy Method Overloading Gotcha"** - Null parameter ambiguity with Jsoup in PDF generation

## Quick Reference

### Service Definition Pattern
```xml
<service verb="create" noun="EntityName">
    <description>Create a new entity record</description>
    <in-parameters>
        <auto-parameters entity-name="EntityName" include="nonpk"/>
        <parameter name="customField" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="entityId"/>
    </out-parameters>
    <actions>
        <!-- Implementation -->
    </actions>
</service>
```

## Key Principles

1. **XML DSL Preference**: Use Moqui's declarative XML elements over inline scripts
2. **Default Value Avoidance**: Never explicitly specify default attribute values
3. **Transaction Defaults**: 98.5% of services use default transaction behavior (omit attribute)
4. **Parameter Validation**: Use built-in validation attributes over manual checks