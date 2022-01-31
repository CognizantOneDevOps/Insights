#-------------------------------------------------------------------------------
# Copyright 2021 Cognizant Technology Solutions
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
Created on Dec 07, 2021

@author: 828567
'''
import datetime
import json
import os
import re
import sys
import urllib.request, urllib.parse, urllib.error
import hashlib
from datetime import datetime as dateTime
from dateutil import parser
from ....core.BaseAgent3 import BaseAgent

class GitAgentV2(BaseAgent):
    repoTrackingPath = None
   
    @BaseAgent.timed
    def process(self):
        self.baseLogger.info('Inside GitAgent process')
        self.timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.almRegEx = str(self.config.get("almKeyRegEx", ''))
        self.getReposUrl = self.config.get("getRepos", '')
        self.accessToken = self.getCredential("accessToken")
        self.headers = {"Authorization": "token " + self.accessToken}
        self.commitsBaseEndPoint = self.config.get("commitsBaseEndPoint", '')
        self.startFromStr = self.config.get("startFrom", '')
        self.startFrom = parser.parse(self.startFromStr, ignoretz=True)
        self.dynamicTemplate = self.config.get('dynamicTemplate', {})
        repoList = self.dynamicTemplate.get("repositories",{}).get("names",[]) #list with repositories names to include for data collection
        self.TrackingCachePathSetup('repoTracking') #creates folder for tracking json files
        self.defaultParams = 'per_page=100&page=%s'
           
        repoPageNum = 1
        fetchNextPage = True
        while fetchNextPage:
            repos = self.getResponse(self.getReposUrl+'?sort=created&'+self.defaultParams % repoPageNum, 'GET', None, None, None, reqHeaders=self.headers)
            if len(repos) == 0:
                fetchNextPage = False
                break
            for repo in repos:
                repoName = repo.get('name', None)
                if len(repoList) > 0 and repoName not in repoList:
                    continue
                if not os.path.isfile(self.repoTrackingPath + repoName + '.json'):
                    self.UpdateTrackingCache(repoName, dict()) # creates repository wise tracking json file
                repoTrackingCache = self.TrackingCacheFileLoad(repoName)
                repoDefaultBranch = repo.get('default_branch', None) 
                self.baseLogger.info('Repository default branch name: '+repoDefaultBranch)
                self.baseLogger.info('Data collection started for Repository: '+repoName)
                
                self.processBranchDetails(repoName, repoTrackingCache, repoDefaultBranch)
            repoPageNum = repoPageNum + 1
                        
    
    def processBranchDetails(self, repoName, repoTrackingCache, repoDefaultBranch):
        self.baseLogger.info('Inside processBranchDetails method')
        allBranches = {repoDefaultBranch: False} # A dictionary with key as branchName and value as modified status
        branchPageNum = 1
        branchData = []
        fetchNextBranchPage = True
        # Fetches Branch details for a repository, stores branch name in dictionary and publish branch details.
        while fetchNextBranchPage:
            getBranchesRestUrl = self.commitsBaseEndPoint+repoName+'/branches?'+ self.defaultParams % branchPageNum
            allBranchDetails = self.getResponse(getBranchesRestUrl, 'GET', None, None, None, reqHeaders=self.headers)
            for branch in allBranchDetails:
                branchName = branch['name']
                branchTrackingDetails = repoTrackingCache.get(branchName, {})
                branchTracking = branchTrackingDetails.get('latestCommitId', None)
                #checks if latest commit details exists in tracking and update modified status in allBranches dictionary
                if branchTracking is None or branchTracking != branch.get('commit', {}).get('sha', None):
                    allBranches[branchName] = True
                else:
                    allBranches[branchName] = False
                if branchName not in repoTrackingCache:
                    branchDict = {
                            "branchName": branch['name'],
                            "repoName": repoName,
                            "authorName":"",
                            "status":"created",
                            'consumptionTime' : self.timeStampNow()
                            }
                    branchData.append(branchDict)
            if len(allBranchDetails) == 100:
                branchPageNum = branchPageNum + 1
            else:
                fetchNextBranchPage = False
                break
        if branchData:
            self.publishBranchDetails(branchData)
            self.baseLogger.info('Branch details published')
            
        # iterates over each branch and fetch commit and pull request details               
        for branch in allBranches:
            injectData = {"repoName":repoName, "branchName":branch}
            if branch == repoDefaultBranch:
                injectData['default'] = True
            else:
                injectData['default'] = False
            if allBranches[branch]:       
                self.processCommitsDetails(injectData, repoTrackingCache, repoDefaultBranch)
            self.processPullRequestDetails(repoName, branch, repoTrackingCache)
    
    #Method to fetch branch wise commit list, parse data, publish and update commit timestamp in tracking json file.      
    def processCommitsDetails(self, injectData, repoTrackingCache, repoDefaultBranch):            
        self.baseLogger.info('Inside processCommitsDetails method') 
        commitData = []
        commitFileData = [] 
        defaultBranchCommitList = [] #List with default branch commit list
        encodedBranchName = urllib.parse.quote_plus(injectData["branchName"].encode('utf-8')) # encodes branch name
        getCommitDetailsUrl = self.commitsBaseEndPoint + injectData["repoName"] +'/commits?sha=' + encodedBranchName 
        branchTrackingDetails = repoTrackingCache.get(injectData["branchName"],{})
        defaultBranchCommitList = repoTrackingCache.get(repoDefaultBranch,{}).get("defaultBranchCommitList", list())
        since = branchTrackingDetails.get('latestCommitDate', None) 
        if not since:
            since = self.startFrom.strftime("%Y-%m-%dT%H:%M:%SZ")
        getCommitDetailsUrl += '&since=' + since
        commitResponseTemplate = self.dynamicTemplate.get('commit', {}).get('commitResponseTemplate', {})
        enableCommitFileUpdation = self.config.get("enableCommitFileUpdation", False)
        commitsMetaData = self.dynamicTemplate.get('commit', {}).get('commitMetadata', {})
        commitBranchRelationMetadata = self.dynamicTemplate.get('extensions', {}).get('commitBranchRelation', {}).get('relationMetadata', {})
        commitFileDetails = self.dynamicTemplate.get('extensions', {}).get('commitFileDetails', None)
        relationMetadata = commitFileDetails.get('relationMetadata') # relation metadata used to create commit relation with branch
        
        fetchNextCommitsPage = True
        commitsPageNum = 1
        latestCommit = None
        while fetchNextCommitsPage: 
            commits = self.getResponse(getCommitDetailsUrl+'&'+ self.defaultParams % commitsPageNum, 'GET', None,None, None, reqHeaders=self.headers) 
            if latestCommit is None and len(commits)> 0:
                latestCommit = commits[0]
            for commit in commits:
                commitId = commit.get('sha', None)
                if len(defaultBranchCommitList) > 0 and commitId in defaultBranchCommitList:
                    continue
                commitMessage = commit.get('commit', dict()). get('message', '')
                almKeyIter = re.finditer(self.almRegEx, commitMessage)
                almKeys = [key.group(0) for key in almKeyIter]
                if almKeys:
                    injectData.update({
                    'gitType':'commit',
                    'almKeys' :almKeys
                    })
                else:
                    injectData.update({
                    'gitType':'orphanCommit'
                    })
                injectData['consumptionTime'] = self.timeStampNow()
                
                if injectData['default']:
                    defaultBranchCommitList.append(commitId)
                
                commitData += self.parseResponse(commitResponseTemplate, commit, injectData)
                if enableCommitFileUpdation :
                    commitFileData += self.updateCommitFileDetails(commitId, injectData["repoName"])
            
            if len(commits) == 0 or len(commits) < 100:
                fetchNextCommitsPage = False
                break 
            commitsPageNum = commitsPageNum + 1 
        
        if commitData:
            self.publishToolsData(commitData, commitsMetaData)
            self.publishToolsData(commitData,commitBranchRelationMetadata)
            self.baseLogger.info('Branch commit details published') 
            if enableCommitFileUpdation and commitFileData :
                self.publishToolsData(commitFileData, relationMetadata)
                self.baseLogger.info('Branch commit file details published') 
        
        if latestCommit:   
            self.updateCommitDetailsInTracking(injectData, latestCommit, repoTrackingCache, defaultBranchCommitList)
            self.UpdateTrackingCache(injectData["repoName"], repoTrackingCache)
            self.baseLogger.info('Branch commit details updated in tracking')
        
    
    #Method used to fetch files changed information for a particular commitId 
    def updateCommitFileDetails(self, commitId, repoName):
        self.baseLogger.info('Inside updateCommitFileDetails method')
        commitFileDetailsUrl = self.commitsBaseEndPoint + repoName + '/commits/' + commitId
        commitFileDetails =self.getResponse(commitFileDetailsUrl,'GET', None, None,None, reqHeaders=self.headers)
        commitSHA = commitFileDetails.get('sha', None)
        commitMessage = commitFileDetails.get('commit',dict()).get('message','')
        author = commitFileDetails.get('commit',dict()).get('author',dict()).get('name','')
        commitTime = commitFileDetails.get('commit',dict()).get('author',dict()).get('date','')
        commitFiles = commitFileDetails.get('files',list())
        parentsCount = len(commitFileDetails.get('parents',list()))
        commitFileDetailsData = []
            
        for file in commitFiles:
            if (parentsCount > 1):
                break 
            
            filename = file.get('filename', None)
            status = file.get('status', None)
            deletions = file.get('deletions', None)
            additions = file.get('additions', None)
            changes = file.get('changes', None)
            filepathHash = hashlib.md5(filename.encode('utf-8')).hexdigest()
            fileExtension = os.path.splitext(filename)[1][1:]

            fileDetailsDict = {
                "commitId":commitSHA,
                "commitMessage":commitMessage,
                "authorName":author,
                "commitTime":commitTime,
                "filename":filename,
                "status":status,
                "additions":additions,
                "deletions":deletions,
                "changes":changes,
                "filepathHash":filepathHash,
                "fileExtension":fileExtension
            }
            
            commitFileDetailsData.append(fileDetailsDict)
        
        self.baseLogger.info('File details collected for commitId: '+commitId)       
        return commitFileDetailsData 
    
    #Method to fetch Pull request details for a particular branch
    def processPullRequestDetails(self, repoName, branchName, repoTrackingCache):
        self.baseLogger.info(" inside processPullRequestDetails method ======")
        fetchNextPullRequestPage = True
        pullRequestPageNum = 1
        latestPullRequest = None
        ownerName = self.commitsBaseEndPoint.split('/')[4]
        pullRequestDetailsUrl = self.commitsBaseEndPoint + repoName + '/pulls?head=' + ownerName +':'+ branchName+'&state=all&sort=updated&direction=desc&' + self.defaultParams
        pullReqData = []
        branchTrackingDetails = repoTrackingCache.get(branchName,{})
        pullReqMetaData = self.dynamicTemplate.get('pullRequest', {}).get('metaData', {})
        pullReqinsighstTimeX = self.dynamicTemplate.get('pullRequest',{}).get('insightsTimeXFieldMapping',None)
        pullReqtimestamp = pullReqinsighstTimeX.get('timefield',None)
        pullReqtimeformat = pullReqinsighstTimeX.get('timeformat',None)
        pullReqisEpoch = pullReqinsighstTimeX.get('isEpoch',False)
        relationMetaData = self.dynamicTemplate.get('extensions', {}).get('PullReqBranchRelation', {}).get('relationMetadata', {})
        responseTemplate = self.dynamicTemplate.get('pullRequest', {}).get('pullReqResponseTemplate', {})
        
        while fetchNextPullRequestPage: 
            pullRequestList = self.getResponse(pullRequestDetailsUrl % pullRequestPageNum, 'GET', None,None, None, reqHeaders=self.headers) 
            if latestPullRequest is None and len(pullRequestList)> 0:
                latestPullRequest = pullRequestList[0]
            for pullRequest in pullRequestList:
                if self.startFrom > parser.parse(pullRequest["created_at"], ignoretz=True):
                    continue
                if "latestPullReqUpdatedDate" in branchTrackingDetails and branchTrackingDetails["latestPullReqUpdatedDate"] == pullRequest['updated_at']:
                    continue
                merged = True if pullRequest.get('merged_at', None) else False
                originBranch = pullRequest.get('head', dict()).get('ref', None)
                branchAlmKeyIter = re.finditer(self.almRegEx, originBranch)
                branchAlmKeys = [key.group(0) for key in branchAlmKeyIter]
                if branchAlmKeys:
                    pullRequest['originbranchAlmKeys']= branchAlmKeys
                    
                originRepo = pullRequest.get('head', dict()).get('repo', dict())
                author = pullRequest.get("user",dict()).get("login", None)
                
                injectData = {
                'repoName': repoName,
                'isMerged': merged,
                'commits': "",
                "changed_files": "",
                'gitType': 'pullRequest', 
                'consumptionTime': self.timeStampNow(),
                'authorName': author
                }
                
                pullReqData += self.parseResponse(responseTemplate, pullRequest, injectData)
            if len(pullRequestList) == 0 or len(pullRequestList) < 100:
                fetchNextPullRequestPage = False
                break 
            pullRequestPageNum = pullRequestPageNum + 1 
            
        if pullReqData:
            self.publishToolsData(pullReqData, pullReqMetaData,pullReqtimestamp,pullReqtimeformat,pullReqisEpoch,True)
            pullReqData = [dict(item, branchName=originBranch) for item in pullReqData]
            self.publishToolsData(pullReqData, relationMetaData,pullReqtimestamp,pullReqtimeformat,pullReqisEpoch,True)
            self.baseLogger.info(" Branch pull request details published ======")
            
            self.updatePullRequestDetailsInTracking(branchName, latestPullRequest, repoTrackingCache)  
            self.UpdateTrackingCache(injectData["repoName"], repoTrackingCache)
            self.baseLogger.info(" Branch pull request details updated in tracking ======")
    
    def publishBranchDetails(self, branchData):
        self.baseLogger.info(" inside publishBranchDetails method ======")
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        branchMetaData = dynamicTemplate.get('branch', {}).get('branchMetadata', {})
        branchesinsighstTimeX = dynamicTemplate.get('branch',{}).get('insightsTimeXFieldMapping',None)
        timestamp = branchesinsighstTimeX.get('timefield',None)
        timeformat = branchesinsighstTimeX.get('timeformat',None)
        isEpoch = branchesinsighstTimeX.get('isEpoch',False)
        self.publishToolsData(branchData, branchMetaData, timestamp, timeformat,isEpoch,True)

    def updateCommitDetailsInTracking(self, injectData, latestCommit, repoTrackingCache, defaultBranchCommitList):
        self.baseLogger.info('Inside updateCommitDetailsInTracking')
        updatetimestamp = latestCommit["commit"]["author"]["date"]
        dt = parser.parse(updatetimestamp)
        fromDateTime = dt + datetime.timedelta(seconds=0o1)
        fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')
        if injectData["branchName"] in repoTrackingCache:
            repoTrackingCache[injectData["branchName"]]['latestCommitDate'] = fromDateTime
            repoTrackingCache[injectData["branchName"]]['latestCommitId'] = latestCommit['sha']
        else:
            repoTrackingCache[injectData["branchName"]] = {'latestCommitDate': fromDateTime, 'latestCommitId': latestCommit["sha"]}
        if injectData["default"]:
            repoTrackingCache[injectData["branchName"]]['defaultBranchCommitList'] = defaultBranchCommitList
        
    def updatePullRequestDetailsInTracking(self, branchName, latestPullRequest, repoTrackingCache):
        self.baseLogger.info('Inside updatePullRequestDetailsInTracking')
        if branchName in repoTrackingCache:
            repoTrackingCache[branchName]['latestPullReqUpdatedDate'] = latestPullRequest['updated_at']
            repoTrackingCache[branchName]['latestPullRequestId'] = latestPullRequest['number']
            repoTrackingCache[branchName]['latestPullRequestState'] = latestPullRequest['state']
        else:
            repoTrackingCache[branchName] = {'latestPullReqUpdatedDate': latestPullRequest['updated_at'], 'latestPullRequestId': latestPullRequest['number'],
                                           'latestPullRequestState': latestPullRequest['state']}
            
    def TrackingCachePathSetup(self, folderName):
        self.baseLogger.info('Inside TrackingCachePathSetup')
        self.repoTrackingPath = os.path.dirname(sys.modules[self.__module__].__file__) + os.path.sep + folderName + os.path.sep
        if not os.path.exists(self.repoTrackingPath):
            os.mkdir(self.repoTrackingPath)

    def TrackingCacheFileLoad(self, fileName):
        self.baseLogger.info('Inside TrackingCacheFileLoad')
        with open(self.repoTrackingPath + fileName + '.json', 'r') as filePointer:
            data = json.load(filePointer)
        return data

    def UpdateTrackingCache(self, fileName, trackingDict):
        self.baseLogger.info('Inside UpdateTrackingCache')
        with open(self.repoTrackingPath + fileName + '.json', 'w') as filePointer:
            json.dump(trackingDict, filePointer, indent=4)
                

if __name__ == "__main__":
    GitAgentV2()