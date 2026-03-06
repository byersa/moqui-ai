#!/usr/bin/env python3
import sys, os, json, urllib.request, argparse

def read_lines(file_path, max_lines):
    lines = []
    with open(file_path, 'r') as f:
        for _ in range(max_lines):
            line = f.readline()
            if not line: break
            lines.append(line)
    return "".join(lines)

def transform_data(wepop_path, seed_path, entities_path, output_path, max_records=5, quiet=False):
    # Context gathering
    if not all(os.path.exists(p) for p in [wepop_path, seed_path, entities_path]):
        print(f"Error: Missing source files: {wepop_path}, {seed_path}, or {entities_path}")
        sys.exit(1)

    # Read snippets safely
    wepop_text = read_lines(wepop_path, 300)
    seed_text = read_lines(seed_path, 300)
    with open(entities_path, 'r') as f:
        entities_text = f.read()

    prompt = f"""You are a Moqui/Quasar Specialist AI. Your task is to transform legacy Moqui XML data into the new 'aitree' entity format.

### Target Entity Definitions (AiTreeEntities.xml):
{entities_text}

### Legacy Seed Data (Meeting Templates - agendaSeedData.xml):
{seed_text}

### Source Data Snippet (Topics - wepop.xml):
{wepop_text}

### Mapping Rules:

#### 1. Legacy 'sh.MeetingTemplate' -> 'aitree.meeting.AgendaContainer'
- meetingTemplateId -> agendaContainerId
- name -> name, shortName -> shortName, orgId -> orgId
- CATEGORY Mapping:
    - If meetingTypeEnumId is 'ShMtgAbstract' or 'ShMtgTopicRepo', use containerCategoryEnumId="AitCategoryAbstract".
- TYPE Mapping:
    - 'ShMtgAbstract' -> containerTypeEnumId="AitContainerAbstract"
    - 'ShMtgTopicRepo' -> containerTypeEnumId="AitContainerRepo"
- DEFAULT STATUS: AgdStatusActive

#### 2. Legacy 'sh.AgendaTopic' -> 'aitree.meeting.AgendaMessage'
- agendaTopicId -> agendaMessageId, orgId -> orgId, partyId -> partyId.
- Match meetingTemplateId to agendaContainerId if possible.

#### 3. Legacy 'sh.SHContent' -> 'aitree.meeting.AgendaMessageContent'
- title -> title, description -> description. Use IDs from wepop.xml snippets.

### CRITICAL INSTRUCTIONS:
- **Literal Value Copying**: Do NOT shorten or modify IDs or string content. If an ID is 'SHMT_0001', keep it 'SHMT_0001'.
- **Correct Output Format**: Use the entity name (including package) as the tag name and fields as attributes.
- **Root Tag**: The entire output MUST be wrapped in a `<entity-facade-xml>` root element.

### Requirement:
- Generate a valid <entity-facade-xml> file.
- Transform the first 5 'sh.MeetingTemplate' records into 'aitree.meeting.AgendaContainer'.
- Transform the first 5 'sh.AgendaTopic' records into 'aitree.meeting.AgendaMessage'.
- Output ONLY the final XML.
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

    print(f"Starting generation (Target: {output_path})...")
    if not quiet:
        print("--- [Qwen Streaming Output] ---")
    
    full_response = ""
    try:
        with urllib.request.urlopen(req, timeout=600) as res:
            for line in res:
                if line:
                    chunk = json.loads(line.decode('utf-8'))
                    response_text = chunk.get('response', '')
                    full_response += response_text
                    if not quiet:
                        print(response_text, end='', flush=True)
                    if chunk.get('done'):
                        break
        
        if not quiet:
            print("\n--- [End of Output] ---")
        
        xml_content = full_response
        if "```xml" in xml_content:
            xml_content = xml_content.split("```xml")[1].split("```")[0].strip()
        elif "```" in xml_content:
            xml_content = xml_content.split("```")[1].split("```")[0].strip()
        
        xml_content = xml_content.strip()
        
        if not xml_content.startswith("<entity-facade-xml"):
             xml_content = '<?xml version="1.0" encoding="UTF-8"?>\n<entity-facade-xml>\n' + xml_content + '\n</entity-facade-xml>'

        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, "w") as f:
            f.write(xml_content)
        
        print(f"\nFinal XML saved successfully to: {output_path}")

    except Exception as e:
        print(f"\nError: {e}")
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Transform Moqui data using local Qwen.')
    parser.add_argument('--quiet', action='store_true', help='Disable streaming output to stdout.')
    parser.add_argument('--records', type=int, default=5, help='Number of records to transform.')
    args = parser.parse_args()

    wepop = "runtime/archive-components/studdle/output/wepop.xml"
    seed = "runtime/component/aitree/blueprints/resources/agendaSeedData.xml"
    entities = "runtime/component/aitree/entity/AiTreeEntities.xml"
    output = "runtime/component/aitree/data/agendaContainerData.xml"
    transform_data(wepop, seed, entities, output, args.records, args.quiet)
