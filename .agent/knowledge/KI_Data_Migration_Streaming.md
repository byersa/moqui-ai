# KI: Data Migration Pattern - Streaming local AI

## Context
When migrating legacy data (like `studdle` wepop.xml) into new Moqui environments (like `aitree`), local AI is efficient but can feel like a "black box" during long-running tasks.

## Strategy: The "Streaming Terminal" Pattern
We avoid silent background processes for data transformation. Instead, we use a streaming Python bridge.

### Benefits:
1. **Real-time Monitoring**: The user sees every record as it is transformed. 
2. **Immediate Error Detection**: If the model mapping drifts (hallucination), the user sees it instantly and can stop the process.
3. **Timeout Safety**: Scripts include a 10-minute safety timeout to prevent infinite CPU/GPU hanging.

## Implementation Tool
**location**: `runtime/component/moqui-ai/bin/qwen-data-transform.py`
**trigger**: `./qwen-data-transform`

### Usage for Users:
- Run the command.
- If the XML looks correct (mapped to `aitree.meeting`), let it finish.
- If it looks wrong, hit `Ctrl+C` immediately.
