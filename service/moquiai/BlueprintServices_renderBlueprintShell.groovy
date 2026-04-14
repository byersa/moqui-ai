import org.moqui.context.ExecutionContext
import groovy.json.JsonOutput

ExecutionContext ec = context.ec

// 1. Bootstrapper
ec.resource.script("component://moqui-ai/script/MoquiAiBootstrapper.groovy", null)

// 2. Blueprint Data
def blueprintResult = ec.service.sync().name("moquiai.BlueprintServices.get#Blueprint")
    .parameter("componentName", componentName)
    .parameter("screenPath", screenPath).call()
def blueprint = blueprintResult?.blueprint ?: [:]
def blueprintJson = JsonOutput.toJson(blueprint).replace('$', '\\$')
def componentName = context.componentName ?: 'unknown'
def screenPath = context.screenPath ?: 'unknown'

// 3. MCP Token
def tokenResult = ec.service.sync().name("moquiai.WebMcpServices.get#ConnectionToken").call()
def mcpToken = tokenResult?.connectionToken ?: "NO_TOKEN"

// 4. Asset Tags
def stylesHtml = (context.moqui_ai_styles ?: []).collect { "<link href=\"${it}\" rel=\"stylesheet\" type=\"text/css\">" }.join('\n')
def scriptsHtml = (context.moqui_ai_scripts ?: []).collect { "<script src=\"${it}\"></script>" }.join('\n')
def coreScriptsHtml = (context.html_scripts ?: []).collect { "<script src=\"${it}\"></script>" }.join('\n')
 
// 5. Available Apps (MCE Shell discovery)
def appsResult = ec.service.sync().name("moquiai.AppServices.get#AvailableApps").call()
def allAvailableApps = appsResult?.apps ?: []
def allAvailableAppsJson = JsonOutput.toJson(allAvailableApps).replace('$', '\\$')

def html = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Aitree Blueprint Launcher: ${screenPath}</title>
    ${stylesHtml}
    <style>
        [v-cloak] { display: none !important; }
        .blueprint-mode { background: #fdfdfd; min-height: 100vh; }
        .q-layout { min-height: 100vh !important; }
        #ai-chat-input input, #ai-chat-input .q-field__native { color: white !important; font-weight: 500; }
        #ai-chat-input .q-field__placeholder { color: rgba(255,255,255,0.5) !important; }
        .animate-pulse { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
        @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: .5; } }
    </style>
    
    <!-- Shell Configuration (MUST load before framework scripts) -->
    <script>
        window.moqui = Object.assign(window.moqui || {}, {});
        
        Object.assign(window.moqui, { rootSetup: function() {
            try {
                console.log("MCE: shell.rootSetup() starting...");
                const { ref, onMounted, computed, provide, inject } = Vue;
                const blueprint = ref(${blueprintJson});
                const isSyncing = ref(false);
                const mcpToken = ref('${mcpToken}');
                const componentName = ref('${componentName}');
                const screenPath = ref('${screenPath}');
                const selectedComponent = ref(null);
                const userInput = ref('');
                const messages = ref([]);
                const isSending = ref(false);

                const ideMode = ref('screen');
                const allScreens = ref([]);
                const allEntities = ref([]);
                const allServices = ref([]);
                const allProjects = ref([]);
                const currentComponent = ref('${componentName}');

                const sendMessage = async (isInit = false) => {
                    if (!isInit && (!userInput.value || isSending.value)) return;
                    const text = isInit ? 'INIT' : userInput.value;
                    if (!isInit) messages.value.push({ role: 'user', text });
                    isSending.value = true; userInput.value = '';
                    try {
                        const res = await fetch('/rest/s1/moquiai/chat', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ mcpToken: mcpToken.value, componentName: componentName.value, screenPath: screenPath.value, prompt: text, context: { blueprint: blueprint.value } })
                        });
                        const data = await res.json();
                        if (data.reply) messages.value.push({ role: 'ai', text: data.reply });
                        if (data.blueprint) {
                            blueprint.value = data.blueprint;
                            isSyncing.value = true;
                            setTimeout(() => isSyncing.value = false, 1000);
                        }
                    } catch (e) {
                        messages.value.push({ role: 'ai', text: 'Peer communication failure. ' + e.message });
                    } finally { isSending.value = false; }
                };

                const saveProperty = async (path, key, value) => {
                    try {
                        isSyncing.value = true;
                        await fetch('/rest/s1/moquiai/saveProperty', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ componentName: componentName.value, screenPath: screenPath.value, blueprintPath: path, key, value })
                        });
                        setTimeout(() => isSyncing.value = false, 500);
                    } catch(e) { console.error("Save failure", e); }
                };

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

                const selectComponent = (comp) => { selectedComponent.value = comp; };
                const switchProject = (p) => { currentComponent.value = p; fetchArtifacts(); };
                const switchScreen = (screen, type = 'screen') => {
                    let path = screen;
                    if (type === 'entity' && !screen.startsWith('entity/')) path = 'entity/' + screen;
                    if (type === 'service' && !screen.startsWith('service/')) path = 'service/' + screen;
                    window.location.href = `/rest/s1/moquiai/render?componentName=` + encodeURIComponent(currentComponent.value) + `&screenPath=` + encodeURIComponent(path);
                };

                onMounted(async () => {
                    debugger;
                    if (window.BlueprintClient) {
                        window.BlueprintClient.setupSSE(componentName.value, screenPath.value, (nb) => blueprint.value = nb, (cmd) => window.BlueprintClient.processCommand(blueprint.value, cmd));
                    }
                    
                    await fetchArtifacts();

                    if (screenPath.value.startsWith('entity/')) ideMode.value = 'entity';
                    else if (screenPath.value.startsWith('service/')) ideMode.value = 'service';

                    // Auto-init chat if empty
                    if (!blueprint.value.structure?.length && !blueprint.value.actions?.length) {
                        setTimeout(() => { if (!isSending.value) { userInput.value = 'INIT'; sendMessage(true); } }, 1200);
                    }
                });

                provide('ideMode', ideMode);
                provide('userInput', userInput);

                return { 
                    data: {
                        blueprint: blueprint,
                        isSyncing: isSyncing,
                        mcpToken: mcpToken,
                        componentName: componentName,
                        screenPath: screenPath,
                        selectedComponent: selectedComponent,
                        userInput: userInput,
                        messages: messages,
                        isSending: isSending,
                        allProjects: allProjects,
                        allScreens: allScreens,
                        allEntities: allEntities,
                        allServices: allServices,
                        currentComponent: currentComponent,
                        ideMode: ideMode,
                        allAvailableApps: ${allAvailableAppsJson}
                    },
                    methods: {
                        sendMessage: sendMessage,
                        selectComponent: selectComponent,
                        saveProperty: saveProperty,
                        switchProject: switchProject,
                        switchScreen: switchScreen,
                        fetchArtifacts: fetchArtifacts,
                        promptNewProject: function() { alert('New Workspace wizard coming soon'); },
                        promptNewArtifact: function() { alert('New Artifact wizard coming soon'); }
                    }
                };
            } catch (err) {
                console.error("MCE: CRITICAL ERROR in shell.rootSetup():", err);
                return {};
            }
        }});
        window.dispatchEvent(new CustomEvent('moqui-bridge-ready'));
    </script>
</head>
<body class="blueprint-mode q-pa-none q-ma-none">
    <!-- Configuration Inputs -->
    <input type="hidden" id="confAppRootPath" value="/">
    <input type="hidden" id="confLinkBasePath" value="/rest/s1/moquiai">
    <input type="hidden" id="confBasePath" value="/rest/s1/moquiai">
    <input type="hidden" id="confMoquiSessionToken" value="${ec.web?.sessionToken ?: ''}">

    <div id="q-app" v-cloak>
        <q-layout view="hHh Lpr fFf" style="height: 100vh" class="bg-blue-grey-1 overflow-hidden">
            <q-header elevated class="bg-blue-grey-10 text-white" style="height: 64px;">
                <q-toolbar class="full-height shadow-10">
                    <div class="row items-center q-mx-md bg-blue-grey-9 q-px-md q-py-xs rounded-borders shadow-inner" style="border: 1px solid rgba(255,255,255,0.1)">
                        <q-icon name="folder_open" size="xs" color="amber-3" class="q-mr-sm"></q-icon>
                        <div class="column">
                            <div class="text-overline text-amber-5" style="line-height: 1; font-size: 0.6rem;">ACTIVE WORKSPACE</div>
                            <div class="text-subtitle2 text-white text-weight-bold" style="line-height: 1;">${componentName}</div>
                        </div>
                        <q-btn flat round dense icon="arrow_drop_down" color="white" class="q-ml-sm">
                            <q-menu dark class="bg-blue-grey-10">
                                <q-list style="min-width: 200px">
                                    <q-item clickable v-for="p in allProjects" :key="p" @click="switchProject(p)" :active="p === '${componentName}'">
                                        <q-item-section>{{ p }}</q-item-section>
                                    </q-item>
                                    <q-separator dark></q-separator>
                                    <q-item clickable @click="promptNewProject">
                                        <q-item-section side><q-icon name="add"></q-icon></q-item-section>
                                        <q-item-section>New Workspace...</q-item-section>
                                    </q-item>
                                </q-list>
                            </q-menu>
                        </q-btn>
                    </div>
                    <q-toolbar-title class="text-weight-light row items-center">
                        <span class="text-grey-5 q-mr-sm">Blueprint:</span>
                        <span class="text-white text-weight-medium">${screenPath}</span>
                        <q-chip v-if="isSyncing" icon="sync" color="amber-14" text-color="black" label="Syncing" size="sm" class="q-ml-md animate-pulse" dense></q-chip>
                        <q-chip v-else icon="security" color="blue-grey-8" text-color="white" label="Linked" size="sm" class="q-ml-md" dense></q-chip>
                    </q-toolbar-title>
                    <q-btn flat round dense icon="history" class="q-mr-xs"></q-btn>
                    <q-btn flat round dense icon="settings"></q-btn>
                </q-toolbar>
            </q-header>

            <q-drawer show-if-above :model-value="true" side="left" bordered class="bg-blue-grey-10 text-white shadow-10" :width="280">
                <div class="column full-height">
                    <div class="bg-blue-grey-11 text-blue-grey-2 q-pa-md row items-center justify-between border-bottom">
                         <div class="text-subtitle2 text-weight-bold row items-center">
                            <q-icon name="explore" class="q-mr-sm" color="amber-7"></q-icon>
                            NAVIGATOR
                         </div>
                         <q-btn flat round dense icon="add" size="sm" color="blue-grey-4" @click="promptNewArtifact"></q-btn>
                    </div>
                    <div class="q-px-md q-py-sm">
                        <q-btn-toggle v-model="ideMode" spread dense no-caps unelevated
                            toggle-color="amber-10" toggle-text-color="black" color="blue-grey-9" text-color="grey-4"
                            :options="[
                                { label: 'UI', value: 'screen', icon: 'display_settings' },
                                { label: 'DB', value: 'entity', icon: 'storage' },
                                { label: 'API', value: 'service', icon: 'api' }
                            ]"></q-btn-toggle>
                    </div>
                    <q-scroll-area class="col q-pa-md">
                        <div v-if="ideMode === 'screen'">
                            <q-list dense padding>
                                <q-item clickable v-for="s in allScreens" :key="s" @click="switchScreen(s, 'screen')" :active="s === '${screenPath}'" class="nav-item rounded-borders q-mb-xs">
                                    <q-item-section avatar class="min-width-auto q-pr-sm">
                                        <q-icon name="description" size="16px" :color="s === '${screenPath}' ? 'amber-8' : 'grey-5'"></q-icon>
                                    </q-item-section>
                                    <q-item-section class="text-caption overflow-hidden ellipsis">{{ s }}</q-item-section>
                                </q-item>
                            </q-list>
                        </div>
                        <div v-if="ideMode === 'entity'">
                            <q-list dense padding>
                                <q-item clickable v-for="e in allEntities" :key="e" @click="switchScreen(e, 'entity')" :active="'entity/' + e === '${screenPath}'" class="nav-item rounded-borders q-mb-xs">
                                    <q-item-section avatar class="min-width-auto q-pr-sm">
                                        <q-icon name="table_chart" size="16px" :color="'entity/' + e === '${screenPath}' ? 'amber-8' : 'grey-5'"></q-icon>
                                    </q-item-section>
                                    <q-item-section class="text-caption overflow-hidden ellipsis">{{ e }}</q-item-section>
                                </q-item>
                            </q-list>
                        </div>
                        <div v-if="ideMode === 'service'">
                            <q-list dense padding>
                                <q-item clickable v-for="sv in allServices" :key="sv" @click="switchScreen(sv, 'service')" :active="'service/' + sv === '${screenPath}'" class="nav-item rounded-borders q-mb-xs">
                                    <q-item-section avatar class="min-width-auto q-pr-sm">
                                        <q-icon name="bolt" size="16px" :color="'service/' + sv === '${screenPath}' ? 'amber-8' : 'grey-5'"></q-icon>
                                    </q-item-section>
                                    <q-item-section class="text-caption overflow-hidden ellipsis">{{ sv }}</q-item-section>
                                </q-item>
                            </q-list>
                        </div>
                    </q-scroll-area>
                    <q-separator dark></q-separator>
                    <div class="q-pa-md text-center text-grey-6 text-overline">AITREE CORE 2.0</div>
                </div>
            </q-drawer>

            <q-drawer show-if-above :model-value="true" side="right" bordered class="bg-blue-grey-10 text-white shadow-10" :width="380">
                <div class="column full-height">
                    <div class="bg-blue-grey-11 text-blue-grey-2 q-pa-md row items-center justify-between border-bottom">
                         <div class="text-subtitle2 text-weight-bold row items-center">
                            <q-icon name="auto_awesome" class="q-mr-sm" color="amber-7"></q-icon>
                            AI ARCHITECT
                         </div>
                    </div>
                    <q-scroll-area class="col q-pa-md chat-messages" ref="chatScroll">
                        <div v-for="(msg, idx) in messages" :key="idx" 
                             :class="['q-mb-md q-pa-sm rounded-borders shadow-2', msg.role === 'user' ? 'bg-blue-grey-9 text-right q-ml-xl' : 'bg-blue-grey-8 text-left q-mr-xl']">
                            <div class="text-overline text-grey-4" style="line-height: 1; font-size: 0.6rem;">{{ msg.role.toUpperCase() }}</div>
                            <div class="text-body2 text-white" style="white-space: pre-wrap;">{{ msg.text }}</div>
                        </div>
                        <div v-if="isSending" class="row justify-center q-my-md">
                            <q-spinner-dots color="amber-7" size="md"></q-spinner-dots>
                        </div>
                    </q-scroll-area>
                    <div class="q-pa-md bg-blue-grey-11 border-top">
                        <q-input v-model="userInput" dark filled dense standout class="rounded-borders" id="ai-chat-input"
                                 placeholder="Ask to change component..." @keyup.enter="sendMessage" :disable="isSending">
                            <template v-slot:append>
                                <q-btn round dense flat icon="send" color="amber-7" @click="sendMessage" :loading="isSending"></q-btn>
                            </template>
                        </q-input>
                    </div>
                </div>
            </q-drawer>

            <q-page-container style="height: calc(100vh - 64px); overflow: hidden;">
                <q-page class="q-pa-md bg-blue-grey-1 full-height">
                    <blueprint-renderer v-if="blueprint" :blueprint="blueprint"></blueprint-renderer>
                </q-page>
            </q-page-container>
        </q-layout>
    </div>

    <!-- Assets -->
    ${coreScriptsHtml}
    ${scriptsHtml}

    <!-- Final Mounting handled by MoquiAiVue.qvt.js -->
</body>
</html>
"""

if (ec.web != null) {
    ec.web.sendTextResponse(html, "text/html", null)
}
return [html: html]
