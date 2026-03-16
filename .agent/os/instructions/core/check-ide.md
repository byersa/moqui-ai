---
description: IDE configuration checker for Moqui Framework development
globs:
alwaysApply: false
version: 1.0
encoding: UTF-8
---

# Check IDE Workflow

## Description

Validates that your IntelliJ IDEA is properly configured for Moqui development, including:
- Git repository structure (root, runtime, components)
- Java/Groovy environment with moqui.war library
- XML schema configuration for Moqui files
- Run configurations (H2, PostgreSQL, tests, cleanDb)
- Gradle build system
- Runtime configuration files
- Project components (localization, utilities, shared)
- Development tools setup
- OpenSearch/Elasticsearch environment variables

> **Note**: See your project's overlay profile for concrete data type values, component names, and project-specific configuration details.

## Implementation

When this command is invoked, perform the following checks:

### 1. Git Repository Structure
- Check for root .git directory (moqui-framework)
- Check for runtime/.git directory (moqui-runtime)
- List all component repositories in runtime/component/*/
- Verify VCS mappings in .idea/vcs.xml

### 2. Java/Groovy Environment
- Check JDK 11+ in .idea/misc.xml
- Verify moqui.war exists in project root
- **REQUIRED**: Check if moqui.war is configured as library in .idea/libraries/ (auto-fixable)
- Verify Groovy source directories exist

### 3. XML Schema Configuration (REQUIRED)
- Check for framework/xsd/framework-catalog.xml
- Count XSD files in framework/xsd/
- **REQUIRED**: Check if XML catalog is configured in .idea/xmlCatalog.xml (auto-fixable)
- Verify schema validation is enabled

### 4. Run Configurations
- Check .idea/workspace.xml for required configurations:
  - Run H2 DB (auto-fixable)
  - **Run PostgreSQL** (auto-fixable - must use environment variables, not script parameters)
  - cleanDb load (auto-fixable) - must have `-Ptypes=seed,seed-initial,{l10n}-install`
  - cleanDb load demo (auto-fixable) - must have `-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo`
- **Test Configurations**: Auto-detect and validate test run configurations:
  - **Component Detection Process**:
    1. Scan each directory in `runtime/component/`
    2. Check for presence of both `component.xml` and `build.gradle`
    3. Verify `build.gradle` contains test task: `grep -q "test" build.gradle`
    4. Extract component name: `grep -o 'name="[^"]*"' component.xml`
    5. Map to expected configuration name: "Test {component-name}"
  - **Expected Test Configurations** (auto-fixable if missing):
    - Auto-detect all testable components from `runtime/component/` directories
    - For each component with tests: Test {component-name} -> `:runtime:component:{component-directory}:test`
    - Test mantle-usl -> `:runtime:component:mantle-usl:test` (special case - uses `install,demo`)
  - **IMPORTANT: Test configurations MUST include cleanDb, load, and -Ptypes= arguments**:
    - The `load` task is in the main project's build.gradle (not component build.gradle)
    - `-Ptypes=` IS REQUIRED - data is NOT auto-loaded when running tests
    - Standardized pattern: `seed,seed-initial,{l10n}-install,{project}-demo,{localization-component}-test,{project}-test,{component-name}-test`
  - **Validation Checks**:
    - Verify run configuration names match component names from component.xml
    - Check gradle task paths include `cleanDb`, `load`, and `:runtime:component:{directory}:test`
    - **Verify -Ptypes= argument** uses standardized pattern
    - **CRITICAL**: Verify `<RunAsTest>true</RunAsTest>` is set for all test configurations (auto-fixable if false or missing)
- **CRITICAL**: Validate PostgreSQL configuration uses environment variables (entity_ds_database, entity_ds_db_conf, entity_ds_schema, etc.)
- Read the complete workspace.xml file to parse all run configurations
- List any additional test configurations found and identify missing ones

### 5. Build System
- Check for gradlew and gradlew.bat
- Verify build.gradle exists
- Check settings.gradle for component inclusion
- Verify Spock test framework directories

### 6. Runtime Configuration
- **REQUIRED**: Check for runtime/conf/MoquiDevConf.xml (auto-fixable)
- **REQUIRED**: Check for runtime/conf/MoquiProductionConf.xml (auto-fixable)
- Validate configuration file structure and key settings
- Check for recommended excluded directories in module configuration (auto-fixable)

### 7. Project Components
- Check for {localization-component} (required)
- Check for {utils-component} (required)
- Check for {shared-component} (optional)
- Check for {main-component} (optional)
- **Check for agent-os** (required - Claude Code configuration repository)
- List any additional components found

> **Note**: See your project's overlay profile for the concrete component names to check.

### 7.1 Agent OS Repository Check
- **REQUIRED**: Verify `runtime/component/moqui-agent-os/` exists
- Check for `runtime/component/moqui-agent-os/.git` (should be a git repository)
- Verify key configuration files exist:
  - `runtime/component/moqui-agent-os/Claude.md` or `CLAUDE.md`
  - `runtime/component/moqui-agent-os/agent-registry.md`
  - `runtime/component/moqui-agent-os/.claude/agents/` directory
  - `runtime/component/moqui-agent-os/skill-integration.md`
- If agent-os is missing, provide instructions to add it:
  ```bash
  cd runtime/component
  git clone <agent-os-repo-url> agent-os
  ```

### 8. Development Tools
- Verify hot reload capability via MoquiDevConf.xml
- Provide debug configuration instructions
- Check component dependencies in component.xml files

### 9. OpenSearch/Elasticsearch Configuration
Check that run configurations include proper OpenSearch environment variables for local development:

**Required Environment Variables:**
- `elasticsearch_mode` - Should be `rest`
- `elasticsearch_host1` - Should be `http://localhost:9200`
- `elasticsearch_url` - Should be `http://localhost:9200`
- `elasticsearch_index_prefix` - Should be `moqui-{project-name}-devel_` (with trailing underscore)

**Project Name Detection:**
The `{project-name}` in the index prefix should be determined using the same logic as component detection:
1. Find the main component directory in `runtime/component/`
2. Use the component directory name or extract from `component.xml`
3. Convert to lowercase and replace spaces/special characters with hyphens
4. Example: For a project named `my-app` -> `moqui-my-app-devel_`

**Check Process:**
1. Parse the run configurations in `.idea/workspace.xml`
2. For each "Run H2 DB" and "Run PostgreSQL" configuration:
   - Check if `<option name="env">` block exists
   - Verify all four OpenSearch environment variables are present
   - Validate values match expected format
3. Report missing or incorrect configurations

**Validation Rules:**
- `elasticsearch_mode` must equal `rest`
- `elasticsearch_host1` must be a valid URL (typically `http://localhost:9200`)
- `elasticsearch_url` must match `elasticsearch_host1`
- `elasticsearch_index_prefix` must follow pattern `moqui-{project-name}-devel_` with trailing underscore

**Why This Matters:**
- Prevents index name collisions when multiple Moqui projects share the same local OpenSearch instance
- Ensures consistent configuration across development environments
- Required for DataDocument indexing and search functionality

## Output Format

Use color-coded symbols:
- check (green) - Check passed
- warning (yellow) - Warning/optional improvement
- cross (red) - Failed/required fix

Provide a summary at the end with:
- Total checks performed
- Number passed/warnings/failed
- Quick action items for fixing issues

## Auto-Fix Capability

When issues are found that can be automatically fixed, offer to apply changes directly:

### Fixable Issues:
1. **Missing excluded directories**: Can add to `.idea/modules/runtime/{project}.runtime.iml`
2. **Missing moqui.war library**: Can create proper moqui.war library configuration in `.idea/libraries/`
3. **Missing XML catalog configuration**: Can create `.idea/xmlCatalog.xml` with framework catalog reference
4. **Missing run configurations**: Can add to `.idea/workspace.xml`
5. **Missing test run configurations**: Can auto-detect testable components and create complete test configurations with cleanDb, load, and -Ptypes
6. **Missing runtime configuration files**: Can create `runtime/conf/MoquiDevConf.xml` and `runtime/conf/MoquiProductionConf.xml`
7. **Incorrect PostgreSQL run configuration**: Can update to use environment variables instead of script parameters
8. **Missing or incorrect `<RunAsTest>` setting**: Can update test configurations to set `<RunAsTest>true</RunAsTest>` for proper IDE test progress display
9. **Missing OpenSearch environment variables**: Can add `elasticsearch_mode`, `elasticsearch_host1`, `elasticsearch_url`, and `elasticsearch_index_prefix` to run configurations
10. **Incomplete test run configurations**: Can update to include cleanDb, load, and standardized -Ptypes= arguments
11. **Missing or incorrect cleanDb load configuration**: Must have `-Ptypes=seed,seed-initial,{l10n}-install` for localization
12. **Missing cleanDb load demo configuration**: Must have `-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo` for demo data

### Process:
1. After displaying check results, identify any fixable issues
2. Ask user: "Would you like me to apply the following fixes directly to your IDE configuration?"
3. List the specific changes that would be made
4. If user confirms, apply the changes using Edit tool
5. Inform user to restart IntelliJ IDEA if needed

### Example Auto-Fixes:
- Add missing excluded directories to runtime module
- Create moqui.war library configuration (replaces old jar-based libraries)
- Create XML catalog configuration for Moqui schema validation
- Add missing run configurations (cleanDb load, test configurations, PostgreSQL with environment variables)
- Create missing runtime configuration files (MoquiDevConf.xml, MoquiProductionConf.xml)
- Fix PostgreSQL run configuration to use environment variables instead of script parameters
- Update VCS mappings if new component repositories detected
- Add OpenSearch environment variables to run configurations

### PostgreSQL Run Configuration Template:
When adding a "Run PostgreSQL" configuration, use environment variables (not script parameters):
```xml
<configuration name="Run PostgreSQL" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="env">
      <map>
        <entry key="elasticsearch_host1" value="http://localhost:9200" />
        <entry key="elasticsearch_index_prefix" value="moqui-{project-name}-devel_" />
        <entry key="elasticsearch_mode" value="rest" />
        <entry key="elasticsearch_url" value="http://localhost:9200" />
        <entry key="entity_ds_database" value="{project_database_name}" />
        <entry key="entity_ds_db_conf" value="postgres" />
        <entry key="entity_ds_host" value="localhost" />
        <entry key="entity_ds_port" value="5432" />
        <entry key="entity_ds_schema" value="public" />
        <entry key="entity_ds_user" value="{project_database_user}" />
      </map>
    </option>
    <option name="executionName" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="scriptParameters" value="" />
    <option name="taskNames">
      <list>
        <option value="run" />
      </list>
    </option>
  </ExternalSystemSettings>
</configuration>
```

Replace placeholders with appropriate values from your project's overlay profile:
- `{project-name}`: The main component name (e.g., "my-app", "my-wms")
- `{project_database_name}`: The PostgreSQL database name (e.g., "my_app", "my_wms")
- `{project_database_user}`: The database user (typically same as database name)

**Note**: The `entity_ds_schema` environment variable is required for PostgreSQL JDBC driver 42.7.5+ compatibility. It ensures proper case-sensitive schema matching in database metadata queries.

### H2 DB Run Configuration Template:
When adding a "Run H2 DB" configuration, include OpenSearch environment variables:
```xml
<configuration name="Run H2 DB" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="env">
      <map>
        <entry key="elasticsearch_host1" value="http://localhost:9200" />
        <entry key="elasticsearch_index_prefix" value="moqui-{project-name}-devel_" />
        <entry key="elasticsearch_mode" value="rest" />
        <entry key="elasticsearch_url" value="http://localhost:9200" />
      </map>
    </option>
    <option name="executionName" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="scriptParameters" value="" />
    <option name="taskNames">
      <list>
        <option value="run" />
      </list>
    </option>
  </ExternalSystemSettings>
</configuration>
```

Replace `{project-name}` with the main component name from your project's overlay profile.

### cleanDb load Configuration Template:
When adding or fixing the "cleanDb load" configuration, use `-Ptypes=seed,seed-initial,{l10n}-install`:
```xml
<configuration name="cleanDb load" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="executionName" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="scriptParameters" value="-Ptypes=seed,seed-initial,{l10n}-install" />
    <option name="taskDescriptions">
      <list />
    </option>
    <option name="taskNames">
      <list>
        <option value="cleanDb" />
        <option value="load" />
      </list>
    </option>
    <option name="vmOptions" />
  </ExternalSystemSettings>
  <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
  <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
  <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
  <DebugAllEnabled>false</DebugAllEnabled>
  <RunAsTest>false</RunAsTest>
  <GradleProfilingDisabled>false</GradleProfilingDisabled>
  <GradleCoverageDisabled>false</GradleCoverageDisabled>
  <method v="2" />
</configuration>
```

**Data Types Explanation:**
- `seed` - Core Moqui framework data
- `seed-initial` - Initial configuration data
- `{l10n}-install` - Project-specific localization/installation data - **ALWAYS required**

> **Note**: See your project's overlay profile for the concrete `{l10n}-install` data type name.

### cleanDb load demo Configuration Template:
When adding the "cleanDb load demo" configuration for demo data:
```xml
<configuration name="cleanDb load demo" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="executionName" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="scriptParameters" value="-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo" />
    <option name="taskDescriptions">
      <list />
    </option>
    <option name="taskNames">
      <list>
        <option value="cleanDb" />
        <option value="load" />
      </list>
    </option>
    <option name="vmOptions" />
  </ExternalSystemSettings>
  <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
  <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
  <ExternalSystemDebugDisabled>false</ExternalSystemDebugDisabled>
  <DebugAllEnabled>false</DebugAllEnabled>
  <RunAsTest>false</RunAsTest>
  <GradleProfilingDisabled>false</GradleProfilingDisabled>
  <GradleCoverageDisabled>false</GradleCoverageDisabled>
  <method v="2" />
</configuration>
```

**Data Types Explanation:**
- `seed,seed-initial,{l10n}-install` - Base data (same as cleanDb load)
- `{project}-demo` - Demo data with relative date expressions (always fresh)

**Important**: Use the project-specific demo type (NOT generic `demo`) for projects that use the demo data refresh standard with `@rel:` date expressions.

> **Note**: See your project's overlay profile for the concrete `{project}-demo` data type name.

### Test Run Configuration Template:
When adding test configurations for components, use the following pattern with cleanDb, load, and -Ptypes:
```xml
<configuration name="Test {component-name}" type="GradleRunConfiguration" factoryName="Gradle">
  <ExternalSystemSettings>
    <option name="executionName" />
    <option name="externalProjectPath" value="$PROJECT_DIR$" />
    <option name="externalSystemIdString" value="GRADLE" />
    <option name="scriptParameters" value="-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{localization-component}-test,{project}-test,{component-name}-test" />
    <option name="taskNames">
      <list>
        <option value="cleanDb" />
        <option value="load" />
        <option value=":runtime:component:{component-directory}:test" />
      </list>
    </option>
    <option name="vmOptions" />
  </ExternalSystemSettings>
  <ExternalSystemDebugServerProcess>true</ExternalSystemDebugServerProcess>
  <ExternalSystemReattachDebugProcess>true</ExternalSystemReattachDebugProcess>
  <DebugAllEnabled>false</DebugAllEnabled>
  <RunAsTest>true</RunAsTest>
  <method v="2" />
</configuration>
```

**CRITICAL**: The `<RunAsTest>true</RunAsTest>` setting is essential for IntelliJ IDEA to display real-time test progress, including:
- Test count updates (X/Y tests passed)
- Pass/fail status as tests execute
- Progress bar
- Execution time in the tab title

**IMPORTANT: -Ptypes= IS REQUIRED for Test Configurations**

Test run configurations MUST include:
1. `cleanDb` - Clean the database before loading data
2. `load` - Load data (this task is in the main project's build.gradle)
3. `:runtime:component:{component}:test` - Run the component tests
4. `-Ptypes=...` - Specify which data types to load

**Standardized data type pattern for project components:**
```
-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo,{localization-component}-test,{project}-test,{component-name}-test
```

- `seed,seed-initial` - Framework core
- `{l10n}-install` - Project localization/installation data (NOT generic `install`)
- `{project}-demo` - Project demo data (NOT generic `demo`)
- `{localization-component}-test` - Localization component test dependency (always included)
- `{project}-test` - Shared project test users
- `{component-name}-test` - Component-specific (harmless if no files exist)

> **Note**: See your project's overlay profile for the concrete data type names to use.

**Component Detection and Validation Logic**:
1. Scan `runtime/component/` directories
2. For each directory, check if both `component.xml` and `build.gradle` exist
3. Verify `build.gradle` contains test task configuration
4. Extract component name from `name="..."` attribute in `component.xml`
5. Use directory name for the gradle task path (`:runtime:component:{directory}:test`)
6. **Verify -Ptypes= argument** uses standardized pattern

**Special Case - mantle-usl**:
The mantle-usl component uses standard Moqui data types (not project-specific):
```
-Ptypes=seed,seed-initial,install,demo
```

### Runtime Configuration Templates:

#### MoquiDevConf.xml Template:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<moqui-conf xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/moqui-conf-3.xsd">
    <default-property name="entity_cache_expire" value="true"/>
    <default-property name="screen_cache_expire" value="true"/>
    <default-property name="transition_cache_expire" value="true"/>
    <default-property name="template_cache_expire" value="true"/>
    <default-property name="service_cache_expire" value="true"/>

    <!-- Development Mode - expires artifact cache entries; don't use these for production, load testing, etc -->
    <artifact-execution-facade>
        <artifacts>
            <artifact name-regex=".*" type="screen" cache="false"/>
            <artifact name-regex=".*" type="transition" cache="false"/>
        </artifacts>
    </artifact-execution-facade>

    <screen-facade>
        <screen location="component://webroot/screen/webroot.xml"/>
    </screen-facade>

    <!-- turn off tarpit in dev mode -->
    <webapp-list>
        <webapp name="webroot">
            <session-config timeout="43200"/>
            <!-- disable tarpit for development -->
            <first-hit-in-visit>false</first-hit-in-visit>
        </webapp>
    </webapp-list>
</moqui-conf>
```

#### MoquiProductionConf.xml Template:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<moqui-conf xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/moqui-conf-3.xsd">

    <screen-facade>
        <screen location="component://webroot/screen/webroot.xml"/>
    </screen-facade>

    <webapp-list>
        <webapp name="webroot">
            <session-config timeout="43200"/>
        </webapp>
    </webapp-list>

</moqui-conf>
```

### Auto-Fix Detection Logic:
When checking runtime configuration:

1. **Missing MoquiDevConf.xml**: Create with development-optimized settings (cache expiration, artifact cache disabled)
2. **Missing MoquiProductionConf.xml**: Create with production-optimized settings (caching enabled)
3. **Incorrect PostgreSQL run config**: Detect script parameters like `-Pconf=conf/MoquiProductionConf.xml` and replace with environment variables
4. **Missing excluded directories**: Add `db/`, `elasticsearch/`, `activemq-data/`, `tmp/` to module exclusions for better IDE performance
5. **Incorrect RunAsTest setting**: For any test configuration (names starting with "Test "), check if `<RunAsTest>` is `false` or missing and update to `true`
6. **Missing OpenSearch environment variables**: Check for `elasticsearch_mode`, `elasticsearch_host1`, `elasticsearch_url`, and `elasticsearch_index_prefix` in run configurations and add if missing

#### OpenSearch Environment Variables Validation:
For each run configuration ("Run H2 DB", "Run PostgreSQL") in workspace.xml:
- Parse the `<option name="env">` section
- Check if all four OpenSearch environment variables are present:
  - `elasticsearch_mode` (expected: "rest")
  - `elasticsearch_host1` (expected: "http://localhost:9200")
  - `elasticsearch_url` (expected: "http://localhost:9200")
  - `elasticsearch_index_prefix` (expected: "moqui-{project-name}-devel_")
- **Auto-fix**: Add missing environment variables to the `<map>` block
- **Project name detection**: Use the same logic as component detection to determine the project name for the index prefix

#### RunAsTest Validation:
For each test configuration in workspace.xml:
- Parse the `<configuration name="Test ..." type="GradleRunConfiguration">` sections
- Check if `<RunAsTest>false</RunAsTest>` or `<RunAsTest>` is missing
- **Auto-fix**: Update to `<RunAsTest>true</RunAsTest>` to enable real-time test progress display
- **Note**: User must close IntelliJ IDEA before applying this fix, then reopen after changes are applied

#### cleanDb load Configuration Validation:
For the "cleanDb load" configuration in workspace.xml:
- Check if configuration exists
- Check if `scriptParameters` contains `-Ptypes=seed,seed-initial,{l10n}-install`
- **Auto-fix**: Create or update configuration with correct `-Ptypes` parameter
- **Common error**: Empty `scriptParameters` means no data types specified

#### cleanDb load demo Configuration Validation:
For the "cleanDb load demo" configuration in workspace.xml:
- Check if configuration exists
- Check if `scriptParameters` contains `-Ptypes=seed,seed-initial,{l10n}-install,{project}-demo`
- **Auto-fix**: Create configuration with correct `-Ptypes` parameter including `{project}-demo`
- **Important**: Verify the project-specific demo type (not generic `demo`) is used

## Example Usage

```
User: /check-ide
Assistant: # IDE Configuration Check for Moqui Framework

Checking IntelliJ IDEA configuration for moqui-framework development...

## Git Repository Structure
- Root git repository detected (.git/)
- Runtime git repository detected (runtime/.git)
- Component repositories found (N):
  - runtime/component/{localization-component}
  - runtime/component/{utils-component}
  - runtime/component/{main-component}
  - (and more...)
- VCS mappings properly configured in .idea/vcs.xml

...
```
