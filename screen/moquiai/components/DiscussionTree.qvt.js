/* This component renders the Staff Huddle Discussion Tree based on the stfhdl-screen.xsd definition. */
moqui.StfhdlDiscussionTree = {
    name: 'stfhdl-discussion-tree',
    props: {
        workEffortId: { type: String, required: true },
        readonly: { type: Boolean, default: false },
        encryptNotes: { type: Boolean, default: true },
        showPatientContext: { type: Boolean, default: false }
    },
    data: function () {
        return {
            topics: [],
            loading: false,
            error: null
        };
    },
    template: `
    <div class="q-pa-md">
        <div v-if="loading" class="row justify-center">
            <q-spinner color="primary" size="3em" />
        </div>
        <div v-else-if="error" class="text-negative">
            {{ error }}
        </div>
        <div v-else>
            <q-tree
                :nodes="topics"
                node-key="workEffortId"
                label-key="workEffortName"
                default-expand-all
            >
                <template v-slot:default-header="prop">
                    <div class="row items-center">
                        <div class="text-weight-bold">{{ prop.node.workEffortName }}</div>
                        <q-chip v-if="prop.node.statusDescription" size="sm" color="primary" text-color="white" class="q-ml-sm">
                            {{ prop.node.statusDescription }}
                        </q-chip>
                         <q-chip v-if="showPatientContext && prop.node.patientName" size="sm" color="secondary" text-color="white" icon="person" class="q-ml-sm">
                            {{ prop.node.patientName }}
                        </q-chip>
                    </div>
                </template>
                <template v-slot:default-body="prop">
                    <div v-if="prop.node.description" class="q-pa-sm text-grey-8">
                         <span v-if="encryptNotes && prop.node.isClinical">
                            [Clinical Note Encrypted/Masked]
                         </span>
                         <span v-else>
                            {{ prop.node.description }}
                         </span>
                    </div>
                    <div class="row q-gutter-sm q-mt-xs" v-if="!readonly">
                         <!-- Actions placeholder -->
                         <q-btn size="sm" flat round color="primary" icon="add_comment" @click.stop="addChild(prop.node)">
                            <q-tooltip>Add Sub-topic</q-tooltip>
                         </q-btn>
                    </div>
                </template>
            </q-tree>
        </div>
    </div>
    `,
    mounted: function () {
        this.fetchTopics();
    },
    methods: {
        fetchTopics: function () {
            this.loading = true;
            this.error = null;
            var vm = this;

            $.ajax({
                type: 'POST',
                url: '/rest/s1/huddle/HuddleDiscussionTree',
                data: { workEffortId: this.workEffortId },
                dataType: 'json',
                headers: { 'moquiSessionToken': this.moqui.webrootVue.sessionToken },
                error: function (jqXHR, textStatus, errorThrown) {
                    vm.error = "Error loading topics: " + textStatus + " " + errorThrown;
                    vm.loading = false;
                },
                success: function (data) {
                    if (data && data.topicTree) {
                        vm.topics = [data.topicTree];
                    } else {
                        vm.topics = [];
                    }
                    vm.loading = false;
                }
            });
        },
        addChild: function (node) {
            var vm = this;
            this.$q.dialog({
                title: 'Add Sub-topic',
                message: 'Enter topic name for: ' + node.workEffortName,
                prompt: {
                    model: '',
                    type: 'text'
                },
                cancel: true,
                persistent: true
            }).onOk(function (data) {
                if (!data) return;

                vm.loading = true;
                $.ajax({
                    type: 'POST',
                    url: '/rest/s1/huddle/HuddleTopic',
                    data: {
                        parentWorkEffortId: node.workEffortId,
                        workEffortName: data
                    },
                    dataType: 'json',
                    headers: { 'moquiSessionToken': vm.moqui.webrootVue.sessionToken },
                    error: function (jqXHR, textStatus, errorThrown) {
                        vm.$q.notify({ type: 'negative', message: 'Error adding topic: ' + errorThrown });
                        vm.loading = false;
                    },
                    success: function () {
                        vm.$q.notify({ type: 'positive', message: 'Topic added successfully' });
                        vm.fetchTopics(); // Refresh the tree
                    }
                });
            });
        }
    }
};
