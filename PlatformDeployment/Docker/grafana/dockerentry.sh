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

echo "[server]" > /opt/grafana/conf/custom.ini
echo "root_url = $GRAFANA_ENDPOINT" >> /opt/grafana/conf/custom.ini

#update grafana config
dos2unix /opt/grafana/conf/defaults.ini
sed -i "/root_url =/ s/=.*/=$GRAFANA_ENDPOINT/" /opt/grafana/conf/defaults.ini
sed -i '/^\[database\]$/,/^\[/{s/^type[[:space:]]*=.*/type = postgres/}' /opt/grafana/conf/defaults.ini
sed -i '/^\[database\]$/,/^\[/{s/^name[[:space:]]*=.*/name = grafana/}' /opt/grafana/conf/defaults.ini
sed -i "/allow_loading_unsigned_plugins =/ s/=.*/=cts-neo-4-j-4-0,neo4j-datasource,Inference,cde-inference-plugin,cde-fusion-panel,cognizant-insights-charts/" /opt/grafana/conf/defaults.ini
sed -i "/allow_embedding =/ s/=.*/=true/" /opt/grafana/conf/defaults.ini
sed -i 's@</body>@<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script></body>@g' /opt/grafana/public/views/index.html

sed -i -e "/^host /s/=.*$/= $POSTGRES_HOST:$POSTGRES_PORT/" /opt/grafana/conf/defaults.ini
sed -i -e "/^user /s/=.*$/= $GRAFANA_DB_USERNAME/" /opt/grafana/conf/defaults.ini
sed -i -e "/^password /s/=.*$/= $GRAFANA_DB_PASSWORD/" /opt/grafana/conf/defaults.ini
sed -i -e "/^max_connections /s/=.*$/= 0/" /opt/grafana/conf/defaults.ini

until `nc -z $POSTGRES_HOST $POSTGRES_PORT`; do
    echo "Waiting on Postgres to come up..."
    sleep 10
done

sleep 20
#starting services
cd /opt/grafana/bin/ && nohup ./grafana-server &> /usr/INSIGHTS_HOME/logs/grafana-server.log 2>&1 &
if [ "$LOKI_ENABLE" == true ]
then
 sh +x /etc/init.d/InsightsLoki start
fi
#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1

