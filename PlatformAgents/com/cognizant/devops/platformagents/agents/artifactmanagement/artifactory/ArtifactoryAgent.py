
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
Created on Feb 16, 2018

@author: 476693 & 513585
'''

import requests, json
from requests.auth import HTTPBasicAuth
from ....core.BaseAgent import BaseAgent
from datetime import datetime, timedelta
import time
class ArtifactoryAgent(BaseAgent):
    def process(self):
        user = self.getCredential("userid")
        passwd = self.getCredential("passwd")
        BaseUrl = self.config.get("BaseUrl", '')
        FirstEndPoint = self.config.get("FirstEndPoint", '')
        self.startDate = None
        if self.tracking == {}:
            self.startFrom = self.config.get("startFrom", '')
            self.startDate = int(time.mktime(time.strptime(self.startFrom, '%Y-%m-%d %H:%M:%S')))
        else:
            self.since = self.tracking['since']
            self.startDate = int(time.mktime(time.strptime(self.since, '%Y-%m-%d %H:%M:%S')))
        useResponseTemplate = self.config.get("useResponseTemplate", False)
        if useResponseTemplate:
            self.response_template = self.getResponseTemplate()
        else:
            self.response_template = ['repo', 'path', 'created', 'createdBy', 'lastModified', 'modifiedBy', 'lastUpdated', 'uri', 'downloadUri' , 'mimeType', 'size']
        repo_list_url=BaseUrl+'repositories'
        json_headers = {"Content-Type":"application/json","Accept":"application/json"}
        list_of_repos = self.getResponse(repo_list_url, 'GET', user, passwd, None, reqHeaders=json_headers)
        with open(self.trackingFilePath, 'r') as config_file:
            self.tracking = json.load(config_file)
        self.data = []
        self.tracking_Data={}
        for length in range(len(list_of_repos)):
            nexturl=[]
            repo_name=list_of_repos[length]['key']
            master_repo_url=FirstEndPoint+repo_name
            nexturl.append(master_repo_url)
            while nexturl!= []:
                child_url=[]
                for url in nexturl:
                    nextrepo=[]
                    next_response = self.getResponse(url, 'GET', user, passwd, None, reqHeaders=json_headers)
                    if next_response is not None:
                        for length in range(len(next_response['children'])):
                            repos=next_response['children'][length]['uri']
                            nextrepo.append(repos)
                        for repo in nextrepo:
                            child_link=url+repo
                            child_reponse = self.getResponse(child_link, 'GET', user, passwd, None, reqHeaders=json_headers)
                            if child_reponse is not None:
                                if 'children' not in child_reponse:
                                    self.response(child_link, child_reponse)
                                else:
                                    child_url.append(child_link)
                nexturl=child_url
        self.publishToolsData(self.data)
        since=datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        self.tracking_Data['since']=since
        self.updateTrackingJson(self.tracking_Data)

    def response(self, url, response):
        response_data={}
        for temp in self.response_template:
            response_data[self.response_template[temp]] = response[temp]
        lastupdated = response_data['lastUpdated']
        # lastupdated=lastupdated.replace('-05:00','')
        lastupdated = lastupdated[:-10]
        lastupdated = int(time.mktime(time.strptime(lastupdated, '%Y-%m-%dT%H:%M:%S')))
        if self.startDate < lastupdated:
                self.data.append(response_data)

if __name__ == "__main__":
    ArtifactoryAgent()
