/* This component renders the Staff Huddle Discussion Tree using q-table with flattened hierarchy. */
moqui.StfhdlDiscussionTree = {
    name: 'stfhdl-discussion-tree',
    props: {
        meetingInstanceId: { type: String, required: false },
        agendaTopicId: { type: String, required: false },
        readonly: { type: Boolean, default: false }
    },
    data: function () {
        return {
            topics: [], // Flattened list
            loading: false,
            error: null,
            columns: [
                { name: 'selected', label: '', align: 'left', field: 'selected', style: 'width: 50px' },
                { name: 'orgLevel', label: 'Org Level', align: 'left', field: 'orgLevel', style: 'width: 120px' },
                { name: 'facets', label: 'Facets', align: 'left', field: 'facets' },
                { name: 'title', label: 'Topic Title', align: 'left', field: 'title', sortable: false },
                { name: 'status', label: 'Status', align: 'center', field: 'statusId' },
                { name: 'actions', label: 'Actions', align: 'right' }
            ],
            selectedTopics: []
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
            <q-table
                :data="topics"
                :columns="columns"
                row-key="agendaTopicId"
                dense
                flat
                bordered
                virtual-scroll
                :pagination="{ rowsPerPage: 0 }"
                hide-bottom
                selection="multiple"
                :selected.sync="selectedTopics"
            >
                <!-- Org Level Column Renderer -->
                <template v-slot:body-cell-orgLevel="props">
                    <q-td :props="props">
                        <div class="row q-gutter-xs no-wrap">
                            <!-- 4 Boxes: Corp, Div, Region, Facility -->
                            <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'CORP') }" title="Corporate">C</div>
                            <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'DIV') }" title="Division">D</div>
                            <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'REG') }" title="Region">R</div>
                            <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'FAC') }" title="Facility">F</div>
                        </div>
                    </q-td>
                </template>

                <!-- Facets Column -->
                <template v-slot:body-cell-facets="props">
                    <q-td :props="props">
                        <div class="row q-gutter-xs">
                             <q-icon v-for="facet in props.row.facets" :key="facet.facetValueLookupId" name="label" size="xs" color="grey" />
                        </div>
                    </q-td>
                </template>

                 <!-- Title Column (Flat, No Indent) -->
                <template v-slot:body-cell-title="props">
                    <q-td :props="props" class="cursor-pointer" @click="props.expand = !props.expand">
                        <div class="text-weight-medium">{{ props.row.title }}</div>
                    </q-td>
                </template>

                <!-- Status Column -->
                <template v-slot:body-cell-status="props">
                     <q-td :props="props">
                        <q-chip size="xs" color="blue-grey" text-color="white">{{ props.row.statusId }}</q-chip>
                     </q-td>
                </template>

                <!-- Actions Column -->
                <template v-slot:body-cell-actions="props">
                    <q-td :props="props">
                        <q-btn dense flat round icon="add" size="sm" @click.stop="addSubTopic(props.row)">
                            <q-tooltip>Add Sub-topic</q-tooltip>
                        </q-btn>
                        <q-btn dense flat round icon="edit" size="sm" @click.stop="editTopic(props.row)" />
                        <q-btn dense flat round icon="history" size="sm" @click.stop="props.expand = !props.expand">
                             <q-tooltip>Content/History</q-tooltip>
                        </q-btn>
                    </q-td>
                </template>

                <!-- Expandable Row Detail -->
                <template v-slot:header="props">
                    <q-tr :props="props">
                        <q-th auto-width />
                        <q-th v-for="col in props.cols" :key="col.name" :props="props">{{ col.label }}</q-th>
                    </q-tr>
                </template>

                 <template v-slot:body="props">
                    <q-tr :props="props">
                        <q-td auto-width>
                            <q-checkbox v-model="props.selected" />
                        </q-td>
                        <q-td v-for="col in props.cols" :key="col.name" :props="props">
                             <!-- Reuse the custom slot renderers or default -->
                             <span v-if="col.name === 'orgLevel'">
                                  <div class="row q-gutter-xs no-wrap">
                                    <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'CORP') }">C</div>
                                    <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'DIV') }">D</div>
                                    <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'REG') }">R</div>
                                    <div class="sh-org-box" :class="{ 'filled': isOrgLevel(props.row, 'FAC') }">F</div>
                                </div>
                             </span>
                             <span v-else-if="col.name === 'facets'">
                                 <q-icon v-for="facet in props.row.facets" :key="facet.facetValueLookupId" name="label" size="xs" color="grey" />
                             </span>
                             <span v-else-if="col.name === 'title'" class="cursor-pointer text-weight-medium" @click="props.expand = !props.expand">
                                 {{ props.row.title }}
                             </span>
                             <span v-else-if="col.name === 'status'">
                                 <q-chip size="xs" color="blue-grey" text-color="white">{{ props.row.statusId }}</q-chip>
                             </span>
                             <span v-else-if="col.name === 'actions'">
                                <q-btn dense flat round icon="add" size="sm" @click.stop="addSubTopic(props.row)" />
                                <q-btn dense flat round icon="edit" size="sm" @click.stop="editTopic(props.row)" />
                                <q-btn dense flat round icon="history" size="sm" @click.stop="props.expand = !props.expand" />
                             </span>
                             <span v-else>{{ col.value }}</span>
                        </q-td>
                    </q-tr>
                    <q-tr v-show="props.expand" :props="props">
                        <q-td colspan="100%">
                            <div class="q-pa-md bg-grey-1">
                                <!-- Detailed Content Component -->
                                <stfhdl-discussion-detail 
                                    :agendaTopicId="props.row.agendaTopicId" 
                                    :initialContent="props.row.description" 
                                    :title="props.row.title"
                                />
                            </div>
                        </q-td>
                    </q-tr>
                </template>

            </q-table>
        </div>
    </div>
    `,
    mounted: function () {
        // Inject CSS for Org Level Boxes
        var style = document.createElement('style');
        style.type = 'text/css';
        style.innerHTML = `
            .sh-org-box {
                display: inline-block;
                width: 18px;
                height: 18px;
                border: 1px solid #ddd;
                font-size: 10px;
                text-align: center;
                line-height: 16px;
                color: #bbb;
                font-weight: bold;
                background-color: #f5f5f5;
                cursor: default;
            }
            .sh-org-box.filled {
                background-color: #1976D2; /* Primary Blue */
                color: white;
                border-color: #1565C0;
            }
        `;
        document.head.appendChild(style);

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
                data: {
                    meetingInstanceId: this.meetingInstanceId,
                    agendaTopicId: this.agendaTopicId
                },
                dataType: 'json',
                headers: { 'moquiSessionToken': this.moqui.webrootVue.sessionToken },
                error: function (jqXHR, textStatus, errorThrown) {
                    vm.error = "Error loading topics: " + textStatus + " " + errorThrown;
                    vm.loading = false;
                },
                success: function (data) {
                    if (data && data.topicTree) {
                        // Flatten the tree
                        vm.topics = vm.flattenTree(data.topicTree);
                    } else {
                        vm.topics = [];
                    }
                    vm.loading = false;
                }
            });
        },
        flattenTree: function (nodes) {
            var flat = [];
            var traverse = function (list) {
                if (!list) return;
                for (var i = 0; i < list.length; i++) {
                    flat.push(list[i]);
                    // Recursively add children immediately after parent (sorted/flattened)
                    if (list[i].children && list[i].children.length > 0) {
                        traverse(list[i].children);
                    }
                }
            };
            traverse(nodes);
            return flat;
        },
        isOrgLevel: function (row, level) {
            // Check the orgLevel array returned from HuddleServices.groovy
            return row.orgLevel && row.orgLevel.indexOf(level) !== -1;
        },
        addSubTopic: function (node) {
            var vm = this;
            this.$q.dialog({
                title: 'Add Sub-topic',
                message: 'Enter topic name for: ' + node.title,
                prompt: { model: '', type: 'text' },
                cancel: true,
                persistent: true
            }).onOk(function (data) {
                if (!data) return;
                vm.loading = true;
                $.ajax({
                    type: 'POST',
                    url: '/rest/s1/huddle/HuddleTopic',
                    data: {
                        parentAgendaTopicId: node.agendaTopicId,
                        meetingInstanceId: vm.meetingInstanceId, // propagate instance
                        title: data
                    },
                    dataType: 'json',
                    headers: { 'moquiSessionToken': vm.moqui.webrootVue.sessionToken },
                    error: function (jqXHR, textStatus, errorThrown) {
                        vm.$q.notify({ type: 'negative', message: 'Error adding topic: ' + errorThrown });
                        vm.loading = false;
                    },
                    success: function () {
                        vm.$q.notify({ type: 'positive', message: 'Topic added successfully' });
                        vm.fetchTopics();
                    }
                });
            });
        },
        editTopic: function (node) {
            var vm = this;
            this.$q.dialog({
                title: 'Edit Topic',
                message: 'Update topic title:',
                prompt: { model: node.title, type: 'text' },
                cancel: true,
                persistent: true
            }).onOk(function (data) {
                if (!data || data === node.title) return;
                vm.loading = true;
                $.ajax({
                    type: 'POST',
                    url: '/rest/s1/huddle/HuddleTopicContent', // Use update service
                    data: {
                        agendaTopicId: node.agendaTopicId,
                        title: data
                    },
                    dataType: 'json',
                    headers: { 'moquiSessionToken': vm.moqui.webrootVue.sessionToken },
                    error: function (jqXHR, textStatus, errorThrown) {
                        vm.$q.notify({ type: 'negative', message: 'Error updating topic: ' + errorThrown });
                        vm.loading = false;
                    },
                    success: function () {
                        vm.$q.notify({ type: 'positive', message: 'Topic updated successfully' });
                        vm.fetchTopics();
                    }
                });
            });
        }
    }
};
moqui.webrootVue.component('stfhdl-discussion-tree', moqui.StfhdlDiscussionTree);
