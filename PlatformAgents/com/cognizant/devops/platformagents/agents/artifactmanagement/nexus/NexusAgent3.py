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
Created on Jun 28, 2016

@author: 463188
'''
import urllib.request, urllib.error, urllib.parse
import xmltodict
import json
import base64
from ....core.BaseAgent3 import BaseAgent

class NexusAgent(BaseAgent):
    
    @BaseAgent.timed
    def process(self):
        self.userid = self.getCredential("userid")
        self.cred = self.getCredential("passwd")
        BaseUrl = self.config.get("baseUrl", '')
        FirstEndPoint = self.config.get("firstEndPoint", '')
        nexIDs = self.getResponse(FirstEndPoint, 'GET', self.userid, self.cred, None)
        previousname = nexIDs["items"][0]["repository"]
        fetchNextPage = True           
        while fetchNextPage:  
            for artifacts in range(len(nexIDs["items"])):
                if nexIDs["items"][artifacts]["repository"] == previousname and artifacts != 0:
                    continue
                else:
                    repoid = nexIDs["items"][artifacts]["repository"]
                    artifactid = nexIDs["items"][artifacts]["name"]
                    previousname = repoid
                    groupid = nexIDs["items"][artifacts]["group"].replace(".", "/", 3)
                    request = urllib.request.Request(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["items"][artifacts]["name"]+"/maven-metadata.xml")
                    request.add_header('Authorization', 'Basic %s' % self.getBase64Value(self.userid,self.cred))
                    mavenmetafile = urllib.request.urlopen(request)#reading base mavenmetadata file to fetch main version
                    #mavenmetafile = urllib2.urlopen(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["items"][artifacts]["name"]+"/maven-metadata.xml")#reading base mavenmetadata file to fetch main version
                    mavenmetadata = xmltodict.parse(mavenmetafile.read())
                    mavenmetafile.close()
                    lastupdated = mavenmetadata["metadata"]["versioning"]["lastUpdated"]
                    tracking = self.trackingUpdation(repoid, lastupdated)
                    self.prepareAndPublish(nexIDs["items"][artifacts], tracking)
            
            continuationToken = nexIDs["continuationToken"]
            if continuationToken is None:
                fetchNextPage= False;                    
            else :
                fetchNextPage= True;
                newFirstEndPoint = FirstEndPoint+'&continuationToken='+str(continuationToken)
                nexIDs = self.getResponse(newFirstEndPoint, 'GET', self.userid, self.cred, None)
                
    def prepareAndPublish(self, nexIDs, tracking):
        repoid = nexIDs["repository"]
        artifactid = nexIDs["name"]
        groupid = nexIDs["group"].replace(".", "/", 3)
        request = urllib.request.Request(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["name"]+"/maven-metadata.xml")
        request.add_header('Authorization', 'Basic %s' % self.getBase64Value(self.userid,self.cred))
        mavenmetafile = urllib.request.urlopen(request)#reading base mavenmetadata file to fetch main version
        mavenmetadata = xmltodict.parse(mavenmetafile.read())
        mavenmetafile.close()
        lastupdated = mavenmetadata["metadata"]["versioning"]["lastUpdated"]
        if tracking>0:
            if tracking == 1:
                if isinstance(mavenmetadata["metadata"]["versioning"]["versions"]["version"],list):
                    for version in mavenmetadata["metadata"]["versioning"]["versions"]["version"]:
                        self.publishdata(repoid, groupid, nexIDs, version, artifactid, lastupdated)
                else:
                    version = mavenmetadata["metadata"]["versioning"]["versions"]["version"]
                    self.publishdata(repoid, groupid, nexIDs, version, artifactid, lastupdated)
            else:
                version = mavenmetadata["metadata"]["versioning"]["versions"]["version"][len(mavenmetadata["metadata"]["versioning"]["versions"]["version"])-1]
                self.publishdata(repoid, groupid, nexIDs, version, artifactid, lastupdated)

    def publishdata(self, repoid, groupid, nexIDs, version, artifactid, lastupdated):
        data = []
        print(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["name"]+"/"+version+"/"+nexIDs["name"]+"-"+version+".pom")
        request = urllib.request.Request(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["name"]+"/"+version+"/"+nexIDs["name"]+"-"+version+".pom")
        request.add_header('Authorization', 'Basic %s' % self.getBase64Value(self.userid,self.cred))
        mainmavenxml = urllib.request.urlopen(request)#reading mavenmetadata file inside main version folder
        #mainmavenxml = urllib.request.urlopen(self.config.get("baseUrl", '')+"repository/"+repoid+"/"+groupid+"/"+nexIDs["name"]+"/"+version+"/"+nexIDs["name"]+"-"+version+".pom")#reading mavenmetadata file inside main version folder
        mainmavendata = mainmavenxml.read()
        mainmavenxml.close()
        artifactfullname = artifactid + "-" + version + "." + xmltodict.parse(mainmavendata)["project"]["packaging"]
        injectData = {}
        injectData["timestamp"] = lastupdated
        injectData["version"] = version
        injectData["currentID"] = groupid+ "-" + artifactfullname
        injectData["resourceKey"] = nexIDs["group"] + ':' + nexIDs["name"]
        injectData["Status"] = "Archive"
        injectData["Author"] = self.userid
        data.append(injectData)
        self.publishToolsData(data)

    def nexus(self, logResponse):
        #print (logResponse)
        return
        
    def getBase64Value(self,userid,cred):
        userpass = '%s:%s' % (userid,cred)
        base64string = base64.standard_b64encode(userpass.encode('utf-8'))
        return base64string.decode('utf-8')

    def trackingUpdation(self, repoid, lastupdated):
        self.loadTrackingConfig()
        if self.tracking.get(repoid) is None:
            self.tracking[repoid] = lastupdated
            self.updateTrackingJson(self.tracking)
            return 1
        else:
            if int(self.tracking.get(repoid, None)) < int(lastupdated):
                self.tracking[repoid] = lastupdated
                self.updateTrackingJson(self.tracking)
                return 2
            else:
                return 0
if __name__ == "__main__":
    NexusAgent()