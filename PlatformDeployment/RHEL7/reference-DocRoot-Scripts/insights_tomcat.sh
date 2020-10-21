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
cd /opt
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformUI3.zip -O PlatformUI3.zip
sudo unzip PlatformUI3.zip && sudo rm -rf PlatformUI3.zip
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformService.war -O PlatformService.war
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/tomcat/apache-tomcat.tar.gz
sudo tar -zxvf apache-tomcat.tar.gz
sudo cp -R ./app /opt/apache-tomcat/webapps
sudo rm -rf PlatformUI3
sudo cp PlatformService.war /opt/apache-tomcat/webapps
sudo rm -rf PlatformService.war
cd apache-tomcat
sudo chmod -R 777 /opt/apache-tomcat
cd /etc/init.d/
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/Tomcat.sh
sudo mv Tomcat.sh Tomcat
sudo chmod +x Tomcat
sudo chkconfig Tomcat on
sleep 10
sudo service Tomcat stop
sudo service Tomcat start
