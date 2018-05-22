pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rundeck
python -c "from com.cognizant.devops.platformagents.agents.deployment.rundeck.RundeckAgent import RundeckAgent; RundeckAgent()"