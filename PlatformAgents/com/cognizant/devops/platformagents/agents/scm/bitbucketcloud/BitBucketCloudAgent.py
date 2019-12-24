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
#from time import mktime
from dateutil import parser
from ....core.BaseAgent import BaseAgent

class BitBucketCloudAgent(BaseAgent):
    def process(self):
        BaseEndPoint = self.config.get("baseEndPoint", '')
        UserId = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        startFrom = self.config.get("startFrom", '')
        startFromLen = len(startFrom)
        startFrom = parser.parse(startFrom)
        getProjectsUrl = BaseEndPoint + '?fields=values.slug'
        pagelen = 100
        bitBucketRepos = self.getResponse(getProjectsUrl, 'GET', UserId, Passwd, None)
        responseTemplate = self.getResponseTemplate()
        data = []
        slugValues = bitBucketRepos["values"];
        for slugValue in slugValues:
            slug = slugValue["slug"]
            injectData = { 'repoKey' : slug }
            trackedCommit = self.tracking.get(slug, None)
            lastFetchedCommit = None
            fetchMoreCommits = True
            commitsUrl = None
            if trackedCommit is not None:
                commitsUrl = BaseEndPoint+'/'+slug+'/commits/'+trackedCommit+'?pagelen='+str(pagelen)
            else:
                commitsUrl = BaseEndPoint+'/'+slug+"/commits?pagelen="+str(pagelen)
            commitsUrl = BaseEndPoint+'/'+slug+"/commits?pagelen="+str(pagelen)
            latestCommitId = None
            skipFirst = False
            while fetchMoreCommits :
                bitBucketCommitsResponse = self.getResponse(commitsUrl, 'GET', UserId, Passwd, None)
                bitBucketCommits = bitBucketCommitsResponse["values"]
                if len(bitBucketCommits) > 0:
                    if latestCommitId is None:
                        latestCommitId = bitBucketCommits[0]['hash']
                    if trackedCommit is None:
                        lastFetchedCommit = self.firstTimeFetch(data, responseTemplate, injectData, bitBucketCommits, startFrom, startFromLen, skipFirst)
                    else:
                        lastFetchedCommit = self.incrementalFetch(data, responseTemplate, injectData, bitBucketCommits, trackedCommit, skipFirst)
                if lastFetchedCommit and len(bitBucketCommits) == pagelen:
                    commitsUrl = BaseEndPoint+'/'+slug+'/commits/'+str(lastFetchedCommit)+'?pagelen='+str(pagelen)
                else:
                    fetchMoreCommits = False
                    break
                skipFirst = True  
            if latestCommitId is not None:
                self.tracking[slug] = latestCommitId 
        self.publishToolsData(data)
        self.updateTrackingJson(self.tracking)
    
    def incrementalFetch(self, data, responseTemplate, injectData, bitBucketCommits, trackingCommitId, skipFirst):
        lastFetchedCommit = None
        if len(bitBucketCommits) > 0:
            if skipFirst:
                bitBucketCommits = bitBucketCommits[1: len(bitBucketCommits)-1]
            for commits in bitBucketCommits:
                if trackingCommitId != commits['hash']:
                    data += self.parseResponse(responseTemplate, commits, injectData)
                    lastFetchedCommit = commits['hash']
                else:
                    lastFetchedCommit = None
                    break;
        return lastFetchedCommit
    
    def firstTimeFetch(self, data, responseTemplate, injectData, bitBucketCommits, trackingDate, startFromLen, skipFirst):
        lastFetchedCommit = None
        if len(bitBucketCommits) > 0:
            if skipFirst:
                bitBucketCommits = bitBucketCommits[1: len(bitBucketCommits)-1]
            for commits in bitBucketCommits:
                commitDate = parser.parse(commits['date'][:startFromLen])
                if commitDate >= trackingDate:
                    data += self.parseResponse(responseTemplate, commits, injectData)
                    lastFetchedCommit = commits['hash']
                else:
                    lastFetchedCommit = None
                    break;
        return lastFetchedCommit
        
if __name__ == "__main__":
    BitBucketCloudAgent()        
