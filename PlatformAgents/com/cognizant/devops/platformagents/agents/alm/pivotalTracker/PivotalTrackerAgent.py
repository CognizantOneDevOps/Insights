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
Created on May 9, 2018

@author: 476693
'''
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import time
import requests
import json
import datetime
from dateutil import parser

class pivotalTrackerAgent(BaseAgent):
    def process(self):
        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        relationMetadata = self.config.get('dynamicTemplate', {}).get('relationMetadata', None)
        storyMetadata = self.config.get('dynamicTemplate', {}).get('storyMetadata', None)
        baseEndPoint = self.config.get('baseEndPoint', '')
        accessToken = self.config.get('token')
        reqHeaders = {'x-trackertoken': accessToken}
        startFrom = self.config.get("startFrom", '')
        timeStampFormat = self.config.get('timeStampFormat')
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime(timeStampFormat)
        workspaces_url = baseEndPoint + "/services/v5/my/workspaces"
        workspaces = self.getResponse(workspaces_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
        data = []
        data_iteration_neo4j = []
        all_projects = []
        for workspace in workspaces:
                workspace_id = workspace['id']
                workspace_name = workspace['name']
                for project_id in workspace['project_ids']:
                        all_projects.append(project_id)
                        trackingDetails = self.tracking.get(workspace_name+ ' ' +str(project_id), None)
                        if trackingDetails is None:
                                startFrom = startFrom
                        else:
                                startFrom = trackingDetails['latestUpdatedDate']
                        stories_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/stories"
                        stories = self.getResponse(stories_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
                        activities_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/activity?occurred_after="+startFrom
                        activities = self.getResponse(activities_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
                        if len(activities) > 0:
                                trackingDetails = { 'latestUpdatedDate' : activities[0]['occurred_at']}
                                self.tracking[workspace_name+ ' ' +str(project_id)] = trackingDetails
                                self.updateTrackingJson(self.tracking)
                                for story in stories:
                                        flag = 0
                                        for activity in activities:
                                                if activity['kind'] == 'story_update_activity' and activity['primary_resources'][0]['name'] == story['name']:
                                                        if len(activity['changes']) > 0:
                                                                flag = 1
                                                                data.append(self.data_prepare(story, project_id, workspace_name, activity)) 
                                        if flag == 0:
                                                data.append(self.data_prepare(story, project_id, workspace_name, activity=None))
                                                
        projects_url = baseEndPoint + "/services/v5/projects"
        projects = self.getResponse(projects_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
        for project in projects:
                project_id = project['id']
                if project_id not in all_projects:
                        all_projects.append(project_id)
                        trackingDetails = self.tracking.get(str(project_id), None)
                        if trackingDetails is None:
                                startFrom = startFrom
                        else:
                                startFrom = trackingDetails['latestUpdatedDate']
                        stories_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/stories"
                        stories = self.getResponse(stories_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
                        activities_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/activity?occurred_after="+startFrom
                        activities = self.getResponse(activities_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
                        if len(activities) > 0:
                                trackingDetails = { 'latestUpdatedDate' : activities[0]['occurred_at']}
                                self.tracking[str(project_id)] = trackingDetails
                                self.updateTrackingJson(self.tracking)
                                for story in stories:
                                        flag = 0
                                        for activity in activities:
                                                if activity['kind'] == 'story_update_activity' and activity['primary_resources'][0]['name'] == story['name']:
                                                        if len(activity['changes']) > 0:
                                                                flag = 1
                                                                data.append(self.data_prepare(story, project_id, None, activity))
                                        if flag == 0:
                                                data.append(self.data_prepare(story, project_id, None, activity=None))
        if len(data) > 0:
                data.reverse()
                self.publishToolsData(data, storyMetadata)
        print all_projects
        for project in all_projects:
            iteration_url = baseEndPoint + '/services/v5/projects/' + str(project) + '/iterations?fields=number%2Cproject_id%2Clength%2Cteam_strength%2Cstories%2Cstart%2Cfinish%2Ckind%2Cvelocity'
            iterations = self.getResponse(iteration_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
            trackingDetails = self.tracking.get('iteration_'+str(project), None)
            if trackingDetails is None:
                pass
            else:
                trackingDetails = self.tracking.get('iteration_'+str(project), None).get('iterationNmber', None)
            for iteration in iterations:
                #ITERATION DETAILS
                injectDataIteration = {}
                iteration_data = []
                injectDataIteration['iterationNmber'] = iteration['number']
                if trackingDetails is None:
                    trackingupdate = {'iterationNmber' : iteration['number']}
                    previous_iteration_number = 0
                else:
                    previous_iteration_number = trackingDetails
                    trackingupdate = {'iterationNmber' : iteration['number']}
                if int(iteration['number']) > int(previous_iteration_number):
                    self.tracking['iteration_'+str(project)] = trackingupdate
                    injectDataIteration['projectId'] = iteration['project_id']
                    injectDataIteration['numberOfStoriesAttached'] = iteration['length']
                    injectDataIteration['iterationStartTime'] = iteration['start']
                    injectDataIteration['iterationFinishTime'] = iteration['finish']
                    injectDataIteration['velocity'] = iteration['velocity']
                    if len(iteration['stories']) > 0:
                        for story in iteration['stories']:
                            injectDataIteration['storyId'] = story['id']
                            iteration_data.append(injectDataIteration)
                            self.publishToolsData(iteration_data, relationMetadata)
                            iteration_data = []
            self.updateTrackingJson(self.tracking)
    def data_prepare(self, story, project_id, workspace_name, activity):
        #STORY DETAILS
        responseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)
        injectdata={}
        injectdata['storyId'] = story['id']
        injectdata['createdAt'] = story['created_at']
        injectdata['storyType'] = story['story_type']
        injectdata['storyName'] = story['name']
        injectdata['currentStoryState'] = story['current_state']
        injectdata['projectId'] = project_id
        injectdata['workSpaceName'] = workspace_name
        if len(story['labels']) > 0:
                for label in story['labels']:
                    injectdata['epicName'] = label['name']
                    injectdata['epicId'] = label['id']
        if activity != None:
            if len(activity['changes']) > 0:
                for change_len in range(0, len(activity['changes'])):
                    change = activity['changes'][change_len]
                    if 'estimate' in change['original_values']:
                        injectdata['storyPoint'] = change['new_values']['estimate']
                        injectdata['lastUpdatedTime'] = change['new_values']['updated_at']
                    elif 'current_state' in change['original_values']:
                        injectdata['storyState'] = change['new_values']['current_state']
                        injectdata['lastUpdatedTime'] = change['new_values']['updated_at']
        return injectdata
if __name__ == "__main__":
    PivotalTrackerAgent()        
