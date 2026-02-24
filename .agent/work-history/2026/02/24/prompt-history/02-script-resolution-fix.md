# Prompt 02: Resolving "#?v=" Script Corruption (MIME Type Error)

## Timestamp
2026-02-24T10:45:00-07:00 (approx)

## User Prompt
The user reported that the application shell rendered but the main content area was empty. The browser console showed: `Refused to execute script from '.../#?v=19c90c9' because its MIME type ('text/html') is not executable`.

## Technical Analysis
- **Root Cause**: `AitreePreActions.groovy` was adding `/moquiai/js/MoquiAiVue.qvt2.js` to the `footer_scripts`. 
- **Mechanism**: Moqui's `ScreenRenderImpl.buildUrl()` (SRI) checks if the target path is a valid screen/transition. Since `moqui-ai` was not mounted as a subscreen of the active `aitree` webapp, SRI failed to resolve it.
- **Moqui Fallback**: When SRI fails a' resolution, it returns `#` and appends the cache-buster `?v=...`. The browser then tried to execute the current HTML page as JavaScript, hitting the MIME type restriction.

## AI Response & Decision
- **Decision**: Instead of fighting the global `MoquiConf.xml` mounting (which was failing to apply), create a local "proxy" transition in the application root (`aitree.xml`).
- **Fix**: Added `<transition name="moquiaiJs">` to `aitree.xml` using `ec.web.sendResourceResponse()`.
- **Fix**: Updated `AitreePreActions.groovy` to use the path `moquiaiJs/MoquiAiVue.qvt2.js`.

## Impact
- Correctly served the `MoquiAiVue.qvt2.js` file.
- Prevented the `#?v=` corruption.
- Successfully booted the Vue application shell.
