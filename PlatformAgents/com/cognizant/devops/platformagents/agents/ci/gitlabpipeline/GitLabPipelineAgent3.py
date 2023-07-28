# -------------------------------------------------------------------------------
# Copyright 2023 Cognizant Technology Solutions
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
'''
Created on 18 May 2023

@author: 911215
'''

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


class GitLabPipelineAgent(BaseAgent):
    @BaseAgent.timed
    def process(self):
        self.baseLogger.info("Inside GitLabPipeline Agent process")
        self.timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.baseEndPoint = self.config.get("baseEndPoint", "")
        self.accessToken = self.config.get("accessToken", "")
        startFromStr = self.config.get("startFrom", "")
        self.startFrom = parser.parse(startFromStr, ignoretz=True)
        self.dynamicTemplate = self.config.get("dynamicTemplate", {})
        self.metaData = self.dynamicTemplate.get("metaData", {})
        self.pipelineJobsRelationMetaData = self.dynamicTemplate.get("relationMetaData", {})
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
                projects = self.getResponse(self.baseEndPoint + "/projects" + defaultParams + str(pageNumber), "GET", None, None, None)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                projects = []
            if len(projects) == 0:
                fetchNextPage = False
                break
            for project in projects:
                projectPath = project.get("path_with_namespace", "")
                group = projectPath.split("/")[0]
                projectName = project.get("name", "")
                projectId = project.get("id", "")
                encodedProjectName = urllib.parse.quote_plus(projectPath)

                if len(restrictedProjects) > 0 and projectName in restrictedProjects:
                    continue

                if not os.path.isfile(self.trackingCachePath + projectPath + ".json"):
                    self.updateTrackingCacheFile(projectPath, dict())

                projectTrackingCache = self.loadProjectTrackingCache(projectPath)
                projectLastActivityAt = project.get("last_activity_at", None)
                projectUpdatedAt = parser.parse(projectLastActivityAt, ignoretz=True)
                if self.startFrom < projectUpdatedAt:
                    projectDict = {
                        "projectId": projectId,
                        "projectName": projectName,
                        "groupName" : group,
                        "path": projectPath,
                        "encodedName": encodedProjectName,
                    }
                    self.processPipelines(projectDict, projectTrackingCache)
            if len(projects) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
                break
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

    def processPipelines(self, projectDict, projectTrackingCache):
        """Fetches all the pipeline in the given projects and processes the data"""
        reqHeaders = {
                        "PRIVATE-TOKEN" : self.accessToken,
                      }
        pageNumber = 1
        fetchNextPage = True
        defaultParams = "?per_page=100&order_by=updated_at&sort=desc&page="
        pipelinesData = []
        pipelinesJobsData = []
        projectPath = projectDict["path"]

        while fetchNextPage:
            try:
                pipelines = self.getResponse(self.baseEndPoint + "/projects/" + str(projectDict["projectId"]) +"/pipelines" + defaultParams + str(pageNumber), "GET", None, None, None, reqHeaders=reqHeaders)
            except Exception as ex:
                self.publishHealthDataForExceptions(ex)
                pipelines = []
            if len(pipelines) == 0:
                fetchNextPage = False
                break
            for pipeline in pipelines:

                pipelineId = pipeline.get("id", "")
                pipelineTracking = projectTrackingCache.get(str(pipelineId), {})
                if not pipelineTracking.get("lastRunTime", None):
                    piplineTrackingUpdatedAt = self.startFrom
                else:
                    piplineTrackingUpdatedAt =  parser.parse(pipelineTracking.get("lastRunTime", None), ignoretz=True)
                    pipelineUpdatedAt = parser.parse(pipeline.get("updated_at", ""), ignoretz=True)

                if not pipelineTracking or piplineTrackingUpdatedAt < pipelineUpdatedAt:              
                    pipelineDict = {
                        "pipelineId" : pipelineId,
                        "status" : pipeline.get("status", ""),
                        "branch" : pipeline.get("ref", ""),
                        "createdAt" : pipeline.get("created_at", ""),
                        "updatedAt" : pipeline.get("updated_at", ""),
                        "webURL": pipeline.get("web_url", ""),
                        "groupName" : projectDict.get("groupName", ""),
                        "projectId" : projectDict.get("projectId", ""),
                        "projectName" : projectDict.get("projectName", ""),
                        "projectPath" : projectDict.get("path", ""),
                        "projectEncodedName": projectDict.get("encodedName", ""),
                    }
                    pipelinesData.append(pipelineDict)
                    projectTrackingCache[str(pipelineId)] = {
                        "lastRunTime" : self.timeStampNow()
                    }               
                else:
                    break                                   
            if len(pipelines) == 100:
                pageNumber = pageNumber + 1
            else:
                fetchNextPage = False
            if pipelinesData:
                try:
                    self.publishPipelinesDetails(pipelinesData, projectPath, projectTrackingCache)
                    self.baseLogger.info("Pipelines detail published")
                    for pipelineInfo in pipelinesData:
                        pipelinesJobsData += self.processJobs(pipelineInfo)
                    self.publishToolsData(pipelinesJobsData, self.pipelineJobsRelationMetaData)
                except Exception as ex:
                    self.baseLogger.error(ex)
                    self.baseLogger.info(pipelinesData)
    
    def publishPipelinesDetails(self, pipelinesData, projectPath, projectTrackingCache):
        """Publishes pipelines data to the Queue"""
        try:
            self.baseLogger.info("Publishing pipelines details")
            pipelinesMetaData = self.metaData.get("pipeline", {})
            pipelinesinsighstTimeX = self.dynamicTemplate.get("pipeline", {}).get("insightsTimeXFieldMapping", None)
            timestamp = pipelinesinsighstTimeX.get("timefield", None)
            timeformat = pipelinesinsighstTimeX.get("timeformat", None)
            isEpoch = pipelinesinsighstTimeX.get("isEpoch", False)
            self.publishToolsData(pipelinesData, pipelinesMetaData, timestamp, timeformat, isEpoch, True)
            self.updateTrackingCache(projectPath, projectTrackingCache)
        except Exception as ex:
            self.baseLogger.error(ex)

    def processJobs(self, pipelineInfo):
        """Fetches all the Jobs for the given pipeline and processes the data"""
        self.baseLogger.info("Processing Jobs for pipeline: " + str(pipelineInfo["projectId"]))
        reqHeaders = {
                        "PRIVATE-TOKEN" : self.accessToken,
                      }
        pageNumber = 1
        fetchNextPage = True
        defaultParams = "?per_page=100&sort=asc&page="
        jobsData = []
        try:
            while fetchNextPage:
                try:
                    jobs = self.getResponse(self.baseEndPoint + "/projects/" + str(pipelineInfo["projectId"]) +"/pipelines/" + str(pipelineInfo["pipelineId"]) +"/jobs" + defaultParams + str(pageNumber), "GET", None, None, None, reqHeaders=reqHeaders)
                except Exception as ex:
                    self.publishHealthDataForExceptions(ex)
                    jobs = []
                if len(jobs) == 0:
                    fetchNextPage = False
                    break
                for job in jobs:
                    commitJson=job.get("commit", {})
                    if commitJson:
                        committerEmail=commitJson.get("committer_email","")
                        committerName=commitJson.get("committer_name","")
                    else:
                        committerEmail=""
                        committerName=""
                    jobsDict = {
                        "jobId" : job.get("id", ""),
                        "jobName" : job.get("name", ""),
                        "stage" : job.get("stage", ""),
                        "status" : job.get("status", ""),
                        "startedAt" : job.get("started_at", ""),
                        "finishedAt" : job.get("finished_at", ""),
                        "duration" : job.get("duration", ""),
                        "committerName": committerName,
                        "committerEmail": committerEmail,
                        "pipelineId" : pipelineInfo.get("pipelineId", ""),
                        "groupName" : pipelineInfo.get("groupName", ""),
                        "projectId" : pipelineInfo.get("projectId", ""),
                        "projectName" : pipelineInfo.get("projectName", ""),
                        "projectPath" : pipelineInfo.get("projectPath", ""),
                        "projectEncodedName": pipelineInfo.get("projectEncodedName", "")
                    }
                    jobsData.append(jobsDict)
                if len(jobs) == 100:
                    pageNumber = pageNumber + 1
                else:
                    fetchNextPage = False
        except Exception as ex:
            self.baseLogger.error(ex)
        return jobsData
    
    def setupTrackingCachePath(self, folderName):
        """Setups the tracking cache folder"""
        self.baseLogger.info("Inside TrackingCache Path Setup")
        self.trackingCachePath = os.path.dirname(
            sys.modules[self.__module__].__file__) + os.path.sep + folderName + os.path.sep
        if not os.path.exists(self.trackingCachePath):
            os.mkdir(self.trackingCachePath)

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
    GitLabPipelineAgent()
