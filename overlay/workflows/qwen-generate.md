---
description: [slash-command] /q-gen: Use local Qwen to generate Moqui/Quasar code snippets from description.
---

This workflow leverages your local Qwen model to write snippet-based Moqui/Quasar CSS/JS artifacts.

1. **Execute Snippet Generator**:
   `python3 runtime/component/moqui-ai/bin/qwen-generate.py "{{prompt}}"`
2. **Review Output**: The code is generated in `generations/latest_generation.js`.
3. **Verify and Integrate**: Copy the valid snippet into your target screen or stylesheet.
