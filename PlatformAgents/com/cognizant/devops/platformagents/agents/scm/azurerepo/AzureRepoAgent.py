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
Created on Jun 1, 2019

@author: 302683
'''
from dateutil import parser
import datetime
from ....core.BaseAgent import BaseAgent
import logging

class AzureRepoAgent(BaseAgent):
    def process(self):
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        Auth = self.config.get("auth", '')
        getRepos = self.config.get("getRepos", '')
        accessToken = self.config.get("accessToken", '')
        commitsBaseEndPoint = self.config.get("commitsBaseEndPoint", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        getReposUrl = getRepos
        enableBranches = self.config.get("enableBranches", False)
        enableBrancheDeletion = self.config.get("enableBrancheDeletion", False)
        reposs = self.getResponse(getReposUrl,'GET', UserID, Passwd, None, authType=Auth)
        repos = reposs.get('value', None)
        responseTemplate = self.getResponseTemplate()
        repoPageNum = 1
        fetchNextPage = True
        while fetchNextPage:
            fetchNextPage = False
            for repo in repos:
                repoName = repo.get('name', None)
                trackingDetails = self.tracking.get(repoName,None)
                if trackingDetails is None:
                    trackingDetails = {}
                    self.tracking[repoName] = trackingDetails
                repoModificationTime = trackingDetails.get('repoModificationTime', None)
                if repoModificationTime is None:
                    repoModificationTime = startFrom
                getRepoPushUrl = getReposUrl+ '/' + repoName + '/pushes'
                reposPushes = self.getResponse(getRepoPushUrl,'GET', UserID, Passwd, None, authType=Auth)
                #Avoid Empty repo with no Pushes
                if(reposPushes.get('count',None) != 0):
                  repoUpdatedAt = reposPushes.get('value',None)[0].get('date',None)
                  repoUpdatedAt = parser.parse(repoUpdatedAt, ignoretz=True)
                  branch_from_tracking_json = []
                  for key in trackingDetails:
                      if key != "repoModificationTime":
                          branch_from_tracking_json.append(key)
                  if startFrom < repoUpdatedAt:
                    trackingDetails['repoModificationTime'] = reposPushes.get('value',None)[0].get('date',None)
                    branches = ['refs/heads/master']
                    if repoName != None:
                        if enableBranches:
                            branches = []
                            allBranches = []
                            branchPage = 1
                            fetchNextBranchPage = True
                            while fetchNextBranchPage:
                                getBranchesRestUrl = commitsBaseEndPoint+repoName+'/refs'
                                branchDetailss = self.getResponse(getBranchesRestUrl,'GET', UserID, Passwd, None, authType=Auth).get('value',None)
                                branchDetails = []
                                for branchs in branchDetailss:
                                    if(branchs.get('name',None)[:11] == 'refs/heads/'):
                                        branchDetails.append(branchs)
                                for branch in branchDetails:
                                    branchName = branch['name']
                                    branchTracking = trackingDetails.get(branchName, {}).get('latestCommitId', None)
                                    allBranches.append(branchName)
                                    if branchTracking is None or branchTracking != branch.get('commit', {}).get('sha', None):
                                        branches.append(branchName)
                                if len(branchDetails) == 30:
                                    branchPage = branchPage + 1
                                else:
                                    fetchNextBranchPage = False
                                    break
                            if len(branches) > 0 :
                                branchesinsighstTimeX=dynamicTemplate.get('branches',{}).get('insightsTimeXFieldMapping',None)
                                timestamp=branchesinsighstTimeX.get('timefield',None)
                                timeformat=branchesinsighstTimeX.get('timeformat',None)
                                isEpoch=branchesinsighstTimeX.get('isEpoch',False)
                                activeBranches = [{ 'repoName' : repoName, 'activeBranches' : allBranches, 'gitType' : 'metadata'}]
                                metadata = {
                                        "dataUpdateSupported" : True,
                                        "uniqueKey" : ["repoName", "gitType"]
                                    }
                                self.publishToolsData(activeBranches, metadata,timestamp,timeformat,isEpoch,True)
                        if enableBrancheDeletion:
                            for key in branch_from_tracking_json:
                                if key not in allBranches:
                                    tracking = self.tracking.get(repoName,None)
                                    if tracking:
                                        lastCommitDate = trackingDetails.get(key, {}).get('latestCommitDate', None)
                                        lastCommitId = trackingDetails.get(key, {}).get('latestCommitId', None)
                                        self.updateTrackingForBranchCreateDelete(trackingDetails, repoName, key, lastCommitDate, lastCommitId)
                                        tracking.pop(key)
                        self.updateTrackingJson(self.tracking)
                        for branch in branches:
                            data = []
                            injectData = {}
                            injectData['repoName'] = repoName
                            injectData['branchName'] = branch
                            parsedBranch = branch[11:]
                            if '+' in parsedBranch:
                                parsedBranch = parsedBranch.replace('+', '%2B')
                            if '&' in parsedBranch:
                                parsedBranch = parsedBranch.replace('&', '%26')
                            fetchNextCommitsPage = True
                            getCommitDetailsUrl = commitsBaseEndPoint+repoName+'/commits?searchCriteria.itemVersion.version='+parsedBranch
                            since = trackingDetails.get(branch, {}).get('latestCommitDate', None)
                            if since != None:
                                getCommitDetailsUrl += '&searchCriteria.fromDate='+since
                            latestCommit = None
                            try:
                                commits = self.getResponse(getCommitDetailsUrl, 'GET', UserID, Passwd, None, authType=Auth).get('value',None)
                                if latestCommit is None and len(commits) > 0:
                                    latestCommit = commits[0]
                                for commit in commits:
                                    if since is not None or startFrom < parser.parse(commit["author"]["date"], ignoretz=True):
                                        data += self.parseResponse(responseTemplate, commit, injectData)
                                    else:
                                        fetchNextCommitsPage = False
                                        self.updateTrackingForBranch(trackingDetails, branch, latestCommit)
                                        break
                            except Exception as ex:
                                print(ex)
                                fetchNextCommitsPage = False
                                logging.error(ex)
                            if len(data) > 0:
                                self.updateTrackingForBranch(trackingDetails, branch, latestCommit)
                                self.publishToolsData(data)
                            self.updateTrackingJson(self.tracking)

    def updateTrackingForBranch(self, trackingDetails, branchName, latestCommit):
        updatetimestamp = latestCommit["author"]["date"]
        dt = parser.parse(updatetimestamp)
        fromDateTime = dt + datetime.timedelta(seconds=01)
        fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')
        trackingDetails[branchName] = { 'latestCommitDate' : fromDateTime, 'latestCommitId' : latestCommit["commitId"]}
    def updateTrackingForBranchCreateDelete(self, trackingDetails, repoName, branchName, lastCommitDate, lastCommitId):
        trackingDetails = self.tracking.get(repoName,None)
        data_branch_delete=[]
        branch_delete = {}
        branch_delete['branchName'] = branchName
        branch_delete['repoName'] = repoName
        branch_delete['event'] = "branchDeletion"
        branch_delete['deletionTime'] =  trackingDetails.get('repoModificationTime',None)
        data_branch_delete.append(branch_delete)
        branchMetadata = {"labels" : ["METADATA"],"dataUpdateSupported" : True,"uniqueKey":["repoName","branchName"]}
        self.publishToolsData(data_branch_delete, branchMetadata,'deletionTime','%Y-%m-%dT%H:%M:%SZ',False,True)

if __name__ == "__main__":
    AzureRepoAgent()