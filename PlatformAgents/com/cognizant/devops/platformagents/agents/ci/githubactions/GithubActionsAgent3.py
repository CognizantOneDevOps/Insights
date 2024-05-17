#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
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
Created on Feb 16th, 2024

@author: 911242
'''
from cProfile import run
import datetime
from datetime import datetime as dateTime
import sys
from dateutil import parser
from ....core.BaseAgent3 import BaseAgent

class GithubActionsAgent(BaseAgent):

    @BaseAgent.timed
    def process(self):
        self.baseLogger.info("Inside GithubActions Agent process")
        self.timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.baseEndPoint = self.config.get("baseEndPoint", "")
        self.accessToken = self.config.get("accessToken", "")
        self.headers = {"Authorization": "token " + self.accessToken}
        startFromStr = self.config.get("startFrom", "")
        self.startFrom = parser.parse(startFromStr, ignoretz=True)
        self.dynamicTemplate = self.config.get("dynamicTemplate", {})
        self.metaData = self.dynamicTemplate.get("metaData", {})
        self.jobStepsRelationMetaData = self.dynamicTemplate.get("relationMetadata", {})
        self.startFromFilter = self.startFrom.strftime("%Y-%m-%dT%H:%M:%SZ")
        self.defaultParams = "?per_page=100&created=>=" + self.startFromFilter + "&sort=asc&page="
        self.processRepository()


    def processRepository(self):
        """Fetching the repository details in an organization and
         processing workflows of the respective repository"""
        selectedRepositories = self.dynamicTemplate.get("selectedRepositories", None)
        pageNumber = 1
        fetchNextPage = True
        try:
            while fetchNextPage:
                repositories = self.getResponse(self.baseEndPoint, 'GET', None, None, None,reqHeaders=self.headers)
                if len(repositories) == 0:
                    fetchNextPage = False
                    break
                for repository in repositories:
                    repositoryURL = repository.get("url", "")
                    repositoryName = repository.get("name", "")
                    if len(selectedRepositories)>0 and repositoryName not in selectedRepositories:
                        continue
                    projectTracking = self.tracking.get(repositoryName, {})
                    self.processWorkflows(repositoryURL, repositoryName)
                    runsURL = repositoryURL+"/actions/runs"
                    self.processRunners(runsURL, repositoryName, projectTracking)
                if len(repositories) >= 100:
                    pageNumber+=1
                else:
                    fetchNextPage = False
                    break
        except Exception as ex:
            exceptionMessage = str(ex)
            self.baseLogger.info("Exception occured in processRepository ", exceptionMessage)
            self.publishHealthDataForExceptions(ex)

    def processWorkflows(self,repositoryURL, repositoryName):
        """Fetching properties of an each workflow and publishing it"""
        repoPageNum = 1
        fetchNextPage = True
        workflowResponseTemplate = self.dynamicTemplate.get("workflowResponseTemplate", {})
        workflowData = []
        injectData = dict()
        workflowsURL = repositoryURL+"/actions/workflows"
        try:
            while fetchNextPage:
                githubActionsWorkFlows = self.getResponse(workflowsURL, 'GET', None, None, None, reqHeaders=self.headers)
                workflows = githubActionsWorkFlows.get('workflows', {})
                workflowTotalCount = githubActionsWorkFlows.get("total_count", 0)
                if len(workflows) == 0 and workflowTotalCount == 0:
                    self.baseLogger.info("No GitHub Actions available in this repository "+repositoryName)
                    fetchNextPage = False
                    break
                else:
                    for workflow in workflows:
                        workflowCreatedAt = workflow.get('created_at',None)
                        #The orginal format is eg:2024-02-26T11:27:12.000+05:30 we are formatting this to 2024-02-12T10:39:27.000000Z for calculation
                        createdParsedTime = datetime.datetime.strptime(workflowCreatedAt,"%Y-%m-%dT%H:%M:%S.%f%z")
                        workflowUpdatedAt = workflow.get('updated_at', None)
                        updatedParsedTime = datetime.datetime.strptime(workflowUpdatedAt,"%Y-%m-%dT%H:%M:%S.%f%z")
                        totalDurationInSeconds = (updatedParsedTime - createdParsedTime).total_seconds()
                        injectData = {
                                "repositoryName": repositoryName,
                                "workflowName":   workflow.get("name", ""),
                                "workflowID": workflow.get("id", None),
                                "workflowTotalCount":workflowTotalCount,
                                "workflowCreatedAt": workflowCreatedAt,
                                "workflowUpdatedAt": workflowUpdatedAt,
                                "totalDurationInSeconds":totalDurationInSeconds
                            }
                        workflowData+=self.parseResponse(workflowResponseTemplate,workflow, injectData)   
                    if len(workflows) >= 100:
                        repoPageNum+=1
                    else:
                        fetchNextPage = False
                    if workflowData:
                        self.publishDataWithTimeStamp("workflows", workflowData)
                        self.baseLogger.info("workflow details published")
                    else:
                        self.baseLogger.info("Workflow data is not available, hence its not published!")
                        break
        except Exception as ex:
            exceptionMessage = str(ex)
            self.baseLogger.info("Exception occured in processWorkflows ", exceptionMessage)
            self.publishHealthDataForExceptions(ex)
    
    def processRunners(self,runsURL, repositoryName, projectTracking):
        """Fetching runs data and publishing it"""
        repoPageNum = 1
        fetchNextPage = True
        runnersData = []
        injectData = dict()
        runsResponseTemplate = self.dynamicTemplate.get("runsResponseTemplate",{})
        try:
            if not projectTracking.get("lastRunTime", None):
                startFrom = self.startFromFilter
            else:
                startFrom = projectTracking.get("lastRunTime")
            self.defaultParams = "?per_page=100&created=>=" + startFrom + "&sort=asc&page="
            while fetchNextPage:
                githubActionsRuns = self.getResponse(runsURL+self.defaultParams+ str(repoPageNum), 'GET', None, None, None, reqHeaders=self.headers)
                workflowRuns = githubActionsRuns.get('workflow_runs', {})
                runsTotalCount = githubActionsRuns.get("total_count", 0)
                if len(workflowRuns) == 0 and runsTotalCount == 0:
                    self.baseLogger.info("No GitHub Actions Workflows available")
                    fetchNextPage = False
                    break
                else:
                    for run in workflowRuns:
                        runCreatedAt = run.get('created_at', None)
                        runUpdatedAt = run.get('updated_at', None)
                        createdAt = datetime.datetime.strptime(runCreatedAt, "%Y-%m-%dT%H:%M:%SZ")
                        updatedAt = datetime.datetime.strptime(runUpdatedAt, "%Y-%m-%dT%H:%M:%SZ")
                        totalDurationInSeconds = (updatedAt - createdAt).total_seconds()
                        injectData = {
                            "runUpdatedAt" : runUpdatedAt,
                            "totalDurationInSeconds":totalDurationInSeconds
                            }
                        runnersData+= self.parseResponse(runsResponseTemplate, run, injectData)
                    if len(workflowRuns) >= 100:
                        repoPageNum+= 1
                    else:
                        fetchNextPage = False
                if runnersData:
                    self.publishDataWithTimeStamp("runs", runnersData)
                    self.baseLogger.info("Runs details published")
                    """passing runs data to process jobs"""
                    for runs in runnersData:
                        self.processJobs(runs)
                else:
                    self.baseLogger.info("Runs data is not available, hence its not published!")
            projectTracking["lastRunTime"] = str(self.timeStampNow())
            self.tracking[repositoryName] = projectTracking
            self.updateTrackingJson(self.tracking)
        except Exception as ex:
            exceptionMessage = str(ex)
            self.baseLogger.info("Exception occured in processRunners ", exceptionMessage)
            self.publishHealthDataForExceptions(ex)

    def processJobs(self, runsPropertiesList):
        """Fetching jobs data and publishing it"""
        repoPageNum = 1
        fetchNextPage = True
        jobData = []
        stepsData = []
        jobSteps = []
        injectData = dict()
        stepsInjectData = dict()
        jobsResponseTemplate = self.dynamicTemplate.get("jobsResponseTemplate",{})
        jobStepsResponseTemplate = self.dynamicTemplate.get("jobStepsResponseTemplate",{})
        jobsURL = runsPropertiesList["jobsURL"]
        runID = runsPropertiesList.get("runID", "")
        jobID = 0
        try:
            while fetchNextPage:
                githubActionsJobs = self.getResponse(jobsURL, 'GET', None, None, None, reqHeaders=self.headers)
                workflowJobs = githubActionsJobs.get('jobs', None)
                jobsTotalCount = githubActionsJobs.get("total_count", None)
                if len(workflowJobs) == 0 and jobsTotalCount == 0:
                    self.baseLogger.info("No GitHub Actions available")
                    fetchNextPage = False
                    break
                else:
                    for job in workflowJobs:
                        completedAt = job.get("completed_at", "")
                        convertCompletedTimeStamp = datetime.datetime.strptime(completedAt, "%Y-%m-%dT%H:%M:%SZ")
                        totalDurationInSeconds = convertCompletedTimeStamp.second
                        jobSteps+=job.get("steps",{})
                        jobID = job.get("id","")
                        injectData = {
                                "completedAt":completedAt,
                                "totalDurationInSeconds":totalDurationInSeconds
                            }
                        jobData+=self.parseResponse(jobsResponseTemplate, job, injectData)
                    for step in jobSteps:
                            stepsInjectData = {
                                "jobID":jobID,
                                "runID":runID
                            }
                            stepsData+=self.parseResponse(jobStepsResponseTemplate,step,stepsInjectData)
                    if len(workflowJobs) >= 100:
                        repoPageNum+=1
                    else:
                        fetchNextPage = False
                if jobData:
                    self.publishDataWithTimeStamp("jobs", jobData)
                    self.baseLogger.info("Jobs details published")
                    self.publishDataWithTimeStamp("steps", stepsData)
                    self.baseLogger.info("Job Steps detail published")
                else:
                    self.baseLogger.info("Jobs data is not available, hence its not published!")
        except Exception as ex:
            exceptionMessage = str(ex)
            self.baseLogger.info("Exception Occured in processJobs ", exceptionMessage)
            self.publishHealthDataForExceptions(ex)
        
    def publishDataWithTimeStamp(self, event, dataToPublish):
        """Publish the data with the appropriate InsightsTimeX TimeStamp format"""
        self.baseLogger.debug("Event to publish ", event)
        insightTimeX = self.dynamicTemplate.get(event, {}).get("insightsTimeXFieldMapping", None)
        timestamp = insightTimeX.get("timefield", None)
        timeformat = insightTimeX.get("timeformat", None)
        isEpoch = insightTimeX.get("isEpoch", False)
        relationShipMetaData = self.jobStepsRelationMetaData.get("jobSteps", {})
        if dataToPublish and event=='steps':
            self.publishToolsData(dataToPublish, relationShipMetaData, timestamp, timeformat, isEpoch, True)
            self.baseLogger.debug(event, " details published")
        else:
            self.publishToolsData(dataToPublish, self.metaData.get(event, {}), timestamp, timeformat, isEpoch, True)
            self.baseLogger.debug(event, " details published")
if __name__ == "__main__":
    GithubActionsAgent()