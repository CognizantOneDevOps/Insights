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
Created on May 31, 2017

@author: 446620
'''
import json
import datetime
from time import mktime
from dateutil import parser

from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class UrbanCodeDeployAgent(BaseAgent):
    def process(self):
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        reportType = self.config.get("reportType", '')
        startFrom = self.config.get("startFrom", '')
        componentExecution = self.config.get("componentExecution", '')
        timeNow = datetime.datetime.now()
        timeNow = long((mktime(timeNow.timetuple()) + timeNow.microsecond/1000000.0) * 1000)
        if not componentExecution:
            if not self.tracking.get("lastUpdated",None):
                startFrom = parser.parse(self.config.get("startFrom", ''))
                startFrom = long((mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0) * 1000)
            else:
                startFrom = self.tracking.get("lastUpdated", None)
            ucdUrl = baseUrl+"/rest/report/adHoc?dateRange=custom&date_low="+str(startFrom)+"&date_hi="+str(timeNow)+"&orderField=date&sortType=desc&type="+str(reportType)
            response = self.getResponse(ucdUrl, 'GET', userid, passwd, None)
            data = []
            responseTemplate = self.getResponseTemplate()
            for item in range(len(response["items"][0])):
                data += self.parseResponse(responseTemplate, response["items"][0][item])
            self.tracking["lastUpdated"] = timeNow
            self.publishToolsData(data)
            self.updateTrackingJson(self.tracking)
        else:
            # Component URL: to get list of component
            rowsPerPage = 50
            pageNumber = 1
            exitCondition = True
            while exitCondition:
                componentUrl = baseUrl+"/rest/deploy/component/details?dateRange=custom&date_low="+str(startFrom)+"&date_hi="+str(timeNow)+"&rowsPerPage="+str(rowsPerPage)+"&pageNumber="+str(pageNumber)+"&orderField=name&sortType=asc&filterFields=active&filterValue_active=true&filterType_active=eq&filterClass_active=Boolean&outputType=BASIC&outputType=SECURITY&outputType=LINKED"
                print("componentUrl - ", componentUrl)
                componentResponse = self.getResponse(componentUrl, 'GET', userid, passwd, None)
                # for each component ID calling deployment URL to get the deployments detail.
                for i in range(len(componentResponse)):
                    self.processDeploymentData(baseUrl, componentResponse[i]["id"], userid, passwd, startFrom, timeNow)
                pageNumber += 1
                exitCondition = len(componentResponse) > 0

    def processDeploymentData(self, baseUrl, componentId, userid, passwd, startFrom, timeNow):
        rowsPerPage = 10
        pageNumber = 1
        exitCondition = True
        deploymentId = ""
        deploymentData = []
        deploymentChildrenData = []
        deploymentResponseTemplate = self.config.get('dynamicTemplate', {}).get('deploymentResponseTemplate',None)
        deploymentResponseTemplateChildren = self.config.get('dynamicTemplate', {}).get('deploymentResponseTemplateChildren',None)
        trackingDetails = self.tracking.get(str(componentId),None)
        if trackingDetails is None:
            trackingDetails = {}
            self.tracking[str(componentId)] = trackingDetails
        while exitCondition:
            deploymentUrl = baseUrl+"/rest/deploy/componentProcessRequest/table?dateRange=custom&date_low="+str(startFrom)+"&date_hi="+str(timeNow)+"&rowsPerPage="+str(rowsPerPage)+"&pageNumber="+str(pageNumber)+"&orderField=calendarEntry.scheduledDate&sortType=desc&filterFields=component.id&filterValue_component.id="+str(componentId)+"&filterType_component.id=eq&filterClass_component.id=UUID&outputType=BASIC&outputType=LINKED&outputType=EXTENDED"
            print("deploymentUrl -", deploymentUrl)
            deploymentResponse = self.getResponse(deploymentUrl, 'GET', userid, passwd, None)
            # Looping through response json to get deployment data
            for i in range(len(deploymentResponse)):
                injectData = {"componentId": componentId, "applicationId": deploymentResponse[i]["application"]["id"]}
                for x in range(len(deploymentResponse[i]["rootTrace"]["children"])):
                    deploymentChildrenData += (self.parseResponse(deploymentResponseTemplateChildren, deploymentResponse[i]["rootTrace"]["children"], injectData))
                deploymentData += (self.parseResponse(deploymentResponseTemplate, deploymentResponse[i]))
                deploymentId = deploymentResponse[i]["id"]
                timeNow = deploymentResponse[i]["startTime"]
                #self.publishToolsData(deploymentData)
                #self.publishToolsData(deploymentChildrenData)
            pageNumber += 1
            exitCondition = len(deploymentResponse) > 0
        # Update tracking json with the last captured deployment time
        trackingDetails["deployments"] = {"deploymentId": deploymentId, "lastUpdated": timeNow}
        self.tracking[str(componentId)] = trackingDetails
        self.updateTrackingJson(self.tracking)
            
if __name__ == "__main__":
    UrbanCodeDeployAgent()
