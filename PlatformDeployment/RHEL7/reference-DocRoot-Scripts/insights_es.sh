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
# install ES  - Some Issues here test on new server
echo "#################### Installing EleasticSearch 7.6.1 with configs ####################"
source /etc/environment
source /etc/profile
mkdir -p $INSIGHTS_APP_ROOT_DIRECTORY/elasticsearch
cd $INSIGHTS_APP_ROOT_DIRECTORY/elasticsearch
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/es/latest/elasticsearch.rpm
sudo rpm -Uvh elasticsearch.rpm
export ES_JAVA_HOME=/usr/share/elasticsearch/jdk
sudo systemctl daemon-reload
sudo systemctl enable elasticsearch.service
sudo systemctl start elasticsearch.service
