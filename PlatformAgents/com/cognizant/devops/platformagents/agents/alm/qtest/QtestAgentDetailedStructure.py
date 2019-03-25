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
from ....core.BaseAgent import BaseAgent
from datetime import datetime as dateTime2
from dateutil import parser
import datetime
import json
import logging.handlers
import base64

class QtestAgent(BaseAgent):
    def process(self):
        baseUrl = self.config.get("baseUrl", None)
        username = self.config.get("username", None)
        password = self.config.get("password", None)
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom, ignoretz=True)
        domainName = "InSightsAlmAgent" + ":"
        authToken = base64.b64encode(domainName.encode('utf-8'))
        token = self.login(authToken, username, password, baseUrl)
        headers = {"accept": "application/json","Authorization": "bearer "+str(token)+""}
        pagination = ["test-cases", "requirements", "test-runs"]
        #In this part we addthe module name where pagination is supported.
        try:
            projectsList = self.getResponse(baseUrl+"/api/v3/projects?assigned=false", 'GET', None, None, None, None, headers)
            #projectListConfig = self.config.get("dynamicTemplate", {}).get("projectList", '')
            projectListConfig = []
            pagination = ["test-cases", "requirements", "test-runs", "trace-matrix-report"]
            filterResponseAlmEntityType = ["defects"]
            almEntities = self.config.get("dynamicTemplate", {}).get("almEntities", None)
            if len(projectsList) > 0:
                for projects in projectsList:
                    projectName = projects.get("name", None)
                    projectId = projects.get("id", None)
                    if self.projectFilterBoolean(projectId, projectListConfig):
                        pass
                    else:
                        continue
                    trackingDetails = self.tracking.get(str(projectId),None)
                    if trackingDetails is None:
                        trackingDetails = {}
                        self.tracking[str(projectId)] = trackingDetails
                    projectStartDate = projects.get("start_date", None)
                    if parser.parse(projectStartDate, ignoretz=True) < startFrom:
                        continue
                    projectLastUpdateDate = self.tracking.get(str(projectId), {}).get("lastupdated", None)
                    if projectLastUpdateDate is not None:
                        startFrom = parser.parse(projectLastUpdateDate, ignoretz=True)
                    if len(almEntities) > 0:                   
                        projectMaxUpdateDate = None
                        for entityType in almEntities:
                            data = []
                            entityUpdatedDate = self.tracking.get(str(projectId), {}).get(entityType, {}).get("entityUpdatedDate", None)
                            if entityUpdatedDate is not None:
                                startFrom = parser.parse(entityUpdatedDate, ignoretz=True)
                            if entityType != "self":
                                #restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/" + str(entityType)
                                restUrl = self.generateDynamicUrl(entityType, projectId, baseUrl, False, None, None)
                                if entityType in pagination:
                                    page_num = 1
                                    page_size = 25
                                    #restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/" + str(entityType) + "?page=" + str(page_num) + "&size=" + str(page_size) + "&expandProps=false&expandSteps=false"
                                    restUrl = self.generateDynamicUrl(entityType, projectId, baseUrl, True, page_num, page_size)
                                nextPageResponse = True
                                entityTypeResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
                                entity_type_available = False
                                while nextPageResponse:
                                    if entityType in pagination and "items" in entityTypeResponse and len(entityTypeResponse["items"]) == 0:
                                        break
                                    elif entityType in pagination and "items" in entityTypeResponse and len(entityTypeResponse["items"]) > 0:
                                        entityTypeResponse = entityTypeResponse.get("items", {})
                                    else:
                                        pass
                                    if entityType in filterResponseAlmEntityType:
                                        entityTypeResponse = self.filterDataStructure(entityType, entityTypeResponse)
                                    if len(entityTypeResponse) > 0:
                                        entity_type_available = True
                                        try:
                                            for res in entityTypeResponse:
                                                lastUpdated = res.get('last_modified_date', None)
                                                if lastUpdated > entityUpdatedDate:
                                                    entityUpdatedDate = lastUpdated
                                                if lastUpdated > projectMaxUpdateDate:
                                                    projectMaxUpdateDate = lastUpdated
                                                if lastUpdated is not None:
                                                    lastUpdated = parser.parse(lastUpdated, ignoretz=True)
                                                if lastUpdated is not None and lastUpdated > startFrom:
                                                    responseTemplate = almEntities.get(entityType, None)
                                                    if responseTemplate:
                                                        injectData= {}
                                                        injectData['projectName'] = projectName
                                                        injectData['projectId'] = projectId
                                                        injectData['almType'] = entityType
                                                        data += self.parseResponse(responseTemplate, res, injectData)
                                        except Exception as ex:
                                            nextPageResponse = False
                                            entity_type_available = False
                                            logging.error("ProjectID: " + str(projectId) + " Type: " +str(entityType) + str(ex))
                                            break
                                    else:
                                        nextPageResponse = False
                                    if entityType in pagination:
                                        page_num = page_num + 1
                                        #restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/" + str(entityType) + "?page=" + str(page_num) + "&size=" + str(page_size) + "&expandProps=false&expandSteps=false"
                                        restUrl = self.generateDynamicUrl(entityType, projectId, baseUrl, True, page_num, page_size)
                                        entityTypeResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
                                    else:
                                        nextPageResponse = False
                                if entity_type_available and entityUpdatedDate is not None:
                                    trackingDetails[entityType] = {"entityUpdatedDate": entityUpdatedDate}
                            metadata = self.config.get("dynamicTemplate", {}).get("almEntityMetaData", None)
                            if len(data) > 0:
                                self.publishToolsData(data, metadata)
                        trackingDetails["lastupdated"] = projectMaxUpdateDate
                        self.tracking[str(projectId)] = trackingDetails
                        self.updateTrackingJson(self.tracking)
                    '''
                    enableTraceMatrixReport = self.config.get("enableTraceMatrixReport", False)
                    if enableTraceMatrixReport:
                        self.getTraceMatrixReport(baseUrl, projectId, projectName, headers)
                    defectMetaData = self.config.get("dynamicTemplate", {}).get("defectMetaData", None)
                    if defectMetaData:
                        self.getDefectsReport(baseUrl, startFrom, projectId, projectName, headers, defectMetaData)
                    '''
        finally:
            self.logout(token, baseUrl)
    def login(self, authToken, username, password, baseUrl):
        headers_token = {'accept': "application/json",'content-type': "application/x-www-form-urlencoded",'authorization': "Basic "+str(authToken)+""}
        payload = "grant_type=password&username="+str(username)+"&password="+str(password)
        tokenResponse = self.getResponse(baseUrl+"/oauth/token", 'POST', None, None, payload, None, headers_token)
        return tokenResponse.get("access_token", None)
    def logout(self, token, baseUrl):
        headerTokenRevoke = {"Authorization": "bearer "+str(token)+""}
        tokenResponse = self.getResponse(baseUrl+"/oauth/revoke", 'POST', None, None, None, None, headerTokenRevoke)
    def projectFilterBoolean(self, projectId, projectListConfig):
        if len(projectListConfig) > 0 and projectId in projectListConfig:
            return True
        elif len(projectListConfig) == 0:
            return True
        else:
            return False
    def filterDataStructure(self, almType, responseObj):
        objs = {almType : True}
        for key, value in objs.iteritems():
            if value == True:
                return responseObj.get(key, None)
    def generateDynamicUrl(self, almType, projectId, baseUrl, pagination, page_num, page_size):
        urlExtension = {
                        "trace-matrix-report": "requirements/trace-matrix-report",
                        "test-cases": "test-cases",
                        "test-runs": "test-runs",
                        "requirements": "requirements",
                        "releases": "releases",
                        "defects": "defects",
                        "modules": "modules",
                        "test-suites": "test-suites",
                        "test-cycles": "test-cycles",
                        }
        expandProps = "&expandProps=false&expandSteps=false"
        if almType == "trace-matrix-report":
            expandProps = ""
        if almType in urlExtension:
            if pagination:
                return baseUrl + "/api/v3/projects/" + str(projectId) + "/" + str(urlExtension.get(almType, "")) + "?page=" + str(page_num) + "&size=" + str(page_size) + expandProps
            else:
                return baseUrl + "/api/v3/projects/" + str(projectId) + "/" + str(urlExtension.get(almType, ""))
    '''
    def getDefectsReport(self, baseUrl, startFrom, projectId, projectName, headers, defectMetaData):
        data = []
        projectLastUpdateDate = self.tracking.get(str(projectId), {}).get("lastUpdatedDefects", None)
        if projectLastUpdateDate is not None:
            startFrom = parser.parse(projectLastUpdateDate, ignoretz=True)
        else:
            startFrom = startFrom
        restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/defects"
        defectsResponse = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
        if len(defectsResponse.get("defects", [])) > 0:
            for defect in defectsResponse.get("defects"):
                lastUpdated = defect.get('last_modified_date', None)
                if projectLastUpdateDate < lastUpdated:
                    projectLastUpdateDate = lastUpdated
                lastUpdated = parser.parse(lastUpdated, ignoretz=True)
                if lastUpdated > startFrom:
                    responseTemplate = self.config.get("dynamicTemplate", {}).get("defectMetaData", {}).get("responseTemplate", None)
                    if responseTemplate:
                        injectData= {}
                        injectData['projectName'] = projectName
                        injectData['projectId'] = projectId
                        injectData['almType'] = "defect"
                        data += self.parseResponse(responseTemplate, defect, injectData)
            metadata = defectMetaData.get("metadata", None)
            if len(data) > 0:
                self.publishToolsData(data, metadata)
            trackingDetails = self.tracking.get(str(projectId),None)
            trackingDetails["lastUpdatedDefects"] = projectLastUpdateDate
            self.tracking[str(projectId)] = trackingDetails
            self.updateTrackingJson(self.tracking)                  
                    
    def getTraceMatrixReport(self, baseUrl, projectId, projectName, headers):
        dataTraceMatrix = []
        metaDataTraceMatrix = self.config.get("dynamicTemplate", {}).get("traceMatrixReportMetadata", None)
        page_size = 25
        page_num = 1
        restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/requirements/trace-matrix-report" + "?page=" + str(page_num) + "&size=" + str(page_size)
        traceMatrixReport = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
        nextPageResponse = True
        while nextPageResponse:
            if len(traceMatrixReport) > 0:
                try:
                    traceMatrixReport = traceMatrixReport[0].get("requirements", None)
                    for matrix in traceMatrixReport:
                        injectData= {}
                        if 'testcases' in matrix:
                            injectData['projectName'] = projectName
                            injectData['projectId'] = projectId
                            injectData['testcases'] = matrix.get('testcases', None).split(',')
                            injectData['linkedTestcaseCount'] = matrix.get('linked-testcases', None)
                            injectData['pid'] = matrix.get('id', None)
                            dataTraceMatrix.append(injectData)
                except Exception as ex:
                    nextPageResponse = False
                    logging.error(ex)
                    break
            else:
                nextPageResponse = False
            page_num = page_num + 1
            restUrl = baseUrl + "/api/v3/projects/" + str(projectId) + "/requirements/trace-matrix-report" + "?page=" + str(page_num) + "&size=" + str(page_size)
            traceMatrixReport = self.getResponse(restUrl, 'GET', None, None, None, None, headers)
        if len(dataTraceMatrix) > 0:
            self.publishToolsData(dataTraceMatrix, metaDataTraceMatrix)
    '''

            
if __name__ == "__main__":
    QtestAgent()       
