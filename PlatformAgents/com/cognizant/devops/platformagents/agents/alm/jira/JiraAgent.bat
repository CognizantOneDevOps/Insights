pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jira
python -c "from com.cognizant.devops.platformagents.agents.alm.jira.JiraAgent import JiraAgent; JiraAgent()"