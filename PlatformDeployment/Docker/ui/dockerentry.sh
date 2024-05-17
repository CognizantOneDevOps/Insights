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

# source /etc/environment
# source /etc/profile

# if [[ ! -z $enablespin ]]
# then
#     hostname="insightsdomain.subdomain.com"
# else
#     hostname=$hostPublicIP
# fi

#Framing Endpoint Url
uiConfigPath='/opt/UI/UI/insights/config/uiConfig.json'

#update uiconfig
echo $(jq --arg serviceHost $SERVICE_ENDPOINT '(.serviceHost) |= $serviceHost' $uiConfigPath) > $uiConfigPath
echo $(jq --arg grafanaHost $GRAFANA_ENDPOINT '(.grafanaHost) |= $grafanaHost' $uiConfigPath) > $uiConfigPath

jq . $uiConfigPath > $uiConfigPath.tmp
mv $uiConfigPath.tmp $uiConfigPath


#starting services
cd /opt/UI/UI/
sudo node UI.js & > UIlog.txt 2> UIerrorlog.txt
#assign tails pid to docker to keep it running continuously

tail -f /dev/null

# now we bring the primary process back into the foreground
# and leave it there
fg %1
