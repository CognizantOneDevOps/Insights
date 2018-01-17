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
from com.cognizant.devops.platformagents.core.BaseAgent import BaseAgent
import logging
import time
import calendar
import random
import string
import os
import json

class DummyDataAgent(BaseAgent):
    def process(self):
              
        #Jira sample json
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
        }
        
        #GIT sample json
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
        
        #Jenkins sample json
        jenkinsSample= {
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
                
        #Jira variables
        jira_status = ['Open','Backlog','To Do','In Progress']
        jira_priority = ['Low','Medium','High']
        jir_issuetype = ['Story','Task','Sub-task','Bug','Epic']
        jira_creator = ['Akshay','Mayank','Vishwajit','Prajakta','Vishal']
        jira_sprint = ['S51','S52','S53','S54','S55']
        jira_project_name = ['PaymentServices','MobileServices','ClaimFinder','AgentLocator']
        jira_key = ['IS-10','IS-11','IS-12','IS-13']
        
        #GIT variables
        repo = ['Insights','InsightsDemo','InsightsTest','InsightsTest']
        author = ['Akshay','Mayank','Vishwajit','Prajakta']
        
        #Jenkins variables
        sprint = ['S51','S52','S53','S54','S55']
        status = ['Success','Failure','Aborted']
        project_name = ['PaymentServices','MobileServices','ClaimFinder','AgentLocator']
        job_name = ['BillingApproved','BillingInvoice','ClaimValidated','ClaimProcessed']
        projectId = ['1001','1002','1003','1004']
        jen_env = ['PROD','DEV','INT']
        
        count = self.tracking.get("dataCount")
        flag = 1
        
        #To save the data count in tracking.json
        script_dir = os.path.dirname(__file__)
        file_path = os.path.join(script_dir, '~/tracking.json') #Input your system path to tracking.json of DummyAgent        
        with open(file_path, "r") as jsonFile: # Open the JSON file for reading
            data = json.load(jsonFile) # Read the JSON into the buffer
                
        while flag == 1:
            jira_data = []
            git_data = []
            jenkins_data = []
            
            #Run-time calculated variables
            currentDT = datetime.datetime.now()
            time_tuple = time.strptime(currentDT.strftime('%Y-%m-%d %H:%M:%S'), '%Y-%m-%d %H:%M:%S')
            time_epoch = time.mktime(time_tuple)
            randomStr = ''.join([random.choice(string.ascii_letters + string.digits) for n in xrange(32)])
            time_start = (random.randint(100, 500))
            time_end = (random.randint(501, 800))
            
            #Jira json configurations
            jiraSample['inSightsTimeX'] = currentDT.strftime("%Y-%m-%d"+'T'+"%H:%M:%S"+'Z')
            jiraSample['jiraUpdated'] = currentDT.strftime("%Y-%m-%d"+'T'+"%H:%M:%S"+'Z')
            jiraSample['inSightsTime'] = time_epoch
            jiraSample['jiraProjectName'] = random.choice(jira_project_name)
            jiraSample['jiraCreator'] = random.choice(jira_creator)
            jiraSample['jiraPriority'] = random.choice(jira_priority)
            jiraSample['jiraIssueType'] = random.choice(jir_issuetype)
            jiraSample['jiraKey'] = random.choice(jira_key)
            jiraSample['sprint'] = random.choice(jira_sprint)
            jiraSample['jiraStatus'] = random.choice(jira_status)
            
            jira_data.append(jiraSample)
            #print jira_data
            self.publishToolsData(jira_data)
            
                        
            #GIT json configurations
            git_time = (time_epoch + time_start)
            git_date = (currentDT+datetime.timedelta(seconds=time_start))
            gitSample['inSightsTimeX'] = git_date.strftime("%Y-%m-%d"+'T'+"%H:%M:%S"+'Z')
            gitSample['gitCommiTime'] = git_date.strftime("%Y-%m-%d"+'T'+"%H:%M:%S"+'Z')
            gitSample['inSightsTime'] = git_time
            gitSample['gitCommitId'] = randomStr
            gitSample['jiraKey'] = jiraSample['jiraKey']
            gitSample['gitReponame'] = random.choice(repo)
            gitSample['gitAuthorName'] = random.choice(author)
                        
            git_data.append(gitSample)
            #print git_data
            self.publishToolsData(git_data)
            
            #Jenkins json configurations
            jenkins_date = (git_date+datetime.timedelta(seconds=time_end))
            jenkins_startTime = gitSample['inSightsTime'] + time_start
            jenkins_endTime = gitSample['inSightsTime'] + time_end
            jenkinsSample['inSightsTimeX'] = jenkins_date.strftime("%Y-%m-%d"+'T'+"%H:%M:%S"+'Z')
            jenkinsSample['inSightsTime'] = jenkins_endTime
            jenkinsSample['gitCommitId'] = gitSample['gitCommitId']
            jenkinsSample['startTime'] = jenkins_startTime
            jenkinsSample['endTime'] = jenkins_endTime
            jenkinsSample['duration'] = jenkins_endTime - jenkins_startTime
            jenkinsSample['status'] = random.choice(status)
            jenkinsSample['sprintID'] = random.choice(sprint)
            jenkinsSample['buildNumber'] = count
            jenkinsSample['jobName'] = random.choice(job_name)
            jenkinsSample['projectName'] = random.choice(project_name)
            jenkinsSample['projectID'] = random.choice(projectId)
            jenkinsSample['environment'] = random.choice(jen_env)
            
            jenkins_data.append(jenkinsSample)
            #print jenkins_data
            self.publishToolsData(jenkins_data)
            
            
            print 'Published data: ',count
            count +=1
                       
            ## Working with buffered content
            tmp = data["dataCount"] 
            data["dataCount"] = count
        
             #To save the data count in tracking.json   
            ## Save our changes to JSON file
            with open(file_path, "w") as jsonFile:
                #print 'Writing'
                json.dump(data, jsonFile)  
                
            time.sleep(40)        

            if( count == 100000 ):
                flag = 0
                 

if __name__ == "__main__":
    DummyDataAgent()       
