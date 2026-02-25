---
description: End the current session, log all work, sync repositories, and switch to a lighter model.
---
# /endSession <previous>

Follow this procedure when you are finished working for the day. Use the optional `<previous>` parameter if you forgot to end the session for a previous development day.

## 1. Document Work History
Generate and update the `summary.md` files in the `.agent/work-history/YYYY/MM/DD/` directory for every component modified during the session (e.g., `moqui-ai`, `aitree`, `huddle`).

- **moqui-ai**: Document platform/framework changes (XSDs, macros, core JS).
- **aitree/huddle**: Document application-specific screens, data logic, and UI implementations.

## 2. Sync Code
// turbo
Execute the `git-sync.sh` script for all modified components to ensure the work is committed and pushed to GitHub.
```bash
./git-sync.sh moqui-ai
./git-sync.sh aitree
```

## 3. Switch Mode and Model
To transition to a lighter state for passive monitoring or quick questions:
- Change the **Planning Mode** (or Mode) to **"fast"**.
- Change the **Model** to **"Gemini 3 Flash"**.

## 4. Final Notification
Inform the user that the session is officially archived and synced, and that you are now operating in 'Fast' mode with 'Gemini 3 Flash'.
