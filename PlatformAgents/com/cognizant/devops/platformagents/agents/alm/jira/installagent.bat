REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jira
nssm install JiraAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\jira\JiraAgent.bat
sleep 2
net start JiraAgent
