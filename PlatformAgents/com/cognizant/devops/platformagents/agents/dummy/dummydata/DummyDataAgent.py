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
Created on Dec 28, 2017
@author: 610962
'''
from dateutil import parser
import datetime
from BaseAgent import BaseAgent
import time
import calendar
import random
import string
import os
import json
import logging.handlers


class DummyDataAgent(BaseAgent):

    def process(self):
    
        self.printLog("DummyDataAgent processing started ",True)      
        jiraSample = {
            "jiraStatus":"Completed",
            "jiraProjectName":"Knowledge Transfer",
            "jiraCreator":"393565",
            "inSightsTimeX":"2016-09-13T14:15:44Z",
            "jiraPriority":"Medium",
            "jiraUpdated":"2016-09-13T14:15:44.000+0530",
            "jiraIssueType":"Story",
            "toolName":"JIRA",
            "jiraKey":"KKT-3",
            "inSightsTime":1473777524,
            "sprint":"Sprint4",
            "fixVersions": "ACS17.0.4.3"
        }
        
        # GIT sample json
        gitSample = {
            "gitCommitId":1,
            "inSightsTimeX":"2016-03-16T10:47:22Z",
            "toolName":"GIT",
            "gitAuthorName":"Akshay",
            "gitReponame":"Insights",
            "inSightsTime":1458122182,
            "jiraKey":"IS-10",
            "gitCommiTime":"2016-03-16T15:47:22Z"
        }
        
        # Jenkins sample json
        jenkinsSample = {
          "environment": "PROD",
          "endTime": 1508351788,
          "gitCommitId":1,
          "jobName": "BillingApproved",
          "duration": 10178,
          "buildNumber": 1,
          "sprintID": "S52",
          "vector": "BUILD",
          "startTime": 1508341610,
          "projectName": "PaymentServices",
          "inSightsTimeX": "2017-10-18T15:46:50Z",
          "status": "Success",
          "toolName": "JENKINS",
          "projectID": "1002"
        }

        # Sonar Sample json
        sonarSample = {
            "id": 4,
            "k": "PaymentServices",
            "nm": "PaymentServices",
            "sc": "PRJ",
            "qu": "TRK"
         }
        sprintSample = {
            "sprintName":"Adoption",
            "sprintId":"ad1",
            "state":"closed"
            }
        
        # Jira variables
        jira_status = ['Open', 'Backlog', 'To Do', 'In Progress', 'Canceled', 'Done', 'Closed', 'Reopen']
        jira_priority = ['Low', 'Medium', 'High']
        jira_issuetype = ['Story', 'Task', 'Sub-task', 'Bug', 'Epic', 'User Story']
        jira_creator = ['Akshay', 'Mayank', 'Vishwajit', 'Prajakta', 'Vishal']
        jira_sprint = ['S51', 'S52', 'S53', 'S54', 'S55']
        jira_project_name = ['PaymentServices', 'MobileServices', 'ClaimFinder', 'AgentLocator']
        Story_Id = ['ST-10', 'ST-11', 'ST-12', 'ST-13', 'ST-14']
        jira_version = ['ACS17.0.4.3', 'BDE17.0.4.3', 'ACS19.0.3.1']	
        state = ['start', 'closed', 'finish', 'deliver']
        Priority = ['2', '3', '4', '5']	
        Author_Name = ['HAri', 'Dhrubaj', 'Akshay', 'Tommy']
        resolution = ['Done', 'Completed', 'Reopen']
        storyPoints = ['1', '2', '3', '5', '8', '13']
        #alm_ID = ['a23', 'a33', 'a44', 'a55']
        progressTimeSec = ['1232', '32342', '2323']
        assigneeID = ['1231212', '2345253', '234234', '1342323']
        assigneeEmail = ['hari@cognizant.com', 'sashikala@cognizant.com', 'drubaj@cognizant.com', 'kalaivani@cognizant.com']
        
        # Sprint variables

        sprint_Name = ['Adoption', 'UIEnhance', 'Three', 'Testphase']	
        state = ['start', 'closed', 'finish', 'deliver']
        issue_Type = ['Bug', 'Sprint_Bug', 'SIT_Bug', 'Performance_Bug', 'Regression_Bug']
        
        # GIT variables
        repo = ['Insights', 'InsightsDemo', 'InsightsTest', 'InsightsTest']
        author = ['Akshay', 'Mayank', 'Vishwajit', 'Prajakta']
        # message = ['Adding debug lines for IS-10 / S51', 'Remvoing bug for IS-11 / S52', 'New feature added for IS-1 / S53', 'Rolling back changes IS-13 / S54']
        Commit_Id = ['123', '456', '789', '111', '009', '008', '007', '990']
       
        # Jenkins variables
        sprint = ['S51', 'S52', 'S53', 'S54', 'S55']
        status = ['Success', 'Failure', 'Aborted']
        project_name = ['PaymentServices', 'MobileServices', 'ClaimFinder', 'AgentLocator']
        job_name = ['BillingApproved', 'BillingInvoice', 'ClaimValidated', 'ClaimProcessed', 'deploy']
        projectId = ['1001', '1002', '1003', '1004']
        jen_env = ['PROD', 'DEV', 'INT', 'RELEASE']
        buildUrl = ['productv4.1.devops.com', 'productv4.2.devops.com', 'productv4.3.devops.com', 'productv4.4.devops.com']
        result = ['SUCCESS', 'FAILURE', 'ABORTED']
        master = ['master1', 'master2']
        # Sonar variables
        project = ['PaymentServices', 'MobileServices', 'ClaimFinder', 'AgentLocator']
        sonar_key = ['payment1', 'Mobile1', 'Claim', 'agent']
        project_id = ['1', '2', '3', '4']
        resourceKey = ['09', '099', '89', '32']
        sonar_quality_gate_Status = ['SUCCESS', 'FAILED']
        sonar_coverage = ['35','50','70','85']
        sonar_complexity = ['35','50','70','85','100','125']
        sonar_duplicate = ['15','25','45','60']
        sonar_techdepth = ['3','5','17','25','21']
        
        rundeck_env=['PROD','DEV','INTG','SIT','UAT']
        
        dataCount = self.config.get("dataCount")
        start_date_days = self.config.get("start_date_days")
        sleepTime= self.config.get("sleepTime")
        createSprintData= self.config.get("createSprintData", False)
        currentDate= datetime.datetime.now() - datetime.timedelta(days=start_date_days)    
        self.printLog(currentDate,True)
        flag = 1
        # To save the data count in tracking.json
        script_dir = os.path.dirname(__file__)
        #print(script_dir)
        file_path = os.path.join(script_dir, 'config.json')
        self.printLog(file_path, False)
        # Input your system path to tracking.json of DummyAgent          
        with open(file_path, "r") as jsonFile:  # Open the JSON file for reading
            data = json.load(jsonFile)  # Read the JSON into the buffer
        #self.printLog('Starting Agent!')
        #currentDT = datetime.datetime.now()
        #print(currentDT)
        record_count = 0  
        total_record_count = 0
        globle_sprintArr = []
        sprint_data = []
        
        
        self.printLog('Jira sprint Started .... 50', False)
        # sprint json configurations
        sprintEndDate=currentDate
        sprintDay=7
        numberOfSprint=150
        try:
            for rangeNumber in range(0,numberOfSprint ) :
                sprint = 'ST-' + str(rangeNumber)
                #if sprint not in globle_sprintArr :
                sprintSample = {}
                sprintStartDate = sprintEndDate
                sprintEndDate=(sprintStartDate + datetime.timedelta(days=sprintDay))
                if createSprintData:
                    self.printLog(sprint +'  '+str(sprintStartDate) +'  '+str(sprintEndDate), False)
                sprintSample['sprintName'] = random.choice(sprint_Name)
                sprintSample['sprintId'] = sprint
                sprintSample['state'] = random.choice(state)
                sprintSample['issueType'] = random.choice(issue_Type)
                sprintSample['sprintStartDate'] =sprintStartDate.strftime("%Y-%m-%dT%H:%M:%SZ")
                sprintSample['sprintEndDate'] = sprintEndDate.strftime("%Y-%m-%dT%H:%M:%SZ")
                sprint_data.append(sprintSample)
                globle_sprintArr.append(sprint)
                #print(sprintSample)
            if createSprintData:
                metadata = {"labels" : ["Sprint"]}
                #self.printLog(len(sprint_data), False)
                total_record_count =total_record_count + len(sprint_data)
                self.publishToolsData(sprint_data, metadata)
        except Exception as ex:
                self.printLog(ex,True)
        
        while flag == 1 :
            jira_data = []
            sprint_data = []
            git_data = []
            jenkins_data = []
            sonar_data = []
            rundeck_data = []
            #print(jira_data)
            # Run-time calculated variables
            currentDT = datetime.datetime.now()
            self.printLog('currentDate '+str(currentDate),True)
            time_tuple = time.strptime(currentDate.strftime('%Y-%m-%d %H:%M:%S'), '%Y-%m-%d %H:%M:%S')
            #print(time_tuple)
            time_epoch = time.mktime(time_tuple)
            #print(time_epoch)
            randomStr = ''.join([random.choice(string.ascii_letters + string.digits) for n in xrange(32)])
            #randonjirakey = 'LS-' + str(''.join([random.choice(string.digits) for n in xrange(1)]))
            #randonGitCommitId = 'CM-' + str(''.join([random.choice(string.digits) for n in xrange(1)]))
            
            time_start = (random.randint(100, 500))
            time_end = (random.randint(501, 800))
            publish_message_count_loop=""
            self.printLog('Jira Started .... ', False)
            # jira_count =[] 
            jira_count = 0
            jira_keyArr = [] 
            jira_sprintArr = [] 
            while jira_count != 50 :
                try:
                    randonjirakey = 'LS-' + str(''.join([random.choice(string.digits) for n in xrange(10)]))
                    randonSprintStringId = 'ST-' + str(''.join([random.choice(string.digits) for n in xrange(3)]))
                    #print(randonSprintStringId)
                    #Jira json configurations
                    time_offset_jira = (random.randint(01, 24))
                    time_offset = (random.randint(101, 800))
                    jira_date =(currentDate + datetime.timedelta(hours=time_offset_jira,seconds=time_offset))
                    sprintNumber =random.choice(globle_sprintArr)
                    self.printLog('sprintNumber '+sprintNumber+' jira date '+str(jira_date), False)
                    jiraSample ={}
                    jiraSample['inSightsTimeX'] = jira_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                    jiraSample['jiraUpdated'] = (jira_date + datetime.timedelta(days=time_offset_jira)).strftime("%Y-%m-%dT%H:%M:%SZ")
                    jiraSample['creationDate'] = jira_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                    jiraSample['inSightsTime'] = time_epoch
                    jiraSample['jiraCreator'] = random.choice(jira_creator)
                    jiraSample['jiraPriority'] = random.choice(jira_priority)
                    jiraSample['jiraIssueType'] = random.choice(jira_issuetype)
                    jiraSample['sprintId'] = sprintNumber
                    jiraSample['jiraStatus'] = random.choice(jira_status)            
                    jiraSample['fixVersions'] = random.choice(jira_version)        
                    jiraSample['issueType'] = random.choice(issue_Type)
                    jiraSample['jiraKey'] = randonjirakey
                    jiraSample['storyId'] = random.choice(Story_Id)
                    jiraSample['Priority'] = random.choice(Priority)
                    jiraSample['projectName'] = random.choice(jira_project_name)
                    jiraSample['resolution'] = random.choice(resolution)
                    jiraSample['storyPoints'] = random.choice(storyPoints)
                    jiraSample['progressTimeSec'] = random.choice(progressTimeSec)
                    jiraSample['assigneeID'] = random.choice(assigneeID)
                    jiraSample['assigneeEmail'] = random.choice(assigneeEmail)
                    jiraSample['authorName'] = random.choice(Author_Name)
                    jiraSample['toolName'] = "JIRA"
                    jiraSample['categoryName'] = "ALM"
                    jira_count += 1
                    jira_data.append(jiraSample)
                    #print(jiraSample)
                    jira_keyArr.append(jiraSample)
                    #if randonSprintStringId not in jira_sprintArr:
                    #    jira_sprintArr.append(randonSprintStringId)
                except Exception as ex:
                    self.printLog(ex,True)
            jiraMetadata = {"labels" : ["JIRA"]}
            total_record_count =total_record_count + len(jira_data)
            self.publishToolsData(jira_data, jiraMetadata)
            publish_message_count_loop=publish_message_count_loop+' Jira Data='+str(len(jira_data))
            #print(jira_keyArr)
            #print(jira_sprintArr)
            
            
            
            self.printLog('GIT Started .... ', False)
            #print(jira_keyArr)
            #print(len(jira_keyArr))
            git = 0
            git_CommitArr = []
            for rangeNumber in range(0, len(jira_keyArr)) :
                git_count = 0
                #print(jirakey)
                jiraSampleData=jira_keyArr[rangeNumber]
                while git_count !=  50: 
                   randonGitCommitId = 'CM-' + str(''.join([random.choice(string.digits) for n in xrange(10)])) 
                   time_offset = (random.randint(101, 800))
                   # GIT json configurations    10 2
                   #print("GIT 1")
                   git_date = (datetime.datetime.strptime(jiraSampleData['inSightsTimeX'],"%Y-%m-%dT%H:%M:%SZ") + datetime.timedelta(seconds=time_offset))
                   git_datetime_epoch = int(time.mktime(git_date.timetuple()))
                   #print(git_datetime_epoch)
                   self.printLog(' jirakey '+ jiraSampleData['jiraKey'] +' jira Date '+jiraSampleData['inSightsTimeX'] +'  GIT Date '+str(git_date), False)
                   gitSample = {}
                   gitSample['inSightsTimeX'] = git_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                   gitSample['gitCommiTime'] = git_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                   gitSample['inSightsTime'] = git_datetime_epoch
                   gitSample['gitCommitId'] = randomStr
                   if git_count < 2001 :
                       gitSample['jiraKey'] = jiraSampleData['jiraKey']
                       gitSample['message'] = 'This commit is associated with jira-key : ' + str(jiraSampleData['jiraKey'])
                   gitSample['gitReponame'] = random.choice(repo)
                   gitSample['gitAuthorName'] = random.choice(author)
                   gitSample['repoName'] = random.choice(repo)
                   gitSample['commitId'] = randonGitCommitId
                   gitSample['toolName'] = "GIT"
                   gitSample['categoryName'] = "SCM"
                   #gitSample['git_date']=str(git_date)
                   git_count += 1
                   #print(gitSample)
                   git_CommitArr.append(gitSample)                 
                   git_data.append(gitSample)
            gitMetadata = {"labels" : ["GIT"]}
            #print(len(git_data))
            total_record_count =total_record_count + len(git_data)
            self.publishToolsData(git_data, gitMetadata)
            publish_message_count_loop=publish_message_count_loop+' GIT Data='+str(len(git_data))
            
            self.printLog('Jenkins Started ....', False)
            #print(git_CommitArr)
            #print(len(git_CommitArr))
            jenkins_count = 0 
            jenkins_keyArr = [] 
            for rangeNumber in range(0, len(git_CommitArr)) :
                try:
                    gitSampleData = git_CommitArr[rangeNumber]
                    #print(gitSampleData) + time_start
                    #print(gitSampleData['commitId'])
                    time_offset = (random.randint(101, 800))
                    randomJenkineBuildNumber = str(''.join([random.choice(string.digits) for n in xrange(10)]))
                    #print('a jenkine key '+randomJenkineBuildNumber +'  '+gitSampleData['inSightsTimeX']) #+'  '+gitSample['git_date']
                    jenkins_date = (datetime.datetime.strptime(gitSampleData['inSightsTimeX'],"%Y-%m-%dT%H:%M:%SZ") + datetime.timedelta(seconds=120))
                    self.printLog('GIT Commit Id '+gitSampleData['commitId']+'  GIT Date '+ gitSampleData['inSightsTimeX'] +' Jenkine Date '+str(jenkins_date), False)
                    jenkins_startTime = (jenkins_date)
                    jenkins_endTime = (jenkins_date + datetime.timedelta(seconds=time_offset))
                    jenkine_epochtime=int(time.mktime(jenkins_date.timetuple()))
                    jenkine_status =random.choice(status)
                    jenkinsSample = {}
                    jenkinsSample['inSightsTimeX'] = (jenkins_date).strftime("%Y-%m-%dT%H:%M:%SZ")
                    jenkinsSample['inSightsTime'] = jenkine_epochtime
                    jenkinsSample['startTime'] = jenkins_startTime.strftime("%Y-%m-%dT%H:%M:%SZ")
                    jenkinsSample['endTime'] = jenkins_endTime.strftime("%Y-%m-%dT%H:%M:%SZ")
                    jenkinsSample['duration'] = (jenkins_endTime - jenkins_startTime).seconds
                    jenkinsSample['status'] = jenkine_status
                    #jenkinsSample['sprintID'] = random.choice(sprint)
                    jenkinsSample['buildNumber'] = randomJenkineBuildNumber
                    jenkinsSample['jobName'] = random.choice(job_name)
                    jenkinsSample['projectName'] = random.choice(project_name)
                    jenkinsSample['projectID'] = random.choice(projectId)
                    jenkinsSample['environment'] = random.choice(jen_env)
                    jenkinsSample['buildUrl'] = random.choice(buildUrl)
                    jenkinsSample['result'] = random.choice(result)
                    jenkinsSample['master'] = random.choice(master)
                    jenkinsSample['jenkins_date']=str(jenkins_date)
                    if rangeNumber < 2001 :
                        jenkinsSample['scmcommitId'] = gitSampleData['commitId']
                    jenkinsSample['toolName'] = "JENKINS"
                    jenkinsSample['categoryName'] = "CI"
                    #print(jenkinsSample)
                    if jenkine_status=="Success":
                        jenkins_keyArr.append(jenkinsSample)
                    jenkins_data.append(jenkinsSample)
                except Exception as ex:
                    self.printLog(ex,True)
            jenkinsMetadata = {"labels" : ["JENKINS"]}
            #self.printLog(len(jenkins_data), False)
            total_record_count =total_record_count + len(jenkins_data)
            self.publishToolsData(jenkins_data, jenkinsMetadata)
            publish_message_count_loop=publish_message_count_loop+' Jenkins Data='+str(len(jenkins_data))
            
            self.printLog('Sonar Started ....', False)
            #print(jenkins_keyArr)
            jenkine_success_build =len(jenkins_keyArr)
            self.printLog('Jenkine Array size for success build '+str(jenkine_success_build),True)
            sonar_count = 0 
            for rangeNumber in range(0, len(jenkins_keyArr))  :
                jenkinsSampleData = jenkins_keyArr[rangeNumber]
                #print(jenkinsSampleData)
                #print(jenkinsSampleData['buildNumber'])
                # Sonar jenkins configuration
                ramdomSonarKey =str(''.join([random.choice(string.digits) for n in xrange(10)]))
                sonar_date = (datetime.datetime.strptime(jenkinsSampleData['inSightsTimeX'],"%Y-%m-%dT%H:%M:%SZ") + datetime.timedelta(seconds=120))
                time_offset = (random.randint(101, 800))
                self.printLog('Jenkine build number '+jenkinsSampleData['buildNumber']+' Jenkine Date '+jenkinsSampleData['inSightsTimeX']+' Sonar Date '+str(sonar_date), False)
                try:
                    sonar_startTime = sonar_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                    sonar_endTime = (sonar_date + datetime.timedelta(seconds=time_offset)).strftime("%Y-%m-%dT%H:%M:%SZ")
                    sonarSample = {}
                    sonarSample['inSightsTimeX'] = sonar_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                    sonarSample['inSightsTime'] = int(time.mktime(sonar_date.timetuple()))
                    sonarSample['startTime'] = sonar_startTime
                    sonarSample['endTime'] = sonar_endTime
                    sonarSample['projectname'] = random.choice(project)
                    sonarSample['ProjectID'] = random.choice(projectId)
                    sonarSample['ProjectKey'] = random.choice(sonar_key)
                    sonarSample['resourceKey'] = random.choice(resourceKey)
                    if rangeNumber < (jenkine_success_build - 200) :
                        sonarSample['jenkineBuildNumber'] = jenkinsSampleData['buildNumber']
                    sonarSample['sonarKey']=ramdomSonarKey
                    sonarSample['sonarQualityGateStatus']= random.choice(sonar_quality_gate_Status)
                    sonarSample['sonarCoverage']= random.choice(sonar_coverage)
                    sonarSample['sonarComplexity']= random.choice(sonar_complexity)
                    sonarSample['sonarDuplicateCode']= random.choice(sonar_duplicate)
                    sonarSample['sonarTechDepth']= random.choice(sonar_techdepth)
                    sonarSample['toolName'] = "SONAR"
                    sonarSample['categoryName'] = "CODEQUALITY"
                    sonar_data.append(sonarSample)
                except Exception as ex:
                    self.printLog(ex,True)
                #print(sonarSample)
            sonarMetadata = {"labels" : ["SONAR"]}
            #print(len(sonar_data))
            total_record_count =total_record_count + len(sonar_data)
            self.publishToolsData(sonar_data, sonarMetadata)
            publish_message_count_loop=publish_message_count_loop+' Sonar Data='+str(len(sonar_data))
            
            self.printLog('Rundeck Started ....', False)
            #print(jenkins_keyArr)
            #print(len(jenkins_keyArr))
            Rundeck_count = 0 
            for rangeNumber in range(0, len(jenkins_keyArr)):
                try:
                    jenkinsSampleData = jenkins_keyArr[rangeNumber]
                    #print(jenkinsSampleData)
                    #print(jenkinsSampleData['buildNumber'])
                    rundeck_date = (datetime.datetime.strptime(jenkinsSampleData['inSightsTimeX'],"%Y-%m-%dT%H:%M:%SZ") + datetime.timedelta(seconds=120))
                    time_offset = (random.randint(101, 800))
                    self.printLog('Jenkine build number '+jenkinsSampleData['buildNumber']+' Jenkine Date '+jenkinsSampleData['inSightsTimeX']+' Runduck Date '+str(rundeck_date), False)
                    rundeck_startTime = rundeck_date
                    rundeck_endTime = (rundeck_date + datetime.timedelta(seconds=time_offset))
                    rundeckSample = {}
                    rundeckSample['inSightsTimeX'] = rundeck_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                    rundeckSample['inSightsTime'] = int(time.mktime(rundeck_startTime.timetuple()))
                    rundeckSample['startTime'] = rundeck_startTime.strftime("%Y-%m-%dT%H:%M:%SZ")
                    rundeckSample['endTime'] = rundeck_endTime.strftime("%Y-%m-%dT%H:%M:%SZ")
                    rundeckSample['status'] = random.choice(status)
                    rundeckSample['environment'] = random.choice(rundeck_env)
                    if rangeNumber < (jenkine_success_build - 200) :
                        rundeckSample['jenkineBuildNumber'] = jenkinsSampleData['buildNumber']               
                    rundeckSample['toolName'] = "RUNDECK"
                    rundeckSample['categoryName'] = "DEPLOYMENT"
                    rundeck_data.append(rundeckSample)
                    #print(rundeckSample)
                except Exception as ex:
                    self.printLog(ex,True)
            RundeckMetadata = {"labels" : ["RUNDECK"]}
            #print(len(rundeck_data))
            total_record_count =total_record_count + len(rundeck_data)
            self.publishToolsData(rundeck_data, RundeckMetadata)
            publish_message_count_loop=publish_message_count_loop+' Rundeck Data='+str(len(rundeck_data))
        
            self.printLog('Published data: '+ str(record_count) + " Details "+publish_message_count_loop,True)
            record_count += 1
            currentDate += datetime.timedelta(days=1)
            #print(currentDate)
                
            time.sleep(sleepTime)      

            if(dataCount == record_count):
                flag = 0
       
        currentCompletedDT = datetime.datetime.now()
        self.printLog('Start Time      '+ str(currentDT),True)
        self.printLog('Completed Time  ==== '+ str(currentCompletedDT),True)
        self.printLog('Total Record count '+str(total_record_count),True)
        self.printLog("Dummy Agent Processing Completed .....",True)
                
if __name__ == "__main__":
    DummyDataAgent() 