# Git Workflow and Commit Guidelines

## Incremental File Staging

When creating or modifying files as part of a task, **add each new file to the git index immediately** after creation:

```bash
git -C runtime/component/{component-name} add path/to/new-file.xml
```

This applies to all file types created during development work: service definitions, entity files, screens, data files, tests, and configuration. Staging incrementally ensures:
- New files are not accidentally left untracked
- The working state is visible via `git status` at any point
- The final commit captures all work without requiring a manual audit of untracked files

**Important**: Only stage files that are part of the current task. Do not use `git add .` or `git add -A` which may capture unrelated changes.

### Multi-Repository Awareness

Remember that each directory level has its own git repository (see `development-guide.md` > Multi-Repository Management). Always use the correct repository context:

```bash
# Component files
git -C runtime/component/{component-name} add src/new-service.xml

# Agent-os files (shared framework guidance)
git -C runtime/component/agent-os add commit-guidelines.md

# Runtime files
git -C runtime add conf/some-config.xml
```

## End-of-Task Commit Offer

At the end of a completed task or spec implementation, **offer to commit** with a short, one-line message. The workflow:

1. **Show a proposed one-line commit message** (under 72 characters) summarizing the change
2. **Offer to extend** to a multi-line message if the user wants to add details
3. **Wait for user approval** before committing — never auto-commit

### Commit Message Format

**One-line format (default offer):**
```
{type}: {concise description of what changed}
```

**Extended format (when user wants more detail):**
```
{type}: {concise description}

{paragraph explaining why the change was made, what problem it solves,
or what feature it enables}
```

Where `{type}` is one of: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`.

### Repository-Specific Conventions

Commit messages should be **relevant to the particular repository** being committed to.

**Project component repositories** (where the main development work happens):
- Reference the ticket/issue ID from the project's tracking system
- Example: `feat: MDT-1234 add postal address validation for DTE receiver`
- Example: `fix: MDT-1234 handle missing PartyGiro in NC generation`
- The specific ticket prefix (e.g., `MDT-`, `PROJ-`, `WMS-`) is defined in the component's `.agent-os/development-guide.md`

**Shared/framework repositories** (agent-os, runtime, framework):
- Use more general descriptions without ticket IDs
- Focus on what the change enables or improves
- Example: `feat: add commit guidelines for git workflow`
- Example: `fix: correct entity-find element ordering in framework guide`

**Supporting changes in secondary repositories**:
- When a change in one repo necessitates a change in another (e.g., adding a parameter in a shared component needed by the project component), describe the change in terms of what it enables
- Example in shared repo: `feat: add organizationPartyId parameter to refresh service`
- Example in project repo: `feat: MDT-567 use org-scoped refresh for daily processing`

### What Makes a Good Commit Message

- **Describes the "what"** concisely in the first line
- **Explains the "why"** in the extended body when the reason isn't obvious
- **References the ticket** in project repos so changes are traceable
- **Stands alone** — someone reading `git log --oneline` should understand the change
- **Uses imperative mood** — "add validation" not "added validation"