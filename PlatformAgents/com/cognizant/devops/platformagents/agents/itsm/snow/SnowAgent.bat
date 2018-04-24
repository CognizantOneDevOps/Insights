pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\snow
python -c "from com.cognizant.devops.platformagents.agents.itsm.snow.snowAgent import snowAgent; snowAgent()"