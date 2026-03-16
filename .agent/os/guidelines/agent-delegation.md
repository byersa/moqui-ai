# Agent Delegation Guidelines

## Overview

When Claude Code delegates tasks to specialist sub-agents via the Task tool, the sub-agents may ask clarifying questions before proceeding with implementation. This document provides guidelines for handling these clarifications.

## Handling Sub-Agent Clarifications

### Core Principle

**Never leave sub-agent questions unanswered.** When a sub-agent asks for clarification:

1. **If you are certain about the answer**: Respond immediately by re-invoking the Task tool with the clarification included in the prompt
2. **If you are uncertain**: Ask the user for clarification, then re-invoke the Task tool with the user's answer

### Anti-Pattern: Abandoning Clarifications

**DO NOT** do this:
```
Sub-agent: "Would you like me to delegate to specialists or implement directly?"
Claude: [Moves on to other work without answering]
```

This creates incomplete work and confuses the workflow.

### Correct Pattern: Addressing Clarifications

**DO** this:

```
Sub-agent: "Would you like me to delegate to specialists or implement directly?"
Claude: [Analyzes question]
  - If project requirements (Claude.md) provide clear guidance → Answer directly
  - If uncertain or requires user preference → Ask user
User: "Delegate to specialists"
Claude: [Re-invokes Task tool with clarification]
```

## Examples

### Example 1: Project Requirements Provide Answer

```markdown
Sub-agent asks: "Should I use enable-audit-log on this entity?"

Claude thinks:
- Check project guidelines
- Agent-OS specifies: "Use enable-audit-log=true for all inventory entities"
- Answer is clear from requirements

Claude responds:
- Re-invoke Task tool
- Include in prompt: "Yes, use enable-audit-log='true' as specified in project guidelines"
```

### Example 2: User Preference Required

```markdown
Sub-agent asks: "Should I create a new Permission entity or use existing UserPermission?"

Claude thinks:
- This is an architectural decision
- No clear guidance in project docs
- User should decide

Claude responds:
- Ask user: "The specialist is asking whether to create a new Permission entity or use existing UserPermission. What would you prefer?"
- Wait for user response
- Re-invoke Task tool with user's answer included
```

### Example 3: Multiple Clarifications

```markdown
Sub-agent asks three questions:
1. "Should I use composite keys or single primary key?"
2. "What naming convention for the test file?"
3. "Should I include integration tests or only unit tests?"

Claude responds:
- Question 1: Check spec → Answer: "Use composite keys as specified in database-schema.md"
- Question 2: Check project patterns → Answer: "Use EntityNameTests.groovy pattern"
- Question 3: Unclear → Ask user: "Should we include integration tests in addition to unit tests?"

Wait for user response, then re-invoke Task tool with all three answers.
```

## Workflow Integration

### During Task Delegation

1. **Invoke Task tool** with specialist agent and detailed prompt
2. **Monitor response** for clarifying questions
3. **Address questions immediately**:
   - Answer from requirements if certain
   - Ask user if uncertain
4. **Re-invoke Task tool** with clarifications included
5. **Verify completion** - ensure sub-agent produces expected outputs

### Best Practices

- **Read project documentation first**: Many questions can be answered from existing guidelines
- **Check specs and technical documents**: Architectural decisions often documented there
- **Be decisive when requirements are clear**: Don't ask user for things already specified
- **Engage user for preferences**: UI/UX choices, architectural trade-offs, feature priorities
- **Consolidate questions**: If multiple clarifications needed, ask user once with all questions
- **Update documentation**: If clarifications reveal gaps, document decisions for future

## Common Clarification Scenarios

### Technical Implementation

**Sub-agent asks**: "Which entity pattern should I use?"
- **Check**: Technical spec, entity patterns documentation
- **If found**: Answer directly from docs
- **If not found**: Ask user for architectural preference

### Testing Strategy

**Sub-agent asks**: "What level of test coverage?"
- **Check**: Testing guide, project standards
- **Usually specified**: Follow documented standards
- **Rarely ask user**: Unless doing something exceptional

### UI/UX Decisions

**Sub-agent asks**: "Should this be a modal or inline form?"
- **Check**: UX guidelines, similar screens in codebase
- **Often ask user**: UX preferences vary by context
- **Consider patterns**: Look at existing similar features

### Security and Permissions

**Sub-agent asks**: "What permission level required?"
- **Check**: Security model documentation, spec requirements
- **Usually specified**: Security requirements documented
- **Ask user if**: New permission type or unclear access control

## Anti-Patterns to Avoid

### ❌ Ignoring Clarifications
```
Sub-agent: "Question about approach?"
Claude: [Continues without answering]
Result: Incomplete work, confused state
```

### ❌ Over-Asking User
```
Sub-agent: "Should I use camelCase?"
Claude: "User, should the specialist use camelCase?"
Result: User frustrated - this is in style guide
```

### ❌ Guessing When Uncertain
```
Sub-agent: "Which database should this go to?"
Claude: "Probably the main one" [guesses without checking]
Result: Wrong implementation, wasted work
```

## Resolution Protocol

When you encounter a sub-agent clarification:

1. ✅ **Acknowledge** the question
2. ✅ **Check documentation** (specs, guidelines, patterns)
3. ✅ **Decide**: Can I answer this with certainty?
   - Yes → Answer and re-invoke Task
   - No → Ask user for clarification
4. ✅ **Re-invoke** Task tool with complete clarification
5. ✅ **Verify** sub-agent proceeds with implementation

## Documentation Updates

When clarifications reveal documentation gaps:

1. Note the gap (missing guideline, unclear spec)
2. After resolving with user, document the decision
3. Update relevant .agent-os/ files
4. Prevent same question in future work

## Summary

**Key Principle**: Always address sub-agent clarifications promptly and completely. Never leave questions unanswered. Check requirements first, ask user when uncertain, and ensure sub-agents have everything they need to complete their work.
