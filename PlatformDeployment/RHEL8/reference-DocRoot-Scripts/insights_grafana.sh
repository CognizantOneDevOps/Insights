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
# Install customized Grafana V7.1.0
echo "#################### Installing Grafana (running as BG process) ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir grafana
cd grafana
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL8/grafana/latest/grafana.tar.gz
sudo tar -zxvf grafana.tar.gz
export GRAFANA_HOME=`pwd`
sudo echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL8/grafana/latest/ldap.toml
sudo cp ldap.toml $GRAFANA_HOME/conf/ldap.toml
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL8/grafana/latest/defaults.ini
sudo cp defaults.ini $GRAFANA_HOME/conf/defaults.ini
sudo nohup ./bin/grafana-server &
sudo echo $! > grafana-pid.txt
sleep 10
sudo chmod -R 777 $INSIGHTS_APP_ROOT_DIRECTORY/grafana
cd /etc/init.d/
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL8/initscripts/Grafana.sh
sudo yum install dos2unix -y
sudo dos2unix Grafana.sh
sudo mv Grafana.sh Grafana
sudo chmod +x Grafana
sudo chkconfig Grafana on
sleep 10
sudo service Grafana stop
sudo service Grafana start
