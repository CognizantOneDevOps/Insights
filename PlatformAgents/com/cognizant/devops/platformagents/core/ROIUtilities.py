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

import logging.handlers
import json
from datetime import datetime

class ROIUtilities():
    
    def __init__(self, messageFactory, tracking, trackingFilePath, config):
        logging.debug('Inside init of ROIUtilities =======')
        self.messageFactory = messageFactory
        self.tracking = tracking
        self.trackingFilePath = trackingFilePath
        self.config = config
    
    def publishROIAgentstatus(self, messageFactory, milestoneDetails, status, message):
        logging.debug('Inside publishROIAgentstatus ========')
        statusQueue = milestoneDetails.get("statusQueue", None)
        logging.debug('ROI agent status queue: '+statusQueue)
        statusDetails = {"milestoneName": milestoneDetails["milestoneName"],
                         "outcomeName": milestoneDetails["outcomeName"], 
                         "outcomeId": milestoneDetails["outcomeId"],
                         "milestoneId": milestoneDetails["milestoneId"]}
        if status == "SUCCESS":
            healthInfo = {"status":status,"message":"outcome data collection completed"}
        elif status == "INPROGRESS":
            healthInfo = {"status":status,"message":"outcome data collection is in progress"}
        else:
            healthInfo = {"status":"ERROR","message":message} 
        statusDetails.update(healthInfo)
        messageFactory.publish(statusQueue, statusDetails)
        
    
    def updateStatusInTracking(self, status, trackId):
        logging.debug('Inside updateStatusInTracking===')
        timeFormat = self.config.get("timeStampFormat", "%Y-%m-%dT%H:%M:%SZ")
        timeStampNow = lambda: datetime.utcnow().strftime(timeFormat)
        milestoneTrackingDetails = self.tracking.get("milestoneDetails", None)
        milestoneTrackingDetails[trackId]["lastUpdatedDate"] = timeStampNow()
        if status == "COMPLETED":
            completedMilestone = self.tracking.get("completedMilestones", None)
            if completedMilestone is None:
                completedMilestone = {}
                self.tracking["completedMilestones"] = completedMilestone
            milestoneTrackingDetails[trackId]["status"] = "COMPLETED"
            completedMilestone[trackId] = milestoneTrackingDetails[trackId]
            milestoneTrackingDetails.pop(trackId)
        if status == "INPROGRESS":
            milestoneTrackingDetails[trackId]["intermediateDate"] = timeStampNow()
            milestoneTrackingDetails[trackId]["status"] = "INPROGRESS"
        if status == "ERROR":
            retry_count = milestoneTrackingDetails.get(trackId, {}).get("retryCount", 0)
            if retry_count == 2:
                milestoneTrackingDetails[trackId]["status"] = "ABORTED"
            else:
                milestoneTrackingDetails[trackId]["status"] = "ERROR"
            milestoneTrackingDetails[trackId]["retryCount"] = retry_count + 1
        self.updateTrackingJson(self.tracking)
        
    def updateTrackingJson(self, data):
        logging.debug('Inside updateTrackingJson===')
        with open(self.trackingFilePath, 'w') as outfile:
            json.dump(data, outfile, indent=4, sort_keys=True)
            
            
        