# Moqui Framework Project Structure Reference

## Framework Guide Reference

For authoritative details, see `runtime/component/moqui-agent-os/framework-guide.md`:
- **"## Moqui Framework Overview"** - Technology stack and capabilities
- **"## Project Structure"** - Multi-repository layout
- **"## Standard Component Structure"** - Component directory anatomy
- **"## Common Development Commands"** - Gradle tasks and development workflow

For infrastructure and deployment, see `runtime/component/moqui-agent-os/development-guide.md`:
- Section `## Multi-Repository Management` - Git operations across repositories
- Section `### Submodule Status Gotcha` - Critical git status behavior

---

## Multi-Repository Structure

Every Moqui project is composed of multiple independent git repositories nested within a single directory tree. Understanding this is essential before making any changes.

### Repository Layers

```
project-root/                        ← Git repo #1: Framework repository
│
├── framework/                       ← Moqui core (tracked in framework repo)
│   ├── src/                         ← Framework Java/Groovy source
│   └── build.gradle                 ← Framework module build
│
├── runtime/                         ← Git repo #2: Runtime repository
│   ├── conf/
│   │   ├── MoquiDevConf.xml         ← Development configuration
│   │   └── MoquiProductionConf.xml  ← Production configuration
│   ├── component/
│   │   ├── {main-component}/        ← Git repo #3: Main custom component
│   │   │   ├── entity/              ← Entity definitions
│   │   │   ├── service/             ← Service definitions
│   │   │   ├── screen/              ← Screen definitions
│   │   │   ├── data/                ← Seed and demo data
│   │   │   ├── template/            ← FTL templates
│   │   │   ├── src/                 ← Groovy/Java source
│   │   │   ├── test/                ← Spock tests
│   │   │   ├── lib/                 ← Component-specific libraries
│   │   │   ├── .agent-os/           ← Project-specific Agent OS config
│   │   │   ├── component.xml        ← Component metadata
│   │   │   └── build.gradle         ← Component build
│   │   │
│   │   ├── moqui-agent-os/          ← Git repo #4: Agent OS (shared config)
│   │   │   ├── .claude/             ← Skills, agents, commands, settings
│   │   │   ├── framework-guide.md
│   │   │   ├── development-guide.md
│   │   │   ├── testing-guide.md
│   │   │   └── ...
│   │   │
│   │   ├── mantle-udm/              ← Git repo #5: Universal Data Model
│   │   ├── mantle-usl/              ← Git repo #6: Universal Service Library
│   │   ├── SimpleScreens/           ← Git repo #7: Standard screens
│   │   └── ...                      ← Additional components (each its own repo)
│   │
│   ├── log/                         ← Application logs (runtime repo)
│   ├── db/                          ← Database files for embedded DB (runtime repo)
│   └── elasticsearch/               ← OpenSearch data directory (runtime repo)
│
├── build.gradle                     ← Root build configuration (framework repo)
├── settings.gradle                  ← Gradle settings (framework repo)
├── CLAUDE.md                        ← Project instructions (framework repo)
├── .claude -> runtime/component/moqui-agent-os/.claude     ← Symlink
└── .agent-os -> runtime/component/moqui-agent-os           ← Symlink
```

### Key Insight: File Ownership

| File Location | Tracked In |
|---------------|-----------|
| `build.gradle`, `CLAUDE.md`, `framework/` | Framework repository (project root) |
| `runtime/conf/`, `runtime/log/`, `runtime/db/` | Runtime repository |
| `runtime/component/{name}/*` | That component's own repository |

### Common Standard Components

| Component | Purpose |
|-----------|---------|
| `mantle-udm` | Universal Data Model — standard entities for orders, products, parties, etc. |
| `mantle-usl` | Universal Service Library — standard services for business logic |
| `SimpleScreens` | Standard admin/management screens |
| `moqui-agent-os` | Agent OS — shared AI development configuration |
| `moqui-elasticsearch` | OpenSearch/Elasticsearch integration component |
| `moqui-hazelcast` | Distributed caching component |
| `moqui-fop` | Apache FOP integration for PDF generation |

## Git Operations

### Checking Status Across Repos

```bash
# Framework root — ONLY shows framework-level changes
git status

# Runtime — shows runtime config/log changes
git -C runtime status

# A specific component
git -C runtime/component/{component-name} status

# All repos at once (via Gradle)
./gradlew gitStatusAll
```

**CRITICAL**: `git status` at the framework root will show "clean" even when components have untracked or modified files. Always check inside the component directory.

### Branch Operations

```bash
# Checkout a branch across ALL repos
./gradlew gitCheckoutAll -Pbranch=feature/my-feature

# Pull all repos
./gradlew gitPullAll

# Create branch in a specific component only
git -C runtime/component/{name} checkout -b feature/my-feature
```

### Committing Changes

```bash
# Framework-level changes (build.gradle, CLAUDE.md, etc.)
git add build.gradle && git commit -m "update build config"

# Runtime changes
git -C runtime add conf/MoquiDevConf.xml
git -C runtime commit -m "update dev config"

# Component changes
git -C runtime/component/{name} add entity/MyEntities.xml
git -C runtime/component/{name} commit -m "add entity definitions"
```

## Gradle Task Reference

### Essential Commands

| Command | Description |
|---------|-------------|
| `./gradlew run` | Start the Moqui application server |
| `./gradlew load` | Load all data files into the database |
| `./gradlew cleanAll` | Clean build artifacts and drop the database |
| `./gradlew test` | Run all Spock tests |
| `./gradlew build` | Build the WAR file for deployment |

### Data Loading

| Command | Description |
|---------|-------------|
| `./gradlew load` | Load all seed + demo data |
| `./gradlew load -Ptypes=seed` | Load only seed data |
| `./gradlew load -Ptypes=seed,seed-initial` | Load seed + initial data |
| `./gradlew load -Ptypes=seed,seed-initial,install` | Load for clean install |

### Git Coordination

| Command | Description |
|---------|-------------|
| `./gradlew gitPullAll` | Pull all repos (framework, runtime, components) |
| `./gradlew gitStatusAll` | Show status of all repos |
| `./gradlew gitCheckoutAll -Pbranch=X` | Checkout branch across all repos |

### Component Management

| Command | Description |
|---------|-------------|
| `./gradlew getComponent -Pcomponent=X` | Download/clone a component |
| `./gradlew downloadOpenSearch` | Download and configure OpenSearch |

## Standard Component Directory Structure

Every Moqui component follows this layout:

```
{component-name}/
├── component.xml           ← Component metadata (name, version, depends-on)
├── build.gradle            ← Component build configuration
├── entity/                 ← Entity definitions (*.xml)
│   └── MyEntities.xml
├── service/                ← Service definitions (*.xml)
│   └── MyServices.xml
├── screen/                 ← Screen definitions (*.xml)
│   └── MyApp.xml
│       └── MyApp/          ← Subscreens
├── data/                   ← Data files loaded by type
│   ├── MySeedData.xml      ← type="seed" — required configuration
│   ├── MyDemoData.xml      ← type="demo" — sample data
│   └── MySecurityData.xml  ← type="seed" — permissions, user groups
├── template/               ← FTL and other templates
├── src/                    ← Groovy/Java source code
│   └── main/groovy/
├── test/                   ← Spock test files
│   └── MyServiceTests.groovy
└── lib/                    ← Component-specific JARs
```

### Key File Types

| File | Location | Purpose |
|------|----------|---------|
| `component.xml` | Component root | Name, version, dependencies |
| `*Entities.xml` | `entity/` | Entity and view-entity definitions |
| `*Services.xml` | `service/` | Service definitions with in/out parameters |
| `*.rest.xml` | `service/` (directly, NOT in subdirectories) | REST API endpoint definitions |
| `*Data.xml` | `data/` | Seed data, enumerations, permissions |
| `*L10n*.xml` | `data/` | Localization/translation data |
| `*.xml` | `screen/` | Screen layout, forms, navigation |

## Agent OS Detection

### Finding Agent OS Configuration

Agent OS configuration lives in `runtime/component/moqui-agent-os/`. The project root has symlinks:

```bash
# These symlinks exist at the project root
.claude -> runtime/component/moqui-agent-os/.claude
.agent-os -> runtime/component/moqui-agent-os
```

### Finding the Main Component

The main component is the primary custom component being developed. To identify it:

1. **Look for `.agent-os/` directories** under `runtime/component/`:
   ```bash
   ls -d runtime/component/*/.agent-os 2>/dev/null
   ```
2. **Exclude known infrastructure components**: `moqui-agent-os` is Agent OS, not the main component
3. **Check `component.xml`** for the component with project-specific entities/services
4. **Look for the development focus**: The component where most service/entity/screen files are being actively modified

### Three-Level Documentation Hierarchy

| Level | Location | Content |
|-------|----------|---------|
| Framework | `runtime/component/moqui-agent-os/` | Project-neutral Moqui guidance |
| Shared Component | `runtime/component/{shared}/.agent-os/` | Shared ERP/WMS functionality |
| Project | `runtime/component/{main}/.agent-os/` | Project-specific business context |

### Configuration Files

| File | Location | Purpose |
|------|----------|---------|
| `framework-guide.md` | `moqui-agent-os/` | Comprehensive Moqui Framework reference |
| `development-guide.md` | `moqui-agent-os/` | Infrastructure, deployment, IDE setup |
| `testing-guide.md` | `moqui-agent-os/` | Unified testing strategy (Spock + Playwright) |
| `skill-integration.md` | `moqui-agent-os/` | Skill/standard integration and conflicts |
| `agent-registry.md` | `moqui-agent-os/` | Complete agent registry and specifications |
| `commit-guidelines.md` | `moqui-agent-os/` | Code commit standards |