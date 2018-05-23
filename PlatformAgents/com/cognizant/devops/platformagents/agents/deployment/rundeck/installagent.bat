REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\rundeck
IF /I "%1%" == "UNINSTALL" (
	net stop RundeckAgent
	sc delete RundeckAgent
) ELSE (
	net stop RundeckAgent
	sc delete RundeckAgent
    nssm install RundeckAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\rundeck\RundeckAgent.bat
    net start RundeckAgent
)
