REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\ucd
nssm install UCDAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\ucd\UCDAgent.bat
sleep 2
net start UCDAgent
