REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\snow
IF /I "%1%" == "UNINSTALL" (
	net stop snowAgent
	sc delete snowAgent
) ELSE (
	net stop snowAgent
	sc delete snowAgent
    nssm install snowAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\snow\SnowAgent.bat
    net start SnowAgent
)
