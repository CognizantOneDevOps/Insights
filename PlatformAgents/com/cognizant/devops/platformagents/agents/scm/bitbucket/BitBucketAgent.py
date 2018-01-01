
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
Created on Dec 28, 2017

@author: 104714
'''
from time import mktime
from dateutil import parser
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent


class BitBucketAgent(BaseAgent):
    def process(self):
        self.baseEndPoint = self.config.get("BaseEndPoint", '')
        self.userId = self.config.get("UserID", '')
        self.passwd = self.config.get("Passwd", '')
        self.scanAllBranches = self.config.get("scanAllBranches", False)
        startFrom = self.config.get("StartFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        self.startFrom = long(startFrom * 1000)
        getProjectsUrl = self.baseEndPoint
        self.responseTemplate = self.getResponseTemplate()
        limit = 100
        start = 0
        fetchNextPage = True
        while fetchNextPage:
            # get all bitbucket projects
            bitBucketProjects = self.getResponse(getProjectsUrl+'?limit='+str(limit)+'&start='+str(start), 'GET', self.userId, self.passwd, None)
            numProjects = len(bitBucketProjects["values"])
            if numProjects == 0:
                fetchNextPage = False
                break;
            for projects in range(numProjects):
                projKey = bitBucketProjects["values"][projects]["key"]
                trackingProject = self.tracking.get(projKey,None)
                if trackingProject is None:
                    trackingProject = {}
                    self.tracking[projKey] = trackingProject
                bitBicketReposUrl = self.baseEndPoint+projKey+"/repos"
                repoStart = 0
                fetchNextRepoPage = True
                while fetchNextRepoPage:
                    # get all repos under a project
                    bitBicketRepos = self.getResponse(bitBicketReposUrl+'?limit='+str(limit)+'&start='+str(repoStart), 'GET', self.userId, self.passwd, None)
                    numRepos = len(bitBicketRepos["values"])
                    if numRepos == 0:
                        fetchNextRepoPage = False
                        break;
                    for repos in range(numRepos):
                        repoName = bitBicketRepos["values"][repos]["slug"]
                        repoTracking = trackingProject.get(repoName, None)
                        if repoTracking is None:
                            repoTracking = {}
                            trackingProject[repoName] = repoTracking

                        if self.scanAllBranches:
                            # scan all branches and commit inside it
                            bitBicketBranchessUrl = self.baseEndPoint+projKey+"/repos/"+repoName+"/branches/"
                            # get all branches under a repo
                            branchStart = 0
                            fetchNextBranchPage = True
                            while fetchNextBranchPage:
                                bitBicketBranches = self.getResponse(bitBicketBranchessUrl+'?limit='+str(limit)+'&start='+str(branchStart), 'GET', self.userId, self.passwd, None)
                                numBranches = len(bitBicketBranches["values"])
                                if numBranches == 0:
                                    fetchNextBranchPage = False
                                    break;
                                for branches in range(numBranches):
                                    branchName = bitBicketBranches["values"][branches]["displayId"]
                                    self.processAllCommitsForBranch(projKey, repoName, branchName, repoTracking)
                                if bitBicketBranches.get("isLastPage", True):
                                    fetchNextBranchPage = False
                                    break;
                                branchStart = bitBicketBranches.get("nextPageStart", None)
                        else:
                            # only scan commits on master branch
                            branchName = "master"
                            self.processAllCommitsForBranch(projKey, repoName, branchName, repoTracking)

                    if bitBicketRepos.get("isLastPage", True):
                        fetchNextRepoPage = False
                        break;
                    repoStart = bitBicketRepos.get("nextPageStart", None)
            # check if this is the last page
            if bitBucketProjects.get("isLastPage", True):
                fetchNextPage = False
                break;
            start = bitBucketProjects.get("nextPageStart", None)


    def processAllCommitsForBranch(self,projKey,repoName, branchName, repoTracking):
        data = []
        injectData = {}
        injectData['branchName'] = branchName
        injectData['repoName'] = repoName
        injectData['projectName'] = projKey
        # get all commits under a branch
        limit=100
        commitStart = 0
        fetchNextComitPage = True
        isTrackingUpdated = False
        branchTracking = repoTracking.get(branchName, None)
        bitBucketCommits = {} # initialize, in case of any exception
        while fetchNextComitPage:
            bitBucketCommitsUrl = self.baseEndPoint+projKey+"/repos/"+repoName+"/commits?until="+branchName
            if branchTracking != None:
                bitBucketCommitsUrl += '&since='+branchTracking
            try:
                bitBucketCommits = self.getResponse(bitBucketCommitsUrl+'&limit='+str(limit)+'&start='+str(commitStart), 'GET', self.userId, self.passwd, None)
                i = 0
                for commits in (bitBucketCommits["values"]):
                    authortimestamp = bitBucketCommits["values"][i]["authorTimestamp"]
                    if self.startFrom < authortimestamp:
                        data += self.parseResponse(self.responseTemplate, commits, injectData)
                    i = i + 1
                if not isTrackingUpdated:
                    repoTracking[branchName] = bitBucketCommits["values"][0]["id"]
                    isTrackingUpdated = True
            except:
                pass
            if bitBucketCommits.get("isLastPage", True):
                fetchNextCommitPage = False
                break;
            commitStart = bitBucketCommits.get("nextPageStart", None)
            return;
        # Update data once its processed for each branch
        if len(data) > 0:
            self.publishToolsData(data)
            self.updateTrackingJson(self.tracking)


if __name__ == "__main__":
    BitBucketAgent()
