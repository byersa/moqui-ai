# Prompt History: Macro-based Screen Hierarchy Refactoring
**Timestamp**: 2026-03-02T16:21:24Z

## User Prompt
The user wanted to re-establish the standard Moqui screen hierarchy (using specialized `aitree` sub-screens) while introducing a modern split-pane experience for active sessions, avoiding hardcoded `@` syntax and `<render-mode>` blocks in widgets.

## Technical Analysis
The challenge was to allow Moqui XML screens to define their layout using high-level blueprint tags (`<screen-layout>`, `<screen-header>`, etc.) while automatically loading associated Vue logic by convention. This required extending the `MoquiAiScreenMacros.qvt2.ftl` and updating the screen structure in `aitree.xml` and `ActiveMeetings.xml`.

## Decision & Rationale
1. **Introduction of `<custom-screen>` macro**: Designed to load `.qvt.js` files by convention (e.g., `ActiveMeetings.qvt.js` for `ActiveMeetings.xml`) from the same directory, simplifying the XML definition.
2. **Standardization of `<screen-split>`**: Initially implemented as a component-based layout for the split-pane view.
3. **Screen Hierarchy Cleanup**: Simplified the `aitree` menu and ensured direct navigation to specialized sub-screens.

## Impact
- Cleaner XML screen definitions.
- Automatic script loading by convention.
- Consistent UI layout across the Meetings module.
