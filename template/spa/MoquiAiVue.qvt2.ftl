<#--
    MoquiAiVue.qvt2.ftl
    Standard SPA root injection for MoquiAi applications.
    Provides the #apps-root container and environment configuration for MoquiAiVue.qvt2.js.
-->
<input type="hidden" id="confMoquiSessionToken" value="${ec.web.sessionToken}">
<input type="hidden" id="confAppHost" value="${ec.web.getHostName(true)}">
<input type="hidden" id="confAppRootPath" value="${appRootPath!ec.web.servletContext.contextPath}">
<input type="hidden" id="confBasePath" value="${basePath!ec.web.servletContext.contextPath + '/apps'}">
<input type="hidden" id="confLinkBasePath" value="${linkBasePath!ec.web.servletContext.contextPath + '/qapps2'}">
<input type="hidden" id="confUserId" value="${ec.user.userId!''}">
<input type="hidden" id="confUsername" value="${ec.user.username!''}">
<input type="hidden" id="confLocale" value="${ec.user.locale.toLanguageTag()}">
<input type="hidden" id="confDarkMode" value="${ec.user.getPreference("QUASAR_DARK")!"false"}">
<input type="hidden" id="confLeftOpen" value="${ec.user.getPreference("QUASAR_LEFT_OPEN")!"false"}">

<#-- The MoquiAiVue app will mount elsewhere (e.g. in the main screen XML) -->


<script>
    window.quasarConfig = {
        brand: { info:'#1e7b8e' },
        notify: { progress:true, closeBtn:'X', position:'top-right' },
        loadingBar: { color:'primary' }
    }
</script>
