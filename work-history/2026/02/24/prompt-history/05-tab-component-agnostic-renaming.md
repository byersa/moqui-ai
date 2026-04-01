# Prompt History: Semantic Refactoring for Navigational Components

- **Timestamp**: 2026-02-24T18:33:00-07:00
- **User Prompts**: 
    - "tackle a housekeeping issue... macro names: q-tabs, q-tab and q-banner are not acceptable"
    - "suggest some alternatives without going and changing anything"
    - "lets go with banner... tabbar-page, tab-page and banner"

## Technical Analysis
The initial implementation of SPA navigational components used the `q-` prefix (e.g., `q-tabs`, `q-banner`), which is tightly coupled to the Quasar UI library. In Moqui's architecture, the XML Screen definition layer should ideally remain agnostic of the specific rendering technology. Hardcoding library prefixes into the XSD and FTL macros creates a "leaky abstraction" that would complicate future transitions to different frontend frameworks or UI kits.

Additionally, generic names like `<tabs>` risk confusion with Moqui's built-in `<subscreens-tabs>` (which are server-side structure driven), requiring names that suggest the navigational and structural context of a modern SPA.

## Decision & Rationale
1. **Semantic Naming Strategy**:
    - **`banner`**: Renamed from `q-banner`. Verified as a completely unreserved term in the Moqui ecosystem, providing a clean, semantic tag for informational callouts.
    - **`tabbar-page` and `tab-page`**: Renamed from `q-tabs` and `q-tab`. The `-page` suffix was chosen to explicitly signal that these elements manage the navigational structure of the current screen context, mapping directly to SPA routes and nested sub-pages.
2. **Platform Agnoticism**: By removing the library-specific prefix, the screen definitions now describe the **intent** (a tabbed page layout) rather than the **implementation** (a Quasar component).
3. **Atomic Refactor**: Updated the `moqui-ai-screen.xsd`, `MoquiAiScreenMacros.qvt2.ftl`, and `MoquiAiVue.qvt2.js` in a single operation to maintain system integrity and avoid breaking the existing "Active Meetings" UI implementations.

## Impact
- **Decoupled Architecture**: Screen definitions in `moqui-ai` and `aitree` are no longer syntax-locked to Quasar.
- **Improved Developer Intent**: The use of `tabbar-page` clearer distinguishes global/manual tab navigation from automatic subscreen tab generation.
- **Extensible Schema**: The addition of `<banner>` to the formal XSD provides a new standard widget available across the entire project.
