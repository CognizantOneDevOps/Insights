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
Created on Jun 22, 2016

@author: 463188
'''
from time import mktime
from dateutil import parser
import json
import requests
import xml.etree.ElementTree as ET
from requests.auth import HTTPBasicAuth
from ....core.BaseAgent3 import BaseAgent

class JenkinsAgent(BaseAgent):
    def process(self):
        self.userid = self.getCredential("userid")
        self.passwd = self.getCredential("passwd")
        self.BaseUrl = self.config.get("baseUrl", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        self.startFrom = int(startFrom * 1000)
        self.responseTemplate = self.getResponseTemplate()
        self.useAllBuildsApi = self.config.get("useAllBuildsApi", False)
        self.buildsApiName = "builds"
        if self.useAllBuildsApi:
            self.buildsApiName = "allBuilds"
        self.data = []
        self.treeApiParams = self.buildApiParameters('', self.responseTemplate)
        self.validateAndCorrectTrackingFormat()
        jenkinsMasters = self.config.get("jenkinsMasters", None)
        if jenkinsMasters:
            for jenkinsMaster in jenkinsMasters:
                self.currentJenkinsMaster = jenkinsMaster
                self.processFolder(jenkinsMasters[jenkinsMaster])
        else:
            self.currentJenkinsMaster = 'master'
            self.processFolder(self.BaseUrl)
        #self.publishToolsData(self.data)
        self.updateTrackingJson(self.tracking)

    def validateAndCorrectTrackingFormat(self):
        tracking = self.tracking
        correctionRequired = False
        for master in tracking:
            keyType = type(tracking[master])
            if keyType is int:
                correctionRequired = True
            break
        if correctionRequired:
            self.tracking = {'master' : self.tracking}
            self.updateTrackingJson(self.tracking)

    def processFolder(self,url):
        restUrl = url + 'api/json?tree=jobs[name,url,buildable,lastBuild[number]]'
        jenkinsProjects = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
        jobs = jenkinsProjects.get('jobs', None);
        if jobs:
            for job in jobs:
                url = job.get('url')
                if job.get('buildable', False):
                    lastBuild = job.get('lastBuild', None)
                    if lastBuild:
                        jobName = job.get('name', None)
                        lastBuildNumber = lastBuild.get('number', None)
                        if lastBuildNumber:
                            self.getJobDetails(url, lastBuildNumber, jobName)
                else:
                    self.processFolder(url)
        else:
            restUrl = url + 'api/json?tree=lastBuild[number],url,name'
            jenkinsProjects = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
            jobUrl = jenkinsProjects['url']
            jobName = jenkinsProjects.get('name', None)
            lastBuild = jenkinsProjects.get('lastBuild', None)
            if lastBuild is not None:
                lastBuildNumber = lastBuild.get('number', None)
                if lastBuildNumber:
                    self.getJobDetails(jobUrl, lastBuildNumber, jobName)


    def getJobDetails(self, url, lastBuild, jobName):
        tillJobCount = 0
        buildsIdentified = False
        nextBatch = 0
        injectData = self.getJobLevelConfig(url)
        if injectData['disabled'] == 'true':
            return;
        injectData['master'] = self.currentJenkinsMaster
        injectData['jobName'] = jobName
        if self.tracking.get(self.currentJenkinsMaster, {}).get(url,None):
            trackingNum = self.tracking.get(self.currentJenkinsMaster).get(url)
            tillJobCount = lastBuild - int(trackingNum)
        else:
            while not buildsIdentified:
                restUrl = url+'api/json?tree='+self.buildsApiName+'[number,timestamp,duration]{'+str(nextBatch)+','+str(nextBatch+100)+'},name'
                jobDetails = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
                builds = jobDetails[self.buildsApiName]
                for build in builds:
                    if self.startFrom < build["timestamp"]:
                        tillJobCount+=1
                    #if not build["duration"] > 0:
                        #startindex=tillJobCount
                    else:
                        self.updateTrackingDetails(url, lastBuild)
                        buildsIdentified = True
                        break
                if len(builds) < 100:
                    buildsIdentified = True
                if not buildsIdentified:
                    nextBatch = nextBatch + 100
        if tillJobCount > 0:
            self.processBuildExecutions(url, tillJobCount, lastBuild, injectData)
    
    def processBuildExecutions(self, url, tillJobCount, lastBuild, injectData):
        start = 0
        end = 0
        trackingUpdated = False
        while start <= tillJobCount:
            end = end + 100
            if end > tillJobCount:
                end = tillJobCount
            restUrl = url+'api/json?tree=' + self.buildsApiName + self.treeApiParams +'{'+str(start)+','+str(end)+'},name'
            jobDetails = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
            builds = jobDetails[self.buildsApiName]
            #add filter logic
            startIndex = 0
            for build in builds:
                if not build["duration"] > 0:
                    startIndex+=1
            completedBuilds = builds[startIndex:len(builds)]
            if len(completedBuilds)>0:
                buildDetails = self.parseResponse(self.responseTemplate, completedBuilds, injectData)
                buildDetails = self.processLogParsing(buildDetails)
                self.publishToolsData(buildDetails)
                if not trackingUpdated:
                    if "id" in completedBuilds[0]:

                        self.updateTrackingDetails(url, completedBuilds[0]["number"])
                    trackingUpdated = True
            start = start + 100
    
    def processLogParsing(self, buildDetails):            
        return buildDetails
    
    def updateTrackingDetails(self, buildUrl, buildNumber):
        currentMasterTracking = self.tracking.get(self.currentJenkinsMaster, None)
        if currentMasterTracking is None:
            currentMasterTracking = {}
            self.tracking[self.currentJenkinsMaster] = currentMasterTracking
        currentMasterTracking[buildUrl] = buildNumber
        self.updateTrackingJson(self.tracking)
    
    
    def buildApiParameters(self, keyName, valueObject):
        valueType = type(valueObject)
        urlToken = None;
        if valueType is dict:
            urlToken = keyName + '['
            for key in valueObject:
                urlToken += self.buildApiParameters(key, valueObject[key]) + ','
            urlToken = urlToken[:-1] + ']'
        elif valueType is list:
            urlToken = self.buildApiParameters(keyName, valueObject[0])
        elif valueType is str or valueType is str:
            urlToken = keyName
        return urlToken
    
    def getJobLevelConfig(self,url):
        jobDetails = self.config.get('jobDetails',{});
        injectData = {
            'disabled' : 'false'
        }
        if len(jobDetails) == 0:
            return injectData
        configXmlUrl = url+"config.xml"
        xmlResponse = self.getResponse(configXmlUrl, 'GET', self.userid, self.passwd, None)
        root = ET.fromstring(xmlResponse.text.encode('UTF-8').strip())
        rootTag = root.tag
        rootTagLen = len(rootTag) + 1
        disabledNode = root.find('disabled')
        if disabledNode:
            injectData['disabled'] = disabledNode.text
        if injectData['disabled'] == 'true':
            return injectData
        for key in jobDetails:
            xpath = jobDetails[key]
            xpath = xpath.replace(rootTag+'/', '', rootTagLen)
            element = root.find(xpath)
            if element is not None:
                injectData[key] = element.text
        return injectData
    def getBuildLog(self,url):
        auth = HTTPBasicAuth(self.userid, self.passwd)
        response = requests.get(url, auth=auth)
        return response.content.decode('utf-8')

if __name__ == "__main__":
    JenkinsAgent()
