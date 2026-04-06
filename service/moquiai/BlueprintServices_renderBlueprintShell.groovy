import org.moqui.context.ExecutionContext
import groovy.json.JsonOutput

ExecutionContext ec = context.ec

// 1. Get initial blueprint payload
def blueprintResult = ec.service.sync().name("moquiai.BlueprintServices.get#Blueprint")
    .parameter("componentName", componentName)
    .parameter("screenPath", screenPath).call()

def blueprint = blueprintResult.blueprint ?: [:]
def blueprintJson = JsonOutput.toJson(blueprint)

// 1.5 Get WebMCP Connection Token
def tokenResult = ec.service.sync().name("moquiai.WebMcpServices.get#ConnectionToken").call()
def mcpToken = tokenResult.connectionToken

// 2. Generate Shell HTML
// Quasar 2, Vue 3, Google Fonts
def vueCdn = "https://cdn.jsdelivr.net/npm/vue@3/dist/vue.global.js"
def quasarCdnJs = "https://cdn.jsdelivr.net/npm/quasar@2.14.2/dist/quasar.umd.prod.js"
def quasarCdnCss = "https://cdn.jsdelivr.net/npm/quasar@2.14.2/dist/quasar.prod.css"

// Embedded Client Script for reliability
def clientScriptLocation = "component://moqui-ai/webroot/js/BlueprintClient.js"
def clientScript = ec.resource.getLocationReference(clientScriptLocation).getText()

context.html = """
<!DOCTYPE html>
<html>
<head>
    <title>Aitree Blueprint Launcher: ${screenPath}</title>
    <link href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,500,700,900|Material+Icons" rel="stylesheet" type="text/css">
    <link href="${quasarCdnCss}" rel="stylesheet" type="text/css">
    <style>
        [v-cloak] { display: none !important; }
        .blueprint-mode { background: #fdfdfd; min-height: 100vh; }
        .q-layout { min-height: 100vh !important; }
    </style>
</head>
<body class="blueprint-mode q-pa-none q-ma-none">
    <div id="q-app" v-cloak>
        <q-layout view="hHh lpR fBf" style="min-height: 100vh">
            <q-page-container>
                <q-page class="q-pa-md">
                    <blueprint-renderer :blueprint="blueprint" />
                    
                    <!-- Live Preview Status Indicator -->
                    <q-page-sticky position="bottom-right" :offset="[24, 24]">
                        <q-chip :icon="isSyncing ? 'sync' : 'cloud_done'" 
                                :color="isSyncing ? 'warning' : 'positive'" 
                                text-color="white"
                                class="shadow-4 q-px-md"
                                size="lg">
                            {{ isSyncing ? 'Syncing...' : 'Live Preview Active' }}
                        </q-chip>
                    </q-page-sticky>
                </q-page>
            </q-page-container>
            
            <!-- Developer Sidebar (WebMCP) -->
            <q-drawer show-if-above side="right" bordered class="bg-grey-1" :width="300">
                <div class="q-pa-md">
                    <div class="text-subtitle1 text-weight-bold row items-center q-mb-md">
                        <q-icon name="code" size="sm" class="q-mr-sm" /> 
                        AI Connection
                    </div>
                    
                    <q-card flat bordered class="bg-white">
                        <q-card-section>
                            <div class="text-caption text-grey">Connection String</div>
                            <div class="text-body2 text-weight-medium text-break" style="word-break: break-all; font-family: monospace;">
                                mcp://localhost:8080/rest/s1/moquiai/getContext?token={{ mcpToken }}
                            </div>
                        </q-card-section>
                        <q-separator />
                        <q-card-actions align="center">
                            <q-chip outline color="primary" size="sm" class="full-width justify-center">
                                Ready for AI Bind
                            </q-chip>
                        </q-card-actions>
                    </q-card>
                </div>
            </q-drawer>

        </q-layout>
    </div>

    <script src="${vueCdn}"></script>
    <script src="${quasarCdnJs}"></script>
    <script src="/rest/s1/moquiai/getClientLibrary?v=1.8"></script>

    <script>
        const { createApp, ref, onMounted } = Vue;
        const { useQuasar } = Quasar;

        const app = createApp({
            setup() {
                const blueprint = ref(${blueprintJson});
                const isSyncing = ref(false);
                const mcpToken = "${mcpToken}";
                const componentName = "${componentName}";
                const screenPath = "${screenPath}";

                const refreshBlueprint = async () => {
                    try {
                        isSyncing.value = true;
                        const data = await BlueprintClient.fetchBlueprint(componentName, screenPath);
                        blueprint.value = data;
                    } catch (e) {
                        console.error('Blueprint sync failed', e);
                    } finally {
                        setTimeout(() => isSyncing.value = false, 1000);
                    }
                };

                onMounted(() => {
                    // Initialize SSE connection for real-time updates and commands
                    BlueprintClient.setupSSE(componentName, screenPath, (newBlueprint) => {
                        blueprint.value = newBlueprint;
                        isSyncing.value = true;
                        setTimeout(() => isSyncing.value = false, 1500);
                    }, (cmd) => {
                        // AI Command Execution Logic
                        if (cmd.action === 'addComponent') {
                            if (!blueprint.value.structure) blueprint.value.structure = [];
                            blueprint.value.structure.push(cmd.payload);
                        } else if (cmd.action === 'updateField') {
                            // Example recursive update could be added here
                            console.log("Architect Mode: Field updated", cmd.payload);
                        } else if (cmd.action === 'clear') {
                            blueprint.value.structure = [];
                        }
                        isSyncing.value = true;
                        setTimeout(() => isSyncing.value = false, 500);
                    });
                });

                return { blueprint, isSyncing, mcpToken };
            }
        });

        app.use(Quasar);
        app.use(BlueprintClient);
        
        BlueprintClient.loadMacros().then(() => {
            app.mount('#q-app');
        });
    </script>
</body>
</html>
"""

if (ec.web != null) {
    ec.web.sendTextResponse(context.html, "text/html", null)
}

return context
