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

class BitBucketAgentAllBranches(BaseAgent):
    def process(self):
        BaseEndPoint = self.config.get("BaseEndPoint", '')
        UserId = self.config.get("UserID", '')
        Passwd = self.config.get("Passwd", '')
        startFrom = self.config.get("StartFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        startFrom = long(startFrom * 1000)
        getProjectsUrl = BaseEndPoint
        # get all bitbucket projects
        bitBucketProjects = self.getResponse(getProjectsUrl, 'GET', UserId, Passwd, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        trackingToken = ''
        for projects in range(len(bitBucketProjects["values"])):
            ProjKey = bitBucketProjects["values"][projects]["key"]
            trackingToken = trackingToken+ProjKey
            bitBicketReposUrl = BaseEndPoint+ProjKey+"/repos"
            # get all repos under a project
            bitBicketRepos = self.getResponse(bitBicketReposUrl, 'GET', UserId, Passwd, None)
            for repos in range(len(bitBicketRepos["values"])):
                repoName = bitBicketRepos["values"][repos]["slug"]
                trackingToken = trackingToken+"/"+repoName
                injectData = {}
                injectData['repoName'] = repoName
                bitBicketBranchessUrl = BaseEndPoint+ProjKey+"/repos/"+repoName+"/branches/"
                # get all branches under a repo
                bitBicketBranches = self.getResponse(bitBicketBranchessUrl, 'GET', UserId, Passwd, None)
                for branches in range(len(bitBicketBranches["values"])):
                    branchName = bitBicketBranches["values"][branches]["displayId"]
                    trackingToken = ProjKey+"/"+repoName+"/"+branchName
                    injectData = {}
                    injectData['branchName'] = branchName
                    bitBucketCommitsUrl = BaseEndPoint+ProjKey+"/repos/"+repoName+"/commits?until="+branchName
                    since = self.tracking.get(trackingToken,None)
                    if since != None:
                        bitBucketCommitsUrl += '&since='+since
                    try:
                        bitBucketCommits = self.getResponse(bitBucketCommitsUrl, 'GET', UserId, Passwd, None)
                        i = 0
                        for commits in (bitBucketCommits["values"]):
                            authortimestamp = bitBucketCommits["values"][i]["authorTimestamp"]
                            if startFrom < authortimestamp:
                                data += self.parseResponse(responseTemplate, commits, injectData)
                            i = i + 1
                        self.tracking[trackingToken] = bitBucketCommits["values"][0]["id"]
                    except:
                        pass
                    trackingToken = ''
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    BitBucketAgentAllBranches()        
