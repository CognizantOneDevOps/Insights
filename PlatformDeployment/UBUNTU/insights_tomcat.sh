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
echo "#################### Installing Tomcat ####################"
cd /opt
echo -n "Enter Release Version: " 
read releaseVersion
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformUI4-${releaseVersion}.zip -O PlatformUI4.zip
sudo unzip PlatformUI4.zip && sudo rm -rf PlatformUI4.zip
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/PlatformService-${releaseVersion}.war -O PlatformService.war
sudo wget --no-check-certificate https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.63/bin/apache-tomcat-9.0.63.tar.gz
sudo tar -zxvf apache-tomcat.tar.gz
sudo mv apache-tomcat-9.0.63 apache-tomcat
sudo cp -R ./app /opt/apache-tomcat/webapps
sudo cp PlatformService.war /opt/apache-tomcat/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat
sudo chmod -R 777 /opt/apache-tomcat
cd /etc/systemd/system
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/UBUNTU/Tomcat.service
sudo systemctl enable Tomcat.service
sudo systemctl start Tomcat