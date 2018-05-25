REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\teamcity
IF /I "%1%" == "UNINSTALL" (
	net stop TeamCityAgent
	sc delete TeamCityAgent
) ELSE (
	net stop TeamCityAgent
	sc delete TeamCityAgent
    nssm install TeamCityAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\teamcity\TeamCityAgent.bat
    net start TeamCityAgent
)
