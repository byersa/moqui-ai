---
alias: ui-config
type: utility
version: 1.2
---

# Skill: Config

## Template: MoquiConf.xml
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<moqui-conf xmlns:xsi="[http://www.w3.org/2001/XMLSchema-instance](http://www.w3.org/2001/XMLSchema-instance)" xsi:noNamespaceSchemaLocation="[http://moqui.org/xsd/moqui-conf-2.1.xsd](http://moqui.org/xsd/moqui-conf-2.1.xsd)">
    <screen-text-output type="qvt" mime-type="text/html" always-standalone="true"
                        macro-template-location="component://moqui-quasar/template/screen-macro/DefaultScreenMacros.qvt.ftl"/>
    <screen-facade>
        <screen location="component://webroot/screen/webroot/apps.xml">
            <subscreens-item name="${componentName}" menu-title="${componentTitle}" menu-index="96"
                             menu-include="true"
                             location="component://${componentName}/screen/${componentName}.xml"/>
        </screen>
    </screen-facade>
</moqui-conf>