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
sudo yum install wget -y
sudo yum install unzip -y
sudo yum install dos2unix -y
wget -O jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64
chmod +x ./jq
sudo cp jq /usr/bin
#Download scripts
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_first_arm.sh -O insights_first.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_java.sh -O insights_java.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_es.sh -O insights_es.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_postgres_arm.sh -O insights_postgres.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_grafana.sh -O insights_grafana.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_python.sh -O insights_python.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_rabbitmq_arm.sh -O insights_rabbitmq.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_tomcat.sh -O insights_tomcat.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_enginejar.sh -O insights_enginejar.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_workflow.sh -O insights_workflow.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_webhook_arm.sh -O insights_webhook.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_agents.sh -O insights_agents.sh
wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/Scripts/PlatformDeployment/RHEL7/reference-DocRoot-Scripts/insights_ext_arm.sh -O insights_ext.sh

#Open insights components ports
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent
sudo firewall-cmd --zone=public --add-port=3000/tcp --permanent
#sudo firewall-cmd --zone=public --add-port=7474/tcp --permanent
#sudo firewall-cmd --zone=public --add-port=7687/tcp --permanent
sudo firewall-cmd --zone=public --add-port=15672/tcp --permanent
sudo firewall-cmd --zone=public --add-port=5672/tcp --permanent
sudo firewall-cmd --zone=public --add-port=9200/tcp --permanent
sudo firewall-cmd --reload
dos2unix *.sh 
chmod +x *.sh
#Insights Installation
echo "Get required env varidables for Insights"
sudo sh insights_first.sh $1
echo "Installing Java"
sudo sh insights_java.sh
echo "Insitalling Elastic Search"
sh insights_es.sh
dbpassword=$1
echo "$1"
echo "$dbpassword"
echo "Install Postgres"
sudo sh insights_postgres.sh $dbpassword
echo "Install Grafana"
sudo sh insights_grafana.sh
echo "Install Python 2.7.11 with required libraries needed for Insights"
sudo sh insights_python.sh
echo "Install Erlang and RabbitMQ"
rabbitadminpwd=$2
rabbitguestpwd=$3
echo "$rabbitadminpwd"
echo "$rabbitguestpwd"
sudo sh insights_rabbitmq.sh $rabbitadminpwd $rabbitguestpwd
echo "Install Tomcat "
sudo sh insights_tomcat.sh
echo "Get Insights Engine"
sudo sh insights_enginejar.sh
echo "Get Insights Workflow"
sudo sh insights_workflow.sh
echo "Get Insights Webhook"
sudo sh insights_webhook.sh $rabbitadminpwd
echo "Get Insights Agents"
sudo sh insights_agents.sh
#echo "Get Insights Initd scripts"
#wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/scripts/insights_initscripts.sh -O insights_initscripts.sh && sh insights_initscripts.sh
