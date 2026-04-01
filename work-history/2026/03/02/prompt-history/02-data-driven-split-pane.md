# Prompt History: Data-driven Split-Pane Refinement
**Timestamp**: 2026-03-02T19:51:24Z

## User Prompt
The user requested a refinement of the `<screen-split>` macro. Instead of referencing a single logic component, it should dynamically iterate over a list of session IDs from a Pinia store (`useMeetingsStore().openSessionIds`) and render a parameterized embedded screen (`MainInstancePanel`).

## Technical Analysis
The existing `<screen-split>` was too monolithic. To support "split-screen awareness," the inner components needed to receive their own context (e.g., `agendaContainerId`). This required updates to the macro definition, the `m-screen-split` Vue component, and the `m-dynamic-container` helper to support attribute delegation.

## Decision & Rationale
1. **Refined `<screen-split>`**: Added `list`, `component`, and `fail-message` attributes to the macro and component.
2. **Prop Delegation in `m-dynamic-container`**: Updated the template to use `v-bind="$attrs"`, allowing parameters passed from the split-pane parent to reach the dynamically loaded Vue component.
3. **Centralized Store State**: Added `openSessionIds` to `meetingsStore.js` and updated the Hub and Activation dialogs to manipulate this list, ensuring reactive UI updates across the SPA.
4. **"Split-Screen Aware" Panel**: Created `MainInstancePanel.xml` which accepts parameters and fits within a flexible column-based layout.

## Impact
- Dynamic, data-driven multi-instance rendering.
- Robust state management for active meeting sessions.
- Reusable split-pane infrastructure for any ID-based component list.
