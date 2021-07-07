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
echo "RabbitMQ"
echo "#################### Installing Erlang , required for Rabbit MQ ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
mkdir erlang && cd erlang
rpm -i $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Rabbitmq/erlang.rpm
echo "#################### Installing Rabbit MQ with configs and user creation ####################"
mkdir rabbitmq && cd rabbitmq
rpm -i $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Rabbitmq/rabbitmq-server.noarch.rpm
cp $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Rabbitmq/rabbitmq-signing-key-public.asc ./
cp  -R $INSIGHTS_OFFLINE_ARTIFACTS_DIRECTORY/Rabbitmq/RabbitMQ/ ./
cd RabbitMQ  && cp rabbitmq.config /etc/rabbitmq/
chkconfig rabbitmq-server on &&  service rabbitmq-server start
rabbitmq-plugins enable rabbitmq_management
sleep 15
echo "RabbitMQ user 'guest' to create user and to set permissions. Please provide it."
echo -n "Password: "
read -s RABBITMQ_GUEST_USER_PASSWORD
echo "RabbitMQ user 'iSight' is going to be created requires password. Please provide it."
echo -n "Password: "
read -s RABBITMQ_ISIGHT_USER_PASSWORD
echo "User creation is in progres.Please wait ..."
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"password":"'$RABBITMQ_ISIGHT_USER_PASSWORD'","tags":"administrator"}' "http://localhost:15672/api/users/iSight"
sleep 15
curl -X PUT -u guest:$RABBITMQ_GUEST_USER_PASSWORD -H "Content-Type: application/json" -d '{"configure":".*","write":".*","read":".*"}' "http://localhost:15672/api/permissions/%2f/iSight"