# -------------------------------------------------------------------------------
# Copyright 2020 Cognizant Technology Solutions
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
# -------------------------------------------------------------------------------
'''
Created on 31 May 2020

@author: 302683
'''
from ....core.BaseAgent import BaseAgent


class AzurePipelineAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        collectionName = self.config.get("collectionName", '')
        isStartFromDateEnabled = self.config.get("isStartFromDateEnabled", False)
        startFromDate = self.config.get("startFromDate", None)
        buildMetadata = {
            "dataUpdateSupported": True,
            "uniqueKey": ["buildId"]
        }
        responseTemplate = self.getResponseTemplate()
        getProjectUrl = BaseUrl + "/" + collectionName + "/_apis/projects?api-version=5.1"
        data = []
        projects = self.getResponse(getProjectUrl, 'GET', UserID, Passwd, None)
        projCount = projects["count"]
        for project in range(projCount):
            injectData = {}
            projectName = projects["value"][project]["name"]
            injectData['projectName'] = projectName
            injectData['isNodeUpdated'] = True
            buildsURL = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/build/builds"
            getBuildsUrl = buildsURL + "?api-version=5.1&queryOrder=finishTimeAscending"
            startFrom = self.tracking.get(projectName, None)
            additionalQueryParameter = ''
            if startFrom is not None:
                additionalQueryParameter = '&minTime=' + startFrom
            elif isStartFromDateEnabled and startFromDate is not None:
                additionalQueryParameter = "&minTime=" + startFromDate
            builds = self.getResponse(getBuildsUrl + additionalQueryParameter, 'GET', UserID, Passwd, None)
            buildCount = builds["count"]
            for buildIterator in range(0, buildCount):
                buildDetail = builds['value'][buildIterator]
                data += self.parseResponse(responseTemplate, buildDetail, injectData)
            self.tracking[projectName] = buildDetail['finishTime']
            if data:
                self.publishToolsData(data, buildMetadata)
                self.updateTrackingJson(self.tracking)
                data = []


if __name__ == "__main__":
    AzurePipelineAgent()
