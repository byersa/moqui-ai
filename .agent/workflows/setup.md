---
description: Initialize and anchor the project environment for developers.
---

This workflow bootstraps the local environment, configures script permissions, and creates root-level shortcuts for common tasks. It should be the FIRST command run by any developer after cloning the repository.

// turbo-all
1. **Initialize Framework**: Use the 'liftoff' skill to ensure Moqui framework and runtime are properly structured.
2. **Fix Permissions**: Ensure all project scripts are executable:
   `chmod +x runtime/component/moqui-ai/bin/*.sh`
3. **Create Shortcuts**: Create root-level symlinks for ease of use (ignored by git):
   `ln -sf runtime/component/moqui-ai/bin/ollama-start.sh ollama-start`
   `ln -sf runtime/component/moqui-ai/bin/ollama-stop.sh ollama-stop`
   `ln -sf runtime/component/moqui-ai/bin/qwen-load.sh qwen-load`
   `ln -sf runtime/component/moqui-ai/bin/qwen-unload.sh qwen-unload`
   `ln -sf runtime/component/moqui-ai/bin/analyze-session.py analyze-session`
   `ln -sf runtime/component/moqui-ai/bin/qwen-generate.py qwen-generate`
4. **Environment Check**: Verify if Ollama is installed:
   `ollama --version || echo "WARNING: Ollama not found. Please install it from ollama.com"`
5. **Model Check**: Ensure the required model is available:
   `ollama list | grep -q 'qwen2.5-coder:7b' || ollama pull qwen2.5-coder:7b`
6. **Report Status**: Confirm that the environment is anchored and ready for development.
