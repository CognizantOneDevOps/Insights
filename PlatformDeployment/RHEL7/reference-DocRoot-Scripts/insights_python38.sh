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
# Pythonecho "#################### Installing Python with Virtual Env ####################"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir python3 && cd python3 && sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/python/Python.tar.gz
sudo tar -zxf Python.tar.gz
cd Python
sudo yum install gcc -y
sudo yum install openssl-devel -y 
sudo yum install bzip2-devel -y  
sudo yum install libffi-devel -y
sudo ./configure --enable-optimizations
sudo make altinstall
sudo rm -f /usr/bin/python3
sudo ln -s $INSIGHTS_APP_ROOT_DIRECTORY/python3/Python/python /usr/bin/python3
cd $INSIGHTS_APP_ROOT_DIRECTORY/python3 && sudo wget https://infra.cogdevops.com/repository/docroot/insights_install/installationScripts/latest/RHEL/python/get-pip.py
python3 --version
sudo python3 get-pip.py
sudo python3 -m pip install setuptools -U
sudo python3 -m pip install pika==1.1.0
sudo python3 -m pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch
python3 --version
sleep 5
