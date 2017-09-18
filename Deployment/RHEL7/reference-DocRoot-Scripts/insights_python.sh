# Python 2.7.11
echo "#################### Installing Python 2.7.11 with Virtual Env ####################"
sudo mkdir python && cd python && sudo wget http://platform.cogdevops.com/InSightsV1.0/python/Python-2.7.11.tgz
sudo tar -zxf Python-2.7.11.tgz && cd Python-2.7.11 && sudo yum install gcc -y && sudo ./configure --prefix=/opt/
sudo make install && cd .. && sudo wget http://platform.cogdevops.com/InSightsV1.0/python/get-pip.py 
sudo python get-pip.py
sudo pip install pika requests apscheduler python-dateutil xmltodict pytz requests_ntlm
sudo mkdir /opt/insightsagents
cd /opt/insightsagents
export INSIGHTS_AGENT_HOME=`pwd`
sudo echo INSIGHTS_AGENT_HOME=`pwd` | sudo tee -a /etc/environment
sudo echo "export" INSIGHTS_AGENT_HOME=`pwd` | sudo tee -a /etc/profile
source /etc/environment
source /etc/profile
sleep 5
echo "Get all avaiable Python Agents"
cd $INSIGHTS_AGENT_HOME
sudo wget http://platform.cogdevops.com/InSightsV1.0/agents/PlatformAgents.zip -O PlatformAgents.zip
sudo unzip PlatformAgents.zip && sudo rm -rf PlatformAgents.zip