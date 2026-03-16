# Commit Message Guidelines

## Commit Message Format

All commit messages should follow the format: `type(context): description`

## Commit Types

- **feat**: A new feature
- **fix**: Bug correction
- **docs**: Documentation changes
- **style**: Code formatting changes (spaces, indentation, etc.)
- **refactor**: Code changes that neither add features nor fix bugs
- **perf**: Performance improvements
- **test**: Add, fix, or improve tests
- **chore**: Build process and auxiliary tool changes

## Commit Guidelines

- Always use English
- Use imperative verb form: "change" not "changed" or "changes"  
- No capital letter at start of description
- No period (.) at the end
- Keep first line under 100 characters
- Reference issue codes in parentheses when applicable
- Do NOT add "Generated with Claude Code" or "Co-Authored-By: Claude" footers to commits
- **CRITICAL**: When adding user-visible text, include Spanish translations in the same commit

## Examples

```bash
feat(dte): add digital signature validation
fix(sii): correct document submission format
docs(claude): update component development guide
test(integration): add SII web service tests
chore(build): update gradle dependencies
```

## Context Guidelines

- Use component names for component-specific changes
- Use functional areas like "api", "ui", "auth" for cross-cutting concerns
- Use "build", "ci", "deploy" for infrastructure changes
- Keep context concise and meaningful