<#include "runtime://template/screen-macro/DefaultScreenMacros.qvt2.ftl" /> 

<#macro "screen-layout">
    <m-screen-layout view="${.node["@view"]!"hHh lpR fFf"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-layout>
</#macro>

<#macro "screen-header">
    <m-screen-header :elevated="${(.node["@elevated"]! != "false")?string}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-header>
</#macro>

<#macro "screen-drawer">
    <m-screen-drawer side="${.node["@side"]!"left"}" <#if .node["@model"]?has_content>v-model="${.node["@model"]}"</#if> behavior="${.node["@behavior"]!"default"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
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
            <#if !icon?has_content><#assign icon = ec.getResource().expand(subItem.menuImage!"", "")></#if>
            <#if !transition?has_content><#assign transition = name></#if>
        </#if>
    </#if>

    <#assign urlInstance = sri.makeUrlByType(transition, urlType, .node, "true")>
    <m-link href="${urlInstance.pathWithParams}">
        <q-btn :flat="true" :no-caps="true" label="${text}" <#if icon?has_content>icon="${icon}"</#if> class="${.node["@class"]!}" style="${.node["@style"]!}"></q-btn>
    </m-link>
</#macro>

<#macro "menu-dropdown">
    <#assign name = .node["@name"]!"">
    <#assign text = ec.getResource().expand(.node["@text"]!"", "")>
    <#assign icon = ec.getResource().expand(.node["@icon"]!"", "")>
    <#assign transition = .node["@transition"]!"">
    <#assign piniaStore = .node["@pinia-store"]!"">
    <#assign piniaList = .node["@pinia-list"]!"">
    <#assign labelField = .node["@label-field"]!"label">
    <#assign keyField = .node["@key-field"]!"id">
    <#assign urlParameter = .node["@url-parameter"]!"">

    <#if name?has_content>
        <#assign subItem = sri.getActiveScreenDef().getSubscreensItem(name)!>
        <#if subItem?has_content>
            <#if !text?has_content><#assign text = ec.getResource().expand(subItem.menuTitle!subItem.name, "")></#if>
            <#if !icon?has_content><#assign icon = ec.getResource().expand(subItem.menuImage!"", "")></#if>
        </#if>
    </#if>

    <#assign targetUrlInstance = sri.makeUrlByType(name!?has_content?then(name, ""), "transition", .node, "true")>
    <#assign apiUrlInstance = sri.makeUrlByType(transition, "transition", .node, "true")>

    <m-menu-dropdown text="${text}" icon="${icon}" transition-url="${apiUrlInstance.pathWithParams}" pinia-store="${piniaStore}" pinia-list="${piniaList}" target-url="${targetUrlInstance.pathWithParams}" label-field="${labelField}" key-field="${keyField}" url-parameter="${urlParameter}" class="${.node["@class"]!}" style="${.node["@style"]!}"></m-menu-dropdown>
</#macro>

<#macro "tabbar-page">
    <m-tabbar-page align="${.node["@align"]!"left"}" :no-caps="${(.node["@no-caps"]! != "false")?string}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-tabbar-page>
</#macro>

<#macro "tab-page">
    <#assign name = .node["@name"]!"">
    <#assign text = ec.getResource().expand(.node["@text"]!"", "")>
    <#assign icon = ec.getResource().expand(.node["@icon"]!"", "")>
    <#assign urlType = .node["@url-type"]!"transition">
    <#assign url = .node["@url"]!name>

    <#if name?has_content>
        <#assign subItem = sri.getActiveScreenDef().getSubscreensItem(name)!>
        <#if subItem?has_content>
            <#if !text?has_content><#assign text = ec.getResource().expand(subItem.menuTitle!subItem.name, "")></#if>
            <#if !icon?has_content><#assign icon = ec.getResource().expand(subItem.menuImage!"", "")></#if>
        </#if>
    </#if>

    <#assign urlInstance = sri.makeUrlByType(url, urlType, .node, "true")>
    <m-tab-page name="${name}" label="${text}" icon="${icon}" url="${urlInstance.pathWithParams}"></m-tab-page>
</#macro>

<#macro "banner">
    <m-banner banner-class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-banner>
</#macro>
