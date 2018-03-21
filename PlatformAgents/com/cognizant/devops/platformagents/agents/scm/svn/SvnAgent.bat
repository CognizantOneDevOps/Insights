pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\svn
python -c "from com.cognizant.devops.platformagents.agents.scm.svn.svnAgent import svnAgent; svnAgent()"