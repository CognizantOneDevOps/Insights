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
Created on Jun 23, 2016

@author: 463188
'''
from time import mktime
from dateutil import parser
from ....core.BaseAgent import BaseAgent

class RundeckAgent(BaseAgent):
    def process(self):
        getProjects = self.config.get("baseEndPoint", '')
        authtoken = self.getCredential("authtoken")
        ExecutionsBaseEndPoint = self.config.get("executionsBaseEndPoint", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        startFrom = long(startFrom * 1000)
        startFrom = str(startFrom)
        getProjectsUrl = getProjects+"?authtoken="+authtoken
        projects = self.getResponse(getProjectsUrl, 'GET', None, None, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        for project in range(len(projects)):
            ProjName = projects[project]["name"]
            if not self.tracking.get(ProjName, ''):
                getProjectDetailsUrl = ExecutionsBaseEndPoint+"/"+ProjName+"/executions?authtoken="+authtoken+"&begin="+startFrom
            else:
                TimeStamp = self.tracking.get(ProjName, '')
                TimeStamp = str(TimeStamp)
                getProjectDetailsUrl = ExecutionsBaseEndPoint+"/"+ProjName+"/executions?authtoken="+authtoken+"&begin="+TimeStamp
            rundeckProjectDetails = self.getResponse(getProjectDetailsUrl, 'GET', None, None, None)
            for executions in rundeckProjectDetails["executions"]:
                data += self.parseResponse(responseTemplate, executions)
                self.tracking[ProjName] = rundeckProjectDetails["executions"][0]["date-ended"]["unixtime"] + 1
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    RundeckAgent()
