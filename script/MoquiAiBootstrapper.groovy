import org.moqui.context.ExecutionContext

ExecutionContext ec = context.ec
long ts = System.currentTimeMillis()

ec.logger.info("MCE: in MoquiAiBootstrapper")
// Initialize standard Moqui lists if they are null (common in REST services)
if (context.html_scripts == null) context.html_scripts = []
if (context.html_stylesheets == null) context.html_stylesheets = []

// Now these will not throw NullPointerException
html_scripts.add('/libs/moment.js/moment-with-locales.min.js')
html_scripts.add('/libs/jquery/jquery.min.js')
// 1. Initialize Private Script and Stylesheet Stacks
if (context.moqui_ai_scripts == null) context.moqui_ai_scripts = []
if (context.moqui_ai_styles == null) context.moqui_ai_styles = []

// 2. Environment Detection
String instancePurpose = System.getProperty("instance_purpose")
boolean isProd = !instancePurpose || instancePurpose == 'production' 

// 3. Core External Libraries (Quasar, Vue, Konva)
html_scripts.add('/libs/moment.js/moment-with-locales.min.js')// Initialize standard Moqui lists if they are null (common in REST services)
if (context.html_scripts == null) context.html_scripts = []
if (context.html_stylesheets == null) context.html_stylesheets = []

// Initialize your custom AI lists
if (context.moqui_ai_scripts == null) context.moqui_ai_scripts = []
if (context.moqui_ai_styles == null) context.moqui_ai_styles = []

// Now these will not throw NullPointerException
html_scripts.add('/libs/moment.js/moment-with-locales.min.js')
html_scripts.add('/libs/jquery/jquery.min.js') 
html_scripts.add('/libs/jquery/jquery.min.js') 
html_scripts.add('https://unpkg.com/konva@9.2.0/konva.min.js') 

if (isProd) {
    html_scripts.add('/js/MoquiLib.min.js') 
    moqui_ai_scripts.add('https://unpkg.com/vue@3.3.4/dist/vue.global.prod.js') 
} else {
    html_scripts.add('/js/MoquiLib.js') 
    moqui_ai_scripts.add('https://unpkg.com/vue@3.3.4/dist/vue.global.js') 
}

// Stylesheets
moqui_ai_styles.add('https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700,900|Material+Icons') 
moqui_ai_styles.add('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200') 
moqui_ai_styles.add('https://unpkg.com/quasar@2.12.6/dist/quasar.prod.css') 

// SPA and State Management
moqui_ai_scripts.add('https://unpkg.com/vue-router@4.2.4/dist/vue-router.global.prod.js') 
moqui_ai_scripts.add('/js/http-vue-loader/httpVueLoader.js') 
moqui_ai_scripts.add('https://unpkg.com/quasar@2.12.6/dist/quasar.umd.prod.js') 
moqui_ai_scripts.add('https://unpkg.com/vue3-sfc-loader@0.8.4/dist/vue3-sfc-loader.min.js') 
moqui_ai_scripts.add('https://unpkg.com/vue-demi@0.14.7/lib/index.iife.js') 
moqui_ai_scripts.add('https://unpkg.com/pinia@2.1.7/dist/pinia.iife.js') 

// 4. Moqui-AI Core Client Logic (WebMCP and Blueprint Engine)
String scriptPrefix = "/moquiai/js/"
moqui_ai_scripts.add(scriptPrefix + "webmcp.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "stores/meetingsStore.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "BlueprintClient.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "MoquiAiVue.qvt.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "MoquiCanvasEditor.qvt.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "MoquiAiApp.qvt.js?v=" + ts) 
moqui_ai_scripts.add(scriptPrefix + "components/ScreenSplit.qvt.js?v=" + ts)