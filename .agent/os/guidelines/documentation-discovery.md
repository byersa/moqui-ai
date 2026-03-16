# Documentation Discovery Guideline

## Purpose

This guideline ensures that undocumented patterns, behaviors, and gotchas discovered during development are systematically captured in the agent-os documentation. Knowledge capture is a mandatory part of every task's finalization step.

## When to Trigger Documentation Discovery

### Automatic Triggers (End of Every Task)

At the end of each implementation task, evaluate whether any of the following were encountered:

1. **Framework Behaviors**: Moqui behaviors not documented in framework-guide.md
2. **Pattern Variations**: New ways to implement existing patterns
3. **Gotchas and Pitfalls**: Error conditions, edge cases, or unexpected behaviors
4. **Configuration Nuances**: Settings or configurations with non-obvious effects
5. **Integration Patterns**: New ways to connect components or external systems
6. **Performance Insights**: Optimization techniques or performance considerations

### Manual Triggers (Ad-hoc Discovery)

Use the `/document-discovery` command when:
- Exploring the codebase reveals undocumented patterns
- Answering questions about "how things work" uncovers gaps
- Debugging reveals framework behaviors worth documenting
- Code review identifies patterns that should be standardized

## Discovery Evaluation Criteria

### Document If

- Pattern was used during implementation but not found in agent-os docs
- Behavior required research or experimentation to understand
- Same question might be asked again in future development
- Pattern applies broadly (not project-specific)
- Discovery would save time for future implementations

### Skip If

- Pattern is already documented (verify with search first)
- Discovery is project-specific (belongs in component `.agent-os/`)
- Pattern is trivial or obvious from Moqui documentation
- Discovery is in the ignore list (see below)

## Content Placement Decision Tree

```
Discovery Type
    │
    ├── Framework behavior, gotcha, or detailed explanation?
    │   └── YES → framework-guide.md
    │       └── Find or create appropriate section header
    │
    ├── Declarative rule, convention, or constraint?
    │   │   (Uses "MUST", "NEVER", "ALWAYS")
    │   └── YES → standards/backend/*.md or standards/frontend/*.md
    │       └── Match to existing standard file or create new
    │
    ├── Step-by-step pattern or how-to guide?
    │   └── YES → references/*.md
    │       └── Add to appropriate reference file
    │
    ├── Infrastructure, deployment, or environment setup?
    │   └── YES → development-guide.md
    │
    └── Testing pattern or strategy?
        └── YES → testing-guide.md
```

### Detailed Placement Rules

| Discovery Type | Primary Location | Cross-Reference To |
|----------------|------------------|-------------------|
| Entity behavior | `framework-guide.md` § Entities | `skills/moqui-entities/references/` |
| Service pattern | `skills/moqui-services/references/` | `framework-guide.md` if complex |
| Screen gotcha | `framework-guide.md` § Screens | `skills/moqui-screens/references/` |
| EntityFilter | `standards/backend/entity-filters.md` | `skills/moqui-entity-filters/` |
| REST API pattern | `skills/moqui-rest-api/references/` | `standards/backend/rest-api.md` |
| Build/deployment | `development-guide.md` | `skills/moqui-build/references/` |
| Testing pattern | `testing-guide.md` | `skills/moqui-testing/references/` |
| XML formatting | `standards/backend/xml-formatting.md` | `skills/moqui-xml/references/` |

## User Prompt Template

When a discovery is identified, prompt the user with:

```
### Documentation Discovery

During this task, I identified an undocumented pattern:

**Discovery**: [Brief description of what was discovered]

**Context**: [Where/how this was encountered]

**Proposed Location**: [Recommended documentation file based on decision tree]

**Options**:
1. **Document now** - I'll create a task to add this to agent-os
2. **Document later** - Note for future documentation
3. **Skip this time** - Don't document, but ask again if encountered
4. **Ignore permanently** - Add to ignore list (won't prompt again)

Which would you prefer?
```

## Ignore List Mechanism

### Structure

The ignore list is maintained at the end of this file. Patterns added here will not trigger documentation prompts.

### Adding to Ignore List

When user selects "Ignore permanently", add an entry:

```markdown
| Pattern | Reason | Date Added |
|---------|--------|------------|
| [Pattern description] | [Why ignored] | [YYYY-MM-DD] |
```

### Reviewing Ignore List

Periodically review the ignore list to:
- Remove patterns that have since been documented elsewhere
- Reconsider patterns that might now be worth documenting
- Clean up outdated entries

## Documentation Task Structure

When user approves documentation, create a structured task:

```markdown
**Task**: Document [discovery name] in agent-os

**Discovery Details**:
- What: [Description of the pattern/behavior]
- Why it matters: [Impact on development]
- Example: [Code snippet or scenario]

**Target Location**: [File path from decision tree]

**Cross-References Required**:
- [ ] Update skill reference if adding to framework-guide.md
- [ ] Update framework-guide.md if adding to skill
- [ ] Verify section headers match cross-references
- [ ] Run validation script after changes

**Content Template**:
[Draft of the documentation to add]
```

## Integration with Task Execution Protocol

This guideline integrates with `task-execution-protocol.md` Step 6 (Knowledge Capture):

1. **Automatic Check**: At task finalization, review patterns used vs documented patterns
2. **Discovery Identification**: List any gaps found
3. **User Prompt**: Present discoveries using the template above
4. **Action**: Execute based on user choice
5. **Validation**: If documented, run cross-reference validation

## Quality Standards for New Documentation

When adding documentation, ensure:

- [ ] Clear, concise explanation of the pattern/behavior
- [ ] Practical example with code snippet
- [ ] Context for when to use (and when not to use)
- [ ] Cross-references to related documentation
- [ ] Follows existing documentation style and format
- [ ] Section header is descriptive and searchable

## Examples of Good Documentation Discoveries

### Example 1: Framework Behavior
**Discovery**: `always-actions` in screens execute even when transition has `response="none"`
**Location**: `framework-guide.md` § Screen Execution Flow
**Cross-ref**: `skills/moqui-screens/references/screen_patterns.md`

### Example 2: Pattern Variation
**Discovery**: Using `entity-options` with `text` attribute for formatted display
**Location**: `skills/moqui-screens/references/form_patterns.md`
**Cross-ref**: Add reference in `framework-guide.md` § Forms

### Example 3: Gotcha
**Discovery**: EntityFilter context not set in scheduled jobs by default
**Location**: `standards/backend/entity-filters.md`
**Cross-ref**: `skills/moqui-entity-filters/references/entity_filter_patterns.md`

---

## Ignore List

Patterns in this list will not trigger documentation prompts.

| Pattern | Reason | Date Added |
|---------|--------|------------|
| *No entries yet* | - | - |

---

*This guideline ensures continuous improvement of agent-os documentation through systematic knowledge capture.*