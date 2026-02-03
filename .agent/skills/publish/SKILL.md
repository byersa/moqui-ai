---
alias: publish
type: orchestrator
version: 1.0
---

# Skill: Publish

## Purpose
The "Gold Standard" deployment command. Ensures code is synced, verified, tested on a running server, and pushed to the cloud.

## Logic
1. **Pre-Flight:** Call `/sync [component]`. This ensures the code and ARIA IDs are correct.
2. **Environment Check:** - Check if Moqui is running (e.g., `curl localhost:8080`).
   - If NOT running: Execute `./gradlew run` with Java 21 flags. Wait for "Server Started" log.
3. **Verification:** - Use Moqui-MCP to render the targeted screen.
   - Confirm semantic health via ARIA/MARIA.
4. **Browser Presentation:** - Open (or refresh) the browser to the screen's URL (e.g., `http://localhost:8080/huddle`).
5. **Cloud Sync:** Call `/git-sync [component]`.