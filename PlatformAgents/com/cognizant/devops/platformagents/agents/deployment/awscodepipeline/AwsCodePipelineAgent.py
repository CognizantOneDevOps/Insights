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

from __future__ import unicode_literals
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
from dateutil import parser
import boto3
import time
import json, ast

class AwsCodePipelineAgent(BaseAgent):
    def process(self):
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%S')
        since = self.tracking.get('lastupdated', None)
        if since == None:
            lastUpdated = startFrom
        else:
            lastUpdated = since
            since = parser.parse(since)
            since = since.strftime('%Y-%m-%dT%H:%M:%S')
            pattern = '%Y-%m-%dT%H:%M:%S'
            since = int(time.mktime(time.strptime(since, pattern)))
        accesskey = self.config.get("awsAccesskey", '')
        secretkey = self.config.get("awsSecretkey", '')
        regionName = self.config.get("awsRegion", '')
        client = boto3.client('codepipeline',
                              aws_access_key_id=accesskey,
                              aws_secret_access_key=secretkey,
                              region_name=regionName)
        tracking_data = []
        injectData = {}
        pipeline = client.list_pipelines(
        )
        for names in pipeline["pipelines"]:
            response = client.get_pipeline_state(
                name=names["name"]
            )
            date = str(response['created'])
            date = parser.parse(date)
            date = date.strftime('%Y-%m-%dT%H:%M:%S')
            pattern = '%Y-%m-%dT%H:%M:%S'
            date = int(time.mktime(time.strptime(date, pattern)))
            if since == None or date > since:
                injectData['pipelineName'] = str(response['pipelineName'])
                injectData['jobName'] = str(response['stageStates'][1]['stageName'])
                injectData['status'] = str(response['stageStates'][0]['actionStates'][0]['latestExecution']['status'])
                summary = response['stageStates'][0]['actionStates'][0]['latestExecution']["errorDetails"]
                injectData['summary'] = summary['message']
                injectData['createTime'] = str(response['created'])
                injectData['pipelineTime'] = response['created'].strftime('%Y-%m-%dT%H:%M:%SZ')
                start = str(response['stageStates'][0]['actionStates'][0]['latestExecution']['lastStatusChange'])
                injectData['pipelineStartTime'] = start
                string = ast.literal_eval(json.dumps(injectData))
                tracking_data.append(string)
                seq = [x['createTime'] for x in tracking_data]
                fromDateTime = max(seq)
                fromDateTime = parser.parse(fromDateTime)
                fromDateTime = fromDateTime.strftime('%Y-%m-%dT%H:%M:%S')
        if tracking_data != []:
            self.publishToolsData(tracking_data)
            self.tracking["lastupdated"] = fromDateTime
            self.updateTrackingJson(self.tracking)

if __name__ == "__main__":
    AwsCodePipelineAgent()
