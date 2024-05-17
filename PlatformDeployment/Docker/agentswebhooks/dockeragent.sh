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
echo $(jq --arg rabbitmqIP $RABBITMQ_HOST '(.mqConfig.host) |= $rabbitmqIP' $configPath) > $configPath
echo $(jq --arg rabbitMqPort $RABBITMQ_PORT '(.mqConfig.port) |= $rabbitMqPort' $configPath) > $configPath
echo $(jq --arg rabbitMqUser $RABBITMQ_USERNAME '(.mqConfig.user) |= $rabbitMqUser' $configPath) > $configPath
echo $(jq --arg rabbitMqPassword $RABBITMQ_PASSWORD '(.mqConfig.password) |= $rabbitMqPassword' $configPath) > $configPath

jq . $configPath > $configPath.tmp
mv $configPath.tmp $configPath

wait-for-url() {
    echo "Testing $1"
    bash -c \
    'while [[ "$(curl -s -u $RABBITMQ_USERNAME:$RABBITMQ_PASSWORD -o /dev/null  -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 2;\
    done' ${1}
    echo "OK!"
}

wait-for-url http://$RABBITMQ_HOST:$RABBITMQ_UI_PORT/api/users/iSight/permissions

until `nc -z $SERVICE_HOST $SERVICE_PORT`; do
    echo "Waiting on Platform Service to come up..."
    sleep 10
done
# add some delay after port is up to validate that services are all up
sleep 10

PROMTAIL_PORT=${PROMTAIL_LISTEN_PORT} yq e  -i '.server.http_listen_port = env(PROMTAIL_PORT)' /opt/InSights/Promtail/promtail-local-config.yaml
LOKI="$LOKI_ENDPOINT/loki/api/v1/push" yq e  -i '.clients[0].url = strenv(LOKI)' /opt/InSights/Promtail/promtail-local-config.yaml

sed -i -e "s/app.mqHost=.*/app.mqHost=$RABBITMQ_HOST/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqUser=.*/app.mqUser=$RABBITMQ_USERNAME/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqPassword=.*/app.mqPassword=$RABBITMQ_PASSWORD/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqExchangeName=.*/app.mqExchangeName=iSight/g" /opt/insightsWebhook/webhook_subscriber.properties
sed -i -e "s/app.mqPort=.*/app.mqPort=$RABBITMQ_PORT/g" /opt/insightsWebhook/webhook_subscriber.properties

echo 'executing script'
#/etc/init.d/InSightsDaemonAgent start
sh +x /opt/insightsagents/AgentDaemon/installdaemonagent.sh dockerAlpine
if [ "$PROMTAIL_ENABLE" == true ]
then
 sh +x /etc/init.d/InsightsPromtail start
fi
cd /opt/insightsWebhook/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/insightsWebhook/PlatformInsightsWebHook.jar  > /dev/null 2>&1
#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
