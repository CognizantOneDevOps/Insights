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
read -p "Enter neo4j version number you want to install(ex. 3.5.28 or 4.4.4): " version_number
version_number=`echo $version_number | sed -e 's/^[[:space:]]*//'`
NEO4J_APOCURL=https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/3.5.0.11/apoc-3.5.0.11-all.jar
NEO4J_URI=https://dist.neo4j.org/neo4j-community-${version_number}-unix.tar.gz
if [ $version_number == "4.4.4" ]
then
  NEO4J_APOCURL=https://github.com/neo4j-contrib/neo4j-apoc-procedures/releases/download/4.4.0.3/apoc-4.4.0.3-all.jar
fi
sudo wget -O neo4j-Insights.tar.gz ${NEO4J_URI}
sudo tar -xzf neo4j-Insights.tar.gz
mv neo4j-community-${version_number} neo4j-Insights
sudo chmod -R 755 neo4j-Insights
sudo wget ${NEO4J_APOCURL}
sudo yes | cp -rf apoc*.jar ./neo4j-Insights/plugins/
sudo echo "apoc.trigger.enabled=true" | sudo tee -a ./neo4j-Insights/conf/neo4j.conf
cd neo4j-Insights
sleep 20
./bin/neo4j start
sleep 40
echo -n "Please enter neo4j default credentials: "
read -s defneo4jcreds
echo -n "Please enter new credentials for neo4j user: "
read -s newneo4jcreds
#curl -X POST -u neo4j:$defneo4jcreds -H "Content-Type: application/json" -d '{"password":"'"$newneo4jcreds"'"}' http://localhost:7474/user/neo4j/password
bin/neo4j-admin set-initial-password $newneo4jcreds
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
sudo wget -O Neo4j https://raw.githubusercontent.com/CognizantOneDevOps/Insights/master/PlatformDeployment/RHEL7/initscripts/Neo4j.sh
sudo chmod +x Neo4j
sudo chkconfig Neo4j on
sleep 10
sudo service Neo4j stop
sudo service Neo4j start
