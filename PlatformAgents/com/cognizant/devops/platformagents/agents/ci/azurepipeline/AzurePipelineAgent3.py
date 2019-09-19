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
Created on 12 April 2017

@author: 446620
'''
from ....core.BaseAgent3 import BaseAgent

class AzurePipelineAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.config.get("userID", '')
        Passwd = self.config.get("passwd", '')
        collectionName = self.config.get("collectionName", '')
        getProjectUrl = BaseUrl+ "/" + collectionName +"/_apis/projects"
        responseTemplate = self.getResponseTemplate()
        data = []
        projects = self.getResponse(getProjectUrl, 'GET', UserID, Passwd, None)
        projCount = projects["count"]
        for project in range(projCount):
            injectData = {}
            projectName = projects["value"][project]["name"]
            injectData['projectName'] = projectName
            getBuildsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/build/builds"
            builds = self.getResponse(getBuildsUrl, 'GET', UserID, Passwd, None)
            bCount = builds["count"]
            startFrom=self.tracking.get(projectName,None)
            if startFrom != None:
                startFrom = startFrom+1
            else:
                startFrom = 1
            for buildIterator in range(0, bCount):
                buildDetail = builds['value'][buildIterator]
                if buildDetail['id'] > startFrom:
                    data += self.parseResponse(responseTemplate, buildDetail, injectData)
            self.tracking[projectName] = buildDetail['id']
        if data != []:
            self.publishToolsData(data)
            self.updateTrackingJson(self.tracking)
			
if __name__ == "__main__":
    AzurePipelineAgent()
