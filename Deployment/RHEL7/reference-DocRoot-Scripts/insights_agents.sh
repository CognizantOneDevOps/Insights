echo "Get all avaiable Python Agents"
cd $INSIGHTS_AGENT_HOME
sudo wget http://platform.cogdevops.com/InSightsV1.0/agents/PlatformAgents.zip -O PlatformAgents.zip
sudo unzip PlatformAgents.zip && sudo rm -rf PlatformAgents.zip