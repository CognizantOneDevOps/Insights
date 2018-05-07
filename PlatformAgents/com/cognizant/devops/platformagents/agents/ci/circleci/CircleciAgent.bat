pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\circleci
python -c "from com.cognizant.devops.platformagents.agents.ci.circleci.CircleAgent import CircleAgent; CircleAgent()"