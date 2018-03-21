REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\citfs
nssm install TFSAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\citfs\CITFSAgent.bat
sleep 2
net start TFSAgent
