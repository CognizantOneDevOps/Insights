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
Created on Aug 16, 2021

@author: 828567
'''
from dateutil import parser
import time
import sys
import os
from datetime import datetime, timedelta
from ....core.BaseAgent3 import BaseAgent
from .ROIUtilities import ROIUtilities
import json
import threading

class ElasticSearchAgent(BaseAgent):
    lock = threading.Lock()
    
    @BaseAgent.timed
    def processROIAgent(self, data):
        self.baseLogger.info(" inside NewRelicAgent process ======")
        eventDetails = json.loads(data)
        milestoneId = eventDetails.get("milestoneId", None)
        milestoneName = eventDetails.get("milestoneName", None)
        milestoneReleaseId = eventDetails.get("milestoneReleaseId", None)
        startDate = eventDetails.get("startDate", None)
        endDate = eventDetails.get("endDate", None)
        outcomeName = eventDetails.get("outcomeName", None)
        outcomeId = eventDetails.get("outcomeId", None)
        formattedStartDate = datetime.fromtimestamp(startDate).strftime("%Y-%m-%dT%H:%M:%SZ")
        formattedEndDate = datetime.fromtimestamp(endDate).strftime("%Y-%m-%dT%H:%M:%SZ")
        self.timeStampNow = lambda: datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        statusQueue = eventDetails.get("statusQueue", None)
        toolConfig = json.loads(eventDetails.get("configJson", {}))
        metricKey = toolConfig.get("logKey", None)
        index_name = "platformservice_index" #toolConfig.get("indexName", None)
        trackId = str(milestoneId)+"_"+str(outcomeId)                     
        
        milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
        if milestoneTrackingDetails is None:
            milestoneTrackingDetails = {}
            self.tracking["milestoneDetails"] = milestoneTrackingDetails
        
        trackingDetails = {
                "milestoneId": milestoneId,
                "milestoneName" : milestoneName,
                "startDate": formattedStartDate,
                "endDate": formattedEndDate,
                "outcomeId": outcomeId,
                "outcomeName": outcomeName,
                "metricKey": metricKey,
                "index_name": index_name,
                "statusQueue": statusQueue,
                "lastUpdatedDate": self.timeStampNow(),
                "status": "NOT_STARTED",
                "milestoneReleaseId": milestoneReleaseId
                }
        if trackId in milestoneTrackingDetails and milestoneTrackingDetails[trackId]["status"] == "ERROR":
            milestoneTrackingDetails[trackId]["outcomeName"] = outcomeName
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
            milestoneTrackingDetails[trackId]["retryCount"] = 0
        else:
            milestoneTrackingDetails[trackId] = trackingDetails
        self.updateTrackingJson(self.tracking)
        self.process()

        
    @BaseAgent.timed 
    def process(self):
        print("===INSIDE==")
        roiUtilities = ROIUtilities(self.messageFactory, self.tracking, self.trackingFilePath, self.config)
        with self.lock:
            self.timeStampNow = lambda: datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
            self.runSchedule = self.config.get("runSchedule", 30)
            print(" test run schedule",self.timeStampNow())
            milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
            if milestoneTrackingDetails is not None:
                milestoneDetails = milestoneTrackingDetails.copy()
                for item in milestoneDetails:
                    try:
                        itemDetails = milestoneDetails[item]
                        milestoneId = itemDetails.get("milestoneId",None)
                        outcomeId = itemDetails.get("outcomeId",None)
                        trackId = str(milestoneId)+"_"+str(outcomeId)
                        if itemDetails["status"] == "INPROGRESS":
                            date = parser.parse(itemDetails["lastUpdatedDate"], ignoretz=True) + timedelta(seconds=self.runSchedule * 60)
                            if self.timeStampNow() < date.strftime("%Y-%m-%dT%H:%M:%SZ"):
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
                        print(" process exception: ",ex)
                        self.baseLogger.error(" error occurred while fetching outcome data: "+str(ex))
                        roiUtilities.updateStatusInTracking("ERROR", trackId)
                        retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
                        if retry_count >= 3:
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "ERROR", str(ex))
        
        
    
    def fetchOutcomeData(self, outcomeDetails):
        print("inside fetchoutcomeData ==")
        self.baseUrl =self.config.get("baseUrl", None)
        self.headers = {"Content-Type":"application/json"}
        metricKey = outcomeDetails.get("metricKey", None)
        index_name = outcomeDetails.get("index_name", None)
        query = self.config.get("dynamicTemplate", {}).get(metricKey, {})
        url = self.baseUrl + "/" + index_name + "/_count"
        must = query.get("query", {}).get("bool",{}).get("must", list())
        endDate = outcomeDetails.get("endDate",None)
        startDate = outcomeDetails.get("intermediateDate",None)
        if self.timeStampNow() < endDate:
            endDate = self.timeStampNow()
        if startDate is None:
            startDate = outcomeDetails.get("startDate",None)
        range = {"@timestamp": {
                    "gte": startDate,
                    "lt" : endDate
                }}
        must[2]["range"] = range
        responseData = self.getResponse(url, 'POST', None, None, data=json.dumps(query), reqHeaders=self.headers)
        print(responseData)
#         total = responseData.get("hits", {}).get("total", {})
#         value = total.get("value", None)
        count = responseData.get("count", None)
        data = {"milestoneId": outcomeDetails["milestoneId"], 
                    "milestoneName": outcomeDetails["milestoneName"],
                    "outcomeName": outcomeDetails["outcomeName"],
                    "outcomeId": outcomeDetails["outcomeId"],
                    "count": count,"from": startDate, "to": endDate,
                    "milestoneReleaseId": outcomeDetails["milestoneReleaseId"]}
        self.publishToolsData(data)
        
    def updateStatusInTracking(self, status, trackId):
        print("inside updateStatusInTracking", status)
        print("trackid", trackId)
        milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
        milestoneTrackingDetails[trackId]["lastUpdatedDate"] = self.timeStampNow()
        if status == "COMPLETED":
            completedMilestone = self.tracking.get("completedMilestones", None)
            if completedMilestone is None:
                completedMilestone = {}
                self.tracking["completedMilestones"] = completedMilestone
            milestoneTrackingDetails[trackId]["status"] = "COMPLETED"
            completedMilestone[trackId] = milestoneTrackingDetails[trackId]
            print("pop")
            milestoneTrackingDetails.pop(trackId)
        if status == "INPROGRESS":
            milestoneTrackingDetails[trackId]["intermediateDate"] = self.timeStampNow()
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
        if status == "ERROR":
            milestoneTrackingDetails[trackId]["status"] = "ERROR"
            retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
            milestoneTrackingDetails[trackId]["retryCount"] = retry_count + 1
        self.updateTrackingJson(self.tracking)
        
        
if __name__ == "__main__":
    ElasticSearchAgent()
