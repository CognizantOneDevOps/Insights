REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rally
nssm install RallyAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\rally\RallyAgent.bat
sleep 2
net start RallyAgent
