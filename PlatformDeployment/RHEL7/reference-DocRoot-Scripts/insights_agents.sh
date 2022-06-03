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
echo "Set up Agent_Daemon"
source /etc/environment
source /etc/profile
cd $INSIGHTS_APP_ROOT_DIRECTORY
sudo mkdir insightsagents
sudo chmod -R 755 insightsagents/
cd insightsagents
export INSIGHTS_AGENT_HOME=`pwd`
echo INSIGHTS_AGENT_HOME=`pwd` |sudo tee -a /etc/environment
echo "export" INSIGHTS_AGENT_HOME=`pwd` |sudo tee -a /etc/profile
sudo mkdir AgentDaemon
sudo mkdir PlatformAgents
sudo chmod -R 755 AgentDaemon
sudo chmod -R 755 PlatformAgents
echo $INSIGHTS_AGENT_HOME
cd AgentDaemon
echo -n "Enter Release Version: " 
read releaseVersion
sudo wget https://github.com/CognizantOneDevOps/Insights/releases/download/v${releaseVersion}/agentdaemon.zip -O agentdaemon.zip
sudo unzip agentdaemon.zip && sudo rm -rf agentdaemon.zip
source /etc/environment
source /etc/profile
sudo sed -i -e "s|/opt/insightsagents/PlatformAgents|"$INSIGHTS_APP_ROOT_DIRECTORY"/insightsagents/PlatformAgents|g" $INSIGHTS_APP_ROOT_DIRECTORY/insightsagents/AgentDaemon/com/cognizant/devops/platformagents/agents/agentdaemon/config.json
sudo chmod +x installdaemonagent.sh
sudo mkdir $INSIGHTS_APP_ROOT_DIRECTORY/agent20
sudo mkdir $INSIGHTS_APP_ROOT_DIRECTORY/agent20/download
sudo chmod -R 777 $INSIGHTS_APP_ROOT_DIRECTORY/agent20
sudo sh installdaemonagent.sh Linux
