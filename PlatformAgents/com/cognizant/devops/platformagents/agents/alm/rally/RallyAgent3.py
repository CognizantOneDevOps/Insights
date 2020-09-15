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
Created on Jun 18, 2018
@author: 476693
'''


from dateutil import parser
from ....core.BaseAgent3 import BaseAgent
import json
import datetime
import time
import logging.handlers

class RallyAgent(BaseAgent):
    def process(self):
        userid = self.getCredential("userid")
        passwd = self.getCredential("passwd")
        accesstoken = self.getCredential("accesstoken")
        baseUrl = self.config.get("baseUrl", '')
        pageSize = self.config.get("dataSize", '')
        storyMetadata = self.config.get('dynamicTemplate', {}).get('storyMetadata', None)
        relationMetadata = self.config.get('dynamicTemplate', {}).get('relationMetadata', None)
        releaseMetadata = self.config.get('dynamicTemplate', {}).get('releaseMetadata', None)
        releaseinsightsTimeX = self.config.get('dynamicTemplate', {}).get('release', {}).get('insightsTimeXFieldMapping',None)
        storyinsightsTimeX = self.config.get('dynamicTemplate', {}).get('story', {}).get('insightsTimeXFieldMapping',None)
        iterationinsightsTimeX = self.config.get('dynamicTemplate', {}).get('iteration', {}).get('insightsTimeXFieldMapping',None)
        specificWorkspaceConfigList = self.config.get('dynamicTemplate', {}).get('specificWorkspaceList', None)
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%SZ')
        responseTemplate = self.getResponseTemplate()
        trackingDetails = {}
        data_iteration = []
        data_release = []
        data = []
        injectIterationData = {}
        injectArtifactData = {}
        ts = time.time()
        specificWorkspaceList=[]
        if len(specificWorkspaceConfigList) == 0:
             specificWorkspaceList = []
             subscriptionListUrl = baseUrl + "subscription"
             logging.debug(" subscriptionListUrl ==== " + subscriptionListUrl)
             subscriptionListRes = self.getResponse(subscriptionListUrl, 'GET', accesstoken, '', None)
             subscriptionUrl = subscriptionListRes['Subscription']['_ref']
             subspaceworkspace = subscriptionUrl + "/Workspaces"
             logging.debug("  subspaceworkspace ==== " + subspaceworkspace)
             subWorkspaceRes = self.getResponse(subspaceworkspace, 'GET', accesstoken, '', None)
             # logging.debug(subWorkspaceRes)
             for i in range (0, len(subWorkspaceRes['QueryResult']['Results'])):
                 specificWorkspaceList.append(subWorkspaceRes['QueryResult']['Results'][i]['_ref'])
        else:
            for workspaceConfigUrl in specificWorkspaceConfigList:
                logging.debug("workspaceConfigUrl from config ==== "+workspaceConfigUrl)
                specificWorkspaceList.append(workspaceConfigUrl)
        trackingDetails = self.tracking.get('LastUpdatedDate', None)        
        if trackingDetails is None:
            lastUpdated = startFrom
        else:
            lastUpdated = trackingDetails
        artifactTypeList = self.config.get('dynamicTemplate', {}).get('artifactType', None)
        arr = self.config.get('dynamicTemplate', {}).get('storyResponseTemplate', None)
        logging.debug(" specificWorkspaceList ==== " + str(len(specificWorkspaceList)))
        try:
            for i in range(0, len(specificWorkspaceList)):
                workspaceUrl = str(specificWorkspaceList[i])
                logging.debug("workspaceUrl ==== " + workspaceUrl)
                artifactListUrl = baseUrl + "artifact?workspace=" + workspaceUrl + "&query=(LastUpdateDate > " + lastUpdated + ")&fetch=true"
                logging.debug(" artifactListUrl   ==== " + artifactListUrl)
                responseTemplate = self.getResponseTemplate()
                pageNum = 1
                fetchNextPage = True
                while fetchNextPage:
                     logging.debug(" artifactListUrl  ==== " + artifactListUrl)
                     artifactListUrlPage = artifactListUrl + '&pagesize=' + str(pageSize) + '&start=' + str(pageNum)
                     logging.debug(" artifactListUrlPage  ==== " + artifactListUrlPage)
                     try:
                         artifactListRes = self.getResponse(artifactListUrlPage, 'GET', accesstoken, '', None)
                         logging.debug(" artifactListRes record count  ==== " + str(artifactListRes['QueryResult']['TotalResultCount']))
                         if len(artifactListRes['QueryResult']['Results']) == 0:
                            fetchNextPage = False
                            break
                         if len(artifactListRes['QueryResult']['Results']) < 20:
                             artifactListResCount = len(artifactListRes['QueryResult']['Results'])
                         else:
                             artifactListResCount = artifactListRes['QueryResult']['PageSize']
                         logging.debug("artifactListResCount 8 ==== " + str(artifactListResCount))
                     except Exception as ex:
                         artifactListResCount=0  
                         self.publishHealthDataForExceptions(ex)
                     for artifact in range(0, artifactListResCount):
                       try:  
                           artifactListData = artifactListRes['QueryResult']['Results'][artifact]
                           artifactUrl = artifactListData['_ref']
                           artifactType = artifactListData['_type']
                           projectUrl = artifactListData['Project']['_ref']
                           projectRef = projectUrl.split("/")
                           projectReflen = len(projectRef)
                           projectId = int(projectRef[projectReflen - 1])
                           logging.debug("artifact ====" + str(artifact) + " projectId ==== " + str(projectId) + " artifactType ==== " + artifactType + " artifactUrl  ==== " + artifactUrl + "  FormattedID  "+artifactListData['FormattedID'])
                           # artifactRes = self.getResponse(artifactUrl, 'GET', accesstoken, '', None)               
                           if (artifactType in artifactTypeList):
                               injectArtifactData = {}
                               injectArtifactData['name'] = artifactListData['Name']
                               injectArtifactData['type'] = artifactListData['_type']
                               injectArtifactData['formattedID'] = artifactListData['FormattedID']
                               injectArtifactData['referenceURL'] = artifactUrl
                               injectArtifactData['description'] = artifactListData['Description']
                               injectArtifactData['creationDate'] = artifactListData['CreationDate']
                               injectArtifactData['workspaceName'] = artifactListData['Workspace']['_refObjectName']
                               workspaceUrl = artifactListData['Workspace']['_ref']
                               workspaceRef = workspaceUrl.split("/")
                               workspaceReflen = len(workspaceRef)
                               injectArtifactData['workspaceID'] = int(workspaceRef[workspaceReflen - 1])
                               injectArtifactData['projectName'] = artifactListData['Project']['_refObjectName']
                               injectArtifactData['projectID'] = projectId
                               injectArtifactData['lastUpdateDate'] = artifactListData['LastUpdateDate']
                               logging.debug("Tag Count ==== " + str(artifactListData['Tags']['Count']))
                               if artifactListData['Tags']:
                                   if artifactListData['Tags']['Count'] != 0:
                                        tagVal = artifactListData['Tags']['_tagsNameArray'][0]
                                        logging.debug("Tag Value tagVal ==== " + str(tagVal))
                                        val = tagVal.get('Name')
                                        injectArtifactData['tagName'] = val
                                        logging.debug("Tag Value ==== " + val)
                               if artifactListData.get('Iteration') and artifactListData['Iteration'] != None :
                                injectArtifactData['iterationName'] = artifactListData['Iteration']['_refObjectName']
                                logging.debug("iterationRef  ==== " + artifactListData['Iteration']['_ref'])
                                iterationRef = artifactListData['Iteration']['_ref'].split("/")
                                iterationReflen = len(iterationRef)
                                iterationId = iterationRef[iterationReflen - 1]
                                injectArtifactData['iterationID'] = int(iterationId)
                               else :
                                injectArtifactData['iterationID'] = 0
                                injectArtifactData['iterationName'] = 0
                               if artifactListData.get('Release') and artifactListData['Release'] != None:
                                injectArtifactData['releaseName'] = artifactListData['Release']['_refObjectName']
                                logging.debug(" releaseName artifact ==== " + str(artifactListData['Release']['_refObjectName']))
                                ReleaseUrl = artifactListData['Release']['_ref']
                                logging.debug(" ReleaseUrl artifact ==== " + ReleaseUrl)
                                ReleasetRes = self.getResponse(ReleaseUrl, 'GET', accesstoken, '', None)
                                for Release in ReleasetRes:
                                  injectArtifactData['releasePlanEstimate'] = ReleasetRes['Release']['PlanEstimate']
                                  injectArtifactData['releasePlannedVelocity'] = ReleasetRes['Release']['PlannedVelocity']
                                  injectArtifactData['releaseDate'] = ReleasetRes['Release']['ReleaseDate']
                                  injectArtifactData['releaseStartDate'] = ReleasetRes['Release']['ReleaseStartDate']
                                  injectArtifactData['releaseID'] = ReleasetRes['Release']['ObjectID']
                               else :
                                 injectArtifactData['Release'] = 0
                                 injectArtifactData['releaseName'] = 0
                               for key in arr:
                                   if ':' in key:
                                    #if key.split(":")[0] in artifactListData:
                                      keyMainTag =artifactListData.get(key.split(":")[0], None)
                                      if keyMainTag is not None:
                                          propertyValue = keyMainTag.get(key.split(":")[1], None)
                                          if propertyValue is not None:  
                                            injectArtifactData[key.split(":")[0]] = propertyValue
                                          else:
                                            injectArtifactData[key.split(":")[0]] = 0
                                      else:
                                         injectArtifactData[key.split(":")[0]] = 0
                                   else:
                                     value = artifactListData.get(key, None)
                                     if value is not None:
                                        injectArtifactData[key] = value
                                     else:
                                        injectArtifactData[key] = 0
                               data.append(injectArtifactData)
                       except Exception as ex:
                            self.publishHealthDataForExceptions(ex)
                     pageNum = 20 + pageNum
            ''' Section used to get all iteration inforamtion  '''
            for Workspace in range(0, len(specificWorkspaceList)):
               workspaceUrl = specificWorkspaceList[Workspace]
               iterationListUrl = baseUrl + "iteration?workspace=" + workspaceUrl + "&fetch=true"
               logging.debug(" iterationListUrl ==== " + iterationListUrl)
               pageNum = 1
               fetchNextPage = True
               while fetchNextPage:
                iterationListUrlPage = iterationListUrl + '?pagesize=' + str(pageSize) + '&start=' + str(pageNum)
                logging.debug(" iterationListUrlPage ==== " + iterationListUrlPage)
                try:
                    iterationListRes = self.getResponse(iterationListUrlPage, 'GET', accesstoken, '', None)
                    logging.debug(" iterationListRes length  ==== "+str(len(iterationListRes['QueryResult']['Results'])))
                    if len(iterationListRes['QueryResult']['Results']) == 0:
                        logging.debug(" iterationListUrlPage is empty  ==== ")
                        fetchNextPage = False
                        break
                    if len(iterationListRes['QueryResult']['Results']) < 20:
                         iterationCount = len(iterationListRes['QueryResult']['Results'])
                    else:
                         iterationCount = iterationListRes['QueryResult']['PageSize']
                except Exception as ex:
                     iterationCount=0 
                     self.publishHealthDataForExceptions(ex) 
                for iterationRes in range(0, iterationCount):
                    try:
                        iterationListData = iterationListRes['QueryResult']['Results'][iterationRes]
                        iterationUrl = iterationListData['_ref']
                        iterationType = iterationListData['_type']
                        logging.debug(" iterationUrl ==== " + iterationUrl)
                        iterationRes = self.getResponse(iterationUrl, 'GET', accesstoken, '', None)
                        injectIterationData = {}
                        workspaceUrl = iterationRes['Iteration']['Workspace']['_ref']
                        workspaceRef = workspaceUrl.split("/")
                        workspaceReflen = len(workspaceRef)
                        projectUrl = iterationRes['Iteration']['Project']['_ref']
                        projectRef = projectUrl.split("/")
                        projectReflen = len(projectRef)
                        iterationRef = iterationRes['Iteration']['_ref'].split("/")
                        iterationReflen = len(iterationRef)
                        iterationId = iterationRef[iterationReflen - 1]
                        responseTemplate = self.config.get('dynamicTemplate', {}).get('iterationResponseTemplate', None)
                        injectIterationData['projectName'] = iterationRes['Iteration']['Project']['_refObjectName']
                        injectIterationData['workspaceID'] = int(workspaceRef[workspaceReflen - 1])
                        injectIterationData['projectID'] = int(projectRef[projectReflen - 1])
                        injectIterationData['iterationID'] = int(iterationId)
                        data_iteration = data_iteration + self.parseResponse(responseTemplate, iterationRes, injectIterationData)
                    except Exception as ex:
                        self.publishHealthDataForExceptions(ex)
                pageNum = 20 + pageNum
            
            ''' Section used to get all release inforamtion  '''
            logging.debug(" Before release code ==== " +str(len(specificWorkspaceList)))
            for workspaceRelease in range(0, len(specificWorkspaceList)):
               workspaceUrl = specificWorkspaceList[workspaceRelease]
               releaseListUrl = baseUrl + "release?workspace=" + workspaceUrl + "&fetch=true"
               logging.debug(" releaseListUrl ==== " + releaseListUrl)
               pageNum = 1
               fetchNextPage = True
               while fetchNextPage:
                releaseListUrlPage = releaseListUrl + '?pagesize=' + str(pageSize) + '&start=' + str(pageNum)
                logging.debug(" releaseListUrlPage ==== " + releaseListUrlPage)
                try:
                    releaseListRes = self.getResponse(releaseListUrlPage, 'GET', accesstoken, '', None)
                    logging.debug(" releaseListRes length  ==== "+str(len(releaseListRes['QueryResult']['Results'])))
                    if len(releaseListRes['QueryResult']['Results']) == 0:
                        logging.debug(" releaseListUrlPage is empty  ==== ")
                        fetchNextPage = False
                        break
                    if len(releaseListRes['QueryResult']['Results']) < 20:
                         releaseCount = len(releaseListRes['QueryResult']['Results'])
                    else:
                         releaseCount = releaseListRes['QueryResult']['PageSize']
                except Exception as ex:
                     releaseCount=0  
                     self.publishHealthDataForExceptions(ex)
                for releaseRes in range(0, releaseCount):
                    try:
                        releaseListData = releaseListRes['QueryResult']['Results'][releaseRes]
                        releaseUrl = releaseListData['_ref']
                        releaseType = releaseListData['_type']
                        logging.debug(" releaseUrl ==== " + releaseUrl)
                        releaseRes = self.getResponse(releaseUrl, 'GET', accesstoken, '', None)
                        injectReleaseData = {}
                        workspaceUrl = releaseRes['Release']['Workspace']['_ref']
                        workspaceRef = workspaceUrl.split("/")
                        workspaceReflen = len(workspaceRef)
                        projectUrl = releaseRes['Release']['Project']['_ref']
                        projectRef = projectUrl.split("/")
                        projectReflen = len(projectRef)
                        releaseRef = releaseRes['Release']['_ref'].split("/")
                        releaseReflen = len(releaseRef)
                        releaseId = releaseRef[releaseReflen - 1]
                        responseTemplate = self.config.get('dynamicTemplate', {}).get('releaseResponseTemplate', None)
                        injectReleaseData['projectName'] = releaseRes['Release']['Project']['_refObjectName']
                        injectReleaseData['workspaceID'] = int(workspaceRef[workspaceReflen - 1])
                        injectReleaseData['projectID'] = int(projectRef[projectReflen - 1])
                        injectReleaseData['releaseID'] = int(releaseId)
                        data_release = data_release + self.parseResponse(responseTemplate, releaseRes, injectReleaseData)
                    except Exception as ex:
                        self.publishHealthDataForExceptions(ex)
                pageNum = 20 + pageNum
        except Exception as ex:
            self.publishHealthDataForExceptions(ex)
        logging.debug(" publish data  ==== " )
        storyTimestamp = storyinsightsTimeX.get('timefield',None)
        storyTimestampFormat = storyinsightsTimeX.get('timeformat',None)
        storyIsEpoch = storyinsightsTimeX.get('isEpoch',False)
        self.publishToolsData(data, storyMetadata,storyTimestamp,storyTimestampFormat,storyIsEpoch,True)
        iterationTimestamp = iterationinsightsTimeX.get('timefield',None)
        iterationTimestampFormat = iterationinsightsTimeX.get('timeformat',None)
        iterationIsEpoch = iterationinsightsTimeX.get('isEpoch',False)
        self.publishToolsData(data_iteration, relationMetadata,iterationTimestamp,iterationTimestampFormat,iterationIsEpoch,True)
        releaseTimestamp = releaseinsightsTimeX.get('timefield',None)
        releaseTimestampFormat = releaseinsightsTimeX.get('timeformat',None)
        releaseIsEpoch = releaseinsightsTimeX.get('isEpoch',False)
        self.publishToolsData(data_release, releaseMetadata,releaseTimestamp,releaseTimestampFormat,releaseIsEpoch,True)
        self.tracking['LastUpdatedDate'] = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%dT%H:%M:%SZ')
        logging.debug(" publish data complete ==== " )
        self.updateTrackingJson(self.tracking)

if __name__ == "__main__":
    RallyAgent()       