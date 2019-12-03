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
from ....core.BaseAgent3 import BaseAgent
import logging.handlers
import json

class snowAgent(BaseAgent):
    warnings.filterwarnings('ignore') 
    def process(self):
        try:
            self.BaseUrl = self.config.get("baseUrl", '')
            self.CR_sysid_url = self.config.get("CR_sysid_url", '')
            self.CR_Url = self.config.get("CR_Url", '')
            self.CR_Approval_Url = self.config.get("CR_Approval_Url", '')
            self.IN_sysid_url = self.config.get("IN_sysid_url", '')
            self.IN_Url = self.config.get("IN_Url", '')
            self.IN_Approval_Url = self.config.get("IN_Approval_Url", '')
            self.username = self.getCredential("userid")
            self.password = self.getCredential("passwd")
            self.tracking_time={}
            self.CR_sys_id=[]
            self.IN_sys_id=[]
            self.response=[]
            self.start_time = self.tracking.get("start_time", '')
            if self.start_time == '':
                self.start_time = self.config.get("startFrom", '')    
            self.end_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S.%f')[:-3]
            self.tracking_time["start_time"]=self.end_time
            self.printdata()
        except Exception as e:
            logging.error(e)
            
    def printdata(self):
        try:
            
            CR_sys_url = self.BaseUrl + self.CR_sysid_url + '%27' + self.start_time + '%27)%40javascript%3Ags.dateGenerate(%27' + self.end_time + '%27)'
            IN_sys_url = self.BaseUrl + self.IN_sysid_url + '%27' + self.start_time + '%27)%40javascript%3Ags.dateGenerate(%27' + self.end_time + '%27)'
            json_headers = {"Content-Type":"application/json","Accept":"application/json"}
            CR_sys_response = requests.get(CR_sys_url, auth=(self.username, self.password), headers=json_headers )
            IN_sys_response = requests.get(IN_sys_url, auth=(self.username, self.password), headers=json_headers )
            CR_sys_data = CR_sys_response.json()
            IN_sys_data = IN_sys_response.json()
            
            for k in CR_sys_data['result']:
                CR_sysid=k['sys_id']
                self.CR_sys_id.append(CR_sysid)
				
            for k in IN_sys_data['result']:
                IN_sysid=k['sys_id']
                self.IN_sys_id.append(IN_sysid)

            if self.CR_sys_id and self.IN_sys_id != []:
                for i in self.CR_sys_id:
                    CR_response_data={}
                    CR_approval_data={}
                    CR_url = self.BaseUrl + self.CR_Url + i
                    CR_response = requests.get(CR_url, auth=(self.username, self.password), headers=json_headers )
                    CR_data = CR_response.json()
                    CR_response_data["data"]=CR_data['result'][0]
                    CR_approval_url = self.BaseUrl + self.CR_Approval_Url + i
                    CR_approval_response = requests.get(CR_approval_url, auth=(self.username, self.password), headers=json_headers )
                    CR_app_data = CR_approval_response.json()
                    CR_approval_data["approval_details"]=CR_app_data['result']
                    CR_response_data["data"].update(CR_approval_data)
                    CR_response_data["data"]["Ticket_type"]="Change Ticket"
                    self.response.append(CR_response_data)

                for i in self.IN_sys_id:
                    IN_response_data={}
                    IN_approval_data={}
                    IN_url = self.BaseUrl + self.IN_Url + i
                    IN_response = requests.get(IN_url, auth=(self.username, self.password), headers=json_headers )
                    IN_data = IN_response.json()
                    IN_response_data["data"]=IN_data['result'][0]
                    IN_approval_url = self.BaseUrl + self.IN_Approval_Url + i
                    IN_approval_response = requests.get(IN_approval_url, auth=(self.username, self.password), headers=json_headers )
                    IN_app_data = IN_approval_response.json()
                    IN_approval_data["approval_details"]=IN_app_data['result']
                    IN_response_data["data"].update(IN_approval_data)
                    IN_response_data["data"]["Ticket_type"]="Incident Ticket"
                    self.response.append(IN_response_data)
					
            self.updateTrackingJson(self.tracking_time)
            
            
        except Exception as e:
            logging.error(e)

         
if __name__ == "__main__":
    snowAgent()
