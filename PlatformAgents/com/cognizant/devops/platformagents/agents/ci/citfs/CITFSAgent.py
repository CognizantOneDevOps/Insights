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
from ....core.BaseAgent import BaseAgent

class CITFSAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.config.get("userID", '')
        Passwd = self.config.get("passwd", '')
        Auth = self.config.get("auth", '')
        getCollectionsUrl = BaseUrl+"/_apis/projectcollections"
        collections = self.getResponse(getCollectionsUrl, 'GET', UserID, Passwd, None, authType=Auth)
        #print(collections)
        responseTemplate = self.getResponseTemplate()
        data = []
        colCount = collections["count"]
        for collection in range(colCount):            
            collectionName = collections["value"][collection]["name"]
            getProjectsUrl = BaseUrl + "/" +collectionName+"/_apis/projects/"
            projects = self.getResponse(getProjectsUrl, 'GET', UserID, Passwd, None, authType=Auth)
            #print(projects)
            projCount = projects["count"]
            for project in range(projCount):
                injectData = {}                
                projectName = projects["value"][project]["name"]
                injectData['collectionName'] = collectionName
                injectData['projectName'] = projectName
                #print(collectionName + "/" + projectName)
                getBuildsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/build/builds"
                #print(getBuildsUrl)
                builds = self.getResponse(getBuildsUrl, 'GET', UserID, Passwd, None, authType=Auth)
                #print(builds)
                bCount = builds["count"]
                #print(bCount)
                for build in range(bCount):
                    buildDetail = builds["value"][build]
                    #print(buildDetail)
                    data += self.parseResponse(responseTemplate, buildDetail, injectData)
                    #getBuildDetailsUrl = BaseUrl + "/" +collectionName + "/" + projectName + "/_apis/build/builds/" + str(changesets["value"][build]["id"])
                    #buildDetails = self.getResponse(getBuildDetailsUrl, 'GET', UserID, Passwd, None, authType=Auth)
                    #print(buildDetails)
                #self.tracking[collectionName + "/" + projectName] = builds["value"][0]["changesetId"]
        #print(data)
        self.publishToolsData(data)
        #self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    CITFSAgent()
