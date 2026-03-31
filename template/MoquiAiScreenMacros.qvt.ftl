<#include "runtime://template/screen-macro/DefaultScreenMacros.qvt.ftl" /> 

<#macro @text>${.node}</#macro>
<#macro @element><#recurse></#macro>

<#macro "label">
    <!-- LABEL DEBUG -->
    <#assign text = ec.getResource().expand(.node["@text"]! .node.@@text! "", "")>
    <div class="text-inline ${.node["@class"]!}" style="${.node["@style"]!}">${text}</div>
</#macro>

<#macro "render-mode">
    <#recurse>
</#macro>

<#macro renderText textNode>
    <#if textNode["@location"]?has_content>
        <#assign text = ec.resource.getLocationText(textNode["@location"], true)!"">
        <#if (textNode["@template"]!"true") == "true"><#assign text = ec.resource.expand(text, "")></#if>
        ${text}
    <#else>
        <#assign inlineSource = textNode.@@text!>
        <#if (textNode["@template"]!"true") == "true">
            <#assign inlineTemplate = inlineSource?interpret>
            <@inlineTemplate/>
        <#else>
            ${inlineSource}
        </#if>
    </#if>
</#macro>

<#macro text>
    <#if !.node["@type"]?has_content || .node["@type"]?split(",")?seq_contains(sri.getRenderMode())>
        <@renderText textNode=.node/>
    </#if>
</#macro>

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
    <#assign piniaStore = .node["@pinia-store"]!"">
    <#assign piniaList = .node["@pinia-list"]!"">
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

    <m-menu-dropdown data-maria-id="${name}" text="${text}" icon="${icon}" transition-url="${apiUrlInstance.pathWithParams}" pinia-store="${piniaStore}" pinia-list="${piniaList}" target-url="${targetUrlInstance.pathWithParams}" label-field="${labelField}" key-field="${keyField}" url-parameter="${urlParameter}" class="${.node["@class"]!}" style="${.node["@style"]!}"></m-menu-dropdown>
</#macro>

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
            <#if !icon?has_content><#assign icon = ""></#if>
        </#if>
    </#if>

    <#assign urlInstance = sri.makeUrlByType(url, urlType, .node, "true")>
    <bp-tab data-maria-id="${name}" name="${name}" label="${text}" icon="${icon}" url="${urlInstance.pathWithParams}"></bp-tab>
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
<#macro "custom-screen">
    <#assign name = .node["@name"]!"">
    <#if name?has_content>
        <#assign location = sri.getActiveScreenDef().location>
        <#assign dir = location?keep_before_last("/")>
        <#assign scriptLocation = dir + "/" + name + ".qvt.js">
        <#assign scriptText = ec.resource.getLocationText(scriptLocation, false)!"">
        <#if scriptText?has_content>
            ${sri.renderText(scriptText, "qvt")}
        </#if>
    </#if>
</#macro>
<#macro "screen-split">
    <#assign list = .node["@list"]!"" >
    <#assign component = .node["@component"]!"">
    <#assign dynamicComponent = .node["@dynamic-component"]!"">
    <#assign failMessage = .node["@fail-message"]!"">
    <#assign failScreen = .node["@fail-screen"]!"">
    <#assign model = .node["@model"]!"50">
    <#assign horizontal = .node["@horizontal"]!"false">
    <#assign limits = .node["@limits"]!"10,90">

    <m-screen-split
        <#if list?has_content>list="${list}"</#if>
        <#if component?has_content>component="${component}"</#if>
        <#if dynamicComponent?has_content>dynamic-component="${dynamicComponent}"</#if>
        <#if failMessage?has_content>fail-message="${failMessage}"</#if>
        <#if failScreen?has_content>fail-screen="${failScreen}"</#if>
        model="${model}"
        horizontal="${horizontal}"
        limits="${limits}"
        <#if .node["@class"]?has_content>extra-class="${.node["@class"]}"</#if>
        <#if .node["@style"]?has_content>extra-style="${.node["@style"]}"</#if>>
        <#recurse>
    </m-screen-split>
</#macro>
<#macro "form-query">
    <m-form-query name="${.node["@name"]}" form-event-string="${.node["@form-event-string"]!""}" 
                  class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-form-query>
</#macro>

<#macro "form-query-field">
    <#assign name = .node["@name"]>
    <#assign type = .node["@type"]!"text">
    <#assign label = .node["@label"]!.node["@title"]!name>
    <#assign operator = .node["@operator"]!"">
    <#assign enumTypeId = .node["@enum-type-id"]!"">
    <#assign statusTypeId = .node["@status-type-id"]!"">
    <#assign optionsUrl = .node["@options-url"]!"">
    <#assign optionsParameters = .node["@options-parameters"]!"">

    <#if enumTypeId?has_content && !optionsUrl?has_content>
        <#assign optionsUrl = "/aitree/getEnumerations">
        <#assign optionsParameters = '{"enumTypeId":"${enumTypeId}"}'>
    </#if>
    <#if statusTypeId?has_content && !optionsUrl?has_content>
        <#assign optionsUrl = "/aitree/getStatusItems">
        <#assign optionsParameters = '{"statusTypeId":"${statusTypeId}"}'>
    </#if>

    <m-form-query-field data-maria-id="${name}" name="${name}" type="${type}" label="${label}"
                        operator="${operator}" options-url="${optionsUrl}"
                        <#if optionsParameters?has_content>:options-parameters='${optionsParameters}'</#if>
                        options-load-init="true"></m-form-query-field>
</#macro>
<#macro "container-row">
    <div class="row ${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </div>
</#macro>

<#macro "row-col">
    <#assign class = .node["@class"]!"" >
    <#if .node["@cols"]?has_content><#assign class = class + " col-" + .node["@cols"]></#if>
    <#if .node["@xs"]?has_content><#assign class = class + " col-xs-" + .node["@xs"]></#if>
    <#if .node["@sm"]?has_content><#assign class = class + " col-sm-" + .node["@sm"]></#if>
    <#if .node["@md"]?has_content><#assign class = class + " col-md-" + .node["@md"]></#if>
    <#if .node["@lg"]?has_content><#assign class = class + " col-lg-" + .node["@lg"]></#if>
    <#if .node["@xl"]?has_content><#assign class = class + " col-xl-" + .node["@xl"]></#if>
    <#if !class?contains("col")><#assign class = class + " col"></#if>
    <div class="${class?trim}" style="${.node["@style"]!""}">
        <#recurse>
    </div>
</#macro>
