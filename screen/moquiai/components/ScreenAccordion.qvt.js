/* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */

moqui.webrootVue.component('m-screen-accordion', {
    name: "mScreenAccordion",
    template: `
        <q-splitter
            v-model="splitterModel"
            :horizontal="horizontal"
            :limits="parsedLimits"
            class="full-width full-height blueprint-accordion"
            :style="style"
        >
            <template v-slot:before>
                <div class="full-height overflow-auto">
                    <slot v-if="$slots.before" name="before"></slot>
                    <component :is="firstVNode" v-else-if="firstVNode"></component>
                    <div v-else class="flex flex-center text-grey-5 full-height">Before Panel</div>
                </div>
            </template>
            <template v-slot:after>
                <div class="full-height overflow-auto">
                    <slot v-if="$slots.after" name="after"></slot>
                    <template v-else-if="remainingVNodes.length > 0">
                        <component v-for="(vn, idx) in remainingVNodes" :key="idx" :is="vn"></component>
                    </template>
                    <div v-else class="flex flex-center text-grey-5 full-height">After Panel</div>
                </div>
            </template>
        </q-splitter>
    `,
    props: {
        model: { type: [Number, String], default: 50 },
        horizontal: { type: [Boolean, String], default: false },
        limits: { type: String, default: "10,90" },
        style: String
    },
    data: function() {
        return {
            internalModel: parseFloat(this.model || 50)
        }
    },
    computed: {
        splitterModel: {
            get: function() { return this.internalModel; },
            set: function(val) {
                this.internalModel = val;
                this.$emit('update:modelValue', val);
                this.$emit('change', val);
            }
        },
        parsedLimits: function() {
            if (typeof this.limits === 'string' && this.limits.includes(',')) {
                return this.limits.split(',').map(Number);
            }
            return [10, 90];
        },
        firstVNode: function() {
            const children = this.getChildren();
            return children.length > 0 ? children[0] : null;
        },
        remainingVNodes: function() {
            const children = this.getChildren();
            return children.length > 1 ? children.slice(1) : [];
        }
    },
    watch: {
        model: function(newVal) {
            this.internalModel = parseFloat(newVal);
        }
    },
    methods: {
        getChildren: function() {
            const slot = this.$slots.default;
            if (!slot) return [];
            // Handle Vue 3 (function) or Vue 2 (array) slots
            return typeof slot === 'function' ? slot() : slot;
        }
    }
});
