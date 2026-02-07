# Project MoquiAi: Agent-Empowered Development

**Mission:** To use Antigravity (AG) to empower Moqui users in their creation of Moqui apps by automating boilerplates, enforcing patterns, and managing the development lifecycle.

## Agent Capability Roadmap

This document tracks the capabilities ("Skills") and improvements planned for the Antigravity Agent to better assist the user in maintaining the Huddle application.

## High Priority
- [ ] **Server Management Skill**:
    -   **Goal**: Allow the Agent to autonomously check status, stop, and start the Moqui server, viewing logs to confirm readiness.
    -   **Motivation**: Reduce user cognitive load and operational friction during development and verification cycles.
    -   **Implementation Details**:
        -   Check PID/Process existence.
        -   Execute start script in background.
        -   Tail `runtime/log/moqui.log` (or stdout) to detect "Ready" state.

## Backlog
- [ ] **Automated UI Testing Skill**:
    -   **Goal**: Capability to run headless browser tests or curl/grep checks to verify UI rendering without manual user intervention.
