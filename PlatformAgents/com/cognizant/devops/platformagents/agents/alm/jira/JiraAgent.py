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
Created on Jun 22, 2016

@author: 463188
'''
from datetime import datetime as dateTime2
import datetime
from dateutil import parser
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class JiraAgent(BaseAgent):
    relationMetadata = {
        'labels' : ['LATEST'],
        'relation' : {
                'properties' : ['addedDuringSprint', 'sprintIssueRegion', 'committedEstimate'],
                'name' : 'HAS_ISSUES',
                'source' : {
                        'constraints' : ["sprintId", "boardId"]
                    },
                'destination' : {
                        'constraints' : ["key"]
                    },
            }
    }
    sprintMetadata = {
        'labels' : ['SPRINT'],
        'dataUpdateSupported' : True,
        'uniqueKey' : 'boardId,sprintId'
    }
    releaseVersionsMetadata = {
        'labels' : ['RELEASE'],
        'dataUpdateSupported' : True,
        'uniqueKey' : 'projectKey,projectId'
    }
        
    def process(self):
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        startFrom = self.config.get("startFrom", '')
        lastUpdated = self.tracking.get("lastupdated", startFrom)
        responseTemplate = self.getResponseTemplate()
        fields = self.extractFields(responseTemplate)
        jiraIssuesUrl = baseUrl+"?jql=updated>='"+lastUpdated+"' ORDER BY updated ASC&maxResults="+str(self.config.get("dataFetchCount", 1000))+'&fields='+fields
        total = 1
        maxResults = 0
        startAt = 0
        updatetimestamp = None
        sprintField = self.config.get("sprintField", None)
        while (startAt + maxResults) < total:
            data = []
            response = self.getResponse(jiraIssuesUrl+'&startAt='+str(startAt + maxResults), 'GET', userid, passwd, None)
            jiraIssues = response["issues"]
            for issue in jiraIssues:
                parsedIssue = self.parseResponse(responseTemplate, issue)
                if sprintField:
                    self.processSprintInformation(parsedIssue, issue, sprintField, self.tracking)
                data += parsedIssue
            maxResults = response['maxResults']
            total = response['total']
            startAt = response['startAt']
            if len(jiraIssues) > 0:
                updatetimestamp = jiraIssues[len(jiraIssues) - 1]["fields"]["updated"]
                dt = parser.parse(updatetimestamp)
                fromDateTime = dt + datetime.timedelta(minutes=01)
                fromDateTime = fromDateTime.strftime('%Y-%m-%d %H:%M')
                self.tracking["lastupdated"] = fromDateTime
                self.publishToolsData(data)
                self.updateTrackingJson(self.tracking)
            else:
                break
        if self.config.get('enableSprintReport', False):
            self.retrieveSprintReports(userid, passwd, self.tracking)            
    
    def extractFields(self, responseTemplate):
        fieldsJson = responseTemplate.get("fields", None)
        fieldsParam = ''
        if fieldsJson:
            for field in fieldsJson:
                fieldsParam += field + ','
        fieldsParam = fieldsParam[:-1]
        if self.config.get("sprintField", None):
            fieldsParam += ','+ self.config.get("sprintField")
        return fieldsParam

    def processSprintInformation(self, parsedIssue, issue, sprintField, tracking):
        if sprintField:
            boardsTracking = tracking.get('boards', None)
            if boardsTracking is None:
                boardsTracking = {}
                tracking['boards'] = boardsTracking
            sprintDetails = issue.get("fields", {}).get(sprintField, None)
            if sprintDetails:
                sprints = []
                boards = []
                for sprint in sprintDetails:
                    sprintData = {}
                    sprintDetail = sprint.split("[")[1][:-1]
                    sprintPropertieTokens = sprintDetail.split(",")
                    for propertyToken in sprintPropertieTokens:
                        propertyKeyValToken = propertyToken.split("=")
                        sprintData[propertyKeyValToken[0]] = propertyKeyValToken[1]
                    boardId = sprintData.get('rapidViewId')
                    sprintId = sprintData.get('id')
                    boardTracking = boardsTracking.get(boardId, None)
                    if boardTracking is None:
                        boardTracking = {}
                        boardsTracking[boardId] = boardTracking
                    sprintTracking = boardTracking.get('sprints', None)
                    if sprintTracking is None:
                        sprintTracking = {}
                        boardTracking['sprints'] = sprintTracking
                    if sprintTracking.get(sprintId, None) is None:
                        sprintTracking[sprintId] = {}
                    if boardId not in boards:
                        boards.append(boardId)
                    if sprintId not in sprints:
                        sprints.append(sprintId)
                parsedIssue[0]['sprints'] = sprints
                parsedIssue[0]['boards'] = boards
     
    def retrieveSprintReports(self, userId, password, tracking):
        sprintDetails = self.config.get('sprintDetails', None)
        boardApiUrl = sprintDetails.get('boardApiUrl')
        boards = tracking.get('boards', None)
        if sprintDetails and boards:
            currentDateTime = dateTime2.now()
            sprintReportUrl = sprintDetails.get('sprintReportUrl', None)
            responseTemplate = sprintDetails.get('sprintReportResponseTemplate', None)
            sprintRetrivalSchedule = self.config.get('sprintRetrivalSchedule', 1440) * 60
            for boardId in boards:
                board = boards[boardId]
                boardName = board.get('name', None)
                if boardName is None:
                    boardRestUrl = boardApiUrl + '/' + str(boardId)
                    try:
                        boardResponse = self.getResponse(boardRestUrl, 'GET', userId, password, None)
                        board['name'] = boardResponse.get('name')
                        board['type'] = boardResponse.get('type')
                        board.pop('error', None)
                    except Exception as ex:
                        board['error'] = str(ex)
                        continue
                sprints = board['sprints']
                for sprintId in sprints:
                    sprint = sprints[sprintId]
                    #For velocity, only the completed sprints are considered
                    lastFetch = sprint.get('lastFetch', None)
                    if lastFetch:
                        lastFetch = parser.parse(lastFetch)
                    sprintClosed = sprint.get('closed', False)
                    if not sprintClosed and (lastFetch is None or (currentDateTime - lastFetch).total_seconds() >= sprintRetrivalSchedule):
                        sprintReportRestUrl = sprintReportUrl + '?rapidViewId='+str(boardId)+'&sprintId='+str(sprintId)
                        try:
                            sprintReportResponse = self.getResponse(sprintReportRestUrl, 'GET', userId, password, None)
                        except Exception as ex:
                            sprint['error'] = str(ex)
                        if sprintReportResponse:
                            content = sprintReportResponse.get('contents', None)
                            sprint['lastFetch'] = currentDateTime.strftime('%Y-%m-%d %H:%M')
                            if sprintReportResponse.get('sprint', {}).get('state', 'OPEN') == 'CLOSED':
                                sprint['closed'] = True
                            injectData = { 'boardId' : boardId, 'sprintId' : sprintId }
                            data = []
                            data += self.addSprintDetails(responseTemplate, content, 'completedIssues', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'issuesNotCompletedInCurrentSprint', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'puntedIssues', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'issuesCompletedInAnotherSprint', injectData)
                            if len(data) > 0:
                                self.publishToolsData(self.getSprintInformation(sprintReportResponse, boardId, sprintId, board['name'], board['type']), self.sprintMetadata)
                                self.publishToolsData(data, self.relationMetadata)
                                self.updateTrackingJson(self.tracking)
    
    def getSprintInformation(self, content, boardId, sprintId, boardName, boardType):
        data = []
        sprint = content.get('sprint')
        sprint.pop('linkedPagesCount', None)
        sprint.pop('remoteLinks', None)
        sprint.pop('sequence', None)
        sprint.pop('id', None)
        sprint['boardId'] = boardId
        sprint['sprintId'] = sprintId
        sprint['boardName'] = boardName
        sprint['boardType'] = boardType
        sprint['sprintName'] = sprint.get('name')
        sprint.pop('name', None)
        timeStampFormat = '%d/%b/%y'
        startDate = sprint.get('startDate', None)
        if startDate and startDate != 'None':
            sprint['startDateEpoch'] = self.getRemoteDateTime(dateTime2.strptime(startDate.split(' ')[0], timeStampFormat)).get('epochTime')
        endDate = sprint.get('endDate', None)
        if endDate and endDate != 'None':
            sprint['endDateEpoch'] = self.getRemoteDateTime(dateTime2.strptime(endDate.split(' ')[0], timeStampFormat)).get('epochTime')
        completeDate = sprint.get('completeDate', None)
        if completeDate and completeDate != 'None':
            sprint['completeDateEpoch'] = self.getRemoteDateTime(dateTime2.strptime(completeDate.split(' ')[0], timeStampFormat)).get('epochTime')
        data.append(sprint)
        return data
        
    def addSprintDetails(self, responseTemplate, content, sprintIssueRegion, injectData):
        issueKeysAddedDuringSprint = content.get('issueKeysAddedDuringSprint', {})
        issues = content.get(sprintIssueRegion, None)
        parsedIssues = []
        if issues:
            parsedIssues = self.parseResponse(responseTemplate, issues, injectData)
            for issue in parsedIssues:
                issue['addedDuringSprint'] = issueKeysAddedDuringSprint.get(issue['key'], False)
                issue['sprintIssueRegion'] = sprintIssueRegion
        return parsedIssues
     
    def retrieveReleaseDetails(self, userId, password, tracking):
        releaseDetails = self.config.get('releaseDetails', {})
        jiraProjectApiUrl = releaseDetails.get('jiraProjectApiUrl', None)
        jiraProjectResponseTemplate = releaseDetails.get('jiraProjectResponseTemplate', None)
        jiraReleaseResponseTemplate = releaseDetails.get('jiraReleaseResponseTemplate', None)
        if jiraProjectApiUrl and jiraProjectResponseTemplate and jiraReleaseResponseTemplate:
            jiraProjects = self.getResponse(jiraProjectApiUrl, 'GET', userId, password, None)
            parsedJiraProjects = self.parseResponse(jiraProjectResponseTemplate, jiraProjects)
            for parsedJiraProject in parsedJiraProjects:
                projectKey = parsedJiraProject['projectKey']
                releaseApiUrl = jiraProjectApiUrl + '/' + projectKey + '/versions'
                releaseVersionsResponse = self.getResponse(releaseApiUrl, 'GET', userId, password, None)
                parsedReleaseVersions = self.parseResponse(jiraReleaseResponseTemplate, releaseVersionsResponse)
                self.publishToolsData(parsedReleaseVersions, self.releaseVersionsMetadata)
                #Add tracking
                #Update the board tracking to include high level of last fetch logic
                #extract the project key from the sprint reports
                #update engine to include array of uniqueKey
                #Update the rest communication facade. Remove option from communication. Add this options at config level (high level)
                    
if __name__ == "__main__":
    JiraAgent()        
