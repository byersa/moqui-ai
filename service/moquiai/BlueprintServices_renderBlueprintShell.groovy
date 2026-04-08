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
                    <!-- ACTIVE BREADCRUMB -->
                    <div class="row items-center q-mx-md bg-blue-grey-9 q-px-md q-py-xs rounded-borders shadow-inner" style="border: 1px solid rgba(255,255,255,0.1)">
                        <q-icon name="folder_open" size="xs" color="amber-3" class="q-mr-sm"></q-icon>
                        <div class="column">
                            <div class="text-overline text-amber-5" style="line-height: 1; font-size: 0.6rem;">ACTIVE WORKSPACE</div>
                            <div class="text-subtitle2 text-white text-weight-bold" style="line-height: 1;">${componentName}</div>
                        </div>
                        <q-btn flat round dense icon="arrow_drop_down" color="white" class="q-ml-sm">
                            <q-menu dark class="bg-blue-grey-10">
                                <q-list style="min-width: 200px">
                                    <q-item-label header class="text-amber">OPEN WORKSPACE</q-item-label>
                                    <q-item v-for="p in allProjects" :key="p" clickable v-close-popup @click="switchProject(p)">
                                        <q-item-section avatar><q-icon name="dashboard" size="xs" /></q-item-section>
                                        <q-item-section>{{ p }}</q-item-section>
                                    </q-item>
                                    <q-separator dark />
                                    <q-item clickable @click="promptNewProject" class="text-amber">
                                        <q-item-section avatar><q-icon name="add_box" /></q-item-section>
                                        <q-item-section>Create New Component</q-item-section>
                                    </q-item>
                                </q-list>
                            </q-menu>
                        </q-btn>
                    </div>

                    <q-toolbar-title class="text-weight-bold" style="letter-spacing: 1.5px;">
                        <div class="row items-center no-wrap">
                            <q-icon name="auto_fix_high" color="amber" class="q-mr-sm shadow-1"></q-icon>
                            <span class="text-overline q-mr-sm text-blue-grey-3" style="font-size: 0.65rem;">AITREE CO-ARCHITECT</span>
                            <span class="text-amber">MCE <span class="text-white">v3.1</span></span>
                        </div>
                    </q-toolbar-title>
                    
                    <q-space></q-space>

                    <q-btn unelevated color="amber" text-color="indigo-10" icon="add_circle" label="NEW BLUEPRINT" class="q-mr-md text-weight-bold" @click="promptNewArtifact('screen')">
                         <q-menu dark class="bg-blue-grey-10">
                             <q-list style="min-width: 150px">
                                 <q-item clickable v-close-popup @click="promptNewArtifact('screen')">
                                     <q-item-section avatar><q-icon name="web" /></q-item-section>
                                     <q-item-section>New UI Screen</q-item-section>
                                 </q-item>
                                 <q-item clickable v-close-popup @click="promptNewArtifact('service')">
                                     <q-item-section avatar><q-icon name="settings" /></q-item-section>
                                     <q-item-section>New Logic Service</q-item-section>
                                 </q-item>
                                 <q-item clickable v-close-popup @click="promptNewArtifact('entity')">
                                     <q-item-section avatar><q-icon name="storage" /></q-item-section>
                                     <q-item-section>New Entity Model</q-item-section>
                                 </q-item>
                             </q-list>
                         </q-menu>
                    </q-btn>

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

            <!-- Navigator Sidebar with Vertical Tabs (Lens) -->
            <q-drawer show-if-above v-model="leftDrawerOpen" side="left" bordered class="bg-blue-grey-10 text-white shadow-20" :width="340" behavior="desktop" :breakpoint="0">
                <div class="row full-height no-wrap">
                    <!-- Vertical Tab Strip (Lens) -->
                    <div class="column bg-blue-grey-9 q-py-md items-center shadow-5" style="width: 65px; border-right: 1px solid rgba(255,255,255,0.05)">
                         <q-tabs v-model="ideMode" vertical dense indicator-color="amber" active-color="amber" class="text-blue-grey-4">
                             <q-tab name="screen" icon="web">
                                 <q-tooltip anchor="center right" self="center left">Screen Mode</q-tooltip>
                             </q-tab>
                             <q-tab name="entity" icon="storage">
                                 <q-tooltip anchor="center right" self="center left">Entity Mode</q-tooltip>
                             </q-tab>
                             <q-tab name="service" icon="settings">
                                 <q-tooltip anchor="center right" self="center left">Service Mode</q-tooltip>
                             </q-tab>
                         </q-tabs>
                         <q-space></q-space>
                         <q-btn flat round icon="history" class="text-blue-grey-4 q-mb-md" size="sm">
                             <q-tooltip anchor="center right" self="center left">Work History</q-tooltip>
                         </q-btn>
                    </div>

                    <!-- Navigator Content Area -->
                    <div class="col scroll q-pa-none column no-wrap">
                        <!-- Registry Source Selector -->
                        <div class="q-pa-sm bg-blue-grey-9 shadow-inner" style="border-bottom: 1px solid rgba(255,255,255,0.1)">
                            <div class="text-overline text-amber q-mb-xs q-pl-xs" style="font-size: 0.6rem; letter-spacing: 1px;">REGISTRY SOURCE</div>
                            <q-select dense filled dark square
                                      v-model="currentComponent" 
                                      :options="allProjects" 
                                      @update:modelValue="fetchArtifacts"
                                      bg-color="transparent" class="q-px-xs">
                                <template v-slot:prepend><q-icon name="hub" size="xs" color="amber-3" /></template>
                            </q-select>
                        </div>

                        <!-- MODE CONTENT PANELS -->
                        <div class="col scroll">
                            <!-- SCREEN MODE CONTENT -->
                            <template v-if="ideMode === 'screen'">
                                <q-list dense padding>
                                    <q-item-label header class="text-weight-bold text-uppercase text-indigo-3 opacity-80" style="font-size: 0.7rem;">Screen Explorer</q-item-label>
                                    <q-item v-for="s in allScreens" :key="s.name" clickable v-ripple 
                                            :active="s.name === screenPath && currentComponent === componentName"
                                            @click="switchScreen(s.name, 'screen')"
                                            active-class="bg-blue-grey-8 text-amber shadow-5" class="q-mx-xs rounded-borders">
                                        <q-item-section avatar class="min-width-auto q-pr-sm">
                                            <q-icon name="description" :color="s.name === screenPath && currentComponent === componentName ? 'amber' : 'blue-grey-4'" size="xs" />
                                        </q-item-section>
                                        <q-item-section>
                                            <q-item-label class="text-caption">{{ s.name }}</q-item-label>
                                        </q-item-section>
                                    </q-item>
                                </q-list>
                            </template>

                            <!-- ENTITY MODE CONTENT -->
                            <template v-else-if="ideMode === 'entity'">
                                <q-list dense padding>
                                    <q-item-label header class="text-weight-bold text-uppercase text-indigo-3 opacity-80" style="font-size: 0.7rem;">Entity Model Library</q-item-label>
                                    <q-item v-for="e in allEntities" :key="e.name" clickable v-ripple 
                                            :active="e.name === screenPath && currentComponent === componentName"
                                            @click="switchScreen(e.name, 'entity')"
                                            active-class="bg-blue-grey-8 text-amber shadow-5" class="q-mx-xs rounded-borders">
                                        <q-item-section avatar class="min-width-auto q-pr-sm">
                                            <q-icon name="storage" :color="e.name === screenPath && currentComponent === componentName ? 'amber' : 'amber-8'" size="xs" />
                                        </q-item-section>
                                        <q-item-section>
                                            <q-item-label class="text-caption">{{ e.name }}</q-item-label>
                                        </q-item-section>
                                    </q-item>
                                </q-list>
                            </template>

                            <!-- SERVICE MODE CONTENT -->
                            <template v-else-if="ideMode === 'service'">
                                <q-list dense padding>
                                    <q-item-label header class="text-weight-bold text-uppercase text-indigo-3 opacity-80" style="font-size: 0.7rem;">Service Registry</q-item-label>
                                    <q-item v-for="s in allServices" :key="s.name" clickable v-ripple 
                                            :active="s.name === screenPath && currentComponent === componentName"
                                            @click="switchScreen(s.name, 'service')"
                                            active-class="bg-blue-grey-8 text-amber shadow-5" class="q-mx-xs rounded-borders">
                                        <q-item-section avatar class="min-width-auto q-pr-sm">
                                            <q-icon name="settings" :color="s.name === screenPath && currentComponent === componentName ? 'amber' : 'blue-grey-4'" size="xs" />
                                        </q-item-section>
                                        <q-item-section>
                                            <q-item-label class="text-caption">{{ s.name }}</q-item-label>
                                        </q-item-section>
                                    </q-item>
                                </q-list>
                            </template>
                        </div>
                    </div>
                </div>
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
                            <template v-if="ideMode === 'screen' && selectedComponent">
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
                            <template v-else-if="ideMode === 'service'">
                                <!-- 1. Selected Action Inspector -->
                                <template v-if="selectedComponent">
                                    <div class="q-gutter-sm">
                                        <div class="text-overline text-indigo-10 bg-indigo-1 q-pa-xs rounded-borders">
                                            ACTION: {{ selectedComponent.type }}
                                        </div>
                                        <q-input dense filled bg-color="grey-1" color="indigo"
                                                 v-model="selectedComponent.id" 
                                                 label="Action ID" 
                                                 @blur="saveProperty(selectedComponent.id, 'id', selectedComponent.id)"
                                                 stack-label></q-input>
                                        
                                        <q-item-label header class="text-weight-bold text-uppercase text-indigo-3" style="font-size: 0.7rem;">Parameters</q-item-label>
                                        <template v-for="(val, key) in (selectedComponent.parameters || {})" :key="key">
                                            <q-input dense filled bg-color="grey-1" color="indigo"
                                                     v-model="selectedComponent.parameters[key]" 
                                                     :label="key" 
                                                     @blur="saveProperty(selectedComponent.id, key, selectedComponent.parameters[key], true)"
                                                     stack-label></q-input>
                                        </template>
                                        <q-btn flat dense color="indigo-8" icon="add" label="Add Parameter" class="full-width q-mt-sm"></q-btn>
                                    </div>
                                </template>
                                
                                <!-- 2. Global Service Contract (Fallback) -->
                                <template v-else>
                                    <div class="q-gutter-sm">
                                        <div class="q-pa-sm bg-indigo-1 rounded-borders q-mb-md shadow-inner">
                                            <div class="text-caption text-indigo-10 text-weight-bold row no-wrap items-center">
                                                <q-icon name="login" class="q-mr-xs"></q-icon> IN PARAMETERS (INPUTS)
                                            </div>
                                            <q-list dense>
                                                <q-item v-for="p in (blueprint.inparameters || blueprint.inParameters || [])" :key="p.name" class="q-px-none">
                                                    <q-item-section>
                                                        <q-item-label class="text-weight-bold">{{ p.name }}</q-item-label>
                                                        <q-item-label caption>{{ p.type }} <span v-if="p.required" class="text-red-8">*</span></q-item-label>
                                                    </q-item-section>
                                                </q-item>
                                            </q-list>
                                            <div v-if="!(blueprint.inparameters || blueprint.inParameters || []).length" class="text-caption text-grey-5 q-pa-xs">No inputs defined</div>
                                        </div>

                                        <div class="q-pa-sm bg-teal-1 rounded-borders shadow-inner">
                                            <div class="text-caption text-teal-10 text-weight-bold row no-wrap items-center">
                                                <q-icon name="logout" class="q-mr-xs"></q-icon> OUT PARAMETERS (OUTPUTS)
                                            </div>
                                            <q-list dense>
                                                <q-item v-for="p in (blueprint.outparameters || blueprint.outParameters || [])" :key="p.name" class="q-px-none">
                                                    <q-item-section>
                                                        <q-item-label class="text-weight-bold">{{ p.name }}</q-item-label>
                                                        <q-item-label caption>{{ p.type }}</q-item-label>
                                                    </q-item-section>
                                                </q-item>
                                            </q-list>
                                            <div v-if="!(blueprint.outparameters || blueprint.outParameters || []).length" class="text-caption text-grey-5 q-pa-xs">No outputs defined</div>
                                        </div>

                                        <div class="q-pa-sm bg-amber-1 rounded-borders shadow-inner">
                                            <div class="text-caption text-amber-10 text-weight-bold row no-wrap items-center">
                                                <q-icon name="bolt" class="q-mr-xs"></q-icon> LOGIC STEPS (ACTIONS)
                                            </div>
                                            <q-list dense>
                                                <q-item v-for="a in (blueprint.actions || [])" :key="a.id" 
                                                        clickable v-ripple @click="selectComponent(a)" class="q-px-none">
                                                    <q-item-section>
                                                        <q-item-label class="text-weight-bold">{{ a.type }}</q-item-label>
                                                        <q-item-label caption>{{ a.id }}</q-item-label>
                                                    </q-item-section>
                                                </q-item>
                                            </q-list>
                                            <div v-if="!(blueprint.actions || []).length" class="text-caption text-grey-5 q-pa-xs">No actions defined</div>
                                        </div>
                                    </div>
                                </template>
                            </template>
                            <div v-else class="fit flex flex-center text-grey-4 column q-pa-xl">
                                <q-icon name="dashboard_customize" size="100px" class="opacity-20"></q-icon>
                                <div class="text-subtitle1 text-center font-weight-light">SELECT NODE TO INSPECT</div>
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
                console.info("Aitree Shell Boot: [path: ${screenPath}] [component: ${componentName}]");
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

                const switchProject = (name) => {
                    if (name === currentComponent.value) return;
                    window.location.href = '/rest/s1/moquiai/render?componentName=' + name + '&screenPath=Welcome';
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

                const promptNewArtifact = (type) => {
                    \$q.dialog({
                        title: 'Register New ' + type.charAt(0).toUpperCase() + type.slice(1),
                        message: 'Enter the blueprint name (id part):',
                        prompt: { model: '', type: 'text', isValid: val => val.length > 2 },
                        cancel: true,
                        persistent: true,
                        dark: true
                    }).onOk(name => {
                        window.location.href = `/rest/s1/moquiai/render?componentName=\${currentComponent.value}&\${type === 'service' ? 'screenPath=service/' + name : 'screenPath=entity/' + name}`;
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

                const saveBlueprintFull = async (newBlueprint) => {
                    const response = await fetch('/rest/s1/moquiai/saveBlueprint', {
                        method: 'POST',
                        headers: { 
                            'Content-Type': 'application/json',
                            'X-CSRF-Token': '${ec.web.sessionToken}' 
                        },
                        body: JSON.stringify({ 
                            componentName: componentName, 
                            screenPath: screenPath,
                            blueprint: newBlueprint
                        })
                    });
                    const data = await response.json();
                    if (data.success) {
                        \$q.notify({ type: 'positive', message: 'Manual Source Pushed & Validated', position: 'bottom-right' });
                        blueprint.value = newBlueprint;
                        flashSync();
                    } else {
                        \$q.notify({ type: 'negative', message: 'Validation Failed: ' + data.message, position: 'bottom-right', multiLine: true, timeout: 0, actions: [{ label: 'Dismiss', color: 'white' }] });
                    }
                };

                window.addEventListener('blueprint-source-save', (e) => {
                    saveBlueprintFull(e.detail);
                });

                const saveProperty = async (id, name, val, isParameter = false) => {
                    if (!val && val !== false && val !== 0) return;
                    console.log("Saving property:", id, name, val, "isParameter:", isParameter);
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
                            properties: { [name]: val },
                            isParameter: isParameter
                        })
                    });
                    const data = await response.json();
                    if (data.success) {
                        \$q.notify({ type: 'positive', message: 'Logic Updated', position: 'bottom-right', timeout: 800 });
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

                const ideMode = ref('screen');
                const allScreens = ref([]);
                const allEntities = ref([]);
                const allServices = ref([]);

                const fetchArtifacts = async () => {
                    const types = ['screen', 'entity', 'service'];
                    for (const type of types) {
                        try {
                            const res = await fetch(`/rest/s1/moquiai/explorer?componentName=\${currentComponent.value}&type=\${type}`);
                            const data = await res.json();
                            if (type === 'screen') allScreens.value = data.artifacts || [];
                            if (type === 'entity') allEntities.value = data.artifacts || [];
                            if (type === 'service') allServices.value = data.artifacts || [];
                        } catch(e) { console.error("Failed to fetch \${type}s", e); }
                    }
                };

                const switchScreen = (screen, type = 'screen') => {
                    let path = screen;
                    if (type === 'entity' && !screen.startsWith('entity/')) path = 'entity/' + screen;
                    if (type === 'service' && !screen.startsWith('service/')) path = 'service/' + screen;
                    const url = `/rest/s1/moquiai/render?componentName=\${currentComponent.value}&screenPath=\${path}`;
                    console.info("Switching Mode:", type, "to path:", path, "URL:", url);
                    window.location.href = url;
                };

                const sendAiCommand = (cmd) => {
                    userInput.value = cmd;
                    sendMessage();
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
                    fetchArtifacts();
                    
                    // Mode Auto-Detection from Path
                    if (screenPath.startsWith('entity/')) ideMode.value = 'entity';
                    else if (screenPath.startsWith('service/')) ideMode.value = 'service';

                    // Empty State Trigger: Auto-start onboarding
                    const struct = blueprint.value.structure || [];
                    const inParams = blueprint.value.inParameters || [];
                    const actions = blueprint.value.actions || [];
                    
                    const hasContent = struct.length > 0 || inParams.length > 0 || actions.length > 0;
                    
                    if (!hasContent) {
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
                    saveProperty, allProjects, allScreens, allEntities, allServices, currentComponent, 
                    switchProject, switchScreen, promptNewProject, promptNewArtifact,
                    ideMode, fetchArtifacts, sendAiCommand
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
