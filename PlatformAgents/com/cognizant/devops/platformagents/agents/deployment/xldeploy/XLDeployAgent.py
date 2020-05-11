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
Created on May 09, 2020

@author: 368419
'''

import json
import datetime
import logging
from datetime import timedelta
from ....core.BaseAgent import BaseAgent

class XLDeployAgent(BaseAgent):
    def process(self):
        baseEndPoint = self.config.get("baseEndPoint", '')
        userID = self.config.get("userID", '')
        passwd = self.config.get("passwd", '')
        startFrom = self.config.get("startFrom", '')
        beginDate = self.tracking.get("startDate", startFrom)
        
        listtasksurl = baseEndPoint + "/task/query?begindate=" + beginDate                
        tasks = self.getResponse(listtasksurl, 'GET', userID, passwd, None)
        
        data = []
        metadata ={"labels" : ["XLDEPLOY_TASKS"],"dataUpdateSupported" : True,"uniqueKey" : ["taskId"]}
        latestDate = beginDate
        for task in tasks:
                    
            if task["metadata"]["taskType"]=="UPGRADE" or task["metadata"]["taskType"]=="INITIAL":
                injectData = {}
                if len(task["metadata"]["application"]) >= 1:               
                   injectData['application_name'] = task["metadata"]["application"]   
                
                injectData['version'] = task["metadata"]["version"]  
                injectData['taskType'] = task["metadata"]["taskType"]  
                injectData['environment_id'] = task["metadata"]["environment_id"]  
                injectData['state'] = task.get("state")
                injectData['startDate'] = task["startDate"]
                if latestDate == None:
                    latestDate =task["startDate"]
                elif task["startDate"] > latestDate: 
                    latestDate = task["startDate"]
                    
                injectData['completionDate'] = task["completionDate"]
                injectData['user'] = task.get("owner")  
                injectData['taskId'] = task.get("id")
                injectData['failures'] = task.get("failures")
                injectData['state2'] = task.get("state2")                
                data.append(injectData)
                
        latestDate= latestDate.split("T")[0]           
        
        self.tracking["startDate"] = latestDate
        self.publishToolsData(data,metadata)
        self.updateTrackingJson(self.tracking)
        
if __name__ == "__main__":
    XLDeployAgent()