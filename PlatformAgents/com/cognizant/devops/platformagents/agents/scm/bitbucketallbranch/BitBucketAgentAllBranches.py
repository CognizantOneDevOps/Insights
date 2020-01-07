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
from ....core.BaseAgent import BaseAgent

class BitBucketAgentAllBranches(BaseAgent):
    def process(self):
        BaseEndPoint = self.config.get("baseEndPoint", '')
        UserId = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        startFrom = long(startFrom * 1000)
        getProjectsUrl = BaseEndPoint
        responseTemplate = self.getResponseTemplate()
        data = []
        limit = 100
        start = 0
        fetchNextPage = True
        while fetchNextPage:
            # get all bitbucket projects
            bitBucketProjects = self.getResponse(getProjectsUrl+'?limit='+str(limit)+'&start='+str(start), 'GET', UserId, Passwd, None)
            numProjects = len(bitBucketProjects["values"])
            if numProjects == 0:
                fetchNextPage = False
                break;
            for projects in range(numProjects):
                ProjKey = bitBucketProjects["values"][projects]["key"]
                trackingProject = self.tracking.get(ProjKey,None)
                if trackingProject is None:
                    trackingProject = {}
                    self.tracking[ProjKey] = trackingProject
                bitBicketReposUrl = BaseEndPoint+ProjKey+"/repos"
                repoStart = 0
                fetchNextRepoPage = True
                while fetchNextRepoPage:
                    # get all repos under a project
                    bitBicketRepos = self.getResponse(bitBicketReposUrl+'?limit='+str(limit)+'&start='+str(repoStart), 'GET', UserId, Passwd, None)
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
                        bitBicketBranchessUrl = BaseEndPoint+ProjKey+"/repos/"+repoName+"/branches/"
                        # get all branches under a repo
                        branchStart = 0
                        fetchNextBranchPage = True
                        while fetchNextBranchPage:
                            bitBicketBranches = self.getResponse(bitBicketBranchessUrl+'?limit='+str(limit)+'&start='+str(branchStart), 'GET', UserId, Passwd, None)
                            numBranches = len(bitBicketBranches["values"])
                            if numBranches == 0:
                                fetchNextBranchPage = False
                                break;
                            for branches in range(numBranches):
                                data = []
                                branchName = bitBicketBranches["values"][branches]["displayId"]
                                branchTracking = repoTracking.get(branchName, None)
                                injectData = {}
                                injectData['branchName'] = branchName
                                injectData['repoName'] = repoName
                                injectData['projectName'] = ProjKey
                                # get all branches under a repo
                                commitStart = 0
                                fetchNextComitPage = True
                                isTrackingUpdated = False
                                while fetchNextComitPage:
                                    bitBucketCommitsUrl = BaseEndPoint+ProjKey+"/repos/"+repoName+"/commits?until="+branchName
                                    if branchTracking != None:
                                        bitBucketCommitsUrl += '&since='+branchTracking
                                    try:
                                        bitBucketCommits = self.getResponse(bitBucketCommitsUrl+'&limit='+str(limit)+'&start='+str(commitStart), 'GET', UserId, Passwd, None)
                                        i = 0
                                        for commits in (bitBucketCommits["values"]):
                                            authortimestamp = bitBucketCommits["values"][i]["authorTimestamp"]
                                            if startFrom < authortimestamp:
                                                data += self.parseResponse(responseTemplate, commits, injectData)
                                            i = i + 1
                                        if not isTrackingUpdated:
                                            repoTracking[branchName] = bitBucketCommits["values"][0]["id"]
                                            isTrackingUpdated = False
                                    except:
                                        pass
                                    if bitBucketCommits.get("isLastPage", True):
                                        fetchNextCommitPage = False
                                        break;
                                    commitStart = bitBucketCommits.get("nextPageStart", None)

                                # Update data once its processed for each branch
                                if len(data) > 0:
                                    self.publishToolsData(data)
                                    self.updateTrackingJson(self.tracking)

                            if bitBicketBranches.get("isLastPage", True):
                                fetchNextBranchPage = False
                                break;
                            branchStart = bitBicketBranches.get("nextPageStart", None)

                    if bitBicketRepos.get("isLastPage", True):
                        fetchNextRepoPage = False
                        break;
                    repoStart = bitBicketRepos.get("nextPageStart", None)
            # check if this is the last page
            if bitBucketProjects.get("isLastPage", True):
                fetchNextPage = False
                break;
            start = bitBucketProjects.get("nextPageStart", None)
if __name__ == "__main__":
    BitBucketAgentAllBranches()
