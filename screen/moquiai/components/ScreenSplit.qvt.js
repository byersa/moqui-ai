moqui.webrootVue.component('m-screen-split', {
    name: "mScreenSplit",
    props: {
        list: [Array, String],
        component: String,
        failMessage: String,
        failScreen: String,
        extraClass: [String, Object, Array],
        extraStyle: [String, Object, Array]
    },
    data: function () {
        return {
            startX: 0,
            blkStartWidMap: {},
            draggingPanelId: null,
            // Track widths to persist resizing during re-renders
            panelWidths: {}
        }
    },
    computed: {
        moqui: function () { return moqui; },
        aiTreeStore: function () {
            // Get the aiTreeStore from Pinia via moqui.pinia
            if (moqui.pinia && moqui.useAiTreeStore) {
                try {
                    return moqui.useAiTreeStore(moqui.pinia);
                } catch (e) {
                    console.warn("Could not get aiTreeStore:", e);
                    return null;
                }
            }
            return null;
        },
        resolvedList: function () {
            if (moqui.isArray(this.list)) return this.list;
            if (moqui.isString(this.list)) {
                // First try to resolve from aiTreeStore
                if (this.aiTreeStore) {
                    // Check if it's a getter on the store
                    if (this.list in this.aiTreeStore) {
                        const val = this.aiTreeStore[this.list];
                        if (moqui.isArray(val)) return val;
                    }
                    // Check $state for raw state values
                    if (this.aiTreeStore.$state && this.list in this.aiTreeStore.$state) {
                        const val = this.aiTreeStore.$state[this.list];
                        if (moqui.isArray(val)) return val;
                    }
                }
                // Return empty array if we can't resolve it
                console.warn("Could not resolve screen-split list from store:", this.list);
                return [];
            }
            return [];
        },
        mergedClass: function () {
            if (!this.extraClass) return '';
            return this.extraClass;
        },
        mergedStyle: function () {
            if (!this.extraStyle) return '';
            return this.extraStyle;
        }
    },
    watch: {
        resolvedList: function (newList) {
            var vm = this;
            this.$nextTick(function () { vm.recalcWidths(); });
        }
    },
    methods: {
        recalcWidths: function () {
            var activeCount = this.resolvedList.length;
            if (activeCount === 0) return;
            var bas = (100 / activeCount);
            for (var i = 0; i < activeCount; i++) {
                var id = this.resolvedList[i];
                var comp = this.$refs['PANE_' + id];
                if (comp && comp.length) {
                    // Only apply auto-width if we don't have a custom width stored or count changed
                    if (!this.panelWidths[id]) {
                        comp[0].style.flex = "0 0 " + bas + "%";
                    } else {
                        comp[0].style.flex = "0 0 " + this.panelWidths[id] + "%";
                    }
                }
            }
        },
        handleDragStart: function (evt, id) {
            this.draggingPanelId = id;
            this.startX = evt.clientX;
            var vm = this;
            this.resolvedList.forEach(function (pId) {
                var comp = vm.$refs['PANE_' + pId];
                if (comp && comp.length) {
                    vm.blkStartWidMap[pId] = comp[0].clientWidth;
                }
            });
            document.addEventListener('mousemove', this.handleDrag);
            document.addEventListener('mouseup', this.handleDragEnd);
        },
        handleDrag: function (evt) {
            if (this.draggingPanelId === null) return;
            var draggingIdx = this.resolvedList.indexOf(this.draggingPanelId);
            if (draggingIdx < 0 || draggingIdx >= this.resolvedList.length - 1) return;

            var nextPanelId = this.resolvedList[draggingIdx + 1];
            var widDiff = evt.clientX - this.startX;
            var totWid = this.$el.clientWidth;

            var newWid = this.blkStartWidMap[this.draggingPanelId] + widDiff;
            var nextWid = this.blkStartWidMap[nextPanelId] - widDiff;

            if (newWid < 100 || nextWid < 100) return;

            var bas1 = (newWid / totWid) * 100;
            var bas2 = (nextWid / totWid) * 100;

            this.panelWidths[this.draggingPanelId] = bas1;
            this.panelWidths[nextPanelId] = bas2;

            this.$refs['PANE_' + this.draggingPanelId][0].style.flex = "0 0 " + bas1 + "%";
            this.$refs['PANE_' + nextPanelId][0].style.flex = "0 0 " + bas2 + "%";
        },
        handleDragEnd: function (evt) {
            this.draggingPanelId = null;
            document.removeEventListener('mousemove', this.handleDrag);
            document.removeEventListener('mouseup', this.handleDragEnd);
        }
    },
    mounted: function () { this.recalcWidths(); },
    template: `
        <div class="row no-wrap full-height bg-grey-2 overflow-hidden m-screen-split" style="min-height: 400px; display: flex; border: 1px dashed #ccc; border-radius: 8px;" :class="mergedClass" :style="mergedStyle">
            <!-- Dynamic Mode: List-driven component loading -->
            <template v-if="list && component">
                <div v-for="(id, index) in resolvedList" :key="id" :ref="'PANE_' + id"
                     class="col-grow border-right-sep bg-white q-ma-xs rounded-borders shadow-1 relative-position overflow-hidden" 
                     style="flex: 1; display: flex; flex-direction: column;">

                    <m-dynamic-container :id="'pane-' + id" :url="component + '?agendaContainerId=' + id" :agenda-container-id="id" style="flex: 1;" />

                    <!-- Resize Handle -->
                    <div v-if="index < resolvedList.length - 1"
                         style="position:absolute; right:-4px; top:0; bottom:0; width:10px; cursor:col-resize; z-index:100; background: transparent;"
                         @mousedown.stop.prevent="handleDragStart($event, id)">
                         <div style="height: 100%; border-right: 2px solid rgba(0,0,0,0.1); margin-right: 4px;"></div>
                    </div>
                </div>

                <!-- Fail State (Empty State) -->
                <div v-if="resolvedList.length === 0" class="flex flex-center full-height full-width column container-q-gutter-md">
                    <q-card flat bordered class="bg-white q-pa-xl text-center shadow-2" style="max-width: 500px; border-radius: 16px;">
                        <q-card-section>
                            <q-icon name="dashboard_customize" size="100px" color="primary" class="q-mb-lg opacity-80" />
                            <div class="text-h4 text-weight-bold text-grey-9 q-mb-md">Ready to Orchestrate?</div>
                            <div class="text-subtitle1 text-grey-7 q-mb-xl">
                                {{ failMessage || 'No active containers are currently selected for this view.' }}
                            </div>
                            
                            <div class="row justify-center q-gutter-md">
                                <q-btn color="primary" icon="add" label="Select Containers" unelevated rounded padding="12px 24px" class="text-weight-bold" />
                                <q-btn outline color="primary" icon="auto_fix_high" label="Seed Sample Data" rounded @click="aiTreeStore ? aiTreeStore.activeContainerIds = ['SAMPLE_1', 'SAMPLE_2'] : null" />
                            </div>
                        </q-card-section>
                        
                        <q-card-section class="bg-grey-1 q-mt-md rounded-borders">
                            <div class="text-caption text-grey-6 text-italic">
                                Tip: Use the 'Architect Mode' toggle above to modify the structure of this screen using the visual canvas.
                            </div>
                        </q-card-section>
                    </q-card>
                </div>
            </template>
            
            <!-- Static Mode: Render children (sections, etc.) -->
            <template v-else>
                <div class="full-width flex flex-center"><slot></slot></div>
            </template>
        </div>
    `
});
