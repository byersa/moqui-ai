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
            parentType: { type: String, default: null },
            context: { type: Object, default: () => ({}) }
        },
        render() {
            const node = this.node;
            const type = node['@type'];

            // 1. Reactive Visibility Toggle
            // Check for 'condition' in the node's attributes for reactive visibility.
            const attributes = node.attributes || {};
            const condition = attributes['condition'];
            if (condition) {
                try {
                    // Create an evaluation bridge that includes the current context (e.g. form row data)
                    const evalContext = { ...this.context, item: this.context, window, moqui };
                    const keys = Object.keys(evalContext);
                    const values = Object.values(evalContext);
                    const evalFn = new Function(...keys, `return (${condition})`);
                    if (!evalFn(...values)) return null;
                } catch (e) {
                    // Log error and default to false to prevent broken UI
                    console.error("Blueprint visibility condition error [" + condition + "]:", e);
                    return null;
                }
            }

            console.log('BlueprintNode Rendering:', type, node);

            if (!type) {
                console.warn('BlueprintNode: missing @type', node);
                return h('div', 'Unknown Node');
            }
            // Map Blueprint types to Quasar/Moqui components
            let componentName = null;
            // Moqui AI blueprints often put name, title, and label at the top level next to @type
            // Ensure they take precedence or are at least present in the flat props object
            let props = {
                id: node.id,
                style: node.style,
                class: node.class,
                ...node.attributes,
                name: node.attributes?.name || node.name,
                title: node.attributes?.title || node.title,
                label: node.attributes?.label || node.label
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
            const renderChildren = (ctx = this.context) => node.children ? node.children.map(child => h(BlueprintNode, { node: child, parentType: type, context: ctx })) : [];

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
                    // Convert transition + parameters to url if url is not present
                    if (props.transition && !props.url) {
                        let targetUrl = props.transition;
                        let queryParts = [];
                        if (node.children) {
                            node.children.forEach(child => {
                                if (child['@type'] === 'parameter') {
                                    const pAttrs = child.attributes || {};
                                    const pName = pAttrs.name;
                                    let pValue = pAttrs.value || pAttrs.from;
                                    if (pAttrs.from) {
                                        pValue = context[pAttrs.from] !== undefined ? context[pAttrs.from] : (props[pAttrs.from] !== undefined ? props[pAttrs.from] : pValue);
                                    }
                                    if (pName && pValue !== undefined && pValue !== null) {
                                        queryParts.push(`${pName}=${encodeURIComponent(pValue)}`);
                                    }
                                }
                            });
                        }
                        if (queryParts.length > 0) {
                            targetUrl += (targetUrl.indexOf('?') > 0 ? '&' : '?') + queryParts.join('&');
                        }
                        props.url = targetUrl;
                    }
                    // CRITICAL: Always ensure renderTargetOnly=true is on the URL so the server suppresses the shell
                    if (props.url) {
                        const separator = props.url.indexOf('?') > 0 ? '&' : '?';
                        if (props.url.indexOf('renderMode=qjson') === -1) props.url += separator + 'renderMode=qjson';
                        const sep2 = props.url.indexOf('?') > 0 ? '&' : '?';
                        if (props.url.indexOf('renderTargetOnly=true') === -1) props.url += sep2 + 'renderTargetOnly=true';
                    }
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

                case 'form-list':
                case 'FormList':
                    // Custom implementation for FormList with advanced cell rendering
                    if (type === 'form-list' && !node.rows) node.rows = node.attributes.rows || node.attributes.list || [];
                    if ((!node.header || node.header.length === 0) && node.children) {
                        // Fallback 1: extract headers from form-list-column or form-list-field children
                        node.header = node.children
                            .filter(c => c['@type'] === 'form-list-column' || c['@type'] === 'form-list-field' || c['@type'] === 'field')
                            .map(c => ({ name: (c.attributes ? c.attributes.name : c.name), title: (c.attributes ? (c.attributes.label || c.attributes.name) : (c.title || c.name)) }));
                    }
                    if ((!node.header || node.header.length === 0) && node.rows && node.rows.length > 0) {
                        // Fallback 2: Auto-discover headers from the first row's _data keys
                        const firstRowData = node.rows[0]._data || node.rows[0];
                        node.header = Object.keys(firstRowData)
                            .filter(k => !k.includes('_') && typeof firstRowData[k] !== 'object')
                            .map(k => ({ name: k, title: k.charAt(0).toUpperCase() + k.slice(1) }));
                    }
                    return h(resolveComponent('q-table'), {
                        title: node.name || props.name || (node.attributes ? node.attributes.name : ''),
                        rows: (node.rows || []).map(r => ({ ...(r._data || r), _fields: r.fields })),
                        columns: (node.header || []).map(h => ({
                            name: h.name,
                            label: h.title || h.name,
                            field: row => row[h.name],
                            align: 'left',
                            sortable: true
                        }))
                    }, {
                        body: (props) => {
                            return h(resolveComponent('q-tr'), { props: props }, {
                                default: () => props.cols.map(col => {
                                    // Find the pre-rendered field widgets for this specific row/column
                                    const field = props.row._fields ? props.row._fields.find(f => f.name === col.name) : null;
                                    // Pass the row data as context so children can evaluate conditions against it
                                    const cellContext = { ...this.context, ...props.row };

                                    return h(resolveComponent('q-td'), { key: col.name, props: props }, {
                                        default: () => (field && field.children && field.children.length > 0)
                                            ? field.children.map(child => h(BlueprintNode, { node: child, context: cellContext }))
                                            : props.value
                                    });
                                })
                            });
                        }
                    });

                case 'Text':
                    // Render raw text/HTML from render-mode
                    return h('span', { innerHTML: node.text || props.text || '' });

                case 'FormField':
                    // Render FormField with a label if title is present
                    const fieldName = props.name || node.name || (node.attributes ? node.attributes.name : null);
                    let fieldLabel = props.title || props.label || node.title || node.label || fieldName;

                    if (fieldLabel === fieldName && fieldName) {
                        // Humanize the name as a fallback (camelCase to Space + Capitalize)
                        fieldLabel = fieldLabel.replace(/([A-Z])/g, ' $1').replace(/^./, (str) => str.toUpperCase()).trim();
                    }

                    return h('div', { class: 'blueprint-field q-mb-md' }, [
                        fieldLabel ? h('div', { class: 'text-caption text-weight-medium text-grey-9 q-mb-xs' }, fieldLabel) : null,
                        ...node.children ? node.children.map(child => h(BlueprintNode, {
                            node: child,
                            parentType: 'FormField',
                            context: { ...this.context, _parentFieldName: fieldName }
                        })) : []
                    ]);

                case 'text-line':
                case 'password':
                    componentName = 'q-input';
                    props.outlined = true;
                    props.dense = true;
                    if (!props.name && this.context._parentFieldName) props.name = this.context._parentFieldName;
                    if (props.value !== undefined) props.modelValue = props.value;
                    if (type === 'password') props.type = 'password';
                    if (!props.label && props.title) props.label = props.title;
                    break;

                case 'date-time':
                case 'm-date-time':
                    componentName = 'm-date-time';
                    props.outlined = true;
                    props.dense = true;
                    if (!props.name && this.context._parentFieldName) props.name = this.context._parentFieldName;
                    if (props.value !== undefined) props.modelValue = props.value;
                    break;

                case 'drop-down':
                case 'm-drop-down':
                    componentName = 'm-drop-down';
                    if (!props.name && this.context._parentFieldName) props.name = this.context._parentFieldName;
                    if (props.value !== undefined) props.modelValue = props.value;
                    break;

                case 'hidden':
                case 'm-hidden':
                    // Don't render anything visible for hidden fields
                    if (!props.name && this.context._parentFieldName) props.name = this.context._parentFieldName;
                    return h('div', { style: 'display: none', 'data-name': props.name, 'data-value': props.value });

                case 'FormSingle':
                    componentName = 'q-form';
                    props.class = 'q-gutter-md q-pa-md';
                    children = renderChildren();
                    break;

                case 'submit':
                    componentName = 'q-btn';
                    props.type = 'submit';
                    props.color = 'primary';
                    props.label = props.title || 'Submit';
                    break;

                case 'display':
                case 'display-entity':
                    componentName = 'div';
                    return h('div', { class: props.class || 'q-pa-xs', style: props.style }, props.text || node.text || '');

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
                case 'date-time':
                case 'm-date-time':
                    componentName = 'm-date-time';
                    props.outlined = true;
                    props.dense = true;
                    if (props.value !== undefined) props.modelValue = props.value;
                    break;

                case 'drop-down':
                case 'm-drop-down':
                    componentName = 'm-drop-down';
                    if (props.value !== undefined) props.modelValue = props.value;
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
                    } else if (type.startsWith('m-')) {
                        componentName = type;
                    } else {
                        // Check for common Moqui widget names to map them to m- components
                        const moquiWidgets = ['container', 'container-row', 'row-col', 'link', 'form-single', 'form-list'];
                        if (moquiWidgets.includes(type)) {
                            componentName = 'm-' + type;
                        } else {
                            componentName = type;
                        }
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

                // For Vue 3 / Quasar 2 components, ensure modelValue is set if value is present
                if (!isHtmlTag && props.value !== undefined && props.modelValue === undefined) {
                    props.modelValue = props.value;
                }

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
