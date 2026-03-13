Vue.component('m-screen-split', {
    name: "mScreenSplit",
    template:
    '<div class="full-width sh-toolbar-and-body full-height">' +
        '<q-toolbar class="sh-toolbar">' +
            '<q-toolbar-title>Screen Split Viewer</q-toolbar-title>' +
            '<div ref="loadingSpinner" style="display:none;"><q-spinner size="3em" color="secondary" /></div>' +
            '<q-btn-dropdown color="primary" text-color="secondary" icon="add" label="Add Screen">' +
                '<q-list>' +
                    '<q-item v-for="screenName in availableScreens" :key="screenName" @click="addScreen(screenName)" clickable>' +
                        '<q-item-section><q-item-label>{{ screenName }}</q-item-label></q-item-section>' +
                    '</q-item>' +
                '</q-list>' +
            '</q-btn-dropdown>' +
        '</q-toolbar>' +
        '<div class="full-height full-width sh-body" style="position:relative; display:flex; flex-direction:row; overflow:hidden;">' +
            '<div v-for="(panel, index) in activePanels" :key="panel.id" :ref="\'INST_\' + panel.id" ' +
                 'class="sh-instance-sizeable-panel" style="flex:1; border-right:1px solid #ccc; position:relative; min-width:100px; display:flex; flex-direction:column;">' +
                 '<div style="text-align:right; padding:5px;"><q-btn dense flat round icon="close" size="sm" @click="removeScreen(panel.id)"/></div>' +
                 '<m-dynamic-container :id="\'panel_\' + panel.id" :url="panel.url" style="flex:1; overflow-y:auto; padding:10px;"></m-dynamic-container>' +
                 '<div style="position:absolute; right:-3px; top:0; bottom:0; width:6px; cursor:col-resize; z-index:10;" ' +
                      '@mousedown="handleDragStart($event, panel.id)"></div>' +
            '</div>' +
        '</div>' +
    '</div>',
    props: {
        targetScreenList: { type: String, required: true }
    },
    data: function() {
        return {
            activePanels: [],
            panelIdCounter: 0,
            startX: 0,
            blkStartWidMap: {},
            draggingPanelId: null
        }
    },
    computed: {
        availableScreens: function() {
            return this.targetScreenList ? this.targetScreenList.split(',').map(function(s){ return s.trim(); }) : [];
        }
    },
    methods: {
        addScreen: function(screenName) {
            this.panelIdCounter++;
            var url = screenName; 
            this.activePanels.push({
                id: this.panelIdCounter,
                name: screenName,
                url: url
            });
            var vm = this;
            this.$nextTick(function() { vm.recalcWidths(); });
        },
        removeScreen: function(panelId) {
            this.activePanels = this.activePanels.filter(function(panel) { return panel.id !== panelId; });
            var vm = this;
            this.$nextTick(function() { vm.recalcWidths(); });
        },
        recalcWidths: function() {
            var activeCount = this.activePanels.length;
            if (activeCount === 0) return;
            var bas = (100 / activeCount);
            for (var i = 0; i < activeCount; i++) {
                var panelId = this.activePanels[i].id;
                var comp = this.$refs['INST_' + panelId];
                if (comp && comp.length) {
                    comp[0].style.flex = "0 0 " + bas + "%";
                }
            }
        },
        handleDragStart: function(evt, panelId) {
            this.draggingPanelId = panelId;
            this.startX = evt.clientX;
            var vm = this;
            this.activePanels.forEach(function(panel) {
                var comp = vm.$refs['INST_' + panel.id];
                if (comp && comp.length) {
                    vm.blkStartWidMap[panel.id] = comp[0].clientWidth;
                }
            });
            document.addEventListener('mousemove', this.handleDrag);
            document.addEventListener('mouseup', this.handleDragEnd);
        },
        handleDrag: function(evt) {
            if (!this.draggingPanelId) return;
            // Get index of the panel being dragged
            var draggingIdx = -1;
            for (var i=0; i<this.activePanels.length; i++) {
                if (this.activePanels[i].id === this.draggingPanelId) { draggingIdx = i; break; }
            }
            // Cannot drag the last panel's right border
            if (draggingIdx < 0 || draggingIdx >= this.activePanels.length - 1) return;
            
            var nextPanelId = this.activePanels[draggingIdx + 1].id;
            var widDiff = evt.clientX - this.startX;
            
            var totWid = this.$el.querySelector('.sh-body').clientWidth;
            
            var newWid = this.blkStartWidMap[this.draggingPanelId] + widDiff;
            var nextWid = this.blkStartWidMap[nextPanelId] - widDiff;
            
            if (newWid < 50 || nextWid < 50) return; // Minimum 50px width
            
            var comp1 = this.$refs['INST_' + this.draggingPanelId][0];
            var comp2 = this.$refs['INST_' + nextPanelId][0];
            
            var bas1 = (newWid / totWid) * 100;
            var bas2 = (nextWid / totWid) * 100;
            
            comp1.style.flex = "0 0 " + bas1 + "%";
            comp2.style.flex = "0 0 " + bas2 + "%";
        },
        handleDragEnd: function(evt) {
            this.draggingPanelId = null;
            document.removeEventListener('mousemove', this.handleDrag);
            document.removeEventListener('mouseup', this.handleDragEnd);
        }
    }
});
