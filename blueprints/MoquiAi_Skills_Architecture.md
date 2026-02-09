# MoquiAi Skills Architecture: AI as a Library

**Goal**: Treat AI capabilities ("Skills") as installable, documented software libraries that can be plugged into both Client (Vue) and Server (Java/Groovy) code.

## 1. The "Skill" Definition
A Skill is a self-contained unit of AI logic. It is **not** just a prompt; it is a full software component.

### Structure of a Skill Package (`component/moqui-ai-skills/sentiment/`)
-   **`manifest.json`**: Metadata (Name, Version, Inputs, Outputs, Cost Estimation).
-   **`prompts/`**: The raw LLM system prompts (versioned).
-   **`service/`**: Server-side Moqui Services (Groovy) that wrap the LLM call.
-   **`client/`**: Client-side Vue composables or JS functions to invoke the skill.
-   **`docs/`**: Auto-generated API documentation.

## 2. Usage Models

### A. Server-Side (The "Heavy Lifter")
Used for background processing, heavy analysis, or when secrets are involved.
```groovy
// In a Moqui Service
def result = ec.service.call("ai.skills.SummarizerService", [
    text: lengthyReport,
    style: "executive"
])
```

### B. Client-Side (The "Interactive Helper")
Used for real-time UI enhancement (autocomplete, smart validation).
```javascript
// In a Vue Component
import { useAiSkill } from 'moqui-ai-client'

const summarizer = useAiSkill('Summarizer')
const summary = await summarizer.execute(inputText.value)
```

## 3. Standardization ( The "Standard Library")
Just as Java has `java.util.*`, MoquiAi will have a core library of AI skills:
-   `ai.core.Extract` (Structured data extraction)
-   `ai.core.Summarize` (Text reduction)
-   `ai.core.Classify` (Routing/Labeling)
-   `ai.core.Generate` (Content creation)

## 4. Discovery & Documentation
-   **Skill Registry**: A runtime service that lists all available skills.
-   **"Javadoc for AI"**: Developers can read the `manifest.json` to know exactly what a skill does, what it costs (tokens), and how to call it, without needing to read the underlying prompt engineering.
