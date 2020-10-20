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
from .JenkinsAgent3 import JenkinsAgent


class JenkinsLogParserAgent(JenkinsAgent):         
    
    def processLogParsing(self, buildDetails):
        dataList = []
        retrieveAllStages = self.config.get("retrieveAllStages", False)
        for build in buildDetails:
            buildAdded = False
            buildUrl = build['buildUrl']
            stageUrl = buildUrl + 'wfapi/describe' 
            logUrl = buildUrl + "consoleText"
            logResponse = self.getBuildLog(logUrl)
            if logResponse.find('Uploaded to nexus: ')!=-1:
                print("into find nexus")
                build["resourcekey"]=logResponse.split('Uploaded to nexus: ')[1].split('\n')[0]
                print("Build #1")
                print( build["resourcekey"])
                build["resourcekey"]=build["resourcekey"][build["resourcekey"].find('repository/'):build["resourcekey"].rfind('/')] + build["resourcekey"][build["resourcekey"].rfind('.'):build["resourcekey"].rfind(' (')]
                print("Build #2")
                print( build["resourcekey"])
                build["resourcekey"]=build["resourcekey"][build["resourcekey"].index('/',build["resourcekey"].index('/') + 1) + 1:build["resourcekey"].rfind('/')]+build["resourcekey"][build["resourcekey"].rfind('.'):]
                print("Build 3 #")
                print( build["resourcekey"])
                build["resourcekey"]=build["resourcekey"][::-1].replace('/','-',2)[::-1]
                print("Build 4 #")
                print( build["resourcekey"])
            else:
                #print("into else 1")
                build["resourcekey"]='Not Found'
            if logResponse.find('Starting deployment process ')!=-1:
                #print("into find Starting deployment process")
                deploymentDetails = logResponse.split('Starting deployment process ')[1].split('\n')[0]
                #print(deploymentDetails)
                build["deploymentProcess"] = deploymentDetails.split(" ")[0].replace("'","")
                #print(build["deploymentProcess"])
                build["deploymentApplication"] = deploymentDetails.split("of application ")[1].split(' ')[0].replace("'","")
                build["deploymentEnv"] = deploymentDetails.split("in environment ")[1].split('\n')[0].replace("'","")
                #print(build["deploymentEnv"])
            else:
                #print("into else 2")
                build["deploymentProcess"] = 'Not Found'
                build["deploymentApplication"] = 'Not Found'
                build["deploymentEnv"] = 'Not Found'
            if logResponse.find('Deployment request id is: ')!=-1:
                #print("into find Deployment request id")
                build["deploymentId"] = logResponse.split('Deployment request id is: ')[1].split('\n')[0].replace("'","")
            else:
                #print("into else 3")
                build["deploymentId"] = 'Not Found'
            if logResponse.find('The deployment result is ')!=-1:
                build["deploymentStatus"] = logResponse.split('The deployment result is ')[1].split('.')[0].replace("'","")
            else:
                #print("into else 4") 
                build["deploymentStatus"] = 'Not Found'
            if retrieveAllStages:
                try:
                    stageResponse = self.getResponse(stageUrl, 'GET', self.userid, self.passwd, None)
                    if stageResponse:
                        stages = stageResponse.get('stages', None)
                        if stages:
                            for stage in stages:
                                buildAdded = True
                                data = copy.deepcopy(build)
                                dataList.append(data)
                                for stageProperty in stage:
                                    data[stageProperty + "Stage"] = stage[stageProperty]
                except Exception as ex:
                    pass
            else:
                logUrl = buildUrl + "console"
                logResponse = self.getBuildLog(logUrl)
                buildStageDetails = {}
                deployStageToEnvMap = {}
                if b'[Pipeline]' in logResponse:
                    stageResponse = self.getResponse(stageUrl, 'GET', self.userid, self.passwd, None)
                    if stageResponse:
                        stages = stageResponse.get('stages', None)
                        if stages:
                            for stage in stages:
                                stageName = stage.get('name', '')
                                if 'Build and Nexus Deploy' in stageName:                                
                                    buildStageDetails['buildStageStatus'] = stage.get('status', '')
                                    buildStageDetails['buildStageStartTimeMS'] = stage.get('startTimeMillis', '')
                                    buildStageDetails['buildStageDurationMS'] = stage.get('durationMillis', '')
                                elif 'Deploy to ' in stageName:
                                    envName = stageName.replace('Deploy to ', '')
                                    deployStageData = {}
                                    deployStageData['deployStageStatus'] = stage.get('status', '')
                                    deployStageData['deployStageStartTimeMS'] = stage.get('startTimeMillis', '')
                                    deployStageData['deployStageDurationMS'] = stage.get('durationMillis', '')
                                    deployStageToEnvMap[envName] = deployStageData
                for buildStagePropery in buildStageDetails:
                    build[buildStagePropery] = buildStageDetails[buildStagePropery]
                try:
                    flag = logResponse.find('****Start of Json Output****')
                    if flag == -1:
                        logTokens = logResponse.split('[Pipeline] echo')
                    else:
                        logTokens = logResponse.split('****Start of Json Output****')
                    for logToken in logTokens:
                        isJsonValid = logToken.find('buildDurationEpoch')
                        if isJsonValid > 0:
                            if flag == -1:
                                deploymentTokens = logToken.split('[Pipeline] }')
                            else:
                                deploymentTokens = logToken.split('****End of Json Output****')
                            
                            if len(deploymentTokens) > 1:
                                data = copy.deepcopy(build)
                                dataList.append(data)
                                buildAdded = True
                                deploymentJsonTokens = deploymentTokens[0].split('{')
                                if len(deploymentJsonTokens) > 1:
                                    deploymentJsonStr = '{' + deploymentJsonTokens[1].split('}')[0] + '}'
                                    buildJson = json.loads(deploymentJsonStr)
                                    for attr in buildJson:
                                        if data.get(attr, None) is None:
                                            data[attr] = buildJson[attr]
                                    envDetails = data.get('envDetail', None)
                                    if envDetails:
                                        deployStageDetails = deployStageToEnvMap.get(envDetails, {})
                                        for deploymentStagePropery in deployStageDetails:
                                            data[deploymentStagePropery] = deployStageDetails[deploymentStagePropery]
                except Exception as ex:
                    pass
            if not buildAdded:
                dataList.append(build)
        return dataList
    
    def getBuildLog(self, url):
        auth = HTTPBasicAuth(self.userid, self.passwd)
        response = requests.get(url, auth=auth)
        return response.content.decode('utf8')


if __name__ == "__main__":
    JenkinsLogParserAgent()
