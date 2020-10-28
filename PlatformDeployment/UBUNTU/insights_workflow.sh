#!/bin/bash
#-------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
# get insights Workflow jar
echo "#################### Getting Insights Workflow Jar ####################"
sudo mkdir /opt/insightsworkflow
cd /opt/insightsworkflow
export INSIGHTS_WORKFLOW=`pwd`
sudo echo INSIGHTS_WORKFLOW=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_WORKFLOW=`pwd` | sudo tee -a /etc/profile
. /etc/environment
. /etc/profile
cd /opt/insightsworkflow
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformWorkflow.jar -O PlatformWorkflow.jar
sleep 2
INSIGHTS_HOME=/usr/INSIGHTS_HOME
sudo nohup java -cp PlatformWorkflow.jar:$INSIGHTS_HOME/workflowjar/* com.cognizant.devops.platformworkflow.workflowtask.app.PlatformWorkflowApplication &
