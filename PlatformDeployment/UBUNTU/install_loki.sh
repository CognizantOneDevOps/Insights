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
# Install Loki
echo "#################### Installing Loki ####################"
. /etc/environment
. /etc/profile
cd /opt
sudo mkdir Loki
cd Loki
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
sudo wget https://$userName:$credential@infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/Loki/loki-linux-amd64.zip
sudo apt-get install unzip
sudo unzip "loki-linux-amd64.zip"
sudo rm -r -f loki-linux-amd64.zip
sudo chmod a+x "loki-linux-amd64"
#sudo ./loki-linux-amd64 -config.file=loki-local-config.yaml
export LOKI_HOME=`pwd`
sudo echo LOKI_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" LOKI_HOME=`pwd` | sudo tee -a /etc/profile
cd /etc/systemd/system
sudo wget https://$userName:$credential@infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/loki/Loki.service
sudo systemctl enable Loki.service
sudo systemctl start Loki
echo "#################### Loki is up ####################"
echo "#################### Installation Completed ####################"

