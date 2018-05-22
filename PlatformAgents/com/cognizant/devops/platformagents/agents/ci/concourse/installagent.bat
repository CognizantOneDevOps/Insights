REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\concourse
nssm install ConcourseAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\concourse\ConcourseAgent.bat
sleep 2
net start ConcourseAgent