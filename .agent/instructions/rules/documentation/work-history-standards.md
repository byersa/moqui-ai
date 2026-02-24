# Standards: Work History & Prompt Logging

All components in the Moqui AI ecosystem must maintain a structured, developer-oriented `work-history`. This ensures that tactical decisions—especially those involving security-functionality tradeoffs—are transparent to future maintainers and agents.

## Directory Structure
```
.agent/work-history/YYYY/MM/DD/
├── summary.md            # High-level overview of the day's accomplishments.
└── prompt-history/       # Technical log of significant prompt/response cycles.
    ├── 01-feature-x.md
    └── 02-bug-fix-y.md
```

## summary.md Requirements
- **Objective**: The primary goal of the session.
- **Changes Made**: Bulleted list of components/files modified.
- **Impact**: What was achieved (e.g., "SPA now loads without 404s").

## prompt-history Requirements
Every "significant" prompt (one that shifts architecture, addresses a complex bug, or changes strategy) must be logged with:
- **Timestamp**: ISO format.
- **User Prompt**: A summary of the user's intent or the specific error reported.
- **Technical Analysis**: Brief explanation of the root cause or design challenge.
- **Decision & Rationale**: Why a specific path was chosen (e.g., "Tactical bypass of authz to unblock UI logic").
- **Impact**: How it changed the system state.

## Component Separation (Multi-Component Repos)
Crucially, when working across multiple components (e.g., `moqui-ai` and `aitree`/`huddle`), the `work-history` must be separated logically between them:
- Changes affecting the **Platform/Framework** (macros, XSDs, UI libraries, generic SPA shells) belong ONLY in `moqui-ai/.agent/work-history/`.
- Changes affecting the **Solution/Application** (app-specific screens, business logic fixes, DB queries, UI implementations of macros) belong ONLY in `[AppName]/.agent/work-history/`.
- Do not duplicate `prompt-history` or `summary.md` entries identically across multiple components unless a prompt fundamentally affects the abstract goals of both environments equally.

## Strategic Documentation
When a strategic decision is made (like the current documentation standard), it should also be codified into an architectural rule within `.agent/instructions/rules/`.
