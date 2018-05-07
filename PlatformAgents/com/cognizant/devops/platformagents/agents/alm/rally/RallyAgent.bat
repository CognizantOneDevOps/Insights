pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rally
python -c "from com.cognizant.devops.platformagents.agents.alm.rally.RallyAgent import RallyAgent; RallyAgent()"