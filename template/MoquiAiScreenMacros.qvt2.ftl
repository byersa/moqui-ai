<#include "runtime://template/screen-macro/DefaultScreenMacros.qvt2.ftl" /> 

<#macro "screen-layout">
    <m-screen-layout view="${.node["@view"]!"hHh lpR fFf"}" class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-screen-layout>
</#macro>

<#macro "screen-header">
    <m-screen-header <#if .node["@elevated"]! != "false">elevated</#if> class="${.node["@class"]!""}" style="${.node["@style"]!""}">
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
