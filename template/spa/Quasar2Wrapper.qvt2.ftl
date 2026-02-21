<#-- moqui-ai/template/spa/Quasar2Wrapper.qvt2.ftl -->
<#-- 
    This template encapsulates the loading of Vue 3, Vue Router, and Quasar 2 libraries.
    It is designed to be injected into the <pre-actions> or <render-mode> of a root SPA wrapper screen 
    to eliminate boilerplate scripts across multiple applications.
-->

<#-- 1. Authentication Check (if needed, though usually handled by wrapper xml) -->
<#if !ec.user.userId && requireAuth!false>
    ${ec.web.saveScreenLastInfo(null, null)}
    ${sri.sendRedirectAndStopRender('/Login')}
</#if>

<#-- 2. Base Libraries -->
${html_scripts.add('/libs/moment.js/moment-with-locales.min.js')}
${html_scripts.add('/libs/jquery/jquery.min.js')}

<#-- 3. Environment-aware Vue/Quasar Loading -->
<#assign instancePurpose = System.getProperty("instance_purpose")!"">

<#if !instancePurpose?has_content || instancePurpose == 'production'>
    <#-- PRODUCTION MODE: Minified libraries -->
    ${html_scripts.add('/js/MoquiLib.min.js')}
    ${footer_scripts.add('/libs/vue/vue.global.prod.min.js')}
    ${footer_scripts.add('/libs/vue/vue.global.js')}
    ${footer_scripts.add('/libs/vue-router/vue-router.global.js')}
    ${footer_scripts.add('/js/http-vue-loader/httpVueLoader.js')}
    ${footer_scripts.add('/libs/quasar/quasar.umd.prod.js')}
    ${footer_scripts.add('/libs/vue3-sfc-loader/vue3-sfc-loader.min.js')}
    ${footer_scripts.add('/routes.js' + ec.web.request.pathInfo)}
    ${footer_scripts.add('/js/WebrootVue.qvt2.min.js')}
<#else>
    <#-- DEVELOPMENT MODE: Unminified libraries for debugging -->
    ${html_scripts.add('/js/MoquiLib.js')}
    ${footer_scripts.add('/libs/vue/vue.global.js')}
    ${footer_scripts.add('/libs/vue/vue.global.js')} <#-- Double add exists in legacy HuddleWrapper context, preserved just in case -->
    ${footer_scripts.add('/libs/vue-router/vue-router.global.js')}
    ${footer_scripts.add('/js/http-vue-loader/httpVueLoader.js')}
    ${footer_scripts.add('/libs/quasar/quasar.umd.prod.js')}
    ${footer_scripts.add('/libs/vue3-sfc-loader/vue3-sfc-loader.min.js')}
    ${footer_scripts.add('/routes.js' + ec.web.request.pathInfo)}
    ${footer_scripts.add('/js/WebrootVue.qvt2.js')}
</#if>
