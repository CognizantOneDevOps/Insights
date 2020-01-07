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
Created on 30 March 2017

@author: 446620
'''
import json
from ....core.BaseAgent import BaseAgent

class TeamCityAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("baseUrl", '')
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        if not self.tracking.get("sinceBuild",None):
            getBuildsUrl = BaseUrl + '/httpAuth/app/rest/builds/'
        else:
            sinceBuild = self.tracking.get("sinceBuild",None)
            getBuildsUrl = BaseUrl + '/httpAuth/app/rest/builds/?sinceBuild='+ str(sinceBuild)   
        teamcityBuilds = self.getResponse(getBuildsUrl, 'GET', UserID, Passwd, None, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        buildCount = teamcityBuilds["count"]
        for build in range(buildCount):
            injectData = {}
            getBuildDetailsUrl = BaseUrl+"/httpAuth/app/rest/builds/"+ str(teamcityBuilds["build"][build]["id"])
            teamcityBuildDetails = self.getResponse(getBuildDetailsUrl, 'GET', UserID, Passwd, None)
            if "lastChanges" in teamcityBuildDetails:
                getBuildChangesUrl = BaseUrl+"/httpAuth/app/rest/changes?locator=build:"+str(teamcityBuilds["build"][build]["id"])
                teamcityBuildChanges = self.getResponse(getBuildChangesUrl, 'GET', UserID, Passwd, None)
                changeCount = teamcityBuildChanges["count"]
                version = []
                for change in range(changeCount):
                    version.append(teamcityBuildChanges["change"][change]["version"])
#                     getChangeDetailsUrl = BaseUrl+"/httpAuth/app/rest/changes/"+ str(teamcityBuildChanges["change"][change]["id"])
#                     teamcityChangeDetails = self.getResponse(getChangeDetailsUrl, 'GET', UserID, Passwd, None)
#                     version.append(teamcityChangeDetails["version"])
                injectData["version"]=version
            data += self.parseResponse(responseTemplate, teamcityBuildDetails, injectData)
        print(json.dumps(data))
        if buildCount>0:
            self.tracking["sinceBuild"] = teamcityBuilds["build"][0]["id"]
            self.updateTrackingJson(self.tracking)
            self.publishToolsData(data)
if __name__ == "__main__":
    TeamCityAgent()
