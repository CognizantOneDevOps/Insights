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
# Pythonecho "#################### Installing Python 3.7.4 with Virtual Env ####################"
cd /opt
sudo mkdir python && cd python && sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/Python-3.7.4.tgz
sudo tar -zxf Python-3.7.4.tgz && cd Python-3.7.4 && sudo yum install gcc -y && sudo yum install openssl-devel -y && sudo yum install bzip2-devel -y  && sudo yum install libffi-devel -y 
sudo ./configure
sudo make altinstall
ln -s /opt/python/Python-3.7.4/python /usr/bin/python3
sudo python3 -m pip install pika==0.12.0
sudo python3 -m pip install requests apscheduler python-dateutil xmltodict pytz requests_ntlm boto3
sleep 5
