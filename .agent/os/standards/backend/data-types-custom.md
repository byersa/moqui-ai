# Custom Data Types

Beyond standard types (seed, seed-initial, install, demo), define custom types for:

> **Project-Specific**: Data types below the standard `seed`, `seed-initial`, `install` are project-specific. Define your own following this pattern: `seed,seed-initial,{l10n}-install,{project}-demo,{component}-test,{project}-test`

## Use Cases

| Custom Type | Purpose |
|-------------|--------|
| `{l10n}-install` | Project-specific install data (replaces standard GL accounts) |
| `{project}-demo` | Demo site data (project-specific demos) |
| `*-test` | Testing data (e.g., `{component}-test`) |
| `dteCertBoleta` | DTE certification data |
| `update` | Data migrations/updates |
| `optional-mounts` | Optional screen mounts |

## When to Create Custom Types
- Standard type loads incompatible data (e.g., different GL accounts)
- Need fine-grained control over what loads in which environment
- Testing data should not load in production
- Demo data is project-specific, not framework demo

## Configuration
Configure in Gradle's `entity_empty_db_load`:

```groovy
// Load project-specific instead of standard install
entity_empty_db_load = "seed,seed-initial,{l10n}-install"

// Include test data for test runs
entity_empty_db_load = "seed,seed-initial,install,{component}-test"
```

## File Naming
```
data/
├── AAASetupData.xml              # type="seed"
├── AACInstallData.xml            # type="{l10n}-install" (custom)
├── BBBOptionalScreenMounts.xml   # type="optional-mounts" (custom)
├── ZaaDemoData.xml               # type="{project}-demo" (custom)
└── TestData.xml                  # type="{component}-test" (custom)
```

## Optional Screen Mounts Pattern

The `optional-mounts` type separates screen mounts that should **not** load by default but can be included when needed. This keeps the main apps menu clean while preserving screens for environments that need them.

### When to Use
- A screen should be available in some deployments but not others
- An admin/utility screen clutters the main apps menu for most users
- You want to remove a screen from the default navigation without deleting it

### File Structure

Use the `BBB` prefix (loads after `AAA` seed data, before other data files):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entity-facade-xml type="optional-mounts">

    <moqui.screen.SubscreensItem userGroupId="ALL_USERS" subscreenName="MyAdmin"
            menuIndex="5" menuInclude="Y" menuTitle="My Admin Screen"
            subscreenLocation="component://my-component/screen/MyAdmin.xml"
            screenLocation="component://webroot/screen/webroot/apps.xml"/>

</entity-facade-xml>
```

### Loading Optional Mounts

Include in Gradle's `entity_empty_db_load` when the screens are needed:

```groovy
// Default: does NOT include optional-mounts
entity_empty_db_load = "seed,seed-initial,install"

// Include optional screens
entity_empty_db_load = "seed,seed-initial,install,optional-mounts"
```

### Key Points
- Security configuration (ArtifactGroup, ArtifactAuthz) should remain in the main seed data file — only the SubscreensItem goes in the optional file
- The screen is still accessible by direct URL without the mount; the mount only controls apps menu presence
- Multiple components can each have their own `BBBOptionalScreenMounts.xml`