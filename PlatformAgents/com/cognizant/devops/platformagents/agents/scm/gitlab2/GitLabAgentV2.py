# -------------------------------------------------------------------------------
# Copyright 2022 Cognizant Technology Solutions
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
# -------------------------------------------------------------------------------

"""
Created on Aug 01, 2022

@author: 911215
"""

import datetime
import json
import os
import sys
import urllib.request
import urllib.parse
import urllib.error
from datetime import datetime as dateTime
from dateutil import parser
from ....core.BaseAgent3 import BaseAgent


class GitLabAgent2(BaseAgent):
    trackingCachePath = None

    @BaseAgent.timed
    def process(self):
        self.baseLogger.info("Inside GitLab Agent process")
        self.timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.getProjectsURL = self.config.get("getRepos", "")
        self.getCommitsBaseURL = self.config.get("commitsBaseEndPoint", "")
        self.accessToken = self.config.get("accessToken", "")
        startFromStr = self.config.get("startFrom", "")
        self.startFrom = parser.parse(startFromStr, ignoretz=True)
        self.dynamicTemplate = self.config.get("dynamicTemplate", {})
        self.metaData = self.dynamicTemplate.get("metaData", {})

        self.setupTrackingCachePath("trackingCache")

        self.processProjects()

    def processProjects(self):
        """Fetches the projects which has activity after the specified period and process them"""
        startFromFilter = self.startFrom.strftime("%Y-%m-%dT%H:%M:%SZ")
        restrictedProjects = self.dynamicTemplate.get("restrictedProjects", None)
        pageNumber = 1
        fetchNextPage = True
        defaultParams = "?access_token=" + self.accessToken + "&per_page=100&last_activity_after=" + startFromFilter + "&sort=asc&page="

        while fetchNextPage:
            try:
                projects = self.getResponse(self.getProjectsURL + defaultParams + str(pageNumber), "GET", None, None, None)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                projects = []
            if len(projects) == 0:
                fetchNextPage = False
                break
            for project in projects:
                projectPath = project.get("path_with_namespace", "")
                projectName = project.get("name", "")
                projectId = project.get("id", "")
                encodedProjectName = urllib.parse.quote_plus(projectPath)
                projectDefaultBranch = project.get("default_branch", None)

                if len(restrictedProjects) > 0 and projectName in restrictedProjects:
                    continue

                if projectDefaultBranch == None:
                    continue

                if not os.path.isfile(self.trackingCachePath + projectPath + ".json"):
                    self.updateTrackingCacheFile(projectPath, dict())

                projectTrackingCache = self.loadProjectTrackingCache(projectPath)
                projectLastActivityAt = project.get("last_activity_at", None)
                projectUpdatedAt = parser.parse(projectLastActivityAt, ignoretz=True)
                if self.startFrom < projectUpdatedAt:
                    projectDict = {
                        "id": projectId,
                        "name": projectName,
                        "path": projectPath,
                        "encodedName": encodedProjectName,
                        "defaultBranch": urllib.parse.quote_plus(projectDefaultBranch)
                    }
                    self.processBranches(projectDict, projectTrackingCache)
            if len(projects) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
                break

    def processBranches(self, projectDict, projectTrackingCache):
        """Fetches all the branches in the given projects and processes the data"""
        self.baseLogger.info("Inside Process Branch Details")
        pageNumber = 1
        fetchNextPage = True
        defaultParams = "?access_token=" + self.accessToken + "&per_page=100&page="
        allBranches = {projectDict["defaultBranch"]: False}
        branchesData = []

        while fetchNextPage:
            getBranchesURL = self.getCommitsBaseURL + projectDict["encodedName"] + "/repository/branches" + defaultParams + str(pageNumber)
            try:
                branches = self.getResponse(getBranchesURL, "GET", None, None, None)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                branches = []
            if len(branches) == 0:
                fetchNextPage = False
                break
            for branch in branches:
                branchName = branch["name"]
                encodedBranchName = urllib.parse.quote_plus(branchName)
                branchTrackingDetails = projectTrackingCache.get(encodedBranchName, {})
                branchLatestCommit = branchTrackingDetails.get("latestCommitId", None)

                if branchLatestCommit is None or branchLatestCommit != branch.get("commit", {}).get("id", None):
                    allBranches[encodedBranchName] = True
                else:
                    allBranches[encodedBranchName] = False
                if encodedBranchName not in projectTrackingCache:
                    branchDict = {
                        "branchName": branch["name"],
                        "projectName": projectDict["name"],
                        "projectPath": projectDict["path"],
                        "projectId": projectDict["id"],
                        "consumptionTime": self.timeStampNow()
                    }
                    branchesData.append(branchDict)
            if len(branches) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
                break
        if branchesData:
            self.publishBranchDetails(branchesData)
            self.baseLogger.info("Branches details published")

        for branch in allBranches:
            injectData = {
                "projectId": projectDict["id"],
                "projectName": projectDict["name"],
                "projectPath": projectDict["path"],
                "branchName": branch,
            }

            if branch == projectDict["defaultBranch"]:
                injectData["default"] = True
            else:
                injectData["default"] = False
            if allBranches[branch]:
                self.processCommits(projectDict["encodedName"], injectData, projectTrackingCache, projectDict["defaultBranch"])
            self.processMergeRequestDetails(projectDict, branch, projectTrackingCache)

    def publishBranchDetails(self, branchData):
        """Publishes branches data to the rabbitmq"""
        self.baseLogger.info("Publishing Branch details")
        branchMetaData = self.metaData.get("branch", {})
        branchesinsighstTimeX = self.dynamicTemplate.get("branch", {}).get("insightsTimeXFieldMapping", None)
        timestamp = branchesinsighstTimeX.get("timefield", None)
        timeformat = branchesinsighstTimeX.get("timeformat", None)
        isEpoch = branchesinsighstTimeX.get("isEpoch", False)
        self.publishToolsData(branchData, branchMetaData, timestamp, timeformat, isEpoch, True)

    def processCommits(self, encodedProjectName, injectData, projectTrackingCache, projectDefaultBranch):
        """Fetches the commits in the given branch and processes them"""
        self.baseLogger.info("Processing Commits")
        fetchNextPage = True
        commitsData = []
        defaultBranchCommitList = []
        encodedBranchName = injectData["branchName"]
        branchTrackingDetails = projectTrackingCache.get(injectData["branchName"], {})
        defaultBranchCommitList = projectTrackingCache.get(projectDefaultBranch, {}).get("defaultBranchCommitList", list())
        since = branchTrackingDetails.get("latestCommitDate", None)

        if not since:
            since = self.startFrom.strftime("%Y-%m-%dT%H:%M:%SZ")
        commitResponseTemplate = self.dynamicTemplate.get("commitResponseTemplate", {})
        commitMetaData = self.metaData.get("commit", {})
        latestCommit = None
        defaultParams = "?access_token=" + self.accessToken + "&ref_name=" + encodedBranchName + "&since=" + since + "&per_page=100&page="
        pageNumber = 1
        while fetchNextPage:
            getCommitsURL = self.getCommitsBaseURL + encodedProjectName + "/repository/commits" + defaultParams + str(pageNumber)
            try:
                commits = self.getResponse(getCommitsURL, "GET", None, None, None)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                commits = []
            if len(commits) == 0:
                fetchNextPage = False
                break
            if latestCommit is None and len(commits) > 0:
                latestCommit = commits[0]
            for commit in commits:
                commitId = commit.get("id", None)
                if len(defaultBranchCommitList) > 0 and commitId in defaultBranchCommitList:
                    continue
                injectData.update({
                    "gitType": "commit",
                })
                injectData["consumptionTime"] = self.timeStampNow()

                if injectData["default"]:
                    defaultBranchCommitList.append(commitId)
                commitsData += self.parseResponse(
                    commitResponseTemplate, commit, injectData)
            if len(commits) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
                break
        if commitsData:
            self.publishToolsData(commitsData, commitMetaData)
            self.baseLogger.info("Branch commit details published")
        if latestCommit:
            self.updateCommitDetailsInTracking(injectData, latestCommit, projectTrackingCache, defaultBranchCommitList)
            self.updateTrackingCache(injectData["projectPath"], projectTrackingCache)
            self.baseLogger.info("Branch commit details updated in tracking")

    def updateCommitDetailsInTracking(self, injectData, latestCommit, projectTrackingCache, defaultBranchCommitList):
        """Updates the commit details in project tracking cache"""
        self.baseLogger.info("Inside updateCommitDetailsInTracking")
        updatetimestamp = latestCommit["committed_date"]
        dt = parser.parse(updatetimestamp)
        fromDateTime = dt + datetime.timedelta(seconds=0o1)
        fromDateTime = fromDateTime.strftime("%Y-%m-%dT%H:%M:%SZ")
        if injectData["branchName"] in projectTrackingCache:
            projectTrackingCache[injectData["branchName"]]["latestCommitDate"] = fromDateTime
            projectTrackingCache[injectData["branchName"]]["latestCommitId"] = latestCommit["id"]
        else:
            projectTrackingCache[injectData["branchName"]] = {"latestCommitDate": fromDateTime,
                                                              "latestCommitId": latestCommit["id"]}
        if injectData["default"]:
            projectTrackingCache[injectData["branchName"]]["defaultBranchCommitList"] = defaultBranchCommitList

    def processMergeRequestDetails(self, projectDict, branchName, projectTrackingCache):
        """Fetches merge request details from the given branch and processes it"""
        self.baseLogger.info("Processing Merge Request Details")
        pageNumber = 1
        fetchNextPage = True
        branchTrackingDetails = projectTrackingCache.get(branchName, {})
        latestMergeRequest = None
        lastTrackedTimeStr = branchTrackingDetails.get("mergeReqModificationTime", self.startFrom)
        mergeReqEndpoint = self.getCommitsBaseURL + projectDict["encodedName"] + "/merge_requests"
        defaultParams = "?access_token=" + self.accessToken + "&state=all&sort=desc&order_by=updated_at&updated_after=" + str(lastTrackedTimeStr) + "&source_branch=" + branchName + "&per_page=100&page="
        mergeReqData = []
        mergeReqResponseTemplate = self.dynamicTemplate.get("mergeReqResponseTemplate", {})
        mergeReqMetaData = self.dynamicTemplate.get("metaData", {}).get("mergeRequest", {})

        while fetchNextPage:
            mergeReqURL = mergeReqEndpoint + defaultParams + str(pageNumber)
            try:
                mergeRequestList = self.getResponse(mergeReqURL, "GET", None, None, None)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                mergeRequestList = []
            if len(mergeRequestList) == 0:
                fetchNextPage = False
                break
            if latestMergeRequest is None and len(mergeRequestList) > 0:
                latestMergeRequest = mergeRequestList[0]
            for mergeRequest in mergeRequestList:
                if "mergeReqModificationTime" in branchTrackingDetails and branchTrackingDetails["mergeReqModificationTime"] == mergeRequest["updated_at"]:
                    continue
                injectData = {
                    "projectName": projectDict["name"],
                    "projectId": projectDict["id"],
                    "projectPath": projectDict["path"],
                    "mergeRequestNumber": mergeRequest.get("iid", 0),
                    "gitType": "mergeRequest",
                    "consumptionTime": self.timeStampNow(),
                    "authorName": mergeRequest.get("author", None).get("name", None),
                }

                if mergeRequest.get("state", None) == "merged":
                    injectData["mergedBy"] = mergeRequest.get("merged_by", {}).get("name", None)

                mergeReqData += self.parseResponse(mergeReqResponseTemplate, mergeRequest, injectData)
                self.baseLogger.info("Merger req data")
            if len(mergeRequestList) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
                break

        if mergeReqData:
            mergeReqinsighstTimeX = self.config.get("dynamicTemplate", {}).get("mergeRequest", {}).get("insightsTimeXFieldMapping", None)
            timestamp = mergeReqinsighstTimeX.get("timefield", None)
            timeformat = mergeReqinsighstTimeX.get("timeformat", None)
            isEpoch = mergeReqinsighstTimeX.get("isEpoch", False)
            self.publishToolsData(mergeReqData, mergeReqMetaData, timestamp, timeformat, isEpoch, True)
            self.updateMergeRequestDetailsInTracking(branchName, latestMergeRequest, projectTrackingCache)
            self.updateTrackingCache(injectData["projectPath"], projectTrackingCache)
            self.baseLogger.info("Branch merge request details updated in tracking ")

    def updateMergeRequestDetailsInTracking(self, branchName, latestMergeRequest, projectTrackingCache):
        """Updates the merge request details in project tracking cache"""
        self.baseLogger.info("Inside updateMergeRequestDetailsInTracking")
        if branchName in projectTrackingCache:
            projectTrackingCache[branchName]["mergeReqModificationTime"] = latestMergeRequest["updated_at"]
            projectTrackingCache[branchName]["latestMergeReqId"] = latestMergeRequest["iid"]
            projectTrackingCache[branchName]["latestMergeReqState"] = latestMergeRequest["state"]
        else:
            projectTrackingCache[branchName] = {"mergeReqModificationTime": latestMergeRequest["updated_at"],
                                                "latestMergeReqId": latestMergeRequest["iid"],
                                                "latestMergeReqState": latestMergeRequest["state"]}

    def setupTrackingCachePath(self, folderName):
        """Setups the tracking cache folder"""
        self.baseLogger.info("Inside TrackingCache Path Setup")
        self.trackingCachePath = os.path.dirname(
            sys.modules[self.__module__].__file__) + os.path.sep + folderName + os.path.sep
        if not os.path.exists(self.trackingCachePath):
            os.mkdir(self.trackingCachePath)

    def updateTrackingCacheFile(self, projectPath, trackingDict):
        """Updates the tracking cache of the provided project"""
        filePath = self.trackingCachePath + projectPath
        if not os.path.exists(os.path.dirname(filePath)):
            try:
                os.makedirs(os.path.dirname(filePath))
            except Exception as err:
                self.baseLogger.error(err)
        with open(self.trackingCachePath + projectPath + ".json", "w") as filePointer:
            json.dump(trackingDict, filePointer)

    def loadProjectTrackingCache(self, projectPath):
        """Loads the tracking cache"""
        self.baseLogger.info("Loading " + projectPath + " tracking cache")
        file = self.trackingCachePath + projectPath + ".json"
        with open(file, "r") as filePointer:
            data = json.load(filePointer)
        return data

    def updateTrackingCache(self, filePath, trackingDict):
        """Updates the tracking cache"""
        self.baseLogger.info("Inside UpdateTrackingCache")
        with open(self.trackingCachePath + filePath + ".json", "w") as filePointer:
            json.dump(trackingDict, filePointer, indent=4)


if __name__ == "__main__":
    GitLabAgent2()
