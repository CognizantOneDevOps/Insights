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
Created on Jun 16, 2016

@author: 146414
'''
from dateutil import parser
import datetime
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class GitAgent(BaseAgent):
    def process(self):
        getRepos = self.config.get("GetRepos", '')
        accessToken = self.config.get("AccessToken", '')
        commitsBaseEndPoint = self.config.get("CommitsBaseEndPoint", '')
        startFrom = self.config.get("StartFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%SZ')
        getReposUrl = getRepos+"?access_token="+accessToken
        repos = self.getResponse(getReposUrl, 'GET', None, None, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        for repo in repos:
            repoName = repo.get('name', None)
            if repoName != None:
                injectData = {}
                injectData['repoName'] = repoName
                getCommitDetailsUrl = commitsBaseEndPoint+repoName+'/commits?access_token='+accessToken
                since = self.tracking.get(repoName,None)
                if since != None:
                    getCommitDetailsUrl += '&since='+since
                commits = self.getResponse(getCommitDetailsUrl, 'GET', None, None, None)
                i = 0
                for commit in commits:
                    if startFrom < commits[i]["commit"]["author"]["date"]:
                        data += self.parseResponse(responseTemplate, commit, injectData)
                    i = i + 1
            if len(commits) > 0:
                updatetimestamp = commits[0]["commit"]["author"]["date"]
                dt = parser.parse(updatetimestamp)
                fromDateTime = dt + datetime.timedelta(seconds=01)
                fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')    
                self.tracking[repoName] = fromDateTime
            else:
                pass
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    GitAgent()       
