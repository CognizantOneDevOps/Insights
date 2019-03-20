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

class PivotalTrackerAgent(BaseAgent):
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
        #This will limit the api call data to this number
        limit=10
        offset=0
        #For iteration what will be number to start from. Ex: if current iteration is 50 then the iteration loop will start to run from 30.
        jump_number = 20
        
        for workspace in workspaces:
                workspace_id = workspace['id']
                workspace_name = workspace['name']
                for project_id in workspace['project_ids']:
                        all_projects.append(project_id)
                        self.prepare_data_for_project(workspace_name, project_id, data)
                                                
        projects_url = baseEndPoint + "/services/v5/projects"
        projects = self.getResponse(projects_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
        for project in projects:
                project_id = project['id']
                if project_id not in all_projects:
                        all_projects.append(project_id)                        
                        self.prepare_data_for_project(None, project_id, data)
                        if len(data) > 0:
                            data.reverse()
                            self.publishToolsData(data, storyMetadata)
                            data = []
        
        print all_projects
        for project in all_projects:
            backlog_stories = []
            total = []
            backlog_url = baseEndPoint + '/services/v5/projects/' + str(project) + '/iterations?scope=backlog'
            backlogs = self.getResponse(backlog_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
            if len(backlogs) > 0 and len(backlogs[0]['stories']) > 0:
                for backlog in backlogs[0]['stories']:
                    if str(project) +'_'+ str(backlog['id']) not in backlog_stories:
                        backlog_stories.append(str(project)+'_'+str(backlog['id']))
            trackingDetails = self.tracking.get('iteration_'+str(project), None)
            if trackingDetails is None:
                iteration_from_tracking = 0
                pass
            else:
                trackingDetails = self.tracking.get('iteration_'+str(project), None).get('iterationNumber', None)
                
                iteration_from_tracking = int(trackingDetails)
            limit_iteration = limit
            offset_iteration=offset#This will be the starting point of that particular api call iteration
            if iteration_from_tracking < jump_number:
                offset = 0
            else:
                offset = iteration_from_tracking - jump_number
            flag = True
            while flag == True:
                iteration_url = baseEndPoint + '/services/v5/projects/' + str(project) + '/iterations?limit='+str(limit_iteration)+'&offset='+str(offset_iteration)+'&fields=number%2Cproject_id%2Clength%2Cteam_strength%2Cstories%2Cstart%2Cfinish%2Ckind%2Cvelocity%2Canalytics'
                iterations = self.getResponse(iteration_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
                if len(iterations) > 0:
                    offset_iteration = offset_iteration + limit_iteration
                else:
                    flag = False
                for iteration in iterations:
                    #ITERATION DETAILS
                    injectDataIteration = {}
                    iteration_data = []
                    injectDataIteration['iterationNumber'] = iteration['number']
                    #---------------------------------------------------------------------------
                    current_epoch_time = time.time()
                    iteration_start_time = iteration['start']
                    iteration_end_time = iteration['finish']
                    pattern = '%Y-%m-%dT%H:%M:%SZ'
                    iteration_start_time = int(time.mktime(time.strptime(iteration_start_time, pattern)))
                    iteration_end_time = int(time.mktime(time.strptime(iteration_end_time, pattern)))
                    #---------------------------------------------------------------------------
                    epic_name = ''
                    if trackingDetails is None:
                        trackingupdate = ''
                        if current_epoch_time >= iteration_start_time and current_epoch_time <= iteration_end_time:#added
                            trackingupdate = {'iterationNumber' : iteration['number']}
                            self.tracking['iteration_'+str(project)] = trackingupdate
                        previous_iteration_number = 0
                    else:
                        previous_iteration_number = trackingDetails
                        if current_epoch_time >= iteration_start_time and current_epoch_time <= iteration_end_time:#added
                            trackingupdate = {'iterationNumber' : iteration['number']}
                            self.tracking['iteration_'+str(project)] = trackingupdate
                    #self.tracking['iteration_'+str(project)] = trackingupdate
                    injectDataIteration['projectId'] = iteration['project_id']
                    injectDataIteration['numberOfStoriesAttached'] = iteration['length']
                    injectDataIteration['iterationStartTime'] = iteration['start']
                    injectDataIteration['iterationFinishTime'] = iteration['finish']
                    injectDataIteration['velocity'] = iteration['velocity']
                    injectDataIteration['cycleTime'] = iteration['analytics']['cycle_time']
                    injectDataIteration['rejectionRate'] = iteration['analytics']['rejection_rate']
                    if len(iteration['stories']) > 0:
                        for story in iteration['stories']:
                            injectDataIteration['storyId'] = story['id']
                            if str(project) + '_' + str(story['id']) in backlog_stories:
                                injectDataIteration['backLog'] = True
                            iteration_data.append(injectDataIteration)
                            self.publishToolsData(iteration_data, relationMetadata)
                            #print iteration_data
                            iteration_data = []
                self.updateTrackingJson(self.tracking)
    def data_prepare(self, story, project_id, workspace_name, epic_list, activity):
        #STORY DETAILS
        responseTemplate = self.config.get('dynamicTemplate', {}).get('responseTemplate', None)
        data_story = []
        injectdata={}
        injectdata['storyId'] = story['id']
        injectdata['createdAt'] = story['created_at']
        injectdata['storyType'] = story['story_type']
        if 'estimate' in story:
            injectdata['estimate'] = story['estimate']
        else:
            pass
        injectdata['storyName'] = story['name']
        injectdata['currentStoryState'] = story['current_state']
        injectdata['projectId'] = project_id
        injectdata['workSpaceName'] = workspace_name
        if len(story['labels']) > 0:
                #---------------------------------------------------------------------------
                epic_name = ''
                epic_id = ''
                label_name=''
                label_id=''
                for label in story['labels']:
                    if label['name'] in epic_list:
                        epic_name = str(label['name']) +','+ epic_name
                        epic_id = str(label['id'])+','+ epic_id
                    else:
                        label_name = str(label['name']) +','+ label_name
                        label_id = str(label['id'])+','+ label_id
                if epic_name != '':
                    injectdata['epicName'] = epic_name[:-1]
                    injectdata['epicId'] = epic_id[:-1]
                else:
                    injectdata['labelName'] = label_name[:-1]
                    injectdata['labelId'] = label_id[:-1]
                #--------------------------------------------------------------------------
        if activity != None:
            if len(activity['changes']) > 0:
                for change_len in range(0, len(activity['changes'])):
                    change = activity['changes'][change_len]
                    if 'original_values' in change and 'estimate' in change['original_values']:
                        injectdata['storyPoint'] = change['new_values']['estimate']
                        #injectdata['lastUpdatedTime'] = change['new_values']['updated_at']
                    elif 'original_values' in change and 'current_state' in change['original_values']:
                        injectdata['storyState'] = change['new_values']['current_state']
                        #injectdata['lastUpdatedTime'] = change['new_values']['updated_at']
                    if 'new_values' in change and 'description' in change['new_values']:
                        injectdata['description'] = change['new_values']['description']
                    #-------------------------------CommitID
                    if 'new_values' in change and 'commit_identifier' in change['new_values']:
                        injectdata['attachedCommitId'] = change['new_values']['commit_identifier']
                        injectdata['attachedScmType'] = change['new_values']['commit_type']
                        injectdata['attachedCommitMessage'] = change['new_values']['text']
                    #-------------------------------CommitID                    
        return injectdata
    
    def prepare_data_for_project(self, workspace_name, project_id, data):

        userid = self.config.get("userid", '')
        passwd = self.config.get("passwd", '')
        baseEndPoint = self.config.get('baseEndPoint', '')
        accessToken = self.config.get('token')
        reqHeaders = {'x-trackertoken': accessToken}
        startFrom = self.config.get("startFrom", '')
        timeStampFormat = self.config.get('timeStampFormat')
        storyMetadata = self.config.get('dynamicTemplate', {}).get('storyMetadata', None)
        startFrom = parser.parse(startFrom)
        startFrom = startFrom.strftime(timeStampFormat)
        limit=10
        offset=0
        
        #-------------------------------CommitID
        story_update_activity_category = ["story_update_activity", "comment_create_activity"]
        #-------------------------------CommitID

        if workspace_name != None:
            trackingDetails = self.tracking.get(workspace_name+ ' ' +str(project_id), None)
        elif workspace_name is None:

            trackingDetails = self.tracking.get(str(project_id), None)
        if trackingDetails is None:
            startFrom = startFrom
        else:
            startFrom = trackingDetails['latestUpdatedDate']
        epic_list=[]
        epics_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/epics"        
        epics = self.getResponse(epics_url, 'GET', userid, passwd, "", reqHeaders=reqHeaders)        
        for epic in epics:
            epic_list.append(epic['label']['name'])
        flag_story = True
        story_details = []
        story_start_date=startFrom
        limit_proj=limit
        offset_proj=offset
        while flag_story == True:
            stories_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/stories?updated_after="+startFrom+"&limit="+str(limit_proj)+"&offset="+str(offset_proj)
            stories = self.getResponse(stories_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
            if len(stories) == 0:
                flag_story=False
            else:
                offset_proj=offset_proj+limit_proj
                story_start_date=stories[-1]['created_at']
                story_details.append(stories)
        limit_proj=limit
        offset_proj=offset
        flag_activity = True
        project_last_updation_time = 0
        while flag_activity == True:
            activities_url = baseEndPoint + "/services/v5/projects/"+str(project_id)+"/activity?occurred_after="+startFrom+"&limit="+str(limit_proj)+"&offset="+str(offset_proj)

            activities = self.getResponse(activities_url, 'GET', userid, passwd, None, reqHeaders=reqHeaders)
            if len(activities) > 0:
                offset_proj=offset_proj+limit_proj
                latest_update_time = int(time.mktime(time.strptime(activities[0]['occurred_at'], timeStampFormat)))

                if project_last_updation_time < latest_update_time:
                    project_last_updation_time = latest_update_time
                    trackingDetails = { 'latestUpdatedDate' : activities[0]['occurred_at']}
                if workspace_name != None:
                    self.tracking[workspace_name+ ' ' +str(project_id)] = trackingDetails
                elif workspace_name is None:

                    self.tracking[str(project_id)] = trackingDetails


                for length_story_details in range(len(story_details)):
                    for story in story_details[length_story_details]:
                        flag = 0
                        for activity in activities:
                            if activity['kind'] in story_update_activity_category and activity['primary_resources'][0]['name'] == story['name']:
                                if len(activity['changes']) > 0:
                                    flag = 1
                                    #self.publishToolsData(self.data_prepare(story, project_id, workspace_name, epic_list, activity), storyMetadata)
                                    data.append(self.data_prepare(story, project_id, workspace_name, epic_list, activity))
                                
                                

                        if flag == 0:
                            #self.publishToolsData(self.data_prepare(story, project_id, workspace_name,epic_list, activity=None), storyMetadata)
                            data.append(self.data_prepare(story, project_id, workspace_name, epic_list, activity=None))
                        #self.publishToolsData(self.data_prepare(story, project_id, workspace_name, activity=None), storyMetadata)
            else:
                flag_activity = False
if __name__ == "__main__":
    PivotalTrackerAgent()        
