REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rally
IF /I "%1%" == "UNINSTALL" (
	net stop RallyAgent
	sc delete RallyAgent
) ELSE (
	net stop RallyAgent
	sc delete RallyAgent
    nssm install RallyAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\rally\RallyAgent.bat
    net start RallyAgent
)