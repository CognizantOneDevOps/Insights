REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\svn
nssm install svnAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\svn\svnAgent.bat
sleep 2
net start svnAgent
