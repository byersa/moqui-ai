/* This component renders the details (body, history) of a Huddle Topic. */
moqui.StfhdlDiscussionDetail = {
    name: 'stfhdl-discussion-detail',
    props: {
        agendaTopicId: { type: String, required: true },
        initialContent: { type: String },
        title: { type: String }
    },
    data: function () {
        return {
            tab: 'body',
            history: [],
            loadingHistory: false
        };
    },
    template: `
    <div>
        <q-tabs
            v-model="tab"
            dense
            class="text-grey"
            active-color="primary"
            indicator-color="primary"
            align="left"
            narrow-indicator
        >
            <q-tab name="body" label="Current Content" />
            <q-tab name="history" label="Version History" />
        </q-tabs>

        <q-separator />

        <q-tab-panels v-model="tab" animated>
            <q-tab-panel name="body">
                <div class="text-subtitle2 q-mb-sm">{{ title }}</div>
                <div v-html="initialContent" class="text-body2"></div>
                <div v-if="!initialContent" class="text-italic text-grey">No content provided.</div>
            </q-tab-panel>

            <q-tab-panel name="history">
                <div v-if="loadingHistory">
                     <q-spinner color="primary" size="2em" />
                </div>
                <div v-else>
                     <q-list separator dense>
                        <q-item v-for="ver in history" :key="ver.contentId">
                            <q-item-section>
                                <q-item-label caption>{{ ver.contentDate }}</q-item-label>
                                <q-item-label>{{ ver.description }}</q-item-label>
                            </q-item-section>
                            <q-item-section side>
                                <q-chip size="xs" dense>{{ ver.userId }}</q-chip>
                            </q-item-section>
                        </q-item>
                        <q-item v-if="history.length === 0">
                            <q-item-section>No history available.</q-item-section>
                        </q-item>
                    </q-list>
                     <q-btn flat label="Load History" color="primary" size="sm" @click="fetchHistory" v-if="history.length === 0 && !loadingHistory" />
                </div>
            </q-tab-panel>
        </q-tab-panels>
    </div>
    `,
    methods: {
        fetchHistory: function () {
            this.loadingHistory = true;
            var vm = this;
            $.ajax({
                type: 'POST',
                url: '/rest/s1/huddle/TopicContentHistory',
                data: { agendaTopicId: this.agendaTopicId },
                dataType: 'json',
                headers: { 'moquiSessionToken': this.moqui.webrootVue.sessionToken },
                error: function (jqXHR, textStatus, errorThrown) {
                    vm.moqui.notify('Error loading history: ' + errorThrown, 'negative');
                    vm.loadingHistory = false;
                },
                success: function (data) {
                    vm.history = data.history || [];
                    vm.loadingHistory = false;
                }
            });
        }
    }
};
moqui.webrootVue.component('stfhdl-discussion-detail', moqui.StfhdlDiscussionDetail);
