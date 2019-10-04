
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
        user = self.config.get("UserID", '')
        passwd = self.config.get("Passwd", '')
        BaseUrl = self.config.get("BaseUrl", '')
        FirstEndPoint = self.config.get("FirstEndPoint", '')
        repo_list_url=BaseUrl+'repositories'
        json_headers = {"Content-Type":"application/json","Accept":"application/json"}
        list_of_repos=requests.get(repo_list_url, auth=HTTPBasicAuth(user, passwd), headers=json_headers).json()
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
                    next_response=requests.get(url, auth=HTTPBasicAuth(user, passwd), headers=json_headers)
                    if next_response.status_code == 200:
                        next_response=next_response.json()
                        for length in range(len(next_response['children'])):
                            repos=next_response['children'][length]['uri']
                            nextrepo.append(repos)
                        for repo in nextrepo:
                            child_link=url+repo
                            child_reponse=requests.get(child_link, auth=HTTPBasicAuth(user, passwd), headers=json_headers)
                            if child_reponse.status_code == 200:
                                child_reponse=child_reponse.json()
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
        response_template=['repo', 'path', 'created', 'createdBy', 'lastModified', 'modifiedBy', 'lastUpdated', 'uri', 'downloadUri' ,'mimeType', 'size']
        for temp in response_template:
            response_data[temp]=response[temp]
        if self.tracking == {}:
            self.data.append(response_data)
        else:
            since=self.tracking['since']
            since = int(time.mktime(time.strptime(since, '%Y-%m-%d %H:%M:%S')))
            lastupdated=response_data['lastUpdated']
           # lastupdated=lastupdated.replace('-05:00','')
            lastupdated = lastupdated[:-10]
            lastupdated=int(time.mktime(time.strptime(lastupdated, '%Y-%m-%dT%H:%M:%S')))
            if since < lastupdated:
                self.data.append(response_data)

if __name__ == "__main__":
    ArtifactoryAgent()
