<#--
    MoquiAiShellMacros.html.ftl
    Minimal HTML macros for MoquiAi semantic tags, used for the SPA shell.
    Provides pass-through to Quasar components without catch-all pollution.
-->

<#include "runtime://template/screen-macro/DefaultScreenMacros.html.ftl"/>


<#macro "screen-layout">
    <m-screen-layout view="${.node["@view"]!}" class="${.node["@class"]!}" style="${.node["@style"]!}">
        <#recurse>
    </m-screen-layout>
</#macro>

<#macro "screen-header">
    <m-screen-header :elevated="${.node["@elevated"]!"true"}" class="${.node["@class"]!}" style="${.node["@style"]!}">
        <#recurse>
    </m-screen-header>
</#macro>

<#macro "screen-drawer">
    <m-screen-drawer side="${.node["@side"]!"left"}" <#if .node["@model"]?has_content>v-model="${.node["@model"]}"</#if> behavior="${.node["@behavior"]!"default"}" class="${.node["@class"]!}" style="${.node["@style"]!}">
         <#recurse>
    </m-screen-drawer>
</#macro>

<#macro "screen-toolbar">
    <m-screen-toolbar class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-toolbar>
</#macro>

<#macro "screen-content">
    <m-screen-content class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-content>
</#macro>

<#macro "subscreens-active">
    <m-subscreens-active item-name="${.node["@item-name"]! .node["@itemName"]! ""}" 
        path-index="${.node["@path-index"]! .node["@pathIndex"]! "-1"}"></m-subscreens-active>
</#macro>

<#macro "subscreens-menu">
    <m-subscreens-menu path-index="${.node["@pathIndex"]!"0"}" type="${.node["@style"]! .node["@type"]! "drawer"}"></m-subscreens-menu>
</#macro>

<#macro "discussion-tree">
    <m-discussion-tree class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-discussion-tree>
</#macro>

<#macro "menu-item">
    <#assign name = .node["@name"]!"">
    <#assign text = ec.getResource().expand(.node["@text"]!"", "")>
    <#assign icon = ec.getResource().expand(.node["@icon"]!"", "")>
    <#assign transition = .node["@transition"]! .node["@url"]!>
    <#assign urlType = .node["@url-type"]!"transition">

    <#if name?has_content>
        <#assign subItem = sri.getActiveScreenDef().getSubscreensItem(name)!>
        <#if subItem?has_content>
            <#if !text?has_content><#assign text = ec.getResource().expand(subItem.menuTitle!subItem.name, "")></#if>
            <#if !icon?has_content><#assign icon = ""></#if>
            <#if !transition?has_content><#assign transition = name></#if>
        </#if>
    </#if>

    <#assign urlInstance = sri.makeUrlByType(transition, urlType, .node, "true")>
    <m-menu-item data-maria-id="${name}" href="${urlInstance.pathWithParams}" text="${text}" icon="${icon}" class="${.node["@class"]!}" style="${.node["@style"]!}"></m-menu-item>
</#macro>

<#macro "menu-dropdown">
    <#assign name = .node["@name"]!"">
    <#assign text = ec.getResource().expand(.node["@text"]!"", "")>
    <#assign icon = ec.getResource().expand(.node["@icon"]!"", "")>
    <#assign transition = .node["@transition"]!"">
    <#assign labelField = .node["@label-field"]!"label">
    <#assign keyField = .node["@key-field"]!"id">
    <#assign urlParameter = .node["@url-parameter"]!"">
    <#assign targetUrlAttr = .node["@target-url"]!name>

    <#if name?has_content>
        <#assign subItem = sri.getActiveScreenDef().getSubscreensItem(name)!>
        <#if subItem?has_content>
            <#if !text?has_content><#assign text = ec.getResource().expand(subItem.menuTitle!subItem.name, "")></#if>
            <#if !icon?has_content><#assign icon = ""></#if>
        </#if>
    </#if>

    <#assign targetUrlInstance = sri.makeUrlByType(targetUrlAttr, "transition", .node, "true")>
    <#assign apiUrlInstance = sri.makeUrlByType(transition, "transition", .node, "true")>

    <m-menu-dropdown data-maria-id="${name}" text="${text}" icon="${icon}" transition-url="${apiUrlInstance.pathWithParams}" target-url="${targetUrlInstance.pathWithParams}" label-field="${labelField}" key-field="${keyField}" url-parameter="${urlParameter}" class="${.node["@class"]!}" style="${.node["@style"]!}"></m-menu-dropdown>
</#macro>

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
