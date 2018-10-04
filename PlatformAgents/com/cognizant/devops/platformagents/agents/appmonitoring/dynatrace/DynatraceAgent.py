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
Created on Sep 24, 2018

@author: Vishwajit Mankar & Gaurav Deshmukh
'''
from dateutil import parser
import time
import sys
import os
from datetime import datetime, timedelta
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging.handlers

class DynatraceAgent(BaseAgent):
    def process(self):
        print("Inside process")
        try:
            self.apiToken = self.config.get("apiToken", None)
            self.start = self.tracking.get('lastFetchTime',None)
            self.end = int(time.time()*1000.0)
            trackingDetails = {}
            data = []
            if self.start is None:
                startFrom = self.config.get("startFrom", None)
                startFrom = parser.parse(startFrom)
                startFrom = time.mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
                self.start = long(startFrom * 1000)
            
            print(self.start)
            print(self.end)
            self.responseTemplate = self.getResponseTemplate()
            self.getHostsDetails(data)
            trackingDetails['lastFetchTime'] = self.end
            #print(data)
            self.publishToolsData(data)
            self.updateTrackingJson(trackingDetails)
        except Exception as e:
            logging.error(e)
            
    def getHostsDetails(self, data):
        hostInfo = self.responseTemplate.get('Host')
        hostURL = hostInfo.get('hostListURL')
        gethostUrlWithToken = hostURL+"?Api-Token="+self.apiToken+'&startTimestamp='+str(self.start)+'&endTimestamp='+str(self.end)+'&per_page=100&sort=created&page=1'
        serverHosts = self.getResponse(gethostUrlWithToken, 'GET', None, None, None)
        fieldToPull = hostInfo.get('relevantHostFields')
        for host in serverHosts:
            hostData = self.parseResponse(fieldToPull, host)
            print(hostData.get('entityId'))
            eventData = getHostEventDetails(hostData.get('entityId'))
            data += hostData
        
        return data
    
    def getHostEventDetails(self,entityId):
        eventInfo = self.responseTemplate.get('Event')
        eventURL = entityId.get('eventListURL')
        getEventUrlWithToken = eventURL+"?Api-Token="+self.apiToken+'&startTimestamp='+str(self.start)+'&endTimestamp='+str(self.end)+'&entityId='+entityId+'&per_page=100&sort=created&page=1'
        hostEvents = self.getResponse(getEventUrlWithToken, 'GET', None, None, None)
        fieldToPull = eventInfo.get('relevantEventFields')
        
        
if __name__ == "__main__":
    DynatraceAgent()
