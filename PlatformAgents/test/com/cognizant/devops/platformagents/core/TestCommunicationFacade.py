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
Created on Jun 15, 2016

@author: 146414
'''
import unittest
from com.cognizant.devops.platformagents.core.CommunicationFacade import CommunicationFacade

class TestCommunicationFacade(unittest.TestCase):


    def testRestFacade(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            facade.communicate('https://api.github.com/users/username/repos', 'GET', None, None, None)
        except BaseException as exception:
            print exception
            assert False

    def testUnsupportedFacadeType(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST123')
            facade.communicate('https://api.github.com/users/username/repos', 'GET', None, None, None)
        except BaseException as exception:
            print exception
            return        
        assert False
                
    def testInvalidRestUrl(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            facade.communicate('https://api.github.com/users/username/repos123', 'GET', None, None, None)
        except BaseException as exception:
            print exception
            return        
        assert False

    def testGitRepoProjectRestCall(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            facade.communicate('https://api.github.com/repos/username/repoName/commits?access_token=authtoken', 'GET', None, None, None)
        except BaseException as exception:
            print exception
            assert False 
        
    def testResponseParsingValidKeys(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            response = facade.communicate('https://api.github.com/repos/username/repoName/commits?access_token=authtoken', 'GET', None, None, None)
            template = {
                            'commit' : {
                                'author': {
                                    'name' : 'userName',
                                    'email': 'userEmail',
                                    'date': 'commitDate'
                                },
                                'committer': {
                                    'name': 'userName1',
                                    'date': 'commitDate1'
                                },
                                'message' : 'message'
                            },    
                            'author': {
                                'avatar_url': 'profileImage',
                                'id' : 'customId',
                                'site_admin' : 'site_admin'
                            }
                        }
            dataArray = facade.processResponse(template, response)
            if dataArray == None or len(dataArray) == 0:
                assert False
        except BaseException as exception:
            print exception
            assert False 
            
    def testResponseParsingInValidKeys(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            response = facade.communicate('https://api.github.com/repos/username/reponame/commits?access_token=authtoken', 'GET', None, None, None)
            template = {
                            'commitInvalid' : {
                                'author': {
                                    'name' : 'userName',
                                    'email': 'userEmail',
                                    'date': 'commitDate'
                                },
                                'committer': {
                                    'name': 'userName1',
                                    'date': 'commitDate1'
                                },
                                'message' : 'message'
                            },    
                            'author': {
                                'avatar_url': 'profileImage',
                                'id' : 'customId',
                                'site_admin' : 'site_admin'
                            }
                        }
            dataArray = facade.processResponse(template, response)
            if dataArray == None or len(dataArray) == 0:
                assert False
        except BaseException as exception:
            print exception
            assert False
     
    def testResponseParsingKeysValueWithArray(self):
        try:
            baseFacade = CommunicationFacade()
            facade = baseFacade.getCommunicationFacade('REST')
            response = facade.communicate('http://JenkinsJob/api/json', 'GET', None, None, None)
            template = {
                            "actions": [
                                {
                                  "causes": [
                                    {
                                      "shortDescription": "jen_ShortDesc"
                                    }
                                  ]
                                },
                                {
                                  "remoteUrls": [
                                    "jen_SCMRemoteUrl"
                                  ]
                                },
                                {
                                  "url": "jen_SonarUrl"
                                }
                              ],
                              "changeSet": {
                                "items": [
                                  {
                                    "commitId": "jen_SCMCommitId",
                                    "author": {
                                      "fullName": "jen_SCMAuthor"
                                    },
                                    "date": "jen_Date"
                                  }
                                ],
                                "kind": "jen_SCMKind"
                              },
                              "duration": "jen_Duration",
                              "id": "jen_BuildNumber",
                              "result": "jen_Result",
                              "timestamp": "jen_TimeStamp",
                              "url": "jen_BuildUrl"
                        }
            dataArray = facade.processResponse(template, response)
            print dataArray
            if dataArray == None or len(dataArray) == 0:
                assert False
        except BaseException as exception:
            print exception
            assert False   
            

if __name__ == "__main__":
    unittest.main()
