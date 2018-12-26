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
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
from datetime import datetime as dateTime2
from dateutil import parser

import datetime
import json
import logging.handlers
import base64
import urllib

class QtestAgent(BaseAgent):
    def process(self):
        baseUrl = self.config.get("baseUrl", None)
        username = self.config.get("username", None)
        password = self.config.get("password", None)
        startFrom = self.config.get("startFrom", '')
        timeStampFormat = self.config.get("timeStampFormat", '')
        extensions = self.config.get('dynamicTemplate', {}).get('extensions', None)
        startFrom = parser.parse(startFrom, ignoretz=True)
        domainName = "InSightsAlmAgent:"
        self.authToken = base64.b64encode(domainName.encode('utf-8'))
        self.token = self.login(self.authToken, username, password, baseUrl)
        headers = {"accept": "application/json","Authorization": "bearer "+self.token}
        pagination = ["test-cases", "requirements", "test-runs", "trace-matrix-report", "defects"]
        self.username , self.password , self.headers , self.baseUrl = username, password, headers, baseUrl
        #In this part we addthe module name where pagination is supported.
        try:
            projectsList = self.getResponse(baseUrl+"/api/v3/projects?assigned=false", 'GET', None, None, None, None, headers)
            almEntities = self.config.get("dynamicTemplate", {}).get("almEntities", None)
            if len(projectsList) > 0:
                for projects in projectsList:
                    projectName = projects.get("name", None)
                    projectId = projects.get("id", None)
                    trackingDetails = self.tracking.get(str(projectId),None)
                    if trackingDetails is None:
                        trackingDetails = {}
                        self.tracking[str(projectId)] = trackingDetails
                    projectStartDate = projects.get("start_date", None)
                    if len(almEntities) > 0:                   
                        entityUpdatedDate = None
                        for entityType in almEntities:
                            page_num = 1
                            page_size = 10
                            data = []
                            metadata = self.config.get("dynamicTemplate", {}).get("almEntityMetaData", None)
                            almEntityRestDetails = self.almEntityRestDetails(entityType, projectId, baseUrl, pagination)
                            entityUpdatedDate = almEntityRestDetails.get('entityUpdatedDate', None)
                            if entityUpdatedDate is not None:
                                startFrom = parser.parse(entityUpdatedDate, ignoretz=True)
                            nextPageResponse = True
                            entity_type_available = False
                            reqIdList = trackingDetails.get(entityType, {}).get("entityIdDict", None)
                            if reqIdList is None:
                                reqIdList = []
                            while nextPageResponse:
                                restUrl = almEntityRestDetails.get('restUrl', None) + almEntityRestDetails.get('entityType', None) + "?expandProps=true&expandSteps=false&expand=descendants&page=" + str(page_num) + "&size=" + str(page_size) + almEntityRestDetails.get('dateTimeStamp', None) + urllib.quote_plus(startFrom.strftime(timeStampFormat)) + "Z"
                                try:
                                    entityTypeResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
                                except Exception as ex1:
                                    nextPageResponse = False
                                    logging.error("ProjectID: " + str(projectId) + " Type: " +str(entityType) + " URL: " + str(restUrl) + "  " + str(ex1))
                                    break
                                if entityType in pagination and "items" in entityTypeResponse and len(entityTypeResponse["items"]) == 0:
                                    break
                                elif entityType in pagination and "items" in entityTypeResponse and len(entityTypeResponse["items"]) > 0:
                                    entityTypeResponse = entityTypeResponse.get("items", {})
                                else:
                                    pass
                                if len(entityTypeResponse) > 0:
                                    entity_type_available = True
                                    try:
                                        for res in entityTypeResponse:
                                            lastUpdated = res.get('last_modified_date', None)
                                            if lastUpdated > entityUpdatedDate:
                                                entityUpdatedDate = lastUpdated
                                            if lastUpdated is not None:
                                                lastUpdated = parser.parse(lastUpdated, ignoretz=True)
                                                if lastUpdated > startFrom:
                                                    responseTemplate = almEntities.get(entityType, None)
                                                    if responseTemplate:
                                                        injectData= {}
                                                        injectData['projectName'] = projectName
                                                        injectData['projectId'] = projectId
                                                        injectData['almType'] = entityType
                                                        #EXTRACTION OF JIRA-KEY FROM NAME FIELD IN REQUIREMENTS.
                                                        if entityType == 'requirements':
                                                            if 'name' in res:
                                                                #matchObj = re.match( r'(.*)-(.*?) .*', res.get('name', ''), re.M|re.I)
                                                                #injectData['jiraKey'] = '-'.join(matchObj.group(1, 2))
                                                                #FOR NOW ASSUMING THAT IN NAME FIELD FIRST WORD WILL BE JIRA KEY.
                                                                injectData['jiraKey'] = res.get('name', '').split(' ')[0]
                                                        #EXTRACTION PROPERTY VALUES FROM API RESPONSE.
                                                        if str(res.get('id')) not in reqIdList:
                                                            reqIdList.append(str(res.get('id')))
                                                        if 'properties' in res:
                                                            for property in res.get('properties', []):
                                                                injectData[str(property.get('field_name').lower()).replace(' ', '')] = property.get('field_value')
                                                        data += self.parseResponse(responseTemplate, res, injectData)
                                                        #FOR COLLECTING DATA BEYOND PARENT/ROOT LEVEL. FUNCTION DEFINITION IS AT THE BOTTOM.
                                                        #self.injectResponseData(data, responseTemplate, res, projectName, projectId, entityType)
                                    except Exception as ex:
                                        nextPageResponse = False
                                        entity_type_available = False
                                        logging.error("ProjectID: " + str(projectId) + " Type: " +str(entityType) + str(ex))
                                        break
                                else:
                                    nextPageResponse = False
                                if almEntityRestDetails.get('pagination', False):
                                    if entityType == "defects":
                                        page_size = page_size + 10
                                    else:
                                        page_num = page_num + 1
                                else:
                                    nextPageResponse = False
                            if entity_type_available and entityUpdatedDate is not None:
                                if len(reqIdList) > 0 and entityType in extensions.get('linkedArtifacts', {}).get('almEntities', {}):
                                    trackingDetails[entityType] = {"entityUpdatedDate": entityUpdatedDate, "entityIdDict": reqIdList}
                                else:
                                    trackingDetails[entityType] = {"entityUpdatedDate": entityUpdatedDate}
                            if len(data) > 0:
                                self.publishToolsData(data, metadata)
                        self.tracking[str(projectId)] = trackingDetails
                        self.updateTrackingJson(self.tracking)                        
        finally:
            self.logout(self.token, baseUrl)
    def login(self, authToken, username, password, baseUrl):
        headers_token = {'accept': "application/json",'content-type': "application/x-www-form-urlencoded",'authorization': "Basic "+str(authToken)+""}
        payload = "grant_type=password&username="+str(username)+"&password="+str(password)
        tokenResponse = self.getResponse(baseUrl+"/oauth/token", 'POST', None, None, payload, None, headers_token)
        if "error" in tokenResponse:
            logging.error("InValid Credentails")
        return tokenResponse.get("access_token", None)
    def logout(self, token, baseUrl):
        headerTokenRevoke = {"Authorization": "bearer "+str(token)+""}
        tokenResponse = self.getResponse(baseUrl+"/oauth/revoke", 'POST', None, None, None, None, headerTokenRevoke)
    def filterDataStructure(self, almType, responseObj):
        objs = {almType : True}
        for key, value in objs.iteritems():
            if value == True:
                return responseObj.get(key, None)
    def almEntityRestDetails(self, entityType, projectId, baseUrl, paginationList):
        urlExtension = {
                        "trace-matrix-report": "requirements/trace-matrix-report",
                        "defects": "defects/last-change"
                        }
        restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/"
        entityRestDetails = {}
        entityRestDetails['restUrl'] = restUrl
        entityRestDetails['entityType'] = entityType
        entityRestDetails['pagination'] = False
        entityRestDetails['dateTimeStamp'] = '&startTime='
        entityRestDetails['entityUpdatedDate'] = self.tracking.get(str(projectId), {}).get(entityType, {}).get("entityUpdatedDate", None)
        if entityType in urlExtension:
            entityRestDetails['entityType'] = str(urlExtension.get(entityType, ""))
        if entityType in paginationList:
            entityRestDetails['pagination'] = True
        return entityRestDetails
    def scheduleExtensions(self):
        extensions = self.config.get('dynamicTemplate', {}).get('extensions', None)
        if extensions:
            linkedArtifacts = extensions.get('linkedArtifacts', None)
            if linkedArtifacts:
                self.registerExtension('linkedArtifacts', self.retrieveLinkedArtifacts, linkedArtifacts.get('runSchedule'))
            requirementMatrix = extensions.get('requirementMatrix', None)
            if requirementMatrix:
                self.registerExtension('requirementMatrix', self.retrieveRequirementMatrix, requirementMatrix.get('runSchedule'))
    def retrieveLinkedArtifacts(self):
        try:
            token = self.login(self.authToken, self.username , self.password , self.baseUrl)
            headers = {"accept": "application/json","Authorization": "bearer "+token}
            linkedArtifacts = self.config.get('dynamicTemplate', {}).get('extensions', {}).get('linkedArtifacts', None)
            trackingDetails = self.tracking
            try:
                for project in trackingDetails:
                    data = []
                    for almEntity in trackingDetails.get(str(project), {}):
                        if almEntity in linkedArtifacts.get('almEntities', None):
                            projectTrackingDetails = trackingDetails.get(str(project))
                            entityIdDict = projectTrackingDetails.get(almEntity, {}).get('entityIdDict', None)
                            if entityIdDict is None:
                                continue
                            try:
                                dictIsNotEmpty = True
                                start = 0
                                end = 15
                                while dictIsNotEmpty:
                                    linkedArtifactUrl = "{}/api/v3/projects/{}/linked-artifacts?type={}&ids={}".format(self.baseUrl, str(project), almEntity, ','.join(entityIdDict[start:end]))
                                    entityTypeResponse = self.getResponse(linkedArtifactUrl, 'GET', None, None, None, None, headers)
                                    for res in entityTypeResponse:
                                        parentId = res.get('id', None)
                                        if 'objects' in res:
                                            injectData = {}
                                            injectData['id'] = parentId
                                            injectData['projectId'] = int(project)
                                            injectData['almType'] = almEntity
                                            for object in res.get('objects', None):                                        
                                                almType = (object.get('self', None).split('/')[-2]).replace('-', '')
                                                if almType not in injectData:
                                                    injectData[almType] = [object.get('id')]
                                                else:
                                                    injectData[almType].append(object.get('id', None)) 
                                            data.append(injectData)
                                    start = end
                                    end = end + 15
                                    if len(entityIdDict[start:end]) == 0:
                                        dictIsNotEmpty = False
                            finally:
                                self.tracking.get(str(project)).get(almEntity, {}).pop('entityIdDict')
                    if len(data) > 0:
                        metadata = self.config.get("dynamicTemplate", {}).get('extensions', {}).get('linkedArtifacts', {}).get("almEntityMetaData", None)
                        self.publishToolsData(data, metadata)
                        self.updateTrackingJson(self.tracking)
            except Exception as ex1:
                logging.error("Error in retrieveLinkedArtifacts which is part of extensions in " + str(ex1))
        finally:
            self.logout(token, self.baseUrl)

    def retrieveRequirementMatrix(self):
        dataMatrix = []
        metadata = self.config.get("dynamicTemplate", {}).get('extensions', {}).get('requirementMatrix', {}).get("almEntityMetaData", None)
        try:
            token = self.login(self.authToken, self.username , self.password , self.baseUrl)
            headers = {"accept": "application/json","Authorization": "bearer "+token}
            trackingDetails = self.tracking
            for project in trackingDetails:
                almEntityRestDetails = self.almEntityRestDetails("trace-matrix-report", project, self.baseUrl, ['trace-matrix-report'])
                nextPageResponse = True
                page_num = 1
                page_size = 25
                while nextPageResponse:
                    restUrl = almEntityRestDetails.get('restUrl', None) + almEntityRestDetails.get('entityType', None) + "?page=" + str(page_num) + "&size=" + str(page_size)
                    try:
                        entityTypeResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)                
                        if entityTypeResponse.__len__() > 0:
                            for res in entityTypeResponse:
                                traceMatrixReport = res.get("requirements", {})
                                for matrix in traceMatrixReport:
                                    injectData= {}
                                    if "testcases" in matrix:
                                        injectData['modulesId'] = res.get('id', None)
                                        injectData['projectId'] = int(project)
                                        injectData['almType'] = "trace-matrix-report"
                                        injectData['testcases'] = matrix.get("testcases", None)
                                        injectData['linkedTestCases'] = matrix.get("linked-testcases", None)
                                        injectData['id'] = matrix.get("id", None)
                                        dataMatrix.append(injectData)
                        else:
                            nextPageResponse = False
                    except Exception as urlError:
                        nextPageResponse = False
                        break
                    page_num = page_num + 1
        finally:
            self.logout(token, self.baseUrl)
        if len(dataMatrix) > 0:
            self.publishToolsData(dataMatrix, metadata)
    '''
    def injectResponseData(self, data, responseTemplate, res, projectName, projectId, entityType):
        injectData= {}
        injectData['projectName'] = projectName
        injectData['projectId'] = projectId
        injectData['almType'] = entityType
        if entityType == 'requirements':
            if 'name' in res:
                injectData['jiraKey'] = res.get('name', '').split(' ')[0]
                combinedProperty = {}
                if 'properties' in res:
                    for property in res.get('properties', []):
                        injectData[property.get('field_name')] = property.get('field_value')
        if "children" in res:
            for child in res.get('children', None):
                data += self.parseResponse(responseTemplate, child, injectData)
                self.injectResponseData(data, responseTemplate, child.get('children', None), projectName, projectId, entityType)
        data += self.parseResponse(responseTemplate, res, injectData)
    '''
if __name__ == "__main__":
    QtestAgent()       
