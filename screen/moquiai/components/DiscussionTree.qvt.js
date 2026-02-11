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
            var vm = this;
            // Mocking service call for now until the backend service is fully implemented
            // In reality, this would call a Moqui service to get the tree structure
            // moqui.service.call('huddle.HuddleServices.getHuddleTopics', { workEffortId: this.workEffortId }, ...)

            // Simulating delay
            setTimeout(function () {
                vm.topics = [
                    {
                        workEffortId: 'WET_1001',
                        workEffortName: 'Morning Standup',
                        statusDescription: 'In Progress',
                        children: [
                            {
                                workEffortId: 'WET_1002',
                                workEffortName: 'Review Fall Risk',
                                description: 'Discuss recent incidents in East Wing.',
                                statusDescription: 'Open',
                                isClinical: true,
                                patientName: 'John Doe',
                                children: []
                            },
                            {
                                workEffortId: 'WET_1003',
                                workEffortName: 'Staffing Shortage',
                                description: 'Need coverage for night shift.',
                                statusDescription: 'Urgent',
                                isClinical: false,
                                children: []
                            }
                        ]
                    }
                ];
                vm.loading = false;
            }, 500);
        },
        addChild: function (node) {
            console.log("Add child to " + node.workEffortName);
            // logic to show dialog and add child topic
        }
    }
};
