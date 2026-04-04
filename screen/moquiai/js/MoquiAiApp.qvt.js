(function () {
    console.log("MoquiAiApp script loading...");
    const componentDef = {
        name: 'MoquiAiApp',
        data() {
            return {
                currentScreenData: null,
                loading: false,
                // Track permissions (simulated for now, should come from Moqui UserAccount)
                canDesign: true
            }
        },
        template: `
            <q-layout view="hHh Lpr lFf">
                <q-header elevated :class="$root.aiTreeStore?.isArchitectMode ? 'bg-indigo-9' : 'bg-primary'">
                    <q-toolbar>
                        <q-btn flat dense round icon="menu" @click="$root.leftOpen = !$root.leftOpen" />
                        <q-toolbar-title class="q-ml-sm">
                            {{ $root.aiTreeStore?.isArchitectMode ? 'MoquiAi Architect' : 'Aitree Care System' }}
                        </q-toolbar-title>

                        <!-- APP SELECTOR -->
                        <q-select
                            v-if="$root.aiTreeStore"
                            v-model="$root.aiTreeStore.selectedApp"
                            :options="$root.aiTreeStore.availableApps"
                            dense flat dark
                            emit-value map-options
                            class="q-mx-md"
                            label-color="white"
                            label="Active Project"
                            style="min-width: 150px"
                            bg-color="transparent"
                        />
                        
                        <q-separator dark vertical inset class="q-mx-lg" v-if="canDesign" />
                        
                        <!-- THE MAGIC TOGGLE -->
                        <q-toggle
                            v-if="canDesign && $root.aiTreeStore"
                            v-model="$root.aiTreeStore.isArchitectMode"
                            checked-icon="architecture"
                            unchecked-icon="visibility"
                            color="orange"
                            label="Architect Mode"
                            left-label
                            @update:model-value="toggleMode"
                        />
                    </q-toolbar>
                </q-header>

                <q-page-container>
                    <transition enter-active-class="animated fadeIn" leave-active-class="animated fadeOut" mode="out-in">
                        <!-- ARCHITECT VIEW: The Visual Canvas -->
                        <div v-if="$root.aiTreeStore?.isArchitectMode" :key="'architect'">
                            <moqui-canvas-editor 
                                :screen-data="currentScreenData" 
                                :spec-path="currentSpecPath" />
                        </div>
                        
                        <!-- PRODUCTION VIEW: The Real App -->
                        <div v-else :key="'production'" class="q-pa-md">
                            <m-blueprint-node 
                                v-if="currentScreenData" 
                                :node="currentScreenData" 
                                :context="{}" />
                            <q-inner-loading :showing="loading">
                                <q-spinner-gears size="50px" color="primary" />
                            </q-inner-loading>
                        </div>
                    </transition>
                </q-page-container>
            </q-layout>
        `,
        computed: {
            currentSpecPath() {
                // Derives the .md path from the current URL
                return this.$root.currentPath;
            }
        },
        methods: {
            async toggleMode(value) {
                this.loading = true;
                try {
                    const app = this.$root.aiTreeStore?.selectedApp?.value || 'aitree';
                    // Fetch the latest .qjson for the current screen to ensure parity
                    const response = await fetch(window.location.pathname + '?renderMode=qjson&app=' + app);
                    const data = await response.json();
                    this.currentScreenData = data;
                } catch (e) {
                    console.error("Failed to sync screen data for mode toggle", e);
                } finally {
                    this.loading = false;
                }
            },
            async fetchAvailableApps() {
                try {
                    const response = await fetch('/rest/s1/moquiai/AppServices/get/AvailableApps');
                    const data = await response.json();
                    if (this.$root.aiTreeStore) {
                        this.$root.aiTreeStore.availableApps = data.apps || [];
                    }
                } catch (e) { console.warn("Failed to fetch available orchestrator apps:", e); }
            }
        },
        async mounted() {
            console.log("MoquiAiApp orchestrator mounted.");
            await this.fetchAvailableApps();
            // Initial load
            if (this.$root.aiTreeStore) this.toggleMode(this.$root.aiTreeStore.isArchitectMode);
        }
    };

    function registerComponent() {
        if (window.moqui && window.moqui.webrootVue) {
            window.moqui.webrootVue.component('moqui-ai-app', componentDef);
            console.info("MoquiAiApp orchestrator officially registered.");
        } else {
            console.warn("window.moqui.webrootVue not ready for MoquiAiApp, retrying in 300ms...");
            setTimeout(registerComponent, 300);
        }
    }
    registerComponent();

})();