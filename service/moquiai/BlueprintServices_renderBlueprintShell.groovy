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
        <q-layout view="hHh Lpr fFf" style="min-height: 100vh">
            
            <q-header elevated class="bg-dark text-white">
                <q-toolbar>
                    <q-btn flat round dense icon="menu" @click="leftDrawerOpen = !leftDrawerOpen" />
                    <q-toolbar-title>
                        <span class="text-overline q-mr-sm text-grey-5">Nursing Home Management</span>
                        <span class="text-weight-bold">Architect Mode</span>
                    </q-toolbar-title>
                    <q-chip :icon="isSyncing ? 'sync' : 'cloud_done'" 
                            :color="isSyncing ? 'warning' : 'positive'" 
                            text-color="white"
                            dense
                            class="q-px-md"
                            size="md">
                        {{ isSyncing ? 'Syncing...' : 'Live Preview Active' }}
                    </q-chip>
                </q-toolbar>
            </q-header>

            <!-- Navigator Sidebar -->
            <q-drawer show-if-above v-model="leftDrawerOpen" side="left" bordered class="bg-grey-1" :width="250">
                <q-list padding>
                    <q-item-label header>Screen Structure</q-label>
                    <q-item v-for="comp in (blueprint.structure || [])" :key="comp.id" clickable v-ripple 
                            :active="selectedComponent && selectedComponent.id === comp.id"
                            @click="selectComponent(comp)">
                        <q-item-section avatar>
                            <q-icon name="widgets" />
                        </q-item-section>
                        <q-item-section>
                            <q-item-label>{{ comp.id || comp.component }}</q-item-label>
                            <q-item-label caption>{{ comp.component }}</q-item-label>
                        </q-item-section>
                    </q-item>
                </q-list>
            </q-drawer>

            <!-- Inspector & Chat Sidebar -->
            <q-drawer show-if-above side="right" bordered class="bg-white" :width="320">
                <div class="column full-height">
                    <!-- Inspector (60%) -->
                    <div class="col-7 scroll q-pa-md">
                        <div class="text-subtitle2 text-weight-bold q-mb-sm flex items-center">
                            <q-icon name="edit_note" size="xs" class="q-mr-xs" />
                            Component Properties
                        </div>
                        <template v-if="selectedComponent">
                            <q-card flat bordered class="bg-grey-1">
                                <q-card-section>
                                    <div class="q-gutter-sm q-mt-sm">
                                        <!-- Core Properties (Always Present) -->
                                        <q-input dense outlined bg-color="white"
                                                 v-model="selectedComponent.properties.label" 
                                                 label="Label"
                                                 @blur="saveProperty('label', selectedComponent.properties.label)"
                                                 placeholder="Enter descriptive label" />
                                        
                                        <q-input dense outlined bg-color="white"
                                                 v-model="selectedComponent.properties.name" 
                                                 label="Name / Field ID"
                                                 @blur="saveProperty('name', selectedComponent.properties.name)" />

                                        <!-- Dynamic Property Editor (The Rest) -->
                                        <template v-for="(val, key) in selectedComponent.properties" :key="key">
                                            <template v-if="key !== 'label' && key !== 'name'">
                                                <q-input v-if="typeof val !== 'boolean'"
                                                         dense outlined bg-color="white"
                                                         v-model="selectedComponent.properties[key]" 
                                                         :label="key.charAt(0).toUpperCase() + key.slice(1)"
                                                         @blur="saveProperty(key, selectedComponent.properties[key])"
                                                         debounce="500">
                                                    <template v-slot:prepend v-if="key === 'icon'">
                                                        <q-icon :name="val" size="xs" />
                                                    </template>
                                                </q-input>
                                                <div v-else class="flex items-center justify-between q-py-xs bg-white q-px-sm rounded-borders border-grey-4" style="border: 1px solid #ddd">
                                                    <div class="text-caption text-grey-8">{{ key.charAt(0).toUpperCase() + key.slice(1) }}</div>
                                                    <q-toggle v-model="selectedComponent.properties[key]"
                                                              dense
                                                              @update:model-value="v => saveProperty(key, v)" />
                                                </div>
                                            </template>
                                        </template>

                                        <!-- ID is read-only metadata -->
                                        <div class="q-mt-sm">
                                            <div class="text-caption text-grey">Component ID</div>
                                            <div class="text-weight-bold text-primary text-overline">{{ selectedComponent.id }}</div>
                                        </div>
                                    </div>
                                </q-card-section>
                            </q-card>
                        </template>
                        <template v-else>
                            <div class="full-height flex flex-center text-grey-5 column">
                                <q-icon name="touch_app" size="md" />
                                <div class="text-caption">Select a component to inspect</div>
                            </div>
                        </template>
                    </div>

                    <q-separator />

                    <!-- AI Peer Chat (40%) -->
                    <div class="col-5 column bg-grey-1">
                        <div class="q-pa-sm text-subtitle2 flex items-center bg-grey-2 border-bottom">
                            <q-icon name="smart_toy" size="xs" color="primary" class="q-mr-xs" />
                            AI Peer
                        </div>
                        <div class="col scroll q-pa-md chat-messages">
                            <div v-for="(msg, i) in messages" :key="i" class="q-mb-md" 
                                 :class="msg.role === 'user' ? 'text-right' : 'text-left'">
                                <q-badge :color="msg.role === 'user' ? 'primary' : 'grey-8'" class="q-pa-sm shadow-1">
                                    {{ msg.text }}
                                </q-badge>
                            </div>
                        </div>
                        <div class="q-pa-sm border-top bg-white">
                            <q-input dense outlined v-model="userInput" placeholder="Ask AI Peer..." 
                                     @keyup.enter="sendMessage"
                                     :disable="isSending">
                                <template v-slot:after>
                                    <q-btn round dense flat icon="send" color="primary" 
                                           @click="sendMessage" :loading="isSending" />
                                </template>
                            </q-input>
                        </div>
                    </div>
                </div>
            </q-drawer>

            <q-page-container>
                <q-page class="q-pa-md bg-grey-2">
                    <div class="bg-white shadow-2 rounded-borders q-mx-auto" style="max-width: 1000px; min-height: 80vh">
                        <blueprint-renderer :blueprint="blueprint" ref="renderer" />
                    </div>
                </q-page>
            </q-page-container>
        </q-layout>
    </div>

    <script src="${vueCdn}"></script>
    <script src="${quasarCdnJs}"></script>
    <script src="/rest/s1/moquiai/getClientLibrary?v=2.1"></script>

    <script>
        const { createApp, ref, onMounted } = Vue;
        const { useQuasar } = Quasar;

        const app = createApp({
            setup() {
                const \$q = useQuasar();
                const blueprint = ref(${blueprintJson});
                const isSyncing = ref(false);
                const mcpToken = "${mcpToken}";
                const componentName = "${componentName}";
                const screenPath = "${screenPath}";

                const leftDrawerOpen = ref(false);
                const selectedComponent = ref(null);
                const userInput = ref('');
                const messages = ref([
                    { role: 'ai', text: 'Ready to architect this screen.' }
                ]);
                const isSending = ref(false);

                const selectComponent = (comp) => {
                    if (!comp.properties) comp.properties = {};
                    selectedComponent.value = comp;
                };

                Vue.provide('selectedComponent', selectedComponent);
                Vue.provide('selectComponent', selectComponent);

                const saveProperty = async (prop, value) => {
                    if (!selectedComponent.value) return;
                    console.log('Saving property ' + prop + '=' + value + ' for ' + (selectedComponent.value ? selectedComponent.value.id : 'none'));
                    
                    isSyncing.value = true;
                    try {
                        const response = await fetch('/rest/s1/moquiai/saveBlueprint', {
                            method: 'POST',
                            headers: { 
                                'Content-Type': 'application/json',
                                'X-CSRF-Token': '${ec.web.sessionToken}' 
                            },
                            body: JSON.stringify({
                                componentName,
                                screenPath,
                                blueprint: blueprint.value
                            })
                        });
                        const data = await response.json();
                        if (data.success || data.result?.success) {
                            console.log("Blueprint persistent save complete.");
                            \$q.notify({ type: 'positive', message: 'Blueprint Saved Permanently', position: 'bottom-right', timeout: 800 });
                            
                            // Milestone 6.2 - Proactive AI Awareness
                            if (prop === 'label' && value.toLowerCase().includes('ssn')) {
                                messages.value.push({ role: 'ai', text: "I see you're adding a Social Security Number. Should I apply the 'sensitive-field' macro and enable HIPAA auditing for this?" });
                            } else if (prop === 'label' && value.toLowerCase().includes('resident first name')) {
                                messages.value.push({ role: 'ai', text: "Label updated to 'Resident First Name'." });
                            }
                        } else {
                            \$q.notify({ type: 'negative', message: 'Save Failed: ' + (data.message || data.errors || 'Unknown Error'), position: 'bottom-right' });
                        }
                    } catch (e) {
                        console.error("Save failure", e);
                    } finally {
                        setTimeout(() => isSyncing.value = false, 500);
                    }
                };

                const sendMessage = async () => {
                    if (!userInput.value.trim() || isSending.value) return;
                    const text = userInput.value;
                    userInput.value = '';
                    messages.value.push({ role: 'user', text });
                    
                    isSending.value = true;
                    try {
                        const response = await fetch('/rest/s1/moquiai/postPrompt', {
                            method: 'POST',
                            headers: { 
                                'Content-Type': 'application/json',
                                'X-CSRF-Token': '${ec.web.sessionToken}' 
                            },
                            body: JSON.stringify({ 
                                prompt: text, 
                                componentName, 
                                screenPath,
                                selectedId: selectedComponent.value?.id,
                                selectedProps: selectedComponent.value?.properties
                            })
                        });
                        const data = await response.json();
                        messages.value.push({ role: 'ai', text: data.result || 'Processing prompt...' });
                    } catch (e) {
                        messages.value.push({ role: 'ai', text: 'Service communication error.' });
                    } finally {
                        isSending.value = false;
                    }
                };

                const findComponentById = (structure, id) => {
                    if (!structure) return null;
                    for (let comp of structure) {
                        if (comp.id === id) return comp;
                        if (comp.children) {
                            const found = findComponentById(comp.children, id);
                            if (found) return found;
                        }
                    }
                    return null;
                };

                onMounted(() => {
                    // Initialize SSE connection for real-time updates and commands
                    BlueprintClient.setupSSE(componentName, screenPath, (newBlueprint) => {
                        blueprint.value = newBlueprint;
                        isSyncing.value = true;
                        setTimeout(() => isSyncing.value = false, 1500);
                    }, (cmd) => {
                        // Flash-Safe AI/System Command Execution Logic
                        BlueprintClient.processCommand(blueprint.value, cmd);
                        isSyncing.value = true;
                        setTimeout(() => isSyncing.value = false, 500);
                    });
                });

                return { 
                    blueprint, isSyncing, mcpToken, 
                    leftDrawerOpen, selectedComponent, selectComponent,
                    userInput, messages, isSending, sendMessage,
                    saveProperty
                };
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
