#-------------------------------------------------------------------------------
# -*- coding: utf-8 -*-
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
Created on Jul 6, 2017

@author: 463188
'''
# Optimization and Pagination might be required. This is the first cut working agent with incremental fetch

from dateutil import parser
from ....core.BaseAgent import BaseAgent
from urllib import quote
import time
import json,ast

class VersionOneAgent(BaseAgent):
    def process(self):
        userid = self.getCredential("userid")
        passwd = self.getCredential("passwd")
        baseUrl = self.config.get("baseUrl", '')
        project = self.config.get("project", '')
        startFrom = self.config.get("startFrom", '')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime('%Y-%m-%dT%H:%M:%S')
        reqHeaders = {
                        "Content-Type" : "text/xml",
                        "Accept" :  "application/json"
                      }
        responseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)
        hierachiesUrl = baseUrl+"Story?where=SecurityScope.Name='"+project+"'&sel=Name,Number,ChangeDate,Timebox.Name,Scope.Name,Status.Name,Estimate"
        hierachies = self.getResponse(hierachiesUrl, 'GET', userid, passwd, None,reqHeaders=reqHeaders)
        for hierarchy in hierachies["Assets"]:
            injectData = {}
            data = []
            name = hierarchy['Attributes']['Name']['value']
            since = self.tracking.get(name,None)
            if since == None:
                lastUpdated = startFrom
            else:
                since = parser.parse(since)
                since = since.strftime('%Y-%m-%dT%H:%M:%S')
                lastUpdated = since
            date = hierarchy['Attributes']['ChangeDate']['value']
            date = parser.parse(date)
            date = date.strftime('%Y-%m-%dT%H:%M:%S')
            if since == None or date > since:
                injectData['storyName']=str(hierarchy['Attributes']['Name']['value'])
                injectData['id']=str(hierarchy['Attributes']['Number']['value'])
                injectData['sprintName']=str(hierarchy['Attributes']['Timebox.Name']['value'])
                injectData['projectName']=str(hierarchy['Attributes']['Scope.Name']['value'])
                injectData['status']=str(hierarchy['Attributes']['Status.Name']['value'])
                injectData['estimate']=str(hierarchy['Attributes']['Estimate']['value'])
                injectData['lastUpdateDate']=date
                data.append(injectData)
                fromDateTime=date
            else:
                fromDateTime = lastUpdated
            if len(hierachies)>0 and len(data)!=0:
                self.tracking[name] = fromDateTime
                versionOneMetadata = {"dataUpdateSupported" : True,"uniqueKey" : ["id"]} 
                #self.publishToolsData(data)
                self.publishToolsData(data, versionOneMetadata)
                self.updateTrackingJson(self.tracking)

if __name__ == "__main__":
    VersionOneAgent()
