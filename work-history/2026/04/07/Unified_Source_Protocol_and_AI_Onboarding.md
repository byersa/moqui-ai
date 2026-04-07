# Work History: Unified Source Protocol & AI Co-Architect Onboarding
**Date**: 2026-04-07
**Context**: Finalizing MCE JSON-First Architecture & Guidance Flow

## Summary of Work
Transitioned the Aitree Blueprint Shell into a fully directive development environment by establishing JSON as the primary "Source of Truth" while maintaining legacy Moqui XML compatibility through an automated "Shadow Compiler."

### 1. Unified Source Protocol (Architecture)
- **Primary Source**: Enforced a architectural rule where `.json` blueprints are the authoritative source for screens, entities, and services. If a `.json` exists, `.xml` files are treated as disposable "shadows."
- **Solution Component Detection**: Updated `moquiai.ProjectServices.get#Components` to filter for active "solution" components (those containing at least one `.json` blueprint in their standard folders).
- **Shadow Compiler**: Refactored `BlueprintServices_saveBlueprint.groovy` to automatically generate a skeletal `.xml` shell ("shadow") in the same directory as any saved `.json` blueprint. This satisfies Moqui's screen-loader while allowing development to remain exclusively in JSON.
- **Screen Navigator**: Integrated a project-wide screen switcher in the MCE shell left drawer, enabling developers to jump between different blueprints in the same component.

### 2. AI Co-Architect Enhancements (AI Chat)
- **Empty-State Auto-Onboarding**: Implemented a mounting trigger in the shell that detects an empty blueprint structure and automatically sends an `INIT` prompt to the AI. The AI then proactively guides the user through the initial modeling steps.
- **Mantle-Aware Interviewing**: Refactored `WebMcpServices_postPrompt.groovy` to support entity-guided modeling. Mentioning an entity like "Person" or an intent like "Intake" now triggers a metadata-aware suggestion for common fields.
- **Bulk Injection Protocol**: Added an `addMultipleComponents` action to the MCE protocol, allowing the AI to populate an entire form block (e.g., First Name, Last Name, Birth Date) in a single turn.
- **Reactive UI Refreshes**: Updated the shell's prompt handler to auto-refresh the preview pane whenever the AI's response text mentions keywords like `suggest`, `inject`, or `model`, providing immediate visual feedback for architectural proposals.

### 3. Stability & Bug Fixes
- **Jackson Recursion Fix**: Resolved a critical 500 Server Error by renaming the `post#Prompt` service out-parameter from `result` to `aiResponse`, preventing an infinite JSON serialization nesting loop.
- **Vue Event Collision**: Fixed a bug where Vue's automatic passing of event objects to `@click` handlers was being misinterpreted as a "hidden" flag in the `sendMessage` function.
- **Service Mapping**: Corrected missing `out-parameters` in `WebMcpServices.xml` to ensure AI responses are actually transmitted to the client.
- **Groovy Syntax**: Resolved multiple syntax ambiguities in `ProjectServices_createComponent.groovy` by utilizing explicit Map casting (`as Map`).

## Verification
-   **Case**: Created `MedicalHistory.json`.
-   **Detection**: Screen Navigator correctly listed the new screen.
-   **Onboarding**: AI immediately asked for the intent.
-   **Modeling**: "Intake for Person" correctly suggested 3 fields.
-   **Injection**: The screen automatically updated to reflect the new structure.

---
**Next Steps**: 
- Implement a "Sync to Shadow" button for manual reconciliation.
- Add field-level validation based on Mantle entity definitions.
- Enhance the AI Chat with real-time access to the Mantle Entity Registry.
