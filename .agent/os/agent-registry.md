# Moqui Framework Agent Registry

This document provides a registry of Moqui Framework-specific specialist agents and references to global agents.

## Framework-Specific Specialist Agents

These agents are specific to Moqui Framework development and are available when working in Moqui projects.

### Moqui Framework Specialists

#### moqui-entity-specialist
- **Description**: Entity definitions, database schema, relationships
- **Domain**: Entity definitions (.xml files in entity/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: blue
- **Specialization**: Database design, entity relationships, field definitions, indexes
- **Size**: 143 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 5 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (entity patterns and query examples)
- **Location**: `.claude/agents/moqui-entity-specialist.md` (framework-specific)
- **Boundaries**: Handles entity structure and relationships, but NOT access control patterns
- **Guidelines**:
  - **Entity Access Control**: Do NOT include tenant/access constraints in entity definitions - use EntityFilters instead following the Entity Access Control Patterns in framework-guide.md
  - **Multi-Tenancy**: Leverage framework built-in multi-tenancy rather than explicit tenant fields except when required for business logic
  - **Security Separation**: Keep entity definitions focused on data structure, not access control patterns

#### moqui-service-definition-specialist
- **Description**: Service interface definitions, parameters, and contracts
- **Domain**: Service definitions (service interface in .xml files in service/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: green
- **Specialization**: Service interfaces, parameter definitions, input/output contracts, service documentation
- **Size**: 136 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 4 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (service patterns and definitions)
- **Location**: `.claude/agents/moqui-service-definition-specialist.md` (framework-specific)
- **Boundaries**: Handles service interfaces and contracts, but NOT implementation logic or transaction patterns
- **Guidelines**:
  - **Interface Design**: Clear parameter definitions, proper input/output contracts
  - **Documentation**: Comprehensive service documentation and usage patterns
  - **Validation**: Parameter validation and constraint definition

#### moqui-service-implementation-specialist
- **Description**: Service business logic implementation and workflow patterns
- **Domain**: Service implementations (actions and business logic in .xml files in service/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: green
- **Specialization**: Business logic implementation, workflow patterns, data manipulation, conditional logic
- **Size**: 136 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 6 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (service implementation and patterns)
- **Location**: `.claude/agents/moqui-service-implementation-specialist.md` (framework-specific)
- **Boundaries**: Handles service logic implementation, but NOT service interfaces or integration patterns
- **Guidelines**:
  - **Code Formatting**: Follow the Service Call Formatting guidelines in framework-guide.md - use single-line formatting for service calls under 120 characters, prioritize readability, and maintain consistency with existing code style
  - **Map Initialization**: Follow the Map and List Initialization guidelines in framework-guide.md - always initialize maps and lists before using methods like `.add()`, use defensive programming patterns, and ensure tenant message collections are properly initialized for StatusInterface services
  - **Entity Access Control**: Follow the Entity Access Control Patterns in framework-guide.md - do NOT include tenant/access constraints in entity-find operations, use EntityFilters instead for access control and tenant separation

#### moqui-service-integration-specialist
- **Description**: External API consumption, webhooks, and resilient communication patterns
- **Domain**: External system integration (HTTP clients, webhooks in .xml files in service/ directories)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob
- **Color**: orange
- **Specialization**: External API consumption, webhook handling, resilient communication, data transformation
- **Size**: 151 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 0 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (service integration patterns)
- **Location**: `.claude/agents/moqui-service-integration-specialist.md` (framework-specific)
- **Boundaries**: Handles outgoing HTTP requests and external system integration, but NOT REST API endpoint definition or incoming requests
- **Guidelines**:
  - **External Integration**: HTTP client implementation, webhook processing, data transformation
  - **Resilience Patterns**: Circuit breakers, retries, timeouts for external calls
  - **Error Handling**: Comprehensive error handling for external system failures

#### moqui-rest-api-specialist
- **Description**: REST API endpoint definition, resource structure, authentication, and API design
- **Domain**: REST API definitions (*.rest.xml files in service/ directories)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob
- **Color**: cyan
- **Specialization**: REST endpoint definition, resource hierarchy, API authentication, request/response mapping
- **Size**: 203 lines, 10K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 1 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (REST API patterns)
- **Location**: `.claude/agents/moqui-rest-api-specialist.md` (framework-specific)
- **Boundaries**: Handles REST API endpoints that expose internal services, but NOT external API consumption
- **Guidelines**:
  - **API Design**: RESTful resource structure, proper HTTP methods, versioning strategy
  - **Authentication**: UserLoginKey management, API key rotation, role-based access
  - **Documentation**: OpenAPI/Swagger generation, client integration examples

#### moqui-xml-api-specialist
- **Description**: XML-based API integration services, SOAP/XML-RPC endpoints, and token-based authentication
- **Domain**: XML API integrations (remote-xml-soap services in .xml files in service/ directories)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob
- **Color**: lime
- **Specialization**: SOAP web services, XML-RPC calls, seed/token authentication, XML payload construction
- **Size**: 345 lines, 16K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 4 (Target: <8) ✅
- **Memory Profile**: Low-Medium ✅
- **References**: `references/` (XML API and service patterns)
- **Location**: `.claude/agents/moqui-xml-api-specialist.md` (framework-specific)
- **Boundaries**: Handles XML/SOAP external API consumption and certificate-based authentication, but NOT REST/JSON APIs
- **Guidelines**:
  - **SOAP Integration**: XML namespace handling, WSDL-based service mapping, complex parameter structures
  - **Authentication**: Seed/token workflows, digital certificate signing, credential management
  - **XML Processing**: MarkupBuilder construction, response parsing, namespace declarations

#### moqui-opensearch-specialist
- **Description**: Moqui Framework OpenSearch/Elasticsearch integration with DataDocuments, DataFeeds, and ElasticFacade
- **Domain**: OpenSearch integration (search services, DataDocument definitions, index management)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob, Skill
- **Color**: purple
- **Specialization**: ElasticFacade operations, DataDocument/DataFeed configuration, search query DSL, index management
- **Size**: 624 lines, 24K (Target: <500 lines, <15KB) - Needs optimization
- **Code Blocks**: 10+ (Target: <8) - Needs optimization
- **Memory Profile**: Medium-High
- **Location**: `.claude/agents/moqui-opensearch-specialist.md` (framework-specific)
- **Boundaries**: Handles OpenSearch/Elasticsearch integration, but NOT general service implementation
- **Guidelines**:
  - **ElasticFacade Usage**: Use ElasticClient interface for all OpenSearch operations
  - **DataDocument Design**: Map entity data to search documents with proper field types
  - **DataFeed Configuration**: Real-time push (DTFDTP_RT_PUSH) or manual pull (DTFDTP_MAN_PULL)
  - **Query DSL**: Build queries using Maps (not JSON strings), proper pagination
  - **Service Patterns**: Set output via context (never return Maps from script services)

#### moqui-screen-specialist (UNIFIED)
- **Description**: Screen structure, layout, forms, navigation, transitions, filter debugging
- **Domain**: All screen XML files (.xml files in screen/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob, Skill, Playwright
- **Color**: cyan
- **Specialization**: Unified screen development - layout, forms, navigation, transitions
- **References**: `references/` (screen patterns, form patterns, rendering modes)
- **Location**: `.claude/agents/moqui-screen-specialist.md` (framework-specific)
- **Boundaries**: Handles all screen aspects (layout, forms, flow, filter debugging), but NOT cross-cutting UX concerns
- **Guidelines**:
  - **Entity Access Control**: Use EntityFilters instead of explicit constraints in entity-find
  - **Screen Navigation**: Use "../" prefix for sibling screens, relative paths for same-component navigation

#### moqui-screen-ux-specialist
- **Description**: User experience, accessibility, and performance optimization
- **Domain**: Cross-cutting UX concerns (accessibility, responsive design, performance)
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: purple
- **Specialization**: WCAG compliance, responsive design, performance optimization, user feedback
- **Size**: 259 lines, 12K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 4 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (screen patterns and form patterns)
- **Location**: `.claude/agents/moqui-screen-ux-specialist.md` (framework-specific)
- **Boundaries**: Handles cross-cutting UX concerns, but NOT core screen functionality or business logic
- **Guidelines**:
  - **CRITICAL Syntax Compliance**: Follow `runtime/component/moqui-agent-os/moqui-screen-references/syntax-anti-patterns.md` - ensure performance-optimized syntax patterns
  - **Accessibility Standards**: WCAG 2.1 AA compliance with ARIA labeling and keyboard navigation
  - **Performance Optimization**: Progressive loading, caching strategies, and user feedback patterns
  - **Responsive Design**: Mobile-first principles with touch-friendly interfaces

#### moqui-data-specialist
- **Description**: Data files, seed data, configuration data, demo data templates with date refresh
- **Domain**: Data files (.xml files in data/ directories), demo templates with `@rel:`/`@epoch:` expressions
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: orange
- **Specialization**: Data management, seed data creation, configuration
- **Size**: 154 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 6 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (data patterns and configuration)
- **Location**: `.claude/agents/moqui-data-specialist.md` (framework-specific)
- **Boundaries**: Handles data file management and seed data, but NOT entity structure or service logic
- **Guidelines**:
  - **Data Integrity**: Comprehensive validation and referential integrity
  - **Seed Data Patterns**: Standardized data loading and initialization
  - **Configuration Management**: Environment-specific configuration patterns

#### moqui-l10n-specialist
- **Description**: Localization and internationalization management for Moqui Framework
- **Domain**: Localization files (.xml files in data/ directories with l10n naming)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob
- **Color**: teal
- **Specialization**: LocalizedMessage, LocalizedEntityField, Spanish Chilean translations, duplicate detection
- **Size**: 122 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 7 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/` (localization patterns)
- **Location**: `.claude/agents/moqui-l10n-specialist.md` (framework-specific)
- **Boundaries**: Handles localization and translation, but NOT core data structure or service logic
- **Guidelines**:
  - **Duplicate Detection**: Always search existing l10n files before creating new translations using grep patterns
  - **File Organization**: Follow component boundaries - use component-specific l10n files for internal translations and {localization-component} cross-component files for shared terms
  - **Chilean Spanish**: Apply formal business register with Chilean terminology and cultural appropriateness
  - **Integration**: Coordinate with screen, service, entity, and data specialists for comprehensive localization coverage

#### moqui-test-specialist (UNIFIED)
- **Description**: Unit and integration testing with Spock framework, test data, authorization testing
- **Domain**: Test files (.groovy files in test/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob, Skill, Playwright
- **Color**: red
- **Specialization**: Unified testing - unit tests, integration tests, authorization testing with Spock
- **References**: `references/` (testing patterns)
- **Location**: `.claude/agents/moqui-test-specialist.md` (framework-specific)
- **Boundaries**: Handles all test writing (unit, integration, authorization), but NOT test execution infrastructure
- **Guidelines**:
  - **Spock Framework**: BDD testing with sequential numbering, data-driven tests
  - **Authorization Testing**: EntityFilter behavior with different user contexts
  - **Transaction Isolation**: Proper cleanup to prevent cascade failures

#### moqui-test-execution-specialist
- **Description**: Test execution, result analysis, performance optimization, CI/CD integration
- **Domain**: Test execution configuration and analysis
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: purple
- **Specialization**: Gradle test execution, performance testing, CI/CD integration, test automation
- **Size**: 136 lines, 8.0K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 7 (Target: <8) ✅
- **Memory Profile**: Low ✅
- **References**: `references/`, `references/`
- **Location**: `.claude/agents/moqui-test-execution-specialist.md` (framework-specific)
- **Boundaries**: Handles test execution and automation, but NOT test creation or individual test logic
- **Guidelines**:
  - **Gradle Integration**: Test task configuration and build pipeline integration
  - **Performance Testing**: Load testing, stress testing, and performance analysis
  - **CI/CD Integration**: Automated testing in continuous integration environments

### Build and Infrastructure Specialists

#### moqui-build-coordinator
- **Description**: Build configurations, deployment, infrastructure
- **Domain**: Build configurations (build.gradle, CI/CD configurations)
- **Tools**: Read, Write, Edit, Bash, Grep, Glob
- **Color**: yellow
- **Specialization**: Gradle build management, deployment automation, infrastructure
- **Size**: 354 lines, 16K (Target: <500 lines, <15KB) ✅ OPTIMIZED
- **Code Blocks**: 5 (Target: <8) ✅
- **Memory Profile**: Low-Medium
- **References**: `references/` (build patterns)
- **Location**: `.claude/agents/moqui-build-coordinator.md` (framework-specific)
- **Boundaries**: Handles build and deployment, but NOT application development or testing logic
- **Guidelines**:
  - **Gradle Configuration**: Build script optimization and dependency management
  - **Deployment Automation**: CI/CD pipeline configuration and infrastructure as code
  - **Environment Management**: Multi-environment deployment and configuration

#### agent-builder
- **Description**: Agent configuration updates, workflow optimization
- **Domain**: Agent ecosystem management (.md files in runtime/component/moqui-agent-os/ directories)
- **Tools**: Read, Write, Edit, Grep, Glob
- **Color**: magenta
- **Specialization**: Agent creation, workflow design, agent coordination patterns
- **Location**: `~/.claude/agents/agent-builder.md` (global)

### Documentation and Presentation Specialists

#### presentation-specialist
- **Description**: LaTeX presentations with beamer package, supporting multi-format compilation, reusable content libraries, and Spanish language documentation
- **Domain**: LaTeX presentation files (.tex files, beamer presentations)
- **Tools**: Read, Write, Edit, MultiEdit, Grep, Glob, Bash
- **Color**: purple
- **Specialization**: Beamer presentations, template management, multi-format output, TikZ graphics
- **Location**: `~/.claude/agents/presentation-specialist.md` (global)

#### course-session-specialist (Global)
- **Description**: Educational course and session planning with learning science principles
- **Location**: `~/.claude/agents/course-session-specialist.md` (global)
- **Note**: This agent is globally available for educational planning across all projects

### Project Coordination

#### moqui-development-coordinator
- **Description**: Complex multi-component coordination requiring multiple specialists
- **Domain**: Multi-agent workflow orchestration
- **Tools**: Task, Read, Write, Edit, Bash
- **Color**: white
- **Specialization**: Multi-agent coordination, workflow management, quality assurance
- **Location**: Virtual agent (coordination patterns, no dedicated file)

## Skill Integration

### Available Skills (11 Domain Sub-Skills)

All skills are local, located in `.claude/skills/`. They are organized in a tiered loading strategy:

**Tier 1 -- Core (load at session start):**
| Skill | Domain |
|-------|--------|
| `moqui-xml` | XML formatting rules, default value handling |
| `moqui-services` | Service definitions, parameters, transactions, jobs |
| `moqui-entities` | Entity definitions, relationships, queries, views |

**Tier 2 -- Contextual (load when task area is detected):**
| Skill | Domain |
|-------|--------|
| `moqui-screens` | Screen layout, forms, navigation, transitions |
| `moqui-data` | Seed data, enumerations, status workflows, security config |
| `moqui-entity-filters` | Row-level security, filter definitions, debugging |
| `moqui-testing` | Spock test patterns, test data, authorization testing |

**Tier 3 -- Specialized (suggest when relevant):**
| Skill | Domain |
|-------|--------|
| `moqui-rest-api` | REST API endpoints, authentication |
| `moqui-l10n` | Localization, translations |
| `moqui-build` | Gradle, CI/CD, component structure |
| `moqui-opensearch` | DataDocuments, DataFeeds, search |

### Skill vs Agent Relationship

| Aspect | Skills | Agents |
|--------|--------|--------|
| Purpose | GUIDANCE and patterns | DOING implementation work |
| Invocation | Auto-invoked based on context | Explicit Task tool call |
| File Access | Read-only references | Full file modification |
| Location | `.claude/skills/` | `.claude/agents/` |

### Agents and Their Skill References

**Service Agents (3)**:
- `moqui-service-definition-specialist` - References `moqui-services` skill
- `moqui-service-implementation-specialist` - References `moqui-services`, `moqui-entity-filters` skills
- `moqui-service-integration-specialist` - References `moqui-services`, `moqui-rest-api` skills

**Entity/Data Agents (2)**:
- `moqui-entity-specialist` - References `moqui-entities` skill
- `moqui-data-specialist` - References `moqui-data`, `moqui-entity-filters` skills

**Screen Agents (2)**:
- `moqui-screen-specialist` - References `moqui-screens`, `moqui-entity-filters` skills
- `moqui-screen-ux-specialist` - References `moqui-screens` skill

**API Agents (2)**:
- `moqui-rest-api-specialist` - References `moqui-rest-api`, `moqui-entity-filters` skills
- `moqui-xml-api-specialist` - References `moqui-services` skill

**Test Agents (2)**:
- `moqui-test-specialist` - References `moqui-testing`, `moqui-entity-filters` skills
- `moqui-test-execution-specialist` - References `moqui-testing`, `moqui-build` skills

**Other Agents (1)**:
- `moqui-l10n-specialist` - References `moqui-l10n` skill

### Conflict Resolution

When skill patterns conflict with local `runtime/component/moqui-agent-os/` standards:

1. **Local enforcement always wins** as the default recommendation
2. **Prompt user** with both approaches shown side-by-side
3. **Let user choose** which approach to apply

See `runtime/component/moqui-agent-os/skill-integration.md` for detailed conflict resolution guidance.

## Agent Categories

### Technical Specialists
- Focus on specific technical domains and implementation patterns
- Deep expertise in particular technologies or frameworks
- Handle domain-specific file types and workflows
- Examples: moqui-entity-specialist, moqui-service-specialist, presentation-specialist

### Infrastructure Specialists
- Manage build systems, deployment, and infrastructure
- Focus on operational aspects and tool configuration
- Handle system-level configurations and automation
- Examples: moqui-build-coordinator, agent-builder

### Coordination Specialists  
- Orchestrate multiple agents for complex tasks
- Manage workflow dependencies and task sequencing
- Handle cross-cutting concerns and integration
- Examples: moqui-development-coordinator

## Usage Guidelines

### Agent Selection Criteria

**Memory-Optimized Selection Patterns:**

**Use Subdivision Specialists when:**
- Working within specific domains with clear boundaries
- Need focused expertise without memory overhead
- Implementing targeted patterns and best practices
- Requiring specialized validation within domain limits

**Service Development Selection:**
- **moqui-service-definition-specialist**: Service interfaces, parameters, contracts
- **moqui-service-implementation-specialist**: Business logic, workflow patterns, transactions
- **moqui-service-integration-specialist**: External APIs, service orchestration

**Screen Development Selection:**
- **moqui-screen-specialist**: All screen aspects (layout, forms, navigation, transitions)
- **moqui-screen-ux-specialist**: Accessibility, performance, user experience

**Testing Strategy Selection:**
- **moqui-test-specialist**: Unit tests, integration tests, authorization testing
- **moqui-test-execution-specialist**: Test automation, CI/CD integration

**Use Infrastructure Specialists when:**
- Configuring build systems or deployment pipelines
- Managing agent ecosystem and workflows
- Handling system-level configurations
- Setting up development environments

**Use Coordination Specialists when:**
- Task requires multiple specialists working together
- Complex workflow orchestration is needed
- Cross-cutting concerns span multiple domains
- Quality coordination across multiple components

**Memory Profile Considerations:**
- **Low Profile** (<12KB): Single-threaded development, quick iterations
- **Low-Medium Profile** (12-15KB): Standard development with moderate complexity
- **Medium Profile** (15-18KB): Complex workflows requiring careful monitoring
- **High Profile** (>18KB): DEPRECATED - Use subdivision instead

### Task Tool Pattern

Always use the Task tool with the following pattern:

```markdown
Task: {Describe the specific task}
Agent: {appropriate-specialist-agent}
Context: 
- Current state: {relevant file paths and current state}
- Requirements: {specific requirements and constraints}
- Quality standards: {validation criteria and standards}
- Integration needs: {how this connects to other components}
```

## Agent Maintenance

### Adding New Agents

1. Create agent file in `.claude/agents/[agent-name].md` (for Moqui specialists)
2. Update this registry with agent details
3. Add agent to CLAUDE.md available specialists list
4. Update development-guide.md specialist agents section
5. Add any file type restrictions to "Never Directly" lists

### Agent Updates

1. Update agent file with new capabilities or workflows
2. Update registry with changed information
3. Test agent functionality with representative tasks
4. Update documentation references as needed

### Quality Standards

All specialist agents must:
- Follow structured workflow patterns with XML tags
- Include comprehensive templates and code patterns
- Provide quality assurance checklists
- Handle error scenarios and troubleshooting
- Support integration with other agents
- Maintain version tracking and change history

## Memory Optimization and Reference Structure

### Reference Structure

All skill references are located in the top-level `references/` directory (tool-agnostic, readable by any AI tool):
```
references/
├── build_patterns.md
├── caching_best_practices.md
├── data_patterns.md
├── entity_filter_patterns.md
├── entity_patterns.md
├── form_patterns.md
├── l10n_patterns.md
├── opensearch_patterns.md
├── project_structure.md
├── query_examples.md
├── rendering_modes.md
├── rest_api_patterns.md
├── screen_patterns.md
├── service_implementation.md
├── service_jobs.md
├── service_patterns.md
├── testing_patterns.md
└── xml_best_practices.md
```

### Agent Development Guidelines

- Keep agents under 500 lines and 15KB
- Limit to 8 or fewer embedded code blocks
- Extract templates to dedicated skill reference files
- Design clear boundaries and single responsibilities

## Global Agents Reference

The following agents are globally available from `~/.claude/agents/` and can be used in any project:

### Development and Infrastructure
- **test-runner**: Test execution and analysis with framework delegation (Location: `~/.claude/agents/test-runner.md`)
- **git-workflow**: Git operations, commits, and pull requests (Location: `~/.claude/agents/git-workflow.md`)
- **agent-builder**: Agent creation and optimization (Location: `~/.claude/agents/agent-builder.md`)

### Documentation and Presentation
- **presentation-specialist**: LaTeX/Beamer presentations (Location: `~/.claude/agents/presentation-specialist.md`)
- **course-session-specialist**: Educational course and session planning (Location: `~/.claude/agents/course-session-specialist.md`)

For complete details on global agents, see: `~/.claude/agents/` (global agents directory)

---

**Last Updated**: 2026-03-01 (Cleaned up archived agent specs, fixed reference paths, updated skill integration)

## Summary of Active Agents (14 Moqui + 1 Global = 15 Total)

| # | Agent | Responsibility | Filter Aspect |
|---|-------|---------------|---------------|
| 1 | moqui-entity-specialist | Entity definitions, schema | - |
| 2 | **moqui-data-specialist** | Data files, **EntityFilter definitions** | Filter setup |
| 3 | moqui-service-definition-specialist | Service interfaces, parameters | - |
| 4 | **moqui-service-implementation-specialist** | Business logic, transactions | Service context |
| 5 | moqui-service-integration-specialist | External API consumption | REST context |
| 6 | **moqui-screen-specialist** (unified) | Layout + Flow + Forms | Screen debugging |
| 7 | moqui-screen-ux-specialist | Accessibility, performance | - |
| 8 | **moqui-test-specialist** (unified) | Unit + Integration testing | Auth testing |
| 9 | moqui-test-execution-specialist | Running tests, CI/CD | - |
| 10 | moqui-rest-api-specialist | REST API endpoints | - |
| 11 | moqui-xml-api-specialist | SOAP/XML integration | - |
| 12 | **moqui-opensearch-specialist** | OpenSearch/Elasticsearch integration | - |
| 13 | moqui-l10n-specialist | Localization | - |
| 14 | moqui-build-coordinator | Build, deployment | - |
| 15 | agent-builder | Agent management (global) | - |

## Consolidation History

The following agents were consolidated for efficiency (no files remain on disk):
- moqui-screen-layout-specialist, moqui-form-specialist, moqui-screen-flow-specialist → **moqui-screen-specialist** (unified)
- moqui-test-unit-specialist, moqui-test-integration-specialist → **moqui-test-specialist** (unified)
- moqui-entity-filter-specialist → **moqui-data-specialist** (EntityFilters are data files)
- moqui-service-transaction-specialist → **moqui-service-implementation-specialist** (transactions are rare, covered by skill)
- moqui-service-specialist, moqui-test-specialist-legacy → deleted (were delegator stubs)

## EntityFilter Cross-Cutting Coverage

EntityFilters are now handled by multiple specialized agents:

| Agent | EntityFilter Aspect |
|-------|---------------------|
| **moqui-data-specialist** | EntityFilterSet/EntityFilter definitions in data files |
| **moqui-service-implementation-specialist** | Filter context setup in REST APIs and services |
| **moqui-screen-specialist** | Filter debugging for missing data issues |
| **moqui-test-specialist** | Authorization-enabled testing scenarios |
| **moqui-rest-api-specialist** | REST endpoint filter context |

## Consolidation Rationale

- **Screen agents (3→1)**: All work on same XML files, artificial distinction caused coordination overhead
- **Test agents (2→1)**: Same Spock patterns, developers think "test" not "unit vs integration"
- **Entity-filter→data**: EntityFilters ARE data files, comprehensive skill covers all aspects
- **Transaction→implementation**: Transaction concerns are rare (2%), covered by skill

**Global Agents**: 5 agents available from `~/.claude/agents/`
**Total Available**: 19 specialist agents (14 active Moqui + 5 global)
