# MoquiAi Front End Strategy

**Goal**: Make the front and backend of MoquiAi more usable by AI to create client-side artifacts efficiently, leveraging modern reactive frameworks.

## 1. Vue & Quasar Subcomponents
Moqui's traditional macro-based XML rendering is powerful but can be rigid. We will pivot to a **Component-First** architecture.
-   **Why?** AI struggles with 500-line XML files. It excels with small, self-contained Vue components.
-   **Strategy**:
    -   **Atomic Design**: Break UI into atoms (Buttons), molecules (Form Fields), and organisms (Data Grids).
    -   **Quasar Framework**: Use Quasar as the bedrock. It provides a massive library of high-quality, responsive components (Tables, Dialogs, Trees) out of the box.
    -   **Smart Wrappers**: Create "Moqui-Aware" wrappers.
        -   `<moqui-date-picker>` -> Wraps `QDate` + handles Moqui date formatting + timezones.
    -   **Event-Driven**: Move away from `onclick="..."` strings. Use Vue events (`@save`, `@cancel`) for cleaner logic separation.

## 2. Client-Side Execution Context (`jsec`)
We need a client-side equivalent to the server's `ec` (Execution Context).
-   **Name**: `jsec` (JavaScript Execution Context)
-   **Purpose**: A global, reactive object available to all components.
-   **Structure**:
    ```javascript
    const jsec = {
      user: { ... },          // User profile, preferences
      l10n: { ... },          // Localization helpers
      router: { ... },        // Smart navigation (history, params)
      agent: { ... },         // Interface to the AI Agent (sendPrompt, etc.)
      bus: { ... },           // Global Event Bus (e.g., "refresh-data")
      cache: { ... }          // Client-side data caching
    };
    ```
-   **Implementation**: Likely a Pinia Store or a dedicated reactive singleton in Vue 3.

## 3. The "Known" Component Repository
To prevent the AI from hallucinating broken UI code, we establish a **Source of Truth**.
-   **The Manifest**: A JSON/Markdown index of all available components.
    -   `MoquiInput`: "Use for text fields.Props: label, modelValue."
    -   `MoquiGrid`: "Use for lists. Props: entity-name, fields."
-   **Usage**: When the AI generates a screen, it *must* consult this repository. "I need a date picker. Checking inventory... Found `MoquiDatePicker`. I will use that."
-   **Docs**: Auto-generated "Storybook" style documentation for both humans and agents.

## 4. Bridge to Legacy
-   **Hybrid Mode**: We can embed these new Vue components inside legacy FTL macros using `createApp().mount()`, allowing a gradual migration.
