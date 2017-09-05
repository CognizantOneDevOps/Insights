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
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import time
class HpAlmAgent(BaseAgent):
    def getHpAlmSSOHeader(self, baseEndPoint):
        userid = self.config.get('userid')
        passwd = self.config.get('passwd')
        authEndPoint = baseEndPoint + '/qcbin/authentication-point/authenticate' 
        responseTupple = {}
        reqHeaders = {
                        "Content-Type" : "application/xml",
                        "Accept" :  "application/xml"
                      }
        self.getResponse(authEndPoint, 'POST', userid, passwd, None, reqHeaders=reqHeaders, responseTupple=responseTupple)
        ssoCookie = responseTupple['cookies']['LWSSO_COOKIE_KEY']
        cookieHeader = {
                       "Cookie" : 'LWSSO_COOKIE_KEY='+ssoCookie+';'
                       }
        siteSessionEndPoint = baseEndPoint + '/qcbin/rest/site-session'
        self.getResponse(siteSessionEndPoint, 'POST', None, None, None, reqHeaders=cookieHeader, responseTupple=responseTupple)
        cookieHeader = {
                       "Cookie" : 'LWSSO_COOKIE_KEY='+ssoCookie+';QCSession='+responseTupple['cookies']['QCSession']
                       }
        return cookieHeader
    
    def signOut(self, baseEndPoint):
        signOutEndPoint = baseEndPoint + '/qcbin/authentication-point/logout'
        self.getResponse(signOutEndPoint, 'GET', None, None, None, reqHeaders={})
    
    def getDomains(self, baseEndPoint, cookieHeader):
        projectsEndPoint = baseEndPoint + '/qcbin/rest/domains?alt=application/json&include-projects-info=y'
        projectResponse = self.getResponse(projectsEndPoint, 'GET', None, None, None, reqHeaders=cookieHeader)
        return projectResponse
    
    def getProjectDetails(self, baseEndPoint, reqHeaders, domain, project, entityName, fields, startFrom):

        domainTracking = self.tracking.get(domain, None)
        if domainTracking == None:
            domainTracking = {}
            self.tracking[domain] = domainTracking

        projectTracking = domainTracking.get(project, None)
       
        if projectTracking == None:
            projectTracking = {}
            domainTracking[project] = projectTracking

        entityTracking = projectTracking.get(entityName, None)

        projectEndPoint = baseEndPoint + '/qcbin/rest/domains/' + domain + '/projects/' + project + '/' + entityName + '?alt=application/json&'+fields
        if entityTracking == None:
            if(entityName == "releases"):
                projectEndPoint += '&query={start-date[>"'+startFrom+'"]}&order-by={start-date[ASC]}'
            else:
                projectEndPoint += '&query={last-modified[>"'+startFrom+'"]}&order-by={last-modified[ASC]}'
        else:
           
            if(entityName == "releases"):
                projectEndPoint += '&query={start-date[>"'+entityTracking+'"]}&order-by={start-date[ASC]}'
            else:
                projectEndPoint += '&query={last-modified[>"'+entityTracking+'"]}&order-by={last-modified[ASC]}'

        projectResponse = self.getResponse(projectEndPoint, 'GET', None, None, None, reqHeaders=reqHeaders)
        
        totalResults = projectResponse.get("TotalResults",0)
        
        dataList = []

        totalLoopInit = totalResults/200
       
        excessCount = totalResults-(totalLoopInit*200)
        
        totalLoop = totalLoopInit+1
        for i in range(totalLoop):
            startIndexInit = i
            i= i+1
            if i > totalLoopInit:
                page_size = excessCount
            else:
                page_size = 200
            
            startIndex=(startIndexInit*200)+1
            projectEndPointLoop = projectEndPoint+'&page-size='+str(page_size)+'&start-index='+str(startIndex)


            projectResponse = self.getResponse(projectEndPointLoop, 'GET', None, None, None, reqHeaders=reqHeaders)
           
            if totalResults > 0:
                entities = projectResponse.get("entities", [])
                for entity in entities:
                    data = {}
                    data['domain'] = domain
                    data['project'] = project
                    data['type'] = entity['Type']
                    fields = entity['Fields']
                    
                    
                    for field in fields:
                        values = field['values']
                        for value in values:
                            
                            fieldValue = value.get('value', '')
                           
                            data[field['Name']] = fieldValue
                    dataList.append(data)
                                   
            if len(dataList) > 0:
                latestRecord = dataList[len(dataList) - 1]
                if(entityName=="releases"):
                    projectTracking[entityName] = latestRecord['start-date']
                else:
                    projectTracking[entityName] = latestRecord['last-modified']
        
        return dataList
        
    def process(self):
        baseEndPoint = self.config.get('baseEndPoint')
        cookieHeader = self.getHpAlmSSOHeader(baseEndPoint)
        domainResponse = self.getDomains(baseEndPoint, cookieHeader)
        dataList = []
        startFrom = self.config.get("startFrom", '')
        almEntities = self.config.get("almEntities")
        if almEntities:
            for almEntity in almEntities:
                fieldsList = almEntities[almEntity]
                if len(fieldsList) > 0:
                    fields = 'fields='
                    for field in fieldsList:
                        fields += field + ','
                    domains = domainResponse.get('Domain')
                    for domain in domains:
                        domainName = domain['Name']
                        projects = domain['Projects']['Project']
                        for project in projects:
                            
                            projectName = project['Name']
                            dataList += self.getProjectDetails(baseEndPoint, cookieHeader, domainName, projectName, almEntity, fields, startFrom)
        if len(dataList) > 0 :
            self.publishToolsData(dataList)
            self.updateTrackingJson(self.tracking)
        self.signOut(baseEndPoint)
if __name__ == "__main__":
    HpAlmAgent()        
