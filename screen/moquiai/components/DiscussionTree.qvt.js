moqui.discussionTree = {
    name: 'discussionTree',
    template: `
        <div class="m-instance-box" :style="widthStyle" style="" draggable="false" @drop.prevent
                       @dragover.prevent @dragenter.prevent >
            <div class="m-instance-box-subunit" >
                <q-toolbar class="m-instance-box-subunit-toolbar" >
                    <div class="m-dept-title " style="text-align:left;">
                        {{meetingInstance?.meetingISO || 'N/A'}} ({{meetingInstanceId}}/{{meetingTemplate?.name || 'N/A'}})
                    </div>
                    <q-space></q-space>
                    <q-btn v-if="showFilter" flat color="primary" text-color="white" rounded dense icon="close" @click="showFilter=false">Filtered Data</q-btn>
                    <q-btn color="primary" text-color="secondary" outline dense label='Filter' @click='showFilterDialog' class="q-mr-sm" ></q-btn>
                    <div class="m-topic-icon-box ">
                        <q-icon name="visibility_off" size="20px" @click="hidePanel" >
                            <q-tooltip>Hide this meeting page</q-tooltip>
                        </q-icon>
                        <q-icon name="content_paste" size="20px" @click="handlePasteClick(null)"  @drop="handlePasteClick($event,1)">
                            <q-tooltip>Create a template page by click here after copying a source content</q-tooltip>
                        </q-icon>
                        <q-icon name="add" size="20px" @click="handleInstanceClick"  >
                            <q-tooltip>Create a template page</q-tooltip>
                        </q-icon>
                        <q-icon name="event" size="20px" @click="showScheduleView"  >
                            <q-tooltip>Show scheduled events</q-tooltip>
                        </q-icon>
                    </div>
                </q-toolbar>
                <div class="m-instance-box-subunit-body" >
                    <q-list>
                        <q-item v-for="topicMap in subPath" :key="topicMap.agendaTopicId" clickable >
                            <q-item-section>
                                <discussion-detail
                                        :ref="'IBP_' + topicMap.agendaTopicId"
                                        :parentAgendaTopicId="topicMap.agendaTopicId"
                                        :abstractSubPath="topicMap.children"
                                        :meetingInstanceId="meetingInstanceId"
                                        @versionHistoryRequest="showVersionHistory"
                                ></discussion-detail>
                            </q-item-section>
                        </q-item>
                    </q-list>
                </div>
            </div>
            <div style="flex-basis:16px; flex-shrink:0; flex-grow:0; height:100%; " :style="borderStyle" draggable="true" 
                @dragstart="startDrag" @drag="handleDragging($event)" @dragend="endDrag($event)" @drop="handleDrop($event,1)"
                @dragover.prevent @dragenter.prevent>
            </div>
            <div style="background-color:#FFBF00; width:6px; height:44px; display:none; " ref="dragdiv" ></div>
        </div>
    `,
    components: {
        'discussion-detail': moqui.discussionDetail,
    },
    props: {
        meetingInstanceId: String,
        meetingTemplateId: String,
        repositoryMeetingTemplateId: String,
        locale: String,
        loadedInstanceIdList: Array,
        threadWidthMap: Object,
        storeFunction: { type: String, default: 'useMeetingsStore' }
    },
    data() {
        return {
            sizeTkn: 'md',
            widthStyle: '',
            status: 'active',
            showFilter: false,
            filteredIdList: [],
            showAllScheduled: 'N',
        };
    },
    setup(props) {
        let storeFunc = moqui[props.storeFunction] || window[props.storeFunction];
        
        if (typeof storeFunc !== 'function') {
            console.warn(`[DiscussionTree] Store function '${props.storeFunction}' not found. Falling back to 'useMeetingsStore'.`);
            storeFunc = window.useMeetingsStore || moqui.useMeetingsStore;
        }

        const store = storeFunc ? storeFunc() : null;
        
        if (store && typeof Vue !== 'undefined') {
            Vue.provide('discussionStore', store);
        }
        
        return { store };
    },
    computed: {
        borderStyle() {
            let cls = { 'background-color': '#FFBF00' };
            if (this.lastInstanceId === this.meetingInstanceId) {
                cls = { 'background-color': 'white' };
            }
            return cls;
        },
        lastInstanceId() {
            return this.loadedInstanceIdList && this.loadedInstanceIdList.length 
                ? this.loadedInstanceIdList[this.loadedInstanceIdList.length - 1] 
                : '';
        },
        meetingInstance() {
            return this.aiTreeStore.getMeetingInstance(this.meetingInstanceId);
        },
        meetingTemplate() {
            return this.meetingInstance ? this.aiTreeStore.getMeetingTemplate(this.meetingInstance.meetingTemplateId) : null;
        },
        shortTopicIdList() {
            if (!this.meetingInstance) return [];
            const abstractKeyList = this.aiTreeStore.getAbstractTreeList({
                meetingTemplateId: this.meetingTemplateId, 
                orgId: this.meetingInstance.orgId, 
                meetingInstanceId: this.meetingInstanceId, 
                showAllScheduled: this.showAllScheduled 
            });
            const instanceKeyList = this.aiTreeStore.getInstanceTreeList({ meetingInstanceId: this.meetingInstanceId });
            return abstractKeyList.concat(instanceKeyList);
        },
        subPath() {
            let shortTopicList = [];
            let idList = this.showFilter ? this.filteredIdList : this.shortTopicIdList;

            idList.forEach(topicId => {
                let threadList = this.aiTreeStore.getShortTopicList(topicId);
                if (threadList && threadList.length) {
                    shortTopicList.push(threadList[0]);
                } else {
                    shortTopicList.push({ agendaTopicId: topicId });
                }
            });

            this.$nextTick(() => {
                // Call buildSubPath on children if needed
            });
            return shortTopicList;
        }
    },
    methods: {
        showFilterDialog() {
            // Mapping
            if (this.$refs.filterdialog) this.$refs.filterdialog.show();
        },
        hidePanel() {
            this.status = "inactive";
            this.sizeTkn = "mn";
            this.$emit('visibilityChanged', { meetingInstanceId: this.meetingInstanceId, sizeTkn: this.sizeTkn, status: this.status });
        },
        handlePasteClick(evt, val) {
            // Mapping
        },
        handleInstanceClick() {
            const personAccount = this.aiTreeStore.getCurrentPersonAccount;
            const parentDomainRoleList = this.aiTreeStore.getParentDomainRoleList;
            const agendaTopic = {
                meetingTemplate: this.meetingTemplate,
                meetingTemplateId: this.meetingTemplateId,
                meetingInstanceId: this.meetingInstanceId,
                orgId: personAccount.orgId, 
                partyId: personAccount.partyId,
            };
            const dialogInMap = {
                agendaTopic: agendaTopic,
                refComp: this, callback: this.handlePersistReturn,
                meetingInstanceId: this.meetingInstance?.meetingInstanceId,
                parentDomainRoleList: parentDomainRoleList,
                linkOpType: 'shop_instancetop',
                repositoryMeetingTemplateId: this.repositoryMeetingTemplateId,
            };
            this.aiTreeStore.setLinkEditParams(dialogInMap);
        },
        showScheduleView() {
            if (this.$refs.scheduleviewdialog) this.$refs.scheduleviewdialog.show();
        },
        showVersionHistory() {
            if (this.$refs.versiondialog) this.$refs.versiondialog.show();
        },
        handlePersistReturn(data) {
            const topicIdList = Object.keys(data.transferDataMap.topicDB);
            this.aiTreeStore.incAgendaTopicIndex();
            this.$nextTick(() => {
                if (topicIdList.length) {
                    this.aiTreeStore.setCurrentAgendaTopicId(topicIdList[0]);
                }
            });
        },
        startDrag(evt) {
            const dupNode = this.$refs.dragdiv;
            dupNode.style.display = "block";
            evt.dataTransfer.setDragImage(dupNode, 0, 0);
            this.$emit('dragStart', { evt: evt, meetingTemplateId: this.meetingTemplateId });
        },
        endDrag(evt) {
            const dupNode = this.$refs.dragdiv;
            dupNode.style.display = "none";
            this.$emit('dragging', { clientX: this.clientX, meetingInstanceId: this.meetingInstanceId });
        },
        handleDragging(e) {
            this.clientX = e.clientX;
        },
        handleDrop(e, something) {
            // Logic
        }
    }
};
