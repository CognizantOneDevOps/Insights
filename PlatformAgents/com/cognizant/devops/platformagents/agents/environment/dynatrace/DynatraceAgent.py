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
            startFrom = self.config.get("startFrom", None)
            startFrom = parser.parse(startFrom)
            self.responseTemplate = self.getResponseTemplate()
            self.getHostsDetails()
        except Exception as e:
            logging.error(e)
            
        tracking_data = []
        
        if tracking_data!=[]:
            self.publishToolsData(tracking_data)
        
    def getHostsDetails(self):
        hostInfo = self.responseTemplate.get('hostInfo')
        print(hostInfo)
        hostURL = hostInfo.get('hostListURL')
        gethostUrlWithToken = hostURL+"?Api-Token="+self.apiToken
        serverHosts = self.getResponse(gethostUrlWithToken+'&per_page=100&sort=created&page=1', 'GET', None, None, None)
        print(serverHosts)
        fieldToPull = hostInfo.get('relevantHostFields')
        data = []
        for host in serverHosts:
            data += self.parseResponse(fieldToPull, host)
        
        print(data)
if __name__ == "__main__":
    DynatraceAgent()
