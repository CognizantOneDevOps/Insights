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
Created on Sep 19, 2017

@author: 476693 & 513585
'''
import boto.ec2.connection
import boto.ec2
import time
import sys
import os
from datetime import datetime, timedelta
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging.handlers

class awsAgent(BaseAgent):
    def process(self):
        try:
            self.id = self.config.get("access_key_id", '')
            self.key = self.config.get("secret_access_key", '')
            self.vpcid = self.config.get("vpc_id", '')
            #print self.vpcid
        except Exception as e:
            logging.error(e)
            
        tracking_data = []
        ec2conn = boto.ec2.connection.EC2Connection(self.id, self.key)
        reservations = ec2conn.get_all_instances()
        instances = [i for r in reservations for i in r.instances]
        for i in instances:
            for value in self.vpcid:
                if i.vpc_id == value:
                    ltime=i.launch_time
                    privateIP=i.private_ip_address
                    pattern='%Y-%m-%dT%H:%M:%S.%fZ'
                    run_sec = time.time() - int(time.mktime(time.strptime(ltime, pattern))) 
                    ctime = datetime(100,1,1) + timedelta(seconds=int(run_sec))
                    tracklist={}
                    tracklist["vpc_id"]=value
                    tracklist["Instance_id"]=i.id
                    tracklist["Private_ip"]=privateIP
                    tracklist["State"]=i.state 
                    tracklist["Runtime"]=("%d days %d hours" % (ctime.day-1, ctime.hour))
                    tracking_data.append(tracklist)
        #print tracking_data
        
if __name__ == "__main__":
    awsAgent()
