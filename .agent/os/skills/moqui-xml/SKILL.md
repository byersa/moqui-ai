---
name: moqui-xml
description: |
  Moqui Framework XML best practices including formatting standards, default value handling, element ordering, and common anti-patterns.

  Use this skill when:
  - Formatting Moqui XML files consistently
  - Avoiding explicit default values that cause warnings
  - Understanding ignore vs ignore-if-empty in econditions
  - Fixing common XML anti-patterns
  - Ensuring schema compliance
---

# Moqui XML Best Practices

## References

| Reference | Description |
|-----------|-------------|
| `../../references/xml_best_practices.md` | XML formatting, default value avoidance, anti-patterns, schema compliance |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## XML Validation and Common Error Corrections"** - Comprehensive validation rules
- **"### Critical XML Structure Rules"** - No subselect, character escaping, field attributes
- **"## XML DSL Default Attributes Best Practices"** - Common defaults to avoid
- **"### Validation Checklist"** - Pre-commit XML validation checklist
- **"### XML Schema Validation"** - Schema file locations, validation best practices
- **"## Code Formatting and Style Guidelines"** - XML tag formatting, if-then-else structure

## Quick Reference

### ignore-if-empty vs ignore
```xml
<!-- BAD: "%${var}%" is never empty, so never ignored -->
<econdition field-name="name" operator="like" value="%${name}%" ignore-if-empty="true"/>

<!-- GOOD: Explicit null check -->
<econdition field-name="name" operator="like" value="%${name}%" ignore="name == null"/>
```

### Default Values to NEVER Specify
```xml
<!-- WRONG: Explicit defaults cause warnings -->
<parameter name="message" type="String"/>        <!-- String is default -->
<service verb="do" noun="Thing" transaction="use-or-begin"/>  <!-- use-or-begin is default -->

<!-- CORRECT: Omit default values -->
<parameter name="message"/>
<service verb="do" noun="Thing"/>
```

## Key Principles

1. **No Explicit Defaults**: Never specify default attribute values (type="String", transaction="use-or-begin")
2. **Use ignore for Patterns**: Use `ignore="var == null"` for string interpolation conditions
3. **Consistent Indentation**: 4-space indentation throughout
4. **Entity Field Types**: Use entity types (id, text-medium), not service types (String, Integer)