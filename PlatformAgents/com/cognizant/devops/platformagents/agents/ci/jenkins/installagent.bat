REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkins
nssm install JenkinsAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\jenkins\JenkinsAgent.bat
sleep 2
net start JenkinsAgent
