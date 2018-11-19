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
        startFrom = parser.parse(startFrom, ignoretz=True)
        domainName = "InSightsAlmAgent:"
        authToken = base64.b64encode(domainName.encode('utf-8'))
        token = self.login(authToken, username, password, baseUrl)
        headers = {"accept": "application/json","Authorization": "bearer "+token}
        pagination = ["test-cases", "requirements", "test-runs", "trace-matrix-report", "defects"]
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
                            page_size = 25
                            data = []
                            almEntityRestDetails = self.almEntityRestDetails(entityType, projectId, baseUrl, pagination)
                            entityUpdatedDate = almEntityRestDetails.get('entityUpdatedDate', None)
                            if entityUpdatedDate is not None:
                                startFrom = parser.parse(entityUpdatedDate, ignoretz=True)
                            nextPageResponse = True
                            entity_type_available = False
                            while nextPageResponse:
                                restUrl = almEntityRestDetails.get('restUrl', None) + almEntityRestDetails.get('entityType', None) + "?expandProps=false&expandSteps=false&page=" + str(page_num) + "&size=" + str(page_size) + almEntityRestDetails.get('dateTimeStamp', None) + urllib.quote_plus(startFrom.strftime(timeStampFormat)) + "Z"
                                entityTypeResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
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
                                                        data += self.parseResponse(responseTemplate, res, injectData)
                                            #Trace matrix do not support date time format. This is why it is outside if else.
                                            if entityType == "trace-matrix-report":
                                                dataMatrix = []
                                                traceMatrixReport = res.get("requirements", {})
                                                parentId = res.get("id", None)
                                                parentName = res.get("name", None)
                                                for matrix in traceMatrixReport:
                                                    injectData= {}
                                                    if "testcases" in matrix:
                                                        injectData['projectName'] = projectName
                                                        injectData['parentId'] = parentId
                                                        injectData['parentName'] = parentName
                                                        injectData['projectId'] = projectId
                                                        injectData['almType'] = "trace-matrix-report"
                                                        injectData['testcases'] = matrix.get("testcases", None)
                                                        injectData['linkedTestCases'] = matrix.get("linked-testcases", None)
                                                        injectData['pid'] = matrix.get("id", None)
                                                        dataMatrix.append(injectData)
                                                if len(dataMatrix) > 0:
                                                    self.publishToolsData(dataMatrix, metadata)
                                    except Exception as ex:
                                        nextPageResponse = False
                                        entity_type_available = False
                                        logging.error("ProjectID: " + str(projectId) + " Type: " +str(entityType) + str(ex))
                                        break
                                else:
                                    nextPageResponse = False
                                if almEntityRestDetails.get('pagination', False):
                                    if entityType == "defects":
                                        page_size = page_size + 25
                                    else:
                                        page_num = page_num + 1
                                else:
                                    nextPageResponse = False
                            if entity_type_available and entityUpdatedDate is not None:
                                trackingDetails[entityType] = {"entityUpdatedDate": entityUpdatedDate}
                            metadata = self.config.get("dynamicTemplate", {}).get("almEntityMetaData", None)
                            if len(data) > 0:
                                self.publishToolsData(data, metadata)
                        self.tracking[str(projectId)] = trackingDetails
                        self.updateTrackingJson(self.tracking)                        
        finally:
            self.logout(token, baseUrl)
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
if __name__ == "__main__":
    QtestAgent()       
