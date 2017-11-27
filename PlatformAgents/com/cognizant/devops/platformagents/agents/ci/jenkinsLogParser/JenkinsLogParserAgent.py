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
Created on Oct 26, 2017

@author: vganjare
'''
import requests
from requests.auth import HTTPBasicAuth
import json
import copy
from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent


class JenkinsLogParserAgent(JenkinsAgent):         
    
    def processLogParsing(self, buildDetails):
        dataList = []
        for build in buildDetails:
            buildUrl = build['buildUrl']
            logUrl = buildUrl + "console"
            logResponse = self.getBuildLog(logUrl)
            logTokens = logResponse.split('****Start of Json Output****')
            buildAdded = False
            for logToken in logTokens:
                deploymentTokens = logToken.split('****End of Json Output****')
                if len(deploymentTokens) > 1:
                    data = copy.deepcopy(build)
                    dataList.append(data)
                    buildAdded = True
                    deploymentJsonStr = '{' + deploymentTokens[0].split('{')[1].split('}')[0] + '}'
                    buildJson = json.loads(deploymentJsonStr)
                    for attr in buildJson:
                        if data.get(attr, None) is None:
                            data[attr] = buildJson[attr]
            
            if '[Pipeline]' in logResponse:
                stageUrl = buildUrl + 'wfapi/describe'
                stageResponse = self.getResponse(stageUrl, 'GET', self.userid, self.passwd, None)
                if stageResponse:
                    stages = stageResponse.get('stages', None)
                    if stages:
                        for stage in stages:
                            stageData = copy.deepcopy(build)
                            buildAdded = True
                            stageData['stageName'] = stage.get('name', '')
                            stageData['stageStatus'] = stage.get('status', '')
                            stageData['stageStartTime'] = stage.get('name', '')
                            stageData['stageDuration'] = stage.get('name', '')
                            stageData['stagePauseDuration'] = stage.get('name', '')
                            dataList.append(stageData)
            if not buildAdded:
                dataList.append(build)
        return dataList
    
    def getBuildLog(self,url):
        auth = HTTPBasicAuth(self.userid, self.passwd)
        response = requests.get(url, auth=auth)
        return response.content

if __name__ == "__main__":
    JenkinsLogParserAgent()
