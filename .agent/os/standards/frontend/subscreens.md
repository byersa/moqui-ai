# Subscreen Standards

### Subscreen Auto-Discovery

**Screens in subscreen directories are auto-discovered:**

```
screen/
├── MyApp.xml                    # Application root
└── MyApp/                       # Subscreen directory (same name as parent)
    ├── Dashboard.xml            # /MyApp/Dashboard
    ├── Orders.xml               # /MyApp/Orders
    └── Orders/                  # Nested subscreens
        ├── FindOrder.xml        # /MyApp/Orders/FindOrder
        └── OrderDetail.xml      # /MyApp/Orders/OrderDetail
```

### Subscreens Definition

**In parent screen:**
```xml
<screen>
    <subscreens default-item="Dashboard">
        <!-- Auto-discovered from same-name directory -->
    </subscreens>
</screen>
```

### Explicit Subscreen Items

```xml
<subscreens default-item="Dashboard">
    <subscreens-item name="Dashboard" menu-title="Dashboard" menu-index="1"/>
    <subscreens-item name="Orders" menu-title="Orders" menu-index="2"/>
    <subscreens-item name="Settings" menu-title="Settings" menu-index="99"/>
</subscreens>
```

### Menu Configuration

| Attribute | Purpose |
|-----------|---------|
| `menu-title` | Display name in navigation |
| `menu-index` | Sort order (lower = first) |
| `menu-include` | Show in menu (default: true) |

```xml
<!-- Hidden from menu but accessible -->
<subscreens-item name="OrderDetail" menu-include="false"/>

<!-- Specific menu order -->
<subscreens-item name="Dashboard" menu-title="Home" menu-index="1"/>
```

### Subscreen-Panel (Tabbed Navigation)

```xml
<widgets>
    <subscreens-panel id="OrderTabs" type="tab">
        <subscreens-item name="Details" menu-title="Details"/>
        <subscreens-item name="Items" menu-title="Items"/>
        <subscreens-item name="History" menu-title="History"/>
    </subscreens-panel>
</widgets>
```

### Subscreen Types

| Type | Display |
|------|---------|
| `tab` | Horizontal tabs |
| `stack` | Vertical accordion |
| `popup` | Modal dialogs |

### Mounted Section Pattern

**Link directly to subscreen:**
```xml
<link url="OrderDetail" text="View Details"
      parameter-map="[orderId:orderId]"/>
```

**Embedded subscreen content:**
```xml
<section name="OrderDetailsSection">
    <widgets>
        <include-screen location="component://example/screen/OrderDetail.xml"/>
    </widgets>
</section>
```

### Subscreen Parameters

**Passed via URL or parameter-map:**
```xml
<!-- Link with parameters -->
<link url="OrderDetail" parameter-map="[orderId:orderId]"/>

<!-- Target screen declares parameters -->
<screen>
    <parameter name="orderId" required="true"/>
</screen>
```

### Menu Hierarchy

```
MyApp (root)
├── Dashboard (menu-index="1")
├── Orders (menu-index="10")
│   ├── FindOrder
│   ├── CreateOrder
│   └── OrderDetail (menu-include="false")
├── Customers (menu-index="20")
└── Settings (menu-index="99")
```

### Always-Actions Inheritance

**Root screen always-actions run for all subscreens:**
```xml
<!-- Root screen -->
<screen>
    <always-actions>
        <!-- Filter context setup - runs for all subscreens -->
        <service-call name="setup#FilterContext" in-map="context" out-map="context"/>
    </always-actions>
    <subscreens/>
</screen>
```

### Pre-Actions vs Always-Actions

| Actions | When Runs | Use For |
|---------|-----------|---------|
| `always-actions` | Every request | Filter context, auth checks |
| `pre-actions` | Before rendering | Data loading |
| `actions` | For rendering | Main screen data |

### Dynamic Subscreen Loading

```xml
<subscreens-active/>
```

**Renders the currently selected subscreen in the hierarchy.**

### Pass-Through Screens for Nested Directories

**When subscreens are organized in subdirectories, each directory level needs an intermediate XML screen file for routing.**

Moqui resolves URL paths step by step. A link to `Ticket/CreateTicket` requires:
1. Parent screen finds `Ticket` as a subscreen → needs `Ticket.xml`
2. `Ticket.xml` finds `CreateTicket` as its subscreen → needs `Ticket/CreateTicket.xml`

Without the intermediate `Ticket.xml`, the path resolution fails silently (no content rendered).

**Directory structure:**
```
screen/
├── MyApp.xml
└── MyApp/
    ├── Ticket.xml              # ← Pass-through screen (REQUIRED)
    ├── Ticket/
    │   ├── CreateTicket.xml    # /MyApp/Ticket/CreateTicket
    │   ├── MyTickets.xml       # /MyApp/Ticket/MyTickets
    │   └── ViewTicket.xml      # /MyApp/Ticket/ViewTicket
    ├── Knowledge.xml           # ← Pass-through screen (REQUIRED)
    └── Knowledge/
        ├── SearchKnowledge.xml
        └── ViewArticle.xml
```

**Minimal pass-through screen:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd"
        require-authentication="true" allow-extra-path="true">

    <widgets><subscreens-active/></widgets>
</screen>
```

**Key attributes:**
- `allow-extra-path="true"` — allows child path segments (e.g., entity IDs) to pass through
- `<subscreens-active/>` — renders whichever child screen matches the next URL path segment

> **Common Gotcha**: If clicking navigation links renders no content in the `<subscreens-active/>` area, the most likely cause is a missing intermediate pass-through screen file.

### Anti-Patterns

```xml
<!-- WRONG: Subscreen directory name doesn't match parent -->
screen/
├── MyApp.xml
└── MyAppScreens/         <!-- Should be "MyApp/" -->

<!-- WRONG: No default-item specified -->
<subscreens>
    <!-- Should specify default-item -->
</subscreens>

<!-- WRONG: Duplicating always-actions in subscreens -->
<!-- Child screens inherit from parent - don't duplicate filter context setup -->

<!-- WRONG: Redundant SubscreensItem for auto-discovered screens -->
<!-- If the screen file is physically inside the parent's subscreen directory,
     it is auto-discovered. Do NOT add a SubscreensItem record in seed data. -->
<moqui.screen.SubscreensItem screenLocation="component://myapp/screen/MyApp/Section.xml"
    subscreenName="Detail" subscreenLocation="component://myapp/screen/MyApp/Section/Detail.xml"
    userGroupId="ALL_USERS" menuInclude="Y"/>
<!-- Detail.xml is already in Section/ directory — this record is redundant -->
```

### When SubscreensItem IS Needed

`SubscreensItem` seed data records are only needed when mounting a screen from a **different component** or a **non-standard location** — i.e., when the screen file is NOT inside the parent screen's auto-discovery directory.

```xml
<!-- CORRECT: Mounting a screen from another component into a parent -->
<moqui.screen.SubscreensItem screenLocation="component://webroot/screen/webroot/apps.xml"
    subscreenName="MyApp" subscreenLocation="component://my-component/screen/MyApp.xml"
    userGroupId="ALL_USERS" menuInclude="Y"/>
```

**Rule of thumb**: If `subscreenLocation` is a child path of `screenLocation`'s directory, the record is redundant. Use `default-menu-title` and `default-menu-index` attributes on the screen's `<screen>` element instead for menu control.

> **Gotcha: `menuInclude` must be explicit on entity records.** The `<subscreens-item>` XML element in screen definitions defaults `menu-include` to `true`, but the `SubscreensItem` **entity record** in seed data does NOT default `menuInclude` to `"Y"` — it defaults to null. A null `menuInclude` means the screen is accessible by URL but **invisible in navigation menus**. Always set `menuInclude="Y"` on `SubscreensItem` seed data records.

### Subscreen Best Practices

- Use `default-item` for landing page
- Use `menu-include="false"` for detail screens
- Organize `menu-index` with gaps (1, 10, 20) for easy insertion
- Keep always-actions in root screen only
- Use meaningful menu titles
