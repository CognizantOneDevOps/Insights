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
from http.cookiejar import logger
'''
Created on Sep 24, 2018

@author: Vishwajit Mankar & Gaurav Deshmukh
'''
from dateutil import parser
import time
import sys
import os
from datetime import datetime, timedelta
from ....core.BaseAgent3 import BaseAgent
import logging.handlers
import json

class DynatraceAgent(BaseAgent):
    def process(self):
        print("Inside process")
        try:
            self.headers = {"Accept":"application/json","charset":"utf-8"}
            self.apiToken = self.getCredential("apiToken")
            self.start = self.tracking.get('lastFetchTime',None)
            self.end = int(time.time()*1000.0)
            trackingDetails = {}
            self.data = []
            if self.start is None:
                startFrom = self.config.get("startFrom", None)
                startFrom = parser.parse(startFrom)
                startFrom = time.mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
                self.start = int(startFrom * 1000)
            self.responseTemplate = self.getResponseTemplate()
            self.getHostsDetails()
            trackingDetails['lastFetchTime'] = self.end
            self.publishToolsData(self.data)
            self.updateTrackingJson(trackingDetails)
        except Exception as e:
            logging.error(e)
            
    def getHostsDetails(self):
        hostInfo = self.responseTemplate.get('Host')
        hostURL = hostInfo.get('hostListURL')
        gethostUrlWithToken = hostURL+"?Api-Token="+self.apiToken+'&startTimestamp='+str(self.start)+'&endTimestamp='+str(self.end)+'&per_page=100&sort=created&page=1'
        serverHosts = self.getResponse(gethostUrlWithToken, 'GET', None, None, None,reqHeaders=self.headers)
        fieldToPull = hostInfo.get('relevantHostFields')
        for host in serverHosts:
            hostDataArry = self.parseResponse(fieldToPull, host)
            hostData = hostDataArry[0]
            self.getHostEventDetails(hostData)
        
    
    def getHostEventDetails(self, hostData):
        a=0
        eventInfo = self.responseTemplate.get('Event')
        eventURL = eventInfo.get('eventListURL')
        getEventUrlWithToken = eventURL+"?Api-Token="+self.apiToken+'&startTimestamp='+str(self.start)+'&endTimestamp='+str(self.end)+'&entityId='+hostData.get('entityId')+'&per_page=100&sort=created&page=1'
        hostEvents = self.getResponse(getEventUrlWithToken, 'GET', None, None, None,reqHeaders=self.headers)
        fieldToPull = eventInfo.get('relevantEventFields')
        eventData={}
        eventhostData={}
        events = hostEvents.get('events')
        for event in events:
            eventDataList = self.parseResponse(fieldToPull, event)
            eventData = eventDataList[0]
            eventhostData = dict(list(eventData.items())+ list(hostData.items()))
            self.data.append(eventhostData)
        return self.data
        
if __name__ == "__main__":
    DynatraceAgent()
