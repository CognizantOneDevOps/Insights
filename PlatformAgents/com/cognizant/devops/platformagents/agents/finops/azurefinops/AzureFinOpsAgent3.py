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
from boto3 import resource

'''
Created on Aug 24, 2022
@author: 716660
'''

import time
from ....core.BaseAgent3 import BaseAgent
from datetime import date
import datetime
import json
from _cffi_backend import typeof
import string
import time
import os
import sys
from dateutil import parser
from ....core.FinOpsUtilities import FinOpsUtilities


class AzureFinOpsAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        
       
        self.apiURL = self.getCredential("apiURL")
        self.endDateToday = date.today().strftime("%Y-%m-%dT%H:%M:%S")
        self.dynamicTemplate = self.config.get('dynamicTemplate', '{}')
        self.uniqueResouseTypeList = [] 
        self.uniqueResouseIdList: List[str] = []
        self.startFromConfig = self.getCredential("startFrom")
        self.startDateFromTracking = self.tracking.get('lastDataCollectionDate',None)
        self.headersWithAuth = {}
        self.contentTypeJson = 'application/json'
        self.collectForecastData = self.config.get('collectForecastData', False)
        self.collectBudgetData = self.config.get('collectBudgetData', False)
        self.agentDir = os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep
        self.baseURL = self.getCredential("baseURL")
        self.subscription_Id = self.getCredential("azureSubscriptionId")
        self.tenant_Id = self.getCredential("azureTenantId")
        self.client_Id = self.getCredential("azureClientId")
        self.secret = self.getCredential("azureSecretkey")

        try:
            
            self.finopsUtilities = FinOpsUtilities(self.agentDir, parentclass=self)
            
            self.subscriptionList = self.getsubscriptionList()
            
            for subscription in self.subscriptionList:
                resourceGroupList  = self.getresourceGroupList(subscription) 
                self.getCostManagementList(subscription)
                
                if self.collectForecastData:
                    self.finopsUtilities.processForecastRecords(subscription, resourceGroupList)
                
                if self.collectBudgetData:
                    self.finopsUtilities.processBudgetData(subscription)
            
        except Exception as ex:
            self.baseLogger.error(ex)
            
    def scheduleExtensions(self):
        extensions = self.config.get('dynamicTemplate', {}).get('extensions', None)
        if extensions:
            apiVersions = extensions.get('apiVersions', None)
            enableApiVersions = extensions.get('enableApiVersions', None)
            if enableApiVersions:
                self.registerExtension('apiVersions', self.finopsUtilities.fetchResourceTypeWiseAPIVersions, apiVersions.get('runSchedule'))
            
    def getsubscriptionList(self):
        
        self.finopsUtilities.prepareRequestHeader(self.contentTypeJson)
        subscriptionurl = self.apiURL + '/subscriptions?api-version=2020-01-01'
        response_subscription = self.getResponse(subscriptionurl, 'GET', None, None, data=None, reqHeaders=self.finopsUtilities.headersWithAuth)
        #self.baseLogger.info(response_subscription)
        return response_subscription['value']
    
    def getresourceGroupList(self, subscription):
        
        resourceGroupList = []
        self.headersWithAuth = self.finopsUtilities.prepareRequestHeader(self.contentTypeJson)
        
        #for subscription in self.subscriptionList:
        subscriptionurl = self.apiURL + "/subscriptions/" + subscription['subscriptionId'] + "/resourcegroups?api-version=2021-04-01"
        response_rg = self.getResponse(subscriptionurl, 'GET', None, None, data=None, reqHeaders=self.finopsUtilities.headersWithAuth)
        resourceGroupList = response_rg['value']
        #self.baseLogger.info(response_rg)
        return resourceGroupList
 


    def getCostManagementList(self, subscription):
        
        self.finopsUtilities.prepareRequestHeader(self.contentTypeJson)
        
        requestBody = str(self.dynamicTemplate.get("costManagement", "{}").get("request", "{}"))
        costMetadata = self.dynamicTemplate.get("costManagement", "{}").get("costMetadata", "{}")
        costReqinsighstTimeX = self.dynamicTemplate.get('costManagement', {}).get('insightsTimeXFieldMapping', None)
        costReqtimestamp = costReqinsighstTimeX.get('timefield', None)
        costReqtimeformat = costReqinsighstTimeX.get('timeformat', None)
        costReqisEpoch = costReqinsighstTimeX.get('isEpoch', False)
        nextPageFetch = True
        pagesize = 500
        usageDate = None
        
        self.fetchStartFromDate(nextPageFetch)
        
        apiVersion = self.finopsUtilities.getApiVersion("2021-10-01", "microsoft.costmanagement/query")        
      
        costAPIurl = self.apiURL + "/subscriptions/" + subscription['subscriptionId'] + "/providers/Microsoft.CostManagement/query?api-version="+apiVersion+"&$top=" + str(pagesize)

        while nextPageFetch:
            costData = []
            try:
                requestBody = requestBody.replace("startFrom", self.startFrom)
                requestBody = requestBody.replace("endTo",self.endDateToday)
                self.baseLogger.info("costAPIurl =========== " + costAPIurl +" startFrom ==== " + self.startFrom +"  endTo ==== " +self.endDateToday )
                response_cost = self.getResponse(costAPIurl, 'POST', None, None, data=requestBody, reqHeaders=self.finopsUtilities.headersWithAuth)
                responsepropeties = response_cost['properties']
                columnsList = responsepropeties.get('columns')
                
                rowList = responsepropeties.get('rows')
                usageDate = self.processCostRecords( costData, columnsList, rowList)
                nextPageLink = responsepropeties.get('nextLink', None)
                
                if len(rowList) == 0 or nextPageLink == None or nextPageLink == '':
                    nextPageFetch = False
                else:
                    self.baseLogger.info(" nextPageLink " + nextPageLink + "  rowcount " + str(len(rowList)))
                    costAPIurl = nextPageLink

                if len(costData) > 0 :
                    self.publishToolsData(costData, costMetadata, costReqtimestamp, costReqtimeformat, costReqisEpoch)
                
            except Exception as ex:
                self.baseLogger.error(ex)
                if str(ex).__contains__("RateLimitingFilter"):
                    time.sleep(10)
                elif str(ex).__contains__("Too many requests. Please retry."):
                    time.sleep(10)
                else:
                    self.baseLogger.error("Existing while loop due to error ")
                    nextPageFetch = False
                    raise Exception(ex)
        
        self.addResourceDetails(subscription)
        
        if usageDate != None:
            self.tracking["lastDataCollectionDate"] =  (parser.parse(usageDate, ignoretz=True)).strftime("%Y-%m-%dT%H:%M:%S")
        
        self.updateTrackingJson(self.tracking)
    
    
    def processCostRecords(self, costData, columnsList, rowList):
        usageDate = None
        for row in rowList:
            costRowDetailDict = {}
            costRowDetailDict['cloudtype'] = 'azure'
            for i in range(0, len(columnsList)):
                columnName = str(columnsList[i].get("name")).lower()
                if columnName== 'currency':
                    costRowDetailDict['currency'] = 'USD'
                elif columnName == 'costusd':
                    costRowDetailDict['cost'] = row[i]
                elif columnName == 'resourcetype':
                    rtype = str(row[i])
                    if rtype not in self.uniqueResouseTypeList:
                        self.uniqueResouseTypeList.append(rtype)
                elif columnName == 'resourceid':
                    resourceIdl = str(row[i]).lower()
                    costRowDetailDict[columnName] = resourceIdl
                    if resourceIdl not in self.uniqueResouseIdList and resourceIdl != '':
                        self.uniqueResouseIdList.append(str(resourceIdl))
                elif columnName == "usagedate":
                    lastUpdated_In_format = datetime.datetime.strptime(str(row[i]), "%Y%m%d")
                    usageDate = lastUpdated_In_format.strftime("%Y-%m-%dT%H:%M:%S")
                    #costRowDetailDict["monthAbr"] = self.getMonthAbrFromNumber(lastUpdated_In_format.month)
                    costRowDetailDict["year"] = lastUpdated_In_format.year
                    costRowDetailDict["month"] = lastUpdated_In_format.month
                    costRowDetailDict["day"] = lastUpdated_In_format.day
                    costRowDetailDict[columnName] = lastUpdated_In_format.strftime("%Y-%m-%d")
                elif columnName == "billingperiod":
                    costRowDetailDict[columnName] = str(row[i])
                else:
                    costRowDetailDict[columnName] = str(row[i])

            costData.append(costRowDetailDict)
        
        return usageDate   


    def prepareResourceTagData(self, tagDetailsList, resource, resourceId):
        if resource.get("tags") != None and len(resource["tags"]) > 0:
            for key in resource["tags"]:
                tagDetailDict = {}
                tagDetailDict["resourceid"] = resourceId
                tagDetailDict["tagkey"] = key
                tagDetailDict["tagvalue"] = resource["tags"][key]
                tagDetailDict['cloudtype'] = 'azure'
                tagDetailsList.append(tagDetailDict)
        
        else:
            tagDetailDict = {}
            tagDetailDict["resourceid"] = resourceId
            tagDetailDict["tagkey"] = "NoTag"
            tagDetailDict["tagvalue"] = "NoValue"
            tagDetailDict['cloudtype'] = 'azure'
            tagDetailsList.append(tagDetailDict)
   
    def fetchStartFromDate(self, nextPageFetch):
        noNeedtoUpdate = False
        nextPageFetch = True 
        if self.startDateFromTracking == None:
            self.startFrom = self.startFromConfig
        else:
            self.startFrom = (parser.parse(self.startDateFromTracking, ignoretz=True) + datetime.timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S")
        if self.startFrom >= self.endDateToday:
            noNeedtoUpdate = True
            nextPageFetch = False
            self.baseLogger.info("latest data already collected  =========== start from   " + str(self.startFrom) + "  ======= end to   " + str(self.endDateToday))
        return nextPageFetch


    def addResourceDetails(self, subscription):
        
        tagDetailsList = []
        resourceDetailsList = []
        resourceMetrics = []
        resourceDetailsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("resourceDetailsMetadata", "{}")
        resourceMetadataIndividual = self.dynamicTemplate.get("resourceTagsIndividual", "{}").get("resourceMetadata", "{}")
        costRelationshipData = self.dynamicTemplate.get("resourceDetails", "{}").get("relationCostMetadata", "{}")
        relationTagsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("relationTagsMetadata", "{}")
        resourceMetricsMetadata = self.dynamicTemplate.get("resourceMetricsdDetails", "{}").get("resourceMetricsMetadata", "{}")
        relationMetricsMetadata = self.dynamicTemplate.get("resourceMetricsdDetails", "{}").get("resourceMetricsRelationMetadata", "{}")
        self.finopsUtilities.prepareRequestHeader(self.contentTypeJson)
        resourceTypeProviderUrl = ""
        
        if self.uniqueResouseTypeList: 
            for resourceType in self.uniqueResouseTypeList:
                if resourceType != "":
                    try:
                        
                        apiVersion = self.finopsUtilities.getApiVersion("2021-04-01", resourceType.lower())

                        resourceTypeProviderUrl = self.apiURL + "/subscriptions/" + subscription['subscriptionId'] + "/providers/" + resourceType + "?api-version=" + apiVersion
                        
                        response_type_rg = self.getResponse(resourceTypeProviderUrl, 'GET', None, None, data=None, reqHeaders=self.finopsUtilities.headersWithAuth)
                        
                        for resource in response_type_rg["value"]:
                            resourceId = str(resource["id"]).lower()
                            resourceIdArr = resourceId.split("/")
                            resourceGrpName = "-"
                            if "resourcegroups" in resourceIdArr:
                                resourceGrpName = resourceIdArr[4]                               

                            resourceDetailDict = {}
                            resourceDetailDict["resourceid"] = resourceId
                            resourceDetailDict["subscriptionname"] = subscription['displayName']
                            resourceDetailDict["subscriptionid"] = subscription['subscriptionId']
                            resourceDetailDict["resourcetype"] = str(resource["type"])
                            resourceDetailDict["resourcename"] = str(resource["name"])
                            resourceDetailDict["resourcegroups"] = str(resourceGrpName).lower()
                            resourceDetailDict["creationtime"] = str(resource.get('properties').get("creationTime"))
                            resourceDetailDict["location"] = str(resource["location"])
                            resourceDetailDict['cloudtype'] = 'azure'
                            
                            resourceDetailDict = self.finopsUtilities.addAdditionalDetailsForResource(resource, resourceDetailDict)
                            self.finopsUtilities.addResourceMetric(resourceId, str(resource["type"]), self.startFrom, resourceMetrics)
                            resourceDetailsList.append(resourceDetailDict)
                            if resourceId not in self.uniqueResouseIdList:
                                self.baseLogger.error(" Resource Not found ======================  " + resourceId)
                            
                    
                            self.prepareResourceTagData(tagDetailsList, resource, resourceId)                                   
                    
                    except Exception as ex:
                        self.baseLogger.error("resourceTypeProviderUrl ====================== " + resourceTypeProviderUrl)
                        self.baseLogger.error(ex)
            
            #self.baseLogger.info(resourceDetailsList)                                    
            self.publishToolsData(resourceDetailsList, resourceDetailsMetadata) 
            self.publishToolsData(tagDetailsList, resourceMetadataIndividual)                 
            self.publishToolsData(resourceDetailsList, costRelationshipData)            
            self.publishToolsData(tagDetailsList, relationTagsMetadata)
            self.publishToolsData(resourceMetrics, resourceMetricsMetadata)
            self.publishToolsData(resourceMetrics, relationMetricsMetadata) 
            
if __name__ == "__main__":
    AzureFinOpsAgent()
