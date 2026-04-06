# Work History - 2026-04-05 (Morning)

## Native Blueprint Compilation & In-Place DSL Injection

Migrated the blueprint compilation process from external shell/Python scripts to a native Moqui service (`moquiai.SpecServices.compile#MarkdownToXml`). This transformation is now the source of truth for generating functional screens from Markdown specifications.

### Key Deliverables
- **Native Service**: Implemented `SpecServices_compileMarkdownToXml.groovy` to perform in-place Groovy DSL injection into Markdown spec files.
- **Polyglot Bridge**: Created `runtime/component/aitree/screen/aitree/Meetings.xml` as a "Bridge" that dynamically extracts and executes the ` ```groovy ` block from `Meetings.md` via `ec.script.run`.
- **Infrastructure**: Configured the `GeneralRunner` to handle the new service noun and ensured CORS whitelisting for standalone MCE access.
- **Cleanup**: Archived legacy `compilemd.sh` and `blueprint-gen.py` scripts to `bin/archive/`.

### Transformation Logic
1. **Extraction**: Resolves the Markdown title (`#`) and macro patterns (`[macro: name]`) against the `RegistryServices.get#EffectiveRegistry`.
2. **Translation**: Maps Markdown tags (e.g., `<subscreens-menu>`) and macros to valid Groovy Screen DSL method calls (e.g., `subscreens_menu()`).
3. **Injection**: Identifies or creates a ` ```groovy ` block in the source MD and overwrites it with the synthesized DSL.
4. **Execution**: The `.xml` bridge file ensures that the web pathing (`/aitree/Meetings`) triggers the execution of the DSL block within the `.md` specimen.

### Verification
- Confirmed transformation success via REST API.
- Verified in-place injection into `runtime/component/aitree/overlay/spec/screen/aitree/Meetings.md`.
- Validated server stability after a hard restart and log cleanup.
