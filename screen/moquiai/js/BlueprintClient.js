/**
 * BlueprintClient.js - A standalone library for rendering Aitree Blueprints using Quasar 2.
 */
const BlueprintClient = {
    install(app) {
        app.component('BlueprintRenderer', {
            props: ['blueprint'],
            setup(props) {
                const dataStore = Vue.ref({});
                const isReady = Vue.ref(false);
                
                Vue.onMounted(async () => {
                    if (props.blueprint && props.blueprint.context) {
                        const ctx = props.blueprint.context;
                        if (ctx.entity && ctx.id) {
                            try {
                                const res = await fetch(`/rest/s1/moquiai/getEntityData?entityName=${ctx.entity}&pkValue=${ctx.id}`);
                                const data = await res.json();
                                if (data.record) {
                                    dataStore.value = data.record;
                                    console.log("Entity Auto-Bind complete:", dataStore.value);
                                }
                            } catch (e) {
                                console.error("Failed to auto-bind entity", e);
                            }
                        }
                    }
                    isReady.value = true;
                });

                const parentSelectedComp = Vue.inject('selectedComponent', null);
                const parentSelectComp = Vue.inject('selectComponent', null);

                const selectedComponent = parentSelectedComp || Vue.ref(null);
                const selectComponent = parentSelectComp || ((comp) => {
                    selectedComponent.value = comp;
                    console.log("Component selected:", comp);
                });

                if (!parentSelectedComp) Vue.provide('selectedComponent', selectedComponent);
                if (!parentSelectComp) Vue.provide('selectComponent', selectComponent);

                Vue.provide('blueprintDataStore', dataStore);
                return { dataStore, isReady, selectedComponent };
            },
            template: `
                <div class="blueprint-container q-pa-md" v-if="blueprint">
                    <q-header elevated class="bg-primary text-white" v-if="blueprint.meta">
                        <q-toolbar>
                            <q-toolbar-title>{{ blueprint.meta.title }}</q-toolbar-title>
                        </q-toolbar>
                    </q-header>
                    
                    <div class="q-mt-xl pt-lg" v-if="blueprint.structure && isReady">
                        <component-factory :components="blueprint.structure" />
                    </div>
                </div>
            `
        });

        app.component('ComponentFactory', {
            props: ['components'],
            render() {
                if (!this.components || !Array.isArray(this.components)) return null;
                const BlueprintComponent = Vue.resolveComponent('BlueprintComponent');
                return Vue.h('div', { class: 'column q-gutter-md' }, 
                    this.components.map((comp, i) => Vue.h(BlueprintComponent, { component: comp, key: comp.id || comp.component || i }))
                );
            }
        });

        app.component('BlueprintComponent', {
            props: ['component'],
            setup() {
                const dataStore = Vue.inject('blueprintDataStore', Vue.ref({}));
                const selectComponent = Vue.inject('selectComponent', () => {});
                const selectedComponent = Vue.inject('selectedComponent', Vue.ref(null));
                return { dataStore, selectComponent, selectedComponent };
            },
            render() {
                const comp = this.component;
                if (!comp) return null;

                const isSelected = this.selectedComponent && this.selectedComponent.id === comp.id;
                
                let type = comp.component ? comp.component.toLowerCase() : 'div';
                const props = comp.properties || {};
                const children = comp.children || [];

                // Check global macros first
                let macroDef = BlueprintClient.macros[type];
                let resolvedComponent = type;
                let resolvedProps = { ...props };
                let resolvedChildren = [...children];
                let isQuasar = false;

                if (macroDef) {
                    resolvedComponent = macroDef.component || 'div';
                    resolvedProps = { ...macroDef.properties, ...resolvedProps };
                    if (macroDef.children) {
                         // Deep copy macro children and potentially merge original children?
                         // For now, let's keep it simple and just use macro children.
                         resolvedChildren = [...(macroDef.children || []), ...resolvedChildren];
                    }
                    type = resolvedComponent.toLowerCase();
                }

                // Apply Auto-Binding from DataStore if ID matches a field
                let boundValue = this.dataStore[comp.id]; // Access it so Vue tracks it
                if (comp.id && boundValue !== undefined) {
                    resolvedProps.value = boundValue;
                }

                if (type.startsWith('q-')) {
                    isQuasar = true;
                    // Automatically normalize 'value' to 'modelValue' to support Vue 3 / Quasar bindings
                    if (resolvedProps.value !== undefined && resolvedProps.modelValue === undefined) {
                        resolvedProps.modelValue = resolvedProps.value;
                        resolvedProps['onUpdate:modelValue'] = (val) => { resolvedProps.modelValue = val; };
                        delete resolvedProps.value;
                    }
                }

                // Simple Factory Mapping Fallback
                let quasarCompName = resolvedComponent;
                let quasarProps = { ...resolvedProps };

                if (!isQuasar) {
                    switch(type) {
                        case 'displayfield':
                        case 'text-field':
                            quasarCompName = 'q-input';
                            quasarProps = { 
                                outlined: true, 
                                label: resolvedProps.label || resolvedProps.name, 
                                modelValue: resolvedProps.value || '',
                                readonly: type === 'displayfield',
                                ...resolvedProps 
                            };
                            isQuasar = true;
                            break;
                        case 'container':
                            quasarCompName = 'div';
                            quasarProps = { class: 'q-pa-sm bg-grey-2 rounded-borders', ...resolvedProps };
                            break;
                        case 'header':
                            quasarCompName = 'div';
                            quasarProps = { class: 'text-h6 q-mb-sm', ...resolvedProps };
                            return Vue.h(quasarCompName, quasarProps, resolvedProps.text || 'Header');
                        default:
                            quasarCompName = 'div';
                            quasarProps = { ...resolvedProps, class: 'q-pa-sm border-dashed text-caption text-grey', style: 'border: 1px dashed #ccc' };
                    }
                }

                const QuasarComp = (quasarCompName.startsWith('q-')) ? Vue.resolveComponent(quasarCompName) : quasarCompName;
                const ComponentFactory = Vue.resolveComponent('ComponentFactory');

                // Pass children definitively via Vue 3 slot objects
                let childNodes = undefined;
                if (resolvedChildren.length > 0) {
                    childNodes = { default: () => Vue.h(ComponentFactory, { components: resolvedChildren }) };
                } else if (resolvedProps.text && !isQuasar) {
                    childNodes = { default: () => resolvedProps.text };
                }

                // Add selection highlighting and click handler
                const finalQuasarProps = {
                    ...quasarProps,
                    onClick: (e) => {
                        e.stopPropagation();
                        this.selectComponent(comp);
                    },
                    style: (quasarProps.style || '') + (isSelected ? '; border: 2px solid #1976D2 !important; box-shadow: 0 0 10px rgba(25,118,210,0.5)' : '')
                };

                return childNodes ? Vue.h(QuasarComp, finalQuasarProps, childNodes) : Vue.h(QuasarComp, finalQuasarProps);
            }
        });
    },

    macros: {},

    async loadMacros() {
        try {
            const response = await fetch('/rest/s1/moquiai/getUiMacros');
            const data = await response.json();
            this.macros = data.macros || {};
            console.log("Aitree UI Macros Loaded:", this.macros);
        } catch (e) {
            console.error("Failed to load macros", e);
        }
    },

    async fetchBlueprint(componentName, screenPath) {
        // Fetch from the getBlueprint REST GET endpoint
        const response = await fetch(`/rest/s1/moquiai/getBlueprint?componentName=${componentName}&screenPath=${screenPath}`);
        const data = await response.json();
        return data.blueprint;
    },

    setupSSE(componentName, screenPath, onUpdate, onCommand) {
        // Setup SSE listener for hot-reload
        const url = `/rest/s1/moquiai/registerClient?componentName=${componentName}&screenPath=${screenPath}`;
        const eventSource = new EventSource(url);

        eventSource.addEventListener('update', (event) => {
            console.log("Blueprint Update Received:", event.data);
            const data = JSON.parse(event.data);
            if (data.screen === screenPath) {
                this.fetchBlueprint(componentName, screenPath).then(onUpdate);
            }
        });

        eventSource.addEventListener('connected', (event) => {
            console.log("SSE Connected:", JSON.parse(event.data));
        });

        eventSource.addEventListener('command', (event) => {
            console.log("Blueprint Command Received:", event.data);
            if (onCommand) {
                onCommand(JSON.parse(event.data));
            }
        });

        eventSource.onerror = (err) => {
            console.error("SSE Error:", err);
            // eventSource.close();
        };

        return eventSource;
    }
};

window.BlueprintClient = BlueprintClient;
