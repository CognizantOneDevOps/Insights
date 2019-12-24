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
import urllib2
import xmltodict
from ....core.BaseAgent import BaseAgent

class NexusAgent(BaseAgent):
    def process(self):
        UserID = self.getCredential("userid")
        Passwd = self.getCredential("passwd")
        BaseUrl = self.config.get("baseUrl", '')
        FirstEndPoint = self.config.get("firstEndPoint", '')
        nexIDs = self.getResponse(FirstEndPoint, 'GET', UserID, Passwd, None)
        data = []
        for artifacts in range(len(nexIDs["data"])):
            repoid = nexIDs["data"][artifacts]["latestSnapshotRepositoryId"]
            groupid = nexIDs["data"][artifacts]["groupId"]
            artifactid = nexIDs["data"][artifacts]["artifactId"]
            version = nexIDs["data"][artifacts]["latestSnapshot"]
            groupidrep = groupid.replace(".", "/", 3)
            mavenmetafile = urllib2.urlopen(BaseUrl+"service/local/repositories/"+repoid+"/content/"+groupidrep+"/"+artifactid+"/"+version+"/maven-metadata.xml")
            mavenmetadata = mavenmetafile.read()
            mavenmetafile.close()
            mavenmetadata = xmltodict.parse(mavenmetadata)
            buildnumber = mavenmetadata["metadata"]["versioning"]["snapshot"]["buildNumber"]
            lastupdated = mavenmetadata["metadata"]["versioning"]["lastUpdated"]
            artifactfullname = artifactid  + "-" + mavenmetadata["metadata"]["versioning"]["snapshotVersions"]["snapshotVersion"][0]["value"] + "." + mavenmetadata["metadata"]["versioning"]["snapshotVersions"]["snapshotVersion"][0]["extension"]
            injectData = {}
            injectData["repoiId"] = repoid
            injectData["groupId"] = groupid
            injectData["artifactId"] = artifactid
            injectData["version"] = version
            injectData["buildNumber"] = buildnumber
            injectData["lastUpdated"] = lastupdated
            injectData["artifactFullName"] = artifactfullname
            data.append(injectData)
        self.publishToolsData(data)
if __name__ == "__main__":
    NexusAgent()        
