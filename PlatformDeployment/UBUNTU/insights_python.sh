#!/bin/bash
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
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/Ubuntu/packages/python/dependencies.zip
#sudo unzip dependencies.zip
#cd dependencies
#sudo dpkg -i *.deb
sudo mkdir /opt/python && cd /opt/python && sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/Ubuntu/packages/python/Python.tar.gz 
sudo tar -zxf Python.tar.gz
cd Python
sudo apt-get install gcc -y
sudo apt-get install libssl-dev -y
sudo apt-get install bzip2-dev -y
sudo apt-get install libffi-dev -y
sudo apt-get install make -y
sudo ./configure --enable-optimizations
sudo make altinstall
sudo rm -f /usr/bin/python
sudo ln -s /opt/python/Python/python /usr/bin/python
sudo python -m pip install pika==1.1.0
sudo python -m pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch
python --version
sleep 5
