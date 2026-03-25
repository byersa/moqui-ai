---
description: Load generated XML data into the Moqui environment.
---

This workflow automates the common task of importing our generated `AgendaContainer` and `AgendaMessage` data into the Moqui environment.

// turbo
1. **Execute Loader**: Run the data-load tool with the target file:
   `./data-load <path_to_xml_file>`

   Example for our generated data:
   `./data-load runtime/component/aitree/data/agendaContainerData.xml`

2. **Verify Load**: Check the Moqui logs or the entity database (if accessible) to ensure the records were correctly inserted.
