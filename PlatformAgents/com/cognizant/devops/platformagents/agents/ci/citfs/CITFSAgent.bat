pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\citfs
python -c "from com.cognizant.devops.platformagents.agents.ci.citfs.CITFSAgent import CITFSAgent; CITFSAgent()"