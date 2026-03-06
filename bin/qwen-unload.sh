#!/bin/bash
# Unload the qwen2.5-coder:7b model from memory.
curl -s http://localhost:11434/api/generate -d '{"model": "qwen2.5-coder:7b", "keep_alive": 0}' > /dev/null
echo "Model qwen2.5-coder:7b unloading..."
ollama ps
