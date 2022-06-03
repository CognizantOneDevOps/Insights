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
echo "#################### Installing Erlang , required for Rabbit MQ ####################"
yum update -y
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
wget  https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm
sudo yum localinstall epel-release-latest-7.noarch.rpm -y
sudo yum --disablerepo="*" --enablerepo="epel" list available
sudo yum search htop
sudo yum info htop
sudo yum install htop -y
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir erlang && cd erlang
sudo wget  https://github.com/rabbitmq/erlang-rpm/releases/download/v23.0.1/erlang-23.0.1-1.el7.x86_64.rpm
sudo mv erlang-23.0.1-1.el7.x86_64 erlang.rpm
#sudo rpm -ivh erlang.rpm
sudo yum localinstall erlang.rpm -y
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo mkdir rabbitmq && cd rabbitmq
sudo rpm --import https://www.rabbitmq.com/rabbitmq-signing-key-public.asc
sudo wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.8.5/rabbitmq-server-3.8.5-1.el7.noarch.rpm
sudo mv rabbitmq-server-3.8.5-1.el7.noarch.rpm rabbitmq-server.noarch.rpm
sudo yum localinstall rabbitmq-server.noarch.rpm -y
sudo wget http://mirror.centos.org/centos/7/os/x86_64/Packages/socat-1.7.3.2-2.el7.x86_64.rpm
sudo rpm -ivh socat-1.7.3.2-2.el7.x86_64.rpm
sudo chkconfig rabbitmq-server on && sudo service rabbitmq-server start
sudo rabbitmq-plugins enable rabbitmq_management
sleep 15
#echo "RabbitMQ user 'iSight' is going to be created requires password. Pleaes provide it."
#echo -n "Password: "
#read -s RABBITMQ_ISIGHT_USER_PASSWORD
RABBITMQ_ISIGHT_USER_PASSWORD=$1
RABBITMQ_GUEST_USER_PASSWORD=$2
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"password":"'$RABBITMQ_ISIGHT_USER_PASSWORD'","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"
