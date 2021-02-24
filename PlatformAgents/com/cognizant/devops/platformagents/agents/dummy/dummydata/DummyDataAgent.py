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

from _hashlib import new
'''
Created on Jan 10, 2021
@author: 666973 Surbhi Gupta
'''
from dateutil import parser
import datetime
from BaseAgent import BaseAgent
import time
import calendar
import random
import string
import os
import sys
import uuid
import json
import logging.handlers


class DummyDataAgent(BaseAgent):

    def process(self):
        # Project Variables
        self.project_names = ['PaymentServices', 'MobileServices', 'ClaimFinder', 'AgentLocator']
        self.projectKeys = ['PS', 'MS', 'CF', 'AL']
        self.projectIds = ["6", '7', '8', '9']
        self.boardIdForProjects = [101, 201, 301, 401]   
        self.startFrom = self.config.get("startFrom")
        self.startDate = parser.parse(self.startFrom, ignoretz=True)
        self.start_date_days = self.config.get("start_date_days")
                
        # JIRA Variables        
        self.numberofRelease = self.config.get("numberofReleasesRequired") 
        self.numberOfSprintsInRelease = self.config.get("numberOfSprintsInRelease")
        self.numberofDaysInSprint = self.config.get("numberofDaysInSprint")
        self.createSprintData = self.config.get('createSprintData')              
        self.Priority = ['High', 'Low', 'Medium']
        self.jiraUsers = ["Vicky" , "Sam", "John", "Tom", "Adam"] 
        self.issueTypes = ['Story', 'Task'] 
        self.storyPoints = ["8" , "5", "3", "2", "1"] 
        self.isStoryClosed = ["True", "False"] 
        
        # GIT Variables
        self.repo = ['Insights', 'Spinnaker', 'OnBot', 'BuildOn']
        self.author = ['John', 'Bruno', 'Charlie', 'Tom', 'Wilson']
        self.branches = ['NewModules', 'BugFixes', 'Enhancements', 'Testing']
        self.master_branches = ['InsightsEnterprise', 'master']
        
        # JENKINS Variables
        self.jenkins_status = ['Success', 'Failed', 'Aborted', 'Unstable']
        # self.jenkins_status = ['Success']
        self.master = ['master1', 'master2']
        self.job_name = ['BillingApproved', 'BillingInvoice', 'ClaimValidated', 'ClaimProcessed', 'deploy']
        self.jen_env = ['PROD', 'DEV', 'INT', 'RELEASE']
        self.buildUrl = ['productv4.1.devops.com', 'productv4.2.devops.com', 'productv4.3.devops.com', 'productv4.4.devops.com']
       
        # SONAR Variables
        self.sonar_quality_gate_Status = ['OK', 'ERROR']
        self.sonar_coverage = ['35', '50', '70', '85']
        self.sonar_complexity = ['35', '50', '70', '85', '100', '125']
        self.sonar_duplicate = ['15', '25', '45', '60']
        self.sonar_techdepth = ['3', '5', '17', '25', '21']
        self.resourceKey = ['09', '099', '89', '32']
        self.sonar_codeCoverage = ['40', '50', '60', '70', '80', '90']
        
        # RUNDECK Variables
        self.rundeck_env = ['PROD', 'DEV', 'INTG', 'SIT', 'UAT'] 
        self.rundeck_status = ['succeeded', 'failed', 'aborted']    
        
        # Nexus Variables
        # self.artifacts_name = ["onlinebanking-0.0.1-20160505.105537-19.war","anybank-0.0.1-20160526.093210-15.war","demoapp-0.0.1-20160425.162709-16.war","demomavenapp-0.0.1-20160926.081623-11.war","iSightonlinebanking-0.0.1-20161213.042537-15.war","demomavenappxl-0.0.1-20161213.140419-19.war"  ]
        self.nexus_status = ['succeeded', 'failed', 'aborted'] 
        self.artifacts_id = ["Service", "UI", "Engine", "Webhook", "Mockserver", "Workflow"]
        self.group_ids = ["com.cts.paymentService", 'com.cts.mobileServices', 'com.cts.claimFinder', 'com.cts.agentLocator']
        self.repoIdSnapshot = ["PaymentService-buildOn", "MobileServices-buildOn", "ClaimFinder-buildOn", "AgentLocator-buildOn"]
        self.repoIdRelease = ["PaymentService-Release", "MobileServices-Release", "ClaimFinder-Release", "AgentLocator-Release"]
        
        # QTest Variables 
        self.module = ["Correlation Builder", " Webhook Module", "Agent Management", "Data Archival"]        
        self.assignedto = ['Charlie', 'Steve', 'Andrew', 'Ricky']      
        self.severity = ['Critical', 'Non-Critical']
        self.submitter = ['Tony', 'John', 'Adam', 'Sumit', 'Jack']
        
        self.releaseStatus = ["Success" , "Bug Raised"] #, "Rollback"
            
        # Looping over each project 
        try :
            for projectName in self.project_names :
                self.projectName = projectName
                self.releaseBugData = []
                self.createProjectData() 
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)#                
                
    def createProjectData (self): 
        try :       
            self.projectKey = self.projectKeys[self.project_names.index(self.projectName)]
            self.projectId = self.projectIds[self.project_names.index(self.projectName)]
            self.groupId = self.group_ids[self.project_names.index(self.projectName)]
            self.noOfSprintsClosedSoFar = 0  # To Track Total Number of Sprints closed for each project
            self.jenkinsBuildNumber = 01  # Total Number of Builds trigerred in each project
            self.pullRequestNo = 1  # Total Number of pull requests raised in each project
            time_offset_hours = (random.randint(01, 04))
            time_offset_seconds = (random.randint(101, 800))
            self.releaseStartDate = self.startDate #datetime.datetime.now() - datetime.timedelta(days=self.start_date_days)
            self.sprintEndDate = self.startDate #datetime.datetime.now() - datetime.timedelta(days=self.start_date_days)
            self.sprintCompleteDate = self.sprintEndDate 
            self.releaseEndDate = (self.releaseStartDate +  self.numberOfSprintsInRelease *(datetime.timedelta(days=self.numberofDaysInSprint)) )
            self.noOfEpicsCreatedSoFar = 0  # Total Number of epics created for each project
            self.issueCreationStarted = (5 * self.numberofRelease) + 1  # To track starting points of the issue keys, as max epics can be created is 5 in a relase..It will track further ekys as well.
            release = 1
            #self.releaseDate = 
            while release <= (self.numberofRelease) :
                self.startReleaseWork(release)
                release = release + 1
                self.releaseStartDate = self.sprintEndDate
                self.releaseEndDate = self.releaseEndDate = (self.releaseStartDate +  self.numberOfSprintsInRelease *(datetime.timedelta(days=self.numberofDaysInSprint)) )
                
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)
            
    def startReleaseWork(self, release):
        try :                       
            self.releaseVersion = release
            self.updatedAt = ""
            self.totalRequirements = []
            self.totalTestCases = []
            self.spillOverStories = []                   
            self.detailsOfEpicsInRelease = []
            self.listofEpicsKeyInCurrentRelease = []          
            self.totalIssuesInRelease = [] 
            self.releaseData =[] 
            numberOfEpicsRequiredForRelease = (random.randint(02, 05)) #Range for epic releases         
            # Creating epics in the beginning of the Release.
            epic_counts = 1    
            releaseSample = {}
            releaseSample["fixVersion"] = "V." + str(self.releaseVersion)
            releaseSample["startDate"] = (self.releaseStartDate).strftime("%Y-%m-%dT%H:%M:%S")
            releaseSample ["endDate"]   =    (self.releaseEndDate).strftime("%Y-%m-%dT%H:%M:%S")
            releaseSample["numberOfEpics"] =  numberOfEpicsRequiredForRelease  
            self.releaseData.append(releaseSample) 
            releaseMetadata = {"labels" : ["ALM", "JIRA", "DATA","RELEASE"]}                
            self.publishToolsData(self.releaseData, releaseMetadata)  
            while epic_counts <= numberOfEpicsRequiredForRelease :  
                self.createEpicsForRelease(epic_counts) 
                epic_counts = epic_counts + 1                
            jiraMetadata = {"labels" : ["ALM", "JIRA", "DATA"], "dataUpdateSupported" : True, "uniqueKey" : ["key"]}                
            self.publishToolsData(self.detailsOfEpicsInRelease, jiraMetadata)       
            for epic in  self.detailsOfEpicsInRelease :  # Moving epics Relea
                epicKey = epic['key']
                self.change_Log(epicKey, "status", "In Progress", "To Do", self.updatedAt)                           
            # self.totalIssuesInRelease = np.concatenate((self.totalIssuesInRelease, self.detailsOfEpicsInRelease))
            self.numberofSprintInCurrentRelease = self.numberOfSprintsInRelease
            rangeNumber = 1  
            self.sprintStartDate = self.releaseStartDate
            self.sprintEndDate =  (self.sprintStartDate + datetime.timedelta(days=self.numberofDaysInSprint))
            self.isRollbackRelease = False
            while rangeNumber <= self.numberofSprintInCurrentRelease  :  # Looping over the sprints              
                       
                self.startSprintWork(rangeNumber)
                
                if rangeNumber == self.numberofSprintInCurrentRelease:
                    isStoriesReleaseReady = True
                else :
                    isStoriesReleaseReady = False                             
                rangeNumber = rangeNumber + 1            
                if isStoriesReleaseReady :
                    status = self.serviceNowProcessing(release)
                    if status == "Success" :
                        issueDetailData = []
                        for issue in self.totalIssuesInRelease:
                            time_offset_seconds = (random.randint(101, 300))
                            changingDate= self.releaseEndDate - datetime.timedelta(seconds=time_offset_seconds)                            
                            self.change_Log(issue['key'], "status", "Done", "Ready for Release", changingDate)
                            issue["status"] = "Done"   
                            lastUpdated_In_format = changingDate.strftime("%Y-%m-%dT%H:%M:%S")   
                            issue["lastUpdated"]   =    lastUpdated_In_format
                            issue["lastUpdatedEpoch"]  =    int(time.mktime(time.strptime(lastUpdated_In_format, "%Y-%m-%dT%H:%M:%S")))                                                                
                            issue['resolutionDate'] = lastUpdated_In_format
                            issue['resolutionDateEpoch'] = int(time.mktime(time.strptime(lastUpdated_In_format, "%Y-%m-%dT%H:%M:%S")))                                         
                            issueDetailData.append(issue) 
                        for issue in self.detailsOfEpicsInRelease:
                            time_offset_seconds = (random.randint(101, 300))
                            changingDate= self.releaseEndDate - datetime.timedelta(seconds=time_offset_seconds)                            
                            self.change_Log(issue['key'], "status", "Done", "In Progress", changingDate)
                            issue["status"] = "Done"   
                            lastUpdated_In_format = changingDate.strftime("%Y-%m-%dT%H:%M:%S")   
                            issue["lastUpdated"]   =    lastUpdated_In_format
                            issue["lastUpdatedEpoch"]  =    int(time.mktime(time.strptime(lastUpdated_In_format, "%Y-%m-%dT%H:%M:%S")))                                                                
                            issue['resolutionDate'] = lastUpdated_In_format
                            issue['resolutionDateEpoch'] = int(time.mktime(time.strptime(lastUpdated_In_format, "%Y-%m-%dT%H:%M:%S")))                                         
                            issueDetailData.append(issue) 
                        jiraMetadata = {"labels" : ["ALM", "JIRA", "DATA"], "dataUpdateSupported" : True, "uniqueKey" : ["key"]}                
                        self.publishToolsData(issueDetailData, jiraMetadata)                  
                     
                            
                self.sprintStartDate = self.sprintEndDate
                self.sprintEndDate =  (self.sprintStartDate + datetime.timedelta(days=self.numberofDaysInSprint))         
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno,ex)    
            
    def createEpicsForRelease(self, epic_count):
        try :
            epicKey = self.projectKeys[self.project_names.index(self.projectName)] + '-' + str(self.noOfEpicsCreatedSoFar + 1)   
            jiraSample = {}                       
            jiraSample['key'] = epicKey                    
            jiraSample['priority'] = random.choice(self.Priority)
            time_offset_hours_epic = (random.randint(01, 24))
            time_offset_seconds_epic = (random.randint(101, 800))         
            createdDate = self.releaseStartDate + datetime.timedelta(hours=time_offset_hours_epic, seconds=time_offset_seconds_epic)
            self.updatedAt = createdDate        
            jiraSample['createdDate'] = createdDate.strftime("%Y-%m-%dT%H:%M:%S")                    
            jiraSample['lastUpdated'] = createdDate.strftime("%Y-%m-%dT%H:%M:%S")
            jiraSample['lastUpdatedEpoch'] = int(time.mktime(time.strptime(createdDate.strftime("%Y-%m-%dT%H:%M:%S"), "%Y-%m-%dT%H:%M:%S")))   
            jiraSample['status'] = 'To Do'
            jiraSample['issueType'] = 'Epic'
            jiraSample['projectName'] = self.projectName       
            jiraSample['reporter'] = random.choice(self.jiraUsers)
            self.detailsOfEpicsInRelease.append(jiraSample)       
            self.listofEpicsKeyInCurrentRelease.append(epicKey)        
            self.noOfEpicsCreatedSoFar = self.noOfEpicsCreatedSoFar + 1 
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)
            
    def startSprintWork(self, rangeNumber):
        try:
            sprintSample = {} 
            sprint_data = []                   
            #self.sprintStartDate = self.sprintEndDate
            self.detailsOfIssues = []  
            self.listofIssueInCurrentSprint = []  
            issue_counts = 1
            time_offset_hours = (random.randint(01, 24))
            time_offset_seconds = (random.randint(101, 800))   
            numberOfIssuesToBeCreatedForSprint = (random.randint(10, 30))   #  Number of issues in a sprint            
            #sprintEndDate = (self.sprintStartDate + datetime.timedelta(days=self.numberofDaysInSprint))
            #sprintCompleteDate = (self.sprintEndDate + datetime.timedelta(hours=time_offset_hours, seconds=time_offset_seconds)) 
            sprint_EndDate_InFormat = self.sprintEndDate.strftime("%Y-%m-%dT%H:%M:%S") 
            sprint_StartDate_InFormat = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")                    
            sprint_CompleteDate_InFormat = self.sprintCompleteDate.strftime("%Y-%m-%dT%H:%M:%S")                   
            epoch_End = int(time.mktime(time.strptime(sprint_EndDate_InFormat, "%Y-%m-%dT%H:%M:%S")))                                  
            epoch_Start = int(time.mktime(time.strptime(sprint_StartDate_InFormat, "%Y-%m-%dT%H:%M:%S")))                   
            epoch_Complete = int(time.mktime(time.strptime(sprint_CompleteDate_InFormat , "%Y-%m-%dT%H:%M:%S")))                                                       
            sprint_Name = self.projectName + " SPRINT " + str(self.noOfSprintsClosedSoFar + 1)                              
            sprintSample['sprintName'] = (sprint_Name)
            self.sprintId = self.projectKey + '_SPRINT_' + str(self.noOfSprintsClosedSoFar + 1)
            sprintSample['sprintId'] = self.sprintId
            sprintSample['boardId'] = self.boardIdForProjects[self.project_names.index(self.projectName)]
            sprintSample['projectName'] = self.projectName
            sprintSample['sprintStartDate'] = sprint_StartDate_InFormat
            sprintSample['sprintEndDate'] = sprint_EndDate_InFormat                    
            sprintSample['sprintStartDateEpoch'] = epoch_Start
            sprintSample['sprintEndDateEpoch'] = epoch_End 
            sprintSample['insightsTimeX'] = self.sprintEndDate.strftime("%Y-%m-%dT%H:%M:%SZ")
            sprintSample['insightsTime'] = epoch_End 
            gmt = time.gmtime()
            currentTime = calendar.timegm(gmt)
            # Creating issues for each sprint
           
                
            for releaseBug in self.releaseBugData: #Any Bug created in the previous release
                self.totalIssuesInRelease.append(releaseBug)
                self.change_Log(releaseBug['key'], "status", "In Progress", "To Do", self.updatedAt) 
                releaseBug['sprints']= self.sprintId
                self.workingOnIssues(releaseBug, rangeNumber,None,None,None,None,None,True)
                    #def workingOnIssues(self, detail, rangeNumber, git_repo=None, git_branch=None, git_toBranch=None, git_author=None, originalStory=None, forceCloseInCurrentSprint=False):   

            if self.isRollbackRelease:
                sprintsAdded = []
                issueDetailData = []
                for issue in self.totalIssuesInRelease:
                    sprintsAdded.append(issue['sprints'])
                    sprintsAdded.append(self.sprintId)                            
                    issue['sprints'] = sprintsAdded
                    issueDetailData.append(issue) 
                jiraMetadata = {"labels" : ["ALM", "JIRA", "DATA"], "dataUpdateSupported" : True, "uniqueKey" : ["key"]}                
                self.publishToolsData(issueDetailData, jiraMetadata)  
            
            
            else  :
                
                while issue_counts <= numberOfIssuesToBeCreatedForSprint : 
                    self.creatingIssue()
                    issue_counts = issue_counts + 1 
                
                for spillStory in self.spillOverStories: #Loop for working on sprill over stories from the previous sprints
                    sprintsAdded = []
                    updateJiraNode = []
                    sprintsAdded.append(spillStory['sprints'])
                    sprintsAdded.append(self.sprintId)            
                    time_offset_seconds = (random.randint(101, 800))                    
                    time_offset_hours = (random.randint(01, 05))
                    self.updatedAt = self.sprintStartDate + datetime.timedelta(hours=time_offset_hours, seconds=time_offset_seconds)
                    spillStory['lastUpdated'] = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")
                    spillStory['lastUpdatedEpoch'] = int(time.mktime(time.strptime(self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S"), "%Y-%m-%dT%H:%M:%S")))   
                    spillStory['sprints'] = sprintsAdded
                    git_repo = spillStory["git_repo"] 
                    git_branch = spillStory["git_branch"] 
                    git_author = spillStory['git_author'] 
                    spillStory.pop("git_branch", None)
                    spillStory.pop("git_author", None)
                    spillStory.pop("git_repo", None)
                    updateJiraNode.append(spillStory)
                    self.totalIssuesInRelease.append(spillStory) 
                    jiraMetadata = {"labels" : ["ALM", "JIRA", "DATA"], "dataUpdateSupported" : True, "uniqueKey" : ["key"]}                
                    self.publishToolsData(updateJiraNode, jiraMetadata)      
                    self.workingOnIssues(spillStory, rangeNumber, git_repo, git_branch, None, git_author)
                # Started Working On Issues
                for detail in self.detailsOfIssues: 
                    self.change_Log(detail['key'], "status", "In Progress", "To Do", self.updatedAt) 
                    self.workingOnIssues(detail, rangeNumber)
                
            if (rangeNumber == (self.numberofSprintInCurrentRelease) and self.releaseVersion == (self.numberofRelease)) :
                sprintSample['state'] = "Active" 
                self.isActiveSprint = True
            else :
                sprintSample['state'] = "Closed"
                self.isActiveSprint = False
                sprintSample['sprintCompleteDate'] = sprint_CompleteDate_InFormat 
                sprintSample['sprintCompleteDateEpoch'] = epoch_Complete                                           
            sprint_data.append(sprintSample) 
            self.noOfSprintsClosedSoFar = self.noOfSprintsClosedSoFar + 1
            metadata = {  "labels":["SPRINT"], "dataUpdateSupported":True, "uniqueKey":["boardId", "sprintId"]}            
            self.publishToolsData(sprint_data, metadata)
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno,ex)    
    
    def workingOnIssues(self, detail, rangeNumber, git_repo=None, git_branch=None, git_toBranch=None, git_author=None, originalStory=None, forceCloseInCurrentSprint=False):   
        try :
            issueDetailData = []                                                       
            workingKey = detail['key']
            updatedAt = detail["lastUpdatedEpoch"] 
            self.updatedAt = datetime.datetime.fromtimestamp(updatedAt)                               
            # Generating GIT Data for every issue
            git_totalCommits = random.randint(1, 5)
            git_count = 1                        
            # Random Choosing of Author,Branch  and Repo done before calling git procressing function so that for 1 story, we have 1 branch,repo and author for n no. of commits
            if git_repo is None and git_branch is None  and  git_author is None:
                git_author = random.choice(self.author)
                git_repo = random.choice(self.repo)
                git_branch = random.choice(self.branches)
            if git_toBranch is None:
                git_toBranch = random.choice(self.master_branches)                      
            while git_count <= git_totalCommits: 
                isOrphanCommit = bool(random.getrandbits(1))                            
                isBuildSuccess = self.gitProcessing(workingKey, self.updatedAt, git_author, git_repo, git_branch, isOrphanCommit)  
                git_count = git_count + 1 
                
            if rangeNumber != self.numberofSprintInCurrentRelease :
                isStoryClosingInCurrentSprint = bool(random.getrandbits(1))
            else :                           
                isStoryClosingInCurrentSprint = True  
                                         
            if isStoryClosingInCurrentSprint or forceCloseInCurrentSprint: 
                self.totalIssuesInRelease.append(detail)                
                if  isBuildSuccess == False:                                             
                    isBuildSuccess = self.gitProcessing(workingKey, self.updatedAt, git_author, git_repo, git_branch, False, True)
                self.pull_request(workingKey, git_repo, git_branch, git_toBranch, git_author, self.updatedAt, "Open") 
                self.pull_request(workingKey, git_repo, git_branch, git_toBranch, git_author, self.updatedAt, "Merged")
                isBuildSuccess = self.gitProcessing(workingKey, self.updatedAt, "root", git_repo, git_branch, False, True)                                               
                self.change_Log(workingKey, "status", "Quality Assurance", "In Progress", self.updatedAt)
                self.change_Log(workingKey, "status", "QA In Progress", "Quality Assurance", self.updatedAt)                      
                if originalStory is not None : 
                    qtestKey = originalStory['key']
                    statusOfRequirement = self.qaTestProcessing(qtestKey, self.updatedAt, True)
                else :
                    statusOfRequirement = self.qaTestProcessing(workingKey, self.updatedAt)
                lastUpdated_In_format = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S")
                if statusOfRequirement == "Passed":
                    self.change_Log(workingKey, "status", "Ready for Release", "Quality Assurance", self.updatedAt)                   
                    metadata = {  "labels":["ALM", "JIRA", "DATA"], "dataUpdateSupported":True, "uniqueKey":["key"]}            
                    self.publishToolsData(issueDetailData, metadata) 
                    issueDetailData = []
                    originalStory = None
                    
                else :
                    bugData = []
                    bugDetail = detail.copy()                    
                    bugKey = self.projectKeys[self.project_names.index(self.projectName)] + '-' + str(self.issueCreationStarted + 1)
                    self.issueCreationStarted = self.issueCreationStarted + 1    
                    bugDetail ["key"] = bugKey
                    bugDetail ['issueType'] = "Bug"
                    bugDetail ['storyPoints'] = ""
                    bugDetail ['summary'] = "Bug Raised for " + workingKey
                    bugDetail["createdAt"] = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S");                  
                    self.change_Log(bugKey, "status", "In Progress", "To Do", self.updatedAt)                 
                    self.change_Log(workingKey, "status", "Reopened", "QA In Progress", self.updatedAt)
                    originalStory = detail.copy()
                    detail["status"] = "Reopened"  
                    detail["bugKeyAssociate"] = bugKey
                    issueDetailData.append(detail)
                    metadata = {  "labels":["ALM", "JIRA", "DATA"], "dataUpdateSupported":True, "uniqueKey":["key"]}            
                    self.publishToolsData(issueDetailData, metadata) 
                    issueDetailData = []
                    self.workingOnIssues(bugDetail, rangeNumber, git_repo, git_branch, git_toBranch, git_author, originalStory, True)                                         
                                           
            else :
                detail["git_repo"] = git_repo 
                detail["git_branch"] = git_branch 
                detail['git_author'] = git_author                                      
                self.spillOverStories.append(detail)                           
        except Exception as ex:
            exc_type, exc_obj, exc_tb = sys.exc_info()
            fname = os.path.split(exc_tb.tb_frame.f_code.co_filename)[1]
            print(exc_type, fname, exc_tb.tb_lineno)

    def creatingIssue(self): 
        jiraSample = {}                             
        issueKey = self.projectKeys[self.project_names.index(self.projectName)] + '-' + str(self.issueCreationStarted + 1)                                                  
        jiraSample['key'] = issueKey                                           
        jiraSample['priority'] = random.choice(self.Priority)
        self.updatedAt = self.sprintStartDate
        jiraSample['createdDate'] = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")        
        jiraSample['lastUpdated'] = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")
        jiraSample['lastUpdatedEpoch'] = int(time.mktime(time.strptime(self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S"), "%Y-%m-%dT%H:%M:%S")))   
        jiraSample['status'] = 'To Do'        
        issueType = random.choice(self.issueTypes)
        jiraSample['issueType'] = issueType
        jiraSample['projectName'] = self.projectName
        jiraSample['reporter'] = random.choice(self.jiraUsers)
        jiraSample['assignee'] = random.choice(self.jiraUsers)
        jiraSample['sprints'] = self.sprintId
        jiraSample['epicKey'] = random.choice(self.listofEpicsKeyInCurrentRelease)
        jiraSample['boardId'] = self.boardIdForProjects[self.project_names.index(self.projectName)]
        jiraSample['fixVersion'] = "V." + str(self.releaseVersion)
        if issueType == 'Story':
            jiraSample['storyPoints'] = random.choice(self.storyPoints)
        self.issueCreationStarted = self.issueCreationStarted + 1
        self.detailsOfIssues.append(jiraSample)
        self.requirement_data = self.createRequiremnetForQtest(issueKey)
        self.listofIssueInCurrentSprint.append(issueKey)        
        #self.totalIssuesInRelease.append(jiraSample)
        
    def createRequiremnetForQtest (self, key):
        requirement_data = []
        # self.totalTestCases = []
        testCaseIds = []
        requirementSample = {}
        requirementSample['almType'] = "requirements"
        requirementSample ['jiraKey'] = key
        requirementSample['projectName'] = self.projectName
        requirementSample['priority'] = random.choice(self.Priority)
        requirementSample['submitter'] = random.choice(self.submitter)
        requirementSample['assignee'] = random.choice(self.assignedto)
        requirementSample['severity'] = random.choice(self.severity)
        requirementSample ['module'] = random.choice(self.module)
        requirementSample['creationDate'] = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S")   
        requirement_id = "RQ_" + str(''.join([random.choice(string.digits) for n in xrange(10)]))
        requirementSample['requirement_id'] = requirement_id        
        noOfTestCases = random.randint(1, 5)
        testCase = 1
        while testCase <= noOfTestCases :
            testCaseId = self.createTestCaseForQtest(requirement_id)
            testCaseIds.append(testCaseId)
            testCase = testCase + 1
        requirementSample['test_ids'] = testCaseIds
        requirement_data.append(requirementSample)
        self.totalRequirements.append(requirementSample)
        # metadata = {  "labels":["ALM", "QTEST", "DATA"], "dataUpdateSupported":True, "uniqueKey":["requirement_id", "jiraKey"]}            
        # self.publishToolsData(requirement_data, metadata) 
        return requirementSample
    
    def createTestCaseForQtest(self, requirement_id):
        testcase_data = []
        testCaseSample = {}
        testCaseSample ['requirementId'] = requirement_id
        testCaseSample['almType'] = "test_case"
        testCaseSample['summary'] = "This test Case deals with " + requirement_id
        testCaseId = "TEST_" + str(''.join([random.choice(string.digits) for n in xrange(10)]))
        testCaseSample ['testCase_id'] = testCaseId
        testcase_data.append(testCaseSample)
        self.totalTestCases.append(testCaseSample)
        # metadata = {  "labels":["ALM", "QTEST", "DATA"], "dataUpdateSupported":True, "uniqueKey":["testCase_id"]}            
        # self.publishToolsData(testcase_data, metadata)   
        return testCaseId    
    
    def gitProcessing (self, workingKey, updatedAt, git_author, git_repo, git_branch, isOrphanCommit, isForceSuccessRequired=False):
        # git_totalCommits = random.randint(1, 12)
        # git_count = 0
        git_data = []
        jenkins_data = []              
        commitId = uuid.uuid4().hex           
        time_offset = (random.randint(101, 800))                                    
        git_date = (updatedAt + datetime.timedelta(seconds=time_offset))
        git_datetime_epoch = int(time.mktime(git_date.timetuple()))
        self.updatedAt = git_date            
        gitSample = {}            
        gitSample['gitCommitTime'] = git_date.strftime("%Y-%m-%dT%H:%M:%SZ")            
        timeStampField = "gitCommitTime",
        timeStampFormat = "%Y-%m-%dT%H:%M:%SZ",
        isEpoch = False
        if isForceSuccessRequired == False : 
            if isOrphanCommit :
                gitSample['message'] = 'This commit is associated with jira-key : # ' + workingKey
            else :
                gitSample['jiraKey'] = workingKey
                gitSample['message'] = 'This commit is associated with jira-key : ' + workingKey       
        else :
            gitSample['jiraKey'] = workingKey
            gitSample['message'] = 'Force Success : ' + workingKey  
            
        gitSample['authorName'] = git_author
        gitSample['branchName'] = git_branch
        gitSample['repoName'] = git_repo
        gitSample['commitId'] = commitId
        gitSample['toolName'] = "GIT"
        # gitSample['updatedAt'] = self.updatedAt
        gitSample['categoryName'] = "SCM"
        # git_count += 1                                    
        # git_CommitArr.append(gitSample)                 
        git_data.append(gitSample)           
        
        gitMetadata = {"labels" : ["SCM", "GIT", "DATA"]}   
        # self.updatedAt =    git_date    
        self.publishToolsData(git_data, gitMetadata, timeStampField, timeStampFormat, isEpoch)        
        isJenkinsBuildSuccess = self.jenkinsProcessing(self.updatedAt, commitId, isForceSuccessRequired) 
        return isJenkinsBuildSuccess
    
    def jenkinsProcessing(self, updatedAt, commitId, isForceSuccessRequired=False):
        jenkins_data = [] 
        time_offset = (random.randint(101, 800))
        # self.jenkinsBuildNumber = self.jenkinsBuildNumber +1                                
# print('a jenkine key '+randomJenkineBuildNumber +'  '+gitSampleData['inSightsTimeX']) #+'  '+gitSample['git_date']
        isOrphanBuild = bool(random.getrandbits(1)) 
        jenkins_date = (updatedAt + datetime.timedelta(seconds=120))
        self.updatedAt = jenkins_date
        # self.printLog('GIT Commit Id '+gitSampleData['commitId']+'  GIT Date '+ gitSampleData['inSightsTimeX'] +' Jenkine Date '+str(jenkins_date), False)
        jenkins_startTime = (jenkins_date)
        jenkins_endTime = (jenkins_date + datetime.timedelta(seconds=time_offset))
        jenkine_epochtime = int(time.mktime(jenkins_date.timetuple()))
        self.updatedAt = jenkins_endTime
        # jenkins_Buildstatus =random.choice(jenkins_status)
        jenkinsSample = {}
        # jenkinsSample['inSightsTimeX'] = (jenkins_date).strftime("%Y-%m-%dT%H:%M:%SZ")
        # jenkinsSample['inSightsTime'] = jenkine_epochtime
        jenkinsSample['startTime'] = jenkins_startTime.strftime("%Y-%m-%dT%H:%M:%SZ")
        jenkinsSample['endTime'] = jenkins_endTime.strftime("%Y-%m-%dT%H:%M:%SZ")
        jenkinsSample['duration'] = (jenkins_endTime - jenkins_startTime).seconds
        
        # jenkinsSample['sprintID'] = random.choice(sprint)
        buildNumberString = self.projectId + str(self.jenkinsBuildNumber)
        jenkinsSample['buildNumber'] = buildNumberString
        jenkinsSample['jobName'] = random.choice(self.job_name)
        jenkinsSample['projectName'] = self.projectName
        # jenkinsSample['updatedAt'] =self.updatedAt
        # jenkinsSample['projectID'] = random.choice(projectId)
        jenkinsSample['environment'] = random.choice(self.jen_env)
        jenkinsSample['buildUrl'] = random.choice(self.buildUrl)
        # jenkinsSample['result'] = random.choice(result)
        jenkinsSample['master'] = random.choice(self.master)
        jenkinsSample['toolName'] = "JENKINS"
        jenkinsSample['categoryName'] = "CI"
        # print(jenkinsSample)
        timeStampField = "startTime",
        timeStampFormat = "%Y-%m-%dT%H:%M:%SZ",
        isEpoch = False  
        # jenkinsSample['jenkins_date']=str(jenkins_date)
        # if rangeNumber < 2001 :
        if isForceSuccessRequired == False :
            jenkinsStatus = random.choice(self.jenkins_status)
            if isOrphanBuild : 
                jenkinsSample['message'] = "Build triggered Manually"
            else :
                jenkinsSample['scmcommitId'] = commitId
                jenkinsSample['message'] = "Build triggered for commit Id " + commitId                
           
            if jenkinsStatus == "Success" or jenkinsStatus == "Unstable":  # Example Maven compilation is suceess.
                isJenkinsBuildSuccess = self.sonarProcessing(buildNumberString, self.updatedAt);            
                if isJenkinsBuildSuccess == True:
                    jenkinsSample['status'] = jenkinsStatus                    
                else :
                    jenkinsSample['status'] = "Failed"                                  
           
            else :
                jenkinsSample['status'] = jenkinsStatus
                isJenkinsBuildSuccess = False
        else :
            jenkinsStatus = "Success"
            jenkinsSample['status'] = jenkinsStatus
            jenkinsSample['scmcommitId'] = commitId
            jenkinsSample['message'] = "Force Success Build triggered for commit Id " + commitId 
            isJenkinsBuildSuccess = self.sonarProcessing(buildNumberString, self.updatedAt, True); 
            # sendStatus = True
            # Nexus :- Artifacts..  (deployed artifacts in which enviornment)  (Artifacts , attach name :- relase name )
        jenkins_data.append(jenkinsSample)
        jenkinsSample = {}
        self.jenkinsBuildNumber = self.jenkinsBuildNumber + 1        
        jenkinsMetadata = {"labels" : ["CI", "JENKINS", "DATA"]}     
        self.publishToolsData(jenkins_data, jenkinsMetadata, timeStampField, timeStampFormat, isEpoch)                        
        return isJenkinsBuildSuccess
    
    def sonarProcessing(self, buildNumberString, updatedAt, isForceSuccessRequired=False):
        sonar_data = []
        isManualAnalysis = bool(random.getrandbits(1)) 
        ramdomSonarKey = str(''.join([random.choice(string.digits) for n in xrange(10)]))
        sonar_date = (updatedAt + datetime.timedelta(seconds=120))       
        time_offset = (random.randint(101, 800))
        # self.printLog('Jenkine build number '+jenkinsSampleData['buildNumber']+' Jenkine Date '+jenkinsSampleData['inSightsTimeX']+' Sonar Date '+str(sonar_date), False)
        sonar_startTime = sonar_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        sonar_endTime = (sonar_date + datetime.timedelta(seconds=time_offset)).strftime("%Y-%m-%dT%H:%M:%SZ")
        self.updatedAt = sonar_date + datetime.timedelta(seconds=time_offset) 
        sonarSample = {}
        # sonarSample['inSightsTimeX'] = sonar_date.strftime("%Y-%m-%dT%H:%M:%SZ")
        # sonarSample['inSightsTime'] = int(time.mktime(sonar_date.timetuple()))
        sonarSample['startTime'] = sonar_startTime
        sonarSample['endTime'] = sonar_endTime
        sonarSample['projectname'] = self.projectName
        # sonarSample['ProjectID'] = random.choice(projectId)
        # sonarSample['ProjectKey'] = random.choice(sonar_key)
        sonarSample['resourceKey'] = random.choice(self.resourceKey)
        # if rangeNumber < (jenkine_success_build - 200) :
        sonarSample['sonarKey'] = ramdomSonarKey        
        sonarSample['sonarCoverage'] = random.choice(self.sonar_coverage)
        sonarSample['sonarComplexity'] = random.choice(self.sonar_complexity)
        sonarSample['sonarDuplicateCode'] = random.choice(self.sonar_duplicate)
        sonarSample['sonarTechDepth'] = random.choice(self.sonar_techdepth)
        sonarSample['code_coverage'] = random.choice(self.sonar_codeCoverage)
        sonarSample['toolName'] = "SONAR"
        sonarSample['categoryName'] = "CODEQUALITY"
        if isForceSuccessRequired == False:
            sonarStatus = random.choice(self.sonar_quality_gate_Status)
            sonarSample['sonarQualityGateStatus'] = sonarStatus
            if isManualAnalysis:
                sonarSample['message'] = "Manually Initiated analysis"
                buildNumberString = None 
                # isJenkinsBuildSuccess ="Failed" # changing the return status  for jenkins build.
            else :
                sonarSample['message'] = "Triggered by jenkins"
                sonarSample['jenkinsBuildNumber'] = buildNumberString
                # isJenkinsBuildSuccess = sonarStaus        
           
            if sonarStatus == "OK":                
                isJenkinsBuildSuccess = self.nexusProcessing(buildNumberString, self.updatedAt);
            else :
                isJenkinsBuildSuccess = False 
            if  isManualAnalysis :
                 isJenkinsBuildSuccess = False
        else :  
            sonarStatus = "OK"
            sonarSample['sonarQualityGateStatus'] = sonarStatus
            sonarSample['jenkinsBuildNumber'] = buildNumberString
            sonarSample['message'] = "Force Success"
            isJenkinsBuildSuccess = self.nexusProcessing(buildNumberString, self.updatedAt, True, True);
        
        sonar_data.append(sonarSample)
        timeStampField = "startTime",
        timeStampFormat = "%Y-%m-%dT%H:%M:%SZ",
        isEpoch = False
        sonarMetadata = {"labels" : ["CODEQUALITY", "SONAR", "DATA"]}
        self.publishToolsData(sonar_data, sonarMetadata, timeStampField, timeStampFormat, isEpoch)
        return isJenkinsBuildSuccess     
    
    def nexusProcessing(self, buildNumberString, updatedAt, isSnapshot=True, isForceSuccessRequired=False):
        nexus_data = []
        nexus_date = (updatedAt + datetime.timedelta(seconds=120)) 
        nexus_Date_in_format = nexus_date.strftime("%Y-%m-%dT%H:%M:%S")
        nexus_date_epoch = int(time.mktime(time.strptime(nexus_Date_in_format, "%Y-%m-%dT%H:%M:%S")))
        # time_offset = (random.randint(101, 800))
        # nexus_startTime = nexus_date
        # nexus_endTime = (nexus_date + datetime.timedelta(seconds=time_offset))
        self.updatedAt = nexus_date
        if isForceSuccessRequired == False :
            nexusStatus = random.choice(self.nexus_status)
        else :
            nexusStatus = "succeeded"
        for artifactid in self.artifacts_id:
            nexusSample = {}
            nexusSample['status'] = nexusStatus
            nexusSample['artifactId'] = artifactid
            nexusSample['groupId'] = self.group_ids[self.project_names.index(self.projectName)]
            if buildNumberString is not None :
                nexusSample["jenkinsBuildNumber"] = buildNumberString
            nexusSample['projectName'] = self.projectName 
            nexusSample['uploadedDate'] = nexus_Date_in_format
            nexusSample['toolName'] = "NEXUS"
            nexusSample['categoryName'] = "ARTIFACTMANAGEMENT"
            if isSnapshot :
                if artifactid == "UI" :
                    artifactName = artifactid + "-" + str(self.releaseVersion) + str(nexus_date_epoch) + ".zip"
                elif artifactid == "Service":
                    artifactName = artifactid + "-" + str(self.releaseVersion) + str(nexus_date_epoch) + ".war"
                else :
                    artifactName = artifactid + "-" + str(self.releaseVersion) + str(nexus_date_epoch) + ".jar"
                repoName = self.repoIdSnapshot[self.project_names.index(self.projectName)]
            else :
                repoName = self.repoIdRelease[self.project_names.index(self.projectName)]
                if artifactid == "UI" :
                    artifactName = artifactid + "-" + str(self.releaseVersion) + ".zip"
                elif artifactid == "Service":
                    artifactName = artifactid + "-" + str(self.releaseVersion) + ".war"
                else :
                    artifactName = artifactid + "-" + str(self.releaseVersion) + ".jar"
            nexusSample['artifactName'] = artifactName
            nexusSample['repoName'] = repoName
            nexus_data.append(nexusSample)
        nexusMetadata = {"labels" : ["ARTIFACTMANAGEMENT", "NEXUS", "DATA"]}            
        timeStampField = "uploadedDate",
        timeStampFormat = "%Y-%m-%dT%H:%M:%S",
        isEpoch = False            
        self.publishToolsData(nexus_data, nexusMetadata, timeStampField, timeStampFormat, isEpoch)
        if nexusStatus == "succeeded":            
            isJenkinsBuildSuccess = self.rundeckProcessing(buildNumberString, self.updatedAt, isForceSuccessRequired);                       
        else :
            isJenkinsBuildSuccess = False             
        return isJenkinsBuildSuccess                  
    
    def rundeckProcessing(self, buildNumberString, updatedAt, isForceSuccessRequired=False):
        rundeck_data = []
        isManualDeployment = bool(random.getrandbits(1))
        rundeck_date = (updatedAt + datetime.timedelta(seconds=120))        
        time_offset = (random.randint(101, 800))
        rundeck_startTime = rundeck_date
        rundeck_endTime = (rundeck_date + datetime.timedelta(seconds=time_offset))
        self.updatedAt = rundeck_endTime
        rundeckSample = {}
         # rundeckSample['inSightsTimeX'] = rundeck_date.strftime("%Y-%m-%dT%H:%M:%SZ")
         # rundeckSample['inSightsTime'] = int(time.mktime(rundeck_startTime.timetuple()))
        rundeckSample['startTime'] = rundeck_startTime.strftime("%Y-%m-%dT%H:%M:%SZ")
        rundeckSample['endTime'] = rundeck_endTime.strftime("%Y-%m-%dT%H:%M:%SZ")        
        rundeckStaus = random.choice(self.rundeck_status)
        
        rundeckSample['environment'] = random.choice(self.rundeck_env)
        rundeckSample['projectName'] = self.projectName
         # if rangeNumber < (jenkine_success_build - 200) :
        if isForceSuccessRequired == False :
            if isManualDeployment :
                rundeckSample['message'] = "Manual Deployment"   
                # rundeckStaus = "failed"  # Appending property for jenkins build.
            else:      
                if buildNumberString is not None :      
                    rundeckSample['jenkinsBuildNumber'] = buildNumberString
                rundeckSample['message'] = "Deployment triggered by jenkins"   
            if rundeckStaus == "succeeded":            
                isJenkinsBuildSuccess = True                        
            else :
                isJenkinsBuildSuccess = False 
            if  isManualDeployment :
                isJenkinsBuildSuccess = False
        else : 
            isJenkinsBuildSuccess = True; 
            rundeckStaus = "succeeded" 
            rundeckSample['jenkinsBuildNumber'] = buildNumberString
            rundeckSample['message'] = "Force Success"
        rundeckSample['status'] = rundeckStaus         
        rundeckSample['toolName'] = "RUNDECK"
        rundeckSample['categoryName'] = "DEPLOYMENT"
        rundeck_data.append(rundeckSample)
        RundeckMetadata = {"labels" : ["DEPLOYMENT", "RUNDECK", "DATA"]}
         # print(len(rundeck_data))
        timeStampField = "startTime",
        timeStampFormat = "%Y-%m-%dT%H:%M:%SZ",
        isEpoch = False
         # total_record_count =total_record_count + len(rundeck_data)
        self.publishToolsData(rundeck_data, RundeckMetadata, timeStampField, timeStampFormat, isEpoch)
        return isJenkinsBuildSuccess   
    
    def qaTestProcessing(self, key, updatedAt, isForceSuccessRequired=False):   
        statusOfRequirement = "Passed"          
        requirementData = []
        testData = []
        for requirement in self.totalRequirements:
            if requirement['jiraKey'] == key :
                requirementId = requirement['requirement_id']
                for testCase in self.totalTestCases: 
                    if requirementId == testCase['requirementId']: 
                        if isForceSuccessRequired == False :         
                            statusOfTest = bool(random.getrandbits(1))
                            if (statusOfTest == False) :
                                statusOfRequirement = "Failed"
                                statusOfTest = "Failed"                                                            
                        else :
                            statusOfTest = "Success"
                            statusOfRequirement = "Passed" 
                        testCase ['status'] = statusOfTest
                        testData.append(testCase)           
                metadata = {  "labels":["ALM", "QTEST", "DATA"]}            
                self.publishToolsData(testData, metadata)
                requirement['status'] = statusOfRequirement
                requirementData.append(requirement)
                metadata = {  "labels":["ALM", "QTEST", "DATA"]}            
                self.publishToolsData(requirementData, metadata) 
                break        
        
        return statusOfRequirement
    
    def serviceNowProcessing(self, releaseVersion):
        serviceNowSample = {}
        serviceNowData = []
        self.releaseBugData = []
        serviceNowSample ['incident_id'] = 'IN_' + str(''.join([random.choice(string.digits) for n in xrange(10)]))
        serviceNowSample ['release_Version'] = "V."+str(releaseVersion)
        serviceNowSample ['project_name'] = self.projectName
        status = random.choice(self.releaseStatus)       
        if status == "Success":
           self.isRollbackRelease = False
           serviceNowSample['summary'] ="Release completed"
        elif status == "Bug Raised" :
           self.isRollbackRelease = False
           status = "Success"
           bugSample = {}           
           bugSample['lastUpdated'] = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")
           bugSample['lastUpdatedEpoch'] = int(time.mktime(time.strptime(self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S"), "%Y-%m-%dT%H:%M:%S")))   
           bugKey=self.projectKeys[self.project_names.index(self.projectName)] + '-' + str(self.issueCreationStarted + 1)
           self.issueCreationStarted = self.issueCreationStarted + 1
           bugSample ['key'] = bugKey
           bugSample ['issueType'] = "Bug"
           bugSample ["summary"] = "Issue generated in the release " + str(releaseVersion) 
           serviceNowSample['summary'] ="Bug is raised " +str(bugKey)
           self.releaseBugData.append(bugSample)                   
        elif status == "Rollback" :
            self.isRollbackRelease = True
            bugSample = {}           
            bugSample['lastUpdated'] = self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S")
            bugSample['lastUpdatedEpoch'] = int(time.mktime(time.strptime(self.sprintStartDate.strftime("%Y-%m-%dT%H:%M:%S"), "%Y-%m-%dT%H:%M:%S")))   
            bugKey=self.projectKeys[self.project_names.index(self.projectName)] + '-' + str(self.issueCreationStarted + 1)
            self.issueCreationStarted = self.issueCreationStarted + 1
            bugSample ['key'] = bugKey
            bugSample ['issueType'] = "Bug"
            bugSample ["summary"] = "Issue generated in the release " + str(releaseVersion) 
            serviceNowSample['summary'] ="Bug is raised " +str(bugKey)
            self.releaseBugData.append(bugSample)                   
            self.numberofSprintInCurrentRelease = self.numberofSprintInCurrentRelease + 1
        serviceNowSample ['status'] = status 
        serviceNowData.append(serviceNowSample)
        metadata = {  "labels":["MONITOR", "SERVICENOW", "DATA"]}            
        self.publishToolsData(serviceNowData, metadata)  
        return status         
    
    def change_Log(self, changeKey, state, toString, fromString, updatedAt):
        print(changeKey)
        print(toString)
        changeLogData = []
        changeLog = {}
        changeLog["state"] = state
        changeLog["fromString"] = fromString
        changeLog['issueKey'] = changeKey
        changeLog['toString'] = toString
        time_offset_hours_change = (random.randint(01, 24))
        time_offset_seconds_change = (random.randint(101, 800))
        time_offset_days_change = (random.randint(01, 05))
        self.updatedAt = (updatedAt + datetime.timedelta(days=time_offset_days_change, hours=time_offset_hours_change, seconds=time_offset_seconds_change))
        changeLog['changeDate'] = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S")                              
        changeLogData.append(changeLog)
        metadata = {  "labels":["CHANGE_LOG", "DATA"]}  
        self.publishToolsData(changeLogData, metadata, "changeDate", "%Y-%m-%dT%H:%M:%S", False)                              
     
    def pull_request(self, changeKey, git_repo, git_branch, git_toBranch, git_author, updatedAt, state)  :
        pullRequestData = []
        pullRequest = {}
        pullRequest["pullrequest_id"] = changeKey
        pullRequest["repo"] = git_repo
        pullRequest["fromBranch"] = git_branch
        pullRequest['toBranch'] = git_toBranch
        pullRequest['author'] = git_author
        pullRequest['state'] = state
        pullRequest ['jiraKey'] = changeKey
        time_offset_hours_change = (random.randint(01, 24))
        time_offset_seconds_change = (random.randint(101, 800))
        time_offset_days_change = (random.randint(01, 05))
        self.updatedAt = (updatedAt + datetime.timedelta(days=time_offset_days_change, hours=time_offset_hours_change, seconds=time_offset_seconds_change))
        
        if state == "Open" : 
            pullRequest['raisedAt'] = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S")  
        else :
            pullRequest['mergedAt'] = self.updatedAt.strftime("%Y-%m-%dT%H:%M:%S")                             
        pullRequestData.append(pullRequest)
        metadata = {  "labels":["PULL_REQUEST_LOGS", "DATA"]}  
        self.publishToolsData(pullRequestData, metadata, "changeDate", "%Y-%m-%dT%H:%M:%S", False) 
        metadata = {  "labels":["PULL_REQUESTS", "DATA"], "dataUpdateSupported":True, "uniqueKey":["pullrequest_id"]}            
        self.publishToolsData(pullRequestData, metadata) 
        # self.pullRequestNo =self.pullRequestNo +1   
    
  
       

if __name__ == "__main__":
    DummyDataAgent() 
