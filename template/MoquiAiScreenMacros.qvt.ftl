<#include "runtime://template/screen-macro/DefaultScreenMacros.qvt.ftl" />

<#macro screen><#recurse></#macro>

<#macro @text>${.node}</#macro>

<#macro @element>
    <#assign nodeName = .node?node_name>
    <#-- Handle custom and Quasar components -->
    <#if nodeName?contains("-") || nodeName?starts_with("q-")>
        <!-- SPA COMPONENT: ${nodeName} -->
        <${nodeName} <#list (.node.@@attributes?keys)![] as attrName>${attrName}="${.node["@"+attrName]}" </#list>>
            <#recurse>
        </${nodeName}>
    <#else>
        <#recurse>
    </#if>
</#macro>

<#macro "render-mode"><#recurse></#macro>

<#macro widgets><#t>
    <#-- Core Shell Check: Using Moqui context stack to preserve state across sub-screen FTL environments -->
    <#if !ec.context.mShellRendered??>
        <#assign _ = ec.context.put("mShellRendered", "true")!"">
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <#list html_stylesheets?if_exists as styleLocation><link rel="stylesheet" href="${sri.buildUrl(styleLocation).url}" type="text/css"></#list>
            <#list html_scripts?if_exists as scriptLocation><script src="${sri.buildUrl(scriptLocation).url}" type="text/javascript"></script></#list>
            <#-- Component Collector: So scripts can register before Vue loads -->
            <script>
                if (!window.moquiPlayground) {
                    console.info("Initializing Moqui Sandbox Collector...");
                    window.moquiPlayground = {
                        _pendingComponents: {},
                        component: function(name, def) { 
                            this._pendingComponents[name] = def;
                            console.info("Collector: cached component " + name); 
                        }
                    };
                }
            </script>
        </head>
        <body id="moqui-spa-root">
            <div id="moqui-standalone">
                <q-layout view="lHh Lpr lFf">
                    <q-page-container>
                        <q-page padding>
                            <#recurse>
                        </q-page>
                    </q-page-container>
                </q-layout>
            </div>
            <#-- Output Core Scripts (Vue, Quasar, etc.) -->
            <#list footer_scripts?if_exists as scriptLocation><script src="${sri.buildUrl(scriptLocation).url}" type="text/javascript"></script></#list>
            <#-- Transition from Collector to Real Vue Instance -->
            <script>
                (function() {
                    if (!window.Vue) { console.error("Vue failed to load!"); return; }
                    var collector = window.moquiPlayground;
                    console.info("Moqui Sandbox: Hydrating from collector...");
                    
                    var playgroundApp = Vue.createApp({});
                    if (window.Quasar) playgroundApp.use(Quasar);
                    
                    // Migrate all cached components to the real app
                    for (var name in collector._pendingComponents) {
                        playgroundApp.component(name, collector._pendingComponents[name]);
                    }
                    
                    // Replace the collector with the real app
                    window.moquiPlayground = playgroundApp;
                    playgroundApp.mount('#moqui-standalone');
                    window.moquiPlayground._mounted = true;
                    console.info("Moqui Sandbox mounted successfully.");
                })();
            </script>
            <#assign scriptText = sri.getScriptWriterText()>
            <#if scriptText?has_content><script>${scriptText}</script></#if>
        </body>
        </html>
    <#else>
        <#recurse>
    </#if>
</#macro>

<#macro "label">
    <#assign text = ec.getResource().expand(.node["@text"]! .node.@@text! "", "")>
    <div class="text-inline ${.node["@class"]!}" style="${.node["@style"]!}">${text}</div>
</#macro>

<#macro "text">
    <#if !.node["@type"]?has_content || .node["@type"]?split(",")?seq_contains(sri.getRenderMode())>
        <#if .node["@location"]?has_content>
            <#assign text = ec.resource.getLocationText(.node["@location"], true)!"">
            <#if (.node["@template"]!"true") == "true"><#assign text = ec.resource.expand(text, "")></#if>
            ${text}
        <#else>
            <#assign inlineSource = .node.@@text!>
            <#if (.node["@template"]!"true") == "true">
                <#assign inlineTemplate = inlineSource?interpret>
                <@inlineTemplate/>
            <#else>
                ${inlineSource}
            </#if>
        </#if>
    </#if>
</#macro>

<#macro "script">
    <#assign src = .node["@src"]!>
    <#assign location = .node["@location"]!>
    <#if src?has_content>
        <script src="${src}" type="text/javascript"></script>
    <#elseif location?has_content>
        <#assign urlInstance = sri.makeUrlByType(location, "resource", .node, "false")>
        <script src="${urlInstance.pathWithParams}" type="text/javascript"></script>
    <#else>
        <script type="text/javascript"><#recurse></script>
    </#if>
</#macro>

<#-- Standard BP Macros Placeholder -->
<#macro "bp-tabbar"><#recurse></#macro>
<#macro "bp-tab"><#recurse></#macro>
<#macro "bp-parameter"><#recurse></#macro>
