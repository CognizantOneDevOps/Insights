REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\teamcity
nssm install TeamCityAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\teamcity\TeamCityAgent.bat
sleep 2
net start TeamCityAgent
