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
        <q-layout view="hHh Lpr fFf" style="min-height: 100vh" class="bg-blue-grey-1">
            
            <q-header elevated class="bg-blue-grey-10 text-white" style="height: 64px;">
                <q-toolbar class="full-height shadow-10">
                    <!-- Project Switcher & NEW button -->
                    <div class="row items-center no-wrap bg-blue-grey-9 q-pa-xs rounded-borders shadow-inner q-mr-md" style="border: 1px solid rgba(255,255,255,0.1)">
                        <q-select v-model="currentComponent" :options="allProjects" dense standout dark 
                                  options-dark color="amber"
                                  bg-color="transparent"
                                  class="q-px-sm" 
                                  style="min-width: 180px;"
                                  @update:model-value="v => switchProject(v)">
                            <template v-slot:prepend>
                                <q-icon name="folder_shared" size="xs" color="amber"></q-icon>
                            </template>
                        </q-select>
                        <q-btn flat round dense icon="add_box" color="amber" @click="promptNewProject" class="q-ml-xs">
                            <q-tooltip>Initialize New Component</q-tooltip>
                        </q-btn>
                    </div>

                    <q-toolbar-title class="text-weight-bold" style="letter-spacing: 1.5px;">
                        <div class="row items-center no-wrap">
                            <q-icon name="auto_fix_high" color="amber" class="q-mr-sm shadow-1"></q-icon>
                            <span class="text-overline q-mr-sm text-blue-grey-3" style="font-size: 0.65rem;">AITREE CO-ARCHITECT</span>
                            <span class="text-amber">MCE <span class="text-white">v3.1</span></span>
                            <q-icon v-show="isSyncingShadow" name="cloud_done" color="green-4" size="sm" class="q-ml-md" />
                            <q-icon v-show="!isSyncingShadow" name="cloud_queue" color="grey-6" size="sm" class="q-ml-md" />
                        </div>
                    </q-toolbar-title>
                    
                    <q-space></q-space>

                    <q-chip :icon="isSyncing ? 'sync' : 'security'" 
                            :color="isSyncing ? 'amber' : 'blue-grey-8'" 
                            text-color="white" dense outline class="q-px-md q-mr-md" size="md">
                        {{ isSyncing ? 'SYNCHRONIZING...' : 'HIPAA COMPLIANCE ACTIVE' }}
                    </q-chip>

                    <q-btn flat round icon="account_circle" class="q-mr-sm">
                         <q-menu anchor="bottom right" self="top right" class="bg-blue-grey-10 text-white">
                             <q-list style="min-width: 150px">
                                 <q-item clickable v-close-popup>
                                     <q-item-section>Settings</q-item-section>
                                 </q-item>
                                 <q-item clickable v-close-popup class="text-negative">
                                     <q-item-section>Disconnect</q-item-section>
                                 </q-item>
                             </q-list>
                         </q-menu>
                    </q-btn>
                </q-toolbar>
            </q-header>

            <!-- Navigator Sidebar -->
            <q-drawer show-if-above v-model="leftDrawerOpen" side="left" bordered class="bg-blue-grey-10 text-white" :width="280" behavior="desktop" :breakpoint="0">
                <q-list padding class="q-mb-md">
                    <q-item-label header class="text-weight-bold text-uppercase text-indigo-3 opacity-80" style="font-size: 0.7rem;">Screen Navigator</q-item-label>
                    <q-item v-for="screen in allScreens" :key="screen" clickable v-ripple 
                            :active="screen === screenPath"
                            @click="switchScreen(screen)"
                            active-class="bg-blue-grey-8 text-amber shadow-5">
                        <q-item-section avatar>
                            <q-icon name="description" :color="screen === screenPath ? 'amber' : 'blue-grey-4'"></q-icon>
                        </q-item-section>
                        <q-item-section>
                            <q-item-label>{{ screen }}</q-item-label>
                        </q-item-section>
                    </q-item>
                </q-list>

                <q-separator dark inset class="q-my-sm"></q-separator>

                <q-list padding>
                    <q-item-label header class="text-weight-bold text-uppercase text-indigo-3 opacity-80" style="font-size: 0.7rem;">Structure Navigator</q-item-label>
                    <q-item v-for="comp in (blueprint.structure || [])" :key="comp.id" clickable v-ripple 
                            :active="selectedComponent && selectedComponent.id === comp.id"
                            @click="selectComponent(comp)"
                            active-class="bg-indigo-10 text-amber shadow-5">
                        <q-item-section avatar>
                            <q-icon name="layers" :color="selectedComponent && selectedComponent.id === comp.id ? 'amber' : 'indigo-4'"></q-icon>
                        </q-item-section>
                        <q-item-section>
                            <q-item-label class="text-weight-bold">{{ comp.id || comp.component }}</q-item-label>
                            <q-item-label caption class="text-indigo-3">{{ comp.component }}</q-item-label>
                        </q-item-section>
                    </q-item>
                </q-list>
            </q-drawer>
            
            <!-- Inspector & Chat Sidebar -->
            <q-drawer show-if-above v-model="rightDrawerOpen" side="right" bordered class="bg-blue-grey-9 shadow-10" :width="400" behavior="desktop" :breakpoint="0">
                <div class="column no-wrap" style="height: 100vh; display: flex; flex-direction: column;">
                    <!-- TOP: Inspector (60% fixed height) -->
                    <div style="height: 60%; display: flex; flex-direction: column; overflow: hidden;" class="bg-white rounded-borders q-ma-sm shadow-5">
                        <div class="q-pa-md text-overline text-weight-bold flex items-center text-indigo-10 bg-indigo-1 border-bottom shadow-1">
                            <q-icon name="tune" size="sm" class="q-mr-xs"></q-icon>
                            PROPERTIES
                        </div>
                        <q-scroll-area class="col q-pa-md">
                            <template v-if="selectedComponent">
                                <div class="q-gutter-sm">
                                    <q-input dense filled bg-color="grey-1" color="indigo"
                                             v-model="selectedComponent.properties.label" 
                                             label="Visual Label"
                                             label-color="indigo-10"
                                             @blur="saveProperty('label', selectedComponent.properties.label)"
                                             stack-label></q-input>
                                    
                                    <q-input dense filled bg-color="grey-1" color="indigo"
                                             v-model="selectedComponent.properties.name" 
                                             label="Runtime Binding ID"
                                             label-color="indigo-10"
                                             @blur="saveProperty('name', selectedComponent.properties.name)" stack-label></q-input>

                                    <!-- Dynamic Property Editor -->
                                    <template v-for="(val, key) in selectedComponent.properties" :key="key">
                                        <template v-if="key !== 'label' && key !== 'name'">
                                            <q-input v-if="typeof val !== 'boolean'"
                                                     dense filled bg-color="grey-1" color="indigo"
                                                     v-model="selectedComponent.properties[key]" 
                                                     :label="key.charAt(0).toUpperCase() + key.slice(1)"
                                                     label-color="indigo-10"
                                                     @blur="saveProperty(key, selectedComponent.properties[key])"
                                                     stack-label></q-input>
                                            <q-toggle v-else v-model="selectedComponent.properties[key]"
                                                      dense color="indigo"
                                                      :label="key.charAt(0).toUpperCase() + key.slice(1)"
                                                      class="full-width q-py-sm q-px-md bg-grey-1 rounded-borders"
                                                      @update:model-value="v => saveProperty(key, v)"></q-toggle>
                                        </template>
                                    </template>
                                </div>
                            </template>
                            <div v-else class="fit flex flex-center text-grey-4 column q-pa-xl">
                                <q-icon name="dashboard_customize" size="100px" class="opacity-20"></q-icon>
                                <div class="text-subtitle1 text-center font-weight-light">SELECT NODE</div>
                            </div>
                        </q-scroll-area>
                    </div>

                    <q-separator color="indigo-10" size="2px" class="q-mx-sm"></q-separator>

                    <!-- BOTTOM: AI Peer Chat (40% fixed height) -->
                    <div style="height: 40%; display: flex; flex-direction: column; overflow: hidden; position: relative;" class="bg-indigo-10 text-white q-ma-sm rounded-borders shadow-5">
                        <div class="q-pa-md text-overline flex items-center bg-indigo-10 shadow-3" style="z-index: 10;">
                            <q-icon name="account_tree" size="sm" color="amber" class="q-mr-sm shadow-1"></q-icon>
                            CO-ARCHITECT
                        </div>
                        
                        <div class="q-pa-sm bg-indigo-9 text-indigo-1 shadow-3" style="z-index: 100; position: relative; pointer-events: auto;">
                            <q-input v-model="userInput" dense standout placeholder="Engage the Architect..." 
                                     autofocus
                                     @keyup.enter="sendMessage"
                                     :disable="isSending"
                                     bg-color="indigo-8"
                                     color="white"
                                     class="shadow-5"
                                     style="pointer-events: auto;">
                                <template v-slot:after>
                                    <q-btn round dense flat icon="arrow_upward" color="amber" 
                                           @click="sendMessage" :loading="isSending" class="bg-indigo-7 shadow-2"></q-btn>
                                </template>
                            </q-input>
                            <div class="row justify-center q-mt-xs">
                                <q-btn flat dense no-caps size="xs" color="amber-3" label="SUGGEST ADDRESS BLOCK" @click="userInput = 'Add an address block'; sendMessage()"></q-btn>
                            </div>
                        </div>

                        <q-scroll-area class="col q-pa-md chat-messages">
                            <div v-for="(msg, i) in messages" :key="i" class="q-mb-md" 
                                 :class="msg.role === 'user' ? 'text-right' : 'text-left'">
                                <div :class="msg.role === 'user' ? 'bg-amber text-black shadow-10' : 'bg-indigo-8 text-white shadow-5'" 
                                     class="q-pa-md rounded-borders inline-block" style="max-width: 90%; border-radius: 12px; line-height: 1.4; border: 1px solid rgba(255,255,255,0.05)">
                                    <div class="text-overline opacity-60 q-mb-xs" style="font-size: 0.6rem;">{{ msg.role === 'user' ? 'YOU' : 'ARCHITECT' }}</div>
                                    <div class="text-body2 whitespace-pre-wrap">{{ msg.text }}</div>
                                </div>
                            </div>
                            <div v-if="isSending" class="flex flex-center q-py-lg">
                                <q-spinner-dots size="40px" color="amber"></q-spinner-dots>
                            </div>
                        </q-scroll-area>
                    </div>
                </div>
            </q-drawer>

            <q-page-container>
                <q-page class="q-pa-md bg-blue-grey-1">
                    <div class="bg-white shadow-10 rounded-borders q-mx-auto" style="max-width: 1000px; min-height: 85vh; border: 1px solid #ddd">
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
                const isSyncingShadow = ref(false);
                const flashSync = () => {
                    isSyncingShadow.value = true;
                    setTimeout(() => isSyncingShadow.value = false, 1200);
                };
                const mcpToken = "${mcpToken}";
                const componentName = '${componentName}';
                const screenPath = '${screenPath}';
                const allProjects = ref([]);
                const allScreens = ref([]);
                const currentComponent = ref(componentName);

                const fetchProjects = async () => {
                    try {
                        const response = await fetch('/rest/s1/moquiai/getComponents');
                        const data = await response.json();
                        allProjects.value = data.components || [];
                    } catch (e) {
                         console.error("Failed to fetch projects", e);
                    }
                };

                const fetchScreens = async () => {
                    try {
                        const response = await fetch('/rest/s1/moquiai/getScreens?componentName=' + componentName);
                        const data = await response.json();
                        allScreens.value = data.screens || [];
                    } catch (e) {
                         console.error("Failed to fetch screens", e);
                    }
                };

                const switchProject = (name) => {
                    if (name === currentComponent.value) return;
                    window.location.href = '/rest/s1/moquiai/render?componentName=' + name + '&screenPath=Welcome';
                };

                const switchScreen = (name) => {
                    if (name === screenPath) return;
                    window.location.href = '/rest/s1/moquiai/render?componentName=' + componentName + '&screenPath=' + name;
                };

                const promptNewProject = () => {
                    \$q.dialog({
                        title: 'New Project Synthesis',
                        message: 'Enter the name of your new Moqui component (e.g. medical-inventory):',
                        prompt: { model: '', type: 'text' },
                        cancel: true,
                        persistent: true,
                        dark: true
                    }).onOk(name => {
                        createProject(name);
                    });
                };

                const createProject = async (name) => {
                    isSyncing.value = true;
                    try {
                        const response = await fetch('/rest/s1/moquiai/createComponent', {
                            method: 'POST',
                            headers: { 
                                'Content-Type': 'application/json',
                                'X-CSRF-Token': '${ec.web.sessionToken}' 
                            },
                            body: JSON.stringify({ componentName: name })
                        });
                        const data = await response.json();
                        if (data.success) {
                            \$q.notify({ type: 'positive', message: data.message });
                            switchProject(name);
                        } else {
                            \$q.notify({ type: 'negative', message: data.message });
                        }
                    } catch (e) {
                        \$q.notify({ type: 'negative', message: 'Fatal Error: ' + e.message });
                    } finally {
                        isSyncing.value = false;
                    }
                };

                const leftDrawerOpen = ref(false);
                const rightDrawerOpen = ref(true);
                const selectedComponent = ref(null);
                const userInput = ref('');
                const messages = ref([]);
                const isSending = ref(false);

                const selectComponent = (comp) => {
                    if (!comp.properties) comp.properties = {};
                    selectedComponent.value = comp;
                };

                Vue.provide('selectedComponent', selectedComponent);
                Vue.provide('selectComponent', selectComponent);
                Vue.provide('rightDrawerOpen', rightDrawerOpen);

                const saveProperty = async (id, name, val) => {
                    if (!val && val !== false && val !== 0) return;
                    console.log("Saving property:", id, name, val);
                    const response = await fetch('/rest/s1/moquiai/saveBlueprint', {
                        method: 'POST',
                        headers: { 
                            'Content-Type': 'application/json',
                            'X-CSRF-Token': '${ec.web.sessionToken}' 
                        },
                        body: JSON.stringify({ 
                            componentName: componentName, 
                            screenPath: screenPath,
                            selectedId: id,
                            properties: { [name]: val }
                        })
                    });
                    const data = await response.json();
                    if (data.success) {
                        \$q.notify({ type: 'positive', message: 'Property Saved', position: 'bottom-right', timeout: 800 });
                        flashSync();
                    }
                };

                const sendMessage = async (inputArg) => {
                    // Check if inputArg is a Boolean (our hidden flag) or an Event
                    const isHidden = (typeof inputArg === 'boolean') ? inputArg : false;
                    
                    console.log("AI Prompt Sending Triggered. Hidden:", isHidden, "userInput:", userInput.value);
                    
                    if (!userInput.value.trim() || isSending.value) return;
                    const text = userInput.value;
                    userInput.value = '';
                    if (!isHidden) messages.value.push({ role: 'user', text });
                    
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
                                componentName: componentName || null, 
                                screenPath: screenPath || null,
                                selectedId: selectedComponent.value?.id || null,
                                selectedProps: selectedComponent.value?.properties || {}
                            })
                        });
                        
                        if (!response.ok) {
                             const errBody = await response.text();
                             console.error("400 Error details:", errBody);
                             throw new Error("Server Error " + response.status + ": " + (errBody || "No details"));
                        }
                        
                        const data = await response.json();
                        console.log("AI Prompt Response received:", data);
                        const result = data.aiResponse || (data.result || 'Done.');
                        messages.value.push({ role: 'ai', text: result });
                        
                        // Pro-active UI Refresh if it sounds like we added, injected, modeled or suggested something
                        if (result.includes('Added') || result.includes('inject') || result.includes('model') || result.includes('suggest')) {
                            isSyncing.value = true;
                            const newBlueprint = await BlueprintClient.fetchBlueprint(componentName, screenPath);
                            if (newBlueprint) {
                                blueprint.value = newBlueprint;
                                flashSync();
                            }
                            setTimeout(() => isSyncing.value = false, 1000);
                        }
                    } catch (e) {
                        console.error("AI prompt failed", e);
                        messages.value.push({ role: 'ai', text: 'FATAL: AI peer communication failure. ' + e.message });
                        \$q.notify({ type: 'negative', message: 'Peer Error: ' + e.message, position: 'top' });
                    } finally {
                        isSending.value = false;
                    }
                };

                onMounted(() => {
                    BlueprintClient.setupSSE(componentName, screenPath, (newBlueprint) => {
                        blueprint.value = newBlueprint;
                        isSyncing.value = true;
                        flashSync();
                        setTimeout(() => isSyncing.value = false, 1500);
                    }, (cmd) => {
                        BlueprintClient.processCommand(blueprint.value, cmd);
                        isSyncing.value = true;
                        flashSync();
                        setTimeout(() => isSyncing.value = false, 500);
                    });
                    fetchProjects();
                    fetchScreens();
                    
                    // Empty State Trigger: Auto-start onboarding
                    const struct = blueprint.value.structure;
                    if (!struct || struct.length === 0) {
                        console.log("Empty blueprint detected, triggering onboarding...");
                        setTimeout(() => {
                            if (!isSending.value) {
                                userInput.value = 'INIT';
                                sendMessage(true); // Call with hidden=true to hide "INIT" from chat
                            }
                        }, 1200);
                    } else {
                        messages.value.push({ role: 'ai', text: 'Moqui AI Architect re-initialized. How can I assist?' });
                    }
                });

                return { 
                    blueprint, isSyncing, isSyncingShadow, mcpToken, componentName, screenPath,
                    leftDrawerOpen, rightDrawerOpen, selectedComponent, selectComponent,
                    userInput, messages, isSending, sendMessage,
                    saveProperty, allProjects, allScreens, currentComponent, switchProject, switchScreen, promptNewProject
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
