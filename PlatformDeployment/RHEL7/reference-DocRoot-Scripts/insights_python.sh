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
# Python 2.7.11
echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir python && cd python && sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
sudo tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && sudo yum install gcc -y && sudo ./configure --prefix=$INSIGHTS_APP_ROOT_DIRECTORY
sudo make install && cd .. && sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/python/get-pip.py
python --version
sudo python get-pip.py
sudo pip install setuptools -U
sudo pip install pika==1.1.0
sudo pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime==1.7.4 neo4j==1.7.6 neobolt==1.7.17 elasticsearch
sleep 5
