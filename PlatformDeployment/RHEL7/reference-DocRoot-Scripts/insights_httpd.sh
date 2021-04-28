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
source /etc/environment
source /etc/profile
sudo yum install httpd -y
cd /etc/httpd/conf
rm -f httpd.conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd.conf
cd /etc/httpd/conf.d
rm -f httpd-vhosts.conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/httpd-vhosts.conf
cd $INSIGHTS_APP_ROOT_DIRECTORY/grafana/conf
wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/httpd/RHEL/http/custom.ini
apachectl -k start
service grafana restart
