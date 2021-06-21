#-------------------------------------------------------------------------------
# Copyright 2021 Cognizant Technology Solutions
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
Created on June 08, 2021

@author: 828567
'''
from datetime import datetime as dateTime
from dateutil import parser
import datetime 
import copy
import re 
import os
import hashlib
import uuid
from ....core.BaseAgent3 import BaseAgent
import json


class JiraWebhookAgent(BaseAgent):
    
    @BaseAgent.timed
    def processWebhook(self,data):
        self.baseLogger.info(" Inside JiraWebhookAgent Process ======")
        dataReceived = json.loads(data)
        self.webhookEvent = dataReceived.get("webhookEvent", None) 
        dynamicTemplate = self.config.get("dynamicTemplate", {})
        fetchProjectMetadata = dynamicTemplate.get("fetchProjectMetadata", False)
        metadataTrackingDetails = self.tracking.get("projectMetadataUpdated", False) 
        
        # project and board details would be fetched and updated in tracking 
        # it's a one time call for metadata updation 
        if not metadataTrackingDetails and fetchProjectMetadata:
            userid=self.getCredential("userid")
            passwd=self.getCredential("passwd")
            projectTrackingDetails = self.tracking.get("projects", None)
            if projectTrackingDetails is None:
                projectTrackingDetails = {}
                self.tracking["projects"] = projectTrackingDetails
            jiraProjectApiUrl = dynamicTemplate.get("extensions", {}).get("releaseDetails",{}).get('jiraProjectApiUrl', None)
            projectResponseTemplate = dynamicTemplate.get("extensions", {}).get("projectDetails",{}).get("jiraProjectResponseTemplate",{})
            jiraProjects = self.getResponse(jiraProjectApiUrl, 'GET',  userid, passwd, None)
            parsedJiraProjects = self.parseResponse(projectResponseTemplate, jiraProjects)
            for eachProject in parsedJiraProjects:
                projectId = eachProject["projectId"]
                eachProject["projectId"] = int(eachProject["projectId"])
                projectTrackingDetails[projectId] =  eachProject.copy()
            metadata = dynamicTemplate.get("extensions", {}).get("projectDetails",{}).get("projectMetadata", {})
            self.publishToolsData(parsedJiraProjects, metadata)
            
            boardTrackingDetails = self.tracking.get("boards", None)
            if boardTrackingDetails is None:
                boardTrackingDetails = {}
                self.tracking["boards"] = boardTrackingDetails 
            boardApiUrl = self.config.get("dynamicTemplate",{}).get("extensions", {}).get("sprints", {}).get('boardApiUrl')
            boardResponseTemplate = dynamicTemplate.get("extensions", {}).get("boardResponseTemplate", {})
            jiraBoards = self.getResponse(boardApiUrl, 'GET', userid, passwd, None)
            boardsList = jiraBoards.get("values", list())
            parsedJiraBoards = self.parseResponse(boardResponseTemplate, boardsList)
            for eachBoard in parsedJiraBoards:
                boardId = eachBoard["boardId"]
                boardTrackingDetails[boardId] =  eachBoard
            self.tracking["projectMetadataUpdated"] = True   
            self.updateTrackingJson(self.tracking)
            
        if(self.webhookEvent and self.webhookEvent.split('_',2)[0] == "jira:issue"):
            self.processIssueDetails(dataReceived)
            if(self.webhookEvent.split('_',2)[1] == "updated"):
                self.processChangeLog(dataReceived)   
        if(self.webhookEvent and self.webhookEvent.split('_',2)[0] == "sprint"):
            self.processSprintDetails(dataReceived)
        if(self.webhookEvent and self.webhookEvent.split('_',2)[0] == "jira:version"):
            self.processReleaseDetails(dataReceived)
        if(self.webhookEvent and self.webhookEvent.split('_',2)[0] == "project"):
            self.processProjectDetails(dataReceived)
            
    def processIssueDetails(self, dataReceived):
        dynamicTemplate = self.config.get("dynamicTemplate",{})
        issueResponseTemplate = dynamicTemplate.get("responseTemplate", {})
        issueDetails = dataReceived.get("issue", {})
        parsedIssueData = self.parseResponse(issueResponseTemplate, issueDetails)
        inwardIssueMetaData = list()
        outwardIssuesMetaData = list()
        issueLinkList = issueDetails.get('fields',{}).get ('issuelinks', list())
        for issueLink in issueLinkList :
            if 'inwardIssue' in issueLink:
                linkType = issueLink.get ('type', {}).get('inward', '')
                key = issueLink.get ('inwardIssue', {}).get('key', '')
                inwardIssueMetaData.append (key+'__'+linkType)
            elif 'outwordIssue' in issueLink:
                linkType = issueLink.get ('type', {}).get('outward', '')
                key =    issueLink.get ('outwardIssue', {}).get('key', '')
                outwardIssuesMetaData.append (key+'__'+linkType)
        parsedIssueData[0]['inwardIssuesMetaData'] = inwardIssueMetaData
        parsedIssueData[0]['outwardIssuesMetaData'] = outwardIssuesMetaData
        if self.webhookEvent.split('_',2)[1] == "deleted":
            parsedIssueData[0]['isDeleted'] = True
        metadata = self.config.get("dynamicTemplate",{}).get("metaData", {})
        self.publishToolsData(parsedIssueData, metadata)
        
        # below code prepare data for SPRINT_HAS_ISSUES relation and publish it
        sprintField = self.config.get("sprintField", None)
        sprintDetails = issueDetails.get("fields",{}).get(sprintField, list())
        if len(sprintDetails):
            sprintResponseTemplate = dynamicTemplate.get("extensions", {}).get("sprintIssueRelation", {}).get("sprintResponseTemplate",{})
            relationMetadata = dynamicTemplate.get("extensions", {}).get("sprintIssueRelation",{}).get("relationMetadata", {})
            relationData = []
            injectData = {"key": parsedIssueData[0]["key"],
                          "projectKey": parsedIssueData[0]["projectKey"]}
            for eachSprint in sprintDetails:
              relationData += self.parseResponse(sprintResponseTemplate, eachSprint, injectData)   
            self.publishToolsData(relationData, relationMetadata)
            
    def processChangeLog(self, dataReceived):
        dynamicTemplate = self.config.get("dynamicTemplate",{})
        changelogTemplate = dynamicTemplate.get("changeLog", {}).get("changeLogItemTemplate", {})
        injectDataTemplate = dynamicTemplate.get("changeLog", {}).get("responseTemplate", {})
        insightsTimeXFieldMapping = dynamicTemplate.get('changeLog', {}).get('insightsTimeXFieldMapping',None)
        timeStampField=insightsTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insightsTimeXFieldMapping.get('timeformat',None)
        isEpoch=insightsTimeXFieldMapping.get('isEpoch',None); 
        metadata = dynamicTemplate.get('changeLog',None).get("metadata",{})
        injectData = self.parseResponse(injectDataTemplate, dataReceived)
        finalData = []
        changedItems = dataReceived.get("changelog", {}).get("items", [])
        for item in changedItems:
            parsedChangelogData = self.parseResponse(changelogTemplate, item, injectData[0])
            finalData += parsedChangelogData
        self.publishToolsData(finalData, metadata ,timeStampField,timeStampFormat,isEpoch,True)
        
        # below code prepare data for JIRA_ISSUE_HAS_CHANGELOG relation and publish it
        projectKey = dataReceived.get("issue", {}).get("fields", {}).get("project", {}).get("key", {})
        relationMetadata = dynamicTemplate.get("extensions", {}).get("IssueChangeLogRelation",{}).get("relationMetadata",{})
        relationInjectData = {"key": injectData[0]["issueKey"],
                              "projectKey": projectKey}
        for data in finalData:
            data.update(relationInjectData.copy())
        self.publishToolsData(finalData, relationMetadata)
                    
        
    def processSprintDetails(self, dataReceived):
        sprintResponseTemplate = self.config.get("dynamicTemplate",{}).get("extensions", {}).get("sprints", {}).get("sprintResponseTemplate",{})
        metadata = self.config.get("dynamicTemplate",{}).get("extensions", {}).get("sprints", {}).get("sprintMetadata",{})
        insightsTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprints', {}).get('insightsTimeXFieldMapping',None)
        timeStampField=insightsTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insightsTimeXFieldMapping.get('timeformat',None)
        isEpoch=insightsTimeXFieldMapping.get('isEpoch',None);
        sprintDeatils = dataReceived.get("sprint",{}) 
        injectData = {}
        if (self.webhookEvent.split('_',2)[1] == "deleted"):
            injectData = {'event' : 'sprintDeleted'}
               
        boardId = dataReceived.get("sprint",{}).get("originBoardId", None)
        boardTrackingDetails = self.tracking.get("boards", None)
        if boardTrackingDetails and boardId in boardTrackingDetails:
            boardDetails = boardTrackingDetails[boardId]   
        parsedSprintData = self.parseResponse(sprintResponseTemplate, sprintDeatils, boardDetails)
        self.publishToolsData(parsedSprintData, metadata,timeStampField,timeStampFormat,isEpoch,True)
        
    def processReleaseDetails(self, dataReceived):
        dynamicTemplate = self.config.get("dynamicTemplate",{})
        responseTemplate = dynamicTemplate.get("extensions", {}).get("releaseDetails",{}).get("jiraReleaseResponseTemplate",{})
        insightsTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('releaseDetails', {}).get('insightsTimeXFieldMapping',None)
        timeStampField=insightsTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insightsTimeXFieldMapping.get('timeformat',None)
        isEpoch=insightsTimeXFieldMapping.get('isEpoch',None); 
        metaData = dynamicTemplate.get("extensions", {}).get("releaseDetails",{}).get("releaseVersionsMetadata",{})
        versionDetails = dataReceived.get("version",{})
        projectDetails = {}
        projectId = dataReceived.get("version",{}).get("projectId", {})
        projectTrackingDetails = self.tracking.get("projects", None)
        if projectTrackingDetails and projectId in projectTrackingDetails:
            projectDetails = projectTrackingDetails[projectId]
            
        parsedReleaseData = self.parseResponse(responseTemplate, versionDetails, projectDetails)
        self.publishToolsData(parsedReleaseData, metaData,timeStampField,timeStampFormat,isEpoch,True)
        
    def processProjectDetails(self, dataReceived):
        dynamicTemplate = self.config.get("dynamicTemplate",{})
        responseTemplate = dynamicTemplate.get("extensions", {}).get("projectDetails",{}).get("jiraProjectResponseTemplate",{})
        metadata = dynamicTemplate.get("extensions", {}).get("projectDetails",{}).get("projectMetadata", {})
        projectDetails = dataReceived.get( "project",{})
        parsedProjectData = self.parseResponse(responseTemplate, projectDetails) 
        projectTrackingDetails = self.tracking.get("projects", None)
        projectId = dataReceived.get( "project",{}).get("id", {})
        if projectTrackingDetails is None:
            projectTrackingDetails = {}
            self.tracking["projects"] = projectTrackingDetails 
        projectTrackingDetails[str(projectId)] = parsedProjectData[0].copy()
        parsedProjectData[0]["updatedAt"] = dataReceived.get( "timestamp", None)
        self.publishToolsData(parsedProjectData, metadata,"updatedAt",True,True)
        self.updateTrackingJson(self.tracking)
             
        
if __name__ == "__main__":
    JiraWebhookAgent()           