# Moqui-AI: Comprehensive Blueprint Authoring Guide & System Prompt

This document serves as the master specification for creating **Blueprints** within the Moqui-AI framework. It combines all architectural guidelines, templates, and best practices to enable an AI (like Gemini) to generate high-quality, actionable Blueprint Markdown files that describe a new application or screen.

---

## 1. The Strategy: AI as Orchestrator

Traditionally, AI agents are asked to write code directly. In complex frameworks like Moqui, this often leads to hallucinations or incorrect component usage. 

**The Solution:** Use the AI strictly as an **Orchestrator**. 
- The developer (or an upstream AI) writes a **Highly Specific Blueprint** (Markdown).
- The `./blueprint-gen` tool then transforms this Blueprint into Moqui XML and Vue/Quasar code by injecting schemas (XSD), business entities, and UI macros into the generation context.

**Goal:** Provide enough **Explicit Scaffolding** so the code generator succeeds on the *first try*.

---

## 2. Blueprint Template Standard

Every Screen Blueprint (`.md` file) MUST follow a consistent structure. If a section is not used, it should state "No content available".

### Core Structure (Markdown Headers)
1. `# [Screen Name]`
2. `## web-settings` (Standalone, require-authentication, etc.)
3. `## parameter` (Expected input parameters)
4. `## always-actions` / `## actions` (Data fetching, business logic, `<entity-find>`)
5. `## pre-actions` / `## condition`
6. `## subscreens` (Crucial for navigation/layouts)
7. `## transition` / `## transition-include`
8. `## widgets` (The visual structure using Moqui and custom macros)
9. `## fail-widgets`
10. `## macro-template`
11. `## Vue and Quasar Integration` (Pinia store, computed, watch, methods)
12. `## Quality Assurance` (Test plan and results)

---

## 3. Detailed Authoring Guidelines

### A. Explicit Architecture Patterns
Always describe the parent/child relationship and the role of the screen.
- **Example:** `ActiveScreens` (Parent) -> `MeetingAgendaSplit` (Child with split-view) -> `MainInstancePage` (Dynamic loader).

### B. Mandatory Subscreen Definitions
For screens using `<subscreens-menu/>`, you MUST list EVERY subscreen with:
- **Name**: The `subscreens-item` name.
- **Target File**: The `.xml` file path.
- **Purpose**: A one-line description.
- **Menu Visibility**: Whether it should be included in the menu.

### C. Widgets & Macros
Do not use vague descriptions. Call out specific Moqui components or AI Macros:
- **Use**: `<screen-layout>`, `<screen-header>`, `<screen-content>`, `<form-list>`, `<form-query>`, `<screen-split>`, `<bp-tabbar>`.
- **Constraint**: Complex widgets (like `screen-split` or `form-list`) should often be placed in their own intermediate screens rather than directly in a parent that has a navigation menu.

### D. CRUD & Entities
When describing CRUD:
- Name the **database entity** explicitly.
- List the exact fields for queries and forms.
- If using `<form-list>`, define the columns.

### E. Pinia & State
Document all interactions with the frontend store:
- Which store is being accessed (e.g., `aiTreeStore`).
- Which getters are read.
- Which actions/mutations are called.

---

## 4. Common Pitfalls to Avoid

- ❌ **Ambiguity**: Avoid saying "Add a search bar". Say "Add a `<form-query>` section acting on the `Meeting` entity".
- ❌ **Missing XML Attributes**: Always specify names, labels, and icons for menu items.
- ❌ **Direct Complexity**: Avoid putting complex widgets directly in a parent screen that manages a subscreen menu. Use intermediate "page" screens.
- ❌ **Ignoring Schema**: The blueprint is the source of truth for the `xml-screen-3.xsd` mapping. Ensure parameters match what actions expect.

---

## 5. Blueprint Checklist (Pre-Generation)

Before considering a blueprint finished, ensure:
- [ ] **Architecture Pattern** is clear.
- [ ] **All subscreens** have file paths and purposes.
- [ ] **Widgets** section contains explicit Moqui/Macro XML tags where possible.
- [ ] **Transition** targets are named.
- [ ] **Pinia interactions** are listed.
- [ ] **Implementation checklist** is included at the end.

---

## 6. Execution Workflow

Once the Blueprint is created:
1. **Locate**: Save in the `blueprints/` folder of your component (e.g., `runtime/component/aitree/blueprints/`).
2. **Generate**: Run `./blueprint-gen path/to/blueprint.md`.
3. **Verify**: Check the generated XML for correctness.
4. **Refine**: If errors occur, update the **Blueprint**, not the code. Re-run the generator.

---

## 7. Sample Blueprint Header Template

```markdown
# [Screen Name]

> **Instructions for AI**: When reading this blueprint to generate Moqui XML:
> - Prefer standard UI widgets (<form-single>, <form-list>).
> - Use <form-query> macro BEFORE <form-list> for search/filtering.
> - Identify parameters mapped in form-query and establish them in parameter sections.

## Description
[One sentence purpose]

**Architecture Pattern:**
- [Parent] -> [Child]

## parameter
- name: [paramName], type: [String|Timestamp|etc], required: [true|false]

## actions
<entity-find entity-name="[EntityName]" list="[listName]">
    <econdition field-name="[fieldName]" from="[paramName]" ignore-if-empty="true"/>
</entity-find>

## subscreens
- **[Name]** -> [FileName].xml ([Purpose])

## widgets
<screen-layout>
    <screen-header>
        <subscreens-menu/>
    </screen-header>
    <screen-content>
        <subscreens-active/>
    </screen-content>
</screen-layout>
```
