#-------------------------------------------------------------------------------
# Copyright 2024 Cognizant Technology Solutions
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
Created on April 12, 2024

@author: 146638, 911242
'''

from datetime import datetime as dateTime, time
import time
import boto3
from ....core.BaseAgent3 import BaseAgent
                
class AwsResilienceHubAgent(BaseAgent):
    @BaseAgent.timed
    def process(self):
        self.baseLogger.info("Inside awsResilienceHub Agent process")
        try:
            self.timeStampNow      = lambda: dateTime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ")
            self.dynamicTemplate   = self.config.get("dynamicTemplate", {})
            self.metaData          = self.dynamicTemplate.get("metaData", {})
            self.appAssessmentRelationMetaData = self.dynamicTemplate.get("relationMetadata", {})
            self.historicalBatchCount = self.config.get("historicalBatchCount", 0)
            self.currentBatchCount = self.config.get("currentBatchCount", 0)

            self.start = self.tracking.get('lastFetchTime',None)

            if not self.tracking : 
                self.tracking  = { 'startTime': '',
                                   'isHistDataInSync': 'No' 
                                    }
            self.processResilienceData()
        except Exception as e:
            self.baseLogger.error(e)

    def convertTimeToEpoh(self, pTime):
        timePattern = '%Y-%m-%dT%H:%M:%S'
        regularTimeFormat = pTime.strftime('%Y-%m-%dT%H:%M:%S')
        epochTime = int(time.mktime(time.strptime(regularTimeFormat, timePattern)))
        return epochTime
    
    def processResilienceData(self):
        if ( self.tracking['isHistDataInSync'] is None or self.tracking['isHistDataInSync'] == "No"):
            self.batch = self.historicalBatchCount
            self.tracking['isHistDataInSync'] = "No"
        else:
            self.batch = self.currentBatchCount

        self.accessKey = self.getCredential("accessKey")
        self.secretKey = self.getCredential("secretKey")
        self.regionName = self.getCredential("regionName")
        
        self.client      = boto3.client('resiliencehub',
                                        aws_access_key_id=self.accessKey,
                                        aws_secret_access_key=self.secretKey,
                                        region_name=self.regionName)
        self.appsList    = self.client.list_apps()
        self.fetchResilienceHubData()
    
    def  fetchResilienceHubData(self):
        self.baseLogger.info("Fetching Historical Data")
        try:
            assessmentData = []
            injectData = dict()
            appData = []
            appResponseTemplate = self.dynamicTemplate.get("appResponseTemplate", {})
            assessmentResponseTemplate = self.dynamicTemplate.get("assessmentResponseTemplate", {})
            for app in self.appsList["appSummaries"]:
                appArn = app.get('appArn', None)
                appName = app.get('name', None)
                appParams = appArn.split(':')
                partition = appParams[0]
                service = appParams[2]
                region = appParams[3]
                accountId = appParams[4]
                resources = appParams[5]
                resource = resources.split('/')
                resourceType = resource[0]
                resourceId = resource[1]
                lastAppComplianceEvaluationTime = self.convertTimeToEpoh(app.get('lastAppComplianceEvaluationTime', None))
                #Injecting the app properties in injectData to pass it to appData
                injectData = {
                    "lastAppComplianceEvaluationTime" : lastAppComplianceEvaluationTime,
                    "partition": partition,
                    "region" : region,
                    "accountId" : accountId,
                    "service":service,
                    "resourceType":resourceType,
                    "resourceId":resourceId
                    }
                appData = self.parseResponse(appResponseTemplate, app, injectData)
                tillBatchComplete = True
                iteration = 0
                nextToken = None
                #Fetch a historical sync till all the records are fetched by batch
                while (tillBatchComplete):
                    if( nextToken is None ):
                        assessments = self.client.list_app_assessments(
                        appArn = appArn,
                        maxResults = self.batch,
                        reverseOrder = True)
                    else:
                        assessments = self.client.list_app_assessments(
                        appArn = appArn,
                        maxResults = self.batch,
                        reverseOrder = True,
                        nextToken = nextToken )
                    
                    for assessment in assessments[ 'assessmentSummaries' ]:
                        assessmentName = assessment.get('assessmentName', None)
                        startTime = self.convertTimeToEpoh(assessment.get('startTime', None))
                        endTime = self.convertTimeToEpoh(assessment.get('endTime', None))
                        injectData = {
                            "appName" : appName, #Have to inject appName here, because its a constraint in relationship
                            "startTime" : startTime,
                            "endTime" : endTime
                        }
                        self.baseLogger.info(str(iteration) + " " + appName + " " + assessmentName + " " + str(startTime))
                        iteration += 1
                        assessmentData+=self.parseResponse(assessmentResponseTemplate,assessment, injectData)

                    if('nextToken' in assessments):
                        nextToken = assessments.get('nextToken', None)
                        self.baseLogger.info("<Batch Started>" )
                    else:
                        self.baseLogger.info("<End of Batch")
                        tillBatchComplete = False
                    iteration += 1

                    if appData and assessmentData:
                        self.publishData("app", appData)
                        self.publishData("assessment", assessmentData)
                        appData = []
                        assessmentData = []
            if( iteration ):
                self.tracking['startTime'] = startTime
                self.tracking['isHistDataInSync']  = "Yes"
                self.updateTrackingJson(self.tracking)
        except Exception as e:
            self.baseLogger.error(e)
    
    def publishData(self, event, dataToPublish):
        """Publish the data based on the event and create relationship"""
        if dataToPublish:
            if event=='assessment':
                relationShipMetaData = self.appAssessmentRelationMetaData.get("appAssessment", {})
                self.publishToolsData(dataToPublish, relationShipMetaData)
            else:
                self.publishToolsData(dataToPublish, self.metaData.get(event, {}))
        else:
            self.baseLogger.info("No Data to publish")

if __name__ == "__main__":
    AwsResilienceHubAgent()
