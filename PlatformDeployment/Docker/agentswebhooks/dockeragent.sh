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

baseExtractionPath="/opt/insightsagents/PlatformAgents"

#UPDATE RabbitMQ details to BaseAgent config.json
configPath="/opt/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json"
dos2unix /opt/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json

echo $(jq --arg baseExtractionPath $baseExtractionPath '(.baseExtractionPath) |= $baseExtractionPath' $configPath) > $configPath
echo $(jq --arg rabbitmqIP $rabbitmqIP '(.mqConfig.host) |= $rabbitmqIP' $configPath) > $configPath
echo $(jq --arg rabbitMqPort $rabbitMqPort '(.mqConfig.port) |= $rabbitMqPort' $configPath) > $configPath
echo $(jq --arg rabbitMqUser $rabbitMqUser '(.mqConfig.user) |= $rabbitMqUser' $configPath) > $configPath
echo $(jq --arg rabbitMqPassword $rabbitMqPassword '(.mqConfig.password) |= $rabbitMqPassword' $configPath) > $configPath

jq . $configPath > $configPath.tmp
mv $configPath.tmp $configPath

neo4jEndpoint=http://$neo4jIP:$neo4jHttpPort
neo4jBoltEndpoint=http://$neo4jIP:$neo4jBoltPort
neo4jToken=$(echo -n $neo4jUser:$neo4jPassword | base64)
grafanaDBEndpoint=jdbc:postgresql://$postgresIP:$postgresPort/grafana
insightsDBUrl=jdbc:postgresql://$postgresIP:$postgresPort/insight
grafanaDBUrl=jdbc:postgresql://$postgresIP:$postgresPort/grafana


if [[ ! -z $enablespin ]]
then
    hostname="insightsdomain.subdomain.com"
else
    hostname=$hostPublicIP
fi

ServiceEndpoint=http://$hostname:$servicePort
insightsServiceURL=http://$hostname:$servicePort/app
grafanaEndpoint=http://$hostPrivateIP:$grafanaPort

#UPDATE ServiceEndpoint - uiConfig.json and server-config.json
configPath='/usr/INSIGHTS_HOME/.InSights/server-config.json'
dos2unix $configPath

wait-for-url() {
    echo "Testing $1"
    bash -c \
    'while [[ "$(curl -s -u $rabbitMqUser:$rabbitMqPassword -o /dev/null  -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 2;\
    done' ${1}
    echo "OK!"
}

wait-for-url http://$rabbitmqIP:$rabbitMqUIPort/api/users/iSight/permissions

until `nc -z $hostname $servicePort`; do
    echo "Waiting on Platform Service to come up..."
    sleep 10
done
# add some delay after port is up to validate that services are all up
sleep 10

echo $(jq --arg grafanaEndpoint $grafanaEndpoint '(.grafana.grafanaEndpoint) |= $grafanaEndpoint' $configPath) > $configPath
echo $(jq --arg grafanaDBEndpoint $grafanaDBEndpoint '(.grafana.grafanaDBEndpoint) |= $grafanaDBEndpoint' $configPath) > $configPath
echo $(jq --arg postgresIP $postgresIP --arg hostPublicIP $hostPublicIP '(.trustedHosts) |= .+ [$postgresIP,$hostPublicIP]' $configPath) > $configPath
echo $(jq --arg neo4jEndpoint $neo4jEndpoint '(.graph.endpoint) |= $neo4jEndpoint' $configPath) > $configPath
echo $(jq --arg neo4jToken $neo4jToken '(.graph.authToken) |= $neo4jToken' $configPath) > $configPath
echo $(jq --arg neo4jBoltEndpoint $neo4jBoltEndpoint '(.graph.boltEndPoint) |= $neo4jBoltEndpoint' $configPath) > $configPath
echo $(jq --arg grafanaDBUser $grafanaDBUser '(.postgre.userName) |= $grafanaDBUser' $configPath) > $configPath
echo $(jq --arg grafanaDBPass $grafanaDBPass '(.postgre.password) |= $grafanaDBPass' $configPath) > $configPath
echo $(jq --arg insightsDBUrl $insightsDBUrl '(.postgre.insightsDBUrl) |= $insightsDBUrl' $configPath) > $configPath
echo $(jq --arg grafanaDBUrl $grafanaDBUrl '(.postgre.grafanaDBUrl) |= $grafanaDBUrl' $configPath) > $configPath
echo $(jq --arg rabbitmqIP $rabbitmqIP '(.messageQueue.host) |= $rabbitmqIP' $configPath) > $configPath
echo $(jq --arg rabbitMqUser $rabbitMqUser '(.messageQueue.user) |= $rabbitMqUser' $configPath) > $configPath
echo $(jq --arg rabbitMqPassword $rabbitMqPassword '(.messageQueue.password) |= $rabbitMqPassword' $configPath) > $configPath
echo $(jq --arg rabbitMqPort $rabbitMqPort '(.messageQueue.port) |= $rabbitMqPort' $configPath) > $configPath
echo $(jq --arg insightsServiceURL $ServiceEndpoint '(.insightsServiceURL) |= $insightsServiceURL' $configPath) > $configPath

sed -i "s/\r$//g" $configPath

jq . $configPath > $configPath.tmp
mv $configPath.tmp $configPath

PROMTAIL_PORT=${PROMTAIL_LISTEN_PORT} yq e  -i '.server.http_listen_port = env(PROMTAIL_PORT)' /opt/InSights/Promtail/promtail-local-config.yaml
LOKI="http://${LOKI_HOST}:${LOKI_PORT}/loki/api/v1/push" yq e  -i '.clients[0].url = strenv(LOKI)' /opt/InSights/Promtail/promtail-local-config.yaml

sed -i -e "s/app.mqHost=.*/app.mqHost=$rabbitmqIP/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqUser=.*/app.mqUser=$rabbitMqUser/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqPassword=.*/app.mqPassword=$rabbitMqPassword/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqExchangeName=.*/app.mqExchangeName=iSight/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqPort=.*/app.mqPort=$rabbitMqPort/g" /opt/insightsWebhook/webhook_subscriber.properties

echo 'executing script'
#/etc/init.d/InSightsDaemonAgent start
sh +x /opt/insightsagents/AgentDaemon/installdaemonagent.sh dockerAlpine
if [ "$enablePromtail" == true ]
then
 sh +x /etc/init.d/InsightsPromtail start
fi
cd /opt/insightsWebhook/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/insightsWebhook/PlatformInsightsWebHook.jar  > /dev/null 2>&1
#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
