REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\snow
nssm install snowAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\snow\SnowAgent.bat
sleep 2
net start SnowAgent
