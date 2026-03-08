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
    wepop_text = read_lines(wepop_path, 800) # Increased for 10 records
    seed_text = read_lines(seed_path, 300)
    with open(entities_path, 'r') as f:
        entities_text = f.read()

    # Fixed Infrastructure Records
    infra_records = [
        '  <aitree.meeting.AgendaContainer agendaContainerId="BASE_REPO" name="Base Repository Container" shortName="Base Repo" containerCategoryEnumId="AitCategoryAbstract" containerTypeEnumId="AitContainerRepo" statusId="AgdStatusActive"/>',
        '  <aitree.meeting.AgendaContainerRelationship parentAgendaContainerId="HdlMorningAbstract" childAgendaContainerId="BASE_REPO" statusId="AgdStatusActive"/>',
        '  <aitree.meeting.AgendaContainerRelationship parentAgendaContainerId="HdlLeadershipAbstract" childAgendaContainerId="BASE_REPO" statusId="AgdStatusActive"/>'
    ]

    prompt = f"""You are a Moqui/Quasar Specialist AI. Your task is to transform legacy Moqui XML data into the new 'aitree' entity format.

### Target Entity Definitions (AiTreeEntities.xml):
{entities_text}

### Source Data Snippet (Topics - wepop.xml):
{wepop_text}

### Golden Sample Transformation:
FROM Legacy:
<sh.AgendaTopic agendaTopicId="SHAT_0001" orgId="SH1_CORP" partyId="100051"/>
<sh.SHContent sHContentId="SHC_0001" title="Staff PPD" description="Staff Pneumococcal Vaccine status update"/>
<sh.AgendaTopicContent agendaTopicId="SHAT_0001" contentId="SHC_0001"/>

TO New Format:
<aitree.meeting.AgendaMessage agendaMessageId="SHAT_0001" orgId="SH1_CORP" partyId="100051" agendaContainerId="BASE_REPO" parentMessageId=""/>
<aitree.meeting.AgendaMessageContent agendaMessageId="SHAT_0001" contentId="SHC_0001" title="Staff PPD" description="Staff Pneumococcal Vaccine status update"/>

### Specific Mapping Rules for this task:
1. Legacy 'sh.AgendaTopic' -> 'aitree.meeting.AgendaMessage'
   - agendaTopicId -> agendaMessageId (LITERAL COPY)
   - orgId -> orgId (LITERAL COPY)
   - partyId -> partyId (LITERAL COPY)
   - ALWAYS set agendaContainerId="BASE_REPO"
   - ALWAYS set parentMessageId="" for top-level topics.
2. Legacy 'sh.SHContent' -> 'aitree.meeting.AgendaMessageContent'
   - title -> title (LITERAL COPY)
   - description -> description (LITERAL COPY)

### Requirement:
- Generate exactly the first {max_records} top-level message records found in the snippet.
- Output ONLY the XML tags for AgendaMessage and AgendaMessageContent.
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
        
        # Extraction & Post-processing
        xml_content = full_response
        if "```xml" in xml_content:
            xml_content = xml_content.split("```xml")[1].split("```")[0].strip()
        elif "```" in xml_content:
            xml_content = xml_content.split("```")[1].split("```")[0].strip()
        
        # Filter out root tags if AI added them
        lines = xml_content.strip().split('\n')
        filtered_lines = [l for l in lines if not l.strip().startswith('<entity-facade-xml') and not l.strip().startswith('</entity-facade-xml') and not l.strip().startswith('<?xml')]
        
        # Build final file with injected infrastructure
        final_xml = ['<?xml version="1.0" encoding="UTF-8"?>', '<entity-facade-xml>']
        final_xml.extend(infra_records)
        final_xml.extend(filtered_lines)
        final_xml.append('</entity-facade-xml>')

        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, "w") as f:
            f.write("\n".join(final_xml))
        
        print(f"\nFinal XML saved successfully with injected infrastructure to: {output_path}")

    except Exception as e:
        print(f"\nError: {e}")
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Transform Moqui data using local Qwen.')
    parser.add_argument('--quiet', action='store_true', help='Disable streaming output to stdout.')
    parser.add_argument('--records', type=int, default=10, help='Number of records to transform.')
    args = parser.parse_args()

    wepop = "runtime/archive-components/studdle/output/wepop.xml"
    seed = "runtime/component/aitree/blueprints/resources/agendaSeedData.xml"
    entities = "runtime/component/aitree/entity/AiTreeEntities.xml"
    output = "runtime/component/aitree/data/agendaContainerData.xml"
    transform_data(wepop, seed, entities, output, args.records, args.quiet)
