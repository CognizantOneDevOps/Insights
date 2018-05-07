pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\tfs
python -c "from com.cognizant.devops.platformagents.agents.scm.tfs.TFSAgent import TFSAgent; TFSAgent()"