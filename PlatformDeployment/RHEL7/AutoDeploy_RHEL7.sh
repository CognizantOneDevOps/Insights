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
sudo yum install wget -y
sudo yum install unzip -y
#sample scripts like sciprt1 , script2 needs to be at same path as orchestartion script
echo "Enter comma seperated software package names from the below list you want to install on this machine. Please make sure you type in exact name as in list. Please type ALL if you want to setup all at once on a Single Machine"
echo "1)insights_first
2)insights_java
3)insights_es
4)insights_neo4j
5)insights_postgres
6)insights_grafana
7)insights_python
8)insights_rabbitmq
9)insights_tomcat
10)insights_enginejar
11)insights_all"
read input
for package in ${input[@]}
do
case $package in
   "insights_first") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_first.sh -O insights_first.sh  && sh insights_first.sh
   ;;
   "insights_java") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_java.sh -O insights_java.sh && sh insights_java.sh
   ;;
   "insights_es") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_es.sh -O insights_es.sh  && sh insights_es.sh
   ;;
   "insights_neo4j") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_neo4j.sh -O insights_neo4j.sh && sh insights_neo4j.sh
   ;;
   "insights_postgres") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_postgres.sh -O insights_postgres.sh && sh insights_postgres.sh
   ;;
   "insights_grafana") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_grafana.sh -O insights_grafana.sh && sh insights_grafana.sh
   ;;
   "insights_python") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_python.sh -O insights_python.sh && sh insights_python.sh
   ;;
   "insights_rabbitmq") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_rabbitmq.sh -O insights_rabbitmq.sh && sh insights_rabbitmq.sh
   ;;
   "insights_tomcat") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_tomcat.sh -O insights_tomcat.sh && sh insights_tomcat.sh
   ;;
   "insights_enginejar") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_enginejar.sh -O insights_enginejar.sh && sh insights_enginejar.sh
   ;;
   "insights_all") sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/scripts/insights_all.sh -O insights_all.sh && sh insights_all.sh
   ;;
   *) echo "$package Not present"
esac
done
