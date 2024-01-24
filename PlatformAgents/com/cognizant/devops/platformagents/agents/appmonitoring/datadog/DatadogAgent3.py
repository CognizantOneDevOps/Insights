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
Created on Jan 05, 2024

@author: 911215 & 2116386
'''
import warnings
import sys
import requests
from datetime import datetime as dateTime2
import datetime
from ....core.BaseAgent3 import BaseAgent
import json

class DatadogAgent(BaseAgent):
    warnings.filterwarnings('ignore')

    @BaseAgent.timed
    def process(self):
        self.baseLogger.info('Inside process')
        try:
            self.BaseUrl = self.config.get("baseUrl", '')
            self.apiKey = self.getCredential("apiKey")
            self.applicationKey = self.getCredential("applicationKey")
            self.tracking_time={}
            self.tracking_time["event"]= {"lastUpdatedTime": None}
            self.eventStartTime = self.tracking.get("event", {}).get("lastUpdatedTime", None)
            if not self.eventStartTime :
                self.eventStartTime = self.config.get("startFrom", '')   
            self.eventLastUpdatedTime = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
            self.requiredMonitorTags = self.config.get('dynamicTemplate', {}).get('monitorMetaData', {}).get("tags", [])
            self.eventMetaData = self.config.get('dynamicTemplate',dict()).get('eventMetaData',None) 
        
            self.processMonitorData()

        except Exception as e:
            self.baseLogger.error(e)
            

    def processMonitorData(self):
        try:
            self.monitorsToProcess = []
            monitorsUrl = self.BaseUrl + '/api/v1/monitor'
            json_headers = {"DD-API-KEY":self.apiKey,"DD-APPLICATION-KEY":self.applicationKey,"Content-Type":"application/json","Accept":"application/json"}
            monitorsResponse = requests.get(monitorsUrl, headers=json_headers )
            monitorsData = monitorsResponse.json()
            
            for monitor in monitorsData:
                requiredTagsCount = 0
                if(monitor["tags"] and len(monitor["tags"]) > 0):
                    for requiredTag in self.requiredMonitorTags:
                        for tag in monitor["tags"]:
                            if requiredTag in tag:
                                requiredTagsCount +=1
                                break
                    if(requiredTagsCount == len(self.requiredMonitorTags)):
                        monitorToProcess = {
                            "monitorId" : monitor.get("id", ""),
                            "monitorName" : monitor.get("name", ""),
                            "priority" : monitor.get("priority", ""),
                            "monitorTags" : monitor.get("tags", ""),
                            "monitorCreated" : monitor.get("created", ""),
                        }
                        for requiredTag in self.requiredMonitorTags:   
                            for tag in monitor.get("tags", []):
                                if requiredTag in tag:
                                    monitorToProcess.update({requiredTag : tag.split(":")[1]})
                                    break
                        self.monitorsToProcess.append(monitorToProcess)

            if self.monitorsToProcess != []:
                for monitor in self.monitorsToProcess:
                    self.processeEventsData(monitor)                  
        except Exception as e:
            self.baseLogger.error(e)

    
    def processeEventsData(self, monitor):
        try:
            self.allEvents = []
            timeStampFormat = '%Y-%m-%d %H:%M:%S'
            startTime=self.getRemoteDateTime(dateTime2.strptime(self.eventStartTime, timeStampFormat)).get('epochTime')
            endTime=self.getRemoteDateTime(dateTime2.strptime(self.eventLastUpdatedTime, timeStampFormat)).get('epochTime')
            eventsUrl = self.BaseUrl + '/api/v2/events?&filter[query]=@monitor_id:' + str(monitor["monitorId"]) + '&filter[from]=' + str(startTime*1000) + '&filter[to]=' + str(endTime*1000) + "&page[limit]=1000"
            json_headers = {"DD-API-KEY":self.apiKey,"DD-APPLICATION-KEY":self.applicationKey,"Content-Type":"application/json","Accept":"application/json"}
            fetchNextPage = True
            while fetchNextPage:
                eventsResponse = requests.get(eventsUrl, headers=json_headers )
                eventsData = eventsResponse.json()
                
                for event in eventsData["data"]:
                    eventAttributes = {}
                    eventAttributes = event.get("attributes", {}).get("attributes", {})

                    if(eventAttributes and event.get("attributes", {}).get("tags", None)):
                        data = {
                            "eventId" : eventAttributes.get("evt", "").get("id", ""),
                            "eventTags" : event.get("attributes", {}).get("tags", []),
                            "recoverTimeInSec" : "" if not eventAttributes.get("duration", None) else eventAttributes.get("duration", "")//1000000000,
                            "eventTitle" : eventAttributes.get("title", ""),
                            "eventStatus" : eventAttributes.get("status", ""),
                            "eventOccurDate" : event.get("attributes", {}).get("timestamp", ""),
                            "eventOccurDateEpoch" : eventAttributes.get("timestamp","")
                        }
                        data.update(monitor)
                        self.allEvents.append(data)

                if (len(eventsData["data"]) < 1000) or (not (eventsData.get("links", {}).get("next", None))):
                    fetchNextPage = False
                    break
                else:
                    eventsUrl = eventsData.get("links", {}).get("next", "")        
            
            self.publishToolsData(self.allEvents, metadata=self.eventMetaData)  
            self.tracking_time["event"]["lastUpdatedTime"]=self.eventLastUpdatedTime
            self.updateTrackingJson(self.tracking_time)

        
        except Exception as e:
            self.baseLogger.error(e)



if __name__ == "__main__":
    DatadogAgent()
