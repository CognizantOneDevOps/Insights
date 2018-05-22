REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\circleci
IF /I "%1%" == "UNINSTALL" (
	net stop CircleciAgent
	sc delete CircleciAgent
) ELSE (
	net stop CircleciAgent
	sc delete CircleciAgent
    nssm install CircleciAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\circleci\CircleciAgent.bat
    net start CircleciAgent
)