REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkins
IF /I "%1%" == "UNINSTALL" (
	net stop JenkinsAgent
	sc delete JenkinsAgent
) ELSE (
	net stop JenkinsAgent
	sc delete JenkinsAgent
    nssm install JenkinsAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkins\JenkinsAgent.bat
    net start JenkinsAgent
)