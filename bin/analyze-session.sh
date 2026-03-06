#!/bin/bash
# analyze-session.sh - Uses local Qwen to extract Knowledge Items from logs.

INPUT_TEXT="$1"
if [ -z "$INPUT_TEXT" ]; then
  echo "Error: No input text provided."
  exit 1
fi

PROMPT="You are the Moqui Knowledge Librarian. Analyze the following developer workflow and extract a Knowledge Item. 
Format your response as a Markdown file with these sections:
# [Title]
## Context
## Problem
## Solution (Specific Moqui XML/Script patterns)
## Prevention (How to avoid re-doing this work)

Workflow text:
$INPUT_TEXT"

# Escape the prompt for JSON
ESCAPED_PROMPT=$(echo "$PROMPT" | jq -Rs .)

curl -s -X POST http://localhost:8080/mcp -d "{}" # Ensure service is alive check is optional here but we use ollama api directly below
# Using Ollama API directly for the LLM call
RESPONSE=$(curl -s -X POST http://localhost:11434/api/generate -d "{
  \"model\": \"qwen2.5-coder:7b\",
  \"prompt\": $ESCAPED_PROMPT,
  \"stream\": false
}")

# Extract response field from JSON
RESULT=$(echo "$RESPONSE" | jq -r '.response')

# Save to the agent knowledge folder
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="runtime/component/moqui-ai/.agent/knowledge/KI_${TIMESTAMP}.md"
echo "$RESULT" > "$FILENAME"

echo "Knowledge crystallized to: $FILENAME"
