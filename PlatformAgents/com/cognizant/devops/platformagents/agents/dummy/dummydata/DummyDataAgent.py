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
import logging
import time
import calendar
import random
import string
import os
import json


class DummyDataAgent(BaseAgent):

    def process(self):
    
        print("DummyDataAgent  ")      
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
          "status": "Failure",
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
        tool_Name = ['JIRA']
        
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
        
        dataCount = self.config.get("dataCount")
        flag = 1
        # To save the data count in tracking.json
        script_dir = os.path.dirname(__file__)
        file_path = os.path.join(script_dir, 'config.json')
        # Input your system path to tracking.json of DummyAgent          
        with open(file_path, "r") as jsonFile:  # Open the JSON file for reading
            data = json.load(jsonFile)  # Read the JSON into the buffer
        print'Starting Agent!'
        currentDT = datetime.datetime.now()
        record_count = 0  
        total_record_count = 0
        while flag == 1:	
            jira_data = []
            sprint_data = []
            git_data = []
            jenkins_data = []
            sonar_data = []
            sprint_data = []
            # Run-time calculated variables
            currentDT = datetime.datetime.now()
            time_tuple = time.strptime(currentDT.strftime('%Y-%m-%d %H:%M:%S'), '%Y-%m-%d %H:%M:%S')
            time_epoch = time.mktime(time_tuple)
            randomStr = ''.join([random.choice(string.ascii_letters + string.digits) for n in xrange(32)])
            #randonjirakey = 'LS-' + str(''.join([random.choice(string.digits) for n in xrange(1)]))
            #randonGitCommitId = 'CM-' + str(''.join([random.choice(string.digits) for n in xrange(1)]))
            
            time_start = (random.randint(100, 500))
            time_end = (random.randint(501, 800))
            
            # jira_count =[]
            jira_count = 0
            jira_keyArr = [] 
            jira_sprintArr = []
            while jira_count != 50 :
                randonjirakey = 'LS-' + str(''.join([random.choice(string.digits) for n in xrange(10)]))
                randonSprintStringId = 'ST-' + str(''.join([random.choice(string.digits) for n in xrange(1)]))
                #print(randonSprintStringId)
                # Jira json configurations   2 15
                jiraSample ={}
                jiraSample['inSightsTimeX'] = currentDT.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                jiraSample['jiraUpdated'] = currentDT.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                jiraSample['inSightsTime'] = time_epoch
                jiraSample['jiraCreator'] = random.choice(jira_creator)
                jiraSample['jiraPriority'] = random.choice(jira_priority)
                jiraSample['jiraIssueType'] = random.choice(jira_issuetype)
                jiraSample['sprintId'] = randonSprintStringId
                jiraSample['jiraStatus'] = random.choice(jira_status)            
                jiraSample['fixVersions'] = random.choice(jira_version)        
                jiraSample['issueType'] = random.choice(issue_Type)
                jiraSample['jiraKey'] = randonjirakey
                jiraSample['storyId'] = random.choice(Story_Id)
                jiraSample['Priority'] = random.choice(Priority)
                jiraSample['status'] = random.choice(status)
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
                jira_keyArr.append(randonjirakey)
                if randonSprintStringId not in jira_sprintArr:
                    jira_sprintArr.append(randonSprintStringId)
               
            jiraMetadata = {"labels" : ["JIRA"]}
            #print(len(jira_data))
            total_record_count =total_record_count + len(jira_data)
            self.publishToolsData(jira_data, jiraMetadata)
                           
            #print(jira_keyArr)
            #print(jira_sprintArr)
            
            # sprint json configurations
            for sprint in jira_sprintArr:
                #print(sprint)
                sprintSample['sprintName'] = random.choice(sprint_Name)
                sprintSample['sprintId'] = sprint
                sprintSample['state'] = random.choice(state)
                sprintSample['issueType'] = random.choice(issue_Type)
                sprint_data.append(sprintSample)

            metadata = {"labels" : ["SPRINT"]}
            #print(len(sprint_data))
            total_record_count =total_record_count + len(sprint_data)
            self.publishToolsData(sprint_data, metadata)
            
            #print('GIT Started ....')
            #print(jira_keyArr)
            #print(len(jira_keyArr))
            git = 0
            git_CommitArr = []
            for jirakey in jira_keyArr: 
                git_count = 0
                #print(jirakey)                
                while git_count != 25 : 
                   randonGitCommitId = 'CM-' + str(''.join([random.choice(string.digits) for n in xrange(10)])) 
                   # GIT json configurations    10 2
                   git_time = (time_epoch + time_start)
                   git_date = (currentDT + datetime.timedelta(seconds=time_start))
                   gitSample = {}
                   gitSample['inSightsTimeX'] = git_date.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                   gitSample['gitCommiTime'] = git_date.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                   gitSample['inSightsTime'] = git_time
                   gitSample['gitCommitId'] = randomStr
                   gitSample['jiraKey'] = jirakey
                   gitSample['gitReponame'] = random.choice(repo)
                   gitSample['gitAuthorName'] = random.choice(author)
                   gitSample['repoName'] = random.choice(repo)
                   gitSample['message'] = 'This commit is associated with jira-key : ' + str(jirakey)
                   gitSample['commitId'] = randonGitCommitId
                   gitSample['toolName'] = "GIT"
                   gitSample['categoryName'] = "SCM"
                   git_count += 1   
                   git_CommitArr.append(gitSample)                 
                   git_data.append(gitSample)
            gitMetadata = {"labels" : ["GIT"]}
            #print(len(git_data))
            total_record_count =total_record_count + len(git_data)
            self.publishToolsData(git_data, gitMetadata)
            
            #print('Jenkins Started ....')
            #print(git_CommitArr)
            #print(len(git_CommitArr))
            jenkins_count = 0 
            jenkins_keyArr = [] 
            for rangeNumber in range(0, len(git_CommitArr)) :
                gitSampleData = git_CommitArr[rangeNumber]
                #print(gitSampleData)
                #print(gitSampleData['commitId'])
                randomJenkineBuildNumber = str(''.join([random.choice(string.digits) for n in xrange(10)]))
                #print('a jenkine key'+randomJenkineBuildNumber)
                # Jenkins json configurations
                jenkins_date = (git_date + datetime.timedelta(seconds=time_end))
                jenkins_startTime = gitSampleData['inSightsTime'] + time_start
                jenkins_endTime = gitSampleData['inSightsTime'] + time_end
                jenkinsSample = {}
                jenkinsSample['inSightsTimeX'] = jenkins_date.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                jenkinsSample['inSightsTime'] = jenkins_endTime
                jenkinsSample['startTime'] = jenkins_startTime
                jenkinsSample['endTime'] = jenkins_endTime
                jenkinsSample['duration'] = jenkins_endTime - jenkins_startTime
                jenkinsSample['status'] = random.choice(status)
                #jenkinsSample['sprintID'] = random.choice(sprint)
                jenkinsSample['buildNumber'] = randomJenkineBuildNumber
                jenkinsSample['jobName'] = random.choice(job_name)
                jenkinsSample['projectName'] = random.choice(project_name)
                jenkinsSample['projectID'] = random.choice(projectId)
                jenkinsSample['environment'] = random.choice(jen_env)
                jenkinsSample['buildUrl'] = random.choice(buildUrl)
                jenkinsSample['result'] = random.choice(result)
                jenkinsSample['master'] = random.choice(master)
                jenkinsSample['scmcommitId'] = gitSampleData['commitId']
                jenkinsSample['toolName'] = "JENKINS"
                jenkinsSample['categoryName'] = "CI"
                jenkins_keyArr.append(jenkinsSample)
                jenkins_data.append(jenkinsSample)
            jenkinsMetadata = {"labels" : ["JENKINS"]}
            #print(len(jenkins_data))
            total_record_count =total_record_count + len(jenkins_data)
            self.publishToolsData(jenkins_data, jenkinsMetadata)
            
            print('Sonar Started ....')
            #print(jenkins_keyArr)
            #print(len(jenkins_keyArr))
            sonar_count = 0 
            for rangeNumber in range(0, len(jenkins_keyArr))  :
                jenkinsSampleData = jenkins_keyArr[rangeNumber]
                #print(jenkinsSampleData)
                #print(jenkinsSampleData['buildNumber'])
                # Sonar jenkins configuration
                ramdomSonarKey =str(''.join([random.choice(string.digits) for n in xrange(10)]))
                sonar_date = (jenkins_date + datetime.timedelta(seconds=time_end))
                sonar_startTime = jenkinsSampleData['inSightsTime'] + time_start
                sonar_endTime = jenkinsSampleData['inSightsTime'] + time_end
                sonarSample = {}
                sonarSample['inSightsTimeX'] = sonar_date.strftime("%Y-%m-%d" + 'T' + "%H:%M:%S" + 'Z')
                sonarSample['inSightsTime'] = sonar_endTime
                sonarSample['projectname'] = random.choice(project)
                sonarSample['ProjectID'] = random.choice(projectId)
                sonarSample['ProjectKey'] = random.choice(sonar_key)
                sonarSample['resourceKey'] = random.choice(resourceKey)
                sonarSample['inSightsTime'] = random.choice(resourceKey)
                sonarSample['jenkineBuildNumber'] = jenkinsSampleData['buildNumber']
                sonarSample['inSightsTimeX'] = random.choice(resourceKey)
                sonarSample['sonarKey']=ramdomSonarKey
                sonarSample['sonarQualityGateStatus']= random.choice(sonar_quality_gate_Status)
                sonarSample['sonarCoverage']= random.choice(sonar_coverage)
                sonarSample['sonarComplexity']= random.choice(sonar_complexity)
                sonarSample['sonarDuplicateCode']= random.choice(sonar_duplicate)
                sonarSample['sonarTechDepth']= random.choice(sonar_techdepth)
                sonarSample['toolName'] = "SONAR"
                sonarSample['categoryName'] = "CODEQUALITY"
                sonar_data.append(sonarSample)				

            sonarMetadata = {"labels" : ["SONAR"]}
            #print(len(sonar_data))
            total_record_count =total_record_count + len(sonar_data)
            self.publishToolsData(sonar_data, sonarMetadata)
        
            print 'Published data: ', record_count
            record_count += 1
                
            time.sleep(1)        

            if(dataCount == record_count):
                flag = 0
                print("Dummy Agent Processing Completed .....")
        
        currentCompletedDT = datetime.datetime.now()
        print('Start Time      '+ str(currentDT))
        print('Completed Time  ==== '+ str(currentCompletedDT))
        #print('Total Record count '+str(total_record_count))
                
if __name__ == "__main__":
    DummyDataAgent()       
