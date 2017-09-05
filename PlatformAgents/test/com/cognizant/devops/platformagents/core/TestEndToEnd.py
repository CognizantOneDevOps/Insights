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
Created on Jun 17, 2016

@author: 146414
'''
from com.cognizant.devops.platformagents.core.MessageQueueProvider import MessageFactory
import time

def main():
    messageFactory = MessageFactory('iSight', 'iSight', 'localhost', 'iSight')
    data = {'count' : 0}             
    def callback(ch, method, properties, body):
        #Code for handling subscribed messages
        print '[x] Received : '+body
        ch.basic_ack(delivery_tag = method.delivery_tag)
        #ch.basic_cancel(method.consumer_tag)
    print 'Subscriber call'
    messageFactory.subscribe("SCM.GIT.*", callback, False)
    

if __name__ == '__main__':
    messageFactory = MessageFactory('iSight', 'iSight', 'localhost', 'iSight')
    data = {'count' : 0}             
    def callback(ch, method, properties, body):
        #Code for handling subscribed messages
        ch.basic_ack(delivery_tag = method.delivery_tag)
        data['count'] += 1
        print body
        #ch.basic_cancel(method.consumer_tag)
    
    messageFactory.subscribe('SCM.GIT.DATA', callback, False)
    #time.sleep(120)
    pass
