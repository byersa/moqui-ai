
const { h, createApp, defineComponent, markRaw } = Vue
const { createRouter, createWebHistory } = VueRouter

const screenPathRoot = $("#confBasePath").val() || '/apps'
const urlPathRoot = $("#confLinkBasePath").val() || '${urlPathRoot!}'
console.log('urlPathRoot', urlPathRoot)
console.log('screenPathRoot', screenPathRoot)

// Define the catch-all route that delegates to BlueprintClient logic
const BlueprintRoute = defineComponent({
    name: 'BlueprintRoute',
    data() {
        return {
            component: null,
            loading: true,
            error: null
        }
    },
    async created() {
        await this.loadBlueprint()
    },
    watch: {
        '$route': 'loadBlueprint' 
    },
    methods: {
        async loadBlueprint() {
            this.loading = true
            this.error = null
            this.component = null
            
            // Construct the API URL for the current route
            // We want the JSON representation of the current path
            const currentPath = this.$route.path
            console.log('BlueprintRoute loading:', currentPath)
            
            try {
                // Fetch with Accept: application/json to trigger DeterministicVueRenderer
                // Ensure we have the full path for the fetch
                // Normalize fetchPath to ensure it starts with /
                let fetchPath = currentPath;
                if (!fetchPath.startsWith('/')) fetchPath = '/' + fetchPath;

                // Construct the full URL for the fetch
                let finalUrl = fetchPath;
                if (urlPathRoot && urlPathRoot !== '/') {
                    const cleanRoot = urlPathRoot.endsWith('/') ? urlPathRoot.slice(0, -1) : urlPathRoot;
                    // Only prepend the root if the path doesn't already contain it
                    if (fetchPath.indexOf(cleanRoot) !== 0) {
                        finalUrl = cleanRoot + (fetchPath === '/' ? '' : fetchPath);
                    }
                }
                
                const fetchPathFinal = finalUrl;
                console.log('BlueprintRoute fetchPathFinal:', fetchPathFinal, 'urlPathRoot:', urlPathRoot);
                
                // Guard: Avoid fetching the app root shell itself as a blueprint
                // If we are at the shell root, we should really be at /Home
                const isShell = fetchPathFinal === '/' || fetchPathFinal === urlPathRoot || fetchPathFinal === (urlPathRoot + '/');
                if (isShell) {
                    console.info('BlueprintRoute: Path is app root shell, fetching /Home instead');
                    const cleanRoot = urlPathRoot.endsWith('/') ? urlPathRoot.slice(0, -1) : urlPathRoot;
                    const homeUrl = cleanRoot + '/Home';
                    
                    const url = new URL(homeUrl, window.location.origin);
                    url.searchParams.set('renderMode', 'qjson');
                    
                    const response = await fetch(url.toString(), {
                        headers: {
                            'Accept': 'application/json',
                            'moquiSessionToken': moqui.webrootVue.moquiSessionToken || $("#confMoquiSessionToken").val()
                        }
                    })
                    if (!response.ok) throw new Error(`HTTP error! status: ${r"${response.status}"}`);
                    const blueprint = await response.json();
                    if (moqui.isBlueprint && moqui.isBlueprint(blueprint)) {
                        this.component = markRaw(moqui.makeBlueprintComponent(blueprint));
                        this.loading = false;
                        return;
                    }
                }
                
                const url = new URL(fetchPathFinal, window.location.origin);
                url.searchParams.set('renderMode', 'qjson');
                const fetchUrl = url.toString();
                
                const response = await fetch(fetchUrl, {
                    headers: {
                        'Accept': 'application/json',
                        'moquiSessionToken': moqui.webrootVue.moquiSessionToken || $("#confMoquiSessionToken").val()
                    }
                })
                
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${r"${response.status}"}`)
                }
                
                const blueprint = await response.json()
                
                if (moqui.isBlueprint && moqui.isBlueprint(blueprint)) {
                    // Create a Vue component from the Blueprint JSON using our helper
                    this.component = markRaw(moqui.makeBlueprintComponent(blueprint))
                } else {
                    console.warn('Response is not a valid ScreenBlueprint', blueprint)
                    this.error = "Invalid response from server"
                }

            } catch (e) {
                console.error('Error loading blueprint', e)
                this.error = e.message
            } finally {
                this.loading = false
            }
        }
    },
    render() {
        if (this.loading) {
            return h('div', { class: 'q-pa-md flex flex-center' }, [
                h('q-spinner', { color: 'primary', size: '3em' })
            ])
        }
        
        if (this.error) {
            return h('div', { class: 'q-pa-md text-negative' }, [
                h('div', 'Error loading screen: ' + this.error),
                h('q-btn', { 
                    label: 'Retry', 
                    color: 'primary', 
                    flat: true, 
                    onClick: this.loadBlueprint 
                })
            ])
        }
        
        if (this.component) {
            return h(this.component)
        }
        
        return h('div', 'No content')
    }
})

moqui.routes = [
    { path: '/', redirect: '/Home' },
    {
        // Catch-all route for everything under this app's root
        path: '/:pathMatch(.*)*', 
        component: BlueprintRoute
    }
]

const router = createRouter({
    history: createWebHistory(urlPathRoot), // Use root history relative to app root
    routes: moqui.routes,
})

// Export router for the app to use
moqui.webrootRouter = router
console.info("Vue Router created and assigned to moqui.webrootRouter")
