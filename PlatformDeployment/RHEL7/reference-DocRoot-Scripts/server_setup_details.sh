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
#Set insights configuration for postgres, rabbitmq, insights, and grafana
#configuration settings for server-config.json
echo "Configuring Insights: Grafana(adminUserName), postgre(userName), RabbitMQ(user), Neo4J(user) which will create in server-config.json :"
#Postgres grafana user credentials using jq
echo -n "Grafana User created in Postgre:"
read POSTGRE_USER_NAME
echo "Grafana User Password created in Postgre:"
read -s POSTGRE_USER_PASSWORD
jq --arg user $POSTGRE_USER_NAME '.postgre.userName=$user' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
jq --arg pass $POSTGRE_USER_PASSWORD '.postgre.password=$pass' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
#RabbitMQ
echo -n "RabbitMQ(user):"
read RABBIT_MQ_USER
echo "Password:"
read -s RABBIT_MQ_USER_PASSWORD
jq --arg user $RABBIT_MQ_USER '.messageQueue.user=$user' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
jq --arg pass $RABBIT_MQ_USER_PASSWORD '.messageQueue.password=$pass' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
#Grafana
echo -n "Grafana(adminUserName):"
read GRAFANA_ADMIN_USER_NAME
echo "Password:"
read -s GRAFANA_ADMIN_USER_PASSWORD
jq --arg user $GRAFANA_ADMIN_USER_NAME '.grafana.adminUserName=$user' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
jq --arg pass $GRAFANA_ADMIN_USER_PASSWORD '.grafana.adminUserPassword=$pass' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f
#NEO4J
echo -n "Neo4J(user):"
read NEO4J_USER
echo "Password:"
read -s NEO4J_PASSWORD
ENCODED_TOKEN=`echo -n $NEO4J_USER:$NEO4J_PASSWORD| base64`
jq --arg token $ENCODED_TOKEN '.graph.authToken=$token' $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json > $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp && mv $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json.tmp $INSIGHTS_HOME_ROOT_DIRECTORY/INSIGHTS_HOME/.InSights/server-config.json -f