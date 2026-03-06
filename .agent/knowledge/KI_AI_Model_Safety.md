# KI: AI Model Safety and Versioning (Snapshots)

## Context
When "training" or fine-tuning local LLMs (like Qwen on Ollama), there is a risk of "Catastrophic Forgetting" (where the model becomes too specialized and loses general reasoning).

## Safety Strategy: The "Snapshot" Pattern
Ollama makes it extremely safe to experiment because the base models are **immutable** (read-only). We never overwrite the original brain.

### 1. Creating a Versioned Copy
Before starting any training or heavy configuration, we create a backup:
`ollama copy qwen2.5-coder:7b qwen-baseline-backup`
This creates a pointer to the current stable version that will never change.

### 2. The "Branching" Brain
When we train, we create a **new model name**:
* `qwen2.5-coder:7b` (The original General Expert)
* `qwen-moqui-v1` (The first attempt at a Specialist)
* `qwen-moqui-v2` (The second attempt)

### 3. Immediate Restoration
If `v2` becomes "brain damaged" or starts giving weird hallucinations:
1. Stop using `v2`.
2. Run the baseline: `ollama run qwen-baseline-backup` or `ollama run qwen2.5-coder:7b`.
3. Delete the broken model: `ollama rm qwen-moqui-v2`.

## Physical Storage
On your Linux Mint system, Ollama stores these brains as binary blobs in:
`/usr/share/ollama/.ollama/models/blobs/`
These files are giant mathematical weights (tensors). They are safe from accidental text edits.

## Summary
You can never "break" your local AI permanently. You are always just one command away from a factory-fresh original brain.
