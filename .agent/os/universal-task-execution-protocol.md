# Universal Task Execution Protocol

This protocol defines the standardized execution framework for all specialist agents.

## Mandatory Execution Framework

Every task MUST follow this structured approach:

### Planning Phase (Before Execution)

1. **ANALYZE**: Task requirements, constraints, and success criteria
2. **DESIGN**: Solution approach using available patterns and skill references
3. **PLAN**: Create 3-5 step task breakdown with checkpoints
4. **ESTIMATE**: Resource requirements and potential complications

### Execution Protocol

1. Execute **ONE step at a time** with checkpoints between steps
2. **REFERENCE** pattern files (`references/`) for detailed patterns
3. **VALIDATE** each step completion before proceeding to the next
4. **CLEANUP** resources and context after each phase

### Step Structure

Each step follows this pattern:

```
Step N: [Phase Name]
  ANALYZE/DESIGN/IMPLEMENT/VALIDATE/FINALIZE the specific task aspect
  REFERENCE: Relevant skill reference files
  CHECKPOINT: Validation criteria before proceeding
```

### Standard 5-Step Workflow

| Step | Phase | Purpose |
|------|-------|---------|
| 1 | Analyze | Understand requirements, review existing code, identify constraints |
| 2 | Design | Plan solution architecture, select patterns, define approach |
| 3 | Implement | Create/modify files following patterns and standards |
| 4 | Validate | Test functionality, verify quality, check standards compliance |
| 5 | Finalize | Clean up, document, summarize results |

## Memory Management

- Keep core instructions to max 150 lines + current step context only
- Use external references (skill files) for all detailed patterns and templates
- Refresh context between major steps to prevent context bloat
- Monitor token usage with compression at 70% capacity

## Quality Gates

Before marking any step as complete:
- [ ] Output matches expected format and standards
- [ ] No regressions introduced in existing functionality
- [ ] Patterns from skill references followed correctly
- [ ] Local enforcement rules applied (standards take precedence over skills)

## Conflict Resolution

When patterns from different sources conflict during execution:
1. **Local standards always win** as the default
2. **Prompt user** with both approaches shown side-by-side
3. **Let user choose** which approach to apply

See `skill-integration.md` for detailed conflict resolution guidance.
