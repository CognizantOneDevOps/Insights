#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
#     of the License at
#   
#     http://www.apache.org/licenses/LICENSE-2.0
#   
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
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
@author: 666973 and 716660
'''
from time import mktime
from dateutil import parser
from ....core.BaseAgent import BaseAgent

class BitBucketCloudAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        self.baseEndPoint = self.config.get("baseEndPoint", '')
        self.userId = self.getCredential("userid")
        self.cred = self.getCredential("passwd")
        self.scanAllBranches = self.config.get("scanAllBranches", False)
        self.scanPullRequests = self.config.get("scanPullRequests", False)
        self.scanReleaseBranches = self.config.get("scanReleaseBranches", False)
        self.startFrom = self.config.get("startFrom", '')
        self.responseTemplate = self.getResponseTemplate()
        self.metadata = self.config.get("metadata","")
        self.slugvaluesList = self.config.get('dynamicTemplate', {}).get('repositoryList', [])
        # This block is used to fetch all repo list  
        if len(self.slugvaluesList) == 0:
            limit = 100
            repoStart = 0
            fetchNextPage = True
            while fetchNextPage: 
                nexturl=self.baseEndPoint+'/'+'?pagelen=100&fields=values.slug'+'&limit='+str(limit)+'&page='+str(repoStart+1)
                self.baseLogger.info('nexturl to fetch repository ==== '+nexturl)
                bitBucketRepos ={}          
                bitBucketRepos = self.getResponse(nexturl,'GET', self.userId, self.cred, None,None)
                self.baseLogger.info(bitBucketRepos['values'])
                numOfRepos = len(bitBucketRepos["values"])
                if numOfRepos == 0:
                    self.baseLogger.info(' nexturl is empty '+str(numOfRepos));
                    fetchNextPage = False
                    break;
                self.baseLogger.info('Number of Repo return '+str(numOfRepos))
                for i in range (0, numOfRepos):
                    self.baseLogger.info('slugvalue numOfRepos scanAllBranches '+str(repoStart)+' ===== '+bitBucketRepos["values"][i].get('slug'))
                    self.slugvaluesList.append(bitBucketRepos["values"][i].get('slug'))
                    nestPageExists=bitBucketRepos.get("next", None)
                    
                self.baseLogger.info(' nestPageExists ' +str(nestPageExists))
                repoStart=repoStart +1 
                 
        self.baseLogger.info('All repository list ==== '+str(len(self.slugvaluesList)))    
             
        for i in range(0, len(self.slugvaluesList)):
            slugrepovalue = str(self.slugvaluesList[i])
            self.baseLogger.info('slugvalue  ===== '+slugrepovalue)
            trackingRepo = self.tracking.get(slugrepovalue,None)
            limit = 20
            if trackingRepo is None:
                trackingRepo = {'Commits':{},
                                'PullRequest':{}}
                self.tracking[slugrepovalue] = trackingRepo 
            trackingRepoCommits = trackingRepo.get('Commits',None)    
            if self.scanAllBranches or self.scanReleaseBranches:                          
                bitBicketBranchessUrl = self.baseEndPoint+'/'+slugrepovalue+"/refs/branches"                           
                branchStart = 0
                fetchNextBranchPage = True
                
                while fetchNextBranchPage:
                    #ogging.info('bitBicketBranchessUrl  ==== '+bitBicketBranchessUrl)
                    bitBicketBranches = self.getResponse(bitBicketBranchessUrl+'?limit='+str(limit)+'&page='+str(branchStart+1), 'GET', self.userId, self.cred, None)
                    numBranches = len(bitBicketBranches["values"])
                    if numBranches == 0:
                        fetchNextBranchPage = False
                        break;
                    for branches in range(numBranches):
                        branchName = bitBicketBranches["values"][branches]["name"]
                        if self.scanReleaseBranches and (branchName == "master" or branchName.startswith("release") ):
                            self.processAllCommitsForBranch(slugrepovalue, branchName, trackingRepoCommits) 
                        else:
                            self.processAllCommitsForBranch(slugrepovalue, branchName, trackingRepoCommits)
                    nestPageExists=bitBicketBranches.get("next", None)
                    if nestPageExists is None:
                        self.baseLogger.info(' Collected all data for repository '+slugrepovalue)
                        fetchNextBranchPage = False
                        break;
                    branchStart=bitBicketBranches.get("page", None) 
            '''else:
                branchName = "master"
                self.processAllCommitsForBranch(slugrepovalue, branchName, trackingRepo)  '''      
            
            # process pull requests for this repo
            if self.scanPullRequests : 
                trackingRepoPullRequest = trackingRepo.get('PullRequest',None)
                self.processPullRequestsForRepo(slugrepovalue, trackingRepoPullRequest)    

    @BaseAgent.timed
    def processAllCommitsForBranch(self,repoName, branchName, repoTracking):
        data = []
        injectData = {}
        injectData['branchName'] = branchName
        injectData['repoName'] = repoName 
               
        # get all commits under a branch
        limit=30
        commitStart = 0     
        fetchNextComitPage = True
        isTrackingUpdated = False
        branchTracking = repoTracking.get(branchName, None)
        bitBucketCommitsUrl = '';
        bitBucketCommits = {} 
        lastCommitId = '';
        lastCommitTime = '';
        
        while fetchNextComitPage:
            if bitBucketCommitsUrl =='':
                bitBucketCommitsUrl = self.baseEndPoint+'/'+repoName+'/commits/'+branchName+'?&pagelen='+str(limit)+'&page='+str(commitStart+1)
                
            if branchTracking != None:
                #bitBucketCommitsUrl += '&since='+branchTracking.get('lastCommitId',None)
                lastCommitTime = branchTracking.get('lastCommitTime',None)
            else:
                lastCommitTime=self.startFrom
                
            lastCommitTimeEpoch = parser.parse(lastCommitTime)
            lastCommitTimeEpoch = mktime(lastCommitTimeEpoch.timetuple()) + lastCommitTimeEpoch.microsecond/1000000.0
            lastCommitTimeEpoch = long(lastCommitTimeEpoch * 1000)
            #self.baseLogger.info('bitBucketCommitsUrl debug === '+bitBucketCommitsUrl)
            try:
                bitBucketCommits = self.getResponse(bitBucketCommitsUrl, 'GET', self.userId, self.cred, None)
                i = 0
                for commits in (bitBucketCommits["values"]):
                    creatortimestamp = bitBucketCommits["values"][i]["date"]
                    #self.baseLogger.info('lastCommitTimeEpoch ==== '+lastCommitTime+'   creatortimestamp === '+creatortimestamp +' commitId '+bitBucketCommits["values"][i]["hash"])
                    authortimestampEpoch = parser.parse(creatortimestamp)
                    authortimestampEpoch = mktime(authortimestampEpoch.timetuple()) + authortimestampEpoch.microsecond/1000000.0
                    authortimestampEpoch = long(authortimestampEpoch * 1000)
                    if  authortimestampEpoch > lastCommitTimeEpoch:
                        data += self.parseResponse(self.responseTemplate, commits, injectData)
                        if not isTrackingUpdated:
                            updatetracking = {'lastCommitId' :bitBucketCommits["values"][0]["hash"] ,
                                               'lastCommitTime':bitBucketCommits["values"][0]["date"] }
                            repoTracking[branchName] = updatetracking
                            isTrackingUpdated = True
                    else :
                        self.baseLogger.info(' No commit found for repo Name '+repoName +'  branch name '+branchName+' time check lastCommitTime ==== '+lastCommitTime+'   commit_timestamp === '+creatortimestamp +' commitId '+bitBucketCommits["values"][i]["hash"])
                        fetchNextComitPage= False;
                        break;
                    i = i + 1
                    
            except Exception as ex:
                print(ex)
                self.baseLogger.error(ex)
                break
            isNextPage=bitBucketCommits.get("next", None)
            if isNextPage is None:
                fetchNextCommitPage = False
                break;
            else:
                bitBucketCommitsUrl = isNextPage;
                commitStart=commitStart+1; 
        self.baseLogger.info('branchName  '+branchName +' repoName '+repoName+'  len(data) '+str(len(data)))                  
        # Update data once its processed for each branch
        if len(data) > 0:
            self.publishToolsData(data)
            self.updateTrackingJson(self.tracking)

    @BaseAgent.timed
    def processPullRequestsForRepo(self,repoName, repoTracking):
        pullRequestTemplate = self.responseTemplate.get("pullRequests",None)
        data = []
        injectData = {}
        injectData['type'] = "pullRequest"
        injectData['repoName'] = repoName
        # get all commits under a branch
        limit = 40
        prStart = 0
        fetchNextPRPage = True
        isTrackingUpdated = False
        pullRequests = {} 
        # initialize, in case of any exception
        # Logic :Fetch pull requests in the order to newest first. In the first fetch we would need to get all pull requests for the repoTracking and compare it with start from date. 
        #As these pull request arrange by descending order, we will stop where update on time is less that start from date, also update recent update on time tracking.json, 
        #We are maintaining pull request status in tracking.json as well .
        #In subsequent request we use tracking json file lastCommitTime to compare API response update on time. Also compare if pull request status has change 
        
        if len(repoTracking) > 0 :
            lastUpdatedOnTime = repoTracking.get('lastUpdatedOnTime',None)
        else:
            lastUpdatedOnTime=self.startFrom
        
        lastUpdatedOnEpoch = parser.parse(lastUpdatedOnTime)
        lastUpdatedOnEpoch = mktime(lastUpdatedOnEpoch.timetuple()) + lastUpdatedOnEpoch.microsecond/1000000.0
        lastUpdatedOnEpoch = long(lastUpdatedOnEpoch * 1000)
        lastUpdatedPRId = None
        while fetchNextPRPage:
            pullRequestUrl = self.baseEndPoint+"/"+repoName+"/pullrequests?state=All&order=NEWEST&pagelen="+str(limit)+'&page='+str(prStart+1)
            try:
                #self.baseLogger.info("pullRequestUrl ===== "+pullRequestUrl)
                pullRequests = self.getResponse(pullRequestUrl, 'GET', self.userId, self.cred, None)
                i = 0
                numOfPullRequest = len(pullRequests["values"])
                self.baseLogger.info("repoName ===== "+repoName+" for page number "+str(prStart+1) +" numOfPullRequest ==== "+str(numOfPullRequest))
                updatetrackingList = {};
                for pullReq in (pullRequests["values"]):
                    prId = pullRequests["values"][i]["id"]
                    state_PR = pullRequests["values"][i]["state"]
                    updatedOnDate = pullRequests["values"][i]["updated_on"]
                    createdOnDate = pullRequests["values"][i]["created_on"]
                    updatedOnDateEpoch = parser.parse(updatedOnDate)
                    updatedOnDateEpoch = mktime(updatedOnDateEpoch.timetuple()) + updatedOnDateEpoch.microsecond/1000000.0
                    updatedOnDateEpoch = long(updatedOnDateEpoch * 1000)
                    
                    if updatedOnDateEpoch > lastUpdatedOnEpoch:
                        lastUpdatedPRIdStatus = repoTracking.get(str(prId), None)
                        if lastUpdatedPRIdStatus != None and state_PR == lastUpdatedPRIdStatus :
                            self.baseLogger.info(" No change in state of  prId ===== "+str(prId) +" state_PR ===== "+state_PR )
                            i = i + 1
                            continue;
                        data += self.parseResponse(pullRequestTemplate, pullReq, injectData)
                        repoTracking[prId] = state_PR
                    else:
                        self.baseLogger.info("No pull request found in date prId ===== "+str(prId) +" updatedOnDate ==== "+updatedOnDate+" state_PR ===== "+state_PR +" lastUpdatedOnTime ==== "+lastUpdatedOnTime)
                        fetchNextPRPage = False
                        break;
                    i = i + 1
                if not isTrackingUpdated :
                    if numOfPullRequest > 0:
                        repoTracking['lastUpdatedOnTime'] = pullRequests["values"][0]["updated_on"]
                        isTrackingUpdated = True
            except Exception as ex:
                print(ex)
                self.baseLogger.error(ex)
                break
            self.updateTrackingJson(self.tracking)
            isNextPage=pullRequests.get("next", None)
            if isNextPage is None:
                fetchNextPRPage = False
                break;
            prStart = pullRequests.get("page",None);
        self.baseLogger.info(' repoName '+repoName+'  len(data) '+str(len(data))) 
        # Update data once its processed for each repository
        if len(data) > 0:
            insighstTimeXFieldMapping = self.config.get('dynamicTemplate', {}).get('insightsTimeXfieldsPullRequest',None)
            timeStampField=insighstTimeXFieldMapping.get('timefield',None)
            timeStampFormat=insighstTimeXFieldMapping.get('timeformat',None)
            isEpoch=insighstTimeXFieldMapping.get('isEpoch',None);
            self.publishToolsData(data,self.metadata,timeStampField,timeStampFormat,isEpoch,True)
        
if __name__ == "__main__":
    BitBucketCloudAgent()