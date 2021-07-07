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
echo "#################### Installing Tomcat9.0.36 ####################"
source /etc/environment
source /etc/profile
wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64
chmod +x ./jq
sudo cp jq /usr/bin
sudo rm -rf jq
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/release/latest/PlatformUI3.zip -O PlatformUI3.zip
sudo unzip PlatformUI3.zip && sudo rm -rf PlatformUI3.zip
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/release/latest/PlatformService.war -O PlatformService.war
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat.tar.gz
sudo tar -zxvf apache-tomcat.tar.gz
sudo cp -R ./app $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps
sudo rm -rf PlatformUI3
export myextip=$(wget -qO- icanhazip.com)
echo $myextip
ServiceEndpoint="http://$myextip:8080"
grafanaEndpoint="http://$myextip:3000/grafana"
#update uiConfig.json
jq --arg serviceHost $ServiceEndpoint '(.serviceHost) |= $serviceHost' $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json >  $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json.tmp && mv $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json.tmp $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json -f
jq --arg grafanaHost $grafanaEndpoint '(.grafanaHost) |= $grafanaHost' $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json >  $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json.tmp && mv $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json.tmp $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps/app/config/uiConfig.json -f
sudo cp PlatformService.war $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat
sudo chmod -R 777 $INSIGHTS_APP_ROOT_DIRECTORY/apache-tomcat
cd /etc/init.d/
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/Tomcat.sh
sudo mv Tomcat.sh Tomcat
sudo chmod +x Tomcat
sudo chkconfig Tomcat on
sleep 10
sudo service Tomcat restart
