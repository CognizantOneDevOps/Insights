REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\git
nssm install GitAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\git\GitAgent.bat
sleep 2
net start GitAgent
