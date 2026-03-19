<#-- moqui-ai/template/spa/Quasar2Wrapper.qvt.ftl -->
<#-- 
    This template encapsulates the loading of Vue 3, Vue Router, and Quasar 2 libraries.
    It is designed to be injected into the <pre-actions> or <render-mode> of a root SPA wrapper screen 
    to eliminate boilerplate scripts across multiple applications.
-->

<#-- 1. Base Libraries -->
<script src="/libs/moment.js/moment-with-locales.min.js"></script>
<script src="/libs/jquery/jquery.min.js"></script>

<#-- 2. Environment-aware Vue/Quasar Loading -->
<#assign instancePurpose = Static["java.lang.System"].getProperty("instance_purpose")!"">

<#if !instancePurpose?has_content || instancePurpose == 'production'>
    <#-- PRODUCTION MODE: Minified libraries -->
    <script src="/js/MoquiLib.min.js"></script>
    <script src="https://unpkg.com/vue@3.3.4/dist/vue.global.prod.js"></script>
    <script src="https://unpkg.com/vue-router@4.2.4/dist/vue-router.global.prod.js"></script>
    <script src="/js/http-vue-loader/httpVueLoader.js"></script>
    <script src="https://unpkg.com/quasar@2.12.6/dist/quasar.umd.prod.js"></script>
    <script src="https://unpkg.com/vue3-sfc-loader@0.8.4/dist/vue3-sfc-loader.min.js"></script>
    <script src="${sri.buildUrl("routes.js").url}"></script>
    <script src="/moquiai/js/MoquiAiVue.qvt.js?v=${.now?long}"></script>
<#else>
    <#-- DEVELOPMENT MODE: Unminified libraries for debugging -->
    <script src="/js/MoquiLib.js"></script>
    <script src="https://unpkg.com/vue@3.3.4/dist/vue.global.js"></script>
    <script src="https://unpkg.com/vue-router@4.2.4/dist/vue-router.global.prod.js"></script>
    <script src="/js/http-vue-loader/httpVueLoader.js"></script>
    <script src="https://unpkg.com/quasar@2.12.6/dist/quasar.umd.prod.js"></script>
    <script src="https://unpkg.com/vue3-sfc-loader@0.8.4/dist/vue3-sfc-loader.min.js"></script>
    <script src="${sri.buildUrl("routes.js").url}"></script>
    <script src="/moquiai/js/MoquiAiVue.qvt.js?v=${.now?long}"></script>
</#if>
