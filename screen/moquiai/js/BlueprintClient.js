/* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */

/**
 * BlueprintClient.js
 * 
 * Client-side infrastructure for rendering MoquiAi JSON-LD Blueprints.
 */

(function () {
    const { h, resolveComponent, defineComponent } = Vue;

    /**
     * BlueprintNode
     * Recursive component that transforms Blueprint nodes into Vue/Quasar components.
     */
    const BlueprintNode = defineComponent({
        name: 'BlueprintNode',
        props: {
            node: { type: Object, required: true }
        },
        render() {
            const node = this.node;
            const type = node['@type'];

            if (!type) {
                console.warn('BlueprintNode: missing @type', node);
                return h('div', 'Unknown Node');
            }

            // Map Blueprint types to Quasar/Moqui components
            let componentName = null;
            let props = { ...node.attributes };
            let children = [];

            switch (type) {
                case 'ScreenBlueprint':
                    return h('div', { class: 'blueprint-root' },
                        node.children ? node.children.map(child => h(BlueprintNode, { node: child })) : []
                    );

                case 'label':
                    componentName = 'div';
                    const text = props.text || '';
                    const labelClass = props.type === 'h4' ? 'text-h4' : '';
                    return h(componentName, { class: labelClass }, text);

                case 'FormSingle':
                    componentName = 'q-form';
                    props.class = 'q-gutter-md';
                    children = node.children ? node.children.map(child => h(BlueprintNode, { node: child })) : [];
                    break;

                case 'FormList':
                    // Custom implementation for FormList
                    return h('q-table', {
                        title: node.name,
                        rows: node.rows.map(r => r._data),
                        columns: node.header.map(h => ({
                            name: h.name,
                            label: h.title,
                            field: h.name,
                            align: 'left'
                        }))
                    });

                case 'FormField':
                    // Basic FormField wrapper
                    return h('div', { class: 'blueprint-field' },
                        node.children ? node.children.map(child => h(BlueprintNode, { node: child })) : []
                    );

                case 'text-line':
                case 'password':
                    componentName = 'q-input';
                    props.outlined = true;
                    props.dense = true;
                    if (type === 'password') props.type = 'password';
                    break;

                case 'submit':
                    componentName = 'q-btn';
                    props.type = 'submit';
                    props.color = 'primary';
                    props.label = props.title || 'Submit';
                    break;

                case 'display':
                    componentName = 'div';
                    // In a real implementation, we'd pull values from the context
                    return h('div', { class: 'q-pa-xs' }, 'Display Value Placeholder');

                case 'SubscreensActive':
                    // This is the tricky part - integration with Moqui's subscreen system
                    return h('m-subscreens-active');

                default:
                    // Fallback for custom components (like discussion-tree)
                    componentName = type;
                    children = node.children ? node.children.map(child => h(BlueprintNode, { node: child })) : [];
            }

            if (componentName) {
                const comp = (componentName.startsWith('q-') || componentName.startsWith('m-'))
                    ? resolveComponent(componentName)
                    : componentName;
                return h(comp, props, () => children);
            }

            return h('div', `Unhandled type: ${type}`);
        }
    });

    moqui.webrootVue.component('m-blueprint-node', BlueprintNode);

    /**
     * Utility to check if a response is a Blueprint
     */
    moqui.isBlueprint = function (obj) {
        return obj && obj['@type'] === 'ScreenBlueprint';
    };

    /**
     * Transforms a Blueprint into a Vue Component object
     */
    moqui.makeBlueprintComponent = function (blueprint) {
        return defineComponent({
            render() {
                return h(BlueprintNode, { node: blueprint });
            }
        });
    };

})();
