---
name: huddle-config
description: Configures Moqui for PostgreSQL by patching build.gradle and MoquiDevConf.xml.
---

# Skill: Huddle PostgreSQL Configurator

## Step 1: Credential Gathering
- **Action:** Ask the user for the following values (or use defaults if provided):
  - `DB_NAME` (Default: `nursinghome`)
  - `DB_USER` (Default: `ofbiz`)
  - `DB_PASS` (Default: `heber`)
  - `DB_PORT` (Default: `5434`)

## Step 2: Build System Patch (PostgreSQL Support)
- **Target:** `${WORKSPACE_ROOT}/build.gradle`
- **Action:** Append the driver dependency and automated install task.
- **Logic:** Use the agent's `replace` or `append` tool to insert the following at the end of the file:
```
  // Huddle Automation: Added PostgreSQL Driver
  task installDrivers { doLast {
      def postgresVersion = '42.7.2'
      def jarName = "postgresql-${postgresVersion}.jar"
      def url = "https://repo1.maven.org/maven2/org/postgresql/postgresql/${postgresVersion}/${jarName}"
      def targetDir = file('runtime/lib')
      def targetFile = file("runtime/lib/${jarName}")

      if (!targetDir.exists()) targetDir.mkdirs()

      if (!targetFile.exists()) {
          logger.lifecycle("Downloading PostgreSQL Driver from ${url}")
          ant.get(src: url, dest: targetFile)
      } else {
          logger.lifecycle("PostgreSQL Driver already exists at ${targetFile}")
      }
  } }

  // Force Mountain Time for Postgres compatibility
  System.setProperty('user.timezone', 'America/Denver')
```

## Step 3: XML Configuration Injection
- **Target File:** `runtime/conf/MoquiDevConf.xml`
- **Action:**
  1.  Check if `runtime/conf/MoquiDevConf.xml` exists.
  2.  **IF IT EXISTS:** Use `replace_file_content` to find the `<entity-facade ...>` block (or `<entity-facade>`) and replace it *only* with the datasource configuration below. **DO NOT** overwrite the entire file.
  3.  **IF IT DOES NOT EXIST:** Create the file with the basic structure and the datasource configuration.
- **Content (Datasource Block):**
```xml
    <entity-facade>
        <datasource group-name="transactional" database-conf-name="postgres" schema-name="public" startup-add-missing="true" default-timezone="America/Denver" >
            <inline-jdbc pool-maxsize="50">
                <xa-properties serverName="localhost" portNumber="{{DB_PORT}}" databaseName="{{DB_NAME}}" user="{{DB_USER}}" password="{{DB_PASS}}"/>
            </inline-jdbc>
        </datasource>
    </entity-facade>
```

## Step 3.2: Java Process Timezone Propagation
- **Target File:** `build.gradle`
- **Action:** Append a configuration block to ensure all forked Java processes (like the `load` task) inherit the correct timezone for PostgreSQL compatibility.
- **Content to Append:**
    ```gradle
    // --- Force Mountain Time for all Moqui Processes (Load/Run/Test) ---
    tasks.withType(JavaExec).configureEach {
        systemProperty 'user.timezone', 'America/Denver'
        systemProperty 'default_time_zone', 'America/Denver'
        systemProperty 'database_time_zone', 'America/Denver'

        // Required for Bitronix/JDBC Proxying on modern JVMs
        jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.base/java.util=ALL-UNNAMED")
    }
    ```

## Step 3.5: Timezone Environment Setup
- **Target File:** `start-huddle.sh`
- **Action:** Prepend the timezone environment variables so they are set every time the server starts.
- **Content to Add:**
  ```bash
  export default_time_zone=America/Denver
  export database_time_zone=America/Denver
  ``


## Step 4: Final Handover
- **Action:** 1. Verify that `runtime/lib` contains the `postgresql` driver.
  2. Confirm that `runtime/conf/MoquiDevConf.xml` has the correct `<datasource>` credentials.
- **Report:** Notify the user that the "Enterprise Handshake" is complete.
- **Next Step:** Prompt the user to execute `./gradlew build` to finalize the environment and then run `./start-huddle.sh` to confirm the PostgreSQL connection is active.
