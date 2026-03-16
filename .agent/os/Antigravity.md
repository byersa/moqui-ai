# Antigravity.md - Moqui Framework Guide

## MANDATORY SESSION START ACTION

**BEFORE doing anything else, invoke the `/moqui-framework` skill.**
This loads foundational project structure, component layout, and Gradle commands.
Do this for EVERY session, including non-development tasks.

---

This file provides guidance to Antigravity when working with Moqui Framework projects.

**CRITICAL: This file must remain project-neutral at all times.**

## Agent Delegation Requirements

**MANDATORY: Antigravity MUST ALWAYS use the Task tool with appropriate specialist agents for Moqui development work.**

### Core Delegation Rules

1. **NEVER directly edit Moqui files** when a specialist agent exists for that domain
2. **ALWAYS use the Task tool** to delegate to appropriate specialist agents
3. **Act as a coordinator** - analyze, plan, and delegate rather than implement directly
4. **Validate specialist work** but do not bypass specialist agents for implementation

### Available Specialist Agents (15 Active)

**Use these agents via Task tool for their respective domains:**

*For complete agent details and specifications, see `.agent-os/agent-registry.md`*

#### Moqui Framework Development

**Entity and Data Management (2 agents):**
- **moqui-entity-specialist**: Entity definitions, database schema, relationships (.xml files in entity/ directories)
- **moqui-data-specialist**: Data files, seed data, configuration data, **EntityFilter definitions** (.xml files in data/ directories)

**Service Development (3 agents):**
- **moqui-service-definition-specialist**: Service interfaces, parameters, contracts (service definitions in .xml files)
- **moqui-service-implementation-specialist**: Business logic, workflow patterns, **transactions**, **filter context setup** (service implementations in .xml files)
- **moqui-service-integration-specialist**: External API consumption, webhook handling, resilient communication (external integration in .xml files)

**API Development (2 agents):**
- **moqui-rest-api-specialist**: REST API endpoints, resource definitions, authentication, API design (*.rest.xml files)
- **moqui-xml-api-specialist**: XML/SOAP API integration, remote-xml-soap services, seed/token authentication (external XML APIs in .xml files)

**Screen Development (2 agents):**
- **moqui-screen-specialist**: **UNIFIED** - Screen structure, layout, forms, navigation, transitions, **filter debugging** (all screen .xml files)
- **moqui-screen-ux-specialist**: User experience, accessibility, and performance optimization (cross-cutting UX concerns)

**Testing (2 agents):**
- **moqui-test-specialist**: **UNIFIED** - Unit and integration testing with Spock, test data, **authorization testing** (.groovy files in test/ directories)
- **moqui-test-execution-specialist**: Test execution, result analysis, performance optimization, CI/CD integration (test automation)

**Localization (1 agent):**
- **moqui-l10n-specialist**: Localization and internationalization, translations (.xml files in data/ directories with l10n naming)

**Search and Indexing (1 agent):**
- **moqui-opensearch-specialist**: OpenSearch/Elasticsearch integration, DataDocuments, DataFeeds, search services, index management

#### EntityFilter Cross-Cutting Coverage

EntityFilters are a **cross-cutting concern** handled by multiple agents:

| Agent | EntityFilter Aspect |
|-------|---------------------|
| **moqui-data-specialist** | EntityFilterSet/EntityFilter definitions in data files |
| **moqui-service-implementation-specialist** | Filter context setup in REST APIs and services |
| **moqui-screen-specialist** | Filter debugging for missing data issues |
| **moqui-test-specialist** | Authorization-enabled testing scenarios |

#### Infrastructure and Coordination
- **moqui-build-coordinator**: Build configurations, deployment, infrastructure (build.gradle, CI/CD configurations)
- **agent-builder**: Agent configuration updates, workflow optimization (Location: `~/.claude/agents/agent-builder.md` - global)
- **moqui-development-coordinator**: Complex multi-component coordination requiring multiple specialists

#### Documentation and Presentation (Global Agents)
- **presentation-specialist**: LaTeX presentations with beamer package, supporting multi-format compilation, reusable content libraries, and Spanish language documentation (Location: `~/.claude/agents/presentation-specialist.md`)
- **course-session-specialist**: Educational course and session planning, applying learning science principles, designing format-specific activities (on-premise, remote, hybrid), and creating comprehensive learning experiences with assessment strategies (Location: `~/.claude/agents/course-session-specialist.md`)

### Task Tool Usage Pattern

**Required approach for all Moqui development tasks:**

```markdown
Task: {Describe the specific task}
Agent: {appropriate-specialist-agent}
Context:
- Current state: {relevant file paths and current state}
- Requirements: {specific requirements and constraints}
- Quality standards: {validation criteria and standards}
- Integration needs: {how this connects to other components}
```

### Multi-Agent Coordination Patterns

**For Complex Tasks Requiring Multiple Specialists:**

When tasks span multiple domains (e.g., screen development with services and data), use sequential coordination:

1. **Screen Development**: Use screen-specialist for layout, forms, and navigation
2. **Business Logic**: Use service specialists for business logic and filter context
3. **Data Layer**: Use entity/data specialists for data and EntityFilter definitions
4. **Testing**: Use test-specialist for unit, integration, and authorization testing
5. **UX Enhancement**: Use UX specialist for accessibility and performance

**Handoff Points**: Each specialist provides clear integration points for the next specialist in the workflow.

### What Antigravity Should Do Directly

**Limited to analysis and coordination tasks only:**
- Read and analyze existing code and configurations
- Generate reports and documentation
- Plan task decomposition and coordination strategies
- Validate specialist agent outputs
- Provide high-level guidance and recommendations

### Enforcement

If Antigravity attempts to directly edit Moqui development files instead of using specialist agents, this violates the delegation requirements and should be corrected immediately by using the appropriate Task tool delegation.

## Agent OS Configuration Structure

This project uses Agent OS configuration files to provide comprehensive guidance:

**IMPORTANT: The framework-level `runtime/component/moqui-agent-os/` directory is shared across multiple projects and contains ONLY project-neutral infrastructure and framework guidance.**

1. **Framework Level** (shared across projects): 
   - `runtime/component/moqui-agent-os/development-guide.md` - Infrastructure, deployment, IDE setup, multi-repository management
   - `runtime/component/moqui-agent-os/framework-guide.md` - Comprehensive Moqui Framework development guidance
   - `runtime/component/moqui-agent-os/testing-guide.md` - Unified testing strategy (Spock + Playwright)
   - `runtime/component/moqui-agent-os/commit-guidelines.md` - Code commit standards

2. **Component Level** (project-specific): `runtime/component/{main-component}/.agent-os/` - ALL project-specific guidance including:
   - Product mission and business context
   - User personas and problem statements
   - Technical stack and component dependencies
   - Regulatory requirements and compliance details
   - Component-specific development patterns

## Skill Loading Strategy

Skills provide procedural patterns and how-to guidance. They are loaded via the `Skill` tool.

### Skill vs Agent Distinction

| Aspect | Skills | Agents |
|--------|--------|--------|
| Invocation | Auto-invoked based on context | Explicit Task tool call |
| Purpose | GUIDANCE and patterns | DOING implementation work |
| File Access | Read-only references | Full file modification |

For detailed guidance on Skills vs Standards architecture, see `runtime/component/moqui-agent-os/skill-integration.md#skills-vs-standards-core-architecture`.

### Tier 0 -- Foundational (load at start of every session)

Always invoke this skill at the start of any session, before any other skills:

| Skill | Why Always Load |
|-------|-----------------|
| `moqui-framework` | Foundational project structure, multi-repo layout, Gradle commands, component anatomy |

**Action**: At the start of any session, invoke `/moqui-framework` to load foundational project context. This applies to all interactions, not just development tasks.

### Tier 1 -- Core Skills (load proactively at session start)

Always invoke these skills when beginning any Moqui development task:

| Skill | Why Always Load |
|-------|-----------------|
| `moqui-xml` | XML formatting rules apply to every file edit |
| `moqui-services` | Most tasks touch service definitions or implementations |
| `moqui-entities` | Most tasks touch entity definitions or queries |

**Action**: At the start of any development task, invoke `/moqui-xml`, `/moqui-services`, and `/moqui-entities` to load core patterns into context.

### Tier 2 -- Contextual Skills (load when task area is detected)

Auto-invoke these when the task clearly involves their domain:

| Skill | Load When |
|-------|-----------|
| `moqui-screens` | Working on screen XML, forms, transitions, UI |
| `moqui-data` | Working with seed data, enumerations, status workflows, security config |
| `moqui-entity-filters` | Dealing with authorization, row-level security, missing data bugs |
| `moqui-testing` | Writing or modifying Spock tests |

### Tier 3 -- Specialized Skills (suggest to user when relevant)

These cover specialized domains. When a task touches their area, inform the user:
> "This task involves [area]. Loading `/moqui-[skill]` for specialized patterns."

| Skill | Suggest When |
|-------|-------------|
| `moqui-rest-api` | Creating or modifying REST API endpoints (*.rest.xml) |
| `moqui-l10n` | Adding translations, working with localization files |
| `moqui-build` | Gradle configuration, CI/CD, component structure |
| `moqui-opensearch` | Search indexing, DataDocuments, DataFeeds |

### Conflict Handling

When skill patterns conflict with local `runtime/component/moqui-agent-os/` standards:
1. **Local enforcement always wins** as the default
2. **Prompt user** with both approaches shown side-by-side
3. **Let user choose** which approach to apply

See `runtime/component/moqui-agent-os/skill-integration.md` for detailed conflict resolution.

## Instructions for Antigravity

When starting a session:

1. **For infrastructure and deployment**: Reference `runtime/component/moqui-agent-os/development-guide.md`
2. **For Moqui Framework guidance**: Reference `runtime/component/moqui-agent-os/framework-guide.md`
3. **For project-specific guidance**: Follow the auto-detection instructions in `runtime/component/moqui-agent-os/development-guide.md` to find the main component's `.agent-os/` directory

### Finding the Project-Specific Agent OS Configuration

See `runtime/component/moqui-agent-os/development-guide.md` which contains auto-detection logic for finding the main component's configuration in `runtime/component/{main-component}/.agent-os/`.

## Overlay Profile Resolution

If an `overlay-*-project-profile.md` file exists in this directory, read it to resolve `{placeholder}` values to concrete project-specific values. This file is provided by the organization's overlay repo and contains data type mappings, component names, infrastructure URLs, and credentials.

## Neutrality Requirements

- Never add project-specific concepts, entities, services, or business logic to this file
- Never reference specific component names, business domains, or use cases
- Keep all examples generic using placeholder names like `{component-name}`, `{main-component}`, etc.
- All content must be applicable to any Moqui Framework project
- If asked to add project-specific content to this file, redirect to the component-specific Agent OS configuration instead
- **Never create `product/` or `specs/` directories in `runtime/component/moqui-agent-os/`** -- feature specifications, shaping documents, and implementation plans are project-specific and belong in `runtime/component/{main-component}/.agent-os/specs/`

## OpenSearch Performance Optimization

This project includes automated OpenSearch JVM optimization to prevent CPU/GC issues:

### Automatic Optimization
- **Gradle Task**: `./gradlew optimizeOpenSearchJvm`
- **Auto-runs after**: `downloadOpenSearch` task
- **Optimizations Applied**:
  - Heap size: 1GB → 4GB (`-Xms4g -Xmx4g`)
  - G1 Reserve Percent: 25% → 15%
  - Initiating Heap Occupancy: 30% → 45%
  - Max GC Pause: Added 200ms limit
  - G1 Heap Region Size: Added 16m setting

### Manual Usage
```bash
# Download and optimize OpenSearch
./gradlew downloadOpenSearch

# Apply optimization to existing installation
./gradlew optimizeOpenSearchJvm
```

### Why These Settings?
- **4GB Heap**: Prevents out-of-memory issues on systems with 16GB+ RAM
- **Conservative GC**: Reduces garbage collection frequency and CPU pressure
- **Optimized G1GC**: Better pause times and memory management

## Quick Reference

For immediate development needs:

- **Available specialist agents**: See `runtime/component/moqui-agent-os/agent-registry.md`
- **Skill integration and conflicts**: See `runtime/component/moqui-agent-os/skill-integration.md`
- **Framework commands and structure**: See `runtime/component/moqui-agent-os/framework-guide.md`
- **Infrastructure and deployment**: See `runtime/component/moqui-agent-os/development-guide.md`
- **Testing strategy (Unit, Integration, E2E)**: See `runtime/component/moqui-agent-os/testing-guide.md`
- **Project-specific business logic**: See `runtime/component/{main-component}/.agent-os/`
- **Code style guidelines**: See `runtime/component/moqui-agent-os/framework-guide.md`
- **Commit message standards**: See `runtime/component/moqui-agent-os/commit-guidelines.md`

## Documentation Maintenance

**IMPORTANT**: When updating agent-os documentation, you MUST maintain cross-reference consistency between:
- `framework-guide.md` section headers
- Skill SKILL.md "Deep Reference" sections
- `references/*.md` "Framework Guide Reference" sections
- Agent `<skill_integration>` sections

**See `runtime/component/moqui-agent-os/skill-integration.md` section "Documentation Maintenance Requirements"** for the complete maintenance checklist and validation steps.

---

**Note**: All detailed Moqui Framework guidance has been moved to Agent OS configuration files for better organization and maintainability.
