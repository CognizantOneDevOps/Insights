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
from ....core.BaseAgent import BaseAgent
import json
import datetime
import time

class RallyAgent(BaseAgent):
    def process(self):
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseUrl = self.config.get("baseUrl", '')
        pageSize = self.config.get("dataSize", '')
        storyMetadata = self.config.get('dynamicTemplate', {}).get('storyMetadata', None)
        relationMetadata = self.config.get('dynamicTemplate', {}).get('relationMetadata', None)
        specificWorkspaceList = self.config.get('dynamicTemplate', {}).get('specificWorkspaceList', None)
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%SZ')
        responseTemplate = self.getResponseTemplate()
        data = []
        injectIterationData = {}
        injectArtifactData = {}
        ts = time.time()
        if len(specificWorkspaceList) ==0:
         specificWorkspaceList = []
         subscriptionListUrl = baseUrl+"subscription"
         subscriptionListRes = self.getResponse(subscriptionListUrl, 'GET', userid, passwd, None)
         subscriptionUrl=subscriptionListRes['Subscription']['_ref']
         subspaceworkspace=subscriptionUrl+"/Workspaces"
         subWorkspaceRes= self.getResponse(subspaceworkspace, 'GET', userid, passwd, None)
         for i in range (0,len(subWorkspaceRes['QueryResult']['Results'])):
             specificWorkspaceList.append( subWorkspaceRes['QueryResult']['Results'][i]['_ref'])
        trackingDetails = self.tracking.get('LastUpdatedDate', None)
        if trackingDetails is None:
            lastUpdated = startFrom
        else:
            lastUpdated = trackingDetails
        artifactTypeList = self.config.get('dynamicTemplate', {}).get('artifactType', None)
        arr = self.config.get('dynamicTemplate', {}).get('storyResponseTemplate', None)
        for i in range(0, len(specificWorkspaceList)):
            workspaceUrl = specificWorkspaceList[i]
            artifactListUrl = baseUrl+"artifact?workspace="+workspaceUrl+"&query=(LastUpdateDate > "+lastUpdated+")&fetch=true"
            responseTemplate = self.getResponseTemplate()
            pageNum = 1
            fetchNextPage = True
            while fetchNextPage:
             artifactListRes = self.getResponse(artifactListUrl+'&pagesize='+str(pageSize)+'&start='+str(pageNum), 'GET', userid, passwd, None)
             if len(artifactListRes['QueryResult']['Results']) == 0:
                fetchNextPage = False
                break
             if len(artifactListRes['QueryResult']['Results']) < 20:
                 artifactListResCount=len(artifactListRes['QueryResult']['Results'])
             else:
                 artifactListResCount=artifactListRes['QueryResult']['PageSize']
             for artifact in range(0,artifactListResCount):
               artifactListData = artifactListRes['QueryResult']['Results'][artifact]
               artifactUrl= artifactListData['_ref']
               artifactType= artifactListData['_type']
               artifactRes = self.getResponse(artifactUrl, 'GET', userid, passwd, None)               
               if artifactType in artifactTypeList:
                   injectArtifactData = {}
                   injectArtifactData['name']=artifactListData['Name']
                   injectArtifactData['type']= artifactListData['_type']
                   injectArtifactData['formattedID']= artifactListData['FormattedID']
                   injectArtifactData['description']=artifactListData['Description']
                   injectArtifactData['creationDate']=artifactListData['CreationDate']
                   injectArtifactData['workspace']=artifactListData['Workspace']['_refObjectName']
                   workspaceUrl=artifactListData['Workspace']['_ref']
                   workspaceRef = workspaceUrl.split("/")
                   workspaceReflen= len(workspaceRef)
                   injectArtifactData['workspaceID']= int(workspaceRef[workspaceReflen-1])
                   injectArtifactData['projectName']=artifactListData['Project']['_refObjectName']
                   projectUrl=artifactListData['Project']['_ref']
                   projectRef = projectUrl.split("/")
                   projectReflen= len(projectRef)
                   injectArtifactData['projectID']= int(projectRef[projectReflen-1])
                   injectArtifactData['lastUpdateDate']=artifactListData['LastUpdateDate']
                   if artifactListData.get('Iteration') and artifactListData['Iteration'] != None :
                    injectArtifactData['iterationName']=artifactListData['Iteration']['_refObjectName']
                    iterationRef = artifactListData['Iteration']['_ref'].split("/")
                    iterationReflen= len(iterationRef)
                    iterationID= iterationRef[iterationReflen-1]
                    injectArtifactData['iterationID']=int(iterationID)
                   else :
                    injectArtifactData['iterationID'] = 0
                   if artifactListData.get('Release') and artifactListData['Release'] != None:
                    injectArtifactData['releaseName']=artifactListData['Release']['_refObjectName']
                    ReleaseUrl=artifactListData['Release']['_ref']
                    ReleasetRes = self.getResponse(ReleaseUrl, 'GET', userid, passwd, None)
                    for Release in ReleasetRes:
                      injectArtifactData['releasePlanEstimate']=ReleasetRes['Release']['PlanEstimate']
                      injectArtifactData['releasePlannedVelocity']=ReleasetRes['Release']['PlannedVelocity']
                      injectArtifactData['releaseDate']=ReleasetRes['Release']['ReleaseDate']
                      injectArtifactData['releaseStartDate']=ReleasetRes['Release']['ReleaseStartDate']             
                   else :
                     injectArtifactData['Release'] = 0
                   for key in arr:
                       if ':' in key:
                        if key.split(":")[0] in artifactListData:
                         injectArtifactData[key.split(":")[0]] = artifactListData.get(key.split(":")[0],None).get(key.split(":")[1],None)
                       else:
                         injectArtifactData[key] = artifactListData.get(key,None)
                   data.append(injectArtifactData)      
             pageNum = 20 + pageNum
        data_iteration = []
        for Workspace in range(0, len(specificWorkspaceList)):
           workspaceUrl = specificWorkspaceList[Workspace]
           iterationListUrl = baseUrl+"iteration?workspace="+workspaceUrl+"&query=&fetch=true"
           pageNum = 1
           fetchNextPage = True
           while fetchNextPage:
            iterationListRes=self.getResponse(iterationListUrl+'?pagesize='+str(pageSize)+'&start='+str(pageNum), 'GET', userid, passwd, None)
            if len(iterationListRes['QueryResult']['Results']) == 0:
                fetchNextPage = False
                break
            if len(iterationListRes['QueryResult']['Results']) < 20:
                 iterationCount=len(iterationListRes['QueryResult']['Results'])
            else:
                 iterationCount=iterationListRes['QueryResult']['PageSize']
            for iterationRes in range(0,iterationCount):
                   iterationListData = iterationListRes['QueryResult']['Results'][iterationRes]
                   iterationUrl= iterationListData['_ref']
                   iterationType= iterationListData['_type']
                   iterationRes = self.getResponse(iterationUrl, 'GET', userid, passwd, None)
                   injectIterationData = {}
                   workspaceUrl=iterationRes['Iteration']['Workspace']['_ref']
                   workspaceRef = workspaceUrl.split("/")
                   workspaceReflen= len(workspaceRef)
                   projectUrl=iterationRes['Iteration']['Project']['_ref']
                   projectRef = projectUrl.split("/")
                   projectReflen= len(projectRef)
                   iterationRef = iterationRes['Iteration']['_ref']
                   iterationReflen= len(iterationRef)
                   iterationID= iterationRef[iterationReflen-1]
                   responseTemplate = self.config.get('dynamicTemplate', {}).get('iterationResponseTemplate', None)
                   injectIterationData['workspaceID']= int(workspaceRef[workspaceReflen-1])
                   injectIterationData['projectID']= int(projectRef[projectReflen-1])
                   injectIterationData['iterationID']=int(iterationID)
                   data_iteration = data_iteration + self.parseResponse(responseTemplate, iterationRes, injectIterationData)
            pageNum = 1 + pageNum
        self.publishToolsData(data, storyMetadata)
        self.publishToolsData(data_iteration, relationMetadata)
        self.tracking['LastUpdatedDate'] = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%dT%H:%M:%SZ')
        self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    RallyAgent()       
