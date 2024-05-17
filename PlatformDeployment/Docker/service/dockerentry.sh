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


#UPDATE IPs -SERVER_CONFIG.JSON

source /etc/environment
source /etc/profile

#Form Neo4j token
neo4jToken=$(echo -n $INSIGHTS_NEO4J_USERNAME:$INSIGHTS_NEO4J_PASSWORD | base64)

#Framing Endpoint Url
grafanaDBEndpoint=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/grafana
insightsDBUrl=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/insight
grafanaDBUrl=jdbc:postgresql://$POSTGRES_HOST:$POSTGRES_PORT/grafana
# if [[ ! -z $enablespin ]]
# then
#     hostname="insightsdomain.subdomain.com"
# else
#     hostname=$UI_ENDPOINT
# fi


#UPDATE server-config.json
configPath='/usr/INSIGHTS_HOME/.InSights/server-config.json'
dos2unix $configPath


jq --arg grafanaEndpoint $GRAFANA_ENDPOINT_PRIVATE '(.grafana.grafanaEndpoint) |= $grafanaEndpoint' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBEndpoint $grafanaDBEndpoint '(.grafana.grafanaDBEndpoint) |= $grafanaDBEndpoint' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg postgresIP $POSTGRES_HOST --arg ServiceEndpoint $SERVICE_ENDPOINT --arg ServiceHostPublic $SERVICE_HOST_PUBLIC --arg insightsUIURL $UI_ENDPOINT '(.trustedHosts) |= .+ [$postgresIP,$ServiceEndpoint,$ServiceHostPublic,$insightsUIURL]' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jEndpoint $INSIGHTS_NEO4J_ENDPOINT '(.graph.endpoint) |= $neo4jEndpoint' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jToken $neo4jToken '(.graph.authToken) |= $neo4jToken' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jBoltEndpoint $INSIGHTS_NEO4J_BOLTENDPOINT '(.graph.boltEndPoint) |= $neo4jBoltEndpoint' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBUser $GRAFANA_DB_USERNAME '(.postgre.userName) |= $grafanaDBUser' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBPass $GRAFANA_DB_PASSWORD '(.postgre.password) |= $grafanaDBPass' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg insightsDBUrl $insightsDBUrl '(.postgre.insightsDBUrl) |= $insightsDBUrl' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBUrl $grafanaDBUrl '(.postgre.grafanaDBUrl) |= $grafanaDBUrl' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitmqIP $RABBITMQ_HOST '(.messageQueue.host) |= $rabbitmqIP' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqUser $RABBITMQ_USERNAME '(.messageQueue.user) |= $rabbitMqUser' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqPassword $RABBITMQ_PASSWORD '(.messageQueue.password) |= $rabbitMqPassword' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqPort $RABBITMQ_PORT '(.messageQueue.port) |= $rabbitMqPort' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg insightsServiceURL $SERVICE_ENDPOINT '(.insightsServiceURL) |= $insightsServiceURL' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg offlineAgentPath '/opt/insightsagents/offline' '(.agentDetails.offlineAgentPath) |= $offlineAgentPath' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg unzipPath '/opt/insightsagents/unzip' '(.agentDetails.unzipPath) |= $unzipPath' $configPath > INPUT.tmp && mv INPUT.tmp $configPath

sed -i "s/\r$//g" $configPath

PROMTAIL_PORT=${PROMTAIL_LISTEN_PORT} yq e  -i '.server.http_listen_port = env(PROMTAIL_PORT)' /opt/InSights/Promtail/promtail-local-config.yaml
LOKI="$LOKI_ENDPOINT/loki/api/v1/push" yq e  -i '.clients[0].url = strenv(LOKI)' /opt/InSights/Promtail/promtail-local-config.yaml


until `nc -z $GRAFANA_HOST $GRAFANA_PORT`; do
    echo "Waiting on Grafana to come up..."
    sleep 10
done
  # add some delay after port is up to validate that services are all up
sleep 10

until `nc -z $INSIGHTS_NEO4J_HOST $INSIGHTS_NEO4J_PORT`; do
    echo "Waiting on neo4j to come up..."
    sleep 10
done
  # add some delay after port is up to validate that services are all up
sleep 10

#updating environmental variables
source /etc/environment
source /etc/profile

#starting services
cd /opt/PlatformService/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/PlatformService/PlatformService.jar  > /dev/null 2>&1 &
if [ "$PROMTAIL_ENABLE" == true ]
then
 sh +x /etc/init.d/InsightsPromtail start
fi

#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
