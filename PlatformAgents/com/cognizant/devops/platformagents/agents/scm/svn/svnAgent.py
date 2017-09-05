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

import pysvn
import time
import sys
import os
from dateutil import parser
import datetime, time
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging.handlers

class svnAgent(BaseAgent):
    def process(self):
        self.repoList = []
        self.data = []
        self.trackingdata = {}
        self.json = self.tracking.get("1", '')
        self.client = pysvn.Client()
        self.head_rev = pysvn.Revision(pysvn.opt_revision_kind.head) 
        try:
            BaseUrl = self.config.get("BaseUrl", '')
            repo1 = BaseUrl + "Insights"
            repo2 = BaseUrl + "git"
            self.repoList.append(repo1)
            self.repoList.append(repo2)
            logging.info('List of Repositories : %s ' % self.repoList)
            self.publishData()
        except Exception as e:
            logging.error(e)

    def publishData(self):
        if self.json == '':
            self.urlno = 1
            try:
                self.date_time = self.config.get("StartFrom", '')
                self.pattern = self.config.get("timeStampFormat", '')
                epoch = int(time.mktime(time.strptime(self.date_time, self.pattern)))
            except Exception as e:
                logging.error(e)
            self.end_rev=pysvn.Revision(pysvn.opt_revision_kind.date, epoch)
            for repo in self.repoList:
                self.retrieveData(repo)
                self.printdata(repo)
            if self.data != []:
                self.publishToolsData(self.data)
            self.updateTrackingJson(self.trackingdata)
            
        else:
            revcount = 1
            while self.json!='':
                self.urlno = 1
                self.trackingdata = {}
                self.data = []
                rev=self.tracking.get(str(revcount), '')
                revNo=rev.get("rev", '')+1
                repourl=rev.get("url", '')
                self.end_rev = pysvn.Revision(pysvn.opt_revision_kind.number, revNo)
                self.printdata(repourl)
                revcount = revcount + 1
                for repo in self.repoList:
                    self.retrieveData(repo)
                if self.data != []:
                    self.publishToolsData(self.data)
                self.updateTrackingJson(self.trackingdata)
               

    def retrieveData(self,reposURL):
        tracklist = {}
        entries = self.client.info2(reposURL, revision=self.head_rev, recurse=False)
        for reposURL, info in entries:
            repoUrl=info.URL
            repoRev=info.rev.number
            tracklist["url"]=repoUrl
            tracklist["rev"]=repoRev
            self.trackingdata[self.urlno]=tracklist
            self.urlno = self.urlno + 1

    def printdata(self,reposURL):
        try:
            msgs = self.client.log(reposURL, revision_start=self.head_rev, revision_end=self.end_rev, discover_changed_paths=True)
            msgs.reverse()
            s = len(msgs)
            loglimit = 200
            while s > loglimit:
                log_message = log_message[:s]
                s = len(log_message) - 1
            for m in msgs:
                printdata ={}
                date = m.data['revprops']['svn:date']
                commit_time = str(datetime.datetime.fromtimestamp(date))
                if commit_time > self.date_time:
                    message = m.data['message']
                    paths = [p.path for p in m.data['changed_paths']]
                    printdata["repo_url"]=reposURL
                    printdata["rev_num"]=m.revision.number
                    printdata["commit_time"]=str(datetime.datetime.fromtimestamp(date))
                    printdata["commit_author"]=m.author
                    printdata["change_filelist"]=paths
                    printdata["commit_log"]=message
                    self.data.append(printdata)
                else:
                    pass
        except Exception:
            pass
        
if __name__ == "__main__":
    svnAgent()
