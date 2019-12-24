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
Created on Jun 22, 2016

@author: 463188
'''
from dateutil import parser
from ....core.BaseAgent import BaseAgent
from urllib import quote
import time

class CircleAgent(BaseAgent):
    def process(self):
        userid = self.getCredential("userid")
        passwd = self.getCredential("passwd")
        BaseUrl = self.config.get("baseUrl", '')
        Project = self.config.get("project", '')
        token = self.config.get("token", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%S')
        responseTemplate = self.getResponseTemplate()
        since = self.tracking.get('lastupdated',None)
        if since == None:
            lastUpdated = startFrom
        else:
            lastUpdated = since
        Url = BaseUrl+Project+"?circle-token="+token
        Response = self.getResponse(Url, 'GET', userid, passwd, None)
        data = []
        for url in Response:
            injectData = {}
            date = url['queued_at']
            if since == None or date > since:
                for index in url["all_commit_details"]:
                    injectData['Commit'] = index['commit']
                    injectData['Commit_url'] = index['commit_url']
                fromDateTime = Response[0]['queued_at']
                data += self.parseResponse(responseTemplate, url, injectData)
            else:
                fromDateTime = Response[0]['queued_at']
        if len(Response)>0:
            self.tracking["lastupdated"] = fromDateTime
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    CircleAgent()