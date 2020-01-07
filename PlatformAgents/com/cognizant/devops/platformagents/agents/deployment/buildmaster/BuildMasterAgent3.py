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


from ....core.BaseAgent3 import BaseAgent
from dateutil import parser
import json

class BuildMasterAgent(BaseAgent):
    def process(self):
        startFrom = self.config.get("StartFrom", '')
        apiKey = str(self.config.get("apiKey", ''))
        baseurl = self.config.get("endpoint", '')
        userid = self.getCredential("userid")
        passwd = self.getCredential("passwd")
        appIds = self.config.get('dynamicTemplate', {}).get('applicationIds', [])
        timeStampFormat = self.config.get('timeStampFormat')
        responseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)

        startFrom = self.createTimeStamp(startFrom, timeStampFormat)
        for appId in appIds:
            allReleases = []
            individualReleaseUrl = baseurl + "Builds_GetBuilds?API_Key=" + apiKey + "&Application_Id=" + appId + "&ReleaseStatus_Name=Active"
            releases = self.getResponse(individualReleaseUrl, 'GET', userid, passwd, None)
            for release in releases:
                release_name = release.get('Release_Number', '')
                if release_name not in allReleases:
                    allReleases.append(release_name)
            for release in allReleases:
                individualReleaseUrl = baseurl + "Builds_GetBuilds?API_Key=" + apiKey + "&Application_Id=" + appId + "&ReleaseStatus_Name=Active&Release_Number=" + release
                releaseRelatedDetails = self.getResponse(individualReleaseUrl, 'GET', userid, passwd, None)
                publishToolData = []
                track = appId + "-" + release
                for detailData in releaseRelatedDetails:
                    try:
                        since = self.tracking.get(track,None)
                        if since is not None:
                            since = parser.parse(since)
                            since = since.strftime(timeStampFormat)
                        if 'Current_ExecutionStart_Date' in detailData:
                            date = detailData.get('Current_ExecutionStart_Date')
                            date = parser.parse(date)
                            date = self.createTimeStamp(detailData.get('Current_ExecutionStart_Date', ''), timeStampFormat)
                            if since is None or date > since:
                                injectData = {}
                                injectData['createTime'] = date                                
                                publishToolData += self.parseResponse(responseTemplate, detailData, injectData)
                    except Exception as ex:
                        self.tracking["latestError"] = ex
                        pass
                if len(releaseRelatedDetails) > 0:
                    fromDateTime = self.createTimeStamp(releaseRelatedDetails[0].get('Current_ExecutionStart_Date', ''), timeStampFormat)
                if len(publishToolData) > 0:
                    self.tracking[track] = fromDateTime
                    self.publishToolsData(publishToolData)
                    self.updateTrackingJson(self.tracking)
    def createTimeStamp(self, time, format):
        time = parser.parse(time)
        return time.strftime(format)
if __name__ == "__main__":
    BuildMasterAgent()
