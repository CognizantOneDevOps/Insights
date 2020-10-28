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
cd /opt
sudo yum install httpd -y
cd /etc/httpd/conf
rm -f httpd.conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd.conf
cd /etc/httpd/conf.d
rm -f httpd-vhosts.conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd-vhosts.conf
myextip=$(wget -qO- icanhazip.com)
echo $myextip
sed -i -e "s|${myextip}:3000|${myextip}\/grafana|g" /usr/INSIGHTS_HOME/.InSights/server-config.json
sed -i -e "s/.*serviceHost.*/    \"serviceHost\": \"$myextip\",/g" /opt/apache-tomcat-8.5.27/webapps/app/config/uiConfig.json
sed -i -e "s/.*grafanaHost.*/    \"grafanaHost\": \"$myextip\/grafana\"/g" /opt/apache-tomcat-8.5.27/webapps/app/config/uiConfig.json
cd /opt/grafana/conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/custom.ini
apachectl -k start
service grafana restart
