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
import copy

from dateutil import parser

from ....core.BaseAgent import BaseAgent


class JiraAgent(BaseAgent):
        
    def process(self):
        self.userid = self.config.get("userid", '')
        self.passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        startFrom = self.config.get("startFrom", '')
        lastUpdated = self.tracking.get("lastupdated", startFrom)
        responseTemplate = self.getResponseTemplate()
        fields = self.extractFields(responseTemplate)
        jiraIssuesUrl = baseUrl+"?jql=updated>='"+lastUpdated+"' ORDER BY updated ASC&maxResults="+str(self.config.get("dataFetchCount", 1000))+'&fields='+fields
        changeLog = self.config.get('changeLog', None)
        if changeLog:
            jiraIssuesUrl = jiraIssuesUrl + '&expand=changelog'
            changeLogFields = changeLog['fields']
            changeLogMetadata = changeLog['metadata']
            changeLogResponseTemplate = changeLog['responseTemplate']
            startFromDate = parser.parse(startFrom)
        total = 1
        maxResults = 0
        startAt = 0
        updatetimestamp = None
        sprintField = self.config.get("sprintField", None)
        while (startAt + maxResults) < total:
            data = []
            workLogData = []
            #jiraIssuesUrl = self.buildJiraRestUrl(baseUrl, startFrom, fields) + '&startAt='+str(startAt + maxResults)
            response = self.getResponse(jiraIssuesUrl+'&startAt='+str(startAt + maxResults), 'GET', self.userid, self.passwd, None)
            jiraIssues = response["issues"]
            for issue in jiraIssues:
                parsedIssue = self.parseResponse(responseTemplate, issue)
                if sprintField:
                    self.processSprintInformation(parsedIssue, issue, sprintField, self.tracking)
                data += parsedIssue
                if changeLog:
                    workLogData += self.processChangeLog(issue, changeLogFields, changeLogResponseTemplate, startFromDate)
            maxResults = response['maxResults']
            total = response['total']
            startAt = response['startAt']
            if len(jiraIssues) > 0:
                updatetimestamp = jiraIssues[len(jiraIssues) - 1]["fields"]["updated"]
                dt = parser.parse(updatetimestamp)
                fromDateTime = dt + datetime.timedelta(minutes=01)
                fromDateTime = fromDateTime.strftime('%Y-%m-%d %H:%M')
                self.tracking["lastupdated"] = fromDateTime
                jiraKeyMetadata = {"dataUpdateSupported" : True,"uniqueKey" : ["key"]}
                self.publishToolsData(data, jiraKeyMetadata)
                #self.publishToolsData(data)
                if len(workLogData) > 0:
                    self.publishToolsData(workLogData, changeLogMetadata)
                self.updateTrackingJson(self.tracking)
            else:
                break
    
    def buildJiraRestUrl(self, baseUrl, startFrom, fields):
        lastUpdatedDate = self.tracking.get("lastupdated", startFrom)
        endDate = parser.parse(lastUpdatedDate) + datetime.timedelta(hours=24)
        endDate = endDate.strftime('%Y-%m-%d %H:%M')
        jiraIssuesUrl = baseUrl+"?jql=updated>='"+lastUpdatedDate+"' AND updated<'"+endDate+"' ORDER BY updated ASC&maxResults="+str(self.config.get("dataFetchCount", 1000))+'&fields='+fields
        changeLog = self.config.get('changeLog', None)
        if changeLog:
            jiraIssuesUrl = jiraIssuesUrl + '&expand=changelog'
        return jiraIssuesUrl
    
    def processChangeLog(self, issue, workLogFields, responseTemplate, startFromDate):
        changeLog = issue.get('changelog', None)
        workLogData = []
        injectData = {'issueKey' : issue['key'] }
        if changeLog:
            histories = changeLog.get('histories', [])
            for change in histories:
                data = self.parseResponse(responseTemplate, change, injectData)[0]
                changeDate = parser.parse(data['changeDate'].split('.')[0]);
                if changeDate > startFromDate:
                    items = change['items']
                    for item in items:
                        if item['field'] in workLogFields:
                            dataCopy = copy.deepcopy(data)
                            dataCopy['changedfield'] = item['field']
                            dataCopy['fromString'] = item['fromString']
                            dataCopy['toString'] = item['toString']
                            dataCopy['from'] = item['from']
                            dataCopy['to'] = item['to']
                            workLogData.append(dataCopy)
        return workLogData
    
    def scheduleExtensions(self):
        extensions = self.config.get('dynamicTemplate', {}).get('extensions', None)
        if extensions:
            #backlog = extensions.get('backlog', None)
            #if backlog:
            #    self.registerExtension('backlog', self.retrieveBacklogDetails, backlog.get('runSchedule'))
            sprints = extensions.get('sprints', None)
            if sprints:
                self.registerExtension('sprints', self.retrieveSprintDetails, sprints.get('runSchedule'))
            sprintReport = extensions.get('sprintReport', None)
            if sprintReport:
                self.registerExtension('sprintReport', self.retrieveSprintReports, sprintReport.get('runSchedule'))
            releaseDetails = extensions.get('releaseDetails', None)
            if releaseDetails:
                self.registerExtension('releaseDetails', self.retrieveReleaseDetails, releaseDetails.get('runSchedule'))
    
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
                        if len(propertyKeyValToken) > 1:
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
                #if len(boards) > 1 :
                #    for board in boards:
                #        boardTracking = boardsTracking.get(board)
                #        sprintTracking = boardTracking.get('sprints')
                #        for sprint in sprints:
                #            if sprintTracking.get(sprint, None) is None:
                #                sprintTracking[sprint] = {}
     
    def retrieveSprintDetails(self):
        sprintDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprints', None)
        boardApiUrl = sprintDetails.get('boardApiUrl')
        boards = self.tracking.get('boards', None)
        if sprintDetails and boards:
            responseTemplate = sprintDetails.get('sprintResponseTemplate', None)
            sprintMetadata = sprintDetails.get('sprintMetadata')
            for boardId in boards:
                data = []
                board = boards[boardId]
                boardRestUrl = boardApiUrl + '/' + str(boardId)
                try:
                    boardResponse = self.getResponse(boardRestUrl, 'GET', self.userid, self.passwd, None)
                    board['name'] = boardResponse.get('name')
                    board['type'] = boardResponse.get('type')
                    board.pop('error', None)
                except Exception as ex:
                    board['error'] = str(ex)
                    #Get the individual sprint details.
                    sprints = board.get('sprints')
                    for sprint in sprints:
                        sprintApiUrl = sprintDetails.get('sprintApiUrl')+'/'+sprint
                        try:
                            sprintResponse = self.getResponse(sprintApiUrl, 'GET', self.userid, self.passwd, None)
                            data.append(self.parseResponse(responseTemplate, sprintResponse)[0])
                        except Exception:
                            pass;
                    if len(data) > 0 : 
                        self.publishToolsData(data, sprintMetadata)
                    continue
                sprintsUrl = boardRestUrl + '/sprint?startAt='
                startAt = 0
                isLast = False
                injectData = {'boardName' : board['name']}
                while not isLast:
                    try:
                        sprintsResponse = self.getResponse(sprintsUrl+str(startAt), 'GET', self.userid, self.passwd, None)
                    except Exception as ex3:
                        #board['error'] = str(ex3)
                        break
                    isLast = sprintsResponse['isLast']
                    startAt = startAt + sprintsResponse['maxResults']
                    sprintValues = sprintsResponse['values']
                    parsedSprints = self.parseResponse(responseTemplate, sprintValues, injectData)
                    for parsedSprint in parsedSprints:
                        if str(parsedSprint.get('boardId')) == str(boardId):
                            data.append(parsedSprint)
                if len(data) > 0 : 
                    self.publishToolsData(data, sprintMetadata)
                    
    def retrieveBacklogDetails(self):
        backlogDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('backlog', None)
        boardApiUrl = backlogDetails.get('boardApiUrl')
        boards = self.tracking.get('boards', None)
        backlogMetadata = backlogDetails.get('backlogMetadata')
        if backlogDetails and boards:
            for boardId in boards:
                data = []
                board = boards[boardId]
                boardRestUrl = boardApiUrl + '/' + str(boardId)
                try:
                    boardResponse = self.getResponse(boardRestUrl, 'GET', self.userid, self.passwd, None)
                    board['name'] = boardResponse.get('name')
                    board['type'] = boardResponse.get('type')
                    board.pop('error', None)
                    backlogUrl = boardRestUrl + '/backlog?fields=[]&startAt='
                    startAt = 0
                    isLast = False
                    while not isLast:
                        backlogResponse = self.getResponse(backlogUrl+str(startAt), 'GET', self.userid, self.passwd, None)
                        isLast = (startAt + backlogResponse['maxResults']) > backlogResponse['total']
                        startAt = startAt + backlogResponse['maxResults']
                        backlogIssues = backlogResponse['issues']
                        for backlogIssue in backlogIssues:
                            issue = {}
                            issue['backlogIssueKey'] = backlogIssue.get('key')
                            issue['projectKey'] = backlogIssue.get('key').split('-')[0]
                            issue['boardName'] = board['name']
                            issue['boardId'] = boardId
                            data.append(issue)
                    if len(data) > 0 : 
                        self.publishToolsData(data, backlogMetadata)
                except Exception as ex:
                    board['error'] = str(ex)
                    #Get the individual sprint details.
    
    def retrieveSprintReports(self):
        sprintDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprintReport', None)
        boardApiUrl = sprintDetails.get('boardApiUrl')
        boards = self.tracking.get('boards', None)
        if sprintDetails and boards:
            sprintReportUrl = sprintDetails.get('sprintReportUrl', None)
            responseTemplate = sprintDetails.get('sprintReportResponseTemplate', None)
            #sprintMetadata = sprintDetails.get('sprintMetadata')
            relationMetadata = sprintDetails.get('relationMetadata')
            for boardId in boards:
                board = boards[boardId]
                boardName = board.get('name', None)
                if boardName is None:
                    boardRestUrl = boardApiUrl + '/' + str(boardId)
                    try:
                        boardResponse = self.getResponse(boardRestUrl, 'GET', self.userid, self.passwd, None)
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
                    #extract the project key from the sprint reports to allow the data tagging
                    sprintClosed = sprint.get('closed', False)
                    if not sprintClosed:
                        sprintReportRestUrl = sprintReportUrl + '?rapidViewId='+str(boardId)+'&sprintId='+str(sprintId)
                        sprintReportResponse = None
                        try:
                            sprintReportResponse = self.getResponse(sprintReportRestUrl, 'GET', self.userid, self.passwd, None)
                        except Exception as ex:
                            sprint['error'] = str(ex)
                        if sprintReportResponse:
                            content = sprintReportResponse.get('contents', None)
                            if sprintReportResponse.get('sprint', {}).get('state', 'OPEN') == 'CLOSED':
                                sprint['closed'] = True
                            injectData = { 'boardId' : int(boardId), 'sprintId' : int(sprintId) }
                            data = []
                            data += self.addSprintDetails(responseTemplate, content, 'completedIssues', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'issuesNotCompletedInCurrentSprint', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'puntedIssues', injectData)
                            data += self.addSprintDetails(responseTemplate, content, 'issuesCompletedInAnotherSprint', injectData)
                            if len(data) > 0:
                                #self.publishToolsData(self.getSprintInformation(sprintReportResponse, boardId, sprintId, board['name'], board['type']), sprintMetadata)
                                self.publishToolsData(data, relationMetadata)
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
                issueKey = issue['key']
                issue['addedDuringSprint'] = issueKeysAddedDuringSprint.get(issueKey, False)
                issue['sprintIssueRegion'] = sprintIssueRegion
                issue['projectKey'] = issueKey.split('-')[0]
        return parsedIssues
     
    def retrieveReleaseDetails(self):
        releaseDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('releaseDetails', None)
        if releaseDetails:
            jiraProjectApiUrl = releaseDetails.get('jiraProjectApiUrl', None)
            jiraProjectResponseTemplate = releaseDetails.get('jiraProjectResponseTemplate', None)
            jiraReleaseResponseTemplate = releaseDetails.get('jiraReleaseResponseTemplate', None)
            releaseVersionsMetadata = releaseDetails.get('releaseVersionsMetadata')
            if jiraProjectApiUrl and jiraProjectResponseTemplate and jiraReleaseResponseTemplate:
                jiraProjects = self.getResponse(jiraProjectApiUrl, 'GET', self.userid, self.passwd, None)
                parsedJiraProjects = self.parseResponse(jiraProjectResponseTemplate, jiraProjects)
                for parsedJiraProject in parsedJiraProjects:
                    projectKey = parsedJiraProject['projectKey']
                    releaseApiUrl = jiraProjectApiUrl + '/' + projectKey + '/versions'
                    releaseVersionsResponse = self.getResponse(releaseApiUrl, 'GET', self.userid, self.passwd, None)
                    parsedReleaseVersions = self.parseResponse(jiraReleaseResponseTemplate, releaseVersionsResponse)
                    self.publishToolsData(parsedReleaseVersions, releaseVersionsMetadata)
                    
if __name__ == "__main__":
    JiraAgent()        
