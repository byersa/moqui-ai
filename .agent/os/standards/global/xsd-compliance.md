# XSD Schema Compliance

**All Moqui XML files MUST conform to the relevant XSD schema** in `framework/xsd/`. Using invalid elements, misspelled attributes, or wrong nesting will cause runtime errors. Always check the correct XSD for the artifact type you are writing.

## XSD-to-Artifact Mapping

| XSD File | Root Element(s) | Used By | Component File Pattern |
|----------|----------------|---------|----------------------|
| `entity-definition-3.xsd` | `entities`, `entity`, `extend-entity`, `view-entity` | Entity definitions | `entity/*.xml` |
| `entity-eca-3.xsd` | `eecas`, `eeca` | Entity Event Condition Actions | `entity/*.eecas.xml` |
| `service-definition-3.xsd` | `services`, `service` | Service definitions | `service/**/*.xml` |
| `service-eca-3.xsd` | `secas`, `seca` | Service Event Condition Actions | `service/**/*.secas.xml` |
| `rest-api-3.xsd` | `resource`, `id`, `method` | REST API resource definitions | `service/*.rest.xml` *(directly in service/, NOT in subdirectories)* |
| `xml-screen-3.xsd` | `screen`, `screen-extend` | Screen definitions | `screen/**/*.xml` |
| `xml-form-3.xsd` | `form-single`, `form-list` | Form widgets (embedded in screens) | `screen/**/*.xml` |
| `xml-actions-3.xsd` | `actions`, `condition` | Action blocks (embedded in services, screens, ECAs) | Embedded within other artifacts |
| `moqui-conf-3.xsd` | `moqui-conf` | Framework/component configuration | `MoquiConf.xml`, `runtime/conf/*.xml` |
| `email-eca-3.xsd` | `emecas`, `emeca` | Email Event Condition Actions | `entity/*.emecas.xml` |
| `common-types-3.xsd` | *(type definitions only)* | Shared types used by all other XSDs | *(Not directly referenced)* |

## Key Rules

1. **Always set `xsi:noNamespaceSchemaLocation`** on root elements to reference the correct XSD:
   ```xml
   <!-- Entity files -->
   <entities xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/entity-definition-3.xsd">

   <!-- Service files -->
   <services xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-definition-3.xsd">

   <!-- Screen files -->
   <screen xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/xml-screen-3.xsd">

   <!-- REST API files -->
   <resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/rest-api-3.xsd">

   <!-- EECA files -->
   <eecas xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/entity-eca-3.xsd">

   <!-- SECA files -->
   <secas xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/service-eca-3.xsd">

   <!-- MoquiConf files -->
   <moqui-conf xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:noNamespaceSchemaLocation="http://moqui.org/xsd/moqui-conf-3.xsd">
   ```

2. **Use only valid XSD-defined elements** — never invent element names. Common mistakes:
   - `<make-value>` WRONG -> `<entity-make-value>` CORRECT
   - `<sequenced-id-primary>` WRONG -> `<entity-sequenced-id-primary>` CORRECT
   - `<find-one>` WRONG -> `<entity-find-one>` CORRECT
   - `<find>` WRONG -> `<entity-find>` CORRECT

   All entity operation elements are prefixed with `entity-`. When in doubt, check the XSD file.

### Valid XML Actions Element Names (from xml-actions-3.xsd)

| Category | Elements |
|----------|----------|
| **Variables** | `set`, `script` |
| **Control flow** | `if`, `else-if`, `else`, `iterate`, `while`, `return`, `message` |
| **Entity read** | `entity-find-one`, `entity-find`, `entity-find-count`, `entity-find-related` |
| **Entity write** | `entity-make-value`, `entity-create`, `entity-update`, `entity-delete`, `entity-delete-by-condition`, `entity-delete-related`, `entity-set` |
| **Entity ID** | `entity-sequenced-id-primary`, `entity-sequenced-id-secondary` |
| **Service** | `service-call` |
| **Logging** | `log` |
| **XML actions** | `actions` (nested block) |
| **Order by** | `order-by` (inside entity-find) |