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
Created on Jul 19, 2017

@author: 476693 & 513585
'''
import json
import pysvn
import time
import sys
import os
from dateutil import parser
import datetime, time
from ....core.BaseAgent import BaseAgent
import logging.handlers

class SvnAgent(BaseAgent):
    def get_login(self, realm, username, may_save ):
        username = self.getCredential("userid")
        password = self.getCredential("passwd")
        return True, username, password, False

    def process(self):
        self.repoList = []
        self.newrepo=[]
        self.existingrepo=[]
        self.client = pysvn.Client()
        self.client.callback_get_login = self.get_login
        self.head_rev = pysvn.Revision(pysvn.opt_revision_kind.head) 
        try:
            self.repoList = self.config.get('dynamicTemplate', {}).get("baseUrl", '')
            self.date_time = self.config.get("startFrom", '')
            self.pattern = self.config.get("timeStampFormat", '')
            logging.info('List of Repositories : %s ' % self.repoList)
            self.trackingData()
            self.publishData()
            if self.data != []:
                print self.data
                self.publishToolsData(self.data)
            self.updateTrackingJson(self.trackingdata) 
        except Exception as e:
            logging.error(e)
        
    def trackingData(self):
        with open(self.trackingFilePath, 'r') as config_file:    
            self.tracking = json.load(config_file)
        for k,v in self.tracking.items():
            for i in self.repoList:
                if i==k:
                    self.existingrepo.append(i)
        for i in self.repoList:
            if i not in self.existingrepo:
                self.newrepo.append(i)
        print self.newrepo, self.existingrepo
            
    def publishData(self):
        self.trackingdata = {}
        self.data = []
            
        for repo in self.newrepo:
            try:
                epoch = int(time.mktime(time.strptime(self.date_time, self.pattern)))
            except Exception as e:
                logging.error(e)
            self.end_rev=pysvn.Revision(pysvn.opt_revision_kind.date, epoch)
            self.retrieveData(repo)
            self.printdata(repo)
            
        for repo in self.existingrepo:
            revNo=self.tracking.get(str(repo), '')+1
            self.end_rev = pysvn.Revision(pysvn.opt_revision_kind.number, revNo)
            self.printdata(repo)
            self.retrieveData(repo)            

    def retrieveData(self,reposURL):
        track={}
        entries = self.client.info2(reposURL, revision=self.head_rev, recurse=False)
        for reposURL, info in entries:
            repoUrl=info.URL
            repoRev=info.rev.number
            track[repoUrl]=repoRev
            self.trackingdata.update(track)

    def printdata(self,reposURL):
        try:
            msgs = self.client.log(reposURL, revision_start=self.head_rev, revision_end=self.end_rev, discover_changed_paths=True)
            msgs.reverse()
            s = len(msgs)
            loglimit = 500
            if s > loglimit:
                log_message = log_message[:s]
                s = len(log_message) - 1
            for m in msgs:
                printdata ={}
                date = m.data['revprops']['svn:date']
                commit_time = str(datetime.datetime.fromtimestamp(date))
                if commit_time > self.date_time:
                    message = m.data['message']
                    paths = [p.path for p in m.data['changed_paths']]
                    printdata["repoUrl"]=reposURL
                    printdata["revNum"]=m.revision.number
                    printdata["commitTime"]=str(datetime.datetime.fromtimestamp(date))
                    printdata["commitAuthor"]=m.author
                    printdata["changeFilelist"]=paths
                    printdata["commitLog"]=message
                    self.data.append(printdata)
                else:
                    pass
        except Exception:
            pass
        
if __name__ == "__main__":
    SvnAgent()
