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
#echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
#sudo mkdir python && cd python && sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
#sudo tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && sudo yum install gcc -y && sudo ./configure --prefix=/opt/
#sudo make install && cd .. && sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/get-pip.py
#sudo python get-pip.py
#sudo pip install pika requests apscheduler python-dateutil xmltodict pytz requests_ntlm
#sudo mkdir /opt/insightsagents
#cd /opt/insightsagents
#export INSIGHTS_AGENT_HOME=`pwd`
#sudo echo INSIGHTS_AGENT_HOME=`pwd` | sudo tee -a /etc/environment
#sudo echo "export" INSIGHTS_AGENT_HOME=`pwd` | sudo tee -a /etc/profile
#source /etc/environment
#source /etc/profile
#sleep 5
#echo "Get all avaiable Python Agents"
#cd $INSIGHTS_AGENT_HOME
#sudo wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/agents/PlatformAgents.zip -O PlatformAgents.zip
#sudo unzip PlatformAgents.zip && sudo rm -rf PlatformAgents.zip

echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
apt-get update
apt install make
mkdir python && cd python && wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/Python-2.7.11.tgz
tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && apt-get install gcc -y && ./configure --prefix=/opt/
make install && cd .. && wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/python/get-pip.py
apt-get install python2.7
apt install python-minimal
sleep 10
python get-pip.py
pip install pika requests apscheduler python-dateutil xmltodict pytz requests_ntlm
mkdir /opt/insightsagents
cd /opt/insightsagents
export INSIGHTS_AGENT_HOME=`pwd`
echo INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/environment
echo "export" INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/profile
sudo -E source /etc/environment
sudo -E source /etc/profile
sleep 5
echo "Get all avaiable Python Agents"
cd $INSIGHTS_AGENT_HOME
wget https://platform.cogdevops.com/insights_install/installationScripts/latest/RHEL/agents/PlatformAgents.zip -O PlatformAgents.zip
unzip PlatformAgents.zip && rm -rf PlatformAgents.zip
