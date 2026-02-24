/* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */

/**
 * BlueprintClient.js
 * 
 * Client-side infrastructure for rendering MoquiAi JSON-LD Blueprints.
 */

(function () {
    const { h, defineComponent, markRaw } = Vue;

    /**
     * BlueprintNode
     * Recursive component that transforms Blueprint nodes into Vue/Quasar components.
     */
    const BlueprintNode = defineComponent({
        name: 'BlueprintNode',
        props: {
            node: { type: Object, required: true },
            parentType: { type: String, default: null }
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

            // Auto-convert string boolean props to actual booleans
            Object.keys(props).forEach(key => {
                if (props[key] === 'true') props[key] = true;
                if (props[key] === 'false') props[key] = false;
            });

            let children = [];

            // Helper to render children
            const renderChildren = () => node.children ? node.children.map(child => h(BlueprintNode, { node: child, parentType: type })) : [];

            switch (type) {
                case 'ScreenBlueprint':
                    return h('div', { class: 'blueprint-root' }, renderChildren());

                case 'label':
                case 'Label':
                    const style = node.style || props.style || '';
                    if (props.type === 'q-toolbar-title' || style.includes('q-toolbar-title')) {
                        componentName = 'q-toolbar-title';
                        return h(componentName, { class: props.class || '' }, node.text || props.text || '');
                    }
                    componentName = 'div';
                    const text = props.text || node.text || '';
                    // Check if we are inside a header or toolbar that is white text, or just apply if props say so
                    // For now, explicit props.class or 'text-white' if parent is primary (heuristic)
                    const labelClass = (props.type === 'h4' ? 'text-h4 ' : '') + (props.class || '');
                    return h(componentName, { class: labelClass }, text);

                case 'FormSingle':
                    componentName = 'q-form';
                    props.class = 'q-gutter-md';
                    children = renderChildren();
                    break;

                case 'FormList':
                    // Custom implementation for FormList
                    return h('q-table', {
                        title: node.name,
                        rows: node.rows.map(r => r._data),
                        columns: node.header.map(h => ({
                            name: h.name,
                            label: h.title || h.name,
                            field: row => row[h.name],
                            align: 'left',
                            sortable: true
                        }))
                    });

                case 'FormField':
                    // Basic FormField wrapper
                    return h('div', { class: 'blueprint-field' }, renderChildren());

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
                    return h('div', { class: 'q-pa-xs' }, 'Display Value Placeholder');

                case 'SubscreensActive':
                    // If we have children (pre-rendered content from server), render them
                    if (node.children && node.children.length > 0) {
                        // Filter out nested SubscreensActive nodes to prevent unwanted recursion/duplication
                        // exact cause of server-side duplication is unknown but this heuristic cleans it up
                        const validChildren = node.children.filter(c => c['@type'] !== 'SubscreensActive');

                        // Use a wrapper to ensure layout integrity
                        return h('div', { class: 'subscreens-active-container' },
                            validChildren.map(child => h(BlueprintNode, { node: child }))
                        );
                    }
                    // Otherwise map to the existing m-subscreens-active component
                    componentName = 'm-subscreens-active';
                    break;

                case 'container-box':
                    componentName = 'm-container-box';
                    break;
                case 'SubscreensMenu':
                    componentName = 'm-subscreens-menu';
                    // Check style for 'toolbar' mode, OR if we are inside a screen-toolbar
                    const menuStyle = node.style || props.style || '';
                    if (menuStyle.includes('toolbar') || this.parentType === 'screen-toolbar') {
                        // Default pathIndex to 0 if missing (common case for header menus where attribute is stripped)
                        if (props.pathIndex === undefined) props.pathIndex = 0;

                        // Ensure we pass all props (like pathIndex) along with the type override
                        return h('m-subscreens-menu', { ...props, type: 'toolbar' });
                    }
                    break;
                case 'Container':
                    componentName = 'div';
                    // Check for specific Quasar types mapped via attributes, e.g. type="q-space"
                    if (props.type === 'q-space') {
                        componentName = 'q-space';
                    }
                    children = renderChildren();
                    break;

                case 'screen-toolbar':
                    componentName = 'm-screen-toolbar';
                    children = renderChildren();
                    break;
                case 'screen-layout':
                    componentName = 'm-screen-layout';
                    children = renderChildren();
                    break;
                case 'screen-header':
                    componentName = 'm-screen-header';
                    children = renderChildren();
                    break;
                case 'screen-drawer':
                    componentName = 'm-screen-drawer';
                    children = renderChildren();
                    break;
                case 'screen-content':
                    componentName = 'm-screen-content';
                    children = renderChildren();
                    break;

                default:
                    // Fallback for custom components (like discussion-tree)
                    componentName = type;
                    children = renderChildren();
            }

            if (componentName) {
                return h(componentName, props, { default: () => children });
            }

            return h('div', `Unhandled type: ${type}`);
        }
    });

    moqui.webrootVue.component('m-blueprint-node', BlueprintNode);

    /**
     * Utility to check if a response is a Blueprint
     * Now checks if object has @type of ScreenBlueprint OR if it's a plain object that looks like one.
     */
    moqui.isBlueprint = function (obj) {
        return obj && (obj['@type'] === 'ScreenBlueprint' || (obj.children && obj.attributes));
    };

    /**
     * Transforms a Blueprint into a Vue Component object
     */
    moqui.makeBlueprintComponent = function (blueprint) {
        return markRaw(defineComponent({
            name: 'BlueprintWrapper',
            render() {
                return h(BlueprintNode, { node: blueprint });
            }
        }));
    };

    console.info("BlueprintClient initialized");

})();
