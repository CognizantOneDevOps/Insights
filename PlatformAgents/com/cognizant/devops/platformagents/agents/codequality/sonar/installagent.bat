REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\sonar
nssm install SonarAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\sonar\SonarAgent.bat
sleep 2
net start SonarAgent
