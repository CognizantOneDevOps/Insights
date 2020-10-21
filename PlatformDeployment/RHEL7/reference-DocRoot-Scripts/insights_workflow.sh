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
# get insights workflow jar
echo "#################### Getting Insights workflow Jar ####################"
sudo mkdir /opt/insightsworkflow
cd /opt/insightsworkflow
export INSIGHTS_WORKFLOW=`pwd`
sudo echo INSIGHTS_WORKFLOW=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_WORKFLOW=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformWorkflow.jar -O PlatformWorkflow.jar
sleep 5
sudo chmod -R 777 /opt/insightsworkflow
cd /etc/init.d/
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/InSightsWebHook.sh
sudo mv InSightsWorkflow.sh InSightsWorkflow
sudo chmod +x InSightsWorkflow
sudo chkconfig InSightsWorkflow on
sleep 10
sudo service InSightsWorkflow stop
sudo service InSightsWorkflow start
