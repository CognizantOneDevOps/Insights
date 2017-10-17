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
import requests
import xml.etree.ElementTree as ET
from requests.auth import HTTPBasicAuth
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class JenkinsAgent(BaseAgent):
    def process(self):
        self.userid = self.config.get("userid", '')
        self.passwd = self.config.get("passwd", '')
        self.BaseUrl = self.config.get("BaseUrl", '')
        startFrom = self.config.get("StartFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = mktime(startFrom.timetuple()) + startFrom.microsecond/1000000.0
        self.startFrom = long(startFrom * 1000)
        self.responseTemplate = self.getResponseTemplate()
        self.useAllBuildsApi = self.config.get("useAllBuildsApi", False)
        self.buildAttrName = "builds"
        if self.useAllBuildsApi:
            self.buildAttrName = "allBuilds"
        self.data = []
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

    def processFolder(self,url):
        restUrl = url + 'api/json?tree=jobs[name,url,buildable,lastBuild[number]]'
        jenkinsProjects = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
        jobs = jenkinsProjects.get('jobs', None);
        if jobs:
            for projects in range(len(jenkinsProjects["jobs"])):
                if "buildable" in jenkinsProjects["jobs"][projects]:
                    jobUrl = jenkinsProjects["jobs"][projects]["url"]
                    if "lastBuild" in jenkinsProjects["jobs"][projects] and jenkinsProjects["jobs"][projects]["lastBuild"] is not None:
                        lastBuild = jenkinsProjects["jobs"][projects]["lastBuild"]["number"]
                        self.getJobDetails(jobUrl, lastBuild)
                else:
                    folderUrl = jenkinsProjects["jobs"][projects]["url"]
                    self.processFolder(folderUrl)
        else:
            restUrl = url + 'api/json?tree=lastBuild[number],url'
            jenkinsProjects = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
            jobUrl = jenkinsProjects["url"]
            lastBuild = jenkinsProjects.get("lastBuild", {}).get("number", None)
            if lastBuild is not None:
                self.getJobDetails(jobUrl, lastBuild)


    def getJobDetails(self, url, lastBuild):
        tillJobCount = 0
        buildsIdentified = False
        nextBatch = 0
        injectData = self.getJobLevelConfig(url)
        if injectData['disabled'] == 'true':
            return;
        injectData['master'] = self.currentJenkinsMaster
        if self.tracking.get(url,None):
            trackingNum = self.tracking.get(url,None)
            tillJobCount = lastBuild - trackingNum
        else:
            while not buildsIdentified:
                if self.useAllBuildsApi:
                    restUrl = url+'api/json?tree=allBuilds[number,timestamp,duration]{'+str(nextBatch)+','+str(nextBatch+100)+'},name'
                else:
                    restUrl = url+'api/json?tree=builds[number,timestamp,duration]{'+str(nextBatch)+','+str(nextBatch+100)+'},name'
                jobDetails = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
                builds = jobDetails[self.buildAttrName]
                for build in builds:
                    if self.startFrom < build["timestamp"]:
                        tillJobCount+=1
                    #if not build["duration"] > 0:
                        #startindex=tillJobCount
                    else:
                        buildsIdentified = True
                        break
                if len(builds) == 0:
                    buildsIdentified = True
                if not buildsIdentified:
                    nextBatch = nextBatch + 100
        if tillJobCount > 0:
            start = 0
            end = 0
            trackingUpdated = False
            while start <= tillJobCount:
                end = end + 100
                if end > tillJobCount:
                    end = tillJobCount + 1
                if self.useAllBuildsApi:
                    restUrl = url+'api/json?tree=allBuilds[number,actions[causes[shortDescription],remoteUrls[scmUrl],url],changeSet[items[commitId,date],kind],duration,id,result,timestamp,url,name,nextBuild]{'+str(start)+','+str(end)+'},name'
                else:
                    restUrl = url+'api/json?tree=builds[number,actions[causes[shortDescription],remoteUrls[scmUrl],url],changeSet[items[commitId,date],kind],duration,id,result,timestamp,url,name,nextBuild]{'+str(start)+','+str(end)+'},name'
                jobDetails = self.getResponse(restUrl, 'GET', self.userid, self.passwd, None)
                injectData['jobName'] = jobDetails["name"]
                builds = jobDetails[self.buildAttrName]
                #add filter logic
                startIndex = 0
                for build in builds:
                    if not build["duration"] > 0:
                        startIndex+=1
                completedBuilds = builds[startIndex:len(builds)]
                if len(completedBuilds)>0:
                    buildDetails = self.parseResponse(self.responseTemplate, completedBuilds, injectData)
                    self.publishToolsData(buildDetails)
                    if not trackingUpdated:
                        currentMasterTracking = self.tracking.get(self.currentJenkinsMaster, None)
                        if currentMasterTracking is None:
                            currentMasterTracking = {}
                            self.tracking[self.currentJenkinsMaster] = currentMasterTracking
                        currentMasterTracking[url] = completedBuilds[0]["number"]
                        self.updateTrackingJson(self.tracking)
                        trackingUpdated = True
                start = start + 100

    def getJobLevelConfig(self,url):
        jobDetails = self.config.get('jobDetails',{});
        injectData = {
            'disabled' : 'false'
        }
        if len(jobDetails) == 0:
            return injectData
        configXmlUrl = url+"config.xml"
        auth = HTTPBasicAuth(self.userid, self.passwd)
        xmlResponse = requests.get(configXmlUrl, auth=auth)
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


if __name__ == "__main__":
    JenkinsAgent()
