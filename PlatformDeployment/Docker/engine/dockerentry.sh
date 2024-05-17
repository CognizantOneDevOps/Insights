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

PROMTAIL_PORT=${PROMTAIL_LISTEN_PORT} yq e  -i '.server.http_listen_port = env(PROMTAIL_PORT)' /opt/InSights/Promtail/promtail-local-config.yaml
LOKI="$LOKI_ENDPOINT/loki/api/v1/push" yq e  -i '.clients[0].url = strenv(LOKI)' /opt/InSights/Promtail/promtail-local-config.yaml

if [ -n "$INSIGHTS_NEO4J_HOST" ]; then
  until `nc -z $INSIGHTS_NEO4J_HOST $INSIGHTS_NEO4J_PORT`; do
    echo "Waiting on neo4j to come up..."
    sleep 10
  done
fi
if [ -n "$RABBITMQ_HOST" ]; then
  until `nc -z $RABBITMQ_HOST $RABBITMQ_PORT`; do
    echo "Waiting on rabbitmq to come up..."
    sleep 10
  done
fi
until `nc -z $SERVICE_HOST $SERVICE_PORT`; do
    echo "Waiting on Platform Service to come up..."
    sleep 10
done


cd /opt/insightsengine/ && nohup java  -Xmx1024M -Xms512M  -jar /opt/insightsengine/PlatformEngine.jar  > /dev/null 2>&1 &
sh +x /etc/init.d/InSightsWorkflow start
if [ "$PROMTAIL_ENABLE" == true ]
then
 sh +x /etc/init.d/InsightsPromtail start
fi

#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
