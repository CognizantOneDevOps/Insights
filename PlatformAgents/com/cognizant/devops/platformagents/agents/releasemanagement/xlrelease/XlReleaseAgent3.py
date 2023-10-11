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
Created on May 09, 2020

@author: 368419
'''

from datetime import datetime as dateTime
import json
from ....core.BaseAgent3 import BaseAgent

class XlReleaseAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        self.baseLogger.info('Inside process')
        timeStampNow = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
        self.baseEndPoint = self.config.get("baseEndPoint", '')
        self.userID = self.config.get("userID", '')
        self.cred = self.config.get("passwd", '')
        timeStampFormat = self.config.get("timeStampFormat", '')
        dynamicTemplate = self.config.get('dynamicTemplate', {})
        releasesReqData = list()
        phasesReqData = list()
        tasksReqData = list()
        foldersReqData = list()
        defReleaseReqResTemplate = {}
        releaseReqResponseTemplate = dynamicTemplate.get('releasesResponseTemplate', defReleaseReqResTemplate)
        defPhasesReqResTemplate = {}
        phasesReqResponseTemplate = dynamicTemplate.get('phasesResponseTemplate', defPhasesReqResTemplate)
        defTasksReqResTemplate = {}
        tasksReqResponseTemplate = dynamicTemplate.get('tasksResponseTemplate', defTasksReqResTemplate)
        defFoldersReqResTemplate = {}
        foldersReqResponseTemplate = dynamicTemplate.get('foldersResponseTemplate', defFoldersReqResTemplate)
        metaData = dynamicTemplate.get('metaData', {})
        releasesMetaData = metaData.get('releases', {})
        phasesMetaData = metaData.get('phases', {})
        tasksMetaData = metaData.get('tasks', {})
        foldersMetaData = metaData.get('folders', {})
        releasesinsighstTimeX = dynamicTemplate.get('releases',{}).get('insightsTimeXFieldMapping',None)
        releasestimestamp = releasesinsighstTimeX.get('timefield',None)
        releasestimeformat = releasesinsighstTimeX.get('timeformat',None)
        releasesisEpoch = releasesinsighstTimeX.get('isEpoch',False)
        phasesinsighstTimeX = dynamicTemplate.get('phases',{}).get('insightsTimeXFieldMapping',None)
        phasestimestamp = phasesinsighstTimeX.get('timefield',None)
        phasestimeformat = phasesinsighstTimeX.get('timeformat',None)
        phasesisEpoch = phasesinsighstTimeX.get('isEpoch',False)
        tasksinsighstTimeX = dynamicTemplate.get('tasks',{}).get('insightsTimeXFieldMapping',None)
        taskstimestamp = tasksinsighstTimeX.get('timefield',None)
        taskstimeformat = tasksinsighstTimeX.get('timeformat',None)
        tasksisEpoch = tasksinsighstTimeX.get('isEpoch',False)
        getReleasesUrl = self.baseEndPoint + "/releases"
        releaseDetails = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('releaseDetails', None)
        releasesToPhasesRelMetadata = releaseDetails.get('releasesToPhasesRelMetadata')
        phasesToTasksRelMetadata = releaseDetails.get('phasesToTasksRelMetadata')
        releasesToTasksRelMetadata = releaseDetails.get('releasesToTasksRelMetadata')
        defReleasesPayloadTemplate = {}
        releasesPayloadTemplate = dynamicTemplate.get('releasesPayloadTemplate', defReleasesPayloadTemplate)
        url = self.baseEndPoint + '/releases/search?page=0&resultsPerPage=100'
        apiHeaders = {'Content-Type': 'application/json', 'accept': 'application/json'}
        if self.tracking.get("folderDetails") is None:
            self.folderDetails = {}
            self.tracking["folderDetails"] = self.folderDetails
        else:
            self.folderDetails = self.tracking.get("folderDetails")
        if self.tracking.get("statusTrackingDetails") is None:
            self.releaseStatusDetails = {}
            self.tracking["statusTrackingDetails"] = self.releaseStatusDetails 
        else:
            self.releaseStatusDetails = self.tracking.get("statusTrackingDetails") 
        foldersListUrl = self.baseEndPoint + "/folders/list?depth=10"
        foldersList = ''
        try:
            foldersList = self.getResponse(foldersListUrl, 'GET', self.userID, self.cred, None)
        except Exception as ex:   
            self.publishHealthDataForExceptions(ex)
        self.foldersLabelList = []
        for folder in foldersList:
            folderId = folder['id']
            folderDict = {"id": folderId, "parentFolderId": None, "title": folder['title'], "level": 'H1'}
            self.foldersLabelList.append(folderDict)
            self.baseLogger.info(folder['title'] + ' : H1')
            self.getSubFoldersTitles(folder, 2, folderId)
        foldersReqData += self.parseResponse(foldersReqResponseTemplate, self.foldersLabelList)
        try:
            listReleases = self.getResponse(self.baseEndPoint + '/releases/search?page=0&resultsPerPage=100', 'POST', self.userID, self.cred, json.dumps(releasesPayloadTemplate), 'None', apiHeaders)
        except Exception as ex:   
            self.publishHealthDataForExceptions(ex)
        releasesData = []
        phasesData = []
        tasksData = []
        releasePageNum = 1
        fetchNextPage = True
        while fetchNextPage:
            if len(listReleases) == 0:
                fetchNextPage = False
                break
            for releaseData in listReleases:
                isNewRelease = False
                releaseId = releaseData.get("id", None)
                releaseStatus = releaseData.get("status", None)
                if releaseId not in self.releaseStatusDetails:
                    self.releaseStatusDetails[releaseId] = releaseStatus
                    isNewRelease = True
                elif releaseStatus != self.releaseStatusDetails.get(releaseId):
                    self.releaseStatusDetails[releaseId] = releaseStatus
                    isNewRelease = True
                if isNewRelease:
                    releasePath = self.getFolderPath(releaseId) 
                    releasesReqData += self.parseResponse(releaseReqResponseTemplate, releaseData, {'releasePath': releasePath, 'consumptionTime': timeStampNow()})
                    phasesListPerRelease = releaseData.get("phases", None)
                    for phaseData in phasesListPerRelease:
                        phaseId = phaseData.get("id", None)
                        phasesReqData += self.parseResponse(phasesReqResponseTemplate, phaseData,  {'releaseId': releaseId, 'consumptionTime': timeStampNow()})
                        phasesData.append(phasesReqData)
                        tasksDetails = phaseData.get("tasks", None)
                        for taskData in tasksDetails:
                            tasksReqData += self.parseResponse(tasksReqResponseTemplate, taskData,  {'releaseId': releaseId, 'phaseId': phaseId, 'consumptionTime': timeStampNow()})
                            tasksData.append(tasksReqData)
            releasePageNum = releasePageNum + 1
            listReleases = self.getResponse(self.baseEndPoint + '/releases/search?page='+str(releasePageNum)+'&resultsPerPage=100', 'POST', self.userID, self.cred, json.dumps(releasesPayloadTemplate), 'None', apiHeaders)
            self.publishToolsData(releasesReqData, releasesMetaData, releasestimestamp, releasestimeformat, releasesisEpoch, True)
            self.publishToolsData(phasesReqData, phasesMetaData, phasestimestamp, phasestimeformat, phasesisEpoch, True)
            self.publishToolsData(tasksReqData, tasksMetaData, taskstimestamp, taskstimeformat, tasksisEpoch, True)
        self.publishToolsData(foldersReqData, foldersMetaData)
        self.publishToolsData(phasesReqData, releasesToPhasesRelMetadata)
        self.publishToolsData(tasksReqData, phasesToTasksRelMetadata)
        self.publishToolsData(tasksReqData, releasesToTasksRelMetadata)
        self.updateTrackingJson(self.tracking)

    def getFolderPath(self, releaseId):
        getFoldersUrl = self.baseEndPoint + "/folders/"
        foldersPath = ''
        splitReleaseId = releaseId.split('/')
        folderNames = splitReleaseId[:-1]
        folderPath = "/".join(str(x) for x in folderNames)
        i = 1;
        pathPrefix = "Applications/"
        while(len(folderNames) > i):
            pathPrefix = pathPrefix + folderNames[i] + '/'
            if pathPrefix in self.folderDetails:
                folderTitle = self.tracking.get("folderDetails").get(pathPrefix).split('/')
                foldersPath = foldersPath + str(folderTitle[-2]) + '/'
            else:
                try:
                    self.folderResponse = self.getResponse(getFoldersUrl + pathPrefix, 'GET', self.userID, self.cred, None)
                except Exception as ex:   
                    self.publishHealthDataForExceptions(ex)
                foldersPath = foldersPath + self.folderResponse['title'] + '/'
            i = i + 1
            self.folderDetails[pathPrefix] = foldersPath
        self.tracking["folderDetails"] = self.folderDetails
        return foldersPath
    
    def getSubFoldersTitles(self, folder_json, level, parentFolderId):
        for children in folder_json['children']:
            childrenId = children['id']
            folderDict = {"id": childrenId, "parentFolderId": parentFolderId, "title": children['title'], "level": 'H' + str(level)}
            self.foldersLabelList.append(folderDict)
            self.baseLogger.info(str(children['title']) + ' : H' + str(level))
            self.getSubFoldersTitles(children, level + 1, childrenId)
        
if __name__ == "__main__":
    XlReleaseAgent()
