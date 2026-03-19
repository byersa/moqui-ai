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

---

# Writing Effective Blueprints for AI Generation

*This document explains the strategy behind AI "Prompt Scaffolding" using the `blueprint-gen` orchestrator.*

## The "AI as Orchestrator" Pattern
Traditionally, an AI agent is asked to write code directly. However, in complex frameworks like Moqui, this leads to continuous iteration and hallucination (e.g., the AI guessing custom component names or omitting required namespaces). 

To solve this, we use the local AI (Qwen) strictly as an **Orchestrator**. The developer's job is not to write code, but to **write a highly specific Blueprint**. The `./blueprint-gen` tool automatically injects your Blueprint, the Moqui XML Schema (XSD), the business entities, and our custom UI macros into the AI's context.

## How to Write a Successful Blueprint
The goal of a blueprint is to provide enough **Explicit Scaffolding** so the AI gets the XML right on the *first try*. If you leave out the structural details, the AI will guess, and you will likely have to rewrite the artifact.

### 1. Explicitly Call Out Macros
Do not use vague terms like "make a tab section". Instead, tell the AI exactly which macro to use from our library:
- ❌ **Bad:** "Make a header with tabs for the subscreens."
- ✅ **Good:** "Use `<screen-layout>` with `<screen-header>`. Inside the header, use a `<bp-tabbar>` with tabs linking to the `ActiveMeetings` and `MeetingHistory` subscreens."

### 2. Specify Entities and Fields (CRUD Screens)
When writing a blueprint for a CRUD screen, you must explicitly name the database entity and the fields you want to view/edit. If using `<form-list>`, provide the exact column breakdown.
- ❌ **Bad:** "Show a list of the containers."
- ✅ **Good:** "Create a `<form-list>` named 'ContainerList'. Use `<entity-find entity-name="aitree.meeting.AgendaContainer" list="containerList">`. Display columns for `agendaContainerId`, `name`, and `statusId`."

### 3. Detail Triggers and Transitions
If a button opens a dialog or transitions to another screen, name the target transition explicitly.
- ❌ **Bad:** "Add an edit button."
- ✅ **Good:** "Add an Action column using a `<dynamic-dialog>` button with transition `EditAgendaContainerDialog`."

By treating the blueprint as a strict specification document rather than a casual chat prompt, you empower the AI to generate compiling, production-ready artifacts instantly, making junior developers exceptionally productive.

---

---
trigger: always_on
---

# Blueprint Template Standard

This rule defines the structure and usage of Screen Blueprint Markdown files to ensure consistent and high-quality generation of Moqui XML screens and Vue/Quasar components.

## Template Structure
Each Screen Blueprint (`.md` file) MUST mirror the structure of `xml-screen-3.xsd` and include the following key sections:

- **Header Properties**: Include attributes like `standalone`, `require-authentication`, etc.
- **Parameters**: Define expected input parameters.
- **Always Actions / Actions**: Describe data fetching and business logic.
- **Widgets**: The visual structure of the screen.
- **Vue / Quasar State**: Explicit mapping of `computed`, `watch`, and Pinia store interactions.
- **Test Plan**: Requirements for verifying screen functionality.
- **Test Results**: Observed behavior during verification.

## Content Guidelines
- **Default Content**: Every sub-element should default to "No content available" if not specified.
- **Transition Descriptions**: "transition" headers should include text descriptions of the actions to be taken.
- **Moqui Priority**: Always prioritize standard Moqui components and macros. Custom Vue components should only be used when standard Moqui components cannot achieve the desired result.
- **Refinement Cycle**: Use the `.md` file to refine the Vue/Quasar component files. The blueprint is the source of truth for both the XML structure and the specialized frontend logic.

## AI Generation Instructions
When generating XML from a blueprint:
1. Include `<parameter>` blocks for all inputs defined in the blueprint.
2. Include an `<actions>` block with the necessary `<entity-find>` or service calls to populate UI lists.
3. Map custom frontend requirements to the appropriate Moqui AI macros (e.g., `<form-query>`, `<form-list>`).

---

---
trigger: always_on
---

Protocol global-blueprint-v1:

Derive Path: Artifact path/to/file.xml maps to .agent/blueprint/path/to/file.md.

Consult First: Read the blueprint before any mkdir, touch, or code generation.

Update State: Check off requirements in the blueprint upon completion.
---

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

---

# MoquiAi Strategy: Blueprints & WebMCP Integration

## Executive Summary of Recent Conversations
This document summarizes the strategic evolution of the MoquiAi platform over the recent development cycle (February 2026), moving from a static rendering framework to an agentic, metadata-driven architecture.

### 1. Platform Taxonomy and Structure
The project established a clear separation between **Platform Components** (`moqui-ai`, `moqui-mcp`) and **Solution Components** (`huddle`, `aitree`). The `.agent` directory in `moqui-ai` was refactored to serve as a "compiled" source of truth for agent behavior and project strategy, moving beyond simple task lists.

### 2. The Birth of MoquiAi Blueprints
To solve the friction between server-side Moqui logic and modern Vue/Quasar frontends, the **MoquiAi Blueprint** system was developed. 
- **Core Engine:** `BlueprintClient.js` and the `DeterministicVueRenderer`.
- **Functionality:** Declarative XML tags (e.g., `<screen-layout>`, `<screen-header>`) are transformed into JSON-LD "Blueprints" on the server and rendered into reactive Quasar components on the client.
- **Outcome:** This provides a high-fidelity, SPA-like experience while maintaining the power of Moqui’s backend transition and security model.

### 3. Domain Model Evolution
Strategic work was performed on the meeting management system within the `aitree` and `huddle` solutions:
- **Agenda Management:** Transitioned to an EAV-style attribute model for `AgendaMessageAttribute`.
- **Meeting Navigation:** Implemented a two-level navigation system for "Active Meetings" vs. "Meeting History," keyed by `AgendaContainerID` and normalized with `fromDate`/`thruDate`.

### 4. Convergence: WebMCP & Blueprints
The most recent shift identifies **WebMCP** as the key to making MoquiAi "Zero-Day Agent Ready." By using Blueprints as the source of truth, the platform can automatically expose client-side tools to browser-native AI agents (like Edge Copilot or Gemini), allowing for seamless human-in-the-loop collaboration.

---

# Investigation Report: WebMCP & MoquiAi Blueprints

## Overview of WebMCP
[WebMCP](https://github.com/webmachinelearning/webmcp) is a proposed JavaScript API that allows web developers to expose their web application's functionality as "tools" to AI agents and assistive technologies directly within the browser. 

Unlike traditional out-of-band MCP integrations where an AI communicates directly with a backend server, WebMCP allows tools to execute on the *client side* within the context of the user's active session. This enables **human-in-the-loop collaborative workflows** where the human and the AI share the same visual interface and active state.

## Overview of MoquiAi Blueprints
Based on the codebase analysis, **MoquiAi Blueprints** (e.g., `BlueprintClient.js`) dynamically render UI components in the browser by transforming declarative JSON-LD representations (`ScreenBlueprint`, `FormList`, `FormField`, etc.) into reactive Vue/Quasar components. Blueprints serve as the bridge between Moqui's backend data definitions and the user's dynamic frontend experience.

## Potential Synergies and Integration Strategies

WebMCP is uniquely suited to complement the MoquiAi Blueprints architecture. Because blueprints already provide a dynamic, metadata-driven UI framework, they can easily be extended to automatically generate and register WebMCP tools for AI agents.

Here is how WebMCP could be used in conjunction with the "blueprints" work:

### 1. Declarative Tool Registration from Blueprints
Since the backend already sends structured JSON-LD blueprints to describe the UI, these blueprints could be extended to include WebMCP tool definitions. 
- **Implementation:** When `BlueprintClient.js` parses a `BlueprintNode`, it could look for an `@tools` or `agentCapabilities` property. It would then use the WebMCP API to register JavaScript functions that wrap the Vue component's methods.
- **Example:** A `FormList` showing active meetings could automatically expose a `filterMeetings(topic)` or `selectMeeting(id)` tool to the browser's AI agent.

### 2. Form and Action Automation
Moqui screens heavily rely on forms (`FormSingle`, `submit`, `text-line`). Filling out complex forms is a prime use case for AI assistance.
- **Implementation:** Whenever a form blueprint is rendered, a generic WebMCP tool like `fillForm(formId, data)` or `submitForm(formId)` could be exposed. 
- **Benefit:** Instead of the agent relying on brittle DOM actuation (guessing CSS selectors and typing), the agent can pass a structured JSON object directly to the client-side form manager, letting Vue handle the reactivity and validation, while the human watches the inputs populate in real-time.

### 3. Agent-Driven Navigation and Routing
Blueprints handle multi-level navigation (like the recent `SubscreensMenu` additions). 
- **Implementation:** Expose a `navigateBlueprint(route)` tool via WebMCP. 
- **Benefit:** If the user asks their browser agent, "Show me my past huddle meetings," the agent can invoke this tool directly. The Vue router will process the navigation, bringing both the human and the agent to the new blueprint screen concurrently.

### 4. Shared Context in "Human-in-the-Loop" Workflows
The WebMCP explainer highlights "creative" or "shopping" collaborative workflows. For MoquiAi applications like Huddle (which handles meeting agendas, observations, etc.), users and agents could collaborate.
- **Example Flow:** The user is looking at a `ScreenBlueprint` of a patient discussion. They ask the browser agent, "Summarize the last 3 meetings and draft a new agenda item." The agent fetches the data (via an exposed `getPastMeetings()` tool), drafts the text, and calls `addAgendaItemDraft(text)`. The Vue UI instantly updates with the drafted text, allowing the user to review, edit, and click "Save".

## Conclusion
Integrating WebMCP with MoquiAi Blueprints would bridge the gap between human-first UIs and agent-first APIs without duplicating effort. By adding WebMCP hook generation into the `BlueprintClient.js` renderer, **every Moqui screen built with blueprints would automatically become an interactive, agent-ready playground**. The AI interacts safely with the frontend architecture, while the user maintains final visual verification and control over the app state.

---

# App Questionnaire Template

**Goal:** This document serves as the high-level, human-readable input that a Voice Assistant or Business Analyst fills out. An Agentic AI (like Antigravity) will read this file and translate the answers into technical `moqui-ai` blueprints (Entities, Screens, Services).

---

## 1. Application Overview
**Q: What is the name of the application or module we are building?**
> [componentTitle]: 
> [componentName] (lowercase, no spaces): 

**Q: In one or two paragraphs, what is the primary business goal of this application?**
> 

## 2. Target Audience & Roles
**Q: Who will be using this application? (List the primary user personas/roles)**
> 

**Q: Are there any specific permissions or access restrictions? (e.g., "Only managers can approve", "HIPAA strict viewing")**
> 

## 3. Core Data Elements (Entities)
**Q: What are the main "things" or "nouns" this application needs to track? (e.g., Patients, Meetings, Invoices, Vehicles)**
> 

**Q: For each of those things, what kind of information do we need to store? (e.g., A Meeting needs a Date, a Title, and an Owner)**
> 

## 4. User Interface & Workflow (Screens)
**Q: What are the main screens or pages the user will navigate between? (e.g., Dashboard, Roster, Calendar)**
> 

**Q: Describe the primary workflow. When a user logs in, what is the first thing they see, and what action do they take?**
> 

**Q: Are there any specific UI requirements? (e.g., "Must work on mobile," "Needs a dashboard chart," "Requires a Discussion Tree")**
> 

## 5. Integrations & Business Logic (Services/Scripts)
**Q: Does this application need to talk to any existing systems or legacy databases?**
> 

**Q: Are there any complex calculations, automated emails, or background tasks that need to happen behind the scenes?**
> 

## 6. Constraints & Terminology
**Q: Are there any industry-specific terms, acronyms, or regulatory constraints the AI needs to be aware of?**
> 

---

# [Screen Name]

> **Instructions for AI**: When reading this blueprint to generate Moqui XML:
> - Prefer using standard UI widgets (`<form-single>`, `<form-list>`).
> - Use the `<form-query>` macro BEFORE `<form-list>` elements if list filtering or search is described in the `widgets` section.
> - Identify parameters mapped in the `form-query` and establish them in `parameter` sections or inputs.
## web-settings
No content available

## parameter
No content available

## always-actions
No content available

## pre-actions
No content available

## condition
No content available

## actions
No content available

## subscreens
No content available

## transition
No content available

## transition-include
No content available

## widgets
No content available

## fail-widgets
No content available

## macro-template
No content available

## Vue and Quasar Integration

### screen-events
No content available

### computed-fields
No content available

### watched-fields
No content available

### methods
No content available

### custom-data-fields
No content available

### pinia-store-interactions
No content available

## Quality Assurance

### test-plan
No content available

### test-result
No content available
