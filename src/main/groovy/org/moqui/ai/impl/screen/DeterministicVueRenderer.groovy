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
            case "subscreens-menu":
                handleSubscreensMenu(node, children, sri)
                break
            case "render-mode":
                handleRenderMode(node, children, sri)
                break
            case "text":
                handleText(node, children, sri)
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
        String sectionName = node.attribute("name")
        // Mapping section to a semantic node
        Map<String, Object> sectionMap = [
            "@type": "Section",
            "name": sectionName,
            "children": []
        ]
        
        // Push the children list to the context so renderSection can populate it
        List parentChildren = (List) sri.ec.contextStack.get("blueprintChildren")
        sri.ec.contextStack.put("blueprintChildren", sectionMap.children)
        try {
            sri.renderSection(sectionName)
        } finally {
            sri.ec.contextStack.put("blueprintChildren", parentChildren)
        }
        
        children.add(sectionMap)
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
        // Form list handling is complex, similar to form-single but with rows
        children.add([
            "@type": "FormList",
            "name": node.attribute("name"),
            "note": "FormList handling pending refinement"
        ])
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
        // or if it has no type specified (default)
        String type = node.attribute("type")
        if (!type || type == sri.getRenderMode() || type == "qjson") {
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
