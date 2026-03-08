#!/usr/bin/env python3
import sys
import json
import urllib.request
import os

def generate_code(prompt):
    # Context gathering
    xsd_path = "runtime/component/moqui-ai/xsd/moqui-ai-screen.xsd"
    macro_path = "runtime/component/moqui-ai/template/MoquiAiScreenMacros.qvt.ftl"
    
    context = ""
    if os.path.exists(xsd_path):
        with open(xsd_path, 'r') as f:
            context += f"### XSD Definition:\n{f.read()}\n\n"
    
    if os.path.exists(macro_path):
        with open(macro_path, 'r') as f:
            context += f"### FreeMarker Macros:\n{f.read()}\n\n"

    # Add example of how components are registered in Moqui
    context += """### Example Component Pattern (from MoquiAiVue.js):
moqui.webrootVue.component('m-menu-item', {
    props: { href: String, text: String, icon: String },
    template: '<q-btn flat no-caps color="white" :to="href" :icon="icon" :label="text"></q-btn>'
});
"""

    full_prompt = f"""You are a Moqui/Quasar Specialist AI. Your task is to generate a new Vue component for the MoquiAi framework.

{context}

### User Request:
{prompt}

### Output Requirement:
1. Provide the Vue component JavaScript (using `moqui.webrootVue.component` pattern).
2. Use Quasar v2 components (e.g., q-card, q-btn, q-list, q-item).
3. Follow the semantic structure defined in the XSD if applicable.
"""

    data = {
        "model": "qwen2.5-coder:7b",
        "prompt": full_prompt,
        "stream": False
    }

    req = urllib.request.Request(
        "http://localhost:11434/api/generate",
        data=json.dumps(data).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )

    print("Generating code using local Qwen model...")
    try:
        with urllib.request.urlopen(req) as res:
            response_data = json.loads(res.read().decode('utf-8'))
            result = response_data.get('response', 'No response from model.')
            
            # Save to a temporary generations file
            os.makedirs("generations", exist_ok=True)
            output_file = "generations/latest_generation.js"
            with open(output_file, 'w') as f:
                f.write(result)
            
            print(f"Code generated successfully! Saved to: {output_file}")
            print("\n--- GENERATED CODE ---\n")
            print(result)
    except Exception as e:
        print(f"Error communicating with Ollama: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: ./qwen-generate.py 'prompt description'")
        sys.exit(1)
    generate_code(sys.argv[1])
