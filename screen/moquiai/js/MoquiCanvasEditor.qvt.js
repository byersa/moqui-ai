(function (moqui) {
    const componentDef = {
        name: 'MoquiCanvasEditor',
        props: {
            screenData: { type: [Object, String], default: () => ({ name: 'Empty', children: [] }) },
            specPath: { type: String, default: '' }
        },
        computed: {
            resolvedData() {
                if (typeof this.screenData === 'string') {
                    try { return JSON.parse(this.screenData); } 
                    catch (e) { console.error("Error parsing screenData string", e); return { name: 'Error', children: [] }; }
                }
                return this.screenData || { name: 'Empty', children: [] };
            }
        },
        template: `
            <div class="moqui-canvas-wrapper q-pa-md" style="min-height: 800px; border: 2px solid #2196f3; border-radius: 12px; background: white;">
                <q-banner dense class="bg-primary text-white q-mb-md shadow-2">
                    <template v-slot:avatar><q-icon name="auto_graph" /></template>
                    Moqui Screen Visualizer
                    <template v-slot:action>
                        <q-btn flat color="white" label="Refresh" icon="refresh" @click="redraw" />
                    </template>
                </q-banner>
                
                <div id="konva-holder" style="border: 1px solid #e0e0e0; border-radius: 8px; background: #ffffff; min-height: 700px; height: 700px; overflow: hidden; position: relative;"></div>
                
                <div class="q-mt-md text-caption text-grey-7">
                    Visualizing: {{ (resolvedData || {}).name }} ({{ ((resolvedData || {}).children || []).length }} top-level elements)
                </div>
            </div>
        `,
        data() {
            return {
                stage: null,
                layer: null
            }
        },
        watch: {
            resolvedData: {
                deep: true,
                handler() { this.redraw(); }
            }
        },
        mounted() {
            console.info("MoquiCanvasEditor mounting... Vue version:", (typeof Vue !== 'undefined' ? Vue.version : 'NOT FOUND'));
            setTimeout(() => {
                this.initKonva();
                this.redraw();
            }, 500);
        },
        methods: {
            initKonva() {
                const holder = document.getElementById('konva-holder');
                if (!holder) return;

                this.stage = new Konva.Stage({
                    container: 'konva-holder',
                    width: holder.offsetWidth,
                    height: 700,
                    draggable: true
                });

                // 1. Add Background Grid
                const bgLayer = new Konva.Layer();
                const gridStep = 50;
                for (let i = 0; i < holder.offsetWidth / gridStep; i++) {
                    bgLayer.add(new Konva.Line({ points: [i * gridStep, 0, i * gridStep, 700], stroke: '#f5f5f5', strokeWidth: 1 }));
                }
                for (let j = 0; j < 700 / gridStep; j++) {
                    bgLayer.add(new Konva.Line({ points: [0, j * gridStep, holder.offsetWidth, j * gridStep], stroke: '#f5f5f5', strokeWidth: 1 }));
                }
                this.stage.add(bgLayer);

                // 2. Add zoom support
                this.stage.on('wheel', (e) => {
                    e.evt.preventDefault();
                    const oldScale = this.stage.scaleX();
                    const pointer = this.stage.getPointerPosition();
                    const mousePointTo = {
                        x: (pointer.x - this.stage.x()) / oldScale,
                        y: (pointer.y - this.stage.y()) / oldScale,
                    };
                    const newScale = e.evt.deltaY > 0 ? oldScale * 0.9 : oldScale * 1.1;
                    this.stage.scale({ x: newScale, y: newScale });
                    this.stage.position({
                        x: pointer.x - mousePointTo.x * newScale,
                        y: pointer.y - mousePointTo.y * newScale,
                    });
                });

                this.layer = new Konva.Layer();
                this.stage.add(this.layer);
            },
            redraw() {
                if (!this.layer) return;
                this.layer.destroyChildren();
                
                let data = this.resolvedData;
                
                // Transparency: Handle 'screen-structure' wrapper by just using its children
                if (data && data.name === 'screen-structure' && data.children) {
                    data = data; // use directly but iterate children
                } else {
                    data = { children: [data] }; // wrap single root
                }
                
                console.info("MoquiCanvasEditor redrawing...");
                
                let currentY = 50;
                const canvasWidth = this.stage.width();
                
                const children = data.children || [];
                children.forEach(node => {
                    currentY = this.drawNode(node, 50, currentY, canvasWidth - 100);
                    currentY += 20; 
                });
                
                this.layer.draw();
            },
            drawNode(node, x, y, width) {
                if (!node || !node.name) return y;
                const padding = 15;
                const headerHeight = 30;
                let contentHeight = 20; // Minimum content padding
                
                // Determine style based on node name
                let color = '#e3f2fd';
                let stroke = '#2196f3';
                let label = node.name;
                
                if (node.name.includes('form-')) { color = '#f1f8e9'; stroke = '#4caf50'; }
                else if (node.name.includes('container')) { color = '#fff3e0'; stroke = '#ff9800'; }
                else if (node.name === 'actions' || node.name === 'pre-actions' || node.name === 'script') { color = '#f3e5f5'; stroke = '#9c27b0'; }
                else if (node.name === 'link' || node.name === 'render-mode') { color = '#ede7f6'; stroke = '#673ab7'; }
                else if (node.name.includes('row') || node.name.includes('col')) { color = '#e0f2f1'; stroke = '#00897b'; }
                else if (node.name === 'screen-split') { color = '#fff8e1'; stroke = '#ffb300'; label = "ORCHESTRATOR: " + (node.attributes.name || 'Splitter'); }
                else if (node.name === 'Missing Blueprint') { color = '#ffebee'; stroke = '#f44336'; label = "ALERT: MOQUI BLUEPRINT MISSING"; }
                
                // Add attributes to label
                if (node.name === 'screen-split') {
                    if (node.attributes.component) label += "\n[Loads: " + node.attributes.component + "]";
                    if (node.attributes.list) label += "\n[List: " + node.attributes.list + "]";
                } else if (node.name === 'Missing Blueprint') {
                    if (node.attributes.text) label += "\n" + node.attributes.text;
                } else if (node.attributes && node.attributes.name) {
                    label += ': ' + node.attributes.name;
                } else if (node.attributes && node.attributes.text) {
                    label += ': ' + node.attributes.text;
                }

                // Override x,y if location attribute exists in [x: 123, y: 456] format
                let localX = x;
                let localY = y;
                if (node.attributes && node.attributes.location) {
                    const locStr = node.attributes.location;
                    const xMatch = locStr.match(/x:\s*(-?\d+)/);
                    const yMatch = locStr.match(/y:\s*(-?\d+)/);
                    if (xMatch && yMatch) {
                        localX = parseInt(xMatch[1]);
                        localY = parseInt(yMatch[1]);
                        // console.info(`Positioning ${node.name} from spec location: [${localX}, ${localY}]`);
                    }
                }

                const group = new Konva.Group({ x: localX, y: localY, draggable: true });
                
                // Draw children first to calculate height
                const lines = label.split('\n').length;
                const dynamicHeaderHeight = headerHeight + (lines > 1 ? (lines - 1) * 15 : 0);
                
                let childY = dynamicHeaderHeight + padding;
                const childWidth = width - (padding * 2);
                
                if (node.children) {
                    node.children.forEach(child => {
                        childY = this.drawNode(child, padding, childY, childWidth);
                        childY += 10;
                    });
                    contentHeight = childY;
                } else {
                    contentHeight = dynamicHeaderHeight + 40;
                }

                // Node background
                const rect = new Konva.Rect({
                    x: 0, y: 0, width: width, height: contentHeight,
                    fill: color, stroke: stroke, strokeWidth: 1, cornerRadius: 4,
                    shadowBlur: 2, shadowOpacity: 0.1
                });
                
                // Node header
                const text = new Konva.Text({
                    x: 10, y: 8, text: label, fontSize: 13, fontStyle: 'bold', fontFamily: 'monospace', fill: '#333',
                    lineHeight: 1.2
                });

                group.add(rect);
                group.add(text);
                
                group.on('dragend', (e) => {
                    const newX = Math.round(group.x());
                    const newY = Math.round(group.y());
                    const widgetId = node.attributes?.id || node.id || node.attributes?.name || node.name;
                    
                    if (this.specPath) {
                        const baseUrl = window.location.pathname.replace(/\/+$/, '');
                        $.ajax({
                            url: baseUrl + "/syncCanvas",
                            type: 'POST',
                            data: {
                                specPath: this.specPath,
                                widgetId: widgetId,
                                newX: newX,
                                newY: newY,
                                moquiSessionToken: window.moqui?.moquiSessionToken
                            },
                            success: (resp) => {
                                console.log("SyncCanvas response:", resp);
                                if (resp.status === "success") {
                                    window.dispatchEvent(new CustomEvent('canvas-synced', { detail: { widgetId, newX, newY } }));
                                } else {
                                    console.error("Canvas sync failed:", resp.message || resp.errors || "Unknown Error");
                                }
                            }
                        });
                    }
                });

                this.layer.add(group);
                
                return y + contentHeight;
            }
        }
    };

    // Register with Moqui SPA (with retry logic)
    function registerComponent() {
        if (typeof moqui !== 'undefined' && moqui.webrootVue && moqui.webrootVue.component) {
            moqui.webrootVue.component('moqui-canvas-editor', componentDef);
            console.info("MoquiCanvasEditor officially registered with moqui.webrootVue.");
        } else {
            console.warn("moqui.webrootVue not ready, retrying registration in 300ms...");
            setTimeout(registerComponent, 300);
        }
    }
    registerComponent();
    window.MoquiCanvasEditor = componentDef;

})(window.moqui);