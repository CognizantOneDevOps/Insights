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
Created on 06 February 2020

@author: 668284
'''
from ....core.BaseAgent import BaseAgent

class TFSCIAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        Auth = self.config.get("auth", '')
        getCollectionsUrl = BaseUrl+"/_apis/_commom/GetJumpList?showTeamsOnly=false&__v=5&navigationContextPackage={}&showStoppedCollections=false"
        collectionResponse = self.getResponse(getCollectionsUrl, 'GET', UserID, Passwd, None, authType=Auth)
        collections = collectionResponse.get("__wrappedArray")
        responseTemplate = self.getResponseTemplate()
        data = []
        colCount = len(collections)
        for collection in range(colCount):            
            collectionName = collections[collection]["name"]
            getProjectsUrl = BaseUrl + "/" + collectionName +"/_apis/projects?api-version=4.1"
            projects = self.getResponse(getProjectsUrl, 'GET', UserID, Passwd, None, authType=Auth)
            projCount = projects["count"]
            for project in range(projCount):
                injectData = {}                
                projectName = projects["value"][project]["name"]
                injectData['collectionName'] = collectionName
                injectData['projectName'] = projectName
                newProject = False
                if not self.tracking.get(collectionName + "/" + projectName, None):
                    getBuildsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/build/builds"\
                    "?queryOrder=finishTimeAscending&$top=100&api-version=4.1"
                    newProject = True
                else:
                    lastBuildTime = self.tracking.get(collectionName + "/" + projectName, None)
                    getBuildsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/build/builds"\
                    "?queryOrder=finishTimeAscending&minTime=" + str(lastBuildTime) + "&$top=100&api-version=4.1"
                builds = self.getResponse(getBuildsUrl, 'GET', UserID, Passwd, None, authType=Auth)
                bCount = builds["count"]
                if not newProject and bCount > 0:
                    builds["value"].pop(0);
                    bCount = bCount - 1
                for build in range(bCount):
                    buildDetail = builds["value"][build]
                    data += self.parseResponse(responseTemplate, buildDetail, injectData)
                    self.tracking[collectionName + "/" + projectName] = builds["value"][build]["finishTime"]
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    TFSCIAgent()
