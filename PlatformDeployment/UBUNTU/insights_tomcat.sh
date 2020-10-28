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
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformUI3.zip -O PlatformUI3.zip
sudo unzip PlatformUI3.zip && sudo rm -rf PlatformUI3.zip
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformService.war -O PlatformService.war
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/tomcat/apache-tomcat.tar.gz
sudo tar -zxvf apache-tomcat.tar.gz
sudo cp -R ./app /opt/apache-tomcat/webapps
sudo cp PlatformService.war /opt/apache-tomcat/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat
sudo chmod -R 777 /opt/apache-tomcat
cd /etc/systemd/system
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/tomcat/Tomcat.service
sudo systemctl enable Tomcat.service
sudo systemctl start Tomcat