# Moqui Screen Blueprint Authoring Guide

**Purpose**: Guidelines for writing complete and actionable screen blueprints that enable correct XML generation on the first attempt.

---

## Required Blueprint Sections

Every screen blueprint MUST include the following sections with complete information:

### 1. Description with Architecture Pattern

**Include:**
- One-sentence purpose statement
- **Architecture Pattern** subsection showing parent/child screen relationships
- List all related screens and their roles

**Example:**
```markdown
## Description
ActiveScreens is the parent navigation screen with a header menu for managing active container instances.

**Architecture Pattern:**
- ActiveScreens → Parent navigation screen with subscreens-menu
- MeetingAgendaSplit → Child screen containing screen-split widget
- AgendaContainerSelectPage → Subscreen for selecting active containers
- MainInstancePage → Dynamically loaded once per active container
```

### 2. Subscreens Section

**MANDATORY for screens using `<subscreens-menu/>`:**

List EVERY subscreen with:
- **Name** - The subscreens-item name attribute
- **Target File** - The location path
- **Purpose** - One-line description of its role
- **Menu Visibility** - Note if `menu-include="false"` should be used

**Example:**
```markdown
## subscreens
- **SelectPage** → AgendaContainerSelectPage.xml (for selecting active containers)
- **MeetingAgendaSplit** → MeetingAgendaSplit.xml (contains screen-split widget)
  - Note: This is the default subscreen for content display
```

**Pitfall to Avoid:**
❌ Don't list subscreens without file paths
❌ Don't omit subscreens that contain widgets
✅ Do specify which subscreen is the content target vs navigation only

### 3. Widgets Section with Complete XML

**Include:**
- Complete XML structure (not snippets)
- All required attributes on every element
- Comments explaining non-obvious choices

**For screens with subscreens-menu, ALWAYS specify:**
```xml
<screen-layout>
    <screen-header>
        <label text="Screen Title" type="h5"/>
        <menu-item name="SubscreenName" text="Display Text" icon="icon_name"/>
        <subscreens-menu/>
    </screen-header>
    <screen-content>
        <subscreens-active/>
    </screen-content>
</screen-layout>
```

**Pitfall to Avoid:**
❌ Don't put complex widgets (screen-split, form-list) directly in parent with subscreens-menu
❌ Don't omit menu-item attributes (name, text, icon)
❌ Don't forget `<subscreens-menu/>` if navigation is needed
✅ Do create intermediate screens for complex widget structures

### 4. Screen-Split Usage (If Applicable)

**Specify which mode:**

**Dynamic Mode** (list-driven):
```xml
<screen-split name="split_name" model="30" horizontal="false"
              list="activeContainerIds" component="MainInstance"
              fail-message="No items selected"/>
```
- `list` - Pinia store expression returning array of IDs
- `component` - Screen name (resolves to {component}Page.xml)
- Each panel loads: `{component}?{parameterName}={id}`

**Static Mode** (inline widgets):
```xml
<screen-split model="30" horizontal="false">
    <container>Left panel content</container>
    <form-single>Right panel content</form-single>
</screen-split>
```
- Children distributed: first → before, remaining → after

### 5. Pinia Store Interactions

**Document all store dependencies:**
```markdown
## Vue and Quasar Integration

### pinia-store-interactions
- **aiTreeStore** - Reads `activeContainerIds` getter
- **aiTreeStore** - Calls `addActiveContainer(containerId)` mutation
- **aiTreeStore** - Calls `removeActiveContainer(containerId)` mutation
```

### 6. Implementation Checklist

**Add a checklist specific to the screen:**
```markdown
### Implementation Checklist
- [ ] Define all subscreens with location paths
- [ ] Specify menu-include for each subscreens-item
- [ ] Document Pinia store interactions
- [ ] Define transition for state changes
- [ ] Specify all widget attributes
- [ ] Define fail-state messages
```

---

## Common Blueprint Mistakes

### Mistake 1: Incomplete Subscreen Definitions

**Wrong:**
```markdown
## subscreens
MainInstancePage (content)
AgendaContainerSelectPage (content)
```

**Correct:**
```markdown
## subscreens
- **SelectPage** → AgendaContainerSelectPage.xml (for selecting containers)
- **MeetingAgendaSplit** → MeetingAgendaSplit.xml (contains screen-split widget)
```

### Mistake 2: Missing Intermediate Screens

**Wrong:** Blueprint shows screen-split directly in parent with subscreens-menu

**Correct:** Create intermediate screen (e.g., MeetingAgendaSplit.xml) that contains the screen-split widget, then reference it as a subscreen.

### Mistake 3: Incomplete Widget Specifications

**Wrong:**
```xml
<menu-item>
<screen-split target-screen-list="MainInstancePage">
```

**Correct:**
```xml
<menu-item name="SelectPage" text="Select Containers" icon="select_all"/>
<screen-split name="as_screen_split" model="30" horizontal="false"
              list="activeContainerIds" component="MainInstance"
              fail-message="No active containers selected."/>
```

### Mistake 4: Missing Navigation Structure

**Wrong:** No subscreens-menu, no menu-item, widgets directly in content

**Correct:** Explicit navigation hierarchy with subscreens-menu and menu-items in header

---

## Pre-Submission Checklist

Before finalizing a screen blueprint:

- [ ] **Architecture Pattern** clearly shows parent/child relationships
- [ ] **All subscreens** listed with file paths and purposes
- [ ] **Widgets section** has complete XML (not snippets)
- [ ] **Menu-items** have name, text, and icon attributes
- [ ] **Subscreens-menu** included if navigation is needed
- [ ] **Complex widgets** are in intermediate screens (not parent)
- [ ] **Pinia store** interactions documented
- [ ] **Fail states** specified (empty lists, errors)
- [ ] **Implementation checklist** included

---

## Related Documents

- `screen-syntax-checklist.md` - XML syntax validation
- `../standards/screen-naming.md` - Screen naming conventions
- `../references/screen-patterns.md` - Common screen patterns
- `../framework-guide.md` - Moqui screen fundamentals

---

## Discovery: How to Find Existing Patterns

When creating a new blueprint:

1. **Search existing screens** for similar patterns:
   ```bash
   find runtime/component -name "*.xml" -path "*/screen/*" | head -20
   ```

2. **Check parent screen** for subscreens structure:
   ```bash
   grep -A5 "<subscreens>" path/to/parent.xml
   ```

3. **Review blueprint directory** for related screens:
   ```bash
   ls blueprints/screen/*/
   ```

4. **Reference implementation**: Look at `runtime/component/aitree/screen/aitree.xml` for complete navigation pattern example

---

**Remember**: A complete blueprint enables correct XML generation on the first attempt. When in doubt, over-specify rather than under-specify.
