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

#
# arfifacts
# https://platform.cogdevops.com/insights_install/release/latest/InSightsUI.zip 
# https://platform.cogdevops.com/insights_install/release/latest/PlatformService.war 
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat-8.5.27.tar.gz
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/initscripts/Tomcat8.sh


echo "#################### Installing Tomcat8 ####################"
cd /opt
sudo cp /usr/Offline_Installation/Tomcat8/apache-tomcat-8.5.27.tar.gz ./
sudo tar -zxvf apache-tomcat-8.5.27.tar.gz
sudo cp -R /usr/Offline_Installation/Insight_Components/InSightsUI/app /opt/apache-tomcat-8.5.27/webapps
sudo cp /usr/Offline_Installation/Insight_Components/PlatformService.war /opt/apache-tomcat-8.5.27/webapps
cd apache-tomcat-8.5.27
sudo chmod -R 777 /opt/apache-tomcat-8.5.27
cd /etc/init.d/
sudo mv /usr/Offline_Installation/Tomcat8/Tomcat8.sh Tomcat8
sudo chmod +x Tomcat8
sudo chkconfig Tomcat8 on
sudo service Tomcat8 start