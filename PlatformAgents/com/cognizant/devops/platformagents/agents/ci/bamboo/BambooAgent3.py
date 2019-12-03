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
Created on 12 April 2017

@author: 476693
'''
from ....core.BaseAgent3 import BaseAgent
from datetime import datetime
import json
class BambooAgent(BaseAgent):
    def process(self):        
        BaseUrl = self.config.get("baseUrl", None)
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        enableAllBranches = self.config.get("enableAllBranches", None)
        getCollectionUrl = BaseUrl + "rest/api/latest/"       
        buildsURL = getCollectionUrl +"plan.json"
        builds = self.getResponse(buildsURL,'GET', UserID, Passwd, None,None)        
        responseTemplate = self.getResponseTemplate()
        build_plan = builds["plans"]["plan"]
        for plan_Individual in build_plan:            
            plan_Individual_Key =  plan_Individual.get('key')  
            alllBranches = []
            jsonObj = {'branchKey': plan_Individual_Key, 'branchName':'master'}
            alllBranches.append(jsonObj)
            if enableAllBranches:
                listAllBranchesUrl = getCollectionUrl + "plan/" + plan_Individual_Key +"/branch.json?max-result=1000"
                branch_list_data = self.getResponse(listAllBranchesUrl,'GET', UserID, Passwd, None,None)
                branch_list_data = branch_list_data.get('branches', {}).get('branch', [])
                for branch in branch_list_data:
                    branchName = branch.get('name')
                    branchKey = branch.get('key')
                    jsonObj = {'branchKey': branchKey, 'branchName':branchName}
                    alllBranches.append(jsonObj)
            for branch in alllBranches:
                branch_name = branch.get('branchName')
                branch_key = branch.get('branchKey')
                plan_Individual_Key = branch_key
                lastBuildNo = self.tracking.get(plan_Individual_Key,None) 
                if lastBuildNo is None:
                    lastBuildNo = 0
                plan_size = 25
                start_index = 0
                Build_running_Complete = True
                plan_Individual_Url = getCollectionUrl + "result/" + plan_Individual_Key +".json?start-index=" + str(start_index) + "&max-result=25"
                plan_Individual_collection = self.getResponse(plan_Individual_Url,'GET', UserID, Passwd, None,None)
                if len(plan_Individual_collection["results"]["result"]) == 0:
                    self.tracking[plan_Individual_Key] = 0
                    continue
                self.tracking[plan_Individual_Key] = plan_Individual_collection["results"]["result"][0]["buildNumber"]
                while Build_running_Complete:
                    data = []
                    for plan_Individual_result_length in range (plan_Individual_collection["results"]["size"]):
                        plan_Individual_result = plan_Individual_collection["results"]["result"][plan_Individual_result_length]
                        plan_Individual_result_key =  plan_Individual_result.get("key")            
                    if len(plan_Individual_collection["results"]["result"]) >0:
                        if lastBuildNo < plan_Individual_collection["results"]["result"][0]["buildNumber"]:
                            for plan_Individual_result_length in range(lastBuildNo,plan_Individual_collection["results"]["size"]):
                                injectData = {}
                                plan_Individual_result = plan_Individual_collection["results"]["result"][plan_Individual_result_length]
                                plan_Individual_result_key =  plan_Individual_result.get("key")
                                plan_Individual_result_url = getCollectionUrl + "result/" + plan_Individual_result_key +".json"
                                plan_Individual_result_details = self.getResponse(plan_Individual_result_url,'GET', UserID, Passwd, None,None)
#                                 utc_time = datetime.strptime(plan_Individual_result_details["buildCompletedTime"].split(".")[0], "%Y-%m-%dT%H:%M:%S")
#                                 epoch_time = (utc_time - datetime(1970, 1, 1)).total_seconds()
#                                 injectData['buildCompletedTime'] = plan_Individual_result_details["buildCompletedTime"].split(".")[0]
#                                 injectData['buildCompletedTimeEpoch'] = epoch_time
#                                 injectData['inSightsTimeX'] = plan_Individual_result_details["buildCompletedTime"].split(".")[0]
#                                 injectData['inSightsTime'] = epoch_time
                                injectData['branchName'] = branch_name
                                injectData['branchKey'] = branch_key
                                data += self.parseResponse(responseTemplate, plan_Individual_result_details, injectData)
                    start_index = start_index + plan_size
                    plan_Individual_Url = getCollectionUrl + "result/" + plan_Individual_Key +".json?start-index=" + str(start_index) + "&max-result=25"
                    plan_Individual_collection = self.getResponse(plan_Individual_Url,'GET', UserID, Passwd, None,None)
                    if plan_Individual_collection["results"]["size"] > 0:
                        Build_running_Complete = True
                    else:
                        Build_running_Complete = False
                    if len(data) > 0:
                        self.publishToolsData(data)
                self.updateTrackingJson(self.tracking)     
if __name__ == "__main__":
    BambooAgent()


