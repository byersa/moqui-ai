# MoquiAi Roadmap & Vision

## Goal
Create a tool ASAP that is ready to be used by someone other than the author.
Make MoquiAi the successor to Moqui by making it a hybrid of Moqui and agent AI.

## Key Topics

### 1. General Direction Changes
- MoquiAi = Moqui + Agent AI (Hybrid)
- Translator code: Convert OFBiz and Moqui apps to MoquiAi apps.
- Divergence from Moqui:
    - Changes in screen tags.
    - Inclusion of AI as part of generated apps.
    - Enhanced use of client-side components.
- Automated Marketing Artifacts:
    - Articles, SEO parameters, license, commerce type (buy, SAAS), etc.

### 2. Revamp .agent Directory
- Goal: Make it most helpful to a user.
- Structure:
    - README.md
    - Documentation folder.
- Concept: Treat "instructions to AG" (app programming & environment programming) as "code".
    - Accumulate instructions into files that can be "compiled" to produce the app.
    - Include installation instructions.
- Configuration:
    - How/where to specify local params (DB credentials, user prefs).
- Artifact Integration:
    - Incorporate standard AG artifacts: `task.md`, implementation plans, walkthroughs.
    - Workflow usage: Are they skills with subskills?
    - Task complexity: Can AG be instructed to make tasks smarter?

### 3. Front End Usability
- Goal: More usable by AI to create client-side artifacts efficiently.
- Strategy:
    - Use Vue/Quasar subcomponents for events/modern client code (addressing Moqui's lag).
    - Explore client-side equivalent to server "execution context" (jsec?).
    - Repository of known JS components.

### 4. AI Modules ("Skills")
- Plug-in skills for client or server-side code.
- Treated as a library (attributes: API, documentation, etc.).

### 5. Repositories
- `moqui-ai` (Core)
- `moqui-ai-base-app`
- `moqui-ai-example`
