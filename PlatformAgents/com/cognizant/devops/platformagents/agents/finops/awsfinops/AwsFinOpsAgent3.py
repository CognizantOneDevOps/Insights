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
Created on Jun 16, 2016
@author: 593714
'''

import sys
from _cffi_backend import typeof
from pip._internal.req.req_install import InstallRequirement
from mmap import ALLOCATIONGRANULARITY
from Tools.scripts.fixcid import rep
from pip._vendor.pyparsing import col
from pandas.core.series import Series
from dateutil.utils import today
from dateutil.relativedelta import relativedelta
import boto3
import time
import calendar
import os
from ....core.BaseAgent3 import BaseAgent
from datetime import datetime, timedelta, date
from dateutil import parser
import json
import csv
import pandas as  pd
import shutil
from zipfile import ZipFile
import math
import statistics
  
class AwsFinOpsAgent(BaseAgent):

    @BaseAgent.timed
    def process(self):
        self.baseLogger.info('Inside process')
        
        #Other data
        self.acskey = self.getCredential("awsAccesskey")
        self.scrtkey = self.getCredential("awsSecretkey")
        self.s3FilePath = self.config.get('s3FilePath')
        
        self.startFromConfig = (parser.parse(self.config.get("startFrom"), ignoretz=True)).date()

        self.trackingJsonMonthlyFolderdict = self.tracking.get('monthlyFolderDetails',{})
        self.metricDataColletionDate = self.tracking.get('metricDataColletionDate',None)
        
        
        self.unzipDirectoryNameValue = "Finops_Report"
        self.unzipDirectoryName = os.path.join(os.path.dirname(__file__) + os.path.sep, self.unzipDirectoryNameValue)
        self.csvBackupDirectoryNameValue = "Finops_Report_Backup"
        self.csvBackupDirectoryName = os.path.join(os.path.dirname(__file__) + os.path.sep, self.csvBackupDirectoryNameValue)
        
        splitedPath = self.s3FilePath[5:].split("/")
        self.s3BucketName = splitedPath[0]
        self.manifestFileName = splitedPath[len(splitedPath)-2]+"-Manifest.json"
        self.prefix = self.s3FilePath[(5+len(splitedPath[0])+1):]
        
        self.dynamicTemplate = self.config.get('dynamicTemplate', '{}')
        
        self.additionalMetrics = self.dynamicTemplate.get("additionalResourceDetails", {})
        
        self.costReqinsighstTimeX = self.dynamicTemplate.get('costManagement',{}).get('insightsTimeXFieldMapping',None)
        self.costReqtimestamp = self.costReqinsighstTimeX.get('timefield',None)
        self.costReqtimeformat = self.costReqinsighstTimeX.get('timeformat',None)
        self.costReqisEpoch = self.costReqinsighstTimeX.get('isEpoch',False)
        
        #Meta data for first three labels and last two relations
        self.costMetadata = self.dynamicTemplate.get("costManagement","{}").get("costMetadata","{}")
        self.resourceMetadata = self.dynamicTemplate.get("resourceTags","{}").get("resourceMetadata","{}")
        self.resourceDetailsMetadata = self.dynamicTemplate.get("resourceDetails","{}").get("resourceDetailsMetadata","{}")
        self.costRelationshipData = self.dynamicTemplate.get("resourceDetails", "{}").get("relationCostMetadata", "{}")
        self.relationTagsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("relationTagsMetadata", "{}")
        
        self.metricMetadata = self.dynamicTemplate.get("metrics","{}").get("metricsMetadata","{}") 
        self.relationMetricsMetadata = self.dynamicTemplate.get("metrics", "{}").get("resourceMetricsRelationMetadata", "{}")        

        self.allResourcesList = []
        
        try:
            self.s3ApiClient = boto3.client('s3', aws_access_key_id=self.acskey, aws_secret_access_key=self.scrtkey)
                
            self.processCsvFileFromS3Bucket()
            
            self.baseLogger.info(" Collecting Metrics data " )
            if self.dynamicTemplate.get("additionalResourceDetails",{}).get("isMetricsNeeded"):
                self.fetchResourcesMetrics()
            
            self.baseLogger.info(" Collecting Forecast data " + str(self.dynamicTemplate.get("forecast",{}).get("isForecastNeeded")) )
            if self.dynamicTemplate.get("forecast",{}).get("isForecastNeeded"):
                self.forecatedCostExplorer()
                
            self.updateTrackingJson(self.tracking)

        except Exception as ex:
            self.baseLogger.error(ex)
    
    #Extract data that are to be pushed to three label inside Neo4j graph db 

    
    def processCsvFileFromS3Bucket(self):
        
        self.getMonthFolderDetails()
        
        #for eachPeriod in periodsList:
        for eachMonthKey, eachMonthValue in self.trackingJsonMonthlyFolderdict.items():
            
            try:
                
                if len(eachMonthValue.get("invoiceid")) > 0 :
                    self.baseLogger.info("Skipping period as invoice is generated -> " + eachMonthKey)
                    continue
                
                #Check if the S3 bucket has latest update using head_object API call on our csv file
                self.manifestPath=self.prefix + os.path.join(eachMonthKey, self.manifestFileName).replace("\\","/")
                response = self.s3ApiClient.head_object(Bucket=self.s3BucketName, Key=self.manifestPath)
                last_modified = (parser.parse(response.get("ResponseMetadata").get("HTTPHeaders").get("last-modified"), ignoretz=True))
                
                if len(eachMonthValue.get("lastModified")) > 0 and last_modified <= parser.parse(eachMonthValue.get("lastModified"), ignoretz=True):
                    continue
                
                if not os.path.exists(self.unzipDirectoryName):
                    os.mkdir(self.unzipDirectoryName)
                else:
                    #Remove any existing folder before downloading the csv files from S3 bucket
                    shutil.rmtree(self.unzipDirectoryName, ignore_errors=True)
                    os.mkdir(self.unzipDirectoryName)
                    
                #Download Manifest file to get the reportKeys to get all the updated files one by one
                csvFileNameListFromManifest = self.loadManifestAndExtractCSVFilePath()
                
                #if not os.path.exists(self.csvBackupDirectoryName):
                #    os.mkdir(self.csvBackupDirectoryName)
                    
                for eachCSVFile in csvFileNameListFromManifest:
                    self.parseCSVFileAndPublishData(eachMonthKey, last_modified, eachCSVFile)

                self.tracking["lastDataCollectionDate"] = str(last_modified)
            except Exception as ex:
                 self.baseLogger.error("Exception in period loop ==== "+self.manifestPath)
                 self.baseLogger.error(ex)

        
        self.tracking["monthlyFolderDetails"] = self.trackingJsonMonthlyFolderdict       
        
        
    def getMonthFolderDetails(self):
        periodsList = []
        if not self.trackingJsonMonthlyFolderdict:
            s3RespFoldersList = self.s3ApiClient.list_objects(Bucket=self.s3BucketName, Prefix=self.prefix, Delimiter='/')
            periodsList = [d['Prefix'].rstrip('/')[d['Prefix'].rstrip('/').rindex("/") + 1:] for d in s3RespFoldersList.get('CommonPrefixes')]
            
            for eachPeriod in periodsList:
                monthstartdate = (parser.parse(eachPeriod.split("-")[0], ignoretz=True)).date()

                if monthstartdate >= self.startFromConfig.replace(day=1):
                    self.trackingJsonMonthlyFolderdict[eachPeriod] = {'lastModified':'', 'invoiceid':''}
        
        else:
            currentMonthStartDt = datetime.today().replace(day=1)
            currentMonthPeriod = currentMonthStartDt.strftime("%Y%m%d") + "-" + (currentMonthStartDt + relativedelta(months=1)).strftime("%Y%m%d")
            if currentMonthPeriod in self.trackingJsonMonthlyFolderdict:
                self.trackingJsonMonthlyFolderdict[currentMonthPeriod] = {'lastModified':'', 'invoiceid':''}


    def parseCSVFileAndPublishData(self, eachMonthKey, last_modified, eachCSVFile):
        
        costList = []
        resourceDetailsList = []
        tagDetailsList = []
        invoiceid = ""
        
        #CSV File Download from S3 Bucket to local directory path
        zipfileName = eachCSVFile[eachCSVFile.rindex("/") + 1:]
        fileName = zipfileName[:-4]
        self.s3ApiClient.download_file(self.s3BucketName, eachCSVFile, zipfileName)
        with ZipFile(zipfileName, 'r') as zip_ref:
            zip_ref.extractall(self.unzipDirectoryName)
            '''
                #Backup folder code 
                monthDirPath = os.path.join(self.csvBackupDirectoryName, eachMonthKey)
                if not os.path.exists(monthDirPath):
                    os.mkdir(monthDirPath)
                backupFile = os.path.join(self.csvBackupDirectoryName, eachMonthKey, zipfileName).replace("\\","/")[:-4]
                backupFileOld = os.path.join(self.csvBackupDirectoryName, eachMonthKey, "Old"+zipfileName).replace("\\","/")[:-4]
                if os.path.exists(backupFileOld):
                    os.remove(backupFileOld)
                    os.rename(backupFile, backupFileOld)
                zip_ref.extractall(os.path.join(self.csvBackupDirectoryName, eachMonthKey).replace("\\","/"))
            '''
        os.remove(zipfileName)
        
        #Read CSV File
        dataframeCUReport = pd.read_csv(self.unzipDirectoryName + "/" + fileName, low_memory=False)
        
        #Fetch all records from csv file
        dfOfCURSortedByDate = dataframeCUReport.sort_values(by="lineItem/UsageStartDate")
        
        #User defined resource tags column collection
        resourceTags = [col for col in dfOfCURSortedByDate.columns.values if col.startswith('resourceTags/user:')]
        dfOfCURSortedByDate.columns.values
        
        #Loop through all the records based on the index position of the rows one by one
        for ind, row in dfOfCURSortedByDate.iterrows():
            resourceId = "-"
            if not pd.isna(row["lineItem/ResourceId"]):
                resourceId = row["lineItem/ResourceId"]
            if invoiceid == "":
                if pd.isna(row["bill/InvoiceId"]):
                    invoiceid = ""
                else:
                    invoiceid = str(row["bill/InvoiceId"])
            costList.append(self.costManagementDetails(row, resourceId))
            resourceDetailsList.append(self.resourceDetailsDict(row, resourceId))
            self.resourceTagsDetailsDict(row, resourceTags, tagDetailsList, resourceId)
        
        self.trackingJsonMonthlyFolderdict[eachMonthKey]["invoiceid"] = invoiceid
        self.trackingJsonMonthlyFolderdict[eachMonthKey]["lastModified"] = str(last_modified)
        
        resourceDetailsListRmdDupl = pd.DataFrame(resourceDetailsList).drop_duplicates().to_dict('records')
        resourceTagsListRmdDupl = pd.DataFrame(tagDetailsList).drop_duplicates().to_dict('records')
        
        self.baseLogger.info("Cost management length = " + str(len(costList)))
        self.baseLogger.info("Resource tags length = " + str(len(resourceTagsListRmdDupl)))
        self.baseLogger.info("Resource details length = " + str(len(resourceDetailsListRmdDupl)))
        
        #Publish to Neo4j via RabbitMQ and InsightEngine
        self.publishToolsData(costList, self.costMetadata, self.costReqtimestamp, self.costReqtimeformat, self.costReqisEpoch)
        self.publishToolsData(resourceDetailsListRmdDupl, self.resourceDetailsMetadata)
        self.publishToolsData(resourceTagsListRmdDupl, self.resourceMetadata)
        self.publishToolsData(resourceDetailsListRmdDupl, self.costRelationshipData)
        self.publishToolsData(resourceTagsListRmdDupl, self.relationTagsMetadata)


    def loadManifestAndExtractCSVFilePath(self):
        self.mainfestFile = os.path.join(self.unzipDirectoryName, self.manifestFileName).replace("\\", "/")
        self.s3ApiClient.download_file(self.s3BucketName, self.manifestPath, self.mainfestFile)
        reourcesJsonFileOpened = open(self.unzipDirectoryName + "/" + self.manifestFileName)      
        response = json.load(reourcesJsonFileOpened)
        return response["reportKeys"]


    #Cost management details data collection
    def costManagementDetails(self, row, resourceid):
        costRowDetailDict = {}
        costRowDetailDict["resourceid"] = resourceid
        if "e" in str(row["lineItem/UnblendedCost"]):    
           costRowDetailDict["cost"] = row["lineItem/UnblendedCost"]
        else:
            costRowDetailDict["cost"] = row["lineItem/UnblendedCost"]
        costRowDetailDict["currency"] = row["pricing/currency"]
        if not pd.isna(row["product/ProductName"]):
            costRowDetailDict["servicename"] = row["product/ProductName"]
        else:
            costRowDetailDict["servicename"] = "-"
        costRowDetailDict["usagetype"] = row["lineItem/UsageType"]
        costRowDetailDict["accountid"] = str(row["lineItem/UsageAccountId"])
        costRowDetailDict["operation"] = row["lineItem/Operation"]
        costRowDetailDict["usagedatestart"] = row["lineItem/UsageStartDate"]
        costRowDetailDict["usagedate"] = row["lineItem/UsageStartDate"].split("T")[0]
        costRowDetailDict["lineitemtype"] = row["lineItem/LineItemType"]
        costRowDetailDict["lineitemid"] = row["identity/LineItemId"]
        costRowDetailDict["cloudtype"] = "aws"
        startDatetime = datetime.strptime(row["lineItem/UsageStartDate"].split("T")[0], "%Y-%m-%d")
        costRowDetailDict["year"] = startDatetime.year
        costRowDetailDict["month"] = startDatetime.month
        costRowDetailDict["day"] = startDatetime.day
        return costRowDetailDict
    
    #Resource tags data collection
    def resourceTagsDetailsDict(self, row, resourceTags, tagDetailsList, resourceid):
        nonNullResourceTags = row.loc[resourceTags].dropna()
        resourceTagsDict = {}
        if len(nonNullResourceTags) > 0 :
            for index, value in nonNullResourceTags.items():
                resourceTagsDict = {}
                resourceTagsDict["resourceid"] = resourceid
                resourceTagsDict["tagkey"] = index.split(":")[1]
                resourceTagsDict["tagvalue"] = value
                resourceTagsDict["cloudtype"] = "aws"
                tagDetailsList.append(resourceTagsDict)
        else :
            resourceTagsDict = {}
            resourceTagsDict["resourceid"] = resourceid
            resourceTagsDict["tagkey"] = "NoTag"
            resourceTagsDict["tagvalue"] = "NoValue"
            resourceTagsDict['cloudtype'] = 'aws'
            tagDetailsList.append(resourceTagsDict)
        #return resourceTagsSet
    
    #Resource details data collection
    def resourceDetailsDict(self, row, resourceid):
        resourceDetailsDict = {}
        resourceDetailsDict["resourceid"] = resourceid
        resourceDetailsDict["accountid"] = str(row["lineItem/UsageAccountId"])
        resourceDetailsDict["subscriptionname"] = ""
        resourceDetailsDict["servicename"] = row["product/ProductName"]
        resourceDetailsDict["resourcetype"] = row["pricing/term"]
        resourceDetailsDict["cloudtype"] = "aws"        
        if row["lineItem/ResourceId"] == row["lineItem/ResourceId"]:
            if row["lineItem/ResourceId"].__contains__(":"):
                lastIndex = row["lineItem/ResourceId"].rindex(":")
                resourceDetailsDict["resourcename"] = row["lineItem/ResourceId"][lastIndex+1:]
            else:
                resourceDetailsDict["resourcename"] = row["lineItem/ResourceId"]
        else:
            resourceDetailsDict["resourcename"] = "-"
        
        if not pd.isna(row["product/region"]):
            resourceDetailsDict["region"] = row["product/region"]
        else:
            resourceDetailsDict["region"] = "NA"
        if not pd.isna(row["pricing/term"]):
            resourceDetailsDict["resourcetype"] = row["pricing/term"]
        else:
            resourceDetailsDict["resourcetype"] = "NA"  
        resourceDetailsDict["instancetype"] = row["product/instanceType"]
        resourceDetailsDict["productgroup"] = row["product/group"]  
        
        if resourceid !='-' and not pd.isna(row['product/region']):
            subdict={subDict:resourceDetailsDict[subDict] for subDict in ['region','resourceid','resourcename','servicename']}
            self.allResourcesList.append(subdict) 
              
        return resourceDetailsDict
    
    def forecatedCostExplorer(self):
        
        forcastMetrics = self.dynamicTemplate.get("forecast",{}).get("forecastMetrics")
        forecastGranularity = self.dynamicTemplate.get("forecast",{}).get('forecastGranularity')
        forecastRegions = self.dynamicTemplate.get("forecast",{}).get('forecastRegions', [])
        
        costReqinsighstTimeX = self.dynamicTemplate.get('forecast',{}).get('insightsTimeXFieldMapping',None)
        costReqtimestamp = costReqinsighstTimeX.get('timefield',None)
        costReqtimeformat = costReqinsighstTimeX.get('timeformat',None)
        costReqisEpoch = costReqinsighstTimeX.get('isEpoch',False)
        client = boto3.client('ce', aws_access_key_id=self.acskey,
                                     aws_secret_access_key=self.scrtkey)
        start=(datetime.today() + timedelta(days=1)).strftime("%Y-%m-%d")
        frcstEndDt = (datetime.today() + relativedelta(months=1)).strftime("%Y-%m-%d")
        forecastRequest = self.dynamicTemplate.get("forecast","{}").get("forecastrequest","{}").get("Filter", "{}")

        if len(forecastRegions) == 0 :
            response = client.get_cost_forecast(
                TimePeriod={
                    'Start': start,
                    'End':  frcstEndDt
                },
                Granularity=forecastGranularity,
                Metric=forcastMetrics
                )
        else:
            
            regions = ""
            for reg in forecastRegions: 
                regions = regions + reg + "\",\""
                
            regions = regions.rstrip(",\"")
            requestFile = json.loads(json.dumps(forecastRequest).replace("allregions", regions))
            
            response = client.get_cost_forecast(
                TimePeriod={
                    'Start': start,
                    'End':  frcstEndDt
                },
                Granularity=forecastGranularity,
                Metric=forcastMetrics,
                Filter=requestFile
                )
        forecastMetadata = self.dynamicTemplate.get("forecast","{}").get("forecastMetadata","{}")
        forecastList = []
        forecastReponseList = response.get("ForecastResultsByTime", [])
        forecastDate = None
        for eachForecastData in forecastReponseList:
            forecastDict = {}
            forecastDict["totalcost"] = response.get("Total").get("Amount")
            forecastDict["currency"] = response.get("Total").get("Unit")
            forecastDict["usagedate"]  = eachForecastData.get("TimePeriod").get("Start")
            forecastDict["enddate"] = eachForecastData.get("TimePeriod").get("End")
            forecastDict["cost"] = eachForecastData.get("MeanValue")
            forecastDict["cloudtype"] = "aws"
            startDatetime = datetime.strptime(eachForecastData.get("TimePeriod").get("Start"), "%Y-%m-%d")
            forecastDate = startDatetime
            forecastDict["day"] = startDatetime.day
            forecastDict["month"] = startDatetime.month
            forecastDict["year"] = startDatetime.year
            forecastList.append(forecastDict)
        self.publishToolsData(forecastList, forecastMetadata, costReqtimestamp, costReqtimeformat, costReqisEpoch)
        self.tracking["forecastDataColletionDate"] = forecastDate.strftime("%Y-%m-%dT%H:%M:%S")

    #Load file difference(delta data) in two different CSV files
    def loadDifferenceInCSVData(self, period, fileNames, timeframes):
        localPath = os.path.join(self.csvBackupDirectoryName, period).replace("\\","/")
        filePath = os.path.join(localPath, fileNames).replace("\\","/")
        filePathOld = os.path.join(localPath, "Old" + fileNames).replace("\\","/")
        df = pd.read_csv(filePath, low_memory=False)
        if os.path.exists(filePathOld):
            dfOld = pd.read_csv(filePathOld, low_memory=False)
        df = df.set_index('h1')
        dfOld = dfOld.set_index('h1')
        df = df.reindex_like(dfOld) 
        diffInDataFrames = df[~(df==dfOld).all(axis=1)]
        self.baseLogger.info(diffInDataFrames)
        return diffInDataFrames
    
    def loadDifferenceInCSVDataMerge(self, period, fileNames, endDate):
        localPath = os.path.join(self.csvBackupDirectoryName, period).replace("\\","/")
        filePath = os.path.join(localPath, fileNames).replace("\\","/")
        filePathOld = os.path.join(localPath, "Old" + fileNames).replace("\\","/")
        df = pd.read_csv(filePath, low_memory=False)
        if os.path.exists(filePathOld):
            dfOld = pd.read_csv(filePathOld, low_memory=False)
       
        df_all = df.merge(dfOld, on=df.columns.to_list(), how='outer', indicator='exists')
        df_lft = df_all[df_all["exists"]=="left_only"]
        df_lft_old = df_all[df_all["exists"]=="right_only"]
        self.baseLogger.info(df_lft)
        self.baseLogger.info(df_lft_old)
        return df_lft

    #Format json file format
    def json_datetime_serializer(self, obj):
        if isinstance(obj, (datetime, date)):
            return obj.isoformat()
        raise TypeError ("Type %s not serializable" % type(obj))
    
    #
    def fetchResourcesMetrics(self):
        try:
            uniqueResources_df = pd.DataFrame(self.allResourcesList).drop_duplicates()
            configResourceMetricDet = self.additionalMetrics.get("resourceMetrics", [])
            
            for eachResourceType, eachResourceValue in configResourceMetricDet.items():
                resourcewithProductName_df = uniqueResources_df[uniqueResources_df['servicename'] == eachResourceValue.get('productName')].drop_duplicates()
                
                if eachResourceType == "AWS/EC2":
                    resourcewithProductName_df = resourcewithProductName_df[uniqueResources_df["resourceid"].str.startswith("i-")]
                
                if len(resourcewithProductName_df.columns) > 0 :
                    regionWiseResourceIdsMappingGroup = resourcewithProductName_df.groupby("region")
                    self.processCWMetrics(regionWiseResourceIdsMappingGroup, eachResourceValue, eachResourceType)
                    
        except Exception as ex:
                 self.baseLogger.error("fetchResources ==== "+self.manifestPath)
                 self.baseLogger.error(ex)
    
    def processCWMetrics(self, regionWiseResourceIdsMappingGroup, eachResourceValue, eachResourceType):
        
        dimensionsName = eachResourceValue.get("dimensionsName")     
        metricsList = eachResourceValue.get("availablemetrics") 
        
        
        if self.metricDataColletionDate ==None:
            startFrom =  (date.today() - timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S")
        else :
            startFrom = parser.parse(self.metricDataColletionDate, ignoretz=True)
            
        enddate = date.today().strftime("%Y-%m-%dT%H:%M:%S")    
         
        for eachregion, dataList in regionWiseResourceIdsMappingGroup:
            metricDataQueryList = []
            
            resourceDistList = dataList.apply(list).to_dict('records')
            
            count = 0
            for eachResource in resourceDistList:
                resourcename = eachResource.get('resourcename')
                
                for eachMetric, eachStatistic in metricsList.items():
                    count = count+1
                    metricDataQueryBuilder= {
                        "Id": "id" + str(count),
                        "MetricStat": {
                            "Metric": {
                                "Namespace": eachResourceType,
                                "MetricName": eachMetric,
                                "Dimensions": [
                                    {
                                        "Name": dimensionsName, 
                                        "Value": resourcename
                                    }
                                ]
                            },
                            "Period": self.additionalMetrics.get("period"),
                            "Stat": eachStatistic
                        }
                    }
                    metricDataQueryList.append(metricDataQueryBuilder)
            metricDataQueryList = json.loads(str(metricDataQueryList).replace("\'", "\""))
            
            
            #More then 500 metric query not allowed so splitting list in group of 499 
            self.baseLogger.info(" MetricQuery: Total metric data query list size ======== "+str(len(metricDataQueryList)))
            batchsMetricsData = list(self.chunks(metricDataQueryList, 499))
            for databatch in batchsMetricsData: 
                metricDataDictList = [] 
                metricDataDictList = self.callAndProcessMetricsAPI(databatch, startFrom, enddate,  eachregion, metricsList)

                if len(metricDataDictList) > 0:                
                    self.publishToolsData(metricDataDictList, self.metricMetadata)
                    self.publishToolsData(metricDataDictList, self.relationMetricsMetadata) 
                    
        self.tracking["metricDataColletionDate"] = enddate
        
    def callAndProcessMetricsAPI(self, metricDataQueryList, startFrom, enddate, eachregion, metricsList):
        metricDataDictList = []
        client = boto3.client('cloudwatch', 
                              aws_access_key_id=self.acskey,
                              aws_secret_access_key=self.scrtkey,
                              region_name=eachregion)     
        self.baseLogger.info(" MetricQuery: Processing metric data query list of ======== "+str(len(metricDataQueryList)))  
        response = client.get_metric_data(
                                MetricDataQueries=metricDataQueryList,
                                StartTime = startFrom,
                                EndTime = enddate)        

        for eachResourceMetric in response['MetricDataResults']:
            metricDataDict = {}
            if len(eachResourceMetric['Values']) > 0:
                
                metricValue = statistics.mean(eachResourceMetric['Values'])
                labelarr = eachResourceMetric["Label"].split()
                metricDataDict['resourceid'] = labelarr[0]
                metricDataDict['id'] = labelarr[0]
                metricDataDict['region'] = eachregion
                metricDataDict['name'] = labelarr[1]
                if len(eachResourceMetric["Timestamps"]) > 0:
                    metricDataDict['timeStamp'] = eachResourceMetric["Timestamps"][0].strftime("%Y-%m-%d")
                else:
                    metricDataDict['timeStamp'] = datetime.today().strftime('%Y-%m-%d')  
                metricDataDict['value'] = metricValue
                metricDataDict['unit'] = metricsList.get(labelarr[1])
                metricDataDict['cloudtype'] = "aws"
                
                metricDataDictList.append(metricDataDict)
            else:
                self.baseLogger.info("No record available for metrics "+str(eachResourceMetric))
        return metricDataDictList
    
    def chunks(self, l, n):
        for i in range(0, len(l), n):
            yield l[i:i + n]
        
if __name__ == "__main__":
    AwsFinOpsAgent()
