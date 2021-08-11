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
Created on April 27, 2021

@author: 828567
'''
from datetime import datetime as dateTime
from dateutil import parser
import datetime 
import copy
import re 
import os
import hashlib
from dateutil import parser
from ....core.BaseAgent3 import BaseAgent
import json


class GitWebhookAgent(BaseAgent):
    
    @BaseAgent.timed
    def processWebhook(self,data):
        self.baseLogger.info(" inside GitWebhookAgent processWebhook ======")
        timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.almRegEx = str(self.config.get("almKeyRegEx", ''))
        dataReceived = json.loads(data)
        
        webhookTrackingDetails = self.tracking.get("webhookDetails", None)
        fetchMetaData = self.config.get("fetchMetaData", False)
        if webhookTrackingDetails is None and fetchMetaData:
            getReposUrl = self.config.get("getRepos", '')
            accessToken = self.getCredential("accessToken")
            headers = {"Authorization": "token " + accessToken}
            commitsBaseEndPoint = self.config.get("commitsBaseEndPoint", '')
            webhookTrackingDetails = {}
            self.tracking["webhookDetails"] = webhookTrackingDetails
            self.baseLogger.info(" before fetching repo details ======")
            repos = self.getResponse(getReposUrl+'?per_page=100&sort=created&page=1', 'GET', None, None, None, reqHeaders=headers)
            repoPageNum = 1
            fetchNextPage = True
            while fetchNextPage:
                if len(repos) == 0:
                    fetchNextPage = False
                    break
                for repo in repos:
                    repoName = repo.get('name', None)
                    webhookTrackingDetails[repoName] = []
                    branchPage = 1
                    fetchNextBranchPage = True
                    while fetchNextBranchPage:
                        self.baseLogger.info(" fetching branch details for repo=",repoName)
                        getBranchesRestUrl = commitsBaseEndPoint+repoName+'/branches?page='+str(branchPage)
                        branchDetails = self.getResponse(getBranchesRestUrl, 'GET', None, None, None, reqHeaders=headers)
                        branchData = []
                        for branch in branchDetails:
                            branchDict = {
                                "branchName": branch['name'],
                                "repoName": repoName,
                                "authorName":"",
                                "status":"created",
                                'consumptionTime' : timeStampNow()
                                }
                            webhookTrackingDetails[repoName].append(branch['name'])
                            branchData.append(branchDict)
                        
                        self.baseLogger.info(" before publish branch metadata details ===")
                        self.publishBranchDetails(branchData)
                              
                        if len(branchDetails) == 30:
                            branchPage = branchPage + 1
                        else:
                            fetchNextBranchPage = False
                            break
                    self.updateTrackingJson(self.tracking)
                repoPageNum = repoPageNum + 1
                repos = self.getResponse(getReposUrl + '?per_page=100&sort=created&page=' + str(repoPageNum), 'GET', None, None, None, reqHeaders=headers)
        
        if("created" in dataReceived and (dataReceived["created"] or dataReceived["deleted"])):
            ref = dataReceived.get("ref",None)
            if(ref.split('/',2)[1] == "heads"):
                self.processBranchDetails(dataReceived)
        if("commits" in dataReceived and len(dataReceived["commits"]) > 0):
            self.processCommits(dataReceived) 
        elif("pull_request" in dataReceived and len(dataReceived["pull_request"]) > 0):
            self.processPullRequestDetails(dataReceived)
        
        
         
    def processCommits(self, commitResponseData):
        self.baseLogger.info(" inside processCommits method ======")
        timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        commitsMetaData = dynamicTemplate.get('commit', {}).get('commitMetadata', {})
        commitBranchRelationMetadata = dynamicTemplate.get('extensions', {}).get('commitBranchRelation', {}).get('relationMetadata', {})
        commitResponseTemplate = dynamicTemplate.get('commit', {}).get('commitResponseTemplate', {})
        default_branch = self.config.get('default_branch', None)
        if default_branch is None:
            default_branch =  commitResponseData.get("repository",dict()).get("default_branch")
        repoName = commitResponseData.get("repository",dict()).get("name")
        ref = commitResponseData.get("ref",None)
        branchName = ref.rsplit('/',1)[1]
        finaldata = []
        commitFileDetails = []
        commitsList = []
        
        if (branchName == default_branch):
            commitsList.append(commitResponseData.get("head_commit",dict()))
            default = True
        else:
            commitsList = commitResponseData.get("commits",list())
            default = False
        
        metaData = {"repoName":repoName, "branchName":branchName, 
                    'consumptionTime':timeStampNow(), "default":default}
        
        for commit in commitsList:
            timestamp = commit.get("timestamp",None)
            commitTime = parser.parse(timestamp, ignoretz=True).astimezone(self.insightsTimeZone).strftime("%Y-%m-%dT%H:%M:%SZ")
            commitMessage = commit.get("message",None)
            almKeyIter = re.finditer(self.almRegEx, commitMessage)
            almKeys = [key.group(0) for key in almKeyIter]
            injectData = metaData.copy()
            injectData["commitTime"] = commitTime
            injectData["timestamp"] = timestamp
            fileInjectData = {}
            
            if almKeys:
                injectData.update({
                'gitType':'commit',
                'almKeys' :almKeys
                })
            else:
                injectData.update({
                'gitType':'orphanCommit'
                })
            
            self.baseLogger.info(" before parseResponse commits details ===")
            parsedData = self.parseResponse(commitResponseTemplate, commit)
            fileInjectData = parsedData[0].copy()
            parsedData[0].update(injectData)
            finaldata += parsedData

            fileInjectData["commitTime"] = commitTime
            modified = commit.get("modified",list())
            added = commit.get("added",list())
            removed = commit.get("removed",list())
            
            if len(modified) > 0:
                commitFileDetails += self.commitFileProcess(modified, fileInjectData, "modified")
            if len(added) > 0:
                commitFileDetails += self.commitFileProcess(added, fileInjectData, "added")
            if len(removed) > 0:
                commitFileDetails += self.commitFileProcess(removed, fileInjectData, "removed")
            
        self.baseLogger.info(" before publish commits details ====")
        self.publishToolsData(finaldata,commitsMetaData)
        self.publishToolsData(finaldata,commitBranchRelationMetadata)
        commitFileTemplate = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('commitFileDetails', None)
        relationMetadata = commitFileTemplate.get('relationMetadata')
        self.publishToolsData(commitFileDetails, relationMetadata)
        self.baseLogger.info(" commits details processing completed ======")
    
    def commitFileProcess(self, fileList, fileDetailsDict, status):
        self.baseLogger.info(" inside commitFileProcess method ====")
        fileDetailsData = []
        
        for file in fileList:
            filedetails = fileDetailsDict.copy()
            filename = file
            filepathHash = hashlib.md5(filename.encode('utf-8')).hexdigest()
            fileExtension = os.path.splitext(filename)[1][1:]
            fileDict ={"filename": filename, "filepathHash": filepathHash, "fileExtension": fileExtension, "status": status}
            filedetails.update(fileDict)
            fileDetailsData.append(filedetails)
        
        return fileDetailsData
    
    def processBranchDetails(self, dataReceived):
        self.baseLogger.info(" inside processBranchDetails ======")
        timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        repoName = dataReceived.get("repository",dict()).get("name")
        ref = dataReceived.get("ref",None)
        branchName = ref.rsplit('/',1)[1]
        authorName = dataReceived.get("pusher",dict()).get("name")
        branchData = []
        if(dataReceived["created"]):
            status = "created"
        elif(dataReceived["deleted"]):
            status = "deleted"
        
        branchDetailsDict = {
                "repoName":repoName,
                "branchName":branchName,
                "authorName":authorName,
                "status":status,
                'consumptionTime' : timeStampNow()
            }
        
        branchData.append(branchDetailsDict)
        self.baseLogger.info(" before publish branch details ======")
        self.publishBranchDetails(branchData)
        self.baseLogger.info(" branch details processing completed ======")
    
        
    def publishBranchDetails(self, branchData):
        self.baseLogger.info(" inside publishBranchDetails method ======")
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        branchMetaData = dynamicTemplate.get('branch', {}).get('branchMetadata', {})
        branchesinsighstTimeX = dynamicTemplate.get('branch',{}).get('insightsTimeXFieldMapping',None)
        timestamp = branchesinsighstTimeX.get('timefield',None)
        timeformat = branchesinsighstTimeX.get('timeformat',None)
        isEpoch = branchesinsighstTimeX.get('isEpoch',False)
        self.publishToolsData(branchData, branchMetaData, timestamp, timeformat,isEpoch,True)
        
    def processPullRequestDetails(self, dataReceived):
        self.baseLogger.info(" inside processPullRequestDetails method ======")
        timeStampNow = lambda: dateTime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
        repoName = dataReceived.get("repository",dict()).get("name")
        pullReqDetails = dataReceived.get("pull_request",dict())
        merged = pullReqDetails.get("merged",None)
        originBranch = pullReqDetails.get('head', dict()).get('ref', None)
        branchAlmKeyIter = re.finditer(self.almRegEx, originBranch)
        branchAlmKeys = [key.group(0) for key in branchAlmKeyIter]
        if branchAlmKeys:
            pullReqDetails['originbranchAlmKeys']= branchAlmKeys
            
        originRepo = pullReqDetails.get('head', dict()).get('repo', dict())
        isForked = originRepo.get('fork', False)
        commits = pullReqDetails.get("commits",None)
        changed_files = pullReqDetails.get("changed_files",None)
        author = dataReceived.get("sender",dict()).get("login", None)
        
        injectData = {
            'repoName': repoName,
            'isMerged': merged,
            'commits': commits,
            "changed_files": changed_files,
            'gitType': 'pullRequest', 
            'consumptionTime': timeStampNow(),
            'authorName': author
            }
        
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        responseTemplate = dynamicTemplate.get('pullRequest', {}).get('pullReqResponseTemplate', {})
        self.baseLogger.info(" before parseResponse pullrequest details ======")
        pullReqData = self.parseResponse(responseTemplate, pullReqDetails, injectData)
        
        pullReqMetaData = dynamicTemplate.get('pullRequest', {}).get('pullRequestMetaData', {})
        pullReqinsighstTimeX = dynamicTemplate.get('pullRequest',{}).get('insightsTimeXFieldMapping',None)
        pullReqtimestamp = pullReqinsighstTimeX.get('timefield',None)
        pullReqtimeformat = pullReqinsighstTimeX.get('timeformat',None)
        pullReqisEpoch = pullReqinsighstTimeX.get('isEpoch',False)
        relationMetaData = dynamicTemplate.get('extensions', {}).get('PullReqBranchRelation', {}).get('relationMetadata', {})
        self.baseLogger.info(" before publish PullRequest Details ======")
        self.publishToolsData(pullReqData, pullReqMetaData,pullReqtimestamp,pullReqtimeformat,pullReqisEpoch,True)
        pullReqData[0]["branchName"] = originBranch
        self.publishToolsData(pullReqData, relationMetaData,pullReqtimestamp,pullReqtimeformat,pullReqisEpoch,True)
        self.baseLogger.info(" PullRequest Details processing completed ======")

         

if __name__ == "__main__":
    GitWebhookAgent()           