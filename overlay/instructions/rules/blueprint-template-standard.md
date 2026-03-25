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
