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
#Installation of Erlang

sudo apt update
read -p "Please enter version number you want to install(3.8 or 3.9): " version_number
version_number=`echo $version_number | sed -e 's/^[[:space:]]*//'`

sudo apt install curl software-properties-common apt-transport-https lsb-release
curl -fsSL https://packages.erlang-solutions
sudo apt update
sudo apt install erlang
echo "deb https://packages.erlang-solutions.com/ubuntu $(lsb_release -cs) contrib" | sudo tee /etc/apt/sources.list.d/erlang.list
sudo apt update
sudo apt install erlang

#Installation of RabbitMQ
curl -s https://packagecloud.io/install/repositories/rabbitmq/rabbitmq-server/script.deb.sh | sudo bash
sudo apt install rabbitmq-server
sudo systemctl enable rabbitmq-server
sudo ufw allow proto tcp from any to any port 5672,15672
