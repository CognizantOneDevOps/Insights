#------------------------------------------------------------------------------
# Copyright 2017 Cognizant Technology Solutions
#   
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License ats
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
echo "#################### Setting up PlatformService ####################"
source /etc/environment
source /etc/profile
wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64
chmod +x ./jq
sudo cp jq /usr/bin
sudo rm -rf jq
cd $INSIGHTS_APP_ROOT_DIRECTORY

#Install Node module for UI
sudo yum install -y gcc-c++ make
curl -sL https://rpm.nodesource.com/setup_18.x | sudo -E bash -
sudo yum install nodejs

echo -n "Enter Release Version: " 
read releaseVersion

sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformService-${releaseVersion}.jar -O PlatformService.jar
export myextip=$(wget -qO- icanhazip.com)
echo $myextip
ServiceEndpoint="http://$myextip:8080"
grafanaEndpoint="http://$myextip:3000/grafana"
#Update server config
mkdir PlatformService
jq --arg serviceHost $ServiceEndpoint '(.serviceHost) |= $serviceHost' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json >  $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
jq --arg grafanaHost $grafanaEndpoint '(.grafanaHost) |= $grafanaHost' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json >  $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
sudo cp PlatformService.jar $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService/
sudo rm -rf PlatformService.jar
cd $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService/
sudo chmod -R 777 $INSIGHTS_APP_ROOT_DIRECTORY/PlatformService/

#update uiConfig.json
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformUI4-${releaseVersion}.zip -O PlatformUI4.zip
sudo unzip PlatformUI4.zip && sudo rm -rf PlatformUI4.zip
sudo cp -R ./UI $INSIGHTS_APP_ROOT_DIRECTORY/
sudo rm -rf PlatformUI4
cd $INSIGHTS_APP_ROOT_DIRECTORY/UI/
npm install
jq --arg serviceHost $ServiceEndpoint '(.serviceHost) |= $serviceHost' $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json >  $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json.tmp && mv $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json.tmp $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json -f
jq --arg grafanaHost $grafanaEndpoint '(.grafanaHost) |= $grafanaHost' $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json >  $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json.tmp && mv $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json.tmp $INSIGHTS_APP_ROOT_DIRECTORY/UI/insights/config/uiConfig.json -f

cd /etc/init.d/
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/Insights_Service.sh
sudo mv Insights_Service.sh Insights_Service
sudo chmod +x Insights_Service
sudo chkconfig Insights_Service on
sleep 10
sudo service Insights_Service restart
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/Insights_UI.sh
sudo mv Insights_UI.sh Insights_UI
sudo chmod +x Insights_UI
sudo chkconfig Insights_UI on
sleep 10
sudo service Insights_UI restart

