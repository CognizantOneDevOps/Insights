#-------------------------------------------------------------------------------
# Copyright 2023 Cognizant Technology Solutions
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

import sys
from dateutil.utils import today
import time
import os
from core.BaseAgent3 import BaseAgent
from datetime import datetime, timedelta, date
from dateutil import parser
import json
import pandas as  pd
from google.cloud import bigquery
from googleapiclient.discovery import build
from google.oauth2 import service_account
from googleapiclient import discovery
from google.cloud import asset_v1
from google.cloud.billing import budgets_v1
from google.cloud import monitoring_v3
from google.protobuf.json_format import MessageToDict
from collections import defaultdict
from google.cloud import recommender
import time
import math

class GcpFinOpsAgent(BaseAgent):

    @BaseAgent.timed
    def process(self):
        key_path = self.config.get('credentials', '{}')  
        self.baseLogger.info('Inside process')        
        self.credentials = service_account.Credentials.from_service_account_file(
            key_path, scopes=[ "https://www.googleapis.com/auth/monitoring","https://www.googleapis.com/auth/monitoring.read", "https://www.googleapis.com/auth/admin.directory.user","https://www.googleapis.com/auth/cloud-billing","https://www.googleapis.com/auth/cloud-platform"],
        ) 
        self.dynamicTemplate = self.config.get('dynamicTemplate', '{}')  
        self.agentBaseDir = os.path.dirname(sys.modules[self.__class__.__module__].__file__) + os.path.sep  
        self.monitoringclient = monitoring_v3.MetricServiceClient(credentials=self.credentials) 
        self.map = defaultdict(list)       
        self.metricDescriptorFileName = "metricdescriptors"  
        self.startFromConfig = self.config.get("startFrom")
        self.trackingReportLastRun = self.tracking.get("LastReportDataCollectionDate")  
        self.costReqinsighstTimeX = self.dynamicTemplate.get('costManagement',{}).get('insightsTimeXFieldMapping',None)
        self.costReqtimestamp = self.costReqinsighstTimeX.get('timefield',None)
        self.costReqtimeformat = self.costReqinsighstTimeX.get('timeformat',None)
        self.costReqisEpoch = self.costReqinsighstTimeX.get('isEpoch',False)
        self.baseCurrency = self.config.get('baseCurrency', "")
        self.projectMetadata = self.dynamicTemplate.get("project", "{}").get("projectMetadata", "{}")
        self.budgetMetadata = self.dynamicTemplate.get("budget", "{}").get("budgetMetadata", "{}")
        self.billingMetadata = self.dynamicTemplate.get("billing", "{}").get("billingMetadata", "{}")
        self.costMetadata = self.dynamicTemplate.get("costManagement","{}").get("costMetadata","{}")
        self.billingclient = discovery.build('cloudbilling', 'v1', credentials=self.credentials)
        self.service = discovery.build('cloudresourcemanager', 'v1', credentials=self.credentials)
        self.allResourcesList = []
        self.projectList = []
        self.billingList = []
        
        
        try:            
            self.getPeriodStartAndEndDate()
            datetocompare = ' '.join(self.periodStartDate.split(' ')[:-1])
            if  datetime.strptime(datetocompare,'%Y-%m-%d %H:%M:%S.%f').date() == date.today():                
                self.baseLogger.info("GCP_Report Data collected till yesterday"+ str(date.today() - timedelta(days=1)))
            else:               
                self.getProjectBillingInfo()
                self.getProjectDetails()
                self.getBudgetDetails()
                self.tracking["LastReportDataCollectionDate"] = datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S.%f') + " UTC"  
                self.updateTrackingJson(self.tracking)
        except Exception as ex:
            self.baseLogger.error(ex)


    def updateMetricDescriptor(self, fileName, trackingDict):
        self.baseLogger.info('Inside updateMetricDescriptor')
        with open(self.agentBaseDir + fileName + '.json', 'w') as filePointer:
            json.dump(trackingDict, filePointer, indent=4) 

    def checkMetricDescriptor(self, fileName):
        self.baseLogger.info('Inside check MetricDescriptor')
        return os.path.isfile(self.agentBaseDir + fileName+ '.json')  

    def loadMetricDescriptor(self,project_name):
        if not os.path.exists(self.agentBaseDir + self.metricDescriptorFileName+ '.json') or ( os.path.exists(self.agentBaseDir + self.metricDescriptorFileName+ '.json') and os.path.getsize(self.agentBaseDir + self.metricDescriptorFileName+ '.json') <= 0):
            self.updateMetricDescriptor(self.metricDescriptorFileName, dict())  
            self.fetchMetricsDescriptors(project_name)
        else:
            self.metricDescriptorTrackingDetail = self.trackingCacheFileLoad(self.metricDescriptorFileName) 
            for k,v in self.metricDescriptorTrackingDetail.items():
                resname = v["resource_name"]
                self.map[resname].append(k) 
    
    def fetchMetricsDescriptors(self,project_name):
        try:
            metricDescDict = {}            
            descriptorlist = self.monitoringclient.list_metric_descriptors(name=project_name)
            descriptorlistObj = MessageToDict(descriptorlist._pb)             
            for descriptor in descriptorlistObj["metricDescriptors"]:
                metricdescObj = {}                
                resource_name = descriptor.get("monitoredResourceTypes",[])[0]                
                type = descriptor.get("type","")
                metrickind = descriptor.get("metricKind","")
                valtype = descriptor.get("valueType","")
                metricdescObj["unit"] = descriptor.get("unit","")
                metricdescObj["resource_name"] = resource_name
                metricdescObj["metricKind"] = metrickind
                metricdescObj["valuetype"] = valtype
                if metrickind == "DELTA"  and valtype != "DISTRIBUTION":
                    metricdescObj["aligner"] = "ALIGN_MEAN" 
                else:
                    metricdescObj["aligner"] ="ALIGN_NONE"              
               
                metricDescDict[type] = metricdescObj
                self.map[resource_name].append(type)
                
        except Exception as ex:
            self.baseLogger.error(ex)  
            
        self.updateTrackingCache(self.metricDescriptorFileName, metricDescDict)  
        self.metricDescriptorTrackingDetail = self.trackingCacheFileLoad(self.metricDescriptorFileName)
    
    def trackingCacheFileLoad(self, fileName):
        self.baseLogger.info('Inside trackingCacheFileLoad')
        with open(self.agentBaseDir + fileName + '.json', 'r') as filePointer:
            data = json.load(filePointer)
        return data 

    def getAligner(self, metricType):
        metricDsecriptorDict = self.metricDescriptorTrackingDetail.get(metricType, None)
        if metricDsecriptorDict == None:
            metricaligner = monitoring_v3.Aggregation.Aligner.ALIGN_MEAN
        else:
            metricaligner = metricDsecriptorDict["aligner"]
        
        return metricaligner
    
    def updateTrackingCache(self, fileName, trackingDict):
        self.baseLogger.info('Inside updateTrackingCache')
        with open(self.agentBaseDir + fileName + '.json', 'w') as filePointer:
            json.dump(trackingDict, filePointer, indent=4) 
    
    def getPeriodStartAndEndDate(self):
        if self.trackingReportLastRun == None:
            self.periodStartDate = self.startFromConfig
        else:
            self.periodStartDate = self.trackingReportLastRun            
                   
    
    def extractDataFromBigQueryTable(self,projectid):
        propertyList = "";
        groupbyList = "";
        datasetdet = self.dynamicTemplate.get("datasetdetails",{})
        projectName=projectid+"."+datasetdet[projectid]["datasetname"]+"."+datasetdet[projectid]["datareporttablename"]
         
        BQ = bigquery.Client(credentials=self.credentials, project=projectid,)
        queryProperties = self.dynamicTemplate.get("QueryProperties",{})
        groupbyProperties = self.dynamicTemplate.get("GroupByProperties",{})
        
        for property in queryProperties.values():
            propertyList += property+","
        
        for property in groupbyProperties.values():
            groupbyList += property+","
            
        propertyList = propertyList[:-1]
        groupbyList = groupbyList[:-1]
        
        query = f"""
        SELECT """+propertyList+""" 
        FROM """+projectName+""" 
        WHERE export_time >= '"""+str(self.periodStartDate)+"""'              
        GROUP BY """+groupbyList+"""      
        """
        self.baseLogger.info('select query: '+ query)
        self.df = BQ.query(query).result().to_dataframe()        
        

    def getCostManagement(self,projectid):    
        self.costList = []
        for index, row in self.df.iterrows():
            try:
                costRowDetailDict = {}
                costRowDetailDict["resourceid"]=""
                if row["resourceglobalname"] != None:
                    costRowDetailDict["resourceid"] = row["resourceglobalname"]
                for label in json.loads(row["labels"]):
                
                    if (label["key"] == "resourceid") or (label["key"] == "goog-resource-type"):
                        costRowDetailDict["resourceid"]= label["value"]
                    else:
                        costRowDetailDict[label["key"]]= label["value"] 
                for syslabel in json.loads(row["systemlabels"]):
                    costRowDetailDict[syslabel["key"]]= syslabel["value"]            
                
                costRowDetailDict["serviceid"] = row["serviceid"] 
                costRowDetailDict["servicename"] = row["servicedesc"]  
                costRowDetailDict["skuid"] = row["skuid"]  
                costRowDetailDict["skuname"] = row["skudesc"]    
                costRowDetailDict["billingaccountid"] = row["billing_account_id"]  
                costRowDetailDict["country"] = row["country"]   
                costRowDetailDict["location"] = row["location"]    
                costRowDetailDict["region"] = row["region"]    
                costRowDetailDict["zone"] = row["zone"]        
                costRowDetailDict["invoiceMonth"] = row["invoicemonth"]  
                costRowDetailDict["cost"] = row["cost"]
                costRowDetailDict["currency"] = row["currency"]
                costRowDetailDict["cloudtype"] = "gcp"
                costRowDetailDict["year"] = row["strdt"].year
                costRowDetailDict["month"] = row["strdt"].month
                costRowDetailDict["day"] = row["strdt"].day
                costRowDetailDict["usagedate"] = str(row["strdt"])
                costRowDetailDict["creditamount"] = row["creditamnt"]
                if math.isnan(costRowDetailDict["creditamount"]) :
                    costRowDetailDict["creditamount"] = 0
                self.dollarrate = row["currency_conversion_rate"]
                costRowDetailDict["dollarrate"] = self.dollarrate 
                costRowDetailDict["costusdrate"] = costRowDetailDict["cost"] / self.dollarrate 
                if math.isnan(costRowDetailDict["costusdrate"]) :
                    costRowDetailDict["costusdrate"] = 0
                costRowDetailDict["creditusdrate"] = costRowDetailDict["creditamount"] / self.dollarrate
                if math.isnan(costRowDetailDict["creditusdrate"]):
                    costRowDetailDict["creditusdrate"] = 0
                costRowDetailDict["actualcost"] = row["cost"] + row["creditamnt"]
                costRowDetailDict["actualcostusdrate"] = costRowDetailDict["actualcost"] / self.dollarrate
                if math.isnan(costRowDetailDict["actualcostusdrate"]):
                    costRowDetailDict["actualcostusdrate"] = 0
                costRowDetailDict["projectid"] = projectid
                self.costList.append(costRowDetailDict)
            except Exception as e:           
                self.baseLogger.info(e)
        
        self.baseLogger.info("Cost management length = " + str(len(self.costList)))            
        
        #Publish to Neo4j via RabbitMQ and InsightEngine
        self.publishToolsData(self.costList, self.costMetadata, self.costReqtimestamp, self.costReqtimeformat, self.costReqisEpoch)
    
    

    def addResourceDetails(self, projectId):
        
        tagDetailsList = []
        resourceDetailsList = [] 
        resourceMetrics = []
        resourceDetailsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("resourceDetailsMetadata", "{}")
        resourceMetadataIndividual = self.dynamicTemplate.get("resourceTagsIndividual", "{}").get("resourceMetadata", "{}")
        relationTagsMetadata = self.dynamicTemplate.get("resourceDetails", "{}").get("relationTagsMetadata", "{}")
        resourceMetricsMetadata = self.dynamicTemplate.get("resourceMetricsdDetails", "{}").get("resourceMetricsMetadata", "{}")
        relationMetricsMetadata = self.dynamicTemplate.get("resourceMetricsdDetails", "{}").get("resourceMetricsRelationMetadata", "{}")
        
        try:
            projectName = "projects/"+projectId
            client = asset_v1.AssetServiceClient(credentials=self.credentials)
            results = client.search_all_resources(scope=projectName,asset_types=["compute.googleapis.com/Instance","bigquery.googleapis.com/Dataset","bigquery.googleapis.com/Table","cloudfunctions.googleapis.com/CloudFunction","iam.googleapis.com/ServiceAccount"])
            reslist = []
            t1 = MessageToDict(results._pb)
            
            for res in t1["results"]:                
                resobj = {}
                resobj["name"] = res.get("name","")
                resobj["location"] = res.get("location","")
                resobj["displayName"] = res.get("displayName","")
                resobj["servicename"] = res.get("assetType","")
                resobj["project"] = res.get("project","")
                resobj["createTime"] = res.get("createTime","")
                resobj["state"] = res.get("state","")
                resobj["parentFullResourceName"] = res["parentFullResourceName"]
                resobj["parentAssetType"] = res["parentAssetType"]
                resobj["cloudtype"] = "gcp"
                for name,val in res.get("additionalAttributes",{}).items():
                    resobj[name] = val
                for ln,lv in res.get("labels",{}).items():
                    resobj[ln] = lv
                if resobj.get("id","") != "":
                    resobj["resourceid"] = resobj.get("id","")
                elif resobj.get("uniqueId","") != "":
                    resobj["resourceid"] = resobj.get("uniqueId","")
                else:
                    resobj["resourceid"] = resobj.get("name","") 
                resourceDetailsList.append(resobj)
                self.prepareResourceTagData(tagDetailsList,res,resobj.get("id",""))
            self.getResourceMetrics(projectName,resourceMetrics)                                
                    
        except Exception as ex: 
            self.baseLogger.error(ex)
            
                                            
        self.publishToolsData(resourceDetailsList, resourceDetailsMetadata) 
        self.publishToolsData(tagDetailsList, resourceMetadataIndividual)                 
        self.publishToolsData(tagDetailsList, relationTagsMetadata)
        self.publishToolsData(resourceMetrics, resourceMetricsMetadata)
        self.publishToolsData(resourceMetrics, relationMetricsMetadata) 
       

    def prepareResourceTagData(self, tagDetailsList, resource, resourceId):
        if bool(resource.get("labels",{})):
            for key,val in resource.get("labels",{}).items():
                tagDetailDict = {}
                tagDetailDict["resourceid"] = resourceId
                tagDetailDict["tagkey"] = key
                tagDetailDict["tagvalue"] = val
                tagDetailDict['cloudtype'] = 'gcp'
                tagDetailsList.append(tagDetailDict)
        
        else:
            tagDetailDict = {}
            tagDetailDict["resourceid"] = resourceId
            tagDetailDict["tagkey"] = "NoTag"
            tagDetailDict["tagvalue"] = "NoValue"
            tagDetailDict['cloudtype'] = 'gcp'
            tagDetailsList.append(tagDetailDict)

    def getProjectDetails(self): 
        request = self.service.projects().list()             

        while request is not None:
            response = request.execute()            
            for project in response.get('projects', []):
                projectObj = {}
                prjId = project["projectId"]
                ancestorid = "0"
                billingresp = self.billingclient.projects().getBillingInfo(name="projects/"+prjId).execute()                
                projectObj["name"] = project["name"]
                projectObj["id"] = prjId
                projectObj["projectnumber"] = project['projectNumber']
                projectObj["lifecycleState"] = project["lifecycleState"]
                projectObj["createTime"] = project["createTime"]   
                projectObj["billingAccountName"] = billingresp["billingAccountName"]
                projectObj["billingEnabled"] = billingresp["billingEnabled"]
                projectObj["cloudtype"] = "gcp"
                ancestryservicerep = self.service.projects().getAncestry(projectId=prjId).execute()
                for ancestor in ancestryservicerep.get('ancestor', []):
                    if ancestor["resourceId"]["type"] == "organization":
                        ancestorid = ancestor["resourceId"]["id"]
                projectObj["organization"] = ancestorid
                self.projectList.append(projectObj)
                self.publishToolsData(self.projectList, self.projectMetadata)
                self.extractDataFromBigQueryTable(prjId)
                self.getCostManagement(prjId)
                self.loadMetricDescriptor("projects/"+prjId)
                self.addResourceDetails(prjId)                
                self.recommendations(prjId)
            request = self.service.projects().list_next(previous_request=request, previous_response=response)
        

    def getResourceMetrics(self,project_name,metriclist):
        start = time.time()
        secs = int(start)
        nanos = int((start - secs) * 10 ** 9)
        end = secs - 600 

        interval = monitoring_v3.TimeInterval(
                {
                    "end_time": {"seconds": secs, "nanos": nanos},
                    "start_time": {"seconds": end, "nanos": nanos},
                }
            )               
              
        for metric_type in self.map['gce_instance']:
            aggregation = monitoring_v3.Aggregation(
                {
                    "alignment_period": {"seconds": 600}, 
                    "per_series_aligner": monitoring_v3.Aggregation.Aligner[self.getAligner(metric_type)],
                }
            )
                      
            results = self.monitoringclient.list_time_series(
                request={
                    "name": project_name,
                    "filter": f'metric.type = "{metric_type}"',
                    "interval": interval,
                    "view": monitoring_v3.ListTimeSeriesRequest.TimeSeriesView.FULL,
                    "aggregation": aggregation
                }
            )
            reslist = MessageToDict(results._pb)
            for res in reslist.get("timeSeries",[]):               
                metricsObj = {}
                metdet = res.get("metric",{})
                resdet = res.get("resource",{})
                valtype = res.get("valueType","")
                metricsObj["metrictype"] = metdet.get("type","")
                metricsObj["instancename"] = metdet.get("labels",{}).get("instance_name","")
                metricsObj["resourcetype"] = resdet.get("type","")
                metricsObj["projectId"] = resdet.get("labels",{}).get("project_id","")
                metricsObj["zone"] = resdet.get("labels",{}).get("zone","")
                metricsObj["resourceid"] = resdet.get("labels",{}).get("instance_id","")
                metricsObj["metrickind"] = res.get("metricKind","")
                metricsObj["valuetype"] = valtype
                metricsObj["startdate"] = res.get("points",[])[0].get("interval",{}).get("startTime",None)
                if valtype == "DOUBLE":
                    metricsObj["value"] = res.get("points",[])[0].get("value",{}).get("doubleValue",0.0)
                elif valtype == "INT64":
                    metricsObj["value"] = res.get("points",[])[0].get("value",{}).get("int64Value",0)
                elif valtype == "STRING":
                    metricsObj["value"] = res.get("points",[])[0].get("value",{}).get("stringValue","")
                else:
                    metricsObj["value"] = res.get("points",[])[0].get("value",{}).get("boolValue","")
                metricsObj["cloudtype"] = "gcp"
                metriclist.append(metricsObj)
                
    def getProjectBillingInfo(self):
        
        billingclientresp = self.billingclient.billingAccounts().list().execute()
        for billing in billingclientresp.get('billingAccounts', []):
            billinfo = {}
            billingname = billing["name"]
            billinfo["name"] = billing["name"]
            billinfo["masterBillingAccount"] = billing["masterBillingAccount"]
            billinfo["displayName"] = billing["displayName"]
            billinfo["open"] = billing["open"]
            billinfo["id"] = billingname.rsplit('/', 1)[1]
            billinfo["cloudtype"] = "gcp"
            self.billingList.append(billinfo)
        self.publishToolsData(self.billingList, self.billingMetadata)

    def getBudgetDetails(self):
        budgetList = [] 
        
        client = budgets_v1.BudgetServiceClient(credentials=self.credentials)
        for billingInfo in self.billingList:
            billingacc = "billingAccounts/"+billingInfo["id"]
            request = budgets_v1.ListBudgetsRequest(
                parent=billingacc,
            )
        
            for element in client.list_budgets(request): 
                budgetObj = {}
                budgetname = element.name
                budgetObj["id"] = budgetname.rsplit('/', 1)[1]
                budgetObj["name"] = budgetname 
                budgetObj["currency"] = element.amount.specified_amount.currency_code
                budgetObj["amount"] = element.amount.specified_amount.units
                budgetObj["displayname"] = element.display_name
                budgetObj["etag"] = element.etag
                budgetObj["credittype"] = element.budget_filter.credit_types_treatment.name
                budgetObj["calendarperiod"] = element.budget_filter.calendar_period.name   
                if budgetObj["currency"] != "USD": 
                    budgetObj["amountUSD"]  = self.dollarrate * element.amount.specified_amount.units
                budgetList.append(budgetObj)
        self.publishToolsData(budgetList, self.budgetMetadata) 
   
    def getOrgHierarchy(self):
        
        v1client = build('cloudresourcemanager', 'v1', credentials=self.credentials, cache_discovery=False)
        v2client = build('cloudresourcemanager', 'v2', credentials=self.credentials, cache_discovery=False) 
        ORGANIZATION_ID = self.organizationId      

        filter='parent.type="organization" AND parent.id="{}"'.format(ORGANIZATION_ID)
        projects_under_org = v1client.projects().list(filter=filter).execute()    
        all_projects = [p['projectId'] for p in projects_under_org['projects']]
    
        parent="organizations/"+ORGANIZATION_ID
        folders_under_org = v2client.folders().list(parent=parent).execute()
                                                                                                                                                                        # Make sure that there are actually folders under the org
        if not folders_under_org:
            return all_projects

        
        folder_ids = [f['name'].split('/')[1] for f in folders_under_org['folders']]
        
        while folder_ids:            
            current_id = folder_ids.pop()           
           
            subfolders = v2client.folders().list(parent="folders/"+current_id).execute()
            
            if subfolders:
                folder_ids.extend([f['name'].split('/')[1] for f in subfolders['folders']])            
            
            filter='parent.type="folder" AND parent.id="{}"'.format(current_id)
            projects_under_folder = v1client.projects().list(filter=filter).execute()            
            
            if projects_under_folder:
                all_projects.extend([p['projectId'] for p in projects_under_folder['projects']])


    def recommendations(self,projectName):
        recommendationsMetadata = self.dynamicTemplate.get("recommendations", "{}").get("recommendationsMetadata", "{}")
        client = recommender.RecommenderClient(credentials=self.credentials)
        zones = self.dynamicTemplate.get("recommendationzones",[])
        recommendationsList = []
        for location in zones:
            parent = client.recommender_path(projectName, location, 'google.compute.instance.MachineTypeRecommender')

            recommendations = client.list_recommendations(parent=parent)        
            recommendlist = MessageToDict(recommendations._pb)
            for recommendation in recommendlist.get("recommendations",[]):
                recommendations = {}
                resoverview = recommendation.get("content",{}).get("overview",{})
                primaryimpact = recommendation.get("primaryImpact",{})
                recommendationType = resoverview.get("recommendedMachineType",{})
                currentType = resoverview.get("currentMachineType",{})
                recommendations["description"] = recommendation.get("description","")
                recommendations["category"] = primaryimpact.get("category","")
                recommendations["resourcename"] = resoverview.get("resourceName","")
                recommendations["resource"] = resoverview.get("resource","")                
                recommendations["recommendedAction"] = resoverview.get("recommendedAction","")
                recommendations["recommendMachineType"] = recommendationType.get("name","")+" memoryMB:"+str(recommendationType.get("memoryMb",0))+" cpus:"+str(recommendationType.get("guestCpus",0))
                recommendations["currentMachineType"] = currentType.get("name","")+" memoryMB:"+str(currentType.get("memoryMb",0))+" cpus:"+str(currentType.get("guestCpus",0))
                recommendations["currencycode"] = recommendation.get("primaryImpact",{}).get("costProjection",{}).get("cost.currencyCode","")
                recommendations["units"] = recommendation.get("primaryImpact",{}).get("costProjection",{}).get("cost",{}).get("units","")
                recommendations["state"] = recommendation.get("stateInfo",{}).get("state","")        
                recommendations["priority"] = recommendation.get("priority","")
                for op_group in recommendation.get("content",{}).get("operationGroups",{}):       
                        recommendations["action"] = op_group.get("operations",[])[0].get("action","")
                        recommendations["resourceType"] =  op_group.get("operations",[])[0].get("resource_type","")
                        recommendations["path"] = op_group.get("operations",[])[0].get("path","")
                        recommendations["value"] = op_group.get("operations",[])[0].get("value",{}).get("string_value","")
                recommendationsList.append(recommendations)
        self.publishToolsData(recommendationsList, recommendationsMetadata)

    #Resource details data collection
    def resourceDetailsDict(self):
        try:
            self.resourceDetailsList = []
            for index, row in self.df.iterrows():
                resourceDetailsDict = {}
                resourceDetailsDict["resourceid"]=""
                if row["resourceglobalname"] == None:
                    if json.loads(row["labels"]) == []:
                        resourceDetailsDict["resourceid"]=""
                    else:
                        for label in json.loads(row["labels"]):
                            if (label["key"] == "resourceid") or (label["key"] == "goog-resource-type"):
                                resourceDetailsDict["resourceid"]= label["value"]
                else:
                    resourceDetailsDict["resourceid"] = row["resourceglobalname"]
                
                if resourceDetailsDict["resourceid"]!="":
                    resourceDetailsDict["servicename"] = row["servicename"]     
                    resourceDetailsDict["accountid"] = row["accountid"]
                    resourceDetailsDict["cloudtype"] = "gcp"  
                    resourceDetailsDict["resourcename"] = resourceDetailsDict["resourceid"]
                    resourceDetailsDict["country"] = row["country"]   
                    resourceDetailsDict["location"] = row["location"]    
                    resourceDetailsDict["region"] = row["region"]    
                    resourceDetailsDict["zone"] = row["zone"]        
                    resourceDetailsDict["resourcetype"] = "NA" 
                    resourceDetailsDict["skuid"] = row["skuid"]
                    resourceDetailsDict["skuname"] = row["skuname"]
                    self.resourceDetailsList.append(resourceDetailsDict)
            
            self.baseLogger.info("resourceDetails length = " + str(len(self.resourceDetailsList)))
           
            resourceDetailsListRmdDupl = pd.DataFrame(self.resourceDetailsList).drop_duplicates().to_dict('records')
            self.baseLogger.info("resourceDetails length after removing duplicates = " + str(len(resourceDetailsListRmdDupl)))    
            
            #Publish to Neo4j via RabbitMQ and InsightEngine
            self.publishToolsData(self.resourceDetailsList, self.resourceDetailsMetadata)
            self.publishToolsData(resourceDetailsListRmdDupl, self.costRelationshipData)
        
        except Exception as e:            
            self.baseLogger.info(e)
        
        
if __name__ == "__main__":
    GcpFinOpsAgent()
