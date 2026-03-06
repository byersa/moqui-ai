# KI: Discussion Tree Vue Component - Qwen Generation Prototype

## Context
This is a prototype generation of the `m-discussion-tree` component, created by the local **Qwen 2.5-Coder:7b** model on the user's dedicated hardware.

## Purpose
To demonstrate recursive Moqui/Quasar component generation using XSD-injected prompts and local AI logic.

---

### Generated Code:

```javascript
moqui.webrootVue.component('m-discussion-tree', {
    props: {
        workEffortId: String,
        patientContext: Object,
        nodeActions: Array
    },
    data() {
        return {
            discussionTree: null
        };
    },
    mounted() {
        this.fetchDiscussionData();
    },
    methods: {
        fetchDiscussionData() {
            // Call the Moqui transition to fetch the discussion data
            this.$moqui.callTransition('getDiscussionData', { workEffortId: this.workEffortId })
                .then(response => {
                    this.discussionTree = response.data;
                })
                .catch(error => {
                    console.error('Error fetching discussion data:', error);
                });
        },
        renderNode(node) {
            return `<q-card class="q-mb-md">
                        <q-card-section>
                            <div class="row items-center no-wrap">
                                <div class="col">
                                    <div class="text-h6">${node.label}</div>
                                    <div class="text-subtitle2">${node.patientContext ? node.patientContext : ''}</div>
                                </div>
                                <div class="col-auto">
                                    ${this.renderActions(node)}
                                </div>
                            </div>
                        </q-card-section>
                        ${node.children ? `<q-card-section>${node.children.map(child => this.renderNode(child)).join('')}</q-card-section>` : ''}
                    </q-card>`;
        },
        renderActions(node) {
            return this.nodeActions.map(action => {
                return `<q-btn flat no-caps color="white" :to="action.transitionUrl" :icon="action.icon" :label="action.label" @click="${action.onClick}"></q-btn>`;
            }).join('');
        }
    },
    template: `
        <div>
            ${this.renderNode(this.discussionTree)}
        </div>
    `
});
```

### Explanation:

1. **Props**:
   - `workEffortId`: The ID of the work-effort (topic) to render.
   - `patientContext`: Optional patient context to display.
   - `nodeActions`: An array of action objects to be rendered on each node.

2. **Data**:
   - `discussionTree`: Holds the fetched discussion data.

3. **Mounted Hook**:
   - Calls the `fetchDiscussionData` method to fetch the discussion data when the component is mounted.

4. **fetchDiscussionData Method**:
   - Calls the Moqui transition `getDiscussionData` with the `workEffortId`.
   - Updates the `discussionTree` with the fetched data.

5. **renderNode Method**:
   - Recursively renders each node using a `q-card`.
   - Displays the node's label and patient context.
   - Renders the actions defined in `nodeActions`.

6. **renderActions Method**:
   - Renders the actions as `q-btn` components.

7. **Template**:
   - Uses the `renderNode` method to render the entire discussion tree.

### Usage Example:

```html
<m-discussion-tree work-effort-id="12345" :node-actions="[
    { label: 'Edit', icon: 'edit', transitionUrl: '/edit-topic' },
    { label: 'Delete', icon: 'delete', transitionUrl: '/delete-topic' }
]"></m-discussion-tree>
```

This component will recursively render the discussion tree, displaying each topic's label, patient context, and the specified actions.