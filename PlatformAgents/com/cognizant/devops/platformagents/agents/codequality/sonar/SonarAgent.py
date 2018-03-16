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
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class SonarAgent(BaseAgent):
    def process(self):
        baseUrl = self.config.get("baseUrl", '')
        projectsUrl = baseUrl+"api/projects/index?format=json"
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        timeStampFormat = self.config.get('timeStampFormat')
        startFrom = startFrom.strftime(timeStampFormat)
        userId = self.config.get("userId", '')
        password = self.config.get("password", '')
        sonarProjects = self.getResponse(projectsUrl, 'GET', userId, password, None)
        metrics = self.config.get("metrics", '')
        metricsParam = ''
        if len(metrics) > 0:
            for metric in metrics:
                metricsParam += metric + ','
        data = []
        for project in sonarProjects:
            projectKey = project["k"]
            projectName = project["nm"]
            timestamp = self.tracking.get(projectKey, startFrom)
            sonarExecutionsUrl = baseUrl+"api/timemachine/index?metrics="+metricsParam+"&resource="+projectKey+"&fromDateTime="+timestamp+"&format=json"
            sonarExecutions = self.getResponse(sonarExecutionsUrl, 'GET', userId, password, None)
            lastUpdatedDate = None
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
