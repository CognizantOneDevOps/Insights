pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkins
python -c "from com.cognizant.devops.platformagents.agents.ci.jenkins.JenkinsAgent import JenkinsAgent; JenkinsAgent()"