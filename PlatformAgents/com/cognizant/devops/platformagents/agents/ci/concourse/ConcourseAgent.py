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
Created on 12 April 2018

@author: 476693 & 610951
'''
from ....core.BaseAgent import BaseAgent
import json

class ConcourseAgent(BaseAgent):
    def process(self):
        BaseUrl = self.config.get("BaseUrl", '')
        UserID = self.config.get("UserID", '')
        Passwd = self.config.get("Passwd", '')
        #responseTemplate = self.config.get("responseTemplate", '')
        responseTemplate = self.getResponseTemplate()
        trackingFilePath = 'tracking.json'
        getPipelinesUrl = BaseUrl+"/api/v1/teams/main/pipelines"
        
        Pipelines = self.getResponse(getPipelinesUrl, 'GET', UserID, Passwd, None, authType=None)
        
        for pipeline in Pipelines:
            pipeline_name = pipeline['name']
            #trackingDetails = self.tracking.get(pipeline_name,None)
            flag=0
            pipeline_jobs_url = getPipelinesUrl+'/'+pipeline_name+'/jobs'
            
            pipeline_jobs = self.getResponse(pipeline_jobs_url, 'GET', UserID, Passwd, None, authType=None)
            
            data = []
            for pipeline_job in pipeline_jobs:
                name= pipeline_job['name']
                trackingDetails = self.tracking.get(pipeline_name+' '+name,None)
                if trackingDetails is None:
                    trackingDetails = { 'latestBuildNumber' : pipeline_job['finished_build']['name']}
                    self.tracking[pipeline_name+' '+name] = trackingDetails
                    self.updateTrackingJson(self.tracking)
                    previous_build_number=0
                else:
                    previous_build_number=trackingDetails['latestBuildNumber']
                    if pipeline_job['finished_build']['name'] > int(previous_build_number):
                        trackingDetails = { 'latestBuildNumber' : pipeline_job['finished_build']['name']}
                        self.tracking[pipeline_name+' '+name] = trackingDetails
                        self.updateTrackingJson(self.tracking)
                if pipeline_job['finished_build']['name'] > previous_build_number:
                    job_details_url=pipeline_jobs_url+'/'+name+'/builds?limit='+str(int(pipeline_job['finished_build']['name']) - int(previous_build_number))
                    
                    job_details = self.getResponse(job_details_url, 'GET', UserID, Passwd, None, authType=None)
                    for job in job_details:
                        injectdata = {}
                        job_resource_url = BaseUrl+'/api/v1/builds/'+str(job['id'])+'/resources'
                        
                        data_commit = {}
                        job_resource = self.getResponse(job_resource_url, 'GET', UserID, Passwd, None, authType=None)
                        if len(job_resource['inputs']) > 0:
                            for len_inputs in range(0, len(job_resource['inputs'])):
                                job_resource_1 = job_resource['inputs'][len_inputs]
                                if len_inputs == 0:
                                    for l in range(0, len(job_resource_1['metadata'])):
                                        data_commit[job_resource_1['metadata'][l]['name']] = job_resource_1['metadata'][l]['value']
                                else:
                                    for l in range(0, len(job_resource_1['metadata'])):
                                        if job_resource_1['metadata'][l]['name'] in data_commit:
                                            data_commit[job_resource_1['metadata'][l]['name']] = data_commit[job_resource_1['metadata'][l]['name']] + ',' + job_resource_1['metadata'][l]['value']
                        for key in data_commit:
                            injectdata[key] = data_commit[key]
                        injectdata['job_name'] = name
                        data += self.parseResponse(responseTemplate, job, injectdata)
            self.publishToolsData(data)
if __name__ == "__main__":
    ConcourseAgent()
