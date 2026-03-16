---
name: moqui-framework
description: |
  Foundational Moqui Framework project knowledge including multi-repository structure, key directories, basic Gradle commands, component anatomy, and Agent OS detection.

  Use this skill when:
  - Starting a new session on a Moqui project
  - Needing to understand the multi-repository layout (framework, runtime, components)
  - Running basic Gradle commands (run, load, build, test)
  - Identifying which git repository a file belongs to
  - Finding the main component or Agent OS configuration
  - Understanding the standard component directory structure
---

# Moqui Framework Project Structure

## References

| Reference | Description |
|-----------|-------------|
| `../../references/project_structure.md` | Multi-repo structure, component discovery, Gradle tasks, Agent OS detection |

### Deep Reference (framework-guide.md)

For detailed patterns, search these sections in `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Moqui Framework Overview"** - Framework capabilities and technology stack
- **"## Project Structure"** - Multi-repository layout and file management
- **"## Standard Component Structure"** - Core directories and source code organization
- **"## Common Development Commands"** - Building, running, data management

### Additional Reference (development-guide.md)

For infrastructure and deployment, see `runtime/component/moqui-agent-os/development-guide.md`:
- Section `## Multi-Repository Management` - Repository structure, file management, git operations
- Section `### Submodule Status Gotcha` - Critical git status behavior across repositories

## Quick Reference

### Multi-Repository Structure
```
project-root/               ← Framework git repo
├── framework/               ← Moqui core code (part of framework repo)
├── runtime/                 ← Separate git repo
│   ├── conf/                ← Configuration files
│   ├── component/           ← Each subdirectory = separate git repo
│   │   ├── {main-component}/    ← Primary development target
│   │   ├── moqui-agent-os/      ← Agent OS (this config)
│   │   ├── mantle-udm/          ← Universal Data Model
│   │   ├── SimpleScreens/       ← Standard screens
│   │   └── ...
│   ├── log/
│   └── db/
├── build.gradle             ← Framework build (part of framework repo)
├── CLAUDE.md                ← Project instructions (part of framework repo)
├── .claude -> runtime/component/moqui-agent-os/.claude   ← symlink
└── .agent-os -> runtime/component/moqui-agent-os         ← symlink
```

### Essential Gradle Commands
```bash
./gradlew run              # Start Moqui server
./gradlew load             # Load all data files into database
./gradlew cleanAll         # Clean build artifacts and database
./gradlew test             # Run all tests
./gradlew gitPullAll       # Pull all repositories
./gradlew gitStatusAll     # Check status of all git repos
```

### Which Git Repo Am I In?
```bash
# Framework root → framework repo
git -C . status

# Runtime → runtime repo
git -C runtime status

# Component → component's own repo
git -C runtime/component/{name} status
```

### Finding the Main Component
The main component is the primary custom component under `runtime/component/`. It has its own `.agent-os/` directory with project-specific context. To identify it:
1. Look for a component with `.agent-os/` that contains product/business documentation
2. It is typically not a standard Moqui component (not mantle-udm, SimpleScreens, etc.)
3. The project is usually named after it

## Key Principles

1. **Each directory level = separate git repo**: Framework root, `runtime/`, and each component under `runtime/component/` are independent repositories
2. **Framework root appears clean**: `git status` at root won't show component changes — always check inside the component
3. **Agent OS is shared**: `runtime/component/moqui-agent-os/` is project-neutral; project-specific config lives in `{main-component}/.agent-os/`
4. **Symlinks connect everything**: `.claude` and `.agent-os` at project root are symlinks to `moqui-agent-os`