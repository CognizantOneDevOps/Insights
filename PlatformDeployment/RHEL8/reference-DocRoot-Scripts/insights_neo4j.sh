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
echo "#################### Installing Neo4j with configs and user creation ####################"
sudo sed  -i '$ a root   soft    nofile  40000' /etc/security/limits.conf
sudo sed  -i '$ a root   hard    nofile  40000' /etc/security/limits.conf
source /etc/environment
cd /opt
sudo mkdir NEO4J_HOME
cd NEO4J_HOME
read -p "Enter neo4j version number you want to install(ex. 3.5.26 or 3.5.28): " version_number
read -p "Enter neo4j password to set: " neo4j_password
version_number=`echo $version_number | sed -e 's/^[[:space:]]*//'`
neo4j_password=`echo $neo4j_password | sed -e 's/^[[:space:]]*//'`
NEO4J_URI=https://dist.neo4j.org/neo4j-community-${version_number}-unix.tar.gz
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
NEO4J_SETTINGS=https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/customNeo4jSettings/neo4j-${version_number}-settings.zip
sudo wget -O neo4j-Insights.tar.gz ${NEO4J_URI}
sudo tar -xzf neo4j-Insights.tar.gz
mv neo4j-community-${version_number} neo4j-Insights
sudo chmod -R 755 neo4j-Insights
sudo wget -O neo4j-settings.zip ${NEO4J_SETTINGS}
sudo unzip neo4j-settings.zip
mv neo4j-${version_number}-settings neo4j-settings
sudo yes | cp -rf ./neo4j-settings/neo4j.conf ./neo4j-Insights/conf/
sudo yes | cp -rf ./neo4j-settings/plugins/ ./neo4j-Insights/plugins/
cd neo4j-Insights
sleep 20
./bin/neo4j start
sleep 40
curl -X POST -u neo4j:neo4j -H "Content-Type: application/json" -d '{"password":"'"$neo4j_password"'"}' http://localhost:7474/user/neo4j/password
sleep 10
cd ..
export NEO4J_INIT_HOME=`pwd`
sudo echo NEO4J_INIT_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" NEO4J_INIT_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sleep 10
sudo chmod -R 777 /opt/NEO4J_HOME
cd /etc/init.d/
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/initscripts/Neo4j.sh
sudo mv Neo4j.sh Neo4j
sudo chmod +x Neo4j
sudo chkconfig Neo4j on
sleep 10
sudo service Neo4j stop
sudo service Neo4j start
