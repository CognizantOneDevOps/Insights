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

#Framing Endpoint Url
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
    hostname=$hostInstance
fi

ServiceEndpoint=http://$hostname:$servicePort
insightsServiceURL=http://$hostname:$servicePort/app
grafanaEndpoint=http://$hostname:$grafanaPort

#UPDATE ServiceEndpoint - uiConfig.json and server-config.json
configPath='/usr/INSIGHTS_HOME/.InSights/server-config.json'
dos2unix $configPath
uiConfigPath='/opt/apache-tomcat/webapps/app/config/uiConfig.json'
dos2unix $uiConfigPath

jq --arg grafanaEndpoint $grafanaEndpoint '(.grafana.grafanaEndpoint) |= $grafanaEndpoint' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBEndpoint $grafanaDBEndpoint '(.grafana.grafanaDBEndpoint) |= $grafanaDBEndpoint' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg postgresIP $postgresIP --arg hostInstance $hostInstance '(.trustedHosts) |= .+ [$postgresIP,$hostInstance]' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jEndpoint $neo4jEndpoint '(.graph.endpoint) |= $neo4jEndpoint' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jToken $neo4jToken '(.graph.authToken) |= $neo4jToken' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg neo4jBoltEndpoint $neo4jBoltEndpoint '(.graph.boltEndPoint) |= $neo4jBoltEndpoint' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBUser $grafanaDBUser '(.postgre.userName) |= $grafanaDBUser' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBPass $grafanaDBPass '(.postgre.password) |= $grafanaDBPass' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg insightsDBUrl $insightsDBUrl '(.postgre.insightsDBUrl) |= $insightsDBUrl' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg grafanaDBUrl $grafanaDBUrl '(.postgre.grafanaDBUrl) |= $grafanaDBUrl' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitmqIP $rabbitmqIP '(.messageQueue.host) |= $rabbitmqIP' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqUser $rabbitMqUser '(.messageQueue.user) |= $rabbitMqUser' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqPassword $rabbitMqPassword '(.messageQueue.password) |= $rabbitMqPassword' $configPath > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg rabbitMqPort $rabbitMqPort '(.messageQueue.port) |= $rabbitMqPort' $configPath  > INPUT.tmp && mv INPUT.tmp $configPath
jq --arg insightsServiceURL $ServiceEndpoint '(.insightsServiceURL) |= $insightsServiceURL' $configPath > INPUT.tmp && mv INPUT.tmp $configPath

sed -i "s/\r$//g" $configPath

#jq . $configPath > $configPath.tmp
#mv $configPath.tmp $configPath


#update uiconfig
echo $(jq --arg serviceHost $ServiceEndpoint '(.serviceHost) |= $serviceHost' $uiConfigPath) > $uiConfigPath
echo $(jq --arg grafanaHost $grafanaEndpoint '(.grafanaHost) |= $grafanaHost' $uiConfigPath) > $uiConfigPath

jq . $uiConfigPath > $uiConfigPath.tmp
mv $uiConfigPath.tmp $uiConfigPath
cp /opt/apache-tomcat/webapps/app/config/uiConfig.json /opt/apache-tomcat/webapps/insights/config/
PROMTAIL_PORT=${PROMTAIL_LISTEN_PORT} yq e  -i '.server.http_listen_port = env(PROMTAIL_PORT)' /opt/InSights/Promtail/promtail-local-config.yaml
LOKI="http://${LOKI_HOST}:${LOKI_PORT}/loki/api/v1/push" yq e  -i '.clients[0].url = strenv(LOKI)' /opt/InSights/Promtail/promtail-local-config.yaml


until `nc -z $hostname $grafanaPort`; do
    echo "Waiting on Grafana to come up..."
    sleep 10
done
  # add some delay after port is up to validate that services are all up
sleep 10

until `nc -z $neo4jIP $neo4jHttpPort`; do
    echo "Waiting on neo4j to come up..."
    sleep 10
done
  # add some delay after port is up to validate that services are all up
sleep 10

#updating environmental variables
source /etc/environment
source /etc/profile

#starting services
cd /opt/apache-tomcat/bin && ./startup.sh
if [ "$enablePromtail" == true ]
then
 sh +x /etc/init.d/InsightsPromtail start
fi

#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
