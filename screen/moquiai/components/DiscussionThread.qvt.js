moqui.discussionThread = {
    name: 'discussionThread',
    template: `
        <div>
            <div class="m-etb-container row"
                draggable="true"
                @dragstart="handleDragStart($event)"
                @drop="handleDropAndPaste($event)"
                @dragend="handleDragEnd($event)"
                @dragenter="handleDragEnter($event)"
                @dragleave="handleDragLeave($event)"
                @dragover="handleDragOver($event)"
            >
                {{ meetingTemplateName }}
                <div class="m-topic-icon-box col-auto row">
                    <q-icon name="add" size="18px" @click="handleTopInstanceAddClick" />
                    <q-icon name="content_paste" class="col-1 m-icon-med" @click="handleDropAndPaste" draggable @drop="handleDropAndPaste($event)" />
                    <q-checkbox v-if="!threadTopicIdList.length" class="col-1 m-svg-event-item m-task-todo" v-model="includedStatusId"
                        toggle-indeterminate size="xs" style="height:30px;" @click="handleIncludedStatusClick" checked-icon="done"
                        indeterminate-icon="done_outline" indeterminate-value="ShInclSelected" true-value="ShInclComplete" false-value="ShInclNotUsed" />
                </div>
                <div v-if="expandLevel">
                    <q-list bordered class="rounded-borders full-width m-instance-blk-list">
                        <q-item v-for="topicId in threadTopicIdList" :key="topicId" class="m-instance-dept-line" @show="processOpen">
                            <div class="m-instance-blk-card">
                                <div class="m-instance-blk-card-section">
                                    <discussion-message
                                        :ref="'SUBCOMP_' + topicId"
                                        :parentAgendaTopicId="parentAgendaTopicId"
                                        :agendaTopicId="topicId"
                                        :meetingInstanceId="meetingInstance?.meetingInstanceId"
                                        :parentExpandLevel="expandLevel"
                                        @includedStatusChanged="handleIncludedStatusChanged"
                                    ></discussion-message>
                                </div>
                            </div>
                        </q-item>
                    </q-list>
                </div>
            </div>
        </div>
    `,
    components: {
        'discussion-message': moqui.discussionMessage,
    },
    props: {
        parentAgendaTopicId: String,
        meetingTemplateId: String,
        meetingISO: String,
    },
    data() {
        return {
            status: 'active',
            sizeTkn: 'md',
            expandLevel: 1,
            childIncludedStatusMap: {},
            meetingInstance: null,
        };
    },
    setup() {
        const store = typeof Vue !== 'undefined' ? Vue.inject('discussionStore') : null;
        return { store };
    },
    computed: {
        subPath() {
            return this.store.getMeetingTemplateTreeList(this.meetingInstance?.meetingTemplateId) || [];
        },
        expandName() {
            return this.expandLevel ? 'expand_less' : 'expand_more';
        },
        includedStatusId: {
            get() {
                let id = 'ShInclNotUsed';
                this.threadTopicIdList.forEach(ky => {
                    const topic = this.store.getAgendaTopic({ agendaTopicId: ky });
                    if (topic && topic.includedStatusId !== 'ShInclNotUsed') {
                        id = topic.includedStatusId;
                    }
                });
                return id;
            },
            set(val) {
                // setter logic
            }
        },
        agendaTopicIdIndex() {
            return this.store.getAgendaTopicIndexMap(this.parentAgendaTopicId) || 0;
        },
        threadTopicIdList() {
            const values = { parentAgendaTopicId: this.parentAgendaTopicId, meetingTemplateId: this.meetingTemplateId, meetingISO: this.meetingISO };
            if (this.meetingInstance) {
                values.meetingInstanceId = this.meetingInstance.meetingInstanceId;
            }
            return this.store.getThreadTopicIdList(values) || [];
        },
        meetingTemplateName() {
            const meetingTemplate = this.store.getMeetingTemplate(this.meetingTemplateId);
            let nm = meetingTemplate ? meetingTemplate.name : 'Unknown Template';
            if (this.meetingInstance) {
                nm += '(' + this.meetingInstance.meetingInstanceId + ')';
            }
            return nm;
        }
    },
    methods: {
        getMeetingInstance() {
            const completePromise = this.store.getSyncMeetingInstance({ meetingTemplateId: this.meetingTemplateId, meetingISO: this.meetingISO });
            completePromise.then(response => {
                this.meetingInstance = response;
            });
        },
        hideBlock() {
            this.expandLevel = this.expandLevel ? 0 : 1;
        },
        handleDropAndPaste() {
            const parentAgendaTopic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId });
            const values = {
                parentAgendaTopicId: this.parentAgendaTopicId,
                meetingTemplateId: parentAgendaTopic.meetingTemplateId,
                meetingInstanceId: this.meetingInstanceId, // Need to make this accessible
                refComp: this,
                callback: this.handleCallback,
            };
            this.store.handleDropAndPaste(values);
        },
        handleTopInstanceAddClick() {
            let personAccount = this.store.getCurrentPersonAccount;
            let parentDomainRoleList = this.store.getParentDomainRoleList;
            const targetUrl = '/huddle/meeting/persistInstanceAgendaTopic';
            const agendaTopic = {
                parentAgendaTopicId: this.parentAgendaTopicId,
                okMsg: 'Save',
                orgId: personAccount.orgId,
                partyId: personAccount.partyId,
                meetingTemplateId: this.meetingTemplateId,
                meetingInstanceId: this.meetingInstance?.meetingInstanceId,
            };
            let inMap = {
                refComp: this, callback: this.handleCallback,
                agendaTopic: agendaTopic,
                parentDomainRoleList: parentDomainRoleList,
                linkOpType: 'shop_threadreply',
                isConcrete: true,
                meetingISO: this.meetingISO,
                targetUrl: targetUrl,
            };
            this.store.setLinkEditParams(inMap);
        },
        handleCallback(values) {
            this.store.incAgendaTopicIndexMap(this.parentAgendaTopicId);
            this.$nextTick(() => {
                this.buildSubPath();
            });
        },
        buildSubPath() {
            if (this.threadTopicIdList && this.threadTopicIdList.length) {
                this.$nextTick(() => {
                    this.threadTopicIdList.forEach(topicId => {
                        const compArr = this.$refs['SUBCOMP_' + topicId];
                        if (compArr && compArr.length) {
                            compArr[0].buildSubPath();
                        }
                    });
                });
            }
        },
        handleIncludedStatusClick() {
            const personAccount = this.store.getCurrentPersonAccount;
            const inclInMap = {
                includedStatusList: [{
                    includedStatusId: 'ShInclSelected',
                    parentAgendaTopicId: this.parentAgendaTopicId,
                    meetingTemplateId: this.meetingTemplateId,
                    meetingInstanceId: this.meetingInstance?.meetingInstanceId,
                    partyId: personAccount.partyId,
                    orgId: personAccount.orgId,
                }],
                refComp: this,
                callback: this.handleCallback,
            };
            this.store.saveIncludedStatus(inclInMap);
        },
        handleIncludedStatusChanged(values) {
            this.childIncludedStatusMap[values.agendaTopicId] = values.includedStatusId;
            this.$emit("includedStatusChanged", values);
        },
        processOpen() {
            // Placeholder logic
        },
        handleDragStart(e) {
            const dupNode = document.createElement('div');
            dupNode.id = 'DRAGDIV';
            dupNode.style.display = 'none';
            document.body.appendChild(dupNode);
            const topic = this.store.getAgendaTopic({ agendaTopicId: this.parentAgendaTopicId });
            dupNode.textContent = topic ? topic.title : 'Drag Node';
            this.store.setCopyData({ agendaTopic: topic });
            e.dataTransfer.effectAllowed = 'copy';
            e.dataTransfer.setDragImage(dupNode, 0, 0);
            e.stopPropagation();
        },
        handleDragEnd(e) {
            const dupNode = document.getElementById('DRAGDIV');
            if (dupNode) {
                dupNode.textContent = '';
                document.body.removeChild(dupNode);
            }
        },
        handleDragOver(e) {
            const isDrag = this.store.getIsDraggable;
            if (isDrag) {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'copy';
                e.dataTransfer.effectAllowed = 'copy';
            } else {
                e.dataTransfer.dropEffect = 'none';
                e.dataTransfer.effectAllowed = 'none';
            }
        },
        handleDragEnter(e) {
            const values = this.store.getCopyData;
            const agendaTopic = values ? values.agendaTopic : {};
            let isDrag = true;
            if (agendaTopic && agendaTopic.agendaTopicId === this.parentAgendaTopicId) {
                isDrag = false;
            }
            this.store.setIsDraggable(isDrag);
        },
        handleDragLeave(e) {
            this.store.setIsDraggable(false);
        }
    },
    created() {
        if (!this.meetingInstance) {
            this.getMeetingInstance();
        }
    }
};
