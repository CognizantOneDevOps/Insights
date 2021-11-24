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
Created on May 7, 2021

@author: 828567
'''
from datetime import datetime as dateTime
from dateutil import parser
import datetime 
import copy
import re 
import os
import hashlib
from dateutil import parser
import uuid
from ....core.BaseAgent3 import BaseAgent
import json


class GitLabIssueWebhookAgent(BaseAgent):
    
    @BaseAgent.timed
    def processWebhook(self,data):
        self.baseLogger.info(" inside GitlabIssue processWebhook ======")
        dataReceived = json.loads(data)
        if "event_type" in dataReceived and  dataReceived["event_type"] == "issue":
            self.processIssueDetails(dataReceived)
        
    def processIssueDetails(self, dataReceived):
        self.baseLogger.info(" inside processIssueDetails method ======")
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        responseTemplate = dynamicTemplate.get('issue', {}).get('issueResponseTemplate', {})
        issueMetadata = dynamicTemplate.get('issue', {}).get('issueMetadata', {})
        timeStampField = dynamicTemplate.get('issue', {}).get('insightsTimeXFieldMapping', {}).get('timefield',None)
        timeStampFormat = dynamicTemplate.get('issue', {}).get('insightsTimeXFieldMapping', {}).get('timeformat',None)
        isEpoch = dynamicTemplate.get('issue', {}).get('insightsTimeXFieldMapping', {}).get('isEpoch',False)
        time = dataReceived.get("object_attributes",{}).get("updated_at",None)
        updatedAt = parser.parse(time, ignoretz=True).strftime("%Y-%m-%dT%H:%M:%SZ")
        dataReceived["object_attributes"]["updated_at"] = updatedAt
        assigneeDict = {}
        labelDict = {}
        
        self.baseLogger.info(" before parseResponse issue data ======")    
        parsedIssueResponse = self.parseResponse(responseTemplate, dataReceived)
        
        if "assignees" not in dataReceived :
            parsedIssueResponse[0].update(self.updateMandatoryFields("assignees", responseTemplate))
        
        if "labels" not in dataReceived :
            parsedIssueResponse[0].update(self.updateMandatoryFields("labels", responseTemplate))
        closed_at = dataReceived.get("object_attributes",{}).get("closed_at")
        if closed_at is None: parsedIssueResponse[0]["issueClosedDate"] = ""
        
        due_date = dataReceived.get("object_attributes",{}).get("due_date")
        if due_date is None: parsedIssueResponse[0]["issueDueDate"] = ""
        
        self.baseLogger.info(" before publish issue data ======") 
        self.publishToolsData(parsedIssueResponse, issueMetadata,timeStampField,timeStampFormat,isEpoch,True)
        
        action = dataReceived["object_attributes"]["action"]
        if action != "open":
            changesMetaData = {"issueId" : parsedIssueResponse[0]["issueId"],
                       "issueDisplayId" : parsedIssueResponse[0]["issueDisplayId"],
                       "updatedById": parsedIssueResponse[0]["updatedById"],
                       "updatedAt": parsedIssueResponse[0]["updatedAt"]}
            self.processChangelog(dataReceived, changesMetaData)
        self.baseLogger.info(" issue details processing completed ======")
                
    def processChangelog(self, dataReceived, changesMetaData):
        self.baseLogger.info(" inside processChangelog method ======")
        changeLogData = dataReceived.get("changes",{})
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        responseTemplate = dynamicTemplate.get('issue', {}).get('issueResponseTemplate', {})
        changes = dynamicTemplate.get("Changes", {})
        metaData = changes.get("metadata", {})
        relationMetaData = changes.get("relationMetadata", {})
        finalData = []
        for changedfield in changeLogData:
            fieldDict = {}
            issueChangeDict = {}
            issueChangeDict = changesMetaData.copy()
            issueChangeDict["changedfield"] = changedfield
            issueChangeDict["changeId"] = str(uuid.uuid1())
            prev_details = changeLogData[changedfield]["previous"]
            current_details = changeLogData[changedfield]["current"]
            if prev_details is not None:
                keyType = type(prev_details)
            else:
                keyType = type(current_details)
            
            if keyType is list:
                if not prev_details:
                    previousData = [self.updateMandatoryFields(changedfield, responseTemplate)]
                else:
                    fieldDict[changedfield] = prev_details
                    previousData = self.parseResponse(responseTemplate, fieldDict)
                if not current_details:
                    currentData = [self.updateMandatoryFields(changedfield, responseTemplate)]
                else:
                    fieldDict[changedfield] = current_details
                    currentData = self.parseResponse(responseTemplate, fieldDict)
                    
                processed_prevDetails = {"prev_" + str(key): val for key, val in list(previousData[0].items())}
                issueChangeDict.update(processed_prevDetails)
                processed_currentDetails = {"current_" + str(key): val for key, val in list(currentData[0].items())}
                issueChangeDict.update(processed_currentDetails)
                finalData.append(issueChangeDict) 
            elif keyType is str or keyType is int: 
                if prev_details is None: prev_details = ""
                if current_details is None: current_details = ""
                issueChangeDict["prev_"+changedfield] = prev_details
                issueChangeDict["current_"+changedfield] = current_details
                finalData.append(issueChangeDict)
        self.baseLogger.info(" before publish issue changelog data ======")
        self.publishToolsData(finalData, metaData)
        for item in finalData:
            item.update( {"gitlab_webhookType":"issue"})
        self.publishToolsData(finalData, relationMetaData) 
        self.baseLogger.info(" issue changelog processing completed ======") 
        
    def updateMandatoryFields(self, field, template):
        self.baseLogger.info(" inside updateMandatoryFields method ======")
        field_details = {}
        if field in template and type(template[field]) is list:
            field_details = template.get(field, list())[0].copy()
            field_details = {field_details[key]:"" for key in field_details}
        return field_details 
                
                    
        
if __name__ == "__main__":
    GitLabIssueWebhookAgent()           