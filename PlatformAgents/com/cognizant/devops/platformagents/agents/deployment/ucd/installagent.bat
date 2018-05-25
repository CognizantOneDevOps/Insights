REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\ucd
IF /I "%1%" == "UNINSTALL" (
	net stop UCDAgent
	sc delete UCDAgent
) ELSE (
	net stop UCDAgent
	sc delete UCDAgent   
    nssm install UCDAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\ucd\UCDAgent.bat
    net start UCDAgent
)
