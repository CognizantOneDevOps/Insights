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
Created on Jul 15, 2021

@author: 658723
'''
from dateutil import parser
from datetime import datetime
from datetime import timedelta
from ....core.BaseAgent3 import BaseAgent

import json

class SpinnakerAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        baseUrl = self.config.get("baseUrl", '')
        applicationsUrl = baseUrl + 'applications'
        accessToken = self.getCredential("accessToken")
        headers = {"Authorization": "Bearer " + accessToken}
        startFrom = self.config.get("startFrom", '')
        spinnakerApplications = self.getResponse(applicationsUrl, 'GET', None, None, None, reqHeaders=headers)
        responseTemplate = self.getResponseTemplate()
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        stagesTemplate = dynamicTemplate.get('stages', {})
        stageMetadata = dynamicTemplate.get('extensions', {}).get('relationMetadata', None)
        executionMetadata = dynamicTemplate.get('metadata', {}).get('executions', None)

        for application in spinnakerApplications:
            applicationName = application["name"]
            data = []
            stageData = []
            timestamp = self.tracking.get(applicationName, startFrom)
            lastUpdatedDate = None
            executionsUrl = applicationsUrl + '/' + applicationName + '/executions/search?triggerTimeStartBoundary=' + str(timestamp)
            executions = self.getResponse(executionsUrl, 'GET', None, None, None, reqHeaders=headers)
            pagenum = 0
            fetchNextPage = True
            while fetchNextPage:
                if len(executions) == 0:
                    fetchNextPage = False
                    break
                for execution in executions:
                    data += self.parseResponse(responseTemplate, execution)
                    stages = execution.get("stages", {})
                    stageData += self.getStageDetails(stages, stagesTemplate, execution["id"])
                    if lastUpdatedDate is None:
                        lastUpdatedDate = execution.get("buildTime")
                        self.tracking[applicationName] =str(lastUpdatedDate + 1)
                self.publishToolsData(data, executionMetadata, "buildTime", None, True)
                self.publishToolsData(stageData, stageMetadata, "stageStartTime", None, True)
                pagenum = pagenum + 10
                executionsPageUrl = executionsUrl + '&startIndex=' + str(pagenum)
                executions = self.getResponse(executionsPageUrl, 'GET', None, None, None, reqHeaders=headers)
            self.updateTrackingJson(self.tracking)

    def getStageDetails(self, stages, template, executionId):
        data = []
        for stage in stages:
            stageData = self.parseResponse(template, stage)
            stageData[0]['pipelineExecutionId'] = executionId
            data += stageData
        return data

if __name__ == "__main__":
    SpinnakerAgent() 