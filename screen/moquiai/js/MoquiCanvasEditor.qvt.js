define(['vue'], function (Vue) {
    return {
        name: 'MoquiCanvasEditor',
        template: `
            <div class="moqui-canvas-wrapper">
                <q-banner dense class="bg-primary text-white q-mb-md">
                    Moqui-AI Visual Canvas (aitree project)
                </q-banner>
                
                <div id="konva-holder" style="border: 1px solid #ccc; background: white; height: 500px;"></div>
                
                <q-btn label="Log Canvas State" @click="logState" color="secondary" class="q-mt-md" />
            </div>
        `,
        data() {
            return {
                stage: null,
                layer: null
            }
        },
        mounted() {
            // Initialize Konva
            this.stage = new Konva.Stage({
                container: 'konva-holder',
                width: window.innerWidth * 0.8,
                height: 500
            });

            this.layer = new Konva.Layer();
            this.stage.add(this.layer);

            // Add a test rectangle representing a "Form"
            const rect = new Konva.Rect({
                x: 50, y: 50, width: 200, height: 100,
                fill: '#e3f2fd', stroke: '#2196f3', strokeWidth: 2,
                draggable: true
            });

            const text = new Konva.Text({
                x: 60, y: 90, text: 'Sample Moqui Form', fontSize: 15
            });

            this.layer.add(rect);
            this.layer.add(text);
            this.layer.draw();
        },
        methods: {
            logState() {
                console.log("Canvas Objects:", this.layer.getChildren());
                this.$q.notify({ message: 'Canvas state logged to console', color: 'positive' });
            }
        }
    };
});

// Register the component globally so the XML can find it
if (window.moqui) {
    moqui.addComponent('moqui-canvas-editor', 'component://moqui-ai/screen/moquiai/js/MoquiCanvasEditor.qvt.js');
}