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
    <#-- Core Shell Rendering: Ensure headers and footers are loaded EXACTLY ONCE for the whole screen tree -->
    <#if !ec.context.mShellRendered??>
        <#assign _ = ec.context.put("mShellRendered", "true")!"">
        <#-- Head content -->
        <#list html_stylesheets?if_exists as styleLocation><link rel="stylesheet" href="${sri.buildUrl(styleLocation).url}" type="text/css"></#list>
        <#list html_scripts?if_exists as scriptLocation><script src="${sri.buildUrl(scriptLocation).url}" type="text/javascript"></script></#list>
        <#-- Surgical Clearing: Stop Moqui's internal engine from seeing these ever again -->
        <#assign _ = html_stylesheets.clear()!"">
        <#assign _ = html_scripts.clear()!"">
        <#assign _ = footer_scripts.clear()!"">

        <#-- Now recurse to the actual screen content (this builds the DOM) -->
        <#recurse>

        <#-- Output Private Collection (moqui_ai_scripts) AFTER recursive content -->
        <#list moqui_ai_scripts?if_exists as scriptLocation>
            <#assign sl = scriptLocation?string>
            <#assign scriptUrl = "">
            <#if sl?starts_with("/") || sl?starts_with("http")>
                <#assign scriptUrl = sl>
            <#elseif sl?starts_with("component://")>
                <#assign scriptUrl = sri.makeUrlByType(sl, "resource", null, "false").pathWithParams>
            <#else>
                <#assign scriptUrl = sri.buildUrl(sl).url>
            </#if>
            <!-- DEBUG PRIVATE STACK: SRC=${sl} -> URL=${scriptUrl} -->
            <script src="${scriptUrl}" type="text/javascript"></script>
        </#list>

        <#assign scriptText = sri.getScriptWriterText()>
        <#if scriptText?has_content><script>${scriptText}</script></#if>
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
