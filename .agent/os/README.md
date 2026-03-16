# Moqui Agent OS

AI development knowledge base for [Moqui Framework](https://www.moqui.org/) projects, with optional [Claude Code](https://claude.com/claude-code) integration and spec-driven methodology.

Provides comprehensive Moqui Framework patterns for entities, services, screens, data, testing, and deployment — usable by any AI tool (Cursor, Windsurf, Copilot, local LLMs) or human developers.

## Three-Layer Architecture

The knowledge is organized into three independent layers. Use only what you need:

### Layer 1: Tool-Agnostic Moqui Knowledge (any AI tool or human)

Pure framework documentation readable by any tool or person:

- **`framework-guide.md`** — Comprehensive Moqui Framework reference (~9,600 lines)
- **`testing-guide.md`** — Unified testing strategy (Spock + Playwright)
- **`standards/`** — Declarative conventions (MUST/NEVER rules) for entities, services, screens, data
- **`references/`** — Detailed pattern guides for each domain (entities, services, screens, etc.)
- **`templates/`** — Ready-to-use XML code templates with placeholder markers

### Layer 2: Claude Code Integration (optional, for Claude Code users)

Claude Code-specific automation built on top of Layer 1:

- **Skills** (`.claude/skills/`) — Procedural patterns invoked via the `Skill` tool
- **Agents** (`.claude/agents/`) — Specialist agents for domain-specific implementation work
- **Commands** (`.claude/commands/`) — User-invocable slash commands for workflows

### Layer 3: Development Methodology (optional, for spec-driven teams)

Process guidance and workflows:

- **Guidelines** (`guidelines/`) — Process guidance for testing, naming, documentation
- **Instructions** (`instructions/`) — Detailed workflow instructions
- **Core Guides** — `development-guide.md`, `commit-guidelines.md`, `agent-registry.md`

### Using Without Claude Code

Point your AI tool to read these files for Moqui Framework knowledge:
1. Start with `framework-guide.md` for comprehensive framework reference
2. Browse `references/` for domain-specific patterns (e.g., `entity_patterns.md`, `service_patterns.md`)
3. Check `standards/` for declarative conventions
4. Use `templates/` for code scaffolding

## Skill Tier Strategy

Skills are organized into four tiers that determine when Claude Code should invoke them. Skills are not auto-loaded from context — they require explicit invocation via the `Skill` tool. The tier system provides a loading discipline so foundational knowledge is always available first, domain patterns load when needed, and specialized knowledge doesn't bloat every interaction.

| Tier | Skills | When Invoked | Purpose |
|------|--------|--------------|---------|
| **Tier 0 — Foundational** | `moqui-framework` | Every session (unconditional) | Project structure, multi-repo layout, Gradle commands |
| **Tier 1 — Core** | `moqui-xml`, `moqui-services`, `moqui-entities` | At start of development tasks | Domain patterns used in most tasks |
| **Tier 2 — Contextual** | `moqui-screens`, `moqui-data`, `moqui-entity-filters`, `moqui-testing` | When task area is detected | Domain-specific patterns invoked on demand |
| **Tier 3 — Specialized** | `moqui-rest-api`, `moqui-l10n`, `moqui-build`, `moqui-opensearch` | Suggested when relevant | Niche patterns for specific domains |

**Invocation flow**: Tier 0 first (project context), then Tier 1 (core patterns), then Tier 2/3 as needed by the task.

### Tier 0 Enforcement

Since skills require explicit invocation, Tier 0 reliability depends on Claude Code following instructions. Two mechanisms reinforce this:

1. **CLAUDE.md placement**: The mandatory Tier 0 action appears at the very top of `CLAUDE.md` — the first instruction Claude Code reads when the file loads.
2. **`SessionStart` hook**: A hook in `~/.claude/settings.json` prints a visible reminder at session start: `[SESSION START] MANDATORY: Invoke the /moqui-framework skill (Tier 0) NOW before responding to the user.`

Together these make it hard to miss. The hook produces output in the transcript before the first user message; the CLAUDE.md placement ensures it's the first instruction in context.

New knowledge discovered during development can be routed to the appropriate skill tier using the `/document-discovery` command. See `skill-integration.md` for full details on skill tiers, content ownership, and conflict resolution.

## Setup

### How Claude Code Discovers Agent OS

Claude Code looks for a `.claude/` directory at the project root. This repo provides that directory with all the agents, skills, commands, and settings. Two symlinks connect your Moqui project to agent-os:

```
your-moqui-project/
├── .claude -> runtime/component/moqui-agent-os/.claude    # Claude Code reads this
├── .agent-os -> runtime/component/moqui-agent-os          # Shorthand for references
├── CLAUDE.md                                               # Project-level instructions
└── runtime/component/
    └── moqui-agent-os/                                     # This repo
```

- **`.claude` symlink**: Makes Claude Code load agents, skills, commands, and settings from agent-os
- **`.agent-os` symlink**: Lets documentation reference `.agent-os/framework-guide.md` etc. without hardcoding the full path
- **`CLAUDE.md`**: Your project's own instructions file (checked into your project repo, not agent-os). It references agent-os guides and configures agent delegation

### Prerequisites

1. **Claude Code** installed and working
2. **Anthropic Skills plugin**:
   ```bash
   /plugin marketplace add anthropics/skills
   ```
### Basic Setup

```bash
# Clone into your Moqui project
cd your-moqui-project
git clone https://github.com/schue/moqui-agent-os.git runtime/component/moqui-agent-os

# Create symlinks from project root
ln -sf runtime/component/moqui-agent-os/.claude .claude
ln -sf runtime/component/moqui-agent-os .agent-os
```

Then create a `CLAUDE.md` at your project root that references agent-os guides. See `Claude.md` in this repo for a template.

### With Organization Overlay

The overlay mechanism lets organizations add private content (credentials, infrastructure, project-specific commands) without modifying the public repo.

```bash
# 1. Clone public repo as above
git clone https://github.com/schue/moqui-agent-os.git runtime/component/moqui-agent-os

# 2. Clone your overlay repo
git clone <overlay-repo-url> runtime/component/<overlay-name>

# 3. Run overlay script to create symlinks
cd runtime/component/<overlay-name>
./overlay.sh

# 4. Create project-root symlinks
cd ../../..
ln -sf runtime/component/moqui-agent-os/.claude .claude
ln -sf runtime/component/moqui-agent-os .agent-os
```

The overlay adds organization-specific agents, commands, settings, and project profiles by symlinking files into the public repo's directory structure. These symlinks are gitignored by the public repo.

### Global Agents (Optional)

Some utility agents (presentation-specialist, course-session-specialist, etc.) are global — they live in `~/.claude/agents/` and are available across all projects. These are not part of this repo but are referenced in `agent-registry.md`.

## Overlay Mechanism

### How It Works

1. The overlay repo contains an `overlay/` directory that mirrors the moqui-agent-os structure
2. `overlay.sh` creates symlinks from moqui-agent-os directories to overlay files
3. Files are prefixed with `overlay-` and gitignored by the public repo
4. Symlinks survive `git pull` in the public repo

The `overlay.sh` script operates in two phases: first it cleans all existing `overlay-*` symlinks from the moqui-agent-os tree, then creates fresh symlinks for every file under the overlay repo's `overlay/` directory. See `development-guide.md` section "How `overlay.sh` Works" for the full reference implementation.

### Overlay File Naming

All overlay files must be prefixed with `overlay-` (except `settings.local.json`). Supported target locations:

| Overlay Source | Purpose |
|---------------|---------|
| `.claude/agents/overlay-*.md` | Organization-specific specialist agents |
| `.claude/commands/overlay-*.md` | Organization-specific slash commands |
| `.claude/skills/overlay-*` | Organization-specific skills |
| `.claude/settings.local.json` | IDE/tool settings with absolute paths |
| `instructions/core/overlay-*.md` | Organization-specific workflow instructions |
| `guidelines/overlay-*.md` | Organization-specific process guidance |
| `standards/{category}/overlay-*.md` | Organization-specific conventions |
| `scripts/overlay-*.sh` | Organization-specific automation scripts |
| `overlay-*.md` (root level) | Infrastructure, project profiles, dev setup |

### Creating a New Overlay

Minimum viable overlay — only two files required:

```
{org}-agent-os/
├── overlay/
│   └── overlay-{org}-project-profile.md   # Map placeholders to your values
└── overlay.sh                              # Copy from development-guide.md
```

Add more files incrementally as needed. See `development-guide.md` section "Creating an Overlay Repo From Scratch" for the complete guide.

### Managing Symlinks

Re-run `overlay.sh` after:
- Adding or removing files in the overlay repo
- Pulling changes in the overlay repo

Not needed after `git pull` in the public repo (symlinks are stable).

### Project Profile

If your overlay provides an `overlay-*-project-profile.md`, it contains concrete values for placeholders used throughout the documentation:

| Placeholder | Description |
|------------|-------------|
| `{l10n}-install` | Custom localization/install data type |
| `{project}-demo` | Custom demo data type |
| `{project}-test` | Shared test data type |
| `{component}-test` | Component-specific test data type |
| `{localization-component}` | Centralized l10n component name |
| `{shared-component}` | Shared utilities component |
| `{container-registry}` | Docker/container registry URL |
| `{admin-user}` / `{admin-password}` | Dev/test credentials |

Claude reads the overlay profile automatically to resolve these placeholders to concrete project-specific values.

## Development Workflow

### Starting a Feature: `/shape-spec`

1. Enter plan mode
2. Run `/agent-os:shape-spec` to define the feature
3. Shape-spec gathers context, surfaces standards, creates spec folder
4. Spec saved to `{main-component}/.agent-os/specs/`

### During Development: Skills

- Claude Code invokes skills (via `Skill` tool) to provide Moqui patterns (entities, services, screens, etc.)
- Use `/agent-os:inject-standards` for explicit standard injection
- Discuss design decisions referencing standards for consistency

### After Development: `/document-discovery`

- Run `/document-discovery` to capture undocumented patterns
- Patterns get added to `framework-guide.md`, `standards/`, or `skills/`
- Keeps the knowledge base growing

### Code Review: `/code-review`

- Multi-repo code review across framework and component repositories
- Domain-specific review (entities, services, screens, data, build)
- Cross-domain integration validation

### Test Generation: `/create-test`

- Interactive test target selection
- Coordinates specialist agents for comprehensive Spock test generation
- Covers unit, integration, and E2E testing

## Directory Structure

```
moqui-agent-os/
│
│   # LAYER 1: Tool-agnostic Moqui knowledge (any AI tool or human)
├── framework-guide.md           # Comprehensive Moqui reference (~9,600 lines)
├── testing-guide.md             # Unified testing strategy (Spock + Playwright)
├── standards/                   # Declarative conventions
│   ├── backend/                 #   Entity, service, data standards
│   ├── frontend/                #   Screen, form, widget standards
│   ├── global/                  #   Build, l10n, XML standards
│   └── testing/                 #   Test standards
├── references/                  # Detailed pattern guides (18 files)
│   ├── entity_patterns.md       #   Entity definitions, relationships, views
│   ├── service_patterns.md      #   Service definitions, parameters, transactions
│   ├── screen_patterns.md       #   Screen layout, navigation, transitions
│   ├── form_patterns.md         #   Form fields, validation, dynamic forms
│   └── ...                      #   (data, testing, l10n, opensearch, etc.)
├── templates/                   # Ready-to-use XML code templates
│   ├── moqui-entity-templates.md
│   ├── moqui-service-templates.md
│   ├── moqui-data-templates.md
│   ├── moqui-test-templates.md
│   └── screen-templates.md
│
│   # LAYER 2: Claude Code integration (optional)
├── .claude/
│   ├── agents/                  #   14 specialist agent configurations
│   ├── skills/                  #   12 skills (SKILL.md wrappers → references/)
│   ├── commands/                #   User-invocable slash commands
│   └── settings.local.json      #   (gitignored) Project-specific settings
│
│   # LAYER 3: Development methodology (optional)
├── guidelines/                  # Process guidance
├── instructions/core/           # Detailed workflow instructions
├── development-guide.md         # Infrastructure and deployment
├── agent-registry.md            # Complete agent registry
├── skill-integration.md         # Skill/standard conflict resolution
├── commit-guidelines.md         # Commit message standards
│
│   # Infrastructure
├── playwright/                  # E2E testing with Playwright
├── scripts/                     # Automation scripts
├── Claude.md                    # Claude Code integration config
└── config.yml                   # Agent OS configuration
```

## Organization-Specific Content

Content that varies by organization belongs in the **overlay repo** or the **component-level `.agent-os/`**:

| Content Type | Location |
|-------------|----------|
| Infrastructure (registry, CI/CD, K8s) | Overlay repo |
| Project credentials (dev/test) | Overlay `settings.local.json` |
| Business domain context | `{main-component}/.agent-os/` |
| Feature specifications | `{main-component}/.agent-os/specs/` |
| Data type mappings | Overlay project profile |
| Branch naming, workflows | Overlay development addendum |

## Available Commands

| Command | Description |
|---------|-------------|
| `/code-review` | Multi-repo code review with domain-specific analysis |
| `/create-test` | Interactive test generation with Spock framework |
| `/document-discovery` | Capture undocumented patterns into documentation |
| `/check-ide` | Verify IDE configuration for Moqui development |
| `/moqui-prepare-migrate` | Analyze and prepare database migrations |
| `/agent-os:shape-spec` | Feature specification workflow |
| `/agent-os:inject-standards` | Inject specific standards into context |

## Quick Reference

| Resource | File |
|----------|------|
| Available agents | `agent-registry.md` |
| Framework patterns | `framework-guide.md` |
| Testing strategy | `testing-guide.md` |
| Development guide | `development-guide.md` |
| Skill integration | `skill-integration.md` |
| Commit standards | `commit-guidelines.md` |
| Standards index | `standards/index.yml` |
| Guidelines index | `guidelines/index.md` |

---

**Neutrality Requirement**: All content in this repository must remain project-neutral and applicable to any Moqui Framework project. Organization-specific content belongs in the overlay repo or component-level `.agent-os/` directories.
