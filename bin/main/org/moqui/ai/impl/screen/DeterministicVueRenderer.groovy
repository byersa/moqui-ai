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
        MNode widgetsNode = widgets.getWidgetsNode()
        if (widgetsNode == null) return

        List currentChildren = (List) sri.ec.contextStack.get("blueprintChildren")
        boolean isRoot = false
        if (currentChildren == null) {
            isRoot = true
            currentChildren = []
            sri.ec.contextStack.put("blueprintChildren", currentChildren)
        }

        if (logger.isDebugEnabled()) logger.debug("DeterministicVueRenderer.render() called for location: ${widgets.getLocation()}, isRoot: ${isRoot}")

        boolean authzDisabled = !sri.ec.artifactExecution.authzDisabled
        if (authzDisabled) sri.ec.artifactExecution.disableAuthz()
        try {
            walkWidgets(widgetsNode, currentChildren, sri)

            // If more screens in path but no SubscreensActive node added, force it
            if (sri.getActiveScreenHasNext() && !currentChildren.any { it["@type"] == "SubscreensActive" }) {
                handleSubscreensActive(null, currentChildren, sri)
            }
        } finally {
            if (authzDisabled) sri.ec.artifactExecution.enableAuthz()
        }

        if (isRoot) {
            Map<String, Object> blueprint = [
                "@context": "https://moqui.ai/contexts/ui",
                "@type": "ScreenBlueprint",
                "location": widgets.getLocation(),
                "children": currentChildren
            ]
            sri.getWriter().write(new JsonBuilder(blueprint).toPrettyString())
            sri.ec.contextStack.remove("blueprintChildren")
        }
    }

    protected void walkWidgets(MNode parentNode, List children, ScreenRenderImpl sri) {
        for (MNode child in parentNode.getChildren()) {
            handleNode(child, children, sri)
        }
    }

    protected void handleNode(MNode node, List children, ScreenRenderImpl sri) {
        String name = node.getName()
        switch(name) {
            case "screen":
            case "widgets":
            case "fail-widgets":
                walkWidgets(node, children, sri)
                break
            case "form-single":
                handleFormSingle(node, children, sri)
                break
            case "form-list":
                handleFormList(node, children, sri)
                break
            case "section":
                handleSection(node, children, sri)
                break
            case "dynamic-dialog":
                Map<String, Object> ddMap = [
                    "@type": "m-dynamic-dialog",
                    "attributes": evaluateAttributes(node, sri)
                ]
                String trans = node.attribute("transition") ?: node.attribute("url")
                String type = node.attribute("url-type") ?: "transition"
                if (trans) {
                    ddMap.attributes.url = sri.makeUrlByType(trans, type, node, "true").getPath()
                }
                children.add(ddMap)
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
                walkWidgets(node, linkMap.children, sri)
                children.add(linkMap)
                break
            case "container-row":
                Map<String, Object> rowMap = [
                    "@type": "m-container-row",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, rowMap.children, sri)
                children.add(rowMap)
                break
            case "row-col":
                Map<String, Object> colMap = [
                    "@type": "m-row-col",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, colMap.children, sri)
                children.add(colMap)
                break
            case "banner":
                Map<String, Object> bannerMap = [
                    "@type": "m-banner",
                    "attributes": evaluateAttributes(node, sri),
                    "children": []
                ]
                walkWidgets(node, bannerMap.children, sri)
                children.add(bannerMap)
                break
            case "container":
                handleContainer(node, children, sri)
                break
            case "label":
                handleLabel(node, children, sri)
                break
            case "subscreens-active":
            case "subscreens-panel":
                handleSubscreensActive(node, children, sri)
                break
            case "render-mode":
                handleRenderMode(node, children, sri)
                break
            case "text":
                handleText(node, children, sri)
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
            case "display":
                Map<String, Object> widgetMap = [
                    "@type": name,
                    "attributes": evaluateAttributes(node, sri)
                ]
                children.add(widgetMap)
                break
            case "menu-item":
                Map<String, Object> miMap = [
                    "@type": "m-menu-item",
                    "attributes": evaluateAttributes(node, sri)
                ]
                String name = node.attribute("name")
                String transition = node.attribute("transition") ?: node.attribute("url") ?: name
                String urlType = node.attribute("url-type") ?: "transition"
                if (transition) {
                    miMap.attributes.href = sri.makeUrlByType(transition, urlType, node, "true").getPath()
                }
                // Try to get defaults from subscreen if not specified
                if (name) {
                    def subItem = sri.getActiveScreenDef().getSubscreensItem(name)
                    if (subItem) {
                        if (!miMap.attributes.text) miMap.attributes.text = sri.ec.resource.expand(subItem.menuTitle ?: subItem.name, "")
                        if (!miMap.attributes.icon) miMap.attributes.icon = sri.ec.resource.expand(subItem.menuImage ?: "", "")
                    }
                }
                children.add(miMap)
                break
            case "menu-dropdown":
                Map<String, Object> mdMap = [
                    "@type": "m-menu-dropdown",
                    "attributes": evaluateAttributes(node, sri)
                ]
                String name = node.attribute("name")
                String trans = node.attribute("transition")
                if (name) {
                     mdMap.attributes["target-url"] = sri.makeUrlByType(name, "transition", node, "true").getPath()
                     def subItem = sri.getActiveScreenDef().getSubscreensItem(name)
                     if (subItem) {
                         if (!mdMap.attributes.text) mdMap.attributes.text = sri.ec.resource.expand(subItem.menuTitle ?: subItem.name, "")
                         if (!mdMap.attributes.icon) mdMap.attributes.icon = sri.ec.resource.expand(subItem.menuImage ?: "", "")
                     }
                }
                if (trans) {
                    mdMap.attributes["transition-url"] = sri.makeUrlByType(trans, "transition", node, "true").getPath()
                }
                children.add(mdMap)
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
                walkWidgets(node, bpMap.children, sri)
                children.add(bpMap)
                break
            default:
                if (logger.isDebugEnabled()) logger.debug("Handling unknown node: ${name} at ${sri.getActiveScreenDef().getLocation()}")
                Map<String, Object> mapNode = [
                    "@type": name,
                    "attributes": evaluateAttributes(node, sri)
                ]
                if (!node.getChildren().isEmpty()) {
                    mapNode.children = []
                    walkWidgets(node, mapNode.children, sri)
                }
                children.add(mapNode)
                break
        }
    }

    protected void handleFormSingle(MNode node, List children, ScreenRenderImpl sri) {
        String formName = node.attribute("name")
        FormInstance formInstance = sri.getFormInstance(formName)
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
                walkWidgets(fieldLayout, formMap.children, sri)
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

    protected void handleField(MNode fieldNode, List children, ScreenRenderImpl sri) {
        // Evaluate conditional fields or default field
        MNode activeField = null
        for (MNode cf in fieldNode.children("conditional-field")) {
            if (sri.ec.resource.condition(cf.attribute("condition"), "")) {
                activeField = cf
                break
            }
        }
        if (!activeField) activeField = fieldNode.first("default-field")
        
        if (activeField) {
            Map<String, Object> fieldMap = [
                "@type": "FormField",
                "name": fieldNode.attribute("name"),
                "attributes": evaluateAttributes(activeField, sri),
                "children": []
            ]
            walkWidgets(activeField, fieldMap.children, sri)
            children.add(fieldMap)
        }
    }

    protected void handleSection(MNode node, List children, ScreenRenderImpl sri) {
        MNode conditionNode = node.first("condition")
        boolean conditionMatch = true
        if (conditionNode != null) {
            conditionMatch = sri.ec.resource.condition(conditionNode.firstValue("expression"), "")
        }

        if (conditionMatch) {
            MNode widgetsNode = node.first("widgets")
            if (widgetsNode) walkWidgets(widgetsNode, children, sri)
        } else {
            MNode failWidgetsNode = node.first("fail-widgets")
            if (failWidgetsNode) walkWidgets(failWidgetsNode, children, sri)
        }
    }

    protected void handleContainer(MNode node, List children, ScreenRenderImpl sri) {
        Map<String, Object> containerMap = [
            "@type": "Container",
            "id": sri.ec.resource.expandNoL10n(node.attribute("id"), ""),
            "style": sri.ec.resource.expandNoL10n(node.attribute("style"), ""),
            "attributes": evaluateAttributes(node, sri),
            "children": []
        ]
        walkWidgets(node, containerMap.children, sri)
        children.add(containerMap)
    }

    protected void handleLabel(MNode node, List children, ScreenRenderImpl sri) {
        children.add([
            "@type": "Label",
            "text": sri.ec.resource.expand(node.attribute("text"), ""),
            "style": sri.ec.resource.expandNoL10n(node.attribute("style"), "")
        ])
    }

    protected void handleFormList(MNode node, List children, ScreenRenderImpl sri) {
        String formName = node.attribute("name")
        if (logger.isInfoEnabled()) logger.info("handleFormList called for form: ${formName}")
        FormInstance formInstance = sri.getFormInstance(formName)
        MNode formNode = formInstance.getFormNode()
        
        ScreenForm.FormListRenderInfo renderInfo = formInstance.makeFormListRenderInfo()
        Iterable list = renderInfo.getListObject(true)
        if (logger.isInfoEnabled()) logger.info("FormList ${formName} has ${list ? 'some' : 'no'} data")
        
        Map<String, Object> formMap = [
            "@type": "FormList",
            "name": formName,
            "header": [],
            "rows": []
        ]
        
        // Capture Header Structure
        for (MNode field in formNode.children("field")) {
            MNode headerField = field.first("header-field")
            if (headerField) {
                Map<String, Object> hField = [
                    "name": field.attribute("name"),
                    "title": sri.ec.resource.expand(field.attribute("title") ?: headerField.attribute("title"), ""),
                    "children": []
                ]
                walkWidgets(headerField, hField.children, sri)
                formMap.header.add(hField)
            }
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
        
        if (logger.isInfoEnabled()) logger.info("FormList ${formName} produced ${formMap.rows.size()} rows")
        children.add(formMap)
    }

    protected void handleSubscreensActive(MNode node, List children, ScreenRenderImpl sri) {
        Map<String, Object> subMap = [
            "@type": "SubscreensActive",
            "children": []
        ]
        
        List parentChildren = (List) sri.ec.contextStack.get("blueprintChildren")
        sri.ec.contextStack.put("blueprintChildren", subMap.children)
        try {
            sri.renderSubscreen()
        } finally {
            sri.ec.contextStack.put("blueprintChildren", parentChildren)
        }
        
        children.add(subMap)
    }

    protected void handleSubscreensMenu(MNode node, List children, ScreenRenderImpl sri) {
        // Menu data can be retrieved from sri.getMenuData() or similar
        children.add([
            "@type": "SubscreensMenu",
            "id": node.attribute("id")
        ])
    }

    protected void handleRenderMode(MNode node, List children, ScreenRenderImpl sri) {
        // Only walk children if this render-mode matches our current mode (qjson)
        // or if it has no type specified (default), or if it contains html/qvt2/qjson
        String type = node.attribute("type")
        if (!type || type == sri.getRenderMode() || type == "qjson" || type.contains("html") || type.contains("qvt2")) {
            walkWidgets(node, children, sri)
        }
    }

    protected void handleText(MNode node, List children, ScreenRenderImpl sri) {
        String text = node.getText()
        if (text != null && text.trim() == '${sri.renderSubscreen()}') {
            handleSubscreensActive(node, children, sri)
            return
        }
        
        // Handle literal text or template
        String type = node.attribute("type")
        if (!type || type == sri.getRenderMode() || type == "qjson") {
            String expandedText = node.attribute("template") == "true" ? sri.ec.resource.expand(text, "") : text
            if (expandedText != null && !expandedText.trim().isEmpty()) {
                children.add([
                    "@type": "Text",
                    "text": expandedText,
                    "location": node.attribute("location")
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
