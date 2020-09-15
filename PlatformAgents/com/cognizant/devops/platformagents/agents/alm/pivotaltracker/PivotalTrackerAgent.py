#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
'''
Created on May 9, 2018

@author: 476693
'''
from ....core.BaseAgent import BaseAgent
import time
import requests
import json
import datetime
from dateutil import parser


class PivotalTrackerAgent(BaseAgent):
    def process(self):
        self.setUpVariables()
        self.login()
        self.getProjectList()
        self.getAllEpics()
        self.storyDataCollection()
        self.getIterationData()
        if self.config.get('getAllActivity', False):
            self.getAllActivities()
        if self.config.get('getAllMembers', False):
            self.getAllMembers()
    def getAllMembers(self):
        self.memberData = []

        memeber_relation_metadata = {"labels" : ["LATEST"],
                                     "relation" : {"name" : "MEMBER_DETAILS",
                                      "properties" : ["ownerId", "memeberName", "memeberEmail"],
                                      "source" : {"constraints" : ["projectId", "memeberName"] },
                                      "destination" : { "constraints" : ["ownerId"]}}}
        for project in self.all_projects:
            projectId = str(project.get('projectId'))
            memberships = self.getResponse(self.baseEndPoint +  "/services/v5/projects/"+ projectId +"/memberships",
                                     'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
            for member in memberships:
                injectData = {}
                injectData['projectId'] = projectId
                injectData['ownerId'] = member.get('person', {}).get('id')
                injectData['memeberName'] = member.get('person', {}).get('name')
                injectData['memeberEmail'] = member.get('person', {}).get('email')
                injectData['memeberUserName'] = member.get('person', {}).get('username')
                self.memberData.append(injectData)
        self.publishToolsData(self.memberData, memeber_relation_metadata)
    def getAllActivities(self):
        self.allActivitiesData = []
        for project in self.all_projects:
            projectId = str(project.get('projectId'))
            activityCollection = True
            offset = 0
            startFrom = self.tracking.get('trackingInfo', {}).get(str(projectId), {}).get('lastActivityDate', self.startFrom)
            lastUpdatedDate = 0
            while activityCollection:
                activities = self.getResponse(self.baseEndPoint +  "/services/v5/projects/"+ projectId + "/activity"
                                              +"?occurred_after=" + startFrom + "&limit=200&offset=" + str(offset),
                                     'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
                offset = offset + 200
                if len(activities) > 0:
                    latest_update_time = int(time.mktime(time.strptime(activities[0]['occurred_at'], self.timeStampFormat)))
                    if lastUpdatedDate < latest_update_time:
                        lastUpdatedDate = latest_update_time
                        trackingDetails = self.tracking.get('trackingInfo', {}).get(str(projectId), {})
                        trackingDetails['lastActivityDate'] = activities[0]['occurred_at']
                for activity in activities:
                    injectData = {}
                    injectData['projectId'] = projectId
                    injectData['nodeType'] = 'activity update'
                    dateCheck = lambda lastUpdatedDate: activity.get('occurred_at', '')
                    if self.startFrom < latest_update_time:
                        activity.get('occurred_at', '')

                    else :
                        lastUpdatedDate=dateCheck(activity.get('occurred_at', ''))
                    if 'primary_resources' in activity:
                        injectData['key'] = activity.get('primary_resources')[0].get('id', '')
                        injectData['storyName'] = activity.get('primary_resources')[0].get('name', '')
                        injectData['storyType'] = activity.get('primary_resources')[0].get('story_type', '')
                    if len( activity.get('changes') ) > 0:
                        for change in activity.get('changes'):
                            injectData['occurredAt'] =  activity.get('occurred_at','')
                            injectData['kind'] =  activity.get('kind','')
                            #injectData['storyState'] =  activity.get('current_state','')
                            injectData['message'] =  activity.get('message','')
                            if 'original_values' in change and 'estimate' in change['original_values']:
                                injectData['storyPoint'] = change['new_values']['estimate']
                            elif 'original_values' in change and 'current_state' in change['original_values']:
                                injectData['storyState'] = change['new_values']['current_state']
                            elif 'new_values' in change and 'description' in change['new_values']:
                                injectData['description'] = change['new_values']['description']
                            if 'new_values' in change and 'commit_identifier' in change.get('new_values'): 
                                injectData['attachedCommitId'] = change['new_values']['commit_identifier']
                                injectData['attachedScmType'] = change['new_values']['commit_type']
                                injectData['attachedCommitMessage'] = change['new_values']['text']
                            self.allActivitiesData += self.parseResponse(self.activityResponseTemplate, activity, injectData)
                if len(activities) == 0:
                    activityCollection = False
                if len(activities)> 0:
                    self.tracking['trackingInfo'][(projectId)] = trackingDetails
                    activityinsighstTimeX = self.config.get('dynamicTemplate', {}).get('activity',{}).get('insightsTimeXFieldMapping',None)
                    timestamp = activityinsighstTimeX.get('timefield',None)
                    timeformat = activityinsighstTimeX.get('timeformat',None)
                    isEpoch = activityinsighstTimeX.get('isEpoch',False)
                    self.publishToolsData(self.allActivitiesData,self.activityMetadata,timestamp,timeformat,isEpoch,True)
                    #self.publishToolsData(self.allActivitiesData,self.activityRelationMetadata)
                    self.updateTrackingJson(self.tracking)
    def getAllEpics(self):
        self.epics = {}
        for project in self.all_projects:
            projectId = str(project.get('projectId'))
            epics = self.getResponse(self.baseEndPoint +  "/services/v5/projects/"+ projectId + "/epics" ,
                                     'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
            epic_list = []
            for epic in epics:
                epic_list.append(epic.get('label', {}).get('name', ''))
            self.epics[projectId] = epic_list
    def storyDataCollection(self):
        story_update_activity_category = ["story_update_activity", "comment_create_activity"]
        for project in self.all_projects:
            projectId = str(project.get('projectId'))
            dataCollection = True
            offset = 0
            startFrom = self.tracking.get('trackingInfo', {}).get(str(projectId), {}).get('lastUpdatedDate', self.startFrom)
            trackingDetails = self.tracking.get('trackingInfo', {}).get(str(projectId), {})
            lastUpdatedDate = 0
            while dataCollection:
                storyDetails = self.getResponse(self.baseEndPoint + "/services/v5/projects/"+ projectId +
                                "/stories?updated_after=" + startFrom + "&limit=200&offset=" + str(offset)
                                                 ,'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
                
                offset = offset + 200
                self.storyPublishData = []
                for story in storyDetails:
                    latest_update_time = int(time.mktime(time.strptime(story.get('updated_at'), self.timeStampFormat)))
                    if lastUpdatedDate < latest_update_time:
                        lastUpdatedDate = latest_update_time
                        trackingDetails['lastUpdatedDate'] = story.get('updated_at')
                    injectData = {}
                    epicName = [i['name'] for i in story.get('labels', [])]
                    injectData['epicName'] = epicName
                    #dateCheck = lambda lastUpdatedDate: story.get('updated_at', '')
                    #if self.startFrom < latest_update_time:
                        #story.get('updated_at', '')
                    #else :
                        #lastUpdatedDate=dateCheck(story.get('updated_at', ''))
                    self.storyPublishData += self.parseResponse(self.storyResponseTemplate, story, injectData)
                if len(storyDetails) == 0:
                    dataCollection = False
                self.tracking['trackingInfo'][str(projectId)] = trackingDetails
                self.publishToolsData(self.storyPublishData, self.storyMetadata)

                self.updateTrackingJson(self.tracking)
    def getIterationData(self):
        self.storyInBacklog = {}
        for project in self.all_projects:
            projectId = str(project.get('projectId'))
            backlogData = self.getResponse(self.baseEndPoint + "/services/v5/projects/" + projectId +
                                           '/iterations?scope=backlog',
                                            'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
            if len(backlogData) > 0 and len(backlogData[0]['stories']) > 0:
                stories = []
                for story in backlogData[0]['stories']:
                    stories.append(story.get('id'))
            self.storyInBacklog[projectId] = stories
            lastIteration = self.tracking.get('trackingInfo', {}).get(projectId, {}).get('iteration', 0)
            iterationContinue = True
            self.iteration_data = []
            while iterationContinue:
                iterations = self.getResponse(self.baseEndPoint + '/services/v5/projects/' + \
                                (projectId) + '/iterations?limit=20'+'&offset='+str(lastIteration) + \
                                '&fields=number%2C' \
                                'project_id%2Clength%2Cteam_strength%2Cstories%2C' \
                                'start%2Cfinish%2Ckind%2Cvelocity%2Canalytics',
                                     'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
               
                lastIteration = lastIteration + 20
                for iteration in iterations:
                    current_iteration_number = iteration.get('number', '') or 0
                    if 'stories' in iteration:
                        for story in iteration.get('stories', []):
                            injectDataIteration = {}
                            injectDataIteration['key'] = story.get('id', '')
                            if story.get('id') in self.storyInBacklog[projectId]:
                                injectDataIteration['backLog'] = True
                            self.iteration_data += self.parseResponse(self.iterationResponseTemplate, iteration, injectDataIteration)
                if len(iterations) !=0:
                    trackingDetails = self.tracking.get('trackingInfo', {}).get(str(projectId), {})
                    trackingDetails['iteration'] = current_iteration_number
                    self.tracking['trackingInfo'][projectId] = trackingDetails
                else :
                    iterationContinue = False
            iterationinsighstTimeX = self.config.get('dynamicTemplate', {}).get('iteration',{}).get('insightsTimeXFieldMapping',None)
            timestamp = iterationinsighstTimeX.get('timefield',None)
            timeformat = iterationinsighstTimeX.get('timeformat',None)
            isEpoch = iterationinsighstTimeX.get('isEpoch',False)
            self.publishToolsData(self.iteration_data,self.relationMetadata,timestamp,timeformat,isEpoch,True)
            self.updateTrackingJson(self.tracking)

    def getProjectList(self):
        trackingDetails = self.tracking
        trackingData = {}
       
        allWorkspaces = self.getResponse(self.baseEndPoint + "/services/v5/my/workspaces" ,
                                            'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
        for workspace in allWorkspaces:
            for project_id in workspace.get('project_ids', []):
                tempDict = {}
                tempDict['workspaceName'] = workspace.get('name')
                tempDict['projectId'] = project_id
                self.all_projects.append(tempDict)
                if project_id:
                    trackingData[project_id] = {}
        all_projects = self.getResponse(self.baseEndPoint + "/services/v5/projects" ,
                                            'GET', self.userid, self.password, None, reqHeaders=self.reqHeaders)
       
        for project in all_projects:
            tempDict = {}
            tempDict['workspaceName'] = None
            tempDict['projectId'] = project.get('id', None)
            project_id = tempDict['projectId']
            tempDict['projectName'] = project.get('name', None)

            if project_id:
                if not trackingDetails.get('trackingInfo',None):
                    trackingData[str(project_id)] = {}
                    trackingDetails['trackingInfo'] = trackingData
                    self.updateTrackingJson(trackingDetails)
            self.all_projects.append(tempDict)

    def setUpVariables(self):
        self.userid = self.config.get('userid', '')
        self.password = self.config.get('passwd', '')
        accessToken = self.config.get('token')
        self.baseEndPoint = self.config.get('baseEndPoint', '')
        self.reqHeaders = {'x-trackertoken': accessToken}
        self.timeStampFormat = self.config.get('timeStampFormat')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        self.startFrom = startFrom.strftime(self.timeStampFormat)

        self.all_projects = []
        self.iterationResponseTemplate = self.config.get('dynamicTemplate', {}).get('iterationResponseTemplate', {})
        self.relationMetadata = self.config.get('dynamicTemplate', {}).get('relationMetadata', None)
        self.storyMetadata = self.config.get('dynamicTemplate', {}).get('storyMetadata', None)
        self.storyResponseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)
        self.activityResponseTemplate = self.config.get('dynamicTemplate', {}).get('activityResponseTemplate', None)
        #self.activityRelationMetadata = self.config.get('dynamicTemplate', {}).get('activityRelationMetadata', None)
        self.activityMetadata = self.config.get('dynamicTemplate', {}).get('activityResponseTemplate', {}).get('ActivityMetadata', None)
    def login(self):
        userid = self.getCredential("userid")
        password = self.getCredential("passwd")
        accessToken = self.getCredential("accesstoken")
        baseEndPoint = self.config.get('baseEndPoint', '')
        reqHeaders = {'x-trackertoken': accessToken}
        trackingDetails = self.tracking

        loginResponse = self.getResponse(baseEndPoint + "/services/v5/me" ,
                                         'GET', userid, password, None, reqHeaders=reqHeaders)
        if loginResponse:
            currentState = str(time.time()) + " - Logged in successfully"
        else:
            currentState = str(time.time()) + " - Unable to login using config credentials."
        trackingDetails["toolInfo"] = currentState
        self.updateTrackingJson(trackingDetails)
if __name__ == "__main__":
    PivotalTrackerAgent()
