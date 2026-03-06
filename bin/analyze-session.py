#!/usr/bin/env python3
import sys
import json
import urllib.request
import datetime
import os

def analyze_session(text):
    prompt = f"""You are the Moqui Knowledge Librarian. Analyze the following developer workflow and extract a Knowledge Item. 
Format your response as a Markdown file with these sections:
# [Title]
## Context
## Problem
## Solution (Specific Moqui XML/Script patterns)
## Prevention (How to avoid re-doing this work)

Workflow text:
{text}"""

    data = {
        "model": "qwen2.5-coder:7b",
        "prompt": prompt,
        "stream": False
    }

    req = urllib.request.Request(
        "http://localhost:11434/api/generate",
        data=json.dumps(data).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )

    try:
        with urllib.request.urlopen(req) as res:
            response_data = json.loads(res.read().decode('utf-8'))
            result = response_data.get('response', 'No response from model.')
            
            timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"runtime/component/moqui-ai/.agent/knowledge/KI_{timestamp}.md"
            
            os.makedirs(os.path.dirname(filename), exist_ok=True)
            with open(filename, 'w') as f:
                f.write(result)
            
            print(f"Knowledge crystallized to: {filename}")
    except Exception as e:
        print(f"Error communicating with Ollama: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: ./analyze-session.sh 'text to analyze'")
        sys.exit(1)
    analyze_session(sys.argv[1])
