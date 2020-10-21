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
# Set InSights Home
# Set InSights Home
echo "#################### Setting up Insights Home ####################"
sudo yum install wget -y
sudo yum install unzip -y
cd /usr
sudo mkdir INSIGHTS_HOME
cd INSIGHTS_HOME
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/InSightsConfig.zip
sudo unzip InSightsConfig.zip && sudo rm -rf InSightsConfig.zip
sudo cp -R InSightsConfig/.InSights/ .
export INSIGHTS_HOME=`pwd`
sudo echo INSIGHTS_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_HOME=`pwd` | sudo tee -a /etc/profile
sudo chmod -R 777 /usr/INSIGHTS_HOME/
source /etc/environment
source /etc/profile
myextip=$(wget -qO- icanhazip.com)
echo $myextip
sed -i -e "s|localhost:3000|${myextip}:3000|g" /usr/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e "s|hostip|${myextip}|g" /usr/INSIGHTS_HOME/.InSights/server-config.json
