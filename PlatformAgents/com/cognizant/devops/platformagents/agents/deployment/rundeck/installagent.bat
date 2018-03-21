REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rundeck
nssm install RundeckAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\rundeck\RundeckAgent.bat
sleep 2
net start RundeckAgent
