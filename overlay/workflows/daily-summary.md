---
description: Use local Qwen to summarize the current session and store knowledge.
---

This workflow leverages your local Qwen model to analyze our progress and create a permanent Knowledge Item so I never forget what we've solved.

1. Execute Python analysis script: `./runtime/component/moqui-ai/bin/analyze-session.py "SUMMARY_OF_TODAY"`
2. Verify the new Knowledge Item in `runtime/component/moqui-ai/.agent/knowledge/`.
