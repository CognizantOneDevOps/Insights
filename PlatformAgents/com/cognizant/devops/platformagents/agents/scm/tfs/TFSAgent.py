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
Created on 10 April 2017

@author: 446620
'''
from ....core.BaseAgent import BaseAgent

class TFSAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
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
                newProject = False                
                if not self.tracking.get(collectionName + "/" + projectName,None):
                    #getChangesetsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/tfvc/changesets/?$orderBy=id asc"
                    getChangesetsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/tfvc/changesets"
                    newProject = True
                else:
                    lastID = self.tracking.get(collectionName+ "/" + projectName,None)
                    getChangesetsUrl = BaseUrl + "/" + collectionName + "/" + projectName + "/_apis/tfvc/changesets?fromId=" + str(lastID)                    
                #print(getChangesetsUrl)
                changesets = self.getResponse(getChangesetsUrl, 'GET', UserID, Passwd, None, authType=Auth)
                #print(changesets)
                csCount = changesets["count"]
                #print(csCount)
                if not newProject:
                    csCount = csCount-1
                for changeset in range(csCount):
                    changesetDetail = changesets["value"][changeset]
                    #print(changesetDetail)
                    data += self.parseResponse(responseTemplate, changesetDetail, injectData)
                    #getChangesetDetailsUrl = BaseUrl + "/" +collectionName + "/" + projectName + "/_apis/tfvc/changesets/" + str(changesets["value"][changeset]["changesetId"])
                    #changesetDetails = self.getResponse(getChangesetDetailsUrl, 'GET', UserID, Passwd, None, authType=Auth)
                    #print(changesetDetails)
                self.tracking[collectionName + "/" + projectName] = changesets["value"][0]["changesetId"]
        #print(data)
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    TFSAgent()
