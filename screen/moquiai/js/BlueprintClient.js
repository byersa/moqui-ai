/* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */

/**
 * BlueprintClient.js
 * 
 * Client-side infrastructure for rendering MoquiAi JSON-LD Blueprints.
 */

(function () {
    const { h, defineComponent, markRaw, resolveComponent, reactive } = Vue;

    /**
     * BlueprintNode
     * Recursive component that transforms Blueprint nodes into Vue/Quasar components.
     */
    const BlueprintNode = defineComponent({
        name: 'BlueprintNode',
        props: {
            node: { type: Object, required: true },
            parentType: String,
            context: { type: Object, required: true },
            sourceUrl: String, // AMB 2026-03-11: Track the origin of the blueprint
            parentFieldName: String
        },
        render() {
            const node = this.node;
            const attributes = node.attributes || {};
            // Priority: explicit attribute overrides -> direct node @type -> fallback to node name
            const type = attributes['@type'] || attributes['type'] || node['@type'] || node.name;

            // 1. Reactive Visibility Toggle
            // Check for 'condition' in the node's attributes for reactive visibility.
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

            // console.log('BlueprintNode Rendering:', type, node);

            if (!type) {
                console.warn('BlueprintNode: missing @type', node);
                return h('div', 'Unknown Node');
            }
            // Map Blueprint types to Quasar/Moqui components
            let componentName = null;
            let props = {
                id: node.id || node.attributes?.id,
                style: (node.style || '') + ' ' + (node.attributes?.style || ''),
                class: (node.class || '') + ' ' + (node.attributes?.class || ''),
                ...node.attributes,
                name: node.attributes?.name || node.name,
                title: node.attributes?.title || node.title,
                label: node.attributes?.label || node.label
            };

            // Ensure data-maria-id is present for tools like WebMCP
            if (props.id && !props['data-maria-id']) props['data-maria-id'] = props.id;

            // Auto-convert string boolean props to actual booleans and kebab-case to camelCase
            Object.keys(props).forEach(key => {
                if (props[key] === 'true') props[key] = true;
                if (props[key] === 'false') props[key] = false;

                let targetKey = key;
                if (key.includes('-')) {
                    targetKey = key.replace(/-([a-z])/g, g => g[1].toUpperCase());
                    if (props[targetKey] === undefined) props[targetKey] = props[key];
                }

                // AMB 2026-03-11: Handle string event attributes (onblur, onchange, etc.)
                // These must be functions in Vue 3 h() calls.
                if (targetKey.startsWith('on') && typeof props[targetKey] === 'string' && targetKey.length > 2) {
                    const jsCode = props[targetKey];
                    props[targetKey] = (event) => {
                        try {
                            const evalContext = { ...this.context, event, window, moqui, $root: this.$root };
                            const keys = Object.keys(evalContext);
                            const values = Object.values(evalContext);
                            const fn = new Function(...keys, jsCode);
                            fn(...values);
                        } catch (e) {
                            console.error(`Error executing blueprint event [${targetKey}]:`, e, "Code:", jsCode);
                        }
                    };
                }
            });

            console.debug('BlueprintNode rendering type:', type, 'props:', props);

            let children = [];

            // Helper to render children
            const renderChildren = (ctx = this.context) => node.children ? node.children.map((child, idx) => h(BlueprintNode, {
                key: child.id || child.name || (type + '_' + idx),
                node: child,
                parentType: type,
                context: ctx,
                sourceUrl: this.sourceUrl, // AMB 2026-03-11: Propagate sourceUrl down
                parentFieldName: this.parentFieldName
            })) : [];

            switch (type) {
                case 'ScreenBlueprint':
                case 'Screen':
                case 'screen':
                case 'screen-structure':
                    console.log("Found Screen root:", node.location || type);
                    return h('div', {
                        ...props,
                        class: 'blueprint-root ' + (props.class || ''),
                        style: "min-height: 200px; " + (props.style || '')
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
                        let targetUrl = node.attributes.url;
                        console.log("BlueprintNode dynamic-dialog initial url:", targetUrl, "node:", node.attributes.id);
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
                                        console.log("BlueprintNode dynamic-dialog resolved param:", pName, "=", pValue, "from context:", context);
                                        queryParts.push(`${pName}=${encodeURIComponent(pValue)}`);
                                    } else {
                                        console.warn("BlueprintNode dynamic-dialog failed to resolve param:", pName, "from context:", context);
                                    }
                                }
                            });
                        }
                        if (queryParts.length > 0) {
                            targetUrl += (targetUrl.indexOf('?') > 0 ? '&' : '?') + queryParts.join('&');
                        }
                        props.url = targetUrl;
                        console.log("BlueprintNode dynamic-dialog final props.url:", props.url);
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
                        ...props,
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
                                    // Use prototype delegation to inherit the reactive context while overlaying row data
                                    const cellContext = Object.assign(Object.create(this.context), props.row);

                                    return h(resolveComponent('q-td'), { key: col.name, props: props }, {
                                        default: () => (field && field.children && field.children.length > 0)
                                            ? field.children.map((child, idx) => h(BlueprintNode, {
                                                key: child.id || child.name || (col.name + '_' + idx),
                                                node: child,
                                                context: cellContext,
                                                sourceUrl: this.sourceUrl // AMB 2026-03-11: Propagate sourceUrl down
                                            }))
                                            : props.value
                                    });
                                })
                            });
                        }
                    });

                case 'Text':
                case 'text':
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

                    let fieldClass = 'blueprint-field q-mb-md';
                    // AMB 2026-03-26: Support multi-column layout if parent is a FormSingle with columnCount
                    if (this.parentType === 'FormSingle' || this.parentType === 'm-form-single') {
                        // Inherit or calculate column width. Default to full width if not specified.
                        // We check the attributes of the current node's parent (the form) or a context flag.
                        // For now, if we are in a FormSingle, we assume grid child.
                        fieldClass += ' col-12';
                        // The actual column scaling happens if the children are wrapped in a 'row'.
                    }

                    return h('div', { class: fieldClass }, [
                        fieldLabel ? h('div', { class: 'text-caption text-weight-medium text-grey-9 q-mb-xs' }, fieldLabel) : null,
                        ...node.children ? node.children.map(child => h(BlueprintNode, {
                            node: child,
                            parentType: 'FormField',
                            context: this.context,
                            parentFieldName: fieldName
                        })) : []
                    ]);

                case 'text-line':
                case 'text-area':
                case 'password':
                    componentName = (type === 'text-area' ? 'q-input' : 'q-input');
                    if (type === 'text-area') props.type = 'textarea';
                    props.outlined = true;
                    props.dense = true;
                    if (!props.name && this.parentFieldName) props.name = this.parentFieldName;
                    const curField = props.name;
                    if (curField) {
                        // Initialize context if value provided and not already there
                        if (this.context[curField] === undefined && props.value !== undefined) this.context[curField] = props.value;
                        props.modelValue = this.context[curField];
                        props['onUpdate:modelValue'] = (val) => { this.context[curField] = val; };
                    }
                    if (type === 'password') props.type = 'password';
                    if (!props.label && props.title) props.label = props.title;
                    break;

                case 'date-time':
                case 'm-date-time':
                    componentName = 'm-date-time';
                    props.outlined = true;
                    props.dense = true;
                    if (!props.name && this.parentFieldName) props.name = this.parentFieldName;
                    if (props.value !== undefined) props.modelValue = props.value;
                    break;

                case 'm-drop-down':
                case 'drop-down':
                    componentName = 'm-drop-down';
                    props.outlined = true;
                    props.dense = true;
                    if (!props.name && this.parentFieldName) props.name = this.parentFieldName;
                    const ddField = props.name;
                    if (ddField) {
                        if (this.context[ddField] === undefined && props.value !== undefined) this.context[ddField] = props.value;
                        props.modelValue = this.context[ddField];
                        props['onUpdate:modelValue'] = (val) => { this.context[ddField] = val; };
                    }
                    break;

                case 'check':
                    componentName = 'q-checkbox';
                    if (!props.name && this.parentFieldName) props.name = this.parentFieldName;
                    const chkField = props.name;
                    if (chkField) {
                        if (this.context[chkField] === undefined && props.value !== undefined) this.context[chkField] = props.value;
                        props.modelValue = this.context[chkField];
                        props['onUpdate:modelValue'] = (val) => { this.context[chkField] = val; };
                    }
                    props.label = props.title || props.label || '';
                    break;

                case 'hidden':
                    const hidName = props.name || this.parentFieldName;
                    if (hidName && props.value !== undefined) {
                        // Crucial: hidden fields MUST be in the context for form submission
                        if (this.context[hidName] === undefined) this.context[hidName] = props.value;
                    }
                    return h('div', { style: 'display: none', 'data-name': hidName, 'data-value': props.value });

                case 'FormSingle':
                case 'm-form-single':
                    componentName = 'q-form';
                    props.class = (props.class || '') + ' q-pa-md';
                    props.onSubmit = () => { this.submitForm(); };
                    
                    const formChildren = renderChildren();
                    if (props.columnCount && parseInt(props.columnCount) > 1) {
                        // Wrap children in a row and apply col-6 to individual fields (handled in FormField case)
                        // Actually, we can just apply a row class here and let FormField handle its own col- class.
                        children = [h('div', { class: 'row q-col-gutter-md' }, formChildren)];
                    } else {
                        children = formChildren;
                    }
                    break;

                case 'form-query':
                case 'm-form-query':
                    componentName = 'm-form-query';
                    // Pass current URL parameters as the initial search state
                    props.searchObj = this.$root.currentParameters;
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
                    // If we have children (pre-rendered content from server), render them directly
                    if (node.children && node.children.length > 0) {
                        return node.children.map(child => h(BlueprintNode, {
                            node: child,
                            context: this.context
                        }));
                    }
                    // Otherwise map to the existing m-subscreens-active component
                    componentName = 'm-subscreens-active';
                    // AMB: Pass pathIndex/path-index from server if present
                    if (node.attributes) {
                        props.pathIndex = node.attributes['path-index'] !== undefined ? node.attributes['path-index'] : (node.attributes.pathIndex || props.pathIndex);
                    }
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
                case 'screen-split':
                case 'm-screen-split':
                    componentName = 'm-screen-split';
                    // Map class/style to extraClass/extraStyle to avoid JS keyword conflicts
                    if (props.class !== undefined) {
                        props.extraClass = props.class;
                        delete props.class;
                    }
                    if (props.style !== undefined) {
                        props.extraStyle = props.style;
                        delete props.style;
                    }
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
                        const moquiWidgets = ['container', 'container-row', 'row-col', 'link', 'form-single', 'form-list', 'text', 'render-mode'];
                        if (moquiWidgets.includes(type)) {
                            // Map to internal m- components, or just use as generic container if no m- version
                            componentName = (type === 'text' || type === 'render-mode' || type === 'container') ? 'div' : 'm-' + type;
                        } else if (type && type.includes('_')) {
                            // AMB: Automatically convert underscore tags like q_btn to hyphenated q-btn
                            componentName = type.replace(/_/g, '-');
                        } else if (type && (type[0] === type[0].toUpperCase())) {
                            // If it's capitalized but unknown (like ScreenBuilder), it's probably a screen root or custom widget
                            // Just render as a div to avoid "Unknown Node" or broken rendering
                            componentName = 'div';
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
                        // Optional: Normalize cases that should be components
                        if (!comp.startsWith('m-') && !comp.startsWith('q-') && !comp.startsWith('bp-')) {
                            // Check for common m- components if they are known Moqui widgets
                            const moquiWidgets = ['container', 'container-row', 'row-col', 'link', 'form-single', 'form-list', 'text', 'render-mode'];
                            if (moquiWidgets.includes(comp)) {
                                try {
                                    const mVersion = resolveComponent('m-' + comp);
                                    if (mVersion && typeof mVersion !== 'string') comp = mVersion;
                                } catch (e) { }
                            }
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

                // AMB 2026-03-11: Strict Vue 3 Component Binding
                if (!isHtmlTag && props.name && this.context) {
                    // 1. Prioritize reactive context for data binding
                    props.modelValue = this.context[props.name];

                    // Fallback to static node value ONLY if context doesn't have it (initial state)
                    if (props.modelValue === undefined && props.value !== undefined) {
                        props.modelValue = props.value;
                        this.context[props.name] = props.value; // Initialize context
                    }

                    // 2. Pass fields prop for Moqui components that depend on it
                    props.fields = this.context;

                    // 3. Strict Vue 3 event binding
                    props['onUpdate:modelValue'] = (val) => {
                        console.log('BlueprintNode onUpdate:modelValue:', props.name, '->', val);
                        this.context[props.name] = val;
                    };

                } else if (!isHtmlTag && props.value !== undefined && props.modelValue === undefined) {
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
        },
        methods: {
            submitForm() {
                try {
                    const node = this.node;
                    const attributes = node.attributes || {};
                    let action = attributes.action || attributes.transition || node.transition;

                    if (!action || action === '#') {
                        // AMB 2026-03-11: If action is '#' or missing, try to resolve transition against sourceUrl
                        const transition = attributes.transition || node.transition;
                        if (transition && this.sourceUrl) {
                            // Strip query params from sourceUrl to get base
                            let base = this.sourceUrl.split('?')[0];
                            // In Moqui, screen URLs are structural nodes. Transitions are nested sub-paths.
                            action = base + (base.endsWith('/') ? '' : '/') + transition;
                            console.log("Resolved '#' action to transition URL:", action);
                        } else {
                            action = this.sourceUrl; // Fallback to sourceUrl itself
                        }
                    }

                    if (!action) {
                        console.error("No action or transition defined for FormSingle", node);
                        return;
                    }

                    // Collect data from context. IMPORTANT: Convert to plain object to avoid proxy serialization issues with jQuery
                    let plainContext = {};
                    try {
                        plainContext = JSON.parse(JSON.stringify(this.context));
                    } catch (e) {
                        console.warn("Could not JSON-clone context, falling back to shallow copy:", e);
                        plainContext = { ...this.context };
                    }
                    const formData = { ...plainContext, moquiSessionToken: this.$root.moquiSessionToken };

                    console.log("Submitting form to", action, "with data:", formData);

                    const vm = this;
                    $.ajax({
                        type: 'POST',
                        url: action,
                        data: formData,
                        dataType: 'json',
                        success: function (resp) {
                            console.log("Form submission successful:", resp);

                            // AMB 2026-03-11: Merge response data back into context property by property
                            if (resp && typeof resp === 'object') {
                                for (const key in resp) {
                                    if (key !== 'messages' && key !== 'screenUrl') {
                                        vm.context[key] = resp[key];
                                    }
                                }
                            }

                            if (resp && resp.screenUrl) {
                                vm.$root.setUrl(resp.screenUrl);
                            } else if (resp && resp.messages) {
                                moqui.notifyMessages(resp.messages);
                            } else {
                                // Default success notification if no messages
                                moqui.notifyMessages([{ type: 'success', message: 'Successfully saved' }]);
                            }

                            // Emit close if we are in a dialog
                            vm.$emit('close');
                        },
                        error: function (jqXHR, textStatus, errorThrown) {
                            console.error("Form submission failed:", textStatus, errorThrown);
                            moqui.handleAjaxError(jqXHR, textStatus, errorThrown);
                        }
                    });
                } catch (err) {
                    console.error("CRITICAL ERROR in submitForm handler:", err);
                    throw err; // Re-throw to ensure Vue logs it if possible, but we've logged it now
                }
            }
        }
    });

    /**
     * Utility to check if a response is a Blueprint
     * Now checks if object has @type or @context to distinguish from standard QVT.
     */
    moqui.isBlueprint = function (obj) {
        return obj && (obj['@type'] || obj['@context'] || (obj.children && obj.blueprint));
    };

    moqui.makeBlueprintComponent = function (blueprint, sourceUrl) {
        console.log('makeBlueprintComponent called for:', blueprint?.['@type'], 'from:', sourceUrl);
        const wrap = defineComponent({
            name: 'BlueprintWrapper',
            data() {
                // Create the base reactive object
                const baseContext = reactive({});

                // Pre-scan blueprint to guarantee Vue binds to all required properties
                const scanNode = (node) => {
                    if (!node) return;

                    // Auto-load Initial Fields before render
                    if (node['@type'] === 'FormSingle' && node.fieldsInitial && typeof node.fieldsInitial === 'object') {
                        for (const key in node.fieldsInitial) {
                            if (baseContext[key] === undefined) {
                                baseContext[key] = node.fieldsInitial[key];
                            }
                        }
                    }

                    const name = node.name || (node.attributes && node.attributes.name) || (node.props && node.props.name);
                    if (name && baseContext[name] === undefined) {
                        baseContext[name] = null;
                    }

                    if (node.children && Array.isArray(node.children)) {
                        node.children.forEach(scanNode);
                    }
                };

                scanNode(blueprint);

                return {
                    reactiveContext: baseContext
                };
            },
            render() {
                return h(BlueprintNode, {
                    node: blueprint,
                    context: this.reactiveContext,
                    sourceUrl: sourceUrl // AMB 2026-03-11: Pass sourceUrl to the root node
                });
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
