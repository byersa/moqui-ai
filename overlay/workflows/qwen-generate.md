---
description: Use local Qwen to generate Moqui/Quasar code based on a description.
---

This workflow leverages your local Qwen model to write actual Moqui artifacts.

1. Execute Generator script: `./runtime/component/moqui-ai/bin/qwen-generate.py "{{prompt}}"`
2. The code will be saved in `generations/latest_generation.js`. 
3. Review and refine before copying to the component screens.
