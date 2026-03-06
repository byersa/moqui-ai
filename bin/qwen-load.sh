#!/bin/bash
# Load the qwen2.5-coder:7b model into memory.
curl -s -X POST http://localhost:11434/api/generate -d '{"model": "qwen2.5-coder:7b"}' > /dev/null
echo "Model qwen2.5-coder:7b loading..."
ollama ps
