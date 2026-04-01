/**
 * MoquiAiApp.qvt.js
 * The core orchestrator for the unified Production/Architect experience.
 */
define(['vue', 'BlueprintClient', 'MoquiCanvasEditor'], function (Vue) {
    return {
        name: 'MoquiAiApp',
        data() {
            return {
                isArchitectMode: false,
                currentScreenData: null,
                loading: false,
                // Track permissions (simulated for now, should come from Moqui UserAccount)
                canDesign: true
            }
        },
        template: `
            <q-layout view="hHh Lpr lFf">
                <q-header elevated :class="isArchitectMode ? 'bg-indigo-9' : 'bg-primary'">
                    <q-toolbar>
                        <q-btn flat dense round icon="menu" @click="$root.leftDrawerOpen = !$root.leftDrawerOpen" />
                        <q-toolbar-title>
                            {{ isArchitectMode ? 'MoquiAi Architect' : 'Aitree Care System' }}
                        </q-toolbar-title>
                        
                        <q-separator dark vertical inset class="q-mx-lg" v-if="canDesign" />
                        
                        <!-- THE MAGIC TOGGLE -->
                        <q-toggle
                            v-if="canDesign"
                            v-model="isArchitectMode"
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
                        <div v-if="isArchitectMode" :key="'architect'">
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
                    // Fetch the latest .qjson for the current screen to ensure parity
                    const response = await fetch(window.location.pathname + '?renderMode=qjson');
                    const data = await response.json();
                    this.currentScreenData = data;
                } catch (e) {
                    console.error("Failed to sync screen data for mode toggle", e);
                } finally {
                    this.loading = false;
                }
            }
        },
        mounted() {
            // Initial load
            this.toggleMode(this.isArchitectMode);
        }
    };
});