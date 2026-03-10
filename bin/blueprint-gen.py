#!/usr/bin/env python3
import sys, os, json, urllib.request, argparse, re

def read_file(path):
    if os.path.exists(path):
        with open(path, 'r') as f:
            return f.read()
    return ""

def get_target_path(blueprint_path):
    abs_bp = os.path.abspath(blueprint_path)
    if "blueprints" not in abs_bp:
        return None
    target = abs_bp.replace("/blueprints/", "/")
    base, ext = os.path.splitext(target)
    content = read_file(blueprint_path)
    if ".js" in content.split("\n")[0].lower():
        return base + ".js"
    return base + ".xml"

def get_sibling_blueprints(blueprint_path):
    directory = os.path.dirname(blueprint_path)
    siblings = []
    if os.path.exists(directory):
        for f in os.listdir(directory):
            if f.endswith(".md") and os.path.join(directory, f) != blueprint_path:
                siblings.append(f"SIB-BLUEPRINT ({f}):\n{read_file(os.path.join(directory, f))}")
    return "\n\n".join(siblings)

def generate_artifact(blueprint_path, dry_run=False, quiet=False, limit=None):
    target_path = get_target_path(blueprint_path)
    if not target_path:
        print(f"Error: Path '{blueprint_path}' is not inside a 'blueprints' directory.")
        sys.exit(1)
        
    # Context Gathering
    xsd_path = "runtime/component/moqui-ai/xsd/moqui-ai-screen.xsd"
    macro_path = "runtime/component/moqui-ai/template/MoquiAiScreenMacros.qvt.ftl"
    entities_path = "runtime/component/aitree/entity/AiTreeEntities.xml"
    
    context = ""
    context += f"### XML Schema (XSD):\n{read_file(xsd_path)}\n\n"
    context += f"### Available UI Macros (FTL):\n{read_file(macro_path)}\n\n"
    context += f"### Business Entities (Moqui XML):\n{read_file(entities_path)}\n\n"
    
    # Recursive Context (Siblings)
    context += "### Related Blueprints (for Navigation/Structure):\n"
    context += get_sibling_blueprints(blueprint_path) + "\n\n"
    
    blueprint_content = read_file(blueprint_path)
    
    golden_sample = """
### Golden Sample Mapping:
BLUEPRINT:
# Meetings Screen
Header showing "Staff Meeting" with a tabbar below.
- Header: label "Staff Meeting"
- Content: bp-tabbar with tabs for "ActiveMeetings" and "MeetingHistory"
- Actions: Find all AgendaContainer records into list "containerList".

OUTPUT (XML):
<screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:noNamespaceSchemaLocation="component://moqui-ai/xsd/moqui-ai-screen.xsd" require-authentication="false">
    <parameter name="name"/>
    <actions>
        <entity-find entity-name="aitree.meeting.AgendaContainer" list="containerList">
            <search-form-inputs default-order-by="name"/>
        </entity-find>
    </actions>
    <subscreens default-item="ActiveMeetings">
        <subscreens-item name="ActiveMeetings" location="component://aitree/screen/aitree/ActiveMeetings.xml"/>
        <subscreens-item name="MeetingHistory" location="component://aitree/screen/aitree/MeetingHistory.xml"/>
    </subscreens>
    <widgets>
        <screen-layout id="meetings-layout">
            <screen-header id="h-meetings">
                <label text="Staff Meeting" type="h5"/>
                <bp-tabbar id="m-tabs">
                    <bp-tab name="ActiveMeetings" text="Active Meetings" icon="play_arrow"/>
                    <bp-tab name="MeetingHistory" text="History" icon="history"/>
                </bp-tabbar>
            </screen-header>
            <screen-content id="c-meetings">
                <form-query name="Search">
                    <form-query-field name="name" label="Name"/>
                </form-query>
                <subscreens-active/>
            </screen-content>
        </screen-layout>
    </widgets>
</screen>
"""

    # Try to extract component name and URL
    component_name = "unknown"
    component_url = "unknown"
    if target_path and "runtime/component/" in target_path:
        parts = target_path.split("runtime/component/")[1].split("/")
        component_name = parts[0]
        component_relative_path = "/".join(parts[1:])
        component_url = f"component://{component_name}/{component_relative_path}"

    prompt = f"""You are a Moqui/Quasar Specialist AI. Your task is to generate valid Moqui XML or JavaScript artifacts from the provided Blueprint requirements.

{context}
{golden_sample}

### TARGET BLUEPRINT TO TRANSFORM:
Blueprint Source: {blueprint_path}
Target Output Path: {target_path}
Target Moqui Component URL: {component_url}
Component Name: {component_name}

Content:
{blueprint_content}

### CRITICAL INSTRUCTIONS:
1. Output ONLY the code for the artifact. No markdown explanations.
2. Ensure the XML is well-formed and follows the XSD exactly.
3. Use the provided UI Macros (screen-layout, screen-header, screen-content, bp-tabbar, bp-tab, etc.).
4. Use <subscreens-active/> inside <screen-content> if this is a parent screen with subtabs.
5. Base all `<subscreens-item location="...">` attributes strictly on the `Target Moqui Component URL`. For example, if generating `{component_url}`, then a subscreen named `ActiveScreens` must have `location="{component_url.replace('.xml', '')}/ActiveScreens.xml"`. NEVER USE ABSOLUTE SYSTEM FILE PATHS in the `location` string.

Output the code block below:
"""

    data = {
        "model": "qwen2.5-coder:7b",
        "prompt": prompt,
        "stream": True 
    }

    req = urllib.request.Request(
        "http://localhost:11434/api/generate",
        data=json.dumps(data).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )

    if not quiet:
        print(f"Generating artifact for: {blueprint_path}")
        print("--- [Qwen Streaming Output] ---")
    
    full_response = ""
    line_count = 0
    try:
        with urllib.request.urlopen(req, timeout=600) as res:
            for line in res:
                if line:
                    chunk = json.loads(line.decode('utf-8'))
                    response_text = chunk.get('response', '')
                    full_response += response_text
                    
                    if not quiet:
                        if limit is None or line_count < limit:
                            print(response_text, end='', flush=True)
                            if "\n" in response_text:
                                line_count += response_text.count("\n")
                        elif line_count == limit:
                            print("\n... [Output Limit Reached] ...")
                            line_count += 1
                            
                    if chunk.get('done'):
                        break
        
        if not quiet:
            print("\n--- [End of Output] ---")
        
        code = full_response.strip()
        if "```xml" in code:
            code = code.split("```xml")[1].split("```")[0].strip()
        elif "```" in code:
            code = code.split("```")[1].split("```")[0].strip()
            
        if dry_run:
            print("\n[DRY RUN] Target:", target_path)
        else:
            os.makedirs(os.path.dirname(target_path), exist_ok=True)
            with open(target_path, 'w') as f:
                f.write(code)
            print(f"\nSuccessfully generated artifact: {target_path}")
            
    except Exception as e:
        print(f"\nError: {e}")
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Generate Moqui artifacts from Blueprints.')
    parser.add_argument('blueprint', help='Path to the .md blueprint file.')
    parser.add_argument('--dry-run', action='store_true', help='Show output without writing to file.')
    parser.add_argument('--quiet', action='store_true', help='Disable streaming output to stdout.')
    parser.add_argument('--limit', type=int, help='Limit streaming output to N lines.')
    args = parser.parse_args()
    generate_artifact(args.blueprint, args.dry_run, args.quiet, args.limit)
