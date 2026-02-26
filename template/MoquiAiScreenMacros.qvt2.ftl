<#include "runtime://template/screen-macro/DefaultScreenMacros.qvt2.ftl" /> 

<#macro "screen-layout"><#if sri.getRenderMode() != "qjson">
    <m-screen-layout view="${.node["@view"]!"hHh lpR fFf"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-layout>
</#if></#macro>
<#macro "screen-header"><#if sri.getRenderMode() != "qjson">
    <m-screen-header :elevated="${(.node["@elevated"]! != "false")?string}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-header>
</#if></#macro>
<#macro "screen-drawer"><#if sri.getRenderMode() != "qjson">
    <m-screen-drawer side="${.node["@side"]!"left"}" <#if .node["@model"]?has_content>v-model="${.node["@model"]}"</#if> behavior="${.node["@behavior"]!"default"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
         <#recurse>
    </m-screen-drawer>
</#if></#macro>

<#macro "screen-toolbar"><#if sri.getRenderMode() != "qjson">
    <m-screen-toolbar class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-toolbar>
</#if></#macro>
<#macro "screen-content"><#if sri.getRenderMode() != "qjson">
    <m-screen-content class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-content>
</#if></#macro>

<#macro "discussion-tree">
    <m-discussion-tree class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-discussion-tree>
</#macro>

<#macro "menu-item"><#if sri.getRenderMode() != "qjson">
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
</#if></#macro>

<#macro "menu-dropdown"><#if sri.getRenderMode() != "qjson">
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
</#if></#macro>

<#macro "bp-tabbar">
    <bp-tabbar <#if .node["@list"]?has_content>list="${.node["@list"]}"</#if> align="${.node["@align"]!"left"}" :no-caps="${(.node["@no-caps"]! != "false")?string}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </bp-tabbar>
</#macro>

<#macro "bp-tab">
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
    <bp-tab name="${name}" label="${text}" icon="${icon}" url="${urlInstance.pathWithParams}"></bp-tab>
</#macro>

<#macro "bp-parameter">
    <bp-parameter name="${.node["@name"]}" value="${.node["@value"]}" pinia-store="${.node["@pinia-store"]}" pinia-field="${.node["@pinia-field"]}"></bp-parameter>
</#macro>

<#macro "banner">
    <m-banner banner-class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-banner>
</#macro>
<#macro "dynamic-dialog">
    <#assign id = .node["@id"]!"">
    <#assign transition = .node["@transition"]!"">
    <#assign urlType = .node["@url-type"]!"transition">
    <#assign urlInstance = sri.makeUrlByType(transition, urlType, .node, "true")>
    <m-dynamic-dialog id="${id}" url="${urlInstance.pathWithParams}" 
                      button-text="${.node["@button-text"]!""}" 
                      icon="${.node["@icon"]!""}"
                      title="${.node["@title"]!""}" 
                      width="${.node["@width"]!""}" 
                      class="${.node["@class"]!""}" 
                      style="${.node["@style"]!""}"></m-dynamic-dialog>
</#macro>
