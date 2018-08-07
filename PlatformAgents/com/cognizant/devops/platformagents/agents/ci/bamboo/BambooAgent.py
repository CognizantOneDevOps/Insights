'''
File: build.py
Uses: This script fetch data from Bamboo rest endpoint, extract required fields from API response and create final json file.
        API Hierarchy are:
        - First API Call will fetch all project and plans.
           - Second Api call will fetch all build details of a Plan
Last Modified: 26-Oct-2016
'''
__author__ = "Roopendra Vishwakarma"
__email__ = "roopendra.vishwakarma@cognizant.com"
__status__ = "Development"
__version__ = '0.1'

import sys
if "D:\SDAMetrics\lib" not in sys.path:
    sys.path.append("D:\SDAMetrics\lib")

from configs import Configs
from cornerstone import CornerStoneSSO
from common import Common
from SDALogger import SDALogger
from dateutil.parser import parse

import json, ConfigParser, base64, datetime

class Build():

    def __init__(self):
        self.configs = Configs()
        self.username = self.configs.decrypt(self.configs.readConfig("bamboo", "username"))
        self.password = self.configs.decrypt(self.configs.readConfig("bamboo", "password"))
        self.bambooUrl = self.configs.readConfig("bamboo", "bambooUrl")
        self.restEndPoint = self.configs.readConfig("bamboo", "restEndPoint")
        self.buildMaxResult = self.configs.readConfig("bamboo", "buildMaxResult")
        self.logger = SDALogger("Build")
        self.commonLib = Common()

    def getBambooAppEaiCode(self, output_json, bambooUrl):
        for data in output_json:
            projects = data.get('AtlassianProjects', '')
            for proj in projects.get('linkedProjects'):
                if proj['bambooURL'] == bambooUrl:
                    return data.get('eai','')
        return None

    def getMaxResult(self):

        try:
            # Get Project Size
            apiProjectSize = self.restEndPoint + "latest/project.json?os_authType=basic&max-results=1"
            projectResponse = self.commonLib.excuteApi(apiProjectSize, self.username, self.password)
            totalProject = projectResponse['projects']['size']
            # Get Maximum plan size
            apiPlanSize = self.restEndPoint + "latest/project.json?expand=projects.project&os_authType=basic&max-results=" + str(totalProject)
            allPlans = self.commonLib.excuteApi(apiPlanSize, self.username, self.password)
            allProjects = allPlans['projects']['project']
            maxPlanSize = max(int(d['plans']['size']) for d in allProjects)
            return max(totalProject, maxPlanSize)
        except Exception, e:
            self.commonLib.printException()
            return

    def getData(self):
        action = self.commonLib.parseArgument()
        if action == 'incr':
            startDateString = self.configs.readConfig("bamboo", "incrDataStartDate")
            startDate= parse(startDateString)

        maxPlanSize = self.getMaxResult()

        try:
            # Fetch all Projects and Plan. Pass max-results value from getMaxResult() Method
            finalProjectPlansApi = self.restEndPoint + "latest/project.json?expand=projects.project.plans&os_authType=basic&max-results=" + str(maxPlanSize)
            allProjectsPlans = self.commonLib.excuteApi(finalProjectPlansApi, self.username, self.password)
            allProjectsPlansJson = allProjectsPlans['projects']['project']

            # Get Cornerstone data for EAI linking with application.
            cs = CornerStoneSSO()
            cornerstoneData = cs.getCornerStoneData()

            data = []
            for projects in allProjectsPlansJson:
                projectsJson = {}
                projectsJson['projectName'] = projects['name']
                projectsJson['projectKey'] = projects['key']
                projectsJson['planSize'] = projects['plans']['size']

                bambooUrl = self.bambooUrl + projects['key']
                eai = ""
                if cornerstoneData:
                    eai = self.getBambooAppEaiCode(cornerstoneData, bambooUrl)

                projectsJson['eai'] = eai
                print('Fetching data for (%s) project ' %(projects['name']))
                self.logger.info('Fetching data for (%s) project ' %(projects['name']))

                plans = projects['plans']['plan']
                planData = []
                for plan in plans:
                    bambooPlansJson = {}
                    planKey = plan['key']
                    bambooPlansJson['planKey'] = planKey
                    bambooPlansJson['planName'] = plan['name']
                    planData.append(bambooPlansJson)
                    '''
                    # Comment this block , Becasue bamboo rest api has issue in fetching build size from max-results=1
                    buildSizeApiCall  = self.restEndPoint + "latest/result/" + planKey + ".json?os_authType=basic&max-results=1"
                    buildSizeResult = self.commonLib.excuteApi(buildSizeApiCall, True)
                    buildSize = buildSizeResult['results']['size']
                    '''
                    buildSize = self.buildMaxResult
                    buildData = []

                    planApiCall  = self.restEndPoint + "latest/result/" + planKey + ".json?expand=results.result&os_authType=basic&max-results=" + str(buildSize)
                    planBuildsJson = self.commonLib.excuteApi(planApiCall, self.username, self.password, True)
                    builds = planBuildsJson['results']['result']
                    for build in builds:
                        buildJson = {}
                        buildJson['buildNumber'] = build['buildNumber']
                        buildJson['buildKey'] = build['key']
                        buildJson['buildState'] = build['buildState']
                        buildJson['buildId'] = build['id']
                        buildJson['buildNumber'] = build['buildNumber']
                        buildJson['buildPlanKey'] = build['planResultKey']['entityKey']['key']
                        buildJson['buildStartedTime'] = build['buildStartedTime']
                        buildJson['buildCompletedDate'] = build['buildCompletedDate']
                        buildJson['buildDuration'] = build['buildDuration']
                        if action == "incr":
                            parseBuildStartDate = parse(build['buildStartedTime'])
                            if parseBuildStartDate > startDate:
                               buildData.append(buildJson)
                        else:
                            buildData.append(buildJson)
                    bambooPlansJson['builds']  =  buildData

                projectsJson['plans']  =  planData
                data.append(projectsJson)

            self.commonLib.writeJsonFile(action, data, self.configs.readConfig("bamboo", "jsonFilePrefix"))
        except Exception, e:
            self.commonLib.printException()
            return

if __name__ == "__main__":
    buildObj = Build()
    data =  buildObj.getData()