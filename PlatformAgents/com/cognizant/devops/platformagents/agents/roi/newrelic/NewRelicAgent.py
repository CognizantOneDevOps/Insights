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
Created on Jul 08, 2021

@author: 828567
'''
from datetime import datetime, timedelta
from dateutil import parser
from ....core.BaseAgent3 import BaseAgent
from .ROIUtilities import ROIUtilities
import json
import urllib
import threading

class NewRelicAgent(BaseAgent):
    lock = threading.Lock()
    
    @BaseAgent.timed
    def processROIAgent(self, data):
        self.baseLogger.info(" inside NewRelicAgent processROIAgent ======")
        outcomeDetails = json.loads(data)
        startDate = outcomeDetails.get("startDate", None)
        endDate = outcomeDetails.get("endDate", None)
        timeFormat = self.config.get("timeStampFormat", "%Y-%m-%dT%H:%M:%SZ")
        formattedStartDate = datetime.fromtimestamp(startDate).strftime(timeFormat)
        formattedEndDate = datetime.fromtimestamp(endDate).strftime(timeFormat)
        self.timeStampNow = lambda: datetime.utcnow().strftime(timeFormat)
        metricUrl = outcomeDetails.get("metricUrl",None)
        toolConfig = json.loads(outcomeDetails.get("configJson", {}))
#         appIds = toolConfig.get("newRelicAppId", None) 
#         appIdList = appIds.split(",")
        metricName = toolConfig.get("metricName", None)
        trackId = str(outcomeDetails["milestoneId"])+"_"+str(outcomeDetails["outcomeId"])  
        parametersList =  json.loads(outcomeDetails.get("requestParameters", None))
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
                "metricName": metricName,
                "lastUpdatedDate": self.timeStampNow(),
                "parameters": parameters,
                "status": "NOT_STARTED"
                }
        
        milestoneTemplate = self.config.get("dynamicTemplate", {}).get("responseTemplate", {}).get("milestoneDetails", {})
        trackingDetails = self.parseResponse(milestoneTemplate, outcomeDetails, injectData)
        
        if trackId in milestoneTrackingDetails and milestoneTrackingDetails[trackId]["status"] == "ABORTED":
            milestoneTrackingDetails[trackId]["metricName"] = metricName
            milestoneTrackingDetails[trackId]["metricUrl"] = metricUrl
            milestoneTrackingDetails[trackId]["parameters"] = parameters
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
            milestoneTrackingDetails[trackId]["retryCount"] = 0
        else:
            milestoneTrackingDetails[trackId] = trackingDetails[0]
        self.updateTrackingJson(self.tracking)
        self.baseLogger.info(" outcome details updated in tracking ======")
        self.process()
      
        
    @BaseAgent.timed 
    def process(self):
        with self.lock:
            self.baseLogger.info(" inside NewRelicAgent process ======")
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
                            roiUtilities.publishROIAgentstatus(self.messageFactory,itemDetails, "INPROGRESS", "inprogress")
                            roiUtilities.updateStatusInTracking("INPROGRESS", trackId)     
                    except Exception as ex:
                        self.baseLogger.error(" error occurred while fetching outcome data: "+str(ex))
                        roiUtilities.updateStatusInTracking("ERROR", trackId)
                        retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
                        if retry_count >= 3:
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "ERROR", str(ex))
                            self.baseLogger.info(" outcome error status message published ======")
    
    def fetchOutcomeData(self, outcomeDetails):
        self.baseLogger.info(" inside fetchoutcomeData ======")
        self.baseUrl = self.config.get("baseUrl", '')
        self.apiKey = self.config.get("apiKey", '')
        self.headers = {"Api-Key": self.apiKey}
        period = self.config.get("period", 3600)
        summarize = self.config.get("summarize", True)
        endDate = outcomeDetails.get("endDate",None)
        startDate = outcomeDetails.get("intermediateDate",None)
        if self.timeStampNow() < endDate:
            endDate = self.timeStampNow()
        if startDate is None:
            startDate = outcomeDetails.get("startDate",None)
        metadata = self.config.get("dynamicTemplate", {}).get("metadata", None)
        finalData = []        
#         appIdList = outcomeDetails.get("appIdList", list())
#         for appId in appIdList:
#         outcomeUrl = self.baseUrl+'/'+str(appId)+'/metrics/data.json?names[]='+outcomeDetails["metricName"]+'&from='+startDate+'&to='+endDate+'&period='+str(period)+'&summarize='+str(summarize)
        metricUrl = outcomeDetails.get("metricUrl",None)
        parameters = outcomeDetails.get("parameters",None)
        if '?' in metricUrl:
            queryParam = dict(urllib.parse.parse_qsl(urllib.parse.urlsplit(metricUrl).query))
            parameters.update(queryParam)
            metricUrl = metricUrl.split("?")[0]
        parameters['from'] = startDate
        parameters['to'] = endDate
        queryString = urllib.parse.urlencode(parameters)
        outcomeUrl = metricUrl+'?'+ queryString
        outcomeResponseData = self.getResponse(outcomeUrl, 'GET', None, None, None, reqHeaders=self.headers)
        metrics = outcomeResponseData.get("metric_data", {}).get("metrics", list())
        
        if len(metrics) > 0:
            timeslicesData = metrics[0]["timeslices"]
            outcomeMetadata = {"milestoneId": outcomeDetails["milestoneId"], 
                "milestoneName": outcomeDetails["milestoneName"],
                "milestoneReleaseId": outcomeDetails["milestoneReleaseId"],
                "outcomeName": outcomeDetails["outcomeName"],
                "outcomeId": outcomeDetails["outcomeId"]}
            for data in timeslicesData:
                values = data.get("values", {})
                data.pop("values")
                data.update(values)
                data.update(outcomeMetadata.copy())
                finalData.append(data)
            self.publishToolsData(finalData, metadata)
            self.baseLogger.info(" outcome data published ======")
        else:
            self.baseLogger.error(" no data returned for outcome name: "+outcomeDetails["outcomeName"]) 
            raise ValueError(' no data returned, please check input parameters.')  
    
        
        
if __name__ == "__main__":
    NewRelicAgent()
