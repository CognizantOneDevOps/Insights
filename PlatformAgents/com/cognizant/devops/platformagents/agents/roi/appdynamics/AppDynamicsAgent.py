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

from dateutil import parser
import time
import sys
import os
from datetime import datetime, timedelta
from ....core.BaseAgent3 import BaseAgent
from ....core.ROIUtilities import ROIUtilities
import json
import threading
import base64
import urllib

class AppDynamicsAgent(BaseAgent):
    lock = threading.Lock()
    
    @BaseAgent.timed
    def processROIAgent(self, data):
        self.baseLogger.info(" inside AppDynamicsAgent process ======")
        eventDetails = json.loads(data)
        startDate = eventDetails.get("startDate", None)
        endDate = eventDetails.get("endDate", None)
        timeFormat = self.config.get("timeStampFormat", "%Y-%m-%dT%H:%M:%SZ")
        formattedStartDate = datetime.fromtimestamp(startDate).strftime(timeFormat)
        formattedEndDate = datetime.fromtimestamp(endDate).strftime(timeFormat)
        self.timeStampNow = lambda: datetime.utcnow().strftime(timeFormat)
        toolConfig = json.loads(eventDetails.get("configJson", {}))
#         appName = toolConfig.get("appDynamicsAppName", None) 
        metricPath = toolConfig.get("appDynamicsMetricPath", None)
        metricUrl = eventDetails.get("metricUrl",None)
        trackId = str(eventDetails["milestoneId"])+"_"+str(eventDetails["outcomeId"])  
        parametersList =  json.loads(eventDetails.get("requestParameters", None))
        parameters = {}
        for params in parametersList:
            values = list(params.values()) 
            parameters[values[0]] = values[1]                   
        
        milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
        if milestoneTrackingDetails is None:
            milestoneTrackingDetails = {}
            self.tracking["milestoneDetails"] = milestoneTrackingDetails
        
        injectData = {           
                "startDate": formattedStartDate,
                "endDate": formattedEndDate,            
                "metricPath": metricPath,
                "lastUpdatedDate": self.timeStampNow(),
                "parameters": parameters,
                "status": "NOT_STARTED"
                }
        
        milestoneTemplate = self.config.get("dynamicTemplate", {}).get("responseTemplate", {}).get("milestoneDetails", {})
        trackingDetails = self.parseResponse(milestoneTemplate, eventDetails, injectData)
        
        if trackId in milestoneTrackingDetails and milestoneTrackingDetails[trackId]["status"] == "ABORTED":
            milestoneTrackingDetails[trackId]["metricPath"] = metricPath
            milestoneTrackingDetails[trackId]["metricUrl"] = metricUrl
            milestoneTrackingDetails[trackId]["parameters"] = parameters
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
            milestoneTrackingDetails[trackId]["retryCount"] = 0
        else:
            milestoneTrackingDetails[trackId] = trackingDetails[0]
        self.updateTrackingJson(self.tracking)
        self.process()       


    @BaseAgent.timed 
    def process(self):
        with self.lock:
            self.baseLogger.info(" inside AppDynamicsAgent process ======")
            roiUtilities = ROIUtilities(self.messageFactory, self.tracking, self.trackingFilePath, self.config)
            timeFormat = self.config.get("timeStampFormat", "%Y-%m-%dT%H:%M:%SZ")
            self.timeStampNow = lambda: datetime.utcnow().strftime(timeFormat)
            self.runSchedule = self.config.get("runSchedule", 30)
            milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
            if milestoneTrackingDetails is not None:
                milestoneDetails = milestoneTrackingDetails.copy()
                for item in milestoneDetails:
                    try:
                        itemDetails = milestoneDetails[item]
                        milestoneId = itemDetails.get("milestoneId",None)
                        outcomeId = itemDetails.get("outcomeId",None)
                        trackId = str(milestoneId)+"_"+str(outcomeId)
                        status = itemDetails.get("status",None)
                        nextRun = parser.parse(itemDetails["lastUpdatedDate"], ignoretz=True) + timedelta(seconds=self.runSchedule * 60)
                        if status == "ABORTED" or (status == "INPROGRESS" and self.timeStampNow() < nextRun.strftime(timeFormat)):
                            continue
                        if self.timeStampNow() > itemDetails["startDate"] and self.timeStampNow() > itemDetails["endDate"]:
                            self.fetchOutcomeData(itemDetails)
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "SUCCESS", "completed")
                            roiUtilities.updateStatusInTracking("COMPLETED", trackId)
                        elif self.timeStampNow() > itemDetails["startDate"] and self.timeStampNow() < itemDetails["endDate"]:
                            self.fetchOutcomeData(itemDetails)
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "INPROGRESS", "inprogress")
                            roiUtilities.updateStatusInTracking("INPROGRESS", trackId)     
                    except Exception as ex:
                        self.baseLogger.error(" error occurred while fetching outcome data: "+str(ex))
                        roiUtilities.updateStatusInTracking("ERROR", trackId)
                        retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
                        if retry_count >= 3:
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "ERROR", str(ex))
    

    def getEncodedAuth(self):
        self.baseUrl = self.config.get("baseUrl", '')
        self.username = self.config.get("username", '')
        self.password = self.config.get("password", '')
        self.account = self.config.get("account", '')
        auth = self.username + '@' + self.account, self.password
        #encodedAuth = base64.encodestring('%s:%s' % auth).replace('\n', '')
        encodedAuth = base64.encodebytes(('%s:%s' % auth).encode()).decode().replace('\n', '')
        return encodedAuth


    def getEpochInMillis(self, date):
        dtTime = datetime.strptime(date, "%Y-%m-%dT%H:%M:%SZ")
        dtEpochTime = int((dtTime - datetime(1970, 1, 1)).total_seconds() * 1e3)
        return dtEpochTime

    def fetchOutcomeData(self, outcomeDetails):
        self.baseLogger.info(" inside fetchOutcomeData ======")
        encodedAuth = self.getEncodedAuth()
        self.headers = {"Authorization": "Basic {0}".format(encodedAuth)}
        endDate = outcomeDetails.get("endDate",None)
        startDate = outcomeDetails.get("intermediateDate",None)
#         appNames = outcomeDetails.get("appName",None)
        metricPath = outcomeDetails.get("metricPath",None)
        if self.timeStampNow() < endDate:
            endDate = self.timeStampNow()
        if startDate is None:
            startDate = outcomeDetails.get("startDate",None)
        
        startEpochTime = self.getEpochInMillis(startDate)
        endEpochTime = self.getEpochInMillis(endDate)
        responseTemplate = self.config.get("dynamicTemplate", {}).get("responseTemplate", {})
        metadata = responseTemplate.get("metadata", None)
        finalData = []
#         listOfApps = appNames.split(',')
#         for app in listOfApps:
#             app = app.strip() 
#             outcomeUrl = self.baseUrl+'/'+app+'/metric-data?output=json&metric-path='+metricPath+'&time-range-type=BETWEEN_TIMES&start-time='+str(startEpochTime)+'&end-time='+str(endEpochTime)+'&rollup=true'
        metricUrl = outcomeDetails.get("metricUrl",None)
        parameters = outcomeDetails.get("parameters",None)
        if '?' in metricUrl:
            queryParam = dict(urllib.parse.parse_qsl(urllib.parse.urlsplit(metricUrl).query))
            parameters.update(queryParam)
            metricUrl = metricUrl.split("?")[0]
        parameters['output'] = "json"
        parameters['time-range-type'] = "BETWEEN_TIMES"
        parameters['start-time'] = str(startEpochTime)
        parameters['end-time'] = str(endEpochTime)
        queryString = urllib.parse.urlencode(parameters)
        outcomeUrl = metricUrl+'?'+ queryString
        outcomeMetadata = {"milestoneId": outcomeDetails["milestoneId"], 
                    "milestoneName": outcomeDetails["milestoneName"],
                    "outcomeName": outcomeDetails["outcomeName"],
                    "outcomeId": outcomeDetails["outcomeId"],
                    "metricPath": metricPath}
        outcomeResponseData = self.getResponse(outcomeUrl, 'GET', None, None, None, reqHeaders=self.headers)
        if len(outcomeResponseData) > 0:
            for metric in outcomeResponseData:
                metricsValues = metric.get("metricValues", {})
                if len(metricsValues) > 0:
                    for data in metricsValues:
                        data.update(outcomeMetadata.copy())
                        finalData.append(data)
                else:
                    self.baseLogger.error(" No metric values returned for outcome name: "+outcomeDetails["outcomeName"]) 
                    raise ValueError(' No metric values returned for outcome name: '+outcomeDetails["outcomeName"])
        else:
            self.baseLogger.error(" metrics data not found, please check input parameters. ") 
            raise ValueError(' metrics data not found, please check input parameters. ')  
        
        if len(finalData) > 0:
            self.publishToolsData(finalData, metadata)
      
            
        
if __name__ == "__main__":
    AppDynamicsAgent()
