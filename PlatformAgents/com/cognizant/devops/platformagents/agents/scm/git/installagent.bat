REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\git
IF /I "%1%" == "UNINSTALL" (
	net stop GitAgent
	sc delete GitAgent
) ELSE (
	net stop GitAgent
	sc delete GitAgent
	nssm install GitAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\git\GitAgent.bat
	net start GitAgent
)
