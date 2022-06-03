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
cd /opt
wget https://github.com/CognizantOneDevOps/Insights/archive/refs/heads/master.zip -O Insightsrepo.zip
sudo unzip Insightsrepo.zip
sudo mkdir /opt/grafana/data/plugins
sudo chmod -R 777 data
sudo cp -r /opt/Insightsrepo/Insights-master/PlatformGrafanaPlugins/Panels/* /opt/grafana/data/plugins/
sudo cp -r /opt/Insightsrepo/Insights-master/PlatformGrafanaPlugins/DataSources/* /opt/grafana/data/plugins/
sudo rm -rf /opt/Insightsrepo/*
cd /opt/grafana/public/dashboards
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformGrafanaPlugins/ScriptedDashboard/iSight_ui3.js
cd /opt/grafana/conf
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformGrafanaPlugins/GrafanaConf/ldap.toml
echo "Please provide postgres grafana user credentials"
echo -n "credentials: "
read -s grafanacreds
sudo sed -i "/type =/ s/=.*/=postgres/" defaults.ini
sudo sed -i "/host =/ s/=.*/=localhost:5432/" defaults.ini
sudo sed -i "/name =/ s/=.*/=grafana/" defaults.ini
sudo sed -i "/user =/ s/=.*/=grafana/" defaults.ini
sudo sed -i "/password =/ s/=.*/=$grafanacreds/" defaults.ini
sudo sed -i "/allow_loading_unsigned_plugins =/ s/=.*/=neo4j-datasource,Inference,cde-inference-plugin,cde-fusion-panel,cognizant-insights-charts/" defaults.ini
sudo sed -i "/allow_embedding =/ s/=.*/=true/" defaults.ini
sudo sed -i 's@</body>@<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script></body>@g' /opt/grafana/public/views/index.html
cd /opt/grafana
export GRAFANA_HOME=`pwd`
sudo echo GRAFANA_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" GRAFANA_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sudo nohup ./bin/grafana-server &
sudo echo $! > grafana-pid.txt
sleep 10
sudo chmod -R 777 /opt/grafana
cd /etc/init.d/
sudo wget  https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/Grafana.sh
sudo yum install dos2unix -y
sudo dos2unix Grafana.sh
sudo mv Grafana.sh Grafana
sudo chmod +x Grafana
sudo chkconfig Grafana on
sleep 10
sudo service Grafana stop
sudo service Grafana start
