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
from ....core.BaseAgent import BaseAgent

class SonarAgent(BaseAgent):
    def process(self):
        baseUrl = self.config.get("baseUrl", '')
        projectsUrl = baseUrl+"api/projects/index?format=json"
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        timeStampFormat = self.config.get('timeStampFormat')
        startFrom = startFrom.strftime(timeStampFormat)
        userId = self.getCredential("userid")
        password = self.getCredential("passwd")
        timeMachineapi = self.config.get("timeMachineapi", '')
        sonarProjects = self.getResponse(projectsUrl, 'GET', userId, password, None)
        metrics = self.config.get('dynamicTemplate', {}).get("metrics", '')
        metricsParam = ''
        if len(metrics) > 0:
            for metric in metrics:
                metricsParam += metric + ','

        for project in sonarProjects:
            data = []
            projectKey = project["k"]
            projectName = project["nm"]
            timestamp = self.tracking.get(projectKey, startFrom)
            lastUpdatedDate = None
            if timeMachineapi == "yes":
                sonarExecutionsUrl = baseUrl+"api/timemachine/index?metrics="+metricsParam+"&resource="+projectKey+"&fromDateTime="+timestamp+"-0000&format=json"
                sonarExecutions = self.getResponse(sonarExecutionsUrl, 'GET', userId, password, None)
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

            else:
                #Find out the artifact version from sonar project analysis api.
                #This api is available from sonar 6.3 version onwards. max records for the api is 500
                timestamp=timestamp.replace("+","%2B")
                pageIndex = 1
                totalPages = 1
                pageSize = 500
                totalRecords = 0
                projectDateVersionMap = {} # store map with data as key and project version as value
                while pageIndex <= totalPages:
                    sonarProjectAnalysisUrl = baseUrl+"api/project_analyses/search?category=VERSION&project="+projectKey+"&from="+timestamp+"-0000&format=json&ps="+str(pageSize)+"&p="+str(pageIndex)
                    projectAnalysis = self.getResponse(sonarProjectAnalysisUrl, 'GET', userId, password, None)
                    totalRecords = projectAnalysis["paging"]["total"]
                    totalPages =  (totalRecords/pageSize) + 1
                    if (totalRecords%pageSize) == 0:
                        totalPages =  (totalRecords/pageSize)

                    analysisArray = projectAnalysis["analyses"]
                    for analysis in analysisArray:
                        date = analysis["date"]
                        category = analysis["events"][0]["category"]
                        version = analysis["events"][0]["name"]
                        if category == "VERSION" and version:
                            # remove appname from Version if present
                            version1 = version.replace(projectName+"-","")
                            projectDateVersionMap[date] = version1

                    #Process for next Page
                    pageIndex += 1

                # Get the Sonar Project past executions from history api.
                pageIndex = 1
                totalPages = 1
                pageSize = 1000
                totalRecords = 0
                while pageIndex <= totalPages:
                    sonarExecutionsUrl = baseUrl+"api/measures/search_history?metrics="+metricsParam+"&component="+projectKey+"&from="+timestamp+"-0000&format=json&ps="+str(pageSize)+"&p="+str(pageIndex)
                    sonarExecutions = self.getResponse(sonarExecutionsUrl, 'GET', userId, password, None)
                    totalRecords = sonarExecutions["paging"]["total"]
                    totalPages =  (totalRecords/pageSize) + 1
                    if (totalRecords%pageSize) == 0:
                        totalPages =  (totalRecords/pageSize)

                    for historydata in range(0,len(sonarExecutions['measures'][0]['history'])):
                        executionData={}
                        analysisDate = sonarExecutions['measures'][0]['history'][historydata]['date']
                        buildVersion = projectDateVersionMap.get(analysisDate,"") # Use get to default value if ket is not found
                        executionData['resourcekey'] = projectKey
                        executionData["projectName"] = projectName
                        executionData['metricdate'] = analysisDate
                        executionData['buildVersion'] = buildVersion
                        for i_metric_length in range(0,len(sonarExecutions['measures'])):
                            if 'value' in sonarExecutions['measures'][i_metric_length]['history'][historydata]:
                                executionData[sonarExecutions['measures'][i_metric_length]['metric']]=sonarExecutions['measures'][i_metric_length]['history'][historydata]['value']
                        data.append(executionData)
                        lastUpdatedDate = executionData['metricdate']

                    #Process for next Page
                    pageIndex += 1

            if lastUpdatedDate:
                lastUpdatedDate = lastUpdatedDate[:self.dateTimeLength]
                dt = datetime.strptime(lastUpdatedDate, timeStampFormat)
                fromDateTime = dt + timedelta(seconds=01)
                fromDateTime = fromDateTime.strftime(timeStampFormat)
                self.tracking[projectKey] = fromDateTime
            self.publishToolsData(data)
            self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    SonarAgent()
