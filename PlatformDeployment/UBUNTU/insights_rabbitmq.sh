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
#sudo mkdir erlang && cd erlang
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/erlang-20.0.5-1.el6.x86_64.rpm
#sudo yum install -y erlang-20.0.5-1.el6.x86_64.rpm
#echo "#################### Installing Rabbit MQ with configs and user creation ####################"
#sudo mkdir rabbitmq && cd rabbitmq
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-server-3.6.5-1.noarch.rpm
#sudo rpm --import http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-signing-key-public.asc
#sudo yum install -y rabbitmq-server-3.6.5-1.noarch.rpm
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/RabbitMQ-3.6.5.zip
#sudo unzip RabbitMQ-3.6.5.zip && cd RabbitMQ-3.6.5 && sudo cp rabbitmq.config /etc/rabbitmq/
#sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
#sudo rabbitmq-plugins enable rabbitmq_management
#sleep 15
#curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
#sleep 15
#curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"

#-------------------------------------------------------------------------------------------------

mkdir erlang && cd erlang
wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/erlang-20.0.5-1.el6.x86_64.rpm
apt-get install alien dpkg-dev debhelper build-essential
alien erlang-20.0.5-1.el6.x86_64.rpm
dpkg -i erlang_20.0.5-2_amd64.deb
mkdir rabbitmq && cd rabbitmq
echo "deb http://www.rabbitmq.com/debian/ testing main" >> /etc/apt/sources.list
curl http://www.rabbitmq.com/rabbitmq-signing-key-public.asc | sudo apt-key add -
curl http://www.rabbitmq.com/rabbitmq-signing-key-public.asc | apt-key add -
apt-get install rabbitmq-server
sleep 15
rabbitmq-plugins enable rabbitmq_management
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"
sleep 15
