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
# install erlang and rabbitmq
echo "#################### Installing Erlang , required for Rabbit MQ ####################"
yum update
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/epel-release-latest-7.noarch.rpm
sudo yum install epel-release-latest-7.noarch.rpm
sudo yum --disablerepo="*" --enablerepo="epel" list available
sudo yum search htop
sudo yum info htop
sudo yum install htop
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir erlang && cd erlang
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/esl-erlang_23.0-1~centos~7_amd64.rpm
sudo mv esl-erlang_23.0-1~centos~7_amd64.rpm erlang.rpm
#sudo rpm -ivh erlang.rpm
sudo yum install erlang.rpm
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo mkdir rabbitmq && cd rabbitmq
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/socat-1.7.3.2-2.el7.x86_64.rpm
sudo rpm -ivh socat-1.7.3.2-2.el7.x86_64.rpm
sudo rpm --import https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-signing-key-public.asc
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-server-3.8.5-1.el7.noarch.rpm
sudo mv rabbitmq-server-3.8.5-1.el7.noarch.rpm rabbitmq-server.noarch.rpm
sudo rpm -ivh rabbitmq-server.noarch.rpm
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/RabbitMQ.zip
sudo unzip RabbitMQ.zip && cd RabbitMQ && sudo cp rabbitmq.config /etc/rabbitmq/
sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
echo "RabbitMQ user 'guest' to create user and to set permissions. Pleaes provide it."
echo -n "Password: "
read -s RABBITMQ_GUEST_USER_PASSWORD
echo "RabbitMQ user 'iSight' is going to be created requires password. Pleaes provide it."
echo -n "Password: "
read -s RABBITMQ_ISIGHT_USER_PASSWORD
echo "User creation is in progres.Please wait ..."
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"password":"'$RABBITMQ_ISIGHT_USER_PASSWORD'","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"
