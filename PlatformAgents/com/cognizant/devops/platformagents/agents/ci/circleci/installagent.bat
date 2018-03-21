REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\circleci
nssm install CircleciAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\circleci\CircleciAgent.bat
sleep 2
net start CircleciAgent
