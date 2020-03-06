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
from datetime import datetime as dateTime
from ....core.BaseAgent3 import BaseAgent
import logging
import urllib
import math
import json
import os
import sys
import re


class GitAgent(BaseAgent):
    trackingCachePath = None
    jiraRegEx = r"([A-Z]{1}[A-Z0-9]+\s?-\s?\d+)"

    def process(self):
        timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        getRepos = self.config.get("getRepos", '')
        accessToken = self.getCredential("accessToken")
        commitsBaseEndPoint = self.config.get("commitsBaseEndPoint", '')
        startFromStr = self.config.get("startFrom", '')
        startFrom = parser.parse(startFromStr, ignoretz=True)
        getReposUrl = getRepos+"?access_token="+accessToken
        enableBranches = self.config.get("enableBranches", False)
        isOptimalDataCollect = self.config.get("enableOptimizedDataRetrieval", False)
        isPullReqCommitAPIDataRetrieval = self.config.get("enablePullReqCommitAPIDataRetrieval", False)
        enableBrancheDeletion = self.config.get("enableBrancheDeletion", False)
        self.setupTrackingCachePath('trackingCache')
        repos = self.getResponse(getReposUrl+'&per_page=100&sort=created&page=1', 'GET', None, None, None)
        responseTemplate = self.getResponseTemplate()
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        metaData = dynamicTemplate.get('metaData', {})
        branchesMetaData = metaData.get('branches', {})
        commitsMetaData = metaData.get('commits', {})
        pullReqMetaData = metaData.get('pullRequest', {})
        defPullReqResTemplate = {
            "number": "pullReqId", "state": "pullReqState",
            "head": {"ref": "originBranch", "repo": {"fork": "isForked"}},
            "base": {"ref": "baseBranch"}, "isMerged": "isMerged"
        }
        pullReqResponseTemplate = dynamicTemplate.get('pullReqResponseTemplate', defPullReqResTemplate)
        repoPageNum = 1
        fetchNextPage = True
        while fetchNextPage:
            if len(repos) == 0:
                fetchNextPage = False
                break
            for repo in repos:
                repoName = repo.get('name', None)
                if not os.path.isfile(self.trackingCachePath + repoName + '.json'):
                    self.updateTrackingCacheFile(repoName, dict())
                repoTrackingCache = self.loadTrackingCacheFile(repoName)
                repoDefaultBranch = repo.get('default_branch', None)
                commitDict = dict()
                trackingDetails = self.tracking.get(repoName, None)
                if trackingDetails is None:
                    trackingDetails = {}
                    self.tracking[repoName] = trackingDetails
                repoModificationTime = trackingDetails.get('repoModificationTime', None)
                if repoModificationTime is None:
                    repoModificationTime = startFrom
                repoUpdatedAt = repo.get('pushed_at', None)
                if repoUpdatedAt is None:
                    repoUpdatedAt = repo.get('updated_at')
                repoUpdatedAt = parser.parse(repoUpdatedAt, ignoretz=True)
                branch_from_tracking_json = []
                for key in trackingDetails:
                    if key != "repoModificationTime":
                        branch_from_tracking_json.append(key)
                if startFrom < repoUpdatedAt:
                    trackingDetails['repoModificationTime'] = repo.get('updated_at')
                    branches = ['master']
                    if repoDefaultBranch != 'master':
                        branches.append(repoDefaultBranch)
                    if repoName != None:
                        if enableBranches:
                            if isOptimalDataCollect:
                                self.retrievePullRequest(commitsBaseEndPoint, repoName, repoDefaultBranch, accessToken,
                                                         trackingDetails, repoTrackingCache, startFromStr, pullReqMetaData,
                                                         pullReqResponseTemplate, commitsMetaData, responseTemplate)
                                if 'commitDict' in repoTrackingCache:
                                    commitDict = repoTrackingCache['commitDict']
                            branches = []
                            allBranches = []
                            branchPage = 1
                            fetchNextBranchPage = True
                            while fetchNextBranchPage:
                                getBranchesRestUrl = commitsBaseEndPoint+repoName+'/branches?access_token='+accessToken+'&page='+str(branchPage)
                                branchDetails = self.getResponse(getBranchesRestUrl, 'GET', None, None, None)
                                for branch in branchDetails:
                                    branchName = branch['name']
                                    branchTrackingDetails = trackingDetails.get(branchName, {})
                                    branchTracking = branchTrackingDetails.get('latestCommitId', None)
                                    allBranches.append(branchName)
                                    if branchTracking is None or branchTracking != branch.get('commit', {}).get('sha', None):
                                        branches.append(branchName)
                                if len(branchDetails) == 30:
                                    branchPage = branchPage + 1
                                else:
                                    fetchNextBranchPage = False
                                    break
                            if len(branches) > 0:
                                activeBranches = [{'repoName': repoName, 'activeBranches': allBranches, 'gitType': 'metadata', 'consumptionTime': timeStampNow()}]
                                self.publishToolsData(activeBranches, branchesMetaData)
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

                        branchesFound = list()
                        branchesNotFound = list()
                        if 'pullReqBranches' in repoTrackingCache:
                            pullReqBranches = repoTrackingCache['pullReqBranches']
                            for branch in branches:
                                if branch in pullReqBranches:
                                    branchesFound.append(branch)
                                else:
                                    branchesNotFound.append(branch)
                        orderedBranches = branchesFound + branchesNotFound
                        injectData = dict()
                        injectData['repoName'] = repoName
                        injectData['gitType'] = 'commit'
                        injectData['fromPullReq'] = False
                        for branch in orderedBranches:
                            hasLatestPullReq = False
                            data = []
                            orphanCommitIdList = list()
                            if branch == repoDefaultBranch:
                                injectData['default'] = True
                            else:
                                injectData['default'] = False
                            parsedBranch = urllib.quote_plus(branch)
                            fetchNextCommitsPage = True
                            getCommitDetailsUrl = commitsBaseEndPoint+repoName+'/commits?sha='+parsedBranch+'&access_token='+accessToken+'&per_page=100'
                            branchTrackingDetails = trackingDetails.get(branch, {})
                            since = branchTrackingDetails.get('latestCommitDate', None)
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
                                        commitId = commit.get('sha', None)
                                        if since is not None or startFrom < parser.parse(commit["commit"]["author"]["date"], ignoretz=True):
                                            if commitId not in commitDict:
                                                commitMessage = commit.get('commit', dict()).get('message', '')
                                                jiraKeyIter = re.finditer(self.jiraRegEx, commitMessage)
                                                jiraKeys = [key.group(0) for key in jiraKeyIter]
                                                if jiraKeys:
                                                    injectData['jiraKeys'] = jiraKeys
                                                else:
                                                    injectData.pop('jiraKeys', '')
                                                injectData['jiraKeyProcessed'] = True
                                                injectData['consumptionTime'] = timeStampNow()
                                                data += self.parseResponse(responseTemplate, commit, injectData)
                                                commitDict[commitId] = False
                                                orphanCommitIdList.append(commitId)
                                            elif not commitDict.get(commitId, False):
                                                orphanCommitIdList.append(commitId)
                                        else:
                                            fetchNextCommitsPage = False
                                            self.updateTrackingForBranch(trackingDetails, branch, latestCommit, repoDefaultBranch)
                                            break
                                    if len(commits) == 0 or len(data) == 0 or len(commits) < 100:
                                        fetchNextCommitsPage = False
                                        break
                                except Exception as ex:
                                    fetchNextCommitsPage = False
                                    logging.error(ex)
                                commitsPageNum = commitsPageNum + 1
                            if data or orphanCommitIdList:
                                self.updateTrackingForBranch(trackingDetails, branch, latestCommit, repoDefaultBranch,
                                                             isOptimalDataCollect, len(data), hasLatestPullReq)
                                self.publishToolsData(data, commitsMetaData)
                                orphanBranch = {
                                    'repoName': repoName,
                                    'branch': branch,
                                    'gitType': 'orphanBranch',
                                    'commit': orphanCommitIdList,
                                    'consumptionTime': timeStampNow()
                                }
                                branchJiraKeyIter = re.finditer(self.jiraRegEx, branch)
                                branchJiraKeys = [key.group(0) for key in branchJiraKeyIter]
                                if branchJiraKeys:
                                    orphanBranch['branchJiraKeys'] = branchJiraKeys
                                self.publishToolsData([orphanBranch, ])
                            self.updateTrackingJson(self.tracking)
                            self.updateTrackingCacheFile(repoName, repoTrackingCache)
            repoPageNum = repoPageNum + 1
            repos = self.getResponse(getReposUrl+'&per_page=100&sort=created&page='+str(repoPageNum), 'GET', None, None, None)

    def retrievePullRequest(self, repoEndPoint, repoName, defaultBranch, accessToken, trackingDetails, trackingCache,
                            startFrom, metaData, responseTemplate, commitMetaData, commitsResponseTemplate):
        timeStampNow = lambda: dateTime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
        injectData = dict()
        pullReqData = list()
        commitData = list()
        branchesDict = dict()
        injectData['repoName'] = repoName
        injectData['fromPullReq'] = True
        injectData['gitType'] = 'commit'
        defaultParams = 'access_token=%s' % accessToken + '&per_page=100&page=%s'
        pullReqUrl = repoEndPoint + repoName + '/pulls?state=all&sort=updated&direction=desc&'
        pullReqUrl += defaultParams
        lastTrackedTimeStr = trackingDetails.get('pullReqModificationTime', startFrom)
        lastTrackedTime = parser.parse(lastTrackedTimeStr, ignoretz=True)
        if 'commitDict' not in trackingCache:
            trackingCache['commitDict'] = dict()
        commitDict = trackingCache['commitDict']
        trackingCache['pullReqBranches'] = list()
        branchesList = trackingCache['pullReqBranches']
        if 'repoPullRequests' not in trackingCache:
            trackingCache['repoPullRequests'] = dict()
        repoPullReq = trackingCache['repoPullRequests']
        pullReqPage = 1
        nextPullReqPage = True
        isLatestPullReqDateSet = False
        while nextPullReqPage:
            pullReqDetails = list()
            try:
                pullReqDetails = self.getResponse(pullReqUrl % pullReqPage, 'GET', None, None, None)
                if pullReqDetails and not isLatestPullReqDateSet:
                    pullReqLatestModifiedTime = pullReqDetails[0].get('updated_at', None)
                    trackingDetails['pullReqModificationTime'] = pullReqLatestModifiedTime
                    isLatestPullReqDateSet = True
            except Exception as err:
                logging.error(err)
            for pullReq in pullReqDetails:
                commitList = list()
                commitIdSet = set()
                latestCommit = dict()
                pullReqNumber = pullReq.get('number', 0)
                updatedAtStr = pullReq.get('updated_at', None)
                updatedAt = parser.parse(updatedAtStr, ignoretz=True)
                if updatedAt <= lastTrackedTime:
                    nextPullReqPage = False
                    break
                pullReq['isMerged'] = True if pullReq.get('merged_at', None) else False
                basePullReqCommitUrl = pullReq.get('commits_url', '') + '?' + defaultParams
                baseBranchDetails = pullReq.get('base', dict())
                baseBranch = baseBranchDetails.get('ref', None)
                if baseBranch == defaultBranch:
                    injectData['default'] = True
                else:
                    injectData['default'] = False
                originBranchDetails = pullReq.get('head', dict())
                originRepo = originBranchDetails.get('repo', dict())
                if originRepo:
                    isForked = originRepo.get('fork', False)
                else:
                    isForked = True
                originBranch = originBranchDetails.get('ref', None)
                commitPage = 1
                nextPullReqCommitPage = True
                while nextPullReqCommitPage:
                    getPullReqCommitUrl = basePullReqCommitUrl % commitPage
                    commitDetails = list()
                    try:
                        commitDetails = self.getResponse(getPullReqCommitUrl, 'GET', None, None, None)
                        if commitDetails:
                            latestCommit = commitDetails[-1]
                    except Exception as err:
                        logging.error(err)
                    for commit in commitDetails:
                        commitId = commit.get('sha', '')
                        commitMessage = commit.get('commit', dict()).get('message', '')
                        jiraKeyIter = re.finditer(self.jiraRegEx, commitMessage)
                        jiraKeys = [key.group(0) for key in jiraKeyIter]
                        if jiraKeys:
                            injectData['jiraKeys'] = jiraKeys
                        else:
                            injectData.pop('jiraKeys', '')
                        injectData['jiraKeyProcessed'] = True
                        injectData['consumptionTime'] = timeStampNow()
                        commitList += self.parseResponse(commitsResponseTemplate, commit, injectData)
                        commitIdSet.add(commitId)
                        commitDict[commitId] = True
                    if len(commitDetails) < 100:
                        nextPullReqCommitPage = False
                    else:
                        commitPage += 1
                mergedSHA = pullReq['merge_commit_sha']
                if pullReq['isMerged'] and mergedSHA:
                    commitIdSet.add(mergedSHA)
                pullReq['commit'] = list(commitIdSet)
                if originBranch not in repoPullReq:
                    repoPullReq[originBranch] = dict()
                originBranchPullReq = repoPullReq[originBranch]
                if baseBranch not in originBranchPullReq:
                    originBranchPullReq[baseBranch] = dict()
                baseOriginPullReqDict = originBranchPullReq[baseBranch]
                baseOriginPullReqDict[str(pullReqNumber)] = {
                    "pullReqId": pullReqNumber,
                    "originBranch": originBranch,
                    "originBranchJiraKeys": branchJiraKeys,
                    "baseBranch": baseBranch,
                    "mergedAt": pullReq.get('merged_at', None),
                    "closedAt": pullReq.get('closed_at', None),
                    "updatedAt": pullReq.get('updated_at', None),
                    "headSHA": originBranchDetails.get('sha', ''),
                    "commitCount": len(pullReq.get('commit', [])),
                    "computedPullReq": False,
                    "isMerged": pullReq.get('isMerged', False),
                    "isForked": isForked
                }
                branchesDict[pullReqNumber] = originBranch
                if not isForked:
                    if originBranch not in trackingDetails:
                        trackingDetails[originBranch] = dict()
                    originTrackingDetails = trackingDetails[originBranch]
                    try:
                        latestCommitTimeStr = latestCommit.get('commit', dict()).get('author', dict()).get('date', None)
                        if 'latestPullReqCommitTime' in originTrackingDetails:
                            latestCommitTime = parser.parse(latestCommitTimeStr, ignoretz=True)
                            lastCommitTimeStr = originTrackingDetails.get('latestPullReqCommitTime', None)
                            lastCommitTime = parser.parse(lastCommitTimeStr, ignoretz=True)
                            if lastCommitTime < latestCommitTime:
                                originTrackingDetails['latestPullReqCommitTime'] = latestCommitTimeStr
                                originTrackingDetails['latestPullReqCommit'] = originBranchDetails.get('sha', '')
                                originTrackingDetails['pullReqCommitCount'] = len(commitList)
                        else:
                            originTrackingDetails['latestPullReqCommitTime'] = latestCommitTimeStr
                            originTrackingDetails['latestPullReqCommit'] = originBranchDetails.get('sha', '')
                            originTrackingDetails['pullReqCommitCount'] = len(commitList)
                        baseBranches = set(originTrackingDetails.get('baseBranches', []))
                        baseBranches.add(baseBranch)
                        originTrackingDetails['baseBranches'] = list(baseBranches)
                        originTrackingDetails['totalPullReqCommits'] = originTrackingDetails.get('totalPullReqCommits', 0) + len(commitList)
                    except Exception as err:
                        logging.error(err)
                        logging.warn(baseOriginPullReqDict[str(pullReqNumber)])
                if commitList:
                    pullReqData += self.parseResponse(responseTemplate, pullReq, {'repoName': repoName, 'gitType': 'pullRequest', 'consumptionTime': timeStampNow()})
                    commitData += commitList
            if len(pullReqDetails) < 100:
                nextPullReqPage = False
            else:
                pullReqPage += 1
        for branch in sorted(branchesDict.items(), key=lambda record: record[0]):
            if branch not in branchesList:
                branchesList.append(branch[1])

        if commitData:
            self.publishToolsData(pullReqData, metaData)
            self.publishToolsData(commitData, commitMetaData)
            self.updateTrackingJson(self.tracking)
            self.updateTrackingCacheFile(repoName, trackingCache)

    # def associatedPullRequest(self):
    #     timeStampNow = lambda: dateTime.now().strftime("%Y-%m-%dT%H:%M:%SZ")
    #     searchAPIEndpoint = self.config.get('searchAPI', None)
    #     accessToken = self.config.get('accessToken', '')
    #     metaData = self.config.get('dynamicTemplate', dict()).get('metaData', dict()).get('commits', dict())
    #     searchUrl = searchAPIEndpoint + "?access_token=" + accessToken + "&q=%s&page=%d&per_page=%d"
    #
    #     for repository in self.tracking:
    #         repositoryDict = self.tracking[repository]
    #         commitDict = repositoryDict.get('commitDict', dict())
    #         associatedPullReq = list()
    #         for commitId, isLinkCaptured in commitDict.items():
    #             # not isLinkCaptured for testing sake removed not
    #             if isLinkCaptured:
    #                 pullReqIdList = list()
    #                 pageSize = 100
    #                 nextResponse, page = True, 1
    #                 pageSetFlag, totalPage = False, 0
    #                 while nextResponse:
    #                     response = dict()
    #                     try:
    #                         url = searchUrl % (commitId, page, pageSize)
    #                         response = self.getResponse(url, 'GET', None, None, None)
    #                         if not pageSetFlag:
    #                             total = response.get('total_count', 0)
    #                             totalPage = int(math.ceil(float(total) / 100))
    #                             pageSetFlag = True
    #                     except Exception as err:
    #                         logging.error(err)
    #                     responseData = response.get('items', None)
    #                     if responseData:
    #                         for pullReqInfo in responseData:
    #                             pullReqIdList.append(pullReqInfo.get('number', None))
    #                         if totalPage == page:
    #                             pageSetFlag, nextResponse = False, False
    #                     else:
    #                         pageSetFlag, nextResponse = False, False
    #                     page = page + 1
    #                 if pullReqIdList:
    #                     associatedPullReq.append({
    #                         "repoName": repository,
    #                         "commitId": commitId,
    #                         "associatedPullReq": pullReqIdList,
    #                         "fromPullReq": True,
    #                         "consumptionTime": timeStampNow()
    #                     })
    #         if associatedPullReq:
    #             self.publishToolsData(associatedPullReq, metaData)

    def setupTrackingCachePath(self, folderName):
        self.trackingCachePath = os.path.dirname(sys.modules[self.__module__].__file__) + os.path.sep + folderName + os.path.sep
        if not os.path.exists(self.trackingCachePath):
            os.mkdir(self.trackingCachePath)

    def loadTrackingCacheFile(self, fileName):
        with open(self.trackingCachePath + fileName + '.json', 'r') as filePointer:
            data = json.load(filePointer)
        return data

    def updateTrackingCacheFile(self, fileName, trackingDict):
        with open(self.trackingCachePath + fileName + '.json', 'w') as filePointer:
            json.dump(trackingDict, filePointer)

    def updateTrackingForBranch(self, trackingDetails, branchName, latestCommit, repoDefaultBranch, isOptimalDataCollect=False, totalCommit=0, hasLatestPullReq=False):
        updatetimestamp = latestCommit["commit"]["author"]["date"]
        dt = parser.parse(updatetimestamp)
        fromDateTime = dt + datetime.timedelta(seconds=0o1)
        fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%SZ')
        if branchName in trackingDetails:
            trackingDetails[branchName]['latestCommitDate'] = fromDateTime
            trackingDetails[branchName]['latestCommitId'] = latestCommit['sha']
        else:
            trackingDetails[branchName] = {'latestCommitDate': fromDateTime, 'latestCommitId': latestCommit["sha"]}
        branchTrackingDetails = trackingDetails[branchName]
        if branchName == repoDefaultBranch:
            branchTrackingDetails['default'] = True
        else:
            branchTrackingDetails['default'] = False
        if isOptimalDataCollect:
            branchTrackingDetails['totalCommit'] = branchTrackingDetails.get('totalCommit', 0) + totalCommit
            if not hasLatestPullReq:
                branchTrackingDetails['commitCount'] = branchTrackingDetails.get('commitCount', 0) + totalCommit
            elif hasLatestPullReq:
                branchTrackingDetails['commitCount'] = totalCommit

    def updateTrackingForBranchCreateDelete(self, trackingDetails, repoName, branchName, lastCommitDate, lastCommitId):
        trackingDetails = self.tracking.get(repoName,None)
        data_branch_delete=[]
        branch_delete = {}
        branch_delete['branchName'] = branchName
        branch_delete['repoName'] = repoName
        branch_delete['event'] = "branchDeletion"
        #branch_delete['lastCommitDate'] = lastCommitDate
        #branch_delete['lastCommitId'] = lastCommitId
        data_branch_delete.append(branch_delete)
        branchMetadata = {"labels": ["METADATA"], "dataUpdateSupported": True, "uniqueKey": ["repoName", "branchName"]}
        self.publishToolsData(data_branch_delete, branchMetadata)


if __name__ == "__main__":
    GitAgent()
