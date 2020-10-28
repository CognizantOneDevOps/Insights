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
echo "#################### Installing Erlang , required for Rabbit MQ ####################"
yum update
cd /tmp
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/epel-release-latest-7.noarch.rpm
sudo yum install epel-release-latest-7.noarch.rpm
sudo yum --disablerepo="*" --enablerepo="epel" list available
sudo yum search htop
sudo yum info htop
sudo yum install htop
cd /opt
sudo mkdir erlang && cd erlang
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/erlang.rpm
sudo rpm -ivh erlang.rpm
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo mkdir rabbitmq && cd rabbitmq
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-server.noarch.rpm
sudo rpm -ivh rabbitmq-server.noarch.rpm
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/socat-1.7.3.2-2.el7.x86_64.rpm
sudo rpm --import https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-signing-key-public.asc
sudo rpm -ivh socat-1.7.3.2-2.e17.x86_64.rpm
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/RabbitMQ.zip
sudo unzip RabbitMQ.zip && cd RabbitMQ && sudo cp rabbitmq.config /etc/rabbitmq/
sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"password":"iSight","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:guest -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"