# Huddle Project Rules
- **Stack:** Moqui 4.0, Java 21, Vue 3/Quasar 2.
- **Privacy:** Every field storing PHI or medical data MUST have `encrypt="true"`.
- **Audit:** Any sensitive entity MUST have `enable-audit-log="true"`.
- **Mantle Extension:** Reuse first. Extend `mantle.party.Party` or `mantle.facility.Facility` before creating new entities.
- **Java 21:** Always use the following flags for runtime:
  `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED`
