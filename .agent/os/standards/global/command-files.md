# Command File Convention

### Core Principle

**Command files in `.claude/commands/` MUST be thin wrappers that delegate workflow details to instruction files in `instructions/core/`.**

Commands should never contain inline workflow logic. All procedural steps, templates, and detailed instructions belong in separate instruction files.

### Required Structure

Every command file MUST follow this structure:

```markdown
# Command Title

Brief description (1-2 sentences).

## Required Context

Load the following skills and guidelines for this workflow:

- @framework-guide.md
- @guidelines/data-updates.md

## Workflow

Refer to the detailed workflow instructions located in @instructions/core/{command-name}.md
```

### Rules

| Rule | Details |
|------|---------|
| **Thin wrapper** | Command files contain ONLY title, description, context references, and instruction file pointer |
| **No inline workflows** | All workflow steps, templates, and examples go in `instructions/core/{command-name}.md` |
| **Required Context** | Commands that orchestrate Moqui work SHOULD load `@framework-guide.md` |
| **Data changes** | Commands involving data changes SHOULD also load `@guidelines/data-updates.md` |
| **`@` prefix** | The `@` prefix loads referenced files as context when the command is invoked |
| **Naming** | Instruction file name MUST match the command file name |

### Why This Matters

- **Separation of concerns**: Command registration is separate from workflow logic
- **Reusability**: Instruction files can be referenced from multiple contexts
- **Maintainability**: Changes to workflow don't require touching the command file
- **Consistency**: All commands follow the same predictable structure

### Anti-Pattern

```markdown
<!-- BAD: Inline workflow in command file -->
# My Command

## Step 1: Do this thing
...200 lines of workflow...

## Step 2: Do another thing
...more inline content...
```

```markdown
<!-- GOOD: Thin wrapper with instruction reference -->
# My Command

Brief description.

## Required Context

- @framework-guide.md

## Workflow

Refer to the detailed workflow instructions located in @instructions/core/my-command.md
```
