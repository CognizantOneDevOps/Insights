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
from datetime import datetime, timedelta
import boto3
import time
import json, ast


class AwsCodeBuildAgent(BaseAgent):
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
        tracking_data = []
        accesskey = self.config.get("awsAccesskey", '')
        secretkey = self.config.get("awsSecretkey", '')
        regionName = self.config.get("awsRegion", '')
        client = boto3.client('codebuild',
                              aws_access_key_id=accesskey,
                              aws_secret_access_key=secretkey,
                              region_name=regionName)
        projects = client.list_projects(
            sortBy='NAME',
            sortOrder='ASCENDING'
        )
        for projectName in projects["projects"]:
            builds = client.list_builds_for_project(
                projectName=projectName,
                sortOrder='ASCENDING'
            )
            for build in builds["ids"]:
                buildDetails = client.batch_get_builds(
                    ids=[
                        build
                    ]
                )
                for buildDetail in buildDetails["builds"]:
                    buildDet = {}
                    string = {}
                    date = str(buildDetail["startTime"])
                    date = parser.parse(date)
                    date = date.strftime('%Y-%m-%dT%H:%M:%S')
                    pattern = '%Y-%m-%dT%H:%M:%S'
                    date = int(time.mktime(time.strptime(date, pattern)))
                    if since == None or date > since:
                        buildDet["buildId"] = str(buildDetail["id"])
                        buildDet["projectName"] = str(buildDetail["projectName"])
                        buildDet["buildComplete"] = str(buildDetail["buildComplete"])
                        buildDet["currentPhase"] = str(buildDetail["currentPhase"])
                        buildDet["createTime"] = str(buildDetail["phases"][-1]["startTime"])
                        startTime = str(buildDetail["startTime"])
                        buildDet["buildStartTime"] = startTime
                        endTime = str(buildDetail["endTime"])
                        buildDet["buildTime"]=buildDetail["endTime"].strftime('%Y-%m-%dT%H:%M:%SZ')
                        buildDet["buildEndTime"] = endTime
                        string = ast.literal_eval(json.dumps(buildDet))
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
    AwsCodeBuildAgent()
