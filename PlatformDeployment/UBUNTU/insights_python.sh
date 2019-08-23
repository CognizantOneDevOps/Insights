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
sudo apt-get update
sudo apt-get install make
mkdir python && cd python && wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && sudo apt-get install gcc -y && sudo ./configure --prefix=/opt/
sudo make install && cd .. && wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/get-pip.py
sudo apt-get install python2.7
sudo apt-get install python-minimal
sleep 10
sudo python get-pip.py
sudo pip install pika requests apscheduler python-dateutil xmltodict pytz requests_ntlm
source /etc/environment
source /etc/profile


