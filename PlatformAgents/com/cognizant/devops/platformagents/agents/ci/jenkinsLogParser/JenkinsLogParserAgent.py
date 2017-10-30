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
import logging
from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent

class JenkinsLogParserAgent(JenkinsAgent):
    
    def processBuildExecutions(self, url, tillJobCount, lastBuild, injectData):
        restUrl = url+'api/json?tree=builds[number,result]{0,100},name,fullDisplayName'
        jobDetails = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
        builds = jobDetails[self.buildsApiName]
        injectData['url'] = url
        injectData['fullDisplayName'] = jobDetails['fullDisplayName']
        injectData['jobName'] = jobDetails['name']
        parsedBuilds = []
        try:
            for build in builds:
                buildNumber = build['number']
                result = build['result']
                logUrl = url + str(buildNumber) + "/console"
                logResponse = self.getBuildLog(logUrl)
                logTokens = logResponse.split('****Start of Json Output****')
                for logToken in logTokens:
                    deploymentTokens = logToken.split('****End of Json Output****')
                    if len(deploymentTokens) > 1:
                        deploymentJsonStr = '{' + deploymentTokens[0].split('{')[1].split('}')[0] + '}'
                        buildJson = json.loads(deploymentJsonStr)
                        buildJson['buildId'] = buildNumber
                        buildJson['result'] = result
                        parsedBuilds.append(buildJson)
                tillJobCount = tillJobCount - 1
                if tillJobCount <= 0:
                    break
        except Exception as ex:
            logging.error(ex)
        buildDetails = self.parseResponse(self.responseTemplate, parsedBuilds, injectData)
        self.publishToolsData(buildDetails)
        self.updateTrackingDetails(url, lastBuild)           
    
    def getBuildLog(self,url):
        auth = HTTPBasicAuth(self.userid, self.passwd)
        response = requests.get(url, auth=auth)
        return response.content

if __name__ == "__main__":
    JenkinsLogParserAgent()
