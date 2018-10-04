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
echo "#################### Installing Tomcat8 ####################"
cd /opt
sudo wget https://platform.cogdevops.com/insights_install/release/latest/InSightsUI.zip -O InSightsUI.zip
sudo unzip InSightsUI.zip && sudo rm -rf InSightsUI.zip
sudo wget https://platform.cogdevops.com/insights_install/release/latest/PlatformService.war -O PlatformService.war
sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat-8.5.27.tar.gz
sudo tar -zxvf apache-tomcat-8.5.27.tar.gz
sudo cp -R ./app /opt/apache-tomcat-8.5.27/webapps
#sudo cp -R app /opt/apache-tomcat-8.5.27/webapps
sudo rm -rf InSightsUI
sudo cp PlatformService.war /opt/apache-tomcat-8.5.27/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat-8.5.27
sudo chmod -R 777 /opt/apache-tomcat-8.5.27
cd /etc/init.d/
sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/initscripts/Tomcat8.sh
sudo mv Tomcat8.sh Tomcat8
sudo chmod +x Tomcat8
sudo chkconfig Tomcat8 on
sudo service Tomcat8 start
