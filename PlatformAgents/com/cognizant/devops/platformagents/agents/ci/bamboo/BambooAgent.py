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

@author: 446620
'''
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent

class BambooAgent(BaseAgent):
    def process(self):        
        BaseUrl = self.config.get("baseUrl", None)
        UserID = self.config.get("userid", None)
        Passwd = self.config.get("passwd", None)
        data = []
        getCollectionUrl = BaseUrl + "rest/api/latest/"       
        buildsURL = getCollectionUrl +"plan.json"
        builds = self.getResponse(buildsURL,'GET', UserID, Passwd, None,None)        
        responseTemplate = self.getResponseTemplate()
        build_plan = builds["plans"]["plan"]
        for plan_Individual in build_plan:            
            plan_Individual_Key =  plan_Individual.get('key')
            plan_Individual_Url = getCollectionUrl + "result/" + plan_Individual_Key +".json"        
            plan_Individual_collection = self.getResponse(plan_Individual_Url,'GET', UserID, Passwd, None,None)
            lastBuildNo = self.tracking.get(plan_Individual_Key,None)
            if lastBuildNo is None:
                lastBuildNo = 0
            self.tracking[plan_Individual_Key] = plan_Individual_collection["results"]["size"]    
            self.updateTrackingJson(self.tracking)
            if lastBuildNo < plan_Individual_collection["results"]["size"]:
                for plan_Individual_result_length in range(lastBuildNo,plan_Individual_collection["results"]["size"]):
                    plan_Individual_result = plan_Individual_collection["results"]["result"][plan_Individual_result_length]
                    plan_Individual_result_key =  plan_Individual_result.get("key")
                    plan_Individual_result_url = getCollectionUrl + "result/" + plan_Individual_result_key +".json"
                    plan_Individual_result_details = self.getResponse(plan_Individual_result_url,'GET', UserID, Passwd, None,None)
                    injectData = {}
                    data += self.parseResponse(responseTemplate, plan_Individual_result_details, injectData)
        if len(data) >0:
            self.publishToolsData(data)        
if __name__ == "__main__":
    BambooAgent()


