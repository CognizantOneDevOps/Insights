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
# install neo4j
echo "#################### Installing Neo4j with configs and user creation ####################"
sudo sed  -i '$ a root   soft    nofile  40000' /etc/security/limits.conf
sudo sed  -i '$ a root   hard    nofile  40000' /etc/security/limits.conf
source /etc/environment
sudo mkdir NEO4J_HOME
cd NEO4J_HOME
sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/neo4j/neo4j-community-3.3.0-unix.tar.gz
sudo tar -xf neo4j-community-3.3.0-unix.tar.gz
sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/neo4j/Neo4j-3.3.0.zip
sudo unzip Neo4j-3.3.0.zip
sudo cp Neo4j-3.3.0/conf/neo4j.conf neo4j-community-3.3.0/conf
sudo cp -R Neo4j-3.3.0/plugins neo4j-community-3.3.0/
cd neo4j-community-3.3.0
sleep 20
sudo ./bin/neo4j start
sleep 40
curl -X POST -u neo4j:neo4j -H "Content-Type: application/json" -d '{"password":"C0gnizant@1"}' http://localhost:7474/user/neo4j/password
sleep 10
cd ..
export NEO4J_INIT_HOME=`pwd`
sudo echo NEO4J_INIT_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" NEO4J_INIT_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile

