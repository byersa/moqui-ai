# AG Workflows: Your "Standard Operating Procedures"

Workflows are the most powerful tool for organizing and speeding up development. Think of them as **Checklists on Steroids**.

## 1. What is a Workflow?
A workflow is a markdown file (e.g., `.agent/skills/workflows/new-feature.md`) that documents a repeatable process. It serves two purposes:
1.  **Documentation**: Tells *you* (the human) the steps.
2.  **Instruction**: Tells *me* (the agent) exactly what to do, step-by-step.

## 2. Speeding Up Work: "Turbo Mode"
Instead of chatting back and forth for 20 minutes ("Create the file", "Review it", "Add tests"), you trigger a workflow with one command:
> "Run the `new-feature` workflow for 'Customer Loyalty Points'."

The agent then:
1.  Reads `new-feature.md`.
2.  Executes step 1 (Create directory).
3.  Executes step 2 (Scaffold files).
4.  Executes step 3 (Add placeholder tests).
5.  Pauses *only* when the workflow says "Ask user for review."

**Result**: A 20-minute chat becomes a 30-second prompt and a coffee break while the agent does the grunt work.

## 3. Organizing Work: "Mental Load Offloading"
You don't need to remember every step of your complex deployment process.
- **Before**: "Wait, did I update the version number? Did I run the linter? Did I check the database migration?"
- **After**: The `deploy.md` workflow has all those checks built-in. You just say "Deploy."

## 4. Examples
- **`bug-fix.md`**:
    1.  Create reproduction test case (fail).
    2.  Analyze code.
    3.  Apply fix.
    4.  Run test case (pass).
- **`onboard-developer.md`**:
    1.  Check for required tools (Java, Node).
    2.  Clone repos.
    3.  Setup local DB credentials.

## 5. Recursive Power
Workflows can call other workflows. A `release-product.md` workflow might call `run-tests.md`, then `build-artifacts.md`, then `deploy-to-staging.md`.
