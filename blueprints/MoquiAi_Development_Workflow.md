# MoquiAi Development Workflow: Prompts vs. Code

**"Code is the final product. Prompts are the source of truth."**

This document defines how we manage the relationship between High-Level Instructions (Prompts) and Low-Level Implementation (Source Code) without forcing a "Hands Off" policy.

## 1. The Core Philosophy: "No Hands Off"
You are a developer. You *must* be able to touch the code.
-   **Direct Manipulation**: You can open `Auth.vue` and hack away at a bug, tweak a CSS value, or optimize a loop.
-   **The Agent's Role**: The agent is not a "Gatekeeper" that forbids manual edits. The agent is a **Collaborator** that needs to be kept in the loop.

## 2. The Cycle: "Generate -> Iterate -> Reflect"

We solve the drift between Prompts and Code with a **Reverse Synchronization** step.

### Step 1: Generate (Prompt -> Code)
You write a high-level instruction in `.agent/instructions/features/auth.md`:
> "Implement a login form with JWT support and a 'Forgot Password' flow."

The Agent reads this and generates:
-   `service/auth/LoginServices.groovy`
-   `screen/auth/Login.vue`

### Step 2: Iterate (Manual Code Editing)
You run the app and realize the login button is off-center, or the error handling is too aggressive.
-   You manually edit `Login.vue`.
-   You manually tweak `LoginServices.groovy`.
-   **State**: The Code is now *ahead* of the Instruction. The Instruction `auth.md` is slightly stale.

### Step 3: Reflect (Code -> Prompt)
This is the missing link. When you are happy with your manual changes, you run a **"Reflect"** task:
> "Agent, I've finalized the Login logic. **Update the instructions to match my code.**"

The Agent:
1.  Reads your modified `Login.vue` and `LoginServices.groovy`.
2.  updates `auth.md` to reflect the new reality (e.g., adding "Error handling must be non-blocking").

## 3. Directory Management
-   **Source Code**: Lives in standard Moqui directories (`screen/`, `service/`). This is what runs.
-   **Instruction Code**: Lives in `.agent/instructions/`. This is what *explains*.

## 4. Practical Implementation
We can tag code to link it back to instructions.

```javascript
// @instruction: .agent/instructions/features/auth.md
// The following logic handles JWT token refresh...
function refreshToken() { ... }
```

By maintaining this link, the Agent knows exactly which instruction file to update when you ask for a "Reflect".

## Summary
-   **Can I write code?** Yes.
-   **Do I have to write prompts?** Yes, for big features.
-   **How do I keep them in sync?** Use the **Reflect** workflow to pull code "back up" into the prompt layer.
