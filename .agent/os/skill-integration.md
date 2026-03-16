# Skill Integration Guide

This document describes how Claude Code skills integrate with the local agent-os configuration.

## Skills vs Standards: Core Architecture

This section explains the fundamental distinction between Skills and Standards in the Agent OS ecosystem.

### Fundamental Distinction

| Type | Nature | Content Focus | Invocation |
|------|--------|---------------|------------|
| **Standards** | Declarative | Conventions, rules, constraints | Explicit (`/inject-standards`) |
| **Skills** | Procedural | How-to guides, step-by-step patterns | Auto-detected by context |

**Standards** answer: "What format/convention must we use?"
**Skills** answer: "How do we implement this pattern?"

### Content Placement Decision Tree

When deciding where to place new content, use this decision tree:

```
New Content
    │
    ├── Describes conventions, rules, constraints?
    │   └── YES → standards/
    │
    ├── Describes step-by-step implementation?
    │   └── YES → .claude/skills/
    │
    ├── Shows "what" without explaining "how"?
    │   └── YES → standards/
    │
    └── Shows "how" with detailed examples?
        └── YES → .claude/skills/
```

### Examples

| Content | Placement | Reason |
|---------|-----------|--------|
| "Service names must use verb#Noun format" | standards/ | Convention |
| "How to create a service with parameters" | skills/ | Procedure |
| "XML attributes must be on separate lines" | standards/ | Rule |
| "Step-by-step DataDocument configuration" | skills/ | Tutorial |
| "EntityFilters must be defined in data files" | standards/ | Constraint |
| "Pattern for implementing search with OpenSearch" | skills/ | How-to |

### Migration Criteria

**Move to Standards when:**
- Content is a naming convention or structural rule
- Content defines mandatory constraints
- Content specifies "MUST", "NEVER", "ALWAYS" requirements
- Content applies across multiple domains

**Keep in Skills when:**
- Content shows complete implementation examples
- Content includes step-by-step instructions
- Content is domain-specific pattern guidance
- Content should auto-invoke based on context

### Relationship to Agents

Note that Skills and Standards differ from Agents:

| Aspect | Standards | Skills | Agents |
|--------|-----------|--------|--------|
| **Purpose** | Define conventions | Provide guidance | Do implementation |
| **Nature** | Declarative rules | Procedural patterns | Active execution |
| **Invocation** | Explicit `/inject-standards` | Auto-invoked | Task tool delegation |
| **File Access** | Read-only reference | Read-only reference | Full modification |

## Available Skills (12 Skills across 4 Tiers)

All skills are local, located in `.claude/skills/`. Each has its own `SKILL.md` that references patterns in the top-level `references/` directory.

**Tier 0 -- Foundational (load every session):**
| Skill | Location | Domain |
|-------|----------|--------|
| `moqui-framework` | `.claude/skills/moqui-framework/` | Multi-repo structure, Gradle commands, component anatomy, Agent OS detection |

**Tier 1 -- Core (load at session start):**
| Skill | Location | Domain |
|-------|----------|--------|
| `moqui-xml` | `.claude/skills/moqui-xml/` | XML formatting rules, default value handling |
| `moqui-services` | `.claude/skills/moqui-services/` | Service definitions, parameters, transactions, jobs |
| `moqui-entities` | `.claude/skills/moqui-entities/` | Entity definitions, relationships, queries, views |

**Tier 2 -- Contextual (load when task area is detected):**
| Skill | Location | Domain |
|-------|----------|--------|
| `moqui-screens` | `.claude/skills/moqui-screens/` | Screen layout, forms, navigation, transitions |
| `moqui-data` | `.claude/skills/moqui-data/` | Seed data, enumerations, status workflows, security config |
| `moqui-entity-filters` | `.claude/skills/moqui-entity-filters/` | Row-level security, filter definitions, debugging |
| `moqui-testing` | `.claude/skills/moqui-testing/` | Spock test patterns, test data, authorization testing |

**Tier 3 -- Specialized (suggest when relevant):**
| Skill | Location | Domain |
|-------|----------|--------|
| `moqui-rest-api` | `.claude/skills/moqui-rest-api/` | REST API endpoints, authentication |
| `moqui-l10n` | `.claude/skills/moqui-l10n/` | Localization, translations |
| `moqui-build` | `.claude/skills/moqui-build/` | Gradle, CI/CD, component structure |
| `moqui-opensearch` | `.claude/skills/moqui-opensearch/` | DataDocuments, DataFeeds, search |

**Invocation**: Auto-invoked when working with Moqui services, entities, screens, or data files

## Layered Integration Model

```
USER REQUEST
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│  SKILLS (Auto-invoked for GUIDANCE)                         │
│  └── .claude/skills/ (12 skills across 4 tiers)             │
│      • moqui-framework (Tier 0 — every session)             │
│      • moqui-xml, moqui-services, moqui-entities (Tier 1)  │
│      • moqui-screens, moqui-data, moqui-entity-filters,    │
│        moqui-testing (Tier 2)                               │
│      • moqui-rest-api, moqui-l10n, moqui-build,            │
│        moqui-opensearch (Tier 3)                            │
└─────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│  AGENTS (Explicit Task tool for IMPLEMENTATION)             │
│  └── runtime/component/moqui-agent-os/.claude/agents/ (14 agents) │
│      • Reference skill patterns                             │
│      • Apply local enforcement rules                        │
│      • Perform file modifications                           │
└─────────────────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│  AGENT-OS REFERENCES (Project-specific ENFORCEMENT)         │
│  └── runtime/component/moqui-agent-os/ - Local standards win      │
│      • XML DSL preference (CRITICAL)                        │
│      • Testing guide (Spock + Playwright)                   │
│      • Project-specific templates                           │
└─────────────────────────────────────────────────────────────┘
```

## Key Principle: LOCAL ALWAYS WINS

When skill patterns conflict with local `.agent-os/` standards, local enforcement takes precedence. Conflicts are surfaced to the user for explicit decision-making.

## Content Ownership Matrix

| Content Area | Primary Owner | Source | Conflict Handling |
|--------------|---------------|--------|-------------------|
| Project structure | Local skill | `references/project_structure.md` | No conflict |
| Service patterns | Local skill | `references/service_patterns.md` | No conflict |
| Entity patterns | Local skill | `references/entity_patterns.md` | No conflict |
| Screen patterns | Local skill | `references/screen_patterns.md` | No conflict |
| Form patterns | Local skill | `references/form_patterns.md` | No conflict |
| REST API patterns | Local skill | `references/rest_api_patterns.md` | No conflict |
| Testing patterns | Local skill | `references/testing_patterns.md` | No conflict |
| L10n patterns | Local skill | `references/l10n_patterns.md` | No conflict |
| XML best practices | Local skill | `references/xml_best_practices.md` | No conflict |
| EntityFilter patterns | Local skill | `references/entity_filter_patterns.md` | No conflict |
| Build patterns | Local skill | `references/build_patterns.md` | No conflict |
| OpenSearch patterns | Local skill | `references/opensearch_patterns.md` | No conflict |

## Conflict Resolution Rules

When patterns from different sources conflict:

1. **PROMPT user** with both approaches shown side-by-side
2. **Show local approach** and alternative clearly
3. **Let user choose** which to apply
4. **Default recommendation** is local standard

## Known Conflicts

### Conflict 1: Script Block Philosophy (Three-Tier Approach)

**Some examples show**: Inline script block examples freely for various patterns

**Local standard**: Three-tier preference:
1. **XML DSL** (preferred) - Use Moqui's declarative XML elements for most service logic
2. **External Groovy file** - When majority of service logic is Groovy, create separate `*.groovy` file referenced via `type="script" location="component://..."`
3. **Inline script blocks** (discouraged) - Only for small calculations/algorithms

**Resolution - Prompt user with three options**:
- "Convert to XML DSL" (default recommendation)
- "Extract to external Groovy file" (when logic is complex/predominantly Groovy)
- "Keep inline script" (only for small snippets)

**Example external Groovy reference**:
```xml
<service verb="process" noun="ComplexLogic" type="script"
         location="component://mycomponent/service/ComplexLogicService.groovy">
    <in-parameters>
        <parameter name="inputData" type="Map" required="true"/>
    </in-parameters>
    <out-parameters>
        <parameter name="result" type="Map"/>
    </out-parameters>
</service>
```

### Conflict 2: Transaction Attributes

**General documentation**: Shows all transaction options with explicit attribute examples

**Local standard**: 98.5% of Moqui framework services use default transaction behavior (omit attribute). Only add explicit transaction attributes for:
- Critical financial operations requiring isolation
- Long-running tasks (>5 minutes)
- Specific rollback/commit requirements

**Resolution - Prompt user each time**:
- "Use default (omit attribute)" (default recommendation)
- "Use explicit transaction='X' as shown" (for special cases only)

## Updating Skills

### Local Skills

All skills are part of this project at `.claude/skills/`, with reference documentation in the top-level `references/` directory:

```bash
# List available skills
ls -la .claude/skills/

# Check a specific skill
cat .claude/skills/moqui-services/SKILL.md

# List reference files
ls -la references/
```

**Important**: Update local skill files directly when patterns need to change.

## Agent Integration (Consolidated)

Specialist agents reference the local skill patterns and implement conflict prompting.

**Total: 14 Agents** (consolidated from 20 for efficiency)

**Service Agents (3)**:
- `moqui-service-definition-specialist` - References `service_patterns.md`
- `moqui-service-implementation-specialist` - References `service_patterns.md`, `entity_filter_patterns.md` (filter context), script block conflict prompting, transactions
- `moqui-service-integration-specialist` - References `service_patterns.md`, `rest_api_patterns.md`

**Entity/Data Agents (2)**:
- `moqui-entity-specialist` - References `entity_patterns.md`
- `moqui-data-specialist` - References `data_patterns.md`, `entity_filter_patterns.md` (EntityFilter definitions)

**Screen Agents (2)**:
- `moqui-screen-specialist` - **UNIFIED**: Layout, forms, flow. References `screen_patterns.md`, `form_patterns.md`, `entity_filter_patterns.md` (filter debugging)
- `moqui-screen-ux-specialist` - References `screen_patterns.md`, `form_patterns.md`

**API Agents (2)**:
- `moqui-rest-api-specialist` - References `rest_api_patterns.md`, `entity_filter_patterns.md`
- `moqui-xml-api-specialist` - References `service_patterns.md`

**Test Agents (2)**:
- `moqui-test-specialist` - **UNIFIED**: Unit + integration testing. References `testing_patterns.md`, `entity_filter_patterns.md` (authorization testing)
- `moqui-test-execution-specialist` - References `testing_patterns.md`, `build_patterns.md`

**Other Agents (3)**:
- `moqui-l10n-specialist` - References `l10n_patterns.md`
- `moqui-build-coordinator` - References `build_patterns.md`

### EntityFilter Cross-Cutting Coverage

EntityFilters are a cross-cutting concern handled by multiple agents:

| Agent | EntityFilter Aspect |
|-------|---------------------|
| `moqui-data-specialist` | EntityFilterSet/EntityFilter definitions in data files |
| `moqui-service-implementation-specialist` | Filter context setup in REST APIs and services |
| `moqui-screen-specialist` | Filter debugging for missing data issues |
| `moqui-test-specialist` | Authorization-enabled testing scenarios |
| `moqui-rest-api-specialist` | REST endpoint filter context |

## Files Preserved (Not Modified by Skills)

These local enforcement files remain authoritative:

| File | Location | Reason |
|------|----------|--------|
| `framework-guide.md` | `agent-os/` | Comprehensive framework reference |
| `testing-guide.md` | `agent-os/` | Exclusive local content (Spock + Playwright) |
| `development-guide.md` | `agent-os/` | Infrastructure and deployment |
| `commit-guidelines.md` | `agent-os/` | Code commit standards |

## Framework Guide Integration

The `framework-guide.md` serves as the **authoritative deep reference** for complex topics. Skills and agents reference it via section headers.

### Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│  Skills (Quick Patterns)                                            │
│  ├── SKILL.md - Entry point with quick reference                    │
│  └── ../../references/*.md - Domain-specific patterns               │
│         ↓ references (via section headers)                          │
├─────────────────────────────────────────────────────────────────────┤
│  framework-guide.md (Deep Reference - 7000+ lines)                  │
│  ├── Comprehensive patterns with detailed explanations              │
│  ├── Edge cases and debugging workflows                             │
│  ├── Framework internals and code references                        │
│  └── Context persistence, AJAX behavior, EntityFilters              │
└─────────────────────────────────────────────────────────────────────┘
```

### How Skills Reference Framework Guide

Skills reference `framework-guide.md` sections by header for advanced topics:

```markdown
**Framework Guide Reference**: For detailed patterns, see `runtime/component/moqui-agent-os/framework-guide.md`:
- **"#### Section Header"** - Brief description of content
```

### Content Placement Decision

| Content Type | Location | Example |
|--------------|----------|---------|
| Quick templates and common patterns | Skill `references/*.md` | Screen template, form-single pattern |
| Detailed explanations with code refs | `framework-guide.md` | AJAX transition context, always-actions execution |
| Declarative rules and constraints | `standards/*.md` | XML formatting rules, naming conventions |
| Project-specific business logic | Component `.agent-os/` | Domain entities, workflows |

## Documentation Maintenance Requirements

**CRITICAL: When updating agent-os documentation, you MUST maintain cross-reference consistency.**

### Reference Chain

The documentation system has interconnected references that must stay synchronized:

```
┌─────────────────────────────────────────────────────────────────────────────┐
│  CLAUDE.md (Entry Point)                                                    │
│  └── Points to: agent-os/framework-guide.md, skill-integration.md           │
├─────────────────────────────────────────────────────────────────────────────┤
│  Skills (.claude/skills/*/SKILL.md)                                         │
│  └── "Deep Reference" section → framework-guide.md section headers          │
├─────────────────────────────────────────────────────────────────────────────┤
│  References (references/*.md)                                                │
│  └── "Framework Guide Reference" → framework-guide.md section headers       │
├─────────────────────────────────────────────────────────────────────────────┤
│  Agents (.claude/agents/*.md)                                               │
│  └── <skill_integration> section → reference files                          │
├─────────────────────────────────────────────────────────────────────────────┤
│  framework-guide.md (Authoritative Deep Reference)                          │
│  └── Section headers are referenced by skills and agents                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Maintenance Checklist

When making changes to agent-os documentation, follow this checklist:

#### When Modifying framework-guide.md

1. **If renaming a section header**:
   - Search all `.claude/skills/*/SKILL.md` files for references to the old header
   - Search all `references/*.md` files for references
   - Update all matching references to use the new header name

2. **If adding a new section**:
   - Determine which skill(s) should reference it
   - Add the section reference to the relevant skill's "Deep Reference" section
   - Add to the relevant `references/*.md` "Framework Guide Reference" section if applicable

3. **If removing a section**:
   - Search for all references to that section header
   - Remove or update all references before deleting

#### When Modifying Skills

1. **If adding a new skill**:
   - Add entry to `skill-integration.md` under "Available Skills" or equivalent section
   - Ensure SKILL.md has proper "Deep Reference" section pointing to framework-guide.md
   - Update agent-registry.md if skill relates to an agent

2. **If modifying skill references**:
   - Verify framework-guide.md sections still exist with those exact headers
   - Test that section search terms will find the correct content

#### When Modifying Agents

1. **If adding/modifying agent skill references**:
   - Verify skill files exist at referenced paths
   - Verify framework-guide.md sections referenced by skills exist

### Search Commands for Maintenance

Use these commands to find cross-references:

```bash
# Find all references to a framework-guide.md section
grep -r "Section Header Name" .claude/skills/ references/

# Find all framework-guide.md references in skills and references
grep -r "framework-guide.md" .claude/skills/ references/

# Find all reference file paths in agents
grep -r "references/" .claude/agents/

# List all section headers in framework-guide.md
grep -n "^## \|^### \|^#### " framework-guide.md
```

### Validation Before Committing

Before committing changes to agent-os documentation:

- [ ] All framework-guide.md section references in skills are valid
- [ ] All skill file paths in agents are valid
- [ ] All "Deep Reference" sections use exact section header names
- [ ] New content is placed in the correct location per Content Placement Decision
- [ ] Cross-cutting concerns are documented in all relevant specialists

### Automated Validation

A validation script is available to check cross-reference consistency:

```bash
# Run validation
./scripts/validate-cross-references.sh

# Run with verbose output (shows valid references too)
./scripts/validate-cross-references.sh --verbose

# Run with fix suggestions
./scripts/validate-cross-references.sh --fix
```

**The script validates:**
1. Section headers in framework-guide.md exist for all skill references
2. File paths referenced in agents exist
3. Skill structure compliance (SKILL.md, references directory)
4. Documentation-discovery.md ignore list structure

**Run validation before committing documentation changes.**

### Documentation Discovery Integration

New patterns discovered during development should be captured systematically:

- **Guideline**: See `guidelines/documentation-discovery.md` for the complete workflow
- **Command**: Use `/document-discovery` for ad-hoc knowledge capture
- **Protocol**: Step 6 of `task-execution-protocol.md` integrates documentation discovery into every task

**Workflow**:
1. At task completion, identify patterns used that aren't documented
2. Prompt user with documentation options (document/skip/ignore)
3. If approved, add to appropriate agent-os file
4. Run `./scripts/validate-cross-references.sh` to ensure consistency
