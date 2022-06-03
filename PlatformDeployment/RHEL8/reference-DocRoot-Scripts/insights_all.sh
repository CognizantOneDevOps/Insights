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

sudo yum update -y
sudo yum install wget -y
sudo yum install unzip -y
sudo yum install dos2unix -y
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_first.sh -O insights_first.sh && dos2unix insights_first.sh && sh insights_first.sh
echo "Installing Java"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_java.sh -O insights_java.sh && dos2unix insights_java.sh && sh insights_java.sh
echo "Install Postgres"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_postgres12.sh -O insights_postgres.sh && dos2unix insights_postgres.sh && sh insights_postgres.sh
echo "Install Grafana"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_grafana.sh -O insights_grafana.sh && dos2unix insights_grafana.sh && sh insights_grafana.sh
echo "Install Python 2.7.11 with required libraries needed for Insights"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_python3.sh -O insights_python3.sh && dos2unix insights_python3.sh && sh insights_python3.sh
echo "Install Neo4j"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_neo4j.sh -O insights_neo4j.sh && dos2unix insights_neo4j.sh && sh insights_neo4j.sh
echo "Install Erlang and RabbitMQ"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_rabbitmq.sh -O insights_rabbitmq.sh && dos2unix insights_rabbitmq.sh && sh insights_rabbitmq.sh
echo "Install Tomcat "
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_tomcat.sh -O insights_tomcat.sh && dos2unix insights_tomcat.sh && sh insights_tomcat.sh
echo "Get Insights Engine"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_enginejar.sh -O insights_enginejar.sh && dos2unix insights_enginejar.sh && sh insights_enginejar.sh
echo "Get Insights Agents"
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_agents.sh -O insights_agents.sh && dos2unix insights_agents.sh && sh insights_agents.sh
#echo "Get Insights Initd scripts"
#wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL8/scripts/insights_initscripts.sh -O insights_initscripts.sh && dos2unix insights_initscripts.sh && sh insights_initscripts.sh
