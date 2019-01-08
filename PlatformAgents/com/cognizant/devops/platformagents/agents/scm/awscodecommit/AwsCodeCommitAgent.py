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


import boto3
import time
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
from datetime import datetime, timedelta


class AwsCodeCommitAgent(BaseAgent):
    def process(self):
        data = []
        accesskey = self.config.get("awsAccesskey", '')
        secretkey = self.config.get("awsSecretkey", '')
        regionName = self.config.get("awsRegion", '')
        client = boto3.client('codecommit',
                              aws_access_key_id=accesskey,
                              aws_secret_access_key=secretkey,
                              region_name=regionName)
        repoList = client.list_repositories(
            sortBy='repositoryName',
            order='ascending'
        )
        for repos in repoList["repositories"]:
            repositoryName = repos["repositoryName"]
            branchList = client.list_branches(
                repositoryName=repos["repositoryName"]
            )
            trackingDetails = self.tracking.get(repos["repositoryName"], None)
            if trackingDetails is None:
                trackingDetails = {}
                self.tracking[repos["repositoryName"]] = trackingDetails
                self.updateTrackingJson(self.tracking)
            for branch in branchList["branches"]:
                getBranch = client.get_branch(
                    repositoryName=repos["repositoryName"],
                    branchName=branch
                )
                if branch not in trackingDetails:
                    trackingDetails[branch] = {}
                    self.updateTrackingJson(self.tracking)
                commitid = getBranch["branch"]["commitId"]
                ncommitId = getBranch["branch"]["commitId"]
                while len(commitid) > 0:
                    if trackingDetails[branch] != {}:
                        if trackingDetails[branch]["commitId"] == commitid:
                            break
                    ncommit = ""
                    getCommit = client.get_commit(
                        repositoryName=repos["repositoryName"],
                        commitId=commitid
                    )
                    epochTimes = getCommit["commit"]["author"]["date"]
                    epochTime, dummy = epochTimes.split('+', 1)
                    commList = {}
                    dateTime = time.strftime('%Y-%m-%dT%H:%M:%SZ',  time.gmtime(float(epochTime)))
                    commList["commitTime"] = dateTime
                    commList["commitId"] = getCommit["commit"]["commitId"]
                    commList["commitName"] = getCommit["commit"]["author"]["name"]
                    commList["commitMessage"] = getCommit["commit"]["message"]
                    commList["repoName"] = repositoryName
                    commList["branchName"] = branch
                    data.append(commList)
                    pcommit = getCommit["commit"]
                    if len(pcommit["parents"]) != 0:
                        ncommit = ', '.join(pcommit["parents"])
                    commitid = ncommit
                trackingDetails[branch] = {"commitId": ncommitId}
                self.updateTrackingJson(self.tracking)
        if len(data) > 0:
            self.publishToolsData(data)


if __name__ == "__main__":
    AwsCodeCommitAgent()
