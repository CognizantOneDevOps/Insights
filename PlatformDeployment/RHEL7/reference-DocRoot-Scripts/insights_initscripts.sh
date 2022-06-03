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
# This script copies all required scripts to /etc/init.d
echo "Copying the init.d scripts"
sudo mkdir initscripts
sudo cd initscripts
#sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/initscripts.zip
#sudo unzip initscripts.zip && cd initscripts chmod +x *.sh
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/Grafana.sh -O Grafana.sh  && dos2unix Grafana.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InSightsEngine.sh -O InSightsEngine.sh  && dos2unix InSightsEngine.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InSightsWebHook.sh -O InSightsWebHook.sh  && dos2unix InSightsWebHook.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InSightsWorkflow.sh -O InSightsWorkflow.sh  && dos2unix InSightsWorkflow.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InsightsLoki.sh  -O InsightsLoki.sh  && dos2unix InsightsLoki.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/InsightsPromtail.sh -O InsightsPromtail.sh  && dos2unix InsightsPromtail.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/Tomcat.sh -O Tomcat.sh  && dos2unix Tomcat.sh 
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/initscripts/Neo4j.sh -O Neo4j.sh  && dos2unix Neo4j.sh 
chmod +x *.sh
sudo cp -rp * /etc/init.d
cd /etc/logrotate.d/
sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/GrafanaLogRotate.sh -O GrafanaLogRotate.sh && dos2unix GrafanaLogRotate.sh 
mv GrafanaLogRotate.sh GrafanaLogRotate
