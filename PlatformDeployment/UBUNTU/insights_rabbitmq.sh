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
# install erlang
#echo "#################### Installing Erlang , required for Rabbit MQ ####################"
cd /opt
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
#wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/rabbitmq/erlang.zip
wget https://github.com/rabbitmq/erlang-rpm/archive/refs/tags/v23.3.4.11.zip
unzip erlang.zip && cd erlang
sudo dpkg -i *.deb
mkdir rabbitmq && cd rabbitmq
echo "deb http://www.rabbitmq.com/debian/ testing main" | sudo tee -a /etc/apt/sources.list
wget -O- https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc | sudo apt-key add -
#wget -O- https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/rabbitmq/rabbitmq-release-signing-key.asc | sudo apt-key add -
wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.9.13/rabbitmq-server_3.9.13-1_all.deb
sudo dpkg -i rabbitmq-server_3.9.13-1_all.deb
#wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/rabbitmq/rabbitmq-server.deb
#sudo dpkg -i rabbitmq-server.deb
sleep 15
wget https://$userName:$credential@infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/rabbitmq/RabbitMQ.zip
sudo unzip RabbitMQ.zip && cd RabbitMQ && sudo cp rabbitmq.config /etc/rabbitmq/
sudo systemctl enable rabbitmq-server && sudo systemctl start rabbitmq-server
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"
sleep 15
