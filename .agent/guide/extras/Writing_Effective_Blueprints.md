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
