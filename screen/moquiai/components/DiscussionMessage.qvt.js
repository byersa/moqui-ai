moqui.discussionMessage = {
    name: 'discussionMessage',
    template: `
        <div class="m-discussion-message" :style="widthStyle">
            <div class="m-thread-hdr">
                <div class="m-left-hdr"></div>
                <div class="m-right-hdr">
                    <q-tree
                        class="m-thread-tree"
                        :id="'threadTree_' + parentAgendaTopicId"
                        ref="threadTree"
                        :nodes="subPath"
                        node-key="agendaTopicId"
                        label-key="description"
                        default-expand-all
                        no-results-label="N/A"
                        no-nodes-label="No Data Yet"
                        tick-strategy="none"
                    >
                        <template v-slot:default-header="prop">
                            <div class="full-height full-width m-thread-template row" :style="borderStyle" draggable="true"
                                @dragstart="startDataDrag($event, prop.key)"
                                @drag="handleDragging($event)"
                                @dragend="endDrag($event)"
                                @drop="handleDrop($event, prop.key)"
                                @dragover.prevent @dragenter.prevent
                            >
                                <q-checkbox v-if="isTop(prop)" class="col-auto m-svg-event-item m-task-checkbox m-task-todo" v-model="includedStatusId" toggle-indeterminate size="xs"
                                    @click="handleIncludedClick"
                                    checked-icon="done" indeterminate-icon="done_outline" indeterminate-value="ShInclSelected" true-value="ShInclComplete" false-value="ShInclNotUsed" />
                                <div class="m-instance-color col m-thread-text">{{ buildChildHeader(prop.key) }}</div>
                                <div class="m-topic-icon-box col-auto">
                                    <q-icon name="edit" size="20px" @click.stop.prevent="handleEditClick(prop.key)" rounded color="primary" />
                                    <q-icon name="reply" size="20px" @click.stop.prevent="handleReplyClick(prop.key)" rounded color="primary" />
                                    <q-icon name="delete" size="20px" @click.stop.prevent="handleDeleteClick(prop.key)" rounded color="primary" />
                                    <q-icon name="content_copy" size="20px" @click="handleCopyClick(prop.key)" />
                                    <q-icon name="content_paste" size="20px" @click="handleDropAndPaste()" draggable @drop="handleDropAndPaste($event)" />
                                </div>
                            </div>
                        </template>
                        <template v-slot:default-body="prop">
                            <div class="row items-center m-thread-desc">
                                <div class="col-12">{{ buildChildBody(prop.key) }}</div>
                            </div>
                        </template>
                    </q-tree>
                </div>
            </div>
            <div style="background-color:red; width:2px; height:2px; display:none;" ref="dragdiv"></div>
        </div>
    `,
    props: {
        parentAgendaTopicId: String,
        agendaTopicId: String,
        meetingInstanceId: String,
        roleTypeId: String,
        parentExpandLevel: Number,
        isDragging: Boolean,
    },
    data() {
        return {
            title: '',
            noData: false,
            expandLevel: 1,
            schedOptions: [
                { includedStatusId: "ShInclUnselected", inclDesc: "Unselected" },
                { includedStatusId: "ShInclActive", inclDesc: "Available" },
                { includedStatusId: "ShInclSelected", inclDesc: "Selected" },
                { includedStatusId: "ShInclComplete", inclDesc: "Complete" },
                { includedStatusId: "ShInclCancelled", inclDesc: "Cancelled" },
            ],
            svgWid: 16,
            svgHgt: 36,
            sizeTkn: 'md',
            combinedWidthIndexCount: 0,
            widthStyle: '',
            status: 'active',
            pairedAgendaTopic: null,
            includedStatusId: 'ShInclNotUsed',
            isExpandedMap: {},
            expanded: [],
        };
    },
    setup() {
        const store = typeof Vue !== 'undefined' ? Vue.inject('discussionStore') : null;
        return { store };
    },
    computed: {
        agendaTopicIdIndex() {
            return this.store.getAgendaTopicIndexMap(this.agendaTopicId) || 0;
        },
        subPath() {
            let lst = [];
            if (this.agendaTopicId) {
                lst = this.getParentChildTopicList(this.agendaTopicId);
                this.includedStatusId = this.agendaTopic.includedStatusId;
            }
            this.$nextTick(() => {
                if (this.$refs.threadTree) this.$refs.threadTree.expandAll();
            });
            return lst;
        },
        agendaTopic() {
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.agendaTopicId });
            if (topic) this.includedStatusId = topic.includedStatusId;
            return topic;
        },
        borderStyle() {
            return { 'border-left': '2px dashed #aaa' };
        }
    },
    methods: {
        getParentChildTopicList(id) {
            // To be implemented based on store actions
            return [];
        },
        isTop(prop) {
            return prop.node && prop.node.isTop;
        },
        buildChildHeader(key) {
            const topic = this.store.getAgendaTopic({ agendaTopicId: key });
            return topic ? topic.title : 'Unknown Title';
        },
        buildChildBody(key) {
            const topic = this.store.getAgendaTopic({ agendaTopicId: key });
            return topic ? topic.description : '';
        },
        showThread() {
            if (this.$refs.instructTree) this.$refs.instructTree.expandAll();
        },
        refresh() { },
        handleCopyClick(topicId) {
            this.store.setCopyData(JSON.stringify(topicId));
        },
        startDataDrag(e, topicId) {
            const copyDataJson = JSON.stringify(topicId);
            this.store.setCopyData(copyDataJson);
            const dupNode = this.$refs.dragdiv;
            e.dataTransfer.setDragImage(dupNode, 0, 0);
            this.$emit('dragStart', { evt: e, dragData: copyDataJson });
            dupNode.style.backgroundColor = "blue";
            document.body.appendChild(dupNode);
        },
        handleDrop(e, topicId) {
            if (!this.isDragging) this.handleDropAndPaste();
        },
        handleDropAndPaste() {
            let parentAgendaTopicId = this.parentAgendaTopicId;
            const parentAgendaTopic = this.store.getAgendaTopic({ agendaTopicId: parentAgendaTopicId });
            const values = {
                parentAgendaTopicId: this.agendaTopicId,
                meetingTemplateId: parentAgendaTopic.meetingTemplateId,
                meetingInstanceId: this.meetingInstanceId,
                refComp: this,
                callback: this.handleCallback,
            };
            this.store.handleDropAndPaste(values);
        },
        handleEditClick(key) {
            let personAccount = this.store.getCurrentPersonAccount;
            const agendaTopic = this.store.getAgendaTopic({ agendaTopicId: key });
            let parentDomainRoleList = this.store.getParentDomainRoleList(key);
            let inMap = {
                refComp: this, callback: this.handleCallback,
                okMsg: 'Save',
                parentDomainRoleList: parentDomainRoleList,
                agendaTopic: agendaTopic,
                linkOpType: 'shop_create',
                meetingTemplateId: this.meetingTemplateId,
                repositoryMeetingTemplateId: agendaTopic.repositoryMeetingTemplateId,
            };
            this.store.setLinkEditParams(inMap);
        },
        handleReplyClick(topicId) {
            let personAccount = this.store.getCurrentPersonAccount;
            let loginAccount = this.store.getLoginPersonAccount;
            const agendaTopic = {
                parentAgendaTopicId: topicId,
                orgId: personAccount.orgId,
                partyId: loginAccount.partyId,
                meetingTemplateId: this.meetingTemplateId,
                meetingInstanceId: this.meetingInstanceId,
                repositoryMeetingTemplateId: this.repositoryMeetingTemplateId,
            };
            let inMap = {
                refComp: this, callback: this.handleCallback,
                agendaTopic: agendaTopic,
                linkOpType: 'shop_threadreply',
                isConcrete: true,
            };
            this.store.setLinkEditParams(inMap);
        },
        handleDeleteClick(key) {
            // Implementation mapping
        },
        handleCallback(values) {
            this.store.incAgendaTopicIndexMap(this.agendaTopicId);
            this.$nextTick(() => {
                if (this.$refs.threadTree) this.$refs.threadTree.expandAll();
            });
        },
        handleIncludedClick() {
            // Implementation mapping
        },
        endDrag(e) {
            // Cleanup
            if (this.$refs.dragdiv && this.$refs.dragdiv.parentNode === document.body) {
                document.body.removeChild(this.$refs.dragdiv);
            }
        },
        handleDragging(e) {
            // Drag logic
        }
    },
    created() {
        console.info('discussionMessage created: ' + this.parentAgendaTopicId + " agendaTopicId: " + this.agendaTopicId);
    },
    mounted() {
        console.info('discussionMessage mounted: ' + this.parentAgendaTopicId);
    }
};
