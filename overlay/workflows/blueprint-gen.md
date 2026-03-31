---
description: [slash-command] /generate: Generate a Moqui artifact (XML/JS) from a Blueprint (Markdown) using the selected AI model.
---

This workflow leverages the selected AI model to transform a design "Blueprint" (Spec) into actual Moqui code artifacts. The generator defaults to the current model specified in the environment.

// turbo
1. **Execute Generator**: Use the `blueprint-gen.py` script to transform the spec into code.
   `python3 runtime/component/moqui-ai/bin/blueprint-gen.py [spec-path]`

2. **Verify Output**: The generator will save the resulting code to the corresponding Moqui directory. For example:
   `/runtime/component/aitree/overlay/spec/screen/aitree/Meetings.md` -> `/runtime/component/aitree/screen/aitree/Meetings.xml`

3. **Dry-Run Mode**: Use `--dry-run` to see the code without writing it to disk:
   `python3 runtime/component/moqui-ai/bin/blueprint-gen.py [spec-path] --dry-run`

4. **Iterate**: If the output is incorrect, refine the Markdown spec and re-run this command.

> [!TIP]
> This command supports both the legacy `/blueprints/` directory and the new **`/overlay/spec/`** structural alignment.
