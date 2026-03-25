# Process: Environment Commands

This file documents the specific command syntax required for this development environment. Use these patterns instead of generic defaults.

## Gradle Commands

### Loading Data
To load data for a specific component, use the `-Pcomponent` property:
```bash
./gradlew load -Pcomponent=[componentName]
```
*Note: Do not use the `component=[name]` syntax without `-P`, as it may not be correctly parsed by the build script.*

## Git Commands
Use the `git-sync` skill for all components as defined in `workflows.md`.
