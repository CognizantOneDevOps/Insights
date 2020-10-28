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
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformInsightsWebHook.jar -O PlatformInsightsWebHook.jar
sleep 2
sudo nohup java -jar PlatformInsightsWebHook.jar &
sleep 10
sudo chmod -R 777 /opt/insightsWebHook
cd /etc/init.d/
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/InSightsWebHook.sh
sudo mv InSightsWebHook.sh InSightsWebHook
sudo chmod +x InSightsWebHook
sudo chkconfig InSightsWebHook on
sleep 10
sudo service InSightsWebHook stop
sudo service InSightsWebHook start
