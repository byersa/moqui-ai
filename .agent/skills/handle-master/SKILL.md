# Skill: Huddle Master Orchestrator
**Alias:** `/rebuild-all`

## Step 1: Infrastructure Baseline
- **Action:** Run the `huddle-config` skill to ensure `build.gradle` and timezones are set.
- **Dependency:** `.agent/skills/huddle-config/SKILL.md`

## Step 2: Component Liftoff
- **Action:** Run the `huddle-liftoff` skill.
- **Tasks:** Pulls `demo` components and switches to `vue3quasar2` branches.
- **Dependency:** `.agent/skills/huddle-liftoff/SKILL.md`

## Step 3: Database & Metadata Sync
- **Action:** Run the `huddle-ingest` skill.
- **Condition:** Requires Moqui server to be running.
- **Tasks:** Triggers the REST service to sync 'huddle' XML with PostgreSQL.
- **Dependency:** `runtime/component/huddle/.agent/skills/huddle-ingest.md`