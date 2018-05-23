REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jira
IF /I "%1%" == "UNINSTALL" (
	net stop JiraAgent
	sc delete JiraAgent
) ELSE (
	net stop JiraAgent
	sc delete JiraAgent
    nssm install JiraAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\jira\JiraAgent.bat
    net start JiraAgent
)