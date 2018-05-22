REM pushd %INSIGHTS_AGENT_HOME%\PlatformAgents\pivotalTracker
nssm install PivotalTrackerAgent %INSIGHTS_AGENT_HOME%\PlatformAgents\pivotalTracker\PivotalTrackerAgent.bat
sleep 2
net start PivotalTrackerAgent