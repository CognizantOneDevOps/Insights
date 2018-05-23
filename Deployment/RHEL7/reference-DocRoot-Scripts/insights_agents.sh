echo "Set up Agent_Daemon"
cd /opt/ && mkdir insightsagent
chmod -R 755 insightsagent/
cd insightsagent
export INSIGHTS_AGENT_HOME=`pwd`
echo INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/environment
echo "export" INSIGHTS_AGENT_HOME=`pwd` | tee -a /etc/profile
mkdir AgentDaemon
mkdir PlatformAgents
chmod -R 755 AgentDaemon
chmod -R 755 PlatformAgents
echo $INSIGHTS_AGENT_HOME
cd AgentDaemon
sudo wget http://platform.cogdevops.com/insights_install/release/latest/agentdaemon.zip -O agentdaemon.zip
sudo unzip agentdaemon.zip && sudo rm -rf agentdaemon.zip
sudo source /etc/environment
sudo source /etc/profile