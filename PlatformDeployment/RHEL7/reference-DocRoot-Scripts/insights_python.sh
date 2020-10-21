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
#sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/python/python_dependencies.zip
#sudo unzip python_dependencies.zip && cd python_dependencies
#sudo rpm -Uvh *.rpm
cd /opt
sudo mkdir python && cd python && sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
sudo tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && sudo yum install gcc -y && sudo ./configure --prefix=/opt/
sudo make install && cd .. && sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/python/get-pip.py
python --version
sudo python get-pip.py
sudo pip install pika==1.1.0
sudo pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch
sleep 5

