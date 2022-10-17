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
        self.accesskey = self.getCredential("awsAccesskey")
        self.secretkey = self.getCredential("awsSecretkey")
        self.dynamicTemplate = self.config.get('dynamicTemplate', '{}')
        self.startFromConfig = self.getCredential("startFrom")
        
        #Tracking json data
        self.startDateFromTracking = self.tracking.get('lastDataCollectionDate',None)
        self.trackingJsonUpdaterList = self.tracking.get('monthlyPeriodUpdates',[])
        
        #S3 Data
        self.s3FilePath = self.config.get('s3FilePath')
        self.unzipDirectoryNameValue = "Finops_Report"
        self.unzipDirectoryName = os.path.join(os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep, self.unzipDirectoryNameValue)

        
        self.csvBackupDirectoryNameValue = "Finops_Report_Backup"
        self.csvBackupDirectoryName = os.path.join(os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep, self.csvBackupDirectoryNameValue)
        
        splitedPath = self.s3FilePath[5:].split("/")
        self.s3BucketName = splitedPath[0]
        self.manifestFileName = splitedPath[len(splitedPath)-2]+"-Manifest.json"
        self.prefix = self.s3FilePath[(5+len(splitedPath[0])+1):]
        self.forcastMetrics = self.dynamicTemplate.get("forecast",{}).get("forecastMetrics")
        self.forecastGranularity = self.dynamicTemplate.get("forecast",{}).get('forecastGranularity')
        self.forecastRegions = self.dynamicTemplate.get("forecast",{}).get('forecastRegions', [])
        self.reportKeys = ""
        self.additionalMetrics = self.dynamicTemplate.get("additionalResourceDetails", {})
        
        try:
            self.s3ApiClient = boto3.client('s3', aws_access_key_id=self.accesskey, aws_secret_access_key=self.secretkey)
            if self.startDateFromTracking == None:
                self.startFrom = (parser.parse(self.startFromConfig, ignoretz=True)).date()
            else:
                self.startFrom =  (parser.parse(self.startDateFromTracking, ignoretz=True)).date() 
                
            self.extractLabelsFromCUReport()
            
        except Exception as ex:
            self.baseLogger.error(ex)
    
    #Extract data that are to be pushed to three label inside Neo4j graph db 
    def extractLabelsFromCUReport(self):
        
        costReqinsighstTimeX = self.dynamicTemplate.get('costManagement',{}).get('insightsTimeXFieldMapping',None)
        costReqtimestamp = costReqinsighstTimeX.get('timefield',None)
        costReqtimeformat = costReqinsighstTimeX.get('timeformat',None)
        costReqisEpoch = costReqinsighstTimeX.get('isEpoch',False)
        
        #Meta data for first three labels and last two relations
        costMetadata = self.dynamicTemplate.get("costManagement","{}").get("costMetadata","{}")
        resourceMetadata = self.dynamicTemplate.get("resourceTags","{}").get("resourceMetadata","{}")
        resourceDetailsMetadata = self.dynamicTemplate.get("resourceDetails","{}").get("resourceDetailsMetadata","{}")
        costRelationshipData = self.dynamicTemplate.get("resourceDetails", "{}").get("relationCostMetadata", "{}")
        relationTagsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("relationTagsMetadata", "{}")
        
        periodRecordsList = []
        periodsList = []
        periodLastModifdDic = {}
        isFirstLoad = False
        
        if self.trackingJsonUpdaterList == []:
            s3RespFoldersList = self.s3ApiClient.list_objects(Bucket=self.s3BucketName, Prefix=self.prefix, Delimiter='/')
            periodsList = [d['Prefix'].rstrip('/')[d['Prefix'].rstrip('/').rindex("/")+1:] for d in s3RespFoldersList.get('CommonPrefixes')]
            isFirstLoad = True
        else:
            for eachRecord in self.trackingJsonUpdaterList:
                if eachRecord.get("invoiceid", "") == "":
                    periodsList.append(eachRecord.get("monthPrefix", ""))
                else:
                    periodRecordsList.append(eachRecord)
                periodLastModifdDic[eachRecord.get("monthPrefix", "")] = (parser.parse(eachRecord.get("lastModified", ""), ignoretz=True))
            currentMonthStartDt = datetime.today().replace(day=1)
            currentMonthPeriod = currentMonthStartDt.strftime("%Y%m%d") + "-" + (currentMonthStartDt + relativedelta(months=1)).strftime("%Y%m%d")
            if not any([period == currentMonthPeriod for period in periodsList]):
                periodsList.append(currentMonthPeriod)
        if not os.path.exists(self.unzipDirectoryName):
            os.mkdir(self.unzipDirectoryName)
        for eachPeriod in periodsList:
            year = eachPeriod[0:4]
            month = eachPeriod[4:6]
            periodStartDate = datetime(int(year), int(month), 1).date()
            if self.startFrom.replace(day=1) > periodStartDate:
                self.baseLogger.info("Skipping period -> " + eachPeriod)
                continue
            else:
                self.baseLogger.info("Processing for Period -> " + eachPeriod)
            monthPeriodDic = {}
            monthPeriodDic["monthPrefix"] = eachPeriod
            self.manifestPath=self.prefix + os.path.join(eachPeriod, self.manifestFileName).replace("\\","/")
            #Check if the S3 bucket has latest update using head_object API call on our csv file
            response = self.s3ApiClient.head_object(Bucket=self.s3BucketName, Key=self.manifestPath,)
            last_modified = (parser.parse(response.get("ResponseMetadata").get("HTTPHeaders").get("last-modified"), ignoretz=True))
            monthPeriodDic["lastModified"] =  str(last_modified)
             
            if not isFirstLoad and periodLastModifdDic.get(eachPeriod) != None and last_modified <= periodLastModifdDic.get(eachPeriod):
                monthPeriodDic["invoiceid"] = ""
                periodRecordsList.append(monthPeriodDic)
                continue
                
            #Download Manifest file to get the reportKeys to get all the updated files one by one
            self.mainfestFile = os.path.join(self.unzipDirectoryName, self.manifestFileName).replace("\\","/")
            self.s3ApiClient.download_file(self.s3BucketName, self.manifestPath, self.mainfestFile)
            self.reportKeys = self.loadManifest()
            
            #Remove any existing folder before downloading the csv files from S3 bucket
            shutil.rmtree(self.unzipDirectoryName, ignore_errors=True)
            if not os.path.exists(self.csvBackupDirectoryName):
                os.mkdir(self.csvBackupDirectoryName)
            for eachCSVFile in self.reportKeys:
                #CSV File Download from S3 Bucket to local directory path
                fileName = eachCSVFile[eachCSVFile.rindex("/")+1:]
                self.s3ApiClient.download_file(self.s3BucketName, eachCSVFile, fileName)         
                with ZipFile(fileName, 'r') as zip_ref:
                    zip_ref.extractall(self.unzipDirectoryName)
                    monthDirPath = os.path.join(self.csvBackupDirectoryName, eachPeriod)
                    if not os.path.exists(monthDirPath):
                        os.mkdir(monthDirPath)
                    backupFile = os.path.join(self.csvBackupDirectoryName, eachPeriod, fileName).replace("\\","/")[:-4]
                    backupFileOld = os.path.join(self.csvBackupDirectoryName, eachPeriod, "Old"+fileName).replace("\\","/")[:-4]
                    if os.path.exists(backupFileOld):
                        os.remove(backupFileOld)
                        os.rename(backupFile, backupFileOld)
                    zip_ref.extractall(os.path.join(self.csvBackupDirectoryName, eachPeriod).replace("\\","/"))
                os.remove(fileName)
#                 if not isFirstLoad:
#                     dataframeCUReport = self.loadDifferenceInCSVData(eachPeriod, fileName[:-4])
#                 else:
                dataframeCUReport = pd.read_csv(self.unzipDirectoryName + "/" + fileName[:-4], low_memory=False)
                #User defined resource tags column collection
                resourceTags = [col for col in dataframeCUReport if col.startswith('resourceTags/user:')]
                #Fetch all records from csv file
                dfOfCURSortedByDate = dataframeCUReport.sort_values(by="lineItem/UsageStartDate")
          
                #List creation- for all 3 labels
                costList = []
                resourceTagsSet = set()
                resourceDetailsSet = set()
                invoiceid = ""
                #Loop through all the records based on the index position of the rows one by one
                for ind, row in dfOfCURSortedByDate.iterrows():
                    resourceId = "-"
                    if not pd.isna(row["lineItem/ResourceId"]):
                        resourceId= row["lineItem/ResourceId"]
                    if invoiceid == "":
                        if pd.isna(row["bill/InvoiceId"]):
                            invoiceid = ""
                        else:
                            invoiceid = str(row["bill/InvoiceId"])
                    costList.append(self.costManagementDetails(row,resourceId))
                    resourceDetailsSet.add(tuple(self.resourceDetailsDict(row, resourceId).items()))
                    resourceTagsSet = self.resourceTagsDetailsDict(row, resourceTags, resourceTagsSet, resourceId)
                
                monthPeriodDic["invoiceid"] = invoiceid  
                
                #Remove duplicate records              
                resourceTagsListRmdDupl=[dict(t) for t in resourceTagsSet]
                resourceDetailsListRmdDupl = [dict(t) for t in resourceDetailsSet]
                
                self.baseLogger.info("Cost management length = " + str(len(costList)))
                self.baseLogger.info("Resource tags length = " + str(len(resourceTagsListRmdDupl)))
                self.baseLogger.info("Resource details length = " + str(len(resourceDetailsListRmdDupl)))
                
                #Publish to Neo4j via RabbitMQ and InsightEngine
                self.publishToolsData(costList, costMetadata, costReqtimestamp, costReqtimeformat, costReqisEpoch)
                self.publishToolsData(resourceDetailsListRmdDupl, resourceDetailsMetadata)
                self.publishToolsData(resourceTagsListRmdDupl, resourceMetadata)
                self.publishToolsData(resourceDetailsListRmdDupl, costRelationshipData)
                self.publishToolsData(resourceTagsListRmdDupl, relationTagsMetadata)
                
                #if self.dynamicTemplate.get("additionalResourceDetails",{}).get("isMetricsNeeded") == False:
                #    self.fetchResources(dfOfCURSortedByDate)
                    
            periodRecordsList.append(monthPeriodDic)
        self.baseLogger.info("Updated monthlyPeriodData " + str(periodRecordsList))
        
        if self.dynamicTemplate.get("forecast",{}).get("isForecastNeeded")== False:
            self.forecatedCostExplorer(self.forecastRegions, self.forecastGranularity, self.forcastMetrics)
            
        self.tracking["lastDataCollectionDate"] = str(last_modified)
        self.tracking["monthlyPeriodUpdates"] = periodRecordsList
        self.updateTrackingJson(self.tracking)

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
    def resourceTagsDetailsDict(self, row, resourceTags, resourceTagsSet, resourceid):
        nonNullResourceTags = row.loc[resourceTags].dropna()
        resourceTagsDict = {}
        if len(nonNullResourceTags) > 0 :
            for index, value in nonNullResourceTags.items():
                resourceTagsDict = {}
                resourceTagsDict["resourceid"] = resourceid
                resourceTagsDict["tagkey"] = index.split(":")[1]
                resourceTagsDict["tagvalue"] = value
                resourceTagsDict["cloudtype"] = "aws"
                resourceTagsSet.add(tuple(resourceTagsDict.items()))
        else :
            resourceTagsDict = {}
            resourceTagsDict["resourceid"] = resourceid
            resourceTagsDict["tagkey"] = "NoTag"
            resourceTagsDict["tagvalue"] = "NoValue"
            resourceTagsDict['cloudtype'] = 'aws'
            resourceTagsSet.add(tuple(resourceTagsDict.items()))
        return resourceTagsSet
    
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
        return resourceDetailsDict
    
    #Load manifest from local and return reportKeys List
    def loadManifest(self):
        reourcesJsonFileOpened = open(self.unzipDirectoryName + "/" + self.manifestFileName)      
        response = json.load(reourcesJsonFileOpened)
        return response["reportKeys"]
    
    def forecatedCostExplorer(self, region, granularity, forecastMetric):
        costReqinsighstTimeX = self.dynamicTemplate.get('forecast',{}).get('insightsTimeXFieldMapping',None)
        costReqtimestamp = costReqinsighstTimeX.get('timefield',None)
        costReqtimeformat = costReqinsighstTimeX.get('timeformat',None)
        costReqisEpoch = costReqinsighstTimeX.get('isEpoch',False)
        client = boto3.client('ce', aws_access_key_id=self.accesskey,
                                     aws_secret_access_key=self.secretkey)
        start=(datetime.today() + timedelta(days=1)).strftime("%Y-%m-%d")
        frcstEndDt = (datetime.today() + relativedelta(months=1)).strftime("%Y-%m-%d")
        forecastRequest = self.dynamicTemplate.get("forecast","{}").get("forecastrequest","{}").get("Filter", "{}")
        regions = ""
        for reg in region: 
            regions = regions + reg + "\",\""
            
        regions = regions.rstrip(",\"")
        requestFile = json.loads(json.dumps(forecastRequest).replace("allregions", regions))

        if (not region == [] and region[0] == "All") or region == []:
            response = client.get_cost_forecast(
                TimePeriod={
                    'Start': start,
                    'End':  frcstEndDt
                },
                Granularity=granularity,
                Metric=forecastMetric
                )
        else:
            response = client.get_cost_forecast(
                TimePeriod={
                    'Start': start,
                    'End':  frcstEndDt
                },
                Granularity=granularity,
                Metric=forecastMetric,
                Filter=requestFile
                )
        forecastMetadata = self.dynamicTemplate.get("forecast","{}").get("forecastMetadata","{}")
        forecastList = []
        forecastReponseList = response.get("ForecastResultsByTime", [])
        for eachForecastData in forecastReponseList:
            forecastDict = {}
            forecastDict["totalcost"] = response.get("Total").get("Amount")
            forecastDict["currency"] = response.get("Total").get("Unit")
            forecastDict["usagedate"]  = eachForecastData.get("TimePeriod").get("Start")
            forecastDict["enddate"] = eachForecastData.get("TimePeriod").get("End")
            forecastDict["cost"] = eachForecastData.get("MeanValue")
            forecastDict["cloudtype"] = "aws"
            startDatetime = datetime.strptime(eachForecastData.get("TimePeriod").get("Start"), "%Y-%m-%d")
            forecastDict["day"] = startDatetime.day
            forecastDict["month"] = startDatetime.month
            forecastDict["year"] = startDatetime.year
            forecastList.append(forecastDict)
        self.publishToolsData(forecastList, forecastMetadata, costReqtimestamp, costReqtimeformat, costReqisEpoch)
        self.baseLogger.info(json.dumps(response,
                        indent=4,
                        default=self.json_datetime_serializer))

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
    def fetchResources(self, dfOfCURSortedByDate):
        fileName2 = os.path.join(self.unzipDirectoryName, dfOfCURSortedByDate)
        dfOfCURSortedByDate = pd.read_csv(fileName2, low_memory=False)
        dfWithUniqueResourceId = dfOfCURSortedByDate.drop_duplicates(subset = ["lineItem/ResourceId"])
        resourceType2ProductNameList = [[key, value.get("ObjectProperties")]
                               for key, value in self.additionalMetrics.get("resourceMetrics", []).items()]
        for eachResourceType, eachProductName in resourceType2ProductNameList:
            dfResourceType = [df 
                                for name, df in dfWithUniqueResourceId.groupby("product/ProductName") 
                                if eachProductName==name][0]
            dfVoidNanValues = dfResourceType[~dfResourceType["lineItem/ResourceId"].isna()]
            resourceName = self.additionalMetrics.get("resourceMetrics", []).get(eachResourceType).get("resourceName")     
            region2ResourceIdMapping = {}
            if eachResourceType == "AWS/EC2":
                dfVoidNanValues = dfVoidNanValues.query('`lineItem/ResourceId`.str.startswith("i-")')
            region2ResourceIdsMapping = dfVoidNanValues.groupby("product/region")["lineItem/ResourceId"].apply(list)
            if len(region2ResourceIdsMapping) > 0:
                self.list_cw_metrics(region2ResourceIdsMapping, resourceName, eachResourceType)
    
    def list_cw_metrics(self, region2ResourceIdMapping, resourceName, eachResourceType):
        metricMetadata = self.dynamicTemplate.get("metrics","{}").get("metricsMetadata","{}")
        matricsDataList = []
        metricsList = [value.get("availablemetrics")
                        for key, value in self.additionalMetrics.get("resourceMetrics", []).items()
                        if key==eachResourceType]
        startFrom =  (date.today() - timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S")
        enddate = date.today().strftime("%Y-%m-%dT%H:%M:%S")
        metricsList = metricsList[0]   
        for eachMetric, eachStatistic in metricsList.items():
            count = 0
            for eachregion, eachResourceIdList  in region2ResourceIdMapping.items():
                metricDataQuery = []
                for eachResourceId in eachResourceIdList:
                    count = count+1
                    metricDataQueryBuilder= {
                        "Id": "id" + str(count),
                        "MetricStat": {
                            "Metric": {
                                "Namespace": eachResourceType,
                                "MetricName": eachMetric,
                                "Dimensions": [
                                    {
                                        "Name": resourceName, 
                                        "Value": eachResourceId
                                    }
                                ]
                            },
                            "Period": self.additionalMetrics.get("period"),
                            "Stat": eachStatistic
                        }
                    }
                    metricDataQuery.append(metricDataQueryBuilder)
                metricDataQuery = json.loads(str(metricDataQuery).replace("\'", "\""))
                rtvalue = self.ec2_metrics_data(metricDataQuery, startFrom, enddate, eachMetric, eachregion)
                for eachResourceId in eachResourceIdList:  
                    matricsDataDict = {}
                    matricsDataDict['resourceid'] = eachResourceId
                    matricsDataDict['id'] = eachResourceId
                    matricsDataDict['region'] = eachregion
                    matricsDataDict['name'] = eachMetric
                    matricsDataDict['timeStamp'] = startFrom
                    matricsDataDict['enddate'] = enddate
                    matricsDataDict['value'] = rtvalue[eachResourceId]
                    matricsDataList.append(matricsDataDict)
        self.baseLogger.info(json.dumps(matricsDataList,
                                indent=4,
                                default=self.json_datetime_serializer))
        self.publishToolsData(matricsDataList, metricMetadata)
        
    def ec2_metrics_data(self, metricDataQuery, startFrom, enddate, eachMetric, eachregion):
        client = boto3.client('cloudwatch', 
                              aws_access_key_id=self.accesskey,
                              aws_secret_access_key=self.secretkey,
                              region_name=eachregion)        
        response = client.get_metric_data(
                                MetricDataQueries=metricDataQuery,
                                StartTime = startFrom,
                                EndTime = enddate)
        metricDataDict = {}
        self.baseLogger.info("Getting metrics data for the Metric name : " + eachMetric)
        self.baseLogger.info(json.dumps(response,
                        indent=4,
                        default=self.json_datetime_serializer))
        for eachResourceMetric in response['MetricDataResults']:
            if eachResourceMetric['Values'] != []:
                metricValue = statistics.mean(eachResourceMetric['Values'])
            else:
                metricValue = 0
            metricDataDict[eachResourceMetric["Label"]] = metricValue
        self.baseLogger.info(metricDataDict)
        return metricDataDict
        
if __name__ == "__main__":
    AwsFinOpsAgent()
