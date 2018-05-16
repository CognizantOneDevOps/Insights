REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\snow
IF /I "%1%" == "UNINSTALL" (
	net stop SnowAgent
	sc delete SnowAgent
) ELSE (
	net stop SnowAgent
	sc delete SnowAgent
        nssm install SnowAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\snow\SnowAgent.bat
        net start SnowAgent
)
