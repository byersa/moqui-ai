# Universal Task Execution Protocol

## Standardized Task Definition and Execution Framework

This protocol defines the mandatory approach for all specialist agents to handle complex tasks in a memory-efficient, step-by-step manner based on 2025 best practices for LLM agent design.

## Core Protocol Structure

### 1. Universal Task Execution Framework

All agents MUST implement this standardized protocol for complex tasks:

```xml
<universal_task_execution>
  <planning_phase>
    MANDATORY: Create 3-5 step task breakdown before execution
    ANALYZE: Task requirements, constraints, and success criteria
    DESIGN: Solution approach using available patterns and references
    PLAN: Execution sequence with memory checkpoints
    ESTIMATE: Resource requirements and potential complications
  </planning_phase>

  <execution_protocol>
    <step number="1" phase="analyze" checkpoint="true">
      ### Step 1: Requirements Analysis
      ANALYZE task_requirements_and_constraints
      REFERENCE: External_patterns_and_guidelines_as_needed
      CHECKPOINT: Validate_understanding_before_proceeding
    </step>

    <step number="2" phase="design" checkpoint="true">
      ### Step 2: Solution Design
      DESIGN solution_using_established_patterns
      REFERENCE: Templates_and_examples_from_external_sources
      CHECKPOINT: Validate_design_meets_requirements
    </step>

    <step number="3" phase="implement" checkpoint="true">
      ### Step 3: Implementation
      IMPLEMENT in_discrete_manageable_phases
      REFERENCE: Implementation_templates_and_best_practices
      CHECKPOINT: Validate_implementation_quality
    </step>

    <step number="4" phase="validate" checkpoint="true">
      ### Step 4: Validation and Testing
      VALIDATE functionality_performance_and_standards
      REFERENCE: Quality_checklists_and_testing_patterns
      CHECKPOINT: Ensure_all_requirements_satisfied
    </step>

    <step number="5" phase="finalize" checkpoint="true">
      ### Step 5: Finalization and Cleanup
      FINALIZE implementation_and_documentation
      CLEANUP temporary_resources_and_context
      SUMMARIZE results_and_next_steps
    </step>

    <step number="6" phase="document" checkpoint="true">
      ### Step 6: Knowledge Capture (Documentation Discovery)
      IDENTIFY: Patterns_used_not_in_agent-os_documentation
      EVALUATE: Discovery_significance_and_documentation_value
      PROMPT: User_with_documentation_options
      IF user_approves:
        DOCUMENT: Add_to_appropriate_agent-os_file
        VALIDATE: Cross-reference_consistency
      REFERENCE: guidelines/documentation-discovery.md
    </step>
  </execution_protocol>

  <memory_management>
    - Execute ONE step at a time with context refresh between steps
    - Use external references instead of loading full patterns into memory
    - Implement checkpoints to preserve progress and enable recovery
    - Monitor token usage and compress context at 70% capacity
    - Clean up resources after each phase completion
  </memory_management>
</universal_task_execution>
```

## Implementation Requirements

### For All Specialist Agents

1. **Mandatory Planning Phase**
   - Always start complex tasks with explicit planning
   - Break tasks into 3-6 manageable steps (including knowledge capture)
   - Identify required external references upfront

2. **Step-by-Step Execution**
   - Execute one step at a time
   - Implement memory checkpoints between steps
   - Validate completion before proceeding

3. **External Reference Integration**
   - Use `.agent-os/[agent-name]-references/` for detailed content
   - Load specific patterns/templates on-demand
   - Avoid embedding large examples in core instructions

4. **Memory Optimization**
   - Core agent instructions: Max 150 lines
   - Progressive context loading
   - Regular cleanup and resource management

### Protocol Activation Triggers

Use this protocol when:
- Task requires more than 2 significant operations
- Multiple files or components involved
- Complex business logic or validation required
- Coordination with other agents needed
- Quality validation and testing required

**Note**: Step 6 (Knowledge Capture) should also be triggered during codebase exploration that reveals undocumented patterns, even without other implementation steps. Use `/document-discovery` for standalone knowledge capture.

### Simplified Tasks Exception

For simple, single-operation tasks:
- Direct execution is acceptable
- Planning phase can be abbreviated
- Still use external references for patterns

## Standard Phrases and Keywords

### Planning Phase Keywords
- `ANALYZE`: Examine requirements and constraints
- `DESIGN`: Create solution approach
- `PLAN`: Define execution sequence
- `ESTIMATE`: Assess complexity and resources

### Execution Phase Keywords
- `IMPLEMENT`: Execute the planned solution
- `VALIDATE`: Check quality and correctness
- `REFERENCE`: Use external documentation/patterns
- `CHECKPOINT`: Save progress and validate before continuing

### Memory Management Keywords
- `LOAD`: Bring specific external content into context
- `CLEANUP`: Remove unnecessary context
- `COMPRESS`: Reduce context size while preserving essentials
- `CHECKPOINT`: Save state for recovery

### Documentation Phase Keywords
- `IDENTIFY`: Find undocumented patterns used during task
- `EVALUATE`: Assess if discovery merits documentation
- `PROMPT`: Present discovery to user for decision
- `DOCUMENT`: Add approved content to agent-os

## Quality Standards

### Step Completion Criteria
Each step must:
- [ ] Achieve its stated objective
- [ ] Pass validation checkpoint
- [ ] Document results for next step
- [ ] Clean up temporary resources
- [ ] Confirm readiness for next phase

### Protocol Compliance Checklist
- [ ] Planning phase completed before implementation
- [ ] External references used instead of embedded content
- [ ] Memory checkpoints implemented between major steps
- [ ] Progressive context management maintained
- [ ] All resources cleaned up after completion
- [ ] Knowledge capture step executed (Step 6)
- [ ] Undocumented patterns identified and user prompted
- [ ] Documentation updated if discoveries approved

## Benefits of Universal Protocol

1. **Consistent User Experience**: All agents follow same predictable pattern
2. **Memory Efficiency**: Dramatic reduction in context window usage
3. **Quality Assurance**: Built-in validation and checkpoints
4. **Maintainability**: Standardized approach across agent ecosystem
5. **Scalability**: Protocol supports complex multi-step workflows
6. **Reliability**: Error recovery through checkpoint system
7. **Continuous Improvement**: Knowledge capture ensures documentation grows with usage

---

*This protocol implements 2025 best practices for memory-efficient LLM agent design with step-by-step execution, external memory management, and standardized quality assurance.*