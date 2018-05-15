REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkinslogparser
IF /I "%1%" == "UNINSTALL" (
	net stop JenkinsLogParserAgent
	sc delete JenkinsLogParserAgent
) ELSE (
	net stop JenkinsLogParserAgent
	sc delete JenkinsLogParserAgent
    nssm install JenkinsLogParserAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkinslogparser\JenkinsLogParserAgent.bat
    net start JenkinsLogParserAgent
)
