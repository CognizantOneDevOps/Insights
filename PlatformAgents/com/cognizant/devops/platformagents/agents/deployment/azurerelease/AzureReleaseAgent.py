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
Created on 27 May 2020

@author: 302683
'''
from ....core.BaseAgent import BaseAgent


class AzureReleaseAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        collectionName = self.config.get("collectionName", '')
        isStartFromDateEnabled = self.config.get("isStartFromDateEnabled", False)
        startFromDate = self.config.get("startFromDate", None)
        APIPageSize = self.config.get("APIPageSize", None)
        deploymentMetadata = {
            "dataUpdateSupported": True,
            "uniqueKey": ["deploymentId"]
        }
        responseTemplate = self.getResponseTemplate()
        getProjectUrl = BaseUrl + "/" + collectionName + "/_apis/projects?api-version=5.1"
        minStartedDate = ''
        if isStartFromDateEnabled and startFromDate is not None:
            minStartedDate = "&minStartedTime=" + str(startFromDate)
        data = []
        projects = self.getResponse(getProjectUrl, 'GET', UserID, Passwd, None)
        projCount = projects["count"]
        for project in range(projCount):
            injectData = {}
            projectName = projects["value"][project]["name"]
            injectData['projectName'] = projectName
            injectData['isNodeUpdated'] = True
            getDeploymentsUrl = 'https://vsrm.dev.azure.com' + "/" + collectionName + "/" + projectName \
                                + "/_apis/release/deployments?api-version=5.1&queryOrder=ascending&$top=" \
                                + str(APIPageSize)
            getDeploymentsUrl += minStartedDate
            startFrom = self.tracking.get(projectName, None)
            additionalQueryParameter = ''
            if startFrom is not None:
                additionalQueryParameter = '&minModifiedTime=' + startFrom
            deployments = self.getResponse(getDeploymentsUrl + additionalQueryParameter, 'GET', UserID, Passwd, None)
            deploymentCount = deployments["count"]
            while deploymentCount > 0:
                for deploymentIterator in range(0, deploymentCount):
                    deploymentDetail = deployments['value'][deploymentIterator]
                    data += self.parseResponse(responseTemplate, deploymentDetail, injectData)
                self.tracking[projectName] = deploymentDetail['lastModifiedOn']
                if data:
                    self.publishToolsData(data, deploymentMetadata)
                    self.updateTrackingJson(self.tracking)
                    data = []
                if deploymentCount < APIPageSize:
                    break
                else:
                    startFrom = self.tracking.get(projectName, None)
                    additionalQueryParameter = ''
                    if startFrom is not None:
                        additionalQueryParameter = '&minModifiedTime=' + startFrom
                    deployments = self.getResponse(getDeploymentsUrl + additionalQueryParameter, 'GET', UserID, Passwd,
                                                   None)
                    deploymentCount = deployments["count"]


if __name__ == "__main__":
    AzureReleaseAgent()
