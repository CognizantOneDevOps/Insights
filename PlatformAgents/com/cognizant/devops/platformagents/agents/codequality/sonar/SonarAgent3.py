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
Created on Jul 1, 2016

@author: 463188
'''
from dateutil import parser
from datetime import datetime
from datetime import timedelta
from ....core.BaseAgent3 import BaseAgent

import json

class SonarAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        self.baseUrl = self.config.get("baseUrl", '')
        timeStampFormat = self.config.get('timeStampFormat')    
        startFrom = parser.parse(self.config.get("startFrom", '')).strftime(timeStampFormat)
        self.userid = self.getCredential("userid")
        self.passwd = self.getCredential("passwd")
        timeMachineapi = self.config.get("timeMachineapi", '')
        self.isActivityNeeded = self.config.get('dynamicTemplate', {}).get("isActivityNeeded",'') 
        self.activityTasks = {}
        if self.isActivityNeeded:
            activityUrl = self.baseUrl+"api/ce/activity"
            activity = self.getResponse(activityUrl, 'GET', self.userid, self.passwd, None)
            self.activityTasks = activity["tasks"]
        metrics = self.config.get('dynamicTemplate', {}).get("metrics", '')
        projectsList = self.config.get('dynamicTemplate', {}).get("projects", [])
        metricsParam = ''
        projectsPageIndex = 1
        projectsPageSize = 500

        try:
            if len(metrics) > 0:
                for metric in metrics:
                    metricsParam += metric + ','
            nextPageResponse = True
            while nextPageResponse:
                projectsUrl = self.baseUrl+"api/components/search?qualifiers=TRK&format=json&ps={}&p={}"
                sonarProjects = self.getResponse(projectsUrl.format(projectsPageSize, projectsPageIndex), 'GET', self.userid, self.passwd, None)
                projectsTotalRecords = sonarProjects["paging"]["total"]
                for project in sonarProjects["components"]: 
                    if len(projectsList)>0 and projectsList[0]!="all" and not project["name"] in projectsList:
                        continue
                        
                    data = []
                    projectKey = project["key"]
                    projectName = project["name"]
                    self.baseLogger.info("Project Name => " + projectName)
                    timestamp = self.tracking.get(projectKey, startFrom)
                    lastUpdatedDate = None
                    if timeMachineapi == "yes":
                        sonarExecutionsUrl = self.baseUrl+"api/timemachine/index?metrics="+metricsParam+"&resource="+projectKey+"&fromDateTime="+timestamp+"-0000&format=json"
                        sonarExecutions = self.getResponse(sonarExecutionsUrl, 'GET', self.userid, self.passwd, None)
                        lastUpdatedDate = self.timeMachine(sonarExecutions,projectKey,projectName)                        
                    else:
                        #Find out the artifact version from sonar project analysis api. 
                        #This api is available from sonar 6.3 version onwards. max records for the api is 500 
                        analysisDateIdMap, projectDateVersionMap = self.projectAnalysesApi(projectKey, projectName, timestamp)
                      
                        # Get the Sonar Project branch Detail past executions from history api. 
                        sonarProjectBranchUrl = self.baseUrl+"api/project_branches/list?project="+projectKey
                        sonarBranchsExecutions = self.getResponse(sonarProjectBranchUrl, 'GET', self.userid, self.passwd, None)
                        branchsArray = sonarBranchsExecutions['branches']
                        for branch in branchsArray:
                            branchName = branch['name']
                            data, lastUpdatedDate = self.searchHistoryApi(metricsParam, projectKey, timestamp, projectName, projectDateVersionMap, branchName, analysisDateIdMap, data)
                    if lastUpdatedDate:
                        lastUpdatedDate = lastUpdatedDate[:self.dateTimeLength]
                        dt = datetime.strptime(lastUpdatedDate, timeStampFormat)
                        fromDateTime = dt + timedelta(seconds=0o1)
                        fromDateTime = fromDateTime.strftime(timeStampFormat)
                        self.tracking[projectKey] = fromDateTime
                    self.publishToolsData(data)
                    self.updateTrackingJson(self.tracking)
                if projectsTotalRecords < projectsPageSize * projectsPageIndex:
                    nextPageResponse = False
                else:
                    projectsPageIndex += 1
        except Exception as ex:
            self.baseLogger.info("Execption occured => " + ex)
                
    def timeMachine(self, sonarExecutions, projectKey, projectName):
        lastUpdatedDate = ''
        try:
            for sonarExecution in sonarExecutions:
                metricsColumns = []
                cols = sonarExecution['cols']
                for col in cols:
                    metricsColumns.append(col['metric'])
                cells = sonarExecution['cells']
                for cell in cells:
                    executionData = {}
                    executionData['metricdate'] = cell['d']
                    executionData["resourcekey"] = projectKey
                    executionData["projectName"] = projectName
                    metricValues = cell['v']
                    for i in range(len(metricValues)):
                        executionData[metricsColumns[i]] = metricValues[i]
                    data.append(executionData)
                    lastUpdatedDate = executionData['metricdate']
        except Exception as ex:
            self.baseLogger.info("Execption occured => " + ex)
        return lastUpdatedDate    
        
    def projectAnalysesApi(self, projectKey, projectName, timestamp): 
        analysisDateIdMap = {}    
        timestamp=timestamp.replace("+","%2B")
        pageIndex = 1
        pageSize = 500
        nextPageResponse = True
        projectDateVersionMap = {} # store map with data as key and project version as value
        try:
            while nextPageResponse:
                sonarProjectAnalysisUrl = self.baseUrl+"api/project_analyses/search?category=VERSION&project="+projectKey+"&from="+timestamp+"-0000&format=json&ps="+str(pageSize)+"&p="+str(pageIndex)
                projectAnalysis = self.getResponse(sonarProjectAnalysisUrl, 'GET', self.userid, self.passwd, None) 
                totalRecords = projectAnalysis["paging"]["total"]
               
                analysisArray = projectAnalysis["analyses"]
                analysisDateIdMap = {}
                for analysis in analysisArray:
                    date = analysis["date"]
                    category = analysis["events"][0]["category"]
                    version = analysis["events"][0]["name"]
                    analysisDateIdMap[date] = analysis["key"]
                    if category == "VERSION" and version:    
                        # remove appname from Version if present
                        version1 = version.replace(projectName+"-","")
                        projectDateVersionMap[date] = version1
                if totalRecords < pageSize * pageIndex:
                    nextPageResponse = False
                else:
                    #Process for next Page
                    pageIndex += 1
        except Exception as ex:
            self.baseLogger.info("Execption occured => " + ex)
        return analysisDateIdMap, projectDateVersionMap
    
    def searchHistoryApi(self, metricsParam, projectKey, timestamp, projectName, projectDateVersionMap, branchName, analysisDateIdMap, data):        
        # Get the Sonar Project past executions from history api. 
        pageIndex = 1
        pageSize = 1000
        lastUpdatedDate = ''
        nextPageResponse = True
        try:            
            while nextPageResponse:
                sonarExecutionsUrl = self.baseUrl+"api/measures/search_history?metrics="+metricsParam+"&component="+projectKey+"&from="+timestamp+"-0000&format=json&ps="+str(pageSize)+"&p="+str(pageIndex)+"&branch="+branchName
                sonarExecutions = self.getResponse(sonarExecutionsUrl, 'GET', self.userid, self.passwd, None)
                totalRecords = sonarExecutions["paging"]["total"]
                
                for historydata in range(0,len(sonarExecutions['measures'][0]['history'])):
                    executionData={}
                    analysisDate = sonarExecutions['measures'][0]['history'][historydata]['date']
                    buildVersion = projectDateVersionMap.get(analysisDate,"") # Use get to default value if ket is not found 
                    executionData['resourcekey'] = projectKey
                    executionData["projectName"] = projectName
                    executionData['branchName']=branchName
                    executionData['metricdate'] = analysisDate
                    executionData['buildVersion'] = buildVersion
                    executionData['analysisId'] = analysisDateIdMap.get(analysisDate)
                    if self.isActivityNeeded:
                        for task in self.activityTasks:
                            if task.get("analysisId") != None and task["analysisId"] == analysisDateIdMap.get(analysisDate):
                                executionData["executionId"] = task["id"]
                    for i_metric_length in range(0,len(sonarExecutions['measures'])):                    
                        if len(sonarExecutions['measures'][i_metric_length]['history'])>0:    
                            if len(sonarExecutions['measures'][i_metric_length]['history'])>historydata and 'value' in sonarExecutions['measures'][i_metric_length]['history'][historydata]:
                                executionData[sonarExecutions['measures'][i_metric_length]['metric']]=sonarExecutions['measures'][i_metric_length]['history'][historydata]['value']                                     
                    data.append(executionData)  
                    lastUpdatedDate = executionData['metricdate']
                if totalRecords < pageSize * pageIndex:
                    nextPageResponse = False
                else:
                    #Process for next Page
                    pageIndex += 1
                lastUpdatedDate = datetime.today().strftime("%Y-%m-%dT%H:%M:%S")
        except Exception as ex:
            self.baseLogger.info("Execption occured => " + ex)
        return data, lastUpdatedDate
    
if __name__ == "__main__":
    SonarAgent()