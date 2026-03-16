# Prompt: Relocate Misplaced Project-Specific Files

Use this prompt when project-specific files have been incorrectly created in the shared `runtime/component/moqui-agent-os/` directory instead of the project-specific `runtime/component/{project-name}/.agent-os/` directory.

## Problem

The shared `agent-os` repository is meant to contain only project-neutral framework guidance. Project-specific content belongs in each project's component-level `.agent-os/` directory. This includes:

- **Product files**: mission, roadmap, tech-stack, business requirements, etc. (`product/`)
- **Specifications**: feature specs, shaping documents, implementation plans (`specs/`)
- **Any content referencing specific component names, entities, services, or business logic**

## Detection

Check if misplaced project-specific files exist:

```bash
# Check for product/ or specs/ directories in shared agent-os
ls runtime/component/moqui-agent-os/product/ 2>/dev/null
ls runtime/component/moqui-agent-os/specs/ 2>/dev/null

# Check git status in agent-os repo
git -C runtime/component/agent-os status --short
```

If you see untracked files in `product/`, `specs/`, or similar project-specific directories, they need to be relocated.

## Prompt to Execute

Copy and run this prompt in Claude Code:

---

**Task: Relocate misplaced project-specific files from shared agent-os to project-specific directory**

1. **Identify the main project component**: Look for `.agent-os/` directories in `runtime/component/*/` to find the project-specific location. The main component typically has a name similar to the framework directory.

2. **Compare contents**: Read files from both locations:
   - Shared (wrong): `runtime/component/moqui-agent-os/product/` or `runtime/component/moqui-agent-os/specs/`
   - Project-specific (correct): `runtime/component/{main-component}/.agent-os/`

3. **Consolidate**:
   - If the project-specific directory has existing files, compare and merge content (newer files in shared location are likely more accurate)
   - If no existing files, simply move the content
   - Preserve comprehensive content from either location

4. **Clean up**: Remove the misplaced directories from the shared agent-os:
   ```bash
   rm -rf runtime/component/moqui-agent-os/product/
   rm -rf runtime/component/moqui-agent-os/specs/
   ```

5. **Verify**:
   ```bash
   # Shared agent-os should be clean
   git -C runtime/component/agent-os status --short

   # Project repo should show new/modified files
   git -C runtime/component/{main-component} status --short
   ```

---

## Expected File Structure

**Shared agent-os (project-neutral only):**
```
runtime/component/moqui-agent-os/
├── CLAUDE.md
├── development-guide.md
├── framework-guide.md
├── testing-guide.md
├── agent-registry.md
├── standards/
│   └── global/           # Framework-wide standards
├── guidelines/           # Framework-wide guidelines
└── prompts/              # Reusable prompts like this one
```

**NOT allowed in shared agent-os** (these are project-specific):
- `product/` - product mission, roadmap, personas
- `specs/` - feature specifications, shaping documents, implementation plans
- Any file referencing specific component names, entities, services, or business domains

**Project-specific (all product and spec content):**
```
runtime/component/{main-component}/.agent-os/
├── product/              # Mission, personas, roadmap, tech-stack
├── specs/                # Feature specs (YYYY-MM-DD-spec-name/)
├── specifications/       # Detailed design specifications
├── patterns/             # Project-specific patterns
└── ...                   # Any other project-specific docs
```

## Criteria: Is This File Project-Specific?

A file belongs in the project-specific `.agent-os/` if **any** of the following are true:

1. It references specific **component names** (e.g., `acme-inventory`, `acme-erp`)
2. It references specific **entity names** or **service names** from a component
3. It describes **business logic**, **user workflows**, or **feature requirements**
4. It contains **shaping notes**, **implementation plans**, or **design decisions** for a feature
5. It would not make sense in a different Moqui project

A file belongs in the shared `agent-os` if it is **purely about Moqui Framework patterns**, infrastructure, or tooling guidance that applies to any project.

## Prevention

The `development-guide.md` already contains guidance to prevent this issue. Ensure Claude Code sessions reference this file, which states:

> "IMPORTANT: Never create `product/` or `specs/` directories in the framework-level `agent-os/`. All project-specific content belongs in the component-level `.agent-os/`."
