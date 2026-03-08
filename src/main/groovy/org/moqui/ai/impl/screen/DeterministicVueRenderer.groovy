package org.moqui.ai.impl.screen

import org.moqui.impl.screen.ScreenRenderImpl
import org.moqui.impl.screen.ScreenWidgetRender
import org.moqui.impl.screen.ScreenWidgets
import org.moqui.impl.screen.ScreenForm
import org.moqui.impl.screen.ScreenForm.FormInstance
import org.moqui.util.MNode
import org.moqui.util.ContextStack
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonBuilder

class DeterministicVueRenderer implements ScreenWidgetRender {
    protected final static Logger logger = LoggerFactory.getLogger(DeterministicVueRenderer.class)

    @Override
    void render(ScreenWidgets widgets, ScreenRenderImpl sri) {
        // Use a consistent key and don't reset if already present (inherited from parent screen)
        List currentChildren = (List) sri.ec.context.get("blueprintChildren")
        boolean isRoot = false
        if (currentChildren == null) {
            isRoot = true
            currentChildren = []
            sri.ec.context.put("blueprintChildren", currentChildren)
        }

        try {
            // AMB 2026-03-02: Support partial rendering for SPA navigation
            // If 'last' is true, we only want the leaf screen of the current path.
            // Using a more robust check for 'hasNext' based on the full path list
            List<String> fullPath = sri.screenUrlInfo.fullPathNameList
            String currentName = sri.getActiveScreenDef().getScreenName()
            int currentIndex = fullPath.indexOf(currentName)
            boolean hasNext = currentIndex >= 0 && currentIndex < (fullPath.size() - 1)
            
            // println "AMB DEBUG: render() location: ${widgets.getLocation()}, current: ${currentName}, index: ${currentIndex}/${fullPath.size()}, hasNext: ${hasNext}, last: ${sri.ec.web.parameters.last}"

            if (sri.ec.web.parameters.last == 'true' && hasNext) {
                if (logger.isInfoEnabled()) logger.info("DeterministicVueRenderer.render() - Skipping ${widgets.getLocation()} because 'last=true' and it has sub-screens in path")
                sri.renderSubscreen()
                return
            }

            if (logger.isInfoEnabled()) logger.info("DeterministicVueRenderer.render() for ${widgets.getLocation()}, isRoot: ${isRoot}, baseChildrenCount: ${currentChildren.size()}")
            
            MNode widgetsNode = widgets.getWidgetsNode()
            if (widgetsNode == null) {
                if (logger.isInfoEnabled()) logger.info("DeterministicVueRenderer.render() - No widgets node for ${widgets.getLocation()}")
                return
            }

            boolean authzDisabled = !sri.ec.artifactExecution.authzDisabled
            if (authzDisabled) sri.ec.artifactExecution.disableAuthz()
            try {
                walkWidgets(widgetsNode, currentChildren, sri, 0)

                // Force SubscreensActive node if there are more screens in the path list
                if (sri.getActiveScreenHasNext() && sri.ec.context.get("SubscreensActiveRendered") != true) {
                    handleSubscreensActive(null, currentChildren, sri, 0)
                }
            } finally {
                sri.ec.context.remove("SubscreensActiveRendered")
                if (authzDisabled) sri.ec.artifactExecution.enableAuthz()
            }
        } finally {
            if (isRoot) {
                Map<String, Object> blueprint = [
                    "@context": "https://moqui.ai/contexts/ui",
                    "@type": "ScreenBlueprint",
                    "location": widgets.getLocation(),
                    "children": currentChildren
                ]
                if (logger.isInfoEnabled()) logger.info("DeterministicVueRenderer produced blueprint with ${currentChildren.size()} root nodes")
                sri.getWriter().write(new JsonBuilder(blueprint).toPrettyString())
                sri.ec.context.remove("blueprintChildren")
            }
        }
    }

    protected void walkWidgets(MNode parentNode, List children, ScreenRenderImpl sri, int depth = 0, MNode fieldNode = null) {
        for (MNode child in parentNode.getChildren()) {
            if (logger.isTraceEnabled()) logger.info("${'  ' * depth}walkWidgets [${depth}]: handling node ${child.getName()}")
            handleNode(child, children, sri, depth + 1, fieldNode)
        }
    }

    protected void handleNode(MNode node, List children, ScreenRenderImpl sri, int depth = 0, MNode fieldNode = null) {
        String name = node.getName()
        switch(name) {
            case "screen":
            case "widgets":
            case "fail-widgets":
                walkWidgets(node, children, sri, depth)
                break
            case "form-single":
                handleFormSingle(node, children, sri, depth)
                break
            case "form-list":
                handleFormList(node, children, sri, depth)
                break
            case "section":
                handleSection(node, children, sri, depth)
                break
            case "dynamic-dialog":
                Map<String, Object> ddMap = [
                    "@type": "m-dynamic-dialog",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                String trans = node.attribute("transition") ?: node.attribute("url")
                String urlType = node.attribute("url-type") ?: "transition"
                if (trans) {
                    ddMap.attributes.url = sri.makeUrlByType(trans, urlType, node, "true").getPath()
                }
                walkWidgets(node, ddMap.children, sri, depth, fieldNode)
                children.add(ddMap)
                break
            case "parameter":
                Map<String, Object> paramMap = [
                    "@type": "parameter",
                    "attributes": evaluateAttributes(node, sri)
                ]
                children.add(paramMap)
                break
            case "link":
                Map<String, Object> linkMap = [
                    "@type": "m-link",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                String url = node.attribute("url")
                String urlType = node.attribute("url-type") ?: "transition"
                if (url && urlType != "plain") {
                    linkMap.attributes.href = sri.makeUrlByType(url, urlType, node, "true").getPath()
                } else if (url) {
                    linkMap.attributes.href = url
                }
                walkWidgets(node, linkMap.children, sri, depth)
                children.add(linkMap)
                break
            case "container-row":
                Map<String, Object> rowMap = [
                    "@type": "m-container-row",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, rowMap.children, sri, depth)
                children.add(rowMap)
                break
            case "row-col":
                Map<String, Object> colMap = [
                    "@type": "m-row-col",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, colMap.children, sri, depth)
                children.add(colMap)
                break
            case "banner":
                Map<String, Object> bannerMap = [
                    "@type": "m-banner",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, bannerMap.children, sri, depth)
                children.add(bannerMap)
                break
            case "container":
                handleContainer(node, children, sri, depth)
                break
            case "label":
                handleLabel(node, children, sri, depth)
                break
            case "subscreens-active":
                handleSubscreensActive(node, children, sri, depth)
                break
            case "subscreens-menu":
                handleSubscreensMenu(node, children, sri, depth)
                break
            case "render-mode":
                handleRenderMode(node, children, sri, depth)
                break
            case "text":
                handleText(node, children, sri, depth)
                break
            case "display":
            case "display-entity":
                Map<String, Object> widgetMap = [
                    "@type": name,
                    "attributes": evaluateAttributes(node, sri)
                ]
                if (fieldNode) {
                    try {
                        String textValue = name == "display" ? sri.getFieldValueString(node) : sri.getFieldEntityValue(node)
                        if (textValue != null) widgetMap.attributes.text = textValue
                    } catch (Exception e) {
                        if (logger.isTraceEnabled()) logger.trace("Error evaluating ${name} value: ${e.message}")
                    }
                }
                children.add(widgetMap)
                break
            case "text-line":
            case "text-area":
            case "drop-down":
            case "date-time":
            case "check":
            case "radio":
            case "password":
            case "hidden":
            case "submit":
                Map<String, Object> widgetMap = [
                    "@type": name,
                    "attributes": evaluateAttributes(node, sri)
                ]
                if (fieldNode && !widgetMap.attributes.value) {
                    try {
                        String textValue = sri.getFieldValueString(node)
                        if (textValue != null) widgetMap.attributes.value = textValue
                    } catch (Exception e) {
                        if (logger.isTraceEnabled()) logger.trace("Error evaluating ${name} value: ${e.message}")
                    }
                }
                children.add(widgetMap)
                break
            case "menu-item":
                Map<String, Object> miAttributes = evaluateAttributes(node, sri)
                String miName = node.attribute("name")
                String miTransition = node.attribute("transition") ?: node.attribute("url") ?: miName
                String miUrlType = node.attribute("url-type") ?: "transition"
                if (miTransition) {
                    miAttributes.href = sri.makeUrlByType(miTransition, miUrlType, node, "true").getPath()
                }
                if (miName) {
                    def subItem = sri.getActiveScreenDef().getSubscreensItem(miName)
                    if (subItem) {
                        if (!miAttributes.text && !miAttributes.label) miAttributes.label = sri.ec.resource.expand(subItem.getMenuTitle() ?: subItem.getName(), "")
                    }
                }
                children.add(["@type": "m-menu-item", "attributes": miAttributes])
                break
            case "menu-dropdown":
                Map<String, Object> mdAttributes = evaluateAttributes(node, sri)
                String mdName = node.attribute("name")
                String mdTrans = node.attribute("transition")
                String mdTargetUrlAttr = node.attribute("target-url") ?: mdName
                if (mdTargetUrlAttr) {
                    mdAttributes["target-url"] = sri.makeUrlByType(mdTargetUrlAttr, "transition", node, "true").getPath()
                }
                if (mdName) {
                    def subItem = sri.getActiveScreenDef().getSubscreensItem(mdName)
                    if (subItem) {
                        if (!mdAttributes.text && !mdAttributes.label) mdAttributes.label = sri.ec.resource.expand(subItem.getMenuTitle() ?: subItem.getName(), "")
                    }
                }
                if (mdTrans) {
                    mdAttributes["transition-url"] = sri.makeUrlByType(mdTrans, "transition", node, "true").getPath()
                }
                children.add(["@type": "m-menu-dropdown", "attributes": mdAttributes])
                break
            case "bp-tabbar":
            case "bp-tab":
            case "screen-layout":
            case "screen-header":
            case "screen-toolbar":
            case "screen-drawer":
            case "screen-content":
                Map<String, Object> bpMap = [
                    "@type": "m-" + name,
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, bpMap.children, sri, depth)
                children.add(bpMap)
                break
            default:
                if (logger.isDebugEnabled()) logger.debug("${'  ' * depth}Handling unknown node: ${name} at ${sri.getActiveScreenDef().getLocation()}")
                
                // Map standard HTML tags to Blueprint types or keep as-is
                String nodeType = name
                if (name == "div" || name == "m-div") nodeType = "Container"
                if (name == "template" || name == "m-template") nodeType = "BlueprintTemplate"

                Map<String, Object> mapNode = [
                    "@type": nodeType,
                    "attributes": evaluateAttributes(node, sri)
                ]
                if (!node.getChildren().isEmpty()) {
                    mapNode.children = []
                    walkWidgets(node, mapNode.children, sri, depth)
                }
                children.add(mapNode)
                break
        }
    }

    protected void handleFormSingle(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        String formName = node.attribute("name")
        FormInstance formInstance = sri.getFormInstance(formName)
        if (logger.isInfoEnabled()) logger.info("${'  ' * depth}handleFormSingle: ${formName}")
        MNode formNode = formInstance.getFormNode()
        
        sri.pushSingleFormMapContext(formNode.attribute("map") ?: "fieldValues")
        try {
            Map<String, Object> formMap = [
                "@type": "FormSingle",
                "name": formName,
                "action": sri.makeUrlByType(formNode.attribute("transition"), "transition", null, "true").getPath(),
                "fieldsInitial": sri.getFormFieldValues(formNode),
                "children": []
            ]
            
            // Handle field layout or fields
            MNode fieldLayout = formNode.first("field-layout")
            if (fieldLayout) {
                walkWidgets(fieldLayout, formMap.children, sri, depth)
            } else {
                for (MNode fieldNode in formNode.children("field")) {
                    handleField(fieldNode, formMap.children, sri)
                }
            }
            children.add(formMap)
        } finally {
            sri.popContext()
        }
    }

    protected void handleFormList(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        String formName = node.attribute("name")
        FormInstance formInstance = sri.getFormInstance(formName)
        if (logger.isInfoEnabled()) logger.info("${'  ' * depth}handleFormList: ${formName}")
        MNode formNode = formInstance.getFormNode()
        
        // This is a simplified version, real one needs data paging etc.
        List list = formInstance.makeFormListRenderInfo().getListObject(true)
        
        Map<String, Object> formMap = [
            "@type": "FormList",
            "name": formName,
            "header": [],
            "rows": []
        ]
        
        // Capture Header
        for (MNode field in formNode.children("field")) {
            String fieldName = field.attribute("name")
            MNode headerField = field.first("header-field")
            MNode defaultField = field.first("default-field")
            
            // Determine title - use standard Moqui title evaluation if possible
            String title = field.attribute("title")
            if (!title && headerField) title = headerField.attribute("title")
            if (!title && defaultField) title = defaultField.attribute("title")
            if (!title) title = sri.ec.l10n.getLowCaseToSpace(fieldName)

            Map<String, Object> hField = [
                "name": fieldName,
                "title": sri.ec.resource.expand(title, ""),
                "children": []
            ]
            if (headerField) walkWidgets(headerField, hField.children, sri, depth, field)
            formMap.header.add(hField)
        }
        
        // Capture Data Rows
        for (Object item in list) {
            Map<String, Object> row = ["_data": item instanceof org.moqui.entity.EntityValue ? item.getMap() : item]
            row.fields = []
            sri.ec.contextStack.push(item)
            try {
                for (MNode field in formNode.children("field")) {
                    handleField(field, row.fields, sri)
                }
            } finally {
                sri.ec.contextStack.pop()
            }
            formMap.rows.add(row)
        }
        
        if (logger.isInfoEnabled()) logger.info("${'  ' * depth}FormList ${formName} produced ${formMap.rows.size()} rows")
        children.add(formMap)
    }

    protected void handleSubscreensActive(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        Map<String, Object> subMap = [
            "@type": "SubscreensActive",
            "children": []
        ]
        if (logger.isInfoEnabled()) logger.info("${'  ' * depth}handleSubscreensActive: starting renderSubscreen")
        
        List parentChildren = (List) sri.ec.context.get("blueprintChildren")
        sri.ec.context.put("blueprintChildren", subMap.children)
        try {
            sri.renderSubscreen()
        } finally {
            if (parentChildren != null) sri.ec.context.put("blueprintChildren", parentChildren)
            else sri.ec.context.remove("blueprintChildren")
        }
        
        sri.ec.context.put("SubscreensActiveRendered", true)
        children.add(subMap)
        if (logger.isInfoEnabled()) logger.info("${'  ' * depth}handleSubscreensActive: finished with ${subMap.children.size()} children")
    }

    protected void handleSubscreensMenu(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        // Menu data can be retrieved from sri.getMenuData() or similar
        children.add([
            "@type": "SubscreensMenu",
            "id": node.attribute("id")
        ])
    }

    protected void handleSection(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        boolean conditionPassed = true
        if (node.attribute("condition")) {
            conditionPassed = sri.ec.resource.condition(node.attribute("condition"), "section.condition")
        }
        if (conditionPassed && node.first("condition")?.first() != null) {
            org.moqui.impl.actions.XmlAction conditionAction = new org.moqui.impl.actions.XmlAction(sri.ec.ecfi, node.first("condition").first(), "section.condition")
            conditionPassed = conditionAction.checkCondition(sri.ec)
        }
        
        if (conditionPassed) {
            if (node.hasChild("widgets")) {
                walkWidgets(node.first("widgets"), children, sri, depth)
            }
        } else {
            if (node.hasChild("fail-widgets")) {
                walkWidgets(node.first("fail-widgets"), children, sri, depth)
            }
        }
    }

    protected void handleContainer(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        Map<String, Object> containerMap = [
            "@type": "Container",
            "id": sri.ec.resource.expandNoL10n(node.attribute("id"), ""),
            "style": sri.ec.resource.expandNoL10n(node.attribute("style"), ""),
            "attributes": evaluateAttributes(node, sri),
            "children": []
        ]
        walkWidgets(node, containerMap.children, sri, depth)
        children.add(containerMap)
    }

    protected void handleLabel(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        Map<String, Object> labelMap = [
            "@type": "Label",
            "text": sri.ec.resource.expand(node.attribute("text") ?: node.getText(), ""),
            "style": sri.ec.resource.expandNoL10n(node.attribute("style"), ""),
            "attributes": evaluateAttributes(node, sri)
        ]
        children.add(labelMap)
    }

    protected void handleField(MNode fieldNode, List children, ScreenRenderImpl sri) {
        // Simple field handler for now
        Map<String, Object> fieldMap = [
            "@type": "FormField",
            "name": fieldNode.attribute("name"),
            "children": []
        ]
        MNode widgetNode = fieldNode.getChildren().find { it.getName() != "header-field" && it.getName() != "condition" }
        if (widgetNode) {
            String widgetName = widgetNode.getName()
            if (widgetName == "default-field" || widgetName == "conditional-field") {
                walkWidgets(widgetNode, fieldMap.children, sri, 0, fieldNode)
            } else {
                handleNode(widgetNode, fieldMap.children, sri, 0, fieldNode)
            }
        }
        children.add(fieldMap)
    }

    protected void handleRenderMode(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        String type = node.attribute("type")
        if (!type || type == sri.getRenderMode() || type == "qjson" || type.contains("html") || type.contains("qvt")) {
            walkWidgets(node, children, sri, depth)
        }
    }

    protected void handleText(MNode node, List children, ScreenRenderImpl sri, int depth = 0) {
        String text = node.getText()
        if (text != null && text.trim() == '${sri.renderSubscreen()}') {
            handleSubscreensActive(node, children, sri, depth)
            return
        }
        
        // Handle literal text or template
        String type = node.attribute("type")
        if (!type || type == sri.getRenderMode() || type == "qjson" || type.contains("html") || type.contains("qvt")) {
            String location = node.attribute("location")
            if (location && (location.endsWith(".ftl") || location.endsWith(".html") || location.endsWith(".qvt")) && sri.getRenderMode() == "qjson") {
                if (logger.isInfoEnabled()) logger.info("${'  ' * depth}Skipping template in JSON mode: ${location}")
                return
            }
            if (!location && text?.contains("<#") && sri.getRenderMode() == "qjson") {
                if (logger.isInfoEnabled()) logger.info("${'  ' * depth}Skipping raw FTL code in JSON mode")
                return
            }

            String expandedText = ""
            if (location) {
                expandedText = sri.ec.resource.getLocationText(location, true) ?: ""
                if (node.attribute("template") == "true") expandedText = sri.ec.resource.expand(expandedText, "")
            } else {
                expandedText = node.attribute("template") == "true" ? sri.ec.resource.expand(text, "") : text
            }

            if (expandedText != null && !expandedText.trim().isEmpty()) {
                children.add([
                    "@type": "Text",
                    "text": expandedText,
                    "location": location
                ])
            }
        }
    }

    protected Map<String, String> evaluateAttributes(MNode node, ScreenRenderImpl sri) {
        Map<String, String> attrs = [:]
        for (entry in node.getAttributes()) {
            String value = entry.getValue()
            if (value?.contains('${')) {
                attrs[entry.getKey()] = sri.ec.resource.expand(value, "")
            } else {
                attrs[entry.getKey()] = value
            }
        }
        return attrs
    }
}
