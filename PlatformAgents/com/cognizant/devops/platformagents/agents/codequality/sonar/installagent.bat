REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\sonar
IF /I "%1%" == "UNINSTALL" (
	net stop SonarAgent
	sc delete SonarAgent
) ELSE (
	net stop SonarAgent
	sc delete SonarAgent
    nssm install SonarAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\sonar\SonarAgent.bat
    net start SonarAgent
)
