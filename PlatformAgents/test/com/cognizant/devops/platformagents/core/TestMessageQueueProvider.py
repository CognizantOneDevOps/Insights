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
from com.cognizant.devops.platformagents.core.MessageQueueProvider import MessageFactory
import time

class TestMessageQueueProvider(unittest.TestCase):

    def testPublishAndSubscribe(self):
        messageFactory = MessageFactory('iSight', 'iSight', 'localhost', 'iSight')
        data = {'count' : 0}             
        def callback(ch, method, properties, body):
            #Code for handling subscribed messages
            ch.basic_ack(delivery_tag = method.delivery_tag)
            data['count'] += 1
            #ch.basic_cancel(method.consumer_tag)
        
        messageFactory.subscribe('TEST.MESSAGE', callback)
        time.sleep(5)
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        time.sleep(5)
        assert data['count'] == 5

    def testSubscriberCancellation(self):
        messageFactory = MessageFactory('iSight', 'iSight', 'localhost', 'iSight')
        data = {'count' : 0}             
        def callback(ch, method, properties, body):
            #Code for handling subscribed messages
            ch.basic_ack(delivery_tag = method.delivery_tag)
            data['count'] += 1
            ch.basic_cancel(method.consumer_tag)
        
        messageFactory.subscribe('TEST.MESSAGE', callback)
        time.sleep(5)
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        messageFactory.publish('TEST.MESSAGE', 'This is test Data')
        time.sleep(5)
        assert data['count'] == 1
    
if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    unittest.main()
