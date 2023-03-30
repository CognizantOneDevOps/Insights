# ------------------------------------------------------------------------------- 
# Copyright 2021 Cognizant Technology Solutions
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
# -------------------------------------------------------------------------------

import os
import sys
import logging.handlers
import json
from datetime import datetime
from datetime import date
from dateutil import parser
import datetime
import inspect
from forex_python.converter import CurrencyRates
from locale import currency

class FinOpsUtilities():
    
    def __init__(self, agentBaseDir, parentclass):
        
        self.parentclass = parentclass
        self.parentclass.baseLogger.info('Inside init of FinOpsUtilities =======')
        self.dynamicTemplate = self.parentclass.config.get('dynamicTemplate', '{}')
        self.apiVersionsTrackingDetail = {}
        self.currencyConversionTrackingDetail = {}
        self.headersWithAuth = {'Authorization':'auth1bc'}
        self.apiVersionFileName = "tracking_API_Versions"
        self.currencyConversionFileName = "tracking_Currency_Conversion"
        self.contentTypeJson = 'application/json'
        self.apiURL = self.parentclass.getCredential("apiURL")
        self.agentBaseDir = agentBaseDir
        self.baseURL = self.parentclass.getCredential("baseURL")
        self.subscription_Id = self.parentclass.getCredential("azureSubscriptionId")
        self.tenant_Id = self.parentclass.getCredential("azureTenantId")
        self.client_Id = self.parentclass.getCredential("azureClientId")
        self.secret = self.parentclass.getCredential("azureSecretkey")
        self.endDateToday = date.today().strftime("%Y-%m-%dT%H:%M:%S")
       
        
        self.access_token = self.generateAuthToken()
        self.loadAPIVersionTacking()
        self.currencyRates = CurrencyRates()
        
       
        
    def generateAuthToken(self):
        
        authtokenurl = self.baseURL + '/' + self.tenant_Id + '/oauth2/token'
        
        token_data = {
            'grant_type': 'client_credentials',
            'client_id': self.client_Id,
            'client_secret': self.secret,
            'resource':self.apiURL,
        }
        
        headers = {'Content-Type': 'application/x-www-form-urlencoded'}
        response = self.parentclass.getResponse(authtokenurl, 'POST', None, None, data=token_data, reqHeaders=headers)
        return response['access_token']
    
    def loadAPIVersionTacking(self):
        if not self.checkAPITrackingFiles(self.apiVersionFileName) :
            self.updateTrackingCache(self.apiVersionFileName, dict())  # creates API version tracking json file
            self.fetchResourceTypeWiseAPIVersions()
        else:
            self.apiVersionsTrackingDetail = self.trackingCacheFileLoad(self.apiVersionFileName)
    
    def loadCurrencyTacking(self, baseCurrency):
        if not self.checkAPITrackingFiles(self.currencyConversionFileName) :
            self.updateTrackingCache(self.currencyConversionFileName, dict())  # creates currency tracking json file
            
        self.currencyConversionTrackingDetail = self.trackingCacheFileLoad(self.currencyConversionFileName)
        self.captureCurrencyConversion(baseCurrency)
        
            
    def fetchResourceTypeWiseAPIVersions(self):
        
        try:
            resourceWiseAPIVersions = {}
            self.prepareRequestHeader(self.contentTypeJson)
            
            resourceProviderUrl = self.apiURL + "/subscriptions/" + self.subscription_Id + "/providers?api-version=2021-04-01"
            response_api = self.parentclass.getResponse(resourceProviderUrl, 'GET', None, None, data=None, reqHeaders=self.headersWithAuth)
            values = response_api["value"]
            self.parentclass.baseLogger.info(str(len(values)))
            for resource in values:
                self.parentclass.baseLogger.info(resource)
                for rtype in resource["resourceTypes"]:
                    resourceTypeVerDet = {}
                    resourceTypeVerDet["namespace"] = resource["namespace"].lower()
                    resourceTypeVerDet["resourcetype"] = rtype["resourceType"].lower()
                    resourceTypeVerDet["resourcetypefullpath"] = resourceTypeVerDet["namespace"] + "/" + resourceTypeVerDet["resourcetype"]
                    if len(rtype["apiVersions"]) > 0:
                        resourceTypeVerDet["apiversion"] = rtype["apiVersions"][0]
                    else:
                        self.parentclass.baseLogger.error("No API version for " + str(resource))
                    resourceWiseAPIVersions[resourceTypeVerDet["resourcetypefullpath"]] = resourceTypeVerDet
        except Exception as ex:
            self.parentclass.baseLogger.error(ex)  
            
        self.updateTrackingCache(self.apiVersionFileName, resourceWiseAPIVersions)  
        self.apiVersionsTrackingDetail = self.trackingCacheFileLoad(self.apiVersionFileName)
        
    def prepareRequestHeader(self, contentType):
        self.headersWithAuth['Authorization'] = 'Bearer ' + self.access_token
        if contentType != None:
            self.headersWithAuth['Content-Type'] = contentType
            
    def getApiVersion(self, defaultVersion, resourceType):
        apiPropertiesDict = self.apiVersionsTrackingDetail.get(resourceType, None)
        if apiPropertiesDict == None:
            metricapiVersion = defaultVersion
        else:
            metricapiVersion = apiPropertiesDict["apiversion"]
        
        return metricapiVersion
    
    def updateTrackingCache(self, fileName, trackingDict):
        self.parentclass.baseLogger.info('Inside updateTrackingCache')
        with open(self.agentBaseDir + fileName + '.json', 'w') as filePointer:
            json.dump(trackingDict, filePointer, indent=4) 
            
    def checkAPITrackingFiles(self, fileName):
        self.parentclass.baseLogger.info('Inside checkAPITrackingFiles')
        return os.path.isfile(self.agentBaseDir + fileName+ '.json')
    
    def trackingCacheFileLoad(self, fileName):
        self.parentclass.baseLogger.info('Inside trackingCacheFileLoad')
        with open(self.agentBaseDir + fileName + '.json', 'r') as filePointer:
            data = json.load(filePointer)
        return data 
    
    def addAdditionalDetailsForResource(self, resource, resourceDetailDict):
        additionlDetails = []
        resourceTypeDet = str(resource["type"])
        returnDict = resourceDetailDict.copy()
        additionalResourceResponseTemplate = self.dynamicTemplate.get("additionalResourceDetails", "{}").get(resourceTypeDet,None)
        if additionalResourceResponseTemplate != None:
            additionlDetails = self.parentclass.parseResponse(additionalResourceResponseTemplate.get("ObjectProperties",{}), resource, returnDict)
            returnDict = additionlDetails[0]
        return returnDict
    
    def addResourceMetric(self, resourceId, resourceTypeDet, startFrom, resourceMetrics):
        additionalResourceResponseTemplate = self.dynamicTemplate.get("additionalResourceDetails", "{}").get(resourceTypeDet,None)
        if additionalResourceResponseTemplate != None:              
            self.publishResourceMetricsData(resourceId,additionalResourceResponseTemplate.get("availablemetrics",{}), startFrom, resourceMetrics)
        
        return resourceMetrics;
        
    def publishResourceMetricsData(self, resourceId, metrics, startFrom, resourceMetrics):
        metricnames = ",".join(metrics) 
        metricapiVersion = self.getApiVersion("2021-05-01", "microsoft.insights/metrics")

        startFromBegin = datetime.datetime.strptime(startFrom, "%Y-%m-%dT%H:%M:%S")
        startFromBegin = startFromBegin.replace(hour=0, minute=0, second=0)
        startFromBeginStr = startFromBegin.strftime("%Y-%m-%dT%H:%M:%S")
        endDate = datetime.datetime.strptime(self.endDateToday, "%Y-%m-%dT%H:%M:%S")
        endDate = endDate.replace(hour=0, minute=0, second=0)
        endDateStr = endDate.strftime("%Y-%m-%dT%H:%M:%S")
        azuremetricapi= self.apiURL + resourceId +"/providers/Microsoft.Insights/metrics?api-version="+metricapiVersion+"&metricnames="+metricnames+"&timespan="+startFromBeginStr+"/"+endDateStr+"&interval=P1D"
        responsemetric = self.parentclass.getResponse(azuremetricapi, 'GET', None, None, data=None, reqHeaders=self.headersWithAuth)
        self.parentclass.baseLogger.info(azuremetricapi)
        responseLst = responsemetric.get("value")
               
        for valueObj in responseLst:
            if len(valueObj["timeseries"]) > 0 :
                timeseriesLst = valueObj["timeseries"][0]["data"]
                for timeseriesObj in timeseriesLst:
                    metricobj ={}
                    metricobj["name"] = valueObj["name"]["value"]
                    metricobj["unit"] = valueObj["unit"]
                    metricobj["timeStamp"] = timeseriesObj["timeStamp"]                
                    metricobj["value"] = timeseriesObj.get(metrics.get(metricobj["name"],""),None)
                    metricobj["id"] = valueObj["id"]                
                    metricobj["resourceid"] = str(resourceId).lower()
                    resourceMetrics.append(metricobj)
            else:
                self.parentclass.baseLogger.info(" No Metrics data available for date url "+str(azuremetricapi))
                
        return resourceMetrics

    
    def processForecastRecords(self,subscription, resourceGroupList):
        self.prepareRequestHeader('application/json')
        forcastrequestBody = str(self.dynamicTemplate.get("forecast", "{}").get("forecastrequest", "{}"))
        forecastMetadata = self.dynamicTemplate.get("forecast", "{}").get("forecastMetadata", "{}")
        forecastReqinsighstTimeX = self.dynamicTemplate.get('forecast', {}).get('insightsTimeXFieldMapping', None)
        forecastReqtimestamp = forecastReqinsighstTimeX.get('timefield', None)
        forecastReqtimeformat = forecastReqinsighstTimeX.get('timeformat', None)
        forecastReqisEpoch = forecastReqinsighstTimeX.get('isEpoch', False)
        forcastDataCollectionInDays = str(self.dynamicTemplate.get("forecast", "{}").get("forcastDataCollectionInDays", "{}"))
        pagesize = 500
        forecastDate  = None
        
        for resourceGroup in resourceGroupList: 
            
            apiVersion = self.getApiVersion("2021-10-01", "microsoft.costmanagement/forecast")
            
            nextPageFeatch = True
            forecastData = [] 
            forecastAPIurl = self.apiURL + "/subscriptions/" + subscription['subscriptionId']+ "/resourceGroups/" + resourceGroup["name"]  +"/providers/Microsoft.CostManagement/forecast?api-version="+apiVersion+"&$top="+str(pagesize)
            
            while nextPageFeatch:
                try:
                    
                    forcastrequestBody = forcastrequestBody.replace("startFrom", self.endDateToday)
                    forecastEndDate =  (parser.parse(self.endDateToday, ignoretz=True) + datetime.timedelta(days=int(forcastDataCollectionInDays))).strftime("%Y-%m-%dT%H:%M:%S") 
                    forcastrequestBody = forcastrequestBody.replace("endTo",forecastEndDate)
                    self.parentclass.baseLogger.info("forcastAPIurl =========== " + forecastAPIurl) 
                     
                    response_forecast = self.parentclass.getResponse(forecastAPIurl, 'POST', None, None, data=forcastrequestBody, reqHeaders=self.headersWithAuth)
                    
                    responsepropeties = response_forecast['properties']
                    columnsList = responsepropeties.get('columns')
                    rowList = responsepropeties.get('rows')
                    forecastDate = self.processForecastData(forecastData, columnsList, rowList,subscription,resourceGroup["name"])
                    
                    nextPageLink = responsepropeties.get('nextLink', None)
                    
                    if len(rowList) == 0 or nextPageLink == None or nextPageLink == '':
                        nextPageFeatch = False
                    else:
                        self.parentclass.baseLogger.info(" nextPageLink " + nextPageLink + "  rowcount " + str(len(rowList)))
                        costAPIurl = nextPageLink
                        
                except Exception as ex:
                    self.parentclass.baseLogger.error(ex)
                    if str(ex).__contains__("RateLimitingFilter"):
                        time.sleep(10)
                    else:
                        self.parentclass.baseLogger.error("ForeCast : Existing while loop due to error ")
                        nextPageFeatch = False
                        
            if len(forecastData) > 0 :
                self.parentclass.publishToolsData(forecastData, forecastMetadata, forecastReqtimestamp, forecastReqtimeformat, forecastReqisEpoch)
                
        if forecastDate == None:
            self.parentclass.baseLogger.info("forecastDate is empty skipping to update in tracking ")
        else:
            self.parentclass.tracking["lastForecastDataCollectionDate"] = (parser.parse(forecastDate, ignoretz=True)).strftime("%Y-%m-%dT%H:%M:%S")
        
        #self.parentclass.updateTrackingJson(self.parentclass.tracking)
        
    def processForecastData(self, forecastData, columnsList, rowList, subscriptions, resourcegrp):
        forecastDate = None
        for row in rowList:
            forcastRowDetailDict = {}
            # print(row)
            forcastRowDetailDict["subscriptionname"] = subscriptions['displayName']
            forcastRowDetailDict["subscriptionid"] = subscriptions['subscriptionId']
            forcastRowDetailDict["resourcegroup"] = str(resourcegrp).lower()
            forcastRowDetailDict['cloudtype'] = 'azure'
            for i in range(0, len(columnsList)):
                columnName = str(columnsList[i].get("name")).lower()
                if columnName== 'currency':
                    forcastRowDetailDict['currency'] = row[i]
                elif columnName == 'costusd':
                    forcastRowDetailDict['cost'] = row[i]                
                elif columnName == "usagedate":
                    lastUpdated_In_format = datetime.datetime.strptime(str(row[i]), "%Y%m%d")
                    forecastDate = lastUpdated_In_format.strftime("%Y-%m-%dT%H:%M:%S")
                    #forcastRowDetailDict["monthAbr"] = self.getMonthAbrFromNumber(lastUpdated_In_format.month)
                    forcastRowDetailDict["year"] = lastUpdated_In_format.year
                    forcastRowDetailDict["month"] = lastUpdated_In_format.month
                    forcastRowDetailDict["day"] = lastUpdated_In_format.day
                    forcastRowDetailDict[columnName] = lastUpdated_In_format.strftime("%Y-%m-%d")              
                else:
                    forcastRowDetailDict[columnName] = str(row[i])
            forecastData.append(forcastRowDetailDict)        
        return forecastDate 
    
    
    def processBudgetData(self,subscription):
        budgetList = []
        self.prepareRequestHeader('application/json')
        budgetMetadata = self.dynamicTemplate.get("budget", "{}").get("budgetMetadata", "{}")
        budgetApiVersionDict = self.apiVersionsTrackingDetail.get("microsoft.consumption/budgets", None)
        if budgetApiVersionDict == None:
            budgetApiVersionDict = "2021-10-01"
        else:
            budgetApiVersionDict = budgetApiVersionDict["apiversion"]
        azurebudgetapi = self.apiURL + "/subscriptions/"+ subscription['subscriptionId'] +"/providers/Microsoft.Consumption/budgets?api-version="+budgetApiVersionDict
        responsemetric = self.parentclass.getResponse(azurebudgetapi, 'GET', None, None, data=None, reqHeaders=self.headersWithAuth)
        #self.parentclass.baseLogger.info(responsemetric)
        responseLst = responsemetric.get("value")
        for valueObj in responseLst:
            budgetObj = {}
            budgetObj["id"] = valueObj["id"]
            budgetObj["name"] = valueObj["name"]
            budgetObjProps = valueObj["properties"]
            budgetObj["startDate"] = budgetObjProps["timePeriod"]["startDate"]
            budgetObj["endDate"] = budgetObjProps["timePeriod"]["endDate"]
            budgetObj["amount"] = budgetObjProps["amount"]
            budgetObj["unit"] = budgetObjProps["currentSpend"]["unit"]
            budgetObj["currentamount"] = budgetObjProps["currentSpend"]["amount"]
            budgetObj["category"] = budgetObjProps["category"]
            budgetObj['cloudtype'] = 'azurecsv'
            startFromDate = (parser.parse(budgetObj["startDate"], ignoretz=True)).strftime("%Y-%m-%d")
            budgetObj["amountUSD"]  = self.convertCostINUSD(budgetObjProps["amount"], startFromDate)
            budgetList.append(budgetObj)
        self.parentclass.publishToolsData(budgetList, budgetMetadata) 
        
        
    def captureCurrencyConversion(self, base_cur):
        try:
            enddate = date.today().strftime("%Y-%m-%d")
            if "lastCurrencyDataCollectionDate" in self.parentclass.tracking:
                startFromDate = self.parentclass.tracking.get("lastCurrencyDataCollectionDate")
            else:
                startFromDate = (parser.parse(self.parentclass.startFromConfig, ignoretz=True)).strftime("%Y-%m-%d")
            currencyConverterapi = 'https://api.exchangerate.host/timeseries?base={0}&start_date={1}&end_date={2}&symbols={3}'.format(base_cur, startFromDate , enddate, 'USD')
            currencyConverteresponse = self.parentclass.getResponse(currencyConverterapi, 'GET', None, None, data=None)
            for key, value in currencyConverteresponse["rates"].items():
                if not self.currencyConversionTrackingDetail.get(key):
                    self.currencyConversionTrackingDetail[key] = value.get("USD")
            self.updateTrackingCache(self.currencyConversionFileName, self.currencyConversionTrackingDetail)
            self.parentclass.tracking["lastCurrencyDataCollectionDate"] = enddate
            self.parentclass.updateTrackingJson(self.parentclass.tracking)
        except Exception as ex:
            self.parentclass.baseLogger.error(ex)
           
    
    
    def convertCostINUSD(self,cost, date): 
        usdCost = cost
        try:
            if date in self.currencyConversionTrackingDetail and cost > 0 :
                usdCost = cost * self.currencyConversionTrackingDetail.get(date)
        except Exception as ex:
            self.parentclass.baseLogger.error(ex)
        return usdCost      
            
        