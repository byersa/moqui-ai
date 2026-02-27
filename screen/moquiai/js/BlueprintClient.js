/* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */

/**
 * BlueprintClient.js
 * 
 * Client-side infrastructure for rendering MoquiAi JSON-LD Blueprints.
 */

(function () {
    const { h, defineComponent, markRaw, resolveComponent } = Vue;

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

            console.log('BlueprintNode Rendering:', type, node);

            if (!type) {
                console.warn('BlueprintNode: missing @type', node);
                return h('div', 'Unknown Node');
            }
            // Map Blueprint types to Quasar/Moqui components
            let componentName = null;
            let props = {
                id: node.id,
                style: node.style,
                class: node.class,
                ...node.attributes
            };

            // Auto-convert string boolean props to actual booleans and kebab-case to camelCase
            Object.keys(props).forEach(key => {
                if (props[key] === 'true') props[key] = true;
                if (props[key] === 'false') props[key] = false;
                if (key.includes('-')) {
                    const camelKey = key.replace(/-([a-z])/g, g => g[1].toUpperCase());
                    if (props[camelKey] === undefined) props[camelKey] = props[key];
                }
            });

            console.debug('BlueprintNode rendering type:', type, 'props:', props);

            let children = [];

            // Helper to render children
            const renderChildren = () => node.children ? node.children.map(child => h(BlueprintNode, { node: child, parentType: type })) : [];

            switch (type) {
                case 'ScreenBlueprint':
                    console.log("Found ScreenBlueprint root:", node.location);
                    return h('div', {
                        class: 'blueprint-root',
                        style: "min-height: 200px;"
                    }, renderChildren());

                case 'label':
                case 'Label':
                    const style = node.style || props.style || '';
                    if (props.type === 'q-toolbar-title' || style.includes('q-toolbar-title')) {
                        componentName = 'q-toolbar-title';
                        return h(componentName, { class: props.class || '' }, node.text || props.text || '');
                    }
                    if (style === 'q-icon' || props.style === 'q-icon') {
                        return h('q-icon', { name: props.text || node.text, class: props.class, style: props['style-attr'] });
                    }
                    componentName = 'div';
                    const text = props.text || node.text || '';
                    const labelClass = (props.type === 'h4' ? 'text-h4 ' : '') + (props.class || '');
                    return h(componentName, { class: labelClass }, text);

                case 'dynamic-dialog':
                case 'm-dynamic-dialog':
                    componentName = 'm-dynamic-dialog';
                    break;
                case 'container-row':
                case 'm-container-row':
                    componentName = 'm-container-row';
                    children = renderChildren();
                    break;
                case 'row-col':
                case 'm-row-col':
                    componentName = 'm-row-col';
                    children = renderChildren();
                    break;
                case 'm-link':
                    componentName = 'm-link';
                    children = renderChildren();
                    break;
                case 'm-menu-item':
                case 'menu-item':
                    componentName = 'm-menu-item';
                    break;
                case 'm-menu-dropdown':
                case 'menu-dropdown':
                    componentName = 'm-menu-dropdown';
                    break;
                case 'm-bp-tabbar':
                case 'bp-tabbar':
                    componentName = 'bp-tabbar';
                    children = renderChildren();
                    break;
                case 'm-bp-tab':
                case 'bp-tab':
                    componentName = 'bp-tab';
                    children = renderChildren();
                    break;

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

                case 'Text':
                    // Render raw text/HTML from render-mode
                    return h('span', { innerHTML: node.text || props.text || '' });

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
                    // If we have children (pre-rendered content from server), render them directly as a fragment
                    if (node.children && node.children.length > 0) {
                        // Filter out nested SubscreensActive nodes to prevent unwanted recursion/duplication
                        return node.children
                            .filter(c => c['@type'] !== 'SubscreensActive')
                            .map(child => h(BlueprintNode, { node: child }));
                    }
                    // Otherwise map to the existing m-subscreens-active component
                    componentName = 'm-subscreens-active';
                    break;

                case 'container-box':
                    componentName = 'm-container-box';
                    break;
                case 'SubscreensMenu':
                    componentName = 'm-subscreens-menu';
                    const menuStyle = node.style || props.style || '';
                    if (menuStyle.includes('toolbar') || this.parentType === 'screen-toolbar') {
                        if (props.pathIndex === undefined) props.pathIndex = 0;
                        return h('m-subscreens-menu', { ...props, type: 'toolbar' });
                    }
                    break;
                case 'Container':
                    componentName = 'div';
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
                case 'BlueprintTemplate':
                    componentName = 'template';
                    children = renderChildren();
                    break;

                case 'screen-content':
                    componentName = 'm-screen-content';
                    children = renderChildren();
                    break;

                default:
                    console.log('BlueprintNode Default handling for:', type);
                    // Standard HTML tags should not be resolved as custom components
                    const htmlTags = ['div', 'span', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 'a', 'img', 'br', 'hr', 'table', 'tr', 'td', 'th', 'thead', 'tbody', 'tfoot', 'section', 'article', 'aside', 'header', 'footer', 'nav', 'main'];
                    if (htmlTags.includes(type)) {
                        componentName = type;
                    } else {
                        // Fallback for custom components (like discussion-tree or m- tags)
                        componentName = type;
                    }
                    children = renderChildren();
            }

            if (componentName) {
                let comp = componentName;
                let isHtmlTag = false;
                if (typeof comp === 'string') {
                    // Standard HTML tags should be used as-is, don't try to resolving them as Vue components
                    isHtmlTag = ['div', 'span', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li', 'a', 'img', 'br', 'hr', 'table', 'tr', 'td', 'th', 'thead', 'tbody', 'tfoot', 'section', 'article', 'aside', 'header', 'footer', 'nav', 'main', 'template'].includes(comp);

                    if (!isHtmlTag) {
                        // Normalize cases that should be components
                        if (!comp.startsWith('m-') && !comp.startsWith('q-') && !comp.startsWith('bp-')) {
                            // Check if m- version exists
                            try {
                                const mVersion = resolveComponent('m-' + comp);
                                if (mVersion && typeof mVersion !== 'string') comp = mVersion;
                            } catch (e) { }
                        }

                        if (typeof comp === 'string') {
                            try {
                                const resolved = resolveComponent(comp);
                                if (resolved && typeof resolved !== 'string') comp = resolved;
                            } catch (e) { /* fallback to string */ }
                        }
                    }
                }

                console.log('BlueprintNode created VNode:', type, '->', componentName);

                // For standard string/HTML tags, pass children directly. For components, use slots.
                if (typeof comp === 'string' && isHtmlTag) {
                    return h(comp, props, children);
                }
                return h(comp, props, { default: () => children });
            }

            console.warn('BlueprintNode: nothing rendered for type:', type);
            return h('div', `Unhandled type: ${type}`);
        }
    });

    /**
     * Utility to check if a response is a Blueprint
     * Now checks if object has @type of ScreenBlueprint OR if it's a plain object that looks like one.
     */
    moqui.isBlueprint = function (obj) {
        return obj && (obj['@type'] === 'ScreenBlueprint' || (obj.children && (obj.attributes || obj.id)));
    };

    /**
     * Transforms a Blueprint into a Vue Component object
     */
    moqui.makeBlueprintComponent = function (blueprint) {
        console.log('makeBlueprintComponent called for:', blueprint?.['@type']);
        const wrap = defineComponent({
            name: 'BlueprintWrapper',
            render() {
                return h(BlueprintNode, { node: blueprint });
            }
        });
        return markRaw ? markRaw(wrap) : wrap;
    };

    /**
     * Fallback for late registration if app wasn't ready
     */
    moqui.registerBlueprintNode = function (app) {
        if (app && app.component) {
            app.component('m-blueprint-node', BlueprintNode);
            console.log("m-blueprint-node registered via late registration");
        }
    };

    if (moqui.webrootVue && moqui.webrootVue.component) {
        moqui.webrootVue.component('m-blueprint-node', BlueprintNode);
    } else {
        console.warn("moqui.webrootVue not found yet, will attempt late registration");
        // Try again in 500ms just in case
        setTimeout(() => moqui.registerBlueprintNode(moqui.webrootVue), 500);
    }

    console.log("BlueprintClient initialized and ready");

})();
