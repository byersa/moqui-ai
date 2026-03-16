# Agent Memory Optimization Guide

This document provides guidance for agent creators to avoid memory issues when designing and implementing specialist agents.

## Memory Issue Analysis: moqui-screen-specialist Case Study

### Problem Identified
The original `moqui-screen-specialist` agent caused Node.js heap out of memory errors (19GB allocation failure) during execution.

**Root Causes:**
1. **Excessive Content**: 703 lines, 24KB file with 10 large XML code blocks
2. **Template Embedding**: Full XML templates stored directly in agent files
3. **Context Accumulation**: All patterns loaded simultaneously in memory
4. **Recursive Processing**: Complex XML structures causing memory-intensive parsing

### Memory Consumption Patterns
- **File Size Impact**: Agents over 20KB show increased memory usage
- **Code Block Density**: 10+ embedded examples significantly impact memory
- **Template Complexity**: Nested XML structures multiply memory requirements
- **Context Loading**: All agent content loaded at initialization

## Agent Size Guidelines

### Recommended Limits
- **File Size**: Keep agents under 15KB (approximately 400-500 lines)
- **Code Blocks**: Maximum 6-8 embedded examples per agent
- **Template Length**: Individual templates under 50 lines
- **Nesting Depth**: Avoid more than 4 levels of nested content sections

### Memory-Efficient Patterns
- **Reference Files**: Store templates in separate `.agent-os/references/` files
- **Modular Content**: Break large agents into specialized sub-agents
- **Lazy Loading**: Reference external content rather than embedding
- **Focused Responsibility**: Single, well-defined purpose per agent

## Agent Subdivision Strategy

### When to Subdivide
**Consider subdividing when agent has:**
- File size > 20KB
- More than 500 lines
- 10+ code blocks
- Multiple distinct responsibilities
- Complex nested template structures

### Subdivision Principles
1. **Single Responsibility**: Each sub-agent handles one domain
2. **Clear Boundaries**: No overlap in functionality between agents
3. **Shared References**: Common patterns in external reference files
4. **Coordinated Workflow**: Sub-agents work together via Task tool

### Example: Screen Specialist Optimization
**Original Agent (703 lines, 24KB)** → **Optimized with reference extraction**

The screen specialist was initially subdivided into 4 agents (layout, form, flow, ux) but later re-unified into:
- `moqui-screen-specialist`: Unified screen agent (layout, forms, navigation)
- `moqui-screen-ux-specialist`: Cross-cutting UX concerns (accessibility, performance)

The key optimization was extracting templates to `references/` rather than embedding them.

## Reference File Organization

### Structure Pattern
```
.agent-os/
├── [domain]-references/
│   ├── templates.md          # Standard templates
│   ├── patterns.md           # Common patterns
│   ├── guidelines.md         # Best practices
│   └── examples.md           # Usage examples
```

### Content Distribution
- **Agent Files**: Core logic, workflows, responsibility definition
- **Reference Files**: Templates, examples, detailed patterns
- **Shared References**: Cross-cutting concerns, XML formatting

## Agent Design Best Practices

### Memory-Efficient Agent Structure
```markdown
---
name: [agent-name]
description: [concise description]
tools: [required tools only]
---

## Core Responsibilities
- [focused responsibility list]

## Key Patterns
- [essential patterns only]

## Workflow
- [step-by-step process]

## Reference Files
- See `.agent-os/[domain]-references/` for detailed templates
- Templates: [reference file links]
- Patterns: [reference file links]

## Quality Checklist
- [validation criteria]
```

### Content Prioritization
**Include in Agent File:**
- Core responsibilities and boundaries
- Essential workflow patterns
- Quality standards and validation
- Integration points with other agents

**Move to Reference Files:**
- Detailed templates and examples
- Comprehensive pattern libraries
- Extended documentation
- Complex code samples

## Agent Registry Updates

### Documentation Requirements
When creating or modifying agents, update:
1. **Agent Registry**: Add size and complexity metrics
2. **Selection Criteria**: Clear boundaries for agent choice
3. **Reference Mapping**: Link to shared reference files
4. **Memory Profile**: Document expected memory usage

### Example Registry Entry
```markdown
#### [agent-name]
- **Size**: [lines/KB] (Target: <500 lines, <15KB)
- **Code Blocks**: [count] (Target: <8)
- **Memory Profile**: Low/Medium/High
- **References**: [list of reference files]
- **Subdivision**: [if subdivided, list sub-agents]
```

## Testing and Validation

### Memory Testing
1. **Size Validation**: Check file size and line count
2. **Template Count**: Verify code block limits
3. **Load Testing**: Test agent invocation with large contexts
4. **Memory Monitoring**: Monitor heap usage during execution

### Performance Benchmarks
- **Small Agent** (<10KB): <2GB heap usage
- **Medium Agent** (10-15KB): <4GB heap usage
- **Large Agent** (>15KB): Requires subdivision

## Migration Strategy

### For Existing Large Agents
1. **Analyze Content**: Identify subdivision opportunities
2. **Extract Templates**: Move to reference files
3. **Create Sub-Agents**: Implement focused specialists
4. **Update Registry**: Document new structure
5. **Test Integration**: Verify coordinated workflows

### Backward Compatibility
- Maintain existing agent names during transition
- Provide clear migration documentation
- Update CLAUDE.md with new delegation rules
- Test with existing workflows

## Implementation Checklist

### For Agent Creators
- [ ] Verify agent size limits before creation
- [ ] Use reference files for templates and patterns
- [ ] Define clear, single responsibility
- [ ] Document memory profile and expected usage
- [ ] Test with realistic data volumes
- [ ] Update agent registry with metrics
- [ ] Coordinate with related agents

### For Agent Maintainers
- [ ] Monitor agent file sizes over time
- [ ] Review and optimize embedded content
- [ ] Consider subdivision when limits exceeded
- [ ] Update reference files instead of agent files
- [ ] Test memory usage after modifications
- [ ] Document changes and impact

---

**This guide should be consulted by the agent-builder specialist when creating or modifying agents to prevent memory issues and ensure optimal performance.**