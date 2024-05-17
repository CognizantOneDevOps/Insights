# -------------------------------------------------------------------------------
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
# -------------------------------------------------------------------------------
'''
Created on Mar 20th, 2024

@author: 911055
'''

import datetime
from datetime import datetime as dateTime
from dateutil import parser
from datetime import datetime
from ....core.BaseAgent3 import BaseAgent


class CheckmarxAgent3(BaseAgent):

    @BaseAgent.timed
    def process(self):
        self.baseLogger.info("Inside Checkmarx Agent process")
        self.baseEndPoint = self.config.get("baseEndPoint", "")
        self.dynamicTemplate = self.config.get("dynamicTemplate", {})
        self.metaData = self.dynamicTemplate.get("metaData", {})
        self.projectScansRelationMetaData = self.dynamicTemplate.get("relationMetadata", {})
        self.processProjectScan()

    def processProjectScan(self):
        self.baseLogger.debug("***Inside processing and project scan***")
        projectResponseTemplate = self.dynamicTemplate.get("projectResponseTemplate", {})
        scanResponseTemplate = self.dynamicTemplate.get("scanResponseTemplate", {})
        selectedProjects = self.dynamicTemplate.get("selectedProjects", None)
        access_token = None
        projectData = []
        scanData = []
        projectArray = []
        projectCount = 0
        try:
            access_token = self.authenticate()
            if access_token:
                # Get list of projects
                projects = self.getProjects(access_token)
                if projects:
                    for project in projects:
                        projectCount += 1
                        project_id = project.get("id", "")
                        project_name = project.get("name", "")
                        if len(selectedProjects) > 0 and project_name not in selectedProjects:
                            continue
                        # Get the project_id's of the selectedProjects
                        if project_name in selectedProjects:
                            projectData += self.parseResponse(projectResponseTemplate, project)
                            projectDetails = {
                                "project_id": project_id,
                                "project_name": project_name
                            }
                            projectArray.append(projectDetails) #The projectArray contains only the details of the projects in the selectedProjects
                        if len(projectArray) == 0:
                            self.baseLogger.info("Projects in selected projects is not present in project response")
                        # If we fetched all the projectDetails in the selectedProjects break it
                        if len(selectedProjects) == len(projectArray):
                            break
                    for projectInfo in projectArray:
                        projectID = projectInfo.get("project_id", "")
                        projectName = projectInfo.get("project_name", "")
                        projectTracking = self.tracking.get(str(projectID), {})
                        # Get details of all runs scan
                        all_scans = self.getAllScans(access_token, projectID, projectTracking)
                        if all_scans:
                            # If scans available for the project, collect projectResponse in projectData
                            for scan in all_scans:
                                scanId = scan.get("id", "")
                                scanFinishedOn = scan.get("dateAndTime", {}).get("finishedOn", "").split(".")[0]
                                injectData = {
                                    "scanId": scanId,
                                    "projectId": projectID,
                                    "projectName": projectName,
                                    "scanFinishedOn": scanFinishedOn
                                }
                            # Get issues occurred in the scan
                                issues = self.getScanIssues(access_token, scanId)
                                if issues:
                                    issues.update(injectData)
                                    scanData += self.parseResponse(scanResponseTemplate, issues)
                        if projectCount == 100:
                            self.publishProjectScanData(projectData, scanData)
                            projectCount = 0
                            projectData = []
                            scanData = []
                    self.publishProjectScanData(projectData, scanData)
        except Exception as e:
            self.baseLogger.info("Exception Occurred in projects: ", str(e))
            self.publishHealthDataForExceptions(e)

    def publishProjectScanData(self, projectDataList, scanDataList):
        insightTimeX = self.dynamicTemplate.get("scans", {}).get("insightsTimeXFieldMapping", None)
        timestamp = insightTimeX.get("timefield", None)
        timeformat = insightTimeX.get("timeformat", None)
        isEpoch = insightTimeX.get("isEpoch", False)
        if projectDataList:
            self.publishToolsData(projectDataList, self.metaData.get("project", {}))
            self.baseLogger.info("project detail published")
        if scanDataList:
            relationShipMetaData = self.projectScansRelationMetaData.get("projectScan", {})
            self.publishToolsData(scanDataList, relationShipMetaData, timestamp, timeformat, isEpoch, True)
            self.baseLogger.info("scan detail published")

    def getProjects(self, access_token):
        """
        Get projects using the provided access token.
        :param access_token: Access token for authentication.
        :return: projects response or None if failed.
        """
        self.baseLogger.debug("***Inside fetching projects response***")
        response = None
        projects_url = self.baseEndPoint + '/cxrestapi/help/projects'
        headers = {'Authorization': 'Bearer ' + access_token}
        try:
            response = self.getResponse(projects_url, 'GET', None, None, None, reqHeaders=headers)
            return response
        except Exception as e:
            self.baseLogger.info("Exception Occurred in getProjects: ", str(e))
            self.publishHealthDataForExceptions(e)

    def authenticate(self):
        self.baseLogger.debug("***Inside fetching access token***")
        # Get the access token
        authUrl = self.baseEndPoint + '/CxRestAPI/auth/identity/connect/token'
        payload = {
            'client_id': self.config.get("clientId", ""),
            'client_secret': self.config.get("clientToken", ""),
            'grant_type': "password",
            'username': self.config.get("userName", ""),
            'password': self.config.get("userToken", "")
            }
        response = None
        try:
            response = self.getResponse(authUrl, "POST", None, None, payload)
            return response.get('access_token', "")
        except Exception as ex:
            self.baseLogger.info("Authentication failed ", str(ex))
            self.publishHealthDataForExceptions(ex)

    def getAllScans(self, access_token, project_id, projectTracking):
        self.baseLogger.debug("Inside processing scans Method ", str(project_id))
        try:
            scanFetchCount = self.config.get("scanFetchCount")
            scans_url = self.baseEndPoint + f'/cxrestapi/help/sast/scans?projectId={project_id}'
            last_scan_id = projectTracking.get("lastScanID", None)

            # If last_scan_id is present means fetch the particular last N data, because all the scans of the selectedProjects were fetched already
            if last_scan_id:
                scans_url += "&last="+ str(scanFetchCount)
            headers = {'Authorization': 'Bearer ' + access_token}
            scans = self.getResponse(scans_url, 'GET', None, None, None, reqHeaders=headers)
            scanData = []
            startFrom = None
            if scans:
                if not last_scan_id:
                    startFromStr = self.config.get("startFrom", "")
                    startFrom = parser.parse(startFromStr, ignoretz=True)
                else:
                    startFrom = None
                    # Get the last N new scans by using the last_scan_id index which tracked and fetched earlier
                    last_scan_array = [scan for scan in scans if str(scan.get("id")) == last_scan_id]
                    if len(last_scan_array) > 0:
                        last_scan = last_scan_array[0]
                        last_scan_index = scans.index(last_scan)
                        scans = scans[0:last_scan_index+1]
                for index in range(0, len(scans)):
                    scan = scans[index]
                    if scan['dateAndTime'] and scan['dateAndTime']['finishedOn']:
                        last_scan_date = datetime.strptime(
                            scan['dateAndTime']['finishedOn'].split(".")[0], '%Y-%m-%dT%H:%M:%S')
                        # lastScanDateAtEpoch = self.getRemoteDateTime(last_scan_date).get('epochTime')
                        if startFrom and startFrom > last_scan_date:
                            break
                        scanData.append(scan)
                    if index == 0:
                        # Store the last finished scan id in the tracking
                        projectTracking["lastScanID"] = str(scan.get("id"))
                        self.tracking[str(project_id)] = projectTracking
                        self.updateTrackingJson(self.tracking)
            else:
                self.baseLogger.info("No scans available: ", scans.text)
            return scanData
        except Exception as e:
            self.baseLogger.info("Exception Occurred in last scans ", str(e))
            self.publishHealthDataForExceptions(e)

    def getScanIssues(self, access_token, scan_id):
        """
        Get scan issues using the provided access token and scan ID.
        :param access_token: Access token for authentication.
        :param scan_id: ID of the scan.
        :return: Scan issues response or None if failed.
        """
        self.baseLogger.debug("***Inside fetching scan issues***")
        response = None
        issues_url = f"{self.baseEndPoint}/cxrestapi/help/sast/scans/{scan_id}/resultsStatistics"
        headers = {"Authorization": f"Bearer {access_token}"}
        try:
            response = self.getResponse(
                issues_url, 'GET', None, None, None, reqHeaders=headers)
            return response
        except Exception as e:
            self.baseLogger.info("Exception Occurred in getScanIssues: ", str(e))
            self.publishHealthDataForExceptions(e)


if __name__ == "__main__":
    CheckmarxAgent3()
