# Copyright 2022 Cognizant Technology Solutions
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
cd /etc/init.d
service rabbitmq-server stop
yum remove esl-*
cd $INSIGHTS_APP_ROOT_DIRECTORY
read -p "Please enter version number you want to install(3.8 or 3.9): " version_number
version_number=`echo $version_number | sed -e 's/^[[:space:]]*//'`
echo -n "Nexus(userName):"
read userName
echo "Nexus credential:"
read -s credential
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/epel-release-latest-7.noarch.rpm
sudo yum install epel-release-latest-7.noarch.rpm
sudo yum --disablerepo="*" --enablerepo="epel" list available
sudo yum search htop
sudo yum info htop
sudo yum install htop
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo rm -rf erlang
sudo mkdir erlang && cd erlang
if [ $version_number == "3.9" ]
then
  echo "installing version 3.9"
  sudo wget https://github.com/rabbitmq/erlang-rpm/releases/download/v23.3.4.11/erlang-23.3.4.11-1.el7.x86_64.rpm
  sudo rpm -Uvh erlang-23.3.4.11-1.el7.x86_64.rpm
        #sudo mv esl-erlang_23.0-1~centos~7_amd64.rpm erlang.rpm
        #sudo rpm -ivh erlang.rpm
        #sudo yum install erlang.rpm
elif [ $version_number == "3.8"]]
then
  echo "installing version 3.8"
  sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/esl-erlang_23.0-1~centos~7_amd64.rpm
  sudo mv esl-erlang_23.0-1~centos~7_amd64.rpm erlang.rpm
else
  echo "Incorrect version"
  exit
fi
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
sudo rm -rf rabbitmq
sudo mkdir rabbitmq && cd rabbitmq
sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/socat-1.7.3.2-2.el7.x86_64.rpm
sudo rpm -ivh socat-1.7.3.2-2.el7.x86_64.rpm
if [ $version_number == "3.9" ]
then
  echo "installing version 3.9"
  sudo rpm --import https://github.com/rabbitmq/signing-keys/releases/download/2.0/rabbitmq-release-signing-key.asc
  sudo wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.9.13/rabbitmq-server-3.9.13-1.el7.noarch.rpm
  sudo rpm -Uvh rabbitmq-server-3.9.13-1.el7.noarch.rpm
elif [ $version_number == "3.8"]
then
  echo "installing version 3.8"
  sudo rpm --import wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-signing-key-public.asc
  sudo wget https://$userName:$credential@infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/rabbitmq/rabbitmq-server-3.8.5-1.el7.noarch.rpm
  sudo mv rabbitmq-server-3.8.5-1.el7.noarch.rpm rabbitmq-server.noarch.rpm
  sudo rpm -ivh rabbitmq-server.noarch.rpm
else
  echo "Incorrect version"
  exit
fi
#sudo mv rabbitmq-server-3.8.5-1.el7.noarch.rpm rabbitmq-server.noarch.rpm
#sudo rpm -ivh rabbitmq-server.noarch.rpm
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
