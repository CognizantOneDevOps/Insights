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
Created on May 31, 2017

@author: 446620
'''
import json
import datetime
from time import mktime
from dateutil import parser

from ....core.BaseAgent import BaseAgent

class UrbanCodeDeployAgent(BaseAgent):
    def process(self):
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        reportType = self.config.get("reportType", '')
        startFrom = self.config.get("startFrom", '')
        
        timeNow = datetime.datetime.now()
        timeNow = long((mktime(timeNow.timetuple()) + timeNow.microsecond/1000000.0) * 1000)
        #print(timeNow)
        if not self.tracking.get("lastUpdated",None):
            startFrom = parser.parse(self.config.get("startFrom", ''))
            startFrom = long((mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0) * 1000)
        else:
            startFrom = self.tracking.get("lastUpdated", None)
        #print(startFrom)
        ucdUrl = baseUrl+"/rest/report/adHoc?dateRange=custom&date_low="+str(startFrom)+"&date_hi="+str(timeNow)+"&orderField=date&sortType=desc&type="+str(reportType)
        #print(ucdUrl)        
        response = self.getResponse(ucdUrl, 'GET', userid, passwd, None)
        #print(response["items"][0])
        data = []
        responseTemplate = self.getResponseTemplate()
        for item in range(len(response["items"][0])):
            data += self.parseResponse(responseTemplate, response["items"][0][item])
        #print(json.dumps(data, indent=2))
        self.tracking["lastUpdated"] = timeNow
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    UrbanCodeDeployAgent()
