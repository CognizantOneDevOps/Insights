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
Created on Aug 22, 2017

@author: 476693 & 513585
'''
import warnings
import time
import sys
import os
import requests
from dateutil import parser
import datetime, time
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging.handlers

class snowAgent(BaseAgent):
    warnings.filterwarnings('ignore') 
    def process(self):
        try:
            self.BaseUrl = self.config.get("BaseUrl", '')
            self.Start_url = self.config.get("Start_url", '')
            self.CR_Url = self.config.get("CR_Url", '')
            self.Approval_Url = self.config.get("Approval_Url", '')
            self.username = self.config.get("username", '')
            self.password = self.config.get("password", '')
            self.tracking_time={}
            self.sys_id=[]
            self.response=[]
            self.start_time = self.tracking.get("start_time", '')
            if self.start_time == '':
                self.start_time = self.config.get("StartFrom", '')    
            self.end_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
            self.tracking_time["start_time"]=self.end_time
            self.printdata()
        except Exception as e:
            logging.error(e)
            
    def printdata(self):
        try:
            
            sys_url = self.BaseUrl + self.Start_url + '%27' + self.start_time + '%27' ')%40javascript%3Ags.dateGenerate(' + '%27' + self.end_time + '%27' ')'
            json_headers = {"Content-Type":"application/json","Accept":"application/json"}
            change_response = requests.get(sys_url, auth=(self.username, self.password), headers=json_headers )
            sys_data = change_response.json()
            
            for k in sys_data['result']:
                sysid=k['sys_id']
                self.sys_id.append(sysid)
            
            for i in self.sys_id:
                response_data={}
                approval_data={}
                change_url = self.BaseUrl + self.CR_Url + i
                approval_response = requests.get(change_url, auth=(self.username, self.password), headers=json_headers )
                change_data = approval_response.json()
                response_data["data"]=change_data['result'][0]
                approval_url = self.BaseUrl + self.Approval_Url + i
                approval_response = requests.get(approval_url, auth=(self.username, self.password), headers=json_headers )
                app_data = approval_response.json()
                approval_data["approval_details"]=app_data['result']
                response_data["data"].update(approval_data)
                self.response.append(response_data)
            if self.response != []:
                print self.response
                self.updateTrackingJson(self.tracking_time)
            
            
        except Exception as e:
            logging.error(e)

         
if __name__ == "__main__":
    snowAgent()
