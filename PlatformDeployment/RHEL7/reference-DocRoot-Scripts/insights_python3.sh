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
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/python_dependencies.zip
#sudo unzip python_dependencies.zip && cd python_dependencies
#sudo rpm -Uvh *.rpm
cd /opt
sudo wget https://infra.cogdevops.com:8443/repository/docroot/insights_install/installationScripts/latest/RHEL/python/Python.tar.gz
sudo tar -zxf Python.tar.gz
cd Python
sudo yum install gcc -y
sudo yum install openssl-devel -y 
sudo yum install bzip2-devel -y  
sudo yum install libffi-devel -y
sudo ./configure --enable-optimizations
sudo make altinstall
sudo rm -f /usr/bin/python
sudo ln -s /opt/Python/python /usr/bin/python
sudo python -m pip install pika==1.1.0
sudo python -m pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3 urllib3 neotime neo4j neobolt elasticsearch
python --version
sleep 5
