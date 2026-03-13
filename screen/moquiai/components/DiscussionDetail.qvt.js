moqui.discussionDetail = {
    name: 'discussionDetail',
    template: `
        <div class="m-topic-section">
            <div class="m-topic-row no-wrap row">
                <q-icon :name="expandName" class="col-1 m-icon-med" style="width:20px; font-weight:900;" size="24px" @click.stop="handleExpandContract" />
                <div :class="rowClass" class="m-topic-header col-auto"
                    draggable="true"
                    @dragstart="handleDragStart($event)"
                    @drop="handleDropAndPaste($event)"
                    @dragend="handleDragEnd($event)"
                    @dragenter="handleDragEnter($event)"
                    @dragleave="handleDragLeave($event)"
                    @dragover="handleDragOver($event)"
                >{{ buildChildHeader() }}</div>
                <div class="m-topic-icon-box col-auto row" ref="iconBox">
                    <q-checkbox disabled class="col-2 m-svg-event-item m-task m-task-todo"
                        v-model="includedStatusId"
                        size="xs" style="height:30px;" true-value="Y" false-value="N" />
                    <div class="col-1"></div>
                    <q-icon v-if="expandLevel" :name="bodyExpandName" class="col-1 m-icon-med" rounded color="primary" @click.stop="handleBodyExpandContract" />
                    <q-icon name="edit" class="col-1 m-icon-med" @click.stop.prevent="handleEditClick" rounded color="primary" />
                    <q-icon name="reply" class="col-1 m-icon-med" @click.stop.prevent="handleReplyClick" rounded color="primary" />
                    <q-icon name="history" class="col-1 m-icon-med" @click.stop.prevent="handleHistoryClick" rounded color="primary" />
                    <q-icon name="content_copy" class="col-1 m-icon-med" @click="handleCopyClick" />
                    <q-icon name="content_paste" class="col-1 m-icon-med" @click="handleDropAndPaste" draggable @drop="handleDropAndPaste($event)" @dragend="handleDragEnd($event)" />
                    <q-icon :name="pinName" :ref="pinName" class="col-1 m-icon-med" :color="pinColor" @click.stop.prevent="showPinMenu">
                        <q-popup-edit v-model="pinPopup">
                            <q-list dense style="min-width: 100px">
                                <q-item clickable v-close-popup>
                                    <q-item-section @click="persistPinned('PtyOrganization')">Pin for organization</q-item-section>
                                </q-item>
                                <q-item clickable v-close-popup>
                                    <q-item-section @click="persistPinned('PtyPerson')">Pin for person</q-item-section>
                                </q-item>
                            </q-list>
                        </q-popup-edit>
                    </q-icon>
                </div>
            </div>
            <div v-if="expandLevel" class="m-topic-body row">
                <div v-if="bodyExpandLevel" class="m-topic-desc col-12" draggable="true"
                    @dragstart="handleDragStart($event)" @drop="handleDropAndPaste($event)" @dragend="handleDragEnd($event)"
                    @dragenter="handleDragEnter($event)" @dragleave="handleDragLeave($event)" @dragover="handleDragOver($event)"
                >{{ buildChildBody() }}</div>
                
                <div class="m-topic-block col-12">
                    <q-tree class="full-width"
                        ref="tree"
                        :id="'TREE_' + parentAgendaTopicId"
                        :nodes="subPath"
                        node-key="meetingTemplateId"
                        label-key="description"
                        no-results-label="N/A"
                        no-nodes-label="_"
                        tick-strategy="none"
                    >
                        <template v-slot:default-header="prop">
                            <discussion-thread class="col-12 m-etb-tree-bdy"
                                :ref="'SUBCOMP_' + prop.node.meetingTemplateId"
                                :parentAgendaTopicId="parentAgendaTopicId"
                                :meetingTemplateId="prop.node.meetingTemplateId"
                                :meetingISO="meetingInstance?.meetingISO"
                                @visibilityChanged="hideBlock"
                                @includedStatusChanged="handleIncludedStatusChanged"
                            ></discussion-thread>
                        </template>
                    </q-tree>
                </div>

                <div class="m-topic-block col-12">
                    <q-list class="full-width">
                        <q-item v-for="topicMap in abstractSubPath" :key="topicMap.agendaTopicId" clickable>
                            <q-item-section>
                                <discussion-detail
                                    :ref="'IBP_' + topicMap.agendaTopicId"
                                    :parentAgendaTopicId="topicMap.agendaTopicId"
                                    :abstractSubPath="topicMap.children"
                                    :meetingInstanceId="meetingInstanceId"
                                ></discussion-detail>
                            </q-item-section>
                        </q-item>
                    </q-list>
                </div>
            </div>
        </div>
    `,
    components: {
        'discussion-thread': moqui.discussionThread,
        'discussion-detail': null // Recursive component
    },
    beforeCreate() {
        this.$options.components['discussion-detail'] = moqui.discussionDetail;
    },
    props: {
        parentAgendaTopicId: String,
        meetingInstanceId: String,
        roleTypeId: String,
        abstractSubPath: Array,
    },
    data() {
        return {
            pinPopup: false,
            expandLevel: 0,
            bodyExpandLevel: 1,
            childIncludedStatusMap: {},
        };
    },
    setup() {
        const store = typeof Vue !== 'undefined' ? Vue.inject('discussionStore') : null;
        return { store };
    },
    computed: {
        pinName() {
            return this.store.pinName(this.parentAgendaTopicId) || 'push_pin';
        },
        rowClass() {
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId });
            return {
                'm-abstract-color': topic && !topic.meetingInstanceId,
                'm-instance-color': topic && !!topic.meetingInstanceId,
            };
        },
        isPinned() {
            return this.store.getIsPinned(this.parentAgendaTopicId);
        },
        pinColor() {
            return this.isPinned === 'Y' ? 'primary' : 'grey-6';
        },
        includedStatusId() {
            let id = 'N';
            // Placeholder logic logic from child components
            return id;
        },
        meetingInstance() {
            return this.store.getMeetingInstance(this.meetingInstanceId);
        },
        expandName() {
            return this.expandLevel ? 'expand_less' : 'expand_more';
        },
        bodyExpandName() {
            return this.bodyExpandLevel ? 'arrow_upward' : 'arrow_downward';
        },
        subPath() {
            return this.store.getMeetingTemplateTreeList(this.meetingInstance?.meetingTemplateId) || [];
        }
    },
    methods: {
        handleExpandContract() {
            this.expandLevel = this.expandLevel ? 0 : 2;
        },
        handleBodyExpandContract() {
            this.bodyExpandLevel = this.bodyExpandLevel ? 0 : 1;
        },
        buildChildHeader() {
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId });
            return topic ? topic.title : 'Topic';
        },
        buildChildBody() {
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId });
            return topic ? topic.description : '';
        },
        showPinMenu() {
            this.pinPopup = true;
        },
        persistPinned(type) {
            // Implementation mapping
        },
        handleEditClick() {
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId, meetingInstanceId: this.meetingInstanceId });
            let inMap = {
                refComp: this, callback: this.handleCallback,
                agendaTopic: topic,
                linkOpType: 'shop_create',
                meetingTemplateId: topic?.meetingTemplateId,
                repositoryMeetingTemplateId: topic?.repositoryMeetingTemplateId,
            };
            this.store.setLinkEditParams(inMap);
        },
        handleReplyClick() {
            let personAccount = this.store.getCurrentPersonAccount;
            let agendaTopic = { parentAgendaTopicId: this.parentAgendaTopicId, meetingTemplateId: this.meetingInstance?.meetingTemplateId, validationConstraintMap: {}, mergedConstraintMap: {} };
            let inMap = {
                refComp: this, callback: this.handleDialogCallback,
                agendaTopic: agendaTopic,
                meetingInstanceId: this.meetingInstanceId,
                orgGroupId: personAccount.orgGroupId, userId: personAccount.userId,
            };
            this.store.setLinkEditParams(inMap);
        },
        handleHistoryClick() {
            this.$emit('versionHistoryRequest');
        },
        handleCopyClick() {
            // Mapping
        },
        handleDropAndPaste(e) {
            // Mapping
        },
        handleDragStart(e) {
            // Mapping
        },
        handleDragEnd(e) {
            // Mapping
        },
        handleDragEnter(e) { },
        handleDragLeave(e) { },
        handleDragOver(e) { },
        hideBlock() {
            // Mapping
        },
        handleIncludedStatusChanged(values) {
            this.childIncludedStatusMap[values.agendaTopicId] = values.includedStatusId;
        },
        handleCallback(values) {
            this.store.incAgendaTopicIndexMap(this.parentAgendaTopicId);
        },
        handleDialogCallback(data) {
            // Handle reply callback
        }
    }
};
