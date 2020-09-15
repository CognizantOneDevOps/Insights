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
Created on Jul 15, 2019

@author: 302683
'''
from datetime import datetime as dateTime2
import datetime
import copy
import json

from dateutil import parser

from ....core.BaseAgent3 import BaseAgent

class AzureBoardAgent(BaseAgent):

    def process(self):
        self.userid = self.getCredential("userid")
        self.passwd = self.getCredential("passwd")
        baseUrl = self.config.get("baseUrl", '')
        wiqlUrl = self.config.get("wiqlUrl", '')

        projectName = self.config.get("projectName", '')
        startFrom = self.config.get("startFrom", '')
        lastUpdated = self.tracking.get("lastupdated", startFrom)
        responseTemplate = self.getResponseTemplate()
        WIQL_URL = wiqlUrl
        newWorkItemQuery = "{\"query\":\"Select [System.Id] From WorkItems Where [System.TeamProject] =='" + \
            projectName + "' AND [System.ChangedDate] > '" + \
            lastUpdated + "' order by [System.ChangedDate] asc\"}"

        changeLog = self.config.get('dynamicTemplate', {}).get('changeLog', None)
        headers = {'Content-Type': 'application/json'}

        if changeLog:
            changeLogFields = changeLog['fields']
            changeLogMetadata = changeLog['metadata']
            changeLogResponseTemplate = changeLog['responseTemplate']
            startFromDate = parser.parse(startFrom)
        updatetimestamp = None
        sprintField = self.config.get("sprintField", None)
        workLogData = []
        wiqlResponse = self.getResponse(WIQL_URL, 'POST', self.userid, self.passwd, newWorkItemQuery, None, headers)
        for workItemIterator in range(0, len(wiqlResponse["workItems"])):
            workItem = wiqlResponse["workItems"][workItemIterator]
            data = []
            newWorkItemData = {}
            workItemUrl = baseUrl + "_apis/wit/workItems/" + \
                str(workItem["id"]) + "?$expand=all"
            workItemData = self.getResponse(workItemUrl, 'GET', self.userid, self.passwd, None)

            injectData = {}
            Parent = []
            Child = []
            Commit = []
            if workItemData.get("relations", None) is not None:
                for relation in workItemData["relations"]:
                    if relation["attributes"].get("name", None) == 'Parent':
                        Parent.append(relation["url"].split('/')[8])
                    if relation["attributes"].get("name", None) == 'Child':
                        Child.append(relation["url"].split('/')[8])
                    if relation["attributes"].get("name", None) == 'Fixed in Commit':
                        Commit.append(relation["url"].lower().split('%2f')[2])

            injectData["Parent"] = Parent
            injectData["Child"]  = Child
            injectData["Commit"] = Commit
            injectData["isNodeUpdated"] = True
            updatetimestamp = workItemData["fields"]["System.ChangedDate"]

            parsedIssue = self.parseResponse(responseTemplate, workItemData, injectData)
            data += parsedIssue
            if changeLog:
                workLogData = self.processChangeLog(baseUrl, workItem["id"], changeLogFields, changeLogResponseTemplate, startFromDate)

            dt = parser.parse(updatetimestamp)
            fromDateTime = dt + datetime.timedelta(minutes=0o1)
            fromDateTime = fromDateTime.strftime('%Y-%m-%d %H:%M')
            self.tracking["lastupdated"] = fromDateTime
            jiraKeyMetadata = {
                "dataUpdateSupported": True, "uniqueKey": ["key"]}
            self.publishToolsData(data, jiraKeyMetadata)
            if len(workLogData) > 0:
                insighstTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('changeLog', {}).get('insightsTimeXFieldMapping',None)
                timeStampField=insighstTimeXFieldMapping.get('timefield',None)
                timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
                isEpoch=insighstTimeXFieldMapping.get('isEpoch',None);
                timeFieldMapping=self.config.get('dynamicTemplate', {}).get('changeLog', {}).get('timeFieldMapping',None) 
                self.publishToolsData(workLogData, changeLogMetadata,timeStampField,timeStampFormat,isEpoch,True)
            self.updateTrackingJson(self.tracking)

    def processChangeLog(self, baseUrl, issue, workLogFields, responseTemplate, startFromDate):
        workItemChangeUrl = baseUrl + \
            "_apis/wit/workItems/" + str(issue) + "/updates"
        workItemDataUpdate = self.getResponse(workItemChangeUrl, 'GET', self.userid, self.passwd, None)
        workLogData = []
        injectData = {'issueKey': str(issue)}
        if workItemDataUpdate:
            histories = workItemDataUpdate["value"]
            for change in histories:
                data = copy.deepcopy(self.parseResponse(responseTemplate, change, injectData)[0])
                changeDate = parser.parse(data['changeDate'][:19])
                if (str(changeDate) == "9999-01-01 00:00:00+00:00") or (changeDate > startFromDate):
                    items = change.get("fields", None)
                    if items:
                        for item in items:
                            if item in workLogFields:
                                dataCopy = copy.deepcopy(data)
                                dataCopy['changedfield'] = item
                                dataCopy['from'] = items[item].get('oldValue', None)
                                dataCopy['to'] = items[item].get('newValue', None)
                                workLogData.append(dataCopy)
                    relations = change.get("relations",None)
                    if relations:
                        relationshipData = dict()
                        if relations.get("added",None):
                            addedRelations = relations.get("added",None)
                            for relation in addedRelations:
                                relation["attributes"]["name"] = relation["attributes"]["name"].replace(" ","")
                                if (relation["attributes"]["name"] == "Parent") or (relation["attributes"]["name"] == "Child") or (relation["attributes"]["name"] == "FixedinCommit"):
                                    if "added"+relation["attributes"]["name"] in relationshipData:
                                        if relation["attributes"]["name"] == "FixedinCommit":
                                            relationshipData["added"+relation["attributes"]["name"]].append(relation["url"].lower().split("%2f")[2])
                                        else:
                                            relationshipData["added"+relation["attributes"]["name"]].append(relation["url"].split("/")[8])
                                    else:
                                        relationshipData["added"+relation["attributes"]["name"]] = []
                                        if relation["attributes"]["name"] == "FixedinCommit":
                                            relationshipData["added"+relation["attributes"]["name"]].append(relation["url"].lower().split("%2f")[2])
                                        else:
                                            relationshipData["added"+relation["attributes"]["name"]].append(relation["url"].split("/")[8])
                        if relations.get("removed",None):
                            removedRelations = relations.get("removed",None)
                            for relation in removedRelations:
                                relation["attributes"]["name"] = relation["attributes"]["name"].replace(" ","")
                                if (relation["attributes"]["name"] == "Parent") or (relation["attributes"]["name"] == "Child") or (relation["attributes"]["name"] == "FixedinCommit"):
                                    if "removed"+relation["attributes"]["name"] in relationshipData:
                                        if relation["attributes"]["name"] == "FixedinCommit":
                                            relationshipData["removed"+relation["attributes"]["name"]].append(relation["url"].lower().split("%2f")[2])
                                        else:
                                            relationshipData["removed"+relation["attributes"]["name"]].append(relation["url"].split("/")[8])
                                    else:
                                        relationshipData["removed"+relation["attributes"]["name"]] = []
                                        if relation["attributes"]["name"] == "FixedinCommit":
                                            relationshipData["removed"+relation["attributes"]["name"]].append(relation["url"].lower().split("%2f")[2])
                                        else:
                                            relationshipData["removed"+relation["attributes"]["name"]].append(relation["url"].split("/")[8])
                        if relationshipData:
                            dataCopy = copy.deepcopy(data)
                            dataCopy['changedfield'] = "relationship"
                            for relationship in relationshipData:
                                dataCopy[relationship] = relationshipData[relationship]
                            workLogData.append(dataCopy)
        return workLogData

    def scheduleExtensions(self):
        extensions = self.config.get(
            'dynamicTemplate', {}).get('extensions', None)
        if extensions:
            sprints = extensions.get('sprints', None)
            if sprints:
                self.registerExtension(
                    'sprints', self.retrieveSprintDetails, sprints.get('runSchedule'))

    def retrieveSprintDetails(self):
        sprintDetails = self.config.get('dynamicTemplate', {}).get(
            'extensions', {}).get('sprints', None)
        insighstTimeXFieldMapping = sprintDetails.get('insightsTimeXFieldMapping',None)
        timeStampField=insighstTimeXFieldMapping.get('timefield',None)
        timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
        isEpoch=insighstTimeXFieldMapping.get('isEpoch',None);
        teamApiUrl = sprintDetails.get('teamApiUrl')
        responseTemplate = sprintDetails.get('sprintResponseTemplate', None)
        sprintMetadata = sprintDetails.get('sprintMetadata')
        userid = self.config.get('userid', None)
        passwd = self.config.get('passwd', None)
        teams = self.getResponse(teamApiUrl, 'GET', userid, passwd, None)["value"]
        for team in teams:
            sprintApiUrl = sprintDetails.get("sprintApiUrl", None)
            if sprintApiUrl:
                sprintApiUrl = sprintApiUrl.replace("<<team>>", team["name"].replace(" ", "%20"))
                injectData = {"teamName": team["name"] }
                sprints = self.getResponse(sprintApiUrl, 'GET', userid, passwd, None)["value"]
                for sprint in sprints:
                    self.publishToolsData(self.parseResponse(responseTemplate, sprint, injectData), sprintMetadata,timeStampField,timeStampFormat,isEpoch,True)

if __name__ == "__main__":
    AzureBoardAgent()