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
import logging

class GitAgent(BaseAgent):
    def process(self):
        getRepos = self.config.get("GetRepos", '')
        accessToken = self.config.get("AccessToken", '')
        commitsBaseEndPoint = self.config.get("CommitsBaseEndPoint", '')
        startFrom = self.config.get("StartFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%SZ')
        getReposUrl = getRepos+"?access_token="+accessToken
        enableBranches = self.config.get("enableBranches", False)
        repos = self.getResponse(getReposUrl, 'GET', None, None, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        for repo in repos:
            repoName = repo.get('name', None)
            branches = ['master']
            if repoName != None:
                if enableBranches:
                    getBranchesRestUrl = commitsBaseEndPoint+repoName+'/branches?access_token='+accessToken
                    branchDetails = self.getResponse(getBranchesRestUrl, 'GET', None, None, None)
                    branches = []
                    for branch in branchDetails:
                        branches.append(branch['name'])
                for branch in branches:
                    commits = []
                    injectData = {}
                    injectData['repoName'] = repoName
                    injectData['branchName'] = branch
                    getCommitDetailsUrl = commitsBaseEndPoint+repoName+'/commits?sha='+branch+'&access_token='+accessToken
                    trackingDetails = self.tracking.get(repoName,None)
                    if trackingDetails is None:
                        trackingDetails = {}
                        self.tracking[repoName] = trackingDetails
                    since = trackingDetails.get(branch, None)
                    if since != None:
                        getCommitDetailsUrl += '&since='+since
                    try:
                        commits = self.getResponse(getCommitDetailsUrl, 'GET', None, None, None)
                        i = 0
                        for commit in commits:
                            if startFrom < commits[i]["commit"]["author"]["date"]:
                                data += self.parseResponse(responseTemplate, commit, injectData)
                            i = i + 1
                    except Exception as ex:
                        logging.error(ex)
                    if len(commits) > 0:
                        updatetimestamp = commits[0]["commit"]["author"]["date"]
                        dt = parser.parse(updatetimestamp)
                        fromDateTime = dt + datetime.timedelta(seconds=01)
                        fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')    
                        trackingDetails[branch] = fromDateTime
                        self.publishToolsData(data)
                        self.updateTrackingJson(self.tracking)
    
if __name__ == "__main__":
    GitAgent()       
