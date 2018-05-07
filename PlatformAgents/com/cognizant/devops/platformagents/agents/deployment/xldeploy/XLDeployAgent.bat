pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\xldeploy
python -c "from com.cognizant.devops.platformagents.agents.deployment.xldeploy.XLDeployAgentAgent import XLDeployAgentAgent; XLDeployAgentAgent()"