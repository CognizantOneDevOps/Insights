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



class AwsCodeDeployAgent(BaseAgent):
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
        client = boto3.client('codedeploy',
                              aws_access_key_id=accesskey,
                              aws_secret_access_key=secretkey,
                              region_name=regionName)
        getlist = []
        tracking_data = []
        getApps = client.list_applications(
        )
        for app in getApps["applications"]:
            depGroups = client.list_deployment_groups(
                applicationName=app
            )
            for groups in depGroups["deploymentGroups"]:
                totalDeploys = client.list_deployments(
                    applicationName=app,
                    deploymentGroupName=groups
                )
                response = client.list_deployments(
                    applicationName=app,
                    deploymentGroupName=groups
                )
                getlist = [[str(deployments) for deployments in response['deployments']] for deployments in response]
                getlist = [item for items in getlist for item in items]
                deployId = list(set(getlist))
                for n in deployId:
                    injectData = {}
                    string = {}
                    deploy = client.get_deployment(
                        deploymentId=n
                    )
                    date = str(deploy['deploymentInfo']['createTime'])
                    date = parser.parse(date)
                    date = date.strftime('%Y-%m-%dT%H:%M:%S')
                    pattern = '%Y-%m-%dT%H:%M:%S'
                    date = int(time.mktime(time.strptime(date, pattern)))
                    if since == None or date > since:
                        injectData['status'] = str(deploy['deploymentInfo']['status'])
                        injectData['applicationName'] = str(deploy['deploymentInfo']['applicationName'])
                        injectData['deploymentId'] = str(deploy['deploymentInfo']['deploymentId'])
                        injectData['deployType'] = str(deploy['deploymentInfo']['deploymentStyle']['deploymentType'])
                        injectData['deploymentGroupName'] = str(deploy['deploymentInfo']['deploymentGroupName'])
                        injectData['createTime'] = str(deploy['deploymentInfo']['createTime'])
                        start = deploy['deploymentInfo']['createTime'].strftime('%Y-%m-%dT%H:%M:%SZ')
                        injectData['deploymentTime'] = start
                        complete = str(deploy['deploymentInfo']['completeTime'])
                        injectData['deploymentEndTime'] = complete
                        injectData['lastUpdated'] = lastUpdated
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
    AwsCodeDeployAgent()
