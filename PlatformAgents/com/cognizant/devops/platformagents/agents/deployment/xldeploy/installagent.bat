REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\xldeploy
nssm install XLDeployAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\xldeploy\XLDeployAgent.bat
sleep 2
net start XLDeployAgent
