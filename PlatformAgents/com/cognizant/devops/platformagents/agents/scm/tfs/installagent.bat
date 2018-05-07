REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\tfs
nssm install TFSAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\tfs\TFSAgent.bat
sleep 2
net start TFSAgent
