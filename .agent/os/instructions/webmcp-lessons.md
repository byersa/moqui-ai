# WebMCP Lessons Learned - 2026-03-19

## Connection Management
- WebMCP tokens must be generated correctly using `node runtime/component/moqui-ai/screen/moquiai/js/websocket-server.js --new`. Trying to parse or guess old tokens from logs usually fails because tokens cycle and expire.
- If the WebMCP tools respond with "No clients available" or similar errors, the connection might be broken, or the token is mismatched. Verifying the token and restarting the node process (`--quit` then restart) is the most reliable way to recover the connection.

## Tool Execution Flow
- Interactions via WebMCP components generally require passing standard Moqui IDs (e.g., using `id="cancel-btn"` on a `<link>` attribute). Modifying the source code `Meetings.xml` to inject these `id` variables explicitly was essential to accurately target elements with the WebMCP `click_element` tool.
- Verifying UI changes natively through the backend JSON API (e.g. `?renderMode=qjson`) is often faster and more deterministic for testing Blueprint structures when WebMCP is unavailable or flaky, as long as you're testing the blueprint structure logic.

## Technical Nuances
- Moqui's `isEditing=true` evaluation defaults parameter typing to strongly-typed Java objects (like `java.lang.Boolean`). This means Groovy string comparisons (`isEditing == 'true'`) against boolean context fields will falsely match or mismatch.
- Rely strictly on standard Groovy truthy/falsy `sri.ec.resource.condition("!isEditing", "")` to prevent the UI from resolving parallel conditional blocks simultaneously.
