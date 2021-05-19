#! /bin/bash
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


#turn on bash's job control

set -m
set -e

#UPDATE IPs -SERVER_CONFIG.JSON

source /etc/environment
source /etc/profile

#Framing Endpoint Url
neo4jEndpoint="http://$neo4jIP:7474"
neo4jBoltEndpoint="http://$neo4jIP:7687"
grafanaDBEndpoint="jdbc:postgresql://$postgresIP:5432/grafana"
insightsDBUrl="jdbc:postgresql://$postgresIP:5432/insight"
grafanaDBUrl="jdbc:postgresql://$postgresIP:5432/grafana"


if [[ ! -z $enablespin ]]
then
    hostname="insightsdomain.subdomain.com"
else
    hostname=$hostIP
fi

ServiceEndpoint="http://$hostname"
grafanaEndpoint="http://$hostname/grafana"

#UPDATE ServiceEndpoint - uiConfig.json and server-config.json
configPath='/usr/INSIGHTS_HOME/.InSights/server-config.json'

echo $(jq --arg grafanaEndpoint $grafanaEndpoint '(.grafana.grafanaEndpoint) |= $grafanaEndpoint' $configPath) > $configPath
echo $(jq --arg grafanaDBEndpoint $grafanaDBEndpoint '(.grafana.grafanaDBEndpoint) |= $grafanaDBEndpoint' $configPath) > $configPath
echo $(jq --arg postgresIP $postgresIP --arg hostIP $hostIP '(.trustedHosts) |= .+ [$postgresIP,$hostIP]' $configPath) > $configPath
echo $(jq --arg neo4jEndpoint $neo4jEndpoint '(.graph.endpoint) |= $neo4jEndpoint' $configPath) > $configPath
echo $(jq --arg neo4jBoltEndpoint $neo4jBoltEndpoint '(.graph.boltEndPoint) |= $neo4jBoltEndpoint' $configPath) > $configPath
echo $(jq --arg grafanaDBUser $grafanaDBUser '(.postgre.userName) |= $grafanaDBUser' $configPath) > $configPath
echo $(jq --arg grafanaDBPass $grafanaDBPass '(.postgre.password) |= $grafanaDBPass' $configPath) > $configPath
echo $(jq --arg insightsDBUrl $insightsDBUrl '(.postgre.insightsDBUrl) |= $insightsDBUrl' $configPath) > $configPath
echo $(jq --arg grafanaDBUrl $grafanaDBUrl '(.postgre.grafanaDBUrl) |= $grafanaDBUrl' $configPath) > $configPath
echo $(jq --arg rabbitmqIP $rabbitmqIP '(.messageQueue.host) |= $rabbitmqIP' $configPath) > $configPath
echo $(jq --arg rabbitMqUser $rabbitMqUser '(.messageQueue.user) |= $rabbitMqUser' $configPath) > $configPath
echo $(jq --arg rabbitMqPassword $rabbitMqPassword '(.messageQueue.password) |= $rabbitMqPassword' $configPath) > $configPath
echo $(jq --arg insightsServiceURL $ServiceEndpoint '(.insightsServiceURL) |= $insightsServiceURL' $configPath) > $configPath

sed -i "s/\r$//g" $configPath

#update grafana config
sed -i -e "s/host = localhost:5432/host = $postgresIP:5432/g"  /opt/grafana/conf/defaults.ini

#update uiconfig
echo $(jq --arg serviceHost $ServiceEndpoint '(.serviceHost) |= $serviceHost' /opt/apache-tomcat/webapps/app/config/uiConfig.json) > /opt/apache-tomcat/webapps/app/config/uiConfig.json
echo $(jq --arg grafanaHost $grafanaEndpoint '(.grafanaHost) |= $grafanaHost' /opt/apache-tomcat/webapps/app/config/uiConfig.json) > /opt/apache-tomcat/webapps/app/config/uiConfig.json

sed -i -e "s/app.mqHost=/app.mqHost=$rabbitmqIP/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqUser=/app.mqUser=$rabbitMqUser/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqPassword=/app.mqPassword=$rabbitMqPassword/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqExchangeName=/app.mqExchangeName=iSight/g" /opt/insightsWebhook/webhook_subscriber.properties


#updating environmental variables
source /etc/environment
source /etc/profile

#updating agent deamon
#cd /opt/insightsagents/AgentDaemon
#echo "DELETE FROM agent_configuration WHERE id=100;" > agentconfigdelete.sql
#psql -h $postgresIP -p 5432 -U postgres -d "insight" -f agentconfigdelete.sql

sed -i -e "s/.*host.*/\"host\": \"$rabbitmqIP\",/g" /opt/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json

#starting services
nohup /usr/sbin/httpd &
cd /opt/grafana/bin && nohup ./grafana-server > grafana-server.log 2>&1 &
cd /opt/apache-tomcat/bin && ./startup.sh
cd /opt/insightsengine/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/insightsengine/PlatformEngine.jar  > /dev/null 2>&1 &
cd /opt/insightsWebhook/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/insightsWebhook/PlatformInsightsWebHook.jar  > /dev/null 2>&1 &
cd /opt/insightsWorkflow/ && nohup java -Xmx1024M -Xms512M -jar /opt/insightsWorkflow/PlatformWorkflow.jar  > /dev/null 2>&1 &
#sh /opt/insightsagents/AgentDaemon/installdaemonagent.sh Linux
/etc/init.d/InSightsDaemonAgent start

#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1