#-------------------------------------------------------------------------------
# Copyright 2020 Cognizant Technology Solutions
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
# Install Promtail
echo "#################### Starting Loki and Promtail Post configurations ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir Promtail
cd Promtail
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/Promtail/promtail-linux-amd64.zip
sudo unzip "promtail-linux-amd64.zip"
sudo rm -r -f promtail-linux-amd64.zip
sudo chmod a+x "promtail-linux-amd64"
export PROMTAIL_HOME=`pwd`
sudo echo PROMTAIL_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" PROMTAIL_HOME=`pwd` | sudo tee -a /etc/profile
cd /etc/init.d/
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/InsightsPromtail.sh
sudo mv InsightsPromtail.sh InsightsPromtail
sudo chmod +x InsightsPromtail
sudo chkconfig InsightsPromtail on
sudo yum install dos2unix
sudo dos2unix /etc/init.d/InsightsPromtail
sudo service InsightsPromtail stop
sleep 5
sudo service InsightsPromtail start
sleep 5
echo "#################### Promtail is up ####################"
echo "#################### Installation Completed ####################"

