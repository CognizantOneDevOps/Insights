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
from core.BaseAgent import BaseAgent
import logging.handlers

class svnAgent(BaseAgent):
    def get_login(self, realm, username, may_save ):
        username = self.config.get("username", '')
        password = self.config.get("password", '')
        return True, username, password, False

    def process(self):
        self.repoList = []
        self.json = self.tracking.get("1", '')
        self.client = pysvn.Client()
        self.client.callback_get_login = self.get_login
        self.head_rev = pysvn.Revision(pysvn.opt_revision_kind.head) 
        try:
            self.repoList = self.config.get("BaseUrl", '')
            self.date_time = self.config.get("StartFrom", '')
            self.pattern = self.config.get("timeStampFormat", '')
            logging.info('List of Repositories : %s ' % self.repoList)
            self.trackingData()
            self.publishData()
        except Exception as e:
            logging.error(e)

    def trackingData(self):
        i=1
        url=0
        self.tracklist=[]
        self.newurl=[]
        self.oldurl=[]
        while url!='':
            url=self.tracking.get(str(i), '')
            if url!='':
                url=url.get("url", '')
                self.tracklist.append(url)
            i=i+1
        for i in self.repoList:
            if i in self.tracklist:
                self.oldurl.append(i)
            else:
                self.newurl.append(i)

    def publishData(self):
        self.urlno = 1
        self.trackingdata = {}
        self.data = []
        for repo in self.newurl:
            print "new data"
            try:
                epoch = int(time.mktime(time.strptime(self.date_time, self.pattern)))
            except Exception as e:
                logging.error(e)
            self.end_rev=pysvn.Revision(pysvn.opt_revision_kind.date, epoch)
            self.retrieveData(repo)
            self.printdata(repo)
            
        for repo in self.oldurl:
            print "old data"
            r = 1
            rev=0
            while rev!='':
                rev=self.tracking.get(str(r), '')
                if rev!='':
                    revNo=rev.get("rev", '')+1
                    print revNo
                    repourl=rev.get("url", '')
                    print repourl
                    self.end_rev = pysvn.Revision(pysvn.opt_revision_kind.number, revNo)
                    if repourl == repo:
                        self.printdata(repo)
                        self.retrieveData(repo)
                    r = r + 1
        
        print self.data
        #if self.data != []:
            #self.publishToolsData(self.data)
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
