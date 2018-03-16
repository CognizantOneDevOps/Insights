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
Created on Jul 6, 2017

@author: 463188
'''
# Optimization and Pagination might be required. This is the first cut working agent with incremental fetch

from dateutil import parser
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class RallyAgent(BaseAgent):
    def process(self):
        accesstoken = self.config.get("accesstoken", '')
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        proxy = self.config.get("proxy", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%S')
        responseTemplate = self.getResponseTemplate()
        since = self.tracking.get('lastupdated',None)
        if since == None:
            lastUpdated = startFrom
        else:
            lastUpdated = since
        headers = {
            'zsessionid': accesstoken,
            'content-type': "application/json"
        }
        proxies = {
            "http": "http://"+userid+":"+passwd+"@"+ proxy,
            "https": "http://"+userid+":"+passwd+"@"+ proxy
        }
        hierachiesUrl = baseUrl+"hierarchicalrequirement?query=(lastUpdateDate > "+lastUpdated+")&fetch=lastUpdateDate,name&order=lastUpdateDate desc"
        hierachies = self.getResponse(hierachiesUrl, 'GET', userid, passwd, None, reqHeaders=headers, proxies=proxies)
        data = []
        for hierarchy in hierachies["QueryResult"]["Results"]:
            injectData = {}
            # inject data is used as sample here update with actual values needed if any
            injectData['TestInject'] = "TestValue"
            data += self.parseResponse(responseTemplate, hierarchy, injectData)
        
        fromDateTime = data[0]['lastUpdateDate']
        if len(hierachies)>0:
            self.tracking["lastupdated"] = fromDateTime
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    RallyAgent()       
