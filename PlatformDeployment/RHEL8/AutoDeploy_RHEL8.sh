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
#  Copyright 2017 Cognizant Technology Solutions
#  
#  Licensed under the Apache License, Version 2.0 (the "License"); you may not
#  use this file except in compliance with the License.  You may obtain a copy
#  of the License at
#  
#    http://www.apache.org/licenses/LICENSE-2.0
#  
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
#  License for the specific language governing permissions and limitations under
#  the License.

# Lets make sure we have wget on the server
sudo yum update -y
sudo yum install wget -y
sudo yum install unzip -y
sudo yum install dos2unix -y
#sample scripts like sciprt1 , script2 needs to be at same path as orchestartion script
echo "Enter comma seperated software package names from the below list you want to install on this machine. Please make sure you type in exact name as in list. Please type ALL if you want to setup all at once on a Single Machine"
echo "1)insights_first
2)insights_java
3)insights_postgres
4)insights_neo4j
5)insights_grafana
6)insights_python
7)insights_rabbitmq
8)insights_tomcat
9)insights_enginejar
10)insights_workflow
11)insights_agents
12)server_setup_details
13)insights_all"
read input
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
for package in ${input[@]}
do
case $package in
   "insights_first") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_first.sh -O insights_first.sh && dos2unix insights_first.sh  && sh insights_first.sh
   ;;
   "insights_java") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_java.sh -O insights_java.sh && dos2unix insights_java.sh && sh insights_java.sh
   ;;
   "insights_postgres") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_postgres12.sh -O insights_postgres12.sh && dos2unix insights_postgres12.sh && sh insights_postgres12.sh
   ;;
   "insights_neo4j") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_neo4j.sh -O insights_neo4j.sh && dos2unix insights_neo4j.sh && sh insights_neo4j.sh
   ;;
   "insights_grafana") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_grafana.sh -O insights_grafana.sh && dos2unix insights_grafana.sh && sh insights_grafana.sh
   ;;
   "insights_python") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_python3.sh -O insights_python3.sh && dos2unix insights_python3.sh && sh insights_python3.sh
   ;;
   "insights_rabbitmq") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_rabbitmq.sh -O insights_rabbitmq.sh && dos2unix insights_rabbitmq.sh && sh insights_rabbitmq.sh
   ;;
   "insights_tomcat") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_tomcat.sh -O insights_tomcat.sh && dos2unix insights_tomcat.sh && sh insights_tomcat.sh
   ;;
   "insights_enginejar") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_enginejar.sh -O insights_enginejar.sh && dos2unix insights_enginejar.sh && sh insights_enginejar.sh
   ;;
   "insights_workflow") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_workflow.sh -O insights_workflow.sh && dos2unix insights_workflow.sh && sh insights_workflow.sh
   ;;
   "insights_agents") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_agents.sh -O insights_agents.sh && dos2unix insights_agents.sh && sh insights_agents.sh
   ;;
   "server_setup_details") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/server_setup_details.sh -O server_setup_details.sh && dos2unix server_setup_details.sh && sh server_setup_details.sh
   ;;
   "insights_all") sudo wget https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL8/reference-DocRoot-Scripts/insights_all.sh -O insights_all.sh && dos2unix insights_all.sh && sh insights_all.sh
   ;;
   *) echo "$package Not present"
esac
done
