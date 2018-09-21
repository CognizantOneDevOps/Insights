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

#
# arfifacts
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/erlang-20.0.5-1.el6.x86_64.rpm
# https://www.rabbitmq.com/releases/rabbitmq-server/v3.6.1/ 
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-signing-key-public.asc
# https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/rabbitmq/RabbitMQ-3.6.5.zip


echo "#################### Installing Erlang , required for Rabbit MQ ####################"
sudo mkdir erlang && cd erlang
rpm -i /usr/Offline_Installation/Rabbitmq/erlang-20.0.5-1.el6.x86_64.rpm
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo mkdir rabbitmq && cd rabbitmq
rpm -i /usr/Offline_Installation/Rabbitmq/rabbitmq-server-3.6.1-1.noarch.rpm
sudo cp /usr/Offline_Installation/Rabbitmq/rabbitmq-signing-key-public.asc ./
sudo cp  -R /usr/Offline_Installation/Rabbitmq/RabbitMQ-3.6.5/ ./
cd RabbitMQ-3.6.5  && cp rabbitmq.config /etc/rabbitmq/
sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"