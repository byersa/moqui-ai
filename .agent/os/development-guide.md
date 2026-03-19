# Moqui Development Guide (Framework Level)

This document provides organization-wide development workflows, infrastructure, and practices. For project-specific guidance, see the main component's Agent OS configuration.

**Note**: For project-specific development guidance, business context, and component details, see `runtime/component/{main-component}/.agent-os/` directory.

## Three-Level Documentation Hierarchy

**CRITICAL: The project ecosystem uses a three-level documentation structure. Each level has distinct responsibilities.**

### Level 1: Framework (`runtime/component/moqui-agent-os/`)

**Scope**: Project-neutral Moqui Framework guidance
**Audience**: All Moqui projects
**Content**:
- Framework patterns and best practices
- Testing strategies and infrastructure
- Deployment guides and CI/CD
- Reusable prompts and agent specifications
- Global standards (tech stack, commit guidelines)

**Rule**: Never add project-specific or business-specific content here.

### Level 2: Shared Component (`runtime/component/{shared-component}/.agent-os/`)

**Scope**: Shared ERP/WMS functionality documentation
**Audience**: All projects using {shared-component}
**Content**:
- Generic inventory, purchasing, warehouse capabilities
- Shared entity and service documentation
- Integration patterns for projects
- Feature availability and roadmap for shared functionality

**Rule**: Only document features that benefit multiple projects. Configurable, reusable components.

### Level 3: Project (`runtime/component/{project}/.agent-os/`)

**Scope**: Project-specific business context
**Audience**: Single project team
**Content**:
- Client-specific requirements and workflows
- Project mission, personas, and problem statements
- Custom business rules and regulatory needs
- Project-specific roadmap and milestones
- Technical decisions unique to the project

**Rule**: All client-specific content belongs here, never in shared components.

### Documentation Location Guide

| Content Type | Location |
|--------------|----------|
| Moqui patterns & best practices | `moqui-agent-os/` (framework) |
| Generic WMS/ERP features | `{shared-component}/.agent-os/` (shared) |
| Client-specific requirements | `{project}/.agent-os/` (project) |
| Reusable prompts | `moqui-agent-os/prompts/` (framework) |
| Project personas & problems | `{project}/.agent-os/` (project) |
| Shared entity documentation | `{shared-component}/.agent-os/` (shared) |

## Overlay Mechanism

The `moqui-agent-os` repository contains **project-neutral** Moqui Framework guidance. Organizations that need to add their own conventions, infrastructure details, credentials, or specialist agents use a separate **overlay repository** that layers organization-specific content on top.

### Architecture

```
runtime/component/
├── moqui-agent-os/          # Public repo — generic Moqui patterns
│   ├── .claude/
│   │   ├── agents/          # Framework specialist agents
│   │   ├── commands/        # Generic commands
│   │   ├── skills/          # Moqui skills
│   │   └── settings.local.json  # (overlay — gitignored)
│   ├── guidelines/          # Process guidance (+ overlay files)
│   ├── instructions/core/   # Workflow instructions (+ overlay files)
│   ├── standards/           # Conventions (+ overlay files)
│   ├── scripts/             # Automation scripts (+ overlay files)
│   ├── framework-guide.md
│   ├── development-guide.md
│   ├── testing-guide.md
│   └── overlay-*.md         # (overlay root files — gitignored)
│
└── {org}-agent-os/          # Private overlay repo — organization-specific
    ├── overlay/             # Mirrors moqui-agent-os structure
    │   ├── .claude/
    │   │   ├── agents/overlay-*.md
    │   │   ├── commands/overlay-*.md
    │   │   ├── skills/overlay-*
    │   │   └── settings.local.json
    │   ├── guidelines/overlay-*.md
    │   ├── instructions/core/overlay-*.md
    │   ├── standards/{category}/overlay-*.md
    │   ├── scripts/overlay-*.sh
    │   ├── overlay-{org}-project-profile.md
    │   ├── overlay-infrastructure.md
    │   └── overlay-local-dev-setup.md
    └── overlay.sh           # Idempotent symlink script
```

### How It Works

1. The overlay repo contains an `overlay/` directory that mirrors the `moqui-agent-os` structure
2. Running `overlay.sh` creates **symlinks** from `moqui-agent-os/` to the overlay files
3. The public repo's `.gitignore` ignores all `overlay-*` files and `settings.local.json`
4. Symlinks survive `git pull` in the public repo (they're gitignored, not tracked)

### How `overlay.sh` Works

The `overlay.sh` script lives in the **overlay repo** (not moqui-agent-os). It operates in two phases:

1. **Clean phase**: Finds and deletes all existing `overlay-*` symlinks anywhere in the `moqui-agent-os` tree (`find -name "overlay-*" -type l -delete`). Also removes `settings.local.json` if it is a symlink.
2. **Link phase**: Iterates every file under the overlay repo's `overlay/` directory, computes its relative path, creates the target directory in `moqui-agent-os` if needed, and creates a symlink (`ln -sf`).

This means the overlay can place files **anywhere** in the `moqui-agent-os` tree — the only requirement is that filenames start with `overlay-` (except `settings.local.json`). The `.gitignore` patterns in the public repo must cover all target locations to prevent accidental commits.

**Reference implementation** (place this as `overlay.sh` at the root of your overlay repo):

```bash
#!/bin/bash
# overlay.sh — Run from the overlay repo directory
# Creates symlinks from moqui-agent-os -> overlay files
# Safe to re-run at any time (idempotent)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
AGENT_OS="$(cd "$SCRIPT_DIR/../moqui-agent-os" && pwd)"

if [ ! -d "$AGENT_OS" ]; then
    echo "Error: moqui-agent-os not found at $AGENT_OS"
    exit 1
fi

echo "Overlay: $SCRIPT_DIR"
echo "Target:  $AGENT_OS"

# Phase 1: Clean existing overlay symlinks
find "$AGENT_OS" -name "overlay-*" -type l -delete
# Also clean settings.local.json if it's a symlink
[ -L "$AGENT_OS/.claude/settings.local.json" ] && rm "$AGENT_OS/.claude/settings.local.json"

# Phase 2: Create fresh symlinks
count=0
for src in $(find "$SCRIPT_DIR/overlay" -type f); do
    rel="${src#$SCRIPT_DIR/overlay/}"
    target="$AGENT_OS/$rel"
    mkdir -p "$(dirname "$target")"
    ln -sf "$src" "$target"
    echo "  Linked: $rel"
    count=$((count + 1))
done

echo "Done. $count overlay files linked."
```

### Valid Overlay Locations

All overlay files must be prefixed with `overlay-` (except `settings.local.json`). The following target locations are supported — each is covered by a corresponding `.gitignore` pattern in the public repo:

| Overlay Source (`overlay/...`) | Target in `moqui-agent-os/` | Purpose |
|------|--------|---------|
| `.claude/agents/overlay-*.md` | `.claude/agents/` | Organization-specific specialist agents |
| `.claude/commands/overlay-*.md` | `.claude/commands/` | Organization-specific slash commands |
| `.claude/skills/overlay-*` | `.claude/skills/` | Organization-specific skills |
| `.claude/settings.local.json` | `.claude/settings.local.json` | IDE/tool settings with absolute paths |
| `instructions/core/overlay-*.md` | `instructions/core/` | Organization-specific workflow instructions |
| `guidelines/overlay-*.md` | `guidelines/` | Organization-specific process guidance |
| `standards/{category}/overlay-*.md` | `standards/{category}/` | Organization-specific conventions |
| `scripts/overlay-*.sh` | `scripts/` | Organization-specific automation scripts |
| `overlay-*.md` | Root level | Infrastructure, project profiles, dev setup |

**Important**: If you add a new target location not listed above, you must also add a corresponding pattern to the `moqui-agent-os/.gitignore` file.

### The Project Profile

The overlay provides an `overlay-{org}-project-profile.md` file that maps generic `{placeholder}` values to concrete, organization-specific values:

| Placeholder | Example Concrete Value |
|-------------|----------------------|
| `{l10n}-install` | Organization's localization install data type |
| `{project}-demo` | Organization's demo data type |
| `{project}-test` | Organization's shared test data type |
| `{component}-test` | Component-specific test data type |
| `{localization-component}` | Name of the l10n component |
| `{shared-component}` | Name of the shared ERP component |
| `{container-registry}` | Docker registry URL |
| `{admin-user}` / `{admin-password}` | Dev/test credentials |
| `{project-branch}` | Branch naming convention |

Claude Code reads this profile automatically to resolve placeholders when executing commands.

### Setting Up the Overlay

```bash
# 1. Clone the public repo
git clone <moqui-agent-os-url> runtime/component/moqui-agent-os

# 2. Clone the overlay repo
git clone <org-overlay-url> runtime/component/{org}-agent-os

# 3. Run the overlay script to create symlinks
cd runtime/component/{org}-agent-os
./overlay.sh

# 4. Create project-root symlinks (see Claude Code Discovery below)
ln -sf runtime/component/moqui-agent-os/.claude .claude
ln -sf runtime/component/moqui-agent-os .agent-os
```

### Re-running the Overlay

`overlay.sh` is **idempotent** — it removes all existing `overlay-*` symlinks, then recreates them from the current overlay directory contents. Re-run it:
- After `git pull` in the overlay repo
- After adding or removing overlay files
- Not needed after `git pull` in `moqui-agent-os` (symlinks are unaffected)

### Creating an Overlay Repo From Scratch

To create a new overlay for your organization:

**1. Create the repo structure:**
```
{org}-agent-os/
├── overlay/
│   ├── .claude/
│   │   └── settings.local.json    # (optional) Tool settings
│   └── overlay-{org}-project-profile.md  # (recommended) Placeholder values
├── overlay.sh                     # (required) Symlink script — copy from above
├── .gitignore                     # (recommended) Ignore generated files
└── README.md                      # (optional) Overlay-specific documentation
```

**2. Minimum viable overlay** — only two files are required:
- `overlay.sh` — copy the reference implementation above
- `overlay/overlay-{org}-project-profile.md` — map placeholders to your values

**3. Add files incrementally** as your organization needs them:
- Infrastructure docs: `overlay/overlay-infrastructure.md`
- Local dev setup: `overlay/overlay-local-dev-setup.md`
- IDE settings: `overlay/.claude/settings.local.json`
- Custom agents: `overlay/.claude/agents/overlay-{name}.md`
- Custom commands: `overlay/.claude/commands/overlay-{name}.md`
- Organization standards: `overlay/standards/{category}/overlay-{name}.md`
- Process guidelines: `overlay/guidelines/overlay-{name}.md`
- Workflow instructions: `overlay/instructions/core/overlay-{name}.md`
- Automation scripts: `overlay/scripts/overlay-{name}.sh`

**4. Run `./overlay.sh`** after adding any files to create the symlinks.

**5. `.gitignore` for the overlay repo** — typically ignore generated/downloaded files:
```
# Example .gitignore for overlay repo
version.json
```

### What Belongs in the Overlay

| Content | Location |
|---------|----------|
| Organization credentials and URLs | Overlay project profile |
| Branch naming conventions | Overlay project profile |
| Concrete data type mappings | Overlay project profile |
| Production infrastructure docs | `overlay/overlay-infrastructure.md` |
| Local dev environment setup | `overlay/overlay-local-dev-setup.md` |
| Organization-specific agents | `overlay/.claude/agents/overlay-{name}.md` |
| Organization-specific commands | `overlay/.claude/commands/overlay-{name}.md` |
| Organization-specific skills | `overlay/.claude/skills/overlay-{name}` |
| IDE settings with absolute paths | `overlay/.claude/settings.local.json` |
| Organization coding conventions | `overlay/standards/{category}/overlay-{name}.md` |
| Organization process guidance | `overlay/guidelines/overlay-{name}.md` |
| Organization workflow instructions | `overlay/instructions/core/overlay-{name}.md` |
| Organization automation scripts | `overlay/scripts/overlay-{name}.sh` |

### What Does NOT Belong in the Overlay

| Content | Correct Location |
|---------|-----------------|
| Feature specifications | `{main-component}/.agent-os/specs/` |
| Product mission and personas | `{main-component}/.agent-os/` |
| Business-specific entities/services | Component source code |
| Framework patterns | `moqui-agent-os/` (public repo) |

## Finding the Project-Specific Agent OS Configuration

**CRITICAL: This framework-level .agent-os directory is shared across multiple projects and MUST remain project-neutral.**

To locate the project-specific Agent OS configuration:

1. **Auto-detection approach**: Look for `.agent-os/` directories in `runtime/component/*/`
2. **Convention-based approach**: The framework directory name typically matches the main component name
3. **Component priority**: Look for components with names closest to the framework directory name

### Instructions for Claude Code

**IMPORTANT: Never create a `product/` directory in the framework-level `.agent-os/`. All project-specific content belongs in the component-level `.agent-os/`.**

When starting a session:
1. Search for `.agent-os/` directories in `runtime/component/` subdirectories
2. If multiple directories exist, prioritize based on:
   - Component with same name as framework directory
   - Component with name closest to framework directory name
   - Most recently modified `.agent-os/` directory as fallback
3. Use that component's `.agent-os/` directory for ALL project-specific guidance including:
   - Product mission and roadmap
   - Business context and user personas
   - Component-specific development patterns
   - Technical stack details
   - Regulatory requirements
4. Reference `{shared-component}/.agent-os/` for shared ERP functionality context
5. Use this file ONLY for general infrastructure and development workflows

## Agent Delegation Strategy

**CRITICAL: Claude Code must ALWAYS use specialist agents for domain-specific tasks instead of performing the work directly.**

### Core Delegation Principle

Claude Code should act as a coordinator and analyzer, delegating all specialized development work to domain-specific sub-agents using the Task tool. This ensures:
- Expert-level implementation using specialized knowledge
- Consistent patterns and best practices across components
- Proper error handling and validation procedures
- Comprehensive quality assurance standards

### Required Specialist Agents

**ALWAYS use the Task tool with these specialist agents for their respective domains:**

*For complete agent registry and specifications, see `.agent-os/agent-registry.md`*

#### Moqui Framework Specialists

**Entity and Data Management:**
- **moqui-entity-specialist**: For entity definitions, database schema, and entity relationships
- **moqui-data-specialist**: For data files, seed data, configuration data, and EntityFilter definitions

**Service Development (use specialized agents):**
- **moqui-service-definition-specialist**: For service interfaces, parameters, and contracts
- **moqui-service-implementation-specialist**: For business logic, workflow patterns, transactions, and filter context setup
- **moqui-service-integration-specialist**: For external API consumption, webhooks, and resilient communication

**API Development:**
- **moqui-rest-api-specialist**: For REST API endpoints, resource definitions, and authentication
- **moqui-xml-api-specialist**: For XML/SOAP API integration and token-based authentication

**Screen Development:**
- **moqui-screen-specialist**: For screen structure, layout, forms, navigation, transitions, and filter debugging (unified specialist)
- **moqui-screen-ux-specialist**: For user experience, accessibility, and performance optimization

**Testing:**
- **moqui-test-specialist**: For unit and integration testing with Spock, test data, and authorization testing (unified specialist)
- **moqui-test-execution-specialist**: For test execution, result analysis, performance optimization, and CI/CD integration

**Localization:**
- **moqui-l10n-specialist**: For localization and internationalization, translations

#### Build and Infrastructure Specialists
- **moqui-build-coordinator**: For build configuration, deployment tasks, and infrastructure management
- **agent-builder**: For agent configuration updates, workflow optimization, and agent ecosystem management

#### Documentation and Presentation Specialists
- **presentation-specialist**: For LaTeX presentation creation and management using beamer package, supporting multi-format compilation, reusable content libraries, and Spanish language documentation
- **course-session-specialist**: For educational course and session planning, applying learning science principles, designing format-specific activities, and creating comprehensive learning experiences with assessment strategies

#### Project Coordination
- **moqui-development-coordinator**: For complex multi-component tasks requiring coordination across multiple specialist agents

### Implementation Requirements

**Task Tool Usage Pattern:**
```
Use Task tool with appropriate specialist agent:
- Provide clear context and requirements
- Include relevant file paths and current state
- Specify quality standards and validation criteria
- Request structured output with implementation details
```

**Never Directly:**
- Edit Moqui entity files (.xml in entity directories)
- Modify service definitions (.xml in service directories)  
- Update screen definitions (.xml in screen directories)
- Create or modify data files (.xml in data directories)
- Write test files (.groovy in test directories)
- Update build configurations (build.gradle, settings.gradle)
- Create or modify LaTeX presentation files (.tex files, beamer presentations)

**Exception**: Simple file reading, analysis, and reporting tasks that don't involve code modifications.

### Quality Validation

After specialist agent execution:
1. **Review Implementation**: Validate that specialist agent followed best practices
2. **Check Integration**: Ensure changes integrate properly with existing codebase
3. **Verify Standards**: Confirm adherence to project coding standards and conventions
4. **Test Recommendations**: Suggest appropriate testing strategies for the changes

This delegation strategy ensures consistent, high-quality implementations while leveraging specialized expertise for each domain area.

## Critical Development Requirements

### Localization (L10n) is MANDATORY

**EVERY text element that users will see MUST have Spanish translation added immediately.**

When adding or modifying any user-visible text:
1. **Add the English text** in your component
2. **Add translation** in the same commit to `{localization-component}/data/l10n-*.xml`
3. **Never defer translations** - they are part of the feature, not optional

This applies to:
- StatusItem and Enumeration descriptions
- Screen labels, form fields, buttons
- Error messages and notifications
- Any text displayed to users

See the [Localization Best Practices](#localization-l10n-best-practices) in framework-guide.md for detailed instructions.

## Infrastructure & Deployment

Infrastructure and deployment details (container orchestration, CI/CD pipelines, database management, container registries, etc.) are organization-specific and provided by the overlay repository. See the overlay's `overlay-infrastructure.md` for your organization's deployment architecture and `overlay-local-dev-setup.md` for local development database and OpenSearch setup.

## IDE Configuration

When working with Moqui projects in your IDE, ensure proper configuration for optimal performance and functionality:

### Automated Configuration Check
**Recommended**: Use the `/check-ide` command to automatically validate and fix your IDE configuration:
- Validates git repository structure, Java/Groovy environment, XML schema configuration
- Auto-detects testable components and creates appropriate test run configurations
- Provides one-click fixes for common configuration issues
- Creates run configurations with proper data type patterns (`{component-name}-test`)

### Initial Project Setup
Follow the instructions at https://moqui.org/m/docs/framework/IDE+Setup under "Create Project from Gradle Files"

### VCS Directory Mappings
- **Critical**: Periodically check that all VCS directory mappings are added for multi-repository components
- Go to Preferences → Version Control and add all directories displayed under "Unregistered roots"
- Should include `runtime/` and all components under `runtime/component/` that are separate git repositories
- Each component in `runtime/component/` should have its own VCS mapping if it's a separate git repository

### Language Injection for Groovy in XML
- **Essential**: Configure Language Injection to enable Groovy code recognition within XML files
- Follow instructions at https://moqui.org/m/docs/framework/IDE+Setup under "Language Injection for Groovy in XML"
- This enables proper syntax highlighting, auto-completion, and error detection for Groovy code embedded in Moqui XML files

### XML Schema Configuration
- Under Languages & Frameworks → Schemas and DTDs → XML Catalog, define Catalog property file
- Use the framework's catalog file to enable XML validation and auto-completion for Moqui XML files
- May require IDE restart to take effect

### Moqui Framework Libraries
- Go to File → Project Structure → Libraries
- Ensure `framework/build/libs/moqui-framework-*.jar` is included (available after build)
- If missing, add it using the '+' button and select the JAR file
- Include in all modules when prompted for proper auto-completion and IDE scaffolding

### Excluded Directories (Performance Optimization)
Configure the following directories to be excluded from indexing to improve IDE performance:
- `/runtime/db` - Database files (H2, Derby, PostgreSQL data)
- `/runtime/lib` - Runtime libraries and JAR files
- `/runtime/log` - Application log files
- `/runtime/opensearch` - OpenSearch data (or `/runtime/elasticsearch`)
- `/runtime/sessions` - User session data
- `/runtime/tmp` - Temporary files and cache

### Component-Level Git Ignores
Ensure that within each component directory, the following are ignored by git:
- `build/` - Build artifacts and compiled classes
- `lib/` - Component-specific libraries (if not version controlled)

## Documentation Resources

### Project Documentation Wiki
- **Primary Resource**: The project wiki directory contains comprehensive project-specific documentation for working with Moqui Framework
- Key sections:
  - `Moqui/How-To/IDE-Setup.md` - Detailed IDE setup instructions
  - `Moqui/Conceptos.md` - Core Moqui concepts
  - `Moqui/FAQ/` - Frequently asked questions and solutions
  - `Moqui/Prácticas-de-Desarrollo/` - Development best practices
- Check for consistency between framework documentation and wiki content

### Official Moqui Documentation
- Framework documentation: https://moqui.org/m/docs/framework
- IDE Setup guides: https://moqui.org/m/docs/framework/IDE+Setup

## Multi-Repository Management

### Repository Structure
All Moqui projects follow the standard multi-repository structure:

- **Framework Repository**: The entire repository (named after the main component)
  - Contains the main Moqui framework in `framework/` directory
  - Contains the runtime environment in `runtime/` directory
  - Orchestrates multiple git repositories through gradle tasks
  - **IMPORTANT**: Only files in the framework root are tracked in this repository

- **Runtime Repository**: `runtime/` directory
  - **Separate git repository** from the framework
  - Contains configuration files, logs, database, and runtime-specific content
  - Files added to `runtime/` (except components) are committed to the runtime repository

- **Main Component**: `runtime/component/{component-name}` 
  - This is the primary custom component being developed
  - **Separate git repository** with its own commit history
  - **This component's repository is the focus of development work**

- **Other Components**: Located in `runtime/component/`
  - Standard Moqui components (mantle-udm, SimpleScreens, etc.)
  - Third-party and custom components
  - **Each component is its own git repository** with independent versioning

### File Management Across Repositories

**CRITICAL: Each directory level has its own git repository:**

#### Framework Repository Files
- `framework/` - Framework code
- `build.gradle`, `settings.gradle` - Build configuration
- `Claude.md` - Framework-level documentation
- `.claude/` → `runtime/component/moqui-agent-os/.claude` (symlink)
- `.agent-os/` → `runtime/component/moqui-agent-os` (symlink)
- Root-level configuration files

#### Runtime Repository Files  
- `runtime/conf/` - Configuration files
- `runtime/log/` - Application logs
- `runtime/db/` - Database files when using integrated DB
- Files directly under `runtime/` (not in component subdirectories)

#### Component Repository Files
- `runtime/component/{component-name}/` - Each component is its own repository
- `runtime/component/{component-name}/.agent-os/` - Component-specific Agent OS configuration
- When adding files to a component, commit within that component's repository
- Component-specific Claude.md files live in their respective component repositories

#### Adding Files - Repository Navigation
```bash
# Framework files - commit from framework root
git add Claude.md .agent-os/
git commit -m "framework change"

# Runtime files - navigate to runtime and commit there
cd runtime
git add conf/MoquiDevConf.xml
git commit -m "runtime config change"

# Component files - navigate to specific component
cd runtime/component/{component-name}
git add src/new-file.groovy .agent-os/
git commit -m "component change"
```

**Always check which repository you're in before committing:**
```bash
pwd  # Check current directory
git status  # Verify you're in the correct repository
```

### Submodule Status Gotcha

**CRITICAL**: Running `git status` in the **framework root** will show the working tree as "clean" even when component submodules have untracked or modified files. This is because components are separate git repositories.

To see actual changes in a component:
```bash
# WRONG: Shows parent repo status only (will appear "clean")
git status

# CORRECT: Check status inside the component's own repository
git -C runtime/component/{component-name} status

# Or navigate into the component
cd runtime/component/{component-name}
git status
```

**When to use this**: After creating or modifying files in a component directory, always verify changes using `git -C` or by navigating into the component directory before committing.

### Git Operations Across Repositories
```bash
# Pull all repositories (framework, runtime, components)
gradle gitPullAll

# Check status of all git repositories
gradle gitStatusAll

# Checkout specific branch across all repos
gradle gitCheckoutAll -Pbranch=branch-name
```

## Multi-Component Feature Workflow

When a feature spans multiple component repositories (e.g., a WMS feature that modifies both the project component and shared ERP component), follow this workflow to coordinate changes across repos.

### Branching Strategy

Each component repository gets its own feature branch using the same ticket/feature ID:

```
my-app repo:     feature/PROJ-42-product-master
shared-erp repo: feature/PROJ-42-product-master
framework repo:  (usually unchanged)
```

Use `gradle gitCheckoutAll -Pbranch=feature/PROJ-42-product-master` to create branches across all repos, or create them individually for only the affected components.

### Migration Coordination

Features spanning multiple repos use the project's migration system with cross-component dependencies:

1. **Same ticket ID**: All migrations for the feature share the ticket ID (e.g., `PROJ-42`)
2. **Explicit dependencies**: Declare ordering between component migrations
3. **Dependency resolution**: The migration system resolves the dependency graph at startup regardless of which repo each migration lives in

See `guidelines/data-updates.md` for the generic migration workflow and the organization's overlay for implementation-specific conventions.

### PR Coordination

- Create PRs in each affected component repository
- Link PRs in their descriptions (reference the ticket ID)
- Merge shared/infrastructure components first, then project-specific components
- CI/CD should test with all feature branches checked out together

### Parallel Feature Branches

Different features may be developed in parallel on different branches. Since migrations use feature-scoped IDs (not semver), there are no version conflicts between parallel branches. When branches merge to the development branch, the migration system executes all pending migrations respecting the dependency graph.

## Release Preparation Checklist

Before merging to the production branch, complete these steps. The `/prepare-migration` command automates most of this workflow.

### 1. Version Bump

For each component with changes:
- Determine the appropriate semver bump (Major/Minor/Patch)
- Update `version` in `component.xml`
- If version was already bumped in a previous commit, verify it's still appropriate

### 2. Migration Relabeling

For each migration registry file (`data/MigrationRegistry.xml`):
- Change `version` from ticket ID to the release semver (e.g., `"PROJ-42"` → `"1.2.0"`)
- **Never change** `migrationId` (this is the primary key and preserves idempotency)
- Verify all `dependsOn` references are still valid

### 3. Validation

- All migration service references resolve to existing services
- No circular dependencies in the migration graph
- All entity changes have corresponding migrations where needed
- Run `migration_dry_run=true` to verify the expected migration execution order

### 4. Cross-Repository Consistency

- All component repos are on the correct branch
- No uncommitted changes in any component (`gradle gitStatusAll`)
- All PRs for the feature are ready to merge

### 5. Commit and Merge

- Commit relabeled migrations and version bumps
- Merge shared/infrastructure components first
- Merge project-specific components last
- Verify production startup with `migration_dry_run=true` before enabling auto-run

## Testing Strategy

> **📚 Comprehensive Testing Documentation Available:**
> - **[Unified Testing Strategy](./testing-guide.md)** - Complete testing approach covering unit, integration, and E2E testing
> - **[E2E Test Data Strategy](./e2e-test-data-strategy.md)** - Framework-agnostic test data generation patterns
> - **[Moqui Testing Implementation Guide](./moqui-testing-guide.md)** - Quick start guide with critical success factors and pitfall solutions
> - **[Testing Best Practices](./testing-best-practices.md)** - Enterprise patterns from mantle-usl analysis
> - **[DTE Testing Patterns](./dte-testing-patterns.md)** - Chilean DTE-specific testing patterns

### Testing Framework

**Multi-Layer Testing Approach:**
- **Unit & Integration**: Spock testing framework (Groovy-based)
  - Test files should end with `*Tests.groovy` or `*BasicFlow.groovy`
  - Located in `src/test/groovy/` directory within each component
  - Requires build.gradle configuration with test dependencies
  - See `mantle-usl/src/test/groovy/` for comprehensive test examples

- **End-to-End Testing**: Playwright framework (JavaScript/TypeScript)
  - Framework-level installation with component-specific configurations
  - Located in `runtime/component/{component}/tests/` directories
  - Multi-browser support (Chromium, Firefox, WebKit)
  - See `PLAYWRIGHT-SETUP.md` for detailed configuration
  - See `.agent-os/testing-guide.md` for unified testing strategy

### Running Tests

**Unit & Integration Tests (Spock)**

**STANDARD PROCEDURE: Always clean database before loading data and testing**
```bash
# Step 1: Clean database and search indices (CRITICAL)
./gradlew cleanDb

# Step 2: Load all seed and demo data (REQUIRED)
./gradlew load -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo

# Step 3: Run tests for a specific component (preferred approach)
./gradlew :runtime:component:{component-name}:test

# Examples:
./gradlew :runtime:component:{component-name}:test
./gradlew :runtime:component:mantle-usl:test
./gradlew :runtime:component:{localization-component}:test

# Run all tests for all components (not recommended - slow)
./gradlew test

# Start/stop ElasticSearch for tests
./gradlew startElasticSearch
./gradlew stopElasticSearch
```

**End-to-End Tests (Playwright)**

```bash
# Run all E2E tests (from framework root)
npm test

# Run component-specific tests
npm run test:{component-name}

# Run with UI mode for debugging
npx playwright test --ui --config runtime/component/{component}/playwright.config.js

# Run specific test file
npx playwright test runtime/component/{component}/tests/functional/workflow.spec.js

# Debug mode with visible browser
npx playwright test --headed --config runtime/component/{component}/playwright.config.js
```

**Why cleanDb is required**: Before loading seed data, you must ensure the database is empty as well as the ElasticSearch or OpenSearch index. This prevents conflicts with existing data and ensures consistent test environments.

### Critical Testing Requirements

**Unit & Integration Tests (Spock):**
1. **Data Loading**: Load required data types based on component being tested
   - **Most components**: `./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{project}-test`
   - **Localization component**: `./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{component}-test,{project}-test`
   - **Note**: Only the localization component uses the `{component}-test` type pattern. Other project components use only `{project}-test` for common test data

**End-to-End Tests (Playwright):**
1. **Test Data Strategy**: Use comprehensive test data generation patterns
   - **Reference**: See `.agent-os/e2e-test-data-strategy.md` for complete implementation guide
   - **Approach**: Service-based data factories with proper isolation and cleanup
2. **Database Isolation**: Implement proper test data isolation
   - **Transaction-based**: Recommended for most scenarios
   - **Prefix-based**: For complex multi-entity tests
   - **Schema-based**: For complete isolation requirements
3. **Suite-Based Execution**: Organize tests with JUnit 5 @Suite annotation
4. **Single-Threaded**: Set `maxParallelForks = 1` in build.gradle
5. **Sequence ID Management**: Use `tempSetSequencedIdPrimary()` for test isolation
6. **Error Handling**: Use `ec.message.hasError()` and `ec.message.getErrorsString()` (see testing-guide.md)
7. **Hazelcast Configuration**: Use auto-increment ports to prevent conflicts
8. **Database Strategy**: Use main database with pre-loaded data (recommended)

### Testing Best Practices
- Use mantle-usl as reference for comprehensive testing patterns
- Each component with tests must have a build.gradle file configured for testing
- Test isolation and cleanup procedures
- Integration testing with OpenSearch services
- E2E testing using Playwright framework in CI/CD pipeline

### Validated Testing Workflow

**Complete Test Execution Process (Validated):**
```bash
# For localization component (uses component-specific test data)
./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test
./gradlew runtime:component:{localization-component}:test

# For other project components (use only {project}-test data)
./gradlew cleanDb load -Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{project}-test
./gradlew runtime:component:{shared-component}:test

# Expected Results:
# - All tests pass with 100% success rate
# - Tests complete within seconds
# - No authentication failures
# - No hanging issues
```

**Key Fixes Applied:**
- ✅ Log4j AsyncAppender deadlock resolved (blocking="false")
- ✅ Database configuration simplified (main database vs separate test database)
- ✅ Hazelcast auto-increment ports configured
- ✅ UserAccount authentication working with {project}-test data type
- ✅ Data loading strategy standardized across components

For detailed implementation guidance, refer to the testing documentation linked above.

## Troubleshooting Common Issues

### Authorization and Data Loading Issues

#### Non-Existent User Group Errors

**Symptom**: Error loading data files with `userGroupId="GROUP_NAME" does not exist`

**Diagnosis Commands**:
```bash
# Find all userGroupId references in data files
grep -r "userGroupId=" runtime/component/*/data/*.xml

# Check if user groups exist in security data
grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml
```

**Solutions**:
1. **Use existing groups**: Replace with `ADMIN`, `{ProjectAdminUsers}`, or `ALL_USERS`
2. **Check existing groups**: Look at `ScreenSetup.xml` for component-specific groups
3. **Remove unnecessary authorization**: Follow main artifact authorization pattern

#### Over-Complex Authorization Errors

**Symptom**: Complex authorization setups that fail to load or work incorrectly

**Solution Pattern**: Use main artifact authorization - define at screen level, let services inherit

### Quick Authorization Validation
```bash
# Validate all user groups exist
for group in $(grep -r "userGroupId=" runtime/component/*/data/*.xml | cut -d'"' -f2 | sort -u); do
    if ! grep -r "UserGroup.*userGroupId=\"$group\"" runtime/component/*/data/*.xml >/dev/null; then
        echo "❌ Missing user group: $group"
    else
        echo "✅ Found user group: $group"
    fi
done
```

### Authorization Best Practices Summary

1. **Use existing user groups** before creating new ones
2. **Define authorization at main screen level** (main artifact pattern)
3. **Let services inherit authorization** from screens
4. **ServiceJobs run in system context** - no user authorization needed
5. **Check for existing groups** with: `grep -r "UserGroup.*userGroupId" runtime/component/*/data/*.xml`

## Gradle Migration Notes

### Gradle 8.x/9.x Compatibility (Moqui 4.x)

When upgrading to Gradle 8.x or 9.x (required for Moqui 4.x with Java 21), address these breaking changes:

**1. `archivePath` Property Removed**

The `archivePath` property was removed in Gradle 8.x. Use `archiveFile.get().asFile` instead:

```groovy
// WRONG (Gradle 7.x and earlier)
def jarFile = project(':framework').jar.archivePath

// CORRECT (Gradle 8.x+)
def jarFile = project(':framework').jar.archiveFile.get().asFile
```

**2. Property Assignment Syntax**

Properties should use `=` assignment syntax:

```groovy
// WRONG (deprecated)
maxParallelForks 1
maven { url "https://plugins.gradle.org/m2/" }

// CORRECT
maxParallelForks = 1
maven { url = uri("https://plugins.gradle.org/m2/") }
```

**3. Execution-Time Project References**

Avoid `project()` calls at execution time:

```groovy
// WRONG (deprecated)
doFirst {
    def otherProject = project(':other')  // Execution-time project reference
}

// CORRECT
def otherProject = project(':other')  // Configuration-time reference
doFirst {
    // Use otherProject here
}
```

## Important Notes

- Always run `gradle getRuntime` first if the runtime directory doesn't exist
- The system requires Java 21+ and Gradle 8.x+ for Moqui 4.x
- Components are downloaded from git repositories or zip archives as configured
- Database schema is managed automatically through entity definitions
- Logs are in `runtime/log/` directory
- For project-specific guidance and troubleshooting, see the main component's `.agent-os/` directory
- Code style guidelines are defined in the agent-os code style configuration
- Commit message standards and git workflow: See `commit-guidelines.md`