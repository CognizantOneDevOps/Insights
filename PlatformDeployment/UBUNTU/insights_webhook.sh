#!/bin/bash
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
# get insights webhook jar
echo "#################### Getting Insights WebHook Jar ####################"
sudo mkdir /opt/insightsWebHook
cd /opt/insightsWebHook
export INSIGHTS_WEBHOOK=`pwd`
sudo echo INSIGHTS_WEBHOOK=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_WEBHOOK=`pwd` | sudo tee -a /etc/profile
. /etc/environment
. /etc/profile
cd /opt/insightsWebHook
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformInsightsWebHook.jar -O PlatformInsightsWebHook.jar
sleep 2
sudo nohup java -jar PlatformInsightsWebHook.jar &
sleep 10
#sudo wget https://platform.cogdevops.com/insights_install/release/latest/PlatformWebhookEngine.jar -O PlatformWebhookEngine.jar
#sleep 2
#sudo nohup java -jar PlatformWebhookEngine.jar &
