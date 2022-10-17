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
import sys
from ....core.BaseAgent3 import BaseAgent
from ....core.FinOpsUtilities import FinOpsUtilities
from datetime import date
import datetime
import json
from _cffi_backend import typeof
import string
import time
import os
from dateutil import parser
import pandas as  pd
from azure.storage.blob import BlobServiceClient
from azure.storage.blob import ContainerClient
import io
from io import StringIO
import csv, re
import shutil



class AzureCSVParserFinOpsAgent(BaseAgent):

    @BaseAgent.timed
    def process(self):
        
        self.endDateToday = date.today().strftime("%Y-%m-%dT%H:%M:%S")
        self.dynamicTemplate = self.config.get('dynamicTemplate', '{}')
        self.startFromConfig = self.getCredential("startFrom")
        self.startDateFromTracking = self.tracking.get('lastModifiedCSVDate', None)
        self.contentTypeJson = 'application/json'
        self.collectForecastData = self.config.get('collectForecastData', False)
        self.storageAccountUrl = self.config.get('storageAccountUrl', "")
        self.storageAccountKey = self.config.get('storageAccountKey', "")
        self.containerName = self.config.get('containerName', "")
        self.storageConnectionString = self.config.get('storageConnectionString', "")
        self.unzipDirectoryName = "Finops_Azure_Report"
        self.uniqueResouseGroupList = [] 
        self.uniquesubscriptionList: List[str] = []
        self.unzipDirPath = os.path.join(os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep, self.unzipDirectoryName)

        
        try:
            self.blob_service_client_instance = BlobServiceClient(
                account_url=self.storageAccountUrl, credential=self.storageAccountKey)
            
            self.addAndRemoveUnzipDirectory()
            self.downloadCSVFiles()           
        
        except Exception as ex:
            self.baseLogger.error(ex)

    def downloadCSVFiles(self): 
        
        if self.startDateFromTracking == None:
            self.startFrom = (parser.parse(self.startFromConfig, ignoretz=True)).strftime("%Y-%m-%dT%H:%M:%S")
        else:
            self.startFrom = (parser.parse(self.startDateFromTracking, ignoretz=True)).strftime("%Y-%m-%dT%H:%M:%S")

        
        file_date_df_filter = self.getLastestModifiedManifestFile()
        
        for index, row in file_date_df_filter.iterrows():
            # self.baseLogger.info(index)
            #self.baseLogger.info(row['blob_name'] + " ========== " + str(row['last_modified']))
            blobname = row['blob_name']
            manifestFilePath = os.path.join(self.unzipDirPath, '_manifest.json')

            blob_client_instance = self.blob_service_client_instance.get_blob_client(self.containerName, blobname, snapshot=None) 
            #
    
            blob_data = blob_client_instance.download_blob()
            data = blob_data.readall()
            self.updateManifest(manifestFilePath, data)
            blobsDetails = self.loadManifest(manifestFilePath, data)
            # self.baseLogger.info(blobsDetails)  
            
            for blobobj in blobsDetails:
                lastDataCollectionDate = self.processCSVFiles(blobobj)
                if lastDataCollectionDate:
                    self.tracking["lastModifiedCSVDate"] = str(row['last_modified'])
                    self.tracking["lastDataCollectionDate"] = str(lastDataCollectionDate)
                    self.tracking["lastProcessBlobFileName"] = str(blobname)
        
        self.updateTrackingJson(self.tracking)

    def getLastestModifiedManifestFile(self): 
        container = ContainerClient.from_connection_string(conn_str=self.storageConnectionString, container_name=self.containerName)
    
        file_date_df = pd.DataFrame(columns=['blob_name', 'last_modified'])
        count = 0
        
        listblobs = container.list_blobs()
        #self.baseLogger.info("Size of list blob "+str(len(list(listblobs))))
        
        for blob in listblobs:
        #for blob in container.list_blobs():
            blobnamedict = blob.get("name")
            if "_manifest.json" in blobnamedict:
                #self.baseLogger.info(blob)
                file_date_df.loc[count] = [f'{blob.name}', f'{blob.last_modified}']
                count = count + 1
                
        for index, row in file_date_df.iterrows():
           self.baseLogger.info(row) 
                
        file_date_df['last_modified'] = pd.to_datetime(file_date_df['last_modified'], format='%Y-%m-%dT%H:%M:%S')
        
        blobRecordsSortWithDate = file_date_df.sort_values(by="last_modified")
        
        file_date_df_filter = blobRecordsSortWithDate.loc[blobRecordsSortWithDate['last_modified'] > self.startFrom,
                                                           ['blob_name', 'last_modified'] ]
        return file_date_df_filter
    
    def loadManifest(self, manifestFilePath, data): 
        with open(manifestFilePath, 'r') as filePointer:
            response = json.load(filePointer)
        return response["blobs"]
    
    def updateManifest(self, manifestFilePath, data):
        try:
            self.baseLogger.info('Inside updateManifest ' + manifestFilePath)
            fix_bytes_value = data.replace(b"'", b'"').replace(b"\r", b'').replace(b"\n", b'')
            bytes_uncoded = fix_bytes_value.decode('unicode_escape')
            res = json.loads(bytes_uncoded)
            with open(manifestFilePath, 'w') as filePointer:
                json.dump(res, filePointer, indent=4)
            
        except Exception as ex:
            self.baseLogger.error(ex)

    def processCSVFiles(self, blobobj):
        
        costMetadata = self.dynamicTemplate.get("costManagement", "{}").get("costMetadata", "{}")
        costReqinsighstTimeX = self.dynamicTemplate.get('costManagement', {}).get('insightsTimeXFieldMapping', None)
        costReqtimestamp = costReqinsighstTimeX.get('timefield', None)
        costReqtimeformat = costReqinsighstTimeX.get('timeformat', None)
        costReqisEpoch = costReqinsighstTimeX.get('isEpoch', False)
        resourceDetailsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("resourceDetailsMetadata", "{}")
        resourceMetadataIndividual = self.dynamicTemplate.get("resourceTagsIndividual", "{}").get("resourceMetadata", "{}")
        costRelationshipData = self.dynamicTemplate.get("resourceDetails", "{}").get("relationCostMetadata", "{}")
        relationTagsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("relationTagsMetadata", "{}")
        
        agentDir = os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep

        blobname = blobobj.get('blobName') 
        
        localpath = os.path.join(agentDir, self.unzipDirectoryName, blobname[blobname.rindex("/") + 1:])
        
        blob_client_instance = self.blob_service_client_instance.get_blob_client(
            self.containerName, blobname, snapshot=None)
        blob_data = blob_client_instance.download_blob()
         
        with open(localpath, "wb") as my_blob:
           blob_data.readinto(my_blob)
       
        costData = []
        resourceDetailsList = []
        tagDetailsList = []
        lastDataCollectionDate = None
       
        csvData = pd.read_csv(localpath)
        
        csvRecordFilterWithDate = csvData.sort_values(by="Date")

        for index, row in csvRecordFilterWithDate.iterrows():
            # self.baseLogger.info(str(index)+"  "+row['Date'])
            lastDataCollectionDate = self.processCostRecords(costData, row)
            
            if not pd.isna(row["ResourceId"]):
                self.resourceDetailsDict(row, resourceDetailsList)
                self.resourceTagsDetails(row, tagDetailsList)
            
        resourceDetailList_unique = pd.DataFrame(resourceDetailsList).drop_duplicates().to_dict('records')
        tagDetailsList_unique = pd.DataFrame(tagDetailsList).drop_duplicates().to_dict('records')
        
        self.baseLogger.info("Cost management length = " + str(len(costData)))
        self.baseLogger.info("Resource details length = " + str(len(resourceDetailList_unique)))
        self.baseLogger.info("Resource tags length = " + str(len(tagDetailsList_unique)))

             
        self.publishToolsData(costData, costMetadata, costReqtimestamp, costReqtimeformat, costReqisEpoch)
        self.publishToolsData(resourceDetailsList, resourceDetailsMetadata) 
        self.publishToolsData(tagDetailsList, resourceMetadataIndividual)   
                      
        self.publishToolsData(resourceDetailList_unique, costRelationshipData)            
        self.publishToolsData(tagDetailsList_unique, relationTagsMetadata)
    
        return lastDataCollectionDate
    
    def processCostRecords(self, costData, row):
        usageDate = None
        costRowDetailDict = {}

        if "/" in row['Date']:
            usageDate = datetime.datetime.strptime(row['Date'], '%m/%d/%Y')
        else:
            usageDate = datetime.datetime.strptime(row['Date'], '%m-%d-%Y')
        
        costRowDetailDict["year"] = usageDate.year
        costRowDetailDict["month"] = usageDate.month
        costRowDetailDict["day"] = usageDate.day
        costRowDetailDict['usagedate'] = usageDate.strftime("%Y-%m-%d")    
        costRowDetailDict['dataSource'] = 'csv'
        costRowDetailDict['cloudtype'] = 'azurecsv'
        costRowDetailDict['currency'] = row["BillingCurrencyCode"]
        costRowDetailDict['cost'] = row["CostInBillingCurrency"]
        costRowDetailDict["resourceid"] = row["ResourceId"]
        costRowDetailDict["servicename"] = row["MeterCategory"]
        costRowDetailDict["metercategory"] = row["MeterCategory"]
        costRowDetailDict["metersubcategory"] = row["MeterSubCategory"]
        costRowDetailDict["meter"] = row["MeterName"]

        costData.append(costRowDetailDict)
        
        return usageDate 
    
    # Resource details data collection
    def resourceDetailsDict(self, row, resourceDetailList):
        resourceDetailsDict = {}
        resourceId = row["ResourceId"]
        if not pd.isna(resourceId) and "/providers/" in resourceId:
            resourcetype = resourceId[resourceId.index("/providers/")+11:resourceId.rindex("/")]
        else:
            resourcetype = row["ConsumedService"]
            
        resourceGroup = str(row["ResourceGroup"]).lower()
        
        resourceDetailsDict["resourceid"] = resourceId
        resourceDetailsDict["subscriptionname"] = row['SubscriptionName']
        resourceDetailsDict["subscriptionid"] = row['SubscriptionId']
        resourceDetailsDict["resourcetype"] = resourcetype
        resourceDetailsDict["resourcename"] = row["ResourceName"]
        resourceDetailsDict["resourcegroups"] = resourceGroup
        resourceDetailsDict["creationtime"] = ""
        resourceDetailsDict["location"] = row["ResourceLocation"]
        resourceDetailsDict["pricingmodel"] = row["PricingModel"]
        resourceDetailsDict['cloudtype'] = 'azurecsv'    
        
        if row['SubscriptionId'] not in self.uniquesubscriptionList and not pd.isna(row['SubscriptionId']):
            self.uniquesubscriptionList.append(row['SubscriptionId']) 
            
        if resourceGroup not in self.uniquesubscriptionList and not pd.isna(resourceGroup):
            self.uniqueResouseGroupList.append(resourceGroup) 
        
        if not pd.isna(row["AdditionalInfo"]):
             res = json.loads(row["AdditionalInfo"])
             for key, value in res.items():
                resourceDetailsDict[key.lower()] = value
        resourceDetailList.append(resourceDetailsDict)
    
        # Resource tags data collection
    def resourceTagsDetails(self, row, tagDetailsList):
        if not pd.isna(row["Tags"]):
            resourceTags = row["Tags"]
            resourceTagArr = resourceTags.split(",")
            if len(resourceTagArr) > 0:
                for resourceTag in resourceTagArr:
                    tag = resourceTag.split(":")
                    tagDetailDict = {}
                    tagDetailDict["resourceid"] = row["ResourceId"]
                    tagDetailDict["tagkey"] = tag[0]
                    tagDetailDict["tagvalue"] = tag[1]
                    tagDetailDict['cloudtype'] = 'azurecsv'
                    tagDetailsList.append(tagDetailDict)
        else:
            tagDetailDict = {}
            tagDetailDict["resourceid"] = row["ResourceId"]
            tagDetailDict["tagkey"] = "NoTag"
            tagDetailDict["tagvalue"] = "NoValue"
            tagDetailDict['cloudtype'] = 'azurecsv'
            tagDetailsList.append(tagDetailDict)
    
    
        
    def resourceTagsDetailsDict(self, row, resourceTags, resourceTagsSet, resourceid):
        nonNullResourceTags = row.loc[resourceTags].dropna()
        resourceTagsDict = {}
        if len(nonNullResourceTags) > 0:
            for index, value in nonNullResourceTags.items():
                resourceTagsDict = {}
                resourceTagsDict["resourceid"] = resourceid
                resourceTagsDict["tagkey"] = index.split(":")[1]
                resourceTagsDict["tagvalue"] = value
                resourceTagsDict["cloudtype"] = "aws"
                resourceTagsSet.add(tuple(resourceTagsDict.items()))
        else:
            resourceTagsDict = {}
            resourceTagsDict["resourceid"] = resourceid
            resourceTagsDict["tagkey"] = "NoTag"
            resourceTagsDict["tagvalue"] = "NoValue"
            resourceTagsDict['cloudtype'] = 'aws'
            resourceTagsSet.add(tuple(resourceTagsDict.items()))
        return resourceTagsSet
    
    def addAndRemoveUnzipDirectory(self):
        if os.path.exists(self.unzipDirPath):
            try:
                shutil.rmtree(self.unzipDirPath)
            except Exception as ex:
                self.baseLogger.error(ex)
        os.mkdir(self.unzipDirPath)

            
if __name__ == "__main__":
    AzureCSVParserFinOpsAgent()
