---
name: huddle-liftoff
description: Full bootstrap for Moqui 4.0. Flattens framework, sets up runtime, and creates dynamic component.
---
# Skill: Moqui Dynamic Project Initializer

## Step 1: Variable Identification
- **Derive BASE_NAME:** Analyze the workspace folder name. Strip suffixes like '-project', '-setup', or '-ai' to find the core project name (e.g., 'huddle-project' becomes 'huddle').
- **Define COMP_PATH:** Set as `runtime/component/{{BASE_NAME}}`.

## Step 2: Framework Flattening (Project Root Setup)
1. **Initialize Framework:**
   - Create a temporary directory `_tmp_framework`.
   - Execute: `git clone https://github.com/moqui/moqui-framework.git _tmp_framework`.
   - **Crucial Action:** Move all contents of `_tmp_framework/` (including `framework/`, `build.gradle`, `gradlew`, `gradle/`, and `MoquiInit.properties`) directly into the current workspace root.
   - Execute: `rm -rf _tmp_framework`.
   
2. **Initialize Runtime:**
   - Execute: `git clone https://github.com/moqui/moqui-runtime.git runtime`.
   - Verify the sibling structure: Root now contains both `framework/` and `runtime/`.

## Step 2.5: Component Set & UI Transition
- **Target:** Terminal / Workspace Root
- **Action:** 1. Fetch the demo component set.
            2. Switch UI-critical components to the Vue 3 / Quasar 2 branch.
- **Commands:**
  ```bash
  # Pull the standard ecosystem
  ./gradlew getComponentSet -PcomponentSet=demo

  # Targeted Branch Switch (Vue 3 / Quasar 2)
  # We iterate through the components and attempt to checkout the branch if it exists.
  for dir in runtime/component/*; do
    if [ -d "$dir/.git" ]; then
      echo "Checking for vue3quasar2 branch in $dir..."
      cd "$dir"
      if git show-ref --verify --quiet refs/remotes/origin/vue3quasar2; then
        git checkout vue3quasar2
      elif git show-ref --verify --quiet refs/heads/vue3quasar2; then
        git checkout vue3quasar2
      else
        echo "No vue3quasar2 branch found in $dir, staying on current branch."
      fi
      cd - > /dev/null
    fi
  done
  ```

## Step 3: Component Scaffolding & Data Modeling
1. **Create Directory:** `{{COMP_PATH}}/entity/`.
2. **Create {{BASE_NAME}}ArtifactEntities.xml:**
   - **Requirement:** Never use `.ent.xml`. Use `Entities.xml` suffix.
   - **Content:**
   ```xml
 <entities xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/entity-definition-3.xsd">

    <entity entity-name="HuddleArtifact" package="huddle.artifact" 
            cache="true" enable-audit-log="true">
        
        <field name="artifactId" type="id" is-pk="true"/>
        
        <field name="artifactName" type="text-medium" enable-audit-log="true"/>
        <field name="artifactTypeEnumId" type="id"/> <field name="fullPath" type="text-long"/> <field name="description" type="text-medium"/>
        <field name="rawContent" type="text-very-long" encrypt="true"/> <field name="checksum" type="text-short"/> <field name="vectorEmbedding" type="binary-very-long"/>
        <field name="lastIndexedDate" type="date-time"/>

        <relationship type="one" title="ArtifactType" related="moqui.basic.Enumeration">
            <key-map field-name="artifactTypeEnumId" related="enumId"/>
        </relationship>
        
        <seed-data>
            <moqui.basic.EnumerationType description="Huddle Artifact Types" enumTypeId="HuddleArtifactType"/>
            <moqui.basic.Enumeration description="Entity Definition" enumId="HatEntity" enumTypeId="HuddleArtifactType"/>
            <moqui.basic.Enumeration description="Service Definition" enumId="HatService" enumTypeId="HuddleArtifactType"/>
            <moqui.basic.Enumeration description="XML Screen" enumId="HatScreen" enumTypeId="HuddleArtifactType"/>
            <moqui.basic.Enumeration description="Logic/Script" enumId="HatLogic" enumTypeId="HuddleArtifactType"/>
        </seed-data>
    </entity>
</entities>
```
## Step 4: Shell Automation & Roadmap (Root Level)
1. **Create the start script:**
   - **Path:** `start-{{BASE_NAME}}.sh`
   - **Content:**
     ```bash
     #!/bin/bash
     # Moqui 4.0 Bootstrapper for Java 21
     # Reflection flags are required for internal module access
     java --add-opens java.base/java.lang=ALL-UNNAMED \
          --add-opens java.base/java.util=ALL-UNNAMED \
          --add-opens java.base/java.time=ALL-UNNAMED \
          --add-opens java.base/java.nio=ALL-UNNAMED \
          -jar framework/moqui.war
     ```
2. **Post-Action:** Run `chmod +x start-{{BASE_NAME}}.sh` to make it executable.

3. **Generate Roadmap:**
   - **Path:** `{{BASE_NAME}}_Master_Roadmap.md`
   - **Content:**
   ```markdown
   # {{BASE_NAME}} Project: Master Roadmap
   - [x] Task 1: Workspace Initialization (Cloned Framework/Runtime, Created Component).
   - [ ] Task 2: Database Artifact Table Definition.
   - [ ] Task 3: Metadata Ingestion Service (Scan & Index Artifacts).
   - [ ] Task 4: Top-Level Portal (5-zone UI: Left Menu, Header, Content, Footer, Sidebar).
   - [ ] Task 5: Start Script Validation (Java 21 Flag Test).
   - [ ] Task 6: Project-wide Artifact Generation.
   - [ ] Task 7: Vector Embeddings for AI Retrieval.

## Step 5: Final Handover
- **Path Reporting:** Report the exact absolute paths for:
  1. The new component directory.
  2. The `Entities.xml` file.
  3. The `start-{{BASE_NAME}}.sh` script.
- **Rules Reinforcement:** Remind the user that all future entity fields must follow the HIPAA `encrypt="true"` rule and audit logging as defined in `.agent/rules/huddle-rules.md`.
- **Next Step:** Prompt the user to run `./gradlew build` to verify the framework and runtime are correctly linked before moving to Task 2 on the Roadmap.
