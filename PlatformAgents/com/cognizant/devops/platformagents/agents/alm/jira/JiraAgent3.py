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
import re 
import logging.handlers
from dateutil import parser

#from com.cognizant.devops.platformagents.core.BaseAgent3 import BaseAgent
from ....core.BaseAgent3 import BaseAgent

class JiraAgent(BaseAgent):
    changedFields = set()

    def process(self):
         self.userid=self.config.get("userid",'')
         self.passwd=self.config.get("passwd",'')
         baseUrl=self.config.get("baseUrl",'')
         startFrom = self.config.get("startFrom",'')
         lastUpdated = self.tracking.get("lastupdated",startFrom)
         currentDate = dateTime2.combine(dateTime2.now().date(), dateTime2.min.time())
         responseTemplate=self.getResponseTemplate()
         fields = self.extractFields(responseTemplate)
         jiraIssuesUrl = baseUrl+"?jql=updated>='"+lastUpdated+"' ORDER BY updated ASC&maxResults="+str(self.config.get("dataFetchCount", 1000))+'&fields='+fields
         enableIssueModificationTimeline = self.config.get('enableIssueModificationTimeline',False)
         enableReSyncTrigger = self.config.get('enableReSyncTrigger', False)
         bypassSprintExtCall = self.config.get('bypassSprintExtCall', False)
         issueModificationTimelineCaptureDate=self.tracking.get("issueModificationTimelineCaptureDate", lastUpdated).split(" ")[0]
         issueModificationTimelineCaptureDate = parser.parse(issueModificationTimelineCaptureDate)
         issueStatusFilter = self.config.get('dynamicTemplate',dict()).get('issueStatusFilter',list())
         changeLog = self.config.get('changeLog',None)
         if changeLog:
             jiraIssuesUrl = jiraIssuesUrl+'&expand=changelog'
             changeLogFields = changeLog['fields']
             changeLogMetadata = changeLog['metadata']
             changeLogResponseTemplate= changeLog['responseTemplate']
             startFromDate = parser.parse(startFrom)
         total = 1
         maxResults = 0
         startAt = 0
         updatetimestamp = None
         sprintField = self.config.get('sprintField',None)
         fieldsList =  list()
         self.propertyExtractor(responseTemplate, fieldsList)
         while (startAt + maxResults) < total:
             data =[]
             workLogData = []
             issueModificationTimeline = []
             #jiraIssuesUrl = self.buildJiraRestUrl(baseUrl, startFrom, fields) + '&startAt='+str(startAt + maxResults)
             response = self.getResponse(jiraIssuesUrl+'&startAt='+str(startAt + maxResults), 'GET', self.userid, self.passwd, None)
             jiraIssues = response["issues"]
             for issue in jiraIssues:
                 parsedIssue = self.parseResponse(responseTemplate, issue)
                 issueStatus = issue.get('fields', dict()).get ('status',dict()).get('name','')
                 isIssueStatusFilter = str(issueStatus) not in issueStatusFilter
                 parsedIssue[0]['processed'] = False
                 inwardIssueMetaData = list()
                 outwardIssuesMetaData = list()
                 issueLinkList = issue.get('fields',{}).get ('issuelinks', list())
                 for issueLink in issueLinkList :
                     if 'inwardIssue' in issueLink:
                         linkType = issueLink.get ('type', {}).get('inward', '')
                         key = issueLink.get ('inwardIssue', {}).get('key', '')
                         inwardIssueMetaData.append (key+'__'+linkType)
                     elif 'outwordIssue' in issueLink:
                         linkType = issueLink.get ('type', {}).get('outward', '')
                         key =    issueLink.get ('outwardIssue', {}).get('key', '')
                         outwardIssuesMetaData.append (key+'__'+linkType)
                 parsedIssue[0]['inwardIssuesMetaData'] = inwardIssueMetaData
                 parsedIssue[0]['outwardIssuesMetaData'] = outwardIssuesMetaData
                 if sprintField:
                     self .processSprintInformation(parsedIssue, issue, isIssueStatusFilter, sprintField,self.tracking)
                 for field in fieldsList:
                     if field not in parsedIssue[0]:
                         parsedIssue[0][field] = None
                     data += parsedIssue
                     if changeLog:
                         workLogData += self.processChangeLog(issue, changeLogFields, changeLogResponseTemplate, startFromDate, enableIssueModificationTimeline, issueModificationTimeline, issueModificationTimelineCaptureDate)
             maxResults = response['maxResults']
             total = response['total']
             startAt = response['startAt']
             if len(jiraIssues) > 0:
                 updatetimestamp = jiraIssues[len(jiraIssues) - 1]["fields"]["updated"]
                 dt = parser.parse(updatetimestamp)
                 fromDateTime = dt + datetime.timedelta(minutes=0o1)
                 fromDateTime = fromDateTime.strftime('%Y-%m-%d %H:%M')
                 self.tracking["lastupdated"] = fromDateTime
                 jiraKeyMetadata = {"dataUpdateSupported" : True,"uniqueKey" : ["key"]}
                 self.publishToolsData(data, jiraKeyMetadata)
                 #self.publishToolsData(data)
                 if len(workLogData) > 0:
                     insighstTimeXFieldMapping = self.config.get('changeLog', {}).get('insightsTimeXFieldMapping',None)
                     timeStampField=insighstTimeXFieldMapping.get('timefield',None)
                     timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
                     isEpoch=insighstTimeXFieldMapping.get('isEpoch',None); 
                     self.publishToolsData(workLogData, changeLogMetadata ,timeStampField,timeStampFormat,isEpoch,True)
                 if len(issueModificationTimeline) > 0:
                     self.publishToolsData(issueModificationTimeline, {"labels": ["LATEST"], "relation": {"properties": list(self.changedFields) + ['fields'], "name":"ISSUE_CHANGE_TIMELINE", "source": {"constraints":["key"]}, "destination": {"labels": ["TIMELINE"], "constraints":["timelineDate","timelineDateEpoch"]}}})
                 self.updateTrackingJson(self.tracking)
             else:
                 break
         latestJiraDateStr = self.tracking["lastupdated"]
         latestJiraDate = parser.parse(latestJiraDateStr)
         lastTrackedDate = parser.parse(self.tracking.get("lastTrakced", lastUpdated).split(' ')[0])
         lastTracked = lastTrackedDate.strftime("%Y-%m-%d %H:%M")
         reSync = self.tracking.get("reSync", False)
         if enableReSyncTrigger:
             if maxResults and not reSync and 0 >= (currentDate - latestJiraDate).total_seconds() <= (26*60*60) and (currentDate - lastTrackedDate).total_seconds() == (24*60*60):
                 self.tracking["lastupdated"] = lastTracked
                 self.tracking["issueModificationTimelineCaptureDate"] = lastTracked
                 self.tracking["reSync"] = True
                 self.tracking ["lastTracked"] = currentDate.strftime("%Y-%m-%d %H:%M" )
             elif reSync and currentDate >= lastTrackedDate:
                 self.tracking["reSync"] = False
         if enableIssueModificationTimeline :
             self.tracking["issueModificationTimelineCaptureDate"] = self.tracking["lastupdated"]
         if enableReSyncTrigger or enableIssueModificationTimeline:
             self.updateTrackingJson(self.tracking)
         if bypassSprintExtCall and maxResults:
             self.retrieveSprintDetails()
             self.retrieveSprintReports()

    def buildJiraRestUrl(self, baseUrl, startFrom, fields):
        lastUpdatedDate = self.tracking.get("lastupdated", startFrom)
        endDate = parser.parse(lastUpdatedDate) + datetime.timedelta(hours=24)
        endDate = endDate.strftime('%Y-%m-%d %H:%M')
        jiraIssuesUrl = baseUrl+"?jql=updated>='"+lastUpdatedDate+"' AND updated<'"+endDate+"' ORDER BY updated ASC&maxResults="+str(self.config.get("dataFetchCount", 1000))+'&fields='+fields
        changeLog = self.config.get('changeLog', None)
        if changeLog:
            jiraIssuesUrl = jiraIssuesUrl + '&expand=changelog'
        return jiraIssuesUrl

    def processChangeLog(self, issue, workLogFields, responseTemplate, startFromDate, enableIssueModificationTimeline, issueModificationTimeline, issueModificationTimelineCaptureDate):
        changeLog = issue.get('changelog', None)
        workLogData = []
        injectData = {'issueKey' : issue['key'] }
        if changeLog:
            histories = changeLog.get('histories', [])
            if enableIssueModificationTimeline:
                self.buildIssueModificationTimeLine(issue['key'], histories, issueModificationTimeline,issueModificationTimelineCaptureDate)
            loadRemoteLinks = False
            remoteIssueLinkDataMap = {}
            for change in histories:
                data = self.parseResponse(responseTemplate, change, injectData)[0]
                changeDate = parser.parse(data['changeDate'].split('.')[0])
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
                            if dataCopy.get('changeField', None) == 'RemoteIssueLink':
                                objectLinkId = dataCopy.get('to', None)
                                if objectLinkId is None:
                                    objectLinkId = dataCopy.get('from',None)
                                if objectLinkId :
                                    remoteIssueLinkDataMap[objectLinkId] = dataCopy
                                    loadRemoteLinks = True 
                    if loadRemoteLinks:
                        try:
                            self.loadRemoteLinks(issue['key'], remoteIssueLinkDataMap)
                        except Exception as ex:
                            logging.error(ex)
        return workLogData

    def loadRemoteLinks(self, issueKey, remoteIssueLinkChangeDataMap):
        remoteIssueLinksConfig = self.config.get('dynamicTemplate', {}).get('extensions',{}).get('remoteIssueLinks',None)
        if  remoteIssueLinksConfig:
            remoteIssueLinkRestUrl = remoteIssueLinksConfig.get("remoteIssueLinkRestUrl").format(issueKey)
            responseTemplate = remoteIssueLinksConfig.get("remoteIssueLinkResponseTemplate")
            remoteIssueLinkResponse = self.getResponse(remoteIssueLinkRestUrl, 'GET', self.userid, self.passwd, None)
            if remoteIssueLinkResponse:
                parsedResponses = self.parseResponse(responseTemplate, remoteIssueLinkResponse)
                for parsedResponse in parsedResponses:
                    remoteLinkId = parsedResponse['remoteLinkId']
                    if remoteLinkId:
                        remoteLinkId = str(remoteLinkId)
                        remoteLinkChangeObject = remoteIssueLinkChangeDataMap.get(remoteLinkId,{})
                        remoteLinkChangeObject.update(parsedResponse)

    def buildIssueModificationTimeLine(self, issueKey, histories, issueModificationTimeline, issueModificationTimelineCaptureDate):
        currentDate = parser.parse(datetime.datetime.now().strftime("%Y-%m-%d"))
        timelineMap = {}
        for change in histories:
            changeDate = parser.parse(change['created'].split('T')[0])
            # validate the working. we need to compare the date and time together. also, we will need to capture the change log till date and time.
            if currentDate>changeDate >= issueModificationTimelineCaptureDate:
                fields = timelineMap.get(str(changeDate), None)
                if fields is None:
                    fields = dict()
                    timelineMap[str(changeDate)] = fields
                items = change['items']
                for item in items:
                    changedField = re.sub (r'[-\+!~@#$%^&*()={}\[\]";<.>//\'\s"]', '' ,str(item['field']).lower()).capitalize()
                    fields[changedField] = fields.get(changedField, 0) +1
        for timelineDate in timelineMap:
            data = dict()
            data['key'] = issueKey
            data['timelineDate'] = timelineDate.split(' ')[0]
            fields = timelineMap[timelineDate]
            data['fields'] = list(fields.keys())
            for field in fields:
                data[field] = fields[field]
                self.changedFields.add(field)
            issueModificationTimeline.append(data)

    def scheduleExtensions(self):
        bypassSprintExtCall = self.config.get ('bypassSprintExtCall',False)
        extensions = self.config.get('dynamicTemplate', {}).get('extensions', None)
        if extensions:
            #backlog = extensions.get('backlog', None)
            #if backlog:
            #    self.registerExtension('backlog', self.retrieveBacklogDetails, backlog.get('runSchedule'))
            sprints = extensions.get('sprints', None)
            if sprints and not bypassSprintExtCall:
                self.registerExtension('sprints', self.retrieveSprintDetails, sprints.get('runSchedule'))
            sprintReport = extensions.get('sprintReport', None)
            if sprintReport and not bypassSprintExtCall:
                self.registerExtension('sprintReport', self.retrieveSprintReports, sprintReport.get('runSchedule'))
            releaseDetails = extensions.get('releaseDetails', None)
            if releaseDetails:
                self.registerExtension('releaseDetails', self.retrieveReleaseDetails, releaseDetails.get('runSchedule'))
            sprintDeletionIdentifier = extensions.get('sprintDeletionIdentifier', None)
            if sprintDeletionIdentifier:
                self.registerExtension('sprintDeletionIdentifier', self.sprintDeletionIdentifier, sprintDeletionIdentifier.get('runSchedule'))

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

    def propertyExtractor (self, temObject, data):
        if temObject is None or data is None:
            return
        keyType = type(temObject)
        if keyType is dict:
            for key in temObject:
                self.propertyExtractor(temObject.get(key, None), data)
        elif keyType is list:
            for valObject in temObject:
                self.propertyExtractor(valObject, data)
        elif keyType in [str, str]:
            data.append(temObject)
        else:
            logging.error ("Response Template not well formed")


    def processSprintInformation(self, parsedIssue, issue,  isIssueStatusFilter,sprintField, tracking):
        sprintStates = set()
        if sprintField:
            boardsTracking = tracking.get('boards', None)
            if boardsTracking is None:
                boardsTracking = {}
                tracking['boards'] = boardsTracking
            sprintDetails = issue.get("fields", {}).get(sprintField, None)
            if sprintDetails:
                try:
                    sprints = []
                    boards = []
                    for sprint in sprintDetails:
                        Version =self.config.get('dynamicTemplate', {}).get('versionUrl','')
                        VersionUrl = self.getResponse(Version, 'GET', self.userid, self.passwd, None)
                        deploymentType =VersionUrl.get('deploymentType','')
                        if (deploymentType) == 'Server':
                            sprintData = {}
                            sprintDetail = sprint.split("[")[1][:-1]
                            sprintPropertieTokens = sprintDetail.split(",")
                            for propertyToken in sprintPropertieTokens:
                                propertyKeyValToken = propertyToken.split("=")
                                if len(propertyKeyValToken) > 1:
                                    sprintData[propertyKeyValToken[0]] = propertyKeyValToken[1]
                            boardId = sprintData.get('rapidViewId')
                            sprintId = sprintData.get('id')
                        else:
                            boardId = sprint.get('boardId')
                            sprintId = sprint.get('id')
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
                except Exception as ex:
                    parsedIssue[0]['error'] = str(ex)

                parsedIssue[0]['sprints'] = sprints
                parsedIssue[0]['boards'] = boards
                #if len(boards) > 1 :
                #    for board in boards:
                #        boardTracking = boardsTracking.get(board)
                #        sprintTracking = boardTracking.get('sprints')
                #        for sprint in sprints:
                #            if sprintTracking.get(sprint, None) is None:
                #                sprintTracking[sprint] = {} 
 
    def retrieveSprintDetails (self):
        sprintDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprints', None)
		
        insighstTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprints', {}).get('insightsTimeXFieldMapping',None)
        timeStampField=insighstTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
        isEpoch=insighstTimeXFieldMapping.get('isEpoch',None);
		
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
                        self.publishToolsData(data, sprintMetadata,timeStampField,timeStampFormat,isEpoch,True)
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
                    self.publishToolsData(data, sprintMetadata,timeStampField,timeStampFormat,isEpoch,True)
    
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
        insighstTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('releaseDetails', {}).get('insightsTimeXFieldMapping',None)
        timeStampField=insighstTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
        isEpoch=insighstTimeXFieldMapping.get('isEpoch',None);

        if releaseDetails:
            jiraProjectApiUrl = releaseDetails.get('jiraProjectApiUrl', None)
            jiraProjectResponseTemplate = releaseDetails.get('jiraProjectResponseTemplate', None)
            jiraReleaseResponseTemplate = releaseDetails.get('jiraReleaseResponseTemplate', None)
            releaseVersionsMetadata = releaseDetails.get('releaseVersionsMetadata')
            if jiraProjectApiUrl and jiraProjectResponseTemplate and jiraReleaseResponseTemplate:
                jiraProjects = self.getResponse(jiraProjectApiUrl, 'GET',  self.userid, self.passwd, None)
                parsedJiraProjects = self.parseResponse(jiraProjectResponseTemplate, jiraProjects)
                for parsedJiraProject in parsedJiraProjects:
                    projectKey = parsedJiraProject['projectKey']
                    releaseApiUrl = jiraProjectApiUrl + '/' + projectKey + '/versions'
                    releaseVersionsResponse = self.getResponse(releaseApiUrl, 'GET', self.userid, self.passwd, None)
                    parsedReleaseVersions = self.parseResponse(jiraReleaseResponseTemplate, releaseVersionsResponse,parsedJiraProject)
                    self.publishToolsData(parsedReleaseVersions, releaseVersionsMetadata,timeStampField,timeStampFormat,isEpoch,True)

    def sprintDeletionIdentifier(self):
        deletedSprintsData = list()
        sprintDeletionIdentifier = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('sprintDeletionIdentifier',None)
        boards = self.tracking.get('boards', None)
        if sprintDeletionIdentifier and boards:
            sprintUrl = sprintDeletionIdentifier.get('sprintApiUrl','')
            userName = self.config.get("userid",'')
            password = self.config.get("passwd",'')
            for boardId in boards:
                boardMetaData = boards[boardId]
                sprints = boardMetaData.get('sprints', {})
                deletedSprints = dict()
                for sprintId in list(sprints.keys()):
                    sprintExists = self.checkingSprintExistence(sprintUrl, userName, password, sprintId)
                    if not sprintExists:
                        deletedSprints[sprintId] = sprints.pop(sprintId, dict())
                    if len(deletedSprints):
                        if 'deletedSprints' not in boardMetaData:
                            boardMetaData['deletedSrints'] = dict()
                        boardMetaData.get('deletedSprints', dict()).update(deletedSprints)
                        for sprintId in deletedSprints:
                            deletedSprintsData.append({'sprintId': int(sprintId), 'boardId': int(boardId), 'event': 'sprintDeleted'})
            if len(deletedSprintsData):
                metaData  = sprintDeletionIdentifier.get('metadata', dict())
                self.publishToolsData(deletedSprintsData, metaData)
                self.updateTrackingJson(self.tracking)
    def checkingSprintExistence(self, sprintUrl, userName, password, sprintId):
        try:
            url = sprintUrl +'/' +sprintId
            self.getResponse(url, 'GET', userName, password, None)
            return True 
        except Exception as err:
            if 'Sprint done not exists' in err.message:
                return False
            else:
                return  True 
if __name__ == "__main__":
    JiraAgent()           