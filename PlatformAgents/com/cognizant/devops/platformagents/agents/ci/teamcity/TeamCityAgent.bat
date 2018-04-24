pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\teamcity
python -c "from com.cognizant.devops.platformagents.agents.ci.teamcity.TeamCityAgent import TeamCityAgent; TeamCityAgent()"