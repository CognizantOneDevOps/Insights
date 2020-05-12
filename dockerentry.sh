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
cd /usr/INSIGHTS_HOME
wget http://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/InSightsConfig.zip
unzip InSightsConfig.zip && rm -rf InSightsConfig.zip
cp -R InSightsConfig/.InSights/ . && rm -rf InSightsConfig
export INSIGHTS_HOME=/usr/INSIGHTS_HOME
echo INSIGHTS_HOME=/usr/INSIGHTS_HOME | tee -a /etc/environment
echo "export INSIGHTS_HOME=/usr/INSIGHTS_HOME" | tee -a /etc/profile
source /etc/environment
source /etc/profile

#Framing Endpoint Url
neo4jEndpoint="http:\/\/$neo4jIP:7474"
grafanaDBEndpoint="jdbc:postgresql:\/\/$postgresIP:5432\/grafana"
insightsDBUrl="jdbc:postgresql:\/\/$postgresIP:5432\/insight"
grafanaDBUrl="jdbc:postgresql:\/\/$postgresIP:5432\/grafana"

if [[ ! -z $enablespin ]]
then
    hostname="insightsdev.cogdevops.com"
else
    hostname=$hostIP
fi

ServiceEndpoint="http:\/\/$hostname"
grafanaEndpoint="http:\/\/$hostname\/grafana"

#UPDATE ServiceEndpoint - uiConfig.json and server-config.json

configPath='/usr/INSIGHTS_HOME/.InSights/server-config.json'
sed -i -e "s/.endpoint\":.*/\"endpoint\": \"$neo4jEndpoint\",/g" $configPath
sed -i -e "s/.grafanaEndpoint\":.*/\"grafanaEndpoint\": \"$grafanaEndpoint\",/g" $configPath
sed -i -e "s/.grafanaDBEndpoint\":.*/\"grafanaDBEndpoint\": \"$grafanaDBEndpoint\",/g" $configPath
sed -i -e "s/.insightsDBUrl\":.*/\"insightsDBUrl\": \"$insightsDBUrl\",/g" $configPath
sed -i -e "s/.grafanaDBUrl\":.*/\"grafanaDBUrl\": \"$grafanaDBUrl\"/g" $configPath
sed -i -e "s/.insightsServiceURL\":.*/\"insightsServiceURL\": \"$ServiceEndpoint\",/g" $configPath
sed -i -e "s|hostip|$hostname|g" $configPath
sed -i -e "s/.host\":.*/\"host\": \"$rabbitmqIP\",/g" $configPath
sed -i "s/\r$//g" $configPath

#update grafana config
sed -i -e "s/host = localhost:5432/host = $postgresIP:5432/g"  /opt/grafana/conf/defaults.ini

#update uiconfig
sed -i -e "s/.serviceHost\":.*/\"serviceHost\": \"$ServiceEndpoint\",/g" /opt/apache-tomcat/webapps/app/config/uiConfig.json
sed -i -e "s/.grafanaHost\":.*/\"grafanaHost\": \"$grafanaEndpoint\",/g" /opt/apache-tomcat/webapps/app/config/uiConfig.json

#updating environmental variables
source /etc/environment
source /etc/profile

#updating agent deamon
cd /opt/insightsagents/AgentDaemon
echo "DELETE FROM agent_configuration WHERE id=100;" > agentconfigdelete.sql
psql -h $postgresIP -p 5432 -U postgres -d "insight" -f agentconfigdelete.sql
sed -i -e "s|psql|psql \-h $postgresIP \-p 5432|g" /opt/insightsagents/AgentDaemon/installdaemonagent.sh
sed -i -e "s|sudo service  |/etc/init.d/|g" /opt/insightsagents/AgentDaemon/installdaemonagent.sh
sed -i -e "s/.*host.*/\"host\": \"$rabbitmqIP\",/g" /opt/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json

#starting services
nohup /usr/sbin/httpd &
cd /opt/grafana && nohup ./bin/grafana-server &
cd /opt/apache-tomcat && nohup ./bin/startup.sh &
cd /opt/insightsengine/ && nohup java  -Xmx1024M -Xms500M  -jar /opt/insightsengine/PlatformEngine.jar &
cd /opt/insightsWebhook/ && nohup java  -Xmx1024M -Xms500M  -jar /opt/insightsWebhook/PlatformInsightsWebHook.jar &
sh /opt/insightsagents/AgentDaemon/installdaemonagent.sh Linux

#assign tails pid to docker to keep it running continuously
tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
