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
from .RestCommunicationFacade3 import RestCommunicationFacade
class CommunicationFacade(object):
    def __init__(self):
        self = self
        
    def getCommunicationFacade(self, facadeType, sslVerify, responseType, enableValueArray):
        if 'REST' == facadeType:
            return RestCommunicationFacade(sslVerify, responseType, enableValueArray);
        else:
            raise ValueError('CommunicationFacade: Unsupported Facade Type '+facadeType)
