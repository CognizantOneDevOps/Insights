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
Created on Jul 16, 2021

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
import xml.etree.ElementTree as ET
import threading

class SplunkAgent(BaseAgent):
    lock = threading.Lock()
    
    @BaseAgent.timed
    def processROIAgent(self, data):
        self.baseLogger.info(" inside SplunkAgent process ======")
        self.timeStampNow = lambda: datetime.utcnow().strftime("%m/%d/%Y:%H:%M:%S")
        eventDetails = json.loads(data)
        milestoneId = eventDetails.get("milestoneId", None)
        milestoneName = eventDetails.get("milestoneName", None)
        milestoneReleaseId = eventDetails.get("milestoneReleaseId", None)
        startDate = eventDetails.get("startDate", None)
        endDate = eventDetails.get("endDate", None)
        outcomeName = eventDetails.get("outcomeName", None)
        outcomeId = eventDetails.get("outcomeId", None)
        formattedStartDate = datetime.fromtimestamp(startDate).strftime("%m/%d/%Y:%H:%M:%S")
        formattedEndDate = datetime.fromtimestamp(endDate).strftime("%m/%d/%Y:%H:%M:%S")
        statusQueue = eventDetails.get("statusQueue", None)
        toolConfig = json.loads(eventDetails.get("configJson", {}))
        indexName = toolConfig.get("splunkIndex", None)
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
                "statusQueue": statusQueue,
                "indexName": indexName,
                "lastUpdatedDate": self.timeStampNow(),
                "status": "NOT_STARTED",
                "milestoneReleaseId": milestoneReleaseId
                }
        
        if trackId in milestoneTrackingDetails and milestoneTrackingDetails[trackId]["status"] == "ERROR":
            milestoneTrackingDetails[trackId]["indexName"] = indexName
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
            milestoneTrackingDetails[trackId]["retryCount"] = 0
        else:
            milestoneTrackingDetails[trackId] = trackingDetails
        self.updateTrackingJson(self.tracking)
        self.process()
     
            
    @BaseAgent.timed 
    def process(self):  
        self.baseLogger.info("=== INSIDE process ==")
        with self.lock:
            roiUtilities = ROIUtilities(self.messageFactory, self.tracking, self.trackingFilePath, self.config)
            self.timeStampNow = lambda: datetime.utcnow().strftime("%m/%d/%Y:%H:%M:%S")
            milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
            if milestoneTrackingDetails:
                milestoneDetails = milestoneTrackingDetails.copy()
                for item in milestoneDetails:
                    try:
                        itemDetails = milestoneDetails[item]
                        milestoneId = itemDetails.get("milestoneId",None)
                        outcomeId = itemDetails.get("outcomeId",None)
                        trackId = str(milestoneId)+"_"+str(outcomeId)
                        if itemDetails["status"] == "INPROGRESS":
                            lastUpdatedDate = datetime.strptime(itemDetails["lastUpdatedDate"], "%m/%d/%Y:%H:%M:%S")
                            date = lastUpdatedDate + timedelta(seconds=self.runSchedule * 60)
                            if self.timeStampNow() < date.strftime("%m/%d/%Y:%H:%M:%S"):
                                continue
                        if self.timeStampNow() > itemDetails["startDate"] and self.timeStampNow() > itemDetails["endDate"]:
                            self.fetchOutcomeData(itemDetails)
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "SUCCESS", "completed")
                            roiUtilities.updateStatusInTracking("COMPLETED", trackId)
                        elif self.timeStampNow() > itemDetails["startDate"] and self.timeStampNow() < itemDetails["endDate"]:
                            self.fetchOutcomeData(itemDetails)
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "INPROGRESS", "inprogress")
                            roiUtilities.updateStatusInTracking("INPROGRESS", trackId)
                        self.updateTrackingJson(self.tracking) 
                    except Exception as ex:
                        print(" process exception: ",ex)
                        self.baseLogger.error(" error occurred while fetching outcome data: "+str(ex))
                        roiUtilities.updateStatusInTracking("ERROR", trackId)
                        retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
                        if retry_count >= 3:
                            roiUtilities.publishROIAgentstatus(self.messageFactory, itemDetails, "ERROR", str(ex))
    
        
    def fetchOutcomeData(self, outcomeDetails): 
        self.baseLogger.info("inside fetchOutcomeData")
        self.baseUrl = self.config.get("baseUrl", None)
        self.username = self.config.get("userName", None)
        self.password = self.config.get("password", None)
        self.headers = {"Accept":"application/xml"} 
        endDate = outcomeDetails.get("endDate",None)
        startDate = outcomeDetails.get("intermediateDate",None)
        if self.timeStampNow() < endDate:
            endDate = self.timeStampNow()
        if startDate is None:
            startDate = outcomeDetails.get("startDate",None)
        query = "search=search index="+outcomeDetails["indexName"]+" earliest="+str(startDate)+" latest="+str(endDate)+"&output_mode=json&exec_mode=oneshot"
#         searchResponse = self.getResponse(self.baseUrl,'POST', self.username, self.password, data=query, reqHeaders=self.headers)
#         responseXml = ET.fromstring(searchResponse)
#         search_id = str(responseXml[0].text)
#         self.baseLogger.info(" splunk outcome search id ======"+search_id)
#         url = self.baseUrl+'/'+search_id
#         time.sleep(15)
#         timeout = time.time() + 15
#         while True:
#             if time.time() > timeout:
#                 response = self.getResponse(url,'GET', self.username, self.password, None, reqHeaders=self.headers)
#                 xmlRes = ET.fromstring(response)
#                 state = xmlRes.find(".//*[@name='dispatchState']").text
#                 print("state",state)
#                 if state == 'DONE' or state == 'FAILED':
#                     break
#             else:
#                 break
#             
#         if state == 'DONE':
#             metadata = self.config.get("dynamicTemplate", {}).get("metadata", {})
#             resultUrl = self.baseUrl+'/'+search_id+'/results'
#             outputMode = "output_mode=json"
#             resultResponse = self.getResponse(resultUrl,'GET', self.username, self.password, data=outputMode, reqHeaders=self.headers).decode('utf-8')
#             jsonRes = json.loads(resultResponse)
#             results = jsonRes.get("results", list())
#             print("results")
#             if len(results) == 0:
#                 self.baseLogger.error("SplunkAgent: no results found for index name: "+outcomeDetails["indexName"])
#                 raise ValueError("SplunkAgent: no results found for index name: "+outcomeDetails["indexName"])
#             else:
#                 outcomeMetadata = {"milestoneId": outcomeDetails["milestoneId"], 
#                             "milestoneName": outcomeDetails["milestoneName"],
#                             "outcomeName": outcomeDetails["outcomeName"],
#                             "outcomeId": outcomeDetails["outcomeId"]}
#                 for res in results:
#                     res.update(outcomeMetadata.copy())
#                 self.publishToolsData(results, metadata)
#         else:
#             self.baseLogger.error(" incorrect dispatch state for index name: "+outcomeDetails["indexName"])
#             raise ValueError('SplunkAgent: incorrect dispatch state for index name: '+outcomeDetails["indexName"])
        
        resultUrl = self.baseUrl+'/export'
        resultResponse = self.getResponse(resultUrl,'POST', self.username, self.password, data=query, reqHeaders=self.headers).decode('utf-8')
        if "result" not in resultResponse:
            self.baseLogger.error("SplunkAgent: no results returned, please check input parameters")
            raise ValueError("SplunkAgent: no results returned, please check input parameters")
        else:
            count = resultResponse.count("}}") - 1
            resultResponse = resultResponse.replace("}}", "}},", count)
            results = '[' + resultResponse + ']'
            #print(results)
            formattedResults = json.loads(results)
            finalData = []
            outcomeMetadata = {"milestoneId": outcomeDetails["milestoneId"], 
                            "milestoneName": outcomeDetails["milestoneName"],
                            "milestoneReleaseId": outcomeDetails["milestoneReleaseId"],
                            "outcomeName": outcomeDetails["outcomeName"],
                            "outcomeId": outcomeDetails["outcomeId"]}
            for item in formattedResults:
                result = item.get("result", {})
                result.update(outcomeMetadata.copy())
                finalData.append(result)
            metadata = self.config.get("dynamicTemplate", {}).get("metadata", {})
            self.publishToolsData(finalData, metadata)
        
    
    def updateStatusInTracking(self, status, trackId):
        self.baseLogger.info("inside updateStatusInTracking", status)
        milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
        milestoneTrackingDetails[trackId]["lastUpdatedDate"] = self.timeStampNow()
        if status == "COMPLETED":
            completedMilestone = self.tracking.get("completedMilestones", None)
            if completedMilestone is None:
                completedMilestone = {}
                self.tracking["completedMilestones"] = completedMilestone
            if trackId in milestoneTrackingDetails:
                milestoneTrackingDetails[trackId]["status"] = "COMPLETED"
                completedMilestone[trackId] = milestoneTrackingDetails[trackId]
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
    SplunkAgent()
