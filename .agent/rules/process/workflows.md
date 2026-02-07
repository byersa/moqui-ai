# Process: Workflows

5. **Skill Convention:** Skills must follow the folder-per-skill pattern: `.agent/skills/[name]/SKILL.md`.

6. **Skill Aliasing:** Every `SKILL.md` must include an 'alias' in the YAML frontmatter matching the `[name]` directory.

10. **Commands and aliases** should use base names only. The Agent is responsible for resolving extensions (`.md`, `.xml`, `.groovy`) based on the project's folder and naming conventions.

11. **Local changes** to AI artifacts and code must be treated as transient until pushed to GitHub. Use the `/git-sync [component]` skill to ensure that the remote repository stays in sync with the local development state.

12. The AI Agent is responsible for the health of the development environment. Commands like `/publish` must verify that the Moqui server is running, start it if necessary, and ensure the Architect's browser is pointed at the resulting work.

18. **Visible Verification:** The "Visible Verification Protocol" is mandatory. When the Agent performs a UI verification or deployment, it MUST launch the User's browser (using `xdg-open` or `open`) to the target URL. The Agent must NOT rely solely on internal headless checks; the novice user must see the result.

Rule 25 (Dev Mode vs Test Mode): The Agent must distinguish between Development and Testing contexts.
- **Dev Mode (Default):** Prioritize "Human-in-the-Loop" verification. The Agent should set up the environment (start server, clear cache), perform the change, and then *pause* to allow the User to visually verify the result (e.g., via `xdg-open`).
- **Test Mode:** When explicitly requested, the Agent may perform headless automated verification (Selenium/Puppeteer) for regression testing.
