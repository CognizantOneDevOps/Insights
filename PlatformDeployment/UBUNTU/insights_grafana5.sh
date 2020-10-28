#!/bin/bash
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
# Install customized Grafana V5.2.2
echo "#################### Installing Grafana (running as BG process) ####################"
cd /opt
sudo mkdir grafana
cd grafana
export GRAFANA_HOME=`pwd`
sudo echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/grafana/grafana.tar.gz
sudo tar -zxvf grafana-5.2.2.tar.gz
GRAFANA_DIR=$GRAFANA_HOME/grafana-5.2.2
echo $GRAFANA_DIR
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/grafana/ldap.toml
sudo cp ldap.toml $GRAFANA_DIR/conf/ldap.toml
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/grafana/defaults.ini
sudo cp defaults.ini $GRAFANA_DIR/conf/defaults.ini
cd $GRAFANA_DIR
echo 'pwd'
sudo nohup ./bin/grafana-server &
echo $! > grafana-pid.txt
cd ..