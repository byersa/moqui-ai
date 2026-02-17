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
    <m-subscreens-active></m-subscreens-active>
</#macro>

<#macro "subscreens-menu">
    <m-subscreens-menu path-index="${.node["@pathIndex"]!"0"}"></m-subscreens-menu>
</#macro>

<#macro "discussion-tree">
    <m-discussion-tree class="${.node["@class"]!""}" style="${.node["@style"]!""}">
        <#recurse>
    </m-discussion-tree>
</#macro>
