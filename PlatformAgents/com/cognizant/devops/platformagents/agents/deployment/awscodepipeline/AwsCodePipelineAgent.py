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
Created on Jun 13, 2019
@author: 368419
'''

from __future__ import unicode_literals
from ....core.BaseAgent import BaseAgent
from dateutil import parser
import boto3
import time
import json, ast


class AwsCodePipelineAgent(BaseAgent):
    def process(self):       
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%S')
        
        accesskey = self.getCredential("awsAccesskey")
        secretkey = self.getCredential("awsSecretkey")
        regionName = self.getCredential("awsRegion")
        client = boto3.client('codepipeline',
                              aws_access_key_id=accesskey,
                              aws_secret_access_key=secretkey,
                              region_name=regionName)

        response = client.list_pipelines()
        length = len(response['pipelines'])
        pipeline = []
        tracking_data = [] 
        for n in range(0,length):
            res = str(response['pipelines'][n]['name'])
            pipeline.append(res)
        pipeline=list(set(pipeline))
        
        for value in pipeline:            
            response = client.list_pipeline_executions(
                 pipelineName=value
                 )           
                       
            since = self.tracking.get(value,None)
            
            if since == None:
                lastUpdated = startFrom
            else:
                since = parser.parse(since)
                since = since.strftime('%Y-%m-%dT%H:%M:%S')
                lastUpdated = since
           
            if len(response['pipelineExecutionSummaries']) > 0:            
                injectData = {}                                               
                for response in response['pipelineExecutionSummaries']:  
                   date = str(response['lastUpdateTime'])
                   date = parser.parse(date)
                   date = date.strftime('%Y-%m-%dT%H:%M:%S')                                      
                   if since == None or date > since:
                       injectData['pipelineName'] = value
                       injectData['status'] = str(response['status'])
                       injectData['jobId'] = str(response['pipelineExecutionId'])
                       injectData['createTime'] = str(response['startTime'])
                       start = str(response['startTime'])
                       start = parser.parse(start)
                       start_e = start.strftime('%Y-%m-%dT%H:%M:%S')
                       start_f = start.strftime('%Y-%m-%d')
                       injectData['startTime'] = start_f
                       pattern = '%Y-%m-%dT%H:%M:%S'
                       epoch = int(time.mktime(time.strptime(start_e,pattern)))
                       injectData['startTimeepoch'] = epoch                   
                       injectData['lastUpdateTime'] = date
                       pattern = '%Y-%m-%dT%H:%M:%S'
                       date = int(time.mktime(time.strptime(date,pattern)))
                       injectData['lastUpdateTimeepoch'] = date
                       string = ast.literal_eval(json.dumps(injectData))
                       tracking_data.append(string)
                       seq = [x['lastUpdateTime'] for x in tracking_data]
                       lastUpdated = max(seq)                     
            self.tracking[value] = lastUpdated  
                      
            if tracking_data!=[]:
                self.publishToolsData(tracking_data)
                self.updateTrackingJson(self.tracking)
if __name__ == "__main__":
    AwsCodePipelineAgent()
