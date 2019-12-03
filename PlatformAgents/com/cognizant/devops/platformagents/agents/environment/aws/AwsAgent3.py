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
from boto.ec2.connection import EC2Connection
from boto.utils import get_instance_metadata
import time
import sys
import os
from datetime import datetime, timedelta
from ....core.BaseAgent3 import BaseAgent
#from core.BaseAgent import BaseAgent
import logging.handlers

class AwsAgent(BaseAgent):
    def process(self):
        try:
            self.id = self.getCredential("access_key_id")
            self.key = self.getCredential("secret_access_key")
            self.vpcid = self.config.get('dynamicTemplate', {}).get("vpc_id", None)
        except Exception as e:
            logging.error(e)
            
        tracking_data = []
        ec2conn = EC2Connection(self.id, self.key)
        reservations = ec2conn.get_all_instances()
        volumes=ec2conn.get_all_volumes()
        instances = [i for r in reservations for i in r.instances]
        for i in instances:
            for value in self.vpcid:
                if i.vpc_id == value:
                    r = ec2conn.get_all_instances(i.id)
                    size=[]
                    Volume_ID=None
                    for vol in volumes:
                        if vol.attach_data.instance_id == i.id:
                            size.append(vol.size)
                    ltime=i.launch_time
                    pattern=self.config.get("timeStampFormat", None)
                    seconds = time.time() - int(time.mktime(time.strptime(ltime, pattern)))
                    years, seconds = divmod(seconds, 12 *30 *24 * 60 * 60)
                    months, seconds = divmod(seconds, 30 *24 * 60 * 60)
                    days, seconds = divmod(seconds, 24 * 60 * 60)
                    hours, seconds = divmod(seconds, 60 * 60)
                    minutes, seconds = divmod(seconds, 60)
                    tracklist={}
                    tracklist["instanceName"]= r[0].instances[0].tags['Name']
                    tracklist["vpcId"]=value
                    tracklist["instanceId"]=i.id
                    tracklist["instanceType"]=i.instance_type
                    tracklist["privateIp"]=i.private_ip_address
                    tracklist["size"]=size
                    tracklist["launchTime"]=i.launch_time
                    tracklist["state"]=i.state
                if i.state == "running":
                    tracklist["runTime"]=("%d years %d months %d days %d hours" % (years, months, days, hours))
                else:
                    pass
                    tracking_data.append(tracklist)
        if tracking_data!=[]:
            self.publishToolsData(tracking_data)
        
if __name__ == "__main__":
    AwsAgent()
