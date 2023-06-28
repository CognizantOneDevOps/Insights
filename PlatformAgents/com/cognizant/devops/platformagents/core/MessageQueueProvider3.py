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
import pika
import _thread
import json
import sys
import logging.handlers

from .AWSSQSConnectionProvider import AWSSQSConnectionProvider
from .RabbitMQConnectionProvider import RabbitMQConnectionProvider

class MessageFactory:
        
    def __init__(self,config):
        logging.debug('Inside init of MessageFactory =======')
            
    def messageQueueHandler(self,config):
        
        self.mqConfig = self.config.get('mqConfig', None)
        self.providerName = self.mqConfig.get('providerName','RabbitMQ')
        logging.debug('Inside message Queue Handler of MessageFactory with providerName as ======='+ self.providerName)
        

        if (self.providerName == "AWSSQS"):
            return AWSSQSConnectionProvider(config)
        else:
            return RabbitMQConnectionProvider(config)