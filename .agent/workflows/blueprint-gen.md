---
description: Generate a Moqui artifact (XML/JS) from a Blueprint (Markdown).
---

This workflow leverages your local Qwen model to transform a design "Blueprint" into actual Moqui code.

// turbo
1. **Execute Generator**: Pass the blueprint path to the generator tool:
   `./blueprint-gen runtime/component/aitree/blueprints/path/to/blueprint.md`

2. **Verify Output**: The generator will save the resulting code to the corresponding Moqui directory (e.g., `runtime/component/aitree/screen/...`).

3. **Dry-Run Mode**: Use `--dry-run` to see the code without writing it to disk:
   `./blueprint-gen runtime/component/aitree/blueprints/path/to/blueprint.md --dry-run`

4. **Iterate**: If the output is incorrect, refine the blueprint and run the generator again.
