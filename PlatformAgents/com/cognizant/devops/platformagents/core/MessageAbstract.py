#-------------------------------------------------------------------------------
# Copyright 2022 Cognizant Technology Solutions
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


import abc
import json
import sys

class MessageAbstract(metaclass=abc.ABCMeta):
    @abc.abstractclassmethod
    def publish(self,routingKey, data, batchSize, metadata):
        pass
    
    @abc.abstractclassmethod
    def subscribe(self, routingKey, callback):
        pass
    
    def chunks(self, l, n):
        for i in range(0, len(l), n):
            yield l[i:i + n]
    
    def buildMessageJson(self, data, metadata=None):
        messageJson = data
        
        if metadata:
            messageJson = {
                    'data' : data,
                    'metadata' : metadata,
                }
        return json.dumps(messageJson)
            
    def getMessageSize(self, data):
        pckSize = 0
        for i in data:
            pckSize = pckSize + sys.getsizeof(str(i))
        return pckSize
