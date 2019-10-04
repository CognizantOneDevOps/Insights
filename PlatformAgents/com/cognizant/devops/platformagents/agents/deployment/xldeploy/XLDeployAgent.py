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
Created on Oct 13, 2016

@author: 463188
'''

import json
import datetime
from datetime import timedelta
from ....core.BaseAgent import BaseAgent

class XLDeployAgent(BaseAgent):
    def process(self):
        BaseEndPoint = self.config.get("baseEndPoint", '')
        UserId = self.config.get("userID", '')
        Passwd = self.config.get("passwd", '')
        begindate = self.tracking.get("begindate", '')
        listtasksurl = BaseEndPoint+"/tasks/v2/export?begindate="+begindate
        try:
            tasks = self.getResponse(listtasksurl, 'GET', UserId, Passwd, None)
            data = []
            responseTemplate = self.getResponseTemplate()
            for task in tasks:
                if task["metadata"]["taskType"]=="UPGRADE" or task["metadata"]["taskType"]=="INITIAL":
                    appverurl = BaseEndPoint+"/repository/ci/Applications/"+task["metadata"]["application"]+"/"+task["metadata"]["version"]
                    try:
                        appdetails = self.getResponse(appverurl, 'GET', UserId, Passwd, None)
                    except:
                        pass
                    data += self.parseResponse(responseTemplate, appdetails)
            trackdate = datetime.date.today() + timedelta(days=1)
            trackdate = trackdate.strftime('%Y-%m-%d')
            self.tracking["begindate"] = trackdate
            self.updateTrackingJson(self.tracking)
            self.publishToolsData(data)
        except:
            pass
if __name__ == "__main__":
    XLDeployAgent()
