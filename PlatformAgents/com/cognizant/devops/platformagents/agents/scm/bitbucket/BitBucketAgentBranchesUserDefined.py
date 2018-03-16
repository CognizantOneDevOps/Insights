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
Created on Jun 28, 2016

@author: 463188
'''
from time import mktime
from dateutil import parser
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class BitBucketAgentBranchesUserDefined(BaseAgent):
    def process(self):
        BaseEndPoint = self.config.get("baseEndPoint", '')
        UserId = self.config.get("userID", '')
        Passwd = self.config.get("passwd", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        startFrom = long(startFrom * 1000)
        getProjectsUrl = BaseEndPoint
        bitBucketProjects = self.getResponse(getProjectsUrl, 'GET', UserId, Passwd, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        self.userInputBranches = self.config.get('dynamicTemplate', {}).get("userInputBranches")
        for projects in range(len(bitBucketProjects["values"])):
            ProjKey = bitBucketProjects["values"][projects]["key"]
            bitBicketReposUrl = BaseEndPoint+ProjKey+"/repos"
            injectData = {}
            injectData['projectKey'] = ProjKey
            bitBicketRepos = self.getResponse(bitBicketReposUrl, 'GET', UserId, Passwd, None)
            for repos in range(len(bitBicketRepos["values"])):
                repoName = bitBicketRepos["values"][repos]["slug"]
                injectData['repoName'] = repoName
                branches = self.getBranches(ProjKey,repoName)
                for branchName in branches:
                    injectData['branchName'] = branchName
                    trackingToken = ProjKey+"/"+repoName+"/"+branchName
                    bitBucketCommitsUrl = BaseEndPoint+ProjKey+"/repos/"+repoName+"/commits?until="+branchName
                    since = self.tracking.get(trackingToken,None)
                    if since != None:
                        bitBucketCommitsUrl += '&since='+since
                    try:
                        isLastPage = False
                        start = 0;
                        trackingUpdated = False
                        commitData = []
                        while not isLastPage:
                            bitBucketCommits = self.getResponse(bitBucketCommitsUrl+'&start='+str(start)+'&limit=500', 'GET', UserId, Passwd, None)
                            commits = bitBucketCommits["values"]
                            if not trackingUpdated and len(commits) > 0: 
                                self.tracking[trackingToken] = commits[0]["id"]
                                trackingUpdated = True
                            for commit in commits:
                                authortimestamp = commit["authorTimestamp"]
                                if startFrom < authortimestamp:
                                    commitData += self.parseResponse(responseTemplate, commit, injectData)
                                else:
                                    break
                            isLastPage = bitBucketCommits["isLastPage"]
                            start = start + 500
                        self.publishToolsData(commitData)
                        self.updateTrackingJson(self.tracking)
                    except:
                        pass

    def getBranches(self,projKey,repoName):
        branches = []
        if self.userInputBranches:
            if projKey in self.userInputBranches:
                if repoName in self.userInputBranches[projKey]:
                    branches = self.userInputBranches[projKey][repoName]
        if len(branches)==0:
            branches.append("master")
        return branches
        
if __name__ == "__main__":
    BitBucketAgentBranchesUserDefined()        
