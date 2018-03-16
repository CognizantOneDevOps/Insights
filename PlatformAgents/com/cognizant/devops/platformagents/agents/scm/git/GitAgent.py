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
        getRepos = self.config.get("getRepos", '')
        accessToken = self.config.get("accessToken", '')
        commitsBaseEndPoint = self.config.get("commitsBaseEndPoint", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        #startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%SZ')
        getReposUrl = getRepos+"?access_token="+accessToken
        enableBranches = self.config.get("enableBranches", False)
        repos = self.getResponse(getReposUrl+'&per_page=100&sort=created&page=1', 'GET', None, None, None)
        responseTemplate = self.getResponseTemplate()
        repoPageNum = 1
        fetchNextPage = True
        while fetchNextPage:
            if len(repos) == 0:
                fetchNextPage = False
                break;
            for repo in repos:
                repoName = repo.get('name', None)
                trackingDetails = self.tracking.get(repoName,None)
                if trackingDetails is None:
                    trackingDetails = {}
                    self.tracking[repoName] = trackingDetails
                repoModificationTime = trackingDetails.get('repoModificationTime', None)
                if repoModificationTime is None:
                    repoModificationTime = startFrom
                repoUpdatedAt = parser.parse(repo.get('updated_at'), ignoretz=True)
                if startFrom < repoUpdatedAt:
                    trackingDetails['repoModificationTime'] = repo.get('updated_at')
                    branches = ['master']
                    if repoName != None:
                        if enableBranches:
                            getBranchesRestUrl = commitsBaseEndPoint+repoName+'/branches?access_token='+accessToken
                            branchDetails = self.getResponse(getBranchesRestUrl, 'GET', None, None, None)
                            branches = []
                            for branch in branchDetails:
                                branches.append(branch['name'])
                        for branch in branches:
                            data = []
                            injectData = {}
                            injectData['repoName'] = repoName
                            injectData['branchName'] = branch
                            fetchNextCommitsPage = True
                            getCommitDetailsUrl = commitsBaseEndPoint+repoName+'/commits?sha='+branch+'&access_token='+accessToken+'&per_page=100'
                            since = trackingDetails.get(branch, None)
                            if since != None:
                                getCommitDetailsUrl += '&since='+since
                            commitsPageNum = 1
                            latestCommit = None
                            while fetchNextCommitsPage:
                                try:
                                    commits = self.getResponse(getCommitDetailsUrl + '&page='+str(commitsPageNum), 'GET', None, None, None)
                                    if latestCommit is None and len(commits) > 0:
                                        latestCommit = commits[0]
                                    for commit in commits:
                                        if since is not None or startFrom < parser.parse(commit["commit"]["author"]["date"], ignoretz=True):
                                            data += self.parseResponse(responseTemplate, commit, injectData)
                                        else:
                                            break
                                    if len(commits) == 0 or len(data) == 0 or len(commits) < 100:
                                        fetchNextCommitsPage = False
                                        break
                                except Exception as ex:
                                    logging.error(ex)
                                commitsPageNum = commitsPageNum + 1
                            if len(data) > 0:
                                updatetimestamp = latestCommit["commit"]["author"]["date"]
                                dt = parser.parse(updatetimestamp)
                                fromDateTime = dt + datetime.timedelta(seconds=01)
                                fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')    
                                trackingDetails[branch] = fromDateTime
                                self.publishToolsData(data)
                                self.updateTrackingJson(self.tracking)
            repoPageNum = repoPageNum + 1
            repos = self.getResponse(getReposUrl+'&per_page=100&sort=created&page='+str(repoPageNum), 'GET', None, None, None)
    
if __name__ == "__main__":
    GitAgent()       
