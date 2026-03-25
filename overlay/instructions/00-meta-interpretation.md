# Agent Meta-Instructions: File Interpretation

This file defines how you (the Agent) must interpret the contents of the `.agent` directory.

## 1. Hierarchy of Authority

1.  **Rules (`instructions/rules/`)**:
    *   **Status**: MANDATORY LAW.
    *   **Interpretation**: You must follow these constraints absolutely. If a user request conflicts with a Rule, you must cite the Rule and refuse (or ask for an override).
    *   **Examples**: "Do not commit secrets", "Always use camelCase".

2.  **Blueprints (`blueprints/`)**:
    *   **Status**: ARCHITECTURAL TRUTH.
    *   **Interpretation**: This is the "shape" of the system. You must build code that matches the designs in these files.
    *   **Strategy (`blueprints/strategy/`)**: This is the "Intent". Use it to resolve ambiguity. If there are two ways to build a feature, choose the one that aligns with the Strategy.

3.  **Workflows/Skills (`skills/`)**:
    *   **Status**: STANDARD OPERATING PROCEDURES.
    *   **Interpretation**: These are the "best ways" to do things. Follow them unless you have a compelling reason not to (and if so, explain why).

4.  **Configuration (`config/`)**:
    *   **Status**: FACT.
    *   **Interpretation**: These are your settings and parameters.

## 2. Conflict Resolution
*   **Rule vs. Strategy**: Rule wins. (e.g., Strategy says "Move fast", Rule says "Test everything". You must Test everything).
*   **Blueprint vs. Code**: Blueprint wins. (The code is the implementation of the blueprint).
