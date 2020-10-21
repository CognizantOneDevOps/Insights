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
# get insights engine jar
# get insights engine jar
echo "#################### Getting Insights Engine Jar ####################"
sudo mkdir /opt/insightsengine
cd /opt/insightsengine
export INSIGHTS_ENGINE=`pwd`
sudo echo INSIGHTS_ENGINE=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_ENGINE=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/release/latest/PlatformEngine.jar -O PlatformEngine.jar
sleep 2
sudo nohup java -jar PlatformEngine.jar &
sudo chmod -R 777 /opt/insightsengine
cd /etc/init.d/
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/InSightsEngine.sh
sudo mv InSightsEngine.sh InsightsEngine
sudo chmod +x InsightsEngine
sudo chkconfig InsightsEngine on
sleep 10
sudo service InsightsEngine stop
sudo service InsightsEngine start
