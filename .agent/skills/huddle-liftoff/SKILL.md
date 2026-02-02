---
name: huddle-liftoff
description: Full bootstrap for Moqui 4.0. Flattens framework, sets up runtime, and creates dynamic component.
---
# Skill: Huddle System Liftoff
**Alias:** `/liftoff`

## Protocol
## Step 1: Bridge Integration
1. **Bridge Integration:** Clone `schue/moqui-mcp` if missing.

## Step 2: Variable Identification
- **Derive BASE_NAME:** Analyze the workspace folder name. Strip suffixes like '-project', '-setup', or '-ai' to find the core project name (e.g., 'huddle-project' becomes 'huddle').
- **Define COMP_PATH:** Set as `runtime/component/{{BASE_NAME}}`.


## Step 3: Component Set & UI Transition
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

## Step 4: Shell Automation & Roadmap (Root Level)
1. **Create the start script:**
   - **Path:** `start-{{BASE_NAME}}.sh`
   - **Content:**
     ```bash
#!/bin/bash
    export default_time_zone=America/Denver
    export database_time_zone=America/Denver
    # Reflection flags are required for internal module access
    java --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/java.time=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     -server \
     -Dmoqui.conf="conf/MoquiDevConf.xml" \
     -Dmoqui.runtime="runtime" \
     -Xmx4096m \
     -Duser.timezone="America/Denver" \
     -Ddefault_time_zone="America/Denver" \
     -Dmoqui.logger.level.xml_action="info" \
     -jar moqui.war

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
