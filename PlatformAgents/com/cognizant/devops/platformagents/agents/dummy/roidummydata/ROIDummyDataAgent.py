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


import datetime
import os
import random
import sys

from dateutil import parser

from .BaseAgent3 import BaseAgent


class ROIDummyDataAgent(BaseAgent):
    def process(self):
        try:
            self.daysDiff = self.config.get("numberOfDaysBetweenOutcomeRecords", 1)
            self.timestampFormat = self.config.get("timeStampFormat")
            milestoneOutcomeMetadata = self.config.get("dynamicTemplate", {}).get("responseTemplate", {}).get("MilestoneOutcomeMetadata", {})
            outcomeDetails = milestoneOutcomeMetadata.get("OutcomeDetails", {})
            milestoneDetails = milestoneOutcomeMetadata.get("MilestoneDetails", {})
            
            for outcome in outcomeDetails.values():
                toolData = []
                self.fieldName = outcome.get("fieldName")
                self.lowBound = outcome.get("range")[0]
                self.upBound = outcome.get("range")[1]
                self.milestoneName = outcome.get("milestoneName")
                outcomeMilestone = milestoneDetails.get(self.milestoneName, {})
                self.milestoneReleaseId = outcomeMilestone.get("milestoneReleaseId")
                self.startFrom = parser.parse(outcomeMilestone.get("startFrom"), ignoretz=True)
                self.endDate = parser.parse(outcomeMilestone.get("endDate"), ignoretz=True)
                
                toolOutcomeMetadata = {
                    "toolName": outcome.get("toolName"),
                    "milestoneName": self.milestoneName,
                    "outcomeName": outcome.get("outcomeName"),
                    "milestoneReleaseId": self.milestoneReleaseId
                    }
                
                # calculating end date by adding days difference
                nextDate = self.startFrom + datetime.timedelta(days=self.daysDiff)
                startDate = self.startFrom
                while(nextDate<=self.endDate):
                    toolData += self.prepareOutcomeData(toolOutcomeMetadata.copy(), startDate, nextDate)
                    startDate = nextDate + datetime.timedelta(minutes= 1 )
                    nextDate = nextDate + datetime.timedelta(days=self.daysDiff)
                delta = self.endDate - startDate
                if(delta.days>0):
                    toolData += self.prepareOutcomeData(toolOutcomeMetadata.copy(), startDate, self.endDate)

                labels = milestoneOutcomeMetadata.get("labels", {}).copy()
                labels.append(outcome.get("toolName").upper())
                toolMetaData = {"labels": labels}
                self.publishToolsData(toolData, toolMetaData, "startTime", "%Y-%m-%dT%H:%M:%S", False)

        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print((exc_type, fname, exc_tb.tb_lineno))
            
    
    def prepareOutcomeData(self, outcomeDetails, startDate, nextDate):
        toolData = []
        outcomeDetails[self.fieldName] = round(random.uniform(self.lowBound, self.upBound),2)
        outcomeDetails["from"] = startDate.strftime(self.timestampFormat)
        outcomeDetails["to"] = nextDate.strftime(self.timestampFormat)
        toolData.append(outcomeDetails)
        return toolData


if __name__ == "__main__":
    ROIDummyDataAgent() 
