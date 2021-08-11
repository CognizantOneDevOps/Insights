# ------------------------------------------------------------------------------- 
# Copyright 2021 Cognizant Technology Solutions
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
# -------------------------------------------------------------------------------
import logging

class LoggingFilter(logging.Filter):

    def __init__(self, baseAgent, formatters,default_formatter):
        self._formatters = formatters
        self._default_formatter = default_formatter
        self.execId = baseAgent.executionId
        self.dataSize = baseAgent.dataSize
        self.dataCount = baseAgent.dataCount
        self.funcName = baseAgent.funcName
        self.baseAgent = baseAgent

        
    def filter(self, record):
        if not hasattr(record,'execId') :
            record.execId = self.baseAgent.executionId
        if not hasattr(record, 'dataSize'):
            record.dataSize = self.baseAgent.dataSize
        if not hasattr(record, 'dataCount'):
            record.dataCount = self.baseAgent.dataCount
        if not hasattr(record,'funcName'):
            record.funcName = self.baseAgent.funcName
        return True
    
    def format(self, record):
        formatter = self._formatters.get(record.name, self._default_formatter)
        return formatter.format(record)
