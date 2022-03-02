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

echo "#################### Installing Grafana (running as BG process) ####################"
source /etc/environment
source /etc/profile
cd /opt
#sudo mkdir grafana
#cd grafana
read -p "Please enter Grafana version number you want to install(ex. 7.5.10 or 8.1.3): " version_number
version_number=`echo $version_number | sed -e 's/^[[:space:]]*//'`
sudo wget https://dl.grafana.com/oss/release/grafana-${version_number}.linux-amd64.tar.gz
sudo tar -zxvf grafana-${version_number}.linux-amd64.tar.gz
sudo mv /opt/grafana-${version_number} /opt/grafana
cd /opt/grafana
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/customGrafanaSettings/${version_number}/plugins.tar.gz
sudo tar -zxvf plugins.tar.gz
sudo mkdir /opt/grafana/data
sudo mkdir /opt/grafana/data/plugins
sudo chmod -R 777 data
sudo cp -r plugins/* ./data/plugins/
sudo rm -rf plugins.tar.gz
export GRAFANA_HOME=`pwd`
sudo echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/customGrafanaSettings/ldap.toml
sudo cp ldap.toml $GRAFANA_HOME/conf/ldap.toml
sudo wget  https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/customGrafanaSettings/${version_number}/defaults.ini
sudo cp defaults.ini $GRAFANA_HOME/conf/defaults.ini
sudo nohup ./bin/grafana-server &
sudo echo $! > grafana-pid.txt
sleep 10
sudo chmod -R 777 /opt/grafana
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
