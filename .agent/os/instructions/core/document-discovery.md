---
description: Documentation discovery and capture workflow
globs:
alwaysApply: false
version: 1.0
encoding: UTF-8
---

# Document Discovery Workflow

## Usage

Invoke this command when:
- Exploring the codebase reveals undocumented patterns
- Answering questions uncovers documentation gaps
- Debugging reveals framework behaviors worth documenting
- You want to capture knowledge from the current session

## Workflow

### Phase 1: Discovery Identification

```xml
<discovery_identification>
  <step number="1" name="gather_context">
    ANALYZE: Current conversation for patterns used or discovered
    IDENTIFY: Knowledge that isn't in agent-os documentation
    CATEGORIZE: Each discovery by type:
      - Framework behavior
      - Pattern variation
      - Gotcha/pitfall
      - Configuration nuance
      - Integration pattern
      - Performance insight
  </step>

  <step number="2" name="verify_undocumented">
    FOR each_discovery:
      SEARCH: agent-os documentation for existing coverage
      VERIFY: Pattern is not already documented
      CHECK: Pattern is not in ignore list
    END FOR
  </step>
</discovery_identification>
```

### Phase 2: User Confirmation

```xml
<user_confirmation>
  <step number="3" name="present_discoveries">
    FOR each_undocumented_discovery:
      PRESENT: Using standard prompt template
      CAPTURE: User decision (document/skip/ignore)
    END FOR
  </step>

  <prompt_template>
    ### Documentation Discovery

    **Discovery**: [Brief description]
    **Context**: [Where/how encountered]
    **Proposed Location**: [Target file based on decision tree]

    **Options**:
    1. Document now
    2. Document later
    3. Skip this time
    4. Ignore permanently
  </prompt_template>
</user_confirmation>
```

### Phase 3: Documentation Update

```xml
<documentation_update>
  <step number="4" name="determine_location">
    APPLY: Content placement decision tree from documentation-discovery.md
    IDENTIFY: Target file and section
    IDENTIFY: Required cross-references
  </step>

  <step number="5" name="draft_content">
    CREATE: Documentation content following quality standards:
      - Clear explanation of pattern/behavior
      - Practical code example
      - When to use (and when not to)
      - Related documentation links
  </step>

  <step number="6" name="update_documentation">
    DELEGATE: To appropriate specialist agent OR
    EXECUTE: Direct edit if simple addition
    UPDATE: Cross-references as required
  </step>

  <step number="7" name="validate">
    RUN: Cross-reference validation script
    VERIFY: Documentation renders correctly
    CONFIRM: All cross-references are valid
  </step>
</documentation_update>
```

## Content Placement Quick Reference

| Discovery Type | Primary Location |
|----------------|------------------|
| Project structure / meta-knowledge | `references/project_structure.md` |
| Framework behavior/gotcha | `framework-guide.md` |
| Declarative rule (MUST/NEVER) | `standards/backend/*.md` |
| Step-by-step pattern | `references/*.md` |
| Infrastructure/deployment | `development-guide.md` |
| Testing pattern | `testing-guide.md` |

## Quality Checklist

Before completing documentation update:

- [ ] Content is clear and concise
- [ ] Code example is practical and tested
- [ ] Cross-references are updated
- [ ] Section headers are descriptive
- [ ] Validation script passes
- [ ] Follows existing documentation style

## Reference

For complete guidelines, see:
- `runtime/component/moqui-agent-os/guidelines/documentation-discovery.md`
- `runtime/component/moqui-agent-os/skill-integration.md` (Documentation Maintenance)

## Examples

### Example 1: Discovering Framework Behavior

**User asks**: "Why isn't my data showing in the screen?"

**Discovery**: EntityFilter context must be explicitly set in REST API pre-service

**Action**: Document in `standards/backend/entity-filters.md` with cross-reference to `skills/moqui-entity-filters/references/entity_filter_patterns.md`

### Example 2: Ad-hoc Pattern Discovery

**User exploring**: "How does caching work for this entity?"

**Discovery**: `cache="true"` on entity doesn't cache view-entity queries

**Action**: Document in `framework-guide.md` section Entity Caching with cross-reference to `skills/moqui-entities/references/caching_best_practices.md`

---

*This command ensures continuous improvement of agent-os documentation through systematic knowledge capture.*
