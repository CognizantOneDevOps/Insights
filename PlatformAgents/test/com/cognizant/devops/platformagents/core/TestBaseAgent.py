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
Created on Jun 16, 2016

@author: 146414
'''
import unittest
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
from com.cognizant.devops.platformagents.core.MessageQueueProvider import MessageFactory
import time
import json
import shutil
import os

class TestBaseAgent(unittest.TestCase):

    def testConfigSubscriber(self):
        shutil.copy('TestConfig.json', 'config.json')
        shutil.copy('TestTracking.json', 'tracking.json')
        baseAgent = BaseAgent()
        testConfigAttr = baseAgent.config.get('testConfigSubscriber', None)
        assert testConfigAttr == None
        baseAgent.config['testConfigSubscriber'] = True
        messageFactory = MessageFactory('iSight', 'iSight', 'localhost', 'iSight')
        messageFactory.publish('SCM.GIT.config', json.dumps(baseAgent.config))
        time.sleep(5)
        testConfigAttr = baseAgent.config.get('testConfigSubscriber', None)
        assert testConfigAttr != None
        os.remove('config.json')
        os.remove('tracking.json')
        pass


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
