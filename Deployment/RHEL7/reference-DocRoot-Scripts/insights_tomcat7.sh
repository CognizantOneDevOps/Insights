#  Copyright 2017 Cognizant Technology Solutions
#  
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License.  You may obtain a copy
#  of the License at
#  
#    http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
#  License for the specific language governing permissions and limitations under
#  the License.
# install tomcat 7
echo "#################### Installing Tomcat7 ####################"
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/InSightsUI.zip -O InSightsUI.zip
sudo unzip InSightsUI.zip && sudo rm -rf InSightsUI.zip
sudo wget http://platform.cogdevops.com/InSightsV1.0/artifacts/PlatformService.war -O PlatformService.war
sudo yum install tomcat -y
sudo yum install tomcat-webapps tomcat-admin-webapps -y
sudo cp -R InSightsUI/app /usr/share/tomcat/webapps
sudo rm -rf InSightsUI
sudo cp PlatformService.war /usr/share/tomcat/webapps
sudo rm -rf PlatformService.war
source /etc/environment && source /etc/profile
sudo chmod 777 /usr/share/tomcat/conf/tomcat.conf
sudo echo INSIGHTS_HOME=$INSIGHTS_HOME >> /usr/share/tomcat/conf/tomcat.conf
sleep 10
sudo systemctl enable tomcat && sudo systemctl start tomcat

