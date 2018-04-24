pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\git
python -c "from com.cognizant.devops.platformagents.agents.scm.git.GitAgent import GitAgent; GitAgent()"